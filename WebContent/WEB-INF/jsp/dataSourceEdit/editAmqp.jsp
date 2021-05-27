<%--
    SoftQ - http://www.softq.pl/
    Copyright (C) 2019 Softq
    @author Radek Jajko
--%>

<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<%@page import="org.scada_lts.ds.amqp.AmqpPointLocatorVO"%>
<%@page import="org.scada_lts.ds.amqp.ExchangeType"%>
<%@page import="org.scada_lts.ds.amqp.MessageAckType"%>
<%@page import="org.scada_lts.ds.amqp.DurabilityType"%>
<script type="text/javascript">
    function exchangeTypeChange() {
        var exType = $get("exchangeType");
        if (exType === "<%= ExchangeType.NONE %>" ) {
            hide("exchangeFields");
            hide("routingFields");
            show("queueFields");
        } else if ( exType === "<%= ExchangeType.DIRECT %>" ) {
            hide("queueFields");
            show("exchangeFields");
            show("routingFields");
        } else if ( exType === "<%= ExchangeType.TOPIC %>" ) {
            hide("queueFields");
            show("exchangeFields");
            show("routingFields");
        } else if ( exType === "<%= ExchangeType.FANOUT %>" ) {
            hide("queueFields");
            show("exchangeFields");
            hide("routingFields");
        }
    }
  function initImpl() {
    exchangeTypeChange()
  }
  function saveDataSourceImpl() {
        DataSourceEditDwr.saveAmqpDataSource($get("dataSourceName"), $get("dataSourceXid"), $get("updatePeriods"),
                      $get("updatePeriodType"),$get("updateAttempts"),$get("serverIpAddress"), $get("serverPortNumber"),$get("serverUsername"), $get("serverPassword"), $get("serverVirtualHost"), saveDataSourceCB);
    }
  function editPointCBImpl(locator) {
        $set("settable", locator.settable);
        $set("writable", locator.writable);
        $set("dataTypeId", locator.dataTypeId);
        $set("exchangeType", locator.exchangeType);
        $set("exchangeName", locator.exchangeName);
        $set("queueName", locator.queueName);
        $set("queueDurability", locator.queueDurability);
        $set("routingKey", locator.routingKey);
        $set("messageAck", locator.messageAck);
    }
  function savePointImpl(locator) {
    delete locator.relinquishable;
    locator.settable = $get("settable");
    locator.writable = $get("writable");
    locator.dataTypeId = $get("dataTypeId");
    locator.exchangeType = $get("exchangeType");
    locator.exchangeName = $get("exchangeName");
    locator.queueName = $get("queueName");
    locator.queueDurability = $get("queueDurability");
    locator.routingKey = $get("routingKey");
    locator.messageAck = $get("messageAck");
    DataSourceEditDwr.saveAmqpPointLocator(
    currentPoint.id, $get("xid"), $get("name"), locator, savePointCB);
    }
  function appendPointListColumnFunctions(pointListColumnHeaders, pointListColumnFunctions)  {
      pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key="dsEdit.amqp.exchangeType"/>";
      pointListColumnFunctions[pointListColumnFunctions.length] = function(p) { return p.pointLocator.exchangeType; };
  }
</script>


<c:set var="dsDesc"><fmt:message key="dsEdit.amqp.desc"/></c:set>
<c:set var="dsHelpId" value="amqpDS"/>
<%@ include file="/WEB-INF/jsp/dataSourceEdit/dsHead.jspf" %>
        <tr>
    <td class="formLabelRequired"><fmt:message key="dsEdit.updatePeriod"/></td>
    <td class="formField">
      <input type="text" id="updatePeriods" value="${dataSource.updatePeriods}" class="formShort" />
      <sst:select id="updatePeriodType" value="${dataSource.updatePeriodType}">
        <tag:timePeriodOptions sst="false" ms="false" s="true" min="true" h="true"/>
      </sst:select>
    </td>
  </tr>
  <tr>
    <td class="formLabelRequired"><fmt:message key="dsEdit.amqp.updateAttempts"/></td>
    <td class="formField"><input type="text" id="updateAttempts" value="${dataSource.updateAttempts}"/></td>
  </tr>
  <tr>
    <td class="formLabelRequired"><fmt:message key="dsEdit.amqp.address"/></td>
    <td class="formField"><input type="text" id="serverIpAddress" value="${dataSource.serverIpAddress}"/></td>
  </tr>
  <tr>
    <td class="formLabelRequired"><fmt:message key="dsEdit.amqp.port"/></td>
    <td class="formField"><input type="text" id="serverPortNumber" value="${dataSource.serverPortNumber}"/></td>
  </tr>
  <tr>
    <td class="formLabelRequired"><fmt:message key="dsEdit.amqp.username"/></td>
    <td class="formField"><input type="text" id="serverUsername" value="${dataSource.serverUsername}"/></td>
  </tr>
  <tr>
    <td class="formLabelRequired"><fmt:message key="dsEdit.amqp.password"/></td>
    <td class="formField"><input type="text" id="serverPassword" value="${dataSource.serverPassword}"/></td>
  </tr>
  <tr>
    <td class="formLabelRequired"><fmt:message key="dsEdit.amqp.virtualhost"/></td>
    <td class="formField"><input type="text" id="serverVirtualHost" value="${dataSource.serverVirtualHost}"/></td>
  </tr>
<%@ include file="/WEB-INF/jsp/dataSourceEdit/dsEventsFoot.jspf" %>

<tag:pointList pointHelpId="amqpPP">

  <tr>
    <td class="formLabelRequired"><fmt:message key="dsEdit.settable"/></td>
    <td class="formField"><input type="checkbox" id="settable"/></td>
  </tr>
  <tr>
    <td class="formLabelRequired"><fmt:message key="dsEdit.writable"/></td>
    <td class="formField"><input type="checkbox" id="writable"/></td>
   </tr>

  <tr>
    <td class="formLabelRequired"><fmt:message key="dsEdit.pointDataType"/></td>
    <td class="formField">
      <select id="dataTypeId">
        <tag:dataTypeOptions excludeMultistate="true" excludeImage="true" />
      </select>
    </td>
  </tr>

  <tr>
    <td class="formLabelRequired"><fmt:message key="dsEdit.amqp.exchangeType"/></td>
    <td class="formField">
        <select id="exchangeType" onchange="exchangeTypeChange()">
            <option value="<c:out value="<%= ExchangeType.NONE %>"/>">Empty</option>
            <option value="<c:out value="<%= ExchangeType.DIRECT %>"/>">Direct</option>
            <option value="<c:out value="<%= ExchangeType.TOPIC %>"/>">Topic</option>
            <option value="<c:out value="<%= ExchangeType.FANOUT %>"/>">Fanout</option>
        </select>
    </td>
  </tr>
  <tbody id="queueFields" style="display:none;">
    <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.amqp.queueName"/></td>
      <td class="formField"><input type="text" id="queueName"/></td>
    </tr>
  </tbody>
  <tr>
    <td class="formLabelRequired"><fmt:message key="dsEdit.amqp.queueDurability"/></td>
    <td class="formField">
        <select id="queueDurability">
            <option value="<c:out value="<%= DurabilityType.DURABLE %>"/>">Durable</option>
            <option value="<c:out value="<%= DurabilityType.TRANSIENT %>"/>">Transient</option>
        </select>
    </td>
  </tr>
  <tbody id="exchangeFields" style="display:none;">
    <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.amqp.exchangeName"/></td>
      <td class="formField"><input type="text" id="exchangeName"/></td>
    </tr>
  </tbody>
  <tbody id="routingFields" style="display:none;">
    <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.amqp.routingKey"/></td>
      <td class="formField"><input type="text" id="routingKey"/></td>
    </tr>
  </tbody>
  <tr>
    <td class="formLabelRequired"><fmt:message key="dsEdit.amqp.messageAck"/></td>
    <td class="formField">
        <select id="messageAck">
            <option value="<c:out value="<%= MessageAckType.NO_ACK %>"/>">No ACK</option>
            <option value="<c:out value="<%= MessageAckType.ACK %>"/>">ACK</option>
        </select>
    </td>
  </tr>

</tag:pointList>