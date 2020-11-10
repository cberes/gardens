package gardenmanager.availability;

import com.ohboywerecamping.area.AreaRepository;
import gardenmanager.area.InMemoryAreaRepository;
import com.ohboywerecamping.campground.CampgroundRepository;
import gardenmanager.campground.InMemoryCampgroundRepository;
import com.ohboywerecamping.campsite.CampsiteRepository;
import gardenmanager.campsite.InMemoryCampsiteRepository;
import com.ohboywerecamping.domain.*;
import gardenmanager.reservation.InMemoryReservationRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class AvailabilityServiceTest {
    private List<Campsite> campsitesWithReservations;
    private CampsiteRepository campsiteRepo;
    private AvailabilityService service;

    private void createService(final int campsitesToReserve, final LocalDate start) {
        final CampgroundRepository campgrounds = new InMemoryCampgroundRepository();
        final AreaRepository areas = new InMemoryAreaRepository(campgrounds.findAll());
        campsiteRepo = new InMemoryCampsiteRepository(areas.findAll());
        campsitesWithReservations = campsiteRepo.findAll().stream().limit(campsitesToReserve).collect(toList());
        service = new AvailabilityService(areas, campsiteRepo,
                new InMemoryReservationRepository(campsitesWithReservations, start));
    }

    @Test
    void testNoReservations() {
        final LocalDate start = LocalDate.now();
        final LocalDate end = start.plusDays(9);

        createService(0, start);
        final String id = campsiteRepo.findAll().get(0).getId();
        final String campgroundId = campsiteRepo.findAll().get(0).getCampground().getId();

        final CampgroundAvailability avail = service.findByCampsiteId(id, start, end.plusDays(1));
        assertThat(avail, notNullValue());
        assertThat(avail.getCampgroundId(), is(campgroundId));
        assertThat(avail.getCampsites().size(), is(1));

        final CampsiteAvailability campsite = avail.getCampsites().get(0);
        AvailabilityTester.test(campsite, id, "IIAAAAAAAA");
        AvailabilityTester.assertStartEnd(campsite, start, end);
    }

    @Test
    void testReservations() {
        final LocalDate start = LocalDate.now();
        final LocalDate end = start.plusDays(9);

        final int siteCount = 2;
        createService(siteCount, start);
        final String id = campsitesWithReservations.get(0).getCampground().getId();

        final CampgroundAvailability avail = service.findByCampgroundId(id, start, end.plusDays(1));
        assertThat(avail, notNullValue());
        assertThat(avail.getCampgroundId(), is(id));
        assertThat(avail.getCampsites().size(), is(siteCount));

        AvailabilityTester.test(avail.getCampsites().get(0), campsitesWithReservations.get(0).getId(), "RRARAAARRA");
        AvailabilityTester.test(avail.getCampsites().get(1), campsitesWithReservations.get(1).getId(), "IIAAAAARRR");

        range(0, siteCount).forEach(i -> AvailabilityTester.assertStartEnd(avail.getCampsites().get(i), start, end));
    }
}
