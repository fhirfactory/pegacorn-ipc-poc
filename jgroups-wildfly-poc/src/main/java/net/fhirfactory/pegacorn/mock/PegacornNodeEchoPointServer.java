package net.fhirfactory.pegacorn.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.apache.commons.lang3.StringUtils;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.RspList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@ApplicationScoped
public class PegacornNodeEchoPointServer {
    private static final Logger LOG = LoggerFactory.getLogger(PegacornNodeEchoPointServer.class);

    private boolean initialised;

    private RpcDispatcher rpcDispatcher;
    private ObjectMapper jsonMapper;
    private JChannel echoServer;
    protected static final long RPC_UNICAST_TIMEOUT = 10000;
    protected static final long RPC_MULTICAST_TIMEOUT = 20000;

    @Inject
    private CamelContext camelContext;

    public PegacornNodeEchoPointServer(){
        initialised = false;
    }

    @PostConstruct
    public void initialise() {
        LOG.debug(".initialise(): Pegacorn Node Echo Point Client");
        if(!initialised) {
            jsonMapper = new ObjectMapper();
            initialiseJGroupsChannel();
            initialised = true;
        }
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

    void initialiseJGroupsChannel(){
        try {
            LOG.info(".initialiseJGroupsChannel(): Entry");
            String stackName = System.getenv("MY_STACK");
            if(StringUtils.isBlank(stackName)){
                LOG.error("Could not resolve stackName, stackName->{}", stackName);
            }
            this.echoServer = new JChannel(stackName);
            LOG.info(".initialiseJGroupsChannel(): Channel initialised, now setting channel name");
            String name = "node-" + UUID.randomUUID().toString();
            getEchoServer().name(name);
            getEchoServer().setDiscardOwnMessages(true);
            this.rpcDispatcher = new RpcDispatcher(getEchoServer(), this);
            LOG.info(".initialiseJGroupsChannel(): connect to cluster");
            getEchoServer().connect("sitea");
            LOG.info(".initialiseJGroupsChannel(): Exit, initialisation complete");
        } catch(Exception ex){
            LOG.error(".initialiseJGroupsChannel(): Error --> " + ex.toString());
            getEchoServer().close();
        }
    }

    public void performScan(){
        // 1st, do a MultiCast test....
        multicastScan();
        // Now do Unicast...
        unicastScan();
    }

    private void multicastScan(){
        LOG.debug(".multicastScan(): Entry");
        try {
            LOG.info("--- Multicast Scan: Start ---");
            Object objectSet[] = new Object[1];
            Class classSet[] = new Class[1];
            objectSet[0] = "(Multicast Request ("+ Instant.now().getEpochSecond() +")";
            classSet[0] = String.class;
            RequestOptions requestOptions = new RequestOptions( ResponseMode.GET_ALL, RPC_MULTICAST_TIMEOUT, false);
            RspList<Object> rsps = rpcDispatcher.callRemoteMethods(null, "endpointPing", objectSet, classSet, requestOptions);
            rsps.forEach((key, val) -> LOG.info("<< " + val.getValue() + " from " + key));
            LOG.info("--- Multicast Scan: End ---");
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
        LOG.info("--- Unicast Scan: Start ---");
        Long fullScanTimeStart = Date.from(Instant.now()).getTime();
        int endpointCount = 0;
        for(Address currentAddress: addressList){
            if(getMyAddress().equals(currentAddress)){
                LOG.info(".unicastScan(): ScanAddress->{}... is me, not calling myself!", currentAddress);
            } else {
                endpointCount += 1;
                LOG.info(".unicastScan(): ScanAddress->{}", currentAddress);
                PegacornNodeEchoRPCPacket finalOutcome = null;
                Long timeStart = Date.from(Instant.now()).getTime();
                int endpointIteration = 0;
                for(int counter=0; counter < 1000; counter += 1) {
                    PegacornNodeEchoRPCPacket outcome = executeRPC(currentAddress);
                    finalOutcome = outcome;
                    endpointIteration += 1;
                }
                Long timeFinish = Date.from(Instant.now()).getTime();
                //LOG.info(".unicastScan(): ", finalOutcome);
                LOG.info("EndPoint:{}, iterations:{}, time:{}", finalOutcome.getSourceAddressName(), endpointIteration, (timeFinish-timeStart));
            }
        }
        Long fullScanTimeEnd = Date.from(Instant.now()).getTime();
        LOG.info("--- Unicast Scan: End ---, EndpointCount->{}, Duration->{}", endpointCount, (fullScanTimeEnd-fullScanTimeStart));
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
            PegacornNodeEchoRPCPacket responsePacket = new PegacornNodeEchoRPCPacket();
            responsePacket.setTargetAddressName(targetAddress.toString());
            responsePacket.setSourceAddressName(getEchoServer().getAddress().toString());
            responsePacket.setPayload("Error (NoSuchMethodException)" + e.getMessage());
            return(responsePacket);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error(".executeRPC: Error (GeneralException) ->{}", e.getMessage());
            PegacornNodeEchoRPCPacket responsePacket = new PegacornNodeEchoRPCPacket();
            responsePacket.setTargetAddressName(targetAddress.toString());
            responsePacket.setSourceAddressName(getEchoServer().getAddress().toString());
            responsePacket.setPayload("Error (GeneralException)" + e.getMessage());
            return(responsePacket);
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
            LOG.trace(".scanAnswer(): {}", responsePacket);
            String responseString = packetToString(responsePacket);
            LOG.debug(".scanAnswer(): Exit, responseString->{}", responseString);
            return (responseString);
        }
    }

    private String packetToString(PegacornNodeEchoRPCPacket packet){
        LOG.debug(".packetToString(): Entry, packet->{}", packet);
        if(packet == null){
            LOG.debug(".packetToString(): packet is null");
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
        if(StringUtils.isBlank(packetString)){
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
