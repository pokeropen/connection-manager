package com.example.springchat.controller;

import static java.lang.String.format;

import com.example.springchat.model.RoomEvent;
import com.example.springchat.model.RoomEvent.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
public class MessagingController {

  private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

  @Autowired
  private SimpMessageSendingOperations messagingTemplate;

  @MessageMapping("/messaging/{roomId}/sendMessage")
  public void sendMessage(@DestinationVariable String roomId, @Payload RoomEvent roomEvent) {
    messagingTemplate.convertAndSend(format("/channel/%s", roomId), roomEvent);
  }

  @MessageMapping("/messaging/{roomId}/addUser")
  public void addUser(@DestinationVariable String roomId, @Payload RoomEvent roomEvent,
      SimpMessageHeaderAccessor headerAccessor) {
    headerAccessor.getSessionAttributes().put("username", roomEvent.getSender());
    messagingTemplate.convertAndSend(format("/channel/%s", roomId), roomEvent);
  }

  @MessageMapping("/messaging/{roomId}/leave")
  public void leave(@DestinationVariable String roomId, @Payload RoomEvent roomEvent,
                      SimpMessageHeaderAccessor headerAccessor) {
    messagingTemplate.convertAndSend(format("/channel/%s", roomId), roomEvent);
  }
}
