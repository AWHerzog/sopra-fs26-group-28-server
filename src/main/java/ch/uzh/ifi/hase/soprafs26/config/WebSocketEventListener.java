package ch.uzh.ifi.hase.soprafs26.config;

import ch.uzh.ifi.hase.soprafs26.service.GameFlowService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private final GameFlowService gameFlowService;
    private final UserService userService;

    public WebSocketEventListener(GameFlowService gameFlowService, UserService userService) {
        this.gameFlowService = gameFlowService;
        this.userService = userService;
    }

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String gameCode = accessor.getFirstNativeHeader("gameCode");
        String token = accessor.getFirstNativeHeader("token");

        if (gameCode == null || token == null) return;

        String username = userService.checkTokenAuthenticity(token).getUsername();

        // Store in session for later use on disconnect
        accessor.getSessionAttributes().put("gameCode", gameCode);
        accessor.getSessionAttributes().put("username", username);
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        if (accessor.getSessionAttributes() == null) return;

        String gameCode = (String) accessor.getSessionAttributes().get("gameCode");
        String username = (String) accessor.getSessionAttributes().get("username");

        if (gameCode == null || username == null) return;

        try {
            gameFlowService.leaveGame(gameCode, username);
        } catch (Exception e) {
            // if game already deleted or player removed (left through leave button)
            System.out.println("Disconnect cleanup skipped: " + e.getMessage());
        }
    }
}
