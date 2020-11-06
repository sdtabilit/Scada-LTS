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
package com.serotonin.mango.util;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

import com.serotonin.mango.Common;
import org.scada_lts.dao.SystemSettingsDAO;
import com.serotonin.mango.rt.maint.VersionCheck;

/**
 * @author Matthew Lohbihler
 */
public class MangoGroveLogAppender extends AppenderSkeleton {

    private final static Log LOG = LogFactory.getLog(MangoGroveLogAppender.class);
    @Override
    protected void append(LoggingEvent event) {
        // In spite of what the configuration file says, we don't care about anything less than an error.
        if (!event.getLevel().isGreaterOrEqual(Level.ERROR))
            return;

        // Check the logging property setting.
        try {
            if (!SystemSettingsDAO.getBooleanValue(SystemSettingsDAO.GROVE_LOGGING, false))
                return;
        }
        catch (Throwable t) {
            // If anything bad happens while trying to figure out if we should log, just fuggetabowit.
            return;
        }

        HttpClient client = Common.getHttpClient();
        PostMethod method = new PostMethod(Common.getGroveUrl(Common.GroveServlets.MANGO_LOG));
        method.addParameter("productId", "Scada-LTS");
        method.addParameter("productVersion", Common.getVersion());
//        method.addParameter("ts", Long.toString(event.timeStamp));
        method.addParameter("level", event.getLevel().toString());
        method.addParameter("message", event.getRenderedMessage());

        String[] throwableStrRep = event.getThrowableStrRep();
        if (throwableStrRep != null) {
            StringBuilder throwable = new StringBuilder();
            for (String s : throwableStrRep)
                throwable.append(s).append("\r\n");
            method.addParameter("throwable", throwable.toString());
        }

        method.addParameter("clazz", event.getLocationInformation().getClassName());
        method.addParameter("method", event.getLocationInformation().getMethodName());
        method.addParameter("file", event.getLocationInformation().getFileName());
        method.addParameter("line", event.getLocationInformation().getLineNumber());

        try {
            int responseCode = client.executeMethod(method);
            if (responseCode != HttpStatus.SC_OK)
                LOG.error("Invalid response code: " + responseCode);
//                LogLog.error("Invalid response code: " + responseCode);
        }
        catch (HttpException e) {
            LOG.error("Error sending log event to grove", e);
//            LogLog.error("Error sending log event to grove", e);
        }
        catch (IOException e) {
            LOG.error("Error sending log event to grove", e);
//            LogLog.error("Error sending log event to grove", e);
        }
    }

    @Override
    public void close() {
        // no op
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
