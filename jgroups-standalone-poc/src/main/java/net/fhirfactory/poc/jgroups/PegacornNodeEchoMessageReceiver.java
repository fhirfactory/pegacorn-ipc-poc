package net.fhirfactory.poc.jgroups;

import org.jgroups.*;
import org.jgroups.protocols.relay.RELAY2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PegacornNodeEchoMessageReceiver implements Receiver {
    private static final Logger LOG = LoggerFactory.getLogger(PegacornNodeEchoMessageReceiver.class);

    protected RELAY2 relay;

    public PegacornNodeEchoMessageReceiver(JChannel channel, RELAY2 relay){
        super();
        LOG.debug(".Constructor(): Entry");
        this.relay = relay;
    }

    @Override
    public void receive(Message msg) {
//        LOG.debug(".receive(): Entry");
        String msgContent = (String)msg.getObject();
        LOG.info(".receive(): Message Received: Source->{} , Content->{}",  msg , msgContent);
    }

    @Override
    public void viewAccepted(View view) {
//        LOG.debug(".viewAccepted(): Entry");
        LOG.debug(".viewAccepted(): Received view -> ", print(view));
    }

    public void printTopology() {
        LOG.info(relay.printTopology(true));
        LOG.info("------------ Sites --------------");
        for(String site: relay.getSites()){
            LOG.info("Relay Site: {}", site);
        }
        LOG.info("----------- Members -------------");
        for(Address localMember : relay.members()){
            LOG.info("Relay Local Address:{}, Member:{}", relay.getLocalAddress(), localMember);
        }
    }

    protected static String print(View view) {
        StringBuilder sb=new StringBuilder();
        boolean first=true;
        sb.append(view.getClass().getSimpleName() + ": ").append(view.getViewId()).append(": ");
        for(Address mbr: view.getMembers()) {
            if(first)
                first=false;
            else
                sb.append(", ");
            sb.append(mbr);
        }
        return sb.toString();
    }
}
