var allProjects = rpcClient.projectService.findAllLight();
var dateFormatter = new DateFmt("%d/%m/%y");

var prepareProjectData = function(search) {
	if (search) {
		search = search.toLowerCase();
	}
	var result = [];
	if (allProjects && allProjects.data && allProjects.data.list) {
		var list = allProjects.data.list;
		for ( var i=0;i<list.length;i++) {
			var proj = list[i];
			var name = proj.projectName;
			var code = proj.projectCode;
			if (name && code) {
				var include = true;
				if (search) {
					if (!search.indexOf(name.toLowerCase())
							&& !search.indexOf(code.toLowerCase())) {
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
						proposedStartDate:formattedDate
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

})