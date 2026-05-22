package ch.uzh.ifi.hase.soprafs26.config;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionService {

    private final ConcurrentHashMap<String, String> sessionToUsername = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> usernameToSession = new ConcurrentHashMap<>();

    public void registerSession(String sessionId, String username) {
        String oldSession = usernameToSession.put(username, sessionId);
        if (oldSession != null) {
            sessionToUsername.remove(oldSession);
        }
        sessionToUsername.put(sessionId, username);
    }

    public void removeSession(String sessionId) {
        String username = sessionToUsername.remove(sessionId);
        if (username != null) {
            usernameToSession.remove(username, sessionId);
        }
    }

    public String getUsernameForSession(String sessionId) {
        return sessionToUsername.get(sessionId);
    }

    // Returns null if the user has no active session (truly disconnected)
    public String getCurrentSessionForUser(String username) {
        return usernameToSession.get(username);
    }
}
