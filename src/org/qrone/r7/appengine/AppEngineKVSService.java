package org.qrone.r7.appengine;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.qrone.kvs.KeyValueStore;
import org.qrone.kvs.KeyValueStoreService;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class AppEngineKVSService implements KeyValueStoreService{
	private DatastoreService db = DatastoreServiceFactory.getDatastoreService();
	private MemcacheService mem = MemcacheServiceFactory.getMemcacheService();

	@Override
	public KeyValueStore getKeyValueStore(String collection) {
		return new AppEngineKeyValueStore(collection);
	}

	private class AppEngineKeyValueStore implements KeyValueStore{
		private String collection;
		public AppEngineKeyValueStore(String collection){
			this.collection = collection;
		}

		@Override
		public byte[] get(String key) {
			byte[] data = (byte[])mem.get(key);
			if(data != null)
				return data;
			try {
				Entity e = db.get(KeyFactory.createKey(collection, key));
				Blob b = (Blob)e.getProperty("value");
				if(b == null)
					return null;
				mem.put(key, b.getBytes());
				return data;
			} catch (EntityNotFoundException e) {
				return null;
			}
		}

		@Override
		public void set(String key, byte[] value) {
			set(key, value, false);
		}

		@Override
		public void set(String key, byte[] value, boolean weak) {
			mem.put(key, value);
			if(!weak){
				Entity e = new Entity(collection, key);
				e.setProperty("value", new Blob(value));
				db.put(e);
			}
		}
	}
}
