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
package de.uni_potsdam.hpi.bpt.promnicat.util.analysis.abstractAnalyses;

import java.util.Map;

import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisConstant;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisProcessModel;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.api.IAnalysis;


/**
 * @author Tobias Metzke
 *
 */
public abstract class AbstractAnalysis implements IAnalysis {

	protected Map<String, AnalysisProcessModel> modelsToAnalyze;
	
	protected final String CSV_ITEMSEPARATOR = AnalysisConstant.ITEMSEPARATOR.getDescription();

	public AbstractAnalysis(Map<String, AnalysisProcessModel> modelsToAnalyze) {
		this.modelsToAnalyze = modelsToAnalyze;
	}
	
	@Override
	public String toResultCSVString() {
		performAnalysis();
		return getResultCSVString();
	}
	

	/**
	 * executes the analysis method every subclass defines itself
	 */
	protected abstract void performAnalysis();
	
	/**
	 * converts the analysis results into a proper CSV format 
	 * @return the CSV format of the results as String
	 */
	protected abstract String getResultCSVString();
}
