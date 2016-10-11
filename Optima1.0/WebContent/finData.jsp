<script>
  var currentPage = 2;
</script>
<%@ include file="header.html"%>
<div class="mainContainer">
	<div id="main" style="width: 100%;" class="main-body">

		<div id="main" style="width: 100%; height: 100%; padding: 10px;box-sizing: border-box;">


				<div id="finances"
					class="ui-tabs ui-widget ui-widget-content ui-corner-all ui-center"
					>
					<div style="min-height: 230px; padding-right: 0px"
						class="grouped-section textOntpp">
						<div class="PlaceTextOnTop">
							<B style="font-size: 14">Constraints</B>
						</div>

						<div id="ulContainer" class="form-section" style="width:96%">
								<ul id="financeList" class="sortable" style="height: initial;margin-top:5px;margin-bottom:5px">
								</ul>
						</div>
						<div class="rightButtons" style="padding-right:10px">
								<button id="addFinanceBtn">Add Overdraft Constraint</button>
								<button id="deleteFinanceBtn">Delete Overdraft
									Constraint</button>
						</div>
					</div>

				</div>


				<div
					class="ui-tabs ui-widget ui-widget-content ui-corner-all ui-center"
					>
										<div style="min-height: 230px; padding-right: 0px"
						class="grouped-section textOntpp">
						<div class="PlaceTextOnTop">
							<B style="font-size: 14">Projects Priority</B>
						</div>

						<div id="ulContainer" class="form-section" style="width:96%">
							<ul id="mainAllProjects" data-role="listview" data-inset="true" style="height: initial;margin-top:5px;margin-bottom:5px"
								class="draggable"></ul>
						</div>
						<div class="rightButtons" style="padding-right:10px">
							<button id="findFinalSolBtn">Schedule</button>
						</div>

					</div>

				</div>

				<div id="currentSolution" class="form-section" style="display: none;margin-top:-20px">
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

			<div id="addFinanceDialog" title="Add Constraint">
				<p class="validateTips">All form fields are required.</p>

				<form id="addUserForm" autocomplete="off">
					<fieldset>
						<label for="financeAmount">Fund Amount:</label> <input type="text"
							id="financeAmount" name="financeAmount" class=""
							style="width: 100%; margin-top: 3px; margin-bottom: 6px" /> <label
							for="interestRate">Interest Rate:</label> <input type="text"
							id="interestRate" name="interestRate" class=""
							style="width: 100%; margin-top: 3px; margin-bottom: 6px" /> <label
							for="financeDate" style="margin-bottom: -5px">End Date:</label> <input
							type="text" id="financeDate" name="financeDate" class=""
							style="width: 100%; margin-top: 3px; margin-bottom: 6px" />
					</fieldset>
				</form>
			</div>

			<script src="js/formutils.js"></script>
			<script src="js/finData.js"></script>
		</div>
	</div>
</div>
<%@ include file="footer.html"%>