$(function() {
	document.title = 'Scheduling';

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
		var solutionResponse = rpcClient.portfolioService.getSolution(portfolioId);
		if (solutionResponse.result==0 && solutionResponse.data) {
			$("#currentSolution").css('display','');
			$("#schedResults").html('');
			$("#schedResults").append(solutionResponse.data); 
			
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
  			if (result.result == 0) {
  				$("#currentSolution").css('display','');
  				var data = result.data;
  				$("#schedResults").html('');
  				$("#schedResults").append(data); 
  				$('#loading-indicator').hide();
  			 } else {
  				 alert("Error generating solution: " + result.message);
  			 }
  		 }, projectID, "", priorityOrder); 
  			 
  	 });

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

function fillProjectList(data) {
	$("#mainAllProjects").html('');
	for (var i = 0; i < data.length; i++) {
		var li = $('<li></li>').addClass('ui-state-default').attr('id',  data[i].projectId).text(data[i].projectCode);
		li.attr('title', (data[i].projectCode));
		li.attr('description', (data[i].projectDescription));

		$("#mainAllProjects").append(li);
	}
	
}
