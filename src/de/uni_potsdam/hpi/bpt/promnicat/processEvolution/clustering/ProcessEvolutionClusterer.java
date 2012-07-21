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
package de.uni_potsdam.hpi.bpt.promnicat.processEvolution.clustering;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.core.Attribute;
import weka.core.EuclideanDistance;
import weka.core.FastVector;
import de.uni_potsdam.hpi.bpt.promnicat.analysisModules.clustering.ClusterNode;
import de.uni_potsdam.hpi.bpt.promnicat.analysisModules.clustering.ClusterTree;
import de.uni_potsdam.hpi.bpt.promnicat.analysisModules.clustering.HierarchicalProcessClusterer;
import de.uni_potsdam.hpi.bpt.promnicat.analysisModules.clustering.ProcessInstance;
import de.uni_potsdam.hpi.bpt.promnicat.analysisModules.clustering.ProcessInstances;
import de.uni_potsdam.hpi.bpt.promnicat.processEvolution.AnalysisHelper;
import de.uni_potsdam.hpi.bpt.promnicat.processEvolution.ProcessEvolutionConstants.PROCESS_EVOLUTION_METRIC;
import de.uni_potsdam.hpi.bpt.promnicat.processEvolution.analyses.ModelLanguageUsageAnalysis;
import de.uni_potsdam.hpi.bpt.promnicat.processEvolution.model.ProcessEvolutionModel;

/**
 * A specific helper that is able to cluster models and sets all necessary parameters.
 * The results are written into a file afterwards.
 * 
 * @author Tobias Metzke
 *
 */
public class ProcessEvolutionClusterer {

	private final static HierarchicalProcessClusterer clusterer = new HierarchicalProcessClusterer(new EuclideanDistance());
	private final static String LINEBREAK = "\n";
	private final static String FILE_PATH = "/resources/new.cluster_results.txt";
	
	/**
	 * cluster the models according to the given configuration
	 * @param models
	 * @param configuration
	 * @throws IOException
	 */
	public static void doClustering(Map<String, ProcessEvolutionModel> models, ProcessEvolutionClusteringConfiguration configuration)  throws IOException {
		// add an initial header to the result string
		StringBuilder clusterResultStringBuilder = new StringBuilder()
		.append("Configuration:")
		.append("[");

		setClusterAttributes(configuration, clusterResultStringBuilder);
		// cluster the models
		ProcessInstances instances = getInstances(models, getNumericAttributes(configuration));
		clusterModels(instances, clusterResultStringBuilder);
		// write the results into a file
		writeResults(clusterResultStringBuilder);
	}

	/**
	 * @param configuration the configuration to get the numeric values from
	 * @return the numeric attributes in a {@link FastVector}
	 */
	private static FastVector getNumericAttributes(ProcessEvolutionClusteringConfiguration configuration) {
		FastVector numericAttributes = new FastVector();
		Map<String,Double> attributes = configuration.getNumericAttributes();
		// add every attribute to the fast vector
		for (String attributeName : attributes.keySet()) {
			Attribute attribute = new Attribute(attributeName);
			attribute.setWeight(attributes.get(attributeName));
			numericAttributes.addElement(attribute);
		}
		return numericAttributes;
	}

	/**
	 * The clusterer depends on a special format for the models to cluster, this methods converts
	 * the models and the numeric values to cluster them by into the correct format
	 * @param models the models to cluster
	 * @param numericAttributes the numeric attributes to cluster by
	 * @return the instances that can be passed on to the clusterer
	 */
	private static ProcessInstances getInstances(Map<String, ProcessEvolutionModel> models, FastVector numericAttributes) {
		ProcessInstances instances = new ProcessInstances("", numericAttributes, null, models.values().size());
		// every model's numeric values and the model itself are added to a ProcessInstance
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
	 * set the attributes from the configuration on the clusterer
	 * @param clusterResultStringBuilder 
	 */
	private static void setClusterAttributes(ProcessEvolutionClusteringConfiguration configuration, StringBuilder clusterResultStringBuilder) {
		FastVector extractedAttributes = getNumericAttributes(configuration);
		String linkType = configuration.getLinkType();
		int numClusters = configuration.getNumClusters();
		clusterer.setLinkType(linkType);
		clusterer.setNumClusters(numClusters);
		clusterer.setAttributes(extractedAttributes);
		clusterer.setDebug(true);
		addAttributesToResult(clusterResultStringBuilder, extractedAttributes, linkType, numClusters);
	}

	/**
	 * add the attributes to the header of the resultString
	 * @param clusterResultStringBuilder 
	 * @param numericAttributes
	 * @param linkType
	 * @param numberOfClusters
	 */
	private static void addAttributesToResult(StringBuilder clusterResultStringBuilder, FastVector numericAttributes, String linkType, int numberOfClusters) {
		for (Object attribute : numericAttributes.toArray())
			if (attribute != null && attribute instanceof Attribute) {
				Attribute realAttribute = (Attribute) attribute;
				clusterResultStringBuilder.append(realAttribute.name() + "(" + realAttribute.weight() + "),");
			}
		
		clusterResultStringBuilder 
			.append("]" + "," + linkType)
			.append("," + numberOfClusters)
			.append(LINEBREAK);
	}

	/**
	 * do the final clustering job with the help of the clusterer
	 * and analyze the clusters afterwards.
	 * For all cluster tree operations see {@link ClusterTree}.
	 * @param instances
	 * @param clusterResultStringBuilder 
	 */
	private static void clusterModels(ProcessInstances instances, StringBuilder clusterResultStringBuilder) {
		try {
			clusterer.buildClusterer(instances);
			ClusterTree<ProcessInstances> clusters = clusterer.getClusters();
			ClusterTree<ProcessInstances> newCluster = clusters.getSubtreeWithMinClusterSize(1);
			analyzeClusters(newCluster, clusterResultStringBuilder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * analyze the resulting clusters.
	 * @param cluster
	 * @param clusterResultStringBuilder 
	 */
	private static void analyzeClusters(ClusterTree<ProcessInstances> cluster, StringBuilder clusterResultStringBuilder) {
		Collection<Collection<ProcessEvolutionModel>> modelGroups = findDistinctModelGroups(cluster);
		int i= 1;
		for (Collection<ProcessEvolutionModel> models : modelGroups) {
			clusterResultStringBuilder
				.append(LINEBREAK + "Cluster " + i++ + ": (" + models.size() + " models)," + LINEBREAK);
			double[] averages = new double[5];
			
			// collect some of the metrics of the groups
			for (ProcessEvolutionModel model : models) {
				averages[0] += model.getNumberOfAdditions();
				averages[1] += model.getNumberOfDeletions();
				averages[2] += model.getCMRIterations();
				averages[3] += model.getNumberOfMovedOrResizedElements();
				averages[4] += model.getRevisions().size();
			}
			// calculate some averages out of the metrics
			for (int j = 0; j < averages.length; j++) {
				averages[j] /= models.size();
			}
			
			// model language analysis of the clusters
			Map<String, ProcessEvolutionModel> modelsToAnalyze = new HashMap<>();
			for(ProcessEvolutionModel model : models)
				modelsToAnalyze.put(model.getName(), model);
			
			// add the results to the result string
			clusterResultStringBuilder
			.append("avg no. additions: " + averages[0] + LINEBREAK)
			.append("avg no. deletions: " + averages[1] + LINEBREAK)
			.append("avg no. reconciliations: " + averages[3] + LINEBREAK)
			.append("avg no. CMR iterations: " + averages[2] + LINEBREAK)
			.append("avg no. revisions: " + averages[4] + LINEBREAK)
			.append(new ModelLanguageUsageAnalysis(modelsToAnalyze, AnalysisHelper.getModelLanguageMetrics()).toResultCSVString());
		}
	}

	/**
	 * Since the resulting cluster has a quite complicated structure,
	 * we just want to deal with the real clusters that contain our
	 * models. This is why this method extracts these.
	 * @param clusters the initial clusters to find our groups in
	 * @return a collection of groups, that contain the process models
	 */
	private static Collection<Collection<ProcessEvolutionModel>> findDistinctModelGroups(
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

	/**
	 * write the results to file
	 * @param clusterResultStringBuilder 
	 * @throws IOException
	 */
	private static void writeResults(StringBuilder clusterResultStringBuilder) throws IOException {
		File resultFile = new File("");
		BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile.getAbsolutePath() + FILE_PATH, true));
		writer.append(clusterResultStringBuilder.toString());
		writer.close();
	}
}
