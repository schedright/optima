<script>
  var currentPage = 6;
</script>
<%@ include file="header.html"%>

<div
	class="mainContainer"
	id="main">
	<div class="main-body">
			<button id="exportSolutionToCSV" style="margin-bottom:10px;position:absolute;top:50px;left:20px;z-index:10">Export to
				CSV</button>
				<div id="schedResults" class="projectTabs">
					<div id="loader-overlay"></div>
				</div>
	</div>

	<script src="js/formutils.js">
    
  </script>
	<script src="js/financials.js">
    
  </script>

	<!--  all page specific js goes here -->
</div>
<%@ include file="footer.html"%>