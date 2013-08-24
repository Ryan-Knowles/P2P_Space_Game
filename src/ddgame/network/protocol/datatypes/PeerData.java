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
public class PeerData extends ASNObj implements Cloneable{
    
//--Contains name of peer and peer GID
//PeerData :== SEQUENCE {
//	name PlayerName,
//	gid  PeerGID;
//}
    
    public static final byte asnType = Encoder.buildASN1byteType(Encoder.CLASS_UNIVERSAL, Encoder.PC_CONSTRUCTED, Encoder.TAG_SEQUENCE);
    
    private PlayerName pName = null;
    private PeerGID pGID = null;
    
    //Getters
    public PlayerName getPlayerName(){return this.pName;}
    public String getPlayerNameAsString(){return this.pName.getPlayerName();}
    public PeerGID getPeerGID(){return this.pGID;}
    public String getPeerGIDAsString(){return this.pGID.getPeerGID();}
    
    //Constructors
    public PeerData(){}
    
    public PeerData(String playerName, String peerGID){
        this.pName = new PlayerName(playerName);
        this.pGID = new PeerGID(peerGID);
    }
    
    public PeerData(PlayerName playerName, PeerGID peerGID){
        this.pName = playerName;
        this.pGID = peerGID;
    }
    
    //Implement Cloneable Interface
    @Override
    public PeerData clone(){
        return new PeerData(this.pName, this.pGID);
    }
    
    //Get string representation of data for debugging
    public String getString(){
        return "PeerData: ("+this.pName.getString()+", "+this.pGID.getString()+")";
    }
    
    //Comparison operation
    public boolean equals(PeerData pd){
        if(!this.pName.equals(pd.getPlayerName())) return false;
        if(!this.pGID.equals(pd.pGID)) return false;
        return true;
    }
    
    @Override
    public Encoder getEncoder() {
        Encoder peerDataEncoder = new Encoder().initSequence();
        
        //Add PlayerName object to encoder
        peerDataEncoder.addToSequence(pName.getEncoder());
        
        //Add PeerGID object to encoder
        peerDataEncoder.addToSequence(pGID.getEncoder());
        
        return peerDataEncoder;
    }

    @Override
    public Object decode(Decoder dec) throws ASN1DecoderFail {
        Decoder peerDataDecoder = dec.getContent();
        
        this.pName = (PlayerName) new PlayerName().decode(peerDataDecoder.getFirstObject(true, PlayerName.asnType));
        
        this.pGID = (PeerGID) new PeerGID().decode(peerDataDecoder.getFirstObject(true, PeerGID.asnType));
        
        return this;
    }
}
