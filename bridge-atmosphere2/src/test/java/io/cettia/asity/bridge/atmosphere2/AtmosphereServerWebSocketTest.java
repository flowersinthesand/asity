/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.cettia.asity.bridge.atmosphere2;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import io.cettia.asity.action.Action;
import io.cettia.asity.bridge.atmosphere2.AsityAtmosphereServlet;
import io.cettia.asity.test.ServerWebSocketTest;
import io.cettia.asity.websocket.ServerWebSocket;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;

import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereResource;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Test;

/**
 * @author Donghwan Kim
 */
public class AtmosphereServerWebSocketTest extends ServerWebSocketTest {

    Server server;

    @Override
    protected void startServer() throws Exception {
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);
        ServletContextHandler handler = new ServletContextHandler();
        handler.addEventListener(new ServletContextListener() {
            @Override
            public void contextInitialized(ServletContextEvent event) {
                ServletContext context = event.getServletContext();
                Servlet servlet = new AsityAtmosphereServlet().onwebsocket(performer.serverAction());
                ServletRegistration.Dynamic reg = context.addServlet(AsityAtmosphereServlet.class.getName(), servlet);
                reg.setAsyncSupported(true);
                reg.setInitParameter(ApplicationConfig.DISABLE_ATMOSPHEREINTERCEPTOR, Boolean.TRUE.toString());
                reg.addMapping("/test");
            }

            @Override
            public void contextDestroyed(ServletContextEvent sce) {}
        });
        server.setHandler(handler);
        server.start();
    }

    @Test
    public void unwrap() {
        performer.onserver(new Action<ServerWebSocket>() {
            @Override
            public void on(ServerWebSocket ws) {
                assertThat(ws.unwrap(AtmosphereResource.class), instanceOf(AtmosphereResource.class));
                performer.start();
            }
        })
        .connect();
    }

    @Override
    protected void stopServer() throws Exception {
        server.stop();
    }

}
