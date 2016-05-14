<script>
	var currentPage = 9;
</script>
<%@ include file="header.html"%>
<div class="mainContainer">
	<div id="main" style="width: 100%;"
		class="main-body">
		<div id="toolbarContainer" style="width: 100%;height:40px">
		<input type="text" name="searchBox" id="searchBox" style="width: 200px;margin-top:5px" class="text ui-widget-content ui-corner-all" placeholder="filter projects" autocomplete="off"/>
		<div class="toolbarButtonsContainer">
			<button id="addProject" class="allProjects_add" title="Add New Project"></button>
			<button id="editProject" class="allProjects_edit" title="Edit Project"></button>
			<button id="deleteProject" class="allProjects_delete" title="Delete Project"></button>
			<button id="importsProjects" class="allProjects_import" title="Import"></button>
			<button id="exportProject" class="allProjects_export" title="Export"></button>
		</div>
		</div>
		<div class="gridScroll">
			<div id="gridContainer" class="projectsGrid"></div>
		</div>

	</div>

	<div id="createOrEditProjectDialog" title="Project">
		<%@ include file="views/addEditProjectForm.html"%>
	</div>
</div>

<script src="js/formutils.js"></script>
<script src="js/allprojects.js"></script>

<%@ include file="footer.html"%>