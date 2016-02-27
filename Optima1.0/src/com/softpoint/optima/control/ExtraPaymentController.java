package com.softpoint.optima.control;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import com.softpoint.optima.OptimaException;
import com.softpoint.optima.ServerResponse;
import com.softpoint.optima.db.Portfolio;
import com.softpoint.optima.db.PortfolioExtrapayment;

/**
 * @author user Bassem
 *
 */
public class ExtraPaymentController {

	
	public ServerResponse create(HttpSession session ,int portofolioId, double extraPaymentAmount, Date extraPaymentDate) throws OptimaException {
		
		EntityController<PortfolioExtrapayment> controller = new EntityController<PortfolioExtrapayment>(session.getServletContext());
		
		PortfolioExtrapayment portofolioExtraPayment = new PortfolioExtrapayment();
		portofolioExtraPayment.setExtraPayment_amount(new BigDecimal(extraPaymentAmount));
		portofolioExtraPayment.setExtraPayment_date(extraPaymentDate);
		
		try {
			
			EntityController<Portfolio> PortofolioController = new EntityController<Portfolio>(session.getServletContext());
			Portfolio portfolio = PortofolioController.find(Portfolio.class, portofolioId);
			portofolioExtraPayment.setPortfolio(portfolio);
			controller.persist(portofolioExtraPayment);
			return new ServerResponse("0", "Success", portofolioExtraPayment);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("FINA0001" , String.format("Error creating extra payment Portoflio %s: %s" , portofolioExtraPayment , e.getMessage() ), e);
		}
	}
	

	public ServerResponse update(HttpSession session ,  int extraPaymentId, int portofolioId,  double extraPaymentAmount, Date extraPaymentDate) throws OptimaException {
		EntityController<PortfolioExtrapayment> controller = new EntityController<PortfolioExtrapayment>(session.getServletContext());
		
		PortfolioExtrapayment portofolioExtraPayment =null;
		Portfolio portfolio = null;
		try {
			//Get the portfolio from the portfolioId
			EntityController<Portfolio> portfolioController = new EntityController<Portfolio>(session.getServletContext());
			portfolio = portfolioController.find(Portfolio.class, portofolioId);
			
			//Set the portfolioFinance
			portofolioExtraPayment = controller.find(PortfolioExtrapayment.class, extraPaymentId);
			portofolioExtraPayment.setExtraPayment_amount(new BigDecimal(extraPaymentAmount));
			portofolioExtraPayment.setExtraPayment_date(extraPaymentDate);
			portofolioExtraPayment.setPortfolio(portfolio);
			
			//merge the portfolioFinance
			controller.merge(portofolioExtraPayment);
			return new ServerResponse("0", "Success", portofolioExtraPayment);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("FINA0002" , String.format("Error updating portofolioExtraPayment for Portfolio %s: %s" , portfolio!=null?portfolio.getPortfolioName():"", e.getMessage() ), e);
		}
	}
	

	public ServerResponse remove(HttpSession session , int extraPaymentId) throws OptimaException {
		EntityController<PortfolioExtrapayment> controller = new EntityController<PortfolioExtrapayment>(session.getServletContext());
		try {
			controller.remove(PortfolioExtrapayment.class , extraPaymentId);
			return new ServerResponse("0", "Success", null);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("FINA0004" , String.format("Error removing extraPayment %d: %s" , extraPaymentId , e.getMessage() ), e);
		}
	}

	
	public ServerResponse find(HttpSession session , int extraPaymentId) throws OptimaException {
		EntityController<PortfolioExtrapayment> controller = new EntityController<PortfolioExtrapayment>(session.getServletContext());
		try {
			PortfolioExtrapayment portfolioExtraPayment = controller.find(PortfolioExtrapayment.class , extraPaymentId);
			return new ServerResponse("0", "Success",portfolioExtraPayment);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("FINA0003" , String.format("Error looking up portfolioExtraPayment %d: %s" , extraPaymentId , e.getMessage() ), e);
		}
	}
		

	public ServerResponse findAll(HttpSession session) throws OptimaException {
		EntityController<PortfolioExtrapayment> controller = new EntityController<PortfolioExtrapayment>(session.getServletContext());
		try {
			List<PortfolioExtrapayment> portfolioExtraPayments = controller.findAll(PortfolioExtrapayment.class);
			return new ServerResponse("0", "Success", portfolioExtraPayments);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("FINA0005" , String.format("Error loading portfolioExtraPayments : %s" , e.getMessage() ), e);
		}
	}
	

	public ServerResponse findAllByPortfolio(HttpSession session , int portfolioId) throws OptimaException {
		try {
			EntityController<Portfolio> portfolioController = new EntityController<Portfolio>(session.getServletContext());
			Portfolio portfolio = portfolioController.find(Portfolio.class, portfolioId);
			List<PortfolioExtrapayment> extraPayments = portfolio.getPortfolioExtrapayments();
			// this list needs to be sorted by finance date
			return new ServerResponse("0", "Success", extraPayments);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("DOFF0006" , String.format("Error loading extraPayments : %s" , e.getMessage() ), e);
		}
	}



	
	
	
	
}
