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

import java.util.Map;

import de.uni_potsdam.hpi.bpt.promnicat.processEvolution.AnalysisConstants;
import de.uni_potsdam.hpi.bpt.promnicat.processEvolution.ProcessEvolutionModel;
import de.uni_potsdam.hpi.bpt.promnicat.processEvolution.ProcessEvolutionModelRevision;
import de.uni_potsdam.hpi.bpt.promnicat.processEvolution.api.IAnalysis;

/**
 * @author Tobi
 *
 */
/**
 * @author Tobi
 *
 */
public class CMRIterationsAnalysis extends AbstractMetricsAnalysis {

	public CMRIterationsAnalysis(
			Map<String, ProcessEvolutionModel> modelsToAnalyze,
			Map<String, ProcessEvolutionModel> analyzedModels) {
		super(modelsToAnalyze, analyzedModels);
	}
	

	/**
	 * @param modelsToAnalyze
	 */
	public CMRIterationsAnalysis(
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
		IAnalysis cmrOccurrencesAnalysis = new CMROccurrencesAnalysis(modelsToAnalyze);
		analyzedModels = cmrOccurrencesAnalysis.getAnalyzedModels();
		for (ProcessEvolutionModel model : analyzedModels.values()) {
			AnalysisConstants lastCMRElement = AnalysisConstants.NONE;
			int iterations = 0;
			for (ProcessEvolutionModelRevision revision : model.getRevisions().values()) {
				if (isNewIteration(lastCMRElement, revision)) {
					setToModeling(lastCMRElement);
					if (isLastRevision(revision, model))
						iterations++;
				} else if (isConcludedIteration(lastCMRElement, revision)) {
					iterations++;
					setToReconciliation(lastCMRElement);
				}
			}
			model.setCMRIterations(iterations);
		}
	}

	/**
	 * @param lastCMRElement
	 * @param revision
	 * @return
	 */
	private boolean isNewIteration(AnalysisConstants lastCMRElement,
			ProcessEvolutionModelRevision revision) {
		return (containsModeling(revision) && !containsReconciliation(revision)) ||
		(containsModeling(revision) & containsReconciliation(revision) & lastCMRElement.equals(AnalysisConstants.RECONCILIATION));
	}

	/**
	 * @param lastCMRElement
	 * @param revision
	 * @return
	 */
	private boolean isConcludedIteration(AnalysisConstants lastCMRElement,
			ProcessEvolutionModelRevision revision) {
		return (!lastCMRElement.equals(AnalysisConstants.RECONCILIATION) && containsReconciliation(revision) && containsModeling(revision)) ||
				(containsReconciliation(revision) && !containsModeling(revision) && lastCMRElement.equals(AnalysisConstants.MODELING));
	}

	/**
	 * @param revision
	 * @return
	 */
	private boolean containsReconciliation(ProcessEvolutionModelRevision revision) {
		return revision.getMetrics().containsKey(AnalysisConstants.RECONCILIATION.getDescription());
	}

	/**
	 * @param revision
	 * @return
	 */
	private boolean containsModeling(ProcessEvolutionModelRevision revision) {
		return revision.getMetrics().containsKey(AnalysisConstants.MODELING.getDescription());
	}

	private void setToModeling(AnalysisConstants lastCMRElement) {
		lastCMRElement = AnalysisConstants.MODELING;
	}

	private void setToReconciliation(AnalysisConstants lastCMRElement) {
		lastCMRElement = AnalysisConstants.RECONCILIATION;
	}

	private boolean isLastRevision(ProcessEvolutionModelRevision revision, ProcessEvolutionModel model) {
		return model.getRevisions().lastKey() == revision.getRevisionNumber();
	}

}
