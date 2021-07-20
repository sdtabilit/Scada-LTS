package org.scada_lts.web.mvc.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serotonin.mango.Common;
import com.serotonin.mango.vo.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.scada_lts.mango.service.SystemSettingsService;
import org.scada_lts.utils.SystemSettingsUtils;
import org.scada_lts.utils.ValidationUtils;
import org.scada_lts.web.mvc.api.json.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Controller for SystemSettings page
 *
 * @author Radoslaw Jajko
 */
@Controller
@RequestMapping(path = "/api/systemSettings")
public class SystemSettingsAPI {

    private static final Log LOG = LogFactory.getLog(SystemSettingsAPI.class);

    private static final String SAVED_MSG = "saved";

    @Resource
    private SystemSettingsService systemSettingsService;

    @RequestMapping(value = "/getSettings", method = RequestMethod.GET)
    public ResponseEntity<String> getSettings(HttpServletRequest request) {
        LOG.info("/api/systemSettings/getSettings");
        try {
            User user = Common.getUser(request);
            if (user != null && user.isAdmin()) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    String json = mapper.writeValueAsString(systemSettingsService.getSettings());
                    return new ResponseEntity<>(json, HttpStatus.OK);
                } catch (JsonProcessingException e) {
                    LOG.error(e);
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/getDefaultLoggingType", produces = "application/json")
    public ResponseEntity<String> getLoggingType(HttpServletRequest request) {
        LOG.info("/api/systemSettings/getDefaultLoggingType");
        try {
            User user = Common.getUser(request);
            if (user != null && user.isAdmin()) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    String json = mapper.writeValueAsString(systemSettingsService.getDefaultLoggingType());
                    return new ResponseEntity<>(json, HttpStatus.OK);
                } catch (JsonProcessingException e) {
                    LOG.error(e);
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/saveDefaultLoggingType/{defaultLoggingType}")
    public ResponseEntity<String> saveLoggingType(HttpServletRequest request, @PathVariable("defaultLoggingType") String defaultLoggingType) {
        LOG.info("/api/systemSettings/saveLoggingType");
        try {
            User user = Common.getUser(request);
            if (user != null && user.isAdmin()) {
                systemSettingsService.setDefaultLoggingType(defaultLoggingType);
                return new ResponseEntity<>(SAVED_MSG, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/getEmail", produces = "application/json")
    public ResponseEntity<JsonSettingsEmail> getEmail(HttpServletRequest request) {
        LOG.info("/api/systemSettings/getEmail");
        try {
            User user = Common.getUser(request);
            if (user != null && user.isAdmin()) {
                return new ResponseEntity<>(systemSettingsService.getEmailSettings(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/saveEmail", consumes = "application/json")
    public ResponseEntity<String> saveEmail(HttpServletRequest request, @RequestBody JsonSettingsEmail jsonSettingsEmail) {
        LOG.info("/api/systemSettings/saveEmail");
        try {
            User user = Common.getUser(request);
            if (user != null && user.isAdmin()) {
                systemSettingsService.saveEmailSettings(jsonSettingsEmail);
                return new ResponseEntity<>(SAVED_MSG, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/getSMSDomain", produces = "application/json")
    public ResponseEntity<String> getSMSDomain(HttpServletRequest request) {
        LOG.info("/api/systemSettings/getSMSDomain");
        try {
            User user = Common.getUser(request);
            if (user != null && user.isAdmin()) {
                return new ResponseEntity<>(systemSettingsService.getSMSDomain(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/saveSMSDomain/{parts_of_domain}", method = RequestMethod.POST)
    public ResponseEntity<String> saveSMSDomain(
            @PathVariable("parts_of_domain") String[] parts_of_domain,
            HttpServletRequest request)  {

        String domain = String.join(".", parts_of_domain);

        LOG.info("/api/systemSettings/getSMSDomain:" + domain);
        ResponseEntity<String> result = null;
        try {
            User user = Common.getUser(request);
            if (user.isAdmin()) {
                systemSettingsService.saveSMSDomain(domain);
                result = new ResponseEntity<String>(HttpStatus.OK);
            } else {
                result = new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            result = new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
        return result;
    }

    @PostMapping(value = "/saveSMSDomain", consumes = {"text/plain", "application/*"})
    public ResponseEntity<String> saveSMSDomainPost(HttpServletRequest request, @RequestBody JsonSettingsSmsDomain smsDomain) {
        LOG.info("/api/systemSettings/saveSMSDomain");
        try {
            User user = Common.getUser(request);
            if (user != null && user.isAdmin()) {
                systemSettingsService.saveSMSDomain(smsDomain.getDomainName());
                return new ResponseEntity<>(SAVED_MSG, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/sendTestEmail", produces = "application/json")
    public ResponseEntity<String> sendTestEmail(HttpServletRequest request) {
        LOG.info("/api/systemSettings/sendTestEmail");
        try {
            User user = Common.getUser(request);
            if (user != null && user.isAdmin()) {
                try {
                    String response = systemSettingsService.sendTestEmail(user);
                    return new ResponseEntity<>(response, HttpStatus.OK);
                } catch (Exception e) {
                    LOG.error(e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/getHttp", produces = "application/json")
    public ResponseEntity<JsonSettingsHttp> getHttp(HttpServletRequest request) {
        LOG.info("/api/systemSettings/getHttp");
        try {
            User user = Common.getUser(request);
            if (user != null && user.isAdmin()) {
                return new ResponseEntity<>(systemSettingsService.getHttpSettings(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/saveHttp", consumes = "application/json")
    public ResponseEntity<String> saveHttp(HttpServletRequest request, @RequestBody JsonSettingsHttp jsonSettingsHttp) {
        LOG.info("/api/systemSettings/saveHttp");
        try {
            User user = Common.getUser(request);
            if (user != null && user.isAdmin()) {
                systemSettingsService.saveHttpSettings(jsonSettingsHttp);
                return new ResponseEntity<>(SAVED_MSG, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/getMisc", produces = "application/json")
    public ResponseEntity<JsonSettingsMisc> getMisc(HttpServletRequest request) {
        LOG.info("/api/systemSettings/getMisc");
        try {
            User user = Common.getUser(request);
            if (user != null && user.isAdmin()) {
                return new ResponseEntity<>(systemSettingsService.getMiscSettings(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/saveMisc", consumes = "application/json")
    public ResponseEntity<String> saveMisc(HttpServletRequest request, @RequestBody JsonSettingsMisc jsonSettingsMisc) {
        LOG.info("/api/systemSettings/saveMisc");
        try {
            User user = Common.getUser(request);
            if (user != null && user.isAdmin()) {
                systemSettingsService.saveMiscSettings(jsonSettingsMisc);
                return new ResponseEntity<>(SAVED_MSG, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/getAuditEventAlarmLevels", produces = "application/json")
    public ResponseEntity<List<JsonSettingsEventLevels>> getAuditEventAlarmLevels(HttpServletRequest request) {
        LOG.info("/api/systemSettings/getAuditEventAlarmLevels");
        try {
            User user = Common.getUser(request);
            if (user != null && user.isAdmin()) {
                return new ResponseEntity<>(systemSettingsService.getAuditEventAlarmLevels(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * @param request          - request with user data
     * @param eventAlarmLevels - Audit Events with ID from 1 to 8 (AuditEventType)
     *                         with alarm levels from 0 to 4 (AlarmLevels)
     * @return Response with HTTP status
     */
    @PostMapping(value = "/saveAuditEventAlarmLevels", consumes = "application/json")
    public ResponseEntity<String> saveAuditEventAlarmLevels(HttpServletRequest request, @RequestBody List<JsonSettingsEventLevels> eventAlarmLevels) {
        LOG.info("/api/systemSettings/saveAuditEventAlarmLevels");
        try {
            User user = Common.getUser(request);
            if (user != null && user.isAdmin()) {
                systemSettingsService.saveAuditEventAlarmLevels(eventAlarmLevels);
                return new ResponseEntity<>(SAVED_MSG, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/getSystemEventAlarmLevels", produces = "application/json")
    public ResponseEntity<List<JsonSettingsEventLevels>> getSystemEventAlarmLevels(HttpServletRequest request) {
        LOG.info("/api/systemSettings/getSystemEventAlarmLevels");
        try {
            User user = Common.getUser(request);
            if (user != null && user.isAdmin()) {
                return new ResponseEntity<>(systemSettingsService.getSystemEventAlarmLevels(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * @param request           - request with user data
     * @param eventSystemLevels - Audit Events with ID from 1 to 10 (SystemEventType)
     *                          Exclude ID=5 it does not exist.
     *                          With alarm levels from 0 to 4 (AlarmLevels)
     * @return Response with HTTP status
     */
    @PostMapping(value = "/saveSystemEventAlarmLevels", consumes = "application/json")
    public ResponseEntity<String> saveSystemEventAlarmLevels(HttpServletRequest request, @RequestBody List<JsonSettingsEventLevels> eventSystemLevels) {
        LOG.info("/api/systemSettings/saveSystemEventAlarmLevels");
        try {
            User user = Common.getUser(request);
            if (user != null && user.isAdmin()) {
                systemSettingsService.saveSystemEventAlarmLevels(eventSystemLevels);
                return new ResponseEntity<>(SAVED_MSG, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/getSystemInfo", produces = "application/json")
    public ResponseEntity<JsonSettingsSystemInfo> getSystemInfo(HttpServletRequest request) {
        try {
            User user = Common.getUser(request);
            if (user != null && user.isAdmin()) {
                return new ResponseEntity<>(systemSettingsService.getSystemInfoSettings(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/saveSystemInfo", consumes = "application/json")
    public ResponseEntity<String> saveSystemInfo(HttpServletRequest request, @RequestBody JsonSettingsSystemInfo jsonSettingsSystemInfo) {
        LOG.info("/api/systemSettings/saveSystemInfo");
        try {
            User user = Common.getUser(request);
            if (user != null && user.isAdmin()) {
                systemSettingsService.saveSystemInfoSettings(jsonSettingsSystemInfo);
                return new ResponseEntity<>(SAVED_MSG, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/getDatabaseType", produces = "application/json")
    public ResponseEntity<String> getDatabaseType(HttpServletRequest request) {
        LOG.info("/api/systemSettings/getDatabaseType");
        try {
            User user = Common.getUser(request);
            if (user != null && user.isAdmin()) {
                String json = "{\"databaseType\":\"" + systemSettingsService.getDatabaseType() + "\"}";
                return new ResponseEntity<>(json, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/saveDatabaseType/{databaseType}")
    public ResponseEntity<String> saveDatabaseType(HttpServletRequest request, @PathVariable("databaseType") String databaseType) {
        LOG.info("/api/systemSettings/saveDatabaseType");
        try {
            User user = Common.getUser(request);
            if (user != null && user.isAdmin()) {
                systemSettingsService.setDatabaseType(databaseType);
                return new ResponseEntity<>(SAVED_MSG, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/getDatabaseSize", produces = "application/json")
    public ResponseEntity<String> getDatabaseSize(HttpServletRequest request) {
        LOG.info("/api/systemSettings/getDatabaseSize");
        try {
            User user = Common.getUser(request);
            if (user != null && user.isAdmin()) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    String json = mapper.writeValueAsString(systemSettingsService.getDatabaseSize());
                    return new ResponseEntity<>(json, HttpStatus.OK);
                } catch (JsonProcessingException e) {
                    LOG.error(e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/purgeData", produces = "application/json")
    public ResponseEntity<String> purgeData(HttpServletRequest request) {
        LOG.info("/api/systemSettings/purgeData");
        LOG.warn("Purging data!");
        try {
            User user = Common.getUser(request);
            if (user != null && user.isAdmin()) {
                systemSettingsService.purgeData();
                return new ResponseEntity<>("{\"status\": \"done\"}", HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/getStartupTime", produces = "application/json")
    public ResponseEntity<String> getStartupTime(HttpServletRequest request) {
        try {
            User user = Common.getUser(request);
            if (user != null && user.isAdmin()) {
                return new ResponseEntity<>(
                        "{\"startupTime\": \"" + systemSettingsService.getStartupTime() + "\"}",
                        HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/getSchemaVersion", produces = "application/json")
    public ResponseEntity<String> getSchemaVersion(HttpServletRequest request) {
        try {
            User user = Common.getUser(request);
            if (user != null && user.isAdmin()) {
                return new ResponseEntity<>(
                        "{\"schemaVersion\": \"" + systemSettingsService.getSchemaVersion() + "\"}",
                        HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/getScadaConfig", produces = "application/json")
    public ResponseEntity<JsonSettingsScadaConfig> getScadaConfig(HttpServletRequest request) {
        LOG.info("/api/systemSettings/getScadaConfig");
        try {
            User user = Common.getUser(request);
            if (user != null && user.isAdmin()) {
                return new ResponseEntity<>(systemSettingsService.getScadaConfig(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/getAggregateSettings", produces = "application/json")
    public ResponseEntity<AggregateSettings> getAggregateSettings(HttpServletRequest request) {
        LOG.info("/api/systemSettings/getAggregateSettings");
        try {
            User user = Common.getUser(request);
            if (user != null && user.isAdmin()) {
                return new ResponseEntity<>(systemSettingsService.getAggregateSettings(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/saveAggregateSettings", consumes = "application/json")
    public ResponseEntity<String> saveAggregateSettings(HttpServletRequest request, @RequestBody AggregateSettings aggregateSettings) {
        LOG.info("/api/systemSettings/saveAggregateSettings");
        try {
            User user = Common.getUser(request);
            if (user != null && user.isAdmin()) {
                String errors = SystemSettingsUtils.validateAggregateSettings(aggregateSettings);
                if(!errors.isEmpty())
                    return ResponseEntity.badRequest().body(ValidationUtils.formatErrorsJson(errors));
                systemSettingsService.saveAggregateSettings(aggregateSettings);
                return new ResponseEntity<>(SAVED_MSG, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
