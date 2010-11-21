package org.qrone.r7.appengine;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class AppEngineServlet extends HttpServlet {
	private AppEngineURIHandler h;
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		if(h == null)
			h = new AppEngineURIHandler(getServletContext());
		h.handle(req, resp, req.getPathInfo(), "");
	}
}
