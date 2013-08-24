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
import ddgame.network.protocol.datatypes.PeerLink;
import table.peer;

/**
 *
 * @author rknowles
 */
public class AddLink extends ASNObj{
    
//--Tells peer at destination to add a new link between two peers
//AddLink :== [APPLICATION 3] SEQUENCE {
//    timeSent    INTEGER,
//    dest        PeerGID,
//    data        PeerLink;
//}
    
    public static final byte asnType = Encoder.buildASN1byteType(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte) 3);
    
    private long timeSent;
    private PeerGID dest;
    private PeerLink link;
    
    //Getters
    public long getTime(){return this.timeSent;}
    public PeerGID getDestination(){return this.dest;}
    public PeerLink getPeerLink(){return this.link;}
    
    //Constructors
    public AddLink(){}
    
    public AddLink(PeerGID dest, PeerLink link){
        this.timeSent = System.currentTimeMillis();
        this.dest = dest;
        this.link = link;
    }
    
    //Encoding/Decoding
    @Override
    public Encoder getEncoder() {
        Encoder alEncoder = new Encoder().initSequence();
        alEncoder.setASN1Type(asnType);
        
        //Add timeSent INTEGER value to encoder
        alEncoder.addToSequence(new Encoder(timeSent));
        
        //Add destination PeerGID object to encoder
        alEncoder.addToSequence(dest.getEncoder());
                
        //Add link PeerLink object to encoder
        alEncoder.addToSequence(link.getEncoder());
        
        return alEncoder;
    }

    @Override
    public Object decode(Decoder dec) throws ASN1DecoderFail {
        
        Decoder alDecoder = dec.getContent();
        
        this.timeSent = (alDecoder.getFirstObject(true, Encoder.TAG_INTEGER).getInteger()).longValue();
        
        this.dest = (PeerGID) new PeerGID().decode(alDecoder.getFirstObject(true, PeerGID.asnType));
        
        this.link = (PeerLink) new PeerLink().decode(alDecoder.getFirstObject(true, PeerLink.asnType));
        
        return this;
    }
}
