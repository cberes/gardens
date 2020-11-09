package com.ohboywerecamping.garden;

import java.util.List;

import com.ohboywerecamping.common.Repository;
import com.ohboywerecamping.domain.GardenPlant;

public interface GardenRepository extends Repository<GardenPlant, String> {
    List<GardenPlant> findAllByGardenerId(String gardenerId);

    List<GardenPlant> findAllByPlantId(String plantId);
}
