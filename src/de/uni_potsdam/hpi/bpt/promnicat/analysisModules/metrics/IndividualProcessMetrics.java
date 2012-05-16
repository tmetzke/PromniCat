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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
	
	private static final String RESULT_FILE_PATH = 
			new File("").getAbsolutePath() + "/resources/analysis/model_results_new.csv";
	
	private static final String ANALYSIS_RESULT_FILE_PATH = 
			new File("").getAbsolutePath() + "/resources/analysis/model_results_analyzed_new.csv";
	
	private static final Logger logger = Logger.getLogger(ProcessMetrics.class.getName());
	private static boolean useFullDB = false;
	
	private static final Collection<ProcessMetricConstants.METRICS> METRICS = new ArrayList<ProcessMetricConstants.METRICS>();
	
	private static final Map<String,Map<Integer, Map<String, Double>>> models = 
			new HashMap<String,Map<Integer,Map<String, Double>>>();
	
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
		// parser should not log parsing errors
		Logger epcParserLog = Logger.getLogger(EpcParser.class.getName());
		epcParserLog.setLevel(Level.SEVERE);
		Logger bpmnParserLog = Logger.getLogger(BpmnParser.class.getName());
		bpmnParserLog.setLevel(Level.SEVERE);

		//run chain
		@SuppressWarnings("unchecked")
		Collection<IUnitDataProcessMetrics<Object> > result = 
			(Collection<IUnitDataProcessMetrics<Object>>) chainBuilder.getChain().execute();
		
		buildUpInternalDataStructure(result);

		writeResultToFile(result);
		logger.info("Wrote results to " + RESULT_FILE_PATH);
//		analysisResult = analyzeResult(result);
//		writeAnalysisToFile(analysisResult);
//		logger.info("Wrote results to " + ANALYSIS_RESULT_FILE_PATH);
	}
	
	private static void buildUpInternalDataStructure(Collection<IUnitDataProcessMetrics<Object>> resultSet) {		
		Collection<String> metricsCollection = new ArrayList<String>();
		for (ProcessMetricConstants.METRICS metric : METRICS)
			metricsCollection.add(metric.name());
		
		for (IUnitDataProcessMetrics<Object> resultItem : resultSet){
			String modelPathWithRevision = resultItem.getModelPath();
			int revisionStringIndex = modelPathWithRevision.indexOf("_rev");
			String processFolder = "2011-04-19_signavio_academic_processes";
			String modelPath = modelPathWithRevision.substring(modelPathWithRevision.indexOf(processFolder) + processFolder.length(),revisionStringIndex);
			int revisionNumber = Integer.valueOf(modelPathWithRevision.substring(revisionStringIndex + 4, modelPathWithRevision.indexOf(".json")));
			
			// new model to be analyzed, so add it to the map of models
			if (!models.containsKey(modelPath)) {
				Map<Integer, Map<String, Double>> revisions = new HashMap<Integer, Map<String, Double>>();
				models.put(modelPath, revisions);
			}
			
			Map<String, Double> revisionValues = new HashMap<String, Double>();
			for (String metric : metricsCollection)
				revisionValues.put(metric, ProcessMetricConstants.METRICS.valueOf(metric).getAttribute(resultItem));
			models.get(modelPath).put(revisionNumber, revisionValues);
		}
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
		for (Entry<String, Map<Integer, Map<String, Double>>> model : models.entrySet())
			resultStringBuilder.append(toCsvString(model));

		writer.write(resultStringBuilder.toString());
		writer.close();
	}

	private static void writeAnalysisToFile(String analysisResult) throws IOException {
		BufferedWriter writer = null;
		writer = new BufferedWriter(new FileWriter(ANALYSIS_RESULT_FILE_PATH));
		StringBuilder resultStringBuilder = new StringBuilder(addHeader()).append(analysisResult);
		writer.write(resultStringBuilder.toString());
		writer.close();
	}

	private static String analyzeResult(Collection<IUnitDataProcessMetrics<Object>> resultSet) {
		StringBuilder resultStringBuilder = new StringBuilder();
//		for (String metric : metricsCollection){
//			double actualValue = ProcessMetricConstants.METRICS.valueOf(metric).getAttribute(resultItem);
//			double oldValue = oldValues.get(modelPath).get(metric);
//			double divisor = oldValue == 0 ? actualValue : oldValue;
//			if (divisor == 0) divisor = 1;
//			double difference = (actualValue - oldValue) * 100 / divisor;
//			
//			models.get(modelPath).put(metric, actualValue);
//			resultStringBuilder
//				.append(new String(ITEMSEPARATOR + difference).replace(".", ","));
//		}
		resultStringBuilder.append("\n");
		return resultStringBuilder.toString();
	}

	private static String toCsvString(Entry<String, Map<Integer, Map<String, Double>>> model) {
		StringBuilder builder = new StringBuilder();
		String modelPath = model.getKey();
		for (Entry<Integer, Map<String, Double>> revision : model.getValue().entrySet()) {
			int revisionNumber = revision.getKey();
			Map<String, Double> metricsValues = revision.getValue();
			builder.append(modelPath);
			builder.append(ITEMSEPARATOR + revisionNumber);
			for (ProcessMetricConstants.METRICS metric : METRICS)
				builder.append(ITEMSEPARATOR + (metricsValues.get(metric.name())).intValue());
			builder.append("\n");
		}
		return builder.toString();
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
		builder.append("Number of Roles" + ITEMSEPARATOR);
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
		defineProcessModelMetrics();
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
		chainBuilder.createProcessModelMetricsCalulatorUnit(METRICS,true);
		
		//collect results
		chainBuilder.createSimpleCollectorUnit();
		return chainBuilder;
	}

	private static void defineProcessModelMetrics() {
		Collections.addAll(METRICS, 
				ProcessMetricConstants.METRICS.NUM_EVENTS,
				ProcessMetricConstants.METRICS.NUM_ACTIVITIES,
				ProcessMetricConstants.METRICS.NUM_GATEWAYS,
				ProcessMetricConstants.METRICS.NUM_NODES,
				ProcessMetricConstants.METRICS.NUM_EDGES,
				ProcessMetricConstants.METRICS.NUM_ROLES);
	}

}
