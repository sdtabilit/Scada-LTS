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
package com.serotonin.mango.web.dwr;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.RuntimeMBeanException;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.script.ScriptException;

import br.org.scadabr.db.dao.UsersProfileDao;
import com.serotonin.db.KeyValuePair;
import com.serotonin.mango.vo.DataPointExtendedNameComparator;
import net.sf.mbus4j.Connection;
import net.sf.mbus4j.MBusAddressing;
import net.sf.mbus4j.TcpIpConnection;
import net.sf.mbus4j.dataframes.MBusResponseFramesContainer;
import net.sf.mbus4j.dataframes.UserDataResponse;
import net.sf.mbus4j.dataframes.datablocks.DataBlock;
import net.sf.openv4j.DataPoint;
import net.sf.openv4j.Devices;
import net.sf.openv4j.Group;
import net.sf.openv4j.Protocol;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jinterop.dcom.common.JISystem;

import br.org.scadabr.OPCItem;
import br.org.scadabr.OPCUtils;
import br.org.scadabr.RealOPCMaster;
import br.org.scadabr.vo.dataSource.alpha2.Alpha2DataSourceVO;
import br.org.scadabr.vo.dataSource.alpha2.Alpha2PointLocatorVO;
import br.org.scadabr.vo.dataSource.asciiFile.ASCIIFileDataSourceVO;
import br.org.scadabr.vo.dataSource.asciiFile.ASCIIFilePointLocatorVO;
import br.org.scadabr.vo.dataSource.asciiSerial.ASCIISerialDataSourceVO;
import br.org.scadabr.vo.dataSource.asciiSerial.ASCIISerialPointLocatorVO;
import br.org.scadabr.vo.dataSource.dnp3.Dnp3IpDataSourceVO;
import br.org.scadabr.vo.dataSource.dnp3.Dnp3PointLocatorVO;
import br.org.scadabr.vo.dataSource.dnp3.Dnp3SerialDataSourceVO;
import br.org.scadabr.vo.dataSource.drStorageHt5b.DrStorageHt5bDataSourceVO;
import br.org.scadabr.vo.dataSource.drStorageHt5b.DrStorageHt5bPointLocatorVO;
import br.org.scadabr.vo.dataSource.iec101.IEC101EthernetDataSourceVO;
import br.org.scadabr.vo.dataSource.iec101.IEC101PointLocatorVO;
import br.org.scadabr.vo.dataSource.iec101.IEC101SerialDataSourceVO;
import br.org.scadabr.vo.dataSource.nodaves7.NodaveS7DataSourceVO;
import br.org.scadabr.vo.dataSource.nodaves7.NodaveS7PointLocatorVO;
import br.org.scadabr.vo.dataSource.opc.OPCDataSourceVO;
import br.org.scadabr.vo.dataSource.opc.OPCPointLocatorVO;
import cc.radiuino.scadabr.vo.datasource.radiuino.RadiuinoDataSourceVO;
import cc.radiuino.scadabr.vo.datasource.radiuino.RadiuinoPointLocatorVO;

import com.dalsemi.onewire.OneWireException;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.db.IntValuePair;
import com.serotonin.io.StreamUtils;
import org.scada_lts.ds.model.ReactivationDs;
import org.scada_lts.ds.reactivation.ReactivationManager;
import org.scada_lts.modbus.SerialParameters;
import com.serotonin.mango.Common;
import com.serotonin.mango.DataTypes;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.EventDao;
import com.serotonin.mango.rt.RuntimeManager;
import com.serotonin.mango.rt.dataImage.IDataPoint;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.rt.dataSource.DataSourceUtils;
import com.serotonin.mango.rt.dataSource.ebro.EBI25Constants;
import com.serotonin.mango.rt.dataSource.http.HttpReceiverData;
import com.serotonin.mango.rt.dataSource.http.HttpRetrieverDataSourceRT;
import com.serotonin.mango.rt.dataSource.meta.DataPointStateException;
import com.serotonin.mango.rt.dataSource.meta.ResultTypeException;
import com.serotonin.mango.rt.dataSource.meta.ScriptExecutor;
import com.serotonin.mango.rt.dataSource.modbus.ModbusDataSource;
import com.serotonin.mango.rt.dataSource.onewire.Network;
import com.serotonin.mango.rt.dataSource.onewire.NetworkPath;
import com.serotonin.mango.rt.dataSource.onewire.OneWireContainerInfo;
import com.serotonin.mango.rt.dataSource.onewire.OneWireDataSourceRT;
import com.serotonin.mango.rt.dataSource.pachube.PachubeDataSourceRT;
import com.serotonin.mango.rt.dataSource.pachube.PachubeValue;
import com.serotonin.mango.rt.dataSource.snmp.Version;
import com.serotonin.mango.rt.dataSource.viconics.ViconicsDataSourceRT;
import com.serotonin.mango.rt.event.EventInstance;
import com.serotonin.mango.util.IntMessagePair;
import com.serotonin.mango.vo.DataPointNameComparator;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.dataSource.PointLocatorVO;
import com.serotonin.mango.vo.dataSource.bacnet.BACnetIPDataSourceVO;
import com.serotonin.mango.vo.dataSource.bacnet.BACnetIPPointLocatorVO;
import com.serotonin.mango.vo.dataSource.ebro.EBI25DataSourceVO;
import com.serotonin.mango.vo.dataSource.ebro.EBI25PointLocatorVO;
import com.serotonin.mango.vo.dataSource.galil.GalilDataSourceVO;
import com.serotonin.mango.vo.dataSource.galil.GalilPointLocatorVO;
import com.serotonin.mango.vo.dataSource.http.HttpImageDataSourceVO;
import com.serotonin.mango.vo.dataSource.http.HttpImagePointLocatorVO;
import com.serotonin.mango.vo.dataSource.http.HttpReceiverDataSourceVO;
import com.serotonin.mango.vo.dataSource.http.HttpReceiverPointLocatorVO;
import com.serotonin.mango.vo.dataSource.http.HttpRetrieverDataSourceVO;
import com.serotonin.mango.vo.dataSource.http.HttpRetrieverPointLocatorVO;
import com.serotonin.mango.vo.dataSource.internal.InternalDataSourceVO;
import com.serotonin.mango.vo.dataSource.internal.InternalPointLocatorVO;
import com.serotonin.mango.vo.dataSource.jmx.JmxDataSourceVO;
import com.serotonin.mango.vo.dataSource.jmx.JmxPointLocatorVO;
import com.serotonin.mango.vo.dataSource.mbus.MBusDataSourceVO;
import com.serotonin.mango.vo.dataSource.mbus.MBusPointLocatorVO;
import com.serotonin.mango.vo.dataSource.mbus.PrimaryAddressingSearch;
import com.serotonin.mango.vo.dataSource.mbus.SecondaryAddressingSearch;
import com.serotonin.mango.vo.dataSource.meta.MetaDataSourceVO;
import com.serotonin.mango.vo.dataSource.meta.MetaPointLocatorVO;
import com.serotonin.mango.vo.dataSource.modbus.ModbusIpDataSourceVO;
import com.serotonin.mango.vo.dataSource.modbus.ModbusIpDataSourceVO.TransportType;
import com.serotonin.mango.vo.dataSource.modbus.ModbusPointLocatorVO;
import com.serotonin.mango.vo.dataSource.modbus.ModbusSerialDataSourceVO;
import com.serotonin.mango.vo.dataSource.modbus.ModbusSerialDataSourceVO.EncodingType;
import com.serotonin.mango.vo.dataSource.nmea.NmeaDataSourceVO;
import com.serotonin.mango.vo.dataSource.nmea.NmeaPointLocatorVO;
import com.serotonin.mango.vo.dataSource.onewire.OneWireDataSourceVO;
import com.serotonin.mango.vo.dataSource.onewire.OneWirePointLocatorVO;
import com.serotonin.mango.vo.dataSource.openv4j.OpenV4JDataSourceVO;
import com.serotonin.mango.vo.dataSource.openv4j.OpenV4JPointLocatorVO;
import com.serotonin.mango.vo.dataSource.pachube.PachubeDataSourceVO;
import com.serotonin.mango.vo.dataSource.pachube.PachubePointLocatorVO;
import com.serotonin.mango.vo.dataSource.persistent.PersistentDataSourceVO;
import com.serotonin.mango.vo.dataSource.persistent.PersistentPointLocatorVO;
import com.serotonin.mango.vo.dataSource.pop3.Pop3DataSourceVO;
import com.serotonin.mango.vo.dataSource.pop3.Pop3PointLocatorVO;
import com.serotonin.mango.vo.dataSource.snmp.SnmpDataSourceVO;
import com.serotonin.mango.vo.dataSource.snmp.SnmpPointLocatorVO;
import com.serotonin.mango.vo.dataSource.sql.SqlDataSourceVO;
import com.serotonin.mango.vo.dataSource.sql.SqlPointLocatorVO;
import com.serotonin.mango.vo.dataSource.viconics.ViconicsDataSourceVO;
import com.serotonin.mango.vo.dataSource.viconics.ViconicsPointLocatorVO;
import com.serotonin.mango.vo.dataSource.virtual.ChangeTypeVO;
import com.serotonin.mango.vo.dataSource.virtual.VirtualDataSourceVO;
import com.serotonin.mango.vo.dataSource.virtual.VirtualPointLocatorVO;
import com.serotonin.mango.vo.dataSource.vmstat.VMStatDataSourceVO;
import com.serotonin.mango.vo.dataSource.vmstat.VMStatPointLocatorVO;
import com.serotonin.mango.vo.event.PointEventDetectorVO;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.mango.web.dwr.beans.BACnetDiscovery;
import com.serotonin.mango.web.dwr.beans.BACnetObjectBean;
import com.serotonin.mango.web.dwr.beans.DataPointDefaulter;
import com.serotonin.mango.web.dwr.beans.EBI25InterfaceReader;
import com.serotonin.mango.web.dwr.beans.EBI25InterfaceUpdater;
import com.serotonin.mango.web.dwr.beans.EventInstanceBean;
import com.serotonin.mango.web.dwr.beans.GalilCommandTester;
import com.serotonin.mango.web.dwr.beans.HttpReceiverDataListener;
import com.serotonin.mango.web.dwr.beans.MBusDiscovery;
import com.serotonin.mango.web.dwr.beans.ModbusNodeScanListener;
import com.serotonin.mango.web.dwr.beans.NmeaUtilListener;
import com.serotonin.mango.web.dwr.beans.OpenV4JDataPointBean;
import com.serotonin.mango.web.dwr.beans.OpenV4JDiscovery;
import com.serotonin.mango.web.dwr.beans.OpenV4JProtocolBean;
import com.serotonin.mango.web.dwr.beans.SnmpOidGet;
import com.serotonin.mango.web.dwr.beans.SqlStatementTester;
import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.code.RegisterRange;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusIdException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.locator.BaseLocator;
import com.serotonin.modbus4j.msg.ModbusRequest;
import com.serotonin.modbus4j.msg.ReadResponse;
import com.serotonin.util.IpAddressUtils;
import com.serotonin.util.StringUtils;
import com.serotonin.viconics.RequestFailureException;
import com.serotonin.viconics.ViconicsDevice;
import com.serotonin.viconics.ViconicsNetwork;
import com.serotonin.viconics.ViconicsTransportException;
import com.serotonin.viconics.msg.NetworkIdentifyRequest;
import com.serotonin.viconics.msg.NetworkIdentifyResponse;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.web.dwr.MethodFilter;
import com.serotonin.web.i18n.LocalizableException;
import com.serotonin.web.i18n.LocalizableMessage;
import com.serotonin.web.taglib.DateFunctions;

/**
 * @author Matthew Lohbihler
 */
public class DataSourceEditDwr extends DataSourceListDwr {
	private static final Log LOG = LogFactory.getLog(DataSourceEditDwr.class);

	//
	//
	// Common methods
	//
	@MethodFilter
	public DwrResponseI18n editInit() {
		DwrResponseI18n response = new DwrResponseI18n();
		response.addData("points", getPoints());
		response.addData("alarms", getAlarms());
		return response;
	}

	private DwrResponseI18n tryDataSourceSave(DataSourceVO<?> ds) {

		DwrResponseI18n response = new DwrResponseI18n();

		ds.validate(response);

		if (!response.getHasMessages()) {
			LOG.debug("Trying to save datasource " + ds.getName());
			Common.ctx.getRuntimeManager().saveDataSource(ds);
			response.addData("id", ds.getId());
			LOG.debug("Response: " + response.toString());
		}

        return response;
    }

    @MethodFilter
    public void cancelTestingUtility() {
        Common.getUser().cancelTestingUtility();
    }

    @MethodFilter
    public List<DataPointVO> enableAllPoints() {
        User user = Common.getUser();
        if (user == null)
            return null;

        DataSourceVO<?> ds = user.getEditDataSource();
        if (ds.getId() == Common.NEW_ID)
            return null;

        List<DataPointVO> points = new DataPointDao().getDataPoints(ds.getId(),
                DataPointNameComparator.instance);
        for (DataPointVO dataPointVO : points) {
            if (!dataPointVO.isEnabled()) {
                Permissions.ensureDataSourcePermission(Common.getUser(),
                        dataPointVO.getDataSourceId());

                RuntimeManager runtimeManager = Common.ctx.getRuntimeManager();
                dataPointVO.setEnabled(true);
                runtimeManager.saveDataPoint(dataPointVO);
            }
        }
        return points;
    }

    @MethodFilter
    public List<DataPointVO> getPoints() {
        User user = Common.getUser();
        if (user == null)
            return null;

        DataSourceVO<?> ds = user.getEditDataSource();
        if (ds.getId() == Common.NEW_ID)
            return null;

        List<DataPointVO> points = new DataPointDao().getDataPoints(ds.getId(),
                DataPointNameComparator.instance);
        return points;
    }

    @MethodFilter
    public DataPointVO getPoint(int pointId) {
        return getPoint(pointId, null);
    }

    private DataPointVO getPoint(int pointId, DataPointDefaulter defaulter) {
        DataSourceVO<?> ds = Common.getUser().getEditDataSource();

        DataPointVO dp;
        if (pointId == Common.NEW_ID) {
            dp = new DataPointVO();
            dp.setXid(new DataPointDao().generateUniqueXid());
            dp.setDataSourceId(ds.getId());
            dp.setPointLocator(ds.createPointLocator());
            dp.setEventDetectors(new ArrayList<PointEventDetectorVO>(0));
            if (defaulter != null)
                defaulter.setDefaultValues(dp);
        } else {
            dp = new DataPointDao().getDataPoint(pointId);
            if (dp != null && dp.getDataSourceId() != ds.getId())
                throw new RuntimeException("Data source id mismatch");
        }

        return dp;
    }

    private DwrResponseI18n validatePoint(int id, String xid, String name,
                                          PointLocatorVO locator, DataPointDefaulter defaulter) {
        DwrResponseI18n response = new DwrResponseI18n();

        DataPointVO dp = getPoint(id, defaulter);
        dp.setXid(xid);
        dp.setName(name);
        dp.setPointLocator(locator);

        if (StringUtils.isEmpty(xid))
            response.addContextualMessage("xid", "validate.required");
        else if (!new DataPointDao().isXidUnique(xid, id))
            response.addContextualMessage("xid", "validate.xidUsed");
        else if (StringUtils.isLengthGreaterThan(xid, 50))
            response.addContextualMessage("xid", "validate.notLongerThan", 50);

        if (StringUtils.isEmpty(name))
            response.addContextualMessage("name", "dsEdit.validate.required");

        locator.validate(response);

        if (!response.getHasMessages()) {
            Common.ctx.getRuntimeManager().saveDataPoint(dp);
            response.addData("id", dp.getId());
            response.addData("points", getPoints());
        }

        return response;
    }

    @MethodFilter
    public List<DataPointVO> deletePoint(int id) {
        DataPointVO dp = getPoint(id, null);
        if (dp != null)
            Common.ctx.getRuntimeManager().deleteDataPoint(dp);
        UsersProfileDao usersProfileDao = new UsersProfileDao();
        usersProfileDao.updateDataPointPermissions();
        return getPoints();
    }

    @MethodFilter
    public Map<String, Object> toggleEditDataSource() {
        DataSourceVO<?> ds = Common.getUser().getEditDataSource();
        return super.toggleDataSource(ds.getId());
    }

    @MethodFilter
    public DwrResponseI18n togglePoint(int dataPointId) {
        DwrResponseI18n response = super.toggleDataPoint(dataPointId);
        response.addData("points", getPoints());
        return response;
    }

    @MethodFilter
    public List<EventInstanceBean> getAlarms() {
        DataSourceVO<?> ds = Common.getUser().getEditDataSource();
        List<EventInstance> events = new EventDao()
                .getPendingEventsForDataSource(ds.getId(), Common.getUser()
                        .getId());
        Collections.sort(events, new Comparator<EventInstance>() {
            @Override
            public int compare(EventInstance lhs, EventInstance rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return lhs.getActiveTimestamp() > rhs.getActiveTimestamp() ? -1 : (lhs.getActiveTimestamp() < rhs.getActiveTimestamp()) ? 1 : 0;
            }
        });
        List<EventInstanceBean> beans = new ArrayList<EventInstanceBean>();
        if (events != null) {
            for (EventInstance event : events)
                beans.add(new EventInstanceBean(event.isActive(), event
                        .getAlarmLevel(), DateFunctions.getTime(event
                        .getActiveTimestamp()), getMessage(event.getMessage())));
        }
        return beans;
    }

    @MethodFilter
    public void updateEventAlarmLevel(int eventId, int alarmLevel) {
        DataSourceVO<?> ds = Common.getUser().getEditDataSource();
        ds.setAlarmLevel(eventId, alarmLevel);
    }

    //
    //
    // Virtual stuff
    //
    @MethodFilter
    public DwrResponseI18n saveVirtualDataSource(String name, String xid,
                                                 int updatePeriods, int updatePeriodType) {
        VirtualDataSourceVO ds = (VirtualDataSourceVO) Common.getUser()
                .getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setUpdatePeriods(updatePeriods);
        ds.setUpdatePeriodType(updatePeriodType);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public IntMessagePair[] getChangeTypes(int dataTypeId) {
        return ChangeTypeVO.getChangeTypes(dataTypeId);
    }

    @MethodFilter
    public DwrResponseI18n saveVirtualPointLocator(int id, String xid,
                                                   String name, VirtualPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }

    //
    //
    // Modbus common stuff
    //
    @MethodFilter
    public Map<String, Object> modbusScanUpdate() {
        Map<String, Object> result = new HashMap<String, Object>();
        ModbusNodeScanListener scan = Common.getUser().getTestingUtility(
                ModbusNodeScanListener.class);
        if (scan == null)
            return null;

        result.put("nodes", scan.getNodesFound());
        result.put("message", scan.getMessage());
        result.put("finished", scan.isFinished());

        return result;
    }

    @MethodFilter
    public DwrResponseI18n saveModbusPointLocator(int id, String xid,
                                                  String name, ModbusPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }

    private void testModbusPointLocator(ModbusMaster modbusMaster,
                                        ModbusPointLocatorVO locator, boolean serial,
                                        DwrResponseI18n response) {
        locator.validate(response);
        if (response.getHasMessages())
            return;

        try {
            BaseLocator<?> bl = ModbusDataSource.createModbusLocator(locator);
            modbusMaster.init();
            Object result = modbusMaster.getValue(bl);
            response.addData("result", new LocalizableMessage(
                    "dsEdit.modbus.locatorTest.result", result));
        } catch (ModbusInitException e) {
            if (serial)
                response.addMessage(new LocalizableMessage(
                        "dsEdit.modbus.locatorTestIp.startError", e
                        .getMessage()));
            else
                response.addMessage(new LocalizableMessage(
                        "dsEdit.modbus.locatorTestSerial.startError", e
                        .getMessage()));
        } catch (ErrorResponseException e) {
            response.addMessage(new LocalizableMessage("common.default", e
                    .getErrorResponse().getExceptionMessage()));
        } catch (ModbusTransportException e) {
            response.addMessage(ModbusDataSource.localExceptionMessage(e));
        } catch (IllegalCharsetNameException e) {
            response.addMessage(new LocalizableMessage(
                    "validate.invalidCharset"));
        } finally {
            modbusMaster.destroy();
        }
    }

    private void testModbusData(ModbusMaster modbusMaster, int slaveId,
                                int range, int offset, int length, boolean serial,
                                DwrResponseI18n response) {
        boolean binary = range == RegisterRange.COIL_STATUS
                || range == RegisterRange.INPUT_STATUS;
        if (length > modbusMaster.getMaxReadCount(range))
            length = modbusMaster.getMaxReadCount(range);
        if (offset + length > 65536)
            length = 65536 - offset;
        response.addData("length", length);

        try {
            ModbusRequest mreq = new ModbusFactory().createReadRequest(slaveId,
                    range, offset, length);

            modbusMaster.init();
            ReadResponse mres = (ReadResponse) modbusMaster.send(mreq);
            if (mres.isException())
                response.addMessage(new LocalizableMessage("common.default",
                        mres.getExceptionMessage()));
            else {
                List<String> results = new ArrayList<String>();
                if (binary) {
                    boolean[] data = mres.getBooleanData();
                    for (int i = 0; i < length; i++)
                        results.add(Integer.toString(offset + i) + " ==> "
                                + Boolean.toString(data[i]));
                } else {
                    short[] data = mres.getShortData();
                    for (int i = 0; i < length; i++)
                        results.add(Integer.toString(offset + i) + " ==> "
                                + StreamUtils.toHex(data[i]));
                }
                response.addData("results", results);
            }
        } catch (ModbusIdException e) {
            response.addMessage(ModbusDataSource.localExceptionMessage(e));
        } catch (ModbusInitException e) {
            if (serial)
                response.addMessage(new LocalizableMessage(
                        "dsEdit.modbus.locatorTestIp.startError", e
                        .getMessage()));
            else
                response.addMessage(new LocalizableMessage(
                        "dsEdit.modbus.locatorTestSerial.startError", e
                        .getMessage()));
        } catch (ModbusTransportException e) {
            response.addMessage(ModbusDataSource.localExceptionMessage(e));
        } finally {
            modbusMaster.destroy();
        }
    }

    //
    //
    // Modbus serial stuff
    //
    @MethodFilter
    public DwrResponseI18n saveModbusSerialDataSource(String name, String xid,
                                                      int updatePeriods, int updatePeriodType, boolean quantize,
                                                      int timeout, int retries, boolean contiguousBatches,
                                                      boolean createSlaveMonitorPoints, int maxReadBitCount,
                                                      int maxReadRegisterCount, int maxWriteRegisterCount,
                                                      String commPortId, int baudRate, int flowControlIn,
                                                      int flowControlOut, int dataBits, int stopBits, int parity,
                                                      String encoding, boolean echo, int concurrency) {
        ModbusSerialDataSourceVO ds = (ModbusSerialDataSourceVO) Common
                .getUser().getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setUpdatePeriods(updatePeriods);
        ds.setUpdatePeriodType(updatePeriodType);
        ds.setQuantize(quantize);
        ds.setTimeout(timeout);
        ds.setRetries(retries);
        ds.setContiguousBatches(contiguousBatches);
        ds.setCreateSlaveMonitorPoints(createSlaveMonitorPoints);
        ds.setMaxReadBitCount(maxReadBitCount);
        ds.setMaxReadRegisterCount(maxReadRegisterCount);
        ds.setMaxWriteRegisterCount(maxWriteRegisterCount);
        ds.setCommPortId(commPortId);
        ds.setBaudRate(baudRate);
        ds.setFlowControlIn(flowControlIn);
        ds.setFlowControlOut(flowControlOut);
        ds.setDataBits(dataBits);
        ds.setStopBits(stopBits);
        ds.setParity(parity);
        ds.setEncodingStr(encoding);
        ds.setEcho(echo);
        ds.setConcurrency(concurrency);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public String modbusSerialScan(int timeout, int retries, String commPortId,
                                   int baudRate, int flowControlIn, int flowControlOut, int dataBits,
                                   int stopBits, int parity, String encoding, int concurrency) {
        ModbusMaster modbusMaster;
        try {
            modbusMaster = createModbusSerialMaster(timeout, retries,
                    commPortId, baudRate, flowControlIn, flowControlOut,
                    dataBits, stopBits, parity, encoding, concurrency);
        } catch (Exception e) {
            return getMessage("dsEdit.modbus.scanError");
        }
        ModbusNodeScanListener scan = new ModbusNodeScanListener(
                getResourceBundle(), modbusMaster, true);
        Common.getUser().setTestingUtility(scan);
        return null;
    }

    @MethodFilter
    public DwrResponseI18n testModbusSerialLocator(int timeout, int retries,
                                                   String commPortId, int baudRate, int flowControlIn,
                                                   int flowControlOut, int dataBits, int stopBits, int parity,
                                                   String encoding, int concurrency, ModbusPointLocatorVO locator) {
        DwrResponseI18n response = new DwrResponseI18n();
        ModbusMaster modbusMaster;
        try {
            modbusMaster = createModbusSerialMaster(timeout, retries,
                    commPortId, baudRate, flowControlIn, flowControlOut,
                    dataBits, stopBits, parity, encoding, concurrency);
            testModbusPointLocator(modbusMaster, locator, true, response);
        } catch (Exception e) {
            response.addMessage(new LocalizableMessage(
                    "dsEdit.modbus.scanError"));
        }
        return response;
    }

    @MethodFilter
    public DwrResponseI18n testModbusSerialData(int timeout, int retries,
                                                String commPortId, int baudRate, int flowControlIn,
                                                int flowControlOut, int dataBits, int stopBits, int parity,
                                                String encoding, int concurrency, int slaveId, int range,
                                                int offset, int length) {
        DwrResponseI18n response = new DwrResponseI18n();
        ModbusMaster modbusMaster;
        try {
            modbusMaster = createModbusSerialMaster(timeout, retries,
                    commPortId, baudRate, flowControlIn, flowControlOut,
                    dataBits, stopBits, parity, encoding, concurrency);
            testModbusData(modbusMaster, slaveId, range, offset, length, true,
                    response);
        } catch (Exception e) {
            response.addMessage(new LocalizableMessage(
                    "dsEdit.modbus.scanError"));
        }
        return response;
    }

    private ModbusMaster createModbusSerialMaster(int timeout, int retries,
                                                  String commPortId, int baudRate, int flowControlIn,
                                                  int flowControlOut, int dataBits, int stopBits, int parity,
                                                  String encoding, int concurrency) throws Exception {
        User user = Common.getUser();
        Permissions.ensureDataSourcePermission(user);

        if (StringUtils.isEmpty(commPortId))
            throw new Exception();

        SerialParameters params = new SerialParameters();
        params.setCommPortId(commPortId);
        params.setPortOwnerName("Mango Modbus Serial Data Source Scan");
        params.setBaudRate(baudRate);
        params.setFlowControlIn(flowControlIn);
        params.setFlowControlOut(flowControlOut);
        params.setDataBits(dataBits);
        params.setStopBits(stopBits);
        params.setParity(parity);

        EncodingType encodingType = EncodingType.valueOf(encoding);

        ModbusMaster modbusMaster;
        if (encodingType == EncodingType.ASCII)
            modbusMaster = new ModbusFactory().createAsciiMaster(params);
        else
            modbusMaster = new ModbusFactory().createRtuMaster(params);
        modbusMaster.setTimeout(timeout);
        modbusMaster.setRetries(retries);

        return modbusMaster;
    }

    //
    //
    // Modbus IP stuff
    //
    @MethodFilter
    public DwrResponseI18n saveModbusIpDataSource(String name, String xid,
                                                  int updatePeriods, int updatePeriodType, boolean quantize,
                                                  int timeout, int retries, boolean contiguousBatches,
                                                  boolean createSlaveMonitorPoints, int maxReadBitCount,
                                                  int maxReadRegisterCount, int maxWriteRegisterCount,
                                                  String transportType, String host, int port, boolean encapsulated,
                                                  boolean createSocketMonitorPoint) {
        ModbusIpDataSourceVO ds = (ModbusIpDataSourceVO) Common.getUser()
                .getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setUpdatePeriods(updatePeriods);
        ds.setUpdatePeriodType(updatePeriodType);
        ds.setQuantize(quantize);
        ds.setTimeout(timeout);
        ds.setRetries(retries);
        ds.setContiguousBatches(contiguousBatches);
        ds.setCreateSlaveMonitorPoints(createSlaveMonitorPoints);
        ds.setMaxReadBitCount(maxReadBitCount);
        ds.setMaxReadRegisterCount(maxReadRegisterCount);
        ds.setMaxWriteRegisterCount(maxWriteRegisterCount);
        ds.setTransportTypeStr(transportType);
        ds.setHost(host);
        ds.setPort(port);
        ds.setEncapsulated(encapsulated);
        ds.setCreateSocketMonitorPoint(createSocketMonitorPoint);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public String modbusIpScan(int timeout, int retries, String transport,
                               String host, int port, boolean encapsulated) {
        ModbusMaster modbusMaster = createModbusIpMaster(timeout, retries,
                transport, host, port, encapsulated);
        ModbusNodeScanListener scan = new ModbusNodeScanListener(
                getResourceBundle(), modbusMaster, false);
        Common.getUser().setTestingUtility(scan);
        return null;
    }

    @MethodFilter
    public DwrResponseI18n testModbusIpLocator(int timeout, int retries,
                                               String transport, String host, int port, boolean encapsulated,
                                               ModbusPointLocatorVO locator) {
        DwrResponseI18n response = new DwrResponseI18n();
        ModbusMaster modbusMaster = createModbusIpMaster(timeout, retries,
                transport, host, port, encapsulated);
        testModbusPointLocator(modbusMaster, locator, false, response);
        return response;
    }

    @MethodFilter
    public DwrResponseI18n testModbusIpData(int timeout, int retries,
                                            String transport, String host, int port, boolean encapsulated,
                                            int slaveId, int range, int offset, int length) {
        DwrResponseI18n response = new DwrResponseI18n();
        ModbusMaster modbusMaster = createModbusIpMaster(timeout, retries,
                transport, host, port, encapsulated);
        testModbusData(modbusMaster, slaveId, range, offset, length, false,
                response);
        return response;
    }

    private ModbusMaster createModbusIpMaster(int timeout, int retries,
                                              String transport, String host, int port, boolean encapsulated) {
        User user = Common.getUser();
        Permissions.ensureDataSourcePermission(user);

        IpParameters params = new IpParameters();
        params.setHost(host);
        params.setPort(port);
        params.setEncapsulated(encapsulated);

        TransportType transportType = TransportType.valueOf(transport);

        ModbusMaster modbusMaster;
        if (transportType == TransportType.TCP_LISTENER)
            modbusMaster = new ModbusFactory().createTcpListener(params);
        else if (transportType == TransportType.UDP)
            modbusMaster = new ModbusFactory().createUdpMaster(params);
        else
            modbusMaster = new ModbusFactory().createTcpMaster(params,
                    transportType == TransportType.TCP_KEEP_ALIVE);
        modbusMaster.setTimeout(timeout);
        modbusMaster.setRetries(retries);

        return modbusMaster;
    }

    //
    //
    // SNMP stuff
    //
    @MethodFilter
    public DwrResponseI18n saveSnmpDataSource(String name, String xid,
                                              int updatePeriods, int updatePeriodType, String host, int port,
                                              int snmpVersion, String community, String securityName,
                                              String authProtocol, String authPassphrase, String privProtocol,
                                              String privPassphrase, int securityLevel, String contextName,
                                              int retries, int timeout, boolean trapEnabled, int trapPort,
                                              String localAddress) {
        SnmpDataSourceVO ds = (SnmpDataSourceVO) Common.getUser()
                .getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setUpdatePeriods(updatePeriods);
        ds.setUpdatePeriodType(updatePeriodType);
        ds.setHost(host);
        ds.setPort(port);
        ds.setSnmpVersion(snmpVersion);
        ds.setCommunity(community);
        ds.setSecurityName(securityName);
        ds.setAuthProtocol(authProtocol);
        ds.setAuthPassphrase(authPassphrase);
        ds.setPrivProtocol(privProtocol);
        ds.setPrivPassphrase(privPassphrase);
        ds.setSecurityLevel(securityLevel);
        ds.setContextName(contextName);
        ds.setRetries(retries);
        ds.setTimeout(timeout);
        ds.setTrapEnabled(trapEnabled);
        ds.setTrapPort(trapPort);
        ds.setLocalAddress(localAddress);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public DwrResponseI18n saveSnmpPointLocator(int id, String xid,
                                                String name, SnmpPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }

    @MethodFilter
    public void snmpGetOid(String oid, String host, int port, int snmpVersion,
                           String community, String securityName, String authProtocol,
                           String authPassphrase, String privProtocol, String privPassphrase,
                           int securityLevel, String contextName,
                           int retries, int timeout) {
        User user = Common.getUser();
        Permissions.ensureDataSourcePermission(user);

        Version version = Version.getVersion(snmpVersion, community,
                securityName, authProtocol, authPassphrase, privProtocol,
                privPassphrase, securityLevel, contextName);
        user.setTestingUtility(new SnmpOidGet(getResourceBundle(), host, port,
                version, oid, retries, timeout));
    }

    @MethodFilter
    public void snmpWalkOid(String oid, String host, int port, int snmpVersion,
                            String community, String securityName, String authProtocol,
                            String authPassphrase, String privProtocol, String privPassphrase,
                            int securityLevel, String contextName,
                            int retries, int timeout) {
        User user = Common.getUser();
        Permissions.ensureDataSourcePermission(user);

        Version version = Version.getVersion(snmpVersion, community,
                securityName, authProtocol, authPassphrase, privProtocol,
                privPassphrase, securityLevel, contextName);
//		user.setTestingUtility(new SnmpOidWalk(getResourceBundle(), host, port,
//				version, oid, retries, timeout));
    }

    @MethodFilter
    public String snmpGetOidUpdate() {
        SnmpOidGet snmpOidGet = Common.getUser().getTestingUtility(
                SnmpOidGet.class);
        if (snmpOidGet == null)
            return null;
        return snmpOidGet.getResult();
    }

//	@MethodFilter
//	public String snmpGetWalkUpdate() {
//		SnmpOidWalk snmpOidWalk = Common.getUser().getTestingUtility(
//				SnmpOidWalk.class);
//		if (snmpOidWalk == null)
//			return null;
//		return snmpOidWalk.getResult();
//	}

    //
    //
    // SQL stuff
    //
    @MethodFilter
    public DwrResponseI18n saveSqlDataSource(String name, String xid,
                                             int updatePeriods, int updatePeriodType, String driverClassname,
                                             String connectionUrl, String username, String password,
                                             String selectStatement, boolean rowBasedQuery) {
        SqlDataSourceVO ds = (SqlDataSourceVO) Common.getUser()
                .getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setUpdatePeriods(updatePeriods);
        ds.setUpdatePeriodType(updatePeriodType);
        ds.setDriverClassname(driverClassname);
        ds.setConnectionUrl(connectionUrl);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setSelectStatement(selectStatement);
        ds.setRowBasedQuery(rowBasedQuery);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public DwrResponseI18n saveSqlPointLocator(int id, String xid, String name,
                                               SqlPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }

    @MethodFilter
    public void sqlTestStatement(String driverClassname, String connectionUrl,
                                 String username, String password, String selectStatement,
                                 boolean rowBasedQuery) {
        User user = Common.getUser();
        Permissions.ensureDataSourcePermission(user);
        user.setTestingUtility(new SqlStatementTester(getResourceBundle(),
                driverClassname, connectionUrl, username, password,
                selectStatement, rowBasedQuery));
    }

    @MethodFilter
    public Map<String, Object> sqlTestStatementUpdate() {
        Map<String, Object> result = new HashMap<String, Object>();
        SqlStatementTester statementTester = Common.getUser()
                .getTestingUtility(SqlStatementTester.class);
        if (statementTester == null)
            return null;
        if (!statementTester.isDone())
            return null;

        if (statementTester.getErrorMessage() != null)
            result.put("error", statementTester.getErrorMessage());
        else
            result.put("resultTable", statementTester.getResultTable());
        return result;
    }

    //
    //
    // HTTP receiver stuff
    //
    @MethodFilter
    public DwrResponseI18n saveHttpReceiverDataSource(String name, String xid,
                                                      String[] ipWhiteList, String[] deviceIdWhiteList) {
        HttpReceiverDataSourceVO ds = (HttpReceiverDataSourceVO) Common
                .getUser().getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setIpWhiteList(ipWhiteList);
        ds.setDeviceIdWhiteList(deviceIdWhiteList);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public DwrResponseI18n saveHttpReceiverPointLocator(int id, String xid,
                                                        String name, HttpReceiverPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }

    @MethodFilter
    public void httpReceiverListenForData(String[] ipWhiteList,
                                          String[] deviceIdWhiteList) {
        User user = Common.getUser();
        Permissions.ensureDataSourcePermission(user);
        user.setTestingUtility(new HttpReceiverDataListener(
                getResourceBundle(), ipWhiteList, deviceIdWhiteList));
    }

    @MethodFilter
    public Map<String, Object> httpReceiverListenerUpdate() {
        Map<String, Object> result = new HashMap<String, Object>();
        HttpReceiverDataListener l = Common.getUser().getTestingUtility(
                HttpReceiverDataListener.class);
        if (l == null)
            return null;

        HttpReceiverData data = l.getData();
        if (data != null) {
            result.put("remoteIp", data.getRemoteIp());
            result.put("deviceId", data.getDeviceId());
            result.put("time", DateFunctions.getTime(data.getTime()));
            result.put("data", data.getData());
        }
        result.put("message", l.getMessage());

        return result;
    }

    @MethodFilter
    public String validateIpMask(String ipMask) {
        return IpAddressUtils.checkIpMask(ipMask);
    }

    //
    //
    // OneWire stuff
    //
    @MethodFilter
    public DwrResponseI18n saveOneWireDataSource(String name, String xid,
                                                 String commPortId, int updatePeriodType, int updatePeriods,
                                                 int rescanPeriodType, int rescanPeriods) {
        OneWireDataSourceVO ds = (OneWireDataSourceVO) Common.getUser()
                .getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setCommPortId(commPortId);
        ds.setUpdatePeriodType(updatePeriodType);
        ds.setUpdatePeriods(updatePeriods);
        ds.setRescanPeriodType(rescanPeriodType);
        ds.setRescanPeriods(rescanPeriods);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public DwrResponseI18n saveOneWirePointLocator(int id, String xid,
                                                   String name, OneWirePointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }

    @MethodFilter
    public DwrResponseI18n readOneWireNetwork(String commPointId) {
        DwrResponseI18n response = new DwrResponseI18n();

        Network network = null;
        try {
            // Check for an RT that already has a network.
            OneWireDataSourceVO ds = (OneWireDataSourceVO) Common.getUser()
                    .getEditDataSource();
            OneWireDataSourceRT rt = (OneWireDataSourceRT) Common.ctx
                    .getRuntimeManager().getRunningDataSource(ds.getId());
            if (rt != null)
                // Use the existing one if it exists (i.e. initialized properly)
                network = rt.getNetwork();

            if (network == null)
                // Create a new one.
                network = new Network(commPointId);

            try {
                network.lock();
                network.quickInitialize();

                List<Long> addresses = network.getAddresses();
                List<OneWireContainerInfo> devices = new ArrayList<OneWireContainerInfo>();
                for (Long address : addresses) {
                    NetworkPath path = network.getNetworkPath(address);
                    if (!path.isCoupler())
                        devices.add(path.getTargetInfo());
                }

                response.addData("devices", devices);
            } finally {
                network.unlock();
            }
        } catch (Exception e) {
            response.addGenericMessage("common.default", e.getMessage());
        } finally {
            try {
                if (network != null)
                    network.terminate();
            } catch (OneWireException e) {
                // no op
            }
        }

        return response;
    }

    @MethodFilter
    public DataPointVO addOneWirePoint(String address) {
        DataPointVO dp = getPoint(Common.NEW_ID, null);
        OneWirePointLocatorVO locator = dp.getPointLocator();
        locator.setAddress(address);
        return dp;
    }

    //
    //
    // Meta stuff
    //
    @MethodFilter
    public DwrResponseI18n saveMetaDataSource(String name, String xid) {
        MetaDataSourceVO ds = (MetaDataSourceVO) Common.getUser()
                .getEditDataSource();
        ds.setXid(xid);
        ds.setName(name);
        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public DwrResponseI18n saveMetaPointLocator(int id, String xid,
                                                String name, MetaPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }

    @MethodFilter
    public DwrResponseI18n validateScript(String script,
                                          List<IntValuePair> context, int dataTypeId) {
        DwrResponseI18n response = new DwrResponseI18n();

        ScriptExecutor executor = new ScriptExecutor();
        try {
            Map<String, IDataPoint> convertedContext = executor
                    .convertContext(context);
            PointValueTime pvt = executor.execute(script, convertedContext,
                    System.currentTimeMillis(), dataTypeId, -1);
            if (pvt.getTime() == -1)
                response.addContextualMessage("script",
                        "dsEdit.meta.test.success", pvt.getValue());
            else
                response.addContextualMessage("script",
                        "dsEdit.meta.test.successTs", pvt.getValue(),
                        DateFunctions.getTime(pvt.getTime()));
        } catch (DataPointStateException e) {
            response.addMessage("context", e.getLocalizableMessage());
        } catch (ScriptException e) {
            response.addContextualMessage("script",
                    "dsEdit.meta.test.scriptError", e.getMessage());
        } catch (ResultTypeException e) {
            response.addMessage("script", e.getLocalizableMessage());
        }

        return response;
    }

    //
    //
    // BACnet I/P stuff
    //
    @MethodFilter
    public DwrResponseI18n saveBACnetIpDataSource(String name, String xid,
                                                  int updatePeriods, int updatePeriodType, int deviceId,
                                                  String broadcastAddress, int port, int timeout, int segTimeout,
                                                  int segWindow, int retries, int covSubscriptionTimeoutMinutes,
                                                  int maxReadMultipleReferencesSegmented,
                                                  int maxReadMultipleReferencesNonsegmented) {
        BACnetIPDataSourceVO ds = (BACnetIPDataSourceVO) Common.getUser()
                .getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setUpdatePeriods(updatePeriods);
        ds.setUpdatePeriodType(updatePeriodType);
        ds.setDeviceId(deviceId);
        ds.setBroadcastAddress(broadcastAddress);
        ds.setPort(port);
        ds.setTimeout(timeout);
        ds.setSegTimeout(segTimeout);
        ds.setSegWindow(segWindow);
        ds.setRetries(retries);
        ds.setCovSubscriptionTimeoutMinutes(covSubscriptionTimeoutMinutes);
        ds.setMaxReadMultipleReferencesSegmented(maxReadMultipleReferencesSegmented);
        ds.setMaxReadMultipleReferencesNonsegmented(maxReadMultipleReferencesNonsegmented);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public DwrResponseI18n saveBACnetIPPointLocator(int id, String xid,
                                                    String name, BACnetIPPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }

    @MethodFilter
    public void sendBACnetWhoIs(int deviceId, String broadcastAddress,
                                int port, int timeout, int segTimeout, int segWindow, int retries,
                                int whoIsPort, int maxReadMultipleReferencesSegmented,
                                int maxReadMultipleReferencesNonsegmented) {
        User user = Common.getUser();
        Permissions.ensureDataSourcePermission(user);

        BACnetDiscovery whoIs = new BACnetDiscovery(getResourceBundle(),
                deviceId, broadcastAddress, port, timeout, segTimeout,
                segWindow, retries, whoIsPort,
                maxReadMultipleReferencesSegmented,
                maxReadMultipleReferencesNonsegmented);
        user.setTestingUtility(whoIs);
    }

    @MethodFilter
    public Map<String, Object> bacnetWhoIsUpdate() {
        Map<String, Object> result = new HashMap<String, Object>();
        BACnetDiscovery test = Common.getUser().getTestingUtility(
                BACnetDiscovery.class);
        if (test == null)
            return null;

        test.addUpdateInfo(result);

        return result;
    }

    @MethodFilter
    public int getBACnetDeviceDetails(int index) {
        BACnetDiscovery test = Common.getUser().getTestingUtility(
                BACnetDiscovery.class);
        if (test == null)
            return index;
        test.getDeviceDetails(index);
        return -1;
    }

    @MethodFilter
    public DwrResponseI18n sendObjectListRequest(int deviceId,
                                                 String broadcastAddress, int port, int timeout, int segTimeout,
                                                 int segWindow, int retries, int maxReadMultipleReferencesSegmented,
                                                 int maxReadMultipleReferencesNonsegmented, String remoteIp,
                                                 int remotePort, int networkNumber, String networkAddress,
                                                 int remoteDeviceId) {
        LocalDevice localDevice = new LocalDevice(deviceId, broadcastAddress);
        localDevice.setPort(port);
        localDevice.setTimeout(timeout);
        localDevice.setSegTimeout(segTimeout);
        localDevice.setSegWindow(segWindow);
        localDevice.setRetries(retries);
        localDevice
                .setMaxReadMultipleReferencesSegmented(maxReadMultipleReferencesSegmented);
        localDevice
                .setMaxReadMultipleReferencesNonsegmented(maxReadMultipleReferencesNonsegmented);

        DwrResponseI18n result = new DwrResponseI18n();
        try {
            localDevice.initialize();

            Address address = new Address(InetAddress.getByName(remoteIp)
                    .getAddress(), remotePort);
            com.serotonin.bacnet4j.Network network = null;
            if (!StringUtils.isEmpty(networkAddress))
                network = new com.serotonin.bacnet4j.Network(networkNumber,
                        networkAddress);
            RemoteDevice d = localDevice.findRemoteDevice(address, network,
                    remoteDeviceId);

            List<BACnetObjectBean> details = BACnetDiscovery.getDetails(
                    localDevice, d);

            result.addData("deviceAddress", d.getAddress().toIpString());
            result.addData("deviceName", d.getName());
            result.addData("deviceIp", d.getAddress().toIpString());
            result.addData("devicePort", d.getAddress().getPort());
            network = d.getNetwork();
            if (network != null) {
                result.addData("deviceNetworkNumber",
                        network.getNetworkNumber());
                result.addData("deviceNetworkAddress",
                        network.getNetworkAddressDottedString());
            }
            result.addData("deviceInstanceNumber", d.getObjectIdentifier()
                    .getInstanceNumber());
            result.addData("deviceDescription",
                    BACnetDiscovery.getDeviceDescription(d));
            result.addData("deviceDetails", details);
        } catch (Exception e) {
            result.addData("error", e.getMessage());
        } finally {
            localDevice.terminate();
        }

        return result;
    }

    @MethodFilter
    public DataPointVO addBacnetPoint(String ip, int port, int networkNumber,
                                      String networkAddress, int deviceInstanceNumber,
                                      BACnetObjectBean bean) {
        DataPointVO dp = getPoint(Common.NEW_ID, null);
        BACnetIPPointLocatorVO locator = dp.getPointLocator();

        dp.setName(bean.getObjectName());

        // Default some of the locator values.
        locator.setRemoteDeviceIp(ip);
        locator.setRemoteDevicePort(port);
        locator.setNetworkNumber(networkNumber);
        locator.setNetworkAddress(networkAddress);
        locator.setRemoteDeviceInstanceNumber(deviceInstanceNumber);
        locator.setObjectTypeId(bean.getObjectTypeId());
        locator.setObjectInstanceNumber(bean.getInstanceNumber());
        locator.setPropertyIdentifierId(PropertyIdentifier.presentValue
                .intValue());
        locator.setDataTypeId(bean.getDataTypeId());
        locator.setUseCovSubscription(bean.isCov());

        // We would like to default text renderer values too, but it's rather
        // inconvenient to do.

        return dp;
    }

    //
    //
    // HTTP Retriever stuff
    //
    @MethodFilter
    public DwrResponseI18n saveHttpRetrieverDataSource(String name, String xid,
                                                       int updatePeriods, int updatePeriodType, String url,
                                                       int timeoutSeconds, int retries, boolean stop,
                                                       String username, String password) {
        HttpRetrieverDataSourceVO ds = (HttpRetrieverDataSourceVO) Common
                .getUser().getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setUpdatePeriods(updatePeriods);
        ds.setUpdatePeriodType(updatePeriodType);
        ds.setUrl(url);
        ds.setTimeoutSeconds(timeoutSeconds);
        ds.setRetries(retries);
        ds.setStop(stop);
        setAuthorizationStaticHeader(ds, username, password);

        return tryDataSourceSave(ds);
    }

    private static void setAuthorizationStaticHeader(HttpRetrieverDataSourceVO ds, String username, String password) {
        toBasicCredentials(username, password).ifPresent(headerValue -> {
            if (ds.getStaticHeaders().isEmpty() || !containsKey(ds.getStaticHeaders(), "Authorization")) {
                ds.getStaticHeaders().add(new KeyValuePair("Authorization", headerValue));
            } else {
                for (KeyValuePair kvp : ds.getStaticHeaders()) {
                    if (kvp.getKey().equalsIgnoreCase("Authorization")) {
                        kvp.setValue(headerValue);
                    }
                }
            }
        });
    }

    private static boolean containsKey(List<KeyValuePair> staticHeaders, String key) {
        for (KeyValuePair kvp : staticHeaders) {
            if (kvp.getKey().equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

    private static Optional<String> toBasicCredentials(String username, String password) {
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            byte[] credentials = (username + ':' + password).getBytes();
            return Optional.of("Basic " + Base64.getEncoder().encodeToString(credentials));
        }
        return Optional.empty();
    }

    public static String[] getBasicCredentials(List<KeyValuePair> staticHeaders) {
        return getAuthorization(staticHeaders)
                .filter(authorization -> authorization.startsWith("Basic")
                        || authorization.startsWith("basic"))
                .map(authorization -> {
                    String base64Credentials = authorization.substring("Basic".length()).trim();
                    byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
                    String credentials = new String(credDecoded, StandardCharsets.UTF_8);
                    // credentials = username:password
                    return credentials.split(":", 2);
                })
                .orElseGet(() -> new String[]{});
    }

    private static Optional<String> getAuthorization(List<KeyValuePair> staticHeaders) {
        for (KeyValuePair kvp : staticHeaders) {
            if (kvp.getKey().equalsIgnoreCase("Authorization")) {
                return Optional.ofNullable(kvp.getValue());
            }
        }
        return Optional.empty();
    }

    public DwrResponseI18n initHttpRetriever() {
        HttpRetrieverDataSourceVO ds = (HttpRetrieverDataSourceVO) Common
                .getUser().getEditDataSource();

        List<KeyValuePair> staticHeaders = ds.getStaticHeaders();

        DwrResponseI18n response = new DwrResponseI18n();
        response.addData("staticHeaders", staticHeaders);
        return response;
    }

    @MethodFilter
    public DwrResponseI18n saveHttpRetrieverDataSourceWithReactivationOptions(String name, String xid,
                                                                              int updatePeriods, int updatePeriodType, String url,
                                                                              int timeoutSeconds, int retries, boolean stop, boolean sleep, short typeReactivation, short valueReactivation,
                                                                              String username, String password) {
        HttpRetrieverDataSourceVO ds = (HttpRetrieverDataSourceVO) Common
                .getUser().getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setUpdatePeriods(updatePeriods);
        ds.setUpdatePeriodType(updatePeriodType);
        ds.setUrl(url);
        ds.setTimeoutSeconds(timeoutSeconds);
        ds.setRetries(retries);
        ds.setStop(stop);
        ReactivationDs rDs = new ReactivationDs(sleep, typeReactivation, valueReactivation);
        ds.setReactivation(rDs);
        setAuthorizationStaticHeader(ds, username, password);

        DwrResponseI18n result;

        if (ds.getId() > 0) {
            ReactivationManager.getInstance().stopReactivation(ds.getId());
        }
        result = tryDataSourceSave(ds);

        if (sleep) {
            ReactivationManager.getInstance().startReactivation(ds.getId());
        }

        return result;
    }


    @MethodFilter
    public DwrResponseI18n saveHttpRetrieverPointLocator(int id, String xid,
                                                         String name, HttpRetrieverPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }

    @MethodFilter
    public String testHttpRetrieverValueParams(String url, int timeoutSeconds,
                                               int retries, String valueRegex, int dataTypeId, String valueFormat,
                                               List<KeyValuePair> staticHeaders) {
        try {
            String data = HttpRetrieverDataSourceRT.getData(url,
                    timeoutSeconds, retries, staticHeaders);

            Pattern valuePattern = Pattern.compile(valueRegex);
            DecimalFormat decimalFormat = null;
            if (dataTypeId == DataTypes.NUMERIC
                    && !StringUtils.isEmpty(valueFormat))
                decimalFormat = new DecimalFormat(valueFormat);
            MangoValue value = DataSourceUtils.getValue(valuePattern, data,
                    dataTypeId, valueFormat, null, decimalFormat, null);
            return getMessage("common.result") + ": " + value.toString();
        } catch (LocalizableException e) {
            return getMessage(e.getLocalizableMessage());
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @MethodFilter
    public String testHttpRetrieverTimeParams(String url, int timeoutSeconds,
                                              int retries, String timeRegex, String timeFormat,
                                              List<KeyValuePair> staticHeaders) {
        try {
            String data = HttpRetrieverDataSourceRT.getData(url,
                    timeoutSeconds, retries, staticHeaders);

            Pattern timePattern = Pattern.compile(timeRegex);
            DateFormat dateFormat = new SimpleDateFormat(timeFormat);
            long time = DataSourceUtils.getValueTime(
                    System.currentTimeMillis(), timePattern, data, dateFormat,
                    null);
            return DateFunctions.getTime(time);
        } catch (LocalizableException e) {
            return getMessage(e.getLocalizableMessage());
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    //
    //
    // HTTP Image stuff
    //
    @MethodFilter
    public DwrResponseI18n saveHttpImageDataSource(String name, String xid,
                                                   int updatePeriods, int updatePeriodType) {
        HttpImageDataSourceVO ds = (HttpImageDataSourceVO) Common.getUser()
                .getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setUpdatePeriods(updatePeriods);
        ds.setUpdatePeriodType(updatePeriodType);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public DwrResponseI18n saveHttpImagePointLocator(int id, String xid,
                                                     String name, HttpImagePointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, new DataPointDefaulter() {
            @Override
            public void setDefaultValues(DataPointVO dp) {
                if (dp.isNew())
                    dp.setLoggingType(DataPointVO.LoggingTypes.NONE);
            }
        });
    }

    //
    //
    // POP3 Email stuff
    //
    @MethodFilter
    public DwrResponseI18n savePop3DataSource(String name, String xid,
                                              int updatePeriods, int updatePeriodType, String pop3Server,
                                              String username, String password) {
        Pop3DataSourceVO ds = (Pop3DataSourceVO) Common.getUser()
                .getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setUpdatePeriods(updatePeriods);
        ds.setUpdatePeriodType(updatePeriodType);
        ds.setPop3Server(pop3Server);
        ds.setUsername(username);
        ds.setPassword(password);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public DwrResponseI18n savePop3PointLocator(int id, String xid,
                                                String name, Pop3PointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }

    @MethodFilter
    public String testPop3ValueParams(String testData, String valueRegex,
                                      int dataTypeId, String valueFormat) {
        try {
            Pattern valuePattern = Pattern.compile(valueRegex);
            DecimalFormat decimalFormat = null;
            if (dataTypeId == DataTypes.NUMERIC
                    && !StringUtils.isEmpty(valueFormat))
                decimalFormat = new DecimalFormat(valueFormat);
            MangoValue value = DataSourceUtils.getValue(valuePattern, testData,
                    dataTypeId, valueFormat, null, decimalFormat, null);
            return getMessage("common.result") + ": " + value.toString();
        } catch (LocalizableException e) {
            return getMessage(e.getLocalizableMessage());
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @MethodFilter
    public String testPop3TimeParams(String testData, String timeRegex,
                                     String timeFormat) {
        try {
            Pattern timePattern = Pattern.compile(timeRegex);
            DateFormat dateFormat = new SimpleDateFormat(timeFormat);
            long time = DataSourceUtils.getValueTime(
                    System.currentTimeMillis(), timePattern, testData,
                    dateFormat, null);
            return DateFunctions.getTime(time);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    //
    //
    // NMEA stuff
    //
    @MethodFilter
    public DwrResponseI18n saveNmeaDataSource(String name, String xid,
                                              String commPortId, int baudRate, int resetTimeout) {
        NmeaDataSourceVO ds = (NmeaDataSourceVO) Common.getUser()
                .getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setCommPortId(commPortId);
        ds.setBaudRate(baudRate);
        ds.setResetTimeout(resetTimeout);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public DwrResponseI18n saveNmeaPointLocator(int id, String xid,
                                                String name, NmeaPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }

    @MethodFilter
    public void nmeaListenForMessages(String commPortId, int baudRate) {
        User user = Common.getUser();
        Permissions.ensureDataSourcePermission(user);
        user.setTestingUtility(new NmeaUtilListener(getResourceBundle(),
                commPortId, baudRate));
    }

    @MethodFilter
    public Map<String, Object> nmeaListenerUpdate() {
        NmeaUtilListener l = Common.getUser().getTestingUtility(
                NmeaUtilListener.class);
        if (l == null)
            return null;

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("messages", l.getMessages());
        result.put("message", l.getMessage());

        return result;
    }

    //
    //
    // Galil stuff
    //
    @MethodFilter
    public DwrResponseI18n saveGalilDataSource(String name, String xid,
                                               String host, int port, int timeout, int retries, int updatePeriods,
                                               int updatePeriodType) {
        GalilDataSourceVO ds = (GalilDataSourceVO) Common.getUser()
                .getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setHost(host);
        ds.setPort(port);
        ds.setTimeout(timeout);
        ds.setRetries(retries);
        ds.setUpdatePeriods(updatePeriods);
        ds.setUpdatePeriodType(updatePeriodType);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public DwrResponseI18n saveGalilPointLocator(int id, String xid,
                                                 String name, GalilPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }

    @MethodFilter
    public String galilTestCommand(String host, int port, int timeout,
                                   String command) {
        User user = Common.getUser();
        Permissions.ensureDataSourcePermission(user);

        try {
            GalilCommandTester tester = new GalilCommandTester(
                    getResourceBundle(), host, port, timeout, command);
            try {
                tester.join();
                return tester.getResult();
            } catch (InterruptedException e) {
                return e.getMessage();
            }
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    //
    //
    // EBI25 stuff
    //
    @MethodFilter
    public DwrResponseI18n saveEBI25DataSource(String name, String xid,
                                               int updatePeriods, int updatePeriodType, int timeout, int retries,
                                               String host, int port, boolean keepAlive) {
        EBI25DataSourceVO ds = (EBI25DataSourceVO) Common.getUser()
                .getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setUpdatePeriods(updatePeriods);
        ds.setUpdatePeriodType(updatePeriodType);
        ds.setTimeout(timeout);
        ds.setRetries(retries);
        ds.setHost(host);
        ds.setPort(port);
        ds.setKeepAlive(keepAlive);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public EBI25InterfaceReader ebi25ReadInterface(String host, int port,
                                                   boolean keepAlive, int timeout, int retries) {
        User user = Common.getUser();
        Permissions.ensureDataSourcePermission(user);
        EBI25DataSourceVO ds = (EBI25DataSourceVO) Common.getUser()
                .getEditDataSource();

        EBI25InterfaceReader reader = new EBI25InterfaceReader(
                getResourceBundle(), ds, host, port, keepAlive, timeout,
                retries);

        if (reader.getErrorMessage() == null) {
            tryDataSourceSave(ds);
            reader.setPoints(getPoints());
        }

        return reader;
    }

    @MethodFilter
    public LocalizableMessage ebi25SyncTime(String host, int port, int timeout,
                                            int retries) {
        return new EBI25InterfaceUpdater().updateSysTime(host, port, timeout,
                retries);
    }

    @MethodFilter
    public DwrResponseI18n saveEBI25PointLocator(int id, String xid,
                                                 String name, final EBI25PointLocatorVO locator) {
        DwrResponseI18n response = new DwrResponseI18n();

        if (locator.getType() == EBI25PointLocatorVO.TYPE_VALUE) {
            EBI25DataSourceVO ds = (EBI25DataSourceVO) Common.getUser()
                    .getEditDataSource();

            if (locator.getSampleRate() < 1)
                response.addContextualMessage("sampleRate",
                        "validate.greaterThanZero");
            else {
                // Try to update the EBI25 interface
                LocalizableMessage errorMessage = new EBI25InterfaceUpdater()
                        .updateLogger(ds.getHost(), ds.getPort(),
                                ds.getTimeout(), ds.getRetries(), locator);

                if (errorMessage == null) {
                    response = validatePoint(id, xid, name, locator,
                            new DataPointDefaulter() {
                                @Override
                                public void setDefaultValues(DataPointVO dp) {
                                    // Update the point's high/low detectors
                                    // with the locator values.
                                    PointEventDetectorVO high = EBI25Constants
                                            .findDetector(
                                                    dp.getEventDetectors(),
                                                    true);
                                    if (high != null)
                                        high.setLimit(locator.getHighLimit());

                                    PointEventDetectorVO low = EBI25Constants
                                            .findDetector(
                                                    dp.getEventDetectors(),
                                                    false);
                                    if (low != null)
                                        low.setLimit(locator.getLowLimit());
                                }
                            });
                } else
                    response.addMessage("sampleRate", errorMessage);
            }
        } else
            response = validatePoint(id, xid, name, locator, null);

        return response;
    }

    //
    //
    // VMStat stuff
    //
    @MethodFilter
    public DwrResponseI18n saveVMStatDataSource(String name, String xid,
                                                int pollSeconds, int outputScale) {
        VMStatDataSourceVO ds = (VMStatDataSourceVO) Common.getUser()
                .getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setPollSeconds(pollSeconds);
        ds.setOutputScale(outputScale);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public DwrResponseI18n saveVMStatPointLocator(int id, String xid,
                                                  String name, VMStatPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }

    //
    //
    // Viconics stuff
    //
    @MethodFilter
    public DwrResponseI18n saveViconicsDataSource(String name, String xid,
                                                  String commPortId, int panId, int channel, int timeout,
                                                  int retries, int networkTimeoutSeconds,
                                                  int deviceWarningTimeoutSeconds, int deviceRemoveTimeoutSeconds,
                                                  int pointValueMinimumFreshnessSeconds, boolean convertToCelsius) {
        ViconicsDataSourceVO ds = (ViconicsDataSourceVO) Common.getUser()
                .getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setCommPortId(commPortId);
        ds.setPanId(panId);
        ds.setChannel(channel);
        ds.setTimeout(timeout);
        ds.setRetries(retries);
        ds.setNetworkTimeoutSeconds(networkTimeoutSeconds);
        ds.setDeviceWarningTimeoutSeconds(deviceWarningTimeoutSeconds);
        ds.setDeviceRemoveTimeoutSeconds(deviceRemoveTimeoutSeconds);
        ds.setPointValueMinimumFreshnessSeconds(pointValueMinimumFreshnessSeconds);
        ds.setConvertToCelsius(convertToCelsius);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public DwrResponseI18n saveViconicsPointLocator(int id, String xid,
                                                    String name, ViconicsPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }

    @MethodFilter
    public DwrResponseI18n getRfModuleInfo() {
        ViconicsDataSourceVO ds = (ViconicsDataSourceVO) Common.getUser()
                .getEditDataSource();

        ViconicsDataSourceRT rt = (ViconicsDataSourceRT) Common.ctx
                .getRuntimeManager().getRunningDataSource(ds.getId());
        DwrResponseI18n response = new DwrResponseI18n();

        if (rt == null)
            response.addGenericMessage("dsEdit.viconics.dataSourceNotStarted");
        else {
            try {
                NetworkIdentifyResponse res = rt
                        .send(new NetworkIdentifyRequest());

                Map<String, String> rfm = new HashMap<String, String>();
                rfm.put("firmware", ViconicsNetwork.getRevisionString(res
                        .getZigbeeFirmwareRevision()));
                rfm.put("networkAddr",
                        Integer.toString(res.getZigbeeNetworkAddress()));
                rfm.put("ieee", ViconicsNetwork.getIeeeString(res.getIeee()));
                rfm.put("chipRevision", Integer.toString(res.getChipRevision()));
                response.addData("rfm", rfm);

                List<Map<String, String>> devices = new ArrayList<Map<String, String>>();
                for (ViconicsDevice device : rt.getDevices()) {
                    Map<String, String> dev = new HashMap<String, String>();
                    dev.put("commAddr",
                            Integer.toString(device.getCommAddress()));
                    dev.put("modelNumber", device.getConfiguration().getModel());
                    dev.put("firmware", ViconicsNetwork
                            .getRevisionString(device.getFirmwareRevision()));
                    dev.put("zigbeeFirmware", ViconicsNetwork
                            .getRevisionString(device
                                    .getZigbeeFirmwareRevision()));
                    dev.put("zigbeeNetworkAddr",
                            Integer.toString(device.getZigbeeNetworkAddress()));
                    dev.put("ieee",
                            ViconicsNetwork.getIeeeString(device.getIeee()));
                    dev.put("chipRevision",
                            Integer.toString(device.getChipRevision()));
                    dev.put("crss",
                            Integer.toString(device.getLinkQualityPercent())
                                    + "%");
                    dev.put("trss",
                            Integer.toString(device.getReturnQualityPercent())
                                    + "%");
                    devices.add(dev);
                }
                response.addData("devices", devices);
            } catch (ViconicsTransportException e) {
                response.addGenericMessage(
                        "dsEdit.viconics.networkIdentifyFailure",
                        e.getMessage());
            } catch (RequestFailureException e) {
                response.addGenericMessage(
                        "dsEdit.viconics.networkIdentifyFailure",
                        e.getMessage());
            }
        }

        return response;
    }

    //
    //
    // MBus stuff
    //
    public DwrResponseI18n saveMBusDataSource(String name, String xid,
                                              TcpIpConnection connection, int updatePeriodType, int updatePeriods) {
        MBusDataSourceVO ds = (MBusDataSourceVO) Common.getUser()
                .getEditDataSource();
        ds.setXid(xid);
        ds.setName(name);
        ds.setConnection(connection);
        ds.setUpdatePeriodType(updatePeriodType);
        ds.setUpdatePeriods(updatePeriods);
        return tryDataSourceSave(ds);
    }

    public DwrResponseI18n saveMBusPointLocator(int id, String xid,
                                                String name, MBusPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }

    public Connection getMBusConn() {
        TcpIpConnection conn = new TcpIpConnection();
        conn.setHost("localhost");
        conn.setPort(2000);
        return conn;
    }

    public void searchMBus(TcpIpConnection conn, PrimaryAddressingSearch addressing) {
        User user = Common.getUser();

        Permissions.ensureDataSourcePermission(user);

        MBusDiscovery discovery = new MBusDiscovery(getResourceBundle(), conn,
                addressing);
        discovery.start();
        user.setTestingUtility(discovery);
    }

    public void searchMBus(TcpIpConnection conn, SecondaryAddressingSearch addressing) {
        User user = Common.getUser();

        Permissions.ensureDataSourcePermission(user);

        MBusDiscovery discovery = new MBusDiscovery(getResourceBundle(), conn,
                addressing);
        discovery.start();
        user.setTestingUtility(discovery);
    }


    public Map<String, Object> mBusSearchUpdate() {
        Map<String, Object> result = new HashMap<String, Object>();
        MBusDiscovery test = Common.getUser().getTestingUtility(
                MBusDiscovery.class);
        if (test == null)
            return null;

        test.addUpdateInfo(result);
        return result;
    }

    public Map<String, Object> getMBusResponseFrames(int deviceIndex) {
        Map<String, Object> result = new HashMap<String, Object>();
        MBusDiscovery test = Common.getUser().getTestingUtility(
                MBusDiscovery.class);
        if (test == null)
            return null;

        test.getDeviceDetails(deviceIndex, result);
        return result;
    }

    public DataPointVO addMBusPoint(String addressing, int deviceIndex,
                                    int rsIndex, int dbIndex) {
        DataPointVO dp = getPoint(Common.NEW_ID, null);
        MBusPointLocatorVO locator = (MBusPointLocatorVO) dp.getPointLocator();

        MBusDiscovery test = Common.getUser().getTestingUtility(
                MBusDiscovery.class);
        if (test == null)
            return null;

        MBusResponseFramesContainer dev = test.getDevice(deviceIndex);
        if (dev.getResponseFrameContainer(rsIndex).getResponseFrame() instanceof UserDataResponse) {
            UserDataResponse udr = (UserDataResponse) dev
                    .getResponseFrameContainer(rsIndex).getResponseFrame();
            DataBlock db = udr.getDataBlock(dbIndex);

            dp.setName(db.getParamDescr());

            locator.setAddressing(MBusAddressing.valueOf(addressing));
            locator.setAddress(dev.getAddress());
            locator.setMedium(dev.getMedium().getLabel());
            locator.setManufacturer(dev.getManufacturer());
            locator.setVersion(dev.getVersion());
            locator.setIdentNumber(dev.getIdentNumber());
            locator.setResponseFrame(dev.getResponseFrameContainer(rsIndex)
                    .getName());
            locator.setDeviceUnit(db.getSubUnit());
            locator.setDifCode(db.getDataFieldCode().getLabel());
            locator.setFunctionField(db.getFunctionField().getLabel());
            locator.setStorageNumber(db.getStorageNumber());
            locator.setTariff(db.getTariff());
            locator.setSiPrefix(db.getVif().getSiPrefix() == null ? null : db
                    .getVif().getSiPrefix().getLabel());
            locator.setUnitOfMeasurement(db.getVif().getUnitOfMeasurement() == null ? null
                    : db.getVif().getUnitOfMeasurement().getLabel());
            locator.setVifType(db.getVif().getVifType().getLabel());
            locator.setVifLabel(db.getVif().getLabel());
            locator.setExponent(db.getVif().getExponent());
            if (db.getVifes() != null) {
                final String[] vifeLabels = new String[db.getVifes().length];
                final String[] vifeTypes = new String[db.getVifes().length];
                for (int i = 0; i < vifeLabels.length; i++) {
                    vifeTypes[i] = db.getVifes()[i].getVifeType().getLabel();
                    vifeLabels[i] = db.getVifes()[i].getLabel();
                }
                locator.setVifeTypes(vifeTypes);
                locator.setVifeTypes(vifeTypes);
                locator.setVifeLabels(vifeLabels);
            } else {
                locator.setVifeLabels(null);
            }
        }
        return dp;
    }

    //
    //
    // OpenV4J stuff
    //
    public void searchOpenV4J(String commPortId) {
        User user = Common.getUser();
        Permissions.ensureDataSourcePermission(user);

        OpenV4JDiscovery discovery = OpenV4JDiscovery.searchDataPoints(
                getResourceBundle(), commPortId);
        user.setTestingUtility(discovery);
    }

    public void detectOpenV4JDevice(String commPortId) {
        User user = Common.getUser();
        Permissions.ensureDataSourcePermission(user);

        OpenV4JDiscovery discovery = OpenV4JDiscovery.detectDevice(
                getResourceBundle(), commPortId);
        ;
        user.setTestingUtility(discovery);
    }

    public DwrResponseI18n saveOpenV4JDataSource(String name, String xid,
                                                 String commPortId, int updatePeriodType, int updatePeriods,
                                                 String device, String protocol) {
        OpenV4JDataSourceVO ds = (OpenV4JDataSourceVO) Common.getUser()
                .getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setCommPortId(commPortId);
        ds.setUpdatePeriodType(updatePeriodType);
        ds.setUpdatePeriods(updatePeriods);
        ds.setDevice(Devices.valueOf(device));
        ds.setProtocol(Protocol.valueOf(protocol));
        return tryDataSourceSave(ds);
    }

    public DwrResponseI18n saveOpenV4JPointLocator(int id, String xid,
                                                   String name, OpenV4JPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }

    public Map<String, Object> openV4JSearchUpdate() {
        Map<String, Object> result = new HashMap<String, Object>();
        OpenV4JDiscovery test = Common.getUser().getTestingUtility(
                OpenV4JDiscovery.class);
        if (test == null)
            return null;

        test.addUpdateInfo(result);
        return result;
    }

    public Map<String, Object> openV4JDetectDeviceUpdate() {
        Map<String, Object> result = new HashMap<String, Object>();
        OpenV4JDiscovery test = Common.getUser().getTestingUtility(
                OpenV4JDiscovery.class);
        if (test == null)
            return null;

        test.addDeviceInfo(result);
        return result;
    }

    public OpenV4JProtocolBean[] getOpenV4jProtocolsOfDevice(String deviceName) {
        return OpenV4JProtocolBean.fromDevice(Devices.valueOf(deviceName));
    }

    public OpenV4JDataPointBean[] getOpenV4jDataPointsOfGroup(String groupName) {
        Group g = Group.valueOf(groupName);
        List<OpenV4JDataPointBean> result = new ArrayList<OpenV4JDataPointBean>();
        for (DataPoint dp : DataPoint.values()) {
            if (dp.getGroup().equals(g)) {
                result.add(new OpenV4JDataPointBean(dp));
            }
        }
        return result.toArray(new OpenV4JDataPointBean[result.size()]);
    }

    public DataPointVO addOpenV4JPoint(String openV4JEnumName) {
        DataPointVO result = getPoint(Common.NEW_ID, null);
        OpenV4JPointLocatorVO locator = (OpenV4JPointLocatorVO) result
                .getPointLocator();

        OpenV4JDiscovery test = Common.getUser().getTestingUtility(
                OpenV4JDiscovery.class);
        if (test == null) {
            return null;
        }

        final DataPoint dp = DataPoint.valueOf(openV4JEnumName);
        result.setName(dp.getGroup().getLabel() + " " + dp.getLabel());
        locator.setDataPointName(dp.getName());
        return result;
    }

    //

    //
    //
    // DNP3 stuff
    //
    @MethodFilter
    public DwrResponseI18n saveDNP3IpDataSource(String name, String xid,
                                                int sourceAddress, int slaveAddress, String host, int port,
                                                int staticPollPeriods, int rbePollPeriods, int rbePeriodType,
                                                boolean quantize, int timeout, int retries) {
        Dnp3IpDataSourceVO ds = (Dnp3IpDataSourceVO) Common.getUser()
                .getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setSourceAddress(sourceAddress);
        ds.setSlaveAddress(slaveAddress);
        ds.setHost(host);
        ds.setPort(port);

        ds.setStaticPollPeriods(staticPollPeriods);
        ds.setRbePeriodType(rbePeriodType);
        ds.setRbePollPeriods(rbePollPeriods);
        ds.setQuantize(quantize);
        ds.setTimeout(timeout);
        ds.setRetries(retries);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public DwrResponseI18n saveDNP3SerialDataSource(String name, String xid,
                                                    int sourceAddress, int slaveAddress, String commPortId,
                                                    int baudRate, int staticPollPeriods, int rbePollPeriods,
                                                    int rbePeriodType, boolean quantize, int timeout, int retries) {
        Dnp3SerialDataSourceVO ds = (Dnp3SerialDataSourceVO) Common.getUser()
                .getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setSourceAddress(sourceAddress);
        ds.setSlaveAddress(slaveAddress);
        ds.setCommPortId(commPortId);
        ds.setBaudRate(baudRate);
        ds.setStaticPollPeriods(staticPollPeriods);
        ds.setRbePeriodType(rbePeriodType);
        ds.setRbePollPeriods(rbePollPeriods);
        ds.setQuantize(quantize);
        ds.setTimeout(timeout);
        ds.setRetries(retries);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public DwrResponseI18n saveDnp3PointLocator(int id, String xid,
                                                String name, Dnp3PointLocatorVO locator) {
        DwrResponseI18n response = new DwrResponseI18n();

        if (locator.getTimeOn() < 0)
            response.addContextualMessage("timeOn",
                    "reports.validate.lessThan0");
        if (locator.getTimeOff() < 0)
            response.addContextualMessage("timeOff",
                    "reports.validate.lessThan0");

        List<DataPointVO> points = getPoints();
        Iterator<DataPointVO> itr = points.iterator();
        while (itr.hasNext()) {
            DataPointVO vo = (DataPointVO) itr.next();
            Dnp3PointLocatorVO loc = (Dnp3PointLocatorVO) vo.getPointLocator();
            if (loc.getDnp3DataType() == ((Dnp3PointLocatorVO) locator)
                    .getDnp3DataType()
                    && loc.getIndex() == ((Dnp3PointLocatorVO) locator)
                    .getIndex() && id != vo.getId()) {
                response.addContextualMessage("index",
                        "dsEdit.dnp3.validate.indexUsed");
            }
        }

        if (!response.getHasMessages())
            return validatePoint(id, xid, name, locator, null);
        return response;
    }

    @MethodFilter
    public DwrResponseI18n saveMultipleDnp3PointLocator(String[] names,
                                                        int[] index, Dnp3PointLocatorVO[] locators) {
        return validateMultipleDnp3Points(names, index, locators, null);
    }

    private DwrResponseI18n validateMultipleDnp3Points(String[] names,
                                                       int[] index, Dnp3PointLocatorVO[] locators,
                                                       DataPointDefaulter defaulter) {
        DwrResponseI18n response = new DwrResponseI18n();

        if (locators[0].getClass().equals(Dnp3PointLocatorVO.class)) {
            if (locators[0].getTimeOn() < 0)
                response.addContextualMessage("timeOn",
                        "reports.validate.lessThan0");
            if (locators[0].getTimeOff() < 0)
                response.addContextualMessage("timeOff",
                        "reports.validate.lessThan0");
        }

        for (int i = 0; i < names.length; i++) {
            locators[i].setIndex(index[i]);

            List<DataPointVO> points = getPoints();
            Iterator<DataPointVO> itr = points.iterator();
            while (itr.hasNext()) {
                Dnp3PointLocatorVO loc = (Dnp3PointLocatorVO) itr.next()
                        .getPointLocator();
                if (loc.getDnp3DataType() == ((Dnp3PointLocatorVO) locators[i])
                        .getDnp3DataType()
                        && loc.getIndex() == ((Dnp3PointLocatorVO) locators[i])
                        .getIndex()) {
                    response.addContextualMessage("index",
                            "dsEdit.dnp3.validate.someIndexUsed");
                }
            }
        }

        if (response.getHasMessages())
            return response;

        for (int i = 0; i < names.length; i++) {
            DataPointVO dp = getPoint(Common.NEW_ID, defaulter);
            dp.setName(names[i]);
            locators[i].setIndex(index[i]);
            dp.setPointLocator(locators[i]);

            if (StringUtils.isEmpty(dp.getXid()))
                response.addContextualMessage("xid", "validate.required");
            else if (!new DataPointDao()
                    .isXidUnique(dp.getXid(), Common.NEW_ID))
                response.addContextualMessage("xid", "validate.xidUsed");
            else if (StringUtils.isLengthGreaterThan(dp.getXid(), 50))
                response.addContextualMessage("xid", "validate.notLongerThan",
                        50);

            locators[i].validate(response);

            if (!response.getHasMessages()) {
                Common.ctx.getRuntimeManager().saveDataPoint(dp);
                response.addData("id", dp.getId());
                response.addData("points", getPoints());
            }
        }
        return response;
    }

    //
    // /
    // / OPC DA stuff
    // /
    //
    @MethodFilter
    public DwrResponseI18n saveOPCDataSource(String name, String xid,
                                             String host, String domain, String user, String password,
                                             String server, int updatePeriods, int updatePeriodType,
                                             boolean quantize) {
        OPCDataSourceVO<?> ds = (OPCDataSourceVO<?>) Common.getUser()
                .getEditDataSource();
        ds.setXid(xid);
        ds.setName(name);
        ds.setHost(host);
        ds.setDomain(domain);
        ds.setUser(user);
        ds.setPassword(password);
        ds.setServer(server);
        ds.setUpdatePeriods(updatePeriods);
        ds.setUpdatePeriodType(updatePeriodType);
        ds.setQuantize(quantize);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public DwrResponseI18n saveOPCPointLocator(int id, String xid, String name,
                                               VirtualPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }

    public ArrayList<String> searchOpcServer(String host, String domain,
                                             String user, String password) {

        Logger log = JISystem.getLogger();
        log.setLevel(Level.OFF);

        ArrayList<String> serverList = new ArrayList<String>();
        try {
            serverList = new RealOPCMaster().listOPCServers(user, password,
                    host, domain);
        } catch (Exception e) {
            serverList.add("-1");
            serverList.add(e.getMessage());
        }
        return serverList;
    }

    public ArrayList<OPCItem> listOPCTags(String host, String domain,
                                          String user, String password, String serverName) {

        LOG.debug("List OPC Tags: " + host + ", " + domain + ", " + user + ", "
                + serverName);

        ArrayList<OPCItem> opcItems = new ArrayList<OPCItem>();

        try {
            opcItems = new RealOPCMaster().browseOPCTags(user, password, host,
                    domain, serverName);
        } catch (Exception e) {
            LOG.warn("Error retriving OPC tags: " + e.getLocalizedMessage());
        }

        return opcItems;
    }

    public OPCItem validateOPCTag(String tag, String user, String password,
                                  String host, String domain, String servername) {

        Logger log = JISystem.getLogger();
        log.setLevel(Level.OFF);

        OPCItem opcItem = new OPCItem(tag, 0, false);

        OPCUtils utils = new OPCUtils();

        opcItem = utils.validateTag(tag, user, password, host, domain,
                servername);
        return opcItem;

    }

    // public void saveOPCTags(OPCItem[] opcItems) {
    // for (int i = 0; i < opcItems.length; i++) {
    // OPCItem opcItem = new OPCItem("", 0, false);
    // }
    // }

    public DwrResponseI18n saveMultipleOPCPointLocator(String[] tags,
                                                       int[] dataTypes, boolean[] settables, OPCPointLocatorVO[] locators,
                                                       String context) {

        return validateMultipleOPCPoints(tags, dataTypes, settables, locators,
                context, null);
    }

    private DwrResponseI18n validateMultipleOPCPoints(String[] tags,
                                                      int[] dataTypes, boolean[] settables, OPCPointLocatorVO[] locators,
                                                      String context, DataPointDefaulter defaulter) {
        DwrResponseI18n response = new DwrResponseI18n();
        OPCDataSourceVO<?> ds = (OPCDataSourceVO<?>) Common.getUser()
                .getEditDataSource();
        if (ds.isNew()) {
            response.addContextualMessage(context,
                    "dsEdit.opc.validate.dataSourceNotSaved");
            return response;
        }
        for (int i = 0; i < locators.length; i++) {
            DataPointVO dp = getPoint(Common.NEW_ID, defaulter);
            dp.setName(tags[i]);
            locators[i].setTag(tags[i]);
            locators[i].setDataTypeId(dataTypes[i]);
            locators[i].setSettable(settables[i]);
            dp.setPointLocator(locators[i]);

            if (StringUtils.isEmpty(dp.getXid()))
                response.addContextualMessage("xid", "validate.required");
            else if (!new DataPointDao()
                    .isXidUnique(dp.getXid(), Common.NEW_ID))
                response.addContextualMessage("xid", "validate.xidUsed");
            else if (StringUtils.isLengthGreaterThan(dp.getXid(), 50))
                response.addContextualMessage("xid", "validate.notLongerThan",
                        50);

            // locators[i].validate(response);
            if (!response.getHasMessages()) {
                Common.ctx.getRuntimeManager().saveDataPoint(dp);
                response.addData("id", dp.getId());
                response.addData("points", getPoints());
            }
        }
        return response;
    }

    // ////
    // ASCII File Reader
    // ////

    public DwrResponseI18n saveASCIIFileDataSource(String name, String xid,
                                                   int updatePeriods, int updatePeriodType, String filePath,
                                                   boolean quantize) {

        ASCIIFileDataSourceVO ds = (ASCIIFileDataSourceVO) Common.getUser()
                .getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setFilePath(filePath);
        ds.setUpdatePeriods(updatePeriods);
        ds.setUpdatePeriodType(updatePeriodType);
        ds.setQuantize(quantize);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public DwrResponseI18n saveASCIIFilePointLocator(int id, String xid,
                                                     String name, ASCIIFilePointLocatorVO locator) {
        locator.setSettable(false);
        return validatePoint(id, xid, name, locator, null);
    }

    public boolean checkFile(String filePath) {
        return new File(filePath).exists();
    }

    // ////
    // DR_STORAGE_HT5B
    // ////

    public DwrResponseI18n saveDrStorageHt5bDataSource(String name, String xid,
                                                       int updatePeriods, int updatePeriodType, String commPortId,
                                                       int baudRate, int dataBits, int stopBits, int parity, int timeout,
                                                       int retries, String initString, boolean quantize) {

        DrStorageHt5bDataSourceVO ds = (DrStorageHt5bDataSourceVO) Common
                .getUser().getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setCommPortId(commPortId);
        ds.setBaudRate(baudRate);
        ds.setDataBits(dataBits);
        ds.setStopBits(stopBits);
        ds.setParity(parity);
        ds.setTimeout(timeout);
        ds.setRetries(retries);
        ds.setUpdatePeriods(updatePeriods);
        ds.setUpdatePeriodType(updatePeriodType);
        ds.setInitString(initString);
        ds.setQuantize(quantize);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public DwrResponseI18n saveDrStorageHt5bPointLocator(int id, String xid,
                                                         String name, DrStorageHt5bPointLocatorVO locator) {
        locator.setSettable(false);
        return validatePoint(id, xid, name, locator, null);
    }

    // ////
    // ASCII Serial
    // ////

    public DwrResponseI18n saveASCIISerialDataSource(String name, String xid,
                                                     int updatePeriods, int updatePeriodType, String commPortId,
                                                     int baudRate, int dataBits, int stopBits, int parity, int timeout,
                                                     int retries, int stopMode, int nChar, int charStopMode,
                                                     String charX, String hexValue, int stopTimeout, String initString,
                                                     int bufferSize, boolean quantize) {

        ASCIISerialDataSourceVO ds = (ASCIISerialDataSourceVO) Common.getUser()
                .getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setCommPortId(commPortId);
        ds.setBaudRate(baudRate);
        ds.setDataBits(dataBits);
        ds.setStopBits(stopBits);
        ds.setParity(parity);
        ds.setTimeout(timeout);
        ds.setRetries(retries);
        ds.setUpdatePeriods(updatePeriods);
        ds.setUpdatePeriodType(updatePeriodType);
        ds.setStopMode(stopMode);
        ds.setnChar(nChar);
        ds.setCharStopMode(charStopMode);
        ds.setCharX(charX);
        ds.setHexValue(hexValue);
        ds.setStopTimeout(stopTimeout);
        ds.setInitString(initString);
        ds.setBufferSize(bufferSize);
        ds.setQuantize(quantize);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public DwrResponseI18n saveASCIISerialPointLocator(int id, String xid,
                                                       String name, ASCIISerialPointLocatorVO locator) {
        locator.setSettable(false);
        return validatePoint(id, xid, name, locator, null);
    }

    //
    // /
    // / IEC101 Serial stuff
    // /
    //
    @MethodFilter
    public DwrResponseI18n saveIEC101SerialDataSource(String name, String xid,
                                                      int updatePeriods, int updatePeriodType, int giRelativePeriod,
                                                      int clockSynchRelativePeriod, int linkLayerAddressSize,
                                                      int linkLayerAddress, int asduAddressSize, int asduAddress,
                                                      int cotSize, int objectAddressSize, int timeout, int retries,
                                                      String commPortId, int baudRate, int dataBits, int stopBits,
                                                      int parity, boolean quantize) {

        IEC101SerialDataSourceVO ds = (IEC101SerialDataSourceVO) Common
                .getUser().getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setUpdatePeriods(updatePeriods);
        ds.setUpdatePeriodType(updatePeriodType);
        ds.setGiRelativePeriod(giRelativePeriod);
        ds.setClockSynchRelativePeriod(clockSynchRelativePeriod);
        ds.setLinkLayerAddressSize(linkLayerAddressSize);
        ds.setLinkLayerAddress(linkLayerAddress);
        ds.setAsduAddressSize(asduAddressSize);
        ds.setAsduAddress(asduAddress);
        ds.setCotSize(cotSize);
        ds.setObjectAddressSize(objectAddressSize);
        ds.setTimeout(timeout);
        ds.setRetries(retries);
        ds.setCommPortId(commPortId);
        ds.setBaudRate(baudRate);
        ds.setDataBits(dataBits);
        ds.setStopBits(stopBits);
        ds.setParity(parity);
        ds.setQuantize(quantize);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public DwrResponseI18n saveIEC101EthernetDataSource(String name,
                                                        String xid, int updatePeriods, int updatePeriodType,
                                                        int giRelativePeriod, int clockSynchRelativePeriod,
                                                        int linkLayerAddressSize, int linkLayerAddress,
                                                        int asduAddressSize, int asduAddress, int cotSize,
                                                        int objectAddressSize, int timeout, int retries, String host,
                                                        int port, boolean quantize) {

        IEC101EthernetDataSourceVO ds = (IEC101EthernetDataSourceVO) Common
                .getUser().getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setUpdatePeriods(updatePeriods);
        ds.setUpdatePeriodType(updatePeriodType);
        ds.setGiRelativePeriod(giRelativePeriod);
        ds.setClockSynchRelativePeriod(clockSynchRelativePeriod);
        ds.setLinkLayerAddressSize(linkLayerAddressSize);
        ds.setLinkLayerAddress(linkLayerAddress);
        ds.setAsduAddressSize(asduAddressSize);
        ds.setAsduAddress(asduAddress);
        ds.setCotSize(cotSize);
        ds.setObjectAddressSize(objectAddressSize);
        ds.setTimeout(timeout);
        ds.setRetries(retries);
        ds.setHost(host);
        ds.setPort(port);
        ds.setQuantize(quantize);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public DwrResponseI18n saveIEC101PointLocator(int id, String xid,
                                                  String name, IEC101PointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }

    //
    //
    // Pachube stuff
    //
    @MethodFilter
    public DwrResponseI18n savePachubeDataSource(String name, String xid,
                                                 String apiKey, int updatePeriods, int updatePeriodType,
                                                 int timeoutSeconds, int retries) {
        PachubeDataSourceVO ds = (PachubeDataSourceVO) Common.getUser()
                .getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setApiKey(apiKey);
        ds.setUpdatePeriods(updatePeriods);
        ds.setUpdatePeriodType(updatePeriodType);
        ds.setTimeoutSeconds(timeoutSeconds);
        ds.setRetries(retries);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public DwrResponseI18n savePachubePointLocator(int id, String xid,
                                                   String name, PachubePointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }

    @MethodFilter
    public String testPachubeValueParams(String apiKey, int timeoutSeconds,
                                         int retries, int feedId, String dataStreamId, int dataTypeId,
                                         String binary0Value) {
        try {
            Map<String, PachubeValue> data = PachubeDataSourceRT.getData(
                    PachubeDataSourceRT.createHttpClient(timeoutSeconds,
                            retries), feedId, apiKey);

            PachubeValue pachubeValue = data.get(dataStreamId);
            if (pachubeValue == null)
                return getMessage(new LocalizableMessage(
                        "event.pachube.dataStreamNotFound", dataStreamId,
                        feedId));

            MangoValue value = DataSourceUtils.getValue(
                    pachubeValue.getValue(), dataTypeId, binary0Value, null,
                    null, null);
            return getMessage("common.result") + ": " + value.toString();
        } catch (LocalizableException e) {
            return getMessage(e.getLocalizableMessage());
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    //
    //
    // JMX stuff
    //
    @MethodFilter
    public DwrResponseI18n saveJmxDataSource(String name, String xid,
                                             boolean useLocalServer, String remoteServerAddr,
                                             int updatePeriodType, int updatePeriods, boolean quantize) {
        JmxDataSourceVO ds = (JmxDataSourceVO) Common.getUser()
                .getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setUseLocalServer(useLocalServer);
        ds.setRemoteServerAddr(remoteServerAddr);
        ds.setUpdatePeriodType(updatePeriodType);
        ds.setUpdatePeriods(updatePeriods);
        ds.setQuantize(quantize);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public DwrResponseI18n saveJmxPointLocator(int id, String xid, String name,
                                               JmxPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }

    @MethodFilter
    public DwrResponseI18n getJmxObjectNames(boolean useLocalServer,
                                             String remoteServerAddr) {
        DwrResponseI18n response = new DwrResponseI18n();
        JMXConnector connector = null;

        try {
            MBeanServerConnection server = null;
            if (useLocalServer)
                server = ManagementFactory.getPlatformMBeanServer();
            else {
                String url = "service:jmx:rmi:///jndi/rmi://"
                        + remoteServerAddr + "/jmxrmi";
                try {
                    connector = JMXConnectorFactory.connect(new JMXServiceURL(
                            url), null);
                    server = connector.getMBeanServerConnection();
                } catch (MalformedURLException e) {
                    response.addGenericMessage("dsEdit.jmx.badUrl",
                            e.getMessage());
                } catch (IOException e) {
                    response.addGenericMessage("dsEdit.jmx.connectionError",
                            e.getMessage());
                }
            }

            if (!response.getHasMessages()) {
                try {
                    Map<String, Object> names = new TreeMap<String, Object>();
                    response.addData("names", names);

                    for (ObjectName on : server.queryNames(null, null)) {
                        List<Map<String, Object>> objectAttributesList = new ArrayList<Map<String, Object>>();
                        names.put(on.getCanonicalName(), objectAttributesList);

                        for (MBeanAttributeInfo attr : server.getMBeanInfo(on)
                                .getAttributes()) {
                            if (attr.getType() == null)
                                continue;

                            Map<String, Object> objectAttributes = new HashMap<String, Object>();
                            try {
                                objectAttributes.put("name", attr.getName());
                                if (attr.getType()
                                        .equals("javax.management.openmbean.CompositeData")) {
                                    objectAttributes.put("type", "Composite");
                                    CompositeData cd = (CompositeData) server
                                            .getAttribute(on, attr.getName());
                                    if (cd != null) {
                                        List<Map<String, Object>> compositeItemsList = new ArrayList<Map<String, Object>>();
                                        objectAttributes.put("items",
                                                compositeItemsList);
                                        for (String key : cd.getCompositeType()
                                                .keySet()) {
                                            Map<String, Object> compositeItems = new HashMap<String, Object>();
                                            compositeItemsList
                                                    .add(compositeItems);
                                            compositeItems.put("name", key);
                                            compositeItems
                                                    .put("type", cd
                                                            .getCompositeType()
                                                            .getType(key)
                                                            .getTypeName());
                                        }
                                    }
                                } else
                                    objectAttributes
                                            .put("type", attr.getType());
                                objectAttributesList.add(objectAttributes);
                            } catch (RuntimeMBeanException e) {
                                // ignore
                            }
                        }
                    }
                } catch (Exception e) {
                    response.addGenericMessage("dsEdit.jmx.readError",
                            e.getMessage());
                    LOG.warn("", e);
                }
            }
        } finally {
            try {
                if (connector != null)
                    connector.close();
            } catch (IOException e) {
                // no op
            }
        }

        return response;
    }

    //
    //
    // Persistent stuff
    //
    @MethodFilter
    public DwrResponseI18n savePersistentDataSource(String name, String xid,
                                                    int port, String authorizationKey, boolean acceptPointUpdates) {
        PersistentDataSourceVO ds = (PersistentDataSourceVO) Common.getUser()
                .getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setPort(port);
        ds.setAuthorizationKey(authorizationKey);
        ds.setAcceptPointUpdates(acceptPointUpdates);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public DwrResponseI18n savePersistentPointLocator(int id, String xid,
                                                      String name, PersistentPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }

    public DwrResponseI18n saveNodaveS7DataSource(String name, String xid,
                                                  int updatePeriods, int updatePeriodType, String filePath,
                                                  boolean quantize, String nodaveWriteBaseCmd) {

        NodaveS7DataSourceVO ds = (NodaveS7DataSourceVO) Common.getUser()
                .getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setFilePath(filePath);
        ds.setUpdatePeriods(updatePeriods);
        ds.setUpdatePeriodType(updatePeriodType);
        ds.setQuantize(quantize);
        ds.setNodaveWriteBaseCmd(nodaveWriteBaseCmd);

        return tryDataSourceSave(ds);

    }

    @MethodFilter
    public DwrResponseI18n saveNodaveS7PointLocator(int id, String xid,
                                                    String name, NodaveS7PointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }

    public DwrResponseI18n saveAlpha2DataSource(String name, String xid,
                                                int updatePeriods, int updatePeriodType, String commPortId,
                                                int baudRate, int dataBits, int stopBits, int parity, int timeout,
                                                int retries, int station) {

        Alpha2DataSourceVO ds = (Alpha2DataSourceVO) Common.getUser()
                .getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setCommPortId(commPortId);
        ds.setBaudRate(baudRate);
        ds.setDataBits(dataBits);
        ds.setStopBits(stopBits);
        ds.setParity(parity);
        ds.setTimeout(timeout);
        ds.setRetries(retries);
        ds.setUpdatePeriods(updatePeriods);
        ds.setUpdatePeriodType(updatePeriodType);
        ds.setStation(station);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public DwrResponseI18n saveAlpha2PointLocator(int id, String xid,
                                                  String name, Alpha2PointLocatorVO locator) {

        return validatePoint(id, xid, name, locator, null);
    }

    //
    //
    // Internal stuff
    //
    @MethodFilter
    public DwrResponseI18n saveInternalDataSource(String name, String xid,
                                                  int updatePeriods, int updatePeriodType) {
        InternalDataSourceVO ds = (InternalDataSourceVO) Common.getUser()
                .getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setUpdatePeriods(updatePeriods);
        ds.setUpdatePeriodType(updatePeriodType);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public DwrResponseI18n saveInternalPointLocator(int id, String xid,
                                                    String name, InternalPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }

    public DwrResponseI18n saveRadiuinoDataSource(String name, String xid,
                                                  int updatePeriods, int updatePeriodType, String commPortId,
                                                  int baudRate, int dataBits, int stopBits, int parity,
                                                  boolean pollingMode, int timeout, int retries) {

        RadiuinoDataSourceVO ds = (RadiuinoDataSourceVO) Common.getUser()
                .getEditDataSource();

        ds.setXid(xid);
        ds.setName(name);
        ds.setCommPortId(commPortId);
        ds.setBaudRate(baudRate);
        ds.setDataBits(dataBits);
        ds.setStopBits(stopBits);
        ds.setParity(parity);
        ds.setPollingMode(pollingMode);
        ds.setTimeout(timeout);
        ds.setRetries(retries);
        ds.setUpdatePeriods(updatePeriods);
        ds.setUpdatePeriodType(updatePeriodType);

        return tryDataSourceSave(ds);
    }

    @MethodFilter
    public DwrResponseI18n saveRadiuinoPointLocator(int id, String xid,
                                                    String name, RadiuinoPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }

}