package com.softpoint.optima.control;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import com.softpoint.optima.OptimaException;
import com.softpoint.optima.ServerResponse;
import com.softpoint.optima.db.Portfolio;
import com.softpoint.optima.db.PortfolioFinance;
import com.softpoint.optima.db.PortfolioLight;
import com.softpoint.optima.db.Project;
import com.softpoint.optima.db.ProjectLight;
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
	public ServerResponse create(HttpSession session, int portofolioId, int projectId, double financingAmount,
			double interestRate, Date financeEndDate) throws OptimaException {

		EntityController<PortfolioFinance> controller = new EntityController<PortfolioFinance>(
				session.getServletContext());

		PortfolioFinance portofolioFinance = new PortfolioFinance();
		portofolioFinance.setFinanceAmount(new BigDecimal(financingAmount));
		portofolioFinance.setInterestRate(new BigDecimal(interestRate));
		portofolioFinance.setFinanceUntillDate(financeEndDate);

		try {
			if (portofolioId != 0) {
				EntityController<PortfolioLight> PortofolioController = new EntityController<PortfolioLight>(
						session.getServletContext());
				PortfolioLight portfolio = PortofolioController.find(PortfolioLight.class, portofolioId);
				portofolioFinance.setPortfolio(portfolio);
			}
			if (projectId != 0) {
				EntityController<ProjectLight> projectController = new EntityController<ProjectLight>(
						session.getServletContext());
				ProjectLight project = projectController.find(ProjectLight.class, projectId);
				portofolioFinance.setProject(project);

			}
			controller.persist(portofolioFinance);
			return new ServerResponse("0", "Success", portofolioFinance);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("FINA0001",
					String.format("Error creating Finance Portoflio %s: %s", portofolioFinance, e.getMessage()), e);
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
	public ServerResponse update(HttpSession session, int financeId, int portofolioId, int projectId,
			double financingAmount, double interestRate, Date financeEndDate) throws OptimaException {
		// EntityController<Portfolio> controller = new
		// EntityController<Portfolio>(session.getServletContext());
		EntityController<PortfolioFinance> controller = new EntityController<PortfolioFinance>(
				session.getServletContext());

		PortfolioFinance portofolioFinance = null;
		PortfolioLight portfolio = null;
		ProjectLight project = null;
		try {
			// Get the portfolio from the portfolioId
			if (portofolioId != 0) {
				EntityController<PortfolioLight> PortofolioController = new EntityController<PortfolioLight>(
						session.getServletContext());
				portfolio = PortofolioController.find(PortfolioLight.class, portofolioId);
			}
			if (projectId != 0) {
				EntityController<ProjectLight> projectController = new EntityController<ProjectLight>(
						session.getServletContext());
				project = projectController.find(ProjectLight.class, projectId);
			}

			// Set the portfolioFinance
			portofolioFinance = controller.find(PortfolioFinance.class, financeId);
			portofolioFinance.setFinanceAmount(new BigDecimal(financingAmount));
			portofolioFinance.setInterestRate(new BigDecimal(interestRate));
			portofolioFinance.setFinanceUntillDate(financeEndDate);
			portofolioFinance.setPortfolio(portfolio);
			portofolioFinance.setProject(project);

			// merge the portfolioFinance
			controller.merge(portofolioFinance);
			return new ServerResponse("0", "Success", portofolioFinance);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("FINA0002", String.format("Error updating portfolio_finance for Portfolio %s: %s",
					portfolio != null ? portfolio.getPortfolioName() : "", e.getMessage()), e);
		}
	}

	/**
	 * @param session
	 * @param financeId
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse remove(HttpSession session, int financeId) throws OptimaException {
		EntityController<PortfolioFinance> controller = new EntityController<PortfolioFinance>(
				session.getServletContext());
		try {
			controller.remove(PortfolioFinance.class, financeId);
			return new ServerResponse("0", "Success", null);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("FINA0004",
					String.format("Error removing PortfolioFinance %d: %s", financeId, e.getMessage()), e);
		}
	}

	/**
	 * @param session
	 * @param financeId
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse find(HttpSession session, int financeId) throws OptimaException {
		EntityController<PortfolioFinance> controller = new EntityController<PortfolioFinance>(
				session.getServletContext());
		try {
			PortfolioFinance portfolioFinance = controller.find(PortfolioFinance.class, financeId);
			return new ServerResponse("0", "Success", portfolioFinance);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("FINA0003",
					String.format("Error looking up finance %d: %s", financeId, e.getMessage()), e);
		}
	}

	/**
	 * @param session
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse findAll(HttpSession session) throws OptimaException {
		EntityController<PortfolioFinance> controller = new EntityController<PortfolioFinance>(
				session.getServletContext());
		try {
			List<PortfolioFinance> portfolioFinances = controller.findAll(PortfolioFinance.class);
			return new ServerResponse("0", "Success", portfolioFinances);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("FINA0005", String.format("Error loading finances : %s", e.getMessage()), e);
		}
	}

	/**
	 * @param session
	 * @param portfolioId
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse findAllByPortfolio(HttpSession session, int portfolioId, int projectId)
			throws OptimaException {
		try {
			List<PortfolioFinance> lFinances = null;
			if (portfolioId != 0) {
				EntityController<Portfolio> portfolioController = new EntityController<Portfolio>(
						session.getServletContext());
				Portfolio portfolio = portfolioController.find(Portfolio.class, portfolioId);
				lFinances = portfolio.getPortfolioFinances();
			} else if (projectId != 0) {
				EntityController<Project> controller = new EntityController<Project>(
						session.getServletContext());
				Project portfolio = controller.find(Project.class, projectId);
				lFinances = portfolio.getPortfolioFinances();
			}
			// this list needs to be sorted by finance date
			return new ServerResponse("0", "Success", lFinances);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("DOFF0006", String.format("Error loading finances : %s", e.getMessage()), e);
		}
	}

	/**
	 * @param session
	 * @param portfolioId
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse findFinanceByDate(HttpSession session, int portfolioId, Date toDate) throws OptimaException {
		try {

			double financeLimit = PaymentUtil.getFinanceLimit(session, portfolioId, toDate);
			// this list needs to be sorted by finance date
			return new ServerResponse("0", "Success", financeLimit);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("DOFF0011",
					String.format("Error finding finance limit by date : %s", e.getMessage()), e);
		}
	}

}
