package org.scada_lts.web.mvc.api;

import com.serotonin.mango.Common;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.mailingList.MailingList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.scada_lts.dao.alarms.Scheduler;
import org.scada_lts.dao.mailingList.MailingListService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping(value = "/api/mailingList")
public class MailingListAPI {

    private static final Log LOG = LogFactory.getLog(MailingListAPI.class);

    @Resource
    private MailingListService mailingListService;

    @GetMapping(value = "/getMailingLists", produces = "application/json")
    public ResponseEntity<List<MailingList>> geMailingLists(HttpServletRequest request) {
        LOG.info("/api/mailingList/getMailingLists");
        try {
            User user = Common.getUser(request);
            if (user != null) {
                List<MailingList> result = mailingListService.getMailingLists();
                return new ResponseEntity<>(result, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
