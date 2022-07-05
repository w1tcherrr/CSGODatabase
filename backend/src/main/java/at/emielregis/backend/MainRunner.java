package at.emielregis.backend;

import at.emielregis.backend.runners.dataexport.DataWriter;
import at.emielregis.backend.runners.httpmapper.CSGOAccountMapper;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public record MainRunner(DataWriter dataWriter, CSGOAccountMapper csgoAccountMapper) {

    /**
     * Runs the DataWriter, which writes all data to excel files.
     * Runs the CSGOAccountMapper which maps the inventories of users.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void run() {
        //dataWriter.write();
        csgoAccountMapper.start();
    }
}
