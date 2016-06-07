package com.softpoint.optima.util;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.softpoint.optima.control.EntityController;
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
	public static void importPrimaveraFile(File xmlFile, HttpSession session) throws Exception {
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
					Project proj = new Project();
					primaveraProject.setProject(proj);
					primaveraProject.getProject().setProjectCode(name);
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
				
				importTasks(project, primaveraProject, guid2taskMap, existingGuid2TaskMap, newDependencies, updatedDependencies);

				SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

				List<DaysOff> vacations = ImportVacations(primaveraProject, doc, formatter2);

				try {
	                byte[] bytes = Files.readAllBytes(xmlFile.toPath());
	                String content = new String(bytes,"UTF-8");
	                primaveraProject.setFileContent(content);
	            } catch (Exception e) {
	            }
				
				if (existingProject) {
					primController.merge(primaveraProject);
					taskController.mergeTransactionStart();
					for (ProjectTask t : guid2taskMap.values()) {
						taskController.mergeTransactionMerge(t);
					}
					taskController.mergeTransactionClose();
				} else {
					projectController.persist(primaveraProject.getProject());
					primController.persist(primaveraProject);
					for (ProjectTask t : guid2taskMap.values()) {
						taskController.persist(t);
					}
				}
				EntityController<TaskDependency> taskDependencyController = new EntityController<TaskDependency>(session.getServletContext());
				for (TaskDependency dep:newDependencies) {
					taskDependencyController.persist(dep);
				}
				for (TaskDependency dep:updatedDependencies) {
					taskDependencyController.merge(dep);
				}
				EntityController<DaysOff> vacController = new EntityController<DaysOff>(session.getServletContext());
				for (DaysOff dayo:vacations) {
					vacController.persist(dayo);
				}
				
				TaskController controller = new TaskController();
				controller.adjustStartDateBasedOnTaskDependency(session, primaveraProject.getProject().getProjectId(), true);
				
				ProjectController.refreshJPAClass(session, PortfolioLight.class);
				ProjectController.refreshJPAClass(session, ProjectLight.class);
			}
		}
	}

	private static void importTasks(Node project, PrimaveraProject primaveraProject, Map<String, ProjectTask> guid2taskMap, Map<String, ProjectTask> existingGuid2TaskMap, List<TaskDependency> newDependencies,
			List<TaskDependency> updatedDependencies) {
		Map<String, Double> activityCost = getTasksCost(project);
		Map<String, ProjectTask> guid2TaskMap = new HashMap<String,ProjectTask>();
		Map<String, ProjectTask> objectId2TaskMap = new HashMap<String,ProjectTask>();
		
		Node projE = project.getFirstChild();
		while (projE != null) {
			if (projE instanceof Element) {
				if ("activity".equals(((Element) projE).getTagName().toLowerCase())) {
					// a task, handle it here
					Element taskNode = (Element) projE;
					String ttype = getElementChildAttributeValue(taskNode, "type");
					String objectId = getElementChildAttributeValue(taskNode, "objectid");
					Double cost = activityCost.get(objectId);
					if (cost==null) {
						cost = (double) 0;
					}
					String tduration = getElementChildAttributeValue(taskNode, "plannedduration");
					int duration = Integer.valueOf(tduration) / 8;
					
					if ("wbs summary".equals(ttype.toLowerCase())) {
						cost += getActivityExpense(project,objectId);
						double overhead = cost;
						overhead /= duration;
						primaveraProject.getProject().setOverheadPerDay(BigDecimal.valueOf(overhead));
						projE = projE.getNextSibling();
						continue;
					}
					
					String tname = getElementChildAttributeValue(taskNode, "name");
					
					String tguid = getElementChildAttributeValue(taskNode, "guid");
//							String temp2 = getElementChildAttributeValue(taskNode, "plannedstartdate");
//							Date tStart = formatter.parse(temp2);
//							temp2 = getElementChildAttributeValue(taskNode, "plannedfinishdate");
//							Date tEnd = formatter.parse(temp2);
					ProjectTask task = existingGuid2TaskMap.get(tguid);
					if (task == null) {
						task = new ProjectTask();
						task.setProject(primaveraProject.getProject());
						guid2taskMap.put(tguid, task);
						task.setTaskGuid(tguid);
						task.setUniformDailyIncome(BigDecimal.ZERO);
						task.setTaskDescription("");
					}
					task.setTaskName(tname);
					//task.setTentativeStartDate(tStart);
					task.setDuration(duration);
					if (cost!=null && duration!=0) {
						task.setUniformDailyCost(BigDecimal.valueOf(cost/duration));
					}
					guid2TaskMap.put(tguid, task);
					objectId2TaskMap.put(objectId, task);
				}
			}
			projE = projE.getNextSibling();
		}
		importTaskDependencies(project, newDependencies, updatedDependencies, objectId2TaskMap);
	}

	private static void importTaskDependencies(Node project, List<TaskDependency> newDependencies, List<TaskDependency> updatedDependencies, Map<String, ProjectTask> objectId2TaskMap) {
		Node projE;
		projE = project.getFirstChild();
		while (projE != null) {
			if (projE instanceof Element) {
				if ("relationship".equals(((Element) projE).getTagName().toLowerCase())) {
					String src = getElementChildAttributeValue(projE, "predecessoractivityobjectid");
					String tgt = getElementChildAttributeValue(projE, "successoractivityobjectid");
					String lags = getElementChildAttributeValue(projE, "lag");
					int lag = 0;
					if (lags!=null) {
						try { 
							lag = Integer.valueOf(lags) / 8;
						} catch (Exception e) {
						}
					}

					if (src!=null && tgt!=null) {
						ProjectTask srcTsk = objectId2TaskMap.get(src);
						ProjectTask tgtTsk = objectId2TaskMap.get(tgt);
						if (srcTsk!=null && tgtTsk!=null) {
							boolean exist = false;
							if (srcTsk.getAsDependency()!=null) {
								for (TaskDependency dep:srcTsk.getAsDependency()) {
									if (dep.getDependent()==tgtTsk) {
										dep.setLag(lag);
										exist = true;
										updatedDependencies.add(dep);
										break;
									}
								}
							}
							if (!exist) {
								TaskDependency taskDependency = new TaskDependency();
								taskDependency.setDependency(srcTsk);
								taskDependency.setDependent(tgtTsk);
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

	private static List<DaysOff> ImportVacations(PrimaveraProject primaveraProject, Document doc, SimpleDateFormat formatter) {
		try {
			List<DaysOff> vacations = new ArrayList<DaysOff>();
			
			Node vacs = browseToNode(doc, "Calendar\\HolidayOrExceptions");
			if (vacs!=null) {
				Node child = vacs.getFirstChild();
				while (child != null) {
					if (child instanceof Element && "holidayorexception".equals(((Element)child).getTagName().toLowerCase())) {
						Node date = null;
						Node worktime = null;
						
						Node sc = child.getFirstChild();
						while (sc!=null) {
							if (sc instanceof Element) {
								String n = (((Element)sc).getTagName().toLowerCase());
								if ("date".equals(n)) {
									date = sc;
								} else if ("worktime".equals(n)) {
									worktime = sc;
								}
							}
							sc = sc.getNextSibling();
						}
						
						if (date!=null && worktime!=null) {
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

	private static Node browseToNode(Document doc, String path) {
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

	private static int getWeekendIndex(Node node) {
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

	private static String getWeekend(Document doc) {
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

	private static List<Node> getProjects(Document doc) {
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

	private static String getElementChildAttributeValue(Node project, String attributeName) {
		String value = "";
		try {
			Node child = project.getFirstChild();
			while (child != null) {
				if (child instanceof Element && attributeName.equals(((Element) child).getTagName().toLowerCase())) {
					Node n = child.getFirstChild();
					if (n!=null) {
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

	private static PrimaveraProject getPrimaveraProject(HttpSession session, String guid) {
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
	
	private static Map<String, Double> getTasksCost(Node project) {
		Map<String,Double> tasksCost = new HashMap<String,Double>();
		
		Node child = project.getFirstChild();
		while (child != null) {
			try {
				if (child instanceof Element && "resourceassignment".equals(((Element) child).getTagName().toLowerCase())) {
					String activityObjetId = getElementChildAttributeValue(child,"activityobjectid");
					String temp = getElementChildAttributeValue(child,"plannedcost");
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
	
	private static Double getActivityExpense(Node project, String objId) {
		Double ret = (double) 0;

		Node child = project.getFirstChild();
		while (child != null) {
			try {
				if (child instanceof Element && "activityexpense".equals(((Element) child).getTagName().toLowerCase())) {
					String activityObjetId = getElementChildAttributeValue(child,"activityobjectid");
					if (activityObjetId.equals(objId)) {
						String temp = getElementChildAttributeValue(child,"plannedcost");
						ret += Double.valueOf(temp);
					}
				}
			} catch (Exception e) {
			}
			child = child.getNextSibling();
		}
		
		return ret;
	}
}
