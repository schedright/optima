$(document).ready(function() {
  var isAdminCheck = rpcClient.usersService.isAdmin();
  if (!isAdminCheck) {
    $("#usersNavBar").hide();
  }
  var currentUser = rpcClient.usersService.getCurrentUser();

  $("#allProjectsNavBar").append("<a href=\"allprojects.jsp\">Projects<img class=\"menuIcon\" src=\"css/header/images/icon_projects.png\" /></a>");
  $("#portfoliosNavBar").append("<a href=\"enterprise.jsp\">Enterprise<img class=\"menuIcon\" src=\"css/header/images/icon_portfolio.png\" /></a>");
  $("#financingNavBar").append("<a href=\"#\">Constraint/Schedule<img class=\"menuIcon\" src=\"css/header/images/icon_financing.png\" /></a>");
  $("#cashflowNavBar").append("<a href=\"#\">Cashflow<img class=\"menuIcon\" src=\"css/header/images/icon_cashflow.png\" /></a>");
  $("#financialNavBar").append("<a href=\"#\">Results<img class=\"menuIcon\" src=\"css/header/images/icon_results2.png\" /></a>");
  $("#projectsRoadMapNavBar").append("<a href=\"plans.jsp\">Capital Plan<img class=\"menuIcon\" src=\"css/header/images/icon_results1.png\" /></a>");
  if (isAdminCheck) {
    $("#usersNavBar").append("<a href=\"users.jsp\">Users</a>");
  }

  $("#menuLogout").click(function() {
    rpcClient.portfolioService.logout();
    location.reload();
  });
  $('#sidebar-btn').click(function() {
    if (!$('#sidebar').hasClass('visible')) {
      setTimeout(function() {
        $('#sidebar').addClass('visible');
      }, 0);
    }
  });
  $('body,html').click(function(e) {
    $('#sidebar').removeClass('visible');
  });

  var updateLinks = function(link1, link2) {
    var link = link1 + link2;
    $("#financingNavBar").children().get(0).parentNode.removeChild($("#financingNavBar").children().get(0));
    $("#financingNavBar").append("<a href=\"finData.jsp?" + link + "\">Constraint/Schedule<img class=\"menuIcon\" src=\"css/header/images/icon_financing.png\" /></a>");

    $("#cashflowNavBar").children().get(0).parentNode.removeChild($("#cashflowNavBar").children().get(0));
    $("#cashflowNavBar").append("<a href=\"cashFlow.jsp?" + link + "\">Cashflow<img class=\"menuIcon\" src=\"css/header/images/icon_cashflow.png\" /></a>");

    $("#financialNavBar").children().get(0).parentNode.removeChild($("#financialNavBar").children().get(0));
    $("#financialNavBar").append("<a href=\"financials.jsp?" + link + "\">Results<img class=\"menuIcon\" src=\"css/header/images/icon_results2.png\" /></a>");
    
    if (link1=='portfolioId') {
      rpcClient.portfolioService.findLight(function(result , exception) {
        if (result.result == 0) {
          $('#financingNavBar').attr('title', "Portfolio: " + result.data.portfolioName);
          $('#cashflowNavBar').attr('title', "Portfolio: " + result.data.portfolioName);
          $('#financialNavBar').attr('title', "Portfolio: " + result.data.portfolioName);
        }
      } , link2);
      
    } else {
      rpcClient.projectService.findLight(function(result , exception) {
        if (result.result == 0) {
          var name = "";
          if (result.data.portfolio) {
            name = "Portfolio: " + result.data.portfolio.portfolioName;
          } else {
            name =  "Project: " + result.data.projectName;
          }

          $('#financingNavBar').attr('title', name);
          $('#cashflowNavBar').attr('title',  name);
          $('#financialNavBar').attr('title', name);
          
        } 
      } , link2);
      
    }
  }
  window.updateLinks = updateLinks;
  setTimeout(function() {
    var activeProject = $.cookie('activeProject');
    var linksUpdated = false;
    if (typeof activeProject === 'string') {
      var tags = activeProject.split(',');
      var projId = null;
      var portId = null;
      for (var i = 0; i < tags.length; i++) {
        var subTag = tags[i].split('=');
        if (subTag.length == 2) {
          // portfolio-" + item.proj.portfolio.portfolioId + "," + "project-" + item.proj.projectId
          if (subTag[0] === 'portfolio') {
            portId = subTag[1];
          } else if (subTag[0] === 'project') {
            projId = subTag[1];
          }
        }
      }
      if (portId) {
        updateLinks("portfolioId=" , portId);
        linksUpdated = true;
      } else if (projId) {
        updateLinks("projectId=" , projId);
        linksUpdated = true;
      }
    }
    if (!linksUpdated) {
      rpcClient.portfolioService.findAllLight(function(result,
          exception) {
        if (result.result == 0) {
          var data = result.data;

          if (data.list.length != 0) {
            var portfolioIndex = 0;

            var savedPortfolioIndex = 0;
            for (var i = 0; i < data.list.length; i++) {
              if (i != portfolioIndex) {
                continue;
              }
              updateLinks("portfolioId=" , data.list[i].portfolioId);
            }

          }
        }
      });
    }
  }, 0);

  var editUserFormUserName = $("#editUserFormUserName");
  var editUserFormPassword1 = $("#editUserFormPassword1");
  var editUserFormPassword2 = $("#editUserFormPassword2");

  $("#editUserDialog").dialog({
    autoOpen : false,
    height : 500,
    width : 450,
    modal : true,
    show : {
      effect : "blind",
      duration : 300
    },
    hide : {
      effect : "fade",
      duration : 300
    },
    buttons : {
      "Save" : function() {
        var bValid = true;

        bValid = bValid && checkLength(editUserFormUserName, "editUserFormUserName", 6, 20);
        bValid = bValid && checkLength(editUserFormPassword1, "editUserFormPassword1", 6, 20);
        if ((editUserFormPassword1.val() !== editUserFormPassword2.val())) {
          editUserFormPassword2.addClass("ui-state-error");
          bValid = false;
        }

        if (bValid) {
          var makeAdmin = true;
          var result = rpcClient.usersService.updateUser(currentUser.userName, editUserFormUserName.val(), editUserFormPassword1.val(), makeAdmin);
          if (result && result.result == 0) {
            location.reload();
          } else {
            showMessage("Update User", 'Error:' + result.message, 'error');
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
  $("#editUserName").click(function() {
    // currentUser
    $("#editUserFormUserName").val(currentUser.userName);
    $("#editUserFormPassword1").val(currentUser.userPass);
    $("#editUserFormPassword2").val(currentUser.userPass);
    $("#editUserDialog").dialog("open");
  });

  if (currentPage == 1)
    $("#portfoliosNavBar").addClass("active");
  else if (currentPage == 2)
    $("#financingNavBar").addClass("active");
  else if (currentPage == 5)
    $("#cashflowNavBar").addClass("active");
  else if (currentPage == 6)
    $("#financialNavBar").addClass("active");
  else if (currentPage == 7)
    $("#projectsRoadMapNavBar").addClass("active");
  else if (currentPage == 8 && isAdminCheck)
    $("#usersNavBar").addClass("active");
  else if (currentPage == 9 && isAdminCheck)
    $("#allProjectsNavBar").addClass("active");

});