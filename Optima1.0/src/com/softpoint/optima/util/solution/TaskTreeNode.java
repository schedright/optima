package com.softpoint.optima.util.solution;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.softpoint.optima.control.TaskController;
import com.softpoint.optima.db.ProjectTask;
import com.softpoint.optima.util.PaymentUtil;
import com.softpoint.optima.util.TaskUtil;

public class TaskTreeNode {

	ProjectTask task;
	ProjectWrapper projectW;
	List<TaskTreeNode> children;
	List<TaskTreeNode> parents;

	Date calculatedTaskEnd;
	Date calculatedTaskStart;
	int calendarDuration;
	int durationWithChildren;

	int daysShift;

	int completedDays;

	Map<TaskTreeNode,Integer> undoList;
	public TaskTreeNode() {
		undoList = new HashMap<TaskTreeNode,Integer>();
	}
	
	public TaskTreeNode(ProjectTask task, ProjectWrapper projectW) {
		super();
		this.task = task;
		this.projectW = projectW;
		children = new ArrayList<TaskTreeNode>();
		parents = new ArrayList<TaskTreeNode>();
		daysShift = 0;
		calculatedTaskEnd = null;
		calculatedTaskStart = null;
		durationWithChildren = -1;
		undoList = new HashMap<TaskTreeNode,Integer>();
	}


	/**
	 * shift the task by few days. it will clear all the start days of all tasks depending on it and will change all the duration with children of both children and parent
	 * so it will just clear them and they will be recalculated the first time they are needed
	 * @param days
	 */
	int shift(int days) {
		daysShift += days;
		if (days>0) {
			//this will be needed if we have task A and task B that dependes on A
			//if B is shifted first, it's days shift will be 1
			//then if we shift A, we need to move back A, otherwise it will be shifted twice
			//but in case moving A wasn't the best solution, we might need to undo, so we don't want to lose B status, 
			//so I keep them in the undoList
			undoList.clear();
			for (TaskTreeNode child:getChildren()) {
				if (child.daysShift>0) {
					undoList.put(child, child.daysShift);
					child.daysShift = 0;
				}
			}
		} else if (days<0){
			for (TaskTreeNode child:getChildren()) {
				if (undoList.containsKey(child)) {
					child.daysShift = undoList.get(child);
				}
			}
			undoList.clear();
		}
		
		clearStartAndEndDateRecursive(this);
		if (days>0) {
			Date startDate = getCalculatedTaskStart();
			//TODO find faster way, when hitting a weekend or vacation, the days shift will not be updated so for the next few shiftings they will be identical
			while (true) {
				daysShift++;
				days++;
				clearStartAndEndDateRecursive(this);
				Date date2 = getCalculatedTaskStart();
				if (date2.after(startDate)) {
					daysShift--;
					days--;
					clearStartAndEndDateRecursive(this);
					break;
				}
			}
		}
		return days;
	}
	
	private void clearStartAndEndDateRecursive(TaskTreeNode taskTreeNode) {
		taskTreeNode.calculatedTaskStart = null;
		taskTreeNode.calculatedTaskEnd = null;
		for (TaskTreeNode child:taskTreeNode.getChildren()) {
			clearStartAndEndDateRecursive(child);
		}
		
		if (taskTreeNode.getChildren().size()==0) {
			clearDurationWithChildrenRecursive(taskTreeNode);
		}
	}

	private void clearDurationWithChildrenRecursive(TaskTreeNode taskTreeNode) {
		taskTreeNode.durationWithChildren = -1;
		for (TaskTreeNode parent:taskTreeNode.getParents()) {
			clearDurationWithChildrenRecursive(parent);
		}
	}
	/*
	 * return the calculated task end, it will use the parents time in case they
	 * got shifted
	 */
	public Date getCalculatedTaskEnd() {
		if (calculatedTaskEnd == null) {
			Date taskStart = task.calculateEffectiveTentativeStartDate();
			// first move the task based on dependencies
			for (TaskTreeNode pNode : parents) {
				int lag = TaskController.getLag(pNode.getTask(), task);
				Date e = TaskUtil.addDays(pNode.getCalculatedTaskEnd(), 1 + lag);
				while (PaymentUtil.isDayOff(e, projectW.getProjectVacations())
						|| TaskUtil.isWeekendDay(e, projectW.getProjectWeekends())) {
					e = TaskUtil.addDays(e, 1);
				}
				if (e.after(taskStart)) {
					taskStart = e;
				}
			}
			if (daysShift > 0) {
				taskStart = TaskUtil.addDays(taskStart, daysShift);
			}
			
			int duration = task.getDuration();
			// skip vacations and weekend
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(taskStart);
			int calendarDuration = 0;
			Date datePointer = taskStart;
			while (duration > 0) {
				if (PaymentUtil.isDayOff(datePointer, projectW.getProjectVacations())
						|| TaskUtil.isWeekendDay(datePointer, projectW.getProjectWeekends())) {
					calendarDuration++;
				} else {
					if (calculatedTaskStart == null) {
						calculatedTaskStart = datePointer;
					}
					duration--;
					calendarDuration++;
				}
				if (duration > 0) {
					calendar.add(Calendar.DATE, 1);
					datePointer = calendar.getTime();
				}
			}
			this.calendarDuration = calendarDuration;
			calculatedTaskEnd = datePointer;
		}
		return calculatedTaskEnd;
	}

	public void setCalculatedTaskEnd(Date calculatedTaskEnd) {
		this.calculatedTaskEnd = calculatedTaskEnd;
	}

	public Date getCalculatedTaskStart() {
		if (calculatedTaskStart == null) {
			getCalculatedTaskEnd();
		}
		return calculatedTaskStart;
	}

	public void setCalculatedTaskStart(Date calculatedTaskStart) {
		this.calculatedTaskStart = calculatedTaskStart;
	}

	public List<TaskTreeNode> getChildren() {
		return children;
	}

	public int getDurationWithChildren() {
		if (durationWithChildren == -1) {
			int maxChildLength = 0;
			Date taskEnd = getCalculatedTaskEnd();
			for (TaskTreeNode child : children) {
				int temp = child.getDurationWithChildren();
				Date taskStart = child.getCalculatedTaskStart();
				temp = temp + TaskUtil.daysBetween(taskEnd, taskStart)-1; //in case the child doesn't start right away
				if (temp > maxChildLength) {
					maxChildLength = temp;
				}
			}
			durationWithChildren = TaskUtil.daysBetween(getCalculatedTaskStart(),getCalculatedTaskEnd())+ 1 + maxChildLength;
		}
		return durationWithChildren;
	}

	public void setDurationWithChildren(int durationWithChildren) {
		this.durationWithChildren = durationWithChildren;
	}

	public List<TaskTreeNode> getParents() {
		return parents;
	}

	public ProjectTask getTask() {
		return task;
	}

	public void setTask(ProjectTask task) {
		this.task = task;
	}

	public String toString() {
		return task.getTaskName();
	}

	public int getCompletedDays() {
		return completedDays;
	}

	public void setCompletedDays(int completedDays) {
		this.completedDays = completedDays;
	}
}
