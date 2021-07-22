package com.serotonin.mango.util;

import com.serotonin.mango.Common;
import com.serotonin.mango.rt.event.AlarmLevels;
import com.serotonin.mango.rt.event.EventInstance;
import com.serotonin.mango.rt.event.handlers.EmailToSmsHandlerRT;
import com.serotonin.mango.rt.event.handlers.NotificationType;
import com.serotonin.mango.rt.event.type.DataPointEventType;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.web.email.MangoEmailContent;
import com.serotonin.mango.web.email.MangoTextContent;
import com.serotonin.mango.web.email.UsedImagesDirective;
import com.serotonin.util.StringUtils;
import com.serotonin.web.email.EmailInline;
import com.serotonin.web.i18n.LocalizableMessage;
import freemarker.template.TemplateException;
import org.scada_lts.dao.SystemSettingsDAO;
import org.scada_lts.utils.PlcAlarmsUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static com.serotonin.mango.util.SendMsgUtils.getDataPointMessage;

public final class EmailContentUtils {

    private EmailContentUtils(){}

    public static MangoTextContent createSmsContent(EventInstance evt, NotificationType notificationType, String alias) throws TemplateException, IOException {
        ResourceBundle bundle = Common.getBundle();
        String subject = getSubject(evt, notificationType, alias, bundle);
        Map<String, Object> model = createSmsModel(evt);
        return new MangoTextContent(notificationType.getFile(), model, bundle, subject, Common.UTF8);
    }

    public static MangoEmailContent createContent(EventInstance evt, NotificationType notificationType, String alias) throws TemplateException, IOException {
        ResourceBundle bundle = Common.getBundle();
        String subject = getSubject(evt, notificationType, alias, bundle);
        UsedImagesDirective inlineImages = new UsedImagesDirective();

        Map<String, Object> model = createModel(evt, inlineImages);
        MangoEmailContent content = new MangoEmailContent(notificationType.getFile(), model, bundle, subject,
                Common.UTF8);

        addInLineToContent(inlineImages, content);
        return content;
    }

    private static void addInLineToContent(UsedImagesDirective inlineImages, MangoEmailContent content) {
        for (String s : inlineImages.getImageList())
            content.addInline(new EmailInline.FileInline(s, Common.ctx.getServletContext().getRealPath(s)));
    }

    private static Map<String, Object> createModel(EventInstance evt, UsedImagesDirective inlineImages) {
        Map<String, Object> model = new HashMap<>();
        model.put("evt", evt);
        if (evt.getContext() != null)
            model.putAll(evt.getContext());
        model.put("img", inlineImages);
        model.put("instanceDescription", SystemSettingsDAO.getValue(SystemSettingsDAO.INSTANCE_DESCRIPTION));
        return model;
    }

    private static Map<String, Object> createSmsModel(EventInstance evt) {
        Map<String, Object> model = new HashMap<>();
        model.put("evt", evt);
        if (evt.getContext() != null)
            model.putAll(evt.getContext());
        return model;
    }

    private static String getSubject(EventInstance evt, NotificationType notificationType, String alias, ResourceBundle bundle) {

        if(evt.getEventType() instanceof DataPointEventType) {
            Map<String, Object> context = evt.getContext();
            DataPointVO dataPoint = (DataPointVO) context.get("point");
            if(isDataPointName(context, dataPoint) && evt.getMessage() != null) {
                if (isPlcAlarm(dataPoint)) {
                    LocalizableMessage subjectMsg;
                    if (notificationType instanceof EmailToSmsHandlerRT.SmsNotificationType) {
                        subjectMsg = evt.getShortMessage();
                    } else {
                        subjectMsg = evt.getMessage();
                    }
                    return evt.getPrettyActiveTimestamp() + " - " + subjectMsg.getLocalizedMessage(bundle);
                } else {
                    LocalizableMessage subjectMsg;
                    LocalizableMessage notifTypeMsg = new LocalizableMessage(notificationType.getKey());
                    subjectMsg = new LocalizableMessage("ftl.subject.default", evt.getPrettyActiveTimestamp(),
                            AlarmLevels.CODES.getCode(evt.getAlarmLevel()).toUpperCase(), getDataPointMessage(dataPoint), notifTypeMsg);
                    return subjectMsg.getLocalizedMessage(bundle);
                }
            }
        }

        // Determine the subject to use.
        LocalizableMessage subjectMsg;
        LocalizableMessage notifTypeMsg = new LocalizableMessage(notificationType.getKey());
        if (StringUtils.isEmpty(alias)) {
            subjectMsg = new LocalizableMessage("ftl.subject.default", evt.getPrettyActiveTimestamp(),
                    AlarmLevels.CODES.getCode(evt.getAlarmLevel()).toUpperCase(), "", notifTypeMsg);
        } else {
            subjectMsg = new LocalizableMessage("ftl.subject.alias", alias, notifTypeMsg);
        }

        return subjectMsg.getLocalizedMessage(bundle);
    }

    private static boolean isDataPointName(Map<String, Object> context, DataPointVO dataPoint) {
        return context != null && dataPoint != null && dataPoint.getName() != null;
    }

    private static boolean isPlcAlarm(DataPointVO dataPoint) {
        return PlcAlarmsUtils.getPlcAlarmLevelByDataPointName(dataPoint.getName()) != AlarmLevels.NONE;
    }
}
