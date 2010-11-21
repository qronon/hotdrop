package org.qrone.r7.appengine;

import java.util.Hashtable;
import java.util.Map;

import org.mozilla.javascript.Scriptable;
import org.qrone.kvs.KVSService;
import org.qrone.kvs.KVSTable;
import org.qrone.r7.script.AbstractScriptable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

public class AppEngineKVSService extends AbstractScriptable implements KVSService{
	private DatastoreService db
		= DatastoreServiceFactory.getDatastoreService();
	private Map<String, AppEngineKVSTable> map = new Hashtable<String, AppEngineKVSTable>();
	
	public AppEngineKVSService(){
	}
	
	@Override
	public KVSTable getCollection(String name) {
		AppEngineKVSTable t = map.get(name);
		if(t == null){
			t = new AppEngineKVSTable(db, name);
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
