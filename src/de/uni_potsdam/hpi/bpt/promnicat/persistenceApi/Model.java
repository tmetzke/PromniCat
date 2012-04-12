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
import java.util.HashSet;
import java.util.logging.Logger;



/**
 * A model represents a collection of {@link Revision} that again store a collection of {@link Representation}.
 * All of them share the same title and origin. 
 * For performance reasons, sometimes not all revisions are loaded from the database. For this case 
 * the nrOfRevisionsInDb tell the total number of revisions found in the database.
 * 
 * @author Andrina Mascher
 *
 */
public class Model extends AbstractPojo {

	/**
	 * id used for identification of {@link Model} 
	 * is independant of database id, needs to be managed by user
	 */
	private String importedId = "";
	
	private String title = "";
	private String origin = "";
	private HashSet<Revision> revisions = new HashSet<Revision>();
	private boolean completelyLoaded; //in case only one Revision is loaded with again only one Representation
	
	private final static Logger logger = Logger.getLogger(Model.class.getName());


	public Model() {
		completelyLoaded = true;
	}
	
	public Model(String title, String origin) {
		super();
		this.title = title;
		this.origin = origin;
		completelyLoaded = true;
	}

	public Model(String title, String origin, String id) {
		this(title, origin);
		this.importedId = id;
	}

	@Override
	public String toString() {
		return "Model [dbId=" + getDbId() 
						+ ", id=" + getImportedId()
						+ ", title=" + getTitle()
						+ ", origin=" + getOrigin()
						+ ", #revisions=" + getNrOfRevisions()
						+ ", #representations=" + getNrOfRepresentations()
						+ ", completelyLoaded=" + completelyLoaded
						+ "]";
	}
	
	/**
	 * Resets all values in this model by the values found in the database for this dbId.
	 * Loads all revisions from the database and all their representations.
	 * 
	 * @param papi the IPersistanceApi to use
	 * @return the new Model
	 */
	public Model loadCompleteModel(IPersistenceApi papi) {
		if(!hasDbId()) {
			logger.info("This model has no database id yet, save it in database first to create this id");
			return this;
		}
		Model newM = papi.loadCompleteModelWithDbId(getDbId());
		this.title = newM.getTitle();
		this.origin = newM.getOrigin();
		setAndConnectRevisions(newM.getRevisions());
		setCompletelyLoaded(true);
		return this;
	}
	
	public String toStringExtended() {
		String s = toString();
		try {
			for(Revision rev : getRevisions()) {
				s +="\n\t" + rev.toString();
				for(Representation rep : rev.getRepresentations()) {
					s +="\n\t\t" + rep.toString();
				}
			}
		} catch(ClassCastException e) {
			s += "\nModel is not fully loaded from database, call loadCompleteModel()";
		}
		return s;
	}

	public int getNrOfRepresentations() {
		int i = 0;
		for(Revision r : revisions) {
			i += r.getNrOfRepresentations();
		}
		return i;
	}
	
	public int getNrOfRevisions() {
		return revisions.size();
	}

	public HashSet<Revision> getRevisions() {
		return revisions;
	}

	/**
	 * Does not set a latestRevision, it will be null afterwards. Call connectLatestRevision.
	 * @param revisions
	 */
	public void setAndConnectRevisions(Collection<Revision> revisions) {
		this.revisions.clear();
		if(revisions == null) {
			return;
		}
		for(Revision rev : revisions) {
			this.revisions.add(rev);
			connectRevision(rev);
		}
	}
	
	public void connectRevision(Revision revision) {
		if(revision != null) {
			revision.model = this;
			this.revisions.add(revision);
		}
	}
	
	public void connectLatestRevision(Revision revision) {
		if(revision != null) {
			revision.model = this;
			this.revisions.add(revision);
			setLatestRevision(revision);
		}
	}

	private void setLatestRevision(Revision revision) {
		if(getLatestRevision() != null) {
			getLatestRevision().setLatestRevision(false);
		}
		revision.setLatestRevision(true);
	}
	
	public Revision getLatestRevision() {
		for(Revision r : getRevisions()) {
			if(r.isLatestRevision()) {
				return r;
			}
		}
		return null;
	}

	/**
	 * @return the id used for {@link Model} identification
	 */
	public String getImportedId() {
		return importedId;
	}

	/**
	 * Set the id used to identify a {@link Model}
	 * @param id the id to set
	 */
	public void setImportedId(String id) {
		this.importedId = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	/**
	 * @return the completelyLoaded
	 */
	public boolean isCompletelyLoaded() {
		return completelyLoaded;
	}	
	
	public void setCompletelyLoaded(boolean completelyLoaded) {
		this.completelyLoaded = completelyLoaded;
	}
}
