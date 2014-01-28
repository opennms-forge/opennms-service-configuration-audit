package org.opennms.forge.serviceconfigurationaudit;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import org.opennms.forge.serviceconfigurationaudit.model.NotificationCompound;
import org.opennms.forge.serviceconfigurationaudit.model.ServiceCompound;
import org.opennms.forge.serviceconfigurationaudit.renderer.ServiceCompoundsRendererExcel;
import org.opennms.forge.serviceconfigurationaudit.renderer.ServiceCompoundsRendererTabOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Starter {

    private final static Logger LOGGER = LoggerFactory.getLogger(Starter.class);
    private final static String FOLDER_PARAMETER = "ConfigFolder";
    private final static String OUT_FILE = "OutPutFile";

    public static void main(String[] args) throws Exception {
        LOGGER.info("Hello World");
        File configFolder = null;
        if (System.getProperty(FOLDER_PARAMETER, null) != null) {
            configFolder = new File((String) System.getProperty(FOLDER_PARAMETER));

            if (configFolder.exists() && configFolder.canRead() && configFolder.isDirectory()) {
                LOGGER.info("Looking for opennms configs at {}", configFolder.getAbsolutePath());

            } else {
                LOGGER.error("Something is wrong with the provided {} with {}", FOLDER_PARAMETER, configFolder.getAbsolutePath());
                LOGGER.debug("Checking file {}", configFolder.getAbsolutePath());
                LOGGER.debug("exists {}", configFolder.exists());
                LOGGER.debug("canRead {}", configFolder.canRead());
                LOGGER.debug("isDirectory {}", configFolder.isDirectory());
                System.exit(1);
            }
        } else {
            LOGGER.error("Please add this parameter at starttime -D{}=YourConfigFolder", FOLDER_PARAMETER);
            LOGGER.error("If you want an excel file as output at -D{}=YourOutFile", OUT_FILE);
            System.exit(1);
        }

        Map<String, ServiceCompound> serviceCompounds = new TreeMap<>();

        PollerChecker pollerChecker = new PollerChecker(configFolder.getAbsolutePath());
        CollectionChecker collectionChecker = new CollectionChecker(configFolder.getAbsolutePath());
        RequisitionChecker requisitionChecker = new RequisitionChecker(configFolder.getAbsolutePath());

        serviceCompounds = pollerChecker.updateServiceCompounds(serviceCompounds);
        serviceCompounds = collectionChecker.updateServiceCompounds(serviceCompounds);
        serviceCompounds = requisitionChecker.updateServiceCompounds(serviceCompounds);

        if (System.getProperty(OUT_FILE, null) != null) {
            File outPutFile = new File(System.getProperty(OUT_FILE));
            ServiceCompoundsRendererExcel rendererExcel = new ServiceCompoundsRendererExcel(outPutFile);
            rendererExcel.render(serviceCompounds);
            LOGGER.info("Excle file was created at {}", outPutFile.getAbsolutePath());
        } else {
            ServiceCompoundsRendererTabOutput rendererTabOutput = new ServiceCompoundsRendererTabOutput();
            rendererTabOutput.render(serviceCompounds);
            LOGGER.info("If you want an excel file as output at -D{}=YourOutFile", OUT_FILE);
        }

        //TODO check notifications...
        Map<String, NotificationCompound> notificationCompounds = new TreeMap<>();

        NotificationCommandChecker notificationCommandChecker = new NotificationCommandChecker(configFolder.getAbsolutePath());
        DestinationPathChecker destinationPathChecker = new DestinationPathChecker(configFolder.getAbsolutePath());

        notificationCompounds = notificationCommandChecker.updateNotificationCompounds(notificationCompounds);
        notificationCompounds = destinationPathChecker.updateNotificationCompounds(notificationCompounds);

        LOGGER.info("All {} NotificationCompounds", notificationCompounds.size());
        for (NotificationCompound notificationCompound : notificationCompounds.values()) {
            LOGGER.info("\t{}", notificationCompound.toString());
        }

        LOGGER.info("Thanks for computing with OpenNMS");
    }
}
