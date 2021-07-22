package com.serotonin.mango.util;

import com.serotonin.mango.Common;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.event.EventInstance;
import com.serotonin.mango.rt.event.handlers.EmailHandlerRT;
import com.serotonin.mango.rt.event.handlers.EmailToSmsHandlerRT;
import com.serotonin.mango.rt.event.handlers.NotificationType;
import com.serotonin.mango.rt.event.type.SystemEventType;
import com.serotonin.mango.rt.maint.work.AfterWork;
import com.serotonin.mango.rt.maint.work.EmailWorkItem;
import com.serotonin.mango.rt.maint.work.EmailNotificationWorkItem;
import com.serotonin.mango.view.event.NoneEventRenderer;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.web.email.MangoEmailContent;
import com.serotonin.util.StringUtils;
import com.serotonin.web.i18n.LocalizableMessage;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public final class SendMsgUtils {

    private static final Log LOG = LogFactory.getLog(SendMsgUtils.class);

    private SendMsgUtils() {}

    public static boolean sendEmail(EventInstance evt, EmailHandlerRT.EmailNotificationType notificationType,
                                     Set<String> addresses, String alias, AfterWork afterWork) {
        try {

            SendEmailConfig sendEmailConfig = SendEmailConfig.newConfigFromSystemSettings();
            validateEmail(evt, notificationType, addresses, alias);

            if (evt.getEventType().isSystemMessage()) {
                if (((SystemEventType) evt.getEventType()).getSystemEventTypeId() == SystemEventType.TYPE_EMAIL_SEND_FAILURE) {
                    // Don't send email notifications about email send failures.
                    LOG.info("Not sending email for event raised due to email failure");
                    return false;
                }
            }

            MangoEmailContent content = EmailContentUtils.createContent(evt, notificationType, alias);
            String[] toAddrs = addresses.toArray(new String[0]);

            InternetAddress[] internetAddresses = SendMsgUtils.convertToInternetAddresses(toAddrs);
            validateAddresses(evt, notificationType, internetAddresses, alias);

            // Send the email.
            EmailNotificationWorkItem emailNotificationWorkItem = EmailNotificationWorkItem.newInstance(internetAddresses, content, afterWork, sendEmailConfig);
            emailNotificationWorkItem.execute();
            return true;

        } catch (Exception e) {
            LOG.error(MessageFormat.format("Info about email: {0}, StackTrace: {1}",
                    getInfoEmail(evt,notificationType,alias),
                    ExceptionUtils.getStackTrace(e)));
            return false;
        }
    }

    public static boolean sendSms(EventInstance evt, EmailToSmsHandlerRT.SmsNotificationType notificationType,
                                   Set<String> addresses, String alias, AfterWork afterWork) {
        try {

            SendEmailConfig sendEmailConfig = SendEmailConfig.newConfigFromSystemSettings();
            validateEmail(evt, notificationType, addresses, alias);

            if (evt.getEventType().isSystemMessage()) {
                if (((SystemEventType) evt.getEventType()).getSystemEventTypeId() == SystemEventType.TYPE_EMAIL_SEND_FAILURE) {
                    // Don't send email notifications about email send failures.
                    LOG.info("Not sending email for event raised due to email failure");
                    return false;
                }
            }
            MangoEmailContent content = EmailContentUtils.createSmsContent(evt, notificationType, alias);
            String[] toAddrs = addresses.toArray(new String[0]);

            InternetAddress[] internetAddresses = SendMsgUtils.convertToInternetAddresses(toAddrs);
            validateAddresses(evt, notificationType, internetAddresses, alias);

            // Send the email.
            EmailNotificationWorkItem emailNotificationWorkItem = EmailNotificationWorkItem.newInstance(internetAddresses, content, afterWork, sendEmailConfig);
            emailNotificationWorkItem.execute();
            return true;

        } catch (Exception e) {
            LOG.error(MessageFormat.format("Info about email: {0}, StackTrace: {1}",
                    getInfoEmail(evt,notificationType,alias),
                    ExceptionUtils.getStackTrace(e)));
            return false;
        }
    }

    public static boolean sendEmail(EventInstance evt, NotificationType notificationType, Set<String> addresses,
                                 String alias) {
        try {

            validateEmail(evt, notificationType, addresses, alias);

            if (evt.getEventType().isSystemMessage()) {
                if (((SystemEventType) evt.getEventType()).getSystemEventTypeId() == SystemEventType.TYPE_EMAIL_SEND_FAILURE) {
                    // Don't send email notifications about email send failures.
                    LOG.info("Not sending email for event raised due to email failure");
                    return false;
                }
            }
            String[] toAddrs = addresses.toArray(new String[0]);
            MangoEmailContent content = EmailContentUtils.createContent(evt, notificationType, alias);

            // Send the email.
            EmailWorkItem.queueEmail(toAddrs, content);
            return true;

        } catch (Exception e) {
            LOG.error(MessageFormat.format("Info about email: {0}, StackTrace: {1}",
                    getInfoEmail(evt,notificationType,alias),
                    ExceptionUtils.getStackTrace(e)));
            return false;
        }
    }

    public static boolean sendSms(EventInstance evt, NotificationType notificationType, Set<String> addresses,
                               String alias) {
        try {

            validateEmail(evt, notificationType, addresses, alias);

            if (evt.getEventType().isSystemMessage()) {
                if (((SystemEventType) evt.getEventType()).getSystemEventTypeId() == SystemEventType.TYPE_EMAIL_SEND_FAILURE) {
                    // Don't send email notifications about email send failures.
                    LOG.info("Not sending email for event raised due to email failure");
                    return false;
                }
            }

            MangoEmailContent content = EmailContentUtils.createSmsContent(evt, notificationType, alias);
            String[] toAddrs = addresses.toArray(new String[0]);

            EmailWorkItem.queueEmail(toAddrs, content);
            return true;

        } catch (Exception e) {
            LOG.error(MessageFormat.format("Info about email: {0}, StackTrace: {1}",
                    getInfoEmail(evt,notificationType,alias),
                    ExceptionUtils.getStackTrace(e)));
            return false;
        }
    }

    private static String getInfoEmail(EventInstance evt, NotificationType notificationType, String alias) {

        String messageInfoAlias = MessageFormat.format("Alias: {0} \n", alias);
        String messageInfoEmail = MessageFormat.format("Event: {0} \n", evt.getId());
        String messageInfoNotification = MessageFormat.format("Notification: {0} \n", notificationType.getKey());
        String subject = "";
        String messageExceptionWhenGetSubjectEmail = "";
        try {
            LocalizableMessage subjectMsg;
            LocalizableMessage notifTypeMsg = new LocalizableMessage(notificationType.getKey());
            if (StringUtils.isEmpty(alias)) {
                if (evt.getId() == Common.NEW_ID)
                    subjectMsg = new LocalizableMessage("ftl.subject.default.log", notifTypeMsg);
                else
                    subjectMsg = new LocalizableMessage("ftl.subject.default.id", notifTypeMsg, evt.getId());
            } else {
                if (evt.getId() == Common.NEW_ID)
                    subjectMsg = new LocalizableMessage("ftl.subject.alias", alias, notifTypeMsg);
                else
                    subjectMsg = new LocalizableMessage("ftl.subject.alias.id", alias, notifTypeMsg, evt.getId());
            }

            ResourceBundle bundle = Common.getBundle();
            subject = subjectMsg.getLocalizedMessage(bundle);
        } catch (Exception e) {
            messageExceptionWhenGetSubjectEmail =  MessageFormat.format("StackTrace for subjectMsg {0}",ExceptionUtils.getStackTrace(e));
        }

        String messages = new StringBuilder()
                .append(messageInfoEmail)
                .append(messageInfoNotification)
                .append(messageInfoAlias)
                .append(subject)
                .append(messageExceptionWhenGetSubjectEmail).toString();

        return messages;
    }

    private static void validateEmail(EventInstance evt, NotificationType notificationType, Set<String> addresses, String alias) throws Exception {

        String messageErrorEventInstance = "Event Instance null \n";
        String messageErrorNotyficationType = "Notification type is null \n";
        String messageErrorEmails = "Don't have e-mail \n";
        String messageErrorAlias = "Don't have alias\n";
        String messages = "";
        if (evt == null || evt.getEventType() == null) messages += messageErrorEventInstance;
        if (notificationType == null) messages += messageErrorNotyficationType;
        if (addresses == null || addresses.size() == 0) messages += messageErrorEmails;
        if (alias == null) messages += messageErrorAlias;

        if (messages.length() > 0) {
            throw new Exception(getInfoEmail(evt, notificationType, alias) + messages );
        }

    }

    private static void validateAddresses(EventInstance evt, NotificationType notificationType,
                                          InternetAddress[] internetAddresses, String alias) throws Exception {

        String messageErrorEmails = "Don't have e-mail \n";
        String messages = "";
        if (internetAddresses == null || internetAddresses.length == 0) messages += messageErrorEmails;

        if (!messages.isEmpty()) {
            throw new Exception(getInfoEmail(evt, notificationType, alias) + messages );
        }
    }

    public static InternetAddress[] convertToInternetAddresses(String[] toAddresses) {
        Set<InternetAddress> addresses = new HashSet<>();
        for (int i = 0; i < toAddresses.length; i++) {
            try {
                InternetAddress internetAddress = new InternetAddress(toAddresses[i]);
                addresses.add(internetAddress);
            } catch (AddressException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return addresses.toArray(new InternetAddress[]{});
    }

    public static String getDataPointMessage(DataPointVO dataPoint) {
        DataPointRT point = Common.ctx.getRuntimeManager().getDataPoint(dataPoint.getId());
        if (dataPoint.getEventTextRenderer() != null &&
                !dataPoint.getEventTextRenderer().getTypeName().equals(NoneEventRenderer.TYPE_NAME) &&
                dataPoint.getEventTextRenderer().getText(point.getPointValue().getValue()) != null &&
                (!dataPoint.getEventTextRenderer().getText(point.getPointValue().getValue()).equals(""))) {
            return " " + dataPoint.getEventTextRenderer().getText(point.getPointValue().getValue());
        }
        else if (dataPoint.getDescription() != null && !dataPoint.getDescription().equals(""))
            return " " + dataPoint.getDescription();
        else
            return " " + dataPoint.getName();
    }
}
