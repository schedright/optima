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
		$("#financialDetails").html('');
		$("#financialDetails").append(solutionResponse.data); 
		
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
