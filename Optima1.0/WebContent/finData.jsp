<script>
  var currentPage = 2;
</script>
<%@ include file="header.html"%>
<div class="mainContainer">
	<div id="main" style="width: 100%;" class="main-body">

		<ul id="paymentMenu" class="contextMenu"
			style="display: none; position: absolute">
			<li data="Delete">Delete</li>
		</ul>

		<ul id="financeMenu" class="contextMenu"
			style="display: none; position: absolute">
			<li data="Delete">Delete</li>
		</ul>

		<div id="main" style="width: 100%; padding: 20px">

			<div id="FinPeriods"
				class="ui-tabs ui-widget ui-widget-content ui-corner-all">
				<label>Constraints</label>


				<div id="finances"
					class="ui-tabs ui-widget ui-widget-content ui-corner-all ui-center"
					style="width: 90%;">

					<!--div class="form-section">
						<div class="two-columns">
							<label for="financeAmount">Finance Amount</label> <input
								type="text" id="financeAmount" name="financeAmount" class="" />
						</div>
						<div class="two-columns right">
							<label for="financeDate">Finance Date</label> <input type="text"
								id="financeDate" name="financeDate" class="" />
						</div>
					</div-->
					<div id="ulContainer" class="form-section">
						<div class="two-columns">
							<ul id="financeList" class="sortable" style="height: initial">

							</ul>
						</div>
						<div class="two-columns">
							<p>
								<button id="addFinanceBtn">Add Finance</button>
							</p>
							<p>
								<button id="deleteFinanceBtn">Delete Finance</button>
							</p>
						</div>

					</div>

				</div>




			</div>

			<div id="addFinanceDialog" title="Add Finance">
				<p class="validateTips">All form fields are required.</p>

				<form id="addUserForm" autocomplete="off">
					<fieldset>
						<label for="financeAmount">Finance Amount</label> <input
							type="text" id="financeAmount" name="financeAmount" class="" />
						<label for="interestRate">Interest Rate</label> <input type="text"
							id="interestRate" name="interestRate" class="" /> <label
							for="financeDate">Finance Date</label> <input type="text"
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