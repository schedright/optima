<%@ include file="header.html"%>

<div id="main" style="width: 100%;">
	<ul>
		<li><a href="#portfolios">Manage Portfolios</a></li>
		<li><a href="#locations">Manage Locations</a></li>
		<li><a href="#clients">Manage Clients</a></li>

	</ul>

	<div id="portfolios">

		<button id="addNew">Add New Portfolio</button>

		<div id="accordion"></div>
		<div id="dvexcel"></div>
		<a id="dlink" style="display=none" ></a>
	</div>
	<div id="locations">
		<%@ include file="views/locations.html"%>
	</div>
	<div id="clients">
		<%@ include file="views/clients.html"%>
	</div>
</div>
<div id="newPortfolioDialog" title="Portfolio">
	<p class="validateTips">All form fields are required.</p>

	<form id="newPortfolioForm">
		<fieldset>
			<label for="portName">Name</label> <input type="text" name="portName"
				id="portName" class="text ui-widget-content ui-corner-all" /> <label
				for="portDescription">Description</label> <input type="text"
				name="portDescription" id="portDescription" value=""
				class="text ui-widget-content ui-corner-all" />
		</fieldset>
	</form>
</div>
<div id="createOrEditProjectDialog" title="Project">
	<%@ include file="views/addEditProjectForm.html"%>
</div>

<div id="deleteProjectConfirmDialog" title="Delete Project?">
	<p>
		<span class="ui-icon ui-icon-alert" style="float: left; margin: 0 7px 20px 0;">
		</span> This project will be permanently deleted and cannot be recovered. Are you sure?
	</p>
</div>
<script src="js/formutils.js"></script>
<script src="js/jquery.base64.js"></script>
<script src="js/excel.export.js"></script>
<script src="js/portfolio.js"></script>


<%@ include file="footer.html"%>