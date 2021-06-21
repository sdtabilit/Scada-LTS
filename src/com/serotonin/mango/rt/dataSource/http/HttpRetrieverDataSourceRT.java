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
package com.serotonin.mango.rt.dataSource.http;

import com.serotonin.db.KeyValuePair;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import com.serotonin.mango.Common;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.SetPointSource;
import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.rt.dataSource.DataSourceRT;
import com.serotonin.mango.rt.dataSource.DataSourceUtils;
import com.serotonin.mango.rt.dataSource.NoMatchException;
import com.serotonin.mango.rt.dataSource.PollingDataSource;
import com.serotonin.mango.vo.dataSource.http.HttpRetrieverDataSourceVO;
import com.serotonin.web.http.HttpUtils;
import com.serotonin.web.i18n.LocalizableException;
import com.serotonin.web.i18n.LocalizableMessage;
import org.scada_lts.ds.StartStopDsRT;
import org.scada_lts.ds.model.ReactivationDs;
import org.scada_lts.ds.reactivation.ReactivationManager;
import org.scada_lts.ds.reactivation.ReactivationConnectHttpRetriever;
import org.scada_lts.ds.state.SleepStateDs;
import org.scada_lts.ds.state.StopChangeEnableStateDs;

import java.util.List;

/**
 * @author Matthew Lohbihler
 */
public class HttpRetrieverDataSourceRT extends PollingDataSource {
    private static final int READ_LIMIT = 1024 * 1024; // One MB

    public static final int DATA_RETRIEVAL_FAILURE_EVENT = 1;
    public static final int PARSE_EXCEPTION_EVENT = 2;

    private final HttpRetrieverDataSourceVO vo;

    public HttpRetrieverDataSourceRT(HttpRetrieverDataSourceVO vo) {
        super(vo);
        setPollingPeriod(vo.getUpdatePeriodType(), vo.getUpdatePeriods(), false);
        this.vo = vo;
    }

    @Override
    public void removeDataPoint(DataPointRT dataPoint) {
        returnToNormal(PARSE_EXCEPTION_EVENT, System.currentTimeMillis());
        super.removeDataPoint(dataPoint);
    }

    @Override
    public void setPointValue(DataPointRT dataPoint, PointValueTime valueTime, SetPointSource source) {
        // no op
    }

    @Override
    protected void doPoll(long time) {
        String data;
        try {
            data = getData(vo.getUrl(), vo.getTimeoutSeconds(), vo.getRetries(), vo.isStop(), vo.getReactivation(), vo.getStaticHeaders());
        } catch (Exception e) {
            LocalizableMessage lm;
            if (e instanceof LocalizableException)
                lm = ((LocalizableException) e).getLocalizableMessage();
            else
                lm = new LocalizableMessage("event.httpRetriever.retrievalError", vo.getUrl(), e.getMessage());
            raiseEvent(DATA_RETRIEVAL_FAILURE_EVENT, time, true, lm);
            return;
        }

        // If we made it this far, everything is good.
        returnToNormal(DATA_RETRIEVAL_FAILURE_EVENT, time);

        // We have the data. Now run the regex.
        LocalizableMessage parseErrorMessage = null;
        for (DataPointRT dp : dataPoints) {
            HttpRetrieverPointLocatorRT locator = dp.getPointLocator();

            try {
                // Get the value
                MangoValue value = DataSourceUtils.getValue(locator.getValuePattern(), data, locator.getDataTypeId(),
                        locator.getBinary0Value(), dp.getVO().getTextRenderer(), locator.getValueFormat(), dp.getVO()
                                .getName());

                // Get the time.
                long valueTime = DataSourceUtils.getValueTime(time, locator.getTimePattern(), data,
                        locator.getTimeFormat(), dp.getVO().getName());

                // Save the new value
                dp.updatePointValue(new PointValueTime(value, valueTime));
            } catch (NoMatchException e) {
                if (!locator.isIgnoreIfMissing()) {
                    if (parseErrorMessage == null)
                        parseErrorMessage = e.getLocalizableMessage();
                }
            } catch (LocalizableException e) {
                if (parseErrorMessage == null)
                    parseErrorMessage = e.getLocalizableMessage();
            }
        }

        if (parseErrorMessage != null)
            raiseEvent(PARSE_EXCEPTION_EVENT, time, false, parseErrorMessage);
        else
            returnToNormal(PARSE_EXCEPTION_EVENT, time);
    }

    private static GetMethod createMethodForClient(String url, List<KeyValuePair> staticHeaders) {
        GetMethod method = new GetMethod(url);
        if (!staticHeaders.isEmpty()) {
            for (KeyValuePair kvp : staticHeaders) {
                if (kvp.getKey().equals("Authorization")) {
                    method.setDoAuthentication(true);
                    method.addRequestHeader(kvp.getKey(), kvp.getValue());
                }
            }
        }
        return method;
    }

    public static boolean testConnection(String url, int timeoutSeconds, int retries, List<KeyValuePair> staticHeaders) {
        String data = "";
        for (int i = 0; i <= retries; i++) {
            HttpClient client = Common.getHttpClient(timeoutSeconds * 1000);
            GetMethod method = null;
            LocalizableMessage message;
            try {
                method = createMethodForClient(url, staticHeaders);
                int responseCode = client.executeMethod(method);
                if (responseCode == HttpStatus.SC_OK) {
                    data = HttpUtils.readResponseBody(method, READ_LIMIT);
                    return true;
                }
                message = new LocalizableMessage("event.http.response", url, responseCode);
            } catch (Exception e) {
                message = DataSourceRT.getExceptionMessage(e);
            } finally {
                if (method != null)
                    method.releaseConnection();
            }
        }
        return false;
    }

    public String getData(String url, int timeoutSeconds, int retries, boolean stop, ReactivationDs r, List<KeyValuePair> staticHeaders) throws LocalizableException {
        String data = "";
        for (int i = 0; i <= retries; i++) {
            HttpClient client = Common.getHttpClient(timeoutSeconds * 1000);
            GetMethod method = null;
            LocalizableMessage message;
            try {
                method = createMethodForClient(url, staticHeaders);
                int responseCode = client.executeMethod(method);
                if (responseCode == HttpStatus.SC_OK) {
                    data = HttpUtils.readResponseBody(method, READ_LIMIT);
                    break;
                }
                message = new LocalizableMessage("event.http.response", url, responseCode);
            } catch (Exception e) {
                message = DataSourceRT.getExceptionMessage(e);
            } finally {
                if (method != null)
                    method.releaseConnection();
            }

            if (retries == i && stop) {
                StartStopDsRT stopDsRT = new StartStopDsRT(vo.getId(), false, new StopChangeEnableStateDs());
                new Thread(stopDsRT).start();
            } else if (retries == i && r.isSleep()) {
                ReactivationConnectHttpRetriever rhr = new ReactivationConnectHttpRetriever();
                ReactivationManager.getInstance().addProcess(rhr, r, vo);
                StartStopDsRT stopDsRT = new StartStopDsRT(vo.getId(),false, new SleepStateDs());
                new Thread(stopDsRT).start();
            }
            else if (retries == i) {
                throw new LocalizableException(message);
            }
        }
        return data;
    }

    @Deprecated
    public static String getData(String url, int timeoutSeconds, int retries, List<KeyValuePair> staticHeaders) throws LocalizableException {
        // Try to get the data.
        String data;
        while (true) {
            HttpClient client = Common.getHttpClient(timeoutSeconds * 1000);
            GetMethod method = null;
            LocalizableMessage message;

            try {
                method = createMethodForClient(url, staticHeaders);
                int responseCode = client.executeMethod(method);
                if (responseCode == HttpStatus.SC_OK) {
                    data = HttpUtils.readResponseBody(method, READ_LIMIT);
                    break;
                }
                message = new LocalizableMessage("event.http.response", url, responseCode);
            }
            catch (Exception e) {
                message = DataSourceRT.getExceptionMessage(e);
            }
            finally {
                if (method != null)
                    method.releaseConnection();
            }

            if (retries <= 0)
                throw new LocalizableException(message);
            retries--;

            // Take a little break instead of trying again immediately.
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                // no op
            }
        }

        return data;
    }
}
