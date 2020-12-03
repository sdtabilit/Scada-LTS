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
package org.scada_lts.mango.service;

import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.UserDao;
import com.serotonin.mango.vo.mailingList.EmailRecipient;
import com.serotonin.mango.vo.mailingList.MailingList;
import com.serotonin.mango.vo.mailingList.UserEntry;
import com.serotonin.mango.web.dwr.beans.RecipientListEntryBean;
import org.joda.time.DateTime;
import org.scada_lts.dao.DAO;
import org.scada_lts.dao.UserDAO;
import org.scada_lts.dao.mailingList.MailingListDAO;
import org.scada_lts.dao.mailingList.MailingListInactiveDAO;
import org.scada_lts.dao.mailingList.MailingListMemberDAO;
import org.scada_lts.mango.adapter.MangoMailingList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service for MailingList
 *
 * @author Mateusz Kaproń Abil'I.T. development team, sdt@abilit.eu
 */
public class MailingListService implements MangoMailingList {

	//TODO
//	@Resource
	private static final MailingListDAO mailingListDAO = new MailingListDAO();

	//TODO
//	@Resource
	private static final MailingListInactiveDAO mailingListInactiveDAO = new MailingListInactiveDAO();

	//TODO
//	@Resource
	private static final MailingListMemberDAO mailingListMemberDAO = new MailingListMemberDAO();

	private void setRelationalData(MailingList mailingList) {
		mailingList.getInactiveIntervals().addAll(mailingListInactiveDAO.getInactiveInterval(mailingList.getId()));
		mailingList.setEntries(mailingListMemberDAO.getEmailRecipient(mailingList.getId()));

		populateEntrySubclasses((mailingList.getEntries()));
	}

	@Override
	public String generateUniqueXid() {
		return DAO.getInstance().generateUniqueXid(MailingList.XID_PREFIX, "mailingLists");
	}

	@Override
	public boolean isXidUnique(String xid, int excludeId) {
		return DAO.getInstance().isXidUnique(xid, excludeId, "mailingLists");
	}

	@Override
	public List<MailingList> getMailingLists() {
		List<MailingList> lists = mailingListDAO.getMailingLists();
		setRelationalData(lists);
		return lists;
	}

	@Override
	public MailingList getMailingList(int id) {
		MailingList mailingList = mailingListDAO.getMailingList(id);
		setRelationalData(mailingList);
		return mailingList;
	}

	@Override
	public MailingList getMailingList(String xid) {
		MailingList mailingList = mailingListDAO.getMailingList(xid);
		if (mailingList != null) {
			setRelationalData(mailingList);
		}
		return mailingList;
	}

	@Override
	public Set<String> getRecipientAddresses(List<RecipientListEntryBean> beans, DateTime sendTime) {
		List<EmailRecipient> entries = new ArrayList<EmailRecipient>(beans.size());
		for (RecipientListEntryBean bean : beans) {
			entries.add(bean.createEmailRecipient());
		}
		populateEntrySubclasses(entries);
		Set<String> addresses = new HashSet<String>();
		for (EmailRecipient entry : entries) {
			entry.appendAddresses(addresses, sendTime);
		}
		return addresses;
	}

	@Override
	public Set<String> getRecipientPhones(List<RecipientListEntryBean> beans, DateTime sendTime) {
		List<EmailRecipient> entries = new ArrayList<EmailRecipient>(beans.size());
		for (RecipientListEntryBean bean : beans) {
			entries.add(bean.createEmailRecipient());
		}
		populateEntrySubclasses(entries);
		Set<String> phones = new HashSet<String>();
		for (EmailRecipient entry : entries) {
			entry.appendPhones(phones, sendTime);
		}
		return phones;
	}

	@Override
	public void populateEntrySubclasses(List<EmailRecipient> entries) {
		// Update the user type entries with their respective user objects.
		UserDAO userDAO = new UserDAO();
		for (EmailRecipient e : entries) {
			if (e instanceof MailingList) {
				setRelationalData((MailingList) e);
			} else if (e instanceof UserEntry) {
				UserEntry ue = (UserEntry) e;
				ue.setUser(userDAO.getUser(ue.getUserId()));
			}
		}
	}

	@Override
	public void saveMailingList(final MailingList mailingList) {
		if (mailingList.getId() == Common.NEW_ID) {
			mailingList.setId(mailingListDAO.insert(mailingList));
		} else {
			mailingListDAO.update(mailingList);
		}
		saveRelationalData(mailingList);
	}

	@Override
	public void saveRelationalData(final MailingList mailingList) {
		mailingListInactiveDAO.delete(mailingList.getId());
		mailingListInactiveDAO.insert(mailingList);

		mailingListMemberDAO.delete(mailingList.getId());
		mailingListMemberDAO.insert(mailingList);
	}

	private void setRelationalData(List<MailingList> mailingLists) {
		for (MailingList mailingList: mailingLists) {
			setRelationalData(mailingList);
		}
	}

	@Override
	public void deleteMailingList(int mailingListId) {
		mailingListDAO.delete(mailingListId);
	}

	public void deleteMailingListMemberWithUserId(int userId) {
		mailingListMemberDAO.deleteWithUserId(userId);
	}
}
