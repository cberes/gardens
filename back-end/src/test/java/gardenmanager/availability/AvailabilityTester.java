package gardenmanager.availability;

import com.ohboywerecamping.domain.CampsiteAvailability;
import com.ohboywerecamping.domain.DateAvailability;
import com.ohboywerecamping.domain.Availability;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

final class AvailabilityTester {
    private static final Map<String, Availability> AVAILABILITY_BY_CODE = stream(Availability.values())
            .collect(toMap(status -> status.name().substring(0, 1), Function.identity()));

    private AvailabilityTester() {
        throw new UnsupportedOperationException("cannot instantiate " + getClass());
    }

    static void assertEmpty(final CampsiteAvailability campsite, final String id) {
        test(campsite, id, "");
    }

    static void test(final CampsiteAvailability campsite, final String id, final String codes) {
        assertThat(campsite.getId(), is(id));
        assertStatus(campsite, codes);
        assertDates(campsite);
    }

    static void assertStatus(final CampsiteAvailability campsite, final String codes) {
        assertThat(campsite.getAvailability().size(), is(codes.length()));

        final String actual = toCodes(campsite.getAvailability());
        final Iterator<Availability> statusIter = toStream(codes).iterator();
        for (DateAvailability date : campsite.getAvailability()) {
            assertThat(date.getStatus(), notNullValue());
            assertThat("Actual codes: " + actual, date.getStatus(), is(statusIter.next()));
        }
    }

    private static Stream<Availability> toStream(final String codes) {
        return codes.chars()
                .mapToObj(code -> Character.valueOf((char) code).toString())
                .map(AVAILABILITY_BY_CODE::get);
    }

    private static String toCodes(final List<DateAvailability> dates) {
        return dates.stream()
                .map(DateAvailability::getStatus)
                .map(status -> status.name().substring(0, 1))
                .collect(joining());
    }

    static void assertDates(final CampsiteAvailability campsite) {
        LocalDate previous = null;
        for (DateAvailability date : campsite.getAvailability()) {
            assertThat(date.getDate(), notNullValue());
            if (previous != null) {
                assertThat(date.getDate(), is(previous.plusDays(1)));
            }
            previous = date.getDate();
        }
    }

    static void assertStartEnd(final CampsiteAvailability campsite, final LocalDate start, final LocalDate end) {
        final int length = campsite.getAvailability().size();
        assertThat(campsite.getAvailability().get(0).getDate(), is(start));
        assertThat(campsite.getAvailability().get(length - 1).getDate(), is(end));
    }
}
