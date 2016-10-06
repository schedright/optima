<script>
  var currentPage = 3;
</script>
<%@ include file="header.html"%>

<div class="mainContainer">
	<div id="main" class="main-body">

		<div id="projectTabs" >

			<div id="loader-overlay"></div>

			<ul>
				<li><a href="#projInfo">Project Details</a></li>
				<li><a href="#projTasks">Project Tasks</a></li>
				<li><a href="#tasksBarChart">Tasks Bar chart</a></li>
			</ul>
			<div id="projInfo">
				<div class="project-info-container">
				<%@ include file="views/projectInfo.html"%>
				<%@ include file="views/projectCal.html"%>
				<%@ include file="views/projectFinanceNew.html"%>
				<%@ include file="views/projectFinancialPeriods.html"%>
				<div id="projectButtons" class="ui-dialog-buttonset">
					<button id="saveProjectBtn" style="width:200px">Save Project</button>
				</div>
				</div>
			</div>
			<div id="projTasks">
				<%@ include file="views/projectTasks.html"%>
			</div>
			<div id="tasksBarChart">
				<div id="tasksGantt" class="gantt tasksgantt" style="height: 100%;width:100%;"></div>
				<div id="projectButtons" class="ui-dialog-buttonset">
					<button id="resetSchedulingBtn" style="width:200px;margin-top:-35px">Reset Project Scheduling</button>
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