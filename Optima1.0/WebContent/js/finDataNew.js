
$(function() {
	document.title = 'Financial Periods';
    $("#paymentTypeRadio").buttonset();

    $('#paymentDate').datepicker({
	showOn : "button",
	buttonImage : "images/calendar.png",
	buttonImageOnly : true
    });

    $('#financeDate').datepicker({
	showOn : "button",
	buttonImage : "images/calendar.png",
	buttonImageOnly : true
    });
    
    $('#extraPaymentDate').datepicker({
    	showOn : "button",
    	buttonImage : "images/calendar.png",
    	buttonImageOnly : true
        });

    var fColumns = [];
    var eColumns = [];

    
    
    $("#addPayment").button({
	icons : {
	    primary : "ui-icon-circle-plus"
	},
	text : true
    }).click(
	    function() {
		var bValid = true;
		$("#paymentDate").removeClass("ui-state-error");
		$("#intremPaymentNumber").removeClass("ui-state-error");
		$("#advancePaymentAmount").removeClass("ui-state-error");
		var projectId = $("#projCodeSelect").val();
		var paymentType = $("input[name=paymentTypeRadio]:checked").attr("id");
		var paymentTypeId = 1;
		var intremPaymentNumber = $("#intremPaymentNumber").val();
		var advancePaymentAmount = $("#advancePaymentAmount").val();
		var intrimPaymentAmount = advancePaymentAmount;
		if (paymentType == "interimPayment") {
		    paymentTypeId = 2;
		    advancePaymentAmount = 0;
		    if (intremPaymentNumber == null || intremPaymentNumber.length == 0) {
			$("#intremPaymentNumber").addClass("ui-state-error");
			bValid = false;
		    }
		} else if (paymentType == "advancePayment") {
		    paymentTypeId = 1;
		    intremPaymentNumber = 0;
		    if (advancePaymentAmount == null || advancePaymentAmount.length == 0
			    || !/^-?\d*\.?\d*$/.test(advancePaymentAmount)) {
			$("#advancePaymentAmount").addClass("ui-state-error");
			bValid = false;
		    }
		}
		var paymentDate = $("#paymentDate").val();
		if (paymentDate != null && paymentDate.length != 0) {
		    paymentDate = new Date(paymentDate);
		} else {
		    $("#paymentDate").addClass("ui-state-error");
		    bvalid = false;
		}

		if (bValid) {
		    var createTaskResult = rpcClient.paymentService.create(projectId, paymentTypeId,
			    advancePaymentAmount,intrimPaymentAmount, paymentDate, intremPaymentNumber);

		    if (createTaskResult.result == 0) {
			location.reload(true);
		    } else {
		    	showMessage("Create Payment",'Error:' + createTaskResult.message,'error');
		    }
		} else {
	    	showMessage("Create Payment","Error: Please check your input",'error');
		}
	    });

    $("#addFinance").button({
	icons : {
	    primary : "ui-icon-circle-plus"
	},
	text : true
    }).click(function() {

	var bValid = true;
	var dateExists = false;
	$("#financeAmount").removeClass("ui-state-error");
	$("#financeDate").removeClass("ui-state-error");

	var financeDate = $("#financeDate").val();
	if (financeDate != null && financeDate.length != 0) {
	    financeDate = new Date(financeDate);
	    if (dateExistes(financeDate, fColumns)) {
		$("#financeDate").addClass("ui-state-error");
		dateExists = true;
	    }
	} else {
	    $("#financeDate").addClass("ui-state-error");
	    bvalid = false;
	}
	var financeAmount = $("#financeAmount").val();
	if (financeAmount == null || financeAmount.length == 0 || !/^-?\d*\.?\d*$/.test(financeAmount)) {
	    $("#financeAmount").addClass("ui-state-error");
	    bValid = false;
	}
	if (dateExists) {
    	showMessage("Add Finance","The entered date exists already in the finaces grid",'error');
	    return;
	}

	if (bValid) {
	    var createFinanceResult = rpcClient.financeService.create(portfolioId, financeAmount, financeDate);

	    if (createFinanceResult.result == 0) {
		location.reload(true);
	    } else {
	    	showMessage("Create Payment",'Error:' + createTaskResult.message,'error');
	    }
	} else {
    	showMessage("Create Payment","Error: Please check your input",'error');
	}
    });
    
    $("#addExtraPayment").button({
    	icons : {
    	    primary : "ui-icon-circle-plus"
    	},
    	text : true
        }).click(function() {

    	var bValid = true;
    	var dateExists = false;
    	$("#extraPaymentAmount").removeClass("ui-state-error");
    	$("#extraPaymentDate").removeClass("ui-state-error");

    	var extraPaymentDate = $("#extraPaymentDate").val();
    	if (extraPaymentDate != null && extraPaymentDate.length != 0) {
    		extraPaymentDate = new Date(extraPaymentDate);
    	    if (dateExistes(extraPaymentDate, eColumns)) {
    		$("#extraPaymentDate").addClass("ui-state-error");
    		dateExists = true;
    	    }
    	} else {
    	    $("#extraPaymentDate").addClass("ui-state-error");
    	    bvalid = false;
    	}
    	var extraPaymentAmount = $("#extraPaymentAmount").val();
    	if (extraPaymentAmount == null || extraPaymentAmount.length == 0 || !/^-?\d*\.?\d*$/.test(extraPaymentAmount)) {
    	    $("#extraPaymentAmount").addClass("ui-state-error");
    	    bValid = false;
    	}
    	if (dateExists) {
	    	showMessage("Add Payment","The entered date exists already in the Extra Payment grid",'error');
    	    return;
    	}

    	if (bValid) {
    	    var createExtraPaymentResult = rpcClient.extraPaymentService.create(portfolioId, extraPaymentAmount, extraPaymentDate);
    	    if (createExtraPaymentResult.result == 0) {
    		location.reload(true);
    	    } else {
		    	showMessage("Create Payment",'Error:' + createExtraPaymentResult.message,'error');
    	    }
    	} else {
	    	showMessage("Create Payment","Error: Please check your input",'error');
    	}
        });
    
    
    
    
    $("#FinPeriods").accordion({

	activate : function(event, ui) {
	    $.cookie('saved_index_fp', null);
	    $.cookie('saved_index_fp', $("#FinPeriods").accordion("option", "active"));
	},
	active : parseInt($.cookie('saved_index_fp')),
	heightStyle : "content"
    });

    var portfolioId = null;

    for ( var i in getURLVariables()) {
	if (i == "portfolioId") {
	    portfolioId = getURLVariables()[i];
	}
    }

    var result = rpcClient.projectService.findAllByPortfolio(portfolioId);
    if (result.result == 0) {
	var fmt = new DateFmt("%w %d-%n-%y");
	var pColumns = [ {
	    id : "project",
	    name : "Project Code",
	    field : "project",
	    minWidth : 120,
	    tag : "project",
	    formatter : formatter
	}, {
	    id : "projectName",
	    name : "Project Name",
	    field : "projectName",
	    tag : "projectName",
	    minWidth : 180
	},

	];
	var pData = [];

	var projectsList = result.data.list;

	for (var i = 0; i < projectsList.length; i++) {

	    $('#projCodeSelect').append(
		    $("<option></option>").val(projectsList[i].projectId).text(projectsList[i].projectCode));
	    
	    $('#collectionProject').append(
			    $("<option></option>").val(projectsList[i].projectId).text(projectsList[i].projectCode));

	    pData[i] = {
		project : "<a id= '" + projectsList[i].projectId + "' href='projectDetails.jsp?projectId="
			+ projectsList[i].projectId + "' target='_blank' tabindex='0'>" + projectsList[i].projectCode
			+ "</a>",
		projectName : projectsList[i].projectName,

	    }
	    var projectPayments = projectsList[i].projectPayments.list;

	    for (var j = 0; j < projectPayments.length; j++) {

			var theDate = new Date(projectPayments[j].paymentDate.time);
			var formattedDate = fmt.format(theDate);
	
			if (!isColumnExist(pColumns, formattedDate)) {
				insertColumn(pColumns , formattedDate , theDate , Slick.Formatters.Currency);
			}
		
		    if (pData[i][formattedDate] == null) {
		    	pData[i][formattedDate] = 0;
		    }
		    
		    var paymentAmount = projectPayments[j].paymentAmount;
		    
		    if (paymentAmount == 0 && projectPayments[j].paymentType.paymentType == "Intrim") {
		    	paymentAmount = projectPayments[j].paymentInitialAmount;
		    }
		    pData[i][formattedDate] += paymentAmount;
	    }
	}

	var pGrid = new Slick.Grid("#paymentsGrid", pData, pColumns, {
	    editable : true,
	    enableAddRow : true,
	    enableCellNavigation : true,
	    enableColumnReorder : true
	});

	pGrid.onContextMenu.subscribe(function(e, args) {
	    e.preventDefault();
	    var cell = pGrid.getCellFromEvent(e);
	    if (cell.cell > 1) {
		$("#paymentMenu").data("cell", cell).css("top", e.pageY).css("left", e.pageX).show();

		$("body").one("click", function() {
		    $("#paymentMenu").hide();
		});
	    }

	});
	$("#paymentMenu").click(function(e) {
	    if (!$(e.target).is("li")) {
		return;
	    }
	    if (!pGrid.getEditorLock().commitCurrentEdit()) {
		return;
	    }
	    var cell = $(this).data("cell");
	    var projectId = $(pData[cell.row].project).attr("id");
	    var paymentDate = pColumns[cell.cell].tag;

	    var deletePaymentsResult = rpcClient.paymentService.removePaymentsByDate(projectId, paymentDate);
	    if (deletePaymentsResult.result == 0) {
		pData[cell.row][pColumns[cell.cell].id] = "";
		pGrid.updateRow(cell.row);
		location.reload(true);
	    } else {
	    	showMessage("Create Payment",'Error:' + deletePaymentsResult.message,'error');
	    }
	});

    }

    var result = rpcClient.financeService.findAllByPortfolio(portfolioId);
    if (result.result == 0) {
	var fmt = new DateFmt("%w %d-%n-%y");
	var fData = [];
	fData[0] = {}
	fColumns.length = 0;
	var financesList = result.data.list;

	for (var i = 0; i < financesList.length; i++) {
	    var theDate = new Date(financesList[i].financeUntillDate.time);
	    fColumns.push({
		id : financesList[i].financeId,
		name : fmt.format(theDate),
		tag : theDate,
		field : "finance_" + financesList[i].financeId,
		minWidth : 120 , formatter : Slick.Formatters.Currency
	    });
	    fData[0]["finance_" + financesList[i].financeId] =  financesList[i].financeAmount;
	}

	fColumns.sort(function(a, b) {
	    return a.tag - b.tag;
	});

	var fGrid = new Slick.Grid("#financeGrid", fData, fColumns, {
	    editable : true,
	    enableAddRow : true,

	    enableCellNavigation : true,
	    enableColumnReorder : true
	});

	fGrid.onContextMenu.subscribe(function(e, args) {
	    e.preventDefault();
	    var cell = fGrid.getCellFromEvent(e);
	    $("#financeMenu").data("cell", cell).css("top", e.pageY).css("left", e.pageX).show();

	    $("body").one("click", function() {
		$("#financeMenu").hide();
	    });

	});

	$("#financeMenu").click(function(e) {
	    if (!$(e.target).is("li")) {
		return;
	    }
	    if (!fGrid.getEditorLock().commitCurrentEdit()) {
		return;
	    }
	    var cell = $(this).data("cell");
	    var financeId = fColumns[cell.cell].id;
	    var deleteFinanceResult = rpcClient.financeService.remove(financeId);
	    if (deleteFinanceResult.result == 0) {
		fColumns.splice(cell.cell, 1);
		fGrid.invalidate();
		fGrid.render();
		location.reload(true);
	    } else {
	    	showMessage("Delete Finance Result",'Error:' + deleteFinanceResult.message,'error');
	    }
	});

    }
    
    
    
    
    
    
    
    
    var result = rpcClient.extraPaymentService.findAllByPortfolio(portfolioId);
    if (result.result == 0) {
	var emt = new DateFmt("%w %d-%n-%y");
	var eData = [];
	eData[0] = {}
	eColumns.length = 0;
	var extraPaymentsList = result.data.list;

	for (var i = 0; i < extraPaymentsList.length; i++) {
	    var theDate = new Date(extraPaymentsList[i].extraPayment_date.time);
	    eColumns.push({
		id : extraPaymentsList[i].extraPayment_id,
		name : fmt.format(theDate),
		tag : theDate,
		field : "extraPayment_" + extraPaymentsList[i].extraPayment_id,
		minWidth : 120 , formatter : Slick.Formatters.Currency
	    });
	    eData[0]["extraPayment_" + extraPaymentsList[i].extraPayment_id] =  extraPaymentsList[i].extraPayment_amount;
	}

	eColumns.sort(function(a, b) {
	    return a.tag - b.tag;
	});

	var eGrid = new Slick.Grid("#extraPaymentGrid", eData, eColumns, {
	    editable : true,
	    enableAddRow : true,

	    enableCellNavigation : true,
	    enableColumnReorder : true
	});

	eGrid.onContextMenu.subscribe(function(e, args) {
	    e.preventDefault();
	    var cell = eGrid.getCellFromEvent(e);
	    $("#extraPaymentMenu").data("cell", cell).css("top", e.pageY).css("left", e.pageX).show();

	    $("body").one("click", function() {
		$("#extraPaymentMenu").hide();
	    });

	});

	$("#extraPaymentMenu").click(function(e) {
	    if (!$(e.target).is("li")) {
		return;
	    }
	    if (!eGrid.getEditorLock().commitCurrentEdit()) {
		return;
	    }
	    var cell = $(this).data("cell");
	    var extraPaymentId = eColumns[cell.cell].id;
	    var deleteExtraPaymentResult = rpcClient.extraPaymentService.remove(extraPaymentId);
	    if (deleteExtraPaymentResult.result == 0) {
		eColumns.splice(cell.cell, 1);
		eGrid.invalidate();
		eGrid.render();
		location.reload(true);
	    } else {
	    	showMessage("Delete Payment",'Error:' + deleteExtraPaymentResult.message,'error');
	    }
	});

    }
    
    
    
    
    $("#collectionProject").change(function() {
    	$('#paymentToCollect').empty();
    	rpcClient.paymentService.getIntrimPaymentsPerProject(function(result, exception) {
    		
    		var fmt = new DateFmt("%w %d-%n-%y");
    		if (result.result == 0) {
    			var data = result.data.list;
    			for (var i = 0 ; i < data.length ; i++) {
    				var theDate = new Date(data[i].paymentDate.time);
    				$('#paymentToCollect').append($("<option></option>").val(data[i].paymentId).text(
    						data[i].paymentInterimNumber + ":" +
    						fmt.format(theDate)  + ":" +
    						data[i].paymentInitialAmount + ":" +
    						data[i].paymentAmount));
    			}
   
    		} else {
		    	showMessage("Get Payment",'Error:' + result.message,'error');
    		}
    	} , $("#collectionProject option:selected").val());
		 	
	 });
    
    rpcClient.paymentService.getIntrimPaymentsPerProject(function(result, exception) {
		
		var fmt = new DateFmt("%w %d-%n-%y");
		if (result.result == 0) {
			var data = result.data.list;
			for (var i = 0 ; i < data.length ; i++) {
				var theDate = new Date(data[i].paymentDate.time);
				$('#paymentToCollect').append($("<option></option>").val(data[i].paymentId).text(
						data[i].paymentInterimNumber + ":" +
						fmt.format(theDate) + ":" +
						data[i].paymentInitialAmount + ":" +
						data[i].paymentAmount));
			}

		} else {
	    	showMessage("Get Payment",'Error:' + result.message,'error');
		}
	} , $("#collectionProject option:selected").val());
    
    $("#collectPayment").button({
    	icons : {
    	    primary : "ui-icon-circle-plus"
    	},
    }).click(function(){
    	var bValid = true;
    	$("#paymentToCollect").removeClass("ui-state-error");
        $("#amountToCollect").removeClass("ui-state-error");
    	
    	var paymentId = $("#paymentToCollect option:selected").val();
    	var amount = $("#amountToCollect").val();
    	if (paymentId == "undefined" || paymentId == null) {
    		$("#paymentToCollect").addClass("ui-state-error");
    	    bValid = false;
	    	showMessage("Collect Payment","No payment selected",'error');
    	}
    	if (amount == null || amount.length == 0 || !/^-?\d*\.?\d*$/.test(amount)) {
    	    $("#amountToCollect").addClass("ui-state-error");
    	    bValid = false;
    	    
    	}
    	if (bValid) {
    		
	    	rpcClient.paymentService.submitAnInterimPayment(function(result, exception) {
	    		if (result.result) {
			    	showMessage("Submit Payment","Payment submitted successfully",'error',{Close:function(){
						$(this).dialog("close");
						location.reload(true);
					}});
	    		} else {
			    	showMessage("Submit Payment",'Error:' + result.message,'error');
	    		}
	    	} , paymentId , amount);
    	}
    	// Validate amount is numeric and >= 0. 
    	// Update payment record with amount
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

function formatter(row, cell, value, columnDef, dataContext) {
    return value;
}

function isColumnExist(columns, elementId) {
    for (var i = 0; i < columns.length; i++) {
	if (columns[i]["id"] == elementId) {
	    return true;
	}
    }
    return false;
}

function sortDatesArray(array) {
    alert('sort func ');
    return array.sort(function(a, b) {
	var c = new Date(a.tag);
	alert('c: ' + c);
	var d = new Date(b.tag);
	alert('d: ' + d);
	return c - d;
    });

}

function insertColumn(columns , formattedDate, date , formatter ) {

		
	var l = 2;
	while (l < columns.length  && columns[l].tag < date  ) {
		l++;	
	} 
	
	
	for (var k = columns.length ; k > l ; k --){
		columns[k] = columns[k-1];
	}
	
	columns[l] = {
	id : formattedDate,
	name : formattedDate,
	field : formattedDate,
	tag : date,
	minWidth : 120,
	formatter : formatter
	};
	
}
// pColumns = sortDatesArray(pColumns);


function dateExistes(date, array) {
    
    for (var i = 0; i < array.length; i++) {
	//alert('date1 - date2: ' + date - array[i].tag);
	if (array[i].tag - date === 0)
	    return true;
    }
    return false;
}
