<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<%@include file="/WEB-INF/tags/decl.tagf"%>
<%@attribute name="styles" fragment="true" %>
<%@attribute name="dwr" %>
<%@attribute name="css" %>
<%@attribute name="js" %>
<%@attribute name="onload" %>
<%@attribute name="jqplugins" %>


<html>
<head>
  <title><c:choose>
    <c:when test="${!empty instanceDescription}">${instanceDescription}</c:when>
    <c:otherwise><fmt:message key="header.title"/></c:otherwise>
  </c:choose></title>

  <!-- Meta -->
  <meta http-equiv="content-type" content="application/xhtml+xml;charset=utf-8"/>
  <meta http-equiv="Content-Style-Type" content="text/css" />
  <meta name="Copyright" content="ScadaLTS &copy;2020"/>
  <meta name="DESCRIPTION" content="ScadaLTS Software"/>
  <meta name="KEYWORDS" content="ScadaLTS Software"/>

  <!-- Style -->
  <link rel="icon" href="images/favicon.ico"/>
  <link rel="shortcut icon" href="images/favicon.ico"/>
  <link href="assets/layout.css" type="text/css" rel="stylesheet"/>
  <link id="pagestyle" href="assets/common_deprecated.css" type="text/css" rel="stylesheet"/>
  <c:forTokens items="${css}" var="cssfile" delims=", ">
    <link href="resources/${cssfile}.css" type="text/css" rel="stylesheet"/>
  </c:forTokens>
  <jsp:invoke fragment="styles"/>

  <!-- Scripts -->
  <script type="text/javascript">
  	var djConfig = { isDebug: false, extraLocale: ['en-us', 'nl', 'nl-nl', 'ja-jp', 'fi-fi', 'sv-se', 'zh-cn', 'zh-tw','xx'] };
  	var ctxPath = "<%=request.getContextPath()%>";
  </script>
  <!-- script type="text/javascript" src="http://o.aolcdn.com/dojo/0.4.2/dojo.js"></script -->
  <script type="text/javascript" src="resources/dojo/dojo.js"></script>
  <script type="text/javascript" src="resources/jQuery/jquery-1.10.2.min.js"></script>
  <c:forTokens items="${jqplugins}" var="plugin" delims=", ">
    <script type="text/javascript" src="resources/jQuery/plugins/${plugin}.js"></script>
  </c:forTokens>
  <script type="text/javascript">
	var jQuery = $;
	$ = null;
  </script>
  <script type="text/javascript" src="dwr/engine.js"></script>
  <script type="text/javascript" src="dwr/util.js"></script>
  <script type="text/javascript" src="dwr/interface/MiscDwr.js"></script>
  <script type="text/javascript" src="resources/soundmanager2-nodebug-jsmin.js"></script>
  <script type="text/javascript" src="resources/common.js"></script>
  <c:forEach items="${dwr}" var="dwrname">
    <script type="text/javascript" src="dwr/interface/${dwrname}.js"></script></c:forEach>
  <c:forTokens items="${js}" var="jsname" delims=", ">
    <script type="text/javascript" src="resources/${jsname}.js"></script></c:forTokens>
  <script type="text/javascript">
    mango.i18n = <sst:convert obj="${clientSideMessages}"/>;
  </script>
  <c:if test="${!simple}">
    <script type="text/javascript" src="resources/header.js"></script>
    <script type="text/javascript">

	    function loadjscssfile(filename, filetype){
			if (filetype=="js"){ //if filename is a external JavaScript file
	    		var fileref=document.createElement('script')
	    		fileref.setAttribute("type","text/javascript")
	    		fileref.setAttribute("src", filename)
			} else if (filetype=="css"){ //if filename is an external CSS file
	    		var fileref=document.createElement("link")
	    		fileref.setAttribute("rel", "stylesheet")
	    		fileref.setAttribute("type", "text/css")
	    		fileref.setAttribute("href", filename)
			}
			if (typeof fileref!="undefined")
	    		document.getElementsByTagName("head")[0].appendChild(fileref)
		};

      dwr.util.setEscapeHtml(false);
      <c:if test="${!empty sessionUser}">
        dojo.addOnLoad(mango.header.onLoad);
        dojo.addOnLoad(function() { setUserMuted(${sessionUser.muted}); });
        dojo.addOnLoad(function() { setUserTheme("${sessionUser.theme}"); });
        <c:if test="${sessionUser.hideMenu}">
          dojo.addOnLoad(function() { setFullscreenIfGraphicView(); });
        </c:if>
      </c:if>

      function setLocale(locale) {
          MiscDwr.setLocale(locale, function() { window.location = window.location });
      }

      function setHomeUrl() {
          MiscDwr.setHomeUrl(window.location.href, function() { alert("Home URL saved"); });
      }

      function goHomeUrl() {
          MiscDwr.getHomeUrl(function(loc) { window.location = loc; });
      }

      function swapStyleSheet(sheet) {
        document.getElementById("pagestyle").setAttribute("href", sheet);
        localStorage.setItem('theme', sheet);
      }

      function setFullscreenIfGraphicView() {
        if(window.location.href.includes("views.shtm")) {
          document.cookie = "fullScreen=yes";
          checkFullScreen();
        }
      }

      function initate() {

        var theme = localStorage.getItem('theme');
        if (theme) {
            document.getElementById("pagestyle").setAttribute("href", theme);
        }

        var style1 = document.getElementById("stylesheet1");
        var style2 = document.getElementById("stylesheet2");
        var style3 = document.getElementById("stylesheet3");

        style1.onclick = function () { swapStyleSheet("assets/common_deprecated.css") };
        style2.onclick = function () { swapStyleSheet("assets/common.css") };
        style3.onclick = function () { swapStyleSheet("assets/common_darker.css") };
      }

      window.onload = initate;

    </script>
  </c:if>
</head>

<body>

<!-- mainHeader -->
<div id="mainHeader">
  <div>
    <img id="logo" src="assets/logo.png" alt="Logo">
  </div>

  <div id="eventsRow">
    <a href="events.shtm">
      <span id="__header__alarmLevelDiv" style="display:none;">
        <img id="__header__alarmLevelImg" src="images/spacer.gif" alt="" border="0" title=""/>
        <span id="__header__alarmLevelText"></span>
      </span>
    </a>
  </div>

  <div>
    <c:if test="${!empty instanceDescription}">
      <span id="instanceDescription" align="right" valign="bottom" class="projectTitle">${instanceDescription}</span>
    </c:if>
  </div>
</div>

<!-- subheader -->
<c:if test="${!simple}">
<div class="navHeader" id="subHeader">
  <div>
    <nav class="flex-default">
      <c:if test="${!empty sessionUser}">
        <div class="spacer">
            <c:choose>
                <c:when test="${sessionUser.hideMenu}">
                    <c:if test="${!empty sessionUser.homeUrl}">
                        <c:set var="homeUrl" value="${fn:split(sessionUser.homeUrl, '?')}" />
                        <c:if test="${homeUrl[0] == 'watch_list.shtm'}">
                            <tag:menuItem href="watch_list.shtm" png="eye" key="header.watchlist"/>
                        </c:if>
                        <c:if test="${homeUrl[0] == 'modern_watch_list.shtm'}">
                          <tag:menuItem href="modern_watch_list.shtm" png="watch_list" key="header.watchlistModern"/>
                        </c:if>
                        <c:if test="${homeUrl[0] == 'views.shtm'}">
                          <tag:menuItem href="views.shtm" png="icon_view" key="header.views"/>
                        </c:if>
                        <c:if test="${homeUrl[0] == 'events.shtm'}">
                          <tag:menuItem href="events.shtm" png="flag_white" key="header.alarms"/>
                        </c:if>
                        <c:if test="${homeUrl[0] == 'app.shtm'}">
                          <tag:menuItem href="app.shtm" png="bell" key="header.alarms"/>
                        </c:if>
                        <c:if test="${homeUrl[0] == 'reports.shtm'}">
                          <tag:menuItem href="reports.shtm" png="report" key="header.reports"/>
                        </c:if>
                    </c:if>
                </c:when>
             <c:otherwise>
                <tag:menuItem href="watch_list.shtm" png="eye" key="header.watchlist"/>
                <tag:menuItem href="modern_watch_list.shtm" png="watch_list" key="header.watchlistModern"/>
                <tag:menuItem href="views.shtm" png="icon_view" key="header.views"/>
                <tag:menuItem href="events.shtm" png="flag_white" key="header.alarms"/>
                <tag:menuItem href="app.shtm" png="bell" key="header.alarms"/>
                <tag:menuItem href="reports.shtm" png="report" key="header.reports"/>
             </c:otherwise>
           </c:choose>
        </div>


        <c:if test="${sessionUser.admin}">
          <div class="spacer">
            <img src="./images/menu_separator.png" class="separator"/>
            <tag:menuItem href="event_handlers.shtm" png="cog" key="header.eventHandlers"/>
            <tag:menuItem href="data_sources.shtm" png="icon_ds" key="header.dataSources"/>
            <tag:menuItem href="scheduled_events.shtm" png="clock" key="header.scheduledEvents"/>
            <tag:menuItem href="compound_events.shtm" png="multi_bell" key="header.compoundEvents"/>
            <tag:menuItem href="point_links.shtm" png="link" key="header.pointLinks"/>
            <tag:menuItem href="scripting.shtm" png="script_gear" key="header.scripts"/>
          </div>
        </c:if>

        <div class="spacer">
          <img src="./images/menu_separator.png" class="separator"/>
          <tag:menuItem href="users.shtm" png="user" key="header.users"/>
        </div>

        <c:if test="${sessionUser.admin}">
          <div class="spacer">
	          <tag:menuItem href="usersProfiles.shtm" png="user_ds" key="header.usersProfiles"/>
            <tag:menuItem href="pointHierarchySLTS" png="folder_brick" key="header.pointHierarchy"/>
            <tag:menuItem href="mailing_lists.shtm" png="book" key="header.mailingLists"/>
            <tag:menuItem href="publishers.shtm" png="transmit" key="header.publishers"/>
            <tag:menuItem href="maintenance_events.shtm" png="hammer" key="header.maintenanceEvents"/>
            <tag:menuItem href="system_settings.shtm" png="application_form" key="header.systemSettings"/>
            <tag:menuItem href="emport.shtm" png="script_code" key="header.emport"/>
            <tag:menuItem href="sql.shtm" png="script" key="header.sql"/>
          </div>
        </c:if>

        <div class="spacer">
          <img src="./images/menu_separator.png" class="separator"/>
          <tag:menuItem href="logout.htm" png="control_stop_blue" key="header.logout"/>
          <tag:menuItem href="help.shtm" png="help" key="header.help"/>
        </div>
      </c:if>

      <c:if test="${empty sessionUser}">
          <tag:menuItem href="login.htm" png="control_play_blue" key="header.login"/>
      </c:if>
      <div id="headerMenuDescription" class="labelDiv" style="position:absolute;display:none;"></div>
    </nav>
  </div>

  <div class="flex-default">
    <div id="navbarUserInfo">
      <c:if test="${!empty sessionUser}">
        <span class="copyTitle"><fmt:message key="header.user"/>:</span>
        <span class="userName">${sessionUser.username}</span>
      </c:if>
    </div>

    <div id="navbarUserProperties" class="flex-default spacer">
      <c:if test="${!empty sessionUser}">
        <c:if test="${!sessionUser.hideMenu}">
            <tag:img id="userMutedImg" onclick="MiscDwr.toggleUserMuted(setUserMuted)" onmouseover="hideLayer('localeEdit')"/>
            <tag:img png="house" title="header.goHomeUrl" onclick="goHomeUrl()" onmouseover="hideLayer('localeEdit')"/>
            <tag:img png="house_link" title="header.setHomeUrl" onclick="setHomeUrl()" onmouseover="hideLayer('localeEdit')"/>
        </c:if>
      </c:if>

      <div class="ptr" onmouseover="showMenu('styleEdit', -100, 10);">
        <tag:img png="theme" title="header.changeTheme"/>
        <div id="styleEdit" class="labelDiv navbar-dropdown--hidden" onmouseout="hideLayer(this)">
          <a class="ptr" id="stylesheet1">Default ScadaBR Theme </a><br/>
          <a class="ptr" id="stylesheet3">Dark ScadaBR Theme</a><br/>
          <a class="ptr" id="stylesheet2">Modern ScadaBR Theme</a><br/>
        </div>
      </div>

      <div class="ptr" onmouseover="showMenu('localeEdit', -40, 10);">
        <tag:img png="world" title="header.changeLanguage"/>
        <div id="localeEdit" class="labelDiv navbar-dropdown--hidden" onmouseout="hideLayer(this)">
          <c:forEach items="${availableLanguages}" var="lang">
            <a class="ptr" onclick="setLocale('${lang.key}')">${lang.value}</a><br/>
          </c:forEach>
        </div>
      </div>
    </div>

  </div>
</div>
</c:if>

<div id="sltsContent" class="content">
  <jsp:doBody/>
</div>
<div id="sltsFooter" class="footer">
    <span>&copy;2012-2021 Scada-LTS <fmt:message key="footer.rightsReserved"/><span>
</div>
<c:if test="${!empty onload}">
  <script type="text/javascript">dojo.addOnLoad(${onload});</script>
</c:if>

</body>
</html>