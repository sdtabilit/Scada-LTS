package benchmarks;

import com.serotonin.mango.rt.dataImage.IDataPoint;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.SetPointSource;

import java.util.Arrays;
import java.util.List;

public class DataPointRTToBenchmark implements IDataPoint {

    private int id;
    private PointValueTime pointValue;

    public DataPointRTToBenchmark(int id, PointValueTime pointValue) {
        this.id = id;
        this.pointValue = pointValue;
    }

    public int getId() {
        return id;
    }

    @Override
    public List<PointValueTime> getLatestPointValues(int limit) {
        return Arrays.asList(pointValue);
    }

    @Override
    public void updatePointValue(PointValueTime newValue) {
        pointValue = newValue;
    }

    @Override
    public void updatePointValue(PointValueTime newValue, boolean async) {
        pointValue = newValue;
    }

    @Override
    public void setPointValue(PointValueTime newValue, SetPointSource source) {
        pointValue = newValue;
    }

    @Override
    public PointValueTime getPointValue() {
        return pointValue;
    }

    @Override
    public PointValueTime getPointValueBefore(long time) {
        return pointValue;
    }

    @Override
    public List<PointValueTime> getPointValues(long since) {
        return Arrays.asList(pointValue);
    }

    @Override
    public List<PointValueTime> getPointValuesBetween(long from, long to) {
        return Arrays.asList(pointValue);
    }

    @Override
    public int getDataTypeId() {
        return pointValue.getValue().getDataType();
    }
}
