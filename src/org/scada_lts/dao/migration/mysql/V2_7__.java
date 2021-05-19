package org.scada_lts.dao.migration.mysql;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.scada_lts.dao.DAO;
import org.springframework.jdbc.core.JdbcTemplate;

public class V2_7__ extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        final JdbcTemplate jdbcTmp = DAO.getInstance().getJdbcTemp();

        jdbcTmp.execute("ALTER TABLE users " +
                "ADD hideMenu BOOLEAN, " +
                "ADD theme ENUM('MODERN', 'DEFAULT');");

        jdbcTmp.update("UPDATE users SET hideMenu = false, theme = 'DEFAULT';");
    }
}