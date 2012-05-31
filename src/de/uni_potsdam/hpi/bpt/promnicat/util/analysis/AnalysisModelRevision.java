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
package de.uni_potsdam.hpi.bpt.promnicat.util.analysis;

import java.util.HashMap;

import org.jbpt.pm.ProcessModel;

import de.uni_potsdam.hpi.bpt.promnicat.util.ProcessMetricConstants.METRICS;


/**
 * @author Tobias Metzke
 *
 */
public class AnalysisModelRevision {

	private HashMap<String, Double> metrics = new HashMap<>();
	
	private int revisionNumber;
	private ProcessModel processModel;
	
	public AnalysisModelRevision(int revisionNumber) {
		setRevisionNumber(revisionNumber);
	}

	public void add(METRICS metric, double metricValue) {
		metrics.put(metric.name(), metricValue);
	}
	
	public void add(String metricKey, double metricValue) {
		metrics.put(metricKey, metricValue);
	}

	/**
	 * @return the revisionNumber
	 */
	public int getRevisionNumber() {
		return revisionNumber;
	}

	/**
	 * @param revisionNumber the revisionNumber to set
	 */
	public void setRevisionNumber(int revisionNumber) {
		this.revisionNumber = revisionNumber;
	}

	public Double get(METRICS metric) {
		return metrics.get(metric.name());
	}
	
	public Double get(String metricKey) {
		return metrics.get(metricKey);
	}

	public void addProcessModel(ProcessModel model) {
		this.processModel = model;
	}
	
	public ProcessModel getProcessModel() {
		return processModel;
	}
	
	public HashMap<String, Double> getMetrics() {
		return metrics;
	}

}
