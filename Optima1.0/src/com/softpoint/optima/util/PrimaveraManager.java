package com.softpoint.optima.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.softpoint.optima.db.PortfolioLight;
import com.softpoint.optima.db.PrimaveraProject;
import com.softpoint.optima.db.Project;
import com.softpoint.optima.db.ProjectLight;
import com.softpoint.optima.db.ProjectTask;

public class PrimaveraManager {
	public static void importPrimaveraFile(File xmlFile, HttpSession session) throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(xmlFile);
				
		EntityController<PrimaveraProject> primController = new EntityController<PrimaveraProject>(session.getServletContext());
		EntityController<Project> projectController = new EntityController<Project>(session.getServletContext());
		EntityController<ProjectTask> taskController = new EntityController<ProjectTask>(session.getServletContext());

		//optional, but recommended
		//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
		doc.getDocumentElement().normalize();
	
		List<Node> projects = getProjects(doc);
		if (!projects.isEmpty()) {
			for (Node project:projects) {
				String name = getProjectAttributeValue(project,"name");
				String guid = getProjectAttributeValue(project,"guid");
				String description = getProjectAttributeValue(project,"description");
				if (description==null) {
					description = "";
				}
				if (name==null || name.length()<3) {
					throw new Exception("Name doesn't exist or invalid");
				}
 				String temp = getProjectAttributeValue(project,"plannedstartdate");
 				//2016-05-01T00:00:00
 				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
 				Date startDate = formatter.parse(temp);
 				
				PrimaveraProject primaveraProject = getPrimaveraProject(session, guid);
				boolean existingProject = true;
				if (primaveraProject==null) {
					existingProject = false;
					primaveraProject = new PrimaveraProject();
					primaveraProject.setProjectGuid(guid);
					Project proj = new Project();
					primaveraProject.setProject(proj);
					primaveraProject.getProject().setProjectCode(name);
				}
				primaveraProject.getProject().setProjectName(name);
				primaveraProject.getProject().setProjectDescription(description);
				primaveraProject.getProject().setPropusedStartDate(startDate);
				
//				cal = brow
						/*
				<StandardWorkWeek>
					<StandardWorkHours>
					<DayOfWeek>Sunday</DayOfWeek>
					<WorkTime>
*/
				
				List<ProjectTask> allTasks = new ArrayList<ProjectTask>();
				Map<String,List<String>> dependencies = new HashMap<String,List<String>>();
				Map<String,ProjectTask> guid2taskMap = new HashMap<String,ProjectTask>();
				Map<String,ProjectTask> existingGuid2TaskMap = new HashMap<String,ProjectTask>();
				if (primaveraProject.getProject().getProjectTasks()!=null) {
					for (ProjectTask tsk:primaveraProject.getProject().getProjectTasks()) {
						if (tsk.getTaskGuid()!=null) {
							existingGuid2TaskMap.put(tsk.getTaskGuid(), tsk);
						}
					}
				}
				
				Node projE = project.getFirstChild();
				while (projE!=null) {
					if (projE instanceof Element) {
						if ("activity".equals(((Element)projE).getTagName().toLowerCase())) {
							//a task, handle it here
							Element taskNode = (Element) projE;
							
							String tname = getProjectAttributeValue(taskNode,"name");
							String tguid = getProjectAttributeValue(taskNode,"guid");
							String temp2 = getProjectAttributeValue(taskNode,"plannedstartdate");
							Date tStart = formatter.parse(temp2);
							temp2 = getProjectAttributeValue(taskNode,"plannedfinishdate");
							Date tEnd = formatter.parse(temp2);
							int duration = PaymentUtil.daysBetween(tStart,tEnd)+1;
							ProjectTask task = existingGuid2TaskMap.get(tguid);
							if (task==null) {
								task = new ProjectTask();
								task.setProject(primaveraProject.getProject());
								guid2taskMap.put(tguid, task);
								task.setTaskGuid(tguid);
							}
							task.setTaskName(tname);
							task.setTentativeStartDate(tStart);
							task.setDuration(duration);
						}
					}
					projE = projE.getNextSibling();
				}
				
				if (existingProject) {
					primController.merge(primaveraProject);
					taskController.mergeTransactionStart();
					for (ProjectTask t:guid2taskMap.values()) {
						taskController.mergeTransactionMerge(t);
					}
					taskController.mergeTransactionClose();
				} else {
					projectController.persist(primaveraProject.getProject());
					primController.persist(primaveraProject);
					for (ProjectTask t:guid2taskMap.values()) {
						taskController.persist(t);
					}

				}
				ProjectController.refreshJPAClass(session, PortfolioLight.class);
				ProjectController.refreshJPAClass(session, ProjectLight.class);
			}
		}
	}
	
	private static List<Node> getProjects(Document doc) {
		List<Node> projects = new ArrayList<Node>();
		Node child = doc.getDocumentElement().getFirstChild();
		while (child!=null) {
			if (child instanceof Element &&  	"project".equals(((Element)child).getTagName().toLowerCase())) {
				projects.add(child);
			}
			child = child.getNextSibling();
		}
		return projects;
	}
	
	private static String getProjectAttributeValue(Node project, String attributeName) {
		String value = null;
		try {
		Node child = project.getFirstChild();
		while (child!=null) {
			if (child instanceof Element &&  	attributeName.equals(((Element)child).getTagName().toLowerCase())) {
				Node n = child.getFirstChild();
				value = n.getTextContent();
				break;
			}
			child = child.getNextSibling();
		}
		} catch(Exception e) {
			//if a
		}
		return value;
	}
	
	private static PrimaveraProject getPrimaveraProject(HttpSession session, String guid) {
		EntityController<PrimaveraProject> controller = new EntityController<PrimaveraProject>(session.getServletContext());
		try {
			List<PrimaveraProject> projects = controller.findAllQuery(PrimaveraProject.class, String.format("SELECT o FROM %s o where o.projectGuid=\'%s\'", PrimaveraProject.class.getName(), guid));
			if (projects.size()>0) {
				return projects.get(0);
			}
		} catch(Exception e) {
			
		}
		return null;
	}
}
