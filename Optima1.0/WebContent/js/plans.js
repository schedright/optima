$('#titleDiv').html('Capital Plan');

var context = {};
context.dependencyActionsStack = {
    add : [],
    remove : []
  };

var planRet = rpcClient.projectService.getPlan();
var planDates = rpcClient.projectService.getPlanDates();
var plansList = planRet.data.map;
var errorMessage = planRet.data.map.errors;

$(function() {
  var projects = plansList.data.list;
  if (errorMessage) {
    setTimeout(function() {
      showMessage("Project Error", '<p>Error getting project details for</p>' + errorMessage, 'error');
    }, 0);
  }
  var allYears = [];
  /*
   * for (var i=0;i<projects.length;i++) { var proj = projects[i].map; var yearsMap = proj.Details.map; for (var k in yearsMap) { if (yearsMap[k]) { if
   * (allYears.indexOf(k)==-1) { allYears.push(k); } } } }
   */

  var dateParts = planDates.data.map.plan_start.split("/");
  var yS = parseInt(dateParts[2]);
  var dateParts2 = planDates.data.map.plan_end.split("/");
  var yE = parseInt(dateParts2[2]);

  $('#refreshPlanBtn').prop('disabled', true);

  allYears.sort();
  var pColumns = [
    {
      id : "Project",
      name : "Project",
      field : "Project",
      minWidth : 120
    }
  ];
  var month = [
      '',
      'Jan',
      'Feb',
      'Mar',
      'Apr',
      'May',
      'Jun',
      'Jul',
      'Aug',
      'Sep',
      'Oct',
      'Nov',
      'Dec'
  ];
  var sm = parseInt(dateParts[0]);
  var em = parseInt(dateParts2[0]);
  for (var i = yS; i < yE + 1; i++) {
    var E = 12;
    if (i == yE) {
      E = em;
    }
    for (var m = sm; m < E + 1; m++) {
      allYears.push(month[m] + ' ' + i);
    }
    sm = 1;
  }

  for (var i = 0; i < allYears.length; i++) {
    pColumns.push({
      id : allYears[i],
      name : allYears[i],
      field : allYears[i],
      minWidth : 120
    });
  }
  pColumns.push({
    id : "Total",
    name : "Total",
    field : "Total",
    minWidth : 120
  });
  var pData = [];
  var getTotal = function(map,
      d) {
    var parts = d.split(' ');
    var y = parts[1];
    var m = parts[0];
    if (map && map[y] && map[y].map && map[y].map[m]) {
      return map[y].map[m];
    }
    return 0;
  };
  var totalsRow = {};
  var allTotal = 0;
  for (var i = 0; i < projects.length; i++) {
    var proj = projects[i].map;
    if (!proj.Details) {
      continue;
    }
    var yearsMap = proj.Details.map;

    var row = {
      "Project" : proj.Project.projectCode
    };
    var t = 0;
    for (var p = 0; p < allYears.length; p++) {
      var payment = getTotal(yearsMap, allYears[p]);
      if (payment) {
        t += payment;
        row[allYears[p]] = parseFloat(payment).toFixed(2);
        if (!totalsRow[allYears[p]]) {
          totalsRow[allYears[p]] = 0;
        }
        totalsRow[allYears[p]] += payment;
      }
    }
    row["Total"] = parseFloat(t).toFixed(2);
    allTotal += t;
    pData.push(row);
  }
  for ( var val in totalsRow) {
    if (typeof totalsRow[val] === 'number') {
      totalsRow[val] = parseFloat(totalsRow[val]).toFixed(2)
    }
  }

  totalsRow["Project"] = "Total";
  totalsRow["Total"] = parseFloat(allTotal).toFixed(2);
  pData.push(totalsRow);

  var pGrid = new Slick.Grid(
      "#projectPayments",
      pData,
      pColumns,
      {
        editable : true,
        enableCellNavigation : true,
        enableColumnReorder : true
      });
  var windowResizeFunc = function() {
    $(".scoll-container").height($('#tasksGantt').height() - 41)
    pGrid.resizeCanvas();
  }; 

  var gantInitialized = false;

  $('#planTabs').tabs({
    activate : function(e,
        ui) {
      if (ui.newTab.index()==1) {
        pGrid.resizeCanvas();
      } else if (ui.newTab.index()==2) {
        if (!gantInitialized) {
          initializeProjectGantt();
          gantInitialized = true;
        }
      }
      $.cookie('plan-selected-tab', ui.newTab.index(), {
        path : '/'
      });
    },
    active : $.cookie('plan-selected-tab')
  });

  if ($.cookie('plan-selected-tab') == 2) {
    initializeProjectGantt();
    gantInitialized = true;
  }

  $(window).resize(windowResizeFunc);
  windowResizeFunc();

  $('input.datepicker').datepicker({
    showOn : "button",
    buttonImage : "images/calendar.png",
    buttonImageOnly : true
  });


  if (planDates.result == 0) {
    var data = planDates.data.map;
    var sd = data.plan_start;
    var ed = data.plan_end;

    $("#pStartDateTxt").val(sd);
    $("#pFinishDateTxt").val(ed);
  }

  $('#savePlanDatesBtn').on('click', function() {
    var sd = $("#pStartDateTxt").val();
    var ed = $("#pFinishDateTxt").val();
    var ret = rpcClient.projectService.savePlanDates(sd, ed);
    if (ret.result != 0) {
    }
    
    if (context.dependencyActionsStack && context.dependencyActionsStack.remove) {
      for (var x = 0; x < context.dependencyActionsStack.remove.length; x++) {
        var tid = context.dependencyActionsStack.remove[x];
        try {
          var remDepCall = rpcClient.projectService.changePlanProject(tid,false);
          if (remDepCall.result != 0) {
            depFailed = true;
          }
        } catch (e) {
          depFailed = true;
        }
      }
    }

    if (context.dependencyActionsStack && context.dependencyActionsStack.add) {
      for (var x = 0; x < context.dependencyActionsStack.add.length; x++) {
        var tid = context.dependencyActionsStack.add[x];
        try {
          var remDepCall = rpcClient.projectService.changePlanProject(tid, true);
          if (remDepCall.result != 0) {
            depFailed = true;
          }
        } catch (e) {
          depFailed = true;
        }
      }
    }

    location.reload();
  });

  var allProjectsCall = rpcClient.projectService.findAllLight();
  var includedProject = rpcClient.projectService.findPlanProjectIds();
  if (allProjectsCall.result == 0 && includedProject.result == 0) {

    var inm = {};
    var includedProjects = includedProject.data.list;
    for (var i = 0; i < includedProjects.length; i++) {
      var proj = includedProjects[i];
      inm[proj.projectId] = true;
    }

    var allProjects = allProjectsCall.data.list;
    for (var i = 0; i < allProjects.length; i++) {
      var proj = allProjects[i];

      var li = $('<li></li>').addClass('ui-state-default').attr('id', proj.projectId).text(proj.projectCode);
      li.attr('title', proj.projectCode);

      if (inm[proj.projectId]) {
        $("#includedProjects").append(li);
      } else {
        $("#allProjects").append(li);
      }
    }

    $("#allProjects, #includedProjects").sortable({
      connectWith : ".sortable",
      item : '> li:not(.ui-state-disabled)',
      dropOnEmpty : true,

    }).disableSelection();

    $("#allProjects").droppable({
      accept : "#includedProjects li",
      hoverClass : "ui-state-hover",
      drop : function(ev,
          ui) {
        var tid = ui.draggable.attr("id");
        if (context.dependencyActionsStack.add.indexOf(tid) == -1) {
          context.dependencyActionsStack.remove.push(tid)
        } else {
          context.dependencyActionsStack.add.splice(context.dependencyActionsStack.add.indexOf(tid), 1);
        }
        return true;

      /*  var res = rpcClient.projectService.changePlanProject(ui.draggable.attr("id"), false);
        $('#refreshPlanBtn').prop('disabled', false);
        return res.result == 0;*/
      }
    });

    $("#includedProjects").droppable({
      accept : "#allProjects li",
      hoverClass : "ui-state-hover",
      drop : function(ev,
          ui) {
        var tid = ui.draggable.attr("id");
        if (context.dependencyActionsStack.remove.indexOf(tid) == -1) {
          context.dependencyActionsStack.add.push(tid)
        } else {
          context.dependencyActionsStack.remove.splice(context.dependencyActionsStack.remove.indexOf(tid), 1);
        }
        return true;
        
/*        var res = rpcClient.projectService.changePlanProject(ui.draggable.attr("id"), true);
        $('#refreshPlanBtn').prop('disabled', false);
        return res.result == 0;
*/      }
    });

  }

  $('#refreshPlanBtn').on('click', function() {
    location.reload();
  });

})
