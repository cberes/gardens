package com.ohboywerecamping.gardener;

import java.util.Optional;

import com.ohboywerecamping.domain.Gardener;

public interface GardenerComponent {
    Optional<Gardener> findGardenerByEmail(String email);

    Gardener findOrCreateGardener(String email);
}
