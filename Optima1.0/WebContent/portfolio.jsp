<script>
	var currentPage = 1;
</script>
<%@ include file="header.html"%>
<div class="mainContainer">
	<div id="main" style="width: 100%;"
		class="main-body">
		<div id="toolbarContainer" style="width: 100%;height:40px;padding-top:6px;">
			<Label for="portfolioSelect" style="float:left;padding-top:12px">Select Portfolio: </Label>
			<select id="portfolioSelect" class="portfolioSelect" >
		</select>
		<div class="portfolioButtonsContainer">
		</div>
		<div class="toolbarButtonsContainer">
			<button id="addPortfolio" class="allProjects_add" title="Add New Portfolio"></button>
			<button id="editPortfolio" class="allProjects_edit" title="Edit Portfolio"></button>
			<button id="deletePortfolio" class="allProjects_delete" title="Delete Portfolio"></button>
			
			<button id="addProjectLink" class="projectlink_add" title="Include project"></button>
			<button id="removeProjectLink" class="projectlink_remove" title="Remove project"></button>
		</div>
		
		</div>
		<div class="gridScroll" style="top:46px">
			<div id="gridContainer" class="projectsGrid"></div>
		</div>

	</div>

	<div id="newPortfolioDialog" title="Portfolio">
		<p class="validateTips">All form fields are required.</p>
	
		<form id="newPortfolioForm">
			<fieldset>
				<label for="portName">Name:</label> <input type="text"
					name="portName" id="portName"
					class="text ui-widget-content ui-corner-all" placeholder="Enter Portfolio Name, from 3 to 32 characters"/> <label
					for="portDescription">Description:</label> <input type="text"
					name="portDescription" id="portDescription" value=""
					class="text ui-widget-content ui-corner-all" placeholder="Enter Description, up to 1024 characters"/>
			</fieldset>
		</form>
	</div>

	<div id="linkToProjectDialog" title="Include Project">
		<p class="validateTips">Select project to include:</p>
	
		<form id="newPortfolioForm">
			<fieldset>
				<select id="projectsSelect" class="projectsSelect"></select>
			</fieldset>
		</form>
	</div>

</div>

<script src="js/formutils.js"></script>
<script src="js/portfolio.js"></script>

<%@ include file="footer.html"%>