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
package de.uni_potsdam.hpi.bpt.promnicat.analysisModules.metrics;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jbpt.pm.ProcessModel;

import de.uni_potsdam.hpi.bpt.promnicat.parser.BpmnParser;
import de.uni_potsdam.hpi.bpt.promnicat.parser.EpcParser;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.DbFilterConfig;
import de.uni_potsdam.hpi.bpt.promnicat.util.Constants;
import de.uni_potsdam.hpi.bpt.promnicat.util.IllegalTypeException;
import de.uni_potsdam.hpi.bpt.promnicat.util.ProcessMetricConstants.METRICS;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisConstant;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisHelper;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisModelRevision;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisProcessModel;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.WriterHelper;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.IFlexibleUnitChainBuilder;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.IUnitChainBuilder;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.UnitChainBuilder;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.IUnitDataProcessMetrics;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.UnitDataProcessMetrics;

/**
 * Analysis module to calculate metrics from all {@link ProcessModel}s of a given database.
 * Furthermore the metrics analysis is analyzed in further steps to gain more high-level
 * insights into the given model collection.
 * 
 * @author Tobias Metzke
 *
 */
public class IndividualProcessMetrics {
	
	private static final boolean HANDLE_SUB_PROCESSES = true;

	/**
	 * path of the model metrics result file
	 */
	private static final String MODEL_RESULT_FILE_PATH = 
			new File("").getAbsolutePath() + "/resources/analysis/new.model_results.csv";
	
	/**
	 * path of the metrics analysis result file, that analyzes the model metrics results
	 */
	private static final String METRICS_ANALYSIS_ABSOLUTE_RESULT_FILE_PATH = 
			new File("").getAbsolutePath() + "/resources/analysis/new.model_results_absolute_analyzed.csv";
	
	/**
	 * path of the metrics analysis result file, that analyzes the model metrics results
	 */
	private static final String METRICS_ANALYSIS_RELATIVE_RESULT_FILE_PATH = 
			new File("").getAbsolutePath() + "/resources/analysis/new.model_results_relative_analyzed.csv";
	
	/**
	 * path of the metrics analysis analysis result file, that analyzes the metrics analysis
	 */
	private static final String ANALYSIS_ANALYSIS_RESULT_FILE_PATH = 
			new File("").getAbsolutePath() + "/resources/analysis/new.analysis_results_analyzed.csv";
	
	private static final String ADD_DELETE_RESULT_FILE_PATH = 
			new File("").getAbsolutePath() + "/resources/analysis/new.add_delete_results.csv";
	
	private static final String MODEL_LANGUAGE_RESULT_FILE_PATH = 
			new File("").getAbsolutePath() + "/resources/analysis/new.model_language_results.csv";
	
	private static final Logger logger = Logger.getLogger(ProcessMetrics.class.getName());
	
	/**
	 * flag to decide whether to use the full database or just a small test subset
	 */
	private static final boolean useFullDB = true;

	/**
	 * the collection of metrics all model revisions will be analyzed by
	 */
	private static Collection<METRICS> processModelMetrics;
	
	/**
	 * @param args
	 * @throws IllegalTypeException 
	 * @throws IllegalArgumentException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IllegalArgumentException, IllegalTypeException, IOException {

		long startTime = System.currentTimeMillis();
		
		IUnitChainBuilder chainBuilder = buildUpUnitChain(useFullDB);		
		logger.info(chainBuilder.getChain().toString() + "\n");		
		
		// parser should not log parsing errors
		Logger epcParserLog = Logger.getLogger(EpcParser.class.getName());
		epcParserLog.setLevel(Level.SEVERE);
		Logger bpmnParserLog = Logger.getLogger(BpmnParser.class.getName());
		bpmnParserLog.setLevel(Level.SEVERE);

		//run chain
		@SuppressWarnings("unchecked")
		Collection<IUnitDataProcessMetrics<Object> > result = 
			(Collection<IUnitDataProcessMetrics<Object>>) chainBuilder.getChain().execute();
		
		Map<String,AnalysisProcessModel> models = buildUpInternalDataStructure(result);

		performAnalyses(models);
		long time = System.currentTimeMillis() - startTime;
		logger.info("Finished Analysis in " + (time / 1000 / 60) + " min " + (time / 1000 % 60) + " sec \n\n");
	}

	/**
	 * Create an new unit chain builder and builds up
	 * a chain to get the metrics of the {@link ProcessModel}s from the given database.
	 * @param useFullDB use full data set or if set to <code>false</code> only a small excerpt
	 * @param metric 
	 * @return the builder with the created chain
	 * @throws IOException if the given configuration file path could not be found
	 * @throws IllegalTypeException if the units of the chain have incompatible input/output types
	 */
	private static IUnitChainBuilder buildUpUnitChain(boolean useFullDB) throws IOException, IllegalTypeException {
		IFlexibleUnitChainBuilder chainBuilder = null;

		if (useFullDB){
			chainBuilder = new UnitChainBuilder("configuration(full).properties", Constants.DATABASE_TYPES.ORIENT_DB, UnitDataProcessMetrics.class);
		} else {
			chainBuilder = new UnitChainBuilder("", Constants.DATABASE_TYPES.ORIENT_DB, UnitDataProcessMetrics.class);
		}
		//build db query
		DbFilterConfig dbFilter = new DbFilterConfig();
		dbFilter.addOrigin(Constants.ORIGINS.BPMAI);		
		dbFilter.addFormat(Constants.FORMATS.BPMAI_JSON);
		dbFilter.addNotation(Constants.NOTATIONS.BPMN1_1);
		dbFilter.addNotation(Constants.NOTATIONS.BPMN2_0);
//		dbFilter.addNotation(Constants.NOTATIONS.EPC);
//		dbFilter.setLatestRevisionsOnly(true);
		chainBuilder.addDbFilterConfig(dbFilter);
		//transform to jBPT and calculate metrics
		chainBuilder.createBpmaiJsonToJbptUnit(false);
		chainBuilder.createProcessModelMetricsCalulatorUnit(getProcessModelMetrics(), HANDLE_SUB_PROCESSES);
		
		//collect results
		chainBuilder.createSimpleCollectorUnit();
		return chainBuilder;
	}

	/**
	 * puts in place the internal data structure where:
	 * 1. each model holds all its revisions
	 * 2. each revision holds the values of all metrics of this revision
	 * 
	 * @param resultSet the collection of results of the metric analysis
	 * @return the internal data structure representation
	 */
	private static Map<String, AnalysisProcessModel> buildUpInternalDataStructure(
			Collection<IUnitDataProcessMetrics<Object>> resultSet) {
		
		Map<String, AnalysisProcessModel> models = new HashMap<>();
		
		for (IUnitDataProcessMetrics<Object> resultItem : resultSet){
			String modelPathWithRevision = resultItem.getModelPath();
			int revisionStringIndex = modelPathWithRevision.indexOf("_rev");
			String processFolder = "2011-04-19_signavio_academic_processes";
			String modelPath = modelPathWithRevision.substring(
					modelPathWithRevision.indexOf(processFolder) + processFolder.length(),revisionStringIndex);
			int revisionNumber = Integer.valueOf(
					modelPathWithRevision.substring(revisionStringIndex + 4, modelPathWithRevision.indexOf(".json")));
			
			// new model to be analyzed, so add it to the map of models
			if (!models.containsKey(modelPath)) {
				AnalysisProcessModel model = new AnalysisProcessModel(modelPath);
				models.put(model.getName(), model);
			}
			
			// add the revision to its model
			AnalysisModelRevision revision = new AnalysisModelRevision(revisionNumber);
			for (METRICS metric : getProcessModelMetrics())
				revision.add(metric, metric.getAttribute(resultItem));
			revision.addProcessModel((ProcessModel)resultItem.getValue());
			models.get(modelPath).add(revision);
		}
		
		return models;
	}
	
	private static Collection<METRICS> getProcessModelMetrics() {
		if (processModelMetrics == null)
			processModelMetrics = AnalysisHelper.getProcessModelMetrics();
		return processModelMetrics;
	}

	/**
	 * @param models
	 * @throws IOException
	 */
	private static void performAnalyses(Map<String, AnalysisProcessModel> models)
			throws IOException {
		// metrics results
		WriterHelper.writeToFile(MODEL_RESULT_FILE_PATH, models);
		logger.info("Wrote model metrics results to " + MODEL_RESULT_FILE_PATH + "\n");
		
		// difference analysis with relative differences
		Map<String, AnalysisProcessModel> analyzedModels = AnalysisHelper.analyzeMetrics(models, true);
		WriterHelper.writeToFile(METRICS_ANALYSIS_RELATIVE_RESULT_FILE_PATH, analyzedModels);
		logger.info("Wrote relative metrics analysis results to " + METRICS_ANALYSIS_RELATIVE_RESULT_FILE_PATH + "\n");
		
		// additions/deletions analysis with absolute numbers
		analyzedModels = AnalysisHelper.analyzeMetrics(models, false, AnalysisConstant.ADD_DELETE.getDescription(), String.valueOf(HANDLE_SUB_PROCESSES));
		WriterHelper.writeToFile(ADD_DELETE_RESULT_FILE_PATH, analyzedModels, AnalysisConstant.ADD_DELETE.getDescription());
		logger.info("Wrote addition/deletion analysis results to " + ADD_DELETE_RESULT_FILE_PATH + "\n");
		
		// difference analysis with absolute differences
		analyzedModels = AnalysisHelper.analyzeMetrics(models, false);
		WriterHelper.writeToFile(METRICS_ANALYSIS_ABSOLUTE_RESULT_FILE_PATH, analyzedModels);
		logger.info("Wrote absolute metrics analysis results to " + METRICS_ANALYSIS_ABSOLUTE_RESULT_FILE_PATH + "\n");
		
		// model language analysis
		analyzedModels = AnalysisHelper.modelLanguageAnalysis(models);
		WriterHelper.writeModelLanguage(MODEL_LANGUAGE_RESULT_FILE_PATH, analyzedModels);
		logger.info("Wrote analysis of model language to " + MODEL_LANGUAGE_RESULT_FILE_PATH + "\n");
		
		// high level analysis of model metrics
		Map<String, Integer> features = AnalysisHelper.highLevelAnalysis(models);
		WriterHelper.writeAnalysisWith(ANALYSIS_ANALYSIS_RESULT_FILE_PATH, features);
		logger.info("Wrote analysis of metrics analysis to " + ANALYSIS_ANALYSIS_RESULT_FILE_PATH + "\n");
	}
}
