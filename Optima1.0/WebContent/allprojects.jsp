<script>
	var currentPage = 9;
</script>
<%@ include file="header.html"%>
<div class="mainContainer">
	<div id="main" style="width: 100%;"
		class="ui-tabs ui-widget ui-widget-content ui-corner-all">
		<label>All Projects</label> 
		<div id="toolbarContainer" style="width: 100%;height:40px">
		<input type="text" name="searchBox" id="searchBox" style="width: 200px;margin-top:5px" class="text ui-widget-content ui-corner-all" placeholder="filter projects" autocomplete="off"/>
		<div style="float:right;height:100%;width:300px">
			<button id="addProject" class="addPortfolioBtn " style="width:16px;height:16px"></button>
			<button id="editProject" class="projbuttonEdit" style="width:16px;height:16px"></button>
			<button id="deleteProject" class="projbuttonDelete" style="width:16px;height:16px"></button>
		</div>
		</div>
		<div id="gridContainer" class="form-section" style="width: 100%;height:600px"></div>

	</div>

</div>
<script src="js/formutils.js"></script>
<script src="js/allprojects.js"></script>

<%@ include file="footer.html"%>