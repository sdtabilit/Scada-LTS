package org.scada_lts.dao.migration.mysql;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.scada_lts.dao.DAO;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @autor grzegorz.bylica@gmail.com on 12.10.2020
 */
public class V2_4__ extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {

        final JdbcTemplate jdbcTmp = DAO.getInstance().getJdbcTemp();

        String sql = "" +
                "ALTER TABLE mangoViews" +
                " ADD modification_time TIMESTAMP" +
                " DEFAULT CURRENT_TIMESTAMP" +
                " ON UPDATE CURRENT_TIMESTAMP;";

        jdbcTmp.execute(sql);

    }


}
