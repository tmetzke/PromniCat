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
import de.uni_potsdam.hpi.bpt.promnicat.util.ProcessMetricConstants.METRICS;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.IFlexibleUnitChainBuilder;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.IUnitChainBuilder;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.UnitChainBuilder;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.IUnitDataProcessMetrics;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.UnitDataProcessMetrics;

/**
 * Analysis module to calculate metrics from all {@link ProcessModel}s of a given database.
 * 
 * @author Tobias Metzke
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
	private static final boolean useFullDB = false;
	
	private static Collection<METRICS> processModelMetrics;
	
	/**
	 * @param args
	 * @throws IllegalTypeException 
	 * @throws IllegalArgumentException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IllegalArgumentException, IllegalTypeException, IOException {

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
		
		Map<String,Map<Integer, Map<String, Double>>> models = buildUpInternalDataStructure(result);

		writeToFile(models);
		logger.info("Wrote results to " + RESULT_FILE_PATH);
		
//		Map<String,Map<Integer, Map<String, Double>>> analyzedModels = analyze(models);
//		writeToFile(analyzedModels);
//		logger.info("Wrote analysis results to " + ANALYSIS_RESULT_FILE_PATH);
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
		chainBuilder.createProcessModelMetricsCalulatorUnit(getProcessModelMetrics(),true);
		
		//collect results
		chainBuilder.createSimpleCollectorUnit();
		return chainBuilder;
	}

	private static Collection<METRICS> getProcessModelMetrics() {
		if (processModelMetrics == null) {
			processModelMetrics = new ArrayList<>();
			Collections.addAll(processModelMetrics, 
				METRICS.NUM_EVENTS,
				METRICS.NUM_ACTIVITIES,
				METRICS.NUM_GATEWAYS,
				METRICS.NUM_NODES,
				METRICS.NUM_EDGES,
				METRICS.NUM_ROLES);
		}
		return processModelMetrics;
	}
	
	private static Map<String, Map<Integer, Map<String, Double>>> buildUpInternalDataStructure(
			Collection<IUnitDataProcessMetrics<Object>> resultSet) {		
		Collection<String> metricsCollection = new ArrayList<>();
		Map<String,Map<Integer, Map<String, Double>>> models = new HashMap<>();
		for (METRICS metric : getProcessModelMetrics())
			metricsCollection.add(metric.name());
		
		for (IUnitDataProcessMetrics<Object> resultItem : resultSet){
			String modelPathWithRevision = resultItem.getModelPath();
			int revisionStringIndex = modelPathWithRevision.indexOf("_rev");
			String processFolder = "2011-04-19_signavio_academic_processes";
			String modelPath = modelPathWithRevision.substring(modelPathWithRevision.indexOf(processFolder) + processFolder.length(),revisionStringIndex);
			int revisionNumber = Integer.valueOf(modelPathWithRevision.substring(revisionStringIndex + 4, modelPathWithRevision.indexOf(".json")));
			
			// new model to be analyzed, so add it to the map of models
			if (!models.containsKey(modelPath)) {
				Map<Integer, Map<String, Double>> revisions = new HashMap<>();
				models.put(modelPath, revisions);
			}
			
			Map<String, Double> revisionValues = new HashMap<>();
			for (String metric : metricsCollection)
				revisionValues.put(metric, METRICS.valueOf(metric).getAttribute(resultItem));
			models.get(modelPath).put(revisionNumber, revisionValues);
		}
		
		return models;
	}
	
	/**
	 * Write the result into a CSV file
	 * @param resultSet the collected result of the chain execution
	 * @throws IOException if file can't be read or written
	 */
	private static void writeToFile(Map<String,Map<Integer, Map<String, Double>>> models) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(RESULT_FILE_PATH));
		StringBuilder resultStringBuilder = new StringBuilder(addHeader());
		// collect result from each model
		for (Entry<String, Map<Integer, Map<String, Double>>> model : models.entrySet())
			resultStringBuilder.append(toCsvString(model));
		writer.write(resultStringBuilder.toString());
		writer.close();
	}

	/**
	 * @return a String representation of the process model metrics
	 * separated by the {@link ProcessMetrics#ITEMSEPARATOR}.
	 */
	private static String addHeader() {
		return new StringBuilder()
			.append("Process Model" + ITEMSEPARATOR)
			.append("Revision" + ITEMSEPARATOR)
			.append("Number of Events" + ITEMSEPARATOR)
			.append("Number of Activities" + ITEMSEPARATOR)
			.append("Number of Gateways" + ITEMSEPARATOR)
			.append("Number of Nodes" + ITEMSEPARATOR)
			.append("Number of Edges" + ITEMSEPARATOR)
			.append("Number of Roles" + ITEMSEPARATOR)
			.append("\n")
			.toString();
	}
	
	private static String toCsvString(Entry<String, Map<Integer, Map<String, Double>>> model) {
		StringBuilder builder = new StringBuilder();
		String modelPath = model.getKey();
		for (Entry<Integer, Map<String, Double>> revision : model.getValue().entrySet()) {
			int revisionNumber = revision.getKey();
			Map<String, Double> metricsValues = revision.getValue();
			builder.append(modelPath);
			builder.append(ITEMSEPARATOR + revisionNumber);
			for (METRICS metric : getProcessModelMetrics())
				builder.append(ITEMSEPARATOR + (metricsValues.get(metric.name())).intValue());
			builder.append("\n");
		}
		return builder.toString();
	}

	private static Map<String,Map<Integer, Map<String, Double>>> analyze(Map<String,Map<Integer, Map<String, Double>>> models) {
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
		return null;
	}
}
