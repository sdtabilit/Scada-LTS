<%--
    Mango - Open Source M2M - http://mango.serotoninsoftware.com
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc.
    @author Matthew Lohbihler
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see http://www.gnu.org/licenses/.
--%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<%@page import="com.serotonin.mango.Common"%>
<%@page import="com.serotonin.mango.vo.mailingList.EmailRecipient"%>
<%@page import="com.serotonin.mango.vo.mailingList.EmailRecipient"%>
<c:set var="NEW_ID"><%= Common.NEW_ID %></c:set>

<tag:page dwr="MailingListsDwr" onload="init">
  <script>
    var users = new Array();
    var editingMailingList;
    var nextAddressEntryId = 1;
    
    function init() {
        MailingListsDwr.init(function(response) {
            users = response.data.users;
            for (var i=0; i<response.data.lists.length; i++) {
                appendMailingList(response.data.lists[i].id);
                updateMailingList(response.data.lists[i]);
            }
            scheduleInit();
        });
    }
    
    function showMailingList(mlId) {
        if (editingMailingList)
            stopImageFader("ml"+ editingMailingList.id +"Img");
        MailingListsDwr.getMailingList(mlId, function(ml) {
            if (!editingMailingList)
                show("mailingListDetails");
            editingMailingList = ml;
            
            $set("xid", ml.xid);
            $set("name", ml.name);
            
            dwr.util.removeAllRows("mailingListEntriesTable");
            var entry;
            for (var i=0; i<ml.entries.length; i++) {
                entry = ml.entries[i];
                if (entry.recipientType == <c:out value="<%= EmailRecipient.TYPE_USER %>"/>)
                    appendUserEntry(entry);
                else if (entry.recipientType == <c:out value="<%= EmailRecipient.TYPE_ADDRESS %>"/>) {
                    entry.referenceId = nextAddressEntryId++;
                    appendAddressEntry(entry);
                } else if (entry.recipientType == <c:out value="<%= EmailRecipient.TYPE_PHONE %>"/>) {
                    entry.referenceId = nextAddressEntryId++;
                    appendPhoneEntry(entry);
                }
            }
            
            updateEmptyListMessage();
            updateUserList();
            
            setInactiveIntervals(ml.inactiveIntervals);
            
            setUserMessage();
        });
        startImageFader("ml"+ mlId +"Img");
        
        if (mlId == ${NEW_ID})
            hide("deleteMailingListImg");
        else
            show("deleteMailingListImg");
    }
    
    function updateUserList() {
        dwr.util.removeAllOptions($("userList"));
        var availUsers = new Array();
        var i, j, user, found;
        for (i=0; i<users.length; i++) {
            user = users[i];
            found = false;
            for (j=0; j<editingMailingList.entries.length; j++) {
                if (editingMailingList.entries[j].referenceId && user.id == editingMailingList.entries[j].referenceId) {
                    found = true;
                    break;
                }
            }
            
            if (!found)
                availUsers[availUsers.length] = user;
        }
        dwr.util.addOptions($("userList"), availUsers, "id", "username");
    }
    
    function saveMailingList() {
        setUserMessage();
        hideContextualMessages("mailingListDetails")
        hideGenericMessages("genericMessages");
        
        MailingListsDwr.saveMailingList(editingMailingList.id, $get("xid"), $get("name"), createRecipList(),
                getInactiveIntervals(), function(response) {
            if (response.hasMessages)
                showDwrMessages(response.messages, $("genericMessages"));
            else {
                if (editingMailingList.id == ${NEW_ID}) {
                    stopImageFader("ml"+ editingMailingList.id +"Img");
                    editingMailingList.id = response.data.mlId;
                    appendMailingList(editingMailingList.id);
                    startImageFader("ml"+ editingMailingList.id +"Img");
                    setUserMessage("<fmt:message key="mailingLists.added"/>");
                    show("deleteMailingListImg");
                }
                else
                    setUserMessage("<fmt:message key="mailingLists.saved"/>");
                MailingListsDwr.getMailingList(editingMailingList.id, updateMailingList)
            }
        });
    }
    
    function sendTestEmail() {
        MailingListsDwr.sendTestEmail(editingMailingList.id, $get("name"), createRecipList(), function(response) {
            stopImageFader("sendTestEmailImg");
            if (response.hasMessages)
                setUserMessage(response.messages[0].genericMessage);
            else
                setUserMessage("<fmt:message key="mailingLists.testEmailMessage"/>");
        });
        startImageFader("sendTestEmailImg");
    }
    
    function createRecipList() {
        var recipList = new Array();
        for (var i=0; i<editingMailingList.entries.length; i++) {
            recipList[i] = {
                recipientType : editingMailingList.entries[i].recipientType,
                referenceId : editingMailingList.entries[i].referenceId,
                referenceAddress : editingMailingList.entries[i].referenceAddress,
                referencePhone : editingMailingList.entries[i].referencePhone
            };
        }
        return recipList;
    }
    
    function setUserMessage(message) {
        if (message) {
            show("userMessage");
            $set("userMessage", message);
        }
        else
            hide("userMessage");
    }
    
    function appendMailingList(mailingListId) {
        createFromTemplate("ml_TEMPLATE_", mailingListId, "mailingListsTable");
    }
    
    function updateMailingList(ml) {
        $set("ml"+ ml.id +"Name", ml.name);
    }
    
    function createUserEntry() {
        var userId = $get("userList");
        var user = null;
        for (var i=0; i<users.length; i++) {
            if (userId == users[i].id) {
                user = users[i];
                break;
            }
        }
        
        if (user == null)
            alert("<fmt:message key="mailingLists.noUser"/>");
        else {
            var userEntry = {
                recipientType : <c:out value="<%= EmailRecipient.TYPE_USER %>"/>,
                referenceId : user.id,
                user : user
            };
            editingMailingList.entries[editingMailingList.entries.length] = userEntry;
            appendUserEntry(userEntry);
        }
        updateUserList();
        updateEmptyListMessage();
    }
    
    function appendUserEntry(userEntry) {
        var content = createFromTemplate("mleUser_TEMPLATE_", userEntry.referenceId, "mailingListEntriesTable");
        setUserImg(userEntry.user.admin, userEntry.user.disabled, $("mle"+ userEntry.referenceId +"Img"));
        $("mle"+ userEntry.referenceId +"Username").innerHTML = userEntry.user.username;
    }
    
    function deleteUserEntry(entryId) {
        // Delete the visual entry.
        $("mailingListEntriesTable").removeChild($("mleUser"+ entryId));
        
        // Delete the list entry.
        for (var i=0; i<editingMailingList.entries.length; i++) {
            if (editingMailingList.entries[i].referenceId == entryId) {
                editingMailingList.entries.splice(i, 1);
                break;
            }
        }
        
        updateUserList();
        updateEmptyListMessage();
    }
    
    function createAddressEntry() {
        var addr = $get("address");
        if (addr == "") {
            alert("<fmt:message key="mailingLists.noAddress"/>");
            return;
        }
        var addressEntry = {
            recipientType : <c:out value="<%= EmailRecipient.TYPE_ADDRESS %>"/>,
            referenceId : nextAddressEntryId++,
            referenceAddress : addr,
            referencePhone : null
        };
        editingMailingList.entries[editingMailingList.entries.length] = addressEntry;
        appendAddressEntry(addressEntry);
        updateEmptyListMessage();
    }

    function createPhoneEntry() {
        var phone = $get("phone");
        if (phone == "") {
            alert("<fmt:message key="mailingLists.noAddress"/>");
            return;
        }
        var phoneEntry = {
            recipientType : <c:out value="<%= EmailRecipient.TYPE_PHONE %>"/>,
            referenceId : nextAddressEntryId++,
            referenceAddress : null,
            referencePhone : phone
        };
        editingMailingList.entries[editingMailingList.entries.length] = phoneEntry;
        appendPhoneEntry(phoneEntry);
        updateEmptyListMessage();
    }
    
    function appendAddressEntry(addressEntry) {
        var content = createFromTemplate("mleAddress_TEMPLATE_", addressEntry.referenceId, "mailingListEntriesTable");
        $("mle"+ addressEntry.referenceId +"Address").innerHTML = addressEntry.referenceAddress;
    }

    function appendPhoneEntry(phoneEntry) {
        var content = createFromTemplate("mlePhone_TEMPLATE_", phoneEntry.referenceId, "mailingListEntriesTable");
        $("mle"+ phoneEntry.referenceId +"Address").innerHTML = phoneEntry.referencePhone;
    }
    
    function deleteAddressEntry(entryId) {
        // Delete the visual entry.
        $("mailingListEntriesTable").removeChild($("mleAddress"+ entryId));
        
        // Delete the list entry.
        for (var i=0; i<editingMailingList.entries.length; i++) {
            if (editingMailingList.entries[i].referenceId == entryId) {
                editingMailingList.entries.splice(i, 1);
                break;
            }
        }

        updateEmptyListMessage();
    }
    
    function updateEmptyListMessage() {
        display("emptyEntryListMessage", editingMailingList.entries.length == 0);
    }
    
    function deleteMailingList() {
        MailingListsDwr.deleteMailingList(editingMailingList.id, function() {
            stopImageFader($("ml"+ editingMailingList.id +"Img"));
            $("mailingListsTable").removeChild($("ml"+ editingMailingList.id));
            hide($("mailingListDetails"));
            editingMailingList = null;
        });
    }
    
    //
    // Schedule editing.
    var mouseDown = false;
    var setOn = false;
    
    function scheduleInit() {
        var tbody = $("scheduleRows");
        var i, tr, td, hour, j, tbl2, tr2, td2, k, content, title, intId;
        for (i=0; i<24; i++) {
            tr = appendNewElement("tr", tbody);
            td = appendNewElement("td", tr);
            hour = ""+ i;
            hour = ("00"+ hour).substring(hour.length);
            td.innerHTML = hour +":00 - "+ hour +":59";
            
            for (j=0; j<7; j++) {
                td = appendNewElement("td", tr);
                td.className = "ptr hreg";
                td.style.paddingLeft = "3px";
                td.onmouseover = function() { hourOver(this); };
                td.onmouseout = function() { hourOut(this); };
                td.onmousedown = function() { return hourDown(this) };
                
                content = '<table cellspacing="1" cellpadding="0"><tr>';
                for (k=0; k<4; k++) {
                    if (k == 0) title = hour +":00";
                    else if (k == 1) title = hour +":15";
                    else if (k == 2) title = hour +":30";
                    else title = hour +":45";
                    intId = getIntervalId(j, i, k);
                    content += '<td id="ivl'+ intId +'" style="padding-right:2px;" title="'+ title +'"';
                    content += ' class="qreg qon" onmouseover="quarterOver(this)" onmouseout="quarterOut(this)"';
                    content += ' onmousedown="quarterDown(this); return false;">&nbsp;</td>';
                }
                content += '</tr></table>';
                td.innerHTML = content;
            }
        }
        
        document.onmousedown = function() { return false; };
        document.onmouseup = function() { mouseDown = false; return false; };
        document.onselectstart = function() { return false; };
    }
    
    function getIntervalId(day, hour, quarter) {
        var interval = quarter;
        interval += hour * 4;
        interval += day * 96;
        return interval;
    }
    
    function quarterOver(node) {
        dojo.html.replaceClass(node, "qhlt", "qreg");
        if (mouseDown)
            setCell(node);
    }
    
    function quarterOut(node) {
        dojo.html.replaceClass(node, "qreg", "qhlt");
    }
    
    function quarterDown(node) {
        mouseDown = true;
        setOn = !isOn(node);
        setCell(node);
        return false;
    }
    
    function setCell(node, on) {
        if (!on && on != false) on = setOn;
        if (on)
            dojo.html.replaceClass(node, "qon", "qoff");
        else
            dojo.html.replaceClass(node, "qoff", "qon");
    }
    
    function isOn(node) { 
        return dojo.html.hasClass(node, "qon");
    }
    
    function hourOver(node) {
        dojo.html.replaceClass(node, "hhlt", "hreg");
        if (mouseDown)
            setHourCells(node);
    }
    
    function hourOut(node) {
        dojo.html.replaceClass(node, "hreg", "hhlt");
        if (mouseDown)
            setHourCells(node);
    }
    
    function hourDown(node) {
        if (mouseDown)
            return;
    
        var tds = node.getElementsByTagName("td");
        mouseDown = true;
        var allOn = true;
        for (var i=0; i<tds.length; i++) {
            if (!isOn(tds[i])) {
                allOn = false;
                break;
            }
        }
        setOn = !allOn;
        return false;
    }
    
    function setHourCells(node) {
        var tds = node.getElementsByTagName("td");
        for (var i=0; i<tds.length; i++)
            setCell(tds[i]);
    }
    
    function setInactiveIntervals(inactive) {
        var d, h, m, iindex = 0, aindex = 0, node;
        for (d=0; d<7; d++) {
            for (h=0; h<24; h++) {
                for (m=0; m<4; m++) {
                    node = $("ivl"+ iindex);
                    if (inactive.length >= aindex && inactive[aindex] == iindex) {
                        setCell(node, false);
                        aindex++;
                    }
                    else
                        setCell(node, true);
                    iindex++;
                }
            }
        }
    }
    
    function getInactiveIntervals() {
        var arr = new Array();
        var d, h, m, iindex = 0, node;
        for (d=0; d<7; d++) {
            for (h=0; h<24; h++) {
                for (m=0; m<4; m++) {
                    node = $("ivl"+ iindex);
                    if (!isOn(node))
                        arr[arr.length] = iindex;
                    iindex++;
                }
            }
        }
        return arr;
    }
  </script>
  
  <table class="subPageHeader">
    <tr>
      <td valign="top">
        <div class="borderDiv">
          <table width="100%">
            <tr>
              <td>
                <span class="smallTitle"><fmt:message key="mailingLists.mailingLists"/></span>
                <tag:help id="mailingLists"/>
              </td>
              <td align="right"><tag:img png="book_add" title="common.add" onclick="showMailingList(${NEW_ID})"
                      id="ml${NEW_ID}Img"/></td>
            </tr>
          </table>
          <table id="mailingListsTable">
            <tbody id="ml_TEMPLATE_" onclick="showMailingList(getMangoId(this))" class="ptr" style="display:none;"><tr>
              <td><tag:img id="ml_TEMPLATE_Img" png="book" title="mailingLists.mailingList"/></td>
              <td class="link" id="ml_TEMPLATE_Name"></td>
            </tr></tbody>
          </table>
        </div>
      </td>
      
      <td valign="top" style="display:none;" id="mailingListDetails">
        <div class="borderDiv">
          <table width="100%">
            <tr>
              <td><span class="smallTitle"><fmt:message key="mailingLists.details"/></span></td>
              <td align="right">
                <tag:img png="save" onclick="saveMailingList();" title="common.save"/>
                <tag:img id="deleteMailingListImg" png="delete" onclick="deleteMailingList();" title="common.delete"/>
                <tag:img id="sendTestEmailImg" png="email_go" onclick="sendTestEmail();" title="common.sendTestEmail"/>
              </td>
            </tr>
          </table>
          
          <table><tr><td colspan="2" id="userMessage" class="formError" style="display:none;"></td></tr></table>
          
          <table width="100%">
            <tr>
              <td class="formLabelRequired"><fmt:message key="common.xid"/></td>
              <td class="formField"><input type="text" id="xid"/></td>
            </tr>
            
            <tr>
              <td class="formLabelRequired"><fmt:message key="mailingLists.name"/></td>
              <td class="formField"><input id="name" type="text" onmousedown="this.focus()"/></td>
            </tr>
            <tr><td class="horzSeparator" colspan="2"></td></tr>
            <tr>
              <td class="formLabel"><fmt:message key="mailingLists.addUser"/></td>
              <td class="formField">
                <select id="userList"></select>
                <tag:img png="add" title="common.add" onclick="createUserEntry()"/>
              </td>
            </tr>
            <tr>
              <td class="formLabel"><fmt:message key="mailingLists.addAddress"/></td>
              <td class="formField">
                <input id="address" type="text" class="formLong" onmousedown="this.focus()"/>
                <tag:img png="add" title="common.add" onclick="createAddressEntry()"/>
              </td>
            </tr>
            <tr>
                <td class="formLabel"><fmt:message key="mailingLists.addPhone"/></td>
                <td class="formField">
                  <input id="phone" type="text" class="formLong" onmousedown="this.focus()"/>
                  <tag:img png="add" title="common.add" onclick="createPhoneEntry()"/>
                </td>
              </tr>
          </table>
          
          <table width="100%">
            <tr><td colspan="3" class="smallTitle"><fmt:message key="mailingLists.entries"/></td></tr>
            <tr id="emptyEntryListMessage">
              <td colspan="3"><fmt:message key="mailingLists.noEntries"/></td>
            </tr>
            <tr id="mleUser_TEMPLATE_" style="display:none;">
              <td width="16"><img id="mle_TEMPLATE_Img" src="images/hourglass.png"/></td>
              <td id="mle_TEMPLATE_Username"></td>
              <td width="16"><tag:img png="bullet_delete" title="common.delete" onclick="deleteUserEntry(getMangoId(this));"/></td>
            </tr>
            <tr id="mleAddress_TEMPLATE_" style="display:none;">
              <td width="16"><tag:img png="email" title="mailingLists.emailAddress"/></td>
              <td id="mle_TEMPLATE_Address"></td>
              <td width="16"><tag:img png="bullet_delete" title="common.delete" onclick="deleteAddressEntry(getMangoId(this));"/></td>
            </tr>
            <tr id="mlePhone_TEMPLATE_" style="display:none;">
                <td width="16"><tag:img png="clock" title="mailingLists.emailAddress"/></td>
                <td id="mle_TEMPLATE_Address"></td>
                <td width="16"><tag:img png="bullet_delete" title="common.delete" onclick="deleteAddressEntry(getMangoId(this));"/></td>
            </tr>
            <tbody id="mailingListEntriesTable">
              <tr><td width="16"></td><td></td><td width="16"></td></tr>
            </tbody>
          </table>
          <table width="100%">
            <tbody id="genericMessages"></tbody>
          </table>
          
          <table>
            <tr><td colspan="5" class="horzSeparator" colspan="2"></td></tr>
            <tr>
              <td class="smallTitle"><fmt:message key="common.activeTime"/></td>
              <td class="qreg qon"></td>
              <td><fmt:message key="common.active"/></td>
              <td class="qreg qoff"></td>
              <td><fmt:message key="common.inactive"/></td>
            </tr>
            <tr>
              <td style="padding:5px;" colspan="5" >
                <table cellspacing="0" cellpadding="0">
                  <tr>
                    <td></td>
                    <th><fmt:message key="common.day.short.mon"/></th>
                    <th><fmt:message key="common.day.short.tue"/></th>
                    <th><fmt:message key="common.day.short.wed"/></th>
                    <th><fmt:message key="common.day.short.thu"/></th>
                    <th><fmt:message key="common.day.short.fri"/></th>
                    <th><fmt:message key="common.day.short.sat"/></th>
                    <th><fmt:message key="common.day.short.sun"/></th>
                  </tr>
                  <tbody id="scheduleRows"></tbody>
                </table>
              </td>
            </tr>
          </table>
        </div>
      </td>
    </tr>
  </table>
</tag:page>