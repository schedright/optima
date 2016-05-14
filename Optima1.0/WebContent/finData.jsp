<script>
  var currentPage = 2;
</script>
<%@ include file="header.html"%>
<div class="mainContainer">
	<div id="main" style="width: 100%;" class="main-body">

		<div id="main" style="width: 100%; height: 100%; padding: 20px">

			<div id="FinPeriods"
				class="ui-tabs ui-widget ui-widget-content ui-corner-all"
				style="height: 40%">
				<label>Constraints</label>


				<div id="finances"
					class="ui-tabs ui-widget ui-widget-content ui-corner-all ui-center"
					style="width: 90%;">
					<div id="ulContainer" class="form-section">
						<div class="two-columns">
							<ul id="financeList" class="sortable" style="height: initial">

							</ul>
						</div>
						<div class="two-columns">
							<p>
								<button id="addFinanceBtn">Add Overdraft Constraint</button>
							</p>
							<p>
								<button id="deleteFinanceBtn">Delete Overdraft Constraint</button>
							</p>
						</div>

					</div>

				</div>
			</div>

			<div id="SolveDiv"
				class="ui-tabs ui-widget ui-widget-content ui-corner-all"
				style="height: 50%">
				<label>Projects Priority</label>
				
				<div class="ui-tabs ui-widget ui-widget-content ui-corner-all ui-center"
					style="width: 90%;">
					<div id="ulContainer" class="form-section">
						<div class="two-columns">
							<ul id="mainAllProjects" data-role="listview" data-inset="true"
							class="draggable"></ul>
						</div>
						<div class="two-columns">
							<p>
								<button id="findFinalSolBtn">Schedule</button>
							</p>
						</div>

					</div>

				</div>
				
				<div id="currentSolution" class="form-section" style="display: none">
					<div class="solutionContainer">
						<div id="schedResults" class="div-Table"></div>
					</div>
				</div>

				<div id="loading-indicator" style="display: none">
					<div id="loading-indicator-image">
						<img src="images/Sched-loaderLogo.png" /><img
							src="images/Sched-loader.gif" />
					</div>
				</div>

			</div>
			<div id="addFinanceDialog" title="Add Constraint">
				<p class="validateTips">All form fields are required.</p>

				<form id="addUserForm" autocomplete="off">
					<fieldset>
						<label for="financeAmount">Fund Amount</label> <input
							type="text" id="financeAmount" name="financeAmount" class="" />
						<label for="interestRate">Interest Rate</label> <input type="text"
							id="interestRate" name="interestRate" class="" /> <label
							for="financeDate">End Date</label> <input type="text"
							id="financeDate" name="financeDate" class="" />
					</fieldset>
				</form>
			</div>

			<script src="js/formutils.js"></script>
			<script src="js/finData.js"></script>
		</div>
	</div>
</div>
<%@ include file="footer.html"%>