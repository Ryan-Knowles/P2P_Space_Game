/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.network.protocol.datatypes;

import ASN1.ASN1DecoderFail;
import ASN1.ASNObj;
import ASN1.Decoder;
import ASN1.Encoder;
import java.util.ArrayList;

/**
 *
 * @author rknowles
 */
public class PeerMap extends ASNObj{
//--Sequence of all node information and all links between those nodes
//PeerMap :== SEQUENCE {
//  peerInfo    SEQUENCE OF PeerData,
//  links       SEQUENCE OF PeerLink;
//}
       
    public static final byte asnType = Encoder.buildASN1byteType(Encoder.CLASS_UNIVERSAL, Encoder.PC_CONSTRUCTED, Encoder.TAG_SEQUENCE);
    
    private ArrayList<PeerData> peerDataList;
    private ArrayList<PeerLink> peerLinkList;
    
    //Getters
    public ArrayList<PeerData> getPeerDataList(){return this.peerDataList;}
    public ArrayList<PeerLink> getPeerLinkList(){return this.peerLinkList;}
    
    //Constructors
    public PeerMap(){}
    
    public PeerMap(ArrayList<PeerData> pdList, ArrayList<PeerLink> plList){
        this.peerDataList = pdList;
        this.peerLinkList = plList;
    }
    
    //Comparison operation
    public boolean equals(PeerMap pm){
        //If this.peerDataList and pm.peerDataList do not contain
        //the exact same list of elements (in any order), return false
        if(!(this.peerDataList.containsAll(pm.getPeerDataList())&&pm.getPeerDataList().containsAll(this.peerDataList))){
            return false;
        }
        //If this.peerLinkList and pm.peerLinkList do not contain
        //the exact same list of elements (in any order), return false
        if(!(this.peerLinkList.containsAll(pm.getPeerLinkList())&&pm.getPeerLinkList().containsAll(this.peerLinkList))){
            return false;
        } 
        
        return true;
    }
    
    @Override
    public Encoder getEncoder() {
        Encoder peerMapEncoder = new Encoder().initSequence();
        
        //Add SEQUENCE OF PeerData to encoder
        Encoder pdEncoder = new Encoder().initSequence();
        
        for(PeerData pd: peerDataList){
            pdEncoder.addToSequence(pd.getEncoder());
        }
        peerMapEncoder.addToSequence(pdEncoder);
        
        //Add SEQUENCE OF PeerLink to encoder
        Encoder plEncoder = new Encoder().initSequence();
        
        for(PeerLink pl: peerLinkList){
            plEncoder.addToSequence(pl.getEncoder());
        }
        peerMapEncoder.addToSequence(plEncoder);
        
        return peerMapEncoder;
    }

    @Override
    public Object decode(Decoder dec) throws ASN1DecoderFail {
        Decoder pmDecoder = dec.getContent();
        
        Decoder pdList = pmDecoder.getFirstObject(true);
        this.peerDataList = pdList.<PeerData>getSequenceOfAL(PeerData.asnType, new PeerData());
        Decoder plList = pmDecoder.getFirstObject(true);
        this.peerLinkList = plList.<PeerLink>getSequenceOfAL(PeerLink.asnType, new PeerLink());
        
        return this;
    }
}
