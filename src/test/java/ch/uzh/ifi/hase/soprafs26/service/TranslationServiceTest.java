package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TranslationServiceTest {

    @Test
    public void translate_throwsWhenApiKeyMissing() {
        TranslationService svc = new TranslationService();
        // apiKey defaults to blank -> should throw SERVICE_UNAVAILABLE
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> svc.translate("hello", "DE"));
        assertEquals(503, ex.getStatusCode().value());
    }

    @Test
    public void translate_returnsCachedValueWhenPresent() throws Exception {
        TranslationService svc = new TranslationService();

        // set apiKey to non-empty so the method proceeds to cache logic
        Field apiKeyField = TranslationService.class.getDeclaredField("apiKey");
        apiKeyField.setAccessible(true);
        apiKeyField.set(svc, "DUMMY_KEY");

        // access and populate private cache map
        Field cacheField = TranslationService.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Map<String, String> cache = (java.util.Map<String, String>) cacheField.get(svc);

        String key = "DE:hello";
        cache.put(key, "Hallo");

        String res = svc.translate("hello", "de");
        assertEquals("Hallo", res);
    }
}
