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
package org.scada_lts.mango.adapter;

import com.serotonin.mango.vo.mailingList.EmailRecipient;
import com.serotonin.mango.vo.mailingList.MailingList;
import com.serotonin.mango.web.dwr.beans.RecipientListEntryBean;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Set;

/**
 * MailingListService adapter
 *
 * @author Mateusz Kaproń Abil'I.T. development team, sdt@abilit.eu
 */
public interface MangoMailingList {

	String generateUniqueXid();

	boolean isXidUnique(String xid, int excludeId);

	List<MailingList> getMailingLists();

	MailingList getMailingList(int id);

	MailingList getMailingList(String xid);

	Set<String> getRecipientAddresses(List<RecipientListEntryBean> beans, DateTime sendTime);

	Set<String> getRecipientPhones(List<RecipientListEntryBean> beans, DateTime sendTime);

	void populateEntrySubclasses(List<EmailRecipient> entries);

	void saveMailingList(final MailingList ml);

	void saveRelationalData(final MailingList ml);

	void deleteMailingList(final int mailingListId);
}
