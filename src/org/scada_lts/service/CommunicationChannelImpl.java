package org.scada_lts.service;

import com.serotonin.mango.rt.event.EventInstance;
import com.serotonin.mango.util.IntervalUtil;
import com.serotonin.mango.vo.mailingList.MailingList;
import org.joda.time.DateTime;
import org.scada_lts.mango.service.SystemSettingsService;
import org.scada_lts.utils.EmailToSmsUtils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

class CommunicationChannelImpl implements CommunicationChannel {

    private final MailingList mailingList;
    private final CommunicationChannelTypable type;
    private final SystemSettingsService systemSettingsService;


    public CommunicationChannelImpl(MailingList mailingList,
                                    CommunicationChannelTypable type,
                                    SystemSettingsService systemSettingsService) {
        this.mailingList = mailingList;
        this.type = type;
        this.systemSettingsService = systemSettingsService;

    }

    @Override
    public int getChannelId() {
        return mailingList.getId();
    }

    @Override
    public int getDailyLimitSentNumber() {
        return mailingList.getDailyLimitSentEmailsNumber();
    }

    @Override
    public String getSendingActivationCron() {
        return mailingList.getCronPattern();
    }

    @Override
    public boolean isCollectInactiveEvents() {
        return mailingList.isCollectInactiveEmails();
    }

    @Override
    public boolean isActiveFor(DateTime fireTime) {
        return IntervalUtil.isActiveByInterval(mailingList, fireTime);
    }

    @Override
    public boolean isActiveFor(EventInstance event) {
        return IntervalUtil.isActiveByInterval(mailingList, event);
    }

    @Override
    public Set<String> getActiveAdresses(DateTime fireTime) {
        Set<String> adresses = new HashSet<>();
        mailingList.appendAddresses(adresses, fireTime, type);
        return addedAtDomainForSms(adresses);
    }

    @Override
    public Set<String> getActiveAdresses(EventInstance event) {
        Set<String> adresses = new HashSet<>();
        DateTime fireTime = new DateTime(event.getActiveTimestamp());
        mailingList.appendAddresses(adresses, fireTime, type);
        return addedAtDomainForSms(adresses);
    }

    @Override
    public Set<String> getAllAdresses() {
        Set<String> adresses = new HashSet<>();
        mailingList.appendAllAddresses(adresses, type);
        return addedAtDomainForSms(adresses);
    }

    @Override
    public CommunicationChannelTypable getType() {
        return type;
    }

    @Override
    public boolean isDailyLimitSent() {
        return mailingList.isDailyLimitSentEmails();
    }

    @Override
    public MailingList getData() {
        return mailingList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommunicationChannelImpl)) return false;
        CommunicationChannelImpl that = (CommunicationChannelImpl) o;
        return getChannelId() == that.getChannelId() &&
                getType() == that.getType();
    }

    @Override
    public int hashCode() {

        return Objects.hash(getChannelId(), getType());
    }

    @Override
    public String toString() {
        return "CommunicationChannelImpl{" +
                "mailingList=" + mailingList +
                ", type=" + type +
                '}';
    }

    private Set<String> addedAtDomainForSms(Set<String> adresses) {
        if(CommunicationChannelType.SMS.equals(type))
            return EmailToSmsUtils.addedAtDomain(adresses, systemSettingsService.getSMSDomain());
        return adresses;
    }
}