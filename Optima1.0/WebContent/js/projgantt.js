function getGanttSource() {
	var tasksSource = [];
	var projects = daysOffList.data.list;
	var fmt = new DateFmt("%d-%m-%y");

	for (var i=0;i<projects.length;i++) {
		var proj = projects[i].map;
		var startDate = proj.Start;
		var endDate = proj.End;
		
		var title  =proj.Project.projectName + '&#013;' + proj.Project.projectDescription + '&#013;' + fmt.format(new Date(startDate.time)) + " - " + fmt.format(new Date(endDate.time));
		tasksSource.push({
			name : proj.Project.projectName,

			desc : proj.Project.projectDescription,

			values : [ {

				from : "/Date(" + startDate.time + ")/",

				to : "/Date(" + endDate.time + ")/",

				label : proj.Project.projectDescription,

				customClass : "ganttRed",

				dataObj : proj.Project.projectId,
				
				title : title

			} ]
		});

	}
	/*
	if (!projectId) {
		showMessage("Update Project","No valid project id","Error on page - a project ID must be provided!!",'error');
	} else {
		var result = rpcClient.taskService.findAllByProject(projectId);
		if (result.result == 0) {
			var taskData = result.data;
			var fmt = new DateFmt("%d-%m-%y");
			for (var i = 0; i < taskData.list.length; i++) {
				
				var startDate = taskData.list[i].calendarStartDate;
				if (startDate == null) {
					startDate =	taskData.list[i].actualStartDate;
				}
				if (startDate == null) {
					startDate = taskData.list[i].scheduledStartDate;
				}
				if (startDate == null) {
					startDate = taskData.list[i].tentativeStartDate;
				}
				if (startDate == null) {
					startDate = new Date();
				}
				
				var duration = taskData.list[i].calenderDuration;
				if (duration == null) {
					duration = taskData.list[i].duration;
				}
				
				var endDate = startDate.time + (duration - 1) * 86400000;

				var actualStartDate = new Date(startDate.time);
				tasksSource.push({
					name : taskData.list[i].taskName,

					desc : taskData.list[i].taskDescription,

					values : [ {

						from : "/Date(" + actualStartDate.getTime() + ")/",

						to : "/Date(" + endDate + ")/",

						label : taskData.list[i].taskDescription,

						customClass : "ganttRed",

						dataObj : taskData.list[i].taskId

					} ]
				});

			}
		} else {
			Message("Cannot load project","Unable to load project: " + result.message,'error');
		}
	}
*/
	return tasksSource;

}


$("#projectsGantt").gantt({

	source : getGanttSource(),

	navigate : "scroll",

	maxScale : "years",
	minScale : "months",
	scale : "months",

	waitText : "Please wait...",

	itemsPerPage : 1000,

	scrollToToday : true,

	onItemClick : function(data) {

		// Open edit task dialog

	},

	onAddClick : function(dt, rowId) {

//		$("#projTasksDialog").data("taskId", null).dialog('option',
//				'title', 'Add New Task').dialog('open');

	},

	onRender : function() {

	}

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

