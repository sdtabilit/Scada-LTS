package org.scada_lts.ds.reactivation;

import com.serotonin.mango.rt.dataSource.http.HttpRetrieverDataSourceRT;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.dataSource.http.HttpRetrieverDataSourceVO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.StatefulJob;
import org.quartz.utils.Key;
import org.scada_lts.dao.DataSourceDAO;
import org.scada_lts.ds.StartStopDsRT;
import org.scada_lts.ds.state.StartSleepStateDs;

import java.util.Map;

/**
 * @autor grzegorz.bylica@gmail.com on 24.10.18
 */
public class ReactivationConnectHttpRetriever implements StatefulJob {

    private static final Log LOG = LogFactory.getLog(ReactivationConnectHttpRetriever.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        Key keyJob = jobExecutionContext.getJobDetail().getKey();

        Map.Entry entry = ReactivationManager.getInstance().getId(keyJob.getName());
        int id = (int) entry.getValue();
        String name = (String) entry.getKey();

        DataSourceDAO dao = new DataSourceDAO();
        DataSourceVO<?> ds = dao.getDataSource(id);

        HttpRetrieverDataSourceVO hrds = (HttpRetrieverDataSourceVO) ds;

        if (HttpRetrieverDataSourceRT.testConnection(hrds.getUrl(), hrds.getTimeoutSeconds(), hrds.getRetries(), hrds.getStaticHeaders())) {

            try {
                jobExecutionContext.getScheduler().deleteJob(keyJob.getName(), keyJob.getGroup());
            } catch (SchedulerException e) {
                LOG.error(e);
            } finally {
                StartStopDsRT stopDsRT = new StartStopDsRT(id,true, new StartSleepStateDs());
                new Thread(stopDsRT).start();
                ReactivationManager.getInstance().removeInfoAboutJob(keyJob.getName());
            }
        } else {
            // nothing to do
        }

    }
}
