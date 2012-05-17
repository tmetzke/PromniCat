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
import java.util.List;
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
 * Furthermore the metrics analysis is analyzed in further steps to gain more high-level
 * insights into the given model collection.
 * 
 * @author Tobias Metzke
 *
 */
public class IndividualProcessMetrics {
	
	/**
	 * split element for CSV file values
	 */
	private static final String ITEMSEPARATOR = ";";	
	
	/**
	 * path of the model metrics result file
	 */
	private static final String MODEL_RESULT_FILE_PATH = 
			new File("").getAbsolutePath() + "/resources/analysis/new_model_results.csv";
	
	/**
	 * path of the metrics analysis result file, that analyzes the model metrics results
	 */
	private static final String METRICS_ANALYSIS_ABSOLUTE_RESULT_FILE_PATH = 
			new File("").getAbsolutePath() + "/resources/analysis/new_model_results_absolute_analyzed.csv";
	
	/**
	 * path of the metrics analysis result file, that analyzes the model metrics results
	 */
	private static final String METRICS_ANALYSIS_RELATIVE_RESULT_FILE_PATH = 
			new File("").getAbsolutePath() + "/resources/analysis/new_model_results_relative_analyzed.csv";
	
	/**
	 * path of the metrics analysis analysis result file, that analyzes the metrics analysis
	 */
	private static final String ANALYSIS_ANALYSIS_RESULT_FILE_PATH = 
			new File("").getAbsolutePath() + "/resources/analysis/new_analysis_results_analyzed.csv";
	
	private static final Logger logger = Logger.getLogger(ProcessMetrics.class.getName());
	
	/**
	 * flag to decide whether to use the full database or just a small test subset
	 */
	private static final boolean useFullDB = true;
	
	/**
	 * the collection of metrics all model revisions will be analyzed by
	 */
	private static Collection<METRICS> processModelMetrics;
	
	/**
	 * the key of the continuous growth indicator of a model
	 */
	private static final String GROWS_VALUE_KEY = "grows";

	/**
	 * the key for the number of models that are analyzed
	 */
	private static final String NUM_MODELS = "number of models";

	/**
	 * the key for the number of models that grow continuously
	 */
	private static final String NUM_GROWING = "continuously growing";

	/**
	 * the key for the number of models that do not grow continuously
	 */
	private static final String NUM_NOT_GROWING = "not always growing";
	
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

		writeToFile(MODEL_RESULT_FILE_PATH, models);
		logger.info("Wrote model metrics results to " + MODEL_RESULT_FILE_PATH + "\n");
		
		Map<String,Map<Integer, Map<String, Double>>> analyzedModels = analyzeMetrics(models, true);
		writeToFile(METRICS_ANALYSIS_RELATIVE_RESULT_FILE_PATH, analyzedModels);
		logger.info("Wrote relative metrics analysis results to " + METRICS_ANALYSIS_RELATIVE_RESULT_FILE_PATH + "\n");
		
		analyzedModels = analyzeMetrics(models, false);
		writeToFile(METRICS_ANALYSIS_ABSOLUTE_RESULT_FILE_PATH, analyzedModels);
		logger.info("Wrote absolute metrics analysis results to " + METRICS_ANALYSIS_ABSOLUTE_RESULT_FILE_PATH + "\n");
		
		highLevelAnalysis(analyzedModels);
		logger.info("Wrote analysis of metrics analysis to " + ANALYSIS_ANALYSIS_RESULT_FILE_PATH);
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

	/**
	 * access to the herein defined metrics that are analyzed per model revision
	 * and displayed in analysis results
	 * @return the metrics all model revisions are analyzed by
	 */
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
	
	/**
	 * puts in place the internal data structure where:
	 * 1. each model holds all its revisions
	 * 2. each revision holds the values of all metrics of this revision
	 * 
	 * @param resultSet the collection of results of the metric analysis
	 * @return the internal data structure representation
	 */
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
	private static void writeToFile(String filePath, Map<String,Map<Integer, Map<String, Double>>> models) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
		StringBuilder resultStringBuilder = new StringBuilder(addHeader());
		// collect result from each model
		for (Entry<String, Map<Integer, Map<String, Double>>> model : models.entrySet())
			resultStringBuilder.append(toCsvString(model));
		writer.write(resultStringBuilder.toString());
		writer.close();
	}

	/**
	 * add the specific header to the analysis files that includes
	 * the model path, revision and the metrics of this revision
	 * @return a String representation of the process model metrics
	 * separated by the {@link ProcessMetrics#ITEMSEPARATOR}.
	 */
	private static String addHeader() {
		StringBuilder builder = new StringBuilder()
			.append("Process Model" + ITEMSEPARATOR)
			.append("Revision" + ITEMSEPARATOR);
		for (METRICS metric : getProcessModelMetrics())
			builder.append(metric.name() + ITEMSEPARATOR);
		builder
			.append("grows?")
			.append("\n");
		return builder.toString();
	}
	
	/**
	 * helper method that puts the real data of the metrics into format.
	 * every model revision is put into a new line with the according metrics
	 * @param model that should be displayed in CSV format
	 * @return a String representation of the formatted model results
	 */
	private static String toCsvString(Entry<String, Map<Integer, Map<String, Double>>> model) {
		String modelPath = model.getKey();
		Map<Integer, Map<String, Double>> modelRevisions = model.getValue();
		List<Integer> revisionNumbers = new ArrayList<>(modelRevisions.keySet());
		Collections.sort(revisionNumbers);
		
		// collect all information from the revisions
		// display each revision in a separate line
		StringBuilder builder = new StringBuilder();
		for (Integer revisionNumber : revisionNumbers) {
			Map<String, Double> revisionValues = modelRevisions.get(revisionNumber);
			builder.append(modelPath);
			builder.append(ITEMSEPARATOR + revisionNumber);
			for (METRICS metric : getProcessModelMetrics())
				builder.append(ITEMSEPARATOR + (revisionValues.get(metric.name())).intValue());
			// if the value of continuous growth is set in this revision, display it
			if (revisionValues.containsKey(GROWS_VALUE_KEY)) 
				builder.append(ITEMSEPARATOR + revisionValues.get(GROWS_VALUE_KEY).intValue());
			builder.append("\n");
		}
		return builder.toString();
	}

	/**
	 * difference analysis of the metrics analysis.
	 * per model revision the difference to the previous revision
	 * is stored for all metrics
	 * @param models the models to be analyzed further
	 * @param relative flag to determine whether the values shall be relative
	 * to the absolute old number (<code>true</code>) or absolute (<code>false</code>)
	 * @return the analyzed models, their revisions and their values
	 */
	private static Map<String,Map<Integer, Map<String, Double>>> analyzeMetrics(Map<String,Map<Integer, Map<String, Double>>> models, boolean relative) {
		// a new data structure to store the results in
		Map<String,Map<Integer, Map<String, Double>>> newModels = new HashMap<>();
		
		for (Entry<String,Map<Integer, Map<String, Double>>> model : models.entrySet()) {
			Map<String, Double> oldValues = getInitialValues();
			List<Integer> revisionNumbers = new ArrayList<>(model.getValue().keySet());
			Collections.sort(revisionNumbers);
			boolean growsContinuously = true;
			
			// perform the analysis of differences for every revision and metric
			for (Integer revisionNumber : revisionNumbers) {
				Map<String, Double> revisionValues = model.getValue().get(revisionNumber);
				for (METRICS metric : getProcessModelMetrics()) {
					double actualValue = revisionValues.get(metric.name());
					double oldValue = oldValues.get(metric.name());
					double divisor = 0;
					int factor = 100;
					if (relative) {
						divisor = oldValue == 0 ? actualValue : oldValue;
						if (divisor == 0) divisor = 1;
					} else {
						divisor = 1;
						factor = 1;
					}
					double difference = (actualValue - oldValue) * factor / divisor;
					oldValues.put(metric.name(),actualValue);
					
					updateResults(newModels, model, revisionNumber, metric,
							difference);
					// if the metric is lower than previously the model is not continuously growing
					if (difference < 0) growsContinuously = false;
				}
			}
			double growsAsDouble = new Double(growsContinuously ? 1 : 0);
			// only put the information of continuous growth into the latest revision
			newModels.get(model.getKey()).get(Collections.max(revisionNumbers)).put(GROWS_VALUE_KEY, growsAsDouble);
		}
		return newModels;
	}

	/**
	 * @param newModels
	 * @param model
	 * @param revisionNumber
	 * @param metric
	 * @param difference
	 */
	private static void updateResults(
			Map<String, Map<Integer, Map<String, Double>>> newModels,
			Entry<String, Map<Integer, Map<String, Double>>> model,
			Integer revisionNumber, METRICS metric, double difference) {
		
		if(!newModels.containsKey(model.getKey()))
			newModels.put(model.getKey(), new HashMap<Integer,Map<String,Double>>());
		if(!newModels.get(model.getKey()).containsKey(revisionNumber))
			newModels.get(model.getKey()).put(revisionNumber, new HashMap<String, Double>());
		newModels.get(model.getKey()).get(revisionNumber).put(metric.name(), difference);
	}
	
	/**
	 * initialize a first collection of metrics zero-values to have a starting
	 * point for the first revision of a model to be compared to
	 * @return
	 */
	private static Map<String, Double> getInitialValues() {
		Map<String, Double> oldValues = new HashMap<>();
		for (METRICS metric : getProcessModelMetrics())
			oldValues.put(metric.name(), new Double(0));
		return oldValues;
	}

	/**
	 * further analyze the already analyzed models and try to find high-level
	 * results like the number of continuously growing models
	 * @param analyzedModels
	 * @throws IOException
	 */
	private static void highLevelAnalysis(Map<String, Map<Integer, Map<String, Double>>> analyzedModels) throws IOException {
		Map<String, Integer> features = new HashMap<>();
		int numberOfModels = analyzedModels.size();
		int growingModels = 0;
		for (Map<Integer, Map<String, Double>> model : analyzedModels.values()) {
			int maxRevision = Collections.max(model.keySet());
			Double grows = model.get(maxRevision).get(GROWS_VALUE_KEY);
			if (grows.equals(new Double(1))) growingModels++;
		}
		features.put(NUM_MODELS, numberOfModels);
		features.put(NUM_GROWING, growingModels);
		features.put(NUM_NOT_GROWING, numberOfModels - growingModels);
		
		writeAnalysisWith(features);
	}

	/**
	 * write the analysis into a new file
	 * @param features
	 * @throws IOException
	 */
	private static void writeAnalysisWith(Map<String, Integer> features)
			throws IOException {
		String resultString = new StringBuilder()
		.append("number of models" + ITEMSEPARATOR)
		.append("growing models" + ITEMSEPARATOR)
		.append("not growing models")
		.append("\n")
		.append(features.get(NUM_MODELS) + ITEMSEPARATOR)
		.append(features.get(NUM_GROWING) + ITEMSEPARATOR)
		.append(features.get(NUM_NOT_GROWING))
		.toString();
	
		BufferedWriter writer = new BufferedWriter(new FileWriter(ANALYSIS_ANALYSIS_RESULT_FILE_PATH));
		writer.write(resultString);
		writer.close();
	}
	
}
