package com.ohboywerecamping.plant;

import java.util.List;

import com.ohboywerecamping.common.Repository;
import com.ohboywerecamping.domain.Plant;

public interface PlantRepository extends Repository<Plant, String> {
    List<Plant> findAllByGardenerId(final String gardenerId);
}
