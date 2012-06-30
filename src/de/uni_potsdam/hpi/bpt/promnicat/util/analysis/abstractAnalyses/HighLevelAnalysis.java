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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisHelper;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisProcessModel;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.api.IAnalysis;

/**
 * @author Tobias Metzke
 *
 */
public class HighLevelAnalysis extends AbstractAnalysis {

	private Collection<IAnalysis> analyses = new ArrayList<IAnalysis>();
	private boolean includeSubprocesses;
	
	public HighLevelAnalysis(Map<String, AnalysisProcessModel> modelsToAnalyze, boolean includeSubprocesses) {
		super(modelsToAnalyze);
		this.includeSubprocesses = includeSubprocesses;
	}

	@Override
	protected void performAnalysis() {
		
		Collections.addAll(analyses, 
				new ModelGrowthAnalysis(modelsToAnalyze),
				new HighLowSameAnalysis(modelsToAnalyze, AnalysisHelper.getProcessModelMetrics()),
				new LazyRevisionsAnalysis(modelsToAnalyze, includeSubprocesses, AnalysisHelper.getIndividualMetrics()),
				new ModelLanguageUsageAnalysis(modelsToAnalyze, AnalysisHelper.getModelLanguageMetrics()),
				new CMRAnalysis(modelsToAnalyze));
	}

	@Override
	protected String getResultCSVString() {
		StringBuilder builder = new StringBuilder();
		for (IAnalysis analysis : analyses)
			builder
				.append(analysis.toResultCSVString())
				.append("\n\n");
		return builder.toString();
	}

}
