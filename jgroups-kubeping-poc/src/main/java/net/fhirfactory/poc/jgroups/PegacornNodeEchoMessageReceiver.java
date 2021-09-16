package net.fhirfactory.poc.jgroups;

import org.jgroups.*;
import org.jgroups.protocols.relay.RELAY2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PegacornNodeEchoMessageReceiver implements Receiver {
    private static final Logger LOG = LoggerFactory.getLogger(PegacornNodeEchoMessageReceiver.class);
    
    JChannel channel;

    public PegacornNodeEchoMessageReceiver(JChannel channel){
        super();
        LOG.debug(".Constructor(): Entry");
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
        LOG.info("----------- Members -------------");
        channel.get
        for(Address localMember : relay.members()){
            LOG.info("Relay Local Address:{}, Member:{}", localMember);
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
