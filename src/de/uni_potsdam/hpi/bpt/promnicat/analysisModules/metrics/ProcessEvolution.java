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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.ListUtils;
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
	
	
	private static final String CLUSTER_FILE_PATH = 
			new File("").getAbsolutePath() + "/resources/analysis/clustering/new.cluster_results_";
	
	private static final Logger logger = Logger.getLogger(ProcessEvolution.class.getName());
	
	private static final String LINEBREAK = "\n";
	
	/**
	 * flag to decide whether to use the full database or just a small test subset
	 */
	private static final boolean useFullDB = true;

	/**
	 * the collection of metrics all model revisions will be analyzed by
	 */
	private static Collection<METRICS> processModelMetrics;
	
	public static HierarchicalProcessClusterer clusterer;
	
	private static StringBuilder clusterResultStringBuilder = new StringBuilder();

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
		time = System.currentTimeMillis() - time;
		logger.info("Finished Analysis in " + (time / 1000 / 60) + " min " + (time / 1000 % 60) + " sec \n\n");
		
		executeClusterTraining(models);
		time = System.currentTimeMillis() - time;
		logger.info("Finished Clustering in " + (time / 1000 / 60) + " min " + (time / 1000 % 60) + " sec \n\n");
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
	 * @throws IOException 
	 */
	private static void executeClusterTraining(Map<String, ProcessEvolutionModel> models) throws IOException{
		int actualClusterTestNumber = 1;
		for (Map<String, Double> numericAttributes : getNumericAttributeVariants())
			for (String linkType : getPossibleLinkTypes())
				for (int numClusters : getNumClusters()) {
					FastVector extractedAttributes = getNumericAttributes(models, numericAttributes);
					setClusterAttributes(linkType, numClusters, extractedAttributes);
					ProcessInstances instances = getInstances(models, extractedAttributes);
					clusterResultStringBuilder
						.append("Configuration:")
						.append(LINEBREAK + "numeric attributes: ");
					for (Object attribute : extractedAttributes.toArray())
						if (attribute != null && attribute instanceof Attribute)
							clusterResultStringBuilder.append(((Attribute) attribute).name() + ",");
					clusterResultStringBuilder
						.append(LINEBREAK + "link type: " + linkType)
						.append(LINEBREAK + "number of clusters: " + numClusters);
					clusterModels(instances);
					BufferedWriter writer = new BufferedWriter(new FileWriter(CLUSTER_FILE_PATH + actualClusterTestNumber++ + ".txt"));
					writer.write(clusterResultStringBuilder.toString());
					writer.close();
					clusterResultStringBuilder = new StringBuilder();
				}
	}

	private static Set<Map<String, Double>> getNumericAttributeVariants() {
		double[] weights = {1,2,3};
		List<String> metrics = new ArrayList<>();
		Collections.addAll(metrics,
				PROCESS_EVOLUTION_METRIC.NUM_ADDITIONS.name(),
				PROCESS_EVOLUTION_METRIC.NUM_DELETIONS.name(),
				PROCESS_EVOLUTION_METRIC.NUM_ITERATIONS.name());

		Collection<Map<String, Double>> variants = new ArrayList<Map<String, Double>>();
		for (String metric1 : metrics)
			for (double weight1 : weights) {
				Map<String, Double> variant = new HashMap<>();
				variant.put(metric1, weight1);
				variants.add(variant);
				List<String> listWithActualMetrics = new ArrayList<>();
				listWithActualMetrics.add(metric1);
				for (String metric2 : (String[])ListUtils.subtract(metrics,listWithActualMetrics).toArray(new String[0]))
					for (double weight2 : weights) {
						variant = new HashMap<>();
						variant.put(metric1, weight1);
						variant.put(metric2, weight2);
						variants.add(variant);
						listWithActualMetrics.add(metric2);
						for (String metric3 : (String[])ListUtils.subtract(metrics,listWithActualMetrics).toArray(new String[0]))
							for (double weight3 : weights) {
								variant = new HashMap<>();
								variant.put(metric1, weight1);
								variant.put(metric2, weight2);
								variant.put(metric3, weight3);
								variants.add(variant);
								listWithActualMetrics.add(metric2);
								for (String metric4 : (String[])ListUtils.subtract(metrics,listWithActualMetrics).toArray(new String[0]))
									for (double weight4 : weights) {
										variant = new HashMap<>();
										variant.put(metric1, weight1);
										variant.put(metric2, weight2);
										variant.put(metric3, weight3);
										variant.put(metric4, weight4);
										variants.add(variant);
									}
							}
					}
			}
		
		return new HashSet<Map<String, Double>>(variants);
	}

	private static Collection<String> getPossibleLinkTypes() {
		Collection<String> links = new ArrayList<>();
		Collections.addAll(links, "AVERAGE", "ADJCOMLPETE", "SINGLE");
		return links;
	}

	private static int[] getNumClusters() {
		int[] numClusters = {3,4,5};
		return numClusters;
	}

	/**
	 * 
	 */
	private static FastVector getNumericAttributes(Map<String, ProcessEvolutionModel> models, Map<String, Double> attributes) {
		FastVector numericAttributes = new FastVector();
		for (String attributeName : attributes.keySet()) {
			Attribute attribute = new Attribute(attributeName);
			attribute.setWeight(attributes.get(attributeName));
			numericAttributes.addElement(attribute);
		}
		return numericAttributes;
	}

	/**
	 * @param models
	 */
	private static ProcessInstances getInstances(Map<String, ProcessEvolutionModel> models, FastVector numericAttributes) {
		ProcessInstances instances = new ProcessInstances("", numericAttributes, null, models.values().size());
		for (ProcessEvolutionModel model : models.values()){
			double[] values = new double[numericAttributes.size()];
			int i = 0;
			for (Object attribute : numericAttributes.toArray())
				if (attribute instanceof Attribute)
					values[i++] = (PROCESS_EVOLUTION_METRIC.valueOf(((Attribute) attribute).name()).getAttribute(model));
			ProcessInstance inst = new ProcessInstance(1, values);
			inst.process = model;
			instances.add(inst);
		}
		return instances;
	}

	/**
	 * 
	 */
	private static void setClusterAttributes(String linkType, int numClusters, FastVector numericAttributes) {
		clusterer = new HierarchicalProcessClusterer(new EuclideanDistance());
		clusterer.setLinkType(linkType);
		clusterer.setNumClusters(numClusters);
		clusterer.setAttributes(numericAttributes);
		clusterer.setDebug(true);
	}

	private static void clusterModels(ProcessInstances instances) {
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
	 * @param cluster
	 */
	private static void analyzeClusters(ClusterTree<ProcessInstances> cluster) {
		Collection<Collection<ProcessEvolutionModel>> clustersWithModels = findDistinctClusters(cluster);
		int i= 1;
		for (Collection<ProcessEvolutionModel> models : clustersWithModels) {
			double averageAdds = 0, averageDeletes = 0, averageCMR = 0;
			for (ProcessEvolutionModel model : models) {
				averageAdds += model.getNumberOfAdditions();
				averageDeletes += model.getNumberOfDeletions();
				averageCMR += model.getCMRIterations();
			}
			averageAdds /= models.size();
			averageDeletes /= models.size();
			averageCMR /= models.size();
			clusterResultStringBuilder
				.append(LINEBREAK + "Cluster " + i++ + ": (" + models.size() + " models)")
				.append(LINEBREAK + "avg. additions: " + averageAdds)
				.append(LINEBREAK + "avg. deletions: " + averageDeletes)
				.append(LINEBREAK + "avg CMR iterations: " + averageCMR);
		}
	}

	/**
	 * @param clusters
	 * @return
	 */
	private static Collection<Collection<ProcessEvolutionModel>> findDistinctClusters(
			ClusterTree<ProcessInstances> clusters) {
		Collection<Collection<ProcessEvolutionModel>> clustersWithModels = new ArrayList<>();
		List<ClusterNode<ProcessInstances>> clusterNodes = null;
		int i = 0;
		boolean foundThem = false;
		while(!foundThem) {
			clusterNodes = clusters.getNodesOnLevel(i++);
			if (clusterNodes != null)
				foundThem = true;
				for (ClusterNode<ProcessInstances> node : clusterNodes) {
					ClusterTree<ClusterNode<ProcessInstances>> newTree = new ClusterTree<>();
					newTree.setRootElement(node);
					List<ClusterNode<ProcessInstances>> treeNodes = newTree.toList();
					Collection<ProcessEvolutionModel> clusterModels = new ArrayList<>();
					for (ClusterNode<ProcessInstances> treeNode : treeNodes)
						for (Object instance : treeNode.getData().getInstances().toArray())
							if (instance instanceof ProcessInstance && ((ProcessInstance)instance).process != null)
								clusterModels.add((ProcessEvolutionModel) ((ProcessInstance) instance).process);
					clustersWithModels.add(clusterModels);
				}
		}
		return clustersWithModels;
	}
}
