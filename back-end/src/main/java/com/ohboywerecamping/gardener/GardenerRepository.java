package com.ohboywerecamping.gardener;

import java.util.Optional;

import com.ohboywerecamping.common.Repository;
import com.ohboywerecamping.domain.Gardener;

public interface GardenerRepository extends Repository<Gardener, String> {
    Optional<Gardener> findByEmail(String email);
}
