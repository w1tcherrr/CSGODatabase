package at.emielregis.backend;

import at.emielregis.backend.runners.dataexport.DataWriter;
import at.emielregis.backend.runners.httpmapper.CSGOAccountMapper;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MainRunner implements ApplicationContextAware {
    private final DataWriter dataWriter;
    private final CSGOAccountMapper csgoAccountMapper;
    private ApplicationContext springContainer;

    public MainRunner(DataWriter dataWriter, CSGOAccountMapper csgoAccountMapper) {
        this.dataWriter = dataWriter;
        this.csgoAccountMapper = csgoAccountMapper;
    }

    /**
     * Runs the CSGOAccountMapper which maps the inventories of users.
     * Runs the DataWriter, which writes all data to excel files.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void run() throws InterruptedException {
        csgoAccountMapper.start();
        dataWriter.write();
        exit();
    }

    public void exit() {
        ((ConfigurableApplicationContext) springContainer).close();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        springContainer = applicationContext;
    }
}
