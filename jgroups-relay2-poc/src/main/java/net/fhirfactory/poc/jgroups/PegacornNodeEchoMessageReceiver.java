package net.fhirfactory.poc.jgroups;

import org.jgroups.*;
import org.jgroups.ReceiverAdapter;
import org.jgroups.protocols.relay.RELAY2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PegacornNodeEchoMessageReceiver extends ReceiverAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(PegacornNodeEchoMessageReceiver.class);

    protected RELAY2 relay;
    protected JChannel channel;

    public PegacornNodeEchoMessageReceiver(JChannel channel, RELAY2 relay){
        super();
        LOG.debug(".Constructor(): Entry");
        this.relay = relay;
        this.channel = channel;
    }

    @Override
    public void receive(Message msg) {
//        LOG.debug(".receive(): Entry");
        String msgContent = (String)msg.getObject();
        LOG.info(".receive(): Message Received: Source->{} , Content->{}",  msg.getSrc() , msgContent);
        Address dst=msg.getDest();
        if(dst == null) {
 //           Message rsp=new Message(msg.getSrc(), "response")
            try {
                this.channel.send(msg.getSrc(), "Response("+msgContent+")");
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void viewAccepted(View view) {
//        LOG.debug(".viewAccepted(): Entry");
        LOG.info(".viewAccepted(): Received view ->{}", print(view));
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
