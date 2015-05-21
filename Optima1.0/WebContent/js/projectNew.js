$(function() {
	
    $(document).tooltip({
	items : "#mainAllTasks li, #allTasks li",
	content : function() {
	    var element = $(this);

	    var output = $("<div></div>");

	    output.addClass('divToolTip');

	    if (element.is("[title]")) {
		// var text = element.text();
		output.append("<h3>Name:</h3><p>" + element.attr('title') + "</p>");
	    }

	    if (element.is("[description]")) {
		// var text = element.text();
		output.append("<h3>Desciption:</h3><p>" + element.attr('description') + "</p>");
	    }
	    return output.html();
	}
    });

    $.widget("ui.pcntspinner", $.ui.spinner, {
	_format : function(value) {
	    return value + '%';
	},

	_parse : function(value) {
	    return parseFloat(value);
	}
    });

    $("#interestRateTxt").pcntspinner({
	min : 0,
	max : 100000,
	step : 1
    });
    
    $("#retainedPercentageTxt").pcntspinner({
    	min : 0,
    	max : 100000,
    	step : 1
        });
    
    $("#advancedPaymentPercentage").pcntspinner({
    	min : 0,
    	max : 100000,
    	step : 1
        });

    $(".spin").spinner({
	min : 0,
	max : 365,
	step : 1
    });

    $('input.datepicker').datepicker({
	showOn : "button",
	buttonImage : "images/calendar.png",
	buttonImageOnly : true
    });

    $('textarea').resizable();

    var projectId = null;

    for ( var i in getURLVariables()) {
		if (i == "projectId") {
		    projectId = getURLVariables()[i];
		}
    }

    /** Start Code for Project Days off */
    $("#pDayOffDateTxt").datepicker({
	showOn : "button",
	buttonImage : "images/calendar.png",
	buttonImageOnly : true,
	numberOfMonths : 3
    });

    $("#daysOffList").sortable({
	revert : true,
	items : "li"
    });

    // $("#mainAllTasks").listview();

    var daysOffList = rpcClient.daysOffService.findAllByProject(projectId);
    var fmt = new DateFmt("%w %d-%n-%y");
    if (daysOffList.result == 0) {
	daysList = daysOffList.data.list;
	for (var i = 0; i < daysList.length; i++) {
	    var dayOff = daysList[i];
	    var theDate = new Date(dayOff.dayOff.time);

	    var li = $('<li></li>').addClass('ui-state-default').attr('id', dayOff.dayoffId).text(fmt.format(theDate));

	    $("#daysOffList").append(li);

	}
    }

    $("#dayOffTrash").droppable({
	accept : "#daysOffList li",
	hoverClass : "ui-state-hover",
	drop : function(ev, ui) {
	    var agree = confirm('The selected day will be deleted permanently!');
	    if (agree) {
		ui.draggable.remove();
		var delDayCall = rpcClient.daysOffService.remove(ui.draggable.attr("id"));
		if (delDayCall.result == 0) {
		    alert('Day deleted!');
		} else
		    alert('Error occured: ' + delDayCall.message);
	    } else {
		return false;
	    }

	}
    });
    
    
    $("#resetSchedulingBtn").button({
    	icons : {
    	    primary : "ui-icon-refresh"
    	},
    	text : true
        }).click(
    		function() {
    			rpcClient.taskService.resetScheduling(function(result, exception) {
    				if (result.result == 0) {
    					alert("Project Scheduling successfully reset.");
    					location.reload(true);
    				} else {
    					alert("Error:" + result.message )
    				}
    			} , projectId);
    		}
    );

    $("#addDayOffBtn").button({
	icons : {
	    primary : "ui-icon-circle-plus"
	},
	text : true
    }).click(
	    function() {
		var dayoff = $("#pDayOffDateTxt").val();
		if (dayoff == null || dayoff == "") {
		    alert("A Date must be selected!");
		} else {
		    var dayOffDate = new Date(dayoff);
		    var createDayOffRequest = rpcClient.daysOffService.create(dayOffDate, "VACATION", projectId);
		    if (createDayOffRequest.result == 0) {
			var dayOff = createDayOffRequest.data;
			var theDate = new Date(dayOff.dayOff.time);
			var li = $('<li></li>').addClass('ui-state-default').attr('id', dayOff.dayoffId).text(
				fmt.format(theDate));

			$("#daysOffList").append(li);
		    } else {
			alert(createDayOffRequest.message);
		    }
		}
	    });

    /** End code for Project Days off */
    $('#projectTabs').tabs({
	activate : function(e, ui) {
	    $.cookie('project-selected-tab', ui.newTab.index(), {
		path : '/'
	    });
	},
	active : $.cookie('project-selected-tab')
    });

    $('#projectTabs button').button();

    if (projectId != null) {
	var tskCall = rpcClient.taskService.findAllByProject(projectId);
	if (tskCall.result == 0) {
	    var tskData = tskCall.data.list;

	
	    fillTaskList(tskData);

	    $("#mainAllTasks").sortable({
		revert : true,
		items : "li"
	    });

	    $("#trash").droppable({
		accept : "#mainAllTasks li",
		hoverClass : "ui-state-hover",
		drop : function(ev, ui) {
		    var agree = confirm('The selected task dependency will be deleted permanently!');
		    if (agree) {
			ui.draggable.remove();
			var delTaskCall = rpcClient.taskService.remove(ui.draggable.attr("id"));
			if (delTaskCall.result == 0) {
			    alert('Task deleted!');
			    location.reload(true);
			} else
			    alert('Error occured: ' + delTaskCall.message);
		    } else {
			return false;
		    }

		}
	    });

	} else {
	    alert('error: ' + tskCall.message);
	}
    }

    $("#mainAllTasks li").dblclick(function() {

	var tskID = $(this).attr('id');
	if (tskID == "undefined") {
	    return false;
	}

	tskID = parseInt(tskID);

	var taskCall = rpcClient.taskService.find(tskID);
	if (taskCall.result == 0) {

	    var d = taskCall.data;
	    var fmt2 = new DateFmt("%m/%d/%y");
	    if (d.tentativeStartDate != null) {
		var tenStartDate = new Date(d.tentativeStartDate.time);
		tenStartDate = fmt2.format(tenStartDate);
	    }

	    var scStartDate = "";
	    if (d.scheduledStartDate != null) {
		scStartDate = new Date(d.scheduledStartDate.time);
		scStartDate = fmt2.format(scStartDate);
	    }

	    var actStartDate = "";
	    if (d.actualStartDate != null) {
		actStartDate = new Date(d.actualStartDate.time);
		actStartDate = fmt2.format(actStartDate);
	    }

	    $("#taskNameTxt").val(d.taskName);
	    $("#durationTxt").val(d.duration);
	    $("#taskDescTxt").val(d.taskDescription);
	    $("#dailyCostTxt").val(d.uniformDailyCost);
	    $("#dailyIncomeTxt").val(d.uniformDailyIncome);
	    $("#sDateTentative").val(tenStartDate);
	    $("#sDateScheduled").val(scStartDate);
	    $("#sDateActual").val(actStartDate);
	    
	    $("#projTasksDialog").data("task", d).dialog('option', 'title', 'Update Task').dialog('open');

	} else {
	    Alert('error: ' + taskCall.message);
	}

    });

    $("#projTasksDialog").dialog(
	    {
		autoOpen : false,
		width : 740,
		height : 'auto',
		modal : true,
		show : {
		    effect : "blind",
		    duration : 1000
		},
		hide : {
		    effect : "fade",
		    duration : 1000
		},

		close : function() {
		    // $(this).data('taskId','');
		    clearDialogFields($(this).attr('id'));
		},
		open : function() {

		    var task = $(this).data('task');

		    if (task == null) {// new task

			$('#divTasksDepends').hide();

		    } else {// in case of update task
			$('#divTasksDepends').show();

			

			var allTaskCall = rpcClient.taskService.findAllByProjectForCertainTask(projectId, task.taskId);
			if (allTaskCall.result == 0) {
			    var allTasks = allTaskCall.data.list;
			    for (var i = 0; i < allTasks.length; i++) {
				//if (allTasks[i].taskId != task.taskId) {
				    var li = $('<li></li>').addClass('ui-state-default').attr('id', allTasks[i].taskId)
					    .text(allTasks[i].taskName);
				    li.attr('title', allTasks[i].taskName);
				    li.attr('description', allTasks[i].taskDescription);
				    $("#allTasks").append(li);
				//}
			    }
			   
			    
			    var depList = task.asDependent.list;
			    
			    
			    
			    for (var i = 0; i < depList.length; i++) {
				
				//alert(' depList[i].taskId: ' +  depList[i].projectTask1.taskId);
				
				    var li = $('<li></li>').addClass('ui-state-default').attr('id', depList[i].dependency.taskId)
					    .text(depList[i].dependency.taskName);
				    li.attr('title', depList[i].dependency.taskName);
				    li.attr('description', depList[i].dependency.taskDescription);
				    $("#dependencies").append(li);
				
			    }
			    
			    

			    $("#allTasks, #dependencies").sortable({
				connectWith : ".sortable",
				item : '> li:not(.ui-state-disabled)',
				dropOnEmpty : true,

			    }).disableSelection();

			    $("#allTasks").droppable(
				    {
					accept : "#dependencies li",
					hoverClass : "ui-state-hover",
					drop : function(ev, ui) {

					    var remDepCall = rpcClient.taskService.removeTaskDependency(task.taskId,
						    ui.draggable.attr("id"));
					    if (remDepCall.result == 0) {
						ui.draggable.remove();
						$(this).append(ui.draggable);
						return true;
					    } else {
						return false;
					    }
					}
				    });

			    $("#dependencies").droppable(
				    {
					accept : "#allTasks li",
					hoverClass : "ui-state-hover",
					drop : function(ev, ui) {

					    var addDepCall = rpcClient.taskService.addTaskDependency(ui.draggable
						    .attr("id"), task.taskId);
					    if (addDepCall.result == 0) {
						ui.draggable.remove();
						$(this).append(ui.draggable);
						return true;
					    } else {
						return false;
					    }

					}
				    });

			} else {
			    alert("Failed to load tasks: " + allTaskCall.message);
			}
		    }
		},
		buttons : {
		    "Submit" : function() {
			$("#sDateTentative").removeClass("ui-state-error");
			$("#durationTxt").removeClass("ui-state-error");

			if (projectId == null) {
			    return false;
			}
			var bValid = true;

			bValid = bValid && checkLength($("#taskNameTxt"), "taskNameTxt", 3, 256);

			var tentativeStartDate = $("#sDateTentative").val();

			if (tentativeStartDate == null || tentativeStartDate.length == 0) {
			    $("#sDateTentative").addClass("ui-state-error");
			    bValid = false;
			}

			var duration = $("#durationTxt").val();

			if (duration == null || duration.length == 0) {
			    $("#durationTxt").addClass("ui-state-error");
			    bValid = false;
			}

			if (!/^[0-9]+$/.test(duration)) {
			    $("#durationTxt").addClass("ui-state-error");
			    bValid = false;
			}

			var dailyCostTxt = $("#dailyCostTxt").val();
			if (dailyCostTxt == null || dailyCostTxt.length == 0) {
			    dailyCostTxt = "0"
			}
			;

			if (!/^-?\d*\.?\d*$/.test(dailyCostTxt)) {
			    $("#dailyCostTxt").addClass("ui-state-error");
			    bValid = false;
			}
			var dailyIncomeTxt = $("#dailyIncomeTxt").val();
			if (dailyIncomeTxt == null || dailyIncomeTxt.length == 0) {
			    dailyIncomeTxt = "0"
			}
			;

			if (!/^-?\d*\.?\d*$/.test(dailyIncomeTxt)) {
			    $("#dailyIncomeTxt").addClass("ui-state-error");
			    bValid = false;
			}
			var scheduledStartDate = $("#sDateScheduled").val();
			if (scheduledStartDate != null && scheduledStartDate.length != 0) {
			    scheduledStartDate = new Date(scheduledStartDate);
			} else {
			    scheduledStartDate = null;
			}
			var actualStartDate = $("#sDateActual").val();
			if (actualStartDate != null && actualStartDate.length != 0) {
			    actualStartDate = new Date(actualStartDate);
			} else {
			    actualStartDate = null;
			}
			if (bValid) {
			    var task = $(this).data('task');
			     if (task != null) { // in case of updating
				// task
				var updCall = rpcClient.taskService.update(task.taskId, projectId, $("#taskNameTxt")
					.val(), $("#taskDescTxt").val(), parseInt($("#durationTxt").val()),
					dailyCostTxt, dailyIncomeTxt, new Date($("#sDateTentative").val()),
					scheduledStartDate, actualStartDate);

				if (updCall.result == 0) {
				    $(this).dialog("close");
				    location.reload(true)
				} else {
				    alert('error: ' + updCall.message);
				}

			    } else { // new task

				var call = rpcClient.taskService.create(projectId, $("#taskNameTxt").val(), $(
					"#taskDescTxt").val(), parseInt($("#durationTxt").val()), dailyCostTxt,
					dailyIncomeTxt, new Date($("#sDateTentative").val()), scheduledStartDate,
					actualStartDate)

				if (call.result == 0) {
				    $(this).dialog("close");
				    location.reload(true);
				} else {
				    alert('Error ..' + call.message);
				}

			    }
			    $(this).dialog("close");
			  } else { //Invalid

			   alert("Please fix your input. Data is invalid");
			  }
		    },
		    "Reset Task Schedule" : function() {
		    	var task = $(this).data('task');
		    	rpcClient.taskService.resetTaskScheduling(function(result ,exception) {
		    		
		    		if (result.result == 0) {
		    			$("#sDateScheduled").val("");
		    			alert("Schedule reset successfully");
		    		} else {
		    			alert(result.message);
		    		}
		    	}, task.taskId);
		    },
		    Cancel : function() {
			$(this).dialog("close");
		    }
		}

	    });

    $('#addTaskBtn').button({
	icons : {
	    primary : "ui-icon-circle-plus"
	}
    });

    $('#addTaskBtn').on('click', function() {
	$("#projTasksDialog").data("task", null).dialog('option', 'title', 'Add New Task').dialog('open');
    });


    $('#saveProjectBtn').on(
	    'click',
	    function() {

		$("#projnameTxt").removeClass("ui-state-error");
		$("#interestRateTxt").removeClass("ui-state-error");
		$("#overHeadPerDayTxt").removeClass("ui-state-error");
		$("#retainedPercentageTxt").removeClass("ui-state-error");
		
		$("#advPaymentAmountTxt").removeClass("ui-state-error");
		$("#delayPenaltyTxt").removeClass("ui-state-error");
		$("#collectPaymentPeriodTxt").removeClass("ui-state-error");
		$("#payRequestsPeriodTxt").removeClass("ui-state-error");
		
		$("#advancedPaymentPercentage").removeClass("ui-state-error");
		var bValid = true;

		bValid = bValid && checkLength($("#projnameTxt"), "projnameTxt", 3, 128);

		var interestRateTxt = $("#interestRateTxt").val();

		if (interestRateTxt.match("\%$") == "%") {
		    interestRateTxt = interestRateTxt.substr(0, interestRateTxt.length - 1);
		}
		if (!/^-?\d*\.?\d*$/.test(interestRateTxt)) {
		    $("#interestRateTxt").addClass("ui-state-error");
		    bValid = false;
		}
		
		var retainedPercentageTxt = $("#retainedPercentageTxt").val();
		
		if (retainedPercentageTxt.match("\%$") == "%") {
			retainedPercentageTxt = retainedPercentageTxt.substr(0, retainedPercentageTxt.length - 1);
		}
		if (!/^-?\d*\.?\d*$/.test(retainedPercentageTxt)) {
		    $("#retainedPercentageTxt").addClass("ui-state-error");
		    bValid = false;
		}
		
		var advancedPaymentPercentage = $("#advancedPaymentPercentage").val();

		if (advancedPaymentPercentage.match("\%$") == "%") {
			advancedPaymentPercentage = advancedPaymentPercentage.substr(0, advancedPaymentPercentage.length - 1);
		}
		if (!/^-?\d*\.?\d*$/.test(advancedPaymentPercentage)) {
		    $("#advancedPaymentPercentage").addClass("ui-state-error");
		    bValid = false;
		}

		var overHeadPerDayTxt = $("#overHeadPerDayTxt").val();

		if (!/^-?\d*\.?\d*$/.test(overHeadPerDayTxt)) {
		    $("#overHeadPerDayTxt").addClass("ui-state-error");
		    bValid = false;
		}
		
		
		var advPaymentAmountTxt = $("#advPaymentAmountTxt").val();
		if (!/^-?\d*\.?\d*$/.test(advPaymentAmountTxt)) {
			$("#advPaymentAmountTxt").addClass("ui-state-error");
		    bValid = false;
		}
		
		var delayPenaltyTxt = $("#delayPenaltyTxt").val();
		if (!/^-?\d*\.?\d*$/.test(delayPenaltyTxt)) {
			$("#delayPenaltyTxt").addClass("ui-state-error");
		    bValid = false;
		}
		
		var collectPaymentPeriodTxt = $("#collectPaymentPeriodTxt").val();
		var payRequestsPeriodTxt = $("#payRequestsPeriodTxt").val();
		
		var pStartDateTxt = $("#pStartDateTxt").val();
		if (pStartDateTxt != null && pStartDateTxt.length != 0) {
		    pStartDateTxt = new Date(pStartDateTxt);
		} else {
		    pStartDateTxt = null;
		}

		var pFinishDateTxt = $("#pFinishDateTxt").val();
		if (pFinishDateTxt != null && pFinishDateTxt.length != 0) {
		    pFinishDateTxt = new Date(pFinishDateTxt);
		} else {
		    pFinishDateTxt = null;
		}

		if (bValid) {
			var interestRateValue = parseFloat(interestRateTxt) / ( 100*360.0 );
			var retainedPercentageValue = parseFloat(retainedPercentageTxt) / 100;
			var advancedPaymentPercentageValue = parseFloat(advancedPaymentPercentage) / 100;
		
		    var call = rpcClient.projectService.update(projectId, $("#projnameTxt").val(), $("#projCodeTxt").val(), $("#projectDescTxt").val(), $("#projStreet").val(),
		    		$("#projectCity option:selected").val(), $("#projectProv option:selected").val(), $("#projectCountry option:selected").val(), $("#projPostCode").val(), pStartDateTxt,
			    pFinishDateTxt, interestRateValue, overHeadPerDayTxt, $("#portfolioId").val(), $("#projectClient").val(), $("#weDays option:selected").val(),
			    retainedPercentageValue, advancedPaymentPercentageValue, advPaymentAmountTxt, delayPenaltyTxt, collectPaymentPeriodTxt, payRequestsPeriodTxt);
		    if (call.result == 0) {
			alert("Project updated successfully");
			location.reload(true);

		    } else {
			alert("Error: " + call.message);
		    }
		}

	    });

    // var projectId = null;

    for ( var i in getURLVariables()) {
	if (i == "projectId") {
	    projectId = getURLVariables()[i];
	}
    }

    if (projectId != null) {

	var locationsCall = rpcClient.locationService.findAll();
	$('#projectCountry').append($("<option></option>").val(-1).text(""));
	$('#projectProv').append($("<option></option>").val(-1).text(""));
	$('#projectCity').append($("<option></option>").val(-1).text(""));
	if (locationsCall.result == 0) {
	    var lData = locationsCall.data.list;

	    // alert('lData.length: ' + lData.length);

	    for (var i = 0; i < lData.length; i++) {
		var e = lData[i];

		if (e.locationType == 'COUNTRY')
		    $('#projectCountry').append($("<option></option>").val(e.locationId).text(e.locationName));

		if (e.locationType == 'PROVINCE')
		    $('#projectProv').append($("<option></option>").val(e.locationId).text(e.locationName));

		if (e.locationType == 'CITY')
		    $('#projectCity').append($("<option></option>").val(e.locationId).text(e.locationName));

	    }

	} else {
	    alert("Error: " + locationsCall.message);
	}

	var clientInfoCall = rpcClient.clientService.findAll();
	$('#projectClient').append($("<option></option>").val(-1).text(""));
	if (clientInfoCall.result == 0) {
	    var cData = clientInfoCall.data.list;

	    for (var i = 0; i < cData.length; i++) {
		var e = cData[i];

		$('#projectClient').append($("<option></option>").val(e.clientId).text(e.clientName));

	    }

	} else {
	    alert("Error: " + clientInfoCall.message);
	}

	var projCall = rpcClient.projectService.find(projectId);

	var pData;

	// var projectClientId;
	if (projCall.result == 0) {
	    pData = projCall.data;
	    
	    $("#portfolioId").val(pData.portfolio.portfolioId);
	    $("#projnameTxt").val(pData.projectName);
	    $("#projCodeTxt").val(pData.projectCode);
	    document.title = pData.projectCode;
	    $("#projectDescTxt").val(pData.projectDescription);

	    $("#projStreet").val(pData.projectAddressStreet);
	    if (pData.country != null) {
		$("#projectCountry").val(pData.country.locationId).prop('selected', true);
	    } else {
		$("#projectCountry").val(-1).prop('selected', true);
	    }

	    if (pData.province != null) {
		$("#projectProv").val(pData.province.locationId).prop('selected', true);
	    } else {
		$("#projectProv").val(-1).prop('selected', true);
	    }
	    if (pData.city != null) {
		$("#projectCity").val(pData.city.locationId).prop('selected', true);
	    } else {
		$("#projectCity").val(-1).prop('selected', true);
	    }
	    if (pData.client != null) {
		$("#projectClient").val(pData.client.clientId).prop('selected', true);
		loadClientInfo(pData.client.clientId);
	    }
	    var fmt2 = new DateFmt("%m/%d/%y");
	    if (pData.propusedStartDate != null) {
		$("#pStartDateTxt").val(fmt2.format(new Date(pData.propusedStartDate.time)));
	    }
	    if (pData.pFinishDateTxt != null) {
		$("#pFinishDateTxt").val(new Date(fmt2.format(pData.proposedFinishDate.time)));
	    }
	    if (pData.weekendDays != null) {
		$("#weDays").val(pData.weekendDays.weekendDaysId).prop('selected', true);
	    } else {
		$("#weDays").val(-1).prop('selected', true);
	    }
	    // setSelectedOption("#projectCountry", pData.country.locationId);
	    // setSelectedOption("#projectProv", pData.province.locationId);
	    // setSelectedOption("#projectCity", pData.city.locationId);

	    $("#projPostCode").val(pData.projectAddressPostalCode);

	    $("#interestRateTxt").val(roundToThreePlaces( parseFloat(pData.interestRate) * 100 * 360));

	    $("#overHeadPerDayTxt").val(pData.overheadPerDay);
	    $("#retainedPercentageTxt").val(pData.retainedPercentage*100);
	    $("#advancedPaymentPercentage").val(pData.advancedPaymentPercentage*100);
	    
	    $("#advPaymentAmountTxt").val(pData.advancedPaymentAmount);
	    $("#delayPenaltyTxt").val(pData.delayPenaltyAmount);
	    $("#collectPaymentPeriodTxt").val(pData.collectPaymentPeriod);
	    $("#payRequestsPeriodTxt").val(pData.paymentRequestPeriod);

	    
	} else {
	    alert("Error: " + projCall.message);
	}

	// /Load Locations

	// /Load clients

	$("#projectClient").change(function() {
	    // alert( "Handler for .change() called." );

	    var selectedClient = $('#projectClient option:selected').attr("value");

	    // alert( "selectedClient: " + selectedClient);

	    loadClientInfo(selectedClient);

	});

    }

});


function loadClientInfo(selectedClient){
	 if (selectedClient != null && selectedClient != -1) {
			var clientCall = rpcClient.clientService.find(selectedClient);

			if (clientCall.result == 0) {
			    var cData = clientCall.data;
			    $("#clientStreet").val(cData.clientAddressStreet);
			    $("#clientCountry").val(cData.country.locationName);
			    $("#clientProv").val(cData.province.locationName);
			    $("#clientCity").val(cData.city.locationName);
			    $("#clientPostCode").val(cData.clientAddressPostalCode);
			} else {
			    alert("Error: " + clientCall.message);
			}
		}else{
			$("#clientStreet").val('');
		    $("#clientCountry").val('');
		    $("#clientProv").val('');
		    $("#clientCity").val('');
		    $("#clientPostCode").val('');
		}
}

/*
 * $(document).ready(function(){
 * 
 * $('#mainAllTasks').trigger('create').listview('refresh');
 * 
 * });
 */

function clearDialogFields(dialogID) {

    $('#' + dialogID + ' input[type=text], #' + dialogID + ' textarea').each(function() {

	$(this).val('');

    });
    $("#allTasks, #dependencies").html("");
}

function fillTaskList(data) {

    $("#mainAllTasks").html('');
    for (var i = 0; i < data.length; i++) {

	// alert(tskData[i].taskId);

	var li = $('<li></li>').addClass('ui-state-default').attr('id', data[i].taskId).text(data[i].taskName);

	li.attr('title', data[i].taskName);
	li.attr('description', data[i].taskDescription);

	$("#mainAllTasks").append(li);
	// $("#allTasks").append(li);
    }

}

function setSelectedOption(select, val) {
    $(select + " option").each(function() {
	if ($(this).val() == val) {
	    $(this).prop("selected", true);
	} else {
	    $(this).removeAttr("selected");
	}
    });

}

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

function isDependentTask(taskId, task) {
    var dependencies = task.taskDependencies2.list;
    for (var i = 0; i < dependencies.length; i++) {
	if (dependencies[i].projectTask1.taskId == taskId) {
	    return true;
	}
    }
    return false;
}

function roundToThreePlaces(num) {    
    return +(Math.round(num + "e+3")  + "e-3");
}