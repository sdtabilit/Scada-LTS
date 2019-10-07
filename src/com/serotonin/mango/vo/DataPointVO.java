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
package com.serotonin.mango.vo;

import com.serotonin.InvalidArgumentException;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.bacnet4j.type.enumerated.EngineeringUnits;
import com.serotonin.json.*;
import com.serotonin.mango.Common;
import com.serotonin.mango.DataTypes;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.util.ChangeComparable;
import com.serotonin.mango.util.ExportCodes;
import com.serotonin.mango.util.LocalizableJsonException;
import com.serotonin.mango.view.chart.BaseChartRenderer;
import com.serotonin.mango.view.chart.ChartRenderer;
import com.serotonin.mango.view.text.BaseTextRenderer;
import com.serotonin.mango.view.text.NoneRenderer;
import com.serotonin.mango.view.text.PlainRenderer;
import com.serotonin.mango.view.text.TextRenderer;
import com.serotonin.mango.vo.dataSource.PointLocatorVO;
import com.serotonin.mango.vo.event.PointEventDetectorVO;
import com.serotonin.util.SerializationHelper;
import com.serotonin.util.StringUtils;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.web.i18n.LocalizableMessage;
import org.scada_lts.utils.ColorUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@JsonRemoteEntity
public class DataPointVO implements Serializable, Cloneable, JsonSerializable, ChangeComparable<DataPointVO> {
    private static final long serialVersionUID = -1;
    public static final String XID_PREFIX = "DP_";

    public interface LoggingTypes {
        int ON_CHANGE = 1;
        int ALL = 2;
        int NONE = 3;
        int INTERVAL = 4;
        int ON_TS_CHANGE = 5;
    }

    private static final ExportCodes LOGGING_TYPE_CODES = new ExportCodes();
    static {
        LOGGING_TYPE_CODES.addElement(LoggingTypes.ON_CHANGE, "ON_CHANGE", "pointEdit.logging.type.change");
        LOGGING_TYPE_CODES.addElement(LoggingTypes.ALL, "ALL", "pointEdit.logging.type.all");
        LOGGING_TYPE_CODES.addElement(LoggingTypes.NONE, "NONE", "pointEdit.logging.type.never");
        LOGGING_TYPE_CODES.addElement(LoggingTypes.INTERVAL, "INTERVAL", "pointEdit.logging.type.interval");
        LOGGING_TYPE_CODES.addElement(LoggingTypes.ON_TS_CHANGE, "ON_TS_CHANGE", "pointEdit.logging.type.tsChange");
    }

    public interface PurgeTypes {
        int DAYS = Common.TimePeriods.DAYS;
        int WEEKS = Common.TimePeriods.WEEKS;
        int MONTHS = Common.TimePeriods.MONTHS;
        int YEARS = Common.TimePeriods.YEARS;
    }

    public interface IntervalLoggingTypes {
        int INSTANT = 1;
        int MAXIMUM = 2;
        int MINIMUM = 3;
        int AVERAGE = 4;
    }

    private static final ExportCodes INTERVAL_LOGGING_TYPE_CODES = new ExportCodes();
    static {
        INTERVAL_LOGGING_TYPE_CODES.addElement(IntervalLoggingTypes.INSTANT, "INSTANT",
                "pointEdit.logging.valueType.instant");
        INTERVAL_LOGGING_TYPE_CODES.addElement(IntervalLoggingTypes.MAXIMUM, "MAXIMUM",
                "pointEdit.logging.valueType.maximum");
        INTERVAL_LOGGING_TYPE_CODES.addElement(IntervalLoggingTypes.MINIMUM, "MINIMUM",
                "pointEdit.logging.valueType.minimum");
        INTERVAL_LOGGING_TYPE_CODES.addElement(IntervalLoggingTypes.AVERAGE, "AVERAGE",
                "pointEdit.logging.valueType.average");
    }

    public static final int ENGINEERING_UNITS_DEFAULT = 95; // No units
    private static ExportCodes ENGINEERING_UNITS_CODES = new ExportCodes();
    static {
        for (int i = 0; i < 190; i++)
            ENGINEERING_UNITS_CODES.addElement(i, StringUtils.capitalize(new EngineeringUnits(i).toString()),
                    "engUnit." + i);
    }

    public LocalizableMessage getDataTypeMessage() {
        return pointLocator.getDataTypeMessage();
    }

    public LocalizableMessage getConfigurationDescription() {
        return pointLocator.getConfigurationDescription();
    }

    public boolean isNew() {
        return id == Common.NEW_ID;
    }

    //
    //
    // Properties
    //
    private int id = Common.NEW_ID;
    private String xid;
    @JsonRemoteProperty
    private String name;
    private int dataSourceId;
    @JsonRemoteProperty
    private String deviceName;
    @JsonRemoteProperty
    private boolean enabled;
    private int pointFolderId;
    private int loggingType = LoggingTypes.ON_CHANGE;
    private int intervalLoggingPeriodType = Common.TimePeriods.MINUTES;
    @JsonRemoteProperty
    private int intervalLoggingPeriod = 15;
    private int intervalLoggingType = IntervalLoggingTypes.INSTANT;
    @JsonRemoteProperty
    private double tolerance = 0;
    private int purgeType = Common.TimePeriods.YEARS;
    @JsonRemoteProperty
    private int purgePeriod = 1;
    @JsonRemoteProperty(typeFactory = BaseTextRenderer.Factory.class)
    private TextRenderer textRenderer;
    @JsonRemoteProperty(typeFactory = BaseChartRenderer.Factory.class)
    private ChartRenderer chartRenderer;
    private List<PointEventDetectorVO> eventDetectors;
    private List<UserComment> comments;
    @JsonRemoteProperty
    private int defaultCacheSize = 1;
    @JsonRemoteProperty
    private boolean discardExtremeValues = false;
    @JsonRemoteProperty
    private double discardLowLimit = -Double.MAX_VALUE;
    @JsonRemoteProperty
    private double discardHighLimit = Double.MAX_VALUE;
    private int engineeringUnits = ENGINEERING_UNITS_DEFAULT;
    @JsonRemoteProperty
    private String chartColour;
    @JsonRemoteProperty
    private String parseErrorValue = "0";

    @JsonRemoteProperty
    private double scalingRawZero = 0;
    @JsonRemoteProperty
    private double scalingRawFull = 0;
    @JsonRemoteProperty
    private double scalingEngineeringZero = 0;
    @JsonRemoteProperty
    private double scalingEngineeringFull = 0;
    
    private PointLocatorVO pointLocator;

    //
    //
    // Convenience data from data source
    //
    private int dataSourceTypeId;
    private String dataSourceName;

    //
    //
    // Required for importing
    //
    @JsonRemoteProperty
    private String dataSourceXid;

    //
    //
    // Runtime data
    //
    /*
     * This is used by the watch list and graphic views to cache the last known value for a point to determine if the
     * browser side needs to be refreshed. Initially set to this value so that point views will update (since null
     * values in this case do in fact equal each other).
     */
    private PointValueTime lastValue = new PointValueTime((MangoValue) null, -1);

    public void resetLastValue() {
        lastValue = new PointValueTime((MangoValue) null, -1);
    }

    public PointValueTime lastValue() {
        return lastValue;
    }

    public void updateLastValue(PointValueTime pvt) {
        lastValue = pvt;
    }

    public String getExtendedName() {
        return deviceName + " - " + name;
    }

    public void defaultTextRenderer() {
        if (pointLocator == null)
            textRenderer = new PlainRenderer("");
        else {
            switch (pointLocator.getDataTypeId()) {
            case DataTypes.IMAGE:
                textRenderer = new NoneRenderer();
                break;
            default:
                textRenderer = new PlainRenderer("");
            }
        }
    }

    /*
     * This value is used by the watchlists. It is set when the watchlist is loaded to determine if the user is allowed
     * to set the point or not based upon various conditions.
     */
    private boolean settable;

    public boolean isSettable() {
        return settable;
    }

    public void setSettable(boolean settable) {
        this.settable = settable;
    }

    @Override
    public String getTypeKey() {
        return "event.audit.dataPoint";
    }

    @Override
    public void addProperties(List<LocalizableMessage> list) {
        AuditEventType.addPropertyMessage(list, "common.xid", xid);
        AuditEventType.addPropertyMessage(list, "dsEdit.points.name", name);
        AuditEventType.addPropertyMessage(list, "common.enabled", enabled);
        AuditEventType.addExportCodeMessage(list, "pointEdit.logging.type", LOGGING_TYPE_CODES, loggingType);
        AuditEventType.addPeriodMessage(list, "pointEdit.logging.period", intervalLoggingPeriodType,
                intervalLoggingPeriod);
        AuditEventType.addExportCodeMessage(list, "pointEdit.logging.valueType", INTERVAL_LOGGING_TYPE_CODES,
                intervalLoggingType);
        AuditEventType.addPropertyMessage(list, "pointEdit.logging.tolerance", tolerance);
        AuditEventType.addPeriodMessage(list, "pointEdit.logging.purge", purgeType, purgePeriod);
        AuditEventType.addPropertyMessage(list, "pointEdit.logging.defaultCache", defaultCacheSize);
        AuditEventType.addPropertyMessage(list, "pointEdit.logging.discard", discardExtremeValues);
        AuditEventType.addPropertyMessage(list, "pointEdit.logging.discardLow", discardLowLimit);
        AuditEventType.addPropertyMessage(list, "pointEdit.logging.engineeringUnits", engineeringUnits);
        AuditEventType.addPropertyMessage(list, "pointEdit.props.chartColour", chartColour);

        pointLocator.addProperties(list);
    }

    @Override
    public void addPropertyChanges(List<LocalizableMessage> list, DataPointVO from) {
        AuditEventType.maybeAddPropertyChangeMessage(list, "common.xid", from.xid, xid);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.points.name", from.name, name);
        AuditEventType.maybeAddPropertyChangeMessage(list, "common.enabled", from.enabled, enabled);
        AuditEventType.maybeAddExportCodeChangeMessage(list, "pointEdit.logging.type", LOGGING_TYPE_CODES,
                from.loggingType, loggingType);
        AuditEventType.maybeAddPeriodChangeMessage(list, "pointEdit.logging.period", from.intervalLoggingPeriodType,
                from.intervalLoggingPeriod, intervalLoggingPeriodType, intervalLoggingPeriod);
        AuditEventType.maybeAddExportCodeChangeMessage(list, "pointEdit.logging.valueType",
                INTERVAL_LOGGING_TYPE_CODES, from.intervalLoggingType, intervalLoggingType);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.logging.tolerance", from.tolerance, tolerance);
        AuditEventType.maybeAddPeriodChangeMessage(list, "pointEdit.logging.purge", from.purgeType, from.purgePeriod,
                purgeType, purgePeriod);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.logging.defaultCache", from.defaultCacheSize,
                defaultCacheSize);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.logging.discard", from.discardExtremeValues,
                discardExtremeValues);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.logging.discardLow", from.discardLowLimit,
                discardLowLimit);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.logging.discardHigh", from.discardHighLimit,
                discardHighLimit);
        AuditEventType.maybeAddPropertyChangeMessage(list, "pointEdit.logging.engineeringUnits", from.engineeringUnits,
                engineeringUnits);
        AuditEventType
                .maybeAddPropertyChangeMessage(list, "pointEdit.props.chartColour", from.chartColour, chartColour);

        pointLocator.addPropertyChanges(list, from.pointLocator);
    }

    public int getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(int dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPointFolderId() {
        return pointFolderId;
    }

    public void setPointFolderId(int pointFolderId) {
        this.pointFolderId = pointFolderId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getXid() {
        return xid;
    }

    public void setXid(String xid) {
        this.xid = xid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @SuppressWarnings("unchecked")
    public <T extends PointLocatorVO> T getPointLocator() {
        return (T) pointLocator;
    }

    public void setPointLocator(PointLocatorVO pointLocator) {
        this.pointLocator = pointLocator;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
        if (deviceName == null)
            deviceName = dataSourceName;
    }

    public String getDataSourceXid() {
        return dataSourceXid;
    }

    public void setDataSourceXid(String dataSourceXid) {
        this.dataSourceXid = dataSourceXid;
    }

    public int getDataSourceTypeId() {
        return dataSourceTypeId;
    }

    public void setDataSourceTypeId(int dataSourceTypeId) {
        this.dataSourceTypeId = dataSourceTypeId;
    }

    public int getLoggingType() {
        return loggingType;
    }

    public void setLoggingType(int loggingType) {
        this.loggingType = loggingType;
    }

    public int getPurgePeriod() {
        return purgePeriod;
    }

    public void setPurgePeriod(int purgePeriod) {
        this.purgePeriod = purgePeriod;
    }

    public int getPurgeType() {
        return purgeType;
    }

    public void setPurgeType(int purgeType) {
        this.purgeType = purgeType;
    }

    public double getTolerance() {
        return tolerance;
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    public TextRenderer getTextRenderer() {
        return textRenderer;
    }

    public void setTextRenderer(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
    }

    public ChartRenderer getChartRenderer() {
        return chartRenderer;
    }

    public void setChartRenderer(ChartRenderer chartRenderer) {
        this.chartRenderer = chartRenderer;
    }

    public List<PointEventDetectorVO> getEventDetectors() {
        return eventDetectors;
    }

    public void setEventDetectors(List<PointEventDetectorVO> eventDetectors) {
        this.eventDetectors = eventDetectors;
    }

    public List<UserComment> getComments() {
        return comments;
    }

    public void setComments(List<UserComment> comments) {
        this.comments = comments;
    }

    public int getDefaultCacheSize() {
        return defaultCacheSize;
    }

    public void setDefaultCacheSize(int defaultCacheSize) {
        this.defaultCacheSize = defaultCacheSize;
    }

    public int getIntervalLoggingPeriodType() {
        return intervalLoggingPeriodType;
    }

    public void setIntervalLoggingPeriodType(int intervalLoggingPeriodType) {
        this.intervalLoggingPeriodType = intervalLoggingPeriodType;
    }

    public int getIntervalLoggingPeriod() {
        return intervalLoggingPeriod;
    }

    public void setIntervalLoggingPeriod(int intervalLoggingPeriod) {
        this.intervalLoggingPeriod = intervalLoggingPeriod;
    }

    public int getIntervalLoggingType() {
        return intervalLoggingType;
    }

    public void setIntervalLoggingType(int intervalLoggingType) {
        this.intervalLoggingType = intervalLoggingType;
    }

    public boolean isDiscardExtremeValues() {
        return discardExtremeValues;
    }

    public void setDiscardExtremeValues(boolean discardExtremeValues) {
        this.discardExtremeValues = discardExtremeValues;
    }

    public double getDiscardLowLimit() {
        return discardLowLimit;
    }

    public void setDiscardLowLimit(double discardLowLimit) {
        this.discardLowLimit = discardLowLimit;
    }

    public double getDiscardHighLimit() {
        return discardHighLimit;
    }

    public void setDiscardHighLimit(double discardHighLimit) {
        this.discardHighLimit = discardHighLimit;
    }

    public int getEngineeringUnits() {
        return engineeringUnits;
    }

    public void setEngineeringUnits(int engineeringUnits) {
        this.engineeringUnits = engineeringUnits;
    }

    public String getChartColour() {
        return chartColour;
    }

    public void setChartColour(String chartColour) {
        this.chartColour = chartColour;
    }

    public String getParseErrorValue() {
        return parseErrorValue==null ? "0" : parseErrorValue;
    }

    public void setParseErrorValue(String parseErrorValue) {
        this.parseErrorValue = parseErrorValue;
    }

    public double getScalingRawZero() {
		return scalingRawZero;
	}

	public void setScalingRawZero(double scalingRawZero) {
		this.scalingRawZero = scalingRawZero;
	}

	public double getScalingRawFull() {
		return scalingRawFull;
	}

	public void setScalingRawFull(double scalingRawFull) {
		this.scalingRawFull = scalingRawFull;
	}

	public double getScalingEngineeringZero() {
		return scalingEngineeringZero;
	}

	public void setScalingEngineeringZero(double scalingEngineeringZero) {
		this.scalingEngineeringZero = scalingEngineeringZero;
	}

	public double getScalingEngineeringFull() {
		return scalingEngineeringFull;
	}

	public void setScalingEngineeringFull(double scalingEngineeringFull) {
		this.scalingEngineeringFull = scalingEngineeringFull;
	}

	public DataPointVO copy() {
        try {
            return (DataPointVO) super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new ShouldNeverHappenException(e);
        }
    }

    @Override
    public String toString() {
        return "DataPointVO [id=" + id + ", xid=" + xid + ", name=" + name + ", dataSourceId=" + dataSourceId
                + ", deviceName=" + deviceName + ", enabled=" + enabled + ", pointFolderId=" + pointFolderId
                + ", loggingType=" + loggingType + ", intervalLoggingPeriodType=" + intervalLoggingPeriodType
                + ", intervalLoggingPeriod=" + intervalLoggingPeriod + ", intervalLoggingType=" + intervalLoggingType
                + ", tolerance=" + tolerance + ", purgeType=" + purgeType + ", purgePeriod=" + purgePeriod
                + ", textRenderer=" + textRenderer + ", chartRenderer=" + chartRenderer + ", eventDetectors="
                + eventDetectors + ", comments=" + comments + ", defaultCacheSize=" + defaultCacheSize
                + ", discardExtremeValues=" + discardExtremeValues + ", discardLowLimit=" + discardLowLimit
                + ", discardHighLimit=" + discardHighLimit + ", engineeringUnits=" + engineeringUnits
                + ", chartColour=" + chartColour + ", pointLocator=" + pointLocator + ", dataSourceTypeId="
                + dataSourceTypeId + ", dataSourceName=" + dataSourceName + ", dataSourceXid=" + dataSourceXid
                + ", lastValue=" + lastValue + ", settable=" + settable + ", parseErrorValue=" + parseErrorValue + "]";
    }

    public void validate(DwrResponseI18n response) {
        if (StringUtils.isEmpty(xid))
            response.addContextualMessage("xid", "validate.required");
        else if (StringUtils.isLengthGreaterThan(xid, 50))
            response.addMessage("xid", new LocalizableMessage("validate.notLongerThan", 50));
        else if (!new DataPointDao().isXidUnique(xid, id))
            response.addContextualMessage("xid", "validate.xidUsed");

        if (StringUtils.isEmpty(name))
            response.addContextualMessage("name", "validate.required");

        if (!LOGGING_TYPE_CODES.isValidId(loggingType))
            response.addContextualMessage("loggingType", "validate.invalidValue");
        if (loggingType == DataPointVO.LoggingTypes.ON_CHANGE && pointLocator.getDataTypeId() == DataTypes.NUMERIC) {
            if (tolerance < 0)
                response.addContextualMessage("tolerance", "validate.cannotBeNegative");
        }

        if (!Common.TIME_PERIOD_CODES.isValidId(intervalLoggingPeriodType))
            response.addContextualMessage("intervalLoggingPeriodType", "validate.invalidValue");
        if (intervalLoggingPeriod <= 0)
            response.addContextualMessage("intervalLoggingPeriod", "validate.greaterThanZero");
        if (!INTERVAL_LOGGING_TYPE_CODES.isValidId(intervalLoggingType))
            response.addContextualMessage("intervalLoggingType", "validate.invalidValue");

        if (!Common.TIME_PERIOD_CODES.isValidId(purgeType))
            response.addContextualMessage("purgeType", "validate.invalidValue");
        if (purgePeriod <= 0)
            response.addContextualMessage("purgePeriod", "validate.greaterThanZero");

        if (textRenderer == null)
            response.addContextualMessage("textRenderer", "validate.required");

        if (defaultCacheSize < 0)
            response.addContextualMessage("defaultCacheSize", "validate.cannotBeNegative");

        if (discardExtremeValues && discardHighLimit <= discardLowLimit)
            response.addContextualMessage("discardHighLimit", "validate.greaterThanDiscardLow");

        if (!StringUtils.isEmpty(chartColour)) {
            try {
                ColorUtils.toColor(chartColour);
            }
            catch (InvalidArgumentException e) {
                response.addContextualMessage("chartColour", "validate.invalidValue");
            }
        }

        pointLocator.validate(response);

        // Check text renderer type
        if (textRenderer != null && !textRenderer.getDef().supports(pointLocator.getDataTypeId()))
            response.addGenericMessage("validate.text.incompatible");

        // Check chart renderer type
        if (chartRenderer != null && !chartRenderer.getDef().supports(pointLocator.getDataTypeId()))
            response.addGenericMessage("validate.chart.incompatible");
    }

    //
    //
    // Serialization
    //
    private static final int version = 10;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        SerializationHelper.writeSafeUTF(out, name);
        SerializationHelper.writeSafeUTF(out, deviceName);
        out.writeBoolean(enabled);
        out.writeInt(pointFolderId);
        out.writeInt(loggingType);
        out.writeInt(intervalLoggingPeriodType);
        out.writeInt(intervalLoggingPeriod);
        out.writeInt(intervalLoggingType);
        out.writeDouble(tolerance);
        out.writeInt(purgeType);
        out.writeInt(purgePeriod);
        out.writeObject(textRenderer);
        out.writeObject(chartRenderer);
        out.writeObject(pointLocator);
        out.writeInt(defaultCacheSize);
        out.writeBoolean(discardExtremeValues);
        out.writeDouble(discardLowLimit);
        out.writeDouble(discardHighLimit);
        out.writeInt(engineeringUnits);                        
        SerializationHelper.writeSafeUTF(out, chartColour);
        SerializationHelper.writeSafeUTF(out, parseErrorValue);
        out.writeDouble(scalingRawZero);
        out.writeDouble(scalingRawFull);
        out.writeDouble(scalingEngineeringZero);
        out.writeDouble(scalingEngineeringFull);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            name = SerializationHelper.readSafeUTF(in);
            deviceName = null;
            enabled = in.readBoolean();
            pointFolderId = 0;
            loggingType = in.readInt();
            intervalLoggingPeriodType = Common.TimePeriods.MINUTES;
            intervalLoggingPeriod = 15;
            intervalLoggingType = IntervalLoggingTypes.INSTANT;
            tolerance = in.readDouble();
            purgeType = in.readInt();
            purgePeriod = in.readInt();
            textRenderer = (TextRenderer) in.readObject();
            chartRenderer = (ChartRenderer) in.readObject();
            pointLocator = (PointLocatorVO) in.readObject();
            defaultCacheSize = 0;
            engineeringUnits = ENGINEERING_UNITS_DEFAULT;
            chartColour = null;
        }
        else if (ver == 2) {
            name = SerializationHelper.readSafeUTF(in);
            deviceName = null;
            enabled = in.readBoolean();
            pointFolderId = in.readInt();
            loggingType = in.readInt();
            intervalLoggingPeriodType = Common.TimePeriods.MINUTES;
            intervalLoggingPeriod = 15;
            intervalLoggingType = IntervalLoggingTypes.INSTANT;
            tolerance = in.readDouble();
            purgeType = in.readInt();
            purgePeriod = in.readInt();
            textRenderer = (TextRenderer) in.readObject();
            chartRenderer = (ChartRenderer) in.readObject();

            // The spinwave changes were not correctly implemented, so we need to handle potential errors here.
            try {
                pointLocator = (PointLocatorVO) in.readObject();
            }
            catch (IOException e) {
                // Turn this guy off.
                enabled = false;
            }
            defaultCacheSize = 0;
            engineeringUnits = ENGINEERING_UNITS_DEFAULT;
            chartColour = null;
        }
        else if (ver == 3) {
            name = SerializationHelper.readSafeUTF(in);
            deviceName = null;
            enabled = in.readBoolean();
            pointFolderId = in.readInt();
            loggingType = in.readInt();
            intervalLoggingPeriodType = Common.TimePeriods.MINUTES;
            intervalLoggingPeriod = 15;
            intervalLoggingType = IntervalLoggingTypes.INSTANT;
            tolerance = in.readDouble();
            purgeType = in.readInt();
            purgePeriod = in.readInt();
            textRenderer = (TextRenderer) in.readObject();
            chartRenderer = (ChartRenderer) in.readObject();

            // The spinwave changes were not correctly implemented, so we need to handle potential errors here.
            try {
                pointLocator = (PointLocatorVO) in.readObject();
            }
            catch (IOException e) {
                // Turn this guy off.
                enabled = false;
            }
            defaultCacheSize = in.readInt();
            engineeringUnits = ENGINEERING_UNITS_DEFAULT;
            chartColour = null;
        }
        else if (ver == 4) {
            name = SerializationHelper.readSafeUTF(in);
            deviceName = null;
            enabled = in.readBoolean();
            pointFolderId = in.readInt();
            loggingType = in.readInt();
            intervalLoggingPeriodType = in.readInt();
            intervalLoggingPeriod = in.readInt();
            intervalLoggingType = in.readInt();
            tolerance = in.readDouble();
            purgeType = in.readInt();
            purgePeriod = in.readInt();
            textRenderer = (TextRenderer) in.readObject();
            chartRenderer = (ChartRenderer) in.readObject();

            // The spinwave changes were not correctly implemented, so we need to handle potential errors here.
            try {
                pointLocator = (PointLocatorVO) in.readObject();
            }
            catch (IOException e) {
                // Turn this guy off.
                enabled = false;
            }
            defaultCacheSize = in.readInt();
            engineeringUnits = ENGINEERING_UNITS_DEFAULT;
            chartColour = null;
        }
        else if (ver == 5) {
            name = SerializationHelper.readSafeUTF(in);
            deviceName = null;
            enabled = in.readBoolean();
            pointFolderId = in.readInt();
            loggingType = in.readInt();
            intervalLoggingPeriodType = in.readInt();
            intervalLoggingPeriod = in.readInt();
            intervalLoggingType = in.readInt();
            tolerance = in.readDouble();
            purgeType = in.readInt();
            purgePeriod = in.readInt();
            textRenderer = (TextRenderer) in.readObject();
            chartRenderer = (ChartRenderer) in.readObject();
            pointLocator = (PointLocatorVO) in.readObject();
            defaultCacheSize = in.readInt();
            discardExtremeValues = in.readBoolean();
            discardLowLimit = in.readDouble();
            discardHighLimit = in.readDouble();
            engineeringUnits = ENGINEERING_UNITS_DEFAULT;
            chartColour = null;
        }
        else if (ver == 6) {
            name = SerializationHelper.readSafeUTF(in);
            deviceName = null;
            enabled = in.readBoolean();
            pointFolderId = in.readInt();
            loggingType = in.readInt();
            intervalLoggingPeriodType = in.readInt();
            intervalLoggingPeriod = in.readInt();
            intervalLoggingType = in.readInt();
            tolerance = in.readDouble();
            purgeType = in.readInt();
            purgePeriod = in.readInt();
            textRenderer = (TextRenderer) in.readObject();
            chartRenderer = (ChartRenderer) in.readObject();
            pointLocator = (PointLocatorVO) in.readObject();
            defaultCacheSize = in.readInt();
            discardExtremeValues = in.readBoolean();
            discardLowLimit = in.readDouble();
            discardHighLimit = in.readDouble();
            engineeringUnits = in.readInt();
            chartColour = null;
        }
        else if (ver == 7) {
            name = SerializationHelper.readSafeUTF(in);
            deviceName = null;
            enabled = in.readBoolean();
            pointFolderId = in.readInt();
            loggingType = in.readInt();
            intervalLoggingPeriodType = in.readInt();
            intervalLoggingPeriod = in.readInt();
            intervalLoggingType = in.readInt();
            tolerance = in.readDouble();
            purgeType = in.readInt();
            purgePeriod = in.readInt();
            textRenderer = (TextRenderer) in.readObject();
            chartRenderer = (ChartRenderer) in.readObject();
            pointLocator = (PointLocatorVO) in.readObject();
            defaultCacheSize = in.readInt();
            discardExtremeValues = in.readBoolean();
            discardLowLimit = in.readDouble();
            discardHighLimit = in.readDouble();
            engineeringUnits = in.readInt();
            chartColour = SerializationHelper.readSafeUTF(in);
        }
        else if (ver == 8) {
            name = SerializationHelper.readSafeUTF(in);
            deviceName = SerializationHelper.readSafeUTF(in);
            enabled = in.readBoolean();
            pointFolderId = in.readInt();
            loggingType = in.readInt();
            intervalLoggingPeriodType = in.readInt();
            intervalLoggingPeriod = in.readInt();
            intervalLoggingType = in.readInt();
            tolerance = in.readDouble();
            purgeType = in.readInt();
            purgePeriod = in.readInt();
            textRenderer = (TextRenderer) in.readObject();
            chartRenderer = (ChartRenderer) in.readObject();
            pointLocator = (PointLocatorVO) in.readObject();
            defaultCacheSize = in.readInt();
            discardExtremeValues = in.readBoolean();
            discardLowLimit = in.readDouble();
            discardHighLimit = in.readDouble();
            engineeringUnits = in.readInt();
            chartColour = SerializationHelper.readSafeUTF(in);
        } else if (ver == 9) {
            name = SerializationHelper.readSafeUTF(in);
            deviceName = SerializationHelper.readSafeUTF(in);
            enabled = in.readBoolean();
            pointFolderId = in.readInt();
            loggingType = in.readInt();
            intervalLoggingPeriodType = in.readInt();
            intervalLoggingPeriod = in.readInt();
            intervalLoggingType = in.readInt();
            tolerance = in.readDouble();
            purgeType = in.readInt();
            purgePeriod = in.readInt();
            textRenderer = (TextRenderer) in.readObject();
            chartRenderer = (ChartRenderer) in.readObject();
            pointLocator = (PointLocatorVO) in.readObject();
            defaultCacheSize = in.readInt();
            discardExtremeValues = in.readBoolean();
            discardLowLimit = in.readDouble();
            discardHighLimit = in.readDouble();
            engineeringUnits = in.readInt();
            chartColour = SerializationHelper.readSafeUTF(in);
            parseErrorValue = SerializationHelper.readSafeUTF(in);
        } else if (ver == 10) {
            name = SerializationHelper.readSafeUTF(in);
            deviceName = SerializationHelper.readSafeUTF(in);
            enabled = in.readBoolean();
            pointFolderId = in.readInt();
            loggingType = in.readInt();
            intervalLoggingPeriodType = in.readInt();
            intervalLoggingPeriod = in.readInt();
            intervalLoggingType = in.readInt();
            tolerance = in.readDouble();
            purgeType = in.readInt();
            purgePeriod = in.readInt();
            textRenderer = (TextRenderer) in.readObject();
            chartRenderer = (ChartRenderer) in.readObject();
            pointLocator = (PointLocatorVO) in.readObject();
            defaultCacheSize = in.readInt();
            discardExtremeValues = in.readBoolean();
            discardLowLimit = in.readDouble();
            discardHighLimit = in.readDouble();
            engineeringUnits = in.readInt();
            chartColour = SerializationHelper.readSafeUTF(in);
            parseErrorValue = SerializationHelper.readSafeUTF(in);
            scalingRawZero = in.readDouble();
            scalingRawFull = in.readDouble();
            scalingEngineeringZero = in.readDouble();
            scalingEngineeringFull = in.readDouble();
        }

        // Check the purge type. Weird how this could have been set to 0.
        if (purgeType == 0)
            purgeType = Common.TimePeriods.YEARS;
        // Ditto for purge period
        if (purgePeriod == 0)
            purgePeriod = 1;
    }

    @Override
    public void jsonSerialize(Map<String, Object> map) {
        map.put("xid", xid);
        map.put("loggingType", LOGGING_TYPE_CODES.getCode(loggingType));
        map.put("intervalLoggingPeriodType", Common.TIME_PERIOD_CODES.getCode(intervalLoggingPeriodType));
        map.put("intervalLoggingType", INTERVAL_LOGGING_TYPE_CODES.getCode(intervalLoggingType));
        map.put("purgeType", Common.TIME_PERIOD_CODES.getCode(purgeType));
        map.put("pointLocator", pointLocator);
        map.put("eventDetectors", eventDetectors);
        map.put("engineeringUnits", ENGINEERING_UNITS_CODES.getCode(engineeringUnits));
    }

    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json) throws JsonException {
        String text = json.getString("loggingType");
        if (text != null) {
            loggingType = LOGGING_TYPE_CODES.getId(text);
            if (loggingType == -1)
                throw new LocalizableJsonException("emport.error.invalid", "loggingType", text,
                        LOGGING_TYPE_CODES.getCodeList());
        }

        text = json.getString("intervalLoggingPeriodType");
        if (text != null) {
            intervalLoggingPeriodType = Common.TIME_PERIOD_CODES.getId(text);
            if (intervalLoggingPeriodType == -1)
                throw new LocalizableJsonException("emport.error.invalid", "intervalLoggingPeriodType", text,
                        Common.TIME_PERIOD_CODES.getCodeList());
        }

        text = json.getString("intervalLoggingType");
        if (text != null) {
            intervalLoggingType = INTERVAL_LOGGING_TYPE_CODES.getId(text);
            if (intervalLoggingType == -1)
                throw new LocalizableJsonException("emport.error.invalid", "intervalLoggingType", text,
                        INTERVAL_LOGGING_TYPE_CODES.getCodeList());
        }

        text = json.getString("purgeType");
        if (text != null) {
            purgeType = Common.TIME_PERIOD_CODES.getId(text);
            if (purgeType == -1)
                throw new LocalizableJsonException("emport.error.invalid", "purgeType", text,
                        Common.TIME_PERIOD_CODES.getCodeList());
        }

        JsonObject locatorJson = json.getJsonObject("pointLocator");
        if (locatorJson != null)
            reader.populateObject(pointLocator, locatorJson);

        JsonArray pedArray = json.getJsonArray("eventDetectors");
        if (pedArray != null) {
            for (JsonValue jv : pedArray.getElements()) {
                JsonObject pedObject = jv.toJsonObject();

                String pedXid = pedObject.getString("xid");
                if (StringUtils.isEmpty(pedXid))
                    throw new LocalizableJsonException("emport.error.ped.missingAttr", "xid");

                // Use the ped xid to lookup an existing ped.
                PointEventDetectorVO ped = null;
                for (PointEventDetectorVO existing : eventDetectors) {
                    if (StringUtils.isEqual(pedXid, existing.getXid())) {
                        ped = existing;
                        break;
                    }
                }

                if (ped == null) {
                    // Create a new one
                    ped = new PointEventDetectorVO();
                    ped.setId(Common.NEW_ID);
                    ped.setXid(pedXid);
                    ped.njbSetDataPoint(this);
                    eventDetectors.add(ped);
                }

                reader.populateObject(ped, pedObject);
            }
        }

        text = json.getString("engineeringUnits");
        if (text != null) {
            engineeringUnits = ENGINEERING_UNITS_CODES.getId(text);
            if (engineeringUnits == -1)
                engineeringUnits = ENGINEERING_UNITS_DEFAULT;
        }
    }
}
