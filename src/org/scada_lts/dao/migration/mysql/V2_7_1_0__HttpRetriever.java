package org.scada_lts.dao.migration.mysql;

import com.serotonin.db.KeyValuePair;
import com.serotonin.mango.vo.dataSource.http.HttpRetrieverDataSourceVO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.scada_lts.dao.DAO;
import org.scada_lts.dao.SerializationData;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class V2_7_1_0__HttpRetriever extends BaseJavaMigration {

    private static final Log LOG = LogFactory.getLog(V2_7_1_0__HttpRetriever.class);

    @Override
    public void migrate(Context context) throws Exception {

        final JdbcTemplate jdbcTmp = DAO.getInstance().getJdbcTemp();

        try {
            migrate(jdbcTmp);
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    private void migrate(JdbcTemplate jdbcTmp) {
        List<HttpRetrieverDataSourceVO> dataSources = jdbcTmp.query("SELECT id, data FROM dataSources where dataSourceType = 11", (resultSet, i) -> {
            try (InputStream inputStream = resultSet.getBinaryStream("data");
                 ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
                HttpRetrieverDataSourceVO dataSourceVO = (HttpRetrieverDataSourceVO) objectInputStream.readObject();
                dataSourceVO.setId(resultSet.getInt("id"));
                if (dataSourceVO.getStaticHeaders() == null)
                    dataSourceVO.setStaticHeaders(new ArrayList<KeyValuePair>());
                return dataSourceVO;
            } catch (IOException | ClassNotFoundException ex) {
                LOG.error(ex.getMessage(), ex);
                return null;
            }
        });

        boolean isNull = dataSources.stream().anyMatch(Objects::isNull);
        if (isNull) {
            throw new IllegalStateException("HttpRetrieverDataSourceVO is null!");
        }

        for (HttpRetrieverDataSourceVO dataSource : dataSources) {
            jdbcTmp.update("UPDATE dataSources set data = ? WHERE id = ?",
                    new SerializationData().writeObject(dataSource), dataSource.getId());
        }
    }
}
