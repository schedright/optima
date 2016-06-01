$(function() {
  document.title = 'SchedRight - Cash Flow';
  var portfolioId = 0;
  var projectId = 0;

  
  $('#accordion').tabs();


  for ( var i in getURLVariables()) {
    if (i == "portfolioId") {
      portfolioId = getURLVariables()[i];
    } else if (i == "projectId") {
      projectId = getURLVariables()[i];
    }
  }
  
  if (portfolioId) {
    rpcClient.portfolioService.findLight(function(result , exception) {
      if (result.result == 0) {
        $('#titleDiv').html("Enterprise: " + result.data.portfolioName);
      }
    } , portfolioId);
  } else if (projectId) {
    rpcClient.projectService.findLight(function(result , exception) {
      if (result.result == 0) {
        if (result.data.portfolio) {
          $('#titleDiv').html("Enterprise: " + result.data.portfolio.portfolioName);
        } else {
          $('#titleDiv').html("Project: " + result.data.projectName);
        }
      } 
    } , projectId);

  } else {
    $('#titleDiv').html('Cash Flow');
  }

  var allResults = rpcClient.portfolioService.getPortfolioCashFlowDataNew2(portfolioId,projectId);
  var dataView = new Slick.Data.DataView();

  if (allResults.result == 0) {
    var fmt = new DateFmt(
        "%w %d-%n-%y");
    var fmt2 = new DateFmt(
        "%d/%m/%y");

    var pColumns = [
      {
        id : "day",
        name : "Day",
        field : "day",
        minWidth : 160
      }
    ];
    var pData = [];
    var rowCount = 0;
    var startDate = new Date(
        allResults.data.map.start.time);
    var endDate = new Date(
        allResults.data.map.end.time);
    var runningDate = new Date(
        startDate);
    while (runningDate <= endDate) {

      var formattedDate = fmt.format(runningDate);
      pColumns.push({
        id : formattedDate,
        name : formattedDate,
        field : formattedDate,
        minWidth : 160,
        formatter : Slick.Formatters.Currency
      });

      runningDate.setDate(runningDate.getDate() + 1);

    }

    $("#exportToExel").button({
      icons : {
        primary : "ui-icon-calculator"
      },
      text : true
    }).click(function() {

      var str = '';
      var header = '';
      for (var i = 0; i < pColumns.length; i++) {
        header += pColumns[i].name + ',';
      }
      header.slice(0, header.Length - 1);
      str += header + '\r\n';
      for (var i = 0; i < pData.length; i++) {
        var line = '';

        for ( var index in pData[i]) {
          line += pData[i][index] + ',';
        }

        line.slice(0, line.Length - 1);

        str += line + '\r\n';
      }

      var blob = new Blob(
          [
            str
          ],
          {
            type : "text/plain;charset=utf-8"
          });
      var date = new Date();
      saveAs(blob, "CashFlow" + date + ".csv");

    });

    // var portfolioCall = rpcClient.portfolioService.find(portfolioId);
    if (allResults.result == 0) {
      var projects = allResults.data.map.projects.list;
      var totalCashout = [];
      var totalFinanceCost = [];
      var totalBalance = [];
      var totalPayments = [];
      var totalNetBalance = [];
      for (var i = 0; i < projects.length; i++) {
        pData[rowCount] = {};
        pData[rowCount]["day"] = "Project " + projects[i].projectCode + ":";
        rowCount++;
        var projectCashFlowData = allResults.data.map[projects[i].projectId];

        if (projectCashFlowData && projectCashFlowData.map) {
          var data = projectCashFlowData;

          var currentCashOutRow = rowCount;
          pData[rowCount] = {};
          pData[rowCount]["day"] = "Cashout ";
          rowCount++;
          /*
           * var currentFinanceCostRow = rowCount; pData[rowCount] = {}; pData[rowCount]["day"] = "Finance Cost "; rowCount++;
           */

          var currentBalanceRow = rowCount;
          pData[rowCount] = {};
          pData[rowCount]["day"] = "Overdraft ";
          rowCount++;

          var currentPaymentsRow = rowCount;
          pData[rowCount] = {};
          pData[rowCount]["day"] = "Payments ";
          rowCount++;

          /*
           * var currentNetBalaceRow = rowCount; pData[rowCount] = {}; pData[rowCount]["day"] = "Net Balance "; rowCount++;
           */

          pData[rowCount] = {};
          pData[rowCount]["day"] = "";
          rowCount++;

          var runningDate = new Date(
              startDate);
          while (runningDate <= endDate) {

            var formattedDate1 = fmt.format(runningDate);
            var formattedDate2 = fmt2.format(runningDate);

            if (totalCashout[formattedDate1] == null)
              totalCashout[formattedDate1] = 0;
            if (totalFinanceCost[formattedDate1] == null)
              totalFinanceCost[formattedDate1] = 0;
            if (totalBalance[formattedDate1] == null)
              totalBalance[formattedDate1] = 0;
            if (totalPayments[formattedDate1] == null)
              totalPayments[formattedDate1] = 0;
            if (totalNetBalance[formattedDate1] == null)
              totalNetBalance[formattedDate1] = 0;
            pData[currentCashOutRow][formattedDate1] = data.map[formattedDate2 + "," + projects[i].projectId].cashout;
            totalCashout[formattedDate1] += data.map[formattedDate2 + "," + projects[i].projectId].cashout;

            // pData[currentFinanceCostRow][formattedDate1] = data.map[formattedDate2 + ","
            // + projects[i].projectId].financeCost;
            totalFinanceCost[formattedDate1] += data.map[formattedDate2 + "," + projects[i].projectId].financeCost;

            pData[currentBalanceRow][formattedDate1] = data.map[formattedDate2 + "," + projects[i].projectId].balance;
            totalBalance[formattedDate1] += data.map[formattedDate2 + "," + projects[i].projectId].balance;

            pData[currentPaymentsRow][formattedDate1] = data.map[formattedDate2 + "," + projects[i].projectId].payments;

            totalPayments[formattedDate1] += data.map[formattedDate2 + "," + projects[i].projectId].payments;

            /*
             * pData[currentNetBalaceRow][formattedDate1] = data.map[formattedDate2 + "," + projects[i].projectId].netBalance;
             */
            totalNetBalance[formattedDate1] += data.map[formattedDate2 + "," + projects[i].projectId].netBalance;

            runningDate.setDate(runningDate.getDate() + 1);

          }

        }

        var svgGraphCall = rpcClient.portfolioService.getCashFlowSVGGraph(portfolioId,projectId)
        if (svgGraphCall.result && svgGraphCall.data) {
          $("#cashflowChartDiv").html('');
          $("#cashflowChartDiv").append(svgGraphCall.data);
        }
      }

      pData[rowCount] = {};
      pData[rowCount]["day"] = "Portoflio Totals:";
      rowCount++;
      currentCashOutRow = rowCount;
      pData[rowCount] = {};
      pData[rowCount]["day"] = "Cashout ";
      rowCount++;
      currentFinanceCostRow = rowCount;
      pData[rowCount] = {};
      pData[rowCount]["day"] = "Finance Cost ";
      rowCount++;

      currentBalanceRow = rowCount;
      pData[rowCount] = {};
      pData[rowCount]["day"] = "Overdraft ";
      rowCount++;

      currentPaymentsRow = rowCount;
      pData[rowCount] = {};
      pData[rowCount]["day"] = "Payments ";
      rowCount++;

      /*
       * currentNetBalaceRow = rowCount; pData[rowCount] = {}; pData[rowCount]["day"] = "Net Balance "; rowCount++;
       */

      pData[rowCount] = {};
      pData[rowCount]["day"] = "";
      rowCount++;

      runningDate = new Date(
          startDate);
      while (runningDate <= endDate) {
        var formattedDate1 = fmt.format(runningDate);

        pData[currentCashOutRow][formattedDate1] = totalCashout[formattedDate1];

        pData[currentFinanceCostRow][formattedDate1] = totalFinanceCost[formattedDate1];

        pData[currentBalanceRow][formattedDate1] = totalBalance[formattedDate1];

        pData[currentPaymentsRow][formattedDate1] = totalPayments[formattedDate1];

        // pData[currentNetBalaceRow][formattedDate1] = totalNetBalance[formattedDate1];

        runningDate.setDate(runningDate.getDate() + 1);
      }

    }

    pData.getItemMetadata = function(row) {

      if (typeof pData[row] === "undefined")
        return "";
      else {
        var col = pData[row]['day'].trim();

        if (col === "Total") {

          return {
            "cssClasses" : "totalRow"
          };
        }
      }

    };

    var pGrid = new Slick.Grid(
        "#cashFlowGrid",
        pData,
        pColumns,
        {
          editable : true,
          enableAddRow : true,
          enableCellNavigation : true,
          enableColumnReorder : true
        });
  }

  setTimeout(function() {
    var solutionResponse = rpcClient.portfolioService.hasSolution(portfolioId,projectId);
    if (solutionResponse.result == 0 && solutionResponse.data && solutionResponse.data == 'TRUE') {
      var result = rpcClient.portfolioService.isInvalidSolution(portfolioId);
      if (result) {
        showMessage('Portfolio has changed', 'The portfolio has changed after last solve, you might need to re-solve again', 'info');

      }
    }
  }, 0);
});

function getURLVariables() {
  var getVars = [];
  var split = location.href.split('?')[1].split('&');
  if (split != null) {
    for ( var i in split) {
      var parts = split[i].split('=');
      getVars[parts[0]] = parts[1];
    }
  }

  return getVars;
}

function DateFmt(fstr) {
  this.formatString = fstr;

  var mthNames = [
      "Jan",
      "Feb",
      "Mar",
      "Apr",
      "May",
      "Jun",
      "Jul",
      "Aug",
      "Sep",
      "Oct",
      "Nov",
      "Dec"
  ];
  var dayNames = [
      "Sun",
      "Mon",
      "Tue",
      "Wed",
      "Thu",
      "Fri",
      "Sat"
  ];
  var zeroPad = function(number) {
    return ("0" + number).substr(-2, 2);
  };

  var dateMarkers = {
    d : [
        'getDate',
        function(v) {
          return zeroPad(v);
        }
    ],
    m : [
        'getMonth',
        function(v) {
          return zeroPad(v + 1);
        }
    ],
    n : [
        'getMonth',
        function(v) {
          return mthNames[v];
        }
    ],
    w : [
        'getDay',
        function(v) {
          return dayNames[v];
        }
    ],
    y : [
      'getFullYear'
    ],
    H : [
        'getHours',
        function(v) {
          return zeroPad(v);
        }
    ],
    M : [
        'getMinutes',
        function(v) {
          return zeroPad(v);
        }
    ],
    S : [
        'getSeconds',
        function(v) {
          return zeroPad(v);
        }
    ],
    i : [
      'toISOString'
    ]
  };

  this.format = function(date) {
    var dateTxt = this.formatString.replace(/%(.)/g, function(m,
        p) {
      var rv = date[(dateMarkers[p])[0]]();

      if (dateMarkers[p][1] != null)
        rv = dateMarkers[p][1](rv);

      return rv;

    });

    return dateTxt;
  };

}
