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

/**
 *
 * @author rknowles
 */
public class DeleteLink extends ASNObj{
    
//--Tells peer at destination to remove a link(if it exists) between two peers
//--Used when disconnection occurs
//DeleteLink :== [APPLICATION 4] SEQUENCE {
//    timeSent    INTEGER,
//    dest        PeerGID,
//    data        PeerLink;
//}
    
    public static final byte asnType = Encoder.buildASN1byteType(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte) 4);
    
    private long timeSent; 
    private PeerGID dest;
    private PeerLink link;
    
    //Getters
    public long getTime(){return this.timeSent;}
    public PeerGID getDestination(){return this.dest;}
    public PeerLink getPeerLink(){return this.link;}
    
    //Constructors
    public DeleteLink(){}
    
    public DeleteLink(PeerGID dest, PeerLink link){
        this.timeSent = System.currentTimeMillis();
        this.dest = dest;
        this.link = link;
    }
    
    //Encoding/Decoding
    @Override
    public Encoder getEncoder() {
        Encoder dlEncoder = new Encoder().initSequence();
        dlEncoder.setASN1Type(asnType);
        
        //Add timeSent INTEGER value to encoder
        dlEncoder.addToSequence(new Encoder(timeSent));
        
        //Add destination PeerGID object to encoder
        dlEncoder.addToSequence(dest.getEncoder());
                
        //Add link PeerLink object to encoder
        dlEncoder.addToSequence(link.getEncoder());
        
        return dlEncoder;
    }

    @Override
    public Object decode(Decoder dec) throws ASN1DecoderFail {
        
        Decoder dlDecoder = dec.getContent();
        
        this.timeSent = (dlDecoder.getFirstObject(true, Encoder.TAG_INTEGER).getInteger()).longValue();
        
        this.dest = (PeerGID) new PeerGID().decode(dlDecoder.getFirstObject(true, PeerGID.asnType));
        
        this.link = (PeerLink) new PeerLink().decode(dlDecoder.getFirstObject(true, PeerLink.asnType));
        
        return this;
    }
}
