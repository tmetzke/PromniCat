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
package de.uni_potsdam.hpi.bpt.promnicat.importer.ibm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.logging.Logger;

import org.json.JSONException;
import org.springframework.util.FileCopyUtils;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import de.uni_potsdam.hpi.bpt.promnicat.importer.AbstractImporter;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.IPersistenceApi;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.Model;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.Representation;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.Revision;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.orientdbObj.PersistenceApiOrientDbObj;
import de.uni_potsdam.hpi.bpt.promnicat.util.Constants;

/**
 * This class is used to import the SAP reference model.
 * 
 * @author Tobias Hoppe
 * 
 */
public class IBMModelImporter extends AbstractImporter {
	private IPersistenceApi persistenceApi = null;
	private final static Logger logger = Logger.getLogger(IBMModelImporter.class.getName());
	/**
	 * Creates a new {@link IBMModelImporter} with the given {@link IPersistenceApi} used for database access.
	 * @param persistenceApi persistence API used by importer
	 */
	public IBMModelImporter(IPersistenceApi persistenceApi) {
		this.persistenceApi = persistenceApi;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_potsdam.hpi.bpt.promnicat.importer.IImporter#importModelsFrom(java.net.URI)
	 */
	@Override
	public void importModelsFrom(String modelDirectory) throws IOException, JSONException {
	    	persistenceApi.openDb();
	    	File rootDir = super.checkModelPath(modelDirectory, true);
	    	Collection<File> files = super.getFilesRecursivelyFromDir(rootDir);
	    	for(File file:files){
	    	    String id = file.getAbsolutePath().replace(rootDir.getAbsolutePath(), "");
	    	    //PromniCat does not support slashes in ids
	    	    id = id .replace(File.separator, "_");
		    Model model = this.persistenceApi.loadCompleteModelWithImportedId(id);
	    	if (model == null){
			//create and save new Model
			model = parseModel(file, id);
			persistenceApi.savePojo(model);

		} else {
		    logger.warning("Model already there");
		}
	    	}

	    	persistenceApi.closeDb();
	}

	private Model parseModel(File file, String id) {
	    Model model = new Model(file.getName(), Constants.ORIGIN_IBM, id);
	    Revision revision = new Revision(0);
	    revision.connectRepresentation(new Representation(Constants.FORMAT_XML, Constants.NOTATION_BPMN2_0, file ));
	    model.connectLatestRevision(revision); 
	    return model;
	}


}
