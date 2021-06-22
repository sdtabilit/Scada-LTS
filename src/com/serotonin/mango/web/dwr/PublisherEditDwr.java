/*
    Mango - Open Source M2M - http://mango.serotoninsoftware.com
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc.
    @author Matthew Lohbihler
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.mango.web.dwr;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.serotonin.db.KeyValuePair;
import com.serotonin.mango.Common;
import com.serotonin.mango.DataTypes;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.rt.publish.persistent.PersistentSenderRT;
import com.serotonin.mango.vo.DataPointExtendedNameComparator;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.publish.PublishedPointVO;
import com.serotonin.mango.vo.publish.PublisherVO;
import com.serotonin.mango.vo.publish.httpSender.HttpPointVO;
import com.serotonin.mango.vo.publish.httpSender.HttpSenderVO;
import com.serotonin.mango.vo.publish.pachube.PachubePointVO;
import com.serotonin.mango.vo.publish.pachube.PachubeSenderVO;
import com.serotonin.mango.vo.publish.persistent.PersistentPointVO;
import com.serotonin.mango.vo.publish.persistent.PersistentSenderVO;
import com.serotonin.mango.web.dwr.beans.HttpSenderTester;
import com.serotonin.util.StringUtils;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.web.i18n.LocalizableMessage;

/**
 * @author Matthew Lohbihler
 */
public class PublisherEditDwr extends BaseDwr {
    private DwrResponseI18n trySave(PublisherVO<? extends PublishedPointVO> p) {
        DwrResponseI18n response = new DwrResponseI18n();

        p.validate(response);
        if (!response.getHasMessages()) {
            Common.ctx.getRuntimeManager().savePublisher(p);
            response.addData("id", p.getId());
        }

        return response;
    }

    public void cancelTestingUtility() {
        Common.getUser().cancelTestingUtility();
    }

    public DwrResponseI18n initSender() {
        List<DataPointVO> allPoints = new DataPointDao().getDataPoints(DataPointExtendedNameComparator.instance, false);

        // Remove image points
        Iterator<DataPointVO> iter = allPoints.iterator();
        while (iter.hasNext()) {
            DataPointVO dp = iter.next();
            if (dp.getPointLocator().getDataTypeId() == DataTypes.IMAGE)
                iter.remove();
        }

        DwrResponseI18n response = new DwrResponseI18n();
        response.addData("publisher", Common.getUser().getEditPublisher());
        response.addData("allPoints", allPoints);
        return response;
    }

    //
    //
    // HTTP sender stuff
    //
    public DwrResponseI18n saveHttpSender(String name, String xid, boolean enabled, List<HttpPointVO> points,
            String url, boolean usePost, List<KeyValuePair> staticHeaders, List<KeyValuePair> staticParameters,
            int cacheWarningSize, boolean changesOnly, boolean raiseResultWarning, int dateFormat,
            boolean sendSnapshot, int snapshotSendPeriods, int snapshotSendPeriodType, String username, String password,
                                          boolean useJSON) {
        HttpSenderVO p = (HttpSenderVO) Common.getUser().getEditPublisher();

        p.setName(name);
        p.setXid(xid);
        p.setEnabled(enabled);
        p.setPoints(points);
        p.setUrl(url);
        p.setUsePost(usePost);
        p.setStaticHeaders(staticHeaders);
        p.setStaticParameters(staticParameters);
        p.setCacheWarningSize(cacheWarningSize);
        p.setChangesOnly(changesOnly);
        p.setRaiseResultWarning(raiseResultWarning);
        p.setDateFormat(dateFormat);
        p.setSendSnapshot(sendSnapshot);
        p.setSnapshotSendPeriods(snapshotSendPeriods);
        p.setSnapshotSendPeriodType(snapshotSendPeriodType);
        setAuthorizationStaticHeader(p, username, password);
        setContentTypeJsonStaticHeader(p, useJSON);

        return trySave(p);
    }

    private static void setContentTypeJsonStaticHeader(HttpSenderVO httpSenderVO, boolean useJSON) {
        if (useJSON && (httpSenderVO.getStaticHeaders().isEmpty() || !containsKey(httpSenderVO.getStaticHeaders(), "Content-Type"))) {
            httpSenderVO.getStaticHeaders().add(new KeyValuePair("Content-Type", "application/json"));
        } else {
            httpSenderVO.getStaticHeaders().removeIf(kvp -> (kvp.getKey().equalsIgnoreCase("Content-Type") &&
                    kvp.getValue().equalsIgnoreCase("application/json")));
        }
    }

    private static void setAuthorizationStaticHeader(HttpSenderVO httpSenderVO, String username, String password) {
        toBasicCredentials(username, password).ifPresent(headerValue -> {
            if (httpSenderVO.getStaticHeaders().isEmpty() || !containsKey(httpSenderVO.getStaticHeaders(), "Authorization")) {
                httpSenderVO.getStaticHeaders().add(new KeyValuePair("Authorization", headerValue));
            } else {
                for (KeyValuePair kvp : httpSenderVO.getStaticHeaders()) {
                    if (kvp.getKey().equalsIgnoreCase("Authorization")) {
                        kvp.setValue(headerValue);
                    }
                }
            }
        });
    }

    private static boolean containsKey(List<KeyValuePair> staticHeaders, String key) {
        for (KeyValuePair kvp : staticHeaders) {
            if (kvp.getKey().equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

    private static Optional<String> toBasicCredentials(String username, String password) {
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            byte[] credentials = (username + ':' + password).getBytes();
            return Optional.of("Basic " + Base64.getEncoder().encodeToString(credentials));
        }
        return Optional.empty();
    }

    public static String[] getBasicCredentials(List<KeyValuePair> staticHeaders) {
        return getAuthorization(staticHeaders)
                .filter(authorization -> authorization.startsWith("Basic")
                        || authorization.startsWith("basic"))
                .map(authorization -> {
                    String base64Credentials = authorization.substring("Basic".length()).trim();
                    byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
                    String credentials = new String(credDecoded, StandardCharsets.UTF_8);
                    // credentials = username:password
                    return credentials.split(":", 2);
                })
                .orElseGet(() -> new String[]{});
    }

    private static Optional<String> getAuthorization(List<KeyValuePair> staticHeaders) {
        for (KeyValuePair kvp : staticHeaders) {
            if (kvp.getKey().equalsIgnoreCase("Authorization")) {
                return Optional.ofNullable(kvp.getValue());
            }
        }
        return Optional.empty();
    }

    public boolean getIsUseJSON() {
        HttpSenderVO p = (HttpSenderVO) Common.getUser().getEditPublisher();

        return p.isUseJSON();
    }

    public void httpSenderTest(String url, boolean usePost, List<KeyValuePair> staticHeaders,
            List<KeyValuePair> staticParameters) {
        Common.getUser().setTestingUtility(new HttpSenderTester(url, usePost, staticHeaders, staticParameters));
    }

    public String httpSenderTestUpdate() {
        HttpSenderTester test = Common.getUser().getTestingUtility(HttpSenderTester.class);
        if (test == null)
            return null;
        return test.getResult();
    }

    //
    //
    // Pachube sender stuff
    //
    public DwrResponseI18n savePachubeSender(String name, String xid, boolean enabled, List<PachubePointVO> points,
            String apiKey, int timeoutSeconds, int retries, int cacheWarningSize, boolean changesOnly,
            boolean sendSnapshot, int snapshotSendPeriods, int snapshotSendPeriodType) {
        PachubeSenderVO p = (PachubeSenderVO) Common.getUser().getEditPublisher();

        p.setName(name);
        p.setXid(xid);
        p.setEnabled(enabled);
        p.setPoints(points);
        p.setApiKey(apiKey);
        p.setTimeoutSeconds(timeoutSeconds);
        p.setRetries(retries);
        p.setCacheWarningSize(cacheWarningSize);
        p.setChangesOnly(changesOnly);
        p.setSendSnapshot(sendSnapshot);
        p.setSnapshotSendPeriods(snapshotSendPeriods);
        p.setSnapshotSendPeriodType(snapshotSendPeriodType);

        return trySave(p);
    }

    //
    //
    // Persistent sender stuff
    //
    public DwrResponseI18n savePersistentSender(String name, String xid, boolean enabled,
            List<PersistentPointVO> points, String host, int port, String authorizationKey, String xidPrefix,
            int syncType, int cacheWarningSize, boolean changesOnly, boolean sendSnapshot, int snapshotSendPeriods,
            int snapshotSendPeriodType) {
        PersistentSenderVO p = (PersistentSenderVO) Common.getUser().getEditPublisher();

        p.setName(name);
        p.setXid(xid);
        p.setEnabled(enabled);
        p.setPoints(points);
        p.setHost(host);
        p.setPort(port);
        p.setAuthorizationKey(authorizationKey);
        p.setXidPrefix(xidPrefix);
        p.setSyncType(syncType);
        p.setCacheWarningSize(cacheWarningSize);
        p.setChangesOnly(changesOnly);
        p.setSendSnapshot(sendSnapshot);
        p.setSnapshotSendPeriods(snapshotSendPeriods);
        p.setSnapshotSendPeriodType(snapshotSendPeriodType);

        return trySave(p);
    }

    public DwrResponseI18n getPersistentSenderStatus() {
        PersistentSenderVO p = (PersistentSenderVO) Common.getUser().getEditPublisher();
        PersistentSenderRT rt = (PersistentSenderRT) Common.ctx.getRuntimeManager().getRunningPublisher(p.getId());

        DwrResponseI18n response = new DwrResponseI18n();
        if (rt == null)
            response.addGenericMessage("publisherEdit.persistent.status.notEnabled");
        else {
            response.addGenericMessage("publisherEdit.persistent.status.pointCount", rt.getPointCount());
            response.addGenericMessage("publisherEdit.persistent.status.queueSize", rt.getQueueSize());
            if (rt.getConnectingIndex() != -1)
                response.addGenericMessage("publisherEdit.persistent.status.connectionState", new LocalizableMessage(
                        "publisherEdit.persistent.status.connecting", rt.getConnectingIndex(), rt.getPointCount()));
            else if (rt.isConnected())
                response.addGenericMessage("publisherEdit.persistent.status.connectionState", new LocalizableMessage(
                        "publisherEdit.persistent.status.connected"));
            else
                response.addGenericMessage("publisherEdit.persistent.status.connectionState", new LocalizableMessage(
                        "publisherEdit.persistent.status.notConnected"));
            response.addGenericMessage("publisherEdit.persistent.status.packetQueueSize", rt.getPacketsToSend());

            int syncStatus = rt.getSyncStatus();
            if (syncStatus == -1)
                response.addGenericMessage("publisherEdit.persistent.status.syncNotRunning");
            else
                response.addGenericMessage("publisherEdit.persistent.status.syncStatus", syncStatus,
                        rt.getPointCount(), rt.getSyncRequestsSent());
        }

        return response;
    }

    public DwrResponseI18n startPersistentSync() {
        PersistentSenderVO p = (PersistentSenderVO) Common.getUser().getEditPublisher();
        PersistentSenderRT rt = (PersistentSenderRT) Common.ctx.getRuntimeManager().getRunningPublisher(p.getId());

        DwrResponseI18n response = new DwrResponseI18n();
        if (rt == null)
            response.addGenericMessage("publisherEdit.persistent.status.notEnabled");
        else if (rt.startSync())
            response.addGenericMessage("publisherEdit.persistent.syncStarted");
        else
            response.addGenericMessage("publisherEdit.persistent.syncNotStarted");

        return response;
    }
}
