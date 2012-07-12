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
package de.uni_potsdam.hpi.bpt.promnicat.importer.sap_rm.test;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Collection;

import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_potsdam.hpi.bpt.promnicat.analysisModules.ConnectedEPC;
import de.uni_potsdam.hpi.bpt.promnicat.importer.sap_rm.SapReferenceModelImporter;
import de.uni_potsdam.hpi.bpt.promnicat.importer.test.ImporterTest;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.DbFilterConfig;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.IPersistenceApi;
import de.uni_potsdam.hpi.bpt.promnicat.util.ConfigurationParser;
import de.uni_potsdam.hpi.bpt.promnicat.util.Constants;
import de.uni_potsdam.hpi.bpt.promnicat.util.IllegalTypeException;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.IUnitChainBuilder;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.UnitChainBuilder;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.IUnitDataJbpt;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.UnitDataJbpt;

/**
 * test class for {@link SapReferenceModelImporter}
 * @author Tobias Hoppe
 *
 */
public class SapReferenceModelImporterTest {

	private static IPersistenceApi persistenceApi = null;

	@BeforeClass
	public static void init(){
		try {
			persistenceApi = new ConfigurationParser(Constants.TEST_DB_CONFIG_PATH).getDbInstance(Constants.DATABASE_TYPES.ORIENT_DB);
		} catch (IOException e) {
			fail("Unexpected exception occurred: " + e.getMessage());
		}
	}

	@AfterClass
	public static void tearDown(){
		persistenceApi.dropDb();
	}

	@Test
	public void testUnknownFileImport(){
		try {
			SapReferenceModelImporter modelImporter = new SapReferenceModelImporter(persistenceApi);
			modelImporter.importModelsFrom("unknown.file");
			fail("Expected exception has not been raised!");
		} catch (Exception e) {

		}
	}

	@Test
	public void importModels(){
		SapReferenceModelImporter modelImporter = new SapReferenceModelImporter(persistenceApi);
		String filePath = "resources/SAP_RM";
		ImporterTest.importModelsTwice(persistenceApi, modelImporter, filePath, 1, 1, 1);
		filePath = "resources/SAP_RM";
		ImporterTest.importModelsTwice(persistenceApi, modelImporter, filePath, 1, 1, 1);	

		persistenceApi.dropDb();
	}

	@Test
	public void parseModels() throws IllegalTypeException, IOException, JSONException{
		SapReferenceModelImporter modelImporter = new SapReferenceModelImporter(persistenceApi);
		String filePath = "resources/SAP_RM";
		modelImporter.importModelsFrom(filePath);
		//build up chain
		persistenceApi.openDb();
		IUnitChainBuilder chainBuilder = new UnitChainBuilder(persistenceApi, 3, UnitDataJbpt.class);
		//build db query
		DbFilterConfig dbFilter = new DbFilterConfig();
		chainBuilder.addDbFilterConfig(dbFilter);
		//transform to jbpt and check for connectedness
		chainBuilder.createBpmaiJsonToJbptUnit();
		chainBuilder.createConnectednessFilterUnit();

		//collect results
		chainBuilder.createSimpleCollectorUnit();


		//run chain
		@SuppressWarnings("unchecked")
		Collection<IUnitDataJbpt<Object> > result = (Collection<IUnitDataJbpt<Object>>) chainBuilder.getChain().execute();

		//print result
		ConnectedEPC.printResult(result);

		persistenceApi.closeDb();
		persistenceApi.dropDb();
	}
}
