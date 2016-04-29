$(function() {
  document.title = 'SchedRight - Financial Periods';

  var portfolioId = 0;
  var projectId = 0;

  for ( var i in getURLVariables()) {
    if (i == "portfolioId") {
      portfolioId = getURLVariables()[i];
    } else if (i == "projectId") {
      projectId = getURLVariables()[i];
    }
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
          showMessage("Delete Finance", 'Error:' + result.message, 'error');
        }
      },
      No : function() {
        $(this).dialog("close");
        return false;
      }
    }
    showMessage('Delete Finance', 'The selected finance will be deleted permanently!', 'warning', buttons);

  });

  var result = rpcClient.financeService.findAllByPortfolio(portfolioId);
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
      var title = formattedDate + "  -  " + financesList[i].financeAmount + "$ @ " + "%";
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
      "Add Finance" : function() {
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
          if (num<0 || num>100) {
            $("#interestRate").addClass("ui-state-error");
            bValid = false;
          }
        }
        
        if (dateExists) {
          showMessage("Add Finance", "The entered date exists already in the finaces grid", 'error');
          return;
        }

        if (bValid) {
          var createFinanceResult = rpcClient.financeService.create(portfolioId, financeAmount, financeDate);

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
});
