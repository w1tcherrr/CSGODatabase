package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.items.Charm;
import at.emielregis.backend.repository.CharmRepository;
import at.emielregis.backend.runners.dataexport.writers.CharmWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

/**
 * Service class for managing {@link Charm} entities.
 * Provides methods required by {@link CharmWriter} for processing charm data.
 */
@Component
public class CharmService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final CharmRepository charmRepository;

    /**
     * Constructs a new {@link CharmService} with the required repository.
     *
     * @param charmRepository Repository for {@link Charm} entities.
     */
    public CharmService(CharmRepository charmRepository) {
        this.charmRepository = charmRepository;
    }

    /**
     * Counts the total number of times the charm with the specified name has been applied to items.
     *
     * @param charmName The name of the charm.
     * @return The total number of applied charms.
     */
    public long countTotalAppliedForCharm(String charmName) {
        LOGGER.info("CharmService#countTotalAppliedForCharm({})", charmName);
        Long amount = charmRepository.countTotalAppliedForItemName(charmName);
        return amount != null ? amount : 0;
    }

}
