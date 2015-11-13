$(function() {
	document.title = 'SchedRight - Scheduling';

	var portfolioId = null;
	for ( var i in getURLVariables()) {
		if (i == "portfolioId") {
			portfolioId = getURLVariables()[i];
		}
	}
	
	$.widget( "ui.pcntspinner", $.ui.spinner, {
	    _format: function(value) { return value + '%'; },

	    _parse: function(value) { return parseFloat(value); }
	});
	
	  $("#reatainedPercentage").pcntspinner({
			min : 0,
			max : 100,
			step : 0.1
		    });
	  
	  
	    $("#mainAllProjects").sortable({
			revert : true,
			items : "li"
		    });

		var projectsList = rpcClient.projectService.findAllByPortfolio(portfolioId);
		if (projectsList.result == 0) {
		    var projectsData = projectsList.data.list;

		
		    fillProjectList(projectsData);
		}
		var solutionResponse = rpcClient.portfolioService.hasSolution(portfolioId);
		if (solutionResponse.result==0 && solutionResponse.data && solutionResponse.data=='TRUE') {
			$("#currentSolution").css('display','');
			$("#schedResults").html('');
			$("#schedResults").append("<p style='margin-left:65px'><a href='financials.jsp?portfolioId=" + portfolioId +"'>Go to the current solution.</a></p>"); 
			
		} else {
			$("#currentSolution").css('display','none');
		}

	
    $("#mainAllProjects").droppable(
		    {
			accept : "#dependencies li",
			hoverClass : "ui-state-hover",
			drop : function(ev, ui) {

			    var remDepCall = rpcClient.taskService.removeTaskDependency(task.taskId,
				    ui.draggable.attr("id"));
			    if (remDepCall.result == 0) {
				ui.draggable.remove();
				$(this).append(ui.draggable);
				return true;
			    } else {
				return false;
			    }
			}
		    });

	
    $("#findFinalSolBtn").button(
      		 {
      			 icons : {
      					secondary : "ui-icon-lightbulb"
      				},
      				text : true
      			 
      		 }
      	 ).click(function(){
				var displayed = $("#currentSolution").css('display');
				if (displayed == 'none') {
					solveIt(portfolioId);
				} else {
	
					var buttons = {
						Yes : function() {
							$(this).dialog("close");
							solveIt(portfolioId);
							return;
						},
						No : function() {
							$(this).dialog("close");
							return;
						}
					}
					showMessage(
							'Solve',
							'The portfolio was already solved before. Do you want to solve it agin?',
							'info', buttons);
				}
  	 });

    $("#exportSolutionToCSV").button(
     		 {
     			icons : {
     			    primary : "ui-icon-calculator"
     			},
     				text : true
     		 }
     	 ).click(function(){
     		var solutionResponse = rpcClient.portfolioService.getSolutionAsCSV(portfolioId);
    		if (solutionResponse.result==0 && solutionResponse.data) {
		        var blob = new Blob([solutionResponse.data], {type: "text/plain;charset=utf-8"});
		        var date = new Date();
		        saveAs(blob, "Solution" + date + ".csv");
    		} else {
    			showMessage("Export to CSV",'Error: Cannot find a valid solution.','error');    			
    		}
     		 
 			 
 	 });
    
});

function solveIt(portfolioId) {
	$('#loading-indicator').show();
	var projectsAccordingToPriority = $("#mainAllProjects").find('li');
	var projectID = projectsAccordingToPriority[0].id;
	var priorityOrder = "";
	var separator = "";
	for (var i = 0; i < projectsAccordingToPriority.length; i++) {
		priorityOrder = priorityOrder + separator + projectsAccordingToPriority[i].id;
		separator = ",";
	}
	rpcClient.projectService.getSolution( function(result , exception) {
			if (result.result == 0 && result.data) {
				$("#currentSolution").css('display','');
				$("#schedResults").html('');
				$("#schedResults").append("<p style='margin-left:65px'><a href='financials.jsp?portfolioId=" + portfolioId +"'>Go to the current solution.</a></p>"); 
				showMessage("Solve Portfolio",'Portfolio solved successfully.','success');
			} else {
				showMessage("Solve Portfolio",'Error:' + result.message,'error');
				$("#currentSolution").css('display','none');
			}
			$('#loading-indicator').hide();
	}, projectID, "", priorityOrder); 
};

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

function fillProjectList(data) {
	$("#mainAllProjects").html('');
	for (var i = 0; i < data.length; i++) {
		var li = $('<li></li>').addClass('ui-state-default').attr('id',  data[i].projectId).text(data[i].projectCode);
		li.attr('title', (data[i].projectCode));
		li.attr('description', (data[i].projectDescription));

		$("#mainAllProjects").append(li);
	}
	
}
