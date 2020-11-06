<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core'%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">

<link
	href="resources/node_modules/bootstrap/dist/css/bootstrap.min.css"
	rel="stylesheet" type="text/css">
<link
	href="resources/node_modules/jquery.fancytree/dist/skin-bootstrap/ui.fancytree.min.css"
	rel="stylesheet" type="text/css">
<link
	href="resources/node_modules/bootstrap3-dialog/dist/css/bootstrap-dialog.min.css"
	rel="stylesheet" type="text/css">

<link
	href="resources/node_modules/sweetalert2/dist/sweetalert2.css"
	rel="stylesheet" type="text/css">

<link href="resources/js-ui/app/css/chunk-vendors.css" rel="stylesheet" type="text/css">
<link href="resources/js-ui/app/css/app.css" rel="stylesheet" type="text/css">

<script src="resources/node_modules/vue-jsoneditor/dist/lib/vjsoneditor.min.js"></script>

<link
	href="resources/node_modules/vue-jsoneditor/dist/lib/vjsoneditor.min.css"
    rel="stylesheet" type="text/css">


<style type="text/css">

/* correcting jsoneditor */
button.jsoneditor-compact {
  background-images: url("../resources/node_modules/vue-jsoneditor/dist/docs/img/jsoneditor-icons.bfab7b1.svg");
}
/* Reduce bootstrap's default 'panel' padding: */
div#tree {
	padding: 3px 5px;
}

#draggableSample, #droppableSample {
	height: 100px;
	padding: 0.5em;
	width: 150px;
	border: 1px solid #AAAAAA;
}

#draggableSample {
	background-color: silver;
	color: #222222;
}

#droppableSample {
	background-color: maroon;
	color: white;
}

#droppableSample.drophover {
	border: 1px solid green;
}

.menu-padding {
	padding-top: 40px;
}

.dropdown-menu-right {
	float: left;
}

.btn-separator:after {
	content: ' ';
	display: block;
	float: left;
	background: #2fb34a;
    margin: 5px 6px;
    height: 16px;
	width: 1px;
}

#mainHeader {
	padding-top: 5px;
	background-color: #d6d5d5;
}

.menu {
	margin-top: 10px;
}

.logo {
	margin: 5px;
}

.margin_nav {
	margin-top: 5px;
	line-height: 20px;
	margin-bottom: 5px;
}

.nav-pills>li>a {
	line-height: 1px;
	margin-right: 1px;
	padding: 4px;
}
.nav-pills>li>a>img {
    max-width: 16px;
}

table {
    font-size: 12px;
    border-color: white;

}

thead th {
    background-color: #006DCC;
    color: white;
}

</style>

</head>
<body onload="onloadHandler();"  onunload="onunloadHandler();" >
	<div class="container-fluid">
		<div class="row">
			<table width="100%" cellspacing="0" cellpadding="0" border="0"
				id="mainHeader">
				<tr>
					<td><img id="logo" class="logo" src="assets/logo.png"
						alt="Logo" /></td>
					<c:if test="${!simple}">
						<td align="center" width="99%" id="eventsRow">
						<span id="__header__MemoryInfo" style="visibility:visible;">
							<input type="button" value="UserSessions" onclick="OnListUserSessions();">
							<input type="button" value="SessonAttributes" onclick="OnListSessionsAttributes();">
							<input type="button" value="WebsocketStats" onclick="OnListWebsocketStats();">

						</span>
						<a
							href="events.shtm">
<!-- 							<span id="__header__alarmLevelDiv" -->
<!-- 								style="display: none;"> <img id="__header__alarmLevelImg" -->
<!-- 									src="images/spacer.gif" alt="" border="0" title="" /> <span -->
<!-- 									id="__header__alarmLevelText"></span> -->
<!-- 							</span> -->
		         			<span id="__header__alarmLevelDiv" style="visibility:visible;display:visible;">
		           			<img id="__header__alarmLevelImg" src="images/spacer.gif" alt="" border="0" title=""/>
		           			<span id="__header__alarmLevelText">text</span>
		         			</span>
						</a></td>
					</c:if>
					<c:if test="${!empty instanceDescription}">
						<td align="right" valign="bottom" class="projectTitle"
							style="padding: 5px; white-space: nowrap;">${instanceDescription}</td>
					</c:if>
				</tr>
			</table>
		</div>



		<div class="row">
			<div class="col-md-12">
				<nav>
					<ul class="nav nav-pills margin_nav">
						<c:if test="${!empty sessionUser}">

							<li role="presentation"><a href="watch_list.shtm"
								class='btn btn-xs' data-toggle="tooltip" data-placement="top"
								title='<fmt:message key="menu.watchlists.tooltip"/>'><img
									src="images/eye.png"></a></li>
							<li role="presentation"><a href="views.shtm"
								class='btn btn-xs' data-toggle="tooltip" data-placement="top"
								title='<fmt:message key="menu.graphicalviews.tooltip"/>'><img
									src="images/icon_view.png"></a></li>
							<li role="presentation"><a href="events.shtm"
								class='btn btn-xs' data-toggle="tooltip" data-placement="top"
								title='<fmt:message key="menu.alarms.tooltip"/>'><img
									src="images/flag_white.png"></a></li>
							<li role="presentation"><a href="reports.shtm"
								class='btn btn-xs' data-toggle="tooltip" data-placement="top"
								title='<fmt:message key="menu.reports.tooltip"/>'><img
									src="images/report.png"></a></li>

							<c:if test="${sessionUser.dataSourcePermission}">
								<span class="btn-separator"></span>
								<li role="presentation"><a href="event_handlers.shtm"
									class='btn btn-xs' data-toggle="tooltip" data-placement="top"
									title='<fmt:message key="menu.event_handlers.tooltip"/>'><img
										src="images/cog.png" /></a></li>
								<li role="presentation"><a href="data_sources.shtm"
									class='btn btn-xs' data-toggle="tooltip" data-placement="top"
									title='<fmt:message key="menu.data_source.tooltip"/>'><img
										src="images/icon_ds.png" /></a></li>
								<li role="presentation"><a href="scheduled_events.shtm"
									class='btn btn-xs' data-toggle="tooltip" data-placement="top"
									title='<fmt:message key="menu.scheduled_events.tooltip"/>'><img
										src="images/clock.png" /></a></li>
								<li role="presentation"><a href="compound_events.shtm"
									class='btn btn-xs' data-toggle="tooltip" data-placement="top"
									title='<fmt:message key="menu.compound_event_detectors.tooltip"/>'><img
										src="images/multi_bell.png" /></a></li>
								<li role="presentation"><a href="point_links.shtm"
									class='btn btn-xs' data-toggle="tooltip" data-placement="top"
									title='<fmt:message key="menu.point_links.tooltip"/>'><img
										src="images/link.png" /></a></li>
								<li role="presentation"><a href="scripting.shtm"
									class='btn btn-xs' data-toggle="tooltip" data-placement="top"
									title='<fmt:message key="menu.scripting.tooltip"/>'><img
										src="images/script_gear.png" /></a></li>
								<span class="btn-separator"></span>
							</c:if>

							<li role="presentation"><a href="users.shtm"
								data-toggle="tooltip" data-placement="top"
								title='<fmt:message key="menu.users.tooltip"/>'><img
									src="images/user.png" /></a></li>
							<c:if test="${sessionUser.admin}">
								<li role="presentation"><a href="usersProfiles.shtm"
									class='btn btn-xs' data-toggle="tooltip" data-placement="top"
									title='<fmt:message key="menu.users_profiles.tooltip"/>'><img
										src="images/user_ds.png" /></a></li>
								<li role="presentation" class="active"><a
									href="pointHierarchySLTS" class='btn btn-xs'
									data-toggle="tooltip" data-placement="top"
									title='<fmt:message key="menu.point_hierarchy.tooltip"/>'><img
										src="images/folder_brick.png" /></a></li>
								<li role="presentation"><a href="mailing_lists.shtm"
									class='btn btn-xs' data-toggle="tooltip" data-placement="top"
									title='<fmt:message key="menu.mailing_lists.tooltip"/>'><img
										src="images/book.png" /></a></li>
								<li role="presentation"><a href="publishers.shtm"
									class='btn btn-xs' data-toggle="tooltip" data-placement="top"
									title='<fmt:message key="menu.publishers.tooltip"/>'><img
										src="images/transmit.png" /></a></li>
								<li role="presentation"><a href="maintenance_events.shtm"
									class='btn btn-xs' data-toggle="tooltip" data-placement="top"
									title='<fmt:message key="menu.maintenance_events.tooltip"/>'><img
										src="images/hammer.png" /></a></li>
								<li role="presentation"><a href="system_settings.shtm"
									class='btn btn-xs' data-toggle="tooltip" data-placement="top"
									title='<fmt:message key="menu.system_setings.tooltip"/>'><img
										src="images/application_form.png" /></a></li>
								<li role="presentation"><a href="emport.shtm"
									class='btn btn-xs' data-toggle="tooltip" data-placement="top"
									title='<fmt:message key="menu.import_export.tooltip"/>'><img
										src="images/script_code.png" /></a></li>
								<li role="presentation"><a href="sql.shtm"
									class='btn btn-xs' data-toggle="tooltip" data-placement="top"
									title='<fmt:message key="menu.sql.tooltip"/>'><img
										src="images/script.png" /></a></li>
								<span class="btn-separator"></span>
							</c:if>

							<li role="presentation"><a href="logout.htm"
								class='btn btn-xs' data-toggle="tooltip" data-placement="top"
								title='<fmt:message key="menu.logout.tooltip"/>'><img
									src="images/control_stop_blue.png"></a></li>
							<li role="presentation"><a href="help.shtm"
								class='btn btn-xs' data-toggle="tooltip" data-placement="top"
								title='<fmt:message key="menu.help.tooltip"/>'><img
									src="images/help.png"></a></li>
						</c:if>
						<c:if test="${empty sessionUser}">
							<li role="presentation"><a href="login.htm"
								class='btn btn-xs' data-toggle="tooltip" data-placement="top"
								title='<fmt:message key="menu.login.tooltip"/>'><img
									src="images/control_play_blue.png"></a></li>
						</c:if>
						<div class="btn-group pull-right menu">
							<button class="btn dropdown-toggle btn-xs" data-toggle="dropdown"
								data-toggle="tooltip" data-placement="top"
								title='<fmt:message key="menu.change_language.tooltip"/>'>
								<img src="images/world.png" alt=""> <span
									class="caret" />
							</button>
							<ul class="dropdown-menu dropdown-menu-right">
								<li><a onclick="setLocale('de')">Deutsch</a></li>
								<li><a onclick="setLocale('ru')">Руссиан</a></li>
								<li><a onclick="setLocale('fi')">Suomi</a></li>
								<li><a onclick="setLocale('pt')">Português</a></li>
								<li><a onclick="setLocale('en')">English</a></li>
								<li><a onclick="setLocale('lu')">Luxembourgeois</a></li>
								<li><a onclick="setLocale('fr')">Francais</a></li>
								<li><a onclick="setLocale('zh')">中文</a></li>
								<li><a onclick="setLocale('nl')">Nederlands</a></li>
								<li><a onclick="setLocale('es')">Español</a></li>
								<li><a onclick="setLocale('pl')">Polski</a></li>
							</ul>
						</div>
						<div class="btn-group pull-right menu">
							<c:if test="${!empty sessionUser}">
								<a href="" class="btn btn-xs"><span><fmt:message
											key="header.user" />: <b>${sessionUser.username}</b>
									</mark></span></a>

								<!-- TODO REST MiscDwr.toggleUserMuted(setUserMuted)
            				<img id="userMutedImg" class="ptr" onclick="MiscDwr.toggleUserMuted(setUserMuted)" onmouseover="hideLayer('localeEdit')" border="0" src="images/sound_none.png" title="Mute" alt="Mute" style="display: inline;">
            			-->
								<!-- TODO goHomeUrl()
            				<img src="images/house.png" alt="" title="Go to my default page" class="ptr" onclick="goHomeUrl()" onmouseover="hideLayer('localeEdit')" border="0">
            			-->
								<!-- TODO setHomeUrl()
            				<img src="images/house_link.png" alt="" title="Make this my default page" class="ptr" onclick="setHomeUrl()" onmouseover="hideLayer('localeEdit')" border="0">
            			-->
							</c:if>
						</div>
					</ul>
				</nav>
			</div>
		</div>

		<div class="row">
			<div id="pointHierarchy" class="col-md-12">
				<c:if test="${!empty sessionUser}">
				    <!--
					<div class="panel panel-default">
						<div class="panel-heading">
							<h3 class="panel-title">
								Search
							</h3>
						</div>
						<div class="panel-body hidden-print">
						  <div class="col-sm-6">

							<input name="query" id="query" autocorrect="off"
								autocomplete="off" placeholder="Enter Search Phrase" type="text">
							<button disabled="disabled" id="btnResetSearch"
								class="btn btn-default btn-sm">×</button>
							<button disabled="disabled" id="btnSearch"
								class="btn btn-default btn-sm">Search</button>

							<div class="collapse" id="searchResultPane">
								<table tabindex="0" id="searchResultTree"
									class="table table-striped table-hover table-condensed table-bordered fancytree-container fancytree-ext-table ">
									<colgroup>
										<col width="*">
										   <col width="100px">
										   <col width="100px">
										<col width="100px">
									</colgroup>
									<thead>
										<tr>
										   <th>Name</th>
										      <th>Actions</th>
										      <th>Path</th>
										   <th>Folder/Point</th>
										</tr>
									</thead>
									<tbody>

									</tbody>

								</table>
							</div>
						   </div>
						   <div class=".col-sm-6">
						   </div>
						</div>
					</div> -->

					<div class="panel panel-default">
						<div class="panel-heading help">
							<b><fmt:message key="pointHierarchySLTS.pointHierarchy" /></b>
						</div>
						<div class="menu panel-heading help">
							<div class="btn-group">
								<button id="addNode" class="btn btn-success"
									data-toggle="tooltip" data-placement="top"
									title='<fmt:message key="menu.point_hierarchy.add.tooltip"/>'>
									<span class="glyphicon glyphicon-plus"></span>
								</button>
								<button id="editNode" class="btn btn-warning"
									data-toggle="tooltip" data-placement="top"
									title='<fmt:message key="menu.point_hierarchy.edit.tooltip"/>'>
									<span class="glyphicon glyphicon-pencil"></span>
								</button>
								<button id="deleteNode" class="btn btn-danger"
									data-toggle="tooltip" data-placement="top"
									title='<fmt:message key="menu.point_hierarchy.delete.tooltip"/>'>
									<span class="glyphicon glyphicon-minus"></span>
								</button>
								<button id="reloadNode" class="btn btn-primary"
									data-toggle="tooltip" data-placement="top"
									title='<fmt:message key="menu.point_hierarchy.refresh.tooltip"/>'>
									<span class="glyphicon glyphicon-refresh"></span>
								</button>
								<button id="infoNode" class="btn btn-primary"
									data-toggle="tooltip" data-placement="top"
									title='<fmt:message key="menu.point_hierarchy.info.tooltip"/>'>
									<span class="glyphicon glyphicon-info-sign"></span>
								</button>
							</div>

							<div class="btn-group">
								<div id="hierarchy-import-export" >

                                    <button onclick="collapseImportExport()" id="Export_Import" class="btn" data-toggle="collapse" href="#collapseImport" role="button" aria-expanded="false" aria-controls="collapseImport">
                                        <span class="glyphicon glyphicon-export"></span> <span class="glyphicon glyphicon-import"></span>
                                    </button>

                            	</div>
							</div>

						</div>
						<div id="tree"
							class=" panel-body fancytree-colorize-hover fancytree-fade-expander pre-scrollable" style="max-height: 65vh"></div>
					</div>

				</c:if>
			</div>
			<div class="collapse" id="collapseImport">
			 <div class="btn-group">
             	<button onclick="closeImportExport()" id="close-export-import" class="btn btn-default"
             	    data-toggle="tooltip" data-placement="top"
             			title='close export/import'>
             			<span class="glyphicon glyphicon-resize-small"></span>
             	</button>
             </div>
                <div id=export-import-ph></div>
            </div>
		</div>
		<table width="100%" cellspacing="0" cellpadding="0" border="0">
			<tr>
				<td colspan="2">&nbsp;</td>
			</tr>
			<tr>
				<td colspan="2" class="footer" align="center">&copy;2012-2020 Scada-LTS All rights reserved. <fmt:message
						key="footer.rightsReserved" /></td>
			</tr>
		</table>
	</div>
</body>

<script src="resources/node_modules/jquery/dist/jquery.min.js"></script>
<script src="resources/node_modules/jquery-ui-dist/jquery-ui.min.js"></script>
<script	src="resources/node_modules/jquery.fancytree/dist/jquery.fancytree-all.min.js"></script>
<script	src="resources/node_modules/bootstrap/dist/js/bootstrap.min.js"></script>
<script	src="resources/node_modules/bootstrap3-dialog/dist/js/bootstrap-dialog.min.js"></script>
<script src="resources/node_modules/stompjs/lib/stomp.js"></script>
<script src="resources/sockjs-0.3.4.js"></script>

<!-- <script src="resources/node_modules/axios/dist/axios.min.js"></script>
<script src="resources/node_modules/vue/dist/vue.min.js"></script>
<script src="resources/node_modules/vue-axios/dist/vue-axios.min.js"></script>-->

<!--<script src="resources/vue-mixins/shared/mixins-export-import.js"></script>
<script src="resources/vue-components/export-import/export-import.js"></script>-->

<script src="resources/node_modules/vue-jsoneditor/dist/lib/vjsoneditor.min.js"></script>
<script src="resources/node_modules/vue-jsoneditor/dist/lib/vjsoneditor.min.css"></script>


<%@ include file="/WEB-INF/jsp/include/vue/vue-app.js.jsp"%>



<script>
"use strict";

var header = { init: "1" };
header.onLoad = function() {
        //header.evtVisualizer = new ImageFader($("__header__alarmLevelDiv"), 75, .2);
};

 var headers = {
		 login: 'admin',
		 passcode: 'passcode',
		 client_id: '564389'
 } ;

var stompClient = null;

var connectCallback = function(frame) {
    	console.log('Connected: ' + frame);
    	stompClient.subscribe('/topic/alarmLevel', function(message) {
        	console.log("message[/topic/alarmLevel]:" + message.body);
    	});
    	stompClient.subscribe("/ws/alarmLevel/register", function(message) {
    		console.log("message[/ws/alarmLevel/register]:" + message.body);
    		stompClient.subscribe("/topic/alarmLevel/"+message.body, function(message) {
    			var response = JSON.parse(message.body);
    			var alarmLevel = parseInt(response.alarmlevel);
    			console.log("response.alarmLevel: "+response.alarmlevel);
   		        if (alarmLevel > 0) {
   		            document.getElementById("__header__alarmLevelText").innerHTML = response.alarmlevel;
   		            setAlarmLevelImg(alarmLevel, "__header__alarmLevelImg");
   		            setAlarmLevelText(alarmLevel, "__header__alarmLevelText");
   		            document.getElementById("__header__alarmLevelDiv").style.visibility = "visible";
   		        	document.getElementById("__header__alarmLevelImg").style.visibility = "visible";
   		        }
   		        else {
   		            document.getElementById("__header__alarmLevelText").innerHTML = "";
   		        	document.getElementById("__header__alarmLevelImg").style.visibility = "hidden";
   		            document.getElementById("__header__alarmLevelDiv").style.visibility =  "hidden";
   		        }
    		})
    		stompClient.send("/ws/alarmLevel", {priority: 1}, "Hello, Spring STOMP - gimme my alarmLevel");
    	} );
    	stompClient.send("/ws/alarmLevel", {priority: 9}, "Hello, Spring STOMP");

    	stompClient.subscribe("/ws/listusers", function(message) {
    		console.log("message[/ws/listusers]:" + message.body);
    	} );

    	stompClient.subscribe("/ws/session", function(message) {
    		console.log("message[/ws/session]:" + message.body);
    	} );

};


function setAlarmLevelImg(alarmLevel, imgNode) {
    if (alarmLevel == 0)
        updateImg(imgNode, "images/flag_green.png", "undef", false, "none");
    else if (alarmLevel == 1)
        updateImg(imgNode, "images/flag_blue.png", "undef", true, "visisble");
    else if (alarmLevel == 2)
        updateImg(imgNode, "images/flag_yellow.png", "undef", true, "visisble");
    else if (alarmLevel == 3)
        updateImg(imgNode, "images/flag_orange.png", "undef", true, "visisble");
    else if (alarmLevel == 4)
        updateImg(imgNode, "images/flag_red.png", "undef", true, "visisble");
    else
        updateImg(imgNode, "(unknown)", "(unknown)", true, "visisble");
}

function setAlarmLevelText(alarmLevel, textNode) {
    textNode = document.getElementById(textNode);
    if (alarmLevel == 0)
        textNode.innerHTML = "";
    else if (alarmLevel == 1)
        textNode.innerHTML = "info";
    else if (alarmLevel == 2)
        textNode.innerHTML = "urgent";
    else if (alarmLevel == 3)
        textNode.innerHTML = "critical";
    else if (alarmLevel == 4)
        textNode.innerHTML = "lifeSafety";
    else
        textNode.innerHTML = "Unknown: "+ alarmLevel;
}

function updateImg(imgNode, src, text, visible, styleType) {
    imgNode = document.getElementById(imgNode);
    //if (visible) {
        imgNode.style.display = styleType;
        if (src)
            imgNode.src = src;
        if (text) {
            imgNode.title = text;
            imgNode.alt = text;
        }
    //}
    //else
    //    imgNode.style.display = 'none';
}

var errorCallback = function(error) {
	alert("Connect error:" + error);
}

function connect(url) {
    var socket = new SockJS(url);
    stompClient = Stomp.over(socket);
    stompClient.heartbeat.outgoing = 20000;
    stompClient.heartbeat.incoming = 0;
    stompClient.connect(headers,  connectCallback, errorCallback);
}

function disconnect() {
    if(stompClient != null) {
    	console.log("Disconnecting...");
        stompClient.disconnect(function(){
        	console.log("Disconnected");
        	alert("Disconnected");
        	stompClient = null;
        });
    }
}


function OnListUserSessions() {
	stompClient.subscribe("/ws/listusers", function(message) {
		console.log("message[/ws/listusers]:\n" + message.body);
	} );
}

function OnListSessionsAttributes() {
	stompClient.subscribe("/ws/session", function(message) {
		console.log("message[/ws/session]:\n" + message.body);
	} );
}

function OnListWebsocketStats() {
	stompClient.send("/ws/websocketStats", function(message) {
		console.log("message[/ws/websocketStats]:\n" + message.body);
	} );
}



var messages = {
  move: "<fmt:message key="pointHierarchySLTS.move"/>",
  folderNotMove: "<fmt:message key="pointHierarchySLTS.folderNotMove"/>",
  close: "<fmt:message key="pointHierarchySLTS.close"/>",
  yes: "<fmt:message key="pointHierarchySLTS.yes"/>",
  folder: "<fmt:message key="pointHierarchySLTS.folder"/>",
  folderChange: "<fmt:message key="pointHierarchySLTS.folderChange"/>",
  confirmDelete: "<fmt:message key="pointHierarchySLTS.confirmDelete"/>",
  folderRemoved: "<fmt:message key="pointHierarchySLTS.folderRemoved"/>",
  key: "<fmt:message key="pointHierarchySLTS.key"/>",
  title: "<fmt:message key="pointHierarchySLTS.title"/>",
  msg: "<fmt:message key="pointHierarchySLTS.msg"/>",
  pleaseSelectElement: "<fmt:message key="pointHierarchySLTS.pleaseSelectElement"/>",
  pleaseSelectElementFolder: "<fmt:message key="pointHierarchySLTS.pleaseSelectElementFolder"/>",
  weOnlyEditFolder: "<fmt:message key="pointHierarchySLTS.weOnlyEditFolder"/>",
  editFolder: "<fmt:message key="pointHierarchySLTS.editFolder"/>",
  titleEdit: "<fmt:message key="pointHierarchySLTS.titleEdit"/>",
  oldTitle: "<fmt:message key="pointHierarchySLTS.oldTitle"/>",
  newTitle: "<fmt:message key="pointHierarchySLTS.newTitle"/>",
  folderNotEdit: "<fmt:message key="pointHierarchySLTS.folderNotEdit"/>",
  errorThrown: "<fmt:message key="pointHierarchySLTS.errorThrown"/>",
  move: "<fmt:message key="pointHierarchySLTS.move"/>",
  keyParent: "<fmt:message key="pointHierarchySLTS.keyParent"/>",
  parent: "<fmt:message key="pointHierarchySLTS.parent"/>",
  folderNotRemove: "<fmt:message key="pointHierarchySLTS.folderNotRemove"/>",
  removeFolder: "<fmt:message key="pointHierarchySLTS.removeFolder"/>",
  warning: "<fmt:message key="pointHierarchySLTS.warning"/>",
  pleaseSelectElement: "<fmt:message key="pointHierarchySLTS.pleaseSelectElement"/>",
  info: "<fmt:message key="pointHierarchySLTS.info"/>",
  isFolder: "<fmt:message key="pointHierarchySLTS.isFolder"/>",
  dataSource: "<fmt:message key="pointHierarchySLTS.dataSource"/>",
  xid: "<fmt:message key="pointHierarchySLTS.xid"/>",
  type: "<fmt:message key="pointHierarchySLTS.type"/>",
  changeOfLanguageFailed: "<fmt:message key="pointHierarchySLTS.changeOfLanguageFailed"/>",
  warningDontRemoveDataPointInRoot: "<fmt:message key="pointHierarchySLTS.warningDontRemoveDataPointInRoot"/>",
  moveDataPointToRoot: "<fmt:message key="pointHierarchySLTS.moveDataPointToRoot"/>",
  areYouSureToMoveElement: "<fmt:message key="pointHierarchySLTS.areYouSureToMoveElement"/>",
  movedElement: "<fmt:message key="pointHierarchySLTS.movedElement"/>"
};
	var glyph_opts = {
    	    map: {
    	      doc: "glyphicon glyphicon-file",
    	      docOpen: "glyphicon glyphicon-file",
    	      checkbox: "glyphicon glyphicon-unchecked",
    	      checkboxSelected: "glyphicon glyphicon-check",
    	      checkboxUnknown: "glyphicon glyphicon-share",
    	      dragHelper: "glyphicon glyphicon-play",
    	      dropMarker: "glyphicon glyphicon-arrow-right",
    	      error: "glyphicon glyphicon-warning-sign",
    	      expanderClosed: "glyphicon glyphicon-plus-sign",
              expanderLazy: "glyphicon glyphicon-plus-sign",
              expanderOpen: "glyphicon glyphicon-minus-sign",
    	      folder: "glyphicon glyphicon-folder-close",
    	      folderOpen: "glyphicon glyphicon-folder-open",
    	      loading: "glyphicon glyphicon-refresh glyphicon-spin"
    	    }
    	  };
	var nodeActivate;
	var nodeDragAndDrop;
	var newNode;

	var pathArray = location.href.split( '/' );
    var protocol = pathArray[0];
    var host = pathArray[2];
    var port = '<c:out value="${appPort}"/>';
    var appScada = '<c:out value="${appName}"/>';
    var url = protocol + '//' + host;
    var myLocation;
    if (!myLocation) {
 	   myLocation = location.protocol + "//" + location.host + "" + appScada + "/";
    }


    function onloadHandler() {
    	// connecting to server websocket endpoint...
       connect(myLocation + '/ws/alarmLevel');
    }

    function onunloadHandler() {
   	   disconnect();
   	}


	/* function selectTree(key, folder) {
    	console.log("selectTree:"+key+" folder:"+folder);
    	 //var tree = $("#tree").fancytree("getTree");
         //var d = tree.toDict(true);
         //alert(JSON.stringify(d));
    	$.ajax({
            type: "GET",
        	dataType: "json",
        	url:myLocation+"/pointHierarchy/paths/"+key+"/"+folder,
        	success: function(msg){
        		var path="/";
        		if (msg.length>0) {
        			console.log(msg);
        			var node=undefined;
        			for (var i=msg.length-1;i>=0;i--) {
        				console.log(key);
        		    	console.log(folder);
        		    	console.log(msg[i]);
        		    	if (node==undefined) {
        		    		node = $("#tree").fancytree("getTree").activateKey(""+msg[i].key);
        		    	} else {
        		    		node.activateKey(""+msg[i].key)
        		    	}
        		    	if (node.isFolder) {
        		    		node.toggleExpanded();
        		    	}
        		    }
        		}
        	},
        	error: function(XMLHttpRequest, textStatus, errorThrown) {
        	  console.log(textStatus);
        	}
        });
    };*/

    var pageGlobal=1;
    var pageStart=0;


    function pages(page) {
    	console.log(page);
    	pageGlobal=page;
    	$("#btnSearch").click();
    };

    function collapseImportExport() {

        if ( $( "div#pointHierarchy" ).hasClass('col-md-8') ) {
          $( "div#pointHierarchy" ).attr("class", "col-md-12");
        } else {

          $( "div#pointHierarchy" ).attr("class", "col-md-8");
        }

    };

    function closeImportExport() {
        $( "#collapseImport" ).attr("class", "collapse");
        $( "div#pointHierarchy" ).attr("class", "col-md-12");
    }

    function reload() {
       var tree = $("#tree").fancytree("getTree");
       tree.reload();
    }

    function refreshCache() {
        $.ajax({
                type: "POST",
            	dataType: "json",
            	url:myLocation+"api/pointHierarchy/cacheRefresh/",
            	success: function(msg){
            		reload();
            	},
            	error: function(XMLHttpRequest, textStatus, errorThrown) {
            	  console.log(textStatus);
            	}
         });
    }

    $(function () {
    	$('[data-toggle="tooltip"]').tooltip();
    	var getParentId = function(node) {
    		if (node != undefined) {
      		  var parentId=0;
      		  try {
      			  parentId = parseInt(node.parent.key);
      			  if (isNaN(parentId)) {
      				  parentId=0;
      			  }
      		  } catch (e) {
      		  }
      		  return parentId;
    		} else {
    			return undefined;
    		}
    	}

    	var toMove ={};
    	$("#tree").fancytree({
    	      extensions: ["dnd","glyph"],
    	      debugLevel: 0,
    	      checkbox: false,
    	      autoScroll: false,
    	      autoActivate: true,
    	      activeVisible: false,
    	      clickFolderMode: 4,
    	      focusOnSelect: true,
    	      generateIds: false,
    	      keyboard: true,
    	      tabindex: -1,
    	      titlesTabbable: false,
  	          activate: function(event, data) {
  	            nodeActivate = data.node;
  	          },
    	      dnd: {
    	    	autoExpandMS: 400,
    	        focusOnClick: true,    // gb disable becouse when expand (not want change focus)
  	            preventVoidMoves: true, // Prevent dropping nodes 'before self', etc.
  	            preventRecursiveMoves: true, // Prevent dropping nodes on own descendants
    	        dragStart: function(node, data) {
      			    nodeDragAndDrop = data.node;
    	        	return true;
    	        },
    	        dragEnter: function(node, data) {
    	        	if( data.node.isFolder() ) {
        			  return true;
    	    	    } else {
    	    	      return false;
    	    	    }
    	        },
    	        dragDrop: function(node, data) {
    	        	BootstrapDialog.show({
    	 		       title: messages.move +':'+ nodeDragAndDrop.title,
    	 		       message: function(dialog) {
    	 		    	 var newNode = data.node;
    	 		    	 toMove.key = nodeDragAndDrop.key;
    	 		    	 toMove.oldParentId = getParentId(nodeDragAndDrop);
    	 		    	 toMove.newParentId = newNode.key;
					     var $content = $('<div><h3 id="title">'+messages.folder+':</h3>'+
					    		 '<ul><li>'+messages.key+':'+nodeDragAndDrop.key+'</li><li>'+messages.title+':<b>'+nodeDragAndDrop.title+'</b></li><li>'+ messages.keyParent + ':'+nodeDragAndDrop.parent.key+'</li><li>'+messages.parent+':<b>'+nodeDragAndDrop.parent.title+'</b></li></ul>'+
					    		 '<ul><li>'+messages.key+':'+newNode.key+'</li><li>'+messages.title+':<b>'+newNode.title+'</b></li><li>'+messages.keyParent+':'+newNode.parent.key+'</li><li>'+messages.parent+':<b>'+newNode.parent.title+'</b></li></ul>'
					     );

    	 		         dialog.setType(BootstrapDialog.TYPE_WARNING);
    	 		         return $content;
    	 		       },
    	 		       buttons: [{
    	 		         id: 'btn-Yes',
    	 		         label: messages.yes,
    	 		         cssClass: 'btn-warning',
    	 		         action: function(dialog) {
    	 		           dialog.getButton('btn-Close').disable();
    	 		           var $button = this;
    	 		           $button.disable();
    	 		           $button.spin();
    	 		           dialog.setClosable(false);
                           $.ajax({
    	 			            type: "POST",
    	 			        	dataType: "json",
    	 			        	url:myLocation+'/pointHierarchy/move/'+toMove.key+'/'+toMove.oldParentId+'/'+toMove.newParentId+'/'+nodeDragAndDrop.isFolder(),
    	 			        	success: function(msg){
    	 			        	  $button.hide();
    	 			 		      $button.stopSpin();
    	 			 		      dialog.setClosable(true);
    	 			 		      dialog.getButton('btn-Close').enable();
    	 			 		      dialog.close();
    	 			 		      reload();
    	 			        	},
    	 			        	  error: function(XMLHttpRequest, textStatus, errorThrown) {
    	 			        	    dialog.getModalBody().html('<div><h3>'+messages.folderNotMove+'</h3><p>'+pointHierarchySLTS.errorThrown+':'+errorThrown+'</p></div>');
    	 			        	    $button.hide();
    	 				 		    $button.stopSpin();
    	 				 		    dialog.setClosable(true);
    	 				 		    dialog.getButton('btn-Close').enable();
    	 			        	  }
    	 			       });
    	 		         }
    	 		       },
    	 		       {
    	 		         id: 'btn-Close',
    	 		         label:'Close',
    	 		         action: function(dialog) {
    	 		           dialog.close();
    	 		         }
    	 		       }]
    	 		     });

    	        }
    	      },
    	      glyph: glyph_opts,
    	      selectMode: 2,
    	      source: {
    	        url: myLocation+"/pointHierarchy/0",
    	        debugDelay: 0,
    	        cache: false},
    	      toggleEffect: { effect: "drop", options: {direction: "left"}, duration: 100 },
    	      wide: {
    	        iconWidth: "1em",     // Adjust this if @fancy-icon-width != "16px"
    	        iconSpacing: "0.5em", // Adjust this if @fancy-icon-spacing != "3px"
    	        levelOfs: "1.5em"     // Adjust this if ul padding != "16px"
    	      },
    	      icon: function(event, data){
    	         if( data.node.isFolder() ) {
    	           return "glyphicon glyphicon-book";
    	         }
    	      },
    	      lazyLoad: function(event, data) {
    	    	data.result = {
    	    	 url: myLocation+"/pointHierarchy/"+data.node.key,
    	         cache: false,
    	         debugDelay: 0
    	         };
    	      }
    	    });
    	$("button#addNode").click(()=>{
    		BootstrapDialog.show({
 		      title: messages.folder,
 		      onshown: function(dialog) {
 		    	 dialog.getModalBody().find('input').focus();
 		      },
 		      message: function(dialog) {
 		        var $content = $('<div>'+messages.title+': <input type="text" class="form-control" maxlength="100"></div>');
 		        dialog.setType(BootstrapDialog.TYPE_SUCCESS);
 		        return $content;
 		      },
 		      buttons: [{
 		        id: 'btn-Yes',
 		        label: messages.yes,
 		        cssClass: 'btn-su',
 		        action: function(dialog) {
 		          dialog.getButton('btn-Close').disable();
 		          var $button = this;
 		          $button.disable();
 		          $button.spin();
 		          dialog.setClosable(false);
 		          $.ajax({
		            type: "POST",
		        	dataType: "json",
		        	url:myLocation+"/pointHierarchy/new/0/"+dialog.getModalBody().find('input').val(),
		        	success: function(msg){
		        	  var titleNewNode = dialog.getModalBody().find('input').val();
		        	  dialog.getModalBody().html('<div><h3>'+messages.folder+':</h3><ul><li>'+messages.key+':<b>'+msg+'</b></li><li>'+messages.title+':<b>'+titleNewNode+'</b></li></ul></div>');
		        	  $button.hide();
		 		      $button.stopSpin();
		 		      dialog.setClosable(true);
		 		      dialog.getButton('btn-Close').enable();
		 		       dialog.close();
		 		       reload();
		        	  },
		        	  error: function(XMLHttpRequest, textStatus, errorThrown) {
		        	    dialog.getModalBody().html('<div><h3>'+messages.folderNotAdd+'</h3><p>'+ messages.errorThrown +':'+errorThrown+'</p></div>');
		        	    $button.hide();
			 		    $button.stopSpin();
			 		    dialog.setClosable(true);
			 		    dialog.getButton('btn-Close').enable();
		        	  }
		        	});
 		        }
 		      },
 		      {
 		         id: 'btn-Close',
 		         label: messages.close,
 		         action: function(dialog) {
 		           dialog.close();
 		         }
 		      }]
 		    });
    	});
    	$("button#deleteNode").click(()=>{
    		if (nodeActivate != undefined) {
    			if ( (getParentId(nodeActivate)==0) && (nodeActivate.isFolder()==false) ) {
    				BootstrapDialog.show({
                        type: BootstrapDialog.TYPE_WARNING,
                        title: messages.warning,
                        message: messages.warningDontRemoveDataPointInRoot,
                        buttons: [{
                            label: messages.close,
           		           action: function(dialog) {
           		             dialog.close();
           		           }
                        }]
                    });
    				return;
    			} else {
    				if ( (nodeActivate != undefined) && (nodeActivate.isFolder()) ) {
		 		      BootstrapDialog.show({
				       title: messages.removeFolder +':'+nodeActivate.title,
				       message: function(dialog) {
				         var $content = $('<div><h3>'+ messages.confirmDelete+'</h3></div>');
				         dialog.setType(BootstrapDialog.TYPE_DANGER);
				         return $content;
				       },
				       buttons: [{
				         id: 'btn-Yes',
				         label: messages.yes,
				         cssClass: 'btn-danger',
				         action: function(dialog) {
				           dialog.getButton('btn-Close').disable();
				           var $button = this;
				           $button.disable();
				           $button.spin();
				           dialog.setClosable(false);
				           $.ajax({
				        	   type: "POST",
				        	   dataType: "json",
				        	   url:myLocation+"/pointHierarchy/del/"+getParentId(nodeActivate)+"/"+nodeActivate.key+"/"+nodeActivate.isFolder(),
				        	   success: function(msg){
				        		   dialog.getModalBody().html('<div><h3>'+messages.folderRemoved+':</h3><ul><li>'+messages.key+':<b>'+nodeActivate.key+'</b></li><li>'+messages.title+':<b>'+nodeActivate.title+'</b></li><li>'+messages.msg+':'+msg+'</li></ul></div>');
						           $button.hide();
						           $button.stopSpin();
						           dialog.setClosable(true);
						           dialog.getButton('btn-Close').enable();
						           dialog.close();
						           reload();
				        	   },
				        	   error: function(XMLHttpRequest, textStatus, errorThrown) {
				        		   dialog.getModalBody().html('<div><h3>'+messages.folderNotRemove+'</h3><p>'+messages.errorThrown+':'+errorThrown+'</p></div>');
					        	   $button.hide();
						 		   $button.stopSpin();
						 		   dialog.setClosable(true);
						 		   dialog.getButton('btn-Close').enable();
				        	   }
				        	});
				         }
				       },
				       {
				         id: 'btn-Close',
				         label:messages.close,
				         action: function(dialog) {
				           dialog.close();
				         }
				       }]
				     });
    				} else {
    					// is not folder
    					BootstrapDialog.show({
    					       title: messages.moveDataPointToRoot +':'+nodeActivate.title,
    					       message: function(dialog) {
    					         var $content = $('<div><h3>'+ messages.areYouSureToMoveElement+'</h3></div>');
    					         dialog.setType(BootstrapDialog.TYPE_DANGER);
    					         return $content;
    					       },
    					       buttons: [{
    					         id: 'btn-Yes',
    					         label: messages.yes,
    					         cssClass: 'btn-danger',
    					         action: function(dialog) {
    					           dialog.getButton('btn-Close').disable();
    					           var $button = this;
    					           $button.disable();
    					           $button.spin();
    					           dialog.setClosable(false);
    					           $.ajax({
    					        	   type: "POST",
    					        	   dataType: "json",
    					        	   url:myLocation+"/pointHierarchy/del/"+getParentId(nodeActivate)+"/"+nodeActivate.key+"/"+nodeActivate.isFolder(),
    					        	   success: function(msg){
    					        		   dialog.getModalBody().html('<div><h3>'+messages.movedElement+':</h3><ul><li>'+messages.key+':<b>'+nodeActivate.key+'</b></li><li>'+messages.title+':<b>'+nodeActivate.title+'</b></li><li>'+messages.msg+':'+msg+'</li></ul></div>');
    							           $button.hide();
    							           $button.stopSpin();
    							           dialog.setClosable(true);
    							           dialog.getButton('btn-Close').enable();
    							           dialog.close();
    							           reload();
    					        	   },
    					        	   error: function(XMLHttpRequest, textStatus, errorThrown) {
    					        		   dialog.getModalBody().html('<div><h3>'+messages.folderNotRemove+'</h3><p>'+messages.errorThrown+':'+errorThrown+'</p></div>');
    						        	   $button.hide();
    							 		   $button.stopSpin();
    							 		   dialog.setClosable(true);
    							 		   dialog.getButton('btn-Close').enable();
    					        	   }
    					        	});
    					         }
    					       },
    					       {
    					         id: 'btn-Close',
    					         label:messages.close,
    					         action: function(dialog) {
    					           dialog.close();
    					         }
    					       }]
    					     });
    				}
    			}
    	   } else {
    		   BootstrapDialog.show({
                   type: BootstrapDialog.TYPE_WARNING,
                   title: messages.warning,
                   message: messages.pleaseSelectElement,
                   buttons: [{
                       label: messages.close,
      		           action: function(dialog) {
      		             dialog.close();
      		           }
                   }]
               });
    	   }
		});
    	$("button#editNode").click(()=>{
    		if ( (nodeActivate != undefined) && (!nodeActivate.isFolder())) {
    			BootstrapDialog.show({
                    type: BootstrapDialog.TYPE_WARNING,
                    title: messages.warning,
                    message: messages.weOnlyEditFolder,
                    buttons: [{
                        label: messages.close,
       		            action: function(dialog) {
       		              dialog.close();
       		           }
                    }]
                });
    			return;
    		} else if ( (nodeActivate != undefined) && (nodeActivate.isFolder()) ) {
 		      BootstrapDialog.show({
		       title: messages.editFolder+':'+nodeActivate.title,
		       onshown: function(dialog) {
	 		    	 dialog.getModalBody().find('input').focus();
	 		   },
		       message: function(dialog) {
		          var $content = $('<div>'+messages.titleEdit+': <input type="text" class="form-control" value="'+nodeActivate.title+'" maxlength="100"><ul><li>'+messages.key+':'+nodeActivate.key+'</li></ul></div>');
		          dialog.setType(BootstrapDialog.TYPE_WARNING);
		          return $content;
		       },
		       buttons: [{
		         id: 'btn-Yes',
		         label: messages.yes,
		         cssClass: 'btn-warning',
		         action: function(dialog) {
		           dialog.getButton('btn-Close').disable();
		           var $button = this;
		           $button.disable();
		           $button.spin();
		           dialog.setClosable(false);
		           var newTitle = dialog.getModalBody().find('input').val();
		           $.ajax({
			            type: "POST",
			        	dataType: "json",
			        	url:myLocation+"/pointHierarchy/edit/"+getParentId(nodeActivate)+"/"+nodeActivate.key+"/"+newTitle,
			        	success: function(msg){
			        	  var titleNewNode = dialog.getModalBody().find('input').val();
			        	  dialog.getModalBody().html('<div><h3>'+messages.folderChange+':</h3><ul><li>'+messages.key+':<b>'+nodeActivate.key+'</b></li><li>'+messages.oldTitle+':<b>'+nodeActivate.title+'</b></li><li>'+messages.newTitle+':<b>'+newTitle+'</b></li></ul></div>');
			        	  $button.hide();
				          $button.stopSpin();
				          dialog.setClosable(true);
				          dialog.getButton('btn-Close').enable();
				          dialog.close();
				          reload();
			        	},
			        	error: function(XMLHttpRequest, textStatus, errorThrown) {
			        	    dialog.getModalBody().html('<div><h3>'+messages.folderNotEdit+'</h3><p>'+messages.errorThrown+':'+errorThrown+'</p></div>');
			        	    $button.hide();
				 		    $button.stopSpin();
				 		    dialog.setClosable(true);
				 		    dialog.getButton('btn-Close').enable();
			        	}
			       });
		         }
		       },
		       {
		         id: 'btn-Close',
		         label:messages.close,
		         action: function(dialog) {
		           dialog.close();
		         }
		       }]
		     });
    	   	} else {
    		   BootstrapDialog.show({
                   type: BootstrapDialog.TYPE_WARNING,
                   title: messages.warning,
                   message: messages.pleaseSelectElementFolder,
                   buttons: [{
                       label: messages.close,
      		           action: function(dialog) {
      		             dialog.close();
      		           }
                   }]
               });
    	   }
		});
    	$("button#reloadNode").click(()=>{

    		refreshCache();
    	});
    	$("button#infoNode").click(()=>{
    		if (nodeActivate != undefined) {
 		      BootstrapDialog.show({
		       title: messages.info,
		       message: function(dialog) {
		         var $content = "";
		         if (nodeActivate.isFolder()){
		           $content = $('<div><h3>'+nodeActivate.title+'</h3>'+
		        		 '<ul>'+
		        		 	'<li>'+messages.key+':'+nodeActivate.key+'</li>'+
		        		 	'<li>'+messages.title+':'+nodeActivate.title+'</li>'+
		        		 	'<li>'+messages.keyParent+':'+getParentId(nodeActivate)+'</li>'+
		        		 	'<li>'+messages.isFolder+':'+nodeActivate.isFolder()+'</li>'+
		        		 	'<li> Folder xid : ' + nodeActivate.data.xid + '</li>'+
		         		 '</ul></div>');
		         } else {
		        	   $content = $('<div><h3>'+nodeActivate.title+'</h3>'+
			        		 '<ul>'+
			        		 '<li>'+messages.key+':'+nodeActivate.key+'</li>'+
			        		 	'<li>'+messages.title+':'+nodeActivate.title+'</li>'+
			        		 	'<li>'+messages.keyParent+':'+getParentId(nodeActivate)+'</li>'+
			        		 	'<li>'+messages.isFolder+':'+nodeActivate.isFolder()+'</li>'+
			        		 	'<li> Folder xid : ' + nodeActivate.data.xid + '</li>'+
			        		 	'<li>'+messages.dataSource+':'+nodeActivate.data.pointHierarchyDataSource.name+''+
			        		 	'<ul>'+
			        		 		'<li>'+messages.key+':'+nodeActivate.data.pointHierarchyDataSource.id+'</li>'+
			        		 		'<li>'+messages.xid+':'+nodeActivate.data.pointHierarchyDataSource.xid+'</li>'+
			        		 		'<li>'+messages.type+':'+nodeActivate.data.pointHierarchyDataSource.dataSourceType+'</li>'+
			        		 	'</ul></li>'+
			         		 '</ul></div>');
		         }
		         dialog.setType(BootstrapDialog.TYPE_INFO);
		         return $content;
		       },
		       buttons: [
		       {
		         id: 'btn-Close',
		         label:messages.close,
		         action: function(dialog) {
		           dialog.close();
		         }
		       }]
		     });
    	   } else {
    		   BootstrapDialog.show({
                   type: BootstrapDialog.TYPE_WARNING,
                   title: messages.warning,
                   message: messages.pleaseSelectElement,
                   buttons: [{
                       label: messages.close,
      		           action: function(dialog) {
      		             dialog.close();
      		           }
                   }]
               });
    	   }
		});
    	var setLocale=function(locale) {
    		 $.ajax({
		            type: "POST",
		        	dataType: "json",
		        	url:myLocation+"/viewutil/"+locale,
		        	success: function(msg){
		        		location.reload();
		        	},
		        	error: function(XMLHttpRequest, textStatus, errorThrown) {
		        		BootstrapDialog.show({
		                    type: BootstrapDialog.TYPE_WARNING,
		                    title: messages.warning,
		                    message: messages.changeOfLanguageFailed,
		                    buttons: [{
		                       label: messages.close,
		       		           action: function(dialog) {
		       		             dialog.close();
		       		           }
		                    }]
		                })
		        	}
    		});
    	};

	    // search

	    var queryGlobal="";


        function updateControls() {
        	var queryGlobal = $.trim($("input[name=query]").val());
        	$("#btnResetSearch")
        		.attr("disabled", queryGlobal.length === 0);
        	$("#btnSearch")
        		.attr("disabled", queryGlobal.length < 2);
        };

        function createPagination() {
        	$('#searchResultTree > tbody').append('<tr><td colspan="2">'+
        			'<nav aria-label="Page navigation">'+
      	  				'<ul class="pagination" style="margin:0px;">'+
      	    				'<li>'+
      	      					'<a onclick="if (pageStart>0) { pageStart=(parseInt(pageStart)-parseInt(5)); pages(5); }" aria-label="Previous">'+
      	        					'<span aria-hidden="true">&laquo;</span>'+
      	      					'</a>'+
      	    				'</li>'+
      	    				'<li id="page'+(parseInt(pageStart)+parseInt(1))+'"><a onclick="pages('+(parseInt(pageStart)+parseInt(1))+')">'+(parseInt(pageStart)+parseInt(1))+'</a></li>'+
      	    				'<li id="page'+(parseInt(pageStart)+parseInt(2))+'"><a onclick="pages('+(parseInt(pageStart)+parseInt(2))+')">'+(parseInt(pageStart)+parseInt(2))+'</a></li>'+
      	    				'<li id="page'+(parseInt(pageStart)+parseInt(3))+'"><a onclick="pages('+(parseInt(pageStart)+parseInt(3))+')">'+(parseInt(pageStart)+parseInt(3))+'</a></li>'+
      	    				'<li id="page'+(parseInt(pageStart)+parseInt(4))+'"><a onclick="pages('+(parseInt(pageStart)+parseInt(4))+')">'+(parseInt(pageStart)+parseInt(4))+'</a></li>'+
      	    				'<li id="page'+(parseInt(pageStart)+parseInt(5))+'"><a onclick="pages('+(parseInt(pageStart)+parseInt(5))+')">'+(parseInt(pageStart)+parseInt(5))+'</a></li>'+
      	    				'<li>'+
      	      					'<a onclick="pageStart=(parseInt(pageStart)+parseInt(5));pages(pageStart+1);" aria-label="Next">'+
      	        					'<span aria-hidden="true">&raquo;</span>'+
      	      					'</a>'+
      	    				'</li>'+
      	  				'</ul>'+
      				'</nav>'+
      			'</td></tr>');
        	$("li[id^=page]").removeClass("active");
    		$('#page'+pageGlobal).addClass("active");
        }

        function setElement(element, index, array) {
        	$('#searchResultTree > tbody > tr').remove();
        	for( var i=0; i<array.length;i++) {
        		var folderOrPoint="";
        		if (array[i].folder) {
        			folderOrPoint="<span class='fancytree-custom-icon glyphicon glyphicon-book'></span>";
        		} else {
        			folderOrPoint="<span class='fancytree-icon glyphicon glyphicon-file'></span>";
        		}

        		$('#searchResultTree > tbody').append('<tr>'+
        				'<td>'+array[i].title+'</td>'+
        				//'<td><button onclick="selectTree('+array[i].key+','+array[i].folder+')" class="btn btn-warning btn-xs">Select</button></td>'+
        				//'<td id="path_'+array[i].key+'_'+array[i].folder+'"></td>'+
        				'<td>'+folderOrPoint+'</td></tr>');
        	}
        };



        function search(query, page) {
            console.log("SEARCH");
        	queryGlobal=$.trim(query);
        	console.log(queryGlobal);
        	console.log(page);
        	$.ajax({
	            type: "GET",
	        	dataType: "json",
	        	url:myLocation+"/pointHierarchy/find/"+queryGlobal+"/"+page,
	        	success: function(msg){

	        	  	if(msg !== undefined ) {
	        	  		$("#searchResultTree").addClass("busy");
	        	  		$('#searchResultTree > tbody > tr').remove();
	        	  		msg.forEach(setElement);
	        	  		createPagination();
	        	  		$("#searchResultPane").collapse("show");
	        	  	} else {
	        	  		$('#searchResultTree > tbody > tr').remove();
	        	  		$("li[id^=page]").removeClass("active");
	        	  		createPagination();
	        	  	}
	        	},
	        	error: function(XMLHttpRequest, textStatus, errorThrown) {
	        		$('#searchResultTree > tbody > tr').remove();
                	$('#searchResultTree > tbody').append('<tr><td>empty</td><td></td><td>empty</td></tr>');
	        	  	console.log(textStatus);
	        	}
	        });
        	// Store the source options for optional paging
        	$("#searchResultTree").addClass("busy");
        };



	    $("input[name=query]").keyup(function(e){

	    	queryGlobal = $.trim($(this).val());
	    	var lastQuery = $(this).data("lastQuery");

	    	if(e && e.which === $.ui.keyCode.ESCAPE || queryGlobal === ""){
	    		$("#btnResetSearch").click();
	    		console.log("btnResetSearch");
	    		pageGlobal=1;
	    		return;
	    	}
	    	if(e && e.which === $.ui.keyCode.ENTER && queryGlobal.length >= 2){
	    		pageGlobal=1;
	    		$("#btnSearch").click();
	    		console.log("btnSearch.click");
	    		return;
	    	}
	    	if( queryGlobal === lastQuery || queryGlobal.length < 2) {
	    		console.log("Ignored query '" + queryGlobal + "'");
	    		return;
	    	}
	    	pageGlobal=1;
	    	$(this).data("lastQuery", queryGlobal);

	    	$("#btnSearch").click();

	    	$("#btnResetSearch").attr("disabled", queryGlobal.length === 0);
	    	$("#btnSearch").attr("disabled", queryGlobal.length < 2);

	    }).focus();

    	$("#btnResetSearch").click(function(e){
    		$("#searchResultPane").collapse("hide");
    		$("input[name=query]").val("");
    		//searchResultTree.clear();
    		updateControls();
    	});

    	$("#btnSearch").click(function(event){
    		console.log(pageGlobal);
    		search(	$("input[name=query]").val(), pageGlobal );
    	}).attr("disabled", true);

    });
    $('.jsoneditor-menu').prop('hidden', true);
    </script>

</html>