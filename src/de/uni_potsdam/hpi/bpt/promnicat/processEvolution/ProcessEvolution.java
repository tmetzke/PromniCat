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
package de.uni_potsdam.hpi.bpt.promnicat.processEvolution;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jbpt.pm.ProcessModel;

import de.uni_potsdam.hpi.bpt.promnicat.parser.BpmnParser;
import de.uni_potsdam.hpi.bpt.promnicat.parser.EpcParser;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.DbFilterConfig;
import de.uni_potsdam.hpi.bpt.promnicat.processEvolution.ProcessEvolutionConstants.PROCESS_EVOLUTION_METRIC;
import de.uni_potsdam.hpi.bpt.promnicat.processEvolution.api.IAnalysis;
import de.uni_potsdam.hpi.bpt.promnicat.processEvolution.clustering.ClusteringThread;
import de.uni_potsdam.hpi.bpt.promnicat.processEvolution.clustering.ProcessEvolutionClusteringConfiguration;
import de.uni_potsdam.hpi.bpt.promnicat.processEvolution.model.ProcessEvolutionModel;
import de.uni_potsdam.hpi.bpt.promnicat.processEvolution.model.ProcessEvolutionModelRevision;
import de.uni_potsdam.hpi.bpt.promnicat.util.Constants;
import de.uni_potsdam.hpi.bpt.promnicat.util.IllegalTypeException;
import de.uni_potsdam.hpi.bpt.promnicat.util.ProcessMetricConstants.METRICS;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.IFlexibleUnitChainBuilder;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.IUnitChainBuilder;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.UnitChainBuilder;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.IUnitDataProcessMetrics;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.UnitDataProcessMetrics;

/**
 * Analysis module that tries to identify different profiles of modeling processes
 * by analyzing process models according to several evolution analyses and
 * clustering by the results of them afterwards.
 * 
 * @author Tobias Metzke
 *
 */
public class ProcessEvolution {
	
	/**
	 * the file ending of the filtered model representations
	 */
	private static final String MODEL_FILE_TYPE = ".json";

	/**
	 * the part of the model path string that indicates the revision number
	 */
	private static final String REVISION_INDICATOR_IN_MODEL_PATH = "_rev";

	/**
	 * flag to decide whether to consider subprocesses while analyzing or not
	 */
	private static final boolean HANDLE_SUB_PROCESSES = true;

	/**
	 * file path of the high-level analysis 
	 */
	private static final String ANALYSIS_ANALYSIS_RESULT_FILE_PATH = 
			new File("").getAbsolutePath() + "/resources/analysis/new.analysis_results_analyzed.csv";

	/**
	 * the logger of this class to display results on the console
	 */
	private static final Logger logger = Logger.getLogger(ProcessEvolution.class.getName());
	
	/**
	 * flag to decide whether the clustering is done or not
	 */
	private static boolean doneWithClustering = false; 
	
	/**
	 * flag to decide whether to use the full database or just a small test subset
	 */
	private static final boolean useFullDB = true;

	/**
	 * the maximum number of threads that cluster the analyzed models
	 */
	private static final int THREAD_NUMBER = 10;

	/**
	 * the collection of metrics all model revisions will be analyzed by
	 */
	private static Collection<METRICS> processModelMetrics;
	
	/**
	 * the list of configurations to cluster by 
	 */
	private static List<ProcessEvolutionClusteringConfiguration> configurations = new ArrayList<>();

	/**
	 * {@link ClusteringThread}s take configurations from the herein list as long as there are some.
	 * If this list is empty, the threads stop and the clustering is done.
	 * @return the top configuration from the list and remove it from the list
	 */
	public static synchronized ProcessEvolutionClusteringConfiguration getNextConfiguration() {
		if (!configurations.isEmpty())
			return configurations.remove(0);
		else {
			System.out.println("List of configs is empty.");
			doneWithClustering = true;
			return null;
		}
	}
	
	/**
	 * run the process evolution by executing the defined chain of utility units,
	 * analyzing the resulting models and clustering the analyzed models
	 * @param args
	 * @throws IllegalArgumentException
	 * @throws IllegalTypeException
	 * @throws IOException
	 */
	public static void main(String[] args) throws IllegalArgumentException, IllegalTypeException, IOException {

		
			long startTime = System.currentTimeMillis();
			
			IUnitChainBuilder chainBuilder = buildUpUnitChain(useFullDB);		
			logger.info(chainBuilder.getChain().toString() + "\n");		
			
			Collection<IUnitDataProcessMetrics<Object>> result = executeChain(chainBuilder);
			Map<String,ProcessEvolutionModel> models = buildUpInternalDataStructure(result);
			long endTime = logTime(startTime, "Finished Data Structure");
		
			Map<String,ProcessEvolutionModel> analyzedModels = performAnalyses(models);
			endTime = logTime(endTime,"Finished Analysis");
			
			doneWithClustering = false;
			executeClusterTraining(analyzedModels);
			endTime = logTime(endTime, "Finished Clustering");
	}

	/**
	 * calculate the time an action took and log it
	 * @param startTime the point in time the previous action ended at
	 * @param message the message to display in log
	 * @return the point in time when this method was called and 
	 * therefore the point in time when the action to be logged ended approximately
	 */
	private static long logTime(long startTime, String message) {
		long time = System.currentTimeMillis();
		long endTime = time - startTime;
		int[] timeParts = new int[2];
		timeParts[0] =  (int) (endTime/1000 < 60 ? 0 : endTime/1000/60);
		timeParts[1] = (int) (endTime/1000 % 60);
		logger.info(message + " in " + timeParts[0]+ " min " + timeParts[1] + " sec \n\n");
		return time;
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
		chainBuilder.createProcessModelMetricsCalulatorUnit(getProcessModelMetrics(), HANDLE_SUB_PROCESSES);
		
		//collect results
		chainBuilder.createSimpleCollectorUnit();
		return chainBuilder;
	}

	/**
	 * execute the defined unit chain and collect the results
	 * @param chainBuilder the defined unit chain
	 * @return the results of the unit chain executions
	 * @exception IllegalTypeException if the input/output types between units do not match
	 */
	private static Collection<IUnitDataProcessMetrics<Object>> executeChain(
			IUnitChainBuilder chainBuilder) throws IllegalTypeException {
		// parser should not log parsing errors
		Logger epcParserLog = Logger.getLogger(EpcParser.class.getName());
		epcParserLog.setLevel(Level.SEVERE);
		Logger bpmnParserLog = Logger.getLogger(BpmnParser.class.getName());
		bpmnParserLog.setLevel(Level.SEVERE);
	
		//run chain
		@SuppressWarnings("unchecked")
		Collection<IUnitDataProcessMetrics<Object> > result = 
			(Collection<IUnitDataProcessMetrics<Object>>) chainBuilder.getChain().execute();
		return result;
	}

	/**
	 * puts in place the internal data structure where:
	 * 1. each model holds all its revisions
	 * 2. each revision holds the values of 
	 * all {@link METRICS} of this revision and the {@link ProcessModel}
	 * 
	 * @param resultSet the collection of results of the metric analysis
	 * @return the internal data structure representation
	 */
	private static Map<String, ProcessEvolutionModel> buildUpInternalDataStructure(
			Collection<IUnitDataProcessMetrics<Object>> resultSet) {
		
		Map<String, ProcessEvolutionModel> models = new HashMap<>();
		
		for (IUnitDataProcessMetrics<Object> resultItem : resultSet){
			String modelPathWithRevision = resultItem.getModelPath();
			// extract the revision number
			int revisionStringIndex = modelPathWithRevision.indexOf(REVISION_INDICATOR_IN_MODEL_PATH);
			int revisionNumber = Integer.valueOf(
					modelPathWithRevision.substring(
							revisionStringIndex + REVISION_INDICATOR_IN_MODEL_PATH.length(), modelPathWithRevision.indexOf(MODEL_FILE_TYPE)));
			// extract the model path without revision number 
			String modelPath = modelPathWithRevision.substring(0,revisionStringIndex);
			
			
			// if it is a revision of a new model, add it to the list of models
			if (!models.containsKey(modelPath)) {
				ProcessEvolutionModel model = new ProcessEvolutionModel(modelPath);
				models.put(model.getName(), model);
			}
			
			// add the revision to its model
			ProcessEvolutionModelRevision revision = new ProcessEvolutionModelRevision(revisionNumber);
			for (METRICS metric : getProcessModelMetrics())
				revision.add(metric, metric.getAttribute(resultItem));
			revision.setProcessModel((ProcessModel)resultItem.getValue());
			models.get(modelPath).add(revision);
		}
		
		return models;
	}
	
	/**
	 * @return the metrics used for analyses
	 */
	private static Collection<METRICS> getProcessModelMetrics() {
		if (processModelMetrics == null)
			processModelMetrics = AnalysisHelper.getProcessModelMetrics();
		return processModelMetrics;
	}

	/**
	 * executes all analyses that are listed in here.
	 * @param models to analyze
	 * @throws IOException if the result file can not be written
	 */
	private static Map<String, ProcessEvolutionModel> performAnalyses(Map<String, ProcessEvolutionModel> models)
			throws IOException {

		IAnalysis highLevel = AnalysisHelper.highLevelAnalysis(models, HANDLE_SUB_PROCESSES);
		writeToCSVFile(ANALYSIS_ANALYSIS_RESULT_FILE_PATH, highLevel);
		logger.info("Wrote analysis of metrics analysis to " + ANALYSIS_ANALYSIS_RESULT_FILE_PATH + "\n");
		
		return highLevel.getAnalyzedModels();
	}
	
	 /** Write the result of the analysis into a CSV file
	 * @param filePath the path to write the results to
	 * @param analysis the analysis that should be written
	 * @throws IOException if the result file can not be written
	 */
	private static void writeToCSVFile(String filePath, IAnalysis analysis) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
		writer.write(analysis.toResultCSVString());
		writer.close();
	}

	/**
	 * Create the clusterer with his attributes and run it afterwards.
	 * @throws IOException if the result can not be written to file
	 */
	private static void executeClusterTraining(Map<String, ProcessEvolutionModel> models) throws IOException{
		// get several attributes like clustering method and number of clusters
		Map<String, Double> numericAttributes = getNumericAttributeVariants();
		String linkType = getLinkType();
		for (int numClusters : getNumClusters())
			configurations.add(new ProcessEvolutionClusteringConfiguration(numericAttributes, linkType, numClusters));

		// execute the clustering in separate threads for every configuration
		int numberOfThreads = configurations.size() > THREAD_NUMBER ? THREAD_NUMBER : configurations.size();
		for (int i = 0; i < numberOfThreads; i++) {
			new ClusteringThread(models);
		}
		
		// wait for the threads to finish to get an exact measurement on how long it took
		while(!doneWithClustering){
			try {
				// wait a bit, maybe all jobs are done by then
				Thread.sleep(15000);
				System.out.println("checking...");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return the numeric attributes that should be considered in clustering
	 */
	private static Map<String, Double> getNumericAttributeVariants() {
		String[] metrics = {
				PROCESS_EVOLUTION_METRIC.NUM_ADDITIONS.name(),
				PROCESS_EVOLUTION_METRIC.NUM_DELETIONS.name(),
				PROCESS_EVOLUTION_METRIC.NUM_ITERATIONS.name(),
				PROCESS_EVOLUTION_METRIC.NUM_LAYOUT_CHANGES.name()};
		Map<String, Double> attributes = new HashMap<>();

		// same weight for every parameter since it does not
		// make a difference (as empirical study proved)
		for (String metric : metrics)
			attributes.put(metric, 1.0);
		return attributes;
	}

	/**
	 * @return the link type to cluster by
	 */
	private static String getLinkType() {
		return "MEAN";
	}

	/**
	 * @return the number of clusters that must occur in the end.
	 * needs to be defined in the clusterer beforehand.
	 */
	private static int[] getNumClusters() {
		int[] numClusters = {4,5};
		return numClusters;
	}
}
