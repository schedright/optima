$('#titleDiv').html('Portfolio');
var allProjects = rpcClient.projectService.findAllLight();

var dateFormatter = new DateFmt(
    "%n %d, %y");

var prepareProjectData = function(projects) {
  var result = [];
  for (var i = 0; i < projects.length; i++) {
    var proj = projects[i];
    var formattedDate = "";
    if (proj.propusedStartDate) {
      var startDate = utcTime2LocalDate(
          proj.propusedStartDate.time);
      formattedDate = dateFormatter.format(startDate);
    }

    var item = {
      projectName : proj.projectName,
      projectCode : proj.projectCode,
      projectId : proj.projectCode,
      proposedStartDate : formattedDate,
      proj : proj
    };
    if (proj.portfolio && proj.portfolio.portfolioName) {
      item.portfolioName = proj.portfolio.portfolioName;
    }
    result.push(item);
  }
  return result;
}

var enableButtons = function(enable) {
  $('#removeProjectLink').prop("disabled", !enable);
}
enableButtons(false);

$(function() {
  var columns = [
      {
        id : "projectName",
        name : "Project Name",
        field : "projectName",
        minWidth : 200
      },
      {
        id : "projectCode",
        name : "Project Code",
        field : "projectCode",
        minWidth : 200
      },
      {
        id : "proposedStartDate",
        name : "Start Date",
        field : "proposedStartDate",
        minWidth : 200
      },
      {
        id : "portfolioName",
        name : "Portfolio Name",
        field : "portfolioName",
        minWidth : 200
      }
  ];

  var data = {};

  var pGrid = new Slick.Grid(
      "#gridContainer",
      data,
      columns,
      {
        editable : true,
        enableCellNavigation : true,
        enableColumnReorder : true,
        // autoHeight:true,
        forceFitColumns : true
      });

  $(window).resize(function() {
    pGrid.resizeCanvas();
  });

  pGrid.setSelectionModel(new Slick.RowSelectionModel());
  pGrid.onClick.subscribe(function(e) {
    var cell = pGrid.getCellFromEvent(e);
    pGrid.setSelectedRows(cell.row);
    e.stopPropagation();
    enableButtons(true);
  });

  pGrid.onDblClick.subscribe(function(e,
      args) {
    var cell = pGrid.getCellFromEvent(e)
    var row = cell.row;
    var item = pGrid.getDataItem(row);
    if (item && item.proj && item.proj.projectId) {
      var selectedProjectCookie = "";
      if (item.proj.portfolio) {
        selectedProjectCookie = "portfolio=" + item.proj.portfolio.portfolioId + "," + "project=" + item.proj.projectId;
      } else {
        selectedProjectCookie = "project=" + item.proj.projectId;
      }
      $.cookie('activeProject', selectedProjectCookie);
      window.location.href = "projectDetails.jsp?projectId=" + item.proj.projectId;
    }
  });

  $('#portfolioSelect').on('change', function(e) {
    var optionSelected = $("option:selected", this);
    if ($("option:selected", this) && $("option:selected", this).data()) {
      var portData = $("option:selected", this).data();

      $.cookie('saved_pfid', null);
      $.cookie('saved_pfid', portData.portfolioId);

      var selectedProjectCookie = "portfolio=" + optionSelected.data().portfolioId; 
      $.cookie('activeProject', selectedProjectCookie);
      
      window.updateLinks("portfolioId=" , optionSelected.data().portfolioId);
      
      var newData = prepareProjectData(portData.projects.list);
      pGrid.setData(newData);
      pGrid.invalidate();

    }
  });

  rpcClient.portfolioService.findAllLight(function(result,
      exception) {
    if (result.result == 0) {
      var data = result.data;
      if (data.list.length > 0) {
        var currentPortfolioId = isNaN(parseInt($.cookie('saved_pfid'))) ? 0 : parseInt($.cookie('saved_pfid'));
        var selectedItem = null;
        for (var i = 0; i < data.list.length; i++) {
          var name = data.list[i].portfolioName;
          var portId = data.list[i].portfolioId;

          if (portId == currentPortfolioId) {
            $('#portfolioSelect').append($("<option></option>").attr("portfolioId", portId).attr("selected", "selected").data(data.list[i]).text(name));
            selectedItem = data.list[i];
            window.updateLinks("portfolioId=" , selectedItem.portfolioId);

          } else {
            $('#portfolioSelect').append($("<option></option>").attr("portfolioId", portId).data(data.list[i]).text(name));
          }
        }
        if (!selectedItem && data.list.length) {
          selectedItem = data.list[0];
        }
        if (selectedItem) {
          var newData = prepareProjectData(selectedItem.projects.list);
          pGrid.setData(newData);
          pGrid.invalidate();
        }
      }
    }
  });

  $('#addPortfolio').on('click', function() {
    $("#newPortfolioDialog").dialog("open");
  });

  $('#editPortfolio').on('click', function() {
    var optionSelected = $("#portfolioSelect option:selected");
    if (optionSelected && optionSelected.data()) {
      var port = optionSelected.data();
      $("#newPortfolioDialog").data("portfolio", port).dialog("open");
    }

  });
  $('#deletePortfolio').on('click', function() {
    var optionSelected = $("#portfolioSelect option:selected");
    if (optionSelected && optionSelected.data()) {
      var port = optionSelected.data();
      var buttons = {
        Yes : function() {
          $(this).dialog("close");

          var call = rpcClient.portfolioService.remove(port.portfolioId);
          if (call.result == 0) {
            location.reload();
          } else {
            showMessage("Delete Project", 'Error:' + result.message, 'error');
          }
        },
        No : function() {
          $(this).dialog("close");
          return false;
        }
      }
      showMessage('Delete Portfolio', 'The selected Portfolio will be deleted permanently!', 'warning', buttons);

    }
  });

  $('#addProjectLink').on('click', function() {
    $("#linkToProjectDialog").dialog("open");
  });

  $('#removeProjectLink').on('click', function() {
    var row = pGrid.getSelectedRows();
    if (row) {
      var item = pGrid.getDataItem(row);
      var optionSelected = $("#portfolioSelect option:selected");
      if (item && item.proj && item.proj.projectId && optionSelected && optionSelected.data()) {
        var portfolioId = optionSelected.data().portfolioId;
        var projectId = item.proj.projectId;
        var buttons = {
          Yes : function() {
            $(this).dialog("close");
            var call = rpcClient.projectService.removePortfolio(projectId);
            if (call.result == 0) {
              location.reload();
            } else {
              showMessage("Remove project", 'Error:' + result.message, 'error');
            }
          },
          No : function() {
            $(this).dialog("close");
            return false;
          }
        }
        showMessage('Remove project', 'The selected project will be removed from this Portfolio and all projects will be reset so they have to be solved again! ', 'warning', buttons);
      }
    }
  });

  $("#newPortfolioDialog").dialog({
    autoOpen : false,
    height : 320,
    width : 450,
    modal : true,
    show : {
      effect : "blind",
      duration : 300
    },
    hide : {
      effect : "fade",
      duration : 300
    },
    open : function() {

      var portfolio = $(this).data("portfolio");
      if (portfolio) {

        $("#portName").val(portfolio.portfolioName);
        $("#portDescription").val(portfolio.portfolioDescreption);

      } else {
        $("#portName").val("");
        $("#portDescription").val("");
      }
    },
    buttons : {
      "Save" : function() {
        var bValid = true;
        var portName = $("#portName");
        var portDescription = $("#portDescription");

        bValid = bValid && checkLength(portName, "portName", 3, 32);
        bValid = bValid && checkLength(portDescription, "portDescription", 0, 1024);

        if (bValid) {
          $(this).dialog("close");

          var portfolio = $(this).data("portfolio");
          if (portfolio) {
            var createPortResult = rpcClient.portfolioService.update(portfolio.portfolioId, portName.val(), portDescription.val());
            if (createPortResult.result == 0) {
              location.reload();
            } else {
              showMessage("Edit Portfolio", 'Error:' + result.message, 'error');
            }

          } else {
            var createPortResult = rpcClient.portfolioService.create(portName.val(), portDescription.val());
            if (createPortResult.result == 0) {
              location.reload();
            } else {
              showMessage("Create Portfolio", 'Error:' + result.message, 'error');
            }

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

  $("#linkToProjectDialog").dialog({
    autoOpen : false,
    height : 240,
    width : 450,
    modal : true,
    show : {
      effect : "blind",
      duration : 300
    },
    hide : {
      effect : "fade",
      duration : 300
    },
    open : function() {
      var unlinkedProjects = rpcClient.projectService.getUnlinkedProjects();
      for (var i = 0; i < unlinkedProjects.data.list.length; i++) {
        var name = unlinkedProjects.data.list[i].projectName;
        var projId = unlinkedProjects.data.list[i].projectId;

        if (i == 0) {
          $('#projectsSelect').append($("<option></option>").attr("projectId", projId).attr("selected", "selected").data(unlinkedProjects.data.list[i]).text(name));
        } else {
          $('#projectsSelect').append($("<option></option>").attr("projectId", projId).data(unlinkedProjects.data.list[i]).text(name));
        }
      }
    },

    buttons : {
      "Link" : function() {
        var projOptionSelected = $("#projectsSelect option:selected");
        var portOptionSelected = $("#portfolioSelect option:selected");

        if (projOptionSelected && projOptionSelected.data() && portOptionSelected && portOptionSelected.data()) {
          $(this).dialog("close");

          var project = projOptionSelected.data();
          var projectId = project.projectId;

          var port = portOptionSelected.data();
          var portfolioId = port.portfolioId;

          var buttons = {
              Yes : function() {
                $(this).dialog("close");
                call = rpcClient.projectService.updateShort(projectId, project.projectName, project.projectCode, project.projectDescription, portfolioId);
                if (call.result == 0) {
                  location.reload();
                } else {
                  showMessage("Link Project", 'Error:' + call.message, 'error');
                }
                return;
              },
              No : function() {
                $(this).dialog("close");
                return;
              }
            }
            showMessage('Link Project', 'All projects will be rest, you have to reschedule', 'info', buttons);

        } else {
          $("#portfolioSelect").addClass("ui-state-error");
        }
      },
      Cancel : function() {
        $(this).dialog("close");
      }
    },
    close : function() {
    }
  });

})