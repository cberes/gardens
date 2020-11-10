package gardenmanager.reservation;

import java.time.LocalDate;
import java.util.*;

import com.ohboywerecamping.domain.Campsite;
import com.ohboywerecamping.domain.Reservation;
import gardenmanager.test.InMemoryRepository;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class InMemoryReservationRepository
        extends InMemoryRepository<Reservation, String> implements ReservationRepository {
    private InMemoryReservationRepository() {
        super(Object::toString, Reservation::setId, emptyList());
    }

    public InMemoryReservationRepository(final List<Campsite> campsites,
                                         final LocalDate start) {
        super(Object::toString, Reservation::setId, reservations(campsites, start));
    }

    @Override
    public List<Reservation> findByCampsiteBetweenDates(final String campsiteId,
                                                        final LocalDate start, final LocalDate end) {
        return findAll().stream()
                .filter(res -> res.getCampsite().getId().equals(campsiteId)
                        && !res.getDate().isBefore(start)
                        && res.getDate().isBefore(end)) // TODO is this inclusive?
                .collect(toList());
    }

    @Override
    public List<Reservation> findByCampsitesBetweenDates(final Collection<String> campsiteIdCol,
                                                         final LocalDate start, final LocalDate end) {
        final Set<String> campsiteIds = new HashSet<>(campsiteIdCol);
        return findAll().stream()
                .filter(res -> campsiteIds.contains(res.getCampsite().getId())
                        && !res.getDate().isBefore(start)
                        && res.getDate().isBefore(end)) // TODO is this inclusive?
                .collect(toList());
    }

    @Override
    public List<Reservation> findByOrder(final String orderId) {
        return findAll().stream()
                .filter(res -> res.getOrder().getId().equals(orderId))
                .collect(toList());
    }

    @Override
    public void store(final Reservation reservation) {
        save(reservation);
    }

    private static List<Reservation> reservations(final List<Campsite> campsites,
                                                  final LocalDate start) {
        final List<Reservation> reservations = new LinkedList<>();
        for (int i = 0; i < campsites.size(); ++i) {
            final Campsite campsite = campsites.get(i);
            final List<Reservation> toAdd = i % 2 == 0 ? reservations1(campsite, start)
                    : reservations2(campsite, start);
            reservations.addAll(toAdd);
        }
        return reservations;
    }

    private static List<Reservation> reservations1(final Campsite campsite,
                                                   final LocalDate start) {
        final List<Reservation> reservations = new ArrayList<>();
        reservations.addAll(buildRange(campsite, start.plusDays(10), start.plusDays(12)));
        reservations.addAll(buildRange(campsite, start.minusDays(12), start.minusDays(9)));
        reservations.addAll(buildRange(campsite, start.plusDays(13), start.plusDays(25)));
        reservations.addAll(buildRange(campsite, start.plusDays(7), start.plusDays(8)));
        reservations.addAll(buildRange(campsite, start.minusDays(4), start));
        reservations.addAll(buildRange(campsite, start.plusDays(3), start.plusDays(4)));
        reservations.addAll(buildRange(campsite, start.plusDays(8), start.plusDays(9)));
        reservations.addAll(buildRange(campsite, start, start.plusDays(2)));
        reservations.addAll(buildRange(campsite, start.minusDays(6), start.minusDays(5)));
        return reservations;
    }

    private static List<Reservation> reservations2(final Campsite campsite,
                                                   final LocalDate start) {
        final List<Reservation> reservations = new ArrayList<>();
        reservations.addAll(buildRange(campsite, start.plusDays(7), start.plusDays(9)));
        reservations.addAll(buildRange(campsite, start.minusDays(9), start.minusDays(5)));
        reservations.addAll(buildRange(campsite, start.plusDays(9), start.plusDays(11)));
        reservations.addAll(buildRange(campsite, start.minusDays(14), start.minusDays(12)));
        return reservations;
    }

    private static List<Reservation> buildRange(final Campsite campsite,
                                                final LocalDate start,
                                                final LocalDate end) {
        return start.datesUntil(end)
                .map(date -> build(campsite, date))
                .collect(toList());
    }

    private static Reservation build(final Campsite campsite,
                                     final LocalDate date) {
        final Reservation reservation = new Reservation();
        reservation.setCampsite(campsite);
        reservation.setDate(date);
        return reservation;
    }

    public static InMemoryReservationRepository empty() {
        return new InMemoryReservationRepository();
    }
}
