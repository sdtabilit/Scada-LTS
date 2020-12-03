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
package org.scada_lts.dao.mailingList;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.mango.vo.mailingList.AddressEntry;
import com.serotonin.mango.vo.mailingList.EmailRecipient;
import com.serotonin.mango.vo.mailingList.MailingList;
import com.serotonin.mango.vo.mailingList.UserEntry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.scada_lts.dao.DAO;
import com.serotonin.mango.vo.mailingList.PhoneEntry;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO for MailingListMember
 *
 * @author Mateusz Kaproń Abil'I.T. development team, sdt@abilit.eu
 */
public class MailingListMemberDAO {

	private static final Log LOG = LogFactory.getLog(MailingListMemberDAO.class);

	private static final String COLUMN_NAME_ID = "mailingListId";
	private static final String COLUMN_NAME_TYPE_ID = "typeId";
	private static final String COLUMN_NAME_USER_ID = "userId";
	private static final String COLUMN_NAME_ADDRESS = "address";
	private static final String COLUMN_NAME_PHONE = "phone";

	// @formatter:off
	private static final String MAILING_MEMBER_SELECT = ""
			+ "select "
				+ COLUMN_NAME_TYPE_ID + ", "
				+ COLUMN_NAME_USER_ID + ", "
				+ COLUMN_NAME_ADDRESS + ", "
				+ COLUMN_NAME_PHONE	+ ", "
			+ "'' from mailingListMembers where "
				+ COLUMN_NAME_ID + "=? ";

	private static final String MAILING_MEMBER_INSERT = ""
			+ "insert into mailingListMembers ("
				+ COLUMN_NAME_ID + ", "
				+ COLUMN_NAME_TYPE_ID + ", "
				+ COLUMN_NAME_USER_ID + ", "
				+ COLUMN_NAME_ADDRESS + ", "
				+ COLUMN_NAME_PHONE + ") "
			+ "values (?,?,?,?,?) ";

	private static final String MAILING_MEMBER_DELETE_WHERE_ML_ID = ""
			+ "delete from mailingListMembers where "
				+ COLUMN_NAME_ID + "=? ";

	private static final String MAILING_MEMBER_DELETE_WHERE_USER_ID = ""
			+ "delete from mailingListMembers where "
				+ COLUMN_NAME_USER_ID + "=? ";
	// @formatter:on

	private class EmailRecipientRowMapper implements RowMapper<EmailRecipient> {

		@Override
		public EmailRecipient mapRow(ResultSet rs, int rowNum) throws SQLException {
			int type = rs.getInt(COLUMN_NAME_TYPE_ID);
			switch (type) {
				case EmailRecipient.TYPE_MAILING_LIST:
					MailingList ml = new MailingList();
					ml.setId(rs.getInt(COLUMN_NAME_ID));
					ml.setName(rs.getString(4));
					return ml;
				case EmailRecipient.TYPE_USER:
					UserEntry ue = new UserEntry();
					ue.setUserId(rs.getInt(COLUMN_NAME_USER_ID));
					return ue;
				case EmailRecipient.TYPE_ADDRESS:
					AddressEntry ae = new AddressEntry();
					ae.setAddress(rs.getString(COLUMN_NAME_ADDRESS));
					return ae;
				case EmailRecipient.TYPE_PHONE:
					PhoneEntry pe = new PhoneEntry();
					pe.setPhone(rs.getString(COLUMN_NAME_PHONE));
					return pe;
			}
			throw new ShouldNeverHappenException(
					"Unknown mailing list entry type: " + type);
		}
	}

	public List<EmailRecipient> getEmailRecipient(int id) {

		if (LOG.isTraceEnabled()) {
			LOG.trace("getEmailRecipients(int id) id:" + id);
		}

		return DAO.getInstance().getJdbcTemp().query(MAILING_MEMBER_SELECT, new Object[]{id}, new EmailRecipientRowMapper());
	}

	@Transactional(readOnly = false,propagation= Propagation.REQUIRES_NEW,isolation= Isolation.READ_COMMITTED,rollbackFor=SQLException.class)
	public void insert(final MailingList mailingList) {

		if (LOG.isTraceEnabled()) {
			LOG.trace("insert(final  MailingList mailingList) mailingList:" + mailingList.toString());
		}

		final List<EmailRecipient> entries = mailingList.getEntries();
		DAO.getInstance().getJdbcTemp().batchUpdate(MAILING_MEMBER_INSERT,
				new BatchPreparedStatementSetter() {
					public int getBatchSize() {
						return entries.size();
					}

					public void setValues(PreparedStatement ps, int i)
							throws SQLException {
						EmailRecipient e = entries.get(i);
						ps.setInt(1, mailingList.getId());
						ps.setInt(2, e.getRecipientType());
						ps.setInt(3, e.getReferenceId());
						ps.setString(4, e.getReferenceAddress());
						ps.setString(5, e.getReferencePhone());
					}
				});
	}

	@Transactional(readOnly = false,propagation= Propagation.REQUIRES_NEW,isolation= Isolation.READ_COMMITTED,rollbackFor=SQLException.class)
	public void delete(int id) {

		if (LOG.isTraceEnabled()) {
			LOG.trace("delete(int id) id:" + id);
		}

		DAO.getInstance().getJdbcTemp().update(MAILING_MEMBER_DELETE_WHERE_ML_ID, new Object[]{id});
	}

	@Transactional(readOnly = false,propagation= Propagation.REQUIRES_NEW,isolation= Isolation.READ_COMMITTED,rollbackFor=SQLException.class)
	public void deleteWithUserId(int userId) {

		if (LOG.isTraceEnabled()) {
			LOG.trace("delete(int userId) userId:" + userId);
		}

		DAO.getInstance().getJdbcTemp().update(MAILING_MEMBER_DELETE_WHERE_USER_ID, new Object[]{userId});
	}
}
