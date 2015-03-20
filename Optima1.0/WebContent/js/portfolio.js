

$(document).ready( function() {
	document.title = 'Portfolios';
	$("#portfoliosNavBar").addClass("active");
	
	$('#main').tabs({ 
	    activate: function (e, ui) { 
	        $.cookie('pf-selected-tab', ui.newTab.index(), { path: '/' }); 
	    }, 
	    active: $.cookie('pf-selected-tab')             
	});
	
	tips = $( ".validateTips" );
	
	$("#deleteProjectConfirmDialog").dialog(
			{ 
				  autoOpen: false,
				  height:240,
			      modal: true,
			      show: {
			          effect: "blind",
			          duration: 1000
			        },
			        hide: {
			          effect: "fade",
			          duration: 1000
			        },
			        buttons: {
				        "Delete This Project": function() {
				        	var project = $(this).data("project");
				        	var call =  rpcClient.projectService.remove( project );
				        	 if (call.result == 0) {
			        			 $( this ).dialog( "close" );
			        			 location.reload();
			        		 } else {
			        			 
			        		 }
				        	
				        },
			        	Cancel: function() {
				          $( this ).dialog( "close" );
				        }
			        }
			        
				}
	);
	$( "#createOrEditProjectDialog" ).dialog({
	      autoOpen: false,
	      height: 500,
	      width: 720,
	      modal: true,
	      show: {
	          effect: "blind",
	          duration: 1000
	        },
	        hide: {
	          effect: "fade",
	          duration: 1000
	        },
	        open : function() {
	        	$("#projectCity").empty();
	        	$("#projectCity").append("<option value=\"0\"></option>");
	        	$("#projectProvince").empty();
	        	$("#projectProvince").append("<option value=\"0\"></option>");
	        	$("#projectCountry").empty();
	        	$("#projectCountry").append("<option value=\"0\"></option>");
	        	rpcClient.locationService.findAllByType(function(result , exception) {
	        		 var cities = result.data;
		        	 if (result.result ==  0 && cities != null ) {
		        		for (var index = 0 ; index < cities.list.length ; index++ ) {
		        			$("#projectCity").append("<option value=\"" + cities.list[index].locationId + "\">" + cities.list[index].locationName + "</option>");
		        		}
		        	 }
	        	 } ,"CITY");
	        	
	        	 
	        	rpcClient.locationService.findAllByType(function(result , exception) {
	        		 var provinces = result.data;
		        	 if (result.result ==  0 && provinces != null ) {
		        		for (var index = 0 ; index < provinces.list.length ; index++ ) {
		        			$("#projectProvince").append("<option value=\"" + provinces.list[index].locationId + "\">" + provinces.list[index].locationName + "</option>");
		        		}
		        	 }
	        	 } , "PROVINCE");
	        	 
	        	 
	        	rpcClient.locationService.findAllByType(function(result , exception) {
	        		 var countries = result.data;
		        	 if (result.result ==  0 && countries != null ) {
		        		for (var index = 0 ; index < countries.list.length ; index++ ) {
		        			$("#projectCountry").append("<option value=\"" + countries.list[index].locationId + "\">" + countries.list[index].locationName + "</option>");
		        		}
		        	 }
	        	 }, "COUNTRY");
	        	 
	        	 
	        	 
	        	 var projectId = $(this).data("projectId");
	        	 if (projectId != null) {
	        		 call = rpcClient.projectService.find(function(call , exception) {
	        			 if (call.result == 0) {
		        			 var data = call.data;
		        			 $("#projectName").val(data.projectName);
		        			 $("#projectCode").val(data.projectCode);
		        			 $("#projectDescription").val(data.projectDescription);
		        			 $("#projectStreetAddress").val(data.projectAddressStreet);
		        			 $("#projectCity option[value=" + data.city.locationId + "]").attr('selected', 'selected');
		        			 $("#projectProvince option[value=" + data.province.locationId + "]").attr('selected', 'selected');
		        			 $("#projectCountry option[value=" + data.country.locationId + "]").attr('selected', 'selected');
		        			 $("#projectPostalCode").val(data.projectAddressPostalCode);
		        		 } else {
		        			 alert("Error: " + call.message);
		        		 }
	        		 } , projectId);
	        		
	        	 } else {
	        		 $("#projectName").val("");
        			 $("#projectCode").val("");
        			 $("#projectDescription").val("");
        			 $("#projectStreetAddress").val("");
        			 $("#projectCity option[value=\"0\"]").attr('selected', 'selected');
        			 $("#projectProvince option[value=\"0\"]").attr('selected', 'selected');
        			 $("#projectCountry option[value=\"0\"]").attr('selected', 'selected');
        			 $("#projectPostalCode").val("");
	        	 }
	        },
	      buttons: {
	    	  "Save" : function() {
	    		 $("#projectName").removeClass( "ui-state-error" );
     			 $("#projectCode").removeClass( "ui-state-error" );
     			 $("#projectDescription").removeClass( "ui-state-error" );
     			 $("#projectStreetAddress").removeClass( "ui-state-error" );
     			 $("#projectCity").removeClass( "ui-state-error" );
     			 $("#projectProvince").removeClass( "ui-state-error" ); 
     			 $("#projectCountry").removeClass( "ui-state-error" );
     			 $("#projectPostalCode").removeClass( "ui-state-error" );
     			 var bValid = true;
     			 bValid = bValid && checkLength( $("#projectName") , "locationName", 3, 32 );
     			 bValid = bValid && checkLength( $("#projectCode") , "locationName", 3, 32 );
     			 if (bValid) {
	        		 var projectId = $(this).data("projectId");
	        		 var portfolioId = $(this).data("portfolioId");
	        		 var call;
	        		 if (projectId == null) {

	        		
	        				 call =  rpcClient.projectService.create(
	        					 $("#projectName").val(),
	        					 $("#projectCode").val(),
	        					 $("#projectDescription").val(),
	        					 $("#projectStreetAddress").val(),
	        					 $('#projectCity option:selected').val(),
	        					 $('#projectProvince option:selected').val(),
	        					 $('#projectCountry option:selected').val(),
	        					 $("#projectPostalCode").val(), 
	        					 null , null , 0.0 , 0.0 , portfolioId , -1, -1);
	        					
	        		 } else {
	        			 call =  rpcClient.projectService.update( projectId , 
	        					 $("#projectName").val(),
	        					 $("#projectCode").val(),
	        					 $("#projectDescription").val(),
	        					 $("#projectStreetAddress").val(),
	        					 $('#projectCity option:selected').val(),
	        					 $('#projectProvince option:selected').val(),
	        					 $('#projectCountry option:selected').val(),
	        					 $("#projectPostalCode").val(), 
	        					 null , null , 0.0 , 0.0 , portfolioId , -1, -1);
	        		 }
	        		  if (call.result == 0) {
	        			 $( this ).dialog( "close" );
	        			 location.reload();
	        		 } else {
	        			 alert(call.result + ":" + call.message);
	        		 }
     			 } 
	    	  },
	    	  "Cancel" : function() {
	    		  $(this).dialog("close");
	    	  }
	      }
		});
	var portName = $( "#portName" ),
    portDescription = $( "#portDescription" ),
    allFields = $( [] ).add( portName ).add( portDescription ),
    tips = $( ".validateTips" );
	$( "#newPortfolioDialog" ).dialog({
	      autoOpen: false,
	      height: 500,
	      width: 450,
	      modal: true,
	      show: {
	          effect: "blind",
	          duration: 1000
	        },
	        hide: {
	          effect: "fade",
	          duration: 1000
	        },
	      buttons: {
	        "Save": function() {
	          var bValid = true;
	          allFields.removeClass( "ui-state-error" );
	 
	          bValid = bValid && checkLength( portName, "portName", 3, 32 );
	          bValid = bValid && checkLength( portDescription, "portDescription", 1, 1024 );
	       
	          if ( bValid ) {
	            var createPortResult = rpcClient.portfolioService.create(portName.val() , portDescription.val()); 
	            if (createPortResult.result == 0) {
	            	$( this ).dialog( "close" );
	            	location.reload();
	            } else {
	            	$( "#newPortfolioDialog" ).append("<div class=\"ui-widget\">" +
					"<div class=\"ui-state-error ui-corner-all\" style=\"padding: 0 .7em;\">" +
						"<p><span class=\"ui-icon ui-icon-alert\" style=\"float: left; margin-right: .3em;\"></span>" +
						"<strong>Alert:</strong>Error: "  + createPortResult.message + "</p>" +
					"</div>" +
				"</div>");
	            }
	          }
	        },
	        Cancel: function() {
	          $( this ).dialog( "close" );
	        }
	      },
	      close: function() {
	        allFields.val( "" ).removeClass( "ui-state-error" );
	      }
	    });
	$( "#addNew" ).button({
	      text: true,
	      icons: {
	        primary: "ui-icon-circle-plus"
	      }
	    }).click(function(){
	    	$( "#newPortfolioDialog" ).dialog( "open" );
	    });
	
	$( "#locationsManagement" ).button({
	      text: true,
	      icons: {
	        primary: "ui-icon-home"
	      }
	    }).click(function(){
	    	window.location.href  = "locations.html";
	    });
	
	$( "#clientManagement" ).button({
	      text: true,
	      icons: {
	        primary: "ui-icon-person"
	      }
	    }).click(function(){
	    	//window.location.href  = "clients.html";
	    	$('#main').load("views/clients.html").fadeIn('slow');
	    });
	
	rpcClient.portfolioService.findAll(function(result , exception) {
		if (result.result == 0) {
			var data = result.data;
			if (data.list.length == 0) {
				$("body").append("<div class=\"ui-widget\">" +
						"<div class=\"ui-state-error ui-corner-all\" style=\"padding: 0 .7em;\">" +
							"<p><span class=\"ui-icon ui-icon-alert\" style=\"float: left; margin-right: .3em;\"></span>" +
							"<strong>Alert:</strong> No Portfolios defined. Please add one</p>" +
						"</div>" +
					"</div>");
			} else {
				
				for (var i = 0; i< data.list.length; i++) {
				var projectsHtml = "<h3>" + data.list[i].portfolioName + "</h3><div id=\"portfolio" + data.list[i].portfolioId + "\">" 
				+ data.list[i].portfolioDescreption + "<br><br><br>" ;
				
				var result2 = rpcClient.projectService.findAllByPortfolio(data.list[i].portfolioId);
				
				if (result2.result == 0) {
					 var projects = result2.data;
					 if (projects != null && projects.list.length != 0 ) {
						 projectsHtml += " <div class=\"prods-cnt\">";
						 projectsHtml += "<div id=\"portfolio" + data.list[i].portfolioId + "list\" class=\"list\"></div>";
						 projectsHtml += "<div id=\"portfolio" + data.list[i].portfolioId + "grid\" class=\"grid\"></div>";
						 projectsHtml += "<div class=\"clear\"></div>";
						for (var j = 0; j < projects.list.length; j++) {
							var contents = projects.list[j].projectName + "[" + projects.list[j].projectDescription + "]" ;
							projectsHtml += " <div class=\"prod-box shadow\"> "
							+ " <button id=\"deleteProject_" + projects.list[j].projectId  + "\" class=\"projbutton_delete\" onClick=\"deleteProject(" + projects.list[j].projectId + ")\"></button>" 
							+ " <button id=\"editProject_" + projects.list[j].projectId  + "\" class=\"projbutton_edit\" onClick=\"editProject(" + projects.list[j].projectId + " , " + data.list[i].portfolioId  + ")\"></button>" 
							+ "<a id=\"project_"+projects.list[j].projectId+"\" class=\"projLink\"  href=\"projectDetails.jsp?projectId=" + projects.list[j].projectId + "\" target=\"_blank\">" 
							+ projects.list[j].projectCode  + "</a> <br><br>" 						
							+ "<div class=\"projectDescCls\">" + contents + "</div>"
							+ "</div>";
						} 	
						
						projectsHtml += "</div>";
					}	
				} 
				 projectsHtml += "<div id=\"projectDetails\"></div>";
		
				 projectsHtml += " <br><br><div id=\"toolbar" + data.list[i].portfolioId  + "\" class=\"ui-widget-header ui-corner-all\">"  
				 +  " <button id=\"addNew" + data.list[i].portfolioId + "\" value=\"Add New Project\">Add New Project</button> ";
				 if (data.list[i].portfolioId != 1) {
					 projectsHtml += " <button id=\"deletePort" + data.list[i].portfolioId + "\" value=\"Delete This Portfolio\">Delete This Portfolio</button> ";
					 projectsHtml += " <button id=\"editPort" + data.list[i].portfolioId + "\" value=\"Edit This Portfolio\">Edit This Portfolio</button> ";
				 }
				 projectsHtml += " <a id=\"finPeriods" + data.list[i].portfolioId + "\" target=\"_blank\">Setup Financial Periods</a> ";
				 projectsHtml += " <a id=\"cashFlow" + data.list[i].portfolioId + "\" target=\"_blank\">Cash Flow</a> ";
				 projectsHtml += " <a id=\"scheduling" + data.list[i].portfolioId + "\" target=\"_blank\">Scheduling</a> ";
				 projectsHtml += " <a id=\"exportExcel_" + data.list[i].portfolioId + "\" target=\"_blank\">Export to Excel</a> ";
				 projectsHtml +=  "</div>";
			 	 projectsHtml +=  "</div>";
				
				$("#accordion").append(projectsHtml);
			 	 
			 	 
				 	 
				$("#addNew" + data.list[i].portfolioId).data("portfolioId" , data.list[i].portfolioId).button({
				      text: true,
				      icons: {
				        primary: "ui-icon-circle-plus"
				      }
				    }).click(function () {
				    	var portfolioId = $(this).data("portfolioId");
				    	$( "#createOrEditProjectDialog" ).data("projectId", null ).data("portfolioId" , portfolioId).dialog("open");
				    	
				    });
				 if (data.list[i].portfolioId != 1) {
					 $("#editPort" + data.list[i].portfolioId).data("portfolio" , data.list[i]).button({
					      text: true,
					      icons: {
					        primary: "ui-icon-document"
					      }
					    }).click(function () {
					    	var portfolio = $(this).data("portfolio");
					    	if ($("#editPortfolioDialog").length == 0) {
						    	$("body").append("<div id=\"editPortfolioDialog\" title=\"Edit Portfolio\">"
						    			+ " <p class=\"validateTips\">All form fields are required.</p>"
						    			+ " <form>"
						    			+ " <fieldset>"
						    			+ " <label for=\"portName\">Name</label>"
						    			+ " <input type=\"text\" name=\"ePortName\" id=\"ePortName\" class=\"text ui-widget-content ui-corner-all\" />"
						    			+ " <label for=\"portDescription\">Description</label>"
						    			+ " <input type=\"text\" name=\"ePortDescription\" id=\"ePortDescription\" value=\"\" class=\"text ui-widget-content ui-corner-all\" />"
						    			+ " </fieldset>"
						    			+ " </form>"
						    			+ " </div>");
					    				    	
						    	$( "#editPortfolioDialog").data("portfolio" , portfolio ).dialog({
						    	      resizable: false,
						    	      height: 500,
						    	      width: 450,
						    	      modal: true,
						    	      show: {
						    	          effect: "blind",
						    	          duration: 1000
						    	        },
						    	        hide: {
						    	          effect: "fade",
						    	          duration: 1000
						    	        },
						    	        open: 
						    	        	function() {
						    	        		
						    	        		 var portfolio = $(this).data('portfolio');
						    	        		 rpcClient.portfolioService.find(function(result , exception) {
						    	        			 if (result.result == 0) {
								    	    			  $("#ePortName").val(result.data.portfolioName);
								    	    			  $("#ePortDescription").val(result.data.portfolioDescreption);
								    	    		  } else {
								    	    			  $( "#editPortfolioDialog" ).append("<div class=\"ui-widget\">" +
								    						"<div class=\"ui-state-error ui-corner-all\" style=\"padding: 0 .7em;\">" +
								    							"<p><span class=\"ui-icon ui-icon-alert\" style=\"float: left; margin-right: .3em;\"></span>" +
								    							"<strong>Alert:</strong>Error: "  + result.message + "</p>" +
								    						"</div>" +
								    					"</div>"); 
								    	    		  }
						    	        		 } , portfolio.portfolioId);
							    	    		 
						    	        	}
						    	        ,
						    	      buttons: {
						    	    	  "Save Portfolio": function() {
						    		          var bValid = true;
						    		          var portfolio = $(this).data('portfolio');
						    		          var portName = $( "#ePortName" ),
						    		          portDescription = $( "#ePortDescription" );
						    		          bValid = bValid && checkLength( portName, "ePortName", 3, 32 );
						    		          bValid = bValid && checkLength( portDescription, "ePortDescription", 1, 1024 );
						    		       
						    		          if ( bValid ) {
						    		        	  rpcClient.portfolioService.update(function(result , exception) {
						    		            	if (result.result == 0) {
							    		            	$( this ).dialog( "close" );
							    		            	location.reload();
							    		            } else {
							    		            	$( "#editPortfolioDialog" ).append("<div class=\"ui-widget\">" +
							    						"<div class=\"ui-state-error ui-corner-all\" style=\"padding: 0 .7em;\">" +
							    							"<p><span class=\"ui-icon ui-icon-alert\" style=\"float: left; margin-right: .3em;\"></span>" +
							    							"<strong>Alert:</strong>Error: "  + result.message + "</p>" +
							    						"</div>" +
							    		            	"</div>");
							    		            }	
						    		            } ,portfolio.portfolioId, portName.val() , portDescription.val()); 
						    		            
						    		          }
						    		        },
						    		        Cancel: function() {
						    		          $( this ).dialog( "close" );
						    		        }
						    		      }
						    	        });
					    	} else {
					    		$("#editPortfolioDialog").data("portfolio" , data.list[i]).dialog("open");
					    	}
						    });
					    
					
					 $("#deletePort" + data.list[i].portfolioId).data("portfolioId" , data.list[i].portfolioId).button({
					      text: true,
					      icons: {
					        primary: "ui-icon-trash"
					      }
					    }).click(function () {	
					    	 var portfolioId = $(this).data('portfolioId');
					    	 if ($("deletePortConfirmDialog").length == 0) {
						    	$("body").append( "	<div id=\"deletePortConfirmDialog\" title=\"Delete portfolio?\"> " 
						    			+ " <p><span class=\"ui-icon ui-icon-alert\" style=\"float: left; margin: 0 7px 20px 0;\"> "
						    			+ "</span> This portfolio with all its projects will be permanently deleted and cannot be recovered. Are you sure?</p>"
						    	 		+ "</div>"); 
						    	
						    	$( "#deletePortConfirmDialog").data("portfolioId" , portfolioId ).dialog({
						    	      resizable: false,
						    	      height:240,
						    	      modal: true,
						    	      show: {
						    	          effect: "blind",
						    	          duration: 1000
						    	        },
						    	        hide: {
						    	          effect: "fade",
						    	          duration: 1000
						    	        },
						    	      buttons: {
						    	    	  "Yes, Delete" : function() {
						    	    		  var portfolioId = $(this).data('portfolioId');
						    	    		  var deletePortResult = rpcClient.portfolioService.remove(portfolioId);
						    	    		  if (deletePortResult.result == 0) {
						    		            	$( this ).dialog( "close" );
						    		            	location.reload();
						    		            } else {
						    		            	$( "#deletePortConfirmDialog").append("<div class=\"ui-widget\">" +
						    						"<div class=\"ui-state-error ui-corner-all\" style=\"padding: 0 .7em;\">" +
						    							"<p><span class=\"ui-icon ui-icon-alert\" style=\"float: left; margin-right: .3em;\"></span>" +
						    							"<strong>Alert:</strong>Error: "  + deletePortResult.message + "</p>" +
						    						"</div>" +
						    					"</div>");
						    		            }
						    	        },
						    	        Cancel: function() {
						    	          $( this ).dialog( "close" );
						    	        }
						    	      }
						    	    });
						    	 } else {
						    		 $( "#deletePortConfirmDialog").data("portfolioId" , portfolioId ).dialog("open");
						    	 }
					    	 }
					    	 
					    );
				 }
				 $("#finPeriods" + data.list[i].portfolioId).button({
				      text: true,
				      icons: {
				        primary: "ui-icon-calculator"
				      }
				    }).attr("href" , "finData.jsp?portfolioId=" + data.list[i].portfolioId);
				 
				 $("#cashFlow" + data.list[i].portfolioId).button({
				      text: true,
				      icons: {
				        primary: "ui-icon-image"
				      }
				    }).attr("href", "cashFlow.jsp?portfolioId=" + data.list[i].portfolioId);/*.data("portolioId" ,  data.list[i].portfolioId).click( function() {
				    	 var portfolioId = $(this).data("portolioId" );
				    	 window.location.href  = "cashFlow.jsp?portfolioId=" + portfolioId;
				    });*/
				 $("#scheduling" + data.list[i].portfolioId).button({
				      text: true,
				      icons: {
				        primary: "ui-icon-calendar"
				      }
				    }).attr("href", "scheduling.jsp?portfolioId=" + data.list[i].portfolioId);
				 
				 $("#exportExcel_" + data.list[i].portfolioId).button({
				      text: true,
				      icons: {
				        primary: "ui-icon-calendar"
				      }
				    }).click(function(){
				    	export2Excel($(this).attr('id').split('_')[1]);
				    	});
				 
				 $("#portfolio" + data.list[i].portfolioId + "list").click(function(){    
				        $('.prod-box').animate({opacity:0},function(){
				            $('.grid').removeClass('grid-active');
				            $('.list').addClass('list-active');
				            $('.prod-box').attr('class', 'prod-box-list shadow');
				            $('.prod-box-list').stop().animate({opacity:1});
				        });
				    });

				    $("#portfolio" + data.list[i].portfolioId + "grid").click(function(){  
				        $('.prod-box-list').animate({opacity:0},function(){
				            $('.list').removeClass('list-active');
				            $('.grid').addClass('grid-active');
				            $('.prod-box-list').attr('class', 'prod-box shadow');
				            $('.prod-box').stop().animate({opacity:1});
				        });
				    });
				
				}
				$( "#accordion" ).accordion({
				    
				    activate: function(event, ui) {
			                    $.cookie('saved_index_pf', null);
			                    $.cookie('saved_index_pf', $("#accordion")
			                            .accordion("option", "active"));
			                },
			                active: parseInt($.cookie('saved_index_pf')),
				      heightStyle: "content"
			    });
			}
		}
		
	});
	
	}
  );

function export2Excel(portfolioId){
	var prtFinances = [];
	
	var prjPayments = [];
	
	var tskData = [];
	
	var projectCashFlows = [];
	
	var cfcols = [];
	
	var dataToExport = [];
	
	var finCall = rpcClient.financeService.findAllByPortfolio(portfolioId);
	var finData = finCall.data.list;
	prtFinances.push({
				name:'Portfolio Finances',
				portfolioName: finData.portfolioName,
	    		columns: [
	                      { headertext: "Portfolio Name", datatype: "String", datafield: "portfolioName", width: "200" }
	                     , { headertext: "Finance Amount", datatype: "Number", datafield: "financeAmount", width: "200" }
	                     ,{ headertext: "Finance Until Date", datatype: "DateTime", datafield: "financeUntillDate", width: "200" }
	                 ],
		
				data:finData
				});
	
	var cashFlowdateRangeCall = rpcClient.portfolioService.getPortfolioDateRange(portfolioId);
	
	if (cashFlowdateRangeCall.result == 0){
	    
	    var fmt = new DateFmt("%w %d-%n-%y");
	    
	    cfcols = [{ headertext: "Day", datatype: "String", datafield: "time",  width : "120"}
	    		,{ headertext: "Project", datatype: "String", datafield: "projectCode",  width : "100"}];
		
	    var startDate = new Date(cashFlowdateRangeCall.data[0].time);
		var endDate = new Date(cashFlowdateRangeCall.data[1].time);
		var runningDate = new Date(startDate);
		while (runningDate <= endDate) {

		    var formattedDate = fmt.format(runningDate);
		    cfcols.push({ headertext : formattedDate, datatype: "String", datafield: "time", width : "120"});

		    runningDate.setDate(runningDate.getDate() + 1);

		}
	    
	}
	    
		
	
	var prjCall = rpcClient.projectService.findAllByPortfolio(portfolioId);
	if (prjCall.result == 0) {
		 var projects = prjCall.data.list;
			for (var i = 0; i < projects.length; i++) {
				//var tskCall = rpcClient.taskService.findAllByProject(projects[i].projectId);
				var tasks = projects[i].projectTasks.list;
				
				   tskData.push({
				    		name:'Project Tasks',
				    		projectCode: projects[i].projectCode,
				    		columns: [
				                      { headertext: "Project Code", datatype: "Number", datafield: "projectCode", width: "100" }
				                     ,{ headertext: "Task Name", datatype: "String", datafield: "taskName", width: "200" }
				                     , { headertext: "Task Description", datatype: "String", datafield: "taskDescription",  width: "200" }
				                     , { headertext: "Start Date", datatype: "DateTime", datafield: "tentativeStartDate", width: "150" }
				                     , { headertext: "Duration", datatype: "Number", format: "xxx", datafield: "duration", ishidden: false, width: "100" }
				                     , { headertext: "Dependency", datatype: "String",  datafield: "asDependent",  width: "200" }
				                 ],
				    		data:tasks
				    		});
				     
				    
				
				
				var payments = projects[i].projectPayments.list;
				
				//console.log(payments.toSource());
				prjPayments.push({
					name:'Project Payments',
					projectCode: projects[i].projectCode,
					columns: [
		                      { headertext: "Project Code", datatype: "Number", datafield: "projectCode", width: "100" }
		                     ,{ headertext: "Payment Type", datatype: "String", datafield: "paymentType", width: "200" }
		                     , { headertext: "Payment Amount", datatype: "Number", format: "$xxx.xx", datafield: "paymentAmount", width: "200" }
		                     , { headertext: "Payment Initial Amount", datatype: "Number", format: "$xxx.xx", datafield: "paymentInitialAmount", width: "200" }
		                     , { headertext: "Payment Date", datatype: "DateTime", datafield: "paymentDate", width: "150" }
		                 ],
					data: payments
				});
				
				
				
				var cashFlowCall = rpcClient.portfolioService.getProjectCashFlowData(projects[i].projectId);
				
				if (cashFlowCall.result != 0)
				    continue;
				
				cashFlowData = cashFlowCall.data;
				
				
				
				
				projectCashFlows.push({
					name:'Cash Flows',
					projectCode: projects[i].projectCode,
					columns: cfcols,
					data: payments
				});
				
			}
			dataToExport.push(tskData);
			dataToExport.push(prjPayments);
			dataToExport.push(prtFinances);
			
			
			
			
			
				    $('#dvexcel').btechco_excelexport({
				    	containerid: "dvexcel",
				    	datatype: $datatype.Json,
				    	dataset: dataToExport,
				    	columns: [
				                      { headertext: "Project Code", datatype: "Number", datafield: "project", width: "100" }
				                     ,{ headertext: "Task Name", datatype: "String", datafield: "taskName", width: "100" }
				                     , { headertext: "Task Description", datatype: "String", datafield: "taskDescription", ishidden: false, width: "200" }
				                     , { headertext: "Start Date", datatype: "DateTime", datafield: "tentativeStartDate", width: "150" }
				                     , { headertext: "Duration", datatype: "Number", format: "xxx", datafield: "duration", ishidden: false, width: "100" }
				                 ]
				    });
				
				
			 	
			
			
	} 
	
}

function deleteProject(projectId) {
	$("#deleteProjectConfirmDialog").data("project" ,projectId ).dialog("open");
}

function editProject(projectId , portfolioId) {
	$("#createOrEditProjectDialog").data("projectId" ,projectId ).data("portfolioId" , portfolioId).dialog("open");
}