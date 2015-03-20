<script>
	var currentPage = 4;
</script>
<%@ include file="headerNew.html"%>

<div style="width: 960px; margin-left: auto; margin-right: auto;">


	<div class="form-section">
		<div class="two-columns">
			<label for="advancePaymentRepayment">Re-payment of Advance Payment</label>
				<Input type="text" name="advancePaymentRepayment" id="advancePaymentRepayment" />
		</div>
	</div>
	<div class="form-section">
		<div class="two-columns">
			<label for="reatainedPercentage">Retained Percentage</label> <Input
				type="text" name="reatainedPercentage" id="reatainedPercentage" readonly />
		</div>
		<div class="form-section">
			<div class="two-columns">
				<label for="extraPayment">Extra Payment/Deduction</label> <Input
					type="text" name="extraPayment" id="extraPayment" />
			</div>



		</div>
		<div class="form-section">
			<div class="two-columns-right">
				<button id="findFinalSolBtn" class="btnRight">Solve</button>
			</div>
		</div>

	</div>


	<script src="js/formutils.js"></script>
	<script src="js/scheduleAutomated.js"></script>
</div>
<%@ include file="footer.html"%>