package net.fhirfactory.poc.jgroups;

import picocli.CommandLine;

@CommandLine.Command(
        name="PegacornNode",
        description="Pegacorn JGroups Test Node",
        subcommands = {PegacornNodeEchoPointServer.class, PegacornNodeEchoPointClient.class}
)
public class PegacornNodeCLI implements Runnable {
    public static void main(String[] args) {
        CommandLine.run(new PegacornNodeCLI(), args);
    }

    @Override
    public void run() {
        System.out.println("The Pegacorn Test Node");
    }

}
