$(function() {
  document.title = 'SchedRight - Financial Periods';

  var portfolioId = 0;
  var projectId = 0;

  var allProjects = null;
  for ( var i in getURLVariables()) {
    if (i == "portfolioId") {
      portfolioId = getURLVariables()[i];
    } else if (i == "projectId") {
      projectId = getURLVariables()[i];
    }
  }

  if (portfolioId) {
    var result = rpcClient.portfolioService.findLight(portfolioId);
    if (result.result == 0) {
      $('#titleDiv').html("Portfolio: " + result.data.portfolioName);
      allProjects = result.data.projects.list;
    }
  } else if (projectId) {
    var result = rpcClient.projectService.findLight(projectId);

    if (result.result == 0) {
      if (result.data.portfolio) {
        allProjects = result.data.portfolio.projects.list;
        $('#titleDiv').html("Portfolio: " + result.data.portfolio.portfolioName);
      } else {
        allProjects = [
          result.data
        ];
        $('#titleDiv').html("Project: " + result.data.projectName);
      }
    }

  } else {
    $('#titleDiv').html('Constraint/Schedule');
  }

  var enableButtons = function(enable) {
    $('#deleteFinanceBtn').prop("disabled", !enable);
  }
  $("#financeList").selectable();
  var selected = {};

  $('#financeList').selectable({
    selected : function(event,
        ui) {
      selected.selection = ui.selected;
      enableButtons(true);

      /*
       * selected.selection = ui.selected; var user = allUsersMap[ui.selected.textContent]; $("#editUserBtn").html( user.isAdmin ? 'Remove Admin' : 'Make
       * Admin'); enableButtons(true);
       */
    },
    unselected : function(event,
        ui) {
      enableButtons(false);
      selected.selection = null;
    }
  });

  $('#financeDate').datepicker({
    showOn : "button",
    buttonImage : "images/calendar.png",
    buttonImageOnly : true
  });

  $("#addFinanceBtn").button({
    text : true
  }).click(function() {
    $("#addFinanceDialog").dialog("open");
  });

  $("#deleteFinanceBtn").button({
    text : true
  }).click(function() {
    var buttons = {
      Yes : function() {
        $(this).dialog("close");
        var result = rpcClient.financeService.remove(selected.selection.id)
        if (result && result.result == 0) {
          // success, reload
          location.reload();
        } else {
          showMessage("Delete Overdraft Constraint", 'Error:' + result.message, 'error');
        }
      },
      No : function() {
        $(this).dialog("close");
        return false;
      }
    }
    showMessage('Delete Overdraft Constraint', 'The selected finance will be deleted permanently!', 'warning', buttons);

  });

  var result = rpcClient.financeService.findAllByPortfolio(portfolioId, projectId);
  if (result.result == 0) {
    var fmt = new DateFmt(
        "%w %d-%n-%y");
    var financesList = result.data.list;
    var dateFormatter = new DateFmt(
        "%d/%m/%y");

    for (var i = 0; i < financesList.length; i++) {
      var theDate = new Date(
          financesList[i].financeUntillDate.time);

      var startDate = new Date(
          financesList[i].financeUntillDate.time);
      formattedDate = dateFormatter.format(startDate);
      var title = formattedDate + "  -  " + financesList[i].financeAmount + "$ @ " + financesList[i].interestRate + "%";
      var li = $('<li></li>').addClass('ui-state-default').attr('id', financesList[i].financeId).text(title);
      li.attr('title', title);

      $("#financeList").append(li);
    }
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

  function formatter(row,
      cell,
      value,
      columnDef,
      dataContext) {
    return value;
  }

  function dateExistes(date,
      array) {
    return false;
    /*
     * for (var i = 0; i < array.length; i++) { // alert('date1 - date2: ' + date - array[i].tag); if (array[i].tag - date === 0) return true; } return false;
     */
  }

  $("#addFinanceDialog").dialog({
    autoOpen : false,
    height : 350,
    width : 300,
    modal : true,
    show : {
      effect : "blind",
      duration : 300
    },
    hide : {
      effect : "fade",
      duration : 300
    },
    buttons : {
      "Add Overdraft Constraint" : function() {
        var bValid = true;
        var dateExists = false;
        $("#financeAmount").removeClass("ui-state-error");
        $("#interestRate").removeClass("ui-state-error");
        $("#financeDate").removeClass("ui-state-error");

        var financeAmount = $("#financeAmount").val();
        var interestRate = $("#interestRate").val();
        var financeDate = $("#financeDate").val();

        if (financeDate != null && financeDate.length != 0) {
          financeDate = new Date(
              financeDate);
          if (dateExistes(financeDate)) {
            $("#financeDate").addClass("ui-state-error");
            dateExists = true;
          }
        } else {
          $("#financeDate").addClass("ui-state-error");
          bvalid = false;
        }
        if (financeAmount == null || financeAmount.length == 0 || !/^-?\d*\.?\d*$/.test(financeAmount)) {
          $("#financeAmount").addClass("ui-state-error");
          bValid = false;
        }
        if (interestRate == null || interestRate.length == 0 || !/^-?\d*\.?\d*$/.test(interestRate)) {
          $("#interestRate").addClass("ui-state-error");
          bValid = false;
        } else {
          var num = parseFloat(interestRate);
          if (num < 0 || num > 100) {
            $("#interestRate").addClass("ui-state-error");
            bValid = false;
          }
        }

        if (dateExists) {
          showMessage("Add Overdraft Constraint", "The entered date exists already in the finaces grid", 'error');
          return;
        }

        if (bValid) {
          var createFinanceResult = rpcClient.financeService.create(portfolioId, projectId, financeAmount, interestRate, financeDate);

          if (createFinanceResult.result == 0) {
            location.reload(true);
          } else {
            showMessage("Create Payment", 'Error:' + createTaskResult.message, 'error');
          }
        } else {
          showMessage("Create Payment", "Error: Please check your input", 'error');
        }
      },
      Cancel : function() {
        $(this).dialog("close");
      }
    },
    close : function() {
    }
  });

  enableButtons(false);

  $("#mainAllProjects").sortable({
    revert : true,
    items : "li"
  });

  // var projectsList = rpcClient.projectService.findAllByPortfolio(portfolioId);
  if (allProjects) {
    // var projectsData = projectsList.data.list;

    fillProjectList(allProjects);
  }
  var solutionResponse = rpcClient.portfolioService.hasSolution(portfolioId, projectId);
  if (solutionResponse.result == 0 && solutionResponse.data && solutionResponse.data == 'TRUE') {
    $("#currentSolution").css('display', '');
    $("#schedResults").html('');
    $("#schedResults").append("<p style='margin-left:65px'><a href='financials.jsp?portfolioId=" + portfolioId + "'>You currently have a solution check Results and Cash Flow.</a></p>");

  } else {
    $("#currentSolution").css('display', 'none');
  }

  $("#mainAllProjects").droppable({
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

  $("#findFinalSolBtn").button({
    text : true
  }).click(function() {
    var displayed = $("#currentSolution").css('display');
    if (displayed == 'none') {
      solveIt(portfolioId, projectId);
    } else {

      var buttons = {
        Yes : function() {
          $(this).dialog("close");
          solveIt(portfolioId, projectId);
          return;
        },
        No : function() {
          $(this).dialog("close");
          return;
        }
      }
      showMessage('Solve', 'The portfolio was already solved before. Do you want to solve it agin?', 'info', buttons);
    }
  });

  $("#exportSolutionToCSV").button({
    icons : {
      primary : "ui-icon-calculator"
    },
    text : true
  }).click(function() {
    var solutionResponse = rpcClient.portfolioService.getSolutionAsCSV(portfolioId);
    if (solutionResponse.result == 0 && solutionResponse.data) {
      var blob = new Blob(
          [
            solutionResponse.data
          ],
          {
            type : "text/plain;charset=utf-8"
          });
      var date = new Date();
      saveAs(blob, "Solution" + date + ".csv");
    } else {
      showMessage("Export to CSV", 'Error: Cannot find a valid solution.', 'error');
    }

  });
});

function solveIt(portfolioId,
    projectId) {
  var projectsAccordingToPriority = $("#mainAllProjects").find('li');
  var projectID = projectsAccordingToPriority[0].id;
  var priorityOrder = "";
  var separator = "";
  for (var i = 0; i < projectsAccordingToPriority.length; i++) {
    priorityOrder = priorityOrder + separator + projectsAccordingToPriority[i].id;
    separator = ",";
  }
  window.portfolioId = portfolioId;
  window.projectId = projectId;
  rpcClient.projectService.getSolution(function(result,
      exception) {
    if (result.result == 0 && result.message == 'Success' && result.data == 'running') {
      // start dialog with progress
      window.htmlElement = showMessageWithProgress("Schedule", "Scheduling ...", 'Info');
      window.intervalId = self.setInterval(function() {
        if (window.htmlElement.parent().length == 0) {
          window.clearInterval(window.intervalId);
        } else {
          var result = rpcClient.projectService.getStatus(window.portfolioId, window.projectId);
          try {
            var bar = window.htmlElement[0].children[0].children[0].children[0].children[1].children[0].children[0].children[0];
            window.htmlElement[0].children[0].children[0].children[0].children[1].children[0].children[0].children[0];
            var jsn = jQuery.parseJSON(result.data);
            var ratio = jsn.DONE * 100 / jsn.TOTAL;
            bar.style.width = ratio + "%";
            if (jsn.STATUS == 'SAVING') {
              window.htmlElement[0].children[0].children[0].children[0].children[0].children[1].children[0].innerHTML = "Saving tasks ( " + jsn.DONE + " / " + jsn.TOTAL + " )";
            } else if (jsn.STATUS == 'RUNNING') {
              window.htmlElement[0].children[0].children[0].children[0].children[0].children[1].children[0].innerHTML = "Scheduling ( " + jsn.DONE + " / " + jsn.TOTAL + " )";
            } else if (jsn.STATUS == 'Success') {
              window.htmlElement[0].children[0].children[0].children[0].children[0].children[1].children[0].innerHTML = "Scheduling is done successfully";
              window.clearInterval(window.intervalId);
            } else if (jsn.STATUS == 'FAILED') {
              window.htmlElement[0].children[0].children[0].children[0].children[0].children[1].children[0].innerHTML = "Failed to Schedule";
              window.htmlElement[0].children[0].children[0].children[0].children[0].children[1].children[1].innerHTML = jsn.ERROR_MESSAGE;
              window.clearInterval(window.intervalId);
              bar.style.background = 'red';
            }
          } catch (e) {
            try {
              window.htmlElement[0].children[0].children[0].children[0].children[0].children[1].children[0].innerHTML = "Error updating the status";
              window.clearInterval(window.intervalId);
            } catch (e2) {
            }
          }
        }
      }, 1000);
    } else if (result.result == 0 && result.message == 'Busy') {
      showMessage("Schedule", 'Error:' + result.data, 'error');
    }
    /*
     * showMessage("Schedule",'Error:' + result.message,'error');
     * 
     * $("#currentSolution").css('display',''); $("#schedResults").html(''); $("#schedResults").append("<p style='margin-left:65px'><a
     * href='financials.jsp?portfolioId=" + portfolioId +"'>You currently have a solution check Results and Cash Flow.</a></p>"); showMessage("Schedule",'Portfolio solved
     * successfully.','success'); } else { showMessage("Schedule",'Error:' + result.message,'error'); $("#currentSolution").css('display','none'); }
     */
  }, projectID, "", priorityOrder);
};

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

function fillProjectList(data) {
  $("#mainAllProjects").html('');
  for (var i = 0; i < data.length; i++) {
    var li = $('<li></li>').addClass('ui-state-default').attr('id', data[i].projectId).text(data[i].projectCode);
    li.attr('title', (data[i].projectCode));
    li.attr('description', (data[i].projectDescription));

    $("#mainAllProjects").append(li);
  }

}
