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
package org.scada_lts.mango.adapter;

import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.bean.PointHistoryCount;
import com.serotonin.mango.vo.hierarchy.PointFolder;
import com.serotonin.mango.vo.hierarchy.PointHierarchy;

import java.util.Comparator;
import java.util.List;

/**
 * DataPointService adapter
 *
 * @author Mateusz Kaproń Abil'I.T. development team, sdt@abilit.eu
 */
public interface MangoDataPoint {

	String generateUniqueXid();

	boolean isXidUnique(String xid, int excludeId);

	String getExtendedPointName(int dataPointId);

	List<DataPointVO> getDataPoints(Comparator<DataPointVO> comparator, boolean includeRelationalData);

	List<DataPointVO> getDataPoints(int dataSourceId, Comparator<DataPointVO> comparator);

	DataPointVO getDataPoint(int id);

	DataPointVO getDataPoint(String xid);

	DataPointVO getDataPointByXid(String xid);

	void saveDataPoint(final DataPointVO dp);

	void insertDataPoint(final DataPointVO dp);

	void updateDataPoint(final DataPointVO dp);

	void updateDataPointShallow(final DataPointVO dp);

	void deleteDataPoints(final int dataSourceId);

	void deleteDataPoint(final int dataPointId);

	void deletePointHistory(int dataPointId);

	void deletePointHistory(int dataPointId, long min, long max);

	void deleteDataPointImpl(String dataPointIdList);

	int getDataPointIdFromDetectorId(int pedId);

	String getDetectorXid(int pedId);

	int getDetectorId(String pedXid, int dataPointId);

	String generateEventDetectorUniqueXid(int dataPointId);

	boolean isEventDetectorXidUnique(int dataPointId, String xid, int excludeId);

	void copyPermissions(final int fromDataPointId, final int toDataPointId);

	PointHierarchy getPointHierarchy();

	void savePointHierarchy(final PointFolder root);

	void savePointFolder(PointFolder folder, int parentId);

	void savePointsInFolder(PointFolder folder);

	List<PointHistoryCount> getTopPointHistoryCounts();
}
