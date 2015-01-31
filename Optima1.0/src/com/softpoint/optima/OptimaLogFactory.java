/**
 * 
 */
package com.softpoint.optima;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;


/**
 * @author WDARWISH
 *
 */

public class OptimaLogFactory {
	
	private Map<String , Logger> activeLoggers = new HashMap<>();
	FileAppender mainFileAppender;

	private PatternLayout layout;
	private PatternLayout outputLayout;
	private ServletContext context;
	/**
	 * 
	 */
	public OptimaLogFactory(ServletContext context) {
		layout = new PatternLayout("%d{ISO8601} [%t] %-5p %c %x - %m%n");
		outputLayout = new PatternLayout("%m%n");
		this.context = context;
	}
	
	
	
		public Logger getProjectLogger(String projectCode){
			
			RollingFileAppender rollingFileAppender = null;
			
			String newLoggerName = "OptimaSolution." + String.valueOf(projectCode)  ;
			
			
			if(activeLoggers.containsKey(newLoggerName))
			{
			  return activeLoggers.get(newLoggerName);
			}
			
			
			try {
				rollingFileAppender = new RollingFileAppender(layout, context.getRealPath( "/logs" + File.separator + newLoggerName + ".log"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (rollingFileAppender != null)
			{
				//Set the rotation properties for the client rolling file appender
				rollingFileAppender.setMaxBackupIndex(10);
				rollingFileAppender.setMaxFileSize("10MB");
				
				Logger clientlog = Logger.getLogger(newLoggerName);
				clientlog.addAppender(rollingFileAppender);
				
				//Add the client log to the activeLoggers map
				activeLoggers.put(newLoggerName, clientlog);
				clientlog.info("Project logger has been just initialized");
				return clientlog;

			}
			//Case when something wrong happens in the creation of the rollingFileAppender
			else
			{
				return null;
			}
		}

		
		public Logger getProjectOutput(String projectCode) {
			FileAppender fileAppender = null;
			
			String newLoggerName = "OptimaSolution." + String.valueOf(projectCode) + new Date().toString().replaceAll(":", "-")  ;
			
			
			try {
				fileAppender = new FileAppender(outputLayout, context.getRealPath( "/logs" + File.separator + newLoggerName + ".txt"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (fileAppender != null)
			{
				
				
				Logger outputLogger = Logger.getLogger(newLoggerName);
				outputLogger.addAppender(fileAppender);
				
				
				return outputLogger;

			}
			else
			{
				return null;
			}
		}
	
}
