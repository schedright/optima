package com.softpoint.optima.control;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import com.softpoint.optima.OptimaException;
import com.softpoint.optima.ServerResponse;
import com.softpoint.optima.db.Portfolio;
import com.softpoint.optima.db.PortfolioFinance;
import com.softpoint.optima.util.PaymentUtil;

/**
 * @author user mhamdy
 *
 */
public class FinanceController {

	
	/**
	 * @param session
	 * @param portofolioId
	 * @param financingAmount
	 * @param financeEndDate
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse create(HttpSession session ,int portofolioId, double financingAmount, Date financeEndDate) throws OptimaException {
		
		EntityController<PortfolioFinance> controller = new EntityController<PortfolioFinance>(session.getServletContext());
		
		PortfolioFinance portofolioFinance = new PortfolioFinance();
		portofolioFinance.setFinanceAmount(new BigDecimal(financingAmount));
		portofolioFinance.setFinanceUntillDate(financeEndDate);
		
		try {
			
			EntityController<Portfolio> PortofolioController = new EntityController<Portfolio>(session.getServletContext());
			Portfolio portfolio = PortofolioController.find(Portfolio.class, portofolioId);
			portofolioFinance.setPortfolio(portfolio);
			controller.persist(portofolioFinance);
			return new ServerResponse("0", "Success", portofolioFinance);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("FINA0001" , String.format("Error creating Finance Portoflio %s: %s" , portofolioFinance , e.getMessage() ), e);
		}
	}
	

	/**
	 * @param session
	 * @param financeId
	 * @param portofolioId
	 * @param financingAmount
	 * @param financeEndDate
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse update(HttpSession session ,  int financeId, int portofolioId,  double financingAmount, Date financeEndDate) throws OptimaException {
		//EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		EntityController<PortfolioFinance> controller = new EntityController<PortfolioFinance>(session.getServletContext());
		
		PortfolioFinance portofolioFinance =null;
		Portfolio portfolio = null;
		try {
			//Get the portfolio from the portfolioId
			EntityController<Portfolio> portfolioController = new EntityController<Portfolio>(session.getServletContext());
			portfolio = portfolioController.find(Portfolio.class, portofolioId);
			
			//Set the portfolioFinance
			portofolioFinance = controller.find(PortfolioFinance.class, financeId);
			portofolioFinance.setFinanceAmount(new BigDecimal(financingAmount));
			portofolioFinance.setFinanceUntillDate(financeEndDate);
			portofolioFinance.setPortfolio(portfolio);
			
			//merge the portfolioFinance
			controller.merge(portofolioFinance);
			return new ServerResponse("0", "Success", portofolioFinance);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("FINA0002" , String.format("Error updating portfolio_finance for Portfolio %s: %s" , portfolio!=null?portfolio.getPortfolioName():"", e.getMessage() ), e);
		}
	}
	
	/**
	 * @param session
	 * @param financeId
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse remove(HttpSession session , int financeId) throws OptimaException {
		EntityController<PortfolioFinance> controller = new EntityController<PortfolioFinance>(session.getServletContext());
		try {
			controller.remove(PortfolioFinance.class , financeId);
			return new ServerResponse("0", "Success", null);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("FINA0004" , String.format("Error removing PortfolioFinance %d: %s" , financeId , e.getMessage() ), e);
		}
	}

	
	/**
	 * @param session
	 * @param financeId
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse find(HttpSession session , int financeId) throws OptimaException {
		EntityController<PortfolioFinance> controller = new EntityController<PortfolioFinance>(session.getServletContext());
		try {
			PortfolioFinance portfolioFinance = controller.find(PortfolioFinance.class , financeId);
			return new ServerResponse("0", "Success",portfolioFinance);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("FINA0003" , String.format("Error looking up finance %d: %s" , financeId , e.getMessage() ), e);
		}
	}
		

	/**
	 * @param session
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse findAll(HttpSession session) throws OptimaException {
		EntityController<PortfolioFinance> controller = new EntityController<PortfolioFinance>(session.getServletContext());
		try {
			List<PortfolioFinance> portfolioFinances = controller.findAll(PortfolioFinance.class);
			return new ServerResponse("0", "Success", portfolioFinances);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("FINA0005" , String.format("Error loading finances : %s" , e.getMessage() ), e);
		}
	}
	
	/**
	 * @param session
	 * @param portfolioId
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse findAllByPortfolio(HttpSession session , int portfolioId) throws OptimaException {
		try {
			EntityController<Portfolio> portfolioController = new EntityController<Portfolio>(session.getServletContext());
			Portfolio portfolio = portfolioController.find(Portfolio.class, portfolioId);
			List<PortfolioFinance> lFinances = portfolio.getPortfolioFinances();
			// this list needs to be sorted by finance date
			return new ServerResponse("0", "Success", lFinances);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("DOFF0006" , String.format("Error loading finances : %s" , e.getMessage() ), e);
		}
	}

	
	/**
	 * @param session
	 * @param portfolioId
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse findFinanceByDate(HttpSession session , int portfolioId , Date toDate) throws OptimaException {
		try {
			
			double financeLimit = PaymentUtil.getFinanceLimit(session, portfolioId,
					toDate);
			// this list needs to be sorted by finance date
			return new ServerResponse("0", "Success",financeLimit );
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("DOFF0011" , String.format("Error finding finance limit by date : %s" , e.getMessage() ), e);
		}
	}


	
	
	
	
}
