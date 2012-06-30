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
import java.util.Set;
import java.util.logging.Logger;

import org.jbpt.pm.ProcessModel;

import de.uni_potsdam.hpi.bpt.promnicat.util.ProcessMetricConstants.METRICS;


/**
 * @author Tobias Metzke
 *
 */
public class AnalysisModelRevision {

	private static final Logger logger = Logger.getLogger(AnalysisModelRevision.class.getName());
	
	private static final String REVISIONS_NOT_EQUAL_EXCEPTION_MESSAGE = "Revisions do not have the same revision number and are therefore not equal, can not be merged.\n";

	private static final String OVERWRITE_METRIC_MESSAGE = "Overwriting metric: ";

	private HashMap<String, Double> metrics = new HashMap<>();
	
	private int revisionNumber;
	private ProcessModel processModel;
	
	public AnalysisModelRevision(int revisionNumber) {
		setRevisionNumber(revisionNumber);
	}

	public AnalysisModelRevision(AnalysisModelRevision revision1,
			AnalysisModelRevision revision2) {
		if (revision1.getRevisionNumber() != revision2.getRevisionNumber())
			throw new RuntimeException(REVISIONS_NOT_EQUAL_EXCEPTION_MESSAGE);
		else {
			for (String metricKey : revision1.getMetricKeys())
				add(metricKey, revision1.get(metricKey));
			for (String metricKey : revision2.getMetricKeys())
				add(metricKey, revision2.get(metricKey));
		}
	}

	public void add(METRICS metric, double metricValue) {
		if (metrics.containsKey(metric.name()))
			logger.warning(OVERWRITE_METRIC_MESSAGE + metric.name());
		metrics.put(metric.name(), metricValue);
	}
	
	public void add(String metricKey, double metricValue) {
		if (metrics.containsKey(metricKey))
				logger.warning(OVERWRITE_METRIC_MESSAGE + metricKey);
		metrics.put(metricKey, metricValue);
	}
	
	public Set<String> getMetricKeys() {
		return metrics.keySet();
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
