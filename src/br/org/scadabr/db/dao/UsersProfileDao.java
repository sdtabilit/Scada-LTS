package br.org.scadabr.db.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import br.org.scadabr.api.exception.DAOException;
import br.org.scadabr.vo.permission.ViewAccess;
import br.org.scadabr.vo.permission.WatchListAccess;
import br.org.scadabr.vo.usersProfiles.UsersProfileVO;

import com.serotonin.db.spring.GenericRowMapper;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.BaseDao;
import com.serotonin.mango.db.dao.UserDao;
import com.serotonin.mango.db.dao.ViewDao;
import com.serotonin.mango.db.dao.WatchListDao;
import com.serotonin.mango.view.View;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.WatchList;
import com.serotonin.mango.vo.permission.DataPointAccess;

public class UsersProfileDao extends BaseDao {
	public Log LOG = LogFactory.getLog(UsersProfileDao.class);

	private static List<UsersProfileVO> currentProfileList = null;

	private static final String PROFILES_SELECT = "select u.id, u.name, u.xid "
			+ "from usersProfiles u";

	private static final String PROFILES_INSERT = "insert into usersProfiles (xid, name) values (?, ?)";

	private static final String PROFILES_UPDATE = "update usersProfiles set "
			+ "  name=? " + "where id=?";

	private static final String PROFILES_DELETE = "delete from usersProfiles where id = (?)";

	private WatchListDao watchlistDao = new WatchListDao();
	private ViewDao viewDao = new ViewDao();
	private UserDao userDao = new UserDao();

	public List<UsersProfileVO> getUsersProfiles() {
		if (currentProfileList == null) {
			currentProfileList = query(PROFILES_SELECT + " order by u.name",
					new UsersProfilesRowMapper());
			populateUserProfilePermissions(currentProfileList);
		}
		return currentProfileList;
	}

	public UsersProfileVO getUserProfileByName(String name) {
		/*
		 * UsersProfileVO profile = queryForObject(PROFILES_SELECT +
		 * " where lower(u.name)=?", new Object[] { name.toLowerCase() }, new
		 * UsersProfilesRowMapper(), null);
		 * 
		 * populateUserProfilePermissions(profile); return profile;
		 */
		ListIterator<UsersProfileVO> iterator = currentProfileList
				.listIterator();
		while (iterator.hasNext()) {
			UsersProfileVO iterProfile = iterator.next();
			LOG.debug(iterProfile.getName() + ' ' + iterProfile.getXid());
			if (iterProfile.getName() == name) {
				return iterProfile;
			}
		}
		LOG.debug("Profile not Found!");
		return null;
	}

	public UsersProfileVO getUserProfileById(int id) {
		/*
		 * UsersProfileVO profile = queryForObject(PROFILES_SELECT +
		 * " where u.id=?", new Object[] { id }, new UsersProfilesRowMapper(),
		 * null); populateUserProfilePermissions(profile); return profile;
		 */
		ListIterator<UsersProfileVO> iterator = currentProfileList
				.listIterator();
		while (iterator.hasNext()) {
			UsersProfileVO iterProfile = iterator.next();
			LOG.debug(iterProfile.getName() + ' ' + iterProfile.getXid());
			if (iterProfile.getId() == id) {
				return iterProfile;
			}
		}
		LOG.debug("Profile not Found!");
		return null;
	}

	public UsersProfileVO getUserProfileByXid(String xid) {
		/*
		 * UsersProfileVO profile = queryForObject(PROFILES_SELECT +
		 * " where u.xid=?", new Object[] { xid }, new UsersProfilesRowMapper(),
		 * null); populateUserProfilePermissions(profile); return profile;
		 */
		ListIterator<UsersProfileVO> iterator = currentProfileList
				.listIterator();
		while (iterator.hasNext()) {
			UsersProfileVO iterProfile = iterator.next();
			LOG.debug(iterProfile.getName() + ' ' + iterProfile.getXid());
			if (iterProfile.getXid() == xid) {
				return iterProfile;
			}
		}
		LOG.debug("Profile not Found!");
		return null;
	}

	public void saveUsersProfile(UsersProfileVO profile) throws DAOException {
		if (profileExistsWithThatName(profile)
				&& profile.getId() == Common.NEW_ID) {
			throw new DAOException();
		}

		saveUsersProfileWithoutNameConstraint(profile);
	}

	private boolean profileExistsWithThatName(UsersProfileVO profile) {
		return getUserProfileByName(profile.getName()) != null;
	}

	public void saveUsersProfileWithoutNameConstraint(UsersProfileVO profile)
			throws DAOException {
		if (profile.getName() == null
				|| profile.getName().replaceAll("\\s+", "").isEmpty()) {
			throw new DAOException();
		}

		if (profile.getXid() == null) {
			profile.setXid(generateUniqueXid(UsersProfileVO.XID_PREFIX,
					"usersProfiles"));
		}

		if (profile.getId() == Common.NEW_ID) {
			insertProfile(profile);
		} else {
			updateProfile(profile);
		}
	}

	public void updateProfile(UsersProfileVO profile) {

		ejt.update(PROFILES_UPDATE,
				new Object[] { profile.getName(), profile.getId() });

		List<Integer> usersIds = queryForList(USERS_PROFILES_USERS_SELECT
				+ " where u.userProfileId=?", new Object[] { profile.getId() },
				Integer.class);

		UserDao userDao = new UserDao();

		for (Integer userId : usersIds) {
			User profileUser = userDao.getUser(userId);
			profile.apply(profileUser);
			userDao.saveUser(profileUser);
			this.updateUsersProfile(profile);
		}

		saveRelationalData(profile);

	}

	public void updateUsersProfile(UsersProfileVO profile) {
		if (profile.retrieveLastAppliedUser() != null) {
			ejt.update("delete from usersUsersProfiles where userId=?",
					new Object[] { profile.retrieveLastAppliedUser().getId() });

			ejt.update(
					"insert into usersUsersProfiles (userProfileId, userId) values (?,?)",
					new Object[] { profile.getId(),
							profile.retrieveLastAppliedUser().getId() });
		}

		for (WatchList watchlist : profile.retrieveWatchlists()) {
			watchlistDao.saveWatchList(watchlist);
		}

		for (View view : profile.retrieveViews()) {
			viewDao.saveView(view);
		}

		ListIterator<UsersProfileVO> iterator = currentProfileList
				.listIterator();
		while (iterator.hasNext()) {
			UsersProfileVO iterProfile = iterator.next();
			LOG.debug(iterProfile.getName() + ' ' + iterProfile.getXid());
			if (iterProfile.getId() == profile.getId()) {
				iterator.set(profile);
			}
		}

	}

	private void insertProfile(UsersProfileVO profile) {

		profile.setId(doInsert(PROFILES_INSERT, new Object[] {
				profile.getXid(), profile.getName() }));

		setViews(profile);
		saveRelationalData(profile);

		currentProfileList.add(profile);
	}

	private class UsersProfilesRowMapper implements
			GenericRowMapper<UsersProfileVO> {

		public UsersProfilesRowMapper() {
		};

		public UsersProfileVO mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			UsersProfileVO edt = new UsersProfileVO();
			edt.setId(rs.getInt(1));
			edt.setName(rs.getString(2));
			edt.setXid(rs.getString(3));
			return edt;
		}
	}

	private static final String SELECT_DATA_SOURCE_PERMISSIONS = "select dataSourceId from dataSourceUsersProfiles where userProfileId=?";
	private static final String SELECT_DATA_POINT_PERMISSIONS = "select dataPointId, permission from dataPointUsersProfiles where userProfileId=?";
	private static final String SELECT_WATCHLIST_PERMISSIONS = "select watchlistId, permission from watchListUsersProfiles where userProfileId=?";
	private static final String SELECT_VIEW_PERMISSIONS = "select viewId, permission from viewUsersProfiles where userProfileId=?";
	private static final String USERS_PROFILES_SELECT = "select userProfileId, userId from usersUsersProfiles u";
	private static final String USERS_PROFILES_USERS_SELECT = "select userId from usersUsersProfiles u";

	private void populateUserProfilePermissions(UsersProfileVO profile) {
		if (profile == null) {
			return;
		}

		LOG.debug("populateDataSources");
		populateDataSources(profile);
		LOG.debug("populateDatapoints");
		populateDatapoints(profile);
		LOG.debug("populateWatchlists");
		populateWatchlists(profile);
		LOG.debug("populateViews");
		populateViews(profile);
		LOG.debug("populateUsers");
		populateUsers(profile);
		LOG.debug("end");

	}

	private void populateUsers(UsersProfileVO profile) {
		profile.defineUsers(queryForList(USERS_PROFILES_USERS_SELECT
				+ " where userProfileId=?", new Object[] { profile.getId() },
				Integer.class));
	}

	private void populateWatchlists(UsersProfileVO profile) {
		profile.setWatchlistPermissions(query(SELECT_WATCHLIST_PERMISSIONS,
				new Object[] { profile.getId() },
				new GenericRowMapper<WatchListAccess>() {
					public WatchListAccess mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						WatchListAccess a = new WatchListAccess(rs.getInt(1),
								rs.getInt(2));
						return a;
					}
				}));

		WatchListDao watchListDao = new WatchListDao();
		List<WatchList> allwatchlists = watchListDao.getWatchLists();
		watchListDao.populateWatchlistData(allwatchlists);
		profile.defineWatchlists(allwatchlists);
	}

	private void populateDatapoints(UsersProfileVO profile) {
		profile.setDataPointPermissions(query(SELECT_DATA_POINT_PERMISSIONS,
				new Object[] { profile.getId() },
				new GenericRowMapper<DataPointAccess>() {
					public DataPointAccess mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						DataPointAccess a = new DataPointAccess();
						a.setDataPointId(rs.getInt(1));
						a.setPermission(rs.getInt(2));
						return a;
					}
				}));
	}

	private void populateDataSources(UsersProfileVO profile) {
		profile.setDataSourcePermissions(queryForList(
				SELECT_DATA_SOURCE_PERMISSIONS,
				new Object[] { profile.getId() }, Integer.class));
	}

	private void populateViews(UsersProfileVO profile) {
		profile.setViewPermissions(query(SELECT_VIEW_PERMISSIONS,
				new Object[] { profile.getId() },
				new GenericRowMapper<ViewAccess>() {
					public ViewAccess mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						ViewAccess a = new ViewAccess(rs.getInt(1), rs
								.getInt(2));
						return a;
					}
				}));

		setViews(profile);
	}

	private void setViews(UsersProfileVO profile) {
		List<View> allviews = new ViewDao().getViews();
		profile.defineViews(allviews);
	}

	private void populateUserProfilePermissions(List<UsersProfileVO> profiles) {
		for (UsersProfileVO profile : profiles) {
			LOG.debug("start");
			populateUserProfilePermissions(profile);
			LOG.debug("end");
		}
	}

	private void saveRelationalData(final UsersProfileVO usersProfile) {
		ejt.update("delete from dataSourceUsersProfiles where userProfileId=?",
				new Object[] { usersProfile.getId() });
		ejt.update("delete from dataPointUsersProfiles where userProfileId=?",
				new Object[] { usersProfile.getId() });
		ejt.update("delete from watchListUsersProfiles where userProfileId=?",
				new Object[] { usersProfile.getId() });
		ejt.update("delete from viewUsersProfiles where userProfileId=?",
				new Object[] { usersProfile.getId() });

		ejt.batchUpdate(
				"insert into dataSourceUsersProfiles (dataSourceId, userProfileId) values (?,?)",
				new BatchPreparedStatementSetter() {
					public int getBatchSize() {
						return usersProfile.getDataSourcePermissions().size();
					}

					public void setValues(PreparedStatement ps, int i)
							throws SQLException {
						ps.setInt(1, usersProfile.getDataSourcePermissions()
								.get(i));
						ps.setInt(2, usersProfile.getId());
					}
				});
		ejt.batchUpdate(
				"insert into dataPointUsersProfiles (dataPointId, userProfileId, permission) values (?,?,?)",
				new BatchPreparedStatementSetter() {
					public int getBatchSize() {
						return usersProfile.getDataPointPermissions().size();
					}

					public void setValues(PreparedStatement ps, int i)
							throws SQLException {
						ps.setInt(1, usersProfile.getDataPointPermissions()
								.get(i).getDataPointId());
						ps.setInt(2, usersProfile.getId());
						ps.setInt(3, usersProfile.getDataPointPermissions()
								.get(i).getPermission());
					}
				});

		ejt.batchUpdate(
				"insert into watchListUsersProfiles (watchlistId, userProfileId, permission) values (?,?,?)",
				new BatchPreparedStatementSetter() {
					public int getBatchSize() {
						return usersProfile.getWatchlistPermissions().size();
					}

					public void setValues(PreparedStatement ps, int i)
							throws SQLException {
						ps.setInt(1, usersProfile.getWatchlistPermissions()
								.get(i).getId());
						ps.setInt(2, usersProfile.getId());
						ps.setInt(3, usersProfile.getWatchlistPermissions()
								.get(i).getPermission());
					}
				});

		ejt.batchUpdate(
				"insert into viewUsersProfiles (viewId, userProfileId, permission) values (?,?,?)",
				new BatchPreparedStatementSetter() {
					public int getBatchSize() {
						return usersProfile.getViewPermissions().size();
					}

					public void setValues(PreparedStatement ps, int i)
							throws SQLException {
						ps.setInt(1, usersProfile.getViewPermissions().get(i)
								.getId());
						ps.setInt(2, usersProfile.getId());
						ps.setInt(3, usersProfile.getViewPermissions().get(i)
								.getPermission());
					}
				});
	}

	public UsersProfileVO getUserProfileByUserId(int userid) {
		UsersProfileVO profile = queryForObject(USERS_PROFILES_SELECT
				+ " where u.userId=?", new Object[] { userid },
				new GenericRowMapper<UsersProfileVO>() {
					public UsersProfileVO mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						UsersProfileVO edt = new UsersProfileVO();
						edt.setId(rs.getInt(1));
						return edt;
					}
				}, null);

		if (profile != null) {
			profile = this.getUserProfileById(profile.getId());
		}

		populateUserProfilePermissions(profile);
		return profile;
	}

	public void grantUserAdminProfile(User user) {
		ejt.update("delete from usersUsersProfiles where userId=?",
				new Object[] { user.getId() });

		// Add user to watchLists
		List<WatchList> watchLists = watchlistDao.getWatchLists();
		for (WatchList wl : watchLists) {
			watchlistDao.removeUserFromWatchList(wl.getId(), user.getId());
		}

		// Remove user from Views
		List<View> views = viewDao.getViews();
		for (View view : views) {
			viewDao.removeUserFromView(view.getId(), user.getId());
		}

		user.resetUserProfile();
	}

	public void resetUserProfile(User user) {
		ejt.update("delete from usersUsersProfiles where userId=?",
				new Object[] { user.getId() });

		// Remove user from watchLists
		List<WatchList> watchLists = watchlistDao.getWatchLists();
		for (WatchList wl : watchLists) {
			watchlistDao.removeUserFromWatchList(wl.getId(), user.getId());
		}

		// Remove user from Views
		List<View> views = viewDao.getViews();
		for (View view : views) {
			viewDao.removeUserFromView(view.getId(), user.getId());
		}

		user.resetUserProfile();
	}

	public void setWatchlistDao(WatchListDao dao) {
		this.watchlistDao = dao;
	}

	public void setViewDao(ViewDao dao) {
		this.viewDao = dao;
	}

	public boolean userProfileExists(String xid) {
		UsersProfileVO profile = getUserProfileByXid(xid);
		return profile != null;
	}

	public void deleteUserProfile(final int usersProfileId) {
		// Get Users from Profile
		List<Integer> usersIds = queryForList(USERS_PROFILES_USERS_SELECT
				+ " where u.userProfileId=?", new Object[] { usersProfileId },
				Integer.class);

		// Reset user profile
		for (Integer userId : usersIds) {
			this.resetUserProfile(userDao.getUser(userId));
		}

		getTransactionTemplate().execute(
				new TransactionCallbackWithoutResult() {
					@SuppressWarnings("synthetic-access")
					@Override
					protected void doInTransactionWithoutResult(
							TransactionStatus status) {
						Object[] args = new Object[] { usersProfileId };
						// Delete relational data
						// ejt.update(
						// "delete from dataSourceUsersProfiles where userProfileId=?",
						// args);
						// ejt.update(
						// "delete from dataPointUsersProfiles where userProfileId=?",
						// args);
						// ejt.update(
						// "delete from watchlistUsersProfiles where userProfileId=?",
						// args);
						// ejt.update(
						// "delete from viewUsersProfiles where userProfileId=?",
						// args);
						// Delete the profile
						ejt.update("delete from usersProfiles where id=?", args);
					}
				});
		currentProfileList.clear();
		currentProfileList = null;
	}

}
