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
package de.uni_potsdam.hpi.bpt.promnicat.persistenceApi;

import java.util.Collection;
import java.util.List;
import java.util.Observer;

/**
 * This is the general interface for the database access.
 * 
 * @author Tobias Hoppe and Andrina Mascher
 *
 */
public interface IPersistenceApi {
	
	
	/**
	 * Opens a connection to the database.
	 * Also creates the database if it does not exist.
	 */
	public void openDb();
	/**
	 * close connection to the database
	 */
	public void closeDb();
	/**
	 * drop the entire database
	 */
	public void dropDb();
	/**
	 * @param aClass a class
	 * @return the number of instances of this class in the database
	 */
	public long countClass(Class<? extends AbstractPojo> aClass);
	/**
	 * @param sqlCommand to execute on the database to provide arbitrary access
	 */
	public void executeCommand(String sqlCommand);
	/**
	 * removes all references to previously loaded or saved objects,
	 * relies on Garbage Collection to free them.
	 */
	public void clearCache();
	
	//--------------------------------------------------------------------------------------------
	//---------------------------------- save and delete -----------------------------------------
	//--------------------------------------------------------------------------------------------
	
	/**
	 * Save any class in the database with all outgoing connections to other instances, if possible.
	 * @param pojo
	 * @return the database id of the saved pojo
	 */
	public String savePojo(AbstractPojo pojo);
	/**
	 * @param dbId the database id of the pojo to be deleted
	 * @return true if object was deleted, false if id was not found
	 */
	public boolean deletePojo(String dbId);
	/**
	 * @param aClass the class to delete all instances from
	 * @return true if deletion was successful
	 */
	public boolean deletePojos(Class<?> aClass);
	/**
	 * If one id was not found in the database, no other id is deleted
	 * and false is returned!
	 * 
	 * @param dbIds a collection of database ids to be deleted
	 * @return true if all ids were deleted
	 */
	public boolean deletePojos(Collection<String> dbIds);
	
	//--------------------------------------------------------------------------------------------
	//---------------------------------- load: 1 object -----------------------------------------
	//--------------------------------------------------------------------------------------------
	
	/**
	 * Loads lightweight {@link Representation} with connections to its {@link Revision}
	 * and {@link Model} without loading sibling our cousin {@link Representation}s.
	 * Returns <code>null</code> if id was not found.
	 * 
	 * @param dbId of the {@link Representation} to load
	 * @return a lightweight {@link Representation}.
	 */
	public Representation loadRepresentation(String dbId);
	/**
	 * Loads a {@link Model} with all outgoing connections to its {@link Revision}s
	 * and {@link Representation}s.
	 * The database id was created by the database at first import.
	 * 
	 * @param dbId the database id of the {@link Model} to load
	 * @return a {@link Model} or <code>null</code>
	 */
	public Model loadCompleteModelWithDbId(String dbId);
	/**
	 * Loads a {@link Model} with all outgoing connections to its {@link Revision}s
	 * and {@link Representation}s.
	 * The imported id was set by the user during import of the model.
	 * 
	 * @param id the imported id of the {@link Model} to load
	 * @return a {@link Model} or <code>null</code>
	 */
	public Model loadCompleteModelWithImportedId(String id);
	/**
	 * Loads a {@link AbstractPojo} and follows all outgoing connections recursively.
	 * 
	 * @param dbId the database id of the {@link AbstractPojo} to load
	 * @return a {@link AbstractPojo} or <code>null</code>
	 */
	public AbstractPojo loadPojo(String dbId);
	
	//--------------------------------------------------------------------------------------------
	//---------------------------------- load: n objects -----------------------------------------
	//--------------------------------------------------------------------------------------------

	/**
	 * Loads lightweight {@link Representation}s with connections to their {@link Revision}s
	 * and {@link Model}s without loading sibling our cousin {@link Representation}s.
	 * 
	 * @param config to define selection criteria in {@link Representation}, {@link Revision}, and {@link Model}
	 * @return a list of lightweight {@link Representation}s
	 */
	public List<Representation> loadRepresentations(DbFilterConfig config);
	/**
	 * Loads lightweight {@link Representation}s with connections to their {@link Revision}s
	 * and {@link Model}s without loading sibling our cousin {@link Representation}s.
	 * If one id was not found in the database, {@link IllegalArgumentException} is thrown.
	 * 
	 * @param dbIds a list of database ids
	 * @return a list of lightweight {@link Representation}s
	 */
	public List<Representation> loadRepresentations(Collection<String> dbIds);
	/**
	 * Loads a list of {@link AbstractPojo}s and follows all outgoing connections recursively.
	 * 
	 * @param aClass the class inherited from {@link AbstractPojo} to load all instances from
	 * @return objects found
	 */
	public List<AbstractPojo> loadPojos(Class<? extends AbstractPojo> aClass);
	/**
	 * Loads a list of {@link AbstractPojo}s and follows all outgoing connections recursively.
	 * If one id was not found in the database, {@link IllegalArgumentException} is thrown.
	 * 
	 * @param dbIds a list of database ids
	 * @return a list of {@link AbstractPojo}s
	 */
	public List<AbstractPojo> loadPojos(Collection<String> dbIds); 
	/**
	 * 
	 * @param noSql a NoSQL string to define all criteria to provide arbitrary access
	 * @return all found objects loaded recursively
	 */
	public List<Object> load(String noSql);
	
	//--------------------------------------------------------------------------------------------
	//---------------------------------- load asynchronously -------------------------------------
	//--------------------------------------------------------------------------------------------

	/**
	 * Loads lightweight {@link Representation}s with connections to their {@link Revision}s
	 * and {@link Model}s without loading sibling our cousin {@link Representation}s.
	 * 
	 * @param config to define selection criteria in {@link Representation}, {@link Revision}, and {@link Model}
	 * @param resultHandler which will be handed a lightweight {@link Representation} in each call of <code>update()</code>
	 */
	public void loadRepresentationsAsync(DbFilterConfig config, final Observer resultHandler);
	/**
	 * Loads lightweight {@link Representation}s with connections to their {@link Revision}s
	 * and {@link Model}s without loading sibling our cousin {@link Representation}s.
	 * If one id was not found in the database, {@link IllegalArgumentException} is thrown.
	 * 
	 * @param dbIds a list of database ids
	 * @param resultHandler which will be handed a lightweight {@link Representation} in each call of <code>update()</code>
	 */
	public void loadRepresentationsAsync(Collection<String> dbIds, final Observer resultHandler);
	/**
	 * Loads a list of {@link AbstractPojo}s and follows all outgoing connections recursively.
	 * 
	 * @param aClass the class inherited from {@link AbstractPojo} to load all instances from
	 * @param resultHandler which will be handed a {@link AbstractPojo} in each call of <code>update()</code>
	 */
	public void loadPojosAsync(Class<? extends AbstractPojo> aClass, final Observer resultHandler);
	/**
	 * Loads a list of {@link AbstractPojo}s and follows all outgoing connections recursively.
	 * If one id was not found in the database, {@link IllegalArgumentException} is thrown.
	 * 
	 * @param dbIds a list of database ids
	 * @param resultHandler which will be handed a {@link AbstractPojo} in each call of <code>update()</code>
	 */
	public void loadPojosAsync(Collection<String> dbIds, final Observer resultHandler);
	/**
	 * 
	 * @param noSql a NoSQL string to define all criteria to provide arbitrary access
	 * @param resultHandler which will be handed a Object result in each call of <code>update()</code>
	 */
	public void loadAsync(String noSql, final Observer resultHandler);
}
