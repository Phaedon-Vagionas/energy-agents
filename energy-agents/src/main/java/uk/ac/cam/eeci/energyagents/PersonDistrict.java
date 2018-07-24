package uk.ac.cam.eeci.energyagents;

import org.javatuples.Pair;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An urban district comprising of several dwellings.
 */
public class PersonDistrict {

    private final List<PersonReference> people;

    public PersonDistrict(Set<PersonReference> people) {
        this.people = new ArrayList<>(people);
        if (people.size() == 0){
            throw new IllegalArgumentException("PersonDistrict must contain at least one person.");
        }
    }

    public CompletableFuture<Map<PersonReference, Enum>> getAllCurrentActivities() {
        Map<PersonReference, Enum> values = new ConcurrentHashMap<>();
        CompletableFuture<Void>[] updates = new CompletableFuture[this.people.size()];
        for (int i = 0; i < this.people.size(); ++i) {
            updates[i] = this.getActivity(this.people.get(i))
                    .thenAccept(pair -> values.put(pair.getValue0(), pair.getValue1()));
        }
        return CompletableFuture.allOf(updates)
                .thenApply(nothing -> values);
    }


    private CompletableFuture<Pair<PersonReference, Enum>> getActivity(PersonReference person) {
        return person.getCurrentActivity().thenApplyAsync(temp -> new Pair<>(person, temp));
    }
}
