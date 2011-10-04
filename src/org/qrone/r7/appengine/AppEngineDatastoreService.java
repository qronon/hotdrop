package org.qrone.r7.appengine;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.Scriptable;
import org.qrone.database.DatabaseService;
import org.qrone.database.DatabaseTable;
import org.qrone.r7.script.AbstractScriptable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

public class AppEngineDatastoreService extends AbstractScriptable implements DatabaseService{
	private DatastoreService db
		= DatastoreServiceFactory.getDatastoreService();
	private Map<String, AppEngineDatastoreTable> map = new Hashtable<String, AppEngineDatastoreTable>();
	
	public AppEngineDatastoreService(){
	}
	
	@Override
	public DatabaseTable getCollection(String name) {
		AppEngineDatastoreTable t = map.get(name);
		if(t == null){
			t = new AppEngineDatastoreTable(db, name);
		}
		return t;
	}

	@Override
	public Object get(String arg0, Scriptable arg1) {
		return getCollection(arg0);
	}

	@Override
	public Object[] getIds() {
		return map.keySet().toArray(new Object[map.size()]);
	}

}
