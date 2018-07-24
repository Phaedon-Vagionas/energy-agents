package uk.ac.cam.eeci.energyagents;

import uk.ac.cam.eeci.framework.Reference;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PersonDistrictReference extends Reference<PersonDistrict> {

    public PersonDistrictReference(PersonDistrict referent) {
        super(referent);
    }

    public CompletableFuture<Map<PersonReference, Enum>> getAllCurrentActivities() {
        return this.referent.getAllCurrentActivities()
                .thenApplyAsync((values) -> values, pool.currentExecutor());
    }
}
