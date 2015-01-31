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
    @ManyToOne
	@JoinColumn(name="dependency_task_id")
	private ProjectTask dependency;

	//bi-directional many-to-one association to ProjectTask
    @ManyToOne
	@JoinColumn(name="dependant_task_id")
	private ProjectTask dependent;

    public TaskDependency() {
    }

	public int getDependencyId() {
		return this.dependencyId;
	}

	public void setDependencyId(int dependencyId) {
		this.dependencyId = dependencyId;
	}

	public ProjectTask getDependency() {
		return this.dependency;
	}

	public void setDependency(ProjectTask dependency) {
		this.dependency = dependency;
	}
	
	public ProjectTask getDependent() {
		return this.dependent;
	}

	public void setDependent(ProjectTask dependent) {
		this.dependent = dependent;
	}
	
}