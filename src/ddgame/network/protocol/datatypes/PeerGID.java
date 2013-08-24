/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.network.protocol.datatypes;

import ASN1.ASN1DecoderFail;
import ASN1.ASNObj;
import ASN1.Decoder;
import ASN1.Encoder;

/**
 *
 * @author rknowles
 */
public class PeerGID extends ASNObj{

//PeerGID :== UTF8String;
    
    public static final byte asnType = Encoder.buildASN1byteType(Encoder.CLASS_UNIVERSAL, Encoder.PC_PRIMITIVE, Encoder.TAG_UTF8String);
    
    private String peerGID = null;
    
    //Getter
    public String getPeerGID(){return this.peerGID;}
    
    //Constructors
    public PeerGID(){}
    
    public PeerGID(String peerGID){
        this.peerGID = peerGID;
    }
    
    //Get string representation of data for debugging
    public String getString(){
        if(this.peerGID==null)return "PeerGID: <null>";
        return "PeerGID: "+this.peerGID.substring(0, 20);
    }
    
    //Comparison operation
    public boolean equals(PeerGID pg){
        return this.peerGID.equals(pg.getPeerGID());
    }
    
    //Encoder/Decoder
    @Override
    public Encoder getEncoder() {
        Encoder stringEnc = new Encoder(this.peerGID);
        return stringEnc;
    }

    @Override
    public Object decode(Decoder dec) throws ASN1DecoderFail {
        this.peerGID = dec.getString(dec.getTypeByte());
        return this;
    }
    
}
