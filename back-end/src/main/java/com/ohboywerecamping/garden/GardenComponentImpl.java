package com.ohboywerecamping.garden;

import java.util.List;

import com.ohboywerecamping.domain.Garden;
import com.ohboywerecamping.domain.GardenPlant;

import static java.util.stream.Collectors.toList;

public class GardenComponentImpl implements GardenComponent {
    private final GardenRepository gardens;

    public GardenComponentImpl(final GardenRepository gardens) {
        this.gardens = gardens;
    }

    @Override
    public List<Garden> findGardensByGardenerId(final String gardenerId) {
        return gardens.findAllByGardenerId(gardenerId).stream()
                .map(GardenComponentImpl::toGarden)
                .distinct()
                .collect(toList());
    }

    private static Garden toGarden(final GardenPlant plant) {
        Garden garden = new Garden();
        garden.setGardenerId(plant.getGardenerId());
        garden.setName(plant.getGarden());
        return garden;
    }
}
