package xlike_hr.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class JerseyApplication {

    private static Logger log = LogManager.getLogger(ConverterService.class);

    private static int getPort() {
        try {
            return Integer.parseInt(System.getenv("XLIKE_PORT"));
        } catch (NumberFormatException e) {
            return 8081;
        }
    }

    public static void main(String[] args) throws URISyntaxException {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        Server jettyServer = new Server(getPort());
        jettyServer.setHandler(context);

        ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);

        // Tells the Jersey Servlet which REST service/class to load.
        Map<String, String> params = new HashMap<String, String>();
        params.put(
                "jersey.config.server.provider.packages",
                "xlike_hr.service");
        jerseyServlet.setInitParameters(params);

        try {
            jettyServer.start();
            jettyServer.join();
        } catch (Exception ex) {
            log.error("Error occurred while starting Jetty", ex);
            System.exit(1);
        } finally {
            jettyServer.destroy();
        }
    }
}