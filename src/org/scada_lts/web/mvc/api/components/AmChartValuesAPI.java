package org.scada_lts.web.mvc.api.components;

import com.serotonin.mango.Common;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.scada_lts.mango.service.DataPointService;
import org.scada_lts.mango.service.SystemSettingsService;
import org.scada_lts.utils.SystemSettingsUtils;
import org.scada_lts.web.mvc.api.AggregateSettings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Controller
@RequestMapping(path = "/api/amcharts")
public class AmChartValuesAPI {
    private static final Log LOG = LogFactory.getLog(AmChartValuesAPI.class);

    private static final DataPointService dpService = new DataPointService();
    private static final SystemSettingsService systemSettingsService = new SystemSettingsService();

    @GetMapping("/")
    public ResponseEntity<List<Map<String, Double>>> getValuesFromTimeRange(
            @RequestParam Set<String> ids,
            @RequestParam long startTs,
            @RequestParam long endTs,
            @RequestParam(required = false, defaultValue = "false")  boolean xid,
            @RequestParam(required = false, defaultValue = "false")  boolean cmp,
            @RequestParam(required = false, defaultValue = "true") boolean configFromSystem,
            AggregateSettings aggregateSettings,
            HttpServletRequest request
    ) {
        try {
            User user = Common.getUser(request);
            if (user != null) {
                if(ids.isEmpty())
                    return ResponseEntity.badRequest().build();
                if (configFromSystem) {
                    aggregateSettings = systemSettingsService.getAggregateSettings();
                } else {
                    String errors = SystemSettingsUtils.validateAggregateSettings(aggregateSettings);
                    if (!errors.isEmpty())
                        return ResponseEntity.badRequest().build();
                }
                if (xid) {
                    return getPointValuesFromRangeByXid(ids, startTs, endTs, cmp, aggregateSettings);
                } else {
                    Set<Integer> dataPointIds = convert(ids);
                    if(dataPointIds.isEmpty())
                        return ResponseEntity.badRequest().build();
                    return getPointValuesFromRangeById(dataPointIds, startTs, endTs, cmp, aggregateSettings);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/by-xid")
    public ResponseEntity<List<Map<String, Double>>> getValuesFromTimeRangeByXid(
            @RequestParam Set<String> ids,
            @RequestParam long startTs,
            @RequestParam long endTs,
            @RequestParam(required = false, defaultValue = "false")  boolean cmp,
            @RequestParam(required = false, defaultValue = "true") boolean configFromSystem,
            AggregateSettings aggregateSettings,
            HttpServletRequest request
    ) {
        try {
            User user = Common.getUser(request);
            if(user != null) {
                if(ids.isEmpty()) {
                    return ResponseEntity.badRequest().build();
                }
                if(configFromSystem) {
                    aggregateSettings = systemSettingsService.getAggregateSettings();
                } else {
                    String errors = SystemSettingsUtils.validateAggregateSettings(aggregateSettings);
                    if(!errors.isEmpty())
                        return ResponseEntity.badRequest().build();
                }
                return getPointValuesFromRangeByXid(ids, startTs, endTs, cmp, aggregateSettings);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/by-id")
    public ResponseEntity<List<Map<String, Double>>> getValuesFromTimeRangeById(
            @RequestParam Set<Integer> ids,
            @RequestParam long startTs,
            @RequestParam long endTs,
            @RequestParam(required = false, defaultValue = "false")  boolean cmp,
            @RequestParam(required = false, defaultValue = "true") boolean configFromSystem,
            AggregateSettings aggregateSettings,
            HttpServletRequest request
    ) {
        try {
            User user = Common.getUser(request);
            if(user != null) {
                if(ids.isEmpty()) {
                    return ResponseEntity.badRequest().build();
                }
                if(configFromSystem) {
                    aggregateSettings = systemSettingsService.getAggregateSettings();
                } else {
                    String errors = SystemSettingsUtils.validateAggregateSettings(aggregateSettings);
                    if(!errors.isEmpty())
                        return ResponseEntity.badRequest().build();
                }
                return getPointValuesFromRangeById(ids, startTs, endTs, cmp, aggregateSettings);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<List<Map<String, Double>>> getPointValuesFromRangeById(Set<Integer> pointIds, long startTs, long endTs, boolean cmp, AggregateSettings aggregateSettings) {
        List<DataPointVO> dataPoints = dpService.getDataPoints(pointIds);
        if(dataPoints.isEmpty())
            return ResponseEntity.notFound().build();
        if(cmp) {
            return new ResponseEntity<>(dpService.getPointValuesToCompareFromRange(dataPoints, startTs, endTs, aggregateSettings), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(dpService.getPointValuesFromRange(dataPoints, startTs, endTs, aggregateSettings), HttpStatus.OK);
        }
    }

    private ResponseEntity<List<Map<String, Double>>> getPointValuesFromRangeByXid(Set<String> pointXids, long startTs, long endTs, boolean cmp, AggregateSettings aggregateSettings) {
        List<DataPointVO> dataPoints = dpService.getDataPointsByXid(pointXids);
        if(dataPoints.isEmpty())
            return ResponseEntity.notFound().build();
        if(cmp) {
            return new ResponseEntity<>(dpService.getPointValuesToCompareFromRange(dataPoints, startTs, endTs, aggregateSettings), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(dpService.getPointValuesFromRange(dataPoints, startTs, endTs, aggregateSettings), HttpStatus.OK);
        }
    }

    private static Set<Integer> convert(Set<String> identifiers) {
        try {
            return identifiers.stream().mapToInt(Integer::parseInt).boxed()
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            LOG.warn(e.getMessage());
            return Collections.emptySet();
        }
    }
}
