/**
 * PromniCAT - Collection and Analysis of Business Process Models
 * Copyright (C) 2012 Cindy Fähnrich, Tobias Hoppe, Andrina Mascher
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_potsdam.hpi.bpt.promnicat.analysisModules.metrics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jbpt.pm.ProcessModel;

import de.uni_potsdam.hpi.bpt.promnicat.parser.BpmnParser;
import de.uni_potsdam.hpi.bpt.promnicat.parser.EpcParser;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.DbFilterConfig;
import de.uni_potsdam.hpi.bpt.promnicat.util.Constants;
import de.uni_potsdam.hpi.bpt.promnicat.util.IllegalTypeException;
import de.uni_potsdam.hpi.bpt.promnicat.util.ProcessMetricConstants;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.IFlexibleUnitChainBuilder;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.IUnitChainBuilder;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.UnitChainBuilder;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.IUnitDataProcessMetrics;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.UnitDataProcessMetrics;

/**
 * Analysis module to calculate metrics from all {@link ProcessModel}s of a given database.
 * 
 * @author Tobias Hoppe
 * edited by Tobias Metzke
 *
 */
public class IndividualProcessMetrics {
	
	/**
	 * split element for CSV file values
	 */
	private static final String ITEMSEPARATOR = ";";	
	private static final String RESULT_FILE_PATH = new File("C:" + File.separator + "Users" + File.separator + "Tobi" + File.separator + "Documents" + File.separator + "EclipseWorkspaces" + File.separator + "SeminarProcessRepositories" + File.separator + "ProcessEvolutionAnalyzer" + File.separator + "resources" + File.separator + "old" + File.separator + "model_results_new.csv").getAbsolutePath();
	private static final Logger logger = Logger.getLogger(ProcessMetrics.class.getName());
	private static boolean useFullDB = true;
	
	/**
	 * @param args
	 * @throws IllegalTypeException 
	 * @throws IllegalArgumentException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IllegalArgumentException, IllegalTypeException, IOException {
		String analysisResult;
		
		IUnitChainBuilder chainBuilder = buildUpUnitChain(useFullDB);		
		logger.info(chainBuilder.getChain().toString() + "\n");		
		adjustLogger();

		//run chain
		@SuppressWarnings("unchecked")
		Collection<IUnitDataProcessMetrics<Object> > result = 
			(Collection<IUnitDataProcessMetrics<Object>>) chainBuilder.getChain().execute();
		
		writeResultToFile(result);
		analysisResult = analyzeResult(result);
		writeAnalysisToFile(analysisResult);
	}

	private static void writeAnalysisToFile(String analysisResult) {
		// TODO Auto-generated method stub
		
	}

	private static String analyzeResult(
			Collection<IUnitDataProcessMetrics<Object>> result) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * parser should not log parsing errors
	 */
	private static void adjustLogger() {
		Logger epcParserLog = Logger.getLogger(EpcParser.class.getName());
		epcParserLog.setLevel(Level.SEVERE);
		Logger bpmnParserLog = Logger.getLogger(BpmnParser.class.getName());
		bpmnParserLog.setLevel(Level.SEVERE);
	}

	/**
	 * Write the result into a CSV file
	 * @param resultSet the collected result of the chain execution
	 * @throws IOException if file can't be read or written
	 */
	private static void writeResultToFile(Collection<IUnitDataProcessMetrics<Object>> resultSet) throws IOException {
		BufferedWriter writer = null;
		writer = new BufferedWriter(new FileWriter(RESULT_FILE_PATH));
		StringBuilder resultStringBuilder = new StringBuilder(addHeader());
		// collect result from each model
		for (IUnitDataProcessMetrics<Object> resultItem : resultSet)
			resultStringBuilder.append(toCsv(resultItem));

		writer.write(resultStringBuilder.toString());
		writer.close();
	}

	private static Object toCsv(IUnitDataProcessMetrics<Object> resultItem) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return a String representation of the process model metrics
	 * separated by the {@link ProcessMetrics#ITEMSEPARATOR}.
	 */
	private static String addHeader() {
		StringBuilder builder = new StringBuilder();
		builder.append("Process Model Path" + ITEMSEPARATOR);
		builder.append("Revision" + ITEMSEPARATOR);
//		builder.append("DB ID" + ITEMSEPARATOR);
//		builder.append("Number of Start Events" + ITEMSEPARATOR);
//		builder.append("Number of Internal Events" + ITEMSEPARATOR);
//		builder.append("Number of End Events" + ITEMSEPARATOR);
		builder.append("Number of Events" + ITEMSEPARATOR);
		builder.append("Number of Activities" + ITEMSEPARATOR);
//		builder.append("Number of And-Splits" + ITEMSEPARATOR);
//		builder.append("Number of And-Joins" + ITEMSEPARATOR);
//		builder.append("Number of Xor-Splits" + ITEMSEPARATOR);
//		builder.append("Number of Xor-Joins" + ITEMSEPARATOR);
//		builder.append("Number of Or-Splits" + ITEMSEPARATOR);
//		builder.append("Number of Or-Joins" + ITEMSEPARATOR);
		builder.append("Number of Gateways" + ITEMSEPARATOR);
		builder.append("Number of Nodes" + ITEMSEPARATOR);
		builder.append("Number of Edges" + ITEMSEPARATOR);
//		builder.append("Number of Data Nodes" + ITEMSEPARATOR);
		//next line added, must be deleted when line after that line shall be reconsidered
		builder.append("Number of Roles");
//		builder.append("Number of Roles" + ITEMSEPARATOR);
//		builder.append("Diameter" + ITEMSEPARATOR);
//		builder.append("Density" + ITEMSEPARATOR);
//		builder.append("Density related to number of Gateways" + ITEMSEPARATOR);
//		builder.append("Coefficient of Connectivity" + ITEMSEPARATOR);
//		builder.append("Coefficient of Network Complexity" + ITEMSEPARATOR);
//		builder.append("Cyclomatic Number" + ITEMSEPARATOR);
//		builder.append("Average Connector Degree" + ITEMSEPARATOR);
//		builder.append("Maximum Connector Degree" + ITEMSEPARATOR);
//		builder.append("Separability" + ITEMSEPARATOR);
//		builder.append("Depth" + ITEMSEPARATOR);
//		builder.append("Cycling" + ITEMSEPARATOR);
//		builder.append("Controlflow Complexity" + ITEMSEPARATOR);
//		builder.append("Cross Connectivity");
		builder.append("\n");
		return builder.toString();
	}

	/**
	 * Create an new unit chain builder and builds up
	 * a chain to get the metrics of the {@link ProcessModel}s from the given database.
	 * @param useFullDB use full data set or if set to <code>false</code> only a small excerpt
	 * @param metric 
	 * @return the builder with the created chain
	 * @throws IOException if the given configuration file path could not be found
	 * @throws IllegalTypeException if the units of the chain have incompatible input/output types
	 */
	private static IUnitChainBuilder buildUpUnitChain(boolean useFullDB) throws IOException, IllegalTypeException {
		IFlexibleUnitChainBuilder chainBuilder = null;
		Collection<ProcessMetricConstants.METRICS> metricsToCalculate = defineProcessModelMetrics();
		if (useFullDB){
			chainBuilder = new UnitChainBuilder("configuration(full).properties", Constants.DATABASE_TYPES.ORIENT_DB, UnitDataProcessMetrics.class);
		} else {
			chainBuilder = new UnitChainBuilder("", Constants.DATABASE_TYPES.ORIENT_DB, UnitDataProcessMetrics.class);
		}
		//build db query
		DbFilterConfig dbFilter = new DbFilterConfig();
		dbFilter.addOrigin(Constants.ORIGINS.BPMAI);		
		dbFilter.addFormat(Constants.FORMATS.BPMAI_JSON);
		dbFilter.addNotation(Constants.NOTATIONS.BPMN1_1);
		dbFilter.addNotation(Constants.NOTATIONS.BPMN2_0);
//		dbFilter.addNotation(Constants.NOTATIONS.EPC);
//		dbFilter.setLatestRevisionsOnly(true);
		chainBuilder.addDbFilterConfig(dbFilter);
		//transform to jBPT and calculate metrics
		chainBuilder.createBpmaiJsonToJbptUnit(false);
		chainBuilder.createProcessModelMetricsCalulatorUnit(metricsToCalculate,true);
		
		//collect results
		chainBuilder.createSimpleCollectorUnit();
		return chainBuilder;
	}

	
	private static Collection<ProcessMetricConstants.METRICS> defineProcessModelMetrics() {
		Collection<ProcessMetricConstants.METRICS> metrics = new ArrayList<ProcessMetricConstants.METRICS>();
		Collections.addAll(metrics, 
				ProcessMetricConstants.METRICS.NUM_EVENTS,
				ProcessMetricConstants.METRICS.NUM_ACTIVITIES,
				ProcessMetricConstants.METRICS.NUM_GATEWAYS,
				ProcessMetricConstants.METRICS.NUM_NODES,
				ProcessMetricConstants.METRICS.NUM_EDGES,
				ProcessMetricConstants.METRICS.NUM_ROLES
				);
		return metrics;
	}

}
