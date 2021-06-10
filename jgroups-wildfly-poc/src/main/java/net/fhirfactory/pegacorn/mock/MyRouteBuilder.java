package net.fhirfactory.pegacorn.mock;

import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.component.dsl.JgroupsComponentBuilderFactory;
import org.jgroups.Address;
import org.jgroups.JChannel;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MyRouteBuilder extends RouteBuilder {

    @Inject
    TimeInformationGenerator timeInformationGenerator;

    @Inject
    CamelContext ctx;

    @Inject
    PegacornNodeEchoPointServer endpointServer;

    Address actualAddress = null;

    @PostConstruct
    private void initialise(){
        endpointServer.initialise();
    }

    @Override
    public void configure() throws Exception {

    	from("timer:EndpointHeartbeat?delay=10000&period=5000")
                .bean(timeInformationGenerator, "tellMeTheTime(\"EndpointServerInstance\")")
                .bean(endpointServer, "performScan");
    }

}
