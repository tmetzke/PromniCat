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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jbpt.pm.ProcessModel;

import weka.core.Attribute;
import weka.core.EuclideanDistance;
import weka.core.FastVector;
import de.uni_potsdam.hpi.bpt.promnicat.analysisModules.clustering.ClusterNode;
import de.uni_potsdam.hpi.bpt.promnicat.analysisModules.clustering.ClusterTree;
import de.uni_potsdam.hpi.bpt.promnicat.analysisModules.clustering.HierarchicalProcessClusterer;
import de.uni_potsdam.hpi.bpt.promnicat.analysisModules.clustering.ProcessInstance;
import de.uni_potsdam.hpi.bpt.promnicat.analysisModules.clustering.ProcessInstances;
import de.uni_potsdam.hpi.bpt.promnicat.parser.BpmnParser;
import de.uni_potsdam.hpi.bpt.promnicat.parser.EpcParser;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.DbFilterConfig;
import de.uni_potsdam.hpi.bpt.promnicat.util.Constants;
import de.uni_potsdam.hpi.bpt.promnicat.util.IllegalTypeException;
import de.uni_potsdam.hpi.bpt.promnicat.util.ProcessMetricConstants.METRICS;
import de.uni_potsdam.hpi.bpt.promnicat.util.processEvolution.AnalysisHelper;
import de.uni_potsdam.hpi.bpt.promnicat.util.processEvolution.ProcessEvolutionConstants;
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
	
	private static final String ADD_DELETE_RESULT_FILE_PATH = 
			new File("").getAbsolutePath() + "/resources/analysis/new.add_delete_results.csv";
//	
//	private static final String MOVED_ELEMENTS_ANALYSIS_RESULT_FILE_PATH = 
//			new File("").getAbsolutePath() + "/resources/analysis/new.layout_changes_results.csv";
//	
//	private static final String MODEL_LANGUAGE_RESULT_FILE_PATH = 
//			new File("").getAbsolutePath() + "/resources/analysis/new.model_language_results.csv";
	
	private static final Logger logger = Logger.getLogger(ProcessEvolution.class.getName());
	
	/**
	 * flag to decide whether to use the full database or just a small test subset
	 */
	private static final boolean useFullDB = true;

	/**
	 * the collection of metrics all model revisions will be analyzed by
	 */
	private static Collection<METRICS> processModelMetrics;
	
	public static HierarchicalProcessClusterer clusterer;
	
	public static FastVector numericAttributes;
	
	private static ProcessInstances instances;
	
	/**
	 * @param args
	 * @throws IllegalTypeException 
	 * @throws IllegalArgumentException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IllegalArgumentException, IllegalTypeException, IOException {

		long startTime = System.currentTimeMillis();
		
		IUnitChainBuilder chainBuilder = buildUpUnitChain(useFullDB);		
		logger.info(chainBuilder.getChain().toString() + "\n");		
		
		Collection<IUnitDataProcessMetrics<Object>> result = executeChain(chainBuilder);
		
		Map<String,ProcessEvolutionModel> models = buildUpInternalDataStructure(result);
		long time = System.currentTimeMillis() - startTime;
		logger.info("Finished Data Structure in " + (time / 1000 / 60) + " min " + (time / 1000 % 60) + " sec \n\n");

		models = performAnalyses(models);
//		setupClusterer(models);
//		clusterModels();
		time = System.currentTimeMillis() - time;
		logger.info("Finished Analysis in " + (time / 1000 / 60) + " min " + (time / 1000 % 60) + " sec \n\n");
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
//		// metrics results
//		WriterHelper.writeToFile(MODEL_RESULT_FILE_PATH, models);
//		logger.info("Wrote model metrics results to " + MODEL_RESULT_FILE_PATH + "\n");
//		
//		// difference analysis with relative differences
//		IAnalysis relativeDifference = AnalysisHelper.analyzeDifferencesInMetrics(models, true);
//		WriterHelper.writeToCSVFile(METRICS_ANALYSIS_RELATIVE_RESULT_FILE_PATH, relativeDifference);
//		logger.info("Wrote relative metrics analysis results to " + METRICS_ANALYSIS_RELATIVE_RESULT_FILE_PATH + "\n");
//		
		// additions/deletions analysis with absolute numbers
		IAnalysis addsDeletes = AnalysisHelper.analyzeAdditionsAndDeletions(models, HANDLE_SUB_PROCESSES);
		WriterHelper.writeToCSVFile(ADD_DELETE_RESULT_FILE_PATH, addsDeletes);
		logger.info("Wrote addition/deletion analysis results to " + ADD_DELETE_RESULT_FILE_PATH + "\n");
//		
//		// difference analysis with absolute differences
//		IAnalysis difference = AnalysisHelper.analyzeDifferencesInMetrics(models, false);
//		WriterHelper.writeToCSVFile(METRICS_ANALYSIS_ABSOLUTE_RESULT_FILE_PATH, difference);
//		logger.info("Wrote absolute metrics analysis results to " + METRICS_ANALYSIS_ABSOLUTE_RESULT_FILE_PATH + "\n");
//		
//		// model language analysis
//		IAnalysis modelLanguage = AnalysisHelper.modelLanguageAnalysis(models);
//		WriterHelper.writeToCSVFile(MODEL_LANGUAGE_RESULT_FILE_PATH, modelLanguage);
//		logger.info("Wrote analysis of model language to " + MODEL_LANGUAGE_RESULT_FILE_PATH + "\n");
//		
//		// number of changed elements according to their position in the model
//		IAnalysis layoutChanges = AnalysisHelper.analyzeElementMovements(models);
//		WriterHelper.writeToCSVFile(MOVED_ELEMENTS_ANALYSIS_RESULT_FILE_PATH, layoutChanges);
//		logger.info("Wrote analysis of moved elements to " + MOVED_ELEMENTS_ANALYSIS_RESULT_FILE_PATH + "\n");
//		
		// high level analysis of model metrics
		IAnalysis highLevel = AnalysisHelper.highLevelAnalysis(models, HANDLE_SUB_PROCESSES);
		WriterHelper.writeToCSVFile(ANALYSIS_ANALYSIS_RESULT_FILE_PATH, highLevel);
		logger.info("Wrote analysis of metrics analysis to " + ANALYSIS_ANALYSIS_RESULT_FILE_PATH + "\n");
		
		return highLevel.getAnalyzedModels();
	}

	/**
	 * Create hierarchical clusterer with his attributes
	 */
	private static void setupClusterer(Map<String, ProcessEvolutionModel> models){
		setNumericAttributes();
		setClusterAttributes();
		setInstances(models);
	}

	/**
	 * 
	 */
	private static void setNumericAttributes() {
		numericAttributes = new FastVector();
		Map<String,Double> attributesToClusterBy = getAttributesToClusterBy();
		for (String attributeName : attributesToClusterBy.keySet()) {
			Attribute attribute = new Attribute(attributeName);
			attribute.setWeight(attributesToClusterBy.get(attributeName));
			numericAttributes.addElement(attribute);
		}
	}

	private static Map<String, Double> getAttributesToClusterBy() {
		Map<String, Double> attributes = new HashMap<String, Double>();
		attributes.put(ProcessEvolutionConstants.PROCESS_EVOLUTION_METRIC.NUM_ITERATIONS.name(), 3.0);
		return attributes;
	}

	/**
	 * 
	 */
	private static void setClusterAttributes() {
		clusterer = new HierarchicalProcessClusterer(new EuclideanDistance());
		clusterer.setLinkType("SINGLE");
		clusterer.setNumClusters(3);
		clusterer.setDebug(true);
		clusterer.setAttributes(numericAttributes);
	}

	/**
	 * @param models
	 */
	private static void setInstances(Map<String, ProcessEvolutionModel> models) {
		instances = new ProcessInstances("", numericAttributes, null, models.values().size());
		for (ProcessEvolutionModel model : models.values()){
			double[] values = new double[numericAttributes.size()];
			int i = 0;
			for (Object attribute : numericAttributes.toArray())
				if (attribute instanceof PROCESS_EVOLUTION_METRIC)
					values[i++] = (((PROCESS_EVOLUTION_METRIC) attribute).getAttribute(model));
			ProcessInstance inst = new ProcessInstance(1, values);
			inst.process = model;
			instances.add(inst);
		}
	}

	private static void clusterModels() {
		try {//cluster the results
			clusterer.buildClusterer(instances);
			ClusterTree<ProcessInstances> clusters = clusterer.getClusters();
			ClusterTree<ProcessInstances> newCluster = clusters.getSubtreeWithMinClusterSize(1);
			analyzeClusters(newCluster);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param newCluster
	 */
	private static void analyzeClusters(ClusterTree<ProcessInstances> newCluster) {
		ArrayList<ClusterNode<ProcessInstances>> clusterNodes = newCluster.getNodesOnLevel(1);
		for (ClusterNode<ProcessInstances> node : clusterNodes)
			for (Object instance : node.getData().getInstances().toArray())
				if (instance instanceof ProcessInstance)
					System.out.println(((ProcessEvolutionModel)((ProcessInstance) instance).process).getName());
	}
}
