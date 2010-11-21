package org.qrone.r7.appengine;

import java.util.Map;

import org.qrone.r7.script.browser.LoginService;
import org.qrone.r7.script.browser.User;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class AppEngineLoginService implements LoginService{
	private UserService service;
	public AppEngineLoginService() {
		service = UserServiceFactory.getUserService();
	}
	
	@Override
	public String loginURL(String doneURL) {
		return service.createLoginURL(doneURL);
	}

	@Override
	public String loginURL(String url, Map<String, String> attrMap,
			String doneURL) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String logoutURL(String doneURL) {
		return service.createLogoutURL(doneURL);
	}

	@Override
	public User getUser() {
		com.google.appengine.api.users.User user = service.getCurrentUser();
		return new User(user.getEmail(), user.getUserId(), service.isUserAdmin());
	}

}
