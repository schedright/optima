package com.softpoint.optima;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.jabsorb.JSONRPCBridge;

import com.softpoint.optima.control.ClientController;
import com.softpoint.optima.control.DaysOffController;
import com.softpoint.optima.control.FinanceController;
import com.softpoint.optima.control.LocationInfoController;
import com.softpoint.optima.control.PaymentController;
import com.softpoint.optima.control.PortfolioController;
import com.softpoint.optima.control.ProjectController;
import com.softpoint.optima.control.TaskController;


public class JsonRpcInitializer implements ServletContextListener {
	public static final String __ENTITY_FACTORY = "entityManagerFactory";
	public static final String __LOG__FACTORY = "logFactory";
	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	
	
	public void contextInitialized(ServletContextEvent event) {
		
		JSONRPCBridge globalBridge = JSONRPCBridge.getGlobalBridge();
		JSONRPCBridge.getSerializer().setMarshallClassHints(false);
		globalBridge.registerObject("optimaServer", new OptimaService() );
		globalBridge.registerObject("projectService", new ProjectController() );
		globalBridge.registerObject("portfolioService" , new PortfolioController());
		globalBridge.registerObject("locationService" , new LocationInfoController());
		globalBridge.registerObject("clientService" , new ClientController());
		globalBridge.registerObject("daysOffService" , new DaysOffController());
		globalBridge.registerObject("financeService" , new FinanceController());
		globalBridge.registerObject("taskService" , new TaskController());
		globalBridge.registerObject("paymentService" , new PaymentController());
		
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("Optima1.0");
		event.getServletContext().setAttribute(__ENTITY_FACTORY, factory);
		EntityManager manager = factory.createEntityManager();
		manager.close();
		System.out.println("Creating Optima LogFactory");
		OptimaLogFactory logFactory = new OptimaLogFactory(event.getServletContext());

		event.getServletContext().setAttribute(__LOG__FACTORY , logFactory);
		System.out.println("Optima LogFactory created successfully...");
		
	}

	public void contextDestroyed(ServletContextEvent event) {
		EntityManagerFactory factory = (EntityManagerFactory)event.getServletContext().getAttribute(__ENTITY_FACTORY);
		if (factory != null) {
			factory.close();
		}
	}
}