$(document).ready( function() {
	$("#portfoliosNavBar").append("<a href=\"main.jsp\"><img src=\"css/header/images/portfolio.png\" />Portfolios</a>");
	$("#financingNavBar").append("<a href=\"#\"><img src=\"css/header/images/financing.png\" />Financing</a>");
	$("#projectsNavBar").append("<a href=\"#\"><img src=\"css/header/images/project.png\" />Projects</a>");
	$("#scheduleNavBar").append("<a href=\"#\"><img src=\"css/header/images/schedule.png\" />Scheduling</a>");
	$("#cashflowNavBar").append("<a href=\"#\"><img src=\"css/header/images/cashflow.png\" />Cashflow</a>");
	$("#financialNavBar").append("<a href=\"#\"><img src=\"css/header/images/financing.png\" />Results</a>");
	$("#projectsRoadMapNavBar").append("<a href=\"#\"><img src=\"css/header/images/financing.png\" />Plans</a>");
	
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
					$("#financingNavBar").children().get(0).remove();
					$("#financingNavBar").append("<a href=\"finData.jsp?portfolioId=" + data.list[i].portfolioId + "\"><img src=\"css/header/images/financing.png\" />Financing</a>");

					$("#scheduleNavBar").children().get(0).remove();
					$("#scheduleNavBar").append("<a href=\"schedule.jsp?portfolioId=" + data.list[i].portfolioId + "\"><img src=\"css/header/images/schedule.png\" />Scheduling</a>");

					$("#cashflowNavBar").children().get(0).remove();
					$("#cashflowNavBar").append("<a href=\"cashFlow.jsp?portfolioId=" + data.list[i].portfolioId + "\"><img src=\"css/header/images/cashflow.png\" />Cashflow</a>");

					$("#financialNavBar").children().get(0).remove();
					$("#financialNavBar").append("<a href=\"financials.jsp?portfolioId=" + data.list[i].portfolioId + "\"><img src=\"css/header/images/financing.png\" />Results</a>");
					
					$("#projectsRoadMapNavBar").children().get(0).remove();
					$("#projectsRoadMapNavBar").append("<a href=\"plans.jsp?portfolioId=" + data.list[i].portfolioId + "\"><img src=\"css/header/images/project.png\" />Plans</a>");
					
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
//	else if(currentPage == 7)
//		$("#exportNavBar").addClass("active");	
	
	
	
});