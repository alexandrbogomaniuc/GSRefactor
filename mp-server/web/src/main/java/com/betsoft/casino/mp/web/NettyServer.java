package com.betsoft.casino.mp.web;

import com.betsoft.casino.mp.config.WebSocketRouter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.jsp.JettyJspServlet;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.springframework.context.ApplicationContext;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import org.springframework.web.servlet.DispatcherServlet;
import reactor.ipc.netty.http.server.HttpServer;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;

/**
 * User: flsh
 * Date: 11.06.18.
 * Start Netty and Jetty servers
 */
public class NettyServer {
    private static final Logger LOG = LogManager.getLogger(NettyServer.class);
    private static boolean standalone = false;
    private Server jettyServer;

    public static void main(String[] args) throws Exception {
        LOG.info("Starting server");
        NettyServer server = new NettyServer();
        standalone = true;
        try {
            server.init();
        } catch (Exception e) {
            LOG.error("Cannot init server", e);
            throw e;
        }
        LOG.info("Server started");
    }

    public static boolean isStandalone() {
        return standalone;
    }

    private void init() throws Exception {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(WebSocketRouter.class);
        context.registerShutdownHook();
        context.refresh();
        startReactorServer(createHttpHandler(context), getHost(context), getPort(context));
        startJettyServer(context);
    }

    public static class HelloServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("<h1>Hello from HelloServlet</h1>");
        }
    }

    //https://github.com/jetty-project/embedded-jetty-jsp
    //http://www.eclipse.org/jetty/documentation/9.4.x/embedding-jetty.html
    private void startJettyServer(WebApplicationContext context) throws Exception {
        LOG.debug("startJettyServer: context={}", context);
        int port = getJettyPort(context);
        String host = getJettyHost(context);
        InetSocketAddress address = InetSocketAddress.createUnresolved(host, port);
        jettyServer = new Server(address);
        disableSendServerVersion(jettyServer);
        URI baseUri = getWebRootResourceUri(context);
        LOG.info("Base URI: {}, jetty address={}", baseUri, address);
        // Create Servlet context
        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setContextPath("/");
        servletContextHandler.setResourceBase(baseUri.toASCIIString());
        servletContextHandler.addEventListener(new ContextLoaderListener(context));
        // Since this is a ServletContextHandler we must manually configure JSP support.
        enableEmbeddedJspSupport(servletContextHandler);
        //Add test hello servlet
        servletContextHandler.addServlet(HelloServlet.class, "/qqq");
        //Add SpringMVC support
        ServletHolder holderDispatcher = new ServletHolder("mvc", DispatcherServlet.class);
        servletContextHandler.addServlet(holderDispatcher, "/mvc/*");
        // Default Servlet (always last, always named "default")
        ServletHolder holderDefault = new ServletHolder("default", DefaultServlet.class);
        holderDefault.setInitParameter("resourceBase", baseUri.toASCIIString());
        holderDefault.setInitParameter("dirAllowed", "false");
        servletContextHandler.addServlet(holderDefault, "/");
        jettyServer.setHandler(servletContextHandler);
        jettyServer.start();
        jettyServer.join();
    }

    private HttpHandler createHttpHandler(ApplicationContext context) {
        return WebHttpHandlerBuilder.applicationContext(context).build();
    }

    private int getPort(ApplicationContext context) {
        return context.getEnvironment().getProperty("server.port", Integer.class, 8080);
    }

    private String getJettyHost(ApplicationContext context) {
        return context.getEnvironment().getProperty("server.jetty.host", String.class, "localhost");
    }

    private int getJettyPort(ApplicationContext context) {
        return context.getEnvironment().getProperty("server.jetty.port", Integer.class, 8081);
    }

    private String getJettyWebRoot(ApplicationContext context) {
        return context.getEnvironment().getProperty("server.jetty.webroot", String.class, "file:/www/html/mp/ROOT/");
    }

    private String getHost(ApplicationContext context) {
        return context.getEnvironment().getProperty("server.host", String.class, "localhost");
    }

    private void startReactorServer(HttpHandler httpHandler, String host, int port) {
        LOG.info("startReactorServer: host={}, port={}", host, port);
        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(httpHandler);
        //HttpServer server = HttpServer.builder().bindAddress(host).port(port).build();
        HttpServer server = HttpServer.create(host, port);
        LOG.debug("startReactorServer: HttpServer options={}", server.options().asDetailedString());
        server.newHandler(adapter).block();
    }

    private URI getWebRootResourceUri(ApplicationContext context) throws FileNotFoundException, URISyntaxException, MalformedURLException {
        String jettyWebRoot = getJettyWebRoot(context);
        URL indexUri = new URL(jettyWebRoot);
        //URL indexUri = this.getClass().getResource(jettyWebRoot);
        if (indexUri == null) {
            throw new FileNotFoundException("Unable to find resource: " + jettyWebRoot);
        }
        return indexUri.toURI();
    }

    private void enableEmbeddedJspSupport(ServletContextHandler servletContextHandler) throws IOException {
        // Establish Scratch directory for the servlet context (used by JSP compilation)
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File scratchDir = new File(tempDir.toString(), "embedded-jetty-jsp");
        if (!scratchDir.exists()) {
            if (!scratchDir.mkdirs()) {
                throw new IOException("Unable to create scratch directory: " + scratchDir);
            }
        }
        servletContextHandler.setAttribute("javax.servlet.context.tempdir", scratchDir);
        // Set Classloader of Context to be sane (needed for JSTL)
        // JSP requires a non-System classloader, this simply wraps the
        // embedded System classloader in a way that makes it suitable
        // for JSP to use
        ClassLoader jspClassLoader = new URLClassLoader(new URL[0], this.getClass().getClassLoader());
        servletContextHandler.setClassLoader(jspClassLoader);
        // Manually call JettyJasperInitializer on context startup
        servletContextHandler.addBean(new JspStarter(servletContextHandler));
        // Create / Register JSP Servlet (must be named "jsp" per spec)
        ServletHolder holderJsp = new ServletHolder("jsp", JettyJspServlet.class);
        holderJsp.setInitOrder(0);
        holderJsp.setInitParameter("logVerbosityLevel", "DEBUG");
        holderJsp.setInitParameter("fork", "false");
        holderJsp.setInitParameter("xpoweredBy", "false");
        holderJsp.setInitParameter("compilerTargetVM", "1.8");
        holderJsp.setInitParameter("compilerSourceVM", "1.8");
        holderJsp.setInitParameter("keepgenerated", "true");
        servletContextHandler.addServlet(holderJsp, "*.jsp");
    }

    private void disableSendServerVersion(Server server) {
        for (Connector connector : server.getConnectors()) {
            for (ConnectionFactory connectionFactory : connector.getConnectionFactories()) {
                if (connectionFactory instanceof HttpConnectionFactory) {
                    ((HttpConnectionFactory) connectionFactory).getHttpConfiguration().setSendServerVersion(false);
                }
            }
        }
    }

    /**
     * JspStarter for embedded ServletContextHandlers
     * <p>
     * This is added as a bean that is a jetty LifeCycle on the ServletContextHandler.
     * This bean's doStart method will be called as the ServletContextHandler starts,
     * and will call the ServletContainerInitializer for the jsp engine.
     */
    public static class JspStarter extends AbstractLifeCycle implements ServletContextHandler.ServletContainerInitializerCaller {
        JettyJasperInitializer sci;
        ServletContextHandler context;

        public JspStarter(ServletContextHandler context) {
            this.sci = new JettyJasperInitializer();
            this.context = context;
            this.context.setAttribute("org.apache.tomcat.JarScanner", new StandardJarScanner());
        }

        @Override
        protected void doStart() throws Exception {
            ClassLoader old = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(context.getClassLoader());
            try {
                sci.onStartup(null, context.getServletContext());
                super.doStart();
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }
        }
    }
}
