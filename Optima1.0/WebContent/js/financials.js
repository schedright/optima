$(function() {
	document.title = 'SchedRight - Financial Details';
	var grid;
  var portfolioId = 0;
  var projectId = 0;
  
  $("#accordion").accordion();

  for ( var i in getURLVariables()) {
    if (i == "portfolioId") {
      portfolioId = getURLVariables()[i];
    } else if (i == "projectId") {
      projectId = getURLVariables()[i];
    }
  }
  
  if (portfolioId) {
    rpcClient.portfolioService.findLight(function(result , exception) {
      if (result.result == 0) {
        $('#titleDiv').html("Portfolio: " + result.data.portfolioName);
      }
    } , portfolioId);
  } else if (projectId) {
    rpcClient.projectService.findLight(function(result , exception) {
      if (result.result == 0) {
        if (result.data.portfolio) {
          $('#titleDiv').html("Portfolio: " + result.data.portfolio.portfolioName);
        } else {
          $('#titleDiv').html("Project: " + result.data.projectName);
        }
      } 
    } , projectId);

  } else {
    $('#titleDiv').html('Financial Results');
  }

	var solutionResponse = rpcClient.portfolioService.getSolution(portfolioId,projectId);

	if (solutionResponse.result == 0 && solutionResponse.data
			&& solutionResponse.data.list && solutionResponse.data.list.length) {
    var ul = "<ul>";

	  for (var i = 0; i < solutionResponse.data.list.length; i++) {
      var projDetails = solutionResponse.data.list[i].map;
      ul += "<li><a href=\"#proj" + i + "\">" +projDetails.name + "</a></li>";
    }
	  ul += "</ul>";
	  $("#schedResults").append(ul);
	  for (var i = 0; i < solutionResponse.data.list.length; i++) {
	    var tab = $('<div style="padding-top:60px" id="proj' + i + '"></div>').appendTo("#schedResults");
//	    var tab = $("#schedResults").append('<div id="proj' + i + '"></div>');
			var projDetails = solutionResponse.data.list[i].map;
			tab
					.append(
							'<div id="TasksGrid_' + i
									+ '" style="height:300px"></div>');
			tab.append('<br/>');// separator
			tab.append(
					'<div id="ProjectNumGrid_' + i
							+ '" style="height:80px"></div>');
			tab.append('<br/>');// separator

			var tasksColumns = [];
			tasksColumns.push({
				id : "name" + i,
				name : "Task Name",
				field : "map",
				formatter : function(row, cell, value, columnDef, dataContext) {
					return value.name;
				},
				minWidth : 200
			});

			if (projDetails.end_final) {
				tasksColumns
						.push({
							name : "Change",
							field : "map",
							formatter : function(row, cell, value, columnDef,
									dataContext) {
								if (value.final == value.original) {
									return "<div style=\"width:16px;height:16px\" class=\"notShiftedTaskLogo\"></div>"
								} else {
									return "<div style=\"width:16px;height:16px\" class=\"shiftedTaskInLogo\"></div>"
								}
							},
							minWidth : 200
						});
			}

			tasksColumns.push({
				id : "org" + i,
				name : "Original Start",
				field : "map",
				formatter : function(row, cell, value, columnDef, dataContext) {
					return value.original;
				},
				minWidth : 200
			});

			if (projDetails.end_final) {
				tasksColumns.push({
					name : "Final Start",
					field : "map",
					formatter : function(row, cell, value, columnDef,
							dataContext) {
						return value.final;
					},
					minWidth : 200
				});
			}
			var tasksData = [ {} ];

			var tasksGrid = new Slick.Grid("#TasksGrid_" + i,
					projDetails.tasks.list, tasksColumns, {
						editable : false,
						enableAddRow : false,
						enableCellNavigation : true,
						enableColumnReorder : false
					});

			var projectCol = [ {
				id : "pn" + i,
				name : projDetails.name,
				field : "name",
				minWidth : 200
			} ];
			if (projDetails.end_final) {
				projectCol
						.push({
							id : "pch" + i,
							name : "Change",
							field : "diff",
							formatter : function(row, cell, value, columnDef,
									dataContext) {
								if (value) {
									if (row==0) {
										return "<div style=\"width:16px;height:16px\" class=\"decreasetProjectProfitLogo\"></div>"
									} else {
										return "<div style=\"width:16px;height:16px\" class=\"shiftedTaskInLogo\"></div>"
										
									}
									
								} else {
									if (row==0) {
										return "<div style=\"width:16px;height:16px\" class=\"sameProjectProfitLogo\"></div>"
									} else {
										return "<div style=\"width:16px;height:16px\" class=\"notShiftedTaskLogo\"></div>"
										
									}
								}
							},
							minWidth : 200
						});
			}
			projectCol.push({
				id : "porg" + i,
				name : "Original",
				field : "original",
				minWidth : 200
			});

			if (projDetails.end_final) {
				projectCol.push({
					id : "pfin" + i,
					name : "Final",
					field : "final",
					minWidth : 200
				});
			}

			var projData = [ {
				name : "Profit",
				original : projDetails.profit_original,
				final : projDetails.profit_final,
				diff : projDetails.profit_original!=projDetails.profit_final
			}, {
				name : "Finish Date",
				original : projDetails.end_original,
				final : projDetails.end_final,
				diff : projDetails.end_original!=projDetails.end_final
			} ];
			
			var projectsGrid = new Slick.Grid("#ProjectNumGrid_" + i,
					projData, projectCol, {
						editable : false,
						enableAddRow : false,
						enableCellNavigation : true,
						enableColumnReorder : false
					});			
		}
	  $("#schedResults").tabs();

		// $("#schedResults").html('');
		/*
		 * if (solutionResponse.message=='Not Solved') {
		 * $("#exportSolutionToCSV").attr('disabled','true'); } else {
		 * $("#exportSolutionToCSV").removeAttr('disabled'); }
		 * 
		 * $("#schedResults").append(solutionResponse.data);
		 */
	}

	$("#exportSolutionToCSV").button({
		icons : {
			primary : "ui-icon-calculator"
		},
		text : true
	}).click(
			function() {
				var solutionResponse = rpcClient.portfolioService
						.getSolutionAsCSV(portfolioId,projectId);
				if (solutionResponse.result == 0 && solutionResponse.data) {
					var blob = new Blob([ solutionResponse.data ], {
						type : "text/plain;charset=utf-8"
					});
					var date = new Date();
					saveAs(blob, "Solution" + date + ".csv");
				} else {
					showMessage("Export to CSV",
							'Error: Cannot find a valid solution.', 'error');
				}

			});

    setTimeout(function() {
    	var solutionResponse = rpcClient.portfolioService.hasSolution(portfolioId,projectId);
    	if (solutionResponse.result==0 && solutionResponse.data && solutionResponse.data=='TRUE') {
    		var result = rpcClient.portfolioService.isInvalidSolution(portfolioId,projectId);
    		if (result) {
    			showMessage(
    					'Portfolio has changed',
    					'The portfolio has changed after last solve, you might need to re-solve again',
    					'info');

    		}
    	}
    },0);

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
