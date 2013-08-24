/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.network.protocol.commands;

import ASN1.ASN1DecoderFail;
import ASN1.ASNObj;
import ASN1.Decoder;
import ASN1.Encoder;
import ddgame.network.protocol.datatypes.PeerData;
import ddgame.network.protocol.datatypes.PeerGID;

/**
 *
 * @author rknowles
 */
public class AddPeer extends ASNObj{
    
//--Tells peer at destination to add a new peer
//AddPeer :== [APPLICATION 2] SEQUENCE {
//    timeSent    INTEGER,
//    dest        PeerGID,
//    data        PeerData;
//}
  
    public static final byte asnType = Encoder.buildASN1byteType(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte) 2);
    
    private long timeSent;
    private PeerGID dest;
    private PeerData peer;
    
    //Getters
    public long getTime(){return this.timeSent;}
    public PeerGID getDestination(){return this.dest;}
    public PeerData getPeerData(){return this.peer;}
    
    //Constructors
    public AddPeer(){}
    
    public AddPeer(PeerGID dest, PeerData peer){
        this.timeSent = System.currentTimeMillis();
        this.dest = dest;
        this.peer = peer;
    }
    
    //Encoding/Decoding
    @Override
    public Encoder getEncoder() {
        Encoder apEncoder = new Encoder().initSequence();
        apEncoder.setASN1Type(asnType);
        
        //Add timeSent INTEGER value to encoder
        apEncoder.addToSequence(new Encoder(timeSent));
        
        //Add destination PeerGID object to encoder
        apEncoder.addToSequence(dest.getEncoder());
                
        //Add data PeerData object to encoder
        apEncoder.addToSequence(peer.getEncoder());
        
        return apEncoder;
    }

    @Override
    public Object decode(Decoder dec) throws ASN1DecoderFail {
        
        Decoder apDecoder = dec.getContent();
        
        this.timeSent = (apDecoder.getFirstObject(true, Encoder.TAG_INTEGER).getInteger()).longValue();
        
        this.dest = (PeerGID) new PeerGID().decode(apDecoder.getFirstObject(true, PeerGID.asnType));
        
        this.peer = (PeerData) new PeerData().decode(apDecoder.getFirstObject(true, PeerData.asnType));
        
        return this;
    }
}
