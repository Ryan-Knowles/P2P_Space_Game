/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.network.protocol.commands;

import ASN1.ASN1DecoderFail;
import ASN1.Decoder;
import ASN1.Encoder;
import ddgame.network.protocol.datatypes.PeerData;

/**
 *
 * @author rknowles
 */
public class JoinRequest extends ASN1.ASNObj {

//--Request to join game session containing PeerData about Requester
//JoinRequest :== [APPLICATION 0] SEQUENCE{
//    timeSent    INTEGER,
//    data        PeerData;
//}

    public static final byte asnType = Encoder.buildASN1byteType(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte) 0);
    
    private long timeSent;
    private PeerData requestor;
    
    //Getters
    public long getTime(){return this.timeSent;}
    public PeerData getPeerData(){return this.requestor;}
    
    //Constructors
    public JoinRequest(){}
    
    public JoinRequest(PeerData requestor){
        this.timeSent = System.currentTimeMillis();
        this.requestor = requestor;
    }
    
    //Get string representation of data for debugging
    public String getString(){
        return "JoinRequest: ( time: "+this.timeSent+", "+this.requestor.getString()+")";
    }
    
    //Encoding/Decoding
    @Override
    public Encoder getEncoder() {
        Encoder jrEncoder = new Encoder().initSequence();
        jrEncoder.setASN1Type(asnType);
        
        //Add timeSent INTEGER value to encoder
        jrEncoder.addToSequence(new Encoder(timeSent));
        
        //Add PeerData object to encoder
        jrEncoder.addToSequence(requestor.getEncoder());
        
        return jrEncoder;
    }

    @Override
    public Object decode(Decoder dec) throws ASN1DecoderFail {
        
        Decoder jrDecoder = dec.getContent();
        
        this.timeSent = (jrDecoder.getFirstObject(true, Encoder.TAG_INTEGER).getInteger()).longValue();
        
        this.requestor = (PeerData) new PeerData().decode(jrDecoder.getFirstObject(true, PeerData.asnType));
        
        return this;
    }
    
}
