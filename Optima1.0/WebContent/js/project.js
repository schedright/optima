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

  $("#resetSchedulingBtn").button({
    text : true
  }).click(function() {
    rpcClient.taskService.resetScheduling(function(result,
        exception) {
      if (result.result == 0) {
        showMessage("Reset Schedule", 'Project scheduling successfully reset.', 'success', {
          Close : function() {
            $(this).dialog("close");
            location.reload(true);
          }
        });
      } else {
        showMessage("Reset Schedule", 'Error:' + result.message, 'error');
      }
    }, projectId);
  });

  /** End code for Project Days off */
  $('#projectTabs').tabs({
    activate : function(e,
        ui) {
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
    } else {
      showMessage("Show Project Details", 'Error: ' + tskCall.message, 'error')
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
      var fmt2 = new DateFmt(
          "%m/%d/%y");
      if (d.tentativeStartDate != null) {
        var tenStartDate = new Date(
            d.tentativeStartDate.time);
        tenStartDate = fmt2.format(tenStartDate);
      }

      var scStartDate = "";
      if (d.scheduledStartDate != null) {
        scStartDate = new Date(
            d.scheduledStartDate.time);
        scStartDate = fmt2.format(scStartDate);
      }

      var actStartDate = "";
      if (d.actualStartDate != null) {
        actStartDate = new Date(
            d.actualStartDate.time);
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
      
      var status = 1;
      var lag = 0;
      if (d.status) {
        status = d.status;
        if (typeof status!='number' || (status<1 || status>3)) {
          status=1;
        }
      }
      if (d.lag) {
        lag = d.lag;
        if (typeof lag!='number' || lag<0) {
          lag = 0;
        }
      }
      $("#statusId").val(status);

      $("#projTasksDialog").data("task", d).dialog('option', 'title', 'Update Task').dialog('open');

    } else {
      showMessage("Get Task", 'Error: ' + tskCall.message, 'error')
    }

  });

  $("#projTasksDialog").dialog({
    autoOpen : false,
    width : 740,
    height : 'auto',
    modal : true,
    show : {
      effect : "blind",
      duration : 300
    },
    hide : {
      effect : "fade",
      duration : 300
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
            // if (allTasks[i].taskId != task.taskId) {
            var li = $('<li></li>').addClass('ui-state-default').attr('id', allTasks[i].taskId).text(allTasks[i].taskName);
            li.attr('title', allTasks[i].taskName);
            li.attr('description', allTasks[i].taskDescription);
            $("#allTasks").append(li);
            // }
          }

          var depList = task.asDependent.list;

          for (var i = 0; i < depList.length; i++) {

            var li = $('<li></li>').addClass('ui-state-default').attr('id', depList[i].dependency.taskId).text(depList[i].dependency.taskName);
            li.attr('title', depList[i].dependency.taskName);
            li.attr('description', depList[i].dependency.taskDescription);
            $("#dependencies").append(li);

          }

          $("#allTasks, #dependencies").sortable({
            connectWith : ".sortable",
            item : '> li:not(.ui-state-disabled)',
            dropOnEmpty : true,

          }).disableSelection();

          $("#allTasks").droppable({
            accept : "#dependencies li",
            hoverClass : "ui-state-hover",
            drop : function(ev,
                ui) {

              var remDepCall = rpcClient.taskService.removeTaskDependency(task.taskId, ui.draggable.attr("id"));
              if (remDepCall.result == 0) {
                ui.draggable.remove();
                $(this).append(ui.draggable);
                return true;
              } else {
                return false;
              }
            }
          });

          $("#dependencies").droppable({
            accept : "#allTasks li",
            hoverClass : "ui-state-hover",
            drop : function(ev,
                ui) {

              var addDepCall = rpcClient.taskService.addTaskDependency(ui.draggable.attr("id"), task.taskId);
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
          showMessage("Load Tasks", 'error:' + allTaskCall.message, 'error')
        }
      }
    },
    buttons : {
      "Submit" : function() {
        $("#durationTxt").removeClass("ui-state-error");

        if (projectId == null) {
          return false;
        }
        var bValid = true;

        bValid = bValid && checkLength($("#taskNameTxt"), "taskNameTxt", 3, 256);

        var tentativeStartDate = $("#sDateTentative").val();

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
          scheduledStartDate = new Date(
              scheduledStartDate);
        } else {
          scheduledStartDate = null;
        }
        var actualStartDate = $("#sDateActual").val();
        if (actualStartDate != null && actualStartDate.length != 0) {
          actualStartDate = new Date(
              actualStartDate);
        } else {
          actualStartDate = null;
        }
        if (bValid) {
          var task = $(this).data('task');
          if (task != null) { // in case of updating
            var d = null;
            if ($("#sDateTentative").val()) {
              d = new Date(
                  $("#sDateTentative").val());
            }
            // task
            var updCall = rpcClient.taskService.update(task.taskId, projectId, $("#taskNameTxt").val(), $("#taskDescTxt").val(), parseInt($("#durationTxt").val()), dailyCostTxt, dailyIncomeTxt, d, scheduledStartDate, actualStartDate, $("#statusId").val());

            if (updCall.result == 0) {
              $(this).dialog("close");
              location.reload(true)
            } else {
              showMessage("Update Task", 'error:' + updCall.message, 'error')
            }

          } else { // new task
            var d = null;
            if ($("#sDateTentative").val()) {
              d = new Date(
                  $("#sDateTentative").val());
            }

            var call = rpcClient.taskService.create(projectId, $("#taskNameTxt").val(), $("#taskDescTxt").val(), parseInt($("#durationTxt").val()), dailyCostTxt, dailyIncomeTxt, d, scheduledStartDate, actualStartDate,$("#statusId").val())

            if (call.result == 0) {
              $(this).dialog("close");
              location.reload(true);
            } else {
              showMessage("Create Task", 'error:' + call.message, 'error')
            }

          }
          $(this).dialog("close");
        } else { // Invalid

          showMessage("Create Task", "Please fix your input. Data is invalid", 'error')
        }
      },
      Cancel : function() {
        $(this).dialog("close");
      }
    }

  });

  $('#addTaskBtn').button();

  $('#addTaskBtn').on('click', function() {
    $("#projTasksDialog").data("task", null).dialog('option', 'title', 'Add New Task').dialog('open');
  });

  $("#saveProjectBtn").button({
    text : true
  }).click(
      function() {
        $("#projnameTxt").removeClass("ui-state-error");
        $("#overHeadPerDayTxt").removeClass("ui-state-error");
        $("#retainedPercentageTxt").removeClass("ui-state-error");

        $("#delayPenaltyTxt").removeClass("ui-state-error");
        $("#collectPaymentPeriodTxt").removeClass("ui-state-error");
        $("#payRequestsPeriodTxt").removeClass("ui-state-error");

        $("#advancedPaymentPercentage").removeClass("ui-state-error");
        var bValid = true;

        bValid = bValid && checkLength($("#projnameTxt"), "projnameTxt", 3, 128);

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

        var delayPenaltyTxt = $("#delayPenaltyTxt").val();
        if (!/^-?\d*\.?\d*$/.test(delayPenaltyTxt)) {
          $("#delayPenaltyTxt").addClass("ui-state-error");
          bValid = false;
        }

        var collectPaymentPeriodTxt = $("#collectPaymentPeriodTxt").val();
        var payRequestsPeriodTxt = $("#payRequestsPeriodTxt").val();

        var pStartDateTxt = $("#pStartDateTxt").val();
        if (pStartDateTxt != null && pStartDateTxt.length != 0) {
          pStartDateTxt = new Date(
              pStartDateTxt);
        } else {
          pStartDateTxt = null;
        }

        var pFinishDateTxt = $("#pFinishDateTxt").val();
        if (pFinishDateTxt != null && pFinishDateTxt.length != 0) {
          pFinishDateTxt = new Date(
              pFinishDateTxt);
        } else {
          pFinishDateTxt = null;
        }

        if (bValid) {
          var retainedPercentageValue = parseFloat(retainedPercentageTxt) / 100;
          var advancedPaymentPercentageValue = parseFloat(advancedPaymentPercentage) / 100;
          var weekend = '';
          var weList = ['#we_sun', '#we_mon', '#we_tue', '#we_wed', '#we_thu', '#we_fri', '#we_sat'];
          for (var i=0;i<7;i++) {
            var checked =  $(weList[i]).prop("checked");
            weekend += (checked)?'1':'0';
          }
          var call = rpcClient.projectService.update(
              projectId,
              $("#projnameTxt").val(),
              $("#projCodeTxt").val(),
              $("#projectDescTxt").val(),
              pStartDateTxt,
              pFinishDateTxt,
              overHeadPerDayTxt,
              $("#portfolioId").val(),
              weekend,
              retainedPercentageValue,
              advancedPaymentPercentageValue,
              delayPenaltyTxt,
              collectPaymentPeriodTxt,
              payRequestsPeriodTxt);
          if (call.result == 0) {
            showMessage("Update Project", "Project updated successfully", 'success', {
              Close : function() {
                $(this).dialog("close");
                location.reload(true);
              }
            });

          } else {
            showMessage("Update Project", "Error:" + call.message, 'error');
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

    var projCall = rpcClient.projectService.find(projectId);

    var pData;

    if (projCall.result == 0) {
      pData = projCall.data;
      var portId = -1;
      if (pData.portfolio && pData.portfolio.portfolioId) {
        portId = pData.portfolio.portfolioId;
      }
      $('#titleDiv').html(pData.projectName);

      $("#portfolioId").val(portId);
      $("#projnameTxt").val(pData.projectName);
      $("#projCodeTxt").val(pData.projectCode);
      document.title = "SchedRight - " + pData.projectCode;
      $("#projectDescTxt").val(pData.projectDescription);

      var fmt2 = new DateFmt(
          "%m/%d/%y");
      if (pData.propusedStartDate != null) {
        $("#pStartDateTxt").val(fmt2.format(new Date(
            pData.propusedStartDate.time)));
      }
      if (pData.proposedFinishDate != null) {
        $("#pFinishDateTxt").val(fmt2.format(new Date(
            pData.proposedFinishDate.time)));
      }
      
      var weList = ['#we_sun', '#we_mon', '#we_tue', '#we_wed', '#we_thu', '#we_fri', '#we_sat'];
      if (pData.weekend && pData.weekend.length==7) {
        for (var i=0;i<7;i++) {
          var checked = pData.weekend[i]=='1';
           $(weList[i]).prop("checked",checked);
        }
        
      }
      
      if (pData.weekendDays != null) {
        $("#weDays").val(pData.weekendDays.weekendDaysId).prop('selected', true);
      } else {
        $("#weDays").val(-1).prop('selected', true);
      }

      $("#overHeadPerDayTxt").val(pData.overheadPerDay);
      $("#retainedPercentageTxt").val(pData.retainedPercentage * 100);
      $("#advancedPaymentPercentage").val(pData.advancedPaymentPercentage * 100);

      $("#delayPenaltyTxt").val(pData.delayPenaltyAmount);
      $("#collectPaymentPeriodTxt").val(pData.collectPaymentPeriod);
      $("#payRequestsPeriodTxt").val(pData.paymentRequestPeriod);

    } else {
      showMessage("Find Project", "Error:" + projCall.message, 'error');
    }

  }

  // Tasks section

  var salectedTask = {};
  $("#deleteTaskBtn").button({
    text : true
  }).click(function() {
    if (salectedTask.selection) {
      var buttons = {
        Yes : function() {
          $(this).dialog("close");
          var delTaskCall = rpcClient.taskService.remove(salectedTask.selection.id);
          if (delTaskCall.result == 0) {
            salectedTask.selection.remove();
            salectedTask.selection = null;
            $('#deleteTaskBtn').prop("disabled", true);
          } else {
            showMessage("Delete Task", 'Error occured: ' + delTaskCall.message, 'error')
          }
        },
        No : function() {
          $(this).dialog("close");
          return false;
        }
      }
      showMessage('Delete Task', 'The selected task will be deleted permanently!', 'warning', buttons);

    }
  });

  $('#deleteTaskBtn').prop("disabled", true);

  $("#mainAllTasks").selectable({
    cancel : '.ui-selected',
    selected : function(event,
        ui) {
      salectedTask.selection = ui.selected;
      $('#deleteTaskBtn').prop("disabled", false);
    },
    unselected : function(event,
        ui) {
      $('#deleteTaskBtn').prop("disabled", true);
      salectedTask.selection = null;
    }
  });

  // Tasks section end

  // Calendar Section
  var daysOffList = rpcClient.daysOffService.findAllByProject(projectId);
  var fmt = new DateFmt(
      "%w %d-%n-%y");
  if (daysOffList.result == 0) {
    daysList = daysOffList.data.list;
    for (var i = 0; i < daysList.length; i++) {
      var dayOff = daysList[i];
      var theDate = new Date(
          dayOff.dayOff.time);

      var li = $('<li></li>').addClass('ui-state-default').attr('id', dayOff.dayoffId).text(fmt.format(theDate));

      $("#daysOffList").append(li);

    }
  }

  $("#addDaysOffDialog").dialog({
    autoOpen : false,
    height : 250,
    width : 450,
    modal : true,
    dialogClass : "ZIndex900",
    show : {
      effect : "blind",
      duration : 300
    },
    hide : {
      effect : "fade",
      duration : 300
    },
    open : function() {
      /** Start Code for Project Days off */
      $("#pDayOffDateTxt").datepicker({
        showOn : "button",
        buttonImage : "images/calendar.png",
        buttonImageOnly : true,
        numberOfMonths : 3
      });
    },
    buttons : {
      "Add" : function() {
        var dayoff = $("#pDayOffDateTxt").val();
        if (dayoff == null || dayoff == "") {
          showMessage("Add day", 'A date must be selected', 'error');
        } else {
          $(this).dialog("close");
          var dayOffDate = new Date(
              dayoff);
          var createDayOffRequest = rpcClient.daysOffService.create(dayOffDate, "VACATION", projectId);
          if (createDayOffRequest.result == 0) {
            var dayOff = createDayOffRequest.data;
            var theDate = new Date(
                dayOff.dayOff.time);
            var li = $('<li></li>').addClass('ui-state-default').attr('id', dayOff.dayoffId).text(fmt.format(theDate));

            $("#daysOffList").append(li);
          } else {
            showMessage("Add day", 'error:' + createDayOffRequest.message, 'error');
          }
        }

      },
      Cancel : function() {
        $(this).dialog("close");
      }
    },
    close : function() {
    }
  });

  $("#addDayOffBtn").button({
    text : true
  }).click(function() {
    $("#addDaysOffDialog").dialog("open");
  });

  var salectedDayOff = {};
  $("#deleteDayOffBtn").button({
    text : true
  }).click(function() {
    if (salectedDayOff.selection) {
      var buttons = {
        Yes : function() {
          $(this).dialog("close");
          var delDayCall = rpcClient.daysOffService.remove(salectedDayOff.selection.id);
          if (delDayCall && delDayCall.result == 0) {
            // success, reload
            salectedDayOff.selection.remove();
            salectedDayOff.selection = null;
            $('#deleteDayOffBtn').prop("disabled", true);
          } else {
            showMessage("Delete Day", 'Error:' + result.message, 'error');
          }
        },
        No : function() {
          $(this).dialog("close");
          return false;
        }
      }
      showMessage('Delete Vacation Day', 'The selected vacation day will be deleted permanently!', 'warning', buttons);

    }
  });

  $('#deleteDayOffBtn').prop("disabled", true);

  $("#daysOffList").selectable();

  $('#daysOffList').selectable({
    selected : function(event,
        ui) {
      salectedDayOff.selection = ui.selected;
      $('#deleteDayOffBtn').prop("disabled", false);
    },
    unselected : function(event,
        ui) {
      $('#deleteDayOffBtn').prop("disabled", true);
      salectedDayOff.selection = null;
    }
  });

  var windowResizeFunc = function() {
    $('#projectTabs').height($('#main').height() - $('#tasksGantt').height())
  };
  $(window).resize(windowResizeFunc);
  windowResizeFunc();

  // Calendar Section end
});

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

    var li = $('<li></li>').addClass('ui-state-default').attr('id', data[i].taskId).text(data[i].taskName);

    li.attr('title', data[i].taskName);
    li.attr('description', data[i].taskDescription);

    $("#mainAllTasks").append(li);
    // $("#allTasks").append(li);
  }

}

function setSelectedOption(select,
    val) {
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

  var mthNames = [
      "Jan",
      "Feb",
      "Mar",
      "Apr",
      "May",
      "Jun",
      "Jul",
      "Aug",
      "Sep",
      "Oct",
      "Nov",
      "Dec"
  ];
  var dayNames = [
      "Sun",
      "Mon",
      "Tue",
      "Wed",
      "Thu",
      "Fri",
      "Sat"
  ];
  var zeroPad = function(number) {
    return ("0" + number).substr(-2, 2);
  };

  var dateMarkers = {
    d : [
        'getDate',
        function(v) {
          return zeroPad(v);
        }
    ],
    m : [
        'getMonth',
        function(v) {
          return zeroPad(v + 1);
        }
    ],
    n : [
        'getMonth',
        function(v) {
          return mthNames[v];
        }
    ],
    w : [
        'getDay',
        function(v) {
          return dayNames[v];
        }
    ],
    y : [
      'getFullYear'
    ],
    H : [
        'getHours',
        function(v) {
          return zeroPad(v);
        }
    ],
    M : [
        'getMinutes',
        function(v) {
          return zeroPad(v);
        }
    ],
    S : [
        'getSeconds',
        function(v) {
          return zeroPad(v);
        }
    ],
    i : [
      'toISOString'
    ]
  };

  this.format = function(date) {
    var dateTxt = this.formatString.replace(/%(.)/g, function(m,
        p) {
      var rv = date[(dateMarkers[p])[0]]();

      if (dateMarkers[p][1] != null)
        rv = dateMarkers[p][1](rv);

      return rv;

    });

    return dateTxt;
  };

}

function isDependentTask(taskId,
    task) {
  var dependencies = task.taskDependencies2.list;
  for (var i = 0; i < dependencies.length; i++) {
    if (dependencies[i].projectTask1.taskId == taskId) {
      return true;
    }
  }
  return false;
}

function roundToThreePlaces(num) {
  return +(Math.round(num + "e+3") + "e-3");
}

$('#mainAllTasks').on('click', function(e) {
  e.preventDefault();

  // This removes the class on selected li's
  $("#mainAllTasks li").removeClass("select");

  // adds 'select' class to the parent li of the clicked element
  // 'this' here refers to the clicked a element
  $(this).closest('li').addClass('select');

  // sets the input field's value to the data value of the clicked a element
  $('#mainAllTasks').val($(this).data('value'));
});