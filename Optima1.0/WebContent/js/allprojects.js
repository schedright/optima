var allProjects = rpcClient.projectService.findAllLight();
var dateFormatter = new DateFmt("%d/%m/%y");

var prepareProjectData = function(search) {
	if (search) {
		search = search.toLowerCase();
	}
	var result = [];
	if (allProjects && allProjects.data && allProjects.data.list) {
		var list = allProjects.data.list;
		for (var i = 0; i < list.length; i++) {
			var proj = list[i];
			var name = proj.projectName;
			var code = proj.projectCode;
			if (name && code) {
				var include = true;
				if (search) {
					if (name.toLowerCase().indexOf(search) == -1
							&& code.toLowerCase().indexOf(search) == -1) {
						include = false;
					}
				}
				if (include) {
					var formattedDate = "";
					if (proj.propusedStartDate) {
						var startDate = new Date(proj.propusedStartDate.time);
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
			}
		}
	}
	return result;
}

var enableButtons = function(enable) {
	$('#editProject').prop("disabled", !enable);
	$('#deleteProject').prop("disabled", !enable);
}
enableButtons(false);

/*
 * $('#addUserBtn').on('click', function() { $("#addUserDialog").dialog("open");
 * });
 */
$(function() {
	var columns = [ {
		id : "projectName",
		name : "Project Name",
		field : "projectName",
		minWidth : 200
	}, {
		id : "projectCode",
		name : "Project Code",
		field : "projectCode",
		minWidth : 200
	}, {
		id : "proposedStartDate",
		name : "Start Date",
		field : "proposedStartDate",
		minWidth : 200
	}, {
		id : "portfolioName",
		name : "Portfolio Name",
		field : "portfolioName",
		minWidth : 200
	} ];

	var data = prepareProjectData();

	var pGrid = new Slick.Grid("#gridContainer", data, columns, {
		editable : true,
		enableAddRow : true,
		enableCellNavigation : true,
		enableColumnReorder : true
	});

	pGrid.setSelectionModel(new Slick.RowSelectionModel());
	pGrid.onClick.subscribe(function(e) {
		var cell = pGrid.getCellFromEvent(e);
		pGrid.setSelectedRows(cell.row);
		e.stopPropagation();
		enableButtons(true);
	});

	pGrid.onDblClick.subscribe(function(e, args) {
		var cell = pGrid.getCellFromEvent(e)
		var row = cell.row;
		var item = pGrid.getDataItem(row);
		if (item && item.proj && item.proj.projectId) {
			window.location.href = "projectDetails.jsp?projectId="
					+ item.proj.projectId;
		}
	});

	$("#searchBox").keyup(function(event) {
		if (event.keyCode == 13) {
			var search = $("#searchBox").val();
			var newData = prepareProjectData(search);
			pGrid.setData(newData);
			pGrid.invalidate();
		}
	});

	$('#addProject').on('click', function() {
		$("#createOrEditProjectDialog").data("project", null).dialog("open");
	});

	$('#editProject').on(
			'click',
			function() {
				var row = pGrid.getSelectedRows();
				if (row) {
					var item = pGrid.getDataItem(row);
					if (item) {
						var portId = -1;
						if (item.portfolio && item.portfolio.portfolioId) {
							portId = item.portfolio.portfolioId;
						}
						$("#createOrEditProjectDialog").data("project", item)
								.dialog("open");
					}
				}
			});
	$('#deleteProject')
			.on(
					'click',
					function() {
						var row = pGrid.getSelectedRows();
						if (row) {
							var item = pGrid.getDataItem(row);
							if (item && item.proj && item.proj.projectId) {
								var projectId = item.proj.projectId;
								var buttons = {
									Yes : function() {
										$(this).dialog("close");
										var call = rpcClient.projectService
												.remove(projectId);
										if (call.result == 0) {
											location.reload();
										} else {
											showMessage("Delete Project",
													'Error:' + result.message,
													'error');
										}
									},
									No : function() {
										$(this).dialog("close");
										return false;
									}
								}
								showMessage(
										'Delete Project',
										'The selected project will be deleted permanently!',
										'warning', buttons);
							}
						}
					});

	$("#createOrEditProjectDialog").dialog(
			{
				autoOpen : false,
				height : 300,
				width : 720,
				modal : true,
				show : {
					effect : "blind",
					duration : 1000
				},
				hide : {
					effect : "fade",
					duration : 1000
				},
				open : function() {

					var project = $(this).data("project");
					if (project && project.proj) {
						project = project.proj;
					}
					if (project != null) {

						$("#projectName").val(project.projectName);
						$("#projectCode").val(project.projectCode);
						$("#projectDescription")
								.val(project.projectDescription);

					} else {
						$("#projectName").val("");
						$("#projectCode").val("");
						$("#projectDescription").val("");
					}
				},
				buttons : {
					"Save" : function() {
						$("#projectName").removeClass("ui-state-error");
						$("#projectCode").removeClass("ui-state-error");
						$("#projectDescription").removeClass("ui-state-error");
						var bValid = true;
						bValid = bValid
								&& checkLength($("#projectName"),
										"projectName", 3, 32);
						bValid = bValid
								&& checkLength($("#projectCode"),
										"projectCode", 3, 32);
						if (bValid) {
							var project = $(this).data("project");
							if (project && project.proj) {
								project = project.proj;
							}
							var portfolioId = -1;
							var projectId = null;
							if (project && project.projectId) {
								projectId = project.projectId;
							}
							if (project && project.portfolio
									&& project.portfolio.portfolioId) {
								portfolioId = project.portfolio.portfolioId;
							}
							var call;
							if (projectId == null) {

								call = rpcClient.projectService.create($(
										"#projectName").val(),
										$("#projectCode").val(), $(
												"#projectDescription").val(),
										null, null, 0.0, 0.0, portfolioId, -1,
										0, 0, 0, 0, 0, 0);

							} else {
								call = rpcClient.projectService.updateShort(
										projectId, $("#projectName").val(), $(
												"#projectCode").val(), $(
												"#projectDescription").val(), portfolioId);
							}
							if (call.result == 0) {
								$(this).dialog("close");
								location.reload();
							} else {
								showMessage(
										projectId == null ? "Create project"
												: "Update project", 'Error:'
												+ call.message, 'error');
							}
						}
					},
					"Cancel" : function() {
						$(this).dialog("close");
					}
				}
			});
})