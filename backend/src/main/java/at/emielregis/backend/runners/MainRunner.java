package at.emielregis.backend.runners;

import at.emielregis.backend.runners.dataexport.DataWriter;
import at.emielregis.backend.runners.httpmapper.SteamAccountMapper;
import at.emielregis.backend.service.SteamAccountService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public record MainRunner(DataWriter dataWriter, SteamAccountMapper steamAccountMapper,
                         SteamAccountService steamAccountService) {

    @EventListener(ApplicationReadyEvent.class)
    public void run() {
        //dataWriter.write();
        steamAccountMapper.run();
    }
}
