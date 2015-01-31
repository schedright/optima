$(document).ready( function() {
	
	rpcClient.locationService.findAll(function(result , exception) {
		var data = result.data;
		if (result.result ==  0 && data != null ) {
			for (var index = 0 ; index < data.list.length ; index++ ) {
				var locationInfo = "<h3>" + data.list[index].locationName + "</h3><div>" 
				+ data.list[index].locationName + "<br>"
				+ data.list[index].locationType + "<br>"	
				+ (data.list[index].parentLocation!=null?data.list[index].parentLocation.locationName:"")
				+ " <br><br><div id=\"toolbar" + data.list[index].locationId  + "\" class=\"ui-widget-header ui-corner-all\">"
				+  " <button id=\"editLocation" + data.list[index].locationId + "\" value=\"Edit This Location\">Edit This Location</button> "
				+  " <button id=\"deleteLocation" + data.list[index].locationId + "\" value=\"Delete This Location\">Delete This Location</button> "
				+ "</div>"
				+ "</div>";
				$("#locationList").append(locationInfo);

				$("#editLocation" + data.list[index].locationId ).data( "locationId" , data.list[index].locationId).button({
				    text: true,
				    icons: {
				      primary: "ui-icon-document"
				    },
				  }).click( function() {
					  var locationId = $(this).data("locationId");
					  $( "#newLocationDialog").data("locationId" , locationId).dialog("open");
				});
				$("#deleteLocation" + data.list[index].locationId ).data( "locationId" , data.list[index].locationId).button({
				    text: true,
				    icons: {
				      primary: "ui-icon-trash"
				    },
				  }).click( function() {
					  var locationId = $(this).data("locationId");
					  $("#deleteLocationConfirmDialog").data("locationId" , locationId).dialog("open");
				});
			}
			$("#locationList").accordion({
			      heightStyle: "content"
		    });
		} else  {
			 alert(result.result + ":" + result.message);
		}
	});
	
	
		
		
		$("#newLocationDialog").dialog({
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
	        open : function () {
	        	$("#parentLocation").empty();
	    		$("#parentLocation").append("<option value=\"0\"></option>");
	        	
	        	 var parentLocationCall =  rpcClient.locationService.findAll();
	        	 var parentLocations = parentLocationCall.data;
	        	 if (parentLocationCall.result ==  0 && parentLocations != null ) {
	        		for (var index = 0 ; index < parentLocations.list.length ; index++ ) {
	        			$("#parentLocation").append("<option value=\"" + parentLocations.list[index].locationId + "\">" + parentLocations.list[index].locationName + "</option>");
	        		}
	        	 }
	        	 var locationId = $(this).data("locationId");
	        	 if (locationId != null) {
	        		 var locationInfoCall = rpcClient.locationService.find(locationId);
	        		 if (locationInfoCall.result == 0) {
	        			 var currentLocation = locationInfoCall.data;
	        			 $("#locationName").val(currentLocation.locationName);
	        			 $("#locationType option[value=" + currentLocation.locationType + "]").attr('selected', 'selected');
	        			 $("#parentLocation option[value=" + currentLocation.parentLocation.locationId + "]").attr('selected', 'selected');
	        		 }
	        	 } else {
	        		 $("#locationName").val("");
        			 $("#locationType option[value=\"\"]").attr('selected', 'selected');
        			 $("#parentLocation option[value=\"0\"]").attr('selected', 'selected');
	        	 }
	        },
	      buttons: {
		        "Save": function() {
		        	 $("#locationName").removeClass( "ui-state-error" );
		        	 $("#locationType").removeClass( "ui-state-error" );
		        	
		        	 
		        	 var bValid = true;
		        	 bValid = bValid && checkLength( $("#locationName") , "locationName", 3, 32 );
		        	 if ($("#locationType").val() == "") {
		        		 bValid = false;
		        		 $("#locationType").addClass( "ui-state-error" );
		        	 }
		        	 
		        	 if ($("#locationType").val() == "") {
		        		 bValid = false;
		        		 $("#locationType").addClass( "ui-state-error" );
		        	 }
		        	 
		        	 if (bValid) {
		        		 var locationId = $(this).data("locationId");
		        		 var call;
		        		 if (locationId == null) {
		        			call =  rpcClient.locationService.create( $("#locationName").val() , 
		        				 $('#locationType option:selected').val() , 
		        				 $('#parentLocation option:selected').val() );
		        		 } else {
		        			 call =  rpcClient.locationService.update( locationId , $("#locationName").val() , 
			        				 $('#locationType option:selected').val() , 
			        				 $('#parentLocation option:selected').val() );
		        		 }
		        		  if (call.result == 0) {
		        			 $( this ).dialog( "close" );
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
		
		$("#addNewLocation").button({
		    text: true,
		    icons: {
		      primary: "ui-icon-circle-plus"
		    },
		  }).click( function() {
				 $( "#newLocationDialog").data("locationId", null).dialog("open");
		});
		
	
	
	$("#deleteLocationConfirmDialog").dialog({ 
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
	        "Delete This Location": function() {
	        	var locationId = $(this).data("locationId");
	        	var call =  rpcClient.locationService.remove( locationId );
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