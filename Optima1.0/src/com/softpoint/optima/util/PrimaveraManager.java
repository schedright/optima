package com.softpoint.optima.util;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.softpoint.optima.OptimaException;
import com.softpoint.optima.ServerResponse;
import com.softpoint.optima.control.EntityController;
import com.softpoint.optima.control.EntityControllerException;
import com.softpoint.optima.control.ProjectController;
import com.softpoint.optima.control.TaskController;
import com.softpoint.optima.db.DaysOff;
import com.softpoint.optima.db.PortfolioLight;
import com.softpoint.optima.db.PrimaveraProject;
import com.softpoint.optima.db.Project;
import com.softpoint.optima.db.ProjectLight;
import com.softpoint.optima.db.ProjectTask;
import com.softpoint.optima.db.TaskDependency;

public class PrimaveraManager {
	private static final DateFormat PRIMAVERA_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	public String importPrimaveraFile(File xmlFile, HttpSession session) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("<ul>");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(xmlFile);

		EntityController<PrimaveraProject> primController = new EntityController<PrimaveraProject>(session.getServletContext());
		EntityController<Project> projectController = new EntityController<Project>(session.getServletContext());
		EntityController<ProjectTask> taskController = new EntityController<ProjectTask>(session.getServletContext());

		// optional, but recommended
		// read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
		doc.getDocumentElement().normalize();

		List<Node> projects = getProjects(doc);
		if (!projects.isEmpty()) {
			for (Node project : projects) {
				String name = getElementChildAttributeValue(project, "name");
				String guid = getElementChildAttributeValue(project, "guid");
				String weekends = getWeekend(doc);
				String description = getElementChildAttributeValue(project, "description");
				if (description == null) {
					description = "";
				}
				if (name == null || name.length() < 3) {
					throw new Exception("Name doesn't exist or invalid");
				}
				String temp = getElementChildAttributeValue(project, "plannedstartdate");
				// 2016-05-01T00:00:00
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				Date startDate = formatter.parse(temp);

				PrimaveraProject primaveraProject = getPrimaveraProject(session, guid);
				boolean existingProject = true;
				if (primaveraProject == null) {
					existingProject = false;
					primaveraProject = new PrimaveraProject();
					primaveraProject.setProjectGuid(guid);
				}
				if (primaveraProject.getProject() == null) {
					sb.append("<li>Creating new project \"" + name + "\"</li>");
					Project proj = new Project();
					proj.setDelayPenaltyAmount(BigDecimal.valueOf(0.0));
					proj.setCollectPaymentPeriod(0);
					proj.setPaymentRequestPeriod(0);
					primaveraProject.setProject(proj);
					primaveraProject.getProject().setProjectCode(name);
				} else {
					sb.append("<li>Updating existing project \"" + name + "\"</li>");
				}

				primaveraProject.getProject().setProjectName(name);
				primaveraProject.getProject().setProjectDescription(description);
				primaveraProject.getProject().setPropusedStartDate(startDate);
				primaveraProject.getProject().setWeekend(weekends);

				// cal = brow
				/*
				 * <StandardWorkWeek> <StandardWorkHours> <DayOfWeek>Sunday</DayOfWeek> <WorkTime>
				 */

				Map<String, ProjectTask> guid2taskMap = new HashMap<String, ProjectTask>();
				Map<String, ProjectTask> existingGuid2TaskMap = new HashMap<String, ProjectTask>();
				List<TaskDependency> newDependencies = new ArrayList<TaskDependency>();
				List<TaskDependency> updatedDependencies = new ArrayList<TaskDependency>();
				if (primaveraProject.getProject().getProjectTasks() != null) {
					for (ProjectTask tsk : primaveraProject.getProject().getProjectTasks()) {
						if (tsk.getTaskGuid() != null) {
							existingGuid2TaskMap.put(tsk.getTaskGuid(), tsk);
						}
					}
				}

				Map<String, ProjectTask> objectId2TaskMap = new HashMap<String, ProjectTask>();
				importTasks(project, primaveraProject, guid2taskMap, existingGuid2TaskMap, objectId2TaskMap);

				SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

				deleteVacations(session, primaveraProject.getProject());
				List<DaysOff> vacations = ImportVacations(primaveraProject, doc, formatter2);

				try {
					byte[] bytes = Files.readAllBytes(xmlFile.toPath());
					String content = new String(bytes, "UTF-8");
					primaveraProject.setFileContent(content);
				} catch (Exception e) {
				}

				if (!guid2taskMap.isEmpty()) {
					sb.append("<li>Creating " + guid2taskMap.values().size() + " new task</li>");
				}

				if (existingProject) {
					int updated = ((primaveraProject.getProject().getProjectTasks() == null) ? 0 : primaveraProject.getProject().getProjectTasks().size()) - guid2taskMap.values().size();
					if (updated > 0) {
						sb.append("<li>Updating " + updated + " task</li>");
					}
					primController.merge(primaveraProject);
					taskController.mergeTransactionStart();
					for (ProjectTask t : primaveraProject.getProject().getProjectTasks()) {
						if (!guid2taskMap.values().contains(t)) {
							taskController.mergeTransactionMerge(t);
						}
					}
					taskController.mergeTransactionClose();
					// then save the new added tasks
					for (ProjectTask t : guid2taskMap.values()) {
						taskController.persist(t);
					}
				} else {
					projectController.persist(primaveraProject.getProject());
					primController.persist(primaveraProject);
					for (ProjectTask t : guid2taskMap.values()) {
						taskController.persist(t);
					}
				}

				if (existingProject) {
					int depCount = getDependenciesCountByProjectId(session, primaveraProject.getProject().getProjectId());
					if (depCount > 0) {
						sb.append("<Li>Removed " + depCount + " dependencies and they will be updated from the imported file</li>");
					}
					removeDependenciesByProject(session, primaveraProject.getProject());
				}
				importTaskDependencies(project, newDependencies, updatedDependencies, objectId2TaskMap);

				EntityController<TaskDependency> taskDependencyController = new EntityController<TaskDependency>(session.getServletContext());
				for (TaskDependency dep : newDependencies) {
					taskDependencyController.persist(dep);
				}
				for (TaskDependency dep : updatedDependencies) {
					taskDependencyController.merge(dep);
				}
				int depCount = getDependenciesCountByProjectId(session, primaveraProject.getProject().getProjectId());
				if (depCount > 0) {
					sb.append("<Li>Created " + depCount + " dependency</li>");
				}
				
				EntityController<DaysOff> vacController = new EntityController<DaysOff>(session.getServletContext());
				
				for (DaysOff dayo : vacations) {
					vacController.persist(dayo);
				}

				TaskController controller = new TaskController();
				controller.adjustStartDateBasedOnTaskDependency(session, primaveraProject.getProject().getProjectId(), true);

				ProjectController.refreshJPAClass(session, PortfolioLight.class);
				ProjectController.refreshJPAClass(session, ProjectLight.class);
			}
		}
		sb.append("</ul>");
		return sb.toString();
	}

	private void deleteVacations(HttpSession session, Project project) {
		EntityController<DaysOff> controller = new EntityController<DaysOff>(session.getServletContext());
		for (DaysOff dayoff : project.getDaysOffs()) {
			try {
				controller.remove(DaysOff.class, dayoff.getDayoffId());
			} catch (EntityControllerException e) {
				e.printStackTrace();
			}			
		}
	}

	public static void removeDependenciesByProject(HttpSession session, Project project) throws OptimaException {
		EntityController<TaskDependency> controller = new EntityController<TaskDependency>(session.getServletContext());
		for (ProjectTask tsk : project.getProjectTasks()) {
			for (TaskDependency dep:tsk.getAsDependency()) {
				try {
					controller.remove(TaskDependency.class, dep.getDependencyId());
				} catch (EntityControllerException e) {
					e.printStackTrace();
				}			
			}
		}
	}

	public static int getDependenciesCountByProjectId(HttpSession session, int projectId) throws OptimaException {
		EntityController<TaskDependency> controller = new EntityController<TaskDependency>(session.getServletContext());
		try {
			Object res = controller.nativeQuery("select count(*) FROM task_dependency where dependant_task_id in (select task_id from project_task where project_id=?1)", projectId);
			if (res instanceof List && ((List) res).size() == 1) {
				return ((Long) ((List) res).get(0)).intValue();
			}
			return 0;
		} catch (EntityControllerException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private void importTasks(Node project, PrimaveraProject primaveraProject, Map<String, ProjectTask> guid2taskMap, Map<String, ProjectTask> existingGuid2TaskMap, Map<String, ProjectTask> objectId2TaskMap) {
		Map<String, Double> activityCost = getTasksCost(project);
		Map<String, ProjectTask> guid2TaskMap = new HashMap<String, ProjectTask>();

		Node projE = project.getFirstChild();
		while (projE != null) {
			if (projE instanceof Element) {
				if ("activity".equals(((Element) projE).getTagName().toLowerCase())) {
					// a task, handle it here
					Element taskNode = (Element) projE;
					String ttype = getElementChildAttributeValue(taskNode, "type");
					String objectId = getElementChildAttributeValue(taskNode, "objectid");
					Double cost = activityCost.get(objectId);
					int type = ProjectTask.TYPE_NPRMAL;
					if (cost == null) {
						cost = (double) 0;
					}
					String tduration = getElementChildAttributeValue(taskNode, "plannedduration");
					int duration = Integer.valueOf(tduration) / 8;

					if ("wbs summary".equals(ttype.toLowerCase())) {
						cost += getActivityExpense(project, objectId);
						double overhead = cost;
						overhead /= duration;
						primaveraProject.getProject().setOverheadPerDay(BigDecimal.valueOf(overhead));
						projE = projE.getNextSibling();
						continue;
					} else if ("start milestone".equals(ttype.toLowerCase())) {
						type = ProjectTask.TYPE_MILESTONE_START;
					} else if ("finish milestone".equals(ttype.toLowerCase())) {
						type = ProjectTask.TYPE_MILESTONE_END;
					} 

					String tname = getElementChildAttributeValue(taskNode, "name");

					String tguid = getElementChildAttributeValue(taskNode, "guid");
					// String temp2 = getElementChildAttributeValue(taskNode, "plannedstartdate");
					// Date tStart = formatter.parse(temp2);
					// temp2 = getElementChildAttributeValue(taskNode, "plannedfinishdate");
					// Date tEnd = formatter.parse(temp2);
					ProjectTask task = existingGuid2TaskMap.get(tguid);
					if (task == null) {
						task = new ProjectTask();
						task.setProject(primaveraProject.getProject());
						guid2taskMap.put(tguid, task);
						task.setTaskGuid(tguid);
						task.setUniformDailyIncome(BigDecimal.ZERO);
						task.setTaskDescription("");
					}
					task.setType(type);
					task.setTaskName(tname);
					// task.setTentativeStartDate(tStart);
					task.setDuration(duration);
					if (cost != null && duration != 0) {
						task.setUniformDailyCost(BigDecimal.valueOf(cost / duration));
					}
					guid2TaskMap.put(tguid, task);
					objectId2TaskMap.put(objectId, task);
				}
			}
			projE = projE.getNextSibling();
		}
	}

	private void importTaskDependencies(Node project, List<TaskDependency> newDependencies, List<TaskDependency> updatedDependencies, Map<String, ProjectTask> objectId2TaskMap) {
		Node projE;
		projE = project.getFirstChild();
		while (projE != null) {
			if (projE instanceof Element) {
				if ("relationship".equals(((Element) projE).getTagName().toLowerCase())) {
					String src = getElementChildAttributeValue(projE, "predecessoractivityobjectid");
					String tgt = getElementChildAttributeValue(projE, "successoractivityobjectid");
					String lags = getElementChildAttributeValue(projE, "lag");
					int lag = 0;
					if (lags != null) {
						try {
							lag = Integer.valueOf(lags) / 8;
						} catch (Exception e) {
						}
					}

					if (src != null && tgt != null) {
						ProjectTask srcTsk = objectId2TaskMap.get(src);
						ProjectTask tgtTsk = objectId2TaskMap.get(tgt);
						if (srcTsk != null && tgtTsk != null) {
							boolean exist = false;
							if (srcTsk.getAsDependency() != null) {
								for (TaskDependency dep : srcTsk.getAsDependency()) {
									if (dep.getDependent() == tgtTsk.getTaskId()) {
										dep.setLag(lag);
										exist = true;
										updatedDependencies.add(dep);
										break;
									}
								}
							}
							if (!exist) {
								TaskDependency taskDependency = new TaskDependency();
								taskDependency.setDependency(srcTsk.getTaskId());
								taskDependency.setDependent(tgtTsk.getTaskId());
								taskDependency.setLag(lag);
								newDependencies.add(taskDependency);
							}
						}
					}
				}
			}
			projE = projE.getNextSibling();
		}
	}

	private List<DaysOff> ImportVacations(PrimaveraProject primaveraProject, Document doc, SimpleDateFormat formatter) {
		try {
			List<DaysOff> vacations = new ArrayList<DaysOff>();

			Node vacs = browseToNode(doc, "Calendar\\HolidayOrExceptions");
			if (vacs != null) {
				Node child = vacs.getFirstChild();
				while (child != null) {
					if (child instanceof Element && "holidayorexception".equals(((Element) child).getTagName().toLowerCase())) {
						Node date = null;
						Node worktime = null;

						Node sc = child.getFirstChild();
						while (sc != null) {
							if (sc instanceof Element) {
								String n = (((Element) sc).getTagName().toLowerCase());
								if ("date".equals(n)) {
									date = sc;
								} else if ("worktime".equals(n)) {
									worktime = sc;
								}
							}
							sc = sc.getNextSibling();
						}

						if (date != null && worktime != null) {
							String dateString = date.getTextContent();
							if (worktime.getAttributes().getNamedItem("xsi:nil") != null) {
								String key = worktime.getAttributes().getNamedItem("xsi:nil").getNodeValue();
								if ("true".equals(key.toLowerCase())) {
									Date dateObj = formatter.parse(dateString);

									DaysOff dayOff = new DaysOff();
									dayOff.setDayOff(dateObj);
									dayOff.setDayoffType("VACATION");
									dayOff.setProject(primaveraProject.getProject());
									vacations.add(dayOff);
								}
							}

						}
					}
					child = child.getNextSibling();
				}
			}
			return vacations;
		} catch (Exception e) {

		}
		return Collections.emptyList();
	}

	private static Map<String, Integer> weekDays = new HashMap<String, Integer>();

	static {
		weekDays.put("sunday", 0);
		weekDays.put("monday", 1);
		weekDays.put("tuesday", 2);
		weekDays.put("wednesday", 3);
		weekDays.put("thursday", 4);
		weekDays.put("friday", 5);
		weekDays.put("saturday", 6);
	}

	private Node browseToNode(Document doc, String path) {
		String[] pathes = path.split("\\\\");
		Node root = doc.getDocumentElement();
		for (int i = 0; i < pathes.length; i++) {
			if (root == null) {
				break;
			}
			String nodeName = pathes[i].toLowerCase();
			Node child = root.getFirstChild();
			while (child != null) {
				if (child instanceof Element && nodeName.equals(((Element) child).getTagName().toLowerCase())) {
					root = child;
					break;
				}
				child = child.getNextSibling();
			}
		}

		return root;
	}

	private int getWeekendIndex(Node node) {
		if (node instanceof Element) {
			Element standardWorkHours = (Element) node;
			if ("standardworkhours".equals(standardWorkHours.getTagName().toLowerCase())) {
				Node dayOfWeek = null;
				Node workTime = null;
				Node child = standardWorkHours.getFirstChild();
				while (child != null) {
					if (child instanceof Element) {
						String name = ((Element) child).getTagName().toLowerCase();
						if ("dayofweek".equals(name)) {
							dayOfWeek = child;
						} else if ("worktime".equals(name)) {
							workTime = child;
						}
					}
					child = child.getNextSibling();
				}
				if (workTime != null && dayOfWeek != null) {
					String day = dayOfWeek.getTextContent().toLowerCase();
					if (weekDays.containsKey(day) && workTime.getAttributes().getNamedItem("xsi:nil") != null) {
						String key = workTime.getAttributes().getNamedItem("xsi:nil").getNodeValue();
						if ("true".equals(key.toLowerCase())) {
							return weekDays.get(day);
						}
					}
				}
			}
		}
		return -1;
	}

	private String getWeekend(Document doc) {
		String weekends = "0000000";

		try {
			Node workweek = browseToNode(doc, "Calendar\\StandardWorkWeek");
			Node child = workweek.getFirstChild();
			while (child != null) {
				int x = getWeekendIndex(child);
				if (x != -1) {
					weekends = weekends.substring(0, x) + "1" + weekends.substring(x + 1);
				}
				child = child.getNextSibling();
			}
		} catch (Exception e) {

		}

		return weekends;
	}

	private List<Node> getProjects(Document doc) {
		List<Node> projects = new ArrayList<Node>();
		Node child = doc.getDocumentElement().getFirstChild();
		while (child != null) {
			if (child instanceof Element && "project".equals(((Element) child).getTagName().toLowerCase())) {
				projects.add(child);
			}
			child = child.getNextSibling();
		}
		return projects;
	}

	private String getElementChildAttributeValue(Node project, String attributeName) {
		String value = "";
		try {
			Node child = project.getFirstChild();
			while (child != null) {
				if (child instanceof Element && attributeName.equals(((Element) child).getTagName().toLowerCase())) {
					Node n = child.getFirstChild();
					if (n != null) {
						value = n.getTextContent();
					}
					break;
				}
				child = child.getNextSibling();
			}
		} catch (Exception e) {
			// if a
		}
		return value;
	}

	private void setElementChildAttributeValue(Node project, String attributeName, String value) {
		try {
			Node n = null;
			Node child = project.getFirstChild();
			while (child != null) {
				if (child instanceof Element && attributeName.equalsIgnoreCase(((Element) child).getTagName().toLowerCase())) {
					n = child;
					break;
				}
				child = child.getNextSibling();
			}
			if (n == null) {
				n = project.getOwnerDocument().createElement(attributeName);
				project.appendChild(n);
			} else {
				Node c = n.getFirstChild();
				while (c!=null) {
					Node c2 = c.getNextSibling();
					n.removeChild(c);
					c = c2;
				}
			}
	        n.appendChild(project.getOwnerDocument().createTextNode(value));
		} catch (Exception e) {
			// if a
		}
	}

	private PrimaveraProject getPrimaveraProject(HttpSession session, String guid) {
		EntityController<PrimaveraProject> controller = new EntityController<PrimaveraProject>(session.getServletContext());
		try {
			List<PrimaveraProject> projects = controller.findAllQuery(PrimaveraProject.class, String.format("SELECT o FROM %s o where o.projectGuid=\'%s\'", PrimaveraProject.class.getName(), guid));
			if (projects.size() > 0) {
				return projects.get(0);
			}
		} catch (Exception e) {

		}
		return null;
	}

	private PrimaveraProject getPrimaveraProject(HttpSession session, int projectId) {
		EntityController<PrimaveraProject> controller = new EntityController<PrimaveraProject>(session.getServletContext());
		try {

			List<PrimaveraProject> projects = controller.findAll(PrimaveraProject.class);
			if (projects.size() > 0) {
				for (PrimaveraProject p : projects) {
					if (p.getProject().getProjectId() == projectId) {
						return p;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private Map<String, Double> getTasksCost(Node project) {
		Map<String, Double> tasksCost = new HashMap<String, Double>();

		Node child = project.getFirstChild();
		while (child != null) {
			try {
				if (child instanceof Element && "resourceassignment".equals(((Element) child).getTagName().toLowerCase())) {
					String activityObjetId = getElementChildAttributeValue(child, "activityobjectid");
					String temp = getElementChildAttributeValue(child, "plannedcost");
					Double cost = Double.valueOf(temp);

					if (tasksCost.containsKey(activityObjetId)) {
						cost += tasksCost.get(activityObjetId);
					}
					tasksCost.put(activityObjetId, cost);
				}
			} catch (Exception e) {
			}
			child = child.getNextSibling();
		}

		return tasksCost;
	}

	private Double getActivityExpense(Node project, String objId) {
		Double ret = (double) 0;

		Node child = project.getFirstChild();
		while (child != null) {
			try {
				if (child instanceof Element && "activityexpense".equals(((Element) child).getTagName().toLowerCase())) {
					String activityObjetId = getElementChildAttributeValue(child, "activityobjectid");
					if (activityObjetId.equals(objId)) {
						String temp = getElementChildAttributeValue(child, "plannedcost");
						ret += Double.valueOf(temp);
					}
				}
			} catch (Exception e) {
			}
			child = child.getNextSibling();
		}

		return ret;
	}

	private void updateActivityExpense(Node project, String objId, Double increasePercentage) {
		Node child1 = project.getFirstChild();
		while (child1 != null) {
			try {
				if (child1 instanceof Element && "activityexpense".equals(((Element) child1).getTagName().toLowerCase())) {
					String activityObjetId = getElementChildAttributeValue(child1, "activityobjectid");
					if (activityObjetId.equals(objId)) {

						Node child = child1.getFirstChild();
						while (child != null) {
							if (child instanceof Element && "plannedcost".equals(((Element) child).getTagName().toLowerCase())) {
								Node n = child.getFirstChild();
								if (n != null) {
									Double val = Double.valueOf(n.getTextContent()) * increasePercentage;
									child.removeChild(n);
									child.appendChild(project.getOwnerDocument().createTextNode(String.valueOf(val)));
								}
								break;
							}
							child = child.getNextSibling();
						}
					}
				}
			} catch (Exception e) {
			}
			child1 = child1.getNextSibling();
		}

	}
	public String exportPrimaveraProject(int projectId, HttpSession session) throws Exception {
		try {
			PrimaveraProject primaveraProject = getPrimaveraProject(session, projectId);
			if (primaveraProject != null) {
				String xml = primaveraProject.getFileContent();
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(new java.io.ByteArrayInputStream(xml.getBytes()));
				
				Map<String, ProjectTask> existingGuid2TaskMap = new HashMap<String, ProjectTask>();
				for (ProjectTask t : primaveraProject.getProject().getProjectTasks()) {
					if (t.getTaskGuid() != null) {
						existingGuid2TaskMap.put(t.getTaskGuid(), t);
					}
				}

				List<Node> projects = getProjects(doc);
				if (!projects.isEmpty()) {
					for (Node project : projects) {
						String guid = getElementChildAttributeValue(project, "guid");
						if (primaveraProject.getProjectGuid().equals(guid)) {

							// Map<String, Double> activityCost = getTasksCost(project);
							Map<String, ProjectTask> guid2TaskMap = new HashMap<String, ProjectTask>();
							for (ProjectTask pt:primaveraProject.getProject().getProjectTasks()) {
								guid2TaskMap.put(pt.getTaskGuid(), pt);
							}
							
							Node projE = project.getFirstChild();
							Node overheadActivity = null;
							Node theProjectNode = null;
							while (projE != null) {
								if (projE instanceof Element) {
									if ("activity".equals(((Element) projE).getTagName().toLowerCase())) {

										// a task, handle it here
										Element taskNode = (Element) projE;

										String ttype = getElementChildAttributeValue(taskNode, "type");
										if ("wbs summary".equals(ttype.toLowerCase())) {
											overheadActivity = taskNode;
										}

										/*
										 * String ttype = getElementChildAttributeValue(taskNode, "type"); String objectId = getElementChildAttributeValue(taskNode, "objectid"); String tduration = getElementChildAttributeValue(taskNode,
										 * "plannedduration"); int duration = Integer.valueOf(tduration) / 8; String tname = getElementChildAttributeValue(taskNode, "name");
										 */
										String tguid = getElementChildAttributeValue(taskNode, "guid");
										ProjectTask task = existingGuid2TaskMap.get(tguid);
										if (task != null && (task.getStatus()==null || task.getStatus() == ProjectTask.STATUS_NOT_STARTED)) {
											Date d = task.getCalendarStartDate();
											
											setElementChildAttributeValue(taskNode, "PlannedStartDate", PRIMAVERA_DATE_FORMATTER.format(d));
											setElementChildAttributeValue(taskNode, "PlannedFinishDate", PRIMAVERA_DATE_FORMATTER.format(TaskUtil.addDays(d, task.getCalenderDuration())));
										}
									}
								}
								projE = projE.getNextSibling();
							}
							
							if (overheadActivity!=null) {
								String objectId = getElementChildAttributeValue(overheadActivity, "objectid");
								Double oldOverheadCost = getActivityExpense(project, objectId);

								EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
								Date[] projDates = PaymentUtil.getProjectExtendedDateRanges(controller, primaveraProject.getProject());
								int projDuration = PaymentUtil.daysBetween(projDates[0], projDates[1]) + 1;
								Double totalOverhead = projDuration * primaveraProject.getProject().getOverheadPerDay().doubleValue();
								if (projDates.length==2 && totalOverhead!=oldOverheadCost) {
									setElementChildAttributeValue(overheadActivity, "PlannedStartDate", PRIMAVERA_DATE_FORMATTER.format(projDates[0]));
									setElementChildAttributeValue(overheadActivity, "PlannedFinishDate", PRIMAVERA_DATE_FORMATTER.format(projDates[1]));
									
									updateActivityExpense(project,objectId,totalOverhead/oldOverheadCost);
								}
							}

							// save and export

							break;// found the correct project, so nothing else is needed
						}
					}
				}
				
				File zipFile = writeDocumentToFile(doc,primaveraProject.getProject().getProjectName());

				byte[] encoded = Files.readAllBytes(Paths.get(zipFile.getPath()));
//				String contents = new String(Files.readAllBytes(Paths.get(zipFile.getAbsolutePath())));
				if (encoded!=null) {
					return new String(Base64.getEncoder().encode(encoded), "UTF-8");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static File writeDocumentToFile(Document document, String fileName) {
		File zipFile = null;
		try {
			File xmlFile = File.createTempFile(fileName, ".xml");
			zipFile = File.createTempFile(fileName, ".zip");
			
			// Make a transformer factory to create the Transformer
			TransformerFactory tFactory = TransformerFactory.newInstance();

			// Make the Transformer
			Transformer transformer = tFactory.newTransformer();

			// Mark the document as a DOM (XML) source
			DOMSource source = new DOMSource(document);

			// Say where we want the XML to go
			StreamResult result = new StreamResult(xmlFile);

			// Write the XML to file
			transformer.transform(source, result);
			
			UnzipUtility.zip(xmlFile.getAbsolutePath(), zipFile.getAbsolutePath(), fileName + ".xml");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return zipFile;
	}
}
