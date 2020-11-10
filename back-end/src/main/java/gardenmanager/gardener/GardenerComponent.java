package gardenmanager.gardener;

import java.util.Optional;

import gardenmanager.domain.Gardener;

public interface GardenerComponent {
    Optional<Gardener> findGardenerByEmail(String email);

    Gardener findOrCreateGardener(String email);
}
