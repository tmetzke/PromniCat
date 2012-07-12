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
package de.uni_potsdam.hpi.bpt.promnicat.processEvolution.abstractAnalyses.metricAnalyses;

import java.util.Collection;
import java.util.Map;

import de.uni_potsdam.hpi.bpt.promnicat.processEvolution.AnalysisConstants;
import de.uni_potsdam.hpi.bpt.promnicat.processEvolution.AnalysisHelper;
import de.uni_potsdam.hpi.bpt.promnicat.processEvolution.ProcessEvolutionModel;
import de.uni_potsdam.hpi.bpt.promnicat.processEvolution.ProcessEvolutionModelRevision;
import de.uni_potsdam.hpi.bpt.promnicat.processEvolution.api.IAnalysis;

/**
 * @author Tobias Metzke
 *
 */
public class CMROccurrencesAnalysis extends AbstractMetricsAnalysis {

	public CMROccurrencesAnalysis(
			Map<String, ProcessEvolutionModel> modelsToAnalyze,
			Map<String, ProcessEvolutionModel> analyzedModels) {
		super(modelsToAnalyze, analyzedModels);
	}
	

	/**
	 * @param modelsToAnalyze
	 */
	public CMROccurrencesAnalysis(
			Map<String, ProcessEvolutionModel> modelsToAnalyze) {
		super(modelsToAnalyze);
	}

	@Override
	protected String addCSVHeader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String toCsvString(ProcessEvolutionModel model) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void performAnalysis() {
		IAnalysis addDeleteAnalysis = AnalysisHelper.analyzeAdditionsAndDeletions(modelsToAnalyze, true);
		analyzedModels = addDeleteAnalysis.getAnalyzedModels();
		IAnalysis movedElementsAnalysis = AnalysisHelper.analyzeElementMovements(modelsToAnalyze, analyzedModels);
		analyzedModels = movedElementsAnalysis.getAnalyzedModels();
		for (ProcessEvolutionModel model : analyzedModels.values()) {
			for (ProcessEvolutionModelRevision revision : model.getRevisions().values()) {
				analyzeAddsDeletes(revision);
				analyzeMovements(revision);
			}
		}
		
		
	}

	private void analyzeAddsDeletes(ProcessEvolutionModelRevision revision) {
		Collection<AnalysisConstants> metrics = AnalysisHelper.getIndividualMetrics();
		for (AnalysisConstants metric : metrics) {
			if(!revision.get(metric.getDescription() + AnalysisConstants.ADDITIONS.getDescription()).equals(new Double(0)) ||
					!revision.get(metric.getDescription() + AnalysisConstants.DELETIONS.getDescription()).equals(new Double(0)))
					revision.add(AnalysisConstants.MODELING.getDescription(), 1);
		}
	}

	private void analyzeMovements(ProcessEvolutionModelRevision revision) {
		if (!revision.get(AnalysisConstants.NEW_LAYOUT.getDescription()).equals(new Double(0)))
			revision.add(AnalysisConstants.RECONCILIATION.getDescription(), 1);
	}

}
