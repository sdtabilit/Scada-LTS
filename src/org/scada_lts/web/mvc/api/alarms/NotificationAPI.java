package org.scada_lts.web.mvc.api.alarms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serotonin.mango.Common;
import com.serotonin.mango.vo.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.scada_lts.dao.alarms.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/api/alarms/notification")
public class NotificationAPI {

    private static final Log LOG = LogFactory.getLog(NotificationAPI.class);

    @Resource
    private NotificationService notificationService;

    @GetMapping(value = "/getAllSchedulers", produces = "application/json")
    public ResponseEntity<List<Scheduler>> getAllSchedulers(HttpServletRequest request) {
        LOG.info("/api/alarms/notification/getAllSchedulers");
        try {
            User user = Common.getUser(request);
            if (user != null) {
                List<Scheduler> result = notificationService.getAllSchedulers();
                return new ResponseEntity<>(result, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/getScheduler/{id}")
    public ResponseEntity<Scheduler> getScheduler(@PathVariable("id") Long id, HttpServletRequest request) {
        LOG.info("/api/alarms/notification/getScheduler/" + id);
        try {
            User user = Common.getUser(request);
            if (user != null) {
                Scheduler result = notificationService.getSchedulerById(id);
                return new ResponseEntity<>(result, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/setScheduler/{rangeId}/{notificationId}")
    public ResponseEntity<Scheduler> createScheduler(
            @PathVariable("rangeId") Long rangeId,
            @PathVariable("notificationId") Long notificationId,
            HttpServletRequest request) {
        LOG.info("/api/alarms/notification/setScheduler/" + rangeId + "/" + notificationId);
        try {
            User user = Common.getUser(request);
            if (user != null) {
                Scheduler response = notificationService.createScheduler(rangeId, notificationId);
                return new ResponseEntity<>(response, HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "/updateScheduler/{id}/{rangeId}/{notificationId}")
    public ResponseEntity<Scheduler> updateScheduler(
            @PathVariable("id") Long id,
            @PathVariable("rangeId") Long rangeId,
            @PathVariable("notificationId") Long notificationId,
            HttpServletRequest request) {
        LOG.info("/api/alarms/notification/updateScheduler/" + id + "/" + rangeId + "/" + notificationId);
        try {
            User user = Common.getUser(request);
            if (user != null) {
                Scheduler response = notificationService.updateScheduler(id, rangeId, notificationId);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(value = "/deleteScheduler/{id}")
    public ResponseEntity<String> deleteScheduler(@PathVariable("id") Long id, HttpServletRequest request) {
        LOG.info("/api/alarms/notification/deleteScheduler/" + id);
        try {
            User user = Common.getUser(request);
            if (user != null) {
                notificationService.deleteScheduler(id);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/getAllRanges", produces = "application/json")
    public ResponseEntity<List<Range>> getAllRanges(HttpServletRequest request) {
        LOG.info("/api/alarms/notification/getAllRanges");
        try {
            User user = Common.getUser(request);
            if (user != null) {
                List<Range> result = notificationService.getAllRanges();
                return new ResponseEntity<>(result, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/getRange/{id}")
    public ResponseEntity<Range> getRange(@PathVariable("id") Long id, HttpServletRequest request) {
        LOG.info("/api/alarms/notification/getScheduler/" + id);
        try {
            User user = Common.getUser(request);
            if (user != null) {
                Range result = notificationService.getRangeById(id);
                return new ResponseEntity<>(result, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/setRange", consumes = "application/json")
    public ResponseEntity<Range> setRange(HttpServletRequest request, @RequestBody Range range) {
        LOG.info("/api/alarms/notification/setRange");
        try {
            User user = Common.getUser(request);
            if (user != null) {
                Range result = notificationService.insertRange(range);
                return new ResponseEntity<>(result, HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "/updateRange", consumes = "application/json")
    public ResponseEntity<Range> updateRange(HttpServletRequest request, @RequestBody Range range) {
        LOG.info("/api/alarms/notification/updateRange");
        try {
            User user = Common.getUser(request);
            if (user != null) {
                Range response = notificationService.updateRange(range);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(value = "/deleteRange/{id}")
    public ResponseEntity<?> deleteRange(@PathVariable("id") Long id, HttpServletRequest request) {
        LOG.info("/api/alarms/notification/deleteRange/" + id);
        try {
            User user = Common.getUser(request);
            if (user != null) {
                notificationService.deleteRange(id);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/getAllNotifications", produces = "application/json")
    public ResponseEntity<List<Notification>> getAllNotifications(HttpServletRequest request) {
        LOG.info("/api/alarms/notification/getAllNotifications");
        try {
            User user = Common.getUser(request);
            if (user != null) {
                List<Notification> result = notificationService.getAllNotifications();
                return new ResponseEntity<>(result, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/setNotification", consumes = "application/json")
    public ResponseEntity<Notification> setNotification(HttpServletRequest request, @RequestBody Notification notification) {
        LOG.info("/api/alarms/notification/setNotification");
        try {
            User user = Common.getUser(request);
            if (user != null) {
                Notification result = notificationService.insertNotification(notification);
                return new ResponseEntity<>(result, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "/updateNotification", consumes = "application/json")
    public ResponseEntity<Notification> updateNotification(HttpServletRequest request, @RequestBody Notification notification) {
        LOG.info("/api/alarms/notification/updateNotification");
        try {
            User user = Common.getUser(request);
            if (user != null) {
                notificationService.updateNotification(notification);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/getSchedulers/dataPoint/{id}")
    public ResponseEntity<List<Scheduler>> getSchedulersForDataPoint(@PathVariable("id") Long id, HttpServletRequest request) {
        LOG.info("/api/alarms/notification/getSchedulers/dataPoint/" + id);
        try {
            User user = Common.getUser(request);
            if (user != null) {
                List<Scheduler> result = notificationService.getDataPointSchedulers(id);
                return new ResponseEntity<>(result, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/getSchedulers/user/{id}")
    public ResponseEntity<List<SchedulerUserData>> getSchedulersUserData(@PathVariable("id") Long id, HttpServletRequest request) {
        LOG.info("/api/alarms/notification/getSchedulers/user/" + id);
        try {
            User user = Common.getUser(request);
            if (user != null) {
                List<SchedulerUserData> result = notificationService.getAllSchedulerUserData(id);
                return new ResponseEntity<>(result, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/acknowledgeSchedulerNotification/{id}")
    public ResponseEntity<?> acknowledgeSchedulerNotification(@PathVariable("id") Long id, HttpServletRequest request) {
        LOG.info("/api/alarms/notification/acknowledgeSchedulerNotification/" + id);
        try {
            User user = Common.getUser(request);
            if (user != null) {
                notificationService.acknowledgeNotification(id);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/bindScheduler/{id}/user/{userId}")
    public ResponseEntity<?> bindSchedulersWithUser(
            @PathVariable("id") Long id,
            @PathVariable("userId") Long userId,
            HttpServletRequest request) {
        LOG.info("/api/alarms/notification/bindScheduler/"+id+"/user/" + userId);
        try {
            User user = Common.getUser(request);
            if (user != null) {
                notificationService.bindSchedulerWithUser(id, userId);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/bindScheduler/{id}/dp/{dpId}")
    public ResponseEntity<?> bindSchedulersWithDataPoint(
            @PathVariable("id") Long id,
            @PathVariable("dpId") Long dataPointId,
            HttpServletRequest request) {
        LOG.info("/api/alarms/notification/bindScheduler/"+id+"/dp/" + dataPointId);
        try {
            User user = Common.getUser(request);
            if (user != null) {
                notificationService.bindSchedulerWithDataPoint(id, dataPointId);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/setSchedulerWithNotification/{rangeId}/{userId}/{dpId}", consumes = "application/json")
    public ResponseEntity<?> createSchedulerWithNotification(
            @PathVariable("rangeId") Long rangeId,
            @PathVariable("userId") Long userId,
            @PathVariable("dpId") Long dataPointId,
            HttpServletRequest request,
            @RequestBody Notification notification) {
        LOG.info("/api/alarms/notification/setSchedulerWithNotification");
        try {
            User user = Common.getUser(request);
            if (user != null) {
                notificationService.createSchedulerWithUserAndDatapoint(notification, rangeId, userId, dataPointId);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/getMailingList/{id}")
    public ResponseEntity<List<MailingListPlcNotification>> getMailingListPlcNotification(@PathVariable("id") Long id, HttpServletRequest request) {
        LOG.info("/api/alarms/notification/getMailingList/" + id);
        try {
            User user = Common.getUser(request);
            if (user != null) {
                List<MailingListPlcNotification> result = notificationService.getMailingListPlcNotification(id);
                return new ResponseEntity<>(result, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/setMailingList", consumes = "application/json")
    public ResponseEntity<MailingListPlcNotification> setMailingListPlcNotification(HttpServletRequest request, @RequestBody MailingListPlcNotification notification) {
        LOG.info("/api/alarms/notification/setMailingList");
        try {
            User user = Common.getUser(request);
            if (user != null) {
                MailingListPlcNotification result = notificationService.createMailingListPlcNotification(notification);
                return new ResponseEntity<>(result, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "/updateMailingList", consumes = "application/json")
    public ResponseEntity<MailingListPlcNotification> updateMailingListPlcNotification(HttpServletRequest request, @RequestBody MailingListPlcNotification notification) {
        LOG.info("/api/alarms/notification/updateMailingList");
        try {
            User user = Common.getUser(request);
            if (user != null) {
                notificationService.updateMailingListPlcNotification(notification);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/getMailingListRecipients/{id}")
    public ResponseEntity<String> getMailingListRecipients(@PathVariable("id") Long id, HttpServletRequest request) {
        LOG.info("/api/alarms/notification/getMailingListRecipients/" + id);
        try {
            User user = Common.getUser(request);
            if (user != null) {
                String json;
                ObjectMapper mapper = new ObjectMapper();
                List<MailingListPlcNotification> dataPointsConfig = notificationService.getMailingListPlcNotification(id);
                List<Map<String, String>> mailingListRecipients = notificationService.getMailingListRecipients(id);
                Map<String, Object> map = new HashMap<>();
                map.put("mailingListId", id);
                map.put("dataPointsConfig", dataPointsConfig);
                map.put("mailingListRecipients", mailingListRecipients);
                json = mapper.writeValueAsString(map);

                return new ResponseEntity<>(json, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
