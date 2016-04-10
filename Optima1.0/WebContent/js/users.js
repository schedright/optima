var isAdminCheck = rpcClient.usersService.isAdmin();
if (!isAdminCheck) {
	window.location.href = "main.jsp";
}

var allUsers = rpcClient.usersService.getAllUser();
var allUsersMap = {};
var selected = {};

var enableButtons = function(enable) {
	$('#editUserBtn').prop("disabled", !enable);
	$('#deleteUserBtn').prop("disabled", !enable);
	$('#resetPasswordBtn').prop("disabled", !enable);
}

var isAdmin = function(user) {
	var admin = false;
	for (var x = 0; x < user.userRoles.list.length; x++) {
		var role = user.userRoles.list[x];
		if (role.roleName == "admin") {
			admin = true;
			break;
		}
	}
	return admin
};

$('#addUserBtn').on('click', function() {
	$("#addUserDialog").dialog("open");
});

$('#editUserBtn').on('click', function() {
	var user = allUsersMap[selected.selection.textContent];
	var result = rpcClient.usersService.updateUser(
			user.userName, user.userName,
			user.userPass, !user.isAdmin);
	if (result && result.result == 0) {
		// success, reload
		location.reload();
	} else {
		showMessage("Add/Remove Permissions", 'Error:'
				+ result.message, 'error');
	}

});

$('#deleteUserBtn').on(
		'click',
		function() {
			var buttons = {
				Yes : function() {
					var user = allUsersMap[selected.selection.textContent];
					$(this).dialog("close");
					if (user) {
						var result = rpcClient.usersService
								.deleteUser(user.userName);
						if (result && result.result == 0) {
							// success, reload
							location.reload();
						} else {
							showMessage("Delete User", 'Error:'
									+ result.message, 'error');
						}
					}
				},
				No : function() {
					$(this).dialog("close");
					return false;
				}
			}
			showMessage('Delete User',
					'The selected user will be deleted permanently!',
					'warning', buttons);
		});

$('#resetPasswordBtn')
		.on(
				'click',
				function() {
					var buttons = {
						Yes : function() {
							var user = allUsersMap[selected.selection.textContent];
							$(this).dialog("close");
							if (user) {
								var result = rpcClient.usersService.updateUser(
										user.userName, user.userName,
										"Password123", user.isAdmin);
								if (result && result.result == 0) {
									// success, reload
									location.reload();
								} else {
									showMessage("Reset User", 'Error:'
											+ result.message, 'error');
								}
							}
						},
						No : function() {
							$(this).dialog("close");
							return false;
						}
					}
					showMessage(
							'Reset Password',
							'User password will reset to default password (Password123), User must change it immediatly',
							'warning', buttons);
				});

var loadUsers = function(users) {
	$("#usersList").html('');
	for (var i = 0; i < users.length; i++) {
		users[i].isAdmin = isAdmin(users[i]);
		allUsersMap[users[i].userName] = users[i];

		var li = $('<li></li>').addClass('ui-state-default').attr('id',
				users[i].userId).text(users[i].userName);
		li.attr('title', users[i].userName);

		$("#usersList").append(li);
	}
}
enableButtons(false);

$(function() {
	$("#usersList").selectable();

	$('#usersList').selectable(
			{
				selected : function(event, ui) {
					selected.selection = ui.selected;
					var user = allUsersMap[ui.selected.textContent];
					$("#editUserBtn").html(
							user.isAdmin ? 'Remove Admin' : 'Make Admin');
					enableButtons(true);
				},
				unselected : function(event, ui) {
					selected.selection = null;
					enableButtons(false);
				}
			});

	document.title = 'User Management';

	if (allUsers && allUsers.list) {
		loadUsers(allUsers.list);
	}
	var addUserFormUserName = $("#addUserFormUserName");
	var addUserFormPassword1 = $("#addUserFormPassword1");
	var addUserFormPassword2 = $("#addUserFormPassword2");
	$("#addUserDialog")
			.dialog(
					{
						autoOpen : false,
						height : 500,
						width : 450,
						modal : true,
						show : {
							effect : "blind",
							duration : 1000
						},
						hide : {
							effect : "fade",
							duration : 1000
						},
						buttons : {
							"Create User" : function() {
								var bValid = true;

								bValid = bValid
										&& checkLength(addUserFormUserName,
												"addUserFormUserName", 6, 20);
								bValid = bValid
										&& checkLength(addUserFormPassword1,
												"addUserFormPassword1", 6, 20);
								if ((addUserFormPassword1.val() !== addUserFormPassword2
										.val())) {
									addUserFormPassword2
											.addClass("ui-state-error");
									bValid = false;
								}

								if (bValid) {
									var makeAdmin = $('#addUserFormIsAdmin')
											.prop("checked");
									var result = rpcClient.usersService
											.addUser(addUserFormUserName.val(),
													addUserFormPassword1.val(),
													makeAdmin);
									if (result && result.result == 0) {
										// success, reload
										location.reload();
									} else {
										showMessage("Create User", 'Error:'
												+ result.message, 'error');
									}
								}
							},
							Cancel : function() {
								$(this).dialog("close");
							}
						},
						close : function() {
							allFields.val("").removeClass("ui-state-error");
						}
					});
})