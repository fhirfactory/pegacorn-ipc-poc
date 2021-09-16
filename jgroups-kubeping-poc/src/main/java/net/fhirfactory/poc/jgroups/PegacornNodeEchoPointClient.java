package net.fhirfactory.poc.jgroups;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(
        name="EchoPointClient",
        description="Sends a message!!!!"
)
public class PegacornNodeEchoPointClient implements Runnable {

    private Address actualAddress;
    private JChannel echoClient;
    private RpcDispatcher rpcDispatcher;
    private boolean cantFindEndpoint = false;

    @CommandLine.Option(names = {"-t", "--target"})
    private String targetNode;

    @CommandLine.Option(names = {"-n", "--nodeName"})
    private String nodeName;

    @CommandLine.Option(names = {"-c", "--cluster"})
    private String site;

    @CommandLine.Option(names = {"-m", "--message"})
    private String messageString;

    @CommandLine.Option(names = {"-f", "--configFile"})
    private String configFile;

    void initialiseJGroupsChannel(){
        try {
            System.out.println(".initialiseJGroupsChannel(): Entry");
            this.echoClient = new JChannel(configFile).name(nodeName);
            this.echoClient.connect(site);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            View view = this.echoClient.view();
            List<Address> members = view.getMembers();
            for(Address member: members) {
                System.out.println(".initialiseJGroupsChannel(): member-> " + member);
                if (targetNode.contentEquals(member.toString())) {
                    System.out.println(".initialiseJGroupsChannel(): Found Target Server Endpoint");
                    this.actualAddress = member;
                    break;
                }
            }
            this.rpcDispatcher = new RpcDispatcher(this.echoClient,null);
        } catch(Exception ex){
            System.out.println(".initialiseJGroupsChannel(): Error --> " + ex.toString());
        }
    }

    @Override
    public void run() {
        System.out.println("Pegacorn Node Echo Point Client");
        this.cantFindEndpoint = false;
        initialiseJGroupsChannel();
        System.out.println("Weird!");
        if(this.cantFindEndpoint){
            System.out.println("sniff, sniff, it doesn't work!!!");
        } else {
 //           for (int counter = 0; counter < 1000; counter += 1) {
                sendMessage();
 //           }
        }
        System.exit(0);
    }

    private void sendMessage(){
        System.out.println(".sendMessage(): Entry, message-> " + messageString);
        Object objectSet[] = new Object[1];
        Class classSet[] = new Class[1];
        objectSet[0] = messageString;
        classSet[0] = String.class;
        RequestOptions requestOptions = new RequestOptions( ResponseMode.GET_FIRST, 5000, true);
        try {
            System.out.println(".sendMessage(): Sending message");
            if(this.actualAddress != null) {
                String response = rpcDispatcher.callRemoteMethod(this.actualAddress, "receiveMessage", objectSet, classSet, requestOptions);
                System.out.println(".sendMessage(): Response --> " + response);
            } else {
                System.out.println(".sendMessage(): Couldn't find endpoint");
                cantFindEndpoint = true;
            }
        } catch(Exception ex){
            ex.printStackTrace();
            System.out.println(".sendMessage(): Error --> " + ex.toString());
        }
        System.out.println(".sendMessage(): Exit");
    }
}
