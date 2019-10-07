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
<%@page import="com.serotonin.mango.DataTypes"%>

<script type="text/javascript">
  dojo.addOnLoad(function() {
    if (dataTypeId == <%= DataTypes.NUMERIC %>) {
          show("engineeringUnitsSection");
          show("scalingSection");
      }
      if (dataTypeId == <%= DataTypes.BINARY %>)
                show("parseErrorValueField");
  });
</script>

<div class="borderDiv marB marR">
  <table>
    <tr>
      <td colspan="3">
        <img src="images/icon_comp_edit.png"/>
        <span class="smallTitle"><fmt:message key="pointEdit.props.props"/></span>
        <tag:help id="dataPointEditing"/>
        <a href="data_point_details.shtm?dpid=${form.id}"><tag:img png="icon_comp" title="pointEdit.props.details"/></a>
      </td>
    </tr>
    
    <tr>
      <td class="formLabelRequired"><fmt:message key="pointEdit.props.ds"/></td>
      <td colspan="2" class="formField">
        ${dataSource.name}
        <a href="data_source_edit.shtm?dsid=${dataSource.id}&pid=${form.id}"><tag:img png="icon_ds_edit"
                title="pointEdit.props.editDs"/></a>
      </td>
    </tr>
      
    <spring:bind path="form.name">
      <tr>
        <td class="formLabelRequired"><fmt:message key="pointEdit.props.name"/></td>
        <td class="formField"><input type="text" name="name" value="${status.value}"/></td>
        <c:if test="${error.name != null}"><td class="formError"><fmt:message key="${error.name}"/></td></c:if>
      </tr>
    </spring:bind>

    <spring:bind path="form.parseErrorValue">
          <tr id="parseErrorValueField" style="display:none;">
            <td class="formLabelRequired">Parse error value:</td>
            <td class="formField"><input type="text" name="parseErrorValue" value="${status.value}"/></td>
          </tr>
    </spring:bind>
    
    <tbody id="engineeringUnitsSection" style="display:none;">
      <spring:bind path="form.engineeringUnits">
        <tr>
          <td class="formLabelRequired"><fmt:message key="pointEdit.props.engineeringUnits"/></td>
          <td class="formField">
            <sst:select name="engineeringUnits" value="${status.value}"><tag:engineeringUnits/></sst:select>
          </td>
          <c:if test="${error.engineeringUnits != null}"><td class="formError"><fmt:message key="${error.engineeringUnits}"/></td></c:if>
        </tr>
      </spring:bind>
    </tbody>

    <tbody id="scalingSection" style="display:none;">
	    <tr>
	      <td class="formLabelRequired"><fmt:message key="pointEdit.logging.scaling"/></td>
	      <td class="formField">
	        <fmt:message key="pointEdit.logging.scaling.rawZero"/> <input id="scalingRawZero" type="text" name="scalingRawZero" value="${form.scalingRawZero}" class="formShort"/> 
	        <fmt:message key="pointEdit.logging.scaling.rawFull"/> <input id="scalingRawFull" type="text" name="scalingRawFull" value="${form.scalingRawFull}" class="formShort"/> 
	        <fmt:message key="pointEdit.logging.scaling.engineeringZero"/> <input id="scalingEngineeringZero" type="text" name="scalingEngineeringZero" value="${form.scalingEngineeringZero}" class="formShort"/> 
	        <fmt:message key="pointEdit.logging.scaling.engineeringFull"/> <input id="scalingEngineeringFull" type="text" name="scalingEngineeringFull" value="${form.scalingEngineeringFull}" class="formShort"/>
	      </td>
	      <td class="formError">
	        <spring:bind path="form.purgeType">
	          <c:if test="${error.purgeType != null}"><td class="formError"><fmt:message key="${error.purgeType}"/></td><br/></c:if>
	        </spring:bind>
	        <spring:bind path="form.purgePeriod"><c:if test="${error.purgePeriod != null}"><td class="formError"><fmt:message key="${error.purgePeriod}"/></td></c:if></spring:bind>
	      </td>
	    </tr>
    </tbody>
    
  </table>
</div>