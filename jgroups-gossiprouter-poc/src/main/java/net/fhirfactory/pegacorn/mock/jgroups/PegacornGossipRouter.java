package net.fhirfactory.pegacorn.mock.jgroups;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jgroups.stack.GossipRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.sql.Date;
import java.time.Instant;

@CommandLine.Command(
        name="PegacornGossipRouter",
        description="Pegacorn Gossip Router"
)
public class PegacornGossipRouter implements Runnable{
    private static final Logger LOG = LoggerFactory.getLogger(PegacornGossipRouter.class);

    GossipRouter gossipRouter;

    @CommandLine.Option(names = {"-p", "--port"})
    private int port;

    @CommandLine.Option(names = {"-a", "--address"})
    private String ipAddress;

    @CommandLine.Option(names = {"-f", "--configFile"})
    private String configFile;

    protected static final long RPC_UNICAST_TIMEOUT = 1000;
    protected static final long RPC_MULTICAST_TIMEOUT = 5000;

    public static void main(String[] args) {
        CommandLine.run(new PegacornGossipRouter(), args);
    }

    @Override
    public void run() {
        LOG.debug("Pegacorn Gossip Router");
        initialiseGossipRouter();
        eventLoop();
        System.exit(0);
    }

    private void initialiseGossipRouter(){
        gossipRouter = new GossipRouter(ipAddress, port);
        try {
            gossipRouter.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOG.info("initialiseGossipRouter(): Bound To = Address->{}, Port->{}",gossipRouter.bindAddress(), gossipRouter.port());
    }

    private void eventLoop(){
        while(true) {
            try {
                Thread.sleep(10000);
                printSomeStatistics();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void printSomeStatistics(){
        LOG.info(".printSomeStatistics(): Print some statistics!!!!(" + Date.from(Instant.now()).toString() +")");
        String addresssMappings = gossipRouter.dumpAddresssMappings();
        String routingTable = gossipRouter.dumpRoutingTable();
        LOG.info(".printSomeStatistics(): Addressing Mappings ->{}", addresssMappings);
        LOG.info(".printSomeStatistics(): Routing Table ->{}", routingTable);
    }
}
