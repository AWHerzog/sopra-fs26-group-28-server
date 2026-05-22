package ch.uzh.ifi.hase.soprafs26.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import ch.uzh.ifi.hase.soprafs26.service.UserService;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private WebSocketSessionService wsSessionService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            if (token != null) {
                try {
                    String username = userService.checkTokenAuthenticity(token).getUsername();
                    wsSessionService.registerSession(accessor.getSessionId(), username);
                } catch (Exception e) {
                    // invalid token — session stays anonymous, no disconnect handling
                }
            }
        }
        return message;
    }
}
