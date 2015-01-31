package com.softpoint.optima.db;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the payment_type database table.
 * 
 */
@Entity
@Table(name="payment_type")
public class PaymentType implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="payment_type_id")
	private int paymentTypeId;

	@Column(name="payment_type")
	private String paymentType;

	//bi-directional many-to-one association to ProjectPayment
	@OneToMany(mappedBy="paymentType")
	private List<ProjectPayment> projectPayments;

    public PaymentType() {
    }

	public int getPaymentTypeId() {
		return this.paymentTypeId;
	}

	public void setPaymentTypeId(int paymentTypeId) {
		this.paymentTypeId = paymentTypeId;
	}

	public String getPaymentType() {
		return this.paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public List<ProjectPayment> getProjectPayments() {
		return this.projectPayments;
	}

	public void setProjectPayments(List<ProjectPayment> projectPayments) {
		this.projectPayments = projectPayments;
	}
	
}