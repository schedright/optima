/**
 * 
 */
package com.softpoint.optima.control;

import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.softpoint.optima.OptimaException;
import com.softpoint.optima.ServerResponse;
import com.softpoint.optima.db.User;
import com.softpoint.optima.db.UserRole;

public class UsersController {

	private boolean hasAccess(HttpServletRequest req, String userName) {
		boolean ret = false;
		if (isAdmin(req)) {
			ret = true;
		} else if (userName != null && !userName.isEmpty()) {
			Principal userPrincipal = req.getUserPrincipal();
			String name = userPrincipal.getName();
			ret = name.equals(userName);
		}
		return ret;
	}

	public List<User> getUserNoAccess(HttpServletRequest req, String name) throws OptimaException {
		HttpSession session = req.getSession();
		EntityController<User> controller = new EntityController<User>(session.getServletContext());
		try {
			List<User> dayOffs = controller.findAll(User.class, "Select u from User u where u.userName = ?1 ",
					name);
			return dayOffs;
		} catch (EntityControllerException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<User> getUser(HttpServletRequest req, String name) throws OptimaException {
		if (hasAccess(req, name)) {
			return getUserNoAccess(req,name);
		}
		return null;
	}

	public User getCurrentUser(HttpServletRequest req) throws OptimaException {
		String cUserName = getCurrentUserName(req);
		List<User> users = getUserNoAccess(req,cUserName);
		if (users.size()==1) {
			return users.get(0);
		}
		return null;
	}

	public List<User> getAllUser(HttpServletRequest req) throws OptimaException {
		if (hasAccess(req, null)) {
			HttpSession session = req.getSession();
			EntityController<User> controller = new EntityController<User>(session.getServletContext());
			try {
				List<User> users = controller.findAll(User.class);
				String cuser = getCurrentUserName(req);
				for (User user:users) {
					if (user.getUserName().equals(cuser)) {
						users.remove(user);
						break;
					}
				}
				return users;
			} catch (EntityControllerException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public Boolean isAdmin(HttpServletRequest req) {
		Boolean admin = false;
		try {
			Principal userPrincipal = req.getUserPrincipal();
			String name = userPrincipal.getName();
			List<User> users = getUserNoAccess(req, name);
			if (users.size() == 1) {
				User user = users.get(0);
				for (UserRole role : user.getUserRoles()) {
					if ("admin".equalsIgnoreCase(role.getRoleName())) {
						admin = true;
						break;
					}
				}
			}
		} catch (Exception e) {

		}
		return admin;
	}

	private String getCurrentUserName(HttpServletRequest req) {
		try {
			Principal userPrincipal = req.getUserPrincipal();
			return userPrincipal.getName();
		} catch (Exception e) {

		}
		return "";
	}

	public ServerResponse isAdminUser(HttpServletRequest req) throws OptimaException {
		Boolean admin = isAdmin(req);
		return new ServerResponse("0", "Success", admin ? "TRUE" : "FALSE");
	}

	public ServerResponse addUser(HttpServletRequest req, String userName, String password, Boolean isAdmin) throws OptimaException {
		if (isAdmin(req)) {
			try {
				HttpSession session = req.getSession();
				User user = new User();
				user.setUserName(userName);
				user.setUserPass(password);
				EntityController<User> controller = new EntityController<User>(session.getServletContext());
				controller.merge(user);
	
				UserRole role1 = new UserRole();
				role1.setRoleName("optima");
				role1.setUserName(userName);
				//role1.setUser(user);

				EntityController<UserRole> controller2 = new EntityController<UserRole>(session.getServletContext());
				controller2.merge(role1);
				if (isAdmin) {
					UserRole role2 = new UserRole();
					role2.setRoleName("admin");
					role2.setUserName(userName);
					//role2.setUser(user);
					controller2.merge(role2);
				}
				return new ServerResponse("0", "Success", "");
			} catch (Exception e) {
				
			}
		}
		return new ServerResponse("-1", "Failed to perform action. required Administrator access","");
	}

	public ServerResponse deleteUser(HttpServletRequest req, String userName) throws OptimaException {
		if (isAdmin(req)) {
			try {
				HttpSession session = req.getSession();
				List<User> users = getUserNoAccess(req, userName);
				if (users.size()==1) {
					EntityController<User> userController = new EntityController<User>(session.getServletContext());
					userController.remove(User.class,users.get(0).getUserId());
				}
				return new ServerResponse("0", "Success", "");
			} catch (Exception e) {
				
			}
		}
		return new ServerResponse("-1", "Failed to perform action. required Administrator access","");
	}

	public ServerResponse updateUser(HttpServletRequest req, String oldName, String newName, String password, Boolean admin) throws OptimaException {
		Boolean isAdmin = isAdmin(req);
		Boolean currentUser = getCurrentUserName(req).equals(oldName);
		if (isAdmin || currentUser) {
			try {
				HttpSession session = req.getSession();
				List<User> users = getUserNoAccess(req, oldName);
				if (users.size()==1) {
					User user = users.get(0);
					user.setUserName(newName);
					user.setUserPass(password);

					EntityController<UserRole> c2 = new EntityController<UserRole>(session.getServletContext());
					for (UserRole role:user.getUserRoles()) {
						role.setUserName(newName);
					}

					EntityController<User> controller2 = new EntityController<User>(session.getServletContext());

					if (isAdmin && !currentUser) {
						Boolean isAlreadyAdmin = false;
						for (UserRole role:user.getUserRoles()) {
							if (role.getRoleName().equals("admin")) {
								isAlreadyAdmin = true;
								if (!admin) {
									user.getUserRoles().remove(role);
									c2 = new EntityController<UserRole>(session.getServletContext());
//									c2.remove(UserRole.class,role.getRoleId());
								}
								break;
							}
						}
						if (admin && !isAlreadyAdmin) {
							c2 = new EntityController<UserRole>(session.getServletContext());
							UserRole role2 = new UserRole();
							role2.setRoleName("admin");
							role2.setUserName(newName);
							
							user.getUserRoles().add(role2);
						}
					}
					
					controller2.merge(user);
					
				}
				return new ServerResponse("0", "Success", "");
			} catch (Exception e) {
				
			}
		}
		return new ServerResponse("-1", "Failed to perform action. required Administrator access","");
	}

}
