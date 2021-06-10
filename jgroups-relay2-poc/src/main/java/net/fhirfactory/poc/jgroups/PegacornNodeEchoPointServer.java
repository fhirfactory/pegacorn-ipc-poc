package net.fhirfactory.poc.jgroups;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jgroups.*;
import org.jgroups.protocols.relay.RELAY2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(
        name="EchoPointServer",
        description="Prints Called Content and Responds!"
)
public class PegacornNodeEchoPointServer implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(PegacornNodeEchoPointServer.class);

    private JChannel echoServer;
    private ObjectMapper jsonMapper;
    private RELAY2 relay;

    @CommandLine.Option(names = {"-n", "--nodeName"})
    private String nodeName;

    @CommandLine.Option(names = {"-c", "--clusterName"})
    private String clusterName;

    @CommandLine.Option(names = {"-f", "--configFile"})
    private String configFile;

    protected static final long RPC_UNICAST_TIMEOUT = 1000;
    protected static final long RPC_MULTICAST_TIMEOUT = 5000;

    @Override
    public void run() {
        LOG.debug("Pegacorn Node Echo Point Client");
        jsonMapper = new ObjectMapper();
        initialiseJGroupsChannel();
        eventLoop();
        System.exit(0);
    }

    public JChannel getEchoServer() {
        return echoServer;
    }


    public Address getMyAddress() {
        return (getEchoServer().getAddress());
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public String getConfigFile() {
        return configFile;
    }

    public RELAY2 getRelay() {
        return relay;
    }

    void initialiseJGroupsChannel(){
        try {
            LOG.debug(".initialiseJGroupsChannel(): Entry");
            LOG.trace(".initialiseJGroupsChannel(): initialise the JChannel, configFile-> "+configFile+", nodeName-> "+ nodeName);
            this.echoServer = new JChannel(configFile);
            LOG.trace(".initialiseJGroupsChannel(): Channel initialised, now setting channel name");
            getEchoServer().setName(nodeName);
            getEchoServer().setDiscardOwnMessages(true);
            this.relay=getEchoServer().getProtocolStack().findProtocol(RELAY2.class);
            if(this.relay != null){
                getRelay().setRouteStatusListener(new PegacornNodeRouteListener(getEchoServer()));
                getRelay().relayMulticasts(true);
            }
            getEchoServer().setReceiver(new PegacornNodeEchoMessageReceiver(getEchoServer(), getRelay()));
            LOG.trace(".initialiseJGroupsChannel(): connect to cluster");
            getEchoServer().connect(clusterName);
            LOG.debug(".initialiseJGroupsChannel(): Exit, initialisation complete");
        } catch(Exception ex){
            LOG.error(".initialiseJGroupsChannel(): Error --> " + ex.toString());
            getEchoServer().close();
        }
    }

    private void eventLoop() {
        while(true) {
            try {
                Thread.sleep(10000);
                multicastBroadcast();
                unicastTransmit();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void multicastBroadcast(){
        String messageString = "Multicast Message:From(" + getEchoServer().getClusterName() + ":" +getEchoServer().getName() + ")";
        try {
            getEchoServer().send(null, messageString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unicastTransmit(){
        LOG.debug(".unicastScan(): Entry");
        List<Address> addressList = getEchoServer().getView().getMembers();
        LOG.info("--- Unicast Scan: Start ---");
        for(Address currentAddress: addressList){
            if(getMyAddress().equals(currentAddress)){
                LOG.debug(".unicastScan(): ScanAddress->{}... is me, not calling myself!", currentAddress);
            } else {
                String messageString = "Unicast Message:From(" + getEchoServer().getClusterName() + ":" +getEchoServer().getName() + ")";
                LOG.info(".unicastScan(): ScanAddress->{}", currentAddress);
                try {
                    getEchoServer().send(currentAddress, messageString);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        LOG.info("--- Unicast Scan: End ---");
        LOG.debug(".unicastScan(): Entry");
    }
}
