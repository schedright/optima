$(function() {

	var clientListResult = rpcClient.clientService.findAll();
	var data = clientListResult.data;
	if (clientListResult.result ==  0 && data != null ) {
		for (var index = 0 ; index < data.list.length ; index++ ) {
			var cityName = (data.list[index].city!=null)?data.list[index].city.locationName:"";
			var provinceName = (data.list[index].province!=null)?data.list[index].province.locationName:"";
			var countryName = (data.list[index].country!=null)?data.list[index].country.locationName:"" ;
			var clientInfo = "<h3>" + data.list[index].clientName + "</h3><div>" 
			+ data.list[index].clientName + "<br>"
			+ data.list[index].clientAddressStreet + "<br>"			
			+ cityName + "<br>"	
			+ provinceName + "<br>"
			+ countryName + "<br>"
			+ " <br><br><div id=\"toolbar" + data.list[index].clientId  + "\" class=\"ui-widget-header ui-corner-all\">"
			+  " <button id=\"editClient" + data.list[index].clientId + "\" value=\"Edit This Location\">Edit This Client</button> "
			+  " <button id=\"deleteClient" + data.list[index].clientId + "\" value=\"Delete This Location\">Delete This Client</button> "
			+ "</div>"
			+ "</div>"
			$("#clientList").append(clientInfo);

			$("#editClient" + data.list[index].clientId ).data( "clientId" , data.list[index].clientId).button({
			    text: true,
			    icons: {
			      primary: "ui-icon-document"
			    },
			  }).click( function() {
				  var clientId = $(this).data("clientId");
				  $( "#newClientDialog").data("clientId" , clientId).dialog("open");
			});
			$("#deleteClient" + data.list[index].clientId ).data( "clientId" , data.list[index].clientId).button({
			    text: true,
			    icons: {
			      primary: "ui-icon-trash"
			    },
			  }).click( function() {
				  var clientId = $(this).data("clientId");
				  $("#deleteClientConfirmDialog").data("clientId" , clientId).dialog("open");
			});
		}
		$("#clientList").accordion({
		      heightStyle: "content"
	    });
		
		$("#newClientDialog").dialog({
		  autoOpen: false,
	      height: 600,
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
	        open : function () {
	        	$("#clientCity").empty();
	        	$("#clientCity").append("<option value=\"0\"></option>");
	        	$("#clientProvince").empty();
	        	$("#clientProvince").append("<option value=\"0\"></option>");
	        	$("#clientCountry").empty();
	        	$("#clientCountry").append("<option value=\"0\"></option>");
	        	 var call =  rpcClient.locationService.findAllByType("CITY");
	        	 var cities = call.data;
	        	 if (call.result ==  0 && cities != null ) {
	        		for (var index = 0 ; index < cities.list.length ; index++ ) {
	        			$("#clientCity").append("<option value=\"" + cities.list[index].locationId + "\">" + cities.list[index].locationName + "</option>");
	        		}
	        	 }
	        	 
	        	 call =  rpcClient.locationService.findAllByType("PROVINCE");
	        	 var provinces = call.data;
	        	 if (call.result ==  0 && provinces != null ) {
	        		for (var index = 0 ; index < provinces.list.length ; index++ ) {
	        			$("#clientProvince").append("<option value=\"" + provinces.list[index].locationId + "\">" + provinces.list[index].locationName + "</option>");
	        		}
	        	 }
	        	 
	        	 call =  rpcClient.locationService.findAllByType("COUNTRY");
	        	 var countries = call.data;
	        	 if (call.result ==  0 && countries != null ) {
	        		for (var index = 0 ; index < countries.list.length ; index++ ) {
	        			$("#clientCountry").append("<option value=\"" + countries.list[index].locationId + "\">" + countries.list[index].locationName + "</option>");
	        		}
	        	 }
	        	 var clientId = $(this).data("clientId");
	        	 if (clientId != null) {
	        		 var clientInfoCall = rpcClient.clientService.find(clientId);
	        		 if (clientInfoCall.result == 0) {
	        			 var currentClient = clientInfoCall.data;
	        			 $("#clientName").val(currentClient.clientName);
	        			 $("#clientStreetAddress").val(currentClient.clientAddressStreet);
	        			 
	        			 $("#clientCity option[value=" + ( currentClient.city!=null?currentClient.city.locationId:"0") + "]").attr('selected', 'selected');
	        			 $("#clientProvince option[value=" + ( currentClient.province!=null?currentClient.province.locationId:"0") + "]").attr('selected', 'selected');
	        			 $("#clientCountry option[value=" + (currentClient.country!=null?currentClient.country.locationId:"0") + "]").attr('selected', 'selected');
	        			 $("#clientPostalCode").val(currentClient.clientAddressPostalCode);	
	        		 }
	        	 } else {
	        		 $("#clientName").val("");
        			 $("#clientStreetAddress").val("");
        			 
        			 $("#clientCity option[value=\"0\"]").attr('selected', 'selected');
        			 $("#clientProvince option[value=\"0\"]").attr('selected', 'selected');
        			 $("#clientCountry option[value=\"0\"]").attr('selected', 'selected');
        			 $("#clientPostalCode").val("");	
	        	 }
	        },
	      buttons: {
		        "Save": function() {
		        	 
		        	 $("#clientName").removeClass( "ui-state-error" );
        			 $("#clientStreetAddress").removeClass( "ui-state-error" );
        			 $("#clientCity").removeClass( "ui-state-error" );
        			 $("#clientProvince").removeClass( "ui-state-error" );
        			 $("#clientCountry").removeClass( "ui-state-error" );
        			 $("#clientPostalCode").removeClass( "ui-state-error" );
		        	
		        	 
		        	 var bValid = true;
		        	 bValid = bValid && checkLength( $("#clientName") , "clientName", 3, 32 );
		        	 if (bValid) {
		        		 var clientId = $(this).data("clientId");
		        		 var call;
		        		
						if (clientId == null) {
							call = rpcClient.clientService.create($(
									"#clientName").val(), $(
									"#clientStreetAddress").val(), $(
									"#clientPostalCode").val(), $(
									'#clientCity option:selected').val(),
									$('#clientProvince option:selected')
											.val(),
									$('#clientCountry option:selected')
											.val());
						} else {
							
							
//							alert('name: ' + $("#clientName").val());
//							alert('street: ' + $("#clientStreetAddress").val());
//							alert('pc: ' + $("#clientPostalCode").val());
//							alert('City: ' + $('#clientCity option:selected').val());
//							alert('Province: ' + $('#clientProvince option:selected').val());
//							alert('Country: ' + $('#clientCountry option:selected').val());
							
						//	update(HttpSession session , int key , String name , String streetAddress , String postalCode , int city , int province , int country) 
							
							call = rpcClient.clientService.update(
									clientId, $("#clientName").val(), $(
											"#clientStreetAddress").val(),
									$("#clientPostalCode").val(), $(
											'#clientCity option:selected')
											.val(),
									$('#clientProvince option:selected')
											.val(),
									$('#clientCountry option:selected')
											.val());
						}
						if (call.result == 0) {
							$(this).dialog("close");
							location.reload();
						} else {
							alert(call.result + ":" + call.message);
						}
						
		        	 }
		        	 
		         },
		        Cancel: function() {
		          $( this ).dialog( "close" );
		        }
	      }
	    });
		
		$("#addNewClient").button({
		    text: true,
		    icons: {
		      primary: "ui-icon-circle-plus"
		    },
		  }).click( function() {
				 $( "#newClientDialog").data("clientId", null).dialog("open");
		});
		
	} else  {
		 alert(clientListResult.result + ":" + clientListResult.message);
	}
	
	$("#deleteClientConfirmDialog").dialog({ 
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
	        "Delete This Client": function() {
	        	var clientId = $(this).data("clientId");
	        	var call =  rpcClient.clientService.remove( clientId );
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
        
	});

});