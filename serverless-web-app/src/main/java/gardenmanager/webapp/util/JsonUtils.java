package gardenmanager.webapp.util;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class JsonUtils {
    private static final ObjectMapper jackson;

    static {
        jackson = new ObjectMapper();
        jackson.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        jackson.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        jackson.registerModule(new JavaTimeModule());
    }

    private JsonUtils() {
        throw new UnsupportedOperationException("cannot instantiate " + getClass());
    }

    public static ObjectMapper jackson() {
        return jackson;
    }

    public static String toJson(final Object obj) {
        try {
            return jackson.writeValueAsString(obj);
        } catch (IOException e) {
            throw new RuntimeException("error creating JSON", e);
        }
    }
}
