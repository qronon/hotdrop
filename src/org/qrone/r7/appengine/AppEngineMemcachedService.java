package org.qrone.r7.appengine;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.qrone.memcached.MemcachedService;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class AppEngineMemcachedService implements MemcachedService {
	private MemcacheService service = MemcacheServiceFactory.getMemcacheService("qrone");

	@Override
	public void clearAll() {
		service.clearAll();
	}

	@Override
	public boolean contains(String key) {
		return service.contains(key);
	}

	@Override
	public boolean delete(String key) {
		return service.delete(key);
	}

	@Override
	public boolean delete(String key, long millisNoReAdd) {
		return service.delete(key, millisNoReAdd);
	}

	@Override
	public Set<String> deleteAll(Collection<String> keys) {
		return service.deleteAll(keys);
	}

	@Override
	public Set<String> deleteAll(Collection<String> keys, long millisNoReAdd) {
		return service.deleteAll(keys, millisNoReAdd);
	}

	@Override
	public Object get(String key) {
		return service.get(key);
	}

	@Override
	public Map<String, Object> getAll(Collection<String> keys) {
		return service.getAll(keys);
	}

	@Override
	public long increment(String key, long delta) {
		return service.increment(key, delta);
	}

	@Override
	public void put(String key, Object value) {
		service.put(key, value);
	}

	@Override
	public void put(String key, Object value, int ttlmillis) {
		service.put(key, value, Expiration.byDeltaMillis(ttlmillis));
	}

	@Override
	public void put(String key, Object value, Date expire) {
		service.put(key, value, Expiration.onDate(expire));
	}

	@Override
	public void put(String key, Object value, int ttlmillis, SetPolicy policy) {
		service.put(key, value, Expiration.byDeltaMillis(ttlmillis), getPolicy(policy));
	}

	@Override
	public void put(String key, Object value, Date expire, SetPolicy policy) {
		service.put(key, value, Expiration.onDate(expire), getPolicy(policy));
	}

	@Override
	public void putAll(Map<String, Object> values) {
		service.putAll(values);
	}

	@Override
	public void putAll(Map<String, Object> values, int ttlmillis) {
		service.putAll(values, Expiration.byDeltaMillis(ttlmillis));
	}

	@Override
	public void putAll(Map<String, Object> values, Date expire) {
		service.putAll(values, Expiration.onDate(expire));
	}

	@Override
	public void putAll(Map<String, Object> values, int ttlmillis,
			SetPolicy policy) {
		service.putAll(values, Expiration.byDeltaMillis(ttlmillis), getPolicy(policy));
	}

	@Override
	public void putAll(Map<String, Object> values, Date expire, SetPolicy policy) {
		service.putAll(values, Expiration.onDate(expire), getPolicy(policy));
	}
	
	private com.google.appengine.api.memcache.MemcacheService.SetPolicy getPolicy(SetPolicy policy){
		switch(policy){
		case SET_ALWAYS:
			return com.google.appengine.api.memcache.MemcacheService.SetPolicy.SET_ALWAYS;
		case ADD_ONLY_IF_NOT_PRESENT:
			return com.google.appengine.api.memcache.MemcacheService.SetPolicy.ADD_ONLY_IF_NOT_PRESENT;
		case REPLACE_ONLY_IF_PRESENT:
			return com.google.appengine.api.memcache.MemcacheService.SetPolicy.REPLACE_ONLY_IF_PRESENT;
		}
		return com.google.appengine.api.memcache.MemcacheService.SetPolicy.SET_ALWAYS;
	}

}
