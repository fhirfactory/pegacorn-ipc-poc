package net.fhirfactory.poc.jgroups;

import org.jgroups.JChannel;

import org.jgroups.protocols.relay.RouteStatusListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PegacornNodeRouteListener implements RouteStatusListener {
    private static final Logger LOG = LoggerFactory.getLogger(PegacornNodeRouteListener.class);

    private JChannel channel;

    public PegacornNodeRouteListener(JChannel channel){
        this.channel = channel;
    }

    public JChannel getChannel() {
        return channel;
    }

    @Override
    public void sitesUp(String... sites) {
        String siteList = String.join(", ", sites);
        LOG.info(".sitesUp(): {}: site(s) {} Came Up", getChannel().getAddress(), siteList);
    }

    @Override
    public void sitesDown(String... sites) {
        String siteList = String.join(", ", sites);
        LOG.info(".sitesDown(): {}: site(s) {} Went down", getChannel().getAddress(), siteList);
    }
}
