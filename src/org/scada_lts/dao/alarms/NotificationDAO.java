package org.scada_lts.dao.alarms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.scada_lts.dao.DAO;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationDAO {

    private static final Log LOG = LogFactory.getLog(NotificationDAO.class);


    private static final String TABLE_NAME_SCHEDULERS = "schedulers";
    private static final String TABLE_NAME_RANGES = "ranges";
    private static final String TABLE_NAME_NOTIFICATIONS = "notifications";
    private static final String TABLE_NAME_SCHEDULERS_DEFPOINTS = "schedulers_defpoints";
    private static final String TABLE_NAME_SCHEDULERS_USERS = "schedulers_users";
    private static final String TABLE_NAME_ML_PLC_NOTIFICATION = "mailingListPlcNotification";

    private static final String VIEW_NAME_SCHEDULERS = "schedulers_view";

    private static final String COLUMN_NAME_ID = "id";
    private static final String COLUMN_NAME_PER_MAIL = "per_mail";
    private static final String COLUMN_NAME_PER_SMS = "per_sms";
    private static final String COLUMN_NAME_MTIME = "mtime";
    private static final String COLUMN_NAME_HOUR_START = "hour_start";
    private static final String COLUMN_NAME_HOUR_STOP = "hour_stop";
    private static final String COLUMN_NAME_DESCRIPTION = "description";
    private static final String COLUMN_NAME_RANGES_ID = "ranges_id";
    private static final String COLUMN_NAME_NOTIFICATIONS_ID = "notifications_id";
    private static final String COLUMN_NAME_SCHEDULERS_ID = "schedulers_id";
    private static final String COLUMN_NAME_DATAPOINTS_ID = "dataPoints_id";
    private static final String COLUMN_NAME_MAILING_LIST_ID = "mailing_list_id";
    private static final String COLUMN_NAME_DATAPOINT_ID = "datapoint_id";
    private static final String COLUMN_NAME_PER_EMAIL = "per_email";
    private static final String COLUMN_NAME_USERS_ID = "users_id";

    private static final String SELECT_FROM_SCHEDULERS_VIEW = "" +
            "SELECT " +
            COLUMN_NAME_ID + ", " +
            COLUMN_NAME_PER_MAIL + ", " +
            COLUMN_NAME_PER_SMS + ", " +
            COLUMN_NAME_MTIME + ", " +
            COLUMN_NAME_HOUR_START + ", " +
            COLUMN_NAME_HOUR_STOP + ", " +
            COLUMN_NAME_DESCRIPTION + " " +
            "FROM " + VIEW_NAME_SCHEDULERS;

    private static final String SELECT_FROM_SCHEDULERS_VIEW_WHERE_ID = "" +
            SELECT_FROM_SCHEDULERS_VIEW +
            " WHERE " + COLUMN_NAME_ID + "=?" +
            ";";

    private static final String INSERT_INTO_SCHEDULERS = "" +
            "INSERT INTO " + TABLE_NAME_SCHEDULERS + " (" +
            COLUMN_NAME_RANGES_ID + ", " +
            COLUMN_NAME_NOTIFICATIONS_ID + ") " +
            "VALUES (?,?);";

    private static final String UPDATE_SCHEDULER = "" +
            "UPDATE " + TABLE_NAME_SCHEDULERS + " SET " +
            COLUMN_NAME_RANGES_ID + "=?, " +
            COLUMN_NAME_NOTIFICATIONS_ID + "=? " +
            "WHERE " + COLUMN_NAME_ID + "=?;";

    private static final String DELETE_SCHEDULER = "" +
            "DELETE FROM " + TABLE_NAME_SCHEDULERS +
            " WHERE " + COLUMN_NAME_ID + "=?;";

    private static final String SELECT_FROM_RANGES = "" +
            "SELECT " +
            COLUMN_NAME_ID + ", " +
            COLUMN_NAME_HOUR_START + ", " +
            COLUMN_NAME_HOUR_STOP + ", " +
            COLUMN_NAME_DESCRIPTION + " " +
            "FROM " + TABLE_NAME_RANGES;

    private static final String SELECT_FROM_RANGES_WHERE_ID = "" +
            SELECT_FROM_RANGES +
            " WHERE " + COLUMN_NAME_ID + "=?;";

    private static final String INSERT_INTO_RANGES = "" +
            "INSERT INTO " + TABLE_NAME_RANGES + " (" +
            COLUMN_NAME_HOUR_START + ", " +
            COLUMN_NAME_HOUR_STOP + ", " +
            COLUMN_NAME_DESCRIPTION + ") " +
            "VALUES (?,?,?);";

    private static final String UPDATE_RANGE = "" +
            "UPDATE " + TABLE_NAME_RANGES + " SET " +
            COLUMN_NAME_HOUR_START + "=?, " +
            COLUMN_NAME_HOUR_STOP + "=?, " +
            COLUMN_NAME_DESCRIPTION + "=? " +
            "WHERE " + COLUMN_NAME_ID + "=?";

    private static final String DELETE_RANGE = "" +
            "DELETE FROM " + TABLE_NAME_RANGES +
            " WHERE " + COLUMN_NAME_ID + "=?;";


    private static final String SELECT_FROM_NOTIFICATIONS = "" +
            "SELECT " +
            COLUMN_NAME_ID + ", " +
            COLUMN_NAME_PER_MAIL + ", " +
            COLUMN_NAME_PER_SMS + ", " +
            COLUMN_NAME_MTIME + " " +
            "FROM " + TABLE_NAME_NOTIFICATIONS;

    private static final String SELECT_FROM_NOTIFICATIONS_WHERE_ID = "" +
            SELECT_FROM_NOTIFICATIONS +
            " WHERE " + COLUMN_NAME_ID + "=?";

    private static final String INSERT_INTO_NOTIFICATIONS = "" +
            "INSERT INTO " + TABLE_NAME_NOTIFICATIONS + " (" +
            COLUMN_NAME_PER_MAIL + ", " +
            COLUMN_NAME_PER_SMS + ", " +
            COLUMN_NAME_MTIME + ") " +
            "VALUES (?,?,?);";

    private static final String UPDATE_NOTIFICATION = "" +
            "UPDATE " + TABLE_NAME_NOTIFICATIONS + " SET " +
            COLUMN_NAME_PER_MAIL + "=?, " +
            COLUMN_NAME_PER_SMS + "=?, " +
            COLUMN_NAME_MTIME + "=? " +
            "WHERE " + COLUMN_NAME_ID + "=?";

    private static final String INSERT_INTO_SCHEDULERS_DEFPOINTS = "" +
            "INSERT INTO " + TABLE_NAME_SCHEDULERS_DEFPOINTS + " (" +
            COLUMN_NAME_DATAPOINTS_ID + ", " +
            COLUMN_NAME_SCHEDULERS_ID + ") " +
            "VALUES (?,?)";

    private static final String INSERT_INTO_SCHEDULERS_USERS = "" +
            "INSERT INTO " + TABLE_NAME_SCHEDULERS_USERS + " (" +
            COLUMN_NAME_USERS_ID + ", " +
            COLUMN_NAME_SCHEDULERS_ID + ") " +
            "VALUES (?,?)";

    private static final String SELECT_SCHEDULER_FROM_DEFPOINTS_BY_DATAPOINT_ID = "" +
            "SELECT " +
            COLUMN_NAME_ID + ", " +
            COLUMN_NAME_PER_MAIL + ", " +
            COLUMN_NAME_PER_SMS + ", " +
            COLUMN_NAME_MTIME + ", " +
            COLUMN_NAME_HOUR_START + ", " +
            COLUMN_NAME_HOUR_STOP + ", " +
            COLUMN_NAME_DESCRIPTION + " " +
            "FROM " + VIEW_NAME_SCHEDULERS + " AS s " +
            "INNER JOIN " + TABLE_NAME_SCHEDULERS_DEFPOINTS + " AS d " +
            "ON s." + COLUMN_NAME_ID + "=d." + COLUMN_NAME_SCHEDULERS_ID +
            " WHERE d." + COLUMN_NAME_DATAPOINTS_ID + "=?";

    private static final String SELECT_SCHEDULERS_WHERE_USER_ID = "" +
            "SELECT " +
            COLUMN_NAME_ID + ", " +
            COLUMN_NAME_PER_MAIL + ", " +
            COLUMN_NAME_PER_SMS + ", " +
            COLUMN_NAME_MTIME + ", " +
            COLUMN_NAME_HOUR_START + ", " +
            COLUMN_NAME_HOUR_STOP + ", " +
            COLUMN_NAME_DESCRIPTION + " " +
            "FROM " + VIEW_NAME_SCHEDULERS + " AS s " +
            "INNER JOIN " + TABLE_NAME_SCHEDULERS_USERS + " AS u " +
            "ON s." + COLUMN_NAME_ID + "=u." + COLUMN_NAME_SCHEDULERS_ID +
            " WHERE u." +COLUMN_NAME_USERS_ID + "=?";

    private static final String SELECT_SCHEDULERS_UD_WHERE_USER_ID = "" +
            "SELECT " +
            "s." + COLUMN_NAME_ID + ", " +
            COLUMN_NAME_PER_MAIL + ", " +
            COLUMN_NAME_PER_SMS + ", " +
            COLUMN_NAME_MTIME + ", " +
            COLUMN_NAME_HOUR_START + ", " +
            COLUMN_NAME_HOUR_STOP + ", " +
            COLUMN_NAME_DESCRIPTION + ", " +
            COLUMN_NAME_DATAPOINTS_ID + ", " +
            "us.email, us.phone " +
            "FROM ((" + VIEW_NAME_SCHEDULERS + " AS s " +
            "INNER JOIN " + TABLE_NAME_SCHEDULERS_USERS + " AS u " +
            "ON s." + COLUMN_NAME_ID + "=u." + COLUMN_NAME_SCHEDULERS_ID + ") " +
            "INNER JOIN " + TABLE_NAME_SCHEDULERS_DEFPOINTS + " AS d " +
            "ON s." + COLUMN_NAME_ID + "=d." +COLUMN_NAME_SCHEDULERS_ID + ") " +
            "INNER JOIN users AS us ON u." + COLUMN_NAME_USERS_ID + "=us.id " +
            "WHERE u." +COLUMN_NAME_USERS_ID + "=?";

    private static final String SELECT_NOTIFICATION_BY_SCHEDULERID = "" +
            "SELECT " +
            "n." + COLUMN_NAME_ID + ", " +
            COLUMN_NAME_PER_MAIL + ", " +
            COLUMN_NAME_PER_SMS + ", " +
            COLUMN_NAME_MTIME + " " +
            " FROM " + TABLE_NAME_NOTIFICATIONS + " AS n " +
            "INNER JOIN " + TABLE_NAME_SCHEDULERS + " AS s " +
            "ON n." +COLUMN_NAME_ID + "=s." + COLUMN_NAME_NOTIFICATIONS_ID +
            " WHERE s." + COLUMN_NAME_ID + "=?";

    private static final String ACKNOWLEDGE_NOTIFICATION = "" +
            "UPDATE " + TABLE_NAME_NOTIFICATIONS + " SET " +
            COLUMN_NAME_MTIME + "=NOW() " +
            "WHERE " + COLUMN_NAME_ID + "=?";

    private static final String SELECT_MLPLCNOTIF_WHERE_MLID = "" +
            "SELECT " +
            COLUMN_NAME_MAILING_LIST_ID + ", " +
            COLUMN_NAME_DATAPOINT_ID + ", " +
            COLUMN_NAME_PER_SMS + ", " +
            COLUMN_NAME_PER_EMAIL + " " +
            "FROM  " + TABLE_NAME_ML_PLC_NOTIFICATION +
            " WHERE "+ COLUMN_NAME_MAILING_LIST_ID +"=?";

    private static final String INSERT_INTO_ML_PLC_NOTIFICATION = "" +
            "INSERT INTO " + TABLE_NAME_ML_PLC_NOTIFICATION + " (" +
            COLUMN_NAME_MAILING_LIST_ID + ", " +
            COLUMN_NAME_DATAPOINT_ID + ", " +
            COLUMN_NAME_PER_SMS + ", " +
            COLUMN_NAME_PER_EMAIL + ") " +
            "VALUES (?,?,?,?);";

    private static final String UPDATE_ML_PLC_NOTIFICATION= "" +
            "UPDATE " + TABLE_NAME_ML_PLC_NOTIFICATION + " SET " +
            COLUMN_NAME_PER_SMS + "=?, " +
            COLUMN_NAME_PER_EMAIL + "=? " +
            "WHERE " + COLUMN_NAME_MAILING_LIST_ID + "=? " +
            "AND " + COLUMN_NAME_DATAPOINT_ID + "=?";

    private static final String SELECT_RECIPIENTS_WHERE_MLID = "" +
            "SELECT ml.mailingListId, ml.address, ml.phone, u.email, u.phone AS user_phone " +
            "FROM mailingListMembers AS ml " +
            "LEFT JOIN users AS u " +
            "ON ml.userId=u.id WHERE mailingListId=?";

    private final JdbcTemplate jdbcTemplate;

    public NotificationDAO() {
        jdbcTemplate = DAO.getInstance().getJdbcTemp();
    }

    public List<Scheduler> getAllSchedulers() {
        try {
            return jdbcTemplate.query(SELECT_FROM_SCHEDULERS_VIEW,
                    new SchedulerRowMapper() {
                    });
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            return null;
        }
    }

    public Scheduler getSchedulerById(Long id) {
        try {
            return jdbcTemplate.queryForObject(SELECT_FROM_SCHEDULERS_VIEW_WHERE_ID,
                    new Object[] {id},
                    new SchedulerRowMapper());
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            return null;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED, rollbackFor = SQLException.class)
    public Scheduler createScheduler(Long rangeId, Long notificationId) {

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(INSERT_INTO_SCHEDULERS, Statement.RETURN_GENERATED_KEYS);
                new ArgumentPreparedStatementSetter(new Object[] {
                        rangeId,
                        notificationId
                }).setValues(ps);
                return ps;
            }
        }, keyHolder);
        return getSchedulerById(keyHolder.getKey().longValue());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED, rollbackFor = SQLException.class)
    public Scheduler updateScheduler(Long schedulerId, Long rangeId, Long notificationId) {
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(UPDATE_SCHEDULER, Statement.RETURN_GENERATED_KEYS);
                new ArgumentPreparedStatementSetter(new Object[]{
                        rangeId,
                        notificationId,
                        schedulerId
                }).setValues(ps);
                return ps;
            }
        });
        return getSchedulerById(schedulerId);
    }

    public void deleteScheduler(Long schedulerId) {
        try {
            jdbcTemplate.update(DELETE_SCHEDULER, schedulerId);
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    public List<Range> getAllRanges() {
        try {
            return jdbcTemplate.query(SELECT_FROM_RANGES,
                    new RangeRowMapper(){});
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }

    public Range getRangeById(Long rangeId) {
        try {
            return jdbcTemplate.queryForObject(SELECT_FROM_RANGES_WHERE_ID,
                    new Object[]{rangeId},
                    new RangeRowMapper() {
                    });
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            return null;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED, rollbackFor = SQLException.class)
    public Range insertRange(Range range) {
        if(LOG.isTraceEnabled()) {
            LOG.trace(range);
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(INSERT_INTO_RANGES, Statement.RETURN_GENERATED_KEYS);
                new ArgumentPreparedStatementSetter(new Object[] {
                        range.getHourStart(),
                        range.getHourStop(),
                        range.getDescription()
                }).setValues(ps);
                return ps;
            }
        }, keyHolder);
        range.setId(keyHolder.getKey().longValue());
        return range;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED, rollbackFor = SQLException.class)
    public Range updateRange(Range range) {
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(UPDATE_RANGE, Statement.RETURN_GENERATED_KEYS);
                new ArgumentPreparedStatementSetter(new Object[]{
                        range.getHourStart(),
                        range.getHourStop(),
                        range.getDescription(),
                        range.getId()
                }).setValues(ps);
                return ps;
            }
        });
        return getRangeById(range.getId());
    }

    public void deleteRange(Long rangeId) {
        try {
            jdbcTemplate.update(DELETE_RANGE, rangeId);
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    public List<Notification> getAllNotifications() {
        try {
            return jdbcTemplate.query(SELECT_FROM_NOTIFICATIONS,
                    new NotificationRowMapper(){});
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }

    public Notification getNotificationById(Long notificationId) {
        try {
            return jdbcTemplate.queryForObject(SELECT_FROM_NOTIFICATIONS_WHERE_ID,
                    new Object[]{notificationId},
                    new NotificationRowMapper() { });
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            return null;
        }
    }

    public Notification getNotificationBySchedulerId(Long schedulerId) {
        try {
            return jdbcTemplate.queryForObject(SELECT_NOTIFICATION_BY_SCHEDULERID,
                    new Object[]{schedulerId},
                    new NotificationRowMapper() { });
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            return null;
        }
    }

    public void acknowledgeNotification(Long notificationId) {
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(ACKNOWLEDGE_NOTIFICATION, Statement.RETURN_GENERATED_KEYS);
                new ArgumentPreparedStatementSetter(new Object[]{
                        notificationId
                }).setValues(ps);
                return ps;
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED, rollbackFor = SQLException.class)
    public Notification insertNotification(Notification notification) {
        if(LOG.isTraceEnabled()) {
            LOG.trace(notification);
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(INSERT_INTO_NOTIFICATIONS, Statement.RETURN_GENERATED_KEYS);
                new ArgumentPreparedStatementSetter(new Object[] {
                        notification.isPerEmail() ? 1 : 0,
                        notification.isPerSms() ? 1 : 0,
                        notification.getMtime()
                }).setValues(ps);
                return ps;
            }
        }, keyHolder);
        notification.setId(keyHolder.getKey().longValue());
        return notification;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED, rollbackFor = SQLException.class)
    public Notification updateNotification(Notification notification) {
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(UPDATE_NOTIFICATION, Statement.RETURN_GENERATED_KEYS);
                new ArgumentPreparedStatementSetter(new Object[]{
                        notification.isPerEmail() ? 1 : 0,
                        notification.isPerSms() ? 1 : 0,
                        notification.getMtime(),
                        notification.getId()
                }).setValues(ps);
                return ps;
            }
        });
        return getNotificationById(notification.getId());
    }

    public List<Scheduler> getDataPointSchedulers(Long dataPointId) {
        try {
            return jdbcTemplate.query(SELECT_SCHEDULER_FROM_DEFPOINTS_BY_DATAPOINT_ID,
                    new Object[]{dataPointId},
                    new SchedulerRowMapper() {
                    });
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }

    public List<MailingListPlcNotification> getMailingListPlcNotifications(Long mailingListId) {
        try {
            return jdbcTemplate.query(SELECT_MLPLCNOTIF_WHERE_MLID,
                    new Object[]{mailingListId},
                    new MailingListPlcNotificationRowMapper() {
                    });
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED, rollbackFor = SQLException.class)
    public MailingListPlcNotification insertMLPlcNotification(MailingListPlcNotification notification) {
        if(LOG.isTraceEnabled()) {
            LOG.trace(notification);
        }

        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(INSERT_INTO_ML_PLC_NOTIFICATION, Statement.RETURN_GENERATED_KEYS);
                new ArgumentPreparedStatementSetter(new Object[] {
                        notification.getMailingListId(),
                        notification.getDataPointId(),
                        notification.isPerSms() ? 1 : 0,
                        notification.isPerEmail() ? 1 : 0
                }).setValues(ps);
                return ps;
            }
        });
        return notification;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED, rollbackFor = SQLException.class)
    public MailingListPlcNotification updateMLPlcNotification(MailingListPlcNotification notification) {
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(UPDATE_ML_PLC_NOTIFICATION, Statement.RETURN_GENERATED_KEYS);
                new ArgumentPreparedStatementSetter(new Object[]{
                        notification.isPerSms() ? 1 : 0,
                        notification.isPerEmail() ? 1 : 0,
                        notification.getMailingListId(),
                        notification.getDataPointId()
                }).setValues(ps);
                return ps;
            }
        });
        return notification;
    }

    public List<Map<String, String>> getMailingListRecipients(Long mailingListId) {
        try {
            return jdbcTemplate.query(SELECT_RECIPIENTS_WHERE_MLID,
                    new Object[]{mailingListId},
                    new RecipientRowMapper() {
                    });
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }

    /**
     * Return all SchedulersUD defined for this specific user
     * with bounded data points by them ID.
     *
     * SchedulersUD is extended Scheduler class containing User email and phone number.
     *
     * @param userId - user ID number to check
     * @return List<SchedulerUserData>
     */
    public List<SchedulerUserData> getAllSchedulersForUser(Long userId) {
        try {
            return jdbcTemplate.query(SELECT_SCHEDULERS_UD_WHERE_USER_ID,
                    new Object[]{userId},
                    new SchedulerUDRowMapper() {
                    });
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            return null;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED, rollbackFor = SQLException.class)
    public void bindSchedulerWithDataPoint(Long schedulerId, Long dataPointId) {
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(INSERT_INTO_SCHEDULERS_DEFPOINTS, Statement.RETURN_GENERATED_KEYS);
                new ArgumentPreparedStatementSetter(new Object[] {
                        dataPointId,
                        schedulerId
                }).setValues(ps);
                return ps;
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED, rollbackFor = SQLException.class)
    public void bindSchedulerWithUser(Long schedulerId, Long userId) {
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(INSERT_INTO_SCHEDULERS_USERS, Statement.RETURN_GENERATED_KEYS);
                new ArgumentPreparedStatementSetter(new Object[] {
                        userId,
                        schedulerId
                }).setValues(ps);
                return ps;
            }
        });
    }

    /* -------- RowMappers -------- */

    private class MailingListPlcNotificationRowMapper implements RowMapper<MailingListPlcNotification> {

        @Override
        public MailingListPlcNotification mapRow(ResultSet resultSet, int i) throws SQLException {
            MailingListPlcNotification result = new MailingListPlcNotification();
            result.setMailingListId(resultSet.getLong(COLUMN_NAME_MAILING_LIST_ID));
            result.setDataPointId(resultSet.getLong(COLUMN_NAME_DATAPOINT_ID));
            result.setPerSms(resultSet.getInt(COLUMN_NAME_PER_SMS) == 1);
            result.setPerEmail(resultSet.getInt(COLUMN_NAME_PER_EMAIL) == 1);
            return result;
        }
    }

    private class RecipientRowMapper implements RowMapper<Map<String, String>> {

        @Override
        public Map<String, String> mapRow(ResultSet resultSet, int i) throws SQLException {
            Map result = new HashMap();
            String address = resultSet.getString("address");
            String phone = resultSet.getString("phone");
            String uAddress = resultSet.getString("email");
            String uPhone = resultSet.getString("user_phone");

            if (address != null && !address.isEmpty()) {
                result.put("address", address);
            } else if (uAddress != null && !uAddress.isEmpty()){
                result.put("address", uAddress);
            } else {
                result.put("address", "");
            }

            if (phone != null && !phone.isEmpty()) {
                result.put("phone", phone);
            } else if (uPhone != null && !uPhone.isEmpty()) {
                result.put("phone", uPhone);
            } else {
                result.put("phone", "");
            }
            return result;
        }
    }

    private class SchedulerRowMapper implements RowMapper<Scheduler> {

        @Override
        public Scheduler mapRow(ResultSet resultSet, int i) throws SQLException {
            Scheduler scheduler = new Scheduler();
            scheduler.setId(resultSet.getLong(COLUMN_NAME_ID));
            scheduler.setPerMail(resultSet.getInt(COLUMN_NAME_PER_MAIL) == 1);
            scheduler.setPerSms(resultSet.getInt(COLUMN_NAME_PER_SMS) == 1);
            scheduler.setMtime(resultSet.getString(COLUMN_NAME_MTIME));
            scheduler.setHourStart(resultSet.getInt(COLUMN_NAME_HOUR_START));
            scheduler.setHourStop(resultSet.getInt(COLUMN_NAME_HOUR_STOP));
            scheduler.setDescription(resultSet.getString(COLUMN_NAME_DESCRIPTION));
            return scheduler;
        }
    }

    private class SchedulerUDRowMapper implements RowMapper<SchedulerUserData> {

        @Override
        public SchedulerUserData mapRow(ResultSet resultSet, int i) throws SQLException {
            SchedulerUserData scheduler = new SchedulerUserData();
            scheduler.setId(resultSet.getLong(COLUMN_NAME_ID));
            scheduler.setPerMail(resultSet.getInt(COLUMN_NAME_PER_MAIL) == 1);
            scheduler.setPerSms(resultSet.getInt(COLUMN_NAME_PER_SMS) == 1);
            scheduler.setMtime(resultSet.getString(COLUMN_NAME_MTIME));
            scheduler.setHourStart(resultSet.getInt(COLUMN_NAME_HOUR_START));
            scheduler.setHourStop(resultSet.getInt(COLUMN_NAME_HOUR_STOP));
            scheduler.setDescription(resultSet.getString(COLUMN_NAME_DESCRIPTION));
            scheduler.setDataPointId(resultSet.getLong(COLUMN_NAME_DATAPOINTS_ID));
            scheduler.setUserEmail(resultSet.getString("email"));
            scheduler.setUserPhone(resultSet.getString("phone"));
            return scheduler;
        }
    }

    private class RangeRowMapper implements RowMapper<Range> {

        @Override
        public Range mapRow(ResultSet resultSet, int i) throws SQLException {
            Range range = new Range();
            range.setId(resultSet.getLong(COLUMN_NAME_ID));
            range.setHourStart(resultSet.getInt(COLUMN_NAME_HOUR_START));
            range.setHourStop(resultSet.getInt(COLUMN_NAME_HOUR_STOP));
            range.setDescription(resultSet.getString(COLUMN_NAME_DESCRIPTION));
            return range;
        }
    }

    private class NotificationRowMapper implements RowMapper<Notification> {

        @Override
        public Notification mapRow(ResultSet resultSet, int i) throws SQLException {
            Notification notif = new Notification();
            notif.setId(resultSet.getLong(COLUMN_NAME_ID));
            notif.setPerEmail(resultSet.getInt(COLUMN_NAME_PER_MAIL) == 1);
            notif.setPerSms(resultSet.getInt(COLUMN_NAME_PER_SMS) == 1);
            notif.setMtime(resultSet.getString(COLUMN_NAME_MTIME));
            return notif;
        }
    }

}
