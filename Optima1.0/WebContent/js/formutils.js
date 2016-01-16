 var rpcClient = new JSONRpcClient("./JSON-RPC");
    function checkLength( o, n, min, max ) {
      if ( o.val().length > max || o.val().length < min ) {
        o.addClass( "ui-state-error" );
        return false;
      } else {
        return true;
      }
    }
 
    function checkRegexp( o, regexp, n ) {
      if ( !( regexp.test( o.val() ) ) ) {
        o.addClass( "ui-state-error" );
        return false;
      } else {
        return true;
      }
    }
    
    
    
    function getFirstN(o , n) {
    	if (o.length <= n + 3) {
    		return o;
    	} else {
    		return o.substring(0, n) + "...";
    	}
    }
   
    
    function DateFmt(fstr) {
        this.formatString = fstr;

        var mthNames = [ "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" ];
        var dayNames = [ "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" ];
        var zeroPad = function(number) {
    	return ("0" + number).substr(-2, 2);
        };

        var dateMarkers = {
    	d : [ 'getDate', function(v) {
    	    return zeroPad(v);
    	} ],
    	m : [ 'getMonth', function(v) {
    	    return zeroPad(v + 1);
    	} ],
    	n : [ 'getMonth', function(v) {
    	    return mthNames[v];
    	} ],
    	w : [ 'getDay', function(v) {
    	    return dayNames[v];
    	} ],
    	y : [ 'getFullYear' ],
    	H : [ 'getHours', function(v) {
    	    return zeroPad(v);
    	} ],
    	M : [ 'getMinutes', function(v) {
    	    return zeroPad(v);
    	} ],
    	S : [ 'getSeconds', function(v) {
    	    return zeroPad(v);
    	} ],
    	i : [ 'toISOString' ]
        };

        this.format = function(date) {
    	var dateTxt = this.formatString.replace(/%(.)/g, function(m, p) {
    	    var rv = date[(dateMarkers[p])[0]]();

    	    if (dateMarkers[p][1] != null)
    		rv = dateMarkers[p][1](rv);

    	    return rv;

    	});

    	return dateTxt;
        };

    }
    
    function showMessage(title, text, type, buttons) {
    	if (!buttons) {
		   buttons = {
			Close : function() {
				$(this).dialog("close");
			}
		};
    	}
    	var icon = '';
    	if (type=='error') {
    		icon = '<div class="errorIcon"></div>';
    	} else if (type=='info') {
    		icon = '<div class="infoIcon"></div>';
    	} else if (type=='success') {
    		icon = '<div class="successIcon"></div>';
    	} else if (type=='warning') {
    		icon = '<div class="warningIcon"></div>';
    	}
		$('<div></div>').appendTo('body')
		  .html('<div><table style="border:0"><tr><td  style="border:0">' + icon + '</td><td style="vertical-align:middle;border:0"><h6>'+ text +'</h6></td></tr></table></div>')
		  .dialog({
		      modal: true, title: title, zIndex: 10000, autoOpen: true,
		      width: '400px', resizable: false,
		      buttons: buttons,
		      close: function (event, ui) {
		          $(this).remove();
		      }
		});			
	}
    
    function showMessageWithProgress(title, text, type, buttons) {
    	if (!buttons) {
		   buttons = {
			Close : function() {
				$(this).dialog("close");
			}
		};
    	}
    	var icon = '';
    	if (type=='error') {
    		icon = '<div class="errorIcon"></div>';
    	} else if (type=='info') {
    		icon = '<div class="infoIcon"></div>';
    	} else if (type=='success') {
    		icon = '<div class="successIcon"></div>';
    	} else if (type=='warning') {
    		icon = '<div class="warningIcon"></div>';
    	}
    	var xx = $('<div></div>').appendTo('body');
    	var htmlElement = xx.html('<div><table style="border:0"><tr><td  style="border:0">' + icon + '</td><td style="vertical-align:middle;border:0"><h6>'+ text +'</h6></td></tr><tr><td colspan=2><div style="border:1px solid;width:100%;height:10px;"><div style="background:green;width:0px;height:100%"></div></div></td></tr></table></div>');
    	var dlg = htmlElement.dialog({
		      modal: true, title: title, zIndex: 10000, autoOpen: true,
		      width: '400px', resizable: false,
		      buttons: buttons,
		      close: function (event, ui) {
		          $(this).remove();
		      }
		});
    	return htmlElement;
	}
