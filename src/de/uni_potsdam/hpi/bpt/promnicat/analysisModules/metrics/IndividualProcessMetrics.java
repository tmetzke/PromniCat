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
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jbpt.pm.ProcessModel;

import de.uni_potsdam.hpi.bpt.promnicat.parser.BpmnParser;
import de.uni_potsdam.hpi.bpt.promnicat.parser.EpcParser;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.DbFilterConfig;
import de.uni_potsdam.hpi.bpt.promnicat.util.Constants;
import de.uni_potsdam.hpi.bpt.promnicat.util.IllegalTypeException;
import de.uni_potsdam.hpi.bpt.promnicat.util.ProcessMetricConstants.METRICS;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisModelRevision;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisProcessModel;
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
	
	private static final String UNALTERING_REVISIONS = "unaltering Revisions";

	private static final String ALTERING_REVISIONS = "altering revisions";

	private static final String NUM_REVISIONS = "number of revisions";

	private static final String LOWER = "lower";

	private static final String SAME = "same";

	private static final String HIGHER = "higher";

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
	private static final boolean useFullDB = false;
	
	/**
	 * the collection of metrics all model revisions will be analyzed by
	 */
	private static Collection<METRICS> processModelMetrics;
	
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
		
		Map<String,AnalysisProcessModel> models = buildUpInternalDataStructure(result);

		writeToFile(MODEL_RESULT_FILE_PATH, models);
		logger.info("Wrote model metrics results to " + MODEL_RESULT_FILE_PATH + "\n");
		
		Map<String, AnalysisProcessModel> analyzedModels = analyzeMetrics(models, true);
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
	private static Map<String, AnalysisProcessModel> buildUpInternalDataStructure(
			Collection<IUnitDataProcessMetrics<Object>> resultSet) {
		
		Map<String, AnalysisProcessModel> models = new HashMap<>();
		
		for (IUnitDataProcessMetrics<Object> resultItem : resultSet){
			String modelPathWithRevision = resultItem.getModelPath();
			int revisionStringIndex = modelPathWithRevision.indexOf("_rev");
			String processFolder = "2011-04-19_signavio_academic_processes";
			String modelPath = modelPathWithRevision.substring(
					modelPathWithRevision.indexOf(processFolder) + processFolder.length(),revisionStringIndex);
			int revisionNumber = Integer.valueOf(
					modelPathWithRevision.substring(revisionStringIndex + 4, modelPathWithRevision.indexOf(".json")));
			
			// new model to be analyzed, so add it to the map of models
			if (!models.containsKey(modelPath)) {
				AnalysisProcessModel model = new AnalysisProcessModel(modelPath);
				models.put(model.getName(), model);
			}
			
			AnalysisModelRevision revision = new AnalysisModelRevision(revisionNumber);
			for (METRICS metric : getProcessModelMetrics())
				revision.add(metric, metric.getAttribute(resultItem));
			models.get(modelPath).add(revision);
		}
		
		return models;
	}
	
	/**
	 * Write the result into a CSV file
	 * @param resultSet the collected result of the chain execution
	 * @throws IOException if file can't be read or written
	 */
	private static void writeToFile(String filePath, Map<String, AnalysisProcessModel> models) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
		StringBuilder resultStringBuilder = new StringBuilder(addHeader());
		// collect result from each model
		for (AnalysisProcessModel model : models.values())
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
		builder.append("grows continuously?");
		return builder.toString();
	}
	
	/**
	 * helper method that puts the real data of the metrics into format.
	 * every model revision is put into a new line with the according metrics
	 * @param model that should be displayed in CSV format
	 * @return a String representation of the formatted model results
	 */
	private static String toCsvString(AnalysisProcessModel model) {
		SortedMap<Integer, AnalysisModelRevision> revisions = model.getRevisions();
		// collect all information from the revisions
		// display each revision in a separate line
		StringBuilder builder = new StringBuilder();
		for (AnalysisModelRevision revision : revisions.values()) {
			builder
				.append("\n")
				.append(model.getName())
				.append(ITEMSEPARATOR + revision.getRevisionNumber());
			for (METRICS metric : getProcessModelMetrics())
				builder.append(ITEMSEPARATOR + revision.get(metric).intValue());
		}
		builder.append(ITEMSEPARATOR + model.isGrowing());
		return builder.toString();
	}

	/**
	 * analysis of the analyzed models.
	 * @param models the models to be analyzed further
	 * @param relative flag to determine whether the values shall be relative
	 * to the absolute old number (<code>true</code>) or absolute (<code>false</code>)
	 * @return the analyzed models, their revisions and their values
	 */
	private static Map<String,AnalysisProcessModel> analyzeMetrics(Map<String, AnalysisProcessModel> models, boolean relative) {
		// a new data structure to store the results in
		Map<String,AnalysisProcessModel> newModels = new HashMap<>();
		
		for (AnalysisProcessModel model : models.values()) {
			AnalysisProcessModel newModel = performDifferenceAnalysisFor(model, relative);
			newModels.put(model.getName(), newModel);
		}
		return newModels;
	}

	/**
	 * per model revision the difference to the previous revision
	 * is stored for all metrics
	 * @param relative flag to determine whether the values shall be relative
	 * @param model the model to be analyzed
	 * @return the analyzed model
	 */
	private static AnalysisProcessModel performDifferenceAnalysisFor(
			AnalysisProcessModel model, boolean relative) {
		
		AnalysisProcessModel newModel = new AnalysisProcessModel(model.getName());
		Map<METRICS, Double> oldValues = getInitialValues();
		// perform the analysis of differences for every revision and metric
		for (AnalysisModelRevision revision : model.getRevisions().values()) {
			AnalysisModelRevision newRevision = new AnalysisModelRevision(revision.getRevisionNumber());
			for (METRICS metric : getProcessModelMetrics()) {
				double actualValue = revision.get(metric);
				double oldValue = oldValues.get(metric);
				double difference = calculateDifference(metric, actualValue, oldValue, relative);
				// save the new value as back-reference for the next revision
				oldValues.put(metric,actualValue);
				newRevision.add(metric, difference);
				// if a metric is actually lower than in the previous revision,
				// the model is not growing continuously
				if (difference < 0) newModel.setGrowing(false);
			}
			newModel.add(newRevision);
		}
		return newModel;
	}

	/**
	 * initialize a first collection of metrics zero-values to have a starting
	 * point for the first revision of a model to be compared to
	 * @return
	 */
	private static Map<METRICS, Double> getInitialValues() {
		Map<METRICS, Double> oldValues = new HashMap<>();
		for (METRICS metric : getProcessModelMetrics())
			oldValues.put(metric, new Double(0));
		return oldValues;
	}

	/**
	 * execution of the difference analysis
	 * @param metric the metric to be analyzed
	 * @param revision the actual revision containing its metric values
	 * @param oldValues the previous set of values
	 * @param relative
	 * @return
	 */
	private static double calculateDifference(METRICS metric,	double actualValue, double oldValue, boolean relative) {
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
		return difference;
	}
	
	/**
	 * further analyze the already analyzed models and try to find high-level
	 * results like the number of continuously growing models
	 * @param analyzedModels
	 * @throws IOException
	 */
	private static void highLevelAnalysis(Map<String, AnalysisProcessModel> analyzedModels) throws IOException {
		// continuously growing models
		Map<String, Integer> features = new HashMap<>();
		int numberOfModels = analyzedModels.size();
		int growingModels = 0;
		for (AnalysisProcessModel model : analyzedModels.values())
			if (model.isGrowing()) growingModels++;
		features.put(NUM_MODELS, numberOfModels);
		features.put(NUM_GROWING, growingModels);
		features.put(NUM_NOT_GROWING, numberOfModels - growingModels);
		
		// number of revisions that higher, lower and do not change the numbers of a metric
		for (METRICS metric : getProcessModelMetrics()) {
			int higher = 0;
			int lower =  0;
			int same = 0;
			for (AnalysisProcessModel model : analyzedModels.values())
				for (AnalysisModelRevision revision : model.getRevisions().values()) {
					double actualValue = revision.get(metric);
					if (actualValue < 0) lower++;
					else if (actualValue == new Double(0)) same++;
					else higher++;
				}
			features.put(metric.name() + HIGHER, higher);
			features.put(metric.name() + SAME, same);
			features.put(metric.name() + LOWER, lower);
		}
		
		// number of revisions that don't alter the number of any metric
		int alteringRevisions = 0;
		int numberOfRevisions = 0;
		for (AnalysisProcessModel model : analyzedModels.values()) {
			numberOfRevisions += model.getRevisions().size();
			for (AnalysisModelRevision revision : model.getRevisions().values())
				for (METRICS metric : getProcessModelMetrics()) 
					if (!revision.get(metric).equals(new Double(0))) {
						alteringRevisions++;
						break;
					}
		}
					
		features.put(NUM_REVISIONS, numberOfRevisions);
		features.put(ALTERING_REVISIONS, alteringRevisions);
		features.put(UNALTERING_REVISIONS, numberOfRevisions - alteringRevisions);
		
		writeAnalysisWith(features);
	}

	/**
	 * write the analysis into a new file
	 * @param features
	 * @throws IOException
	 */
	private static void writeAnalysisWith(Map<String, Integer> features)
			throws IOException {
		StringBuilder resultBuilder = new StringBuilder()
			.append(NUM_MODELS + ITEMSEPARATOR)
			.append(NUM_GROWING + ITEMSEPARATOR)
			.append(NUM_NOT_GROWING)
			.append("\n")
			.append(features.get(NUM_MODELS) + ITEMSEPARATOR)
			.append(features.get(NUM_GROWING) + ITEMSEPARATOR)
			.append(features.get(NUM_NOT_GROWING))
			.append("\n\n");			
		
		for (METRICS metric : getProcessModelMetrics())
			resultBuilder.append(ITEMSEPARATOR + metric);
		String[] measures = {HIGHER,SAME,LOWER};
		
		for (String measure : measures) {
			resultBuilder
				.append("\n")
				.append(measure);
			for (METRICS metric : getProcessModelMetrics())
				resultBuilder.append(ITEMSEPARATOR + features.get(metric.name() + measure));
		}
		
		resultBuilder
			.append("\n\n")
			.append(NUM_REVISIONS + ITEMSEPARATOR)
			.append(ALTERING_REVISIONS + ITEMSEPARATOR)
			.append(UNALTERING_REVISIONS +ITEMSEPARATOR)
			.append("\n")
			.append(features.get(NUM_REVISIONS) +ITEMSEPARATOR)
			.append(features.get(ALTERING_REVISIONS) +ITEMSEPARATOR)
			.append(features.get(UNALTERING_REVISIONS) +ITEMSEPARATOR);
			
		BufferedWriter writer = new BufferedWriter(new FileWriter(ANALYSIS_ANALYSIS_RESULT_FILE_PATH));
		writer.write(resultBuilder.toString());
		writer.close();
	}
	
}
