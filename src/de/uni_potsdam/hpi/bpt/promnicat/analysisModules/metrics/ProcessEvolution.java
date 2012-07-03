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

import java.io.File;
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
import de.uni_potsdam.hpi.bpt.promnicat.util.Constants;
import de.uni_potsdam.hpi.bpt.promnicat.util.IllegalTypeException;
import de.uni_potsdam.hpi.bpt.promnicat.util.ProcessMetricConstants.METRICS;
import de.uni_potsdam.hpi.bpt.promnicat.util.processEvolution.AnalysisHelper;
import de.uni_potsdam.hpi.bpt.promnicat.util.processEvolution.ClusteringThread;
import de.uni_potsdam.hpi.bpt.promnicat.util.processEvolution.ProcessEvolutionClusteringConfiguration;
import de.uni_potsdam.hpi.bpt.promnicat.util.processEvolution.ProcessEvolutionConstants.PROCESS_EVOLUTION_METRIC;
import de.uni_potsdam.hpi.bpt.promnicat.util.processEvolution.ProcessEvolutionModel;
import de.uni_potsdam.hpi.bpt.promnicat.util.processEvolution.ProcessEvolutionModelRevision;
import de.uni_potsdam.hpi.bpt.promnicat.util.processEvolution.WriterHelper;
import de.uni_potsdam.hpi.bpt.promnicat.util.processEvolution.api.IAnalysis;
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
public class ProcessEvolution {
	
	private static final boolean HANDLE_SUB_PROCESSES = true;

//	/**
//	 * path of the model metrics result file
//	 */
//	private static final String MODEL_RESULT_FILE_PATH = 
//			new File("").getAbsolutePath() + "/resources/analysis/new.model_results.csv";
	
//	/**
//	 * path of the metrics analysis result file, that analyzes the model metrics results
//	 */
//	private static final String METRICS_ANALYSIS_ABSOLUTE_RESULT_FILE_PATH = 
//			new File("").getAbsolutePath() + "/resources/analysis/new.model_results_absolute_analyzed.csv";
//	
//	/**
//	 * path of the metrics analysis result file, that analyzes the model metrics results
//	 */
//	private static final String METRICS_ANALYSIS_RELATIVE_RESULT_FILE_PATH = 
//			new File("").getAbsolutePath() + "/resources/analysis/new.model_results_relative_analyzed.csv";
//	
	/**
	 * path of the metrics analysis analysis result file, that analyzes the metrics analysis
	 */
	private static final String ANALYSIS_ANALYSIS_RESULT_FILE_PATH = 
			new File("").getAbsolutePath() + "/resources/analysis/new.analysis_results_analyzed.csv";
	
//	private static final String ADD_DELETE_RESULT_FILE_PATH = 
//			new File("").getAbsolutePath() + "/resources/analysis/new.add_delete_results.csv";
//	
//	private static final String MOVED_ELEMENTS_ANALYSIS_RESULT_FILE_PATH = 
//			new File("").getAbsolutePath() + "/resources/analysis/new.layout_changes_results.csv";
//	
//	private static final String MODEL_LANGUAGE_RESULT_FILE_PATH = 
//			new File("").getAbsolutePath() + "/resources/analysis/new.model_language_results.csv";
	
	private static final Logger logger = Logger.getLogger(ProcessEvolution.class.getName());
	
	private static boolean doneWithClustering = false; 
	
	/**
	 * flag to decide whether to use the full database or just a small test subset
	 */
	private static final boolean useFullDB = true;

	private static final int THREAD_NUMBER = 10;

	/**
	 * the collection of metrics all model revisions will be analyzed by
	 */
	private static Collection<METRICS> processModelMetrics;
	
	private static List<ProcessEvolutionClusteringConfiguration> configurations = new ArrayList<>();

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
	 * @param args
	 * @throws IllegalTypeException 
	 * @throws IllegalArgumentException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IllegalArgumentException, IllegalTypeException, IOException {

		for (int i = 0; i < 1; i++) {
			long startTime = System.currentTimeMillis();
			
			IUnitChainBuilder chainBuilder = buildUpUnitChain(useFullDB);		
			logger.info(chainBuilder.getChain().toString() + "\n");		
			
			Collection<IUnitDataProcessMetrics<Object>> result = executeChain(chainBuilder);
			
			Map<String,ProcessEvolutionModel> models = buildUpInternalDataStructure(result);
			long endTime = logTime(startTime, "Finished Data Structure");
	
			models = performAnalyses(models);
			endTime = logTime(endTime,"Finished Analysis");
			
			doneWithClustering = false;
			executeClusterTraining(models);
			endTime = logTime(endTime, "Finished Clustering");
		}
	}

	/**
	 * @param startTime
	 * @return
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
	 * @param chainBuilder
	 * @return
	 * @throws IllegalTypeException
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
			int revisionStringIndex = modelPathWithRevision.indexOf("_rev");
			String processFolder = "2011-04-19_signavio_academic_processes";
			String modelPath = modelPathWithRevision.substring(
					modelPathWithRevision.indexOf(processFolder) + processFolder.length(),revisionStringIndex);
			int revisionNumber = Integer.valueOf(
					modelPathWithRevision.substring(revisionStringIndex + 4, modelPathWithRevision.indexOf(".json")));
			
			// new model to be analyzed, so add it to the map of models
			if (!models.containsKey(modelPath)) {
				ProcessEvolutionModel model = new ProcessEvolutionModel(modelPath);
				models.put(model.getName(), model);
			}
			
			// add the revision to its model
			ProcessEvolutionModelRevision revision = new ProcessEvolutionModelRevision(revisionNumber);
			for (METRICS metric : getProcessModelMetrics())
				revision.add(metric, metric.getAttribute(resultItem));
			revision.addProcessModel((ProcessModel)resultItem.getValue());
			models.get(modelPath).add(revision);
		}
		
		return models;
	}
	
	private static Collection<METRICS> getProcessModelMetrics() {
		if (processModelMetrics == null)
			processModelMetrics = AnalysisHelper.getProcessModelMetrics();
		return processModelMetrics;
	}

	/**
	 * executes all analyses that are listed in here.
	 * every analysis also takes care of being written, e.g. to file.
	 * @param models
	 * @throws IOException
	 */
	private static Map<String, ProcessEvolutionModel> performAnalyses(Map<String, ProcessEvolutionModel> models)
			throws IOException {
		// high level analysis of model metrics
		IAnalysis highLevel = AnalysisHelper.highLevelAnalysis(models, HANDLE_SUB_PROCESSES);
		WriterHelper.writeToCSVFile(ANALYSIS_ANALYSIS_RESULT_FILE_PATH, highLevel);
		logger.info("Wrote analysis of metrics analysis to " + ANALYSIS_ANALYSIS_RESULT_FILE_PATH + "\n");
		
		return highLevel.getAnalyzedModels();
	}

	/**
	 * Create hierarchical clusterer with his attributes
	 * @throws IOException 
	 */
	private static void executeClusterTraining(Map<String, ProcessEvolutionModel> models) throws IOException{
		Map<String, Double> numericAttributes = getNumericAttributeVariants();
		String linkType = getLinkType();
		for (int numClusters : getNumClusters())
			configurations.add(new ProcessEvolutionClusteringConfiguration(numericAttributes, linkType, numClusters));
		
		int numberOfThreads = configurations.size() > THREAD_NUMBER ? THREAD_NUMBER : configurations.size();
		for (int i = 0; i < numberOfThreads; i++) {
			new ClusteringThread(models);
		}
		
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

	private static Map<String, Double> getNumericAttributeVariants() {
		String[] metrics = {
				PROCESS_EVOLUTION_METRIC.NUM_ADDITIONS.name(),
				PROCESS_EVOLUTION_METRIC.NUM_DELETIONS.name(),
				PROCESS_EVOLUTION_METRIC.NUM_ITERATIONS.name()};
		Map<String, Double> attributes = new HashMap<>();
		// same weight for every parameter since it does not
		// make a difference (as empirical study proved)
		for (String metric : metrics)
			attributes.put(metric, 1.0);
		return attributes;
	}

	private static String getLinkType() {
		return "MEAN";
	}

	private static int[] getNumClusters() {
		int[] numClusters = {4,5};
		return numClusters;
	}
}
