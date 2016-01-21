<script>
	var currentPage = 4;
</script>
<%@ include file="headerNew.html"%>
<div class="mainContainer">
	<div id="main" style="width: 100%;" class="ui-tabs ui-widget ui-widget-content ui-corner-all">
		<label>Projects Priority</label>
		<div class="form-section">
			<div class="one-column project-priorities">
				<ul id="mainAllProjects" data-role="listview" data-inset="true"  class="draggable"></ul>
			</div>
		</div>
	
			<div class="ui-dialog-buttonset">
				<div class="two-columns-right">
					<button id="findFinalSolBtn" class="btnRight">Solve</button>
				</div>
			</div>

		<div id="currentSolution" class="form-section" style="display:none">
			<div class="solutionContainer">
			<div id="schedResults" class="div-Table">
			</div>
			</div>
		</div>
	
	<!-- 
		<div id="loading-indicator" style="display:none" ><div id="loading-indicator-image"><img src="images/Sched-loaderLogo-Grey.png"/><img src="images/Sched-loader.gif"/></div></div>
	-->
		<div id="loading-indicator" style="display:none" ><div id="loading-indicator-image"><img src="images/Sched-loaderLogo.png"/><img src="images/Sched-loader.gif"/></div></div>
	

</div>
	<script src="js/formutils.js"></script>
	<script src="js/scheduleAutomated.js"></script>

<%@ include file="footer.html"%>