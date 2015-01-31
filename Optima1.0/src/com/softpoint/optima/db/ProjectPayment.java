package com.softpoint.optima.db;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;


/**
 * The persistent class for the project_payment database table.
 * 
 */
@Entity
@Table(name="project_payment")
public class ProjectPayment implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="payment_id")
	private int paymentId;

	@Column(name="payment_amount")
	private BigDecimal paymentAmount;

	@Column(name="payment_initial_amount")
	private BigDecimal paymentInitialAmount;

	@Temporal( TemporalType.DATE)
	@Column(name="payment_date")
	private Date paymentDate;

	@Column(name="payment_interim_number")
	private String paymentInterimNumber;

	//bi-directional many-to-one association to PaymentType
    @ManyToOne
	@JoinColumn(name="payment_type_id")
	private PaymentType paymentType;

	//bi-directional many-to-one association to Project
    @ManyToOne
	@JoinColumn(name="project_id")
	private Project project;

    public ProjectPayment() {
    }

	public int getPaymentId() {
		return this.paymentId;
	}

	public void setPaymentId(int paymentId) {
		this.paymentId = paymentId;
	}

	public BigDecimal getPaymentAmount() {
		return this.paymentAmount;
	}

	public void setPaymentAmount(BigDecimal paymentAmount) {
		this.paymentAmount = paymentAmount;
	}

	public BigDecimal getPaymentInitialAmount() {
		return this.paymentInitialAmount;
	}

	public void setPaymentInitialAmount(BigDecimal paymentInitialAmount) {
		this.paymentInitialAmount = paymentInitialAmount;
	}
	
	public Date getPaymentDate() {
		return this.paymentDate;
	}

	public void setPaymentDate(Date paymentDate) {
		this.paymentDate = paymentDate;
	}

	public String getPaymentInterimNumber() {
		return this.paymentInterimNumber;
	}

	public void setPaymentInterimNumber(String paymentInterimNumber) {
		this.paymentInterimNumber = paymentInterimNumber;
	}

	public PaymentType getPaymentType() {
		return this.paymentType;
	}

	public void setPaymentType(PaymentType paymentType) {
		this.paymentType = paymentType;
	}
	
	public Project getProject() {
		return this.project;
	}

	public void setProject(Project project) {
		this.project = project;
	}
	
}