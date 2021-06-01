package net.fhirfactory.poc.jgroups;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PegacornNodeEchoRPCPacket {
    private static final Logger LOG = LoggerFactory.getLogger(PegacornNodeEchoRPCPacket.class);

    private String targetAddressName;
    private String sourceAddressName;
    private String payload;

    public PegacornNodeEchoRPCPacket(){
        targetAddressName = null;
        sourceAddressName = null;
        payload = null;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getTargetAddressName() {
        return targetAddressName;
    }

    public void setTargetAddressName(String targetAddressName) {
        this.targetAddressName = targetAddressName;
    }

    public String getSourceAddressName() {
        return sourceAddressName;
    }

    public void setSourceAddressName(String sourceAddressName) {
        this.sourceAddressName = sourceAddressName;
    }

    @Override
    public String toString() {
        return "PegacornNodeEchoRPCPacket{" +
                "targetAddressName=" + targetAddressName +
                ", sourceAddressName=" + sourceAddressName +
                ", payload='" + payload + '\'' +
                '}';
    }
}
