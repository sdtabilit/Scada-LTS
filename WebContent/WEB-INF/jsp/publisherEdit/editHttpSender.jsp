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
<%@page import="com.serotonin.mango.vo.publish.httpSender.HttpSenderVO"%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<script type="text/javascript">
  var staticHeaderList = new Array();
  var staticParameterList = new Array();
  var allPoints = new Array();  
  var selectedPoints = new Array();  
  
  function init() {
      PublisherEditDwr.initSender(initCB);
  }
  dojo.addOnLoad(init);
  
  function initCB(response) {
      var i;
      var list = response.data.allPoints;
      for (var i=0; i<list.length; i++)
          allPoints[allPoints.length] = {
                  id: list[i].id, name: list[i].extendedName, enabled: list[i].enabled, type: list[i].dataTypeMessage};
          
      list = response.data.publisher.staticHeaders;
      for (i=0; i<list.length; i++)
          staticHeaderList[staticHeaderList.length] = {key: list[i].key, value: list[i].value};
      refreshStaticHeaderList();
      
      list = response.data.publisher.staticParameters;
      for (i=0; i<list.length; i++)
          staticParameterList[staticParameterList.length] = {key: list[i].key, value: list[i].value};
      refreshStaticParameterList();
      
      list = response.data.publisher.points;
      for (i=0; i<list.length; i++)
          addToSelectedArray(list[i].dataPointId, list[i].parameterName, list[i].includeTimestamp);
      refreshSelectedPoints();
      PublisherEditDwr.getBasicCredentials(staticHeaderList, setCredentials);
      PublisherEditDwr.getIsUseJSON(setUseJSON);
  }

  function setCredentials(credentials) {
      $set("username", credentials[0]);
      $set("password", credentials[1]);
  }

  function setUseJSON(useJSON) {
      $set("useJSON", useJSON);
  }
  
  function addStaticHeader() {
      var key = $get("sheaderKey");
      var value = $get("sheaderValue");
      
      if (!key || key.trim().length == 0) {
          alert("<fmt:message key="publisherEdit.httpSender.keyRequired"/>");
          return;
      }
      
      for (var i=0; i<staticHeaderList.length; i++) {
          if (staticHeaderList[i].key == key) {
              alert("<fmt:message key="publisherEdit.httpSender.keyExists"/>: '"+ key +"'");
              return;
          }
      }
      
      staticHeaderList[staticHeaderList.length] = {key: key, value: value};
      staticHeaderList.sort();
      refreshStaticHeaderList();
  }
  
  function removeStaticHeader(index) {
      staticHeaderList.splice(index, 1);
      refreshStaticHeaderList();
  }
  
  function refreshStaticHeaderList() {
      dwr.util.removeAllRows("staticHeaderList");
      if (staticHeaderList.length == 0)
          show("noStaticHeadersMsg");
      else {
          hide("noStaticHeadersMsg");
          dwr.util.addRows("staticHeaderList", staticHeaderList, [
                  function(data) { return data.key +"="+ data.value; },
                  function(data, options) {
                      return "<img src='images/bullet_delete.png' class='ptr' title='<fmt:message key="publisherEdit.httpSender.removeHeader"/>' "+
                              "onclick='removeStaticHeader("+ options.rowIndex + ");'/>";
                  }
                  ], null);
      }
  }
  
  function addStaticParameter() {
      var key = $get("sparamKey");
      var value = $get("sparamValue");
      
      if (!key || key.trim().length == 0) {
          alert("<fmt:message key="publisherEdit.httpSender.keyRequired"/>");
          return;
      }
      
      for (var i=0; i<staticParameterList.length; i++) {
          if (staticParameterList[i].key == key) {
              alert("<fmt:message key="publisherEdit.httpSender.keyExists"/>: '"+ key +"'");
              return;
          }
      }
      
      staticParameterList[staticParameterList.length] = {key: key, value: value};
      staticParameterList.sort();
      refreshStaticParameterList();
  }
  
  function removeStaticParameter(index) {
      staticParameterList.splice(index, 1);
      refreshStaticParameterList();
  }
  
  function refreshStaticParameterList() {
      dwr.util.removeAllRows("staticParameterList");
      if (staticParameterList.length == 0)
          show("noStaticParametersMsg");
      else {
          hide("noStaticParametersMsg");
          dwr.util.addRows("staticParameterList", staticParameterList, [
                  function(data) { return data.key +"="+ data.value; },
                  function(data, options) {
                      return "<img src='images/bullet_delete.png' class='ptr' title='<fmt:message key="publisherEdit.httpSender.removeParam"/>' "+
                              "onclick='removeStaticParameter("+ options.rowIndex + ");'/>";
                  }
                  ], null);
      }
  }
  
  function selectPoint() {
      var pointId = $get("availablePoints");
      addToSelectedArray(pointId, null, true);
      refreshSelectedPoints();
  }
  
  function addToSelectedArray(pointId, parameterName, includeTimestamp) {
      var data = getElement(allPoints, pointId);
      
      if (parameterName == null)
          parameterName = data.name;
      
      if (data) {
          // Missing names imply that the point was deleted, so ignore.
          selectedPoints[selectedPoints.length] = {
              id : pointId,
              pointName : data.name,
              enabled : data.enabled,
              pointType : data.type,
              parameterName: parameterName,
              includeTimestamp: includeTimestamp
          };
      }
  }
  
  function removeFromSelectedPoints(pointId) {
      removeElement(selectedPoints, pointId);
      refreshSelectedPoints();
  }
  
  function refreshSelectedPoints() {
      dwr.util.removeAllRows("selectedPoints");
      if (selectedPoints.length == 0)
          show("selectedPointsEmpty");
      else {
          hide("selectedPointsEmpty");
          dwr.util.addRows("selectedPoints", selectedPoints,
              [
                  function(data) { return data.pointName; },
                  function(data) { return "<img src='images/"+ (data.enabled ? "brick_go" : "brick_stop") +".png'/>"; },
                  function(data) { return data.pointType; },
                  function(data) {
                          return "<input type='text' value='"+ data.parameterName +"' "+
                                  "onblur='updateParameterName("+ data.id +", this.value)'/>";
                  },
                  function(data) {
                          return "<input type='checkbox' "+ (data.includeTimestamp ? "checked='checked' " : "") +
                                  "onclick='updateIncludeTimestamp("+ data.id +", this.checked)'/>";
                  },
                  function(data) { 
                          return "<img src='images/bullet_delete.png' class='ptr' "+
                                  "onclick='removeFromSelectedPoints("+ data.id +")'/>";
                  }
              ],
              {
                  rowCreator: function(options) {
                      var tr = document.createElement("tr");
                      tr.className = "row"+ (options.rowIndex % 2 == 0 ? "" : "Alt");
                      return tr;
                  },
                  cellCreator: function(options) {
                      var td = document.createElement("td");
                      if (options.cellNum == 1 || options.cellNum == 4)
                          td.align = "center";
                      return td;
                  } 
              });
      }
      refreshAvailablePoints();
  }
  
  function refreshAvailablePoints() {
      dwr.util.removeAllOptions("availablePoints");
      var availPoints = new Array();
      for (var i=0; i<allPoints.length; i++) {
          var found = false;
          for (var j=0; j<selectedPoints.length; j++) {
              if (selectedPoints[j].id == allPoints[i].id) {
                  found = true;
                  break;
              }
          }
          
          if (!found)
              availPoints[availPoints.length] = allPoints[i];
      }
      dwr.util.addOptions("availablePoints", availPoints, "id", "name");
  }
  
  function updateParameterName(pointId, parameterName) {
      updateElement(selectedPoints, pointId, "parameterName", parameterName);
  }
  
  function updateIncludeTimestamp(pointId, includeTimestamp) {
      updateElement(selectedPoints, pointId, "includeTimestamp", includeTimestamp);
  }
  
  function savePublisherImpl(name, xid, enabled, cacheWarningSize, changesOnly, sendSnapshot, snapshotSendPeriods,
          snapshotSendPeriodType) {
      // Clear messages.
      hide("urlMsg");
      hide("pointsMsg");
      
      var points = new Array();
      for (var i=0; i<selectedPoints.length; i++)
          points[points.length] = {dataPointId: selectedPoints[i].id, parameterName: selectedPoints[i].parameterName,
                  includeTimestamp: selectedPoints[i].includeTimestamp};
      
      PublisherEditDwr.saveHttpSender(name, xid, enabled, points, $get("url"), $get("usePost") == "true", 
    		  staticHeaderList, staticParameterList, cacheWarningSize, changesOnly, $get("raiseResultWarning"),
    		  $get("dateFormat"), sendSnapshot, snapshotSendPeriods, snapshotSendPeriodType,
              $get("username"), $get("password"), $get("useJSON"), savePublisherCB);
  }
  
  function httpSendTest() {
      showMessage("httpSendTestMessage", "<fmt:message key="publisherEdit.httpSender.sending"/>");
      showMessage("httpSendTestData");
      httpSendTestButtons(true);
      PublisherEditDwr.httpSenderTest($get("url"), $get("usePost") == "true", staticHeaderList, staticParameterList,
    		  httpSendTestCB);
  }
  
  function httpSendTestButtons(sending) {
      setDisabled("httpSendTestBtn", sending);
      setDisabled("httpSendTestCancelBtn", !sending);
  }
  
  function httpSendTestCB() {
      setTimeout(httpSendTestUpdate, 2000);
  }
  
  function httpSendTestUpdate() {
      PublisherEditDwr.httpSenderTestUpdate(httpSendTestUpdateCB);
  }
  
  function httpSendTestUpdateCB(result) {
      if (result || result == "") {
          showMessage("httpSendTestMessage");
          if (result)
              showMessage("httpSendTestData", result);
          else
              showMessage("httpSendTestData", "<fmt:message key="publisherEdit.httpSender.noResponseData"/>");
          httpSendTestButtons(false);
      }
      else
          httpSendTestCB();
  }
  
  function httpSendTestCancel() {
      PublisherEditDwr.cancelTestingUtility(httpSendTestCancelCB);
  }
  
  function httpSendTestCancelCB() {
      httpSendTestButtons(false);
      showMessage("httpSendTestMessage", "<fmt:message key="common.cancelled"/>");
  }
</script>

<table cellpadding="0" cellspacing="0">
  <tr>
    <td valign="top">
      <div class="borderDiv marR marB">
        <table>
          <tr>
            <td colspan="2" class="smallTitle"><fmt:message key="publisherEdit.httpSender.props"/> <tag:help id="httpSenderPublishing"/></td>
          </tr>
          
          <tr>
            <td class="formLabelRequired"><fmt:message key="publisherEdit.httpSender.method"/></td>
            <td class="formField">
              <sst:select id="usePost" value="${publisher.usePost}">
                <sst:option value="false">GET</sst:option>
                <sst:option value="true">POST</sst:option>
              </sst:select>
            </td>
          </tr>

          <tr>
            <td class="formLabelRequired"><fmt:message key="publisherEdit.httpSender.useJSON"/></td>
            <td class="formField"><sst:checkbox id="useJSON" /></td>
          </tr>
          
          <tr>
            <td class="formLabelRequired"><fmt:message key="publisherEdit.httpSender.url"/></td>
            <td class="formField">
              <input type="text" id="url" value="${publisher.url}" class="formLong"/>
              <div id="urlMsg" class="formError" style="display:none;"></div>
            </td>
          </tr>

          <tr>
            <td class="formLabelRequired"><fmt:message key="publisherEdit.httpSender.credentials"/></td>
            <td class="formField">
              <fmt:message key="publisherEdit.httpSender.username"/> <input type="text" id="username" class="formShort"/>
              <fmt:message key="publisherEdit.httpSender.password"/> <input type="password" id="password" class="formShort"/>
            </td>
          </tr>
          
          <tr>
            <td class="formLabelRequired"><fmt:message key="publisherEdit.httpSender.staticHeaders"/></td>
            <td class="formField">
              <fmt:message key="publisherEdit.httpSender.headerKey"/> <input type="text" id="sheaderKey" class="formShort"/>
              <fmt:message key="publisherEdit.httpSender.headerValue"/> <input type="text" id="sheaderValue" class="formShort"/>
              <tag:img png="add" title="publisherEdit.httpSender.addStaticHeader" onclick="addStaticHeader()"/>
              <table>
                <tr id="noStaticHeadersMsg" style="display:none"><td><fmt:message key="publisherEdit.httpSender.noStaticHeaders"/></td></tr>
                <tbody id="staticHeaderList"></tbody>
              </table>
            </td>
          </tr>
          
          <tr>
            <td class="formLabelRequired"><fmt:message key="publisherEdit.httpSender.staticParams"/></td>
            <td class="formField">
              <fmt:message key="publisherEdit.httpSender.paramKey"/> <input type="text" id="sparamKey" class="formShort"/>
              <fmt:message key="publisherEdit.httpSender.paramValue"/> <input type="text" id="sparamValue" class="formShort"/>
              <tag:img png="add" title="publisherEdit.httpSender.addStaticParam" onclick="addStaticParameter()"/>
              <table>
                <tr id="noStaticParametersMsg" style="display:none"><td><fmt:message key="publisherEdit.httpSender.noStaticParams"/></td></tr>
                <tbody id="staticParameterList"></tbody>
              </table>
            </td>
          </tr>
          
          <tr>
            <td class="formLabelRequired"><fmt:message key="publisherEdit.httpSender.raiseResultWarning"/></td>
            <td class="formField"><sst:checkbox id="raiseResultWarning"
                    selectedValue="${publisher.raiseResultWarning}"/></td>
          </tr>
          
          <tr>
            <td class="formLabelRequired"><fmt:message key="publisherEdit.httpSender.dateFormat"/></td>
            <td class="formField">
              <sst:select id="dateFormat" value="${publisher.dateFormat}">
                <sst:option value="<%= Integer.toString(HttpSenderVO.DATE_FORMAT_BASIC) %>"><fmt:message key="publisherEdit.httpSender.dateFormat.basic"/></sst:option>
                <sst:option value="<%= Integer.toString(HttpSenderVO.DATE_FORMAT_TZ) %>"><fmt:message key="publisherEdit.httpSender.dateFormat.tz"/></sst:option>
                <sst:option value="<%= Integer.toString(HttpSenderVO.DATE_FORMAT_UTC) %>"><fmt:message key="publisherEdit.httpSender.dateFormat.utc"/></sst:option>
              </sst:select>
            </td>
          </tr>
        </table>
      </div>
    </td>
    
    <td valign="top">
      <div class="borderDiv marB">
        <table>
          <tr><td class="smallTitle"><fmt:message key="publisherEdit.httpSender.sendTest"/></td></tr>
          <tr>
            <td align="center">
              <input id="httpSendTestBtn" type="button" value="<fmt:message key="publisherEdit.httpSender.sendStaticParams"/>" onclick="httpSendTest();"/>
              <input id="httpSendTestCancelBtn" type="button" value="<fmt:message key="publisherEdit.httpSender.cancel"/>" onclick="httpSendTestCancel();"/>
            </td>
          </tr>
          <tr><td id="httpSendTestMessage" class="formError"></td></tr>
          <tr><td class="formField"><pre id="httpSendTestData"></pre></td></tr>
        </table>
      </div>
    </td>
  </tr>
</table>

<table cellpadding="0" cellspacing="0"><tr><td>
  <div class="borderDiv">
    <table width="100%">
      <tr>
        <td class="smallTitle"><fmt:message key="publisherEdit.points"/></td>
        <td align="right">
          <select id="availablePoints"></select>
          <tag:img png="icon_comp_add" onclick="selectPoint()"/>
        </td>
      </tr>
    </table>
    
    <table cellspacing="1" cellpadding="0">
      <tr class="rowHeader">
        <td><fmt:message key="publisherEdit.point.name"/></td>
        <td><fmt:message key="publisherEdit.point.status"/></td>
        <td><fmt:message key="publisherEdit.point.type"/></td>
        <td><fmt:message key="publisherEdit.httpSender.point.param"/></td>
        <td><fmt:message key="publisherEdit.httpSender.point.timestamp"/></td>
        <td></td>
      </tr>
      <tbody id="selectedPointsEmpty" style="display:none;"><tr><td colspan="5"><fmt:message key="publisherEdit.noPoints"/></td></tr></tbody>
      <tbody id="selectedPoints"></tbody>
    </table>
    <div id="pointsMsg" class="formError" style="display:none;"></div>
  </div>
</td></tr></table>