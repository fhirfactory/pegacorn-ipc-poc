package net.fhirfactory.poc.jgroups;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.MembershipListener;
import org.jgroups.View;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.RspList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@CommandLine.Command(
        name="EchoPointServer",
        description="Prints Called Content and Responds!"
)
public class PegacornNodeEchoPointServer implements Runnable, MembershipListener {
    private static final Logger LOG = LoggerFactory.getLogger(PegacornNodeEchoPointServer.class);

    private JChannel echoServer;
    private RpcDispatcher rpcDispatcher;
    private ObjectMapper jsonMapper;

    private String nodeName;

    private String clusterName;

    protected static final long RPC_UNICAST_TIMEOUT = 3000;
    protected static final long RPC_MULTICAST_TIMEOUT = 5000;

    @Override
    public void run() {

        PropertiesRetriever propertiesRetriever = new PropertiesRetriever();
        this.nodeName = propertiesRetriever.getMandatoryProperty("MY_PROCESSING_PLANT_NAME") + "." + Long.toHexString(UUID.randomUUID().getLeastSignificantBits());
        this.clusterName = propertiesRetriever.getMandatoryProperty("MY_CLUSTER_NAME");

        LOG.info("Pegacorn Node Echo Point Client");
        jsonMapper = new ObjectMapper();
        initialiseJGroupsChannel();
        eventLoop();
        System.exit(0);
    }

    public JChannel getEchoServer() {
        return echoServer;
    }

    public RpcDispatcher getRpcDispatcher() {
        return rpcDispatcher;
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
        return ("site-a-private-network-ipc.xml");
    }


    @Override
    public void viewAccepted(View newView) {
        LOG.warn("JGroup Cluster Membership Change-----------------------------------(Start)--------");
        List<Address> addressList = newView.getMembers();
        if(getEchoServer() != null) {
           LOG.warn("JGroupsCluster->{}", getEchoServer().getClusterName());
        } else {
            LOG.warn("JGroupsCluster still Forming");
        }
        int counter = 0;
        for(Address currentAddress: addressList){
            LOG.warn("["+counter+"]"+" member -> {}", currentAddress);
            counter += 1;
        }
        LOG.warn("JGroup Cluster Membership Change-----------------------------------(End)----------");
    }



    void initialiseJGroupsChannel(){
        try {
            LOG.info(".initialiseJGroupsChannel(): Entry");
            LOG.info(".initialiseJGroupsChannel(): initialise the JChannel, configFile-> "+getConfigFile()+", nodeName-> "+ nodeName);
            this.echoServer = new JChannel(getConfigFile());
            LOG.info(".initialiseJGroupsChannel(): Channel initialised, now setting channel name");
            getEchoServer().name(nodeName);
            getEchoServer().setDiscardOwnMessages(true);
            this.rpcDispatcher = new RpcDispatcher(getEchoServer(), this);
            this.rpcDispatcher.setMembershipListener(this);
            LOG.info(".initialiseJGroupsChannel(): connect to cluster");
            getEchoServer().connect(clusterName);
            LOG.info(".initialiseJGroupsChannel(): Exit, initialisation complete");
        } catch(Exception ex){
            LOG.error(".initialiseJGroupsChannel(): Error --> " + ex.toString());
            getEchoServer().close();
        }
    }

    private void eventLoop() {
        while(true) {
            try {
                Thread.sleep(30000);
                performScan();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void performScan(){
        // 1st, do a MultiCast test....
        // multicastScan();
        // Now do Unicast...
        unicastScan();
    }

    private void multicastScan(){
        LOG.debug(".multicastScan(): Entry");
        try {
            LOG.debug("--- Multicast Scan: Start ---");
            Object objectSet[] = new Object[1];
            Class classSet[] = new Class[1];
            objectSet[0] = "(Multicast Request ("+ Instant.now().getEpochSecond() +")";
            classSet[0] = String.class;
            RequestOptions requestOptions = new RequestOptions( ResponseMode.GET_ALL, RPC_MULTICAST_TIMEOUT, false);
            RspList<Object> rsps = rpcDispatcher.callRemoteMethods(null, "endpointPing", objectSet, classSet, requestOptions);
            rsps.forEach((key, val) -> LOG.info("<< " + val.getValue() + " from " + key));
            LOG.debug("--- Multicast Scan: End ---");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOG.debug(".multicastScan(): Exit");
    }

    private void unicastScan(){
        LOG.debug(".unicastScan(): Entry");
        List<Address> addressList = getEchoServer().getView().getMembers();
        System.out.println("--- Unicast Scan: Start ---");
        int counter = 0;
        for(Address currentAddress: addressList){
            if(getMyAddress().equals(currentAddress)){
                LOG.debug(".unicastScan(): ScanAddress->{}... is me, not calling myself!", currentAddress);
            } else {
                PegacornNodeEchoRPCPacket outcome = executeRPC(currentAddress);
                String outcomeString = "Pass!";
                if(outcome == null){
                    outcomeString = "Fail!";
                }
                System.out.println("["+ counter + "]"+" member -> " + currentAddress + ", outcome -> " + outcomeString);
                counter += 1;
                LOG.debug("EndPoint Response->{}",outcome);
            }
        }
        System.out.println("--- Unicast Scan: End ---");
        LOG.debug(".unicastScan(): Entry");
    }

    private PegacornNodeEchoRPCPacket executeRPC(Address targetAddress){
        LOG.debug(".executeRPC(): Entry, targetAddress->{}", targetAddress);
        try {
            Object objectSet[] = new Object[1];
            Class classSet[] = new Class[1];
            PegacornNodeEchoRPCPacket messagePacket = new PegacornNodeEchoRPCPacket();
            messagePacket.setPayload("(Unicast Request ("+Instant.now().getEpochSecond()+")::> ["+ targetAddress + "]");
            messagePacket.setSourceAddressName(getEchoServer().getAddress().toString());
            messagePacket.setTargetAddressName(targetAddress.toString());
            String messagePacketAsString = packetToString(messagePacket);
            objectSet[0] = messagePacketAsString;
            classSet[0] = String.class;
            RequestOptions requestOptions = new RequestOptions( ResponseMode.GET_FIRST, RPC_UNICAST_TIMEOUT);
            String response = getRpcDispatcher().callRemoteMethod(targetAddress, "scanAnswer", objectSet, classSet, requestOptions);
            PegacornNodeEchoRPCPacket responsePacket = stringToPacket(response);
            LOG.debug(".executeRPC(): Exit, responsePacket->{}", responsePacket);
            return(responsePacket);
        } catch (NoSuchMethodException e) {
            LOG.error(".executeRPC(): Error (NoSuchMethodException) ->{}", e.getMessage());
            return(null);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error(".executeRPC: Error (GeneralException) ->{}", e.getMessage());
            return(null);
        }
    }

    public String scanAnswer(String msgString) {
        LOG.debug(".scanAnswer(): Entry, msgString->{}", msgString);
        PegacornNodeEchoRPCPacket msg = stringToPacket(msgString);
        if(msg.getSourceAddressName().equals(getMyAddress().toString())){
            LOG.debug(".scanAnswer(): Exit, query is from myself!");
            return(null);
        } else {
            String line = "IncomingSignal[" + msg.getPayload() + "]: ResponseSignal[" + getEchoServer().getName() + "(" + Instant.now().getEpochSecond() + ")]";
            PegacornNodeEchoRPCPacket responsePacket = new PegacornNodeEchoRPCPacket();
            responsePacket.setPayload(line);
            responsePacket.setTargetAddressName(msg.getSourceAddressName());
            responsePacket.setSourceAddressName(getMyAddress().toString());
            LOG.debug(".scanAnswer(): {}", responsePacket);
            String responseString = packetToString(responsePacket);
            LOG.debug(".scanAnswer(): Exit, responseString->{}", responseString);
            return (responseString);
        }
    }

    private String packetToString(PegacornNodeEchoRPCPacket packet){
        LOG.debug(".packetToString(): Entry, packet->{}", packet);
        if(packet == null){
            LOG.info(".packetToString(): packet is null");
            return(null);
        }
        try {
            String packetAsString = jsonMapper.writeValueAsString(packet);
            LOG.debug(".packetToString(): Exit, packetAsString->{}", packetAsString);
            return(packetAsString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            LOG.debug(".packetToString(): Exit, packetAsString->null");
            return(null);
        }
    }

    private PegacornNodeEchoRPCPacket stringToPacket(String packetString){
        LOG.debug(".stringToPacket(): Entry, packetString->{}", packetString);
        if(packetString.isEmpty()){
            PegacornNodeEchoRPCPacket responsePacket = new PegacornNodeEchoRPCPacket();
            responsePacket.setPayload(null);
            responsePacket.setTargetAddressName(getMyAddress().toString());
            responsePacket.setSourceAddressName(getMyAddress().toString());
            LOG.debug(".stringToPacket(): Exit, packetString is empty, returning empty PegacornNodeEchoRPCPacket!");
            return(responsePacket);
        }
        try {
            PegacornNodeEchoRPCPacket packet = jsonMapper.readValue(packetString, PegacornNodeEchoRPCPacket.class);
            LOG.debug(".stringToPacket(): Exit, packet->{}", packet);
            return(packet);
        } catch (IOException e) {
            e.printStackTrace();
            PegacornNodeEchoRPCPacket responsePacket = new PegacornNodeEchoRPCPacket();
            responsePacket.setPayload(null);
            responsePacket.setTargetAddressName(getMyAddress().toString());
            responsePacket.setSourceAddressName(getMyAddress().toString());
            LOG.debug(".stringToPacket(): Exit, IOException, returning empty PegacornNodeEchoRPCPacket!");
            return(responsePacket);
        }
    }

    public String endpointPing(String test){
        return("Ping");
    }
}
