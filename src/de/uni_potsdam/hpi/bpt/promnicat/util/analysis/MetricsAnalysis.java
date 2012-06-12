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
import java.util.Map;

/**
 * @author Tobias Metzke
 * 
 */
public abstract class MetricsAnalysis {

	protected String name;
	protected Map<String, AnalysisProcessModel> analyzedModels = new HashMap<String, AnalysisProcessModel>();
	protected Map<String, AnalysisProcessModel> modelsToAnalyze;

	public MetricsAnalysis(Map<String, AnalysisProcessModel> modelsToAnalyze) {
		this.modelsToAnalyze = modelsToAnalyze;
	}

	/**
	 * executes the analysis method every subclass defines
	 */
	public abstract void performAnalysis();

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the models
	 */
	public Map<String, AnalysisProcessModel> getAnalyzedModels() {
		return analyzedModels;
	}

	/**
	 * @param models
	 *            the models to set
	 */
	public void setAnalyzedModels(Map<String, AnalysisProcessModel> models) {
		this.analyzedModels = models;
	}
}
