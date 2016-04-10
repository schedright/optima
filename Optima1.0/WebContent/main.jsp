<script>var currentPage=1;</script>
<%@ include file="header.html"%>
<div class="mainContainer">

	<div id="main" style="width: 100%;">
		<div style="height:40px"></div>

		<div id="portfolios">
			<button id="addNew" class="btnRight">Add New Portfolio</button>
			
			<div id="accordion" class="accordionAfterBtn"></div>
			<div id="dvexcel"></div>
			<a id="dlink" style=""></a>
		</div>
	</div>
	<div id="newPortfolioDialog" title="Portfolio">
		<p class="validateTips">All form fields are required.</p>

		<form id="newPortfolioForm">
			<fieldset>
				<label for="portName">Name</label> <input type="text"
					name="portName" id="portName"
					class="text ui-widget-content ui-corner-all" placeholder="Enter Portfolio Name, from 3 to 32 characters"/> <label
					for="portDescription">Description</label> <input type="text"
					name="portDescription" id="portDescription" value=""
					class="text ui-widget-content ui-corner-all" placeholder="Enter Description, up to 1024 characters"/>
			</fieldset>
		</form>
	</div>
	<div id="createOrEditProjectDialog" title="Project">
		<%@ include file="views/addEditProjectForm.html"%>
	</div>

	<div id="deleteProjectConfirmDialog" title="Delete Project?">
		<p>
			<span class="ui-icon ui-icon-alert"
				style="float: left; margin: 0 7px 20px 0;"> </span> This project
			will be permanently deleted and cannot be recovered. Are you sure?
		</p>
	</div>


</div>

<script src="js/formutils.js"></script>
<script src="js/jquery.base64.js"></script>
<script src="js/excel.export.js"></script>
<script src="js/portfolio.js"></script>


<%@ include file="footer.html"%>