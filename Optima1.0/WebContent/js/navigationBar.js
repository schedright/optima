$(document).ready( function() {
	$("#portfoliosNavBar").append("<a href=\"main.jsp\"><img width=\"32\" height=\"32\"  src=\"css/header/images/icon_portfolio.png\" />Portfolios</a>");
	$("#financingNavBar").append("<a href=\"#\"><img width=\"32\" height=\"32\"  src=\"css/header/images/icon_financing.png\" />Financing</a>");
	$("#projectsNavBar").append("<a href=\"#\"><img width=\"32\" height=\"32\"  src=\"css/header/images/icon_projects.png\" />Projects</a>");
	$("#scheduleNavBar").append("<a href=\"#\"><img width=\"32\" height=\"32\"  src=\"css/header/images/icon_Scheduling.png\" />Scheduling</a>");
	$("#cashflowNavBar").append("<a href=\"#\"><img width=\"32\" height=\"32\"  src=\"css/header/images/icon_cashflow.png\" />Cashflow</a>");
	$("#financialNavBar").append("<a href=\"#\"><img width=\"32\" height=\"32\"  src=\"css/header/images/icon_results2.png\" />Results</a>");
	$("#projectsRoadMapNavBar").append("<a href=\"plans.jsp\"><img width=\"32\" height=\"32\"  src=\"css/header/images/icon_results1.png\" />Plans</a>");
	
	setTimeout(function() {
	rpcClient.portfolioService.findAll(function(result , exception) {
		if (result.result == 0) {
			var data = result.data;
			
			if (data.list.length == 0) {
				$("#projectsNavBar").append("<ul><li class=\"firstRowMenu\"><a href=\"\">No Projects defined.</a></li></ul>");
			} else {
				var liFirstRow = "<li class=\"firstRowMenu\">";
				var liFirstRowProjects = "<li class=\"firstRowMenu\">";
				var projectsList ="<ul>";
				
				var portfolioIndex = 0;
				var savedPortfolioIndex = $.cookie('saved_index_pf');
				if (savedPortfolioIndex) {
					try {
						portfolioIndex = parseInt(savedPortfolioIndex);
					} catch (e) {
					}
				}
				for (var i = 0; i< data.list.length; i++) {
					if (i != portfolioIndex) {
						continue;
					}
					$("#financingNavBar").children().get(0).parentNode.removeChild($("#financingNavBar").children().get(0));
					$("#financingNavBar").append("<a href=\"finData.jsp?portfolioId=" + data.list[i].portfolioId + "\"><img width=\"32\" height=\"32\"  src=\"css/header/images/icon_financing.png\" />Financing</a>");

					$("#scheduleNavBar").children().get(0).parentNode.removeChild($("#scheduleNavBar").children().get(0));
					$("#scheduleNavBar").append("<a href=\"schedule.jsp?portfolioId=" + data.list[i].portfolioId + "\"><img width=\"32\" height=\"32\"  src=\"css/header/images/icon_Scheduling.png\" />Scheduling</a>");

					$("#cashflowNavBar").children().get(0).parentNode.removeChild($("#cashflowNavBar").children().get(0));
					$("#cashflowNavBar").append("<a href=\"cashFlow.jsp?portfolioId=" + data.list[i].portfolioId + "\"><img width=\"32\" height=\"32\"  src=\"css/header/images/icon_cashflow.png\" />Cashflow</a>");

					$("#financialNavBar").children().get(0).parentNode.removeChild($("#financialNavBar").children().get(0));
					$("#financialNavBar").append("<a href=\"financials.jsp?portfolioId=" + data.list[i].portfolioId + "\"><img width=\"32\" height=\"32\"  src=\"css/header/images/icon_results2.png\" />Results</a>");
					
					var result2 = rpcClient.projectService.findAllByPortfolio(data.list[i].portfolioId);
					if (result2.result == 0) {
						 var projects = result2.data;
						 if (projects != null && projects.list.length != 0 ) {
							for (var j = 0; j < projects.list.length; j++) {
								projectsList += liFirstRowProjects + "<a href=\"projectDetails.jsp?projectId=" +  projects.list[j].projectId + "\">" + projects.list[j].projectCode + "</a></li>";
								liFirstRowProjects = "<li>";
							} 	
						}	
					} 

					liFirstRow = "<li>";
				}
				
				projectsList += "</ul>";
				$("#projectsNavBar").append(projectsList);
			}
		}
	});
	},0);
	
	if(currentPage == 1)
		$("#portfoliosNavBar").addClass("active");
	else if(currentPage == 2)
		$("#financingNavBar").addClass("active");
	else if(currentPage == 3)
		$("#projectsNavBar").addClass("active");
	else if(currentPage == 4)
		$("#scheduleNavBar").addClass("active");
	else if(currentPage == 5)
		$("#cashflowNavBar").addClass("active");
	else if(currentPage == 6)
		$("#financialNavBar").addClass("active");	
	else if(currentPage == 7)
		$("#projectsRoadMapNavBar").addClass("active");	
	
	
	
});