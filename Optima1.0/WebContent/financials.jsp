<script>
  var currentPage = 6;
</script>
<%@ include file="header.html"%>

<div
	style="border: 20px solid transparent;border-left-width:0px; border-bottom-width: 0px; border-right-width: 0px; border-top-width: 45px; position: absolute; top: 0; height: 100%; width: 100%; box-sizing: border-box; overflow-y: auto"
	id="main" >

	<div class="form-section" id="financialDetails">
		<div id="currentSolution" class="form-section">
			<button id="exportSolutionToCSV" style="margin-bottom:10px;position:absolute;top:60px;left:20px;z-index:10">Export to
				CSV</button>
			<div class="solutionContainer">
				<div id="schedResults">
					<div id="loader-overlay"></div>
				</div>

			</div>
		</div>
	</div>

	<script src="js/formutils.js">
    
  </script>
	<script src="js/financials.js">
    
  </script>

	<!--  all page specific js goes here -->
</div>
<%@ include file="footer.html"%>