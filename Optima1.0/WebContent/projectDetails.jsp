<script>
  var currentPage = 3;
</script>
<%@ include file="header.html"%>

<div class="mainContainer">
	<div id="main" class="main-body">

		<div id="projectTabs" >

			<div id="loader-overlay"></div>

			<ul>
				<li><a href="#projInfo">Project Information</a></li>
				<li><a href="#projCalendar">Project Calendar</a></li>
				<li><a href="#projTasks">Project Tasks</a></li>
				<li><a href="#projFinanceData">Project Financial Data</a></li>
				<li><a href="#financialPeriodsData">Financial Periods</a></li>
				<li><a href="#tasksBarChart">Tasks Bar chart</a></li>
			</ul>
			<div id="projInfo">
				<%@ include file="views/projectInfo.html"%>

			</div>
			<div id="projCalendar">
				<%@ include file="views/projectCal.html"%>
			</div>
			<div id="projTasks">
				<%@ include file="views/projectTasks.html"%>
			</div>
			<div id="projFinanceData">
				<%@ include file="views/projectFinanceNew.html"%>
			</div>

			<div id="financialPeriodsData">
				<%@ include file="views/projectFinancialPeriods.html"%>
			</div>
			<div id="tasksBarChart">
				<div id="tasksGantt" class="gantt tasksgantt" style="height: 100%;width:100%"></div>
			</div>

		</div>
		<div id="projectButtons" class="ui-dialog-buttonset">
			<button id="resetSchedulingBtn" style="width:200px">Reset Project Scheduling</button>
			<button id="saveProjectBtn" style="width:200px">Save Project</button>
		</div>
	</div>
</div>
<script src="js/formutils.js">
  
</script>

<script src="js/gantt.js">
  
</script>

<script src="js/project.js">
  
</script>

<%@ include file="footer.html"%>