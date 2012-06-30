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

import java.util.Collection;
import java.util.Map;

import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisConstant;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisHelper;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisModelRevision;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisProcessModel;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.api.IAnalysis;

/**
 * @author Tobias Metzke
 *
 */
public class CMROccurrencesAnalysis extends AbstractMetricsAnalysis {

	/**
	 * @param modelsToAnalyze
	 */
	public CMROccurrencesAnalysis(
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
		IAnalysis addDeleteAnalysis = AnalysisHelper.analyzeAdditionsAndDeletions(modelsToAnalyze, true);
		IAnalysis movedElementsAnalysis = AnalysisHelper.analyzeElementMovements(modelsToAnalyze);
		modelsToAnalyze = meltAnalyses(addDeleteAnalysis,movedElementsAnalysis);
		for (AnalysisProcessModel model : modelsToAnalyze.values()) {
			AnalysisProcessModel newModel = new AnalysisProcessModel(model.getName());
			for (AnalysisModelRevision revision : model.getRevisions().values()) {
				AnalysisModelRevision newRevision = new AnalysisModelRevision(revision.getRevisionNumber());
				analyzeAddsDeletes(revision, newRevision);
				analyzeMovements(revision, newRevision);
				newModel.add(newRevision);
			}
			analyzedModels.put(model.getName(), newModel);
		}
	}

	private Map<String, AnalysisProcessModel> meltAnalyses(IAnalysis firstAnalysis,
			IAnalysis secondAnalysis) {
		Map<String, AnalysisProcessModel> firstModelMap = firstAnalysis.getAnalyzedModels();
		Map<String, AnalysisProcessModel> secondModelMap = secondAnalysis.getAnalyzedModels(); 
		for (AnalysisProcessModel model : firstModelMap.values()) {
			if (secondModelMap.containsKey(model.getName()))
				for(AnalysisModelRevision revision : model.getRevisions().values()) {
					AnalysisModelRevision secondRevision = secondModelMap.get(model.getName()).getRevisions().get(revision.getRevisionNumber());
					for (String metricKey : revision.getMetrics().keySet())
						secondRevision.add(metricKey, revision.get(metricKey));
				}
			else
				secondModelMap.put(model.getName(), model);
		}
		return secondModelMap;
	}

	private void analyzeAddsDeletes(AnalysisModelRevision revision,
			AnalysisModelRevision newRevision) {
		Collection<AnalysisConstant> metrics = AnalysisHelper.getIndividualMetrics();
		for (AnalysisConstant metric : metrics) {
			if(!revision.get(metric.getDescription() + AnalysisConstant.ADDITIONS.getDescription()).equals(new Double(0)) ||
					!revision.get(metric.getDescription() + AnalysisConstant.DELETIONS.getDescription()).equals(new Double(0)))
					newRevision.add(AnalysisConstant.MODELING.getDescription(), 1);
		}
	}

	private void analyzeMovements(AnalysisModelRevision revision,
			AnalysisModelRevision newRevision) {
		if (!revision.get(AnalysisConstant.NEW_LAYOUT.getDescription()).equals(new Double(0)))
			newRevision.add(AnalysisConstant.RECONCILIATION.getDescription(), 1);
	}

}
