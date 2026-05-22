package ch.uzh.ifi.hase.soprafs26.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Game;
import ch.uzh.ifi.hase.soprafs26.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs26.service.GameService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class WebSocketEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);
    private static final int DISCONNECT_GRACE_SECONDS = 8;

    @Autowired
    private WebSocketSessionService wsSessionService;

    @Autowired
    private GameService gameService;

    @Autowired
    private GameRepository gameRepository;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        String username = wsSessionService.getUsernameForSession(sessionId);
        wsSessionService.removeSession(sessionId);

        if (username == null) return;

        String usernameSnapshot = username;
        scheduler.schedule(() -> {
            // If the user reconnected within the grace window, skip
            if (wsSessionService.getCurrentSessionForUser(usernameSnapshot) != null) return;

            Game game = gameRepository.findGameByPlayerUsernameAndStatus(usernameSnapshot, GameStatus.WAITING);
            if (game == null) return;

            log.info("Dirty-leave cleanup: removing {} from lobby {}", usernameSnapshot, game.getCode());
            try {
                gameService.leaveGame(game.getCode(), usernameSnapshot);
            } catch (Exception e) {
                log.warn("Dirty-leave cleanup failed for {}: {}", usernameSnapshot, e.getMessage());
            }
        }, DISCONNECT_GRACE_SECONDS, TimeUnit.SECONDS);
    }
}
