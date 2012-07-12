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
import de.uni_potsdam.hpi.bpt.promnicat.analysisModules.metrics.ProcessEvolution;
import de.uni_potsdam.hpi.bpt.promnicat.processEvolution.ProcessEvolutionConstants.PROCESS_EVOLUTION_METRIC;

/**
 * @author Tobias Metzke
 *
 */
public class ClusteringThread extends Thread {

	private final HierarchicalProcessClusterer clusterer = new HierarchicalProcessClusterer(new EuclideanDistance());
	private StringBuilder clusterResultStringBuilder = new StringBuilder();
	private final String LINEBREAK = "\n";
	private Map<String, ProcessEvolutionModel> models;
	private final String FILE_PATH = "/resources/analysis/clustering/new.cluster_training_thread" + this.getId() + ".txt";
	
	/**
	 * 
	 */
	public ClusteringThread(Map<String, ProcessEvolutionModel> models) {
		this.models = models;
		start();
	}
	
	@Override
	public void run() {
		
		boolean done = false;
		while(!done) {
			try {
				ProcessEvolutionClusteringConfiguration configuration = ProcessEvolution.getNextConfiguration();
				if (configuration != null)
					doClustering(configuration);
				else
					done = true;
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * @throws IOException
	 */
	private void doClustering(ProcessEvolutionClusteringConfiguration configuration) throws IOException {
		Map<String, Double> numericAttributes = configuration.getNumericAttributes();
		FastVector extractedAttributes = getNumericAttributes(models, numericAttributes );
		String linkType = configuration.getLinkType();
		int numClusters = configuration.getNumClusters();
		setClusterAttributes(linkType, numClusters, extractedAttributes);
		ProcessInstances instances = getInstances(models, extractedAttributes);
		clusterResultStringBuilder
			.append(LINEBREAK)
			.append("Configuration:")
			.append("[");
		for (Object attribute : extractedAttributes.toArray())
			if (attribute != null && attribute instanceof Attribute) {
				Attribute realAttribute = (Attribute) attribute;
				clusterResultStringBuilder.append(realAttribute.name() + "(" + realAttribute.weight() + "),");
			}
		clusterResultStringBuilder 
			.append("]" + "," + linkType)
			.append("," + numClusters)
			.append(LINEBREAK);
		clusterModels(instances);
		System.out.println("Thread " + getId() + " done clustering.");
		File resultFile = new File("");
		BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile.getAbsolutePath() + FILE_PATH, true));
		writer.append(clusterResultStringBuilder.toString());
		writer.close();
		clusterResultStringBuilder = new StringBuilder();
	}
	
	private FastVector getNumericAttributes(Map<String, ProcessEvolutionModel> models, Map<String, Double> attributes) {
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
	private ProcessInstances getInstances(Map<String, ProcessEvolutionModel> models, FastVector numericAttributes) {
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
	private void setClusterAttributes(String linkType, int numClusters, FastVector numericAttributes) {
		clusterer.setLinkType(linkType);
		clusterer.setNumClusters(numClusters);
		clusterer.setAttributes(numericAttributes);
		clusterer.setDebug(true);
	}

	private void clusterModels(ProcessInstances instances) {
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
	private void analyzeClusters(ClusterTree<ProcessInstances> cluster) {
		Collection<Collection<ProcessEvolutionModel>> clustersWithModels = findDistinctClusters(cluster);
		int i= 1;
		for (Collection<ProcessEvolutionModel> models : clustersWithModels) {
			clusterResultStringBuilder
				.append(LINEBREAK + "Cluster " + i++ + ": (" + models.size() + " models)," + LINEBREAK);
			double[] averages = new double[4];
			for (ProcessEvolutionModel model : models) {
				averages[0] += model.getNumberOfAdditions();
				averages[1] += model.getNumberOfDeletions();
				averages[2] += model.getCMRIterations();
				averages[3] += model.getNumberOfMovedOrResizedElements();
			}
			for (int j = 0; j < averages.length; j++) {
				averages[j] /= models.size();
			}
			clusterResultStringBuilder
			.append("avg additions: " + averages[0] + LINEBREAK)
			.append("avg deletions: " + averages[1] + LINEBREAK)
			.append("avg CMR: " + averages[2] + LINEBREAK)
			.append("avg layout changes: " + averages[3] + LINEBREAK);
		}
	}

	/**
	 * @param clusters
	 * @return
	 */
	private Collection<Collection<ProcessEvolutionModel>> findDistinctClusters(
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
