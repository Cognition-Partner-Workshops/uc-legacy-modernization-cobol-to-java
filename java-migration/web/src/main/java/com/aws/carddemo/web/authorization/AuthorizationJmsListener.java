package com.aws.carddemo.web.authorization;

import jakarta.jms.Destination;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "carddemo.jms.enabled", havingValue = "true")
public class AuthorizationJmsListener {
  private final AuthorizationService service;
  private final JmsTemplate jms;

  public AuthorizationJmsListener(AuthorizationService service, JmsTemplate jms) {
    this.service = service;
    this.jms = jms;
  }

  @JmsListener(destination = "${carddemo.jms.authorization-request:carddemo.authorization.request}")
  public void receive(String body, jakarta.jms.Message message) throws jakarta.jms.JMSException {
    AuthorizationService.Reply reply = service.authorize(AuthorizationCodec.decodeRequest(body));
    Destination destination = message.getJMSReplyTo();
    if (destination != null) {
      jms.convertAndSend(destination, AuthorizationCodec.encodeReply(reply));
    }
  }
}
