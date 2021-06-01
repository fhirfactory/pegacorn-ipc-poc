package net.fhirfactory.pegacorn.mock;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class MyTargetWriter {
    private static final Logger LOG = LoggerFactory.getLogger(MyTargetWriter.class);

    Address actualAddress;
    JChannel irisServerClient;

    public MyTargetWriter(){
        this.actualAddress = null;
        this.irisServerClient = null;
    }

    @PostConstruct
    void initialise(){
        try {
            irisServerClient = new JChannel().name("IrisServerClient");
            irisServerClient.connect("ladon-iris");
            View view = irisServerClient.view();
            List<Address> members = view.getMembers();
            for(Address member: members) {
                if ("IrisServer".contentEquals(member.toString())) {
                    LOG.info("Found Server Endpoint");
                    actualAddress = member;
                    break;
                }
            }
        } catch(Exception ex){
            LOG.error("Error --> " + ex.toString());
        }
    }

    public String sendMessage(){
        LOG.info(".sendMessage(): actualAddress --> {}, irisServerClient --> {}", actualAddress, irisServerClient);
        try {
            if(actualAddress != null) {
                LOG.info("Sending --> ");
                irisServerClient.send(actualAddress, "The Target is Found! --> Name:" + actualAddress.toString());
             
            }
        } catch(Exception ex){
            LOG.error("Error --> " + ex.toString());
        }
        return("Done!");
    }

}
