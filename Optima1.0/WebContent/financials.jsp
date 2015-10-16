<script>var currentPage=6;</script>
<%@ include file="headerNew.html"%>

<div style="margin:20px 20px 20px 20px;">

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