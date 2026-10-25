package co.wethinkcode.logisticsconnect;

import java.util.Optional;

/** Resolves a hub ID (or alias) to its canonical ID, or empty if the hub does not exist. */
@FunctionalInterface
public interface HubDirectory {
    Optional<String> canonicalId(String hubId) throws HubServiceUnavailableException;
}
