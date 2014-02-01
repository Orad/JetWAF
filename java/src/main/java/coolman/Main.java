package coolman;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Jetwaf Entry point class.
 * @author oz
 *
 */
public class Main {

	/**
	 * 
	 *	Init and start JetWaf.
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		final String webappDirLocation = "src/main/webapp/";
		//Allow port configuration from environment variable.
		String webPort = System.getenv("PORT");
		if (webPort == null || webPort.isEmpty()) {
			webPort = "8080";
		}
		
		//Create and configure servlet container
		Server server = new Server(Integer.valueOf(webPort));
		WebAppContext root = new WebAppContext();
		// Logger log =
		// root.setLogger(new )
		root.setContextPath("/"); 
		//The servlet and the filter are configured in web.xml
		//This way changes can be made without recomile.
		root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
		root.setResourceBase(webappDirLocation);
		root.setParentLoaderPriority(true);
		server.setHandler(root);
		server.start();
		//wait for the thread. don't let the process quit as long as the server running.
		server.join();
	}
}
