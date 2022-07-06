package at.emielregis.backend;

import at.emielregis.backend.runners.dataexport.DataWriter;
import at.emielregis.backend.runners.httpmapper.CSGOAccountMapper;
import at.emielregis.backend.service.ItemService;
import at.emielregis.backend.service.StickerService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MainRunner {
    private final DataWriter dataWriter;
    private final CSGOAccountMapper csgoAccountMapper;
    private final ItemService itemService;
    private final StickerService stickerService;

    public MainRunner(DataWriter dataWriter, CSGOAccountMapper csgoAccountMapper, ItemService itemService, StickerService stickerService) {
        this.dataWriter = dataWriter;
        this.csgoAccountMapper = csgoAccountMapper;
        this.itemService = itemService;
        this.stickerService = stickerService;
    }

    /**
     * Runs the DataWriter, which writes all data to excel files.
     * Runs the CSGOAccountMapper which maps the inventories of users.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void run() throws InterruptedException {
        //dataWriter.write();
        csgoAccountMapper.start();
    }
}
