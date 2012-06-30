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

import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisProcessModel;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.abstractAnalyses.metricAnalyses.CMRIterationsAnalysis;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.api.IAnalysis;

/**
 * @author Tobias Metzke
 *
 */
public class CMRAnalysis extends AbstractAnalysis {

	private static final int MIDDLE = 3;
	private static final int LOW = 1;
	
	private int numberOfHighIterationNumbers, numberOfMiddleIterationNumbers, numberOfLowIterationNumbers;
	
	public CMRAnalysis(Map<String, AnalysisProcessModel> modelsToAnalyze,
			Map<String, AnalysisProcessModel> analyzedModels) {
		super(modelsToAnalyze, analyzedModels);
		numberOfHighIterationNumbers = numberOfLowIterationNumbers = numberOfMiddleIterationNumbers = 0;
	}

	/**
	 * @param modelsToAnalyze
	 */
	public CMRAnalysis(
			Map<String, AnalysisProcessModel> modelsToAnalyze) {
		this(modelsToAnalyze, null);
	}

	@Override
	protected void performAnalysis() {
		IAnalysis iterationsAnalysis = new CMRIterationsAnalysis(modelsToAnalyze);
		for (AnalysisProcessModel model : iterationsAnalysis.getAnalyzedModels().values()) {
			int iterations = model.getCMRIterations();
			if (iterations <= LOW)
				numberOfLowIterationNumbers++;
			else if (iterations <= MIDDLE)
				numberOfMiddleIterationNumbers++;
			else
				numberOfHighIterationNumbers++;
		}
		
	}

	@Override
	protected String getResultCSVString() {
		return new StringBuilder()
			.append("iterations > " + MIDDLE)
			.append(CSV_ITEMSEPARATOR + "iterations > " + LOW)
			.append(CSV_ITEMSEPARATOR + "iterations <= " + LOW)
			.append("\n")
			.append(numberOfHighIterationNumbers)
			.append(CSV_ITEMSEPARATOR + numberOfMiddleIterationNumbers)
			.append(CSV_ITEMSEPARATOR + numberOfLowIterationNumbers)
			.toString();
	}

}
