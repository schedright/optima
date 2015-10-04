$(document).ready( function() {
	$("#portfoliosNavBar").append("<a href=\"mainNew.jsp\"><img src=\"css/header/images/portfolio.png\" />Portfolios</a>");
	
	rpcClient.portfolioService.findAll(function(result , exception) {
		if (result.result == 0) {
			var data = result.data;
			$("#financingNavBar").append("<a href=\"#\"><img src=\"css/header/images/financing.png\" />Financing</a>");
			$("#projectsNavBar").append("<a href=\"#\"><img src=\"css/header/images/project.png\" />Projects</a>");
			$("#scheduleNavBar").append("<a href=\"#\"><img src=\"css/header/images/schedule.png\" />Scheduling</a>");
			$("#cashflowNavBar").append("<a href=\"#\"><img src=\"css/header/images/cashflow.png\" />Cashflow</a>");
			$("#financialNavBar").append("<a href=\"#\"><img src=\"css/header/images/financing.png\" />Financial</a>");
			$("#exportNavBar").append("<a href=\"#\"><img src=\"css/header/images/export.png\" />Export Results</a>");
			
			
			if (data.list.length == 0) {
				$("#financingNavBar").append("<ul><li class=\"firstRowMenu\"><a href=\"\">No Portfolios defined.</a></li></ul>");
				$("#projectsNavBar").append("<ul><li class=\"firstRowMenu\"><a href=\"\">No Projects defined.</a></li></ul>");
				$("#scheduleNavBar").append("<ul><li class=\"firstRowMenu\"><a href=\"\">No Portfolios defined.</a></li></ul>");
				$("#cashflowNavBar").append("<ul><li class=\"firstRowMenu\"><a href=\"\">No Portfolios defined.</a></li></ul>");
				$("#financialNavBar").append("<ul><li class=\"firstRowMenu\"><a href=\"\">No Projects defined.</a></li></ul>");
				$("#exportNavBar").append("<ul><li class=\"firstRowMenu\"><a href=\"\">No Projects defined.</a></li></ul>");
			
			} else {
				var liFirstRow = "<li class=\"firstRowMenu\">";
				var liFirstRowProjects = "<li class=\"firstRowMenu\">";
				var financingList ="<ul>";
				var schedulingList ="<ul>";
				var projectsList ="<ul>";
				var cashflowList ="<ul>";
				var exportList ="<ul>";
				var financialList ="<ul>";
				
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
					financingList += liFirstRow + "<a href=\"finDataNew.jsp?portfolioId=" + data.list[i].portfolioId + "\">" + data.list[i].portfolioName + "</a></li>";
					schedulingList += liFirstRow + "<a href=\"scheduleNew.jsp?portfolioId=" + data.list[i].portfolioId + "\">" + data.list[i].portfolioName + "</a></li>";
					cashflowList += liFirstRow + "<a href=\"cashFlowNew.jsp?portfolioId=" + data.list[i].portfolioId + "\">" + data.list[i].portfolioName + "</a></li>";
					financialList += liFirstRow + "<a href=\"financials.jsp?portfolioId=" + data.list[i].portfolioId + "\">" + data.list[i].portfolioName + "</a></li>";
					
					
					
					var result2 = rpcClient.projectService.findAllByPortfolio(data.list[i].portfolioId);
					if (result2.result == 0) {
						 var projects = result2.data;
						 if (projects != null && projects.list.length != 0 ) {
							for (var j = 0; j < projects.list.length; j++) {
								projectsList += liFirstRowProjects + "<a href=\"projectDetailsNew.jsp?projectId=" +  projects.list[j].projectId + "\">" + projects.list[j].projectCode + "</a></li>";
								exportList += liFirstRowProjects + "<a href=\"projectDetailsNew.jsp?projectId=" + projects.list[j].projectId + "\">" + projects.list[j].projectCode + "</a></li>";
								liFirstRowProjects = "<li>";
							} 	
						}	
					} 

					liFirstRow = "<li>";
				}
				financingList += "</ul>";
				$("#financingNavBar").append(financingList);
				
				projectsList += "</ul>";
				$("#projectsNavBar").append(projectsList);
				
				schedulingList += "</ul>";
				$("#scheduleNavBar").append(schedulingList);
				
				cashflowList += "</ul>";
				$("#cashflowNavBar").append(cashflowList);
				
				financialList += "</ul>";
				$("#financialNavBar").append(financialList);

				exportList += "</ul>";
				$("#exportNavBar").append(exportList);
				
			}
		}
	});
	
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
		$("#exportNavBar").addClass("active");	
	
	
	
});