import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

@SuppressWarnings("serial")
public class HelloWorld extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
            IOException {
        resp.getWriter().print("Hello from Java!\n");
    }

    public static void main(final String[] args) throws Exception {
        final String webDir = HelloWorld.class.getClassLoader().getResource("webroot").toExternalForm();

        final ServletContextHandler context = new ServletContextHandler();
        context.addServlet(new ServletHolder(new HelloWorld()), "/hello");

        final ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(false);
        resourceHandler.setWelcomeFiles(new String[] { "index.html" });
        resourceHandler.setResourceBase(webDir);

        final HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resourceHandler, context });

        final Server server = new Server(Integer.valueOf(System.getenv("PORT")));
        server.setHandler(handlers);
        server.start();
        server.join();
    }
}