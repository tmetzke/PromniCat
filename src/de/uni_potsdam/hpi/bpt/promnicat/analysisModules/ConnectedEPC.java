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
package de.uni_potsdam.hpi.bpt.promnicat.analysisModules;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

import org.jbpt.pm.ProcessModel;

import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.DbFilterConfig;
import de.uni_potsdam.hpi.bpt.promnicat.util.Constants;
import de.uni_potsdam.hpi.bpt.promnicat.util.IllegalTypeException;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.IUnitChainBuilder;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.UnitChain;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.UnitChainBuilder;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.IUnitDataJbpt;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.UnitDataJbpt;

/**
 * Represents following scenario:
 * Find all diagrams from database that
 * are from BPMAI
 * and are written in EPC.
 * Check all these diagrams for connectedness.
 * 
 * @author Andrina Mascher, Tobias Hoppe, Cindy Fähnrich
 *
 */
public class ConnectedEPC {
	
	private final static Logger logger = Logger.getLogger(ConnectedEPC.class.getName());
	
	public static void main(String[] args) throws IllegalTypeException, IOException {
		
		//configure for time measurement
		long startTime = System.currentTimeMillis();
		
		//build up chain
		IUnitChainBuilder chainBuilder = new UnitChainBuilder("configuration(full).properties", Constants.DATABASE_TYPES.ORIENT_DB, UnitDataJbpt.class);
		buildUpUnitChain(chainBuilder);
		
		logger.info(chainBuilder.getChain().toString());
		
		//run chain
		@SuppressWarnings("unchecked")
		Collection<IUnitDataJbpt<Object> > result = (Collection<IUnitDataJbpt<Object>>) chainBuilder.getChain().execute();
		
		//print result
		printResult(result);
		
		//finish time measurement
		long time = System.currentTimeMillis() - startTime;
		System.out.println("Time needed: " + (time / 1000 / 60) + " min " + (time / 1000 % 60) + " sec \n\n");
	}
	
	/**
	 * Configures and builds up the {@link UnitChain} by invoking the corresponding builder methods.
	 * @param chainBuilder
	 * @throws IllegalTypeException 
	 */
	private static void buildUpUnitChain(IUnitChainBuilder chainBuilder) throws IllegalTypeException {
		//build db query
		DbFilterConfig dbFilter = new DbFilterConfig();
		dbFilter.addOrigin(Constants.ORIGINS.BPMAI);
		dbFilter.addNotation(Constants.NOTATIONS.EPC);
		dbFilter.addFormat(Constants.FORMATS.BPMAI_JSON);
		chainBuilder.addDbFilterConfig(dbFilter);
		//transform to jbpt and check for connectedness
		chainBuilder.createBpmaiJsonToJbptUnit();
		chainBuilder.createConnectednessFilterUnit();
		
		//collect results
		chainBuilder.createSimpleCollectorUnit();
	}
	
	/**
	 * Iterates through the results and prints whether the process models are connected or not and the amount
	 * of entries and exists. 
	 * @param results from the execution of the {@link UnitChain}
	 */
	private static void printResult(Collection<IUnitDataJbpt<Object> > results){
		
		for (IUnitDataJbpt<Object> item : results){
			ProcessModel result = (ProcessModel) item.getValue();
			if (result != null) {
				logger.info("Diagram is connected.");
				//log number of entries and exits
				logger.info("Diagram has " + result.getEntries().size() + " entries and " + result.getExits().size() + " exits\n.");
			} else {
				logger.info("Diagram is not connected.");
			}
		}
	}
}
