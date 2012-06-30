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

import java.util.Collection;
import java.util.HashMap;
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
public class LazyRevisionsAnalysis extends AbstractAnalysis {

	private boolean includeSubprocesses;
	private Collection<AnalysisConstant> metrics;
	private int numberOfAlteringRevisions = 0;
	private int numberOfRevisions = 0;

	/**
	 * @param modelsToAnalyze
	 * @param collection 
	 */
	public LazyRevisionsAnalysis(
			Map<String, AnalysisProcessModel> modelsToAnalyze, 
			Map<String, AnalysisProcessModel> analyzedModels,
			boolean includeSubpreocesses, Collection<AnalysisConstant> metrics) {
		super(modelsToAnalyze, analyzedModels);
		this.includeSubprocesses = includeSubpreocesses;
		this.metrics = metrics;
	}
	
	public LazyRevisionsAnalysis(
			Map<String, AnalysisProcessModel> modelsToAnalyze, 
			boolean includeSubpreocesses, Collection<AnalysisConstant> metrics) {
		this(modelsToAnalyze, null, includeSubpreocesses, metrics);
	}

	@Override
	protected void performAnalysis() {
		Map<String, AnalysisProcessModel> modelsWithAlteringRevisions = new HashMap<String, AnalysisProcessModel>();

		// analyze additions and deletions and find altering revisions
		IAnalysis addsDeletes = AnalysisHelper.analyzeAdditionsAndDeletions(modelsToAnalyze, includeSubprocesses);
		analyzedModels = addsDeletes.getAnalyzedModels();
		for (AnalysisProcessModel model : analyzedModels.values()) {
			numberOfRevisions += model.getRevisions().size();
			for (AnalysisModelRevision revision : model.getRevisions().values())
				for (AnalysisConstant metric : metrics)
					if (!revision.get(metric.getDescription() + AnalysisConstant.ADDITIONS.getDescription()).equals(new Double(0))
							|| !revision.get(metric.getDescription() + AnalysisConstant.ADDITIONS.getDescription()).equals(new Double(0))) {
						if (!modelsWithAlteringRevisions.containsKey(model.getName()))
							modelsWithAlteringRevisions.put(model.getName(), new AnalysisProcessModel(model.getName()));
						modelsWithAlteringRevisions.get(model.getName()).add(revision);
						break;
					}
		}
		
		// if revision not already present as altering revision,
		// add it to altering revision if it is one concerning layout changes
		IAnalysis layoutChanges = AnalysisHelper.analyzeElementMovements(modelsToAnalyze);
		Map<String, AnalysisProcessModel> newLayoutModels = layoutChanges.getAnalyzedModels();
		for (AnalysisProcessModel model : newLayoutModels.values())
			for (AnalysisModelRevision revision : model.getRevisions().values())
				if (modelsWithAlteringRevisions.containsKey(model.getName()))
					if (!modelsWithAlteringRevisions.get(model.getName()).getRevisions().containsKey(revision.getRevisionNumber()))
						if (!revision.get(AnalysisConstant.NEW_LAYOUT.getDescription()).equals(new Double(0)))
							modelsWithAlteringRevisions.get(model.getName()).add(revision);
		
		for (AnalysisProcessModel model : modelsWithAlteringRevisions.values()) {
			AnalysisProcessModel analyzedModel = analyzedModels.get(model.getName());
			if (analyzedModel != null) {
					analyzedModel.setNumberOfAlteringRevisions(model.getRevisions().size());
			}
			numberOfAlteringRevisions += model.getRevisions().size();
		}
	}

	@Override
	protected String getResultCSVString() {
		return new StringBuilder()
			.append(AnalysisConstant.NUM_REVISIONS.getDescription() + CSV_ITEMSEPARATOR)
			.append(AnalysisConstant.ALTERING_REVISIONS.getDescription() + CSV_ITEMSEPARATOR)
			.append(AnalysisConstant.UNALTERING_REVISIONS.getDescription() +CSV_ITEMSEPARATOR)
			.append("\n")
			.append(numberOfRevisions +CSV_ITEMSEPARATOR)
			.append(numberOfAlteringRevisions +CSV_ITEMSEPARATOR)
			.append(numberOfRevisions - numberOfAlteringRevisions +CSV_ITEMSEPARATOR)
			.toString();
	}

}
