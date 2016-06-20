package com.softpoint.optima.db;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the task_dependency database table.
 * 
 */
@Entity
@Table(name="task_dependency")
public class TaskDependency implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="dependency_id")
	private int dependencyId;

	//bi-directional many-to-one association to ProjectTask
	@Column(name="dependency_task_id")
	private int dependency;

	//bi-directional many-to-one association to ProjectTask
	@Column(name="dependant_task_id")
	private int dependent;

	@Column(name="lag")
	private Integer lag;

    public TaskDependency() {
    }

	public int getDependencyId() {
		return this.dependencyId;
	}

	public void setDependencyId(int dependencyId) {
		this.dependencyId = dependencyId;
	}

	public int getDependency() {
		return this.dependency;
	}

	public void setDependency(int dependency) {
		this.dependency = dependency;
	}
	
	public int getDependent() {
		return this.dependent;
	}

	public void setDependent(int dependent) {
		this.dependent = dependent;
	}

	@Override
	public String toString() {
		return "TaskDependency [dependency=" + dependency + ", dependent=" + dependent + "]";
	}

	public Integer getLag() {
		if (lag==null) {
			return 0;
		}
		return lag;
	}

	public void setLag(Integer lag) {
		this.lag = lag;
	}
	
/*	public ProjectTask findTask(Project p, int id) {
		ProjectTask tsk = null;
		for (ProjectTask t:p.getProjectTasks()) {
			if (t.getTaskId()==id) {
				tsk=t;
				break;
			}
		}
		return tsk;
	}
*/	
}