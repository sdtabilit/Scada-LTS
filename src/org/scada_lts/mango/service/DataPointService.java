/*
 * (c) 2016 Abil'I.T. http://abilit.eu/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.scada_lts.mango.service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.serotonin.mango.rt.dataImage.SetPointSource;
import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.permission.Permissions;
import org.apache.commons.logging.LogFactory;
import org.jfree.util.Log;
import org.quartz.SchedulerException;
import org.scada_lts.cache.EventDetectorsCache;
import org.scada_lts.config.ScadaConfig;
import org.scada_lts.dao.DAO;
import org.scada_lts.dao.DataPointDAO;
import org.scada_lts.dao.DataPointUserDAO;
import org.scada_lts.dao.DataSourceDAO;
import org.scada_lts.dao.PointEventDetectorDAO;
import org.scada_lts.dao.model.point.PointValue;
import org.scada_lts.dao.pointhierarchy.PointHierarchyDAO;
import org.scada_lts.dao.PointLinkDAO;
import org.scada_lts.dao.UserCommentDAO;
import org.scada_lts.dao.pointvalues.PointValueAmChartDAO;
import org.scada_lts.dao.pointvalues.PointValueDAO;
import org.scada_lts.dao.pointvalues.PointValueDAO4REST;
import org.scada_lts.dao.watchlist.WatchListDAO;
import org.scada_lts.mango.adapter.MangoDataPoint;
import org.scada_lts.mango.adapter.MangoPointHierarchy;
import org.scada_lts.service.pointhierarchy.PointHierarchyService;
import org.scada_lts.web.mvc.api.dto.PointValueDTO;
import org.scada_lts.web.mvc.api.json.JsonBinaryEventTextRenderer;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;
import com.serotonin.db.IntValuePair;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.PointValueDao;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.vo.DataPointExtendedNameComparator;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.bean.PointHistoryCount;
import com.serotonin.mango.vo.event.PointEventDetectorVO;
import com.serotonin.mango.vo.hierarchy.PointFolder;
import com.serotonin.mango.vo.hierarchy.PointHierarchy;
import com.serotonin.mango.vo.link.PointLinkVO;
import com.serotonin.mango.vo.permission.DataPointAccess;
import com.serotonin.util.Tuple;

import static org.scada_lts.utils.AggregateUtils.*;

/**
 * Service for DataPointDAO
 *
 * @author Mateusz Kaproń Abil'I.T. development team, sdt@abilit.eu
 */
@Service
public class DataPointService implements MangoDataPoint {

	private static final org.apache.commons.logging.Log LOG = LogFactory.getLog(DataPointService.class);

	private static final DataPointDAO dataPointDAO = new DataPointDAO();

	private DataSourceDAO dataSourceDAO = new DataSourceDAO();

	private static final UserCommentDAO userCommentDAO = new UserCommentDAO();

	private static final PointEventDetectorDAO pointEventDetectorDAO = new PointEventDetectorDAO();

	private static final PointHierarchyDAO pointHierarchyDAO = new PointHierarchyDAO();

	private static final DataPointUserDAO dataPointUserDAO = new DataPointUserDAO();

	private static final PointValueDAO pointValueDAO = new PointValueDAO();

	private static final WatchListDAO watchListDAO = new WatchListDAO();

	private static final PointLinkDAO pointLinkDAO = new PointLinkDAO();

	private static final PointHierarchyService pointHierarchyService = new PointHierarchyService();

	private static final PointValueAmChartDAO pointValueAmChartDao = new PointValueAmChartDAO();

	private static final PointValueDAO4REST pointValueDao4Rest = new PointValueDAO4REST();

	@Override
	public String generateUniqueXid() {
		return DAO.getInstance().generateUniqueXid(DataPointVO.XID_PREFIX, "dataPoints");
	}

	@Override
	public boolean isXidUnique(String xid, int excludeId) {
		return DAO.getInstance().isXidUnique(xid, excludeId, "dataPoints");
	}

	@Override
	public String getExtendedPointName(int dataPointId) {
		DataPointVO dataPoint = getDataPoint(dataPointId);
		if (dataPoint == null) {
			return "?";
		}
		return dataPoint.getExtendedName();
	}

	@Override
	public DataPointVO getDataPoint(String xId) {
		DataPointVO dp = dataPointDAO.getDataPoint(xId);
		setRelationalData(dp);
		return dp;
	}
	@Override
	public DataPointVO getDataPointByXid(String xid) {
		DataPointVO dp = dataPointDAO.getDataPoint(xid);
		return dp;
	}
	@Override
	public DataPointVO getDataPoint(int id) {
		DataPointVO dp = dataPointDAO.getDataPoint(id);
		setRelationalData(dp);
		return dp;
	}

	@Override
	public List<DataPointVO> getDataPoints(Comparator<DataPointVO> comparator, boolean includeRelationalData) {
		List<DataPointVO> dpList = dataPointDAO.getDataPoints();
		if (includeRelationalData) {
			setRelationalData(dpList);
		}
		if (comparator != null) {
			Collections.sort(dpList, comparator);
		}
		return dpList;
	}

	@Override
	public List<DataPointVO> getDataPoints(int dataSourceId, Comparator<DataPointVO> comparator) {
		List<DataPointVO> dpList = dataPointDAO.getDataPoints(dataSourceId);
		setRelationalData(dpList);
		if (comparator != null) {
			Collections.sort(dpList, comparator);
		}
		return dpList;
	}

	public Map<DataPointVO, List<PointValue>> getDataPoints(String partOfNameDS, String typeDS, String partOfNamePoint, Date startTime, Date endTime) {

		List<DataPointVO> listAllPoints = new LinkedList<>();
		List<DataSourceVO<?>>  listDs = dataSourceDAO.getDataSourceBaseOfName(partOfNameDS);
		for (DataSourceVO<?> ds : listDs) {
			if (ds.getType().name().equals(typeDS.toUpperCase().trim())) {
				List<DataPointVO> dpList = dataPointDAO.getDataPoints(ds.getId());
				listAllPoints.addAll(dpList);
			}
		}

		List<DataPointVO> listPointToGetData = new LinkedList<DataPointVO>();
		for (DataPointVO dpVO : listAllPoints) {
			if (dpVO.getName().contains(partOfNamePoint)) {
				listPointToGetData.add(dpVO);
			}
		}
		Map<DataPointVO, List<PointValue>> pointsWithListData = new HashMap<>();

		for (DataPointVO dp : listPointToGetData) {
			List<PointValue> data = pointValueDAO.filtered(
					" dataPointId = ? and ts > ? and ts < ?",
					 new Object[]{dp.getId(),
					 startTime.getTime(),
					 endTime.getTime()},
					0 );
			pointsWithListData.put(dp,data);
		}

		return pointsWithListData;
	}

	public List<DataPointVO> getPlcDataPoints(int dataSourceId) {
		List<DataPointVO> datapointList = dataPointDAO.getPlcDataPoints(dataSourceId);
		return datapointList;
	}

	public List<PointValueDTO> valuesPointBooleanBaseOnNameFilter2DTO(Map<DataPointVO, List<PointValue>> values) {
		List<PointValueDTO> result = new LinkedList<>();
		for (Map.Entry<DataPointVO, List<PointValue>> entry : values.entrySet()) {
			DataPointVO dpVO = entry.getKey();

			List<PointValue> values4DpVO = entry.getValue();
			for (PointValue pv : values4DpVO) {
				result.add(new PointValueDTO(
						pv.getPointValue().getTime(),
						dpVO.getName(),
						pv.getPointValue().getValue().toString()
				));
			}
		}
		return result;
	}

	public void save(User user, String value, String xid, int typePointValueOfREST) {
		DataPointVO dpvo = dataPointDAO.getDataPoint(xid);
		pointValueDao4Rest.save(value, typePointValueOfREST, dpvo.getId());
		setPoint(user, dpvo, value);
	}

	private void setPoint(User user, DataPointVO point, String valueStr) {
		Permissions.ensureDataPointSetPermission(user, point);
		setPointImpl(point, valueStr, user);
	}

	public void saveAPI(User user, String value, String xid) {
		DataPointVO dpvo = dataPointDAO.getDataPoint(xid);
		Permissions.ensureDataPointSetPermission(user, dpvo);
		setPointImpl(dpvo, value, user);
	}

	private void setPointImpl(DataPointVO point, String valueStr, SetPointSource source) {
		if (point == null)
			return;

		if (valueStr == null)
			Common.ctx.getRuntimeManager().relinquish(point.getId());
		else {
			// Convert the string value into an object.
			MangoValue value = MangoValue.stringToValue(valueStr, point.getPointLocator().getDataTypeId());
			Common.ctx.getRuntimeManager().setDataPointValue(point.getId(), value, source);
		}
	}

	private void setRelationalData(List<DataPointVO> dpList) {
		for (DataPointVO dp: dpList) {
			setRelationalData(dp);
		}
	}

	private void setRelationalData(DataPointVO dp) {
		if (dp != null) {
			setEventDetectors(dp);
			setPointComments(dp);
		}
	}

	@Override
	public void saveDataPoint(final DataPointVO dp) {
		if (dp.getId() == -1) {
			insertDataPoint(dp);
			PointHierarchyDAO.cachedPointHierarchy = null;
			MangoPointHierarchy.getInst().addDataPoint(dp);
		} else {
			updateDataPoint(dp);
			MangoPointHierarchy.getInst().updateDataPoint(dp);
		}
	}

	@Override
	public void insertDataPoint(final DataPointVO dp) {
		//Create default text renderer
		if (dp.getTextRenderer() == null) {
			dp.defaultTextRenderer();
		}

		dp.setId(dataPointDAO.insert(dp));
		saveEventDetectors(dp);
	}

	@Override
	public void updateDataPoint(final DataPointVO dp) {
		DataPointVO oldDp = dataPointDAO.getDataPoint(dp.getId());
		if (oldDp.getPointLocator().getDataTypeId() != dp.getPointLocator().getDataTypeId()) {
			new PointValueDao().deletePointValuesWithMismatchedType(dp.getId(), dp.getPointLocator().getDataTypeId());
		}

		updateDataPointShallow(dp);
		saveEventDetectors(dp);
	}

	@Override
	public void updateDataPointShallow(final DataPointVO dp) {
		dataPointDAO.update(dp);
	}

	@Override
	public void deleteDataPoint(int dataPointId) {
		try {
			DataPointVO dp = getDataPoint(dataPointId);
			beforePointDelete(dataPointId);
			deletePointHistory(dataPointId);
			deleteDataPointImpl(Integer.toString(dataPointId));
		} catch (EmptyResultDataAccessException e) {
			Log.error(e);
			return;
		}
	}

	@Override
	public void deleteDataPoints(int dataSourceId) {
		List<DataPointVO> oldList = getDataPoints(dataSourceId, null);
		for (DataPointVO dp: oldList) {
			beforePointDelete(dp.getId());
		}
		for (DataPointVO dp: oldList) {
			deletePointHistory(dp.getId());
		}

		List<Integer> pointIds = dataPointDAO.getDataPointsIds(dataSourceId);
		if (pointIds.size() > 0) {
			StringBuilder idsWithCommaSB = new StringBuilder();
			Iterator idsIterator = pointIds.iterator();
			idsWithCommaSB.append(pointIds.get(0));
			while (idsIterator.hasNext()) {
				idsWithCommaSB.append(",");
				idsWithCommaSB.append(idsIterator.next());
			}

			deleteDataPointImpl(idsWithCommaSB.toString());
		}
	}

	private void beforePointDelete(int dpId) {
		for (PointLinkVO link: pointLinkDAO.getPointLinksForPoint(dpId)) {
			Common.ctx.getRuntimeManager().deletePointLink(link.getId());
		}
	}

	@Override
	public void deletePointHistory(int dpId) {
		//long min = pointValueDAO.getMinTs(dpId);
		//long max = pointValueDAO.getMaxTs(dpId);
		deletePointHistory(dpId, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Override
	public void deletePointHistory(int dpId, long min, long max) {
		while (true) {
			try {
				pointValueDAO.deletePointValuesBeforeWithOutLast(dpId, max);
				break;
			} catch (UncategorizedSQLException e) {
                if ("The total number of locks exceeds the lock table size".equals(e.getSQLException().getMessage())) {
                    long mid = (min + max) >> 1;
                    deletePointHistory(dpId, min, mid);
                    min = mid;
                }
                else
                    throw e;
            }
		}
	}

	//TODO rewrite int[] dataPointIds 
	@Override
	public void deleteDataPointImpl(String dataPointIds) {

		dataPointDAO.deleteEventHandler(dataPointIds);
		userCommentDAO.deleteUserCommentPoint(dataPointIds);
		pointEventDetectorDAO.deleteWithId(dataPointIds);
		for (String id: dataPointIds.split(",")){
			dataPointUserDAO.deleteWhereDataPointId(Integer.valueOf(id));
		}
		watchListDAO.deleteWatchListPoints(dataPointIds);
		dataPointDAO.deleteWithIn(dataPointIds);

		PointHierarchyDAO.cachedPointHierarchy = null;
		MangoPointHierarchy.getInst().deleteDataPoint(dataPointIds);
	}

	@Override
	public int getDataPointIdFromDetectorId(int pointEventDetectorId) {
		return pointEventDetectorDAO.getDataPointId(pointEventDetectorId);
	}

	@Override
	public String getDetectorXid(int pointEventDetectorId) {
		return pointEventDetectorDAO.getXid(pointEventDetectorId);
	}

	@Override
	public int getDetectorId(String pointEventDetectorXid, int dataPointId) {
		return pointEventDetectorDAO.getId(pointEventDetectorXid, dataPointId);
	}

	@Override
	public String generateEventDetectorUniqueXid(int dataPointId) {
		String xid = DAO.getInstance().generateXid(PointEventDetectorVO.XID_PREFIX);
		while (!isEventDetectorXidUnique(dataPointId, xid, -1)) {
			xid = DAO.getInstance().generateXid(PointEventDetectorVO.XID_PREFIX);
		}
		return xid;
	}

	@Override
	public boolean isEventDetectorXidUnique(int dataPointId, String xid, int excludeId) {
		return pointEventDetectorDAO.isEventDetectorXidUnique(dataPointId, xid, excludeId);
	}

	private void setEventDetectors(DataPointVO dataPoint) {
		dataPoint.setEventDetectors(getEventDetectors(dataPoint));
	}

	public List<PointEventDetectorVO> getEventDetectors(DataPointVO dataPoint) {

		EventDetectorsCache.LOG.trace("getEventDetectors() dpId:" + dataPoint.getId());
		long startTime = 0;
		if (EventDetectorsCache.LOG.isTraceEnabled()) {
			startTime = System.currentTimeMillis();
		}

		List<PointEventDetectorVO> result = null;
		try {
			boolean cacheEnable = ScadaConfig.getInstance().getBoolean(ScadaConfig.ENABLE_CACHE, false);
			if (cacheEnable) {
				result = EventDetectorsCache.getInstance().getEventDetectors(dataPoint);
			} else {
				result = pointEventDetectorDAO.getPointEventDetectors(dataPoint);
			}
		} catch (SchedulerException | IOException e) {
			EventDetectorsCache.LOG.error(e);
		}

		long endTime = 0;
		if (EventDetectorsCache.LOG.isTraceEnabled()) {
			endTime = System.currentTimeMillis();
		}
		EventDetectorsCache.LOG.trace("TimeExecute:"+(endTime-startTime)+ " getEventDetectors() dpId:"+dataPoint.getId());

		return result;
	}

	public void saveEventDetectors(DataPointVO dataPoint) {
		List<PointEventDetectorVO> detectors = getEventDetectors(dataPoint);

		for (PointEventDetectorVO pointEventDetector: detectors) {
			if(!dataPoint.getEventDetectors().contains(pointEventDetector)) {
				pointEventDetectorDAO.delete(dataPoint.getId(), pointEventDetector.getId());
			}
		}
		
		for (PointEventDetectorVO pointEventDetector: dataPoint.getEventDetectors()) {
			try {
			    pointEventDetectorDAO.insert(pointEventDetector);
			} catch (DuplicateKeyException e) {
				pointEventDetectorDAO.update(pointEventDetector);
			}
		}
	}

	public void updateEventDetectorWithType(PointEventDetectorVO eventDetector) {
		pointEventDetectorDAO.updateWithType(eventDetector);
	}

	public void deleteEventDetector(DataPointVO dataPoint, int id){
		pointEventDetectorDAO.delete(dataPoint.getId(), id);
	}

	private PointEventDetectorVO removeFromList(List<PointEventDetectorVO> list, int id) {
		for (PointEventDetectorVO ped : list) {
			if (ped.getId() == id) {
				list.remove(ped);
				return ped;
			}
		}
		return null;
	}

	@Override
	public void copyPermissions(final int fromDataPointId, final int toDataPointId) {

		final List<Tuple<Integer,Integer>> ups = dataPointUserDAO.getDataPointUsers(fromDataPointId);
		dataPointUserDAO.insert(ups, toDataPointId);
	}

	private void setPointComments(DataPointVO dp) {
		dp.setComments(userCommentDAO.getPointComments(dp));
	}

	@Override
	public void savePointsInFolder(PointFolder folder) {

		// Save the points in the subfolders
		for (PointFolder sf : folder.getSubfolders()) {
			savePointsInFolder(sf);
		}

		// Update the folder references in the points.
		DataPointVO dp;
		for (IntValuePair p : folder.getPoints()) {
			dp = getDataPoint(p.getKey());
			// The point may have been deleted while editing the hierarchy.
			if (dp != null) {
				dp.setPointFolderId(folder.getId());
				dataPointDAO.update(dp);
			}
		}
	}

	@Override
	public PointHierarchy getPointHierarchy() {
		if (PointHierarchyDAO.cachedPointHierarchy == null) {
			final Map<Integer, List<PointFolder>> folders = pointHierarchyDAO.getFolderList();

			PointHierarchy ph = new PointHierarchy();
			PointHierarchyService phService = new PointHierarchyService();
			phService.addFoldersToHierarchy(ph, 0 ,folders);

			List<DataPointVO> points = getDataPoints(DataPointExtendedNameComparator.instance, false);
			for (DataPointVO dataPoint: points) {
				ph.addDataPoint(dataPoint.getId(), dataPoint.getPointFolderId(), dataPoint.getExtendedName());
			}

			PointHierarchyDAO.cachedPointHierarchy = ph;
		}
		return PointHierarchyDAO.cachedPointHierarchy;
	}

	@Override
	public void savePointHierarchy(PointFolder root) {
		pointHierarchyService.savePointHierarchy(root);
	}

	@Override
	public void savePointFolder(PointFolder folder, int parentId) {
		pointHierarchyService.savePointFolder(folder, parentId);
	}

	public List<PointHistoryCount> getTopPointHistoryCounts() {
		List<PointHistoryCount> counts = pointValueDAO.getTopPointHistoryCounts();
		List<DataPointVO> points = getDataPoints(DataPointExtendedNameComparator.instance, false);

		for (PointHistoryCount c : counts) {
			for (DataPointVO point : points) {
				if (point.getId() == c.getPointId()) {
					c.setPointName(point.getExtendedName());
					break;
				}
			}
		}

		return counts;
	}

	@Deprecated
	public List<DataPointAccess> getDataPointAccessList(final int userId) {
		return dataPointUserDAO.getDataPointAccessList(userId);
	}

    @Deprecated
	public void deleteDataPointUser(int userId) {
		dataPointUserDAO.delete(userId);
	}

    @Deprecated
	public void insertPermissions(User user) {
		dataPointUserDAO.insertPermissions(user);
	}

	public JsonBinaryEventTextRenderer getBinaryEventTextRenderer(DataPointVO dataPointVO, int value) {
		JsonBinaryEventTextRenderer json = new JsonBinaryEventTextRenderer();
		if (value == 0) {
			json.setEventText(dataPointVO.getEventTextRenderer().getText(false));
		} else if (value == 1) {
			json.setEventText(dataPointVO.getEventTextRenderer().getText(true));
		}
		return json;
	}

	public List<Map<String, Double>> getPointValuesFromRangeXid(String pointString, long startTs, long endTs) {
		List<Integer> pointIds = getDataPointIdsByXid(pointString);
		if(pointIds.isEmpty())
			return Collections.emptyList();
		if (withAggregation()) {
			return pointValueAmChartDao.convertToAmChartDataObject(aggregateValuesFromRange(startTs, endTs, pointIds));
		}
		return pointValueAmChartDao.getPointValuesFromRange(pointIds.stream().mapToInt(i -> i).toArray(), startTs, endTs);
	}

	public List<Map<String, Double>> getPointValuesFromRangeId(String pointString, long startTs, long endTs) {
		List<Integer> pointIds = getDataPointIds(pointString);
		if (pointIds.isEmpty())
			return Collections.emptyList();
		if (withAggregation()) {
			return pointValueAmChartDao.convertToAmChartDataObject(aggregateValuesFromRange(startTs, endTs, pointIds));
		}
		return pointValueAmChartDao.getPointValuesFromRange(pointIds.stream().mapToInt(i -> i).toArray(), startTs, endTs);
	}

	public List<Map<String, Double>> getPointValuesToCompareFromRangeXid(String pointString, long startTs, long endTs) {
		List<Integer> pointIds = getDataPointIdsByXid(pointString);
		if(pointIds.isEmpty())
			return Collections.emptyList();
		if (withAggregation()) {
			return pointValueAmChartDao.convertToAmChartCompareDataObject(aggregateValuesFromRange(startTs, endTs, pointIds), pointIds.get(0));
		}
		return pointValueAmChartDao.getPointValuesToCompareFromRange(pointIds.stream().mapToInt(i -> i).toArray(), startTs, endTs);
	}

	public List<Map<String, Double>> getPointValuesToCompareFromRangeId(String pointString, long startTs, long endTs) {
		List<Integer> pointIds = getDataPointIds(pointString);
		if(pointIds.isEmpty())
			return Collections.emptyList();
		if (withAggregation()) {
			return pointValueAmChartDao.convertToAmChartCompareDataObject(aggregateValuesFromRange(startTs, endTs, pointIds), pointIds.get(0));
		}
		return pointValueAmChartDao.getPointValuesToCompareFromRange(pointIds.stream().mapToInt(i -> i).toArray(), startTs, endTs);
	}

    public List<DataPointVO> getDataPoints(List<Integer> pointIds) {
        return pointIds.stream()
                .map(a -> getDataPointOpt(a))
				.filter(Optional::isPresent)
				.map(Optional::get)
                .collect(Collectors.toList());
    }

	private List<Integer> getDataPointIds(String pointString) {
		List<Integer> pointIds = new ArrayList<>();
		for(String id: pointString.split(",")) {
			pointIds.add(Integer.parseInt(id));
		}
		return pointIds;
	}

	private List<Integer> getDataPointIdsByXid(String pointString) {
		List<Integer> pointIds = new ArrayList<>();
		for(String xid: pointString.split(",")) {
			pointIds.add(getDataPoint(xid).getId());
		}
		return pointIds;
	}

	private List<PointValueAmChartDAO.DataPointSimpleValue> aggregateValuesFromRange(long startTs, long endTs, List<Integer> pointIds) {
		int limit = getNumberOfValuesLimit();
		List<PointValueAmChartDAO.DataPointSimpleValue> pvcList = pointValueAmChartDao.getPointValuesFromRangeWithLimit(pointIds.stream().mapToInt(i -> i).toArray(), startTs, endTs, limit + 1);
		if (pvcList.size() > limit) {
			pvcList.clear();
			return aggregateSortValues(startTs, endTs, pointIds, limit);
		}
		return pvcList;
	}

	private List<PointValueAmChartDAO.DataPointSimpleValue> aggregateSortValues(long startTs, long endTs, List<Integer> pointIds, int limit) {
		long intervalMs = calculateIntervalMs(startTs, endTs, pointIds.size());
		return getDataPoints(pointIds).stream()
				.flatMap(dataPoint -> pointValueAmChartDao.aggregatePointValues(dataPoint, startTs, endTs, intervalMs, limit).stream())
				.sorted(Comparator.comparingLong(PointValueAmChartDAO.DataPointSimpleValue::getTimestamp))
				.collect(Collectors.toList());
	}

    private Optional<DataPointVO> getDataPointOpt(Integer a) {
        if(a == null)
            return Optional.empty();
        DataPointRT dataPointRT = Common.ctx.getRuntimeManager().getDataPoint(a);
        if(dataPointRT == null) {
            try {
                return Optional.ofNullable(getDataPoint(a));
            } catch (Exception ex) {
                LOG.error(ex.getMessage());
                return Optional.empty();
            }
        }
        return Optional.ofNullable(dataPointRT.getVO());
    }
}
