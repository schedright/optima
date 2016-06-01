package com.softpoint.optima.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

public class PeriodLogGenerator {

	private static final String HTML_FILE = "<html><style>td{position:relative;} .stripedDiv{top:0;bottom:0;left:0;right:0;position:absolute;background-image: linear-gradient(to right top,transparent 33%,black 33%,black 66%,transparent 66%);background-size: 3px 3px;}</style><body><H1>Enterprise:%PORTFOLIO%</H1><H2>Project:%PROJECT%</H2><H2>%START% - %END%</H>2%ITERATIONS%</body></html>";
	private static final String ITERATION = "<H1>Iteration # %ITERATION_NUMBER%</H1><p>%PREVIOUS%</p><Table border=\"1\"><tr>%TASKS_DATES%</tr>%TASKS%</Table><p>%DETAILS%</p>";
	private static final String TRIAL = "<p>%PREVIOUS%</p><Table  border=\"1\"><tr>%TASKS_DATES%</tr>%TASKS%</Table><p>%DETAILS%</p>";
	private static final String DETAILS = "<Table><tr><td>Balance:</td><td>%Balance%</td></tr>"
			+ "<tr><td>Finance I:</td><td>%Finance I%</td></tr>"
			+ "<tr><td>Payment I:</td><td>%Payment I%</td></tr>"
			+ "<tr><td>Cash-out current:</td><td>%Cash-out current%</td></tr>"
			+ "<tr><td>Cost current:</td><td>%Cost current%</td></tr>"
			+ "<tr><td>Finance II:</td><td>%Finance II%</td></tr>"
			+ "<tr><td>Payment II:</td><td>%Payment II%</td></tr>"
			+ "<tr><td>Cash-out next:</td><td>%Cash-out next%</td></tr>"
			+ "<tr><td>Cash-Out other:</td><td>%Cash-Out other%</td></tr></table>";

	private String filePath;
	String fileContent;
	List<String> iterations;
	String currentPerionText;

	public PeriodLogGenerator(ServletContext context, String baseName, String startDate, String endDate) {
		super();
		int index = 1;
		String logPath = context.getRealPath("/logs");
		File folder = new File(logPath);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		while (true) {
			String fileName = "/logs/" + baseName + "_P" + index + ".html";
			filePath = context.getRealPath(fileName);
			File file = new File(filePath);
			if (!file.exists()) {
				break;
			}
			index++;
		}
		iterations = new ArrayList<String>();
		fileContent = HTML_FILE;
		fileContent = fileContent.replaceAll("%START%", startDate);
		fileContent = fileContent.replaceAll("%END%", endDate);
	}

	public void flushFile() {
		PrintWriter out = null;
		try {
			StringBuilder iters = new StringBuilder();
			for (String s : iterations) {
				iters.append(s);
			}
			fileContent = fileContent.replace("%ITERATIONS%", iters.toString());
			out = new PrintWriter(filePath);
			out.print(fileContent);
		} catch (FileNotFoundException e) {
		} finally {
			out.close();
		}
	}

	public void setProject(String portfolioName, String projectName) {
		fileContent = fileContent.replace("%PORTFOLIO%", portfolioName);
		fileContent = fileContent.replace("%PROJECT%", projectName);
	}

	String currentIteration = null;
	int maxIteration = -1;
	String previousTrials = "";

	public void startIteration(int iteration2, String shortVersion) {
		boolean newFix = iteration2 == maxIteration;
		currentIteration = newFix ? TRIAL : ITERATION.replace("%ITERATION_NUMBER%", String.valueOf(iteration2));
		if (!newFix) {
			previousTrials = "";
		}
		previousTrials += "\r<p>" + shortVersion + "</p>";
		currentIteration = currentIteration.replace("%PREVIOUS%", previousTrials);
		maxIteration = iteration2;
	}

	public void setIterationDates(String headerRow) {
		currentIteration = currentIteration.replace("%TASKS_DATES%", headerRow);
	}

	public void addIterationTask(String taskRow) {
		currentIteration = currentIteration.replace("%TASKS%", taskRow + "\r%TASKS%");
	}

	public void setDetails(double totalCostCurrent, double payment, double extraPaymentNextPeriod, double financeLimit, double financeLimitNextPeriod, double leftOverCost, double leftOverNextCost, double openBalance, double cashOutOthers) {
		String details = DETAILS;
		details = details.replace("%Cost current%", Double.toString(totalCostCurrent));
		details = details.replace("%Payment I%", Double.toString(payment));
		details = details.replace("%Payment II%", Double.toString(extraPaymentNextPeriod));
		details = details.replace("%Finance I%", Double.toString(financeLimit));
		details = details.replace("%Finance II%", Double.toString(financeLimitNextPeriod));
		details = details.replace("%Cash-out current%", Double.toString(leftOverCost));
		details = details.replace("%Cash-out next%", Double.toString(leftOverNextCost));
		details = details.replace("%Balance%", Double.toString(openBalance));
		details = details.replace("%Cash-Out other%", Double.toString(cashOutOthers));
		currentIteration = currentIteration.replace("%DETAILS%", details);
	}
	
	public void finishTask() {
		currentIteration = currentIteration.replace("%TASKS%", "");
		iterations.add(currentIteration);
		currentIteration = null;
		// previousTrials = "";
	}

}
