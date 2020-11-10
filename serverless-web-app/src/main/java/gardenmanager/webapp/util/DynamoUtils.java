package gardenmanager.webapp.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Collection;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import static java.util.stream.Collectors.toList;

public final class DynamoUtils {
    private DynamoUtils() {
        throw new UnsupportedOperationException("cannot instantiate " + getClass());
    }

    public static AttributeValue bool(final boolean b) {
        return AttributeValue.builder().bool(b).build();
    }

    public static AttributeValue n(final int i) {
        return AttributeValue.builder().n(Integer.toString(i)).build();
    }

    public static AttributeValue s(final String s) {
        return AttributeValue.builder().s(s).build();
    }

    public static AttributeValue s(final Enum<?> v) {
        return AttributeValue.builder().s(v.name()).build();
    }

    public static AttributeValue s(final LocalDate d) {
        return AttributeValue.builder().s(d.toString()).build();
    }

    public static AttributeValue s(final ZonedDateTime t) {
        return AttributeValue.builder().s(t.toString()).build();
    }

    public static AttributeValue l(final Collection<String> values) {
        return AttributeValue.builder().l(values.stream()
                .map(value -> s(value))
                .collect(toList())).build();
    }

    public static String randomId() {
        final byte[] buffer = new byte[16];
        try {
            SecureRandom.getInstanceStrong().nextBytes(buffer);
        } catch (NoSuchAlgorithmException e) {
            // I'll take that risk
            throw new RuntimeException(e);
        }
        return toUrlString(buffer);
    }

    private static String toUrlString(byte[] buffer) {
        return Base64.getEncoder().encodeToString(buffer)
                .replace('+', '-')
                .replace('/', '_')
                .replace("=", "");
    }
}
