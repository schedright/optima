<script>var currentPage=5;</script>
<%@ include file="headerNew.html"%>

<div style="width: 960px; margin-left: auto; margin-right: auto;">
<div id="accordion">
  <h3>Cash Flow</h3>
	<div style="overflow:hidden;width:100%;height:100%">
	<div class="form-section" style="margin:5px">
		<button id="exportToExel">Export to Excel</button>
		<button id="exportToExelGraph">Export Graph</button>
		<div id="cashFlowGrid" style="width:960; height:500px;"></div>
	</div>
	</div>
	<h3>Cash flow chart</h3>
	<div style="overflow:hidden;width:100%;height:100%">
	<div id="cashflowChartDiv" style="width:960; height:540px;overflow-x:auto;overflow-y:hidden">
 	</div>
 	</div>
</div>
	 <script src="js/formutils.js"> </script> 

 	<script src="js/cashFlowNew.js"></script>
	<!--  all page specific js goes here -->
</div>
<%@ include file="footer.html"%>