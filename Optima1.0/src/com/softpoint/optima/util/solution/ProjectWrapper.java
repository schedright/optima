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
	Map<ProjectTask, TaskTreeNode> allTreeNodesMap;
	List<TaskTreeNode> allTasks;
	List<ProjectTask> eventTasks;
	
	Boolean finished;
	private String projectWeekends;
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
		eventTasks = new ArrayList<ProjectTask>();
		projectWeekends = project.getWeekend();
		projectVacations = project.getDaysOffs();

		allTreeNodesMap = new HashMap<ProjectTask, TaskTreeNode>();
		List<ProjectTask> allTasks = project.getProjectTasks();
		totalTasks = allTasks.size();
		for (ProjectTask tsk : allTasks) {
			if (tsk.getType() == ProjectTask.TYPE_NPRMAL) {
				addTaskNodes(tsk);
			} else {
				eventTasks.add(tsk);
			}
		}

		for (ProjectTask tsk : allTasks) {
			if (tsk.getType() == ProjectTask.TYPE_NPRMAL) {
				TaskTreeNode node = allTreeNodesMap.get(tsk);
				this.allTasks.add(node);
				if (node.parents.size() == 0) {
					tasks.add(node);
					rootTasks.add(node);
				}
			}
		}

		allTreeNodesMap.clear();
		allTreeNodesMap = null;
	}

	private void addDep(TaskTreeNode n1, ProjectTask depT) {
		if (depT.getType()==ProjectTask.TYPE_NPRMAL) {
			addTaskNodes(depT);
			TaskTreeNode tsk2Node = allTreeNodesMap.get(depT);
			n1.children.add(tsk2Node);
			tsk2Node.parents.add(n1);
			
		} else {
			for (TaskDependency dep : depT.getAsDependency()) {
				ProjectTask tsk2 = project.findTask(dep.getDependent());
				addDep(n1,tsk2);
			}
		}
		
	}
	
	private void addTaskNodes(ProjectTask tsk) {
		TaskTreeNode node = allTreeNodesMap.get(tsk);
		if (node == null) {
			node = new TaskTreeNode(tsk, this);
			allTreeNodesMap.put(tsk, node);

			List<TaskDependency> dependencies = tsk.getAsDependency();
			for (TaskDependency dep : dependencies) {
				ProjectTask tsk2 = project.findTask(dep.getDependent());
				addDep(node, tsk2);
			}
		}
	}

	public List<TaskTreeNode> getTasks() {
		return tasks;
	}

	public String getProjectWeekends() {
		return projectWeekends;
	}

	public List<DaysOff> getProjectVacations() {
		return projectVacations;
	}

	public int getProjectDuratoin() {
		int maxDuration = Integer.MIN_VALUE;
		Date projectStart = project.getPropusedStartDate();
		for (TaskTreeNode task : rootTasks) {
			Date ts = task.getCalculatedTaskStart();
			int shift = (int) PortfolioSolver.differenceInDays(projectStart, ts);

			int temp = task.getDurationWithChildren() + shift;
			if (maxDuration < temp) {
				maxDuration = temp;
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

	public List<TaskTreeNode> getRootTasks() {
		return rootTasks;
	}

}
