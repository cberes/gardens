package com.ohboywerecamping.garden;

import java.util.List;

import com.ohboywerecamping.domain.Garden;

public interface GardenComponent {
    List<Garden> findGardensByGardenerId(String gardenerId);
}
