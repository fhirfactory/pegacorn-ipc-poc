package net.fhirfactory.pegacorn.mock;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.View;

import javax.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class TimeInformationGenerator {

    public String tellMeTheTime(String descriminator){
        Date nowDate = Date.from(Instant.now());
        String nowDateString = "From Ladon (contenxt --> "+ descriminator + "): Time is: " + nowDate.toString();
        //
        //
        //

        return(nowDateString);
    }
}
