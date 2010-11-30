package org.qrone.r7.appengine;

import org.mozilla.javascript.Scriptable;
import org.qrone.database.DatabaseCursor;
import org.qrone.r7.script.browser.Function;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.datastore.Query.SortDirection;

public class AppEngineDatastoreCursor implements DatabaseCursor{
	private DatastoreService db;
	private int limit = -1;
	private int current = 0;
	private Query query;
	private QueryResultIterable<Entity> iter;
	private Scriptable p;
	
	public AppEngineDatastoreCursor(DatastoreService db, Query query, Scriptable p ) {
		this.db = db;
		this.query = query;
		this.p = p;
	}
	
	private QueryResultIterator<Entity> iterator(){
		if(iter == null){
			PreparedQuery pq = db.prepare(query);
			iter = pq.asQueryResultIterable();
		}
		return iter.iterator();
	}
	

	@Override
	public void forEach(Function func) {
		for (; iterator().hasNext();) {
			func.call(iterator().next());
		}
	}

	@Override
	public boolean hasNext() {
		if(limit < 0){
			return iterator().hasNext();
		}else{
			if(limit <= 0){
				return false;
			}else{
				return iterator().hasNext();
			}
		}
	}

	@Override
	public DatabaseCursor limit(Number o) {
		limit = o.intValue();
		return this;
	}

	@Override
	public Object next() {
		return AppEngineUtil.fromEntity(nextRaw(), p);
	}

	public Entity nextRaw() {
		limit--;
		if(limit != 0)
			return iterator().next();
		else
			return null;
	}

	@Override
	public DatabaseCursor skip(Number o) {
		current += o.intValue();
		return this;
	}

	@Override
	public DatabaseCursor sort(Scriptable o) {
		Object[] ids = o.getIds();
		for (int i = 0; i < ids.length; i++) {
			if(ids[i] instanceof String){
				Object obj = o.get((String)ids[i], o);
				if(obj instanceof Boolean && ((Boolean)obj).booleanValue())
					query.addSort((String)ids[i], SortDirection.ASCENDING);
				else if(obj instanceof Number && ((Number)obj).intValue() > 0)
					query.addSort((String)ids[i], SortDirection.ASCENDING);
				else if(obj == null)
					query.addSort((String)ids[i], SortDirection.ASCENDING);
				else
					query.addSort((String)ids[i], SortDirection.DESCENDING);	
				return this;
			}
		}
		return this;
	}

}
