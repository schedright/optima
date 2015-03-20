$(function() {
	document.title = 'Scheduling';

	var portfolioId = null;
	for ( var i in getURLVariables()) {
		if (i == "portfolioId") {
			portfolioId = getURLVariables()[i];
		}
	}
	
	$.widget( "ui.pcntspinner", $.ui.spinner, {
	    _format: function(value) { return value + '%'; },

	    _parse: function(value) { return parseFloat(value); }
	});
	
	  $("#reatainedPercentage").pcntspinner({
			min : 0,
			max : 100,
			step : 0.1
		    });
	  
	var advancePaymentRepayment = $("#advancePaymentRepayment").val();
	var extraPayment = $("#extraPayment").val();
	var reatainedPercentage = $("#reatainedPercentage").val().replace('%' ,'');
	
	if (advancePaymentRepayment == null || advancePaymentRepayment.length == 0 ) {
		advancePaymentRepayment = 0;
	};
	if (reatainedPercentage == null || reatainedPercentage.length == 0 ) {
		reatainedPercentage = 0;
	};
	
	if (extraPayment == null || extraPayment.length == 0 ) {
		extraPayment = 0;
	};
	
    $("#findFinalSolBtn").button(
      		 {
      			 icons : {
      					secondary : "ui-icon-lightbulb"
      				},
      				text : true
      			 
      		 }
      	 ).click(function(){
      		alert("This page is trying to find the solution for Portfolio:" + portfolioId + "<br>" + 
      				"Advance Payment Repayment" + advancePaymentRepayment + "<br>" + 
      				"Reatained Percentage" + reatainedPercentage + "<br>" + 
      				"Extra Payment" + extraPayment);
      		
      		/*rpcClient.projectService.getPeriodSolution( function(result , exception) {
      			if (result.result == 0) {
      				var data = result.data.list;
      			} else {
      				 alert("Error generating solution: " + result.message);
      			} 
      		});*/
      	});
      		
	

});

function getURLVariables() {
	var getVars = [];
	var split = location.href.split('?')[1].split('&');
	if (split != null) {
		for ( var i in split) {
			var parts = split[i].split('=');
			getVars[parts[0]] = parts[1];
		}
	}

	return getVars;
}
