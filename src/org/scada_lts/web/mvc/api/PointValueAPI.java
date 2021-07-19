package org.scada_lts.web.mvc.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.serotonin.mango.vo.dataSource.DataSourceVO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.scada_lts.dao.model.point.PointValueTypeOfREST;
import org.scada_lts.mango.service.DataPointService;
import org.scada_lts.mango.service.DataSourceService;
import org.scada_lts.mango.service.PointValueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serotonin.mango.Common;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.types.AlphanumericValue;
import com.serotonin.mango.rt.dataImage.types.BinaryValue;
import com.serotonin.mango.rt.dataImage.types.ImageValue;
import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.rt.dataImage.types.MultistateValue;
import com.serotonin.mango.rt.dataImage.types.NumericValue;
import com.serotonin.mango.view.text.TextRenderer;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;

import static org.scada_lts.dao.model.point.PointValueTypeOfREST.validPointValueType;
import static org.scada_lts.utils.ValidationUtils.formatErrorsJson;
import static org.scada_lts.utils.ValidationUtils.msgIfNullOrInvalid;


class ValueTime implements Serializable {

    private static final long serialVersionUID = -1253618593346111896L;

    private String value;
    private Long ts;

    public ValueTime(String value, Long ts) {
        setValue(value);
        setTs(ts);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

}

class ValuesToJSON implements Serializable {

    private static final long serialVersionUID = 6842996239503634071L;

    private List<ValueTime> values;
    private Long fromTs;
    private Long toTs;
    private String name;
    private String xid;
    private String type;
    private TextRenderer textRenderer;
    private String chartColour;

    public ValuesToJSON(List<ValueTime> values, DataPointVO dpvo, String type, Long from, Long to) {
        setValues(values);
        setFromTs(from);
        setToTs(to);
        setName(dpvo.getName());
        setXid(dpvo.getXid());
        setType(type);
        setTextRenderer(dpvo.getTextRenderer());
        setChartColour(dpvo.getChartColour());
    }

    public void setFromTs(Long fTs) {
        this.fromTs = fTs;
    }

    public Long getFromTs() {
        return this.fromTs;
    }

    public void setToTs(Long tTs) {
        this.toTs = tTs;
    }

    public Long getToTs() {
        return this.toTs;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setXid(String xid) {
        this.xid = xid;
    }

    public String getXid() {
        return xid;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setTextRenderer(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
    }

    public TextRenderer getTextRenderer() {
        return textRenderer;
    }

    public void setChartColour(String chartColour) {
        this.chartColour = chartColour;
    }

    public String getChartColour() {
        return chartColour;
    }

    public List<ValueTime> getValues() {
        return values;
    }

    public void setValues(List<ValueTime> values) {
        this.values = values;
    }

}

class ValueToJSON implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String value;
    private String formattedValue;
    private Long ts;
    private String name;
    private String xid;
    private String type;
    private TextRenderer textRenderer;
    private String chartColour;
    private boolean enabled;

    void set(PointValueTime pvt, DataPointVO dpvo) {
        setId(dpvo.getId());
        setValue(pvt.getValue());
        setTs(pvt.getTime());
        setName(dpvo.getName());
        setXid(dpvo.getXid());
        setTextRenderer(dpvo.getTextRenderer());
        setChartColour(dpvo.getChartColour());
        setFormattedValue(textRenderer.getText(pvt, 1) + textRenderer.getMetaText());
        setEnabled(dpvo.isEnabled());
    }

    public void setId(int id) { this.id = id; }

    public int getId() { return this.id; }

    public String getValue() {
        return value;
    }

    public void setValue(MangoValue value) {
        if (value instanceof AlphanumericValue) {
            this.value = ((AlphanumericValue) value).getStringValue();
            setType("AlphanumericValue");
        } else if (value instanceof BinaryValue) {
            this.value = String.valueOf(((BinaryValue) value).getBooleanValue());
            setType("BinaryValue");
        } else if (value instanceof ImageValue) {
            this.value = ((ImageValue) value).getFilename();
            setType("ImageValue");
        } else if (value instanceof MultistateValue) {
            this.value = String.valueOf(((MultistateValue) value).getIntegerValue());
            setType("MultistateValue");
        } else if (value instanceof NumericValue) {
            this.value = String.valueOf(((NumericValue) value).getFloatValue());
            setType("NumericValue");
        }
        //error
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getXid() {
        return xid;
    }

    public void setXid(String xid) {
        this.xid = xid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the textRenderer
     */
    public TextRenderer getTextRenderer() {
        return textRenderer;
    }

    /**
     * @param textRenderer the textRenderer to set
     */
    public void setTextRenderer(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
    }

    /**
     * @return the chartColour
     */
    public String getChartColour() {
        return chartColour;
    }

    /**
     * @param chartColour the chartColour to set
     */
    public void setChartColour(String chartColour) {
        this.chartColour = chartColour;
    }

    /**
     * @return the formattedValue
     */
    public String getFormattedValue() {
        return formattedValue;
    }

    /**
     * @param formattedValue the formattedValue to set
     */
    public void setFormattedValue(String formattedValue) {
        this.formattedValue = formattedValue;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}


/**
 * Controller for API pointValue
 *
 * @author Grzesiek Bylica grzegorz.bylica@gmail.com
 */
@Controller
public class PointValueAPI {

    private static final Log LOG = LogFactory.getLog(PointValueAPI.class);

    private DataPointService dataPointService = new DataPointService();
    private DataSourceService dataSourceService = new DataSourceService();

    @Resource
    private PointValueService pointValueService;

    private String getValue(MangoValue value, String type) {

        if (value instanceof AlphanumericValue) {
            type = "AlphanumericValue";
            return ((AlphanumericValue) value).getStringValue();
        } else if (value instanceof BinaryValue) {
            type = "BinaryValue";
            return String.valueOf(((BinaryValue) value).getBooleanValue());
        } else if (value instanceof ImageValue) {
            type = "ImageValue";
            return ((ImageValue) value).getFilename();
        } else if (value instanceof MultistateValue) {
            type = "ImageValue";
            return String.valueOf(((MultistateValue) value).getIntegerValue());
        } else if (value instanceof NumericValue) {
            type = "NumericValue";
            return String.valueOf(((NumericValue) value).getFloatValue());
        } else {
            //TODO throw not type defined
            return null;
        }
    }

    /**
     * @param xid
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/point_value/getValue/{xid}", method = RequestMethod.GET)
    public ResponseEntity<String> getValue(@PathVariable("xid") String xid, HttpServletRequest request) {
        LOG.info("/api/point_value/getValue/{xid} id:" + xid);

        try {
            User user = Common.getUser(request);

            if (user != null) {
                DataPointVO dpvo = dataPointService.getDataPoint(xid);
                PointValueTime pvt = pointValueService.getLatestPointValue(dpvo.getId());
                String json = null;
                ObjectMapper mapper = new ObjectMapper();

                ValueToJSON v = new ValueToJSON();
                v.set(pvt, dpvo);

                json = mapper.writeValueAsString(v);

                return new ResponseEntity<String>(json, HttpStatus.OK);
            }

            return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @param id
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/point_value/getValue/id/{id}", method = RequestMethod.GET)
    public ResponseEntity<String> getValue(@PathVariable("id") int id, HttpServletRequest request) {
        LOG.info("/api/point_value/getValue/id/{id} id:" + id);

        try {
            User user = Common.getUser(request);

            if (user != null) {
                DataPointVO dpvo = dataPointService.getDataPoint(id);
                PointValueTime pvt = pointValueService.getLatestPointValue(dpvo.getId());
                String json = null;
                ObjectMapper mapper = new ObjectMapper();

                ValueToJSON v = new ValueToJSON();
                v.set(pvt, dpvo);

                json = mapper.writeValueAsString(v);

                return new ResponseEntity<String>(json, HttpStatus.OK);
            }

            return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * @param xid
     * @param type    (0 - unknown, 1 - binary, 2 - multistate, 3 - double, 4 - string)
     * @param value   (for binary [0,1]
     * @param request
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/api/point_value/setValue/{xid}/{type}/{value}", method = RequestMethod.POST)
    public ResponseEntity<String> setValue(
            @PathVariable("xid") String xid,
            @PathVariable("type") int type,
            @PathVariable("value") String value,
            HttpServletRequest request) {

        LOG.info("/api/point_value/setValue/{xid}/{type}/{value} xid:" + xid + " type:" + type + " value:" + value);

        try {
            User user = Common.getUser(request);
            if (user != null) {

                dataPointService.save(user, value, xid, type);

                return new ResponseEntity<String>(value, HttpStatus.OK);
            }

            return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @param xid       Data Point Export ID
     * @param type      Data Point Value Type (0 - unknown, 1 - binary, 2 - multistate, 3 - double, 4 - string)
     * @param value     Value to be saved (for binary [0,1])
     * @param request   HTTP Request with user data
     * @return value
     */
    @PostMapping(value = "/api/point_value/setValue/{xid}/{type}")
    public ResponseEntity<String> setValueV2(
            @PathVariable("xid") String xid,
            @PathVariable("type") Integer type,
            @RequestBody String value,
            HttpServletRequest request) {
        LOG.info("/api/point_value/setValue/{xid}/{type}\n - xid:" + xid + " type:" + type + " value:" + value);

        try {
            User user = Common.getUser(request);
            if(user != null) {
                String error = msgIfNullOrInvalid("Correct type", type, a -> !validPointValueType(a));
                if (!error.isEmpty()) {
                    return ResponseEntity.badRequest().body(formatErrorsJson(error));
                }
                if(type != PointValueTypeOfREST.TYPE_STRING) { value = convertInputValue(value); }
                dataPointService.save(user, value, xid, type);
                return new ResponseEntity<>(value, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * @param xid
     * @param type    (0 - binary, 1 - multistate, 2 - double, 3 - string)
     * @param value   (for binary [0,1]
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/point_value/setValue/{xid}/{type}/{value}", method = RequestMethod.GET)
    public ResponseEntity<String> setValueGet(
            @PathVariable("xid") String xid,
            @PathVariable("type") int type,
            @PathVariable("value") String value,
            HttpServletRequest request) {

        LOG.info("/api/point_value/setValue/{xid}/{type}/{value} xid:" + xid + " type:" + type + " value:" + value);

        try {
            User user = Common.getUser(request);
            if (user != null) {

                dataPointService.save(user, value, xid, type);

                return new ResponseEntity<String>(value, HttpStatus.OK);
            }

            return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * @param ts,     xid
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/point_value/getValuesFromTime/{ts}/{xid}", method = RequestMethod.GET)
    public ResponseEntity<String> getValuesFromTime(@PathVariable("ts") long ts, @PathVariable("xid") String xid, HttpServletRequest request) {

        LOG.info("/api/point_value/getValuesFromTime/{ts}/{xid} ts:" + ts + " id:" + xid);

        try {
            User user = Common.getUser(request);
            DataPointVO dpvo = dataPointService.getDataPoint(xid);
            if (user != null) {
                long to = System.currentTimeMillis();
                List<PointValueTime> pvts = pointValueService.getPointValuesBetween(dpvo.getId(), ts, to);
                String json = null;
                ObjectMapper mapper = new ObjectMapper();

                List<ValueTime> values = new ArrayList<ValueTime>();
                String type = null;
                for (PointValueTime pvt : pvts) {
                    values.add(new ValueTime(getValue(pvt.getValue(), type), pvt.getTime()));
                }
                ValuesToJSON v = new ValuesToJSON(values, dpvo, type, ts, to);
                json = mapper.writeValueAsString(v);

                return new ResponseEntity<String>(json, HttpStatus.OK);
            }

            return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @param ts,     xid
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/point_value/getValuesFromTime/id/{ts}/{xid}", method = RequestMethod.GET)
    public ResponseEntity<String> getValuesFromTimeId(@PathVariable("ts") long ts, @PathVariable("xid") int id, HttpServletRequest request) {

        LOG.info("/api/point_value/getValuesFromTime/{ts}/{xid} ts:" + ts + " id:" + id);

        try {
            User user = Common.getUser(request);
            DataPointVO dpvo = dataPointService.getDataPoint(id);
            if (user != null) {
                long to = System.currentTimeMillis();
                List<PointValueTime> pvts = pointValueService.getPointValuesBetween(dpvo.getId(), ts, to);
                String json = null;
                ObjectMapper mapper = new ObjectMapper();

                List<ValueTime> values = new ArrayList<ValueTime>();
                String type = null;
                for (PointValueTime pvt : pvts) {
                    values.add(new ValueTime(getValue(pvt.getValue(), type), pvt.getTime()));
                }
                ValuesToJSON v = new ValuesToJSON(values, dpvo, type, ts, to);
                json = mapper.writeValueAsString(v);

                return new ResponseEntity<String>(json, HttpStatus.OK);
            }

            return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @param xid id of datapoint
     * @param sts start timestamp
     * @param ets end timestamp
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/point_value/getValuesFromTimePeriod/xid/{xid}/{sts}/{ets}", method = RequestMethod.GET)
    public ResponseEntity<String> getValuesFromTimePeriodXid(@PathVariable("xid") String xid, @PathVariable("sts") long sts, @PathVariable("ets") long ets, HttpServletRequest request) {

        LOG.info("/api/point_value/getValuesFromTimePeriod/xid/{id}/{sts}/{ets} id: " + xid + " sts: " + sts + " ets: " + ets);

        try {
            User user = Common.getUser(request);
            DataPointVO dpvo = dataPointService.getDataPoint(xid);
            if (user != null) {
                List<PointValueTime> pvts = pointValueService.getPointValuesBetween(dpvo.getId(), sts, ets);
                String json = null;
                ObjectMapper mapper = new ObjectMapper();

                List<ValueTime> values = new ArrayList<ValueTime>();
                String type = null;
                if(pvts.size() > 0) {
                    MangoValue value = pvts.get(0).getValue();
                    if(value instanceof AlphanumericValue) { type = "Alphanumeric"; }
                    else if(value instanceof BinaryValue) { type = "Binary"; }
                    else if(value instanceof MultistateValue) { type = "Multistate"; }
                    else if(value instanceof NumericValue) { type = "Numeric"; }
                }
                for (PointValueTime pvt : pvts) {
                    values.add(new ValueTime(getValue(pvt.getValue(), type), pvt.getTime()));
                }
                ValuesToJSON v = new ValuesToJSON(values, dpvo, type, sts, ets);
                json = mapper.writeValueAsString(v);

                return new ResponseEntity<String>(json, HttpStatus.OK);
            }

            return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @param id, sts, ets - id of datapoint, start timestamp, end timestamp
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/point_value/getValuesFromTimePeriod/{id}/{sts}/{ets}", method = RequestMethod.GET)
    public ResponseEntity<String> getValuesFromTimePeriod(@PathVariable("id") int id, @PathVariable("sts") long sts, @PathVariable("ets") long ets, HttpServletRequest request) {

        LOG.info("/api/point_value/getValuesFromTimePeriod/{id}/{sts}/{ets} id: " + id + " sts: " + sts + " ets: " + ets);

        try {
            User user = Common.getUser(request);
            DataPointVO dpvo = dataPointService.getDataPoint(id);
            if (user != null) {
                List<PointValueTime> pvts = pointValueService.getPointValuesBetween(dpvo.getId(), sts, ets);
                String json = null;
                ObjectMapper mapper = new ObjectMapper();

                List<ValueTime> values = new ArrayList<ValueTime>();
                String type = null;
                if(pvts.size() > 0) {
                    MangoValue value = pvts.get(0).getValue();
                    if(value instanceof AlphanumericValue) { type = "Alphanumeric"; }
                    else if(value instanceof BinaryValue) { type = "Binary"; }
                    else if(value instanceof MultistateValue) { type = "Multistate"; }
                    else if(value instanceof NumericValue) { type = "Numeric"; }
                }
                for (PointValueTime pvt : pvts) {
                    values.add(new ValueTime(getValue(pvt.getValue(), type), pvt.getTime()));
                }
                ValuesToJSON v = new ValuesToJSON(values, dpvo, type, sts, ets);
                json = mapper.writeValueAsString(v);

                return new ResponseEntity<String>(json, HttpStatus.OK);
            }

            return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/api/point_value/updateMetaDataPointByScript/{xid}", method = RequestMethod.GET)
    public ResponseEntity<String> updateMetaDataPointByScript(@PathVariable("xid") String xid, HttpServletRequest request) {

        try {
            User user = Common.getUser(request);
            if (user != null) {
                if(dataPointService.getDataPoint(xid).getDataSourceTypeId()==DataSourceVO.Type.META.getId()) {
                    pointValueService.updateMetaDataPointByScript(user, xid);
                } else {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<String>(HttpStatus.OK);
    }

    @RequestMapping(value = "/api/point_value/updateMetaDataPointsByScript/{xid}", method = RequestMethod.GET)
    public ResponseEntity<String> updateMetaDataPointsByScript(@PathVariable("xid") String xid, HttpServletRequest request) {

        try {
            User user = Common.getUser(request);
            if (user != null) {

                pointValueService.updateAllMetaDataPointsFromDatasourceByScript(user, xid);

            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<String>(HttpStatus.OK);
    }

    @RequestMapping(value = "/api/point_value/updateAllMetaDataPointsByScript/", method = RequestMethod.GET)
    public ResponseEntity<String> updateAllMetaDataPointsByScript(HttpServletRequest request) {

        try {
            User user = Common.getUser(request);
            if (user != null) {

                pointValueService.updateAllMetaDataPointsByScript(user);

            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<String>(HttpStatus.OK);
    }

    /**
     * Convert Input Value
     * @param value Input value to be converted
     * @return converted input string
     */
    private String convertInputValue(String value) {

        String inappropriateChars = "[=\\s]";
        String replaceComma = "%2C";

        value = value.replaceAll(inappropriateChars, "");
        value = value.replaceAll(replaceComma, ".");
        String[] result = value.split("\\.");
        if(result.length > 1) {
            value = result[0] + "." + result[1];
        }
        return value;
    }

}

