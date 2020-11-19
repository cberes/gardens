package gardenmanager.webapp.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorMessage {
    private final String message;

    @JsonCreator
    public ErrorMessage(@JsonProperty("message") final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
