function getGanttSource() {
	var tasksSource = [];
	var projects = plansList.data.list;
	var fmt = new DateFmt("%n %d, %y");

	for (var i=0;i<projects.length;i++) {
		var proj = projects[i].map;
		var startDate = proj.Start;
		var endDate = proj.End;
		if (startDate && startDate.time && endDate && endDate.time) {
	    startDate.time += utcDateOffset;
	    endDate.time += utcDateOffset;
		}
		
		var title  =proj.Project.projectName + '&#013;' + proj.Project.projectDescription + '&#013;' + fmt.format(utcTime2LocalDate(startDate.time)) + " - " + fmt.format(utcTime2LocalDate(endDate.time));
		tasksSource.push({
			name : proj.Project.projectName,

			desc : proj.Project.projectDescription,

			values : [ {

				from : "/Date(" + startDate.time + ")/",

				to : "/Date(" + endDate.time + ")/",

				label : proj.Project.projectCode,

				customClass : "ganttRed",

				dataObj : proj.Project.projectId,
				
				title : title

			} ]
		});

	}
	
	return tasksSource;

}

var toDate = function(strDate) {
	var dateParts = strDate.split("/");
	var date = new Date(dateParts[2], (dateParts[0] - 1), dateParts[1]);
	return date;
};
var data = planDates.data.map;
var sd = toDate(data.plan_start);
var ed = toDate(data.plan_end);
var dates = [sd,ed];

$("#projectsGantt").gantt({

	source : getGanttSource(),

	navigate : "scroll",

	maxScale : "years",
	minScale : "months",
	scale : "months",

	waitText : "Please wait...",

	itemsPerPage : 1000,

	scrollToToday : true,

	includedDates : dates,
	
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

