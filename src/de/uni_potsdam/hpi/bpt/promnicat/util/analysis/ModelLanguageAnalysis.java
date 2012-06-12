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

import java.util.Map;

import de.uni_potsdam.hpi.bpt.promnicat.util.ProcessMetricConstants.METRICS;

/**
 * @author Tobias Metzke
 *
 */
public class ModelLanguageAnalysis extends MetricsAnalysis {

	public ModelLanguageAnalysis(Map<String, AnalysisProcessModel> modelsToAnalyze) {
		super(modelsToAnalyze);
	}

	@Override
	public void performAnalysis() {
		for (AnalysisProcessModel model : modelsToAnalyze.values()) {
			AnalysisProcessModel newModel = new AnalysisProcessModel(model.getName());
			for (AnalysisModelRevision revision : model.getRevisions().values()) {
				AnalysisModelRevision newRevision = new AnalysisModelRevision(revision.getRevisionNumber());
				if (revision.get(METRICS.NUM_NODES) > 0 || revision.get(METRICS.NUM_EDGES) > 0)
					newRevision.add(AnalysisConstant.CONTROL_FLOW.getDescription(), 1);
				if (revision.get(METRICS.NUM_DATA_NODES) > 0)
					newRevision.add(AnalysisConstant.DATA_FLOW.getDescription(), 1);
				if (revision.get(METRICS.NUM_ROLES) > 0)
					newRevision.add(AnalysisConstant.ORGANISATION.getDescription(), 1);
				newModel.add(newRevision);
			}
			analyzedModels.put(model.getName(), newModel);
		}
	}

}
