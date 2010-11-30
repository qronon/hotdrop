package org.qrone.r7.appengine;

import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.Scriptable;
import org.qrone.database.DatabaseCursor;
import org.qrone.database.DatabaseTable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;

public class AppEngineDatastoreTable implements DatabaseTable{
	private DatastoreService db;
	private String collection;
	
	public AppEngineDatastoreTable(DatastoreService db, String collection) {
		this.collection = collection;
	}

	@Override
	public DatabaseCursor find() {
		return new AppEngineDatastoreCursor(db, new Query(collection), null);
	}
	
	@Override
	public DatabaseCursor find(Scriptable o){
		return find(o, null);
	}
	
	@Override
	public DatabaseCursor find(Scriptable o, Scriptable p) {
		Query q = new Query(collection);
		Object[] l = o.getIds();
		for (int i = 0; i < o.getIds().length; i++) {
			String key = null;
			Object value = null;
			if(l[i] instanceof Number){
				key = ((Number)l[i]).toString();
				value = o.get(((Number)l[i]).intValue(), o);
			}else{
				key = ((String)l[i]);
				value = o.get(((String)l[i]), o);
			}
			if(value instanceof String)
				queryToFilter(q, key, (String)value);
			else
				queryToFilter(q, key, value);
		}
		return new AppEngineDatastoreCursor(db, q, p);
	}
	
	public DatabaseCursor find(Scriptable o, Scriptable p, Number skip){
		return find(o, p).skip(skip);
	}
	
	public DatabaseCursor find(Scriptable o, Scriptable p, Number skip, Number limit){
		return find(o, p).skip(skip).limit(limit);
	}
	
	private void queryToFilter(Query q, String key, String value){
		q.addFilter(key, FilterOperator.EQUAL, value);
	}

	private void queryToFilter(Query q, String key, Object value){
		if(value instanceof Scriptable){
			Scriptable s = (Scriptable)value;
			
			queryToFilter(q, key, s, "$gt", FilterOperator.GREATER_THAN);
			queryToFilter(q, key, s, "$gte", FilterOperator.GREATER_THAN_OR_EQUAL);
			queryToFilter(q, key, s, "$lt", FilterOperator.LESS_THAN);
			queryToFilter(q, key, s, "$lte", FilterOperator.LESS_THAN_OR_EQUAL);
			queryToFilter(q, key, s, "$ne", FilterOperator.NOT_EQUAL);
		}
	}
	
	private void queryToFilter(Query q, String key, Scriptable s, String type, FilterOperator op){
		if(s.has(type,s)){
			Object v = AppEngineUtil.to(s.get(key, s));
			if(v != null)
				q.addFilter(key, op, v);
		}
	}

	@Override
	public void remove(Scriptable o) {
		AppEngineDatastoreCursor c = (AppEngineDatastoreCursor)find(o);
		List<Key> keylist = new ArrayList<Key>();
		while(c.hasNext()) {
			Entity e = c.nextRaw();
			keylist.add(e.getKey());
		}
		db.delete(keylist);
	}
	

	@Override
	public void save(Scriptable o) {
		db.put(AppEngineUtil.toEntity(collection, o));
	}

	@Override
	public void drop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insert(Scriptable o) {
		// TODO Auto-generated method stub
		
	}
	
	
}
