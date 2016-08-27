window.weekdays = null;

function getGanttSource() {

	var projectId = null;
	for ( var i in getURLVariables()) {
		if (i == "projectId") {
			projectId = getURLVariables()[i];
		}
	}
	tasksSource = [];
	if (!projectId) {
		showMessage("Update Project","No valid project id","Error on page - a project ID must be provided!!",'error');
	} else {
		var result = rpcClient.taskService.findAllByProject(projectId);
		if (result.result == 0) {
			var taskData = result.data;
			var fmt = new DateFmt("%n %d, %y");
			for (var i = 0; i < taskData.list.length; i++) {
				if (i==0) {
				  //get the weekend
				  var we = taskData.list[i].project.weekend;
				  if (typeof we == 'string' && we.length==7) {
				    window.weekdays = [];
				    for (var x=0;x<7;x++) {
				      if (we[x]=='0') {
				        window.weekdays.push(' wd');
				      } else {
				        window.weekdays.push(' sa');
				      }
				    }
				  }
				}
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
				} else {
				  startDate.time += utcDateOffset(startDate.time);
				}
				
				var duration = taskData.list[i].calenderDuration;
				if (duration == null) {
					duration = taskData.list[i].duration;
				}
				
				var endDate = startDate.time + (duration - 1) * 86400000;

				var actualStartDate = new Date(startDate.time);
				var title  = "";
				if (duration==0) {
				  title = taskData.list[i].taskName;
				} else {
	        title = taskData.list[i].taskName + '&#013;';
	        if (taskData.list[i].taskDescription) {
	          title += taskData.list[i].taskDescription + '&#013;'
	        }
	        title += fmt.format(new Date(startDate.time)) + " - " + fmt.format(new Date(endDate)) + '&#013;';
	        title += "Duration: " + taskData.list[i].duration + ' Days&#013;';
	        title += "Daily Cost: " + taskData.list[i].uniformDailyCost + '$&#013;';
	        title += "Daily Income: " + taskData.list[i].uniformDailyIncome + '$&#013;';
				}
					//proj.Project.projectName + '&#013;' + proj.Project.projectDescription + '&#013;' + fmt.format(new Date(startDate.time)) + " - " + fmt.format(new Date(endDate.time));
				tasksSource.push({
					name : taskData.list[i].taskName,

					desc : taskData.list[i].taskDescription,

					values : [ {

						from : "/Date(" + actualStartDate.getTime() + ")/",

						to : "/Date(" + endDate + ")/",

						label : taskData.list[i].taskName,

						customClass : "ganttRed",

						dataObj : taskData.list[i].taskId,
						
						title : title

					} ]
				});

			}
		} else {
			Message("Cannot load project","Unable to load project: " + result.message,'error');
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
	excludeYears :  true,
	  
	waitText : "Please wait...",

	itemsPerPage : 1000,

	scrollToToday : true,

	weekdays_classes : window.weekdays,
	
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