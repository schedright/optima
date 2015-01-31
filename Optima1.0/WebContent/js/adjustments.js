$(function() {

  
	
   // Once this page is loaded (or reload), reset everything
   var pPaymentSummaryGridData = [];
   localStorage.setItem('adjustmentsData', JSON.stringify(pPaymentSummaryGridData));
	
	
	var pPaymentSummaryGridCols  = [ 
    { id : "project", name : "Project",  field : "project",  minWidth : 120 } ,
		{ id : "payment", name : "Payment",  field : "payment",  minWidth : 80 , formatter : Slick.Formatters.Currency} ,
		{ id : "repayment", name : "Repayment",  field : "repayment",  minWidth : 80 , formatter : Slick.Formatters.Currency},
		{ id : "retained", name : "Retained (%)",  field : "retained",  minWidth : 80 },
		{ id : "extra", name : "Extra(+/-)",  field : "extra",  minWidth : 80 , formatter : Slick.Formatters.Currency},
		{ id : "netPayment", name : "Net Payment",  field : "netPayment",  minWidth : 80 , formatter : Slick.Formatters.Currency} 
	];

	
	 		 
	
	var pPaymentSummaryGrid = new Slick.Grid("#paymentSummaryGrid", pPaymentSummaryGridData, pPaymentSummaryGridCols , {
   		    editable : true,
   		    enableAddRow : true,
   		    enableCellNavigation : true,
   		    enableColumnReorder : true
   		});
	
	 var portfolioId = null;
     for ( var i in getURLVariables()) {
		if (i == "portfolioId") {
		    portfolioId = getURLVariables()[i];
		}
	}
     

     
	$("#enterAdjustment").button({
			icons : {
				secondary : "ui-icon-triangle-1-e"
			},
			text : true
		    }).click(
		    		
			    function() {
			    
			    	$( "#adjustmentsDialog" )
			    		.data("fromDate"  , $("#currentStart").data("tag"))
			    		.data("toDate"    , $("#currentEnd").data("tag"))
			    		.data("next"      , $("#nextEnd").data("tag")).dialog('open');
			});
			
 	$( "#adjustmentsDialog" ).dialog({
      resizable: false,
      autoOpen : false,
      width:800,
      modal: true,
      show : {
	    effect : "blind",
	    duration : 1000
 		},
 	  hide : {
	    effect : "fade",
	    duration : 1000
 		},
 		open : function() { 
 			$('#adjustmentProject').empty();
 		     rpcClient.portfolioService.getProjectsByPaymentDate(function(result , exception) {
 				if (result.result == 0) {
 					projectsList = result.data.list;
 					for (var i = 0; i < projectsList.length; i++) {
 					    $('#adjustmentProject').append($("<option" + (i==0?" selected " :"")+ "></option>").val(projectsList[i].projectId).text(projectsList[i].projectCode));
 					} 
 				
 				} else {
 					alert("Error" + result.message);
 				}
 				} , portfolioId, $(this).data("toDate") );
 		},
      buttons: {
        "Close": function() {
          $( this ).dialog( "close" );
        }
      }
    });
 	  $("#reatainedPercentage").pcntspinner({
 			min : 0,
 			max : 100,
 			step : 0.1
 		    });
 	$("#paymentStart").datepicker({
 		showOn : "button",
 		buttonImage : "images/calendar.png",
 		buttonImageOnly : true
 	    });
 	
 	$("#paymentEnd").datepicker({
 		showOn : "button",
 		buttonImage : "images/calendar.png",
 		buttonImageOnly : true
 	    });
 	
 	$("#addPayment").button({
		icons : {
			secondary : "ui-icon-plus"
		},
		text : true
	    }).click(
		    function() {
		    	var bValid = true;
		    	var advancePaymentRepayment = $("#advancePaymentRepayment").val();
		    	var paymentStart = $("#paymentStart").val();
		    	var paymentEnd = $("#paymentEnd").val();
		    	var paymentStartDate = null;
		    	var paymentEndDate = null;
		    	var reatainedPercentage = $("#reatainedPercentage").val().replace('%' ,'');
		    
		    	var extraPayment = $("#extraPayment").val();
		    	var projectId = $("#adjustmentProject option:selected").val();
		    	var projectName = $("#adjustmentProject option:selected").text();
		    	
		    	
		    	
		    	$("#advancePaymentRepayment").removeClass("ui-state-error");
		    	$("#paymentStart").removeClass("ui-state-error");
		    	$("#paymentEnd").removeClass("ui-state-error");
		    	$("#reatainedPercentage").removeClass("ui-state-error");
		    	$("#extraPayment").removeClass("ui-state-error");
		    	if (advancePaymentRepayment == null || advancePaymentRepayment.length == 0 ) {
		    		advancePaymentRepayment = 0;
		    	};
		    
		    	if (paymentStart == null || paymentStart.length == 0 ) {
		    		$("#paymentStart").addClass("ui-state-error");
		    	    bValid = false;
		    	} else {
		    		paymentStartDate = Date.parse(paymentStart);
		    	};
		    	
		    	if (paymentEnd == null || paymentEnd.length == 0 ) {
		    		$("#paymentEnd").addClass("ui-state-error");
		    	    bValid = false;
		    	} else {
		    		paymentEndDate = Date.parse(paymentEnd);
				};
				
				if (paymentStartDate >= paymentEndDate) {
					$("#paymentStart").addClass("ui-state-error");
					$("#paymentEnd").addClass("ui-state-error");
					bValid = false;
				}
		    	if ( !/^-?\d*\.?\d*$/.test(advancePaymentRepayment)) {
		    	    $("#advancePaymentRepayment").addClass("ui-state-error");
		    	    bValid = false;
		    	}
		    	
		    	if (reatainedPercentage == null || reatainedPercentage.length == 0 ) {
		    		reatainedPercentage = 0;
		    	};
		    	
		    	if (extraPayment == null || extraPayment.length == 0 ) {
		    		extraPayment = 0;
		    	};
		    	if ( !/^-?\d*\.?\d*$/.test(extraPayment)) {
		    	    $("#extraPayment").addClass("ui-state-error");
		    	    bValid = false;
		    	}
		    	
		    	if (bValid) {
		    		
		    		var payment  = rpcClient.portfolioService.getExtraCachNextPeriodByProjectNoAdjustments(projectId , $("#currentEnd").data("tag") , 
		    				$("#nextEnd").data("tag") ,  new Date($("#paymentStart").val()) , new Date($("#paymentEnd").val())).data; // TODO, load from server
		    		var netPayment = payment; 

		    		
		    		netPayment -= ((parseFloat(netPayment) * parseFloat(reatainedPercentage)) / 100.0);
		    		
		    		netPayment += parseFloat(extraPayment);

		    		netPayment -= parseFloat(advancePaymentRepayment);

		    		
		    		insertOrUpdate(projectId, projectName ,payment,advancePaymentRepayment ,reatainedPercentage,extraPayment ,netPayment, paymentStartDate , paymentEndDate);
		    		
		    	}
		    	pPaymentSummaryGrid.updateRowCount();
		    	pPaymentSummaryGrid.invalidate();
		        pPaymentSummaryGrid.render();
		        
		    	
		});
 	
 	 	
 	function insertOrUpdate(projectId, projectName ,payment,advancePaymentRepayment ,reatainedPercentage,extraPayment ,netPayment ,paymentStart , paymentEnd ) {
 		var found = false;
 		for (var i = 0; i < pPaymentSummaryGridData.length; i++) {
 			if (pPaymentSummaryGridData[i].project == projectName) {
 				pPaymentSummaryGridData[i].payment = payment;
 				pPaymentSummaryGridData[i].repayment = advancePaymentRepayment;
 				pPaymentSummaryGridData[i].retained = reatainedPercentage;
 				pPaymentSummaryGridData[i].extra = extraPayment;
 				pPaymentSummaryGridData[i].netPayment = netPayment;
 				pPaymentSummaryGridData[i].projectId = projectId;
 				pPaymentSummaryGridData[i].paymentStart = paymentStart;
 				pPaymentSummaryGridData[i].paymentEnd = paymentEnd;
 				found = true;
 			}
 		}
 		if (found == false) {
	 		pPaymentSummaryGridData.push(
	                {
	               	"project"    : projectName ,
	               	"payment"    : payment   ,  
	               	"repayment"  : advancePaymentRepayment ,
                    "retained"   : reatainedPercentage,
                    "extra"      : extraPayment,
                    "netPayment" : netPayment ,
                    "paymentStart" :paymentStart,
                    "paymentEnd" : paymentEnd,
                    "projectId" : projectId
	                });
 		} 
 		localStorage.setItem('adjustmentsData', JSON.stringify(pPaymentSummaryGridData));
 	}
 	
 	

 }); 

function formatter(row, cell, value, columnDef, dataContext) {
    return value;
}

