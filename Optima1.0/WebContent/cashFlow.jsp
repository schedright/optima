<script>
  var currentPage = 5;
</script>
<%@ include file="header.html"%>

<div class="mainContainer">
	<div id="accordion" class="main-body">

		<div id="projectTabs" >
		
			<div id="loader-overlay"></div>

			<ul>
				<li><a href="#cashFlow">Cash Flow</a></li>
				<li><a href="#flowChart">Cash flow chart</a></li>
			</ul>

			<div id="cashFlow">
				<div style="overflow: hidden; height: 100%">
					<div class="form-section" style="margin: 5px">
						<button id="exportToExel" style="margin-bottom: 10px">Export
							to Excel</button>
						<div id="cashFlowGrid" style="width: 100%; height: 500px;"></div>
					</div>
				</div>
			</div>
			<div id="flowChart">
				<div style="overflow: hidden; height: 100%">
					<div id="cashflowChartDiv"
						style="height: 540px; overflow-x: auto; overflow-y: auto"></div>
				</div>
			</div>
		</div>
	</div>
	<script src="js/formutils.js">
    
  </script>

	<script src="js/cashFlow.js"></script>
	<!--  all page specific js goes here -->
</div>
<%@ include file="footer.html"%>