package co.wethinkcode.logisticsconnect;

import java.util.Optional;

/** Finds a hub by ID or alias, or empty if it does not exist. */
@FunctionalInterface
public interface HubLookup {
    Optional<HubInfo> find(String hubId) throws DependencyUnavailableException;
}
