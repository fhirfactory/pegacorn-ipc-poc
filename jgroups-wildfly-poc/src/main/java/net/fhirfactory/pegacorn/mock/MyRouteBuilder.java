package net.fhirfactory.pegacorn.mock;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.component.dsl.JgroupsComponentBuilderFactory;
import org.apache.camel.component.jgroups.JGroupsComponent;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.View;

import java.util.List;

@ApplicationScoped
public class MyRouteBuilder extends RouteBuilder {

    @Inject
    TimeInformationGenerator timeInformationGenerator;

    @Inject
    CamelContext ctx;

    @Inject
    MyTargetWriter targetedWriter;

    Address actualAddress = null;

    @Override
    public void configure() throws Exception {
        JChannel ladonToIris = new JChannel().name("IrisClient");
        JgroupsComponentBuilderFactory.jgroups().channel(ladonToIris).register(ctx, "LadonToIris");

        JChannel irisToLadon = new JChannel().name("LadonServer");
        JgroupsComponentBuilderFactory.jgroups().channel(irisToLadon).register(ctx, "IrisToLadon");

    	from("timer:LadonIs")
                .bean(timeInformationGenerator, "tellMeTheTime(\"LadonToIris\")")
                .to("LadonToIris:ladon-iris")
                .bean(targetedWriter, "sendMessage");

    	from("IrisToLadon:ladon-iris?enableViewMessages=true")
                .log(LoggingLevel.INFO, "Ladon (from Iris): Content on the wire --> ${body}");
    }

}
