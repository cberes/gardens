package com.ohboywerecamping.gardener;

import java.util.Optional;

import com.ohboywerecamping.domain.Gardener;

public class GardenerComponentImpl implements GardenerComponent {
    private final GardenerRepository gardeners;

    public GardenerComponentImpl(final GardenerRepository gardeners) {
        this.gardeners = gardeners;
    }

    @Override
    public Optional<Gardener> findGardenerByEmail(final String email) {
        return gardeners.findByEmail(email);
    }

    @Override
    public Gardener findOrCreateGardener(final String email) {
        return gardeners.findByEmail(email)
                .orElseGet(() -> create(email));
    }

    private Gardener create(final String email) {
        final Gardener gardener = new Gardener();
        gardener.setEmail(email);
        gardeners.save(gardener);
        return gardener;
    }
}
