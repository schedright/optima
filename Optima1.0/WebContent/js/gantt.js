function getGanttSource() {

	var projectId = null;
	for ( var i in getURLVariables()) {
		if (i == "projectId") {
			projectId = getURLVariables()[i];
		}
	}
	tasksSource = [];
	if (projectId == null) {
		alert("Error on page - a project ID must be provided!!");
	} else {
		var result = rpcClient.taskService.findAllByProject(projectId);
		if (result.result == 0) {
			var taskData = result.data;

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

				tasksSource.push({
					name : taskData.list[i].taskName,

					desc : taskData.list[i].taskDescription,

					values : [ {

						from : "/Date(" + startDate.time + ")/",

						to : "/Date(" + endDate + ")/",

						label : taskData.list[i].taskDescription,

						customClass : "ganttRed",

						dataObj : taskData.list[i].taskId

					} ]
				});

			}
		} else {
			alert("Unable to load project: " + result.message);
		}
	}

	return tasksSource;

}


$("#tasksGantt").gantt({

	source : getGanttSource(),

	navigate : "scroll",

	maxScale : "months",
	minScale : "days",
	scale : "days",

	waitText : "Please wait...",

	itemsPerPage : 16,

	scrollToToday : true,

	onItemClick : function(data) {

		// Open edit task dialog

	},

	onAddClick : function(dt, rowId) {

		$("#projTasksDialog").data("taskId", null).dialog('option',
				'title', 'Add New Task').dialog('open');

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

/*
 * $(".gantt").popover({
 * 
 * selector: ".bar",
 * 
 * title: "I'm a popover",
 * 
 * content: "And I'm the content of said popover.",
 * 
 * trigger: "hover"
 * 
 * });
 */