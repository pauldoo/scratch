package notepad;

import static java.lang.Integer.valueOf;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.sun.jersey.spi.container.servlet.ServletContainer;

public class Launcher {

    public static void main(final String[] args) throws Exception {
        final HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { createStaticHandler(), createJerseyHandler() });

        final Server server = new Server(getPort());
        server.setHandler(handlers);
        server.start();
        server.join();
    }

    private static Handler createStaticHandler() {
        final String webDir = Launcher.class.getClassLoader().getResource("webroot").toExternalForm();
        final ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(false);
        resourceHandler.setWelcomeFiles(new String[] { "index.html" });
        resourceHandler.setResourceBase(webDir);
        return resourceHandler;
    }

    private static Handler createJerseyHandler() {
        final ServletHolder holder = new ServletHolder(ServletContainer.class);
        holder.setInitParameter("com.sun.jersey.config.property.resourceConfigClass",
                "com.sun.jersey.api.core.PackagesResourceConfig");
        holder.setInitParameter("com.sun.jersey.config.property.packages", "notepad");
        holder.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");

        final ServletContextHandler context = new ServletContextHandler();
        context.addServlet(holder, "/*");

        return context;
    }

    private static int getPort() {
        final String valueFromEnvironment = System.getenv("PORT");
        return (valueFromEnvironment != null) ? valueOf(valueFromEnvironment) : 5000;
    }
}