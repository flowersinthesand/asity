/*
 * Copyright 2018 the original author or authors.
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
package io.cettia.asity.bridge.jwa1;

import io.cettia.asity.websocket.AbstractServerWebSocket;
import io.cettia.asity.websocket.ServerWebSocket;

import javax.websocket.MessageHandler;
import javax.websocket.SendHandler;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

/**
 * {@link ServerWebSocket} for Java WebSocket API 1.
 *
 * @author Donghwan Kim
 */
public class JwaServerWebSocket extends AbstractServerWebSocket {

  private final Session session;
  private final HandshakeRequest handshakeRequest;
  private final SendHandler sendHandler = result -> {
    if (!result.isOK()) {
      errorActions.fire(result.getException());
    }
  };

  public JwaServerWebSocket(Session session) {
    this.session = session;
    this.handshakeRequest = (HandshakeRequest) this.session.getUserProperties().get(HandshakeRequest.class.getName());
    // Uses `void addMessageHandler(MessageHandler handler)` to support 1.0
    session.addMessageHandler(new MessageHandler.Whole<String>() {
      @Override
      public void onMessage(String message) {
        textActions.fire(message);
      }
    });
    session.addMessageHandler(new MessageHandler.Whole<ByteBuffer>() {
      @Override
      public void onMessage(ByteBuffer message) {
        binaryActions.fire(message);
      }
    });
  }

  void onError(Throwable e) {
    errorActions.fire(e);
  }

  void onClose() {
    closeActions.fire();
  }

  @Override
  public String uri() {
    // session.getRequestURI() returns the full URI starting with protocol
    // not request URI starting with path
    URI uri = session.getRequestURI();
    return uri.getPath() + (uri.getQuery() != null ? "?" + uri.getQuery() : "");
  }

  @Override
  public Set<String> headerNames() {
    return this.handshakeRequest.getHeaders().keySet();
  }

  @Override
  public List<String> headers(String name) {
    return this.handshakeRequest.getHeaders().get(name);
  }

  @Override
  protected void doClose() {
    try {
      session.close();
    } catch (IOException e) {
      errorActions.fire(e);
    }
  }

  @Override
  protected void doSend(ByteBuffer byteBuffer) {
    session.getAsyncRemote().sendBinary(byteBuffer, sendHandler);
  }

  @Override
  protected void doSend(String data) {
    session.getAsyncRemote().sendText(data, sendHandler);
  }

  /**
   * {@link Session} is available.
   */
  @Override
  public <T> T unwrap(Class<T> clazz) {
    return Session.class.isAssignableFrom(clazz) ? clazz.cast(session) : null;
  }

}
