$(function() {
	document.title = 'Scheduling';
	var grid;

	
	
	 $('#periodTab').tabs();
	 
	 // $('#cashOutCurrentPeriod').tabs();
	 var schedule;
	 
	
     var portfolioId = null;
     var currentDate = null;
    for ( var i in getURLVariables()) {
		if (i == "portfolioId") {
		    portfolioId = getURLVariables()[i];
		}
		if (i == "currentDate") {		
			currentDate = new Date(getURLVariables()[i]);
		}
	}
    
    if (currentDate == null) {
    	currentDate = new Date();
    }
    
    $.widget("ui.pcntspinner", $.ui.spinner, {
    	_format : function(value) {
    	    return value + '%';
    	},

    	_parse : function(value) {
    	    return parseFloat(value);
    	}
     });

    
    $("#retainagePercent").spinner({
    	min : 0,
    	max : 100,
    	step : 1
        });
    
    
    $("#periodShift").spinner({
    	min : 0,
    	max : 365,
    	step : 1
        });



	getPeriodBoundries(rpcClient , currentDate , portfolioId );
	 
	 $("#previousPeriod").button({
			icons : {
			    primary : "ui-icon-triangle-1-w"
			},
			text : true
		    }).click(
			    function() {
		    	var theDate = $("#currentStart").data("tag");
				var day = 1000*60*60*24;
			    theDate.setTime(theDate.getTime() - day);
			    var url = ""  + window.location;
			    var currentDateLocation = url.indexOf("currentDate");
			    if (currentDateLocation == -1) {
			    	url += "&currentDate=" + theDate.toJSON();
			    } else {
			    	url = url.substring(0 , currentDateLocation ) + "currentDate=" + theDate.toJSON();
			    }
			    window.location = url;			    
			});
	 
	 $("#nextPeriod").button({
			icons : {
				secondary : "ui-icon-triangle-1-e"
			},
			text : true
		    }).click(
			    function() {
			    var theDate = $("#currentEnd").data("tag");
			    var day = 1000*60*60*24;
			    theDate.setTime(theDate.getTime() + day);
			    var url = ""  + window.location;
			    var currentDateLocation = url.indexOf("currentDate");
			    if (currentDateLocation == -1) {
			    	url += "&currentDate=" + theDate.toJSON();
			    } else {
			    	url = url.substring(0 , currentDateLocation ) + "currentDate=" + theDate.toJSON();
			    }
			    window.location = url;
			});
	 
	 $("#solveForPeriod").button({
		 icons : {
				secondary : "ui-icon-calculator"
			},
			text : true
		 
	 }).click(function() {
		 
		  
		 
		 window.location.href  = "solve.jsp?portfolioId=" + portfolioId 
		 			+ "&from=" + $("#currentStart").data("tag").toJSON() 
		 			+ "&to="   + $("#currentEnd").data("tag").toJSON()
		 			+ "&next="   + $("#nextEnd").data("tag").toJSON();
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

function getPeriodBoundries(rpcClient, currentDate, portfolioId) {
	var fmt = new DateFmt("%w %d-%n-%y");
	rpcClient.portfolioService.getSchedulePeriod( function(result , exception) {
	 if (result.result == 0) {
		 schedule = result.data;
		
		 var currentStart = new Date(schedule.current.dateFrom.time);
		 var formattedDate = fmt.format(currentStart);
		 $("#currentStart").val(formattedDate);
		 $("#currentStart").data("tag" , currentStart);

		 
		
		 var currentEnd = new Date(schedule.current.dateTo.time);
		 formattedDate = fmt.format(currentEnd);
		 $("#currentEnd").val(formattedDate);
		 $("#currentEnd").data("tag" , currentEnd);
		 
		 var nextStart = new Date(schedule.next.dateFrom.time);
		 formattedDate = fmt.format(nextStart);
		 $("#nextStart").val(formattedDate);
		 $("#nextStart").data("tag" , nextStart);
		 
		 var nextEnd = new Date(schedule.next.dateTo.time);
		 formattedDate = fmt.format(nextEnd);
		 $("#nextEnd").val(formattedDate);
		 $("#nextEnd").data("tag" , nextEnd);
		 
		 rpcClient.portfolioService.getCashoutCurrentPeriod( function(result , exception) {
			 if (result.result == 0) {
				 var data = result.data.list;
				 if (data.length > 0) {
						 var pCachOutCurPeriodGridData = [];
						 var totalCashout = 0;
						 var totalOpenBalance = 0;
						 var totalFinanceCost = 0;
						 var totalPayment = 0;
						 for (var i = 0; i < data.length ; i++) {
		
							 pCachOutCurPeriodGridData[i] = {
									 "project" :  "<a id= '" + data[i].projectId + "' href='projectDetails.jsp?projectId="
										+ data[i].projectId + "' target='_blank' tabindex='0'>" + data[i].projectCode
										+ "</a>"  ,
									 "taskCost" : data[i].taskCost,
									 "overhead": data[i].overhead,
									 "cashout" : data[i].cashout,
									// "financeCost" : data[i].financeCost
							 };
							 
							 totalCashout +=  data[i].cashout;
							 totalOpenBalance += data[i].openingBalance;
							// totalFinanceCost +=  data[i].financeCost;
							 totalPayment += data[i].projectPayment;
						 }
					 
						 var pCashoutColumns = [ { id : "project", name : "Project",  field : "project",  minWidth : 120 , formatter : formatter} ,
							   	                  { id : "taskCost", name : "Task Cost",  field : "taskCost",  minWidth : 120 , formatter : Slick.Formatters.Currency} ,
							   	                  { id : "overhead", name : "Overhead",  field : "overhead",  minWidth : 120 , formatter : Slick.Formatters.Currency},
							   	                  { id : "cashout", name : "Cash Out",  field : "cashout",  minWidth : 120 , formatter : Slick.Formatters.Currency},
							   	                 /* { id : "financeCost", name : "Finance Cost",  field : "financeCost",  minWidth : 120 , formatter : Slick.Formatters.Currency}*/];
		
					   	 grid = new Slick.Grid("#cashOutCurrentPeriodGrid", pCachOutCurPeriodGridData , pCashoutColumns, {
					   		    editable : true,
					   		    enableAddRow : true,
					   		    enableCellNavigation : true,
					   		    enableColumnReorder : true
					   		});
					   	 
					   
				   	 
					   	 rpcClient.financeService.findFinanceByDate( function(result , exception) {
					   		
					   		 if (result.result == 0) {
					   			 var currentFinancingLimit = result.data;
					   			
							   	 rpcClient.portfolioService.getExtraCachCurrentPeriod( function(result , exception) {
							   		if (result.result == 0) { 
								   		var pExtraCachColumns = [ { id : "finance", name : "Finance",  field : "finance",  minWidth : 120 , formatter : Slick.Formatters.Currency} ,
												   	                  { id : "balance", name : "Balance",  field : "balance",  minWidth : 120 , formatter : Slick.Formatters.Currency} ,
												   	                  { id : "payment", name : "Payment",  field : "payment",  minWidth : 120 , formatter : Slick.Formatters.Currency} ,
												   	                  { id : "cashout", name : "Cash Out",  field : "cashout",  minWidth : 120 , formatter : Slick.Formatters.Currency},
												   	                 /* { id : "financeCost", name : "Finance Cost",  field : "financeCost",  minWidth : 120 , formatter : Slick.Formatters.Currency},*/
												   	                  { id : "extraCash", name : "Extra Cash",  field : "extraCash",  minWidth : 120 , formatter : Slick.Formatters.Currency}];
										var extraPaymentCurrentPeriod = result.data;
										var pExtraCachData = [];
								   		pExtraCachData[0] = {
								   				"finance" :	currentFinancingLimit,
								   				"balance" : totalOpenBalance,
								   				"payment" : totalPayment ,
								   				"cashout" : totalCashout,
								   				//"financeCost" : totalFinanceCost ,
								   				"extraCash" : currentFinancingLimit + totalOpenBalance -  totalCashout /*+ totalFinanceCost*/ + extraPaymentCurrentPeriod
								   		};
				
								   	 grid = new Slick.Grid("#extraCacheCurrentPeriodGrid", pExtraCachData , pExtraCachColumns, {
								   		    editable : true,
								   		    enableAddRow : true,
								   		    enableCellNavigation : true,
								   		    enableColumnReorder : true
								   		});
						
							   		} else {
							   			alert(result.message);
							   		}
							   		 
							   	 } , portfolioId , currentStart, currentEnd);
					   		 } else {
					   			 alert(result.message);
					   		 }
					   	 } , portfolioId , currentStart);
					 } else {
						 alert("There are no project defined in this portfolio!");
						 $("#solveForPeriod").attr('disabled','disabled');
					 }
					   	 
				 } else {
					 alert(result.message);
				 }
				 
				 
			 }, portfolioId ,currentStart, currentEnd);
		
	 }  else { 
		 alert(result.message)
	 }
	} , currentDate  , portfolioId );
}

function formatter(row, cell, value, columnDef, dataContext) {
    return value;
}


function isEmpty(str) {
    return (!str || 0 === str.length);
}

function isBlank(str) {
    return (!str || /^\s*$/.test(str));
}