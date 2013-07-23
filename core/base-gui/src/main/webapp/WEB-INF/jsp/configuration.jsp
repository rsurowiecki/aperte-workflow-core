﻿<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div id="configuration" hidden="true">
	<div class="toggle-buttons">
	<div class="process-queue-name">
		<spring:message code="configuration.process.table.header" />
	</div>
	<fieldset data-role="controlgroup">
		<button id="button-processesTable-name" type="button" class="btn mobile-button" data-toggle="button" onClick="toggleColumn(this, 'process', 'name');" ><spring:message code="processes.button.hide.processname" /></button>
		<button id="button-processesTable-step" type="button" class="btn mobile-button" data-toggle="button" onClick="toggleColumn(this, 'process','step');" ><spring:message code="processes.button.hide.step" /></button>
		<button id="button-processesTable-code" type="button" class="btn mobile-button" data-toggle="button" onClick="toggleColumn(this, 'process','code');" ><spring:message code="processes.button.hide.processcode" /></button>
		<button id="button-processesTable-creator" type="button" class="btn mobile-button" data-toggle="button" onClick="toggleColumn(this, 'process','creator');" ><spring:message code="processes.button.hide.creator" /></button>
		<button id="button-processesTable-assignee" type="button" class="btn mobile-button" data-toggle="button" onClick="toggleColumn(this, 'process','assignee');" ><spring:message code="processes.button.hide.assignee" /></button>
		<button id="button-processesTable-creationDate" type="button" class="btn mobile-button" data-toggle="button" onClick="toggleColumn(this, 'process','creationDate');" ><spring:message code="processes.button.hide.creationdate" /></button>
		<button id="button-processesTable-deadline" type="button" class="btn mobile-button" data-toggle="button" onClick="toggleColumn(this, 'process','deadline');" ><spring:message code="processes.button.hide.deadline" /></button>
	</fieldset>
	</div>
</div>

<script type="text/javascript">
	function toggleColumn(button, viewName, columnName)
	{
		queueViewManager.toggleColumn(viewName, columnName);
	}	

</script>