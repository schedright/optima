package com.softpoint.optima;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.jabsorb.JSONRPCBridge;

import com.softpoint.optima.control.DaysOffController;
import com.softpoint.optima.control.ExtraPaymentController;
import com.softpoint.optima.control.FinanceController;
import com.softpoint.optima.control.PaymentController;
import com.softpoint.optima.control.PortfolioController;
import com.softpoint.optima.control.ProjectController;
import com.softpoint.optima.control.TaskController;
import com.softpoint.optima.control.UsersController;

public class JsonRpcInitializer implements ServletContextListener {
	public static final String __ENTITY_FACTORY = "entityManagerFactory";
	public static final String __LOG__FACTORY = "logFactory";
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */

	public void contextInitialized(ServletContextEvent event) {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

		JSONRPCBridge globalBridge = JSONRPCBridge.getGlobalBridge();
		JSONRPCBridge.getSerializer().setMarshallClassHints(false);
		globalBridge.registerObject("optimaServer", new OptimaService());
		globalBridge.registerObject("projectService", new ProjectController());
		globalBridge.registerObject("portfolioService", new PortfolioController());
		globalBridge.registerObject("daysOffService", new DaysOffController());
		globalBridge.registerObject("financeService", new FinanceController());
		globalBridge.registerObject("extraPaymentService", new ExtraPaymentController());
		globalBridge.registerObject("taskService", new TaskController());
		globalBridge.registerObject("paymentService", new PaymentController());
		globalBridge.registerObject("usersService", new UsersController());

		Map addedOrOverridenProperties = new HashMap();
		
		String name = event.getServletContext().getContextPath();
		if (name.startsWith("/")) {
			name = name.substring(1);
		}

		try {
			File configDir = new File(System.getProperty("catalina.base"), "conf");
			File configFile = new File(configDir, "db.properties");
			InputStream stream = new FileInputStream(configFile);
			Properties props = new Properties();
			props.load(stream);
			if (props.get(name+"-url")!=null) {
				addedOrOverridenProperties.put("javax.persistence.jdbc.url", props.get(name+"-url"));
			}
			if (props.get(name+"-username")!=null) {
				addedOrOverridenProperties.put("javax.persistence.jdbc.user", props.get(name+"-username"));
			}
			if (props.get(name+"-password")!=null) {
				addedOrOverridenProperties.put("javax.persistence.jdbc.password", props.get(name+"-password"));
			}

		} catch (Exception e) {
		}

		// System.getProperties().list(System.out);

		EntityManagerFactory factory = Persistence.createEntityManagerFactory("Optima1.0", addedOrOverridenProperties);

		event.getServletContext().setAttribute(__ENTITY_FACTORY, factory);
		EntityManager manager = factory.createEntityManager();
		manager.close();
		// System.out.println("Creating Optima LogFactory");
		OptimaLogFactory logFactory = new OptimaLogFactory(event.getServletContext());

		event.getServletContext().setAttribute(__LOG__FACTORY, logFactory);
		// System.out.println("Optima LogFactory created successfully...");

	}

	public void contextDestroyed(ServletContextEvent event) {
		EntityManagerFactory factory = (EntityManagerFactory) event.getServletContext().getAttribute(__ENTITY_FACTORY);
		if (factory != null) {
			factory.close();
		}
	}
}