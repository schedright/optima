$(document).ready( function() {
	var isAdminCheck = rpcClient.usersService.isAdmin();
	if (!isAdminCheck) {
		$("#usersNavBar").hide();
	}
	var currentUser = rpcClient.usersService.getCurrentUser();
	
	$("#allProjectsNavBar").append("<a href=\"allprojects.jsp\"><img width=\"55\" height=\"55\" style=\"margin-top:-19\" src=\"css/header/images/icon_projects.png\" />Projects</a>");
	$("#portfoliosNavBar").append("<a href=\"main.jsp\"><img width=\"55\" height=\"55\" style=\"margin-top:-19\" src=\"css/header/images/icon_portfolio.png\" />Portfolios</a>");
	$("#financingNavBar").append("<a href=\"#\"><img width=\"55\" height=\"55\" style=\"margin-top:-19\" src=\"css/header/images/icon_financing.png\" />Financing</a>");
	$("#projectsNavBar").append("<a href=\"#\"><img width=\"55\" height=\"55\" style=\"margin-top:-19\" src=\"css/header/images/icon_projects.png\" />Bar Chart</a>");
	$("#scheduleNavBar").append("<a href=\"#\"><img width=\"55\" height=\"55\" style=\"margin-top:-19\" src=\"css/header/images/icon_Scheduling.png\" />Scheduling</a>");
	$("#cashflowNavBar").append("<a href=\"#\"><img width=\"55\" height=\"55\" style=\"margin-top:-19\" src=\"css/header/images/icon_cashflow.png\" />Cashflow</a>");
	$("#financialNavBar").append("<a href=\"#\"><img width=\"55\" height=\"55\" style=\"margin-top:-19\" src=\"css/header/images/icon_results2.png\" />Results</a>");
	$("#projectsRoadMapNavBar").append("<a href=\"plans.jsp\"><img width=\"55\" height=\"55\" style=\"margin-top:-19\" src=\"css/header/images/icon_results1.png\" />Capital Plan</a>");
	if (isAdminCheck) {
		$("#usersNavBar").append("<a href=\"users.jsp\"><img width=\"55\" height=\"55\" style=\"margin-top:-19\" src=\"css/header/images/icon_results1.png\" />Users</a>");
	}
	
	$( "#menuLogout" ).click(function() {
		rpcClient.portfolioService.logout();
		location.reload();
		});
	setTimeout(function() {
	rpcClient.portfolioService.findAllLight(function(result , exception) {
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
					$("#financingNavBar").append("<a href=\"finData.jsp?portfolioId=" + data.list[i].portfolioId + "\"><img width=\"55\" height=\"55\" style=\"margin-top:-19\" src=\"css/header/images/icon_financing.png\" />Financing</a>");

					$("#scheduleNavBar").children().get(0).parentNode.removeChild($("#scheduleNavBar").children().get(0));
					$("#scheduleNavBar").append("<a href=\"schedule.jsp?portfolioId=" + data.list[i].portfolioId + "\"><img width=\"55\" height=\"55\" style=\"margin-top:-19\" src=\"css/header/images/icon_Scheduling.png\" />Scheduling</a>");

					$("#cashflowNavBar").children().get(0).parentNode.removeChild($("#cashflowNavBar").children().get(0));
					$("#cashflowNavBar").append("<a href=\"cashFlow.jsp?portfolioId=" + data.list[i].portfolioId + "\"><img width=\"55\" height=\"55\" style=\"margin-top:-19\" src=\"css/header/images/icon_cashflow.png\" />Cashflow</a>");

					$("#financialNavBar").children().get(0).parentNode.removeChild($("#financialNavBar").children().get(0));
					$("#financialNavBar").append("<a href=\"financials.jsp?portfolioId=" + data.list[i].portfolioId + "\"><img width=\"55\" height=\"55\" style=\"margin-top:-19\" src=\"css/header/images/icon_results2.png\" />Results</a>");
					
					if (data.list[i].projects && data.list[i].projects.list && data.list[i].projects.list.length) {
						var projects = data.list[i].projects;
						for (var j = 0; j < projects.list.length; j++) {
							projectsList += liFirstRowProjects + "<a href=\"projectDetails.jsp?projectId=" +  projects.list[j].projectId + "\">" + projects.list[j].projectCode + "</a></li>";
							liFirstRowProjects = "<li>";
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

	var editUserFormUserName = $("#editUserFormUserName");
	var editUserFormPassword1 = $("#editUserFormPassword1");
	var editUserFormPassword2 = $("#editUserFormPassword2");

	$("#editUserDialog")
	.dialog(
			{
				autoOpen : false,
				height : 500,
				width : 450,
				modal : true,
				show : {
					effect : "blind",
					duration : 1000
				},
				hide : {
					effect : "fade",
					duration : 1000
				},
				buttons : {
					"Save" : function() {
						var bValid = true;

						bValid = bValid
								&& checkLength(editUserFormUserName,
										"editUserFormUserName", 6, 20);
						bValid = bValid
								&& checkLength(editUserFormPassword1,
										"editUserFormPassword1", 6, 20);
						if ((editUserFormPassword1.val() !== editUserFormPassword2
								.val())) {
							editUserFormPassword2
									.addClass("ui-state-error");
							bValid = false;
						}

						if (bValid) {
							var makeAdmin = true;
							var result = rpcClient.usersService
									.updateUser(currentUser.userName,
											editUserFormUserName.val(),
											editUserFormPassword1.val(),
											makeAdmin);
							if (result && result.result == 0) {
								location.reload();
							} else {
								showMessage("Update User", 'Error:'
										+ result.message, 'error');
							}
						}
					},
					Cancel : function() {
						$(this).dialog("close");
					}
				},
				close : function() {
					allFields.val("").removeClass("ui-state-error");
				}
			});	
	$( "#editUserName" ).click(function() {
//		currentUser
		$("#editUserFormUserName").val(currentUser.userName);
		$("#editUserFormPassword1").val(currentUser.userPass);
		$("#editUserFormPassword2").val(currentUser.userPass);
		$("#editUserDialog").dialog("open");
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
		$("#projectsRoadMapNavBar").addClass("active");	
	else if(currentPage == 8 && isAdminCheck)
		$("#usersNavBar").addClass("active");	
	else if(currentPage == 9 && isAdminCheck)
		$("#allProjectsNavBar").addClass("active");	

	
	
});