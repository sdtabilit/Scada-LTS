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
package org.scada_lts.dao.model.mailingList;

/**
 * MailingListMember bean
 *
 * @author Mateusz Kaproń Abil'I.T. development team, sdt@abilit.eu
 */
public class MailingListMember {

	private int mailingListId;
	private int typeId;
	private int userId;
	private String address;
	private String phone;

	public MailingListMember() {

	}

	public MailingListMember(int mailingListId, int typeId, int userId, String address, String phone) {
		this.mailingListId = mailingListId;
		this.typeId = typeId;
		this.userId = userId;
		this.address = address;
		this.phone = phone;
	}

	public int getMailingListId() {
		return mailingListId;
	}

	public void setMailingListId(int mailingListId) {
		this.mailingListId = mailingListId;
	}

	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		MailingListMember that = (MailingListMember) o;

		if (mailingListId != that.mailingListId) {
			return false;
		}
		if (typeId != that.typeId) {
			return false;
		}
		if (userId != that.userId) {
			return false;
		}
		return address.equals(that.address);

	}

	@Override
	public int hashCode() {
		int result = mailingListId;
		result = 31 * result + typeId;
		result = 31 * result + userId;
		result = 31 * result + address.hashCode();
		result = 31 * result + phone.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "MailingListMember{" +
				"mailingListId=" + mailingListId +
				", typeId=" + typeId +
				", userId=" + userId +
				", address='" + address +
				", phone='" + phone + '\'' +
				'}';
	}
}
