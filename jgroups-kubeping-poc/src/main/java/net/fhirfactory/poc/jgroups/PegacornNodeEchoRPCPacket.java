package net.fhirfactory.poc.jgroups;

public class PegacornNodeEchoRPCPacket {

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
