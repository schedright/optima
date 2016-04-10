<script>
	var currentPage = 8;
</script>
<%@ include file="header.html"%>
<div class="mainContainer">
	<div id="main" style="width: 100%;"
		class="ui-tabs ui-widget ui-widget-content ui-corner-all">
		<label>Users</label>

		<div id="divTasksDepends" class="form-section">
			<div class="two-columns">
				<ul id="usersList" class="sortable">
				
				</ul>
			</div>
			<div class="two-columns">
				<p>
				<button id="addUserBtn">Add user</button>
				</p>
				<p>
				<button id="editUserBtn">Make Admin</button>
				</p>
				<p>
				<button id="deleteUserBtn">Delete user</button>
				</p>
				<p>
				<button id="resetPasswordBtn">Reset Password</button>
				</p>
			</div>
	
		</div>

		<div id="addUserDialog" title="Create User">
			<p class="validateTips">All form fields are required.</p>

			<form id="addUserForm" autocomplete="off">
				<fieldset>
					<label for="addUserFormUserName">Username</label> 
					<input type="text" name="addUserFormUserName" id="addUserFormUserName" class="text ui-widget-content ui-corner-all" placeholder="Enter user name, from 6 to 20 characters" autocomplete="off"/> 
					<label for="addUserFormPassword1">Password</label> 
					<input type="password" name="addUserFormPassword1" id="addUserFormPassword1" value="" class="text ui-widget-content ui-corner-all" placeholder="Enter Password, from 6 to 20 characters" autocomplete="off"/>
					<label for="addUserFormPassword2">Confirm Password</label> 
					<input type="password" name="addUserFormPassword2" id="addUserFormPassword2" value="" class="text ui-widget-content ui-corner-all" placeholder="Enter Password, from 6 to 20 characters" autocomplete="off"/>
					<input type="checkbox" name="addUserFormIsAdmin" id="addUserFormIsAdmin"> Make Administrator
				</fieldset>
			</form>
		</div>

	</div>
	<script src="js/formutils.js"></script>
	<script src="js/users.js"></script>

	<%@ include file="footer.html"%>