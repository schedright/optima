<script>var currentPage=2;</script>
<%@ include file="header.html"%>
<div class="mainContainer">
 <ul id="paymentMenu" class="contextMenu" style="display:none;position:absolute">
  	<li data="Delete">Delete</li>
  </ul>
  
  <ul id="financeMenu" class="contextMenu" style="display:none;position:absolute">
  	<li data="Delete">Delete</li>
  </ul>
  
    <ul id="extraPaymentMenu" class="contextMenu" style="display:none;position:absolute">
  	<li data="Delete">Delete</li>
  </ul>
  
<div id="main" style="width:100%;">

<div id="FinPeriods" class="ui-tabs ui-widget ui-widget-content ui-corner-all">

   <h3 class="ui-widget-header">Finances and Extra payments</h3>
 
   <div id="finances" class="ui-tabs ui-widget ui-widget-content ui-corner-all ui-center"  style="width:90%;">
   
   <div class="form-section">

					<div class="two-columns">
						<label for="financeAmount">Finance Amount</label> <input
							type="text" id="financeAmount" name="financeAmount" class="" />
					</div>
					<div class="two-columns right">
						<label for="financeDate">Finance Date</label> <input type="text"
							id="financeDate" name="financeDate" class="" />
					</div>
	</div>
		<div class="form-section">
	<div class="two-columns">
			<button id="addFinance">Add Finance</button>
	</div>
	</div>
	
		<table>
		<tr>	
		 <td valign="top" width="50%">
   		  <div id="financeGrid" style="width:1000px; height:80px;"></div>
   		</td>
   		</tr>
   		</table>
   		
   		
   		
	   <div class="form-section">
					<div class="two-columns">
						<label for="extraPaymentAmount">Extra Payment Amount</label> <input
							type="text" id="extraPaymentAmount" name="extraPaymentAmount"
							class="" />
					</div>
					<div class="two-columns right">
						<label for="extraPaymentDate">Extra Payment Date</label> <input
							type="text" id="extraPaymentDate" name="extraPaymentDate"
							class="" />
					</div>
				</div>
	
	<div class="form-section">
	<div class="two-columns">
			<button id="addExtraPayment">Add Extra Payment</button>
	</div>
	</div>
	
		<table>
		<tr>	
		 <td valign="top" width="50%">
   		  <div id="extraPaymentGrid" style="width:1000px; height:80px;"></div>
   		</td>
   		</tr>
   		</table>
   	</div>
   	
   


 </div>
 
	
  <script src="js/formutils.js"> </script> 
 <script src="js/finData.js"> </script> 
 
</div>
</div>
<%@ include file="footer.html"%>