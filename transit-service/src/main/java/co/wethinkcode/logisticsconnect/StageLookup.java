package co.wethinkcode.logisticsconnect;

/** Current delay stage for a (canonical) hub ID. */
@FunctionalInterface
public interface StageLookup {
    int stageFor(String hubId) throws DependencyUnavailableException;
}
