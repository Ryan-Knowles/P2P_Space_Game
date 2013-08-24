/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.network.protocol.commands;

import ASN1.ASN1DecoderFail;
import ASN1.ASNObj;
import ASN1.Decoder;
import ASN1.Encoder;
import ddgame.network.protocol.datatypes.PeerGID;
import ddgame.network.protocol.datatypes.PeerLink;
import ddgame.network.protocol.datatypes.ShipData;

/**
 *
 * @author rknowles
 */
public class UpdateShip extends ASNObj{
    
//--Update physics of ship state
//UpdateShip :== [APPLICATION 5] SEQUENCE {
//    timeSent    INTEGER,
//    dest        PeerGID,
//    shipData    ShipData;
//    
//}

    public static final byte asnType = Encoder.buildASN1byteType(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte) 5);
    
    private long timeSent;
    private PeerGID dest;
    private ShipData shipData;
    
    //Getters
    public long getTime(){return this.timeSent;}
    public PeerGID getDestination(){return this.dest;}
    public ShipData getShipData(){return this.shipData;}
    
    //Constructors
    public UpdateShip(){}
    
    public UpdateShip(PeerGID dest, ShipData shipData){
        this.timeSent = System.currentTimeMillis();
        this.dest = dest;
        this.shipData = shipData;
    }
    
    public UpdateShip(String dest, ShipData shipData){
        this.timeSent = System.currentTimeMillis();
        this.dest = new PeerGID(dest);
        this.shipData = shipData;
    }
    
    //Get string representation of data for debugging
    public String getString(){
        String header = "UpdateShip: (";
        String time =  "time: "+this.timeSent;
        String destination = "\n\tdestination: "+this.dest.getString();
        String shipinfo = "\n\t"+this.shipData.getString();
        String footer = ")\n";
        return header+time+destination+shipinfo+footer;
    }
    
    
    //Encoding/Decoding
    @Override
    public Encoder getEncoder() {
        Encoder usEncoder = new Encoder().initSequence();
        usEncoder.setASN1Type(asnType);
        
        //Add timeSent INTEGER value to encoder
        usEncoder.addToSequence(new Encoder(timeSent));
        
        //Add destination PeerGID object to encoder
        usEncoder.addToSequence(dest.getEncoder());
                
        //Add shipData ShipData object to encoder
        usEncoder.addToSequence(shipData.getEncoder());
        
        return usEncoder;
    }

    @Override
    public Object decode(Decoder dec) throws ASN1DecoderFail {
        
        Decoder usDecoder = dec.getContent();
        
        this.timeSent = (usDecoder.getFirstObject(true, Encoder.TAG_INTEGER).getInteger()).longValue();
        
        this.dest = (PeerGID) new PeerGID().decode(usDecoder.getFirstObject(true, PeerGID.asnType));
        
        this.shipData = (ShipData) new ShipData().decode(usDecoder.getFirstObject(true, ShipData.asnType));
        
        return this;
    }

}
