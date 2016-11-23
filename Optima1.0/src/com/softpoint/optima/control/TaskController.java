package com.softpoint.optima.control;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import com.softpoint.optima.OptimaException;
import com.softpoint.optima.ServerResponse;
import com.softpoint.optima.db.Project;
import com.softpoint.optima.db.ProjectTask;
import com.softpoint.optima.db.TaskDependency;
import com.softpoint.optima.struct.TaskSolution;
import com.softpoint.optima.util.PaymentUtil;
import com.softpoint.optima.util.ProjectSolutionDetails;
import com.softpoint.optima.util.TaskUtil;

/**
 * @author user mhamdy
 * 
 */
public class TaskController {

	/**
	 * @param session
	 * @param projectId
	 * @param taskDescription
	 * @param duration
	 * @param uniformDailyCost
	 * @param uniformDailyincome
	 * @param tentativeStartDate
	 * @param scheduledStartDate
	 * @param actualStartDate
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse create(HttpSession session, int projectId, String taskName, String taskDescription, int duration, double uniformDailyCost, double uniformDailyincome, Date tentativeStartDate, Date scheduledStartDate,
			Date actualStartDate, int status) throws OptimaException {

		EntityController<ProjectTask> controller = new EntityController<ProjectTask>(session.getServletContext());

		// Instantiate a project task ProjectTask set its properties
		ProjectTask projectTask = new ProjectTask();
		projectTask.setActualStartDate(actualStartDate);
		projectTask.setDuration(duration);
		projectTask.setScheduledStartDate(scheduledStartDate);
		projectTask.setTentativeStartDate(tentativeStartDate);
		// projectTask.setCalendarStartDate(tentativeStartDate);
		projectTask.setUniformDailyCost(new BigDecimal(uniformDailyCost));
		projectTask.setUniformDailyIncome(new BigDecimal(uniformDailyincome));
		projectTask.setTaskDescription(taskDescription);
		projectTask.setTaskName(taskName);
		projectTask.setStatus(status);

		try {

			// Get the Project using the ProjectId and set it to the projectTask
			EntityController<Project> projectController = new EntityController<Project>(session.getServletContext());
			Project project = projectController.find(Project.class, projectId);
			projectTask.setProject(project);

			controller.persist(projectTask);
			adjustStartDateBasedOnTaskDependency(session, projectId, false);
			return new ServerResponse("0", "Success", projectTask);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("TASK0001", String.format("Error creating Project Task %s: %s", projectTask, e.getMessage()), e);
		}
	}

	/**
	 * @param session
	 * @param taskId
	 * @param projectId
	 * @param taskDescription
	 * @param duration
	 * @param uniformDailyCost
	 * @param uniformDailyincome
	 * @param tentativeStartDate
	 * @param scheduledStartDate
	 * @param actualStartDate
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse update(HttpSession session, int taskId, int projectId, String taskName, String taskDescription, int duration, double uniformDailyCost, double uniformDailyincome, Date tentativeStartDate, Date scheduledStartDate,
			Date actualStartDate, int status) throws OptimaException {

		EntityController<ProjectTask> controller = new EntityController<ProjectTask>(session.getServletContext());

		ProjectTask projectTask = null;
		Project project = null;
		try {
			// Get the Project from the ProjectId
			EntityController<Project> ProjectController = new EntityController<Project>(session.getServletContext());
			project = ProjectController.find(Project.class, projectId);

			// Set the projectTask
			projectTask = controller.find(ProjectTask.class, taskId);
			projectTask.setActualStartDate(actualStartDate);
			projectTask.setDuration(duration);
			projectTask.setScheduledStartDate(scheduledStartDate);
			if (!projectTask.calculateEffectiveTentativeStartDate().equals(tentativeStartDate)) {
				projectTask.setCalendarStartDate(null);
			}
			projectTask.setTentativeStartDate(tentativeStartDate);

			projectTask.setUniformDailyCost(new BigDecimal(uniformDailyCost));
			projectTask.setUniformDailyIncome(new BigDecimal(uniformDailyincome));
			projectTask.setTaskDescription(taskDescription);
			projectTask.setTaskName(taskName);
			projectTask.setStatus(status);

			projectTask.setProject(project);
			controller.merge(projectTask);

			adjustStartDateBasedOnTaskDependency(session, projectId, false);
			return new ServerResponse("0", "Success", projectTask);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("TASK0002", String.format("Error updating projectTask for Project %s: %s", project != null ? project.getProjectName() : "", e.getMessage()), e);
		}
	}

	/*
	 * public ServerResponse resetTaskScheduling(HttpSession session, int taskId ) throws OptimaException { EntityController<ProjectTask> controller = new EntityController<ProjectTask>(session.getServletContext());
	 * 
	 * ProjectTask projectTask = null; try {
	 * 
	 * //System.out.println("Resetting"); projectTask = controller.find(ProjectTask.class, taskId); projectTask.setScheduledStartDate(null); projectTask.setCalendarStartDate(null); projectTask.setCalenderDuration(0);
	 * controller.merge(projectTask); adjustStartDateBasedOnTaskDependency(session, projectTask.getProject().getProjectId() , false); return new ServerResponse("0", "Success", null); } catch (EntityControllerException e) {
	 * e.printStackTrace(); return new ServerResponse("TASK0002-3", String.format("Error resetting scheduling for task %d: %s", taskId, e.getMessage()), e); }
	 * 
	 * }
	 */ public ServerResponse updateStartDate(HttpSession session, int taskId, Date scheduledStartDate) throws OptimaException {

		EntityController<ProjectTask> controller = new EntityController<ProjectTask>(session.getServletContext());

		ProjectTask projectTask = null;
		try {

			// Set the projectTask
			projectTask = controller.find(ProjectTask.class, taskId);
			projectTask.setScheduledStartDate(scheduledStartDate);
			controller.merge(projectTask);
			adjustStartDateBasedOnTaskDependency(session, projectTask.getProject().getProjectId(), false);
			return new ServerResponse("0", "Success", projectTask);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("TASK0002-2", String.format("Error updating start date for task %d: %s", taskId, e.getMessage()), e);
		}
	}

	/**
	 * @param session
	 * @param taskId
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse remove(HttpSession session, int taskId) throws OptimaException {
		EntityController<ProjectTask> controller = new EntityController<ProjectTask>(session.getServletContext());
		try {
			ProjectTask task = controller.find(ProjectTask.class, taskId);
			controller.remove(ProjectTask.class, taskId);

			if (task != null) {
				adjustStartDateBasedOnTaskDependency(session, task.getProject().getProjectId(), false);
			}
			return new ServerResponse("0", "Success", null);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("TASK0004", String.format("Error removing ProjectTask %d: %s", taskId, e.getMessage()), e);
		}
	}

	/**
	 * @param session
	 * @param taskId
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse find(HttpSession session, int taskId) throws OptimaException {
		EntityController<ProjectTask> controller = new EntityController<ProjectTask>(session.getServletContext());
		try {
			ProjectTask projectTask = controller.find(ProjectTask.class, taskId);
			return new ServerResponse("0", "Success", projectTask);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("TASK0003", String.format("Error looking up projectTask %d: %s", taskId, e.getMessage()), e);
		}
	}

	/**
	 * @param session
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse findAll(HttpSession session) throws OptimaException {
		EntityController<ProjectTask> controller = new EntityController<ProjectTask>(session.getServletContext());
		try {
			List<ProjectTask> projectTasks = controller.findAll(ProjectTask.class);
			return new ServerResponse("0", "Success", projectTasks);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("TASK0005", String.format("Error loading projectTasks : %s", e.getMessage()), e);
		}
	}

	/**
	 * @param session
	 * @param projectId
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse findAllByProject(HttpSession session, int projectId) throws OptimaException {
		// EntityController<ProjectTask> controller = new
		// EntityController<ProjectTask>(session.getServletContext());
		try {

			EntityController<Project> projectcontroller = new EntityController<Project>(session.getServletContext());
			Project project = projectcontroller.find(Project.class, projectId);
			// List<ProjectTask> projectTasks =
			// controller.findAll(ProjectTask.class ,
			// "Select * FROM ProjectTask WHERE project = 1 " , project);
			return new ServerResponse("0", "Success", project.getProjectTasks());
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("TASK0006", String.format("Error loading projectPayments : %s", e.getMessage()), e);
		}
	}

	/**
	 * @param session
	 * @param taskId
	 * @return
	 * @throws OptimaException
	 *             This function should return only the tasks on which task(taskId) could depend
	 */

	public ServerResponse findAllByProjectForCertainTask(HttpSession session, int projectId, int taskId) throws OptimaException {
		try {

			EntityController<Project> projectcontroller = new EntityController<Project>(session.getServletContext());
			Project project = projectcontroller.find(Project.class, projectId);

			EntityController<ProjectTask> ProjectTaskcontroller = new EntityController<ProjectTask>(session.getServletContext());
			ProjectTask currentTask = ProjectTaskcontroller.find(ProjectTask.class, taskId);

			List<ProjectTask> projectTasksTemp = new ArrayList<>(project.getProjectTasks());

			projectTasksTemp.remove(currentTask);

			for (TaskDependency depRelations : currentTask.getAsDependent()) {
				projectTasksTemp.remove(project.findTask(depRelations.getDependency()));
			}

			projectTasksTemp = removeForwardDependencies(session, projectTasksTemp, currentTask);
			return new ServerResponse("0", "Success", projectTasksTemp);

		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("TASK0006", String.format("Error finding task dependencies : %s", e.getMessage()), e);
		}
	}

	/**
	 * @param projectTasksTemp
	 * @param currentTask
	 */
	private List<ProjectTask> removeForwardDependencies(HttpSession session, List<ProjectTask> projectTasksTemp, ProjectTask currentTask) {
		for (TaskDependency depRelations : currentTask.getAsDependency()) {
			projectTasksTemp.remove(currentTask.getProject().findTask(depRelations.getDependent()));
			projectTasksTemp = removeForwardDependencies(session, projectTasksTemp, currentTask.getProject().findTask(depRelations.getDependent()));
		}
		return projectTasksTemp;
	}

	/**
	 * @param session
	 * @param projectId
	 * @param taskIdOne
	 * @param taskIdtwo
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse addTaskDependency(HttpSession session, int taskIdOne, int taskIdtwo) throws OptimaException {

		// Create a Task Controller
		EntityController<ProjectTask> projectTaskcontroller = new EntityController<ProjectTask>(session.getServletContext());

		try {

			// Get task one and two by using task Id one and two
			ProjectTask taskOne = projectTaskcontroller.find(ProjectTask.class, taskIdOne);
			ProjectTask taskTwo = projectTaskcontroller.find(ProjectTask.class, taskIdtwo);

			// Create a TaskDependency controller and object, 5set to the object
			// the project as well as the dependencies
			EntityController<TaskDependency> taskDependencyController = new EntityController<TaskDependency>(session.getServletContext());
			TaskDependency taskDependency = new TaskDependency();
			taskDependency.setDependency(taskOne.getTaskId());
			taskDependency.setDependent(taskTwo.getTaskId());

			// Persist the taskDependency using the controller
			taskDependencyController.persist(taskDependency);
			adjustStartDateBasedOnTaskDependency(session, taskOne.getProject().getProjectId(), false);

			return new ServerResponse("0", "Success", taskDependency);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("TASK0007", String.format("Error Adding task dependencies : %s", e.getMessage()), e);
		}
	}

	public ServerResponse updateTaskDependency(HttpSession session, int taskDependencyId, int lag) throws OptimaException {
		EntityController<TaskDependency> controller = new EntityController<TaskDependency>(session.getServletContext());
		try {
			TaskDependency dependency = controller.find(TaskDependency.class, taskDependencyId);
			dependency.setLag(lag);
			controller.merge(dependency);
			return new ServerResponse("0", "Success", null);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("TASK0008", String.format("Error removing TaskDependency %d: %s", taskDependencyId, e.getMessage()), e);
		}
	}

	/**
	 * @param session
	 * @param taskDependencyId
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse removeTaskDependency(HttpSession session, int taskDependencyId) throws OptimaException {
		EntityController<TaskDependency> controller = new EntityController<TaskDependency>(session.getServletContext());
		EntityController<ProjectTask> tcontroller = new EntityController<ProjectTask>(session.getServletContext());
		try {
			TaskDependency dependency = controller.find(TaskDependency.class, taskDependencyId);
			controller.remove(TaskDependency.class, taskDependencyId);
			ProjectTask task = tcontroller.find(ProjectTask.class, dependency.getDependency());
			adjustStartDateBasedOnTaskDependency(session, task.getProject().getProjectId(), false);
			return new ServerResponse("0", "Success", null);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("TASK0008", String.format("Error removing TaskDependency %d: %s", taskDependencyId, e.getMessage()), e);
		}
	}

	/**
	 * @param session
	 * @param taskDependencyId
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse removeTaskDependency(HttpSession session, int dependentTaskId, int dependencyTaskId) throws OptimaException {
		EntityController<ProjectTask> controller = new EntityController<ProjectTask>(session.getServletContext());
		try {

			ProjectTask dependentTask = controller.find(ProjectTask.class, dependentTaskId);

			List<TaskDependency> dependencies = dependentTask.getAsDependent();
			for (TaskDependency dependency : dependencies) {
				if (dependency.getDependency() == dependencyTaskId) {
					EntityController<TaskDependency> depTaskcontroller = new EntityController<TaskDependency>(session.getServletContext());
					depTaskcontroller.remove(TaskDependency.class, dependency.getDependencyId());
					return new ServerResponse("0", "Success", null);
				}
			}
			adjustStartDateBasedOnTaskDependency(session, dependentTask.getProject().getProjectId(), false);
			return new ServerResponse("TASK0010", String.format("Task Dependency is not defined"), null);

		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("TASK0009", String.format("Error removing TaskDependency %d %d: %s", dependentTaskId, dependencyTaskId, e.getMessage()), e);
		}
	}

	/**
	 * @param session
	 * @param projectId
	 */
	public void adjustStartDateBasedOnTaskDependency(final HttpSession session, int projectId, boolean resetFirst) {
		EntityController<ProjectTask> taskController = new EntityController<ProjectTask>(session.getServletContext(), false);

		try {
			EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
			if (resetFirst) {
				Project project = controller.find(Project.class, projectId);
				taskController.dml(ProjectTask.class, "Update ProjectTask t set t.calendarStartDate = null , t.scheduledStartDate = null where t.project = ?1", project);
			}

			// We need to reread the project here in all cases.
			Project project = controller.find(Project.class, projectId);
			List<ProjectTask> rootTasks = getRootTasks(project, taskController);
			taskController.mergeTransactionStart();
			List<ProjectTask> tasksToSave = new ArrayList<ProjectTask>();
			for (ProjectTask task : rootTasks) {
				processTask(session, task, project, taskController, tasksToSave);
			}
			for (ProjectTask t : tasksToSave) {
				taskController.mergeTransactionMerge(t);
			}
			taskController.mergeTransactionClose();
			
			try {
				final Project p  = project;
				new Thread() {
					public void run() {
						ProjectSolutionDetails details = new ProjectSolutionDetails(false, p);
						details.savePaymentToDB(session);
					}
				}.start();
			} catch (Exception e) {

			}

		} catch (EntityControllerException e) {
			e.printStackTrace();
		} finally {
			if (taskController != null) {
				taskController.closeLocalManager();
			}
		}
	}

	public static int getLag(ProjectTask source, ProjectTask target) {
		for (TaskDependency dep : source.getAsDependency()) {
			if (dep.getDependent() == target.getTaskId()) {
				return dep.getLag();
			}
		}
		;
		return 0;
	}

	protected void processTask(HttpSession session, ProjectTask task, Project project, EntityController<ProjectTask> controller, List<ProjectTask> tasksToSave) throws EntityControllerException {
		// Bug#1 Shifting is not working correctly when changing weekends! -- BassemVic
		// System.out.println(task.getTaskId());
		// task = controller.find(ProjectTask.class, task.getTaskId());

		calculateCalederDuration(project, task);
		// controller.mergeTransactionMerge(task);
		tasksToSave.add(task);
		for (TaskDependency dependency : task.getAsDependency()) {
			ProjectTask nextTask = project.findTask(dependency.getDependent());
			if (nextTask != null) {
				Date nextTaskStartDate = nextTask.getCalendarStartDate();
				Calendar cal = Calendar.getInstance();
				cal.setTime(task.getCalendarStartDate());
				cal.add(Calendar.DATE, task.getCalenderDuration() + getLag(task, nextTask));
				if (nextTask.getCalendarStartDate() == null || nextTask.getCalendarStartDate().before(cal.getTime())) {
					Date newDate = adjustStart(project, cal.getTime());
					if (nextTask.getType()==ProjectTask.TYPE_MILESTONE_END) {
						newDate = task.getType()==ProjectTask.TYPE_NPRMAL?adjustStartBackword(project,TaskUtil.addDays(newDate, -1)):task.getCalendarStartDate();
					} else if (nextTask.getType()==ProjectTask.TYPE_MILESTONE_START && task.getType()==ProjectTask.TYPE_MILESTONE_END) {
						newDate = adjustStart(project, TaskUtil.addDays(cal.getTime(),1));
					}
					if (nextTaskStartDate == null || nextTaskStartDate.before(newDate)) {
						nextTask.setCalendarStartDate(newDate);
						nextTask.setTentativeStartDate(newDate);
						// controller.mergeTransactionMerge(nextTask);
					}
				}
				processTask(session, project.findTask(dependency.getDependent()), project, controller, tasksToSave);
			}
		}
	}

	/**
	 * @param project
	 * @param task
	 */
	protected void calculateCalederDuration(Project project, ProjectTask task) {
		if (task.getType()==ProjectTask.TYPE_NPRMAL) {
			task.setCalenderDuration(getDuration(project, task.getCalendarStartDate(), task.getDuration()));
		} else {
			task.setCalenderDuration(0);
		}

	}

	public static int getDuration(Project project, Date startDate, int originalDuration) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		int duration = originalDuration;
		int calendarDuration = 0;
		while (duration > 0) {
			if (PaymentUtil.isDayOff(startDate, project.getDaysOffs()) || TaskUtil.isWeekendDay(startDate, project.getWeekend())) {
				calendarDuration++;
			} else {
				duration--;
				calendarDuration++;
			}
			calendar.add(Calendar.DATE, 1);
			startDate = calendar.getTime();

		}
		return calendarDuration;
	}

	/**
	 * @param project
	 * @return
	 */
	public List<ProjectTask> getRootTasks(Project project, EntityController<ProjectTask> controller) throws EntityControllerException {
		ArrayList<ProjectTask> rootTasks = new ArrayList<ProjectTask>();
		for (ProjectTask task : project.getProjectTasks()) {
			if (task.getAsDependent() == null || task.getAsDependent().isEmpty()) {
				rootTasks.add(task);
				task.setCalendarStartDate(adjustStart(project, task.calculateEffectiveTentativeStartDate()));
				controller.merge(task);
			}
		}
		return rootTasks;
	}

	/**
	 * adjust the calendar start to
	 * 
	 * @return
	 */
	private static Date adjustStart(Project project, Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		while (true) {
			if (!PaymentUtil.isDayOff(date, project.getDaysOffs()) && !TaskUtil.isWeekendDay(date, project.getWeekend())) {
				return date;
			}

			calendar.add(Calendar.DATE, 1);
			date = calendar.getTime();
		}
	}

	private static Date adjustStartBackword(Project project, Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		while (true) {
			if (!PaymentUtil.isDayOff(date, project.getDaysOffs()) && !TaskUtil.isWeekendDay(date, project.getWeekend())) {
				return date;
			}

			calendar.add(Calendar.DATE, -1);
			date = calendar.getTime();
		}
	}

	
	public ServerResponse resetScheduling(HttpSession session, int projectId) {
		adjustStartDateBasedOnTaskDependency(session, projectId, true);
		return new ServerResponse("0", "Success", null);
	}

}
