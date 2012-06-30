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
package de.uni_potsdam.hpi.bpt.promnicat.util.analysis.abstractAnalyses.metricAnalyses;

import java.util.Map;

import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisConstant;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisModelRevision;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisProcessModel;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.api.IAnalysis;

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
			Map<String, AnalysisProcessModel> modelsToAnalyze,
			Map<String, AnalysisProcessModel> analyzedModels) {
		super(modelsToAnalyze, analyzedModels);
	}
	

	/**
	 * @param modelsToAnalyze
	 */
	public CMRIterationsAnalysis(
			Map<String, AnalysisProcessModel> modelsToAnalyze) {
		super(modelsToAnalyze);
	}

	@Override
	protected String addCSVHeader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String toCsvString(AnalysisProcessModel model) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void performAnalysis() {
		IAnalysis cmrOccurrencesAnalysis = new CMROccurrencesAnalysis(modelsToAnalyze);
		for (AnalysisProcessModel model : cmrOccurrencesAnalysis.getAnalyzedModels().values()) {
			AnalysisConstant lastCMRElement = AnalysisConstant.NONE;
			int iterations = 0;
			for (AnalysisModelRevision revision : model.getRevisions().values()) {
				if (isNewIteration(lastCMRElement, revision)) {
					setToModeling(lastCMRElement);
					if (isLastRevision(revision, model))
						iterations++;
				} else if (isConcludedIteration(lastCMRElement, revision)) {
					iterations++;
					setToReconciliation(lastCMRElement);
				}
			}
			AnalysisProcessModel newModel = new AnalysisProcessModel(model.getName());
			newModel.setCMRIterations(iterations);
			analyzedModels.put(model.getName(), newModel);
		}
	}

	/**
	 * @param lastCMRElement
	 * @param revision
	 * @return
	 */
	private boolean isNewIteration(AnalysisConstant lastCMRElement,
			AnalysisModelRevision revision) {
		return (containsModeling(revision) && !containsReconciliation(revision)) ||
		(containsModeling(revision) & containsReconciliation(revision) & lastCMRElement.equals(AnalysisConstant.RECONCILIATION));
	}

	/**
	 * @param lastCMRElement
	 * @param revision
	 * @return
	 */
	private boolean isConcludedIteration(AnalysisConstant lastCMRElement,
			AnalysisModelRevision revision) {
		return (!lastCMRElement.equals(AnalysisConstant.RECONCILIATION) && containsReconciliation(revision) && containsModeling(revision)) ||
				(containsReconciliation(revision) && !containsModeling(revision) && lastCMRElement.equals(AnalysisConstant.MODELING));
	}

	/**
	 * @param revision
	 * @return
	 */
	private boolean containsReconciliation(AnalysisModelRevision revision) {
		return revision.getMetrics().containsKey(AnalysisConstant.RECONCILIATION.getDescription());
	}

	/**
	 * @param revision
	 * @return
	 */
	private boolean containsModeling(AnalysisModelRevision revision) {
		return revision.getMetrics().containsKey(AnalysisConstant.MODELING.getDescription());
	}

	private void setToModeling(AnalysisConstant lastCMRElement) {
		lastCMRElement = AnalysisConstant.MODELING;
	}

	private void setToReconciliation(AnalysisConstant lastCMRElement) {
		lastCMRElement = AnalysisConstant.RECONCILIATION;
	}

	private boolean isLastRevision(AnalysisModelRevision revision, AnalysisProcessModel model) {
		return model.getRevisions().lastKey() == revision.getRevisionNumber();
	}

}
