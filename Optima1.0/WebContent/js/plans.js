var daysOffList = rpcClient.projectService.getPlan();

$(function() {
	var projects = daysOffList.data.list;
	var allYears = [];
	for (var i=0;i<projects.length;i++) {
		var proj = projects[i].map;
		var yearsMap = proj.Details.map;
		for (var k in yearsMap) {
			if (yearsMap[k]) {
				if (allYears.indexOf(k)==-1) {
					allYears.push(k);
				}
			}
		}
	}
	allYears.sort();
	var pColumns = [ {
	    id : "Year",
	    name : "Year",
	    field : "Year",
	    minWidth : 120
	} ];

	for (var i=0;i<allYears.length;i++) {
		  pColumns.push({
				id : allYears[i],
				name : allYears[i],
				field : allYears[i],
				minWidth : 120
			    });
	}
	pColumns.push({
		id : "Total",
		name : "Total",
		field : "Total",
		minWidth : 120
	    });
	var pData = [];
	var getTotal = function(map) {
		if (!map || !map.map) {
			return 0;
		} else {
			var sum = 0;
			for (var key in map.map) {
				sum += map.map[key];
			}
			return sum;
		}
	};
	var allRowsTotal = 0;
	for (var i=0;i<projects.length;i++) {
		var proj = projects[i].map;
		var yearsMap = proj.Details.map;
		
		var row = {"Year":proj.Project.projectDescription};
		var t = 0;
		for (var p=0;p<allYears.length;p++) {
			var payment = getTotal(yearsMap[allYears[p]]);
			if (payment) {
				t+=payment;
				row[allYears[p]] = parseFloat(payment).toFixed(2);
			}
		}
		row["Total"] = parseFloat(t).toFixed(2);
		allRowsTotal += t;
		pData.push(row);
	}	
	pData.push({"Year":"Total", "Total":parseFloat(allRowsTotal).toFixed(2)})
	
	var pGrid = new Slick.Grid("#projectPayments", pData, pColumns, {
	    editable : false,
	    enableAddRow : false,
	    enableCellNavigation : true,
	    enableColumnReorder : false
	  });

})
