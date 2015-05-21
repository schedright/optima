$(function() {
	document.title = 'Solution';
	 $('#solutionTab').tabs();
	  $( "#OutputFormat" ).buttonset();
	 
	var portfolioId = null;
	var fromDate = null;
	var toDate = null;
	var nextPeriodEndDate = null;
	var selectedProject = 0;
	var advanceRepayment = 0;
	var retainPercent = 0;
	var extraPayment = 0;
	var periodShift = 0;
	var solvedProjects = null;
	var solvedProjectsIds = null;
    for ( var i in getURLVariables()) {
		if (i == "portfolioId") {
		    portfolioId = getURLVariables()[i];
		}
		if (i == "from") {		
			fromDate = new Date(getURLVariables()[i]);
		}
		if (i == "to") {		
			toDate = new Date(getURLVariables()[i]);
		}
		if (i == "next") {
			nextPeriodEndDate = new Date(getURLVariables()[i]);			  
		}
		if (i == "selectedProject") {
			selectedProject =  getURLVariables()[i];	
		}
		
		if (i == "solvedProjects") {
			solvedProjects = getURLVariables()[i];
		}
		
		if (i == "solvedProjectsIds") {
			solvedProjectsIds = getURLVariables()[i];
		}
		
    }	
    
    $(function() {
        $( "#sortable" ).sortable();
        $( "#sortable" ).disableSelection();
      });
    
    var fmt2 = new DateFmt("%w %d-%n-%y");
    $("#start").text("Current Period Start Date: " + fmt2.format(fromDate));
    $("#end").text("Current Period End Date: " + fmt2.format(toDate));
    $("#next").text("Next Period End Date: " + fmt2.format(nextPeriodEndDate));
    var adjustmentInformation =  localStorage.getItem('adjustmentsData');
   
	if (adjustmentInformation == null) {
		adjustmentInformation = [];
	} else {
		adjustmentInformation = JSON.parse(adjustmentInformation);
	}

    
    $( "#dialog-form" ).dialog({
	      autoOpen: false,
	      height: 600,
	      width: 640,
	      modal: true,
	      
	      open : function() {
	    	  var fmt = new DateFmt("%w %d-%n-%y");
    		  var data = $(this).data("solution");
    		  var pEligTasksResultColumns = [ 
   	                  { id : "taskName", name : "Task Name",  field : "taskName",  minWidth : 120 , formatter : formatter} ,
   	                  { id : "taskDesc", name : "Task Description",  field : "taskDesc",  minWidth : 120 , formatter : formatter },
   	                  { id : "duration", name : "Duration(Calendar)",  field : "duration",  minWidth : 140 , formatter : formatter }  ,
   	                  { id : "taskDate", name : "Task Date",  field : "taskDate",  minWidth : 120 , formatter : formatter  }
   	             ];
				
				var pEligTasksResultData = [];
				if (data != null) {
					
					for (var i = 0; i < data.length ; i++ ) {
						var theDate = new Date(data[i].scheduledStartDate.time);
						pEligTasksResultData[i] = {
								
								"taskName" : data[i].taskName,
								"taskDesc" : data[i].taskDescription,
								"duration" : data[i].calenderDuration, 
								"taskDate" : fmt.format(theDate)
						}
						 
					 }
					 var pEligTasksResultGrid = new Slick.Grid("#solutionGrid", pEligTasksResultData , pEligTasksResultColumns, {
			   		    editable : true,
			   		    enableAddRow : true,
			   		    enableCellNavigation : true,
			   		    enableColumnReorder : true
			   		});
				} else {
					alert("Error calculating solution");
				}
    	  },
	      buttons: {
	    	  "Schedule Tasks" : function() {
	    		  var data = $(this).data("solution");
	    		  var currentProject = $(this).data("currentProject");
	    		  if (data.length != 0) {
	    			  rpcClient.projectService.commitSolution( function(result , exception) { 
		    			  if (result.result == 0) {
		    				  alert("Solution submitted successfully");
		    				  $( "#dialog-form" ).dialog( "close" );
		    				  location.reload(true);
		    			  } else {
		    				  alert("Error: " + result.message);
		    			  }
		    		  }, currentProject , data );  
	    		  } else {
	    			  alert("Nothing to schedule");
	    		  }
	    		  	    		  
	    	  },
	    	  "Close" : function() {
	    		  $( this ).dialog( "close" );
	    	  } 
	    	  
	    	  
	      }
	 });

  
    
    $("#generateSolution").button(
   		 {
   			 icons : {
   					secondary : "ui-icon-lightbulb"
   				},
   				text : true
   			 
   		 }
   	 ).click(function(){
   		$('#generateSolution').button("disable");
   			 rpcClient.projectService.getPeriodSolution( function(result , exception) {
   			$('#generateSolution').button("enable");
   			if (result.result == 0) {
   				var data = result.data.list;
   				$( "#dialog-form" ).data("solution" , data).data("currentProject" ,  $("#currentProject option:selected").val()).dialog("open");
   			 } else {
   				 alert("Error generating solution: " + result.message);
   			 }
   		 }, 
   		 $("#currentProject option:selected").val() , fromDate , toDate , nextPeriodEndDate , JSON.stringify(adjustmentInformation) , solvedProjectsIds ,  $("input[name=output]:checked").attr("id")); 
   	 });
	 var allProjectsResult = rpcClient.projectService.findAllByPortfolio(portfolioId);
	 if (allProjectsResult.result == 0) {
		 var projectsList = allProjectsResult.data.list;
		 for (var i = 0; i < projectsList.length; i++) {
		    $('#currentProject').append($("<option></option>").val(projectsList[i].projectId).text(projectsList[i].projectCode));
		    if (selectedProject == 0) {
		    	if (i == 0) {
		    		$("#currentProject option[value=" + projectsList[i].projectId + "]").attr('selected', 'selected');
		    	}
 		    } else if (projectsList[i].projectId == selectedProject) {
		    	 $("#currentProject option[value=" + projectsList[i].projectId + "]").attr('selected', 'selected');
		    } 
		 }
	 } else {
		 alert("Error: " + allProjectsResult.message);
	 }

	 
	 var pCachOutNextPeriodGridData = [];
	 
	 $("#currentProject").change(function() {
		 window.location.href  = "solve.jsp?portfolioId=" + portfolioId 
			+ "&from=" + fromDate.toJSON() 
			+ "&to="   + toDate.toJSON()
			+ "&next="   + nextPeriodEndDate.toJSON()
		    + "&selectedProject=" + $("#currentProject option:selected").val();
		 	
	 });
	 
	 
	 // load data for period, project and portfolio
	 
	 rpcClient.portfolioService.getCashoutCurrentPeriod( function(result , exception) {
		 
		 if (result.result == 0) {
			 var data = result.data.list;
			 if (data.length > 0) {
					 var pCachOutCurPeriodGridData = [];
					 var totalCashout = 0;
					 var totalOpenBalance = 0;
					 for (var i = 0; i < data.length ; i++) {
						 
						 pCachOutCurPeriodGridData[i] = {
								 "project" :  "<a id= '" + data[i].projectId + "' href='projectDetails.jsp?projectId="
									+ data[i].projectId + "' target='_blank' tabindex='0'>" + data[i].projectCode
									+ "</a>"  ,
								 "taskCost" : data[i].taskCost,
								 "overhead": data[i].overhead,
								 "cashout" : data[i].cashout,
								
						 };
						 
						 totalCashout +=  data[i].cashout;
						 totalOpenBalance += data[i].openingBalance;
					
					 }
					 
				 rpcClient.financeService.findFinanceByDate( function(result , exception) {
					 if (result.result == 0) {
			   			 var currentFinancingLimit = result.data;
			   			 rpcClient.portfolioService.getExtraCachCurrentPeriod( function(result , exception) {
			   				 if (result.result == 0) {
								 var extraPaymentCurrentPeriod = result.data;
								 var solvedProjectsCost = 0.0;
								 if (solvedProjectsIds != null && solvedProjectsIds.length > 0 ) {
									 var solvedProjectsIdsArray = solvedProjectsIds.split(",");
									for (var i = 0; i < solvedProjectsIdsArray.length ; i++ ) {
									
										var result2 = rpcClient.projectService.getSolutionCurrentPeriodCost( solvedProjectsIdsArray[i] , fromDate, toDate  );
										if (result2.result == 0) {
											solvedProjectsCost += parseFloat(result2.data);
										}
									} 
									 
								 }
								 
								 var totalAvailCash =  currentFinancingLimit + totalOpenBalance -  totalCashout + extraPaymentCurrentPeriod - solvedProjectsCost;
								 $("#extraCash").val("$" +  totalAvailCash.toFixed(2));
								 
								 var OtherProjectsCurrentPeriodCost = 0;
								 
								 rpcClient.projectService.getOtherProjectsCurrentPeriodCost(function(result , exception) {
									 if (result.result == 0) {
										 OtherProjectsCurrentPeriodCost = result.data;
									 } else {
											 	alert(result.message);
										 }
									 } , $("#currentProject option:selected").val(), fromDate, toDate);
								 
								 
								 rpcClient.projectService.getSolutionCurrentPeriodCost(function(result , exception) {
									 
									 if (result.result == 0) {
										 var pCurrentPeriodCostColumns = [ { id : "project", name : "Project",  field : "project",  minWidth : 120 , formatter : formatter} ,
											   	                  { id : "taskCost", name : "Task Cost",  field : "taskCost",  minWidth : 120 , formatter : Slick.Formatters.Currency}];
										 var pCurrentPeriodCostData = [];
										 var totalCurrentCost = result.data;
										 pCurrentPeriodCostData[0] = {
												"project" : $("#currentProject option:selected").text(),
												"taskCost" : totalCurrentCost
										 };
										 
										 pCurrentPeriodCostData[1] = {
													"project" : "Other projects",
													"taskCost" : OtherProjectsCurrentPeriodCost
											 };

										 var pCurrentPeriodCostGrid = new Slick.Grid("#costCurrentPeriodGrid", pCurrentPeriodCostData , pCurrentPeriodCostColumns, {
									   		    editable : true,
									   		    enableAddRow : true,
									   		    enableCellNavigation : true,
									   		    enableColumnReorder : true
									   		});
										 rpcClient.portfolioService.getCashoutCurrentPeriod(function(result , exception) {
											 
											 if (result.result == 0) {
												 var data = result.data.list;
												 if (data.length > 0) {
														 
														 var totalCashout = 0;
														 var totalOpenBalance = 0;
													     var cashoutCurrent = 0;
														 for (var i = 0; i < data.length ; i++) {
										
															 pCachOutNextPeriodGridData[i] = {
																	 "project" :  "<a id= '" + data[i].projectId + "' href='projectDetails.jsp?projectId="
																		+ data[i].projectId + "' target='_blank' tabindex='0'>" + data[i].projectCode
																		+ "</a>"  ,
																	 "taskCost" : data[i].taskCost,
																	 "overhead": data[i].overhead,
																	 "cashout" : data[i].cashout,
																	 "projectId" : data[i].projectId
																	
															 };
															 
															 if (data[i].projectId ==  $("#currentProject option:selected").val()
																	 || isSolvedProject(data[i].projectId , solvedProjectsIds)
																	 ) 
															 {
																 totalCashout +=  data[i].taskCost ;
															 }
															 totalCashout += data[i].overhead;
															 
															 rpcClient.portfolioService.getCashoutPreviousPeriod(function(result , exception) {
																 
																 if (result.result == 0) {
																	 var dataOpenBalance = result.data;
																	 totalOpenBalance = dataOpenBalance;
														
																 } else {
																	 	alert(result.message);
																 }
															 } , portfolioId , fromDate);
															 
															 rpcClient.portfolioService.getPortfolioLeftOverCost(function(result , exception) {
																 
																 if (result.result == 0) {
																	 cashoutCurrent = result.data;
																 } else {
																	 	alert(result.message);
																 }
															 } , portfolioId , fromDate, toDate);

															 
															 //alert("totalCashout: " +  totalCashout);
															 //alert("totalOpenBalance: " +  totalOpenBalance);
														 }
													 
														 var pCashoutColumns = [  { id : "project", name : "Project",  field : "project",  minWidth : 120 , formatter : formatter} ,
															   	                  { id : "taskCost", name : "Task Cost",  field : "taskCost",  minWidth : 120 , formatter : Slick.Formatters.Currency} ,
															   	                  { id : "overhead", name : "Overhead",  field : "overhead",  minWidth : 120 , formatter : Slick.Formatters.Currency},
															   	                  { id : "cashout", name : "Cash Out",  field : "cashout",  minWidth : 120 , formatter : Slick.Formatters.Currency}];
														 
														 var checkboxSelector = new Slick.CheckboxSelectColumn({
															 cssClass: "cell-checkbox"
														 });
														 
														 pCashoutColumns.push(checkboxSelector.getColumnDefinition());
														 
														 var pCashoutColumnsOptions =  {
														   		    editable : true,
														   		    enableCellNavigation : true,
														   		    asyncEditorLoading: false,
														   	        autoEdit: false
														   		};
														 
													   	var pCachOutCurPeriodGrid = new Slick.Grid("#cashOutNextPeriodGrid", pCachOutNextPeriodGridData , pCashoutColumns, pCashoutColumnsOptions);
													   	pCachOutCurPeriodGrid.setSelectionModel(new Slick.RowSelectionModel({selectActiveRow: false}));
													   	pCachOutCurPeriodGrid.registerPlugin(checkboxSelector);

													    var columnpicker = new Slick.Controls.ColumnPicker(pCashoutColumns, pCachOutCurPeriodGrid, pCashoutColumnsOptions);
												        if (solvedProjects != null && solvedProjects != "") {
															 var projectsArray = solvedProjects.split(",");
															 for (var i = 0 ; i < projectsArray.length; i++) {
																 projectsArray[i] = parseInt(projectsArray[i]);
															 }
															 if (projectsArray.length != 0) {
																 pCachOutCurPeriodGrid.setSelectedRows(projectsArray);
															 }
														 }
													    pCachOutCurPeriodGrid.onSelectedRowsChanged.subscribe(function (evt, args) {
													    	
													    	 var solvedProjectsIds = [];
													    	 for (var i = 0; i < args.rows.length; i++) {
													    		 solvedProjectsIds.push(pCachOutNextPeriodGridData[args.rows[i]].projectId)
													    	 }
													    	 
													    	 window.location.href  = "solve.jsp?portfolioId=" + portfolioId 
													 			+ "&from=" + fromDate.toJSON() 
													 			+ "&to="   + toDate.toJSON()
													 			+ "&next="   + nextPeriodEndDate.toJSON()
													 			+ "&selectedProject=" + $("#currentProject option:selected").val()
													 			+ "&solvedProjects=" + args.rows
													 			+ "&solvedProjectsIds=" + solvedProjectsIds;
													    	
													    	
													    });
												

													   	 rpcClient.financeService.findFinanceByDate( function(result , exception) {
													   		 
													   		 if (result.result == 0) {
													   			 var currentFinancingLimit = result.data;
															   	 rpcClient.portfolioService.getExtraCachNextPeriod( function(result , exception) {
															   		if (result.result == 0) { 
																   		var pExtraCachColumns = [ { id : "finance", name : "Finance",  field : "finance",  minWidth : 120 , formatter : Slick.Formatters.Currency} ,
																				   	                  { id : "balance", name : "Balance",  field : "balance",  minWidth : 120 , formatter : Slick.Formatters.Currency} ,
																				   	                  { id : "paymentCurrent", name : "Payment current",  field : "paymentCurrent",  minWidth : 120 , formatter : Slick.Formatters.Currency} ,
																				   	                  { id : "cashoutCurrent", name : "Cash Out current",  field : "cashoutCurrent",  minWidth : 120 , formatter : Slick.Formatters.Currency},
																				   	                  { id : "currentCost", name : "Current Cost",  field : "currentCost",  minWidth : 120 , formatter : Slick.Formatters.Currency} ,
																				   	                  { id : "paymentNext", name : "Payment Next",  field : "paymentNext",  minWidth : 120 , formatter : Slick.Formatters.Currency} ,
																				   	                  { id : "cashoutNext", name : "Cash Out next",  field : "cashoutNext",  minWidth : 120 , formatter : Slick.Formatters.Currency},
																				   	                  { id : "extraCash", name : "Extra Cash",  field : "extraCash",  minWidth : 120 , formatter : Slick.Formatters.Currency}];
																		var extraPaymentNextPeriod = result.data;
																		
																		var extraPaymentCurrentPeriod = 0;
																		rpcClient.portfolioService.getExtraCachCurrentPeriod( function(result , exception) {
																	   		if (result.result == 0) { 
																	   			extraPaymentCurrentPeriod = result.data;
																				var pExtraCachData = [];
																				//alert("currentFinancingLimit: " + currentFinancingLimit + "\n" + "totalOpenBalance: " + totalOpenBalance + "\n" + "totalCashout: " + totalCashout + "\n" + "extraPaymentCurrentPeriod: " + extraPaymentCurrentPeriod);
																		   		pExtraCachData[0] = {
																		   				"finance" :	currentFinancingLimit,
																		   				"balance" : totalOpenBalance,
																		   				"paymentCurrent" : extraPaymentCurrentPeriod,
																		   				"cashoutCurrent" : cashoutCurrent,
																		   				"currentCost" : totalCurrentCost + solvedProjectsCost,
																		   				"paymentNext" : extraPaymentNextPeriod,
																		   				"cashoutNext" : totalCashout,																   				
																		   				"extraCash" : currentFinancingLimit + extraPaymentNextPeriod + totalOpenBalance - totalCurrentCost - solvedProjectsCost -  totalCashout - cashoutCurrent /*+ totalFinanceCost*/ + extraPaymentCurrentPeriod
																		   		};
																		   		
																		   	 var pExtraCashCurPeriodGrid = new Slick.Grid("#extraCashtNextPeriodGrid", pExtraCachData , pExtraCachColumns, {
																		   		    editable : true,
																		   		    enableAddRow : true,
																		   		    enableCellNavigation : true,
																		   		    enableColumnReorder : true
																		   		});
																	   		} else {
																	   			 alert(result.message);
																	   		 }
																	   	} , portfolioId , fromDate , toDate);
																		

															   		} else {
															   			alert(result.message);
															   		}
															   		 
																   	 } , portfolioId , toDate, nextPeriodEndDate , JSON.stringify(adjustmentInformation));
														   		 } else {
														   			 alert(result.message);
														   		 }
													   	 } , portfolioId , toDate);
										
												 }
											 } else {
												 alert(result.message);
											 } 
											
										 } , portfolioId , toDate , nextPeriodEndDate );   	              

									 } else {
										 alert("Error: " + result.message + exception.message);
									 }
								 } , $("#currentProject option:selected").val() , fromDate , toDate);

							 } else {
								 alert(result.message);
							 }
							 
						 } , portfolioId , fromDate, toDate);
			   		 } else {
			   			alert(result.message);
			   		 }
			   	 } , portfolioId , fromDate);
			 }
			 
		 } else {
			 alert(result.message)
		 }
	 } , portfolioId ,fromDate, toDate);
   	
   	 
	 
	 
	 
	
	 
	 
	 
	
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

function formatter(row, cell, value, columnDef, dataContext) {
    return value;
}

function DateFormatter(row, cell, value, columnDef, dataContext) {
    return (value.getMonth()+1)+"/"+value.getDate()+"/"+value.getFullYear();
  }

function getISODateTime(d){
    // padding function
    var s = function(a,b){return(1e15+a+"").slice(-b)};

    // default date parameter
    if (typeof d === 'undefined'){
        d = new Date();
    };

    // return ISO datetime
    return d.getFullYear() + '-' +
        s(d.getMonth()+1,2) + '-' +
        s(d.getDate(),2) + ' ' +
        s(d.getHours(),2) + ':' +
        s(d.getMinutes(),2) + ':' +
        s(d.getSeconds(),2);
}

function DateFmt(fstr) {
    this.formatString = fstr;

    var mthNames = [ "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" ];
    var dayNames = [ "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" ];
    var zeroPad = function(number) {
	return ("0" + number).substr(-2, 2);
    };

    var dateMarkers = {
	d : [ 'getDate', function(v) {
	    return zeroPad(v);
	} ],
	m : [ 'getMonth', function(v) {
	    return zeroPad(v + 1);
	} ],
	n : [ 'getMonth', function(v) {
	    return mthNames[v];
	} ],
	w : [ 'getDay', function(v) {
	    return dayNames[v];
	} ],
	y : [ 'getFullYear' ],
	H : [ 'getHours', function(v) {
	    return zeroPad(v);
	} ],
	M : [ 'getMinutes', function(v) {
	    return zeroPad(v);
	} ],
	S : [ 'getSeconds', function(v) {
	    return zeroPad(v);
	} ],
	i : [ 'toISOString' ]
    };

    this.format = function(date) {
	var dateTxt = this.formatString.replace(/%(.)/g, function(m, p) {
	    var rv = date[(dateMarkers[p])[0]]();

	    if (dateMarkers[p][1] != null)
		rv = dateMarkers[p][1](rv);

	    return rv;

	});

	return dateTxt;
    };
       

}

function isSolvedProject(projectId , solvedProjectsIds) {
	if (solvedProjectsIds == null || solvedProjectsIds.length == 0 ) {
		return false;
	}
	for (var i = 0 ; i < solvedProjectsIds.length; i++) {
		if (projectId == solvedProjectsIds[i]) {
			return true;
		}
	}
	return false;
}