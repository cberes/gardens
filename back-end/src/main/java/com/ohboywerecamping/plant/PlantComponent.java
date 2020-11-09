package com.ohboywerecamping.plant;

import java.util.List;
import java.util.Optional;

import com.ohboywerecamping.domain.Plant;

public interface PlantComponent {
    Optional<Plant> findPlantById(String id);

    List<Plant> findPlantsByGardenerId(String gardenerId);

    String save(Plant plant);

    void delete(Plant plant);
}
