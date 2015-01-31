<%@ include file="header.html"%>

 <div id="projectWrapper" >
	<div id="periodTab" >
	 <h3>Scheduling Period</h3>
	 <br>
	 <button id="previousPeriod">Previous Period</button>
	 <button id="nextPeriod">Next Period</button>
		
		
	<div class="form-section">
	 <label>Current</label> 
	 <div class="two-columns">
	 <label for="currentStart">Start</label>
		<Input type="text" name="currentStart"
				id="currentStart"
				readonly="readonly"/>
	 </div>
	 <div class="two-columns">
	   <label for="currentEnd">End</label>
		<Input type="text" name="currentEnd"
				id="currentEnd"
				 readonly="readonly" />
	 </div>
	 </div>
	  <br>
	  <br>
	  <div class="form-section">
		 <label>Next</label> 
		 <div class="two-columns">
		 <label for="nextStart">Start</label>
			<Input type="text" name="nextStart"
					id="nextStart"
					readonly="readonly" />
		 </div>
		 <div class="two-columns">
		   <label for="nextEnd">End</label>
			<Input type="text" name="nextEnd"
					id="nextEnd"
					readonly="readonly"/>
	    </div>
	    </div>
	    
	    
		 
			 <div class="form-section">
			 <h3>Cashout Current Period (Portfolio)</h3>
	<!-- 		 <div class="action-button print"> -->
	<!-- 			<input type="submit" id="printCachFlowBtn" Value="Print" /> -->
	<!-- 		 </div> -->
				<div class="form-section">
				<div id="cashOutCurrentPeriodGrid" style="width:1024px; height:150px;"></div>
				</div>
			 </div>
		 
		 <div class="form-section">
		 <h3>Extra Cash Current Period (Portfolio)</h3>
		 <br>
			<div class="form-section">
			<div id="extraCacheCurrentPeriodGrid" style="width:1024px; height:100px;"></div>
			</div>
		 </div>
		  
		 
		 <div class="form-section">
		 	<div class="two-columns-right">
		 		<button id="enterAdjustment">Enter Adjustments</button>
 				<button id="solveForPeriod">Initial Solution</button>
 			</div>
 		 </div>
		 
	   </div>

		
 	<div id="adjustmentsDialog" title="Adjustments">
 		<div class="form-section">
	 		<div class="two-columns">
			 	<label for="adjustmentProject">Project</label>
			 	<select name="adjustmentProject"
						id="adjustmentProject"
						class="custom-combobox-input ui-widget ui-widget-content ui-state-default ui-corner-all">
				</select>
				
				
		 	</div>
		 	<div class="two-columns">
		 		
		 	</div>
	 	
	 </div>
	 <div class="form-section">
	 <label>Payment Period</label> 
	 <div class="two-columns">
	 <label for="paymentStart">Start</label>
		<Input type="text" name="paymentStart"
				id="paymentStart" 
				/>
	 </div>
	 <div class="two-columns">
	   <label for="paymentEnd">End</label>
		<Input type="text" name="paymentEnd"
				id="paymentEnd"
				  />
	 </div>
	 </div>
	  <div class="form-section">
		  <div class="two-columns">
		   <label for="advancePaymentRepayment">Re-payment of Advance Payment</label>
			<Input type="text" name="advancePaymentRepayment"
					id="advancePaymentRepayment"
					 />
		 </div>
	  </div>
	   <div class="form-section">
		  <div class="two-columns">
		   <label for="reatainedPercentage">Retained Percentage</label>
			<Input type="text" name="reatainedPercentage"
					id="reatainedPercentage" readonly
					  />
		 </div>
		  <div class="form-section">
		  <div class="two-columns">
		   <label for="extraPayment">Extra Payment/Deduction</label>
			<Input type="text" name="extraPayment"
					id="extraPayment"
					  />
		 </div>
		 
		   <div class="form-section">
		 	<div class="two-columns-right">
		 		<button id="addPayment">Add</button>
 			</div>
 		 </div>
 		 
 		  <div class="form-section">
		 <h3>Summary of payments</h3>

			<div class="form-section">
			<div id="paymentSummaryGrid" style="width:600x; height:150px;"></div>
			</div>
		 </div>
		 
	  </div>
  		
	</div>		
	 
	 
	
	
 </div>
</div>
	
	  <script src="js/formutils.js"> </script> 
	  <script src="js/scheduling.js"> </script> <!--  all page specific js goes here -->
	  <script src="js/adjustments.js"> </script> <!--  all page specific js goes here -->
	  
	

<%@ include file="footer.html"%>