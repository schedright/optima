<script>var currentPage=6;</script>
<%@ include file="headerNew.html"%>

<div style="width: 960px; margin-left: auto; margin-right: auto;">

	<div class="form-section" id="financialDetails">
		<div id="currentSolution" class="form-section">
			<H2 style="color:green">Solution</H2>
			<button id="exportSolutionToCSV" class="btnRight">Export to CSV</button>
			<div class="solutionContainer">
			<div id="schedResults" class="div-Table">
			</div>
			</div>
		</div>
	</div>
	
	 <script src="js/formutils.js"> </script> 
	 <script src="js/financials.js"> </script> 

	<!--  all page specific js goes here -->
</div>
<%@ include file="footer.html"%>