<script>
  var currentPage = 7;
</script>
<%@ include file="header.html"%>

<div class="mainContainer">
	<div id="main" style="width: 100%;" class="main-body">
		<div id="planTabs" style="width: 100%;height:100%">
			<ul>
				<li><a href="#planDates">Plan Setup</a></li>
				<li><a href="#projectPaymentsTab">Contractors' Payments</a></li>
				<li><a href="#projectsBarChart">Projects Bar Chart</a></li>
			</ul>

			<div id="planDates">
				<div class="project-info-container">
				<div class="form-section grouped-section textOntpp">
					<div class="PlaceTextOnTop">
						<B>Project Calendar</B>
					</div>
					<div class="two-columns">

						<label for="pStartDateTxt">Plan Start Date</label> <input
							type="text" id="pStartDateTxt" class="datepicker"
							name="pStartDateTxt" />

					</div>

					<div class="two-columns right">

						<label for="pFinishDateTxt">Plan Finish Date</label> <input
							type="text" id="pFinishDateTxt" name="pFinishDateTxt"
							class="datepicker" />

					</div>
				</div>

				<div class="form-section grouped-section textOntpp">
					<div class="PlaceTextOnTop">
						<B>Project Calendar</B>
					</div>

					<div class="form-section divTasksDepends">
						<div class="two-columns">
							<label>All Projects</label>
							<ul id="allProjects" class="sortable">
	
							</ul>
						</div>
						<div id="listConnect"></div>
						<div class="two-columns">
							<label>Plan Projects</label>
							<ul id="includedProjects" class="sortable">
							</ul>
						</div>
	
					</div>
				</div>
								
				<div id="projectButtons" class="ui-dialog-buttonset">
					<button id="savePlanDatesBtn"
						style="padding: 0.4em 1em; color: #555555">Save/Refresh Plan Setup</button>
				</div>
				</div>
			</div>
			<div id="projectPaymentsTab">
				<div id="projectPayments" class="projectPayment"></div>
			</div>
			<div id="projectsBarChart">
				<div id="projectsGantt" class="gantt"></div>
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