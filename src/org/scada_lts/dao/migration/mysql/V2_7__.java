package org.scada_lts.dao.migration.mysql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.scada_lts.dao.DAO;
import org.springframework.jdbc.core.JdbcTemplate;

public class V2_7__ extends BaseJavaMigration {

    private static final Log LOG = LogFactory.getLog(V2_7__.class);

    @Override
    public void migrate(Context context) throws Exception {

        final JdbcTemplate jdbcTmp = DAO.getInstance().getJdbcTemp();

        jdbcTmp.execute("CREATE VIEW springUser AS SELECT username, password, disabled = 'N' as enabled from users;");

        jdbcTmp.execute("CREATE TABLE userRoles (userId int, role LONGTEXT, FOREIGN KEY (userId) REFERENCES users(id));");

        jdbcTmp.execute("INSERT INTO userRoles (userId, role) SELECT id, " +
                "CASE " +
                "WHEN admin = 'Y' THEN 'ROLE_ADMIN' " +
                "WHEN admin != 'Y' THEN 'ROLE_USER'" +
                " END " +
                "FROM users;");

    }
}
