/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.network.protocol.datatypes;

import ddgame.network.protocol.datatypes.PeerGID;
import ASN1.ASN1DecoderFail;
import ASN1.ASNObj;
import ASN1.Decoder;
import ASN1.Encoder;

/**
 *
 * @author rknowles
 */
public class PeerLink extends ASNObj implements Cloneable{
    
//--Contains link about two peers
//PeerLink :== SEQUENCE {
//    firstnode   PeerGID,
//    secondnode  PeerGID
//}
    
    public static final byte asnType = Encoder.buildASN1byteType(Encoder.CLASS_UNIVERSAL, Encoder.PC_CONSTRUCTED, Encoder.TAG_SEQUENCE);
    
    private PeerGID node1 = null;
    private PeerGID node2 = null;
    
    //Getters
    public PeerGID getFirstNode(){return this.node1;}
    public String getFirstNodeAsString(){return this.node1.getPeerGID();}
    public PeerGID getSecondNode(){return this.node2;}
    public String getSecondNodeAsString(){return this.node2.getPeerGID();}
    public String getOppositePeer(String peerGID){
        if(this.node1.getPeerGID().equals(peerGID)){
            return this.node2.getPeerGID();
        }
        
        if(this.node2.getPeerGID().equals(peerGID)){
            return this.node1.getPeerGID();
        }
        
        //Invalid peerGID, not contained in this PeerLink
        return null;
    }
    
    //Constructors
    public PeerLink(){}
    
    public PeerLink(PeerGID n1, PeerGID n2){
        this.node1 = n1;
        this.node2 = n2;
    }
    
    //Get string representation of data for debugging
    public String getString(){
        return "PeerLink: ("+this.node1.getString()+", "+this.node2.getString()+")";
    }
    
    //Implement Cloneable Interface
    @Override
    public PeerLink clone(){
        return new PeerLink(this.node1, this.node2);
    }
    
    //Comparison operation
    public boolean equals(PeerLink pl){
        //if this.node1==pl.node1 && this.node2==pl.node2 is false
        if( !(this.node1.equals(pl.getFirstNode())&&this.node2.equals(pl.getSecondNode())) ) {
            //and if this.node1==pl.node2 && this.node2==pl.node1 is false
            if( !(this.node1.equals(pl.getSecondNode())&&this.node2.equals(pl.getFirstNode())) ) {
                //return false
                return false;
            }
        }
        //else return true
        return true;
    }
    
    //Encoding/Decoding
    @Override
    public Encoder getEncoder() {
        Encoder linkEncoder = new Encoder().initSequence();
        
        //Add node1 to encoder
        linkEncoder.addToSequence(node1.getEncoder());
        
        //Add node2 to encoder
        linkEncoder.addToSequence(node2.getEncoder());
        
        return linkEncoder;
    }

    @Override
    public Object decode(Decoder dec) throws ASN1DecoderFail {
        Decoder plDecoder = dec.getContent();
        
        this.node1 = (PeerGID) new PeerGID().decode(plDecoder.getFirstObject(true));
        
        this.node2 = (PeerGID) new PeerGID().decode(plDecoder.getFirstObject(true));
        
        return this;
    }
    
}
