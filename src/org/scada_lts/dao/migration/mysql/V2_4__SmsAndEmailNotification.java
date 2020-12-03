package org.scada_lts.dao.migration.mysql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.scada_lts.dao.DAO;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author rjajko@softq.pl
 */

public class V2_4__SmsAndEmailNotification extends BaseJavaMigration {

    private static final Log LOG = LogFactory.getLog(V2_4__SmsAndEmailNotification.class);

    @Override
    public void migrate(Context context) throws Exception {

        final JdbcTemplate jdbcTmp = DAO.getInstance().getJdbcTemp();

        createRangesTable(jdbcTmp);
        createNotificationsTable(jdbcTmp);
        createSchedulersTable(jdbcTmp);
        createSchedulersDefPointsTable(jdbcTmp);
        createSchedulersUsersTable(jdbcTmp);
        createSchedulerMailingListTable(jdbcTmp);
        createSchedulersView(jdbcTmp);
        createMailingListPlcNotification(jdbcTmp);

        alterMailingListTable(jdbcTmp);

    }

    private void createRangesTable(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("" +
                "CREATE TABLE ranges (" +
                "id INT(11) NOT NULL AUTO_INCREMENT," +
                "hour_start INT(10)," +
                "hour_stop INT(10)," +
                "description VARCHAR(45)," +
                "PRIMARY KEY (id)" +
                ");"
        );
    }

    private void createNotificationsTable(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("" +
                "CREATE TABLE notifications (" +
                "id INT(11) NOT NULL AUTO_INCREMENT," +
                "per_mail INT(10)," +
                "per_sms INT(10)," +
                "mtime TIMESTAMP(6)," +
                "PRIMARY KEY (id)" +
                ");"
        );
    }

    private void createSchedulersTable(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("" +
                "CREATE TABLE schedulers (" +
                "id INT(11) NOT NULL AUTO_INCREMENT," +
                "ranges_id INT(10)," +
                "notifications_id INT(10)," +
                "PRIMARY KEY (id)," +
                "FOREIGN KEY (ranges_id) REFERENCES ranges(id)," +
                "FOREIGN KEY (notifications_id) REFERENCES notifications(id)" +
                ");"
        );
    }

    private void createSchedulersDefPointsTable(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("" +
                "CREATE TABLE schedulers_defpoints (" +
                "dataPoints_id INT(11)," +
                "schedulers_id INT(11)" +
                ");"
        );
    }

    private void createSchedulersUsersTable(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("" +
                "CREATE TABLE schedulers_users (" +
                "id INT(11) NOT NULL AUTO_INCREMENT," +
                "users_id INT(11)," +
                "schedulers_id INT(11)," +
                "PRIMARY KEY (id)," +
                "FOREIGN KEY (users_id) REFERENCES users(id)" +
                ");"
        );
    }

    private void createSchedulerMailingListTable(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("" +
                "CREATE TABLE schedulers_mailing_list (" +
                "mailing_list_id INT (11), " +
                "schedulers_id INT (11)," +
                "FOREIGN KEY (mailing_list_id) REFERENCES mailingLists(id)," +
                "FOREIGN KEY (schedulers_id) REFERENCES schedulers(id));");
    }

    private void alterMailingListTable(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("" +
                "ALTER TABLE mailingListMembers " +
                "ADD phone VARCHAR(20)");
    }

    private void createSchedulersView(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("" +
                "CREATE VIEW schedulers_view AS " +
                "SELECT " +
                "s.id, " +
                "n.per_mail, " +
                "n.per_sms, " +
                "n.mtime, " +
                "r.hour_start, " +
                "r.hour_stop, " +
                "r.description " +
                "FROM ((schedulers AS s " +
                "INNER JOIN notifications AS n ON s.notifications_id=n.id) " +
                "INNER JOIN ranges AS r ON s.ranges_id=r.id);"
        );
    }

    private void createMailingListPlcNotification(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("" +
                "CREATE TABLE mailingListPlcNotification (" +
                "mailing_list_id INT (11), " +
                "datapoint_id INT (11), " +
                "per_sms INT(10), " +
                "per_email INT(10), " +
                "PRIMARY KEY (mailing_list_id, datapoint_id), " +
                "FOREIGN KEY (mailing_list_id) REFERENCES mailingLists(id), " +
                "FOREIGN KEY (datapoint_id) REFERENCES dataPoints(id));");
    }

}
