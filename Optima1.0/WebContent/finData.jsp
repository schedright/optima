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
  
  <h3 class="ui-widget-header">Payments</h3>
  <div >
  
 
   <div class="form-sction">
	  <div class="two-columns">
		<label for="projCodeSelect">Project Code</label> <select name="projCodeSelect"
				id="projCodeSelect"
				class="custom-combobox-input ui-widget ui-widget-content ui-state-default ui-corner-all">
		</select>
		</div>
	</div>
	
	<div class="form-section">
				<label for="paymentTypeRadio">Payment Type</label>
			<div id="paymentTypeRadio" >
				<input type="radio" id="advancePayment" name="paymentTypeRadio" />
				<label for="advancePayment">Advance Payment</label>
			    <input type="radio" id="interimPayment" name="paymentTypeRadio" checked="checked" />
			    <label for="interimPayment">Interim Payment</label>
			</div>
	</div>
	<div class="form-section">
		<div class="two-columns">
			<label for="advancePaymentAmount">Payment Amount</label>
			<input type="text" id="advancePaymentAmount" name="advancePaymentAmount" class=""/>
		</div>
		<div class="two-columns right">
			<label for="intremPaymentNumber">Interim Payment #</label>
			<input type="text" id="intremPaymentNumber" name="intremPaymentNumber" class=""/>
		</div>	
	</div>
	<div class="form-section">
		<div class="two-columns">
			<label for="paymentDate">Payment Date</label>
			<input type="text" id="paymentDate" name="paymentDate" class=""/>
		</div>
	</div>
	<div class="form-section">
		<div class="two-columns">
				<button id="addPayment">Add Payment</button>
		</div>
	</div>
	
		<table>
		<tr>	
		 <td valign="top" width="50%">
   		  <div id="paymentsGrid" style="width:1000px; height:200px;"></div>
   		</td>
   		</tr>
   		</table>
		
	</div>

  <h3 class="ui-widget-header">Payments Collection</h3>
	<div id="paymentCollection" >
			<div class="two-columns">
			 	<label for="collectionProject">Project</label>
			 	<select name="collectionProject"
						id="collectionProject"
						class="custom-combobox-input ui-widget ui-widget-content ui-state-default ui-corner-all">
				</select>
	 		</div>
	 		
	 		<div class="two-columns">
			 	<label for="paymentToCollect">Payment</label>
			 	<select name="paymentToCollect"
						id="paymentToCollect"
						class="custom-combobox-input ui-widget ui-widget-content ui-state-default ui-corner-all">
				</select>
	 		</div>
	 		
	 		<div class="two-columns">
				<label for="amountToCollect">Amount</label>
				<input type="text" id="amountToCollect" name="amountToCollect" class=""/>
			</div>	
			<div class="form-section">
				<div class="two-columns">
				<button id="collectPayment">Collect Payment</button>
				</div>
			</div>
	</div>
  
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