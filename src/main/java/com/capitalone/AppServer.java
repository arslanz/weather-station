package com.capitalone;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;

public class AppServer {

	public static void main(String[] args) {
		ResourceConfig config = new ResourceConfig();
		config.packages("com.capitalone");
		//Allows bean validation error information to be sent to client
		config.property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
		ServletHolder servlet = new ServletHolder(new ServletContainer(config));

		Server server = new Server(2222);
		String pathSpec = "/*";
		ServletContextHandler context = new ServletContextHandler(server, pathSpec);
		context.addServlet(servlet, pathSpec);

		try {
			server.start();
			server.join();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			server.destroy();
		}
	}
}