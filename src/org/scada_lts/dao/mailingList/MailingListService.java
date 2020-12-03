package org.scada_lts.dao.mailingList;

import com.serotonin.mango.vo.mailingList.MailingList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MailingListService {

    private static final Log LOG = LogFactory.getLog(MailingListService.class);

    private MailingListDAO mailingListDAO = new MailingListDAO();

    public List<MailingList> getMailingLists() {
        return mailingListDAO.getMailingLists();
    }
}
