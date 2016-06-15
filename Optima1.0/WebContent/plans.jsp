<script>
  var currentPage = 7;
</script>
<%@ include file="header.html"%>

<div class="mainContainer">
	<div id="main" style="width: 100%;"
		class="main-body">
	<div id="projectsGantt" class="gantt"></div>
	<div id="planTabs" style="width: 100%;">
		<ul>
			<li><a href="#planDates">Finance Period</a></li>
			<li><a href="#projectsList">Select Projects</a></li>
			<li><a href="#projectPaymentsTab">Contractors' Payments</a></li>
		</ul>

		<div id="planDates">
			<div class="two-columns">

				<label for="pStartDateTxt">Plan Start Date</label> <input
					type="text" id="pStartDateTxt" class="datepicker"
					name="pStartDateTxt" />

			</div>

			<div class="two-columns right">

				<label for="pFinishDateTxt">Plan Finish Date</label> <input
					type="text" id="pFinishDateTxt" name="pFinishDateTxt"
					class="datepicker" />
				<div style="padding-top: 15px">
					<button id="savePlanDatesBtn" style="padding:0.4em 1em;color: #555555">Save Plan Period</button>
				</div>

			</div>

		</div>
		<div id="projectsList" >
			<div id="divTasksDepends" class="form-section">
				<div class="two-columns">
					<label>All Projects</label>
					<ul id="allProjects" class="sortable">

					</ul>
				</div>
				<div id="listConnect"></div>
				<div class="two-columns">
					<label>Included Projects</label>
					<ul id="includedProjects" class="sortable">
					</ul>
					<button id="refreshPlanBtn" style="margin-top: 10px;padding:0.4em 1em;color: #555555">Refresh
						Plan</button>
				</div>

			</div>
		</div>
		<div id="projectPaymentsTab" >
			<div id="projectPayments" class="projectPayment"></div>
		</div>
	</div>
	</div>
</div>

<script src="js/formutils.js">
  
</script>

<script src="js/plans.js">
  
</script>

<script src="js/projgantt.js">
  
</script>

<%@ include file="footer.html"%>