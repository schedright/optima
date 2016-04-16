<script>
  var currentPage = 3;
</script>
<%@ include file="header.html"%>

<div class="mainContainer">
	<div id="main" style="width: 100%;" class="main-body">

		<div id="tasksGantt" class="gantt tasksgantt" style="height: 420px"></div>
		
		<div id="projectTabs">

			<div id="loader-overlay"></div>

			<ul>
				<li><a href="#projInfo">Project Information</a></li>
				<li><a href="#projCalendar">Project Calendar</a></li>
				<li><a href="#projTasks">Project Tasks</a></li>
				<li><a href="#projFinanceData">Project Financial Data</a></li>
				<li><a href="#financialPeriodsData">Financial Periods</a></li>
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


			<div class="form-section">
				<div id="projectButtons" class="ui-dialog-buttonset">
					<button id="resetSchedulingBtn">Reset Project Scheduling</button>
					<button id="saveProjectBtn">Save Project</button>
				</div>
			</div>

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