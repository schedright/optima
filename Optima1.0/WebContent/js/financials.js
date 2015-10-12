$(function() {
	document.title = 'Financial Details';
	var grid;

	
	
     var portfolioId = null;
     var currentDate = null;
    for ( var i in getURLVariables()) {
		if (i == "portfolioId") {
		    portfolioId = getURLVariables()[i];
		}
		if (i == "currentDate") {		
			currentDate = new Date(getURLVariables()[i]);
		}
	}
    
    if (currentDate == null) {
    	currentDate = new Date();
    }
    
	var solutionResponse = rpcClient.portfolioService.getSolution(portfolioId);
	if (solutionResponse.result==0 && solutionResponse.data) {
		$("#schedResults").html('');
		$("#schedResults").append(solutionResponse.data); 
		$("#exportSolutionToCSV").removeAttr('disabled');
		
	} else {
		$("#schedResults").html('');
		$("#schedResults").append("<p style='margin-left:65px'>Project is not solved</p><p style='margin-left:65px'>Go to <a href='scheduleNew.jsp?portfolioId=" + portfolioId + "'>Scheduling tab</a> and solve</p>"); 
		$("#exportSolutionToCSV").attr('disabled','true');
	}
	 
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
