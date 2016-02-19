package com.softpoint.optima.util.solution;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.softpoint.optima.db.DaysOff;
import com.softpoint.optima.db.Project;
import com.softpoint.optima.db.ProjectTask;
import com.softpoint.optima.db.TaskDependency;
import com.softpoint.optima.db.WeekendDay;

public class ProjectWrapper {
	Project project;
	List<TaskTreeNode> tasks;
	List<TaskTreeNode> rootTasks;
	public Boolean getFinished() {
		return finished;
	}

	public void setFinished(Boolean finished) {
		this.finished = finished;
	}

	public Project getProject() {
		return project;
	}

	List<TaskTreeNode> completedTasks;
	Map<ProjectTask,TaskTreeNode> allTreeNodesMap;
	List<TaskTreeNode> allTasks;
	Boolean finished;
	private WeekendDay projectWeekends;
	private List<DaysOff> projectVacations;
	int totalTasks;
	public ProjectWrapper(Project project) {
		super();
		this.project = project;
		finished = false;
		tasks = new ArrayList<TaskTreeNode>();
		rootTasks = new ArrayList<TaskTreeNode>();
		completedTasks = new ArrayList<TaskTreeNode>();
		allTasks = new ArrayList<TaskTreeNode>();
		projectWeekends = project.getWeekendDays();
		projectVacations = project.getDaysOffs();
		
		allTreeNodesMap = new HashMap<ProjectTask,TaskTreeNode>();
		List<ProjectTask> allTasks = project.getProjectTasks();
		totalTasks = allTasks.size();
		for (ProjectTask tsk:allTasks) {
			addTaskNodes(tsk);
		}
		
		for (ProjectTask tsk:allTasks) {
			TaskTreeNode node = allTreeNodesMap.get(tsk);
			this.allTasks.add(node);
			if (node.parents.size()==0) {
				tasks.add(node);
				rootTasks.add(node);
			}
		}

		allTreeNodesMap.clear();
		allTreeNodesMap=null;
	}
	
	private void addTaskNodes(ProjectTask tsk) {
		TaskTreeNode node = allTreeNodesMap.get(tsk);
		if (node==null) {
			node = new TaskTreeNode(tsk, this);
			allTreeNodesMap.put(tsk, node);
			
			List<TaskDependency> dependencies = tsk.getAsDependency();
			for (TaskDependency dep:dependencies) {
				ProjectTask tsk2 = dep.getDependent();
				addTaskNodes(tsk2);
				TaskTreeNode tsk2Node = allTreeNodesMap.get(tsk2);
				node.children.add(tsk2Node);
				tsk2Node.parents.add(node);
			}
		}
	}

	public List<TaskTreeNode> getTasks() {
		return tasks;
	}
	

	public WeekendDay getProjectWeekends() {
		return projectWeekends;
	}

	public List<DaysOff> getProjectVacations() {
		return projectVacations;
	}

	public int getProjectDuratoin() {
		int maxDuration = Integer.MIN_VALUE;
		for (TaskTreeNode task:rootTasks) {
			if (maxDuration < task.getDurationWithChildren()) {
				maxDuration = task.getDurationWithChildren();
			}
		}
		return maxDuration;
	}

	public int getTotalTasks() {
		return totalTasks;
	}

	public List<TaskTreeNode> getAllTasks() {
		return allTasks;
	}

}
