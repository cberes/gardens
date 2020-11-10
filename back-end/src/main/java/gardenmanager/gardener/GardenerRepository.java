package gardenmanager.gardener;

import java.util.Optional;

import gardenmanager.common.Repository;
import gardenmanager.domain.Gardener;

public interface GardenerRepository extends Repository<Gardener, String> {
    Optional<Gardener> findByEmail(String email);
}
