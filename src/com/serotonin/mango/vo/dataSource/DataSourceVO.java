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
package com.serotonin.mango.vo.dataSource;

import br.org.scadabr.vo.dataSource.alpha2.Alpha2DataSourceVO;
import br.org.scadabr.vo.dataSource.asciiFile.ASCIIFileDataSourceVO;
import br.org.scadabr.vo.dataSource.asciiSerial.ASCIISerialDataSourceVO;
import br.org.scadabr.vo.dataSource.dnp3.Dnp3IpDataSourceVO;
import br.org.scadabr.vo.dataSource.dnp3.Dnp3SerialDataSourceVO;
import br.org.scadabr.vo.dataSource.drStorageHt5b.DrStorageHt5bDataSourceVO;
import br.org.scadabr.vo.dataSource.iec101.IEC101EthernetDataSourceVO;
import br.org.scadabr.vo.dataSource.iec101.IEC101SerialDataSourceVO;
import br.org.scadabr.vo.dataSource.nodaves7.NodaveS7DataSourceVO;
import br.org.scadabr.vo.dataSource.opc.OPCDataSourceVO;
import cc.radiuino.scadabr.vo.datasource.radiuino.RadiuinoDataSourceVO;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.json.*;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.DataSourceDao;
import com.serotonin.mango.rt.dataSource.DataSourceRT;
import com.serotonin.mango.rt.event.AlarmLevels;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.rt.event.type.EventType;
import com.serotonin.mango.util.ChangeComparable;
import com.serotonin.mango.util.ExportCodes;
import com.serotonin.mango.util.LocalizableJsonException;
import com.serotonin.mango.vo.dataSource.bacnet.BACnetIPDataSourceVO;
import com.serotonin.mango.vo.dataSource.ebro.EBI25DataSourceVO;
import com.serotonin.mango.vo.dataSource.galil.GalilDataSourceVO;
import com.serotonin.mango.vo.dataSource.http.HttpImageDataSourceVO;
import com.serotonin.mango.vo.dataSource.http.HttpReceiverDataSourceVO;
import com.serotonin.mango.vo.dataSource.http.HttpRetrieverDataSourceVO;
import com.serotonin.mango.vo.dataSource.internal.InternalDataSourceVO;
import com.serotonin.mango.vo.dataSource.jmx.JmxDataSourceVO;
import com.serotonin.mango.vo.dataSource.mbus.MBusDataSourceVO;
import com.serotonin.mango.vo.dataSource.meta.MetaDataSourceVO;
import com.serotonin.mango.vo.dataSource.modbus.ModbusIpDataSourceVO;
import com.serotonin.mango.vo.dataSource.modbus.ModbusSerialDataSourceVO;
import com.serotonin.mango.vo.dataSource.nmea.NmeaDataSourceVO;
import com.serotonin.mango.vo.dataSource.onewire.OneWireDataSourceVO;
import com.serotonin.mango.vo.dataSource.openv4j.OpenV4JDataSourceVO;
import com.serotonin.mango.vo.dataSource.pachube.PachubeDataSourceVO;
import com.serotonin.mango.vo.dataSource.persistent.PersistentDataSourceVO;
import com.serotonin.mango.vo.dataSource.pop3.Pop3DataSourceVO;
import com.serotonin.mango.vo.dataSource.snmp.SnmpDataSourceVO;
import com.serotonin.mango.vo.dataSource.sql.SqlDataSourceVO;
import com.serotonin.mango.vo.dataSource.viconics.ViconicsDataSourceVO;
import com.serotonin.mango.vo.dataSource.virtual.VirtualDataSourceVO;
import com.serotonin.mango.vo.dataSource.vmstat.VMStatDataSourceVO;
import com.serotonin.mango.vo.event.EventTypeVO;
import com.serotonin.util.StringUtils;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.web.i18n.LocalizableMessage;
import org.scada_lts.ds.amqp.AmqpDataSourceVO;
import org.scada_lts.ds.state.MigrationOrErrorSerializeChangeEnableState;
import org.scada_lts.ds.state.IStateDs;
import org.scada_lts.ds.state.change.ChangeStatus;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

abstract public class DataSourceVO<T extends DataSourceVO<?>> extends ChangeStatus implements
		Serializable, Cloneable, JsonSerializable, ChangeComparable<T> {
	public enum Type {
		EBI25(16, "dsEdit.ebi25", false) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new EBI25DataSourceVO();
			}
		},
		VICONICS(18, "dsEdit.viconics", false) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new ViconicsDataSourceVO();
			}
		},
		BACNET(10, "dsEdit.bacnetIp", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new BACnetIPDataSourceVO();
			}
		},
		DNP3_IP(21, "dsEdit.dnp3Ip", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new Dnp3IpDataSourceVO();
			}
		},
		DNP3_SERIAL(22, "dsEdit.dnp3Serial", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new Dnp3SerialDataSourceVO();
			}
		},
		GALIL(14, "dsEdit.galil", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new GalilDataSourceVO();
			}
		},
		HTTP_RECEIVER(7, "dsEdit.httpReceiver", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new HttpReceiverDataSourceVO();
			}
		},
		HTTP_RETRIEVER(11, "dsEdit.httpRetriever", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new HttpRetrieverDataSourceVO();
			}
		},
		HTTP_IMAGE(15, "dsEdit.httpImage", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new HttpImageDataSourceVO();
			}
		},
		INTERNAL(27, "dsEdit.internal", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new InternalDataSourceVO();
			}
		},
		JMX(26, "dsEdit.jmx", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new JmxDataSourceVO();
			}
		},
		M_BUS(20, "dsEdit.mbus", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return MBusDataSourceVO.createNewDataSource();
			}
		},
		META(9, "dsEdit.meta", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new MetaDataSourceVO();
			}
		},
		MODBUS_IP(3, "dsEdit.modbusIp", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new ModbusIpDataSourceVO();
			}
		},
		MODBUS_SERIAL(2, "dsEdit.modbusSerial", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new ModbusSerialDataSourceVO();
			}
		},
		NMEA(13, "dsEdit.nmea", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new NmeaDataSourceVO();
			}
		},
		ONE_WIRE(8, "dsEdit.1wire", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new OneWireDataSourceVO();
			}
		},
		OPEN_V_4_J(19, "dsEdit.openv4j", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new OpenV4JDataSourceVO();
			}
		},
		/*
		 * FHZ_4_J(40, "dsEdit.fhz4j", true) {
		 * 
		 * @Override public DataSourceVO<?> createDataSourceVO() { return new
		 * Fhz4JDataSourceVO(); } },
		 */PACHUBE(23, "dsEdit.pachube", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new PachubeDataSourceVO();
			}
		},
		PERSISTENT(24, "dsEdit.persistent", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new PersistentDataSourceVO();
			}
		},
		POP3(12, "dsEdit.pop3", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new Pop3DataSourceVO();
			}
		},
		SNMP(5, "dsEdit.snmp", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new SnmpDataSourceVO();
			}
		},
		/*
		 * SPINWAVE(4, "dsEdit.spinwave", true) {
		 * 
		 * @Override public DataSourceVO<?> createDataSourceVO() { return new
		 * SpinwaveDataSourceVO(); } },
		 */SQL(6, "dsEdit.sql", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new SqlDataSourceVO();
			}
		},
		VIRTUAL(1, "dsEdit.virtual", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new VirtualDataSourceVO();
			}
		},
		VMSTAT(17, "dsEdit.vmstat", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new VMStatDataSourceVO();
			}
		},
		OPC(32, "dsEdit.opc", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new OPCDataSourceVO();
			}
		},
		ASCII_FILE(33, "dsEdit.asciiFile", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new ASCIIFileDataSourceVO();
			}
		},
		ASCII_SERIAL(34, "dsEdit.asciiSerial", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new ASCIISerialDataSourceVO();
			}
		},

		IEC101_SERIAL(35, "dsEdit.iec101Serial", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new IEC101SerialDataSourceVO();
			}
		},
		IEC101_ETHERNET(36, "dsEdit.iec101Ethernet", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new IEC101EthernetDataSourceVO();
			}
		},

		NODAVE_S7(37, "dsEdit.nodaves7", false) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new NodaveS7DataSourceVO();
			}
		},

		DR_STORAGE_HT5B(38, "dsEdit.drStorageHt5b", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new DrStorageHt5bDataSourceVO();
			}
		},
		ALPHA_2(39, "dsEdit.alpha2", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new Alpha2DataSourceVO();
			}
		},
		RADIUINO(41, "dsEdit.radiuino", true) {
			@Override
			public DataSourceVO<?> createDataSourceVO() {
				return new RadiuinoDataSourceVO();
			}
		},
		AMQP(45, "dsEdit.amqp", true) {
		 	@Override
			public DataSourceVO<?> createDataSourceVO() {
		 		return new AmqpDataSourceVO();
			}
		};

		private Type(int id, String key, boolean display) {
			this.id = id;
			this.key = key;
			this.display = display;
		}

		private final int id;
		private final String key;
		private final boolean display;

		public int getId() {
			return id;
		}

		public String getKey() {
			return key;
		}

		public boolean isDisplay() {
			return display;
		}

		public abstract DataSourceVO<?> createDataSourceVO();

		public static Type valueOf(int id) {
			for (Type type : values()) {
				if (type.id == id)
					return type;
			}
			return null;
		}

		public static Type valueOfIgnoreCase(String text) {
			for (Type type : values()) {
				if (type.name().equalsIgnoreCase(text))
					return type;
			}
			return null;
		}

		public static List<String> getTypeList() {
			List<String> result = new ArrayList<String>();
			for (Type type : values())
				result.add(type.name());
			return result;
		}
	}

	public static final String XID_PREFIX = "DS_";

	public static DataSourceVO<?> createDataSourceVO(int typeId) {
		return Type.valueOf(typeId).createDataSourceVO();
	}

	public static String generateXid() {
		return Common.generateXid("DS_");
	}

	abstract public Type getType();

	abstract public LocalizableMessage getConnectionDescription();

	abstract public PointLocatorVO createPointLocator();

	abstract public DataSourceRT createDataSourceRT();

	abstract public ExportCodes getEventCodes();

	final public List<EventTypeVO> getEventTypes() {
		List<EventTypeVO> eventTypes = new ArrayList<EventTypeVO>();
		addEventTypes(eventTypes);
		return eventTypes;
	}

	abstract protected void addEventTypes(List<EventTypeVO> eventTypes);

	public boolean isNew() {
		return id == Common.NEW_ID;
	}

	private int id = Common.NEW_ID;
	private String xid;
	@JsonRemoteProperty
	private String name;
	@JsonRemoteProperty
	private boolean enabled;

	private IStateDs state;

	private Map<Integer, Integer> alarmLevels = new HashMap<Integer, Integer>();

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public IStateDs getState() {

		return state;
	}

	public void setState(IStateDs state) {
		notifyListeners(
				this,
				this.state,
				this.state = state);
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

	public void setAlarmLevel(int eventId, int level) {
		alarmLevels.put(eventId, level);
	}

	public int getAlarmLevel(int eventId, int defaultLevel) {
		Integer level = alarmLevels.get(eventId);
		if (level == null)
			return defaultLevel;
		return level;
	}

	public EventTypeVO getEventType(int eventId) {
		for (EventTypeVO vo : getEventTypes()) {
			if (vo.getTypeRef2() == eventId)
				return vo;
		}
		return null;
	}

	protected EventTypeVO createEventType(int eventId,
			LocalizableMessage message) {
		return createEventType(eventId, message,
				EventType.DuplicateHandling.IGNORE, AlarmLevels.URGENT);
	}

	protected EventTypeVO createEventType(int eventId,
			LocalizableMessage message, int duplicateHandling,
			int defaultAlarmLevel) {
		return new EventTypeVO(EventType.EventSources.DATA_SOURCE, getId(),
				eventId, message, getAlarmLevel(eventId, defaultAlarmLevel),
				duplicateHandling);
	}

	public void validate(DwrResponseI18n response) {
		if (StringUtils.isEmpty(xid))
			response.addContextualMessage("xid", "validate.required");
		else if (!new DataSourceDao().isXidUnique(xid, id))
			response.addContextualMessage("xid", "validate.xidUsed");
		else if (StringUtils.isLengthGreaterThan(xid, 50))
			response.addContextualMessage("xid", "validate.notLongerThan", 50);

		if (StringUtils.isEmpty(name))
			response.addContextualMessage("dataSourceName",
					"validate.nameRequired");
		if (StringUtils.isLengthGreaterThan(name, 40))
			response.addContextualMessage("dataSourceName",
					"validate.nameTooLong");
	}

	protected String getMessage(ResourceBundle bundle, String key,
			Object... args) {
		return new LocalizableMessage(key, args).getLocalizedMessage(bundle);
	}

	public DataSourceVO<?> copy() {
		try {
			return (DataSourceVO<?>) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new ShouldNeverHappenException(e);
		}
	}

	@Override
	public String getTypeKey() {
		return "event.audit.dataSource";
	}

	@Override
	public final void addProperties(List<LocalizableMessage> list) {
		AuditEventType.addPropertyMessage(list, "dsEdit.head.name", name);
		AuditEventType.addPropertyMessage(list, "common.xid", xid);
		AuditEventType.addPropertyMessage(list, "common.enabled", enabled);
		AuditEventType.addPropertyMessage(list, "common.state", state);


		addPropertiesImpl(list);
	}

	@Override
	public final void addPropertyChanges(List<LocalizableMessage> list, T from) {
		AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.head.name",
				from.getName(), name);
		AuditEventType.maybeAddPropertyChangeMessage(list, "common.xid",
				from.getXid(), xid);
		AuditEventType.maybeAddPropertyChangeMessage(list, "common.enabled",
				from.isEnabled(), enabled);

		AuditEventType.maybeAddPropertyChangeMessage(list, "common.describeStatus", from.getState(), state);

		addPropertyChangesImpl(list, from);
	}

	abstract protected void addPropertiesImpl(List<LocalizableMessage> list);

	abstract protected void addPropertyChangesImpl(
			List<LocalizableMessage> list, T from);

	//
	// /
	// / Serialization
	// /
	//
	private static final long serialVersionUID = -1;
	private static final int version = 2;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(version);
		out.writeBoolean(enabled);
		out.writeObject(alarmLevels);
		out.writeObject(state);
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int ver = in.readInt();

		// Switch on the version of the class so that version changes can be
		// elegantly handled.
		if (ver == 1) {
			enabled = in.readBoolean();
			alarmLevels = new HashMap<Integer, Integer>();
			try {
				state = (IStateDs) in.readObject();
			} catch (Exception e) {
				state = new MigrationOrErrorSerializeChangeEnableState();
			}

		} else if (ver == 2) {
			enabled = in.readBoolean();
			alarmLevels = (HashMap<Integer, Integer>) in.readObject();
			try {
				state = (IStateDs) in.readObject();
			} catch (Exception e) {
				state = new MigrationOrErrorSerializeChangeEnableState();
			}
		}
	}

	@Override
	public void jsonSerialize(Map<String, Object> map) {
		map.put("xid", xid);
		map.put("type", getType().name());

		ExportCodes eventCodes = getEventCodes();
		if (eventCodes != null && eventCodes.size() > 0) {
			Map<String, String> alarmCodeLevels = new HashMap<String, String>();

			for (int i = 0; i < eventCodes.size(); i++) {
				int eventId = eventCodes.getId(i);
				int level = getAlarmLevel(eventId, AlarmLevels.URGENT);
				alarmCodeLevels.put(eventCodes.getCode(eventId),
						AlarmLevels.CODES.getCode(level));
			}

			map.put("alarmLevels", alarmCodeLevels);
		}
	}

	@Override
	public void jsonDeserialize(JsonReader reader, JsonObject json)
			throws JsonException {
		// Can't change the type.

		JsonObject alarmCodeLevels = json.getJsonObject("alarmLevels");
		if (alarmCodeLevels != null) {
			ExportCodes eventCodes = getEventCodes();
			if (eventCodes != null && eventCodes.size() > 0) {
				for (String code : alarmCodeLevels.getProperties().keySet()) {
					int eventId = eventCodes.getId(code);
					if (!eventCodes.isValidId(eventId))
						throw new LocalizableJsonException(
								"emport.error.eventCode", code,
								eventCodes.getCodeList());

					String text = alarmCodeLevels.getString(code);
					int level = AlarmLevels.CODES.getId(text);
					if (!AlarmLevels.CODES.isValidId(level))
						throw new LocalizableJsonException(
								"emport.error.alarmLevel", text, code,
								AlarmLevels.CODES.getCodeList());

					setAlarmLevel(eventId, level);
				}
			}
		}
	}

	protected void serializeUpdatePeriodType(Map<String, Object> map,
			int updatePeriodType) {
		map.put("updatePeriodType",
				Common.TIME_PERIOD_CODES.getCode(updatePeriodType));
	}

	protected Integer deserializeUpdatePeriodType(JsonObject json)
			throws JsonException {
		String text = json.getString("updatePeriodType");
		if (text == null)
			return null;

		int value = Common.TIME_PERIOD_CODES.getId(text);
		if (value == -1)
			throw new LocalizableJsonException("emport.error.invalid",
					"updatePeriodType", text,
					Common.TIME_PERIOD_CODES.getCodeList());

		return value;
	}

}
