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
package io.cettia.asity.bridge.grizzly2;

import io.cettia.asity.bridge.grizzly2.AsityWebSocketApplication;
import io.cettia.asity.test.ServerWebSocketTest;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketEngine;

/**
 * @author Donghwan Kim
 */
public class GrizzlyServerWebSocketTest extends ServerWebSocketTest {

    HttpServer server;

    @Override
    protected void startServer() throws Exception {
        server = HttpServer.createSimpleServer(null, port);
        NetworkListener listener = server.getListener("grizzly");
        listener.registerAddOn(new WebSocketAddOn());
        WebSocketEngine.getEngine().register("", "/test", new AsityWebSocketApplication().onwebsocket(performer.serverAction()));
        server.start();
    }

    @Override
    protected void stopServer() throws Exception {
        server.shutdownNow();
    }

}