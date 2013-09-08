/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.network.protocol.datatypes;

import ddgame.data.Ship;
import ASN1.ASN1DecoderFail;
import ASN1.ASNObj;
import ASN1.Decoder;
import ASN1.Encoder;

/**
 *
 * @author rknowles
 */
public class ShipData extends ASNObj implements Cloneable{
    
//ShipData :== SEQUENCE {
//	  lastUpdate	INTEGER,	
//    player	  	PeerData,
//    shipFacing  	REAL,
//    xCenter     	INTEGER,
//    yCenter     	INTEGER,
//    xDestination  INTEGER,
//    yDestination  INTEGER;
//}
    
    public static final byte asnType = Encoder.buildASN1byteType(Encoder.CLASS_UNIVERSAL, Encoder.PC_CONSTRUCTED, Encoder.TAG_SEQUENCE);
    
    private long lastUpdate;
    private PeerData player;
    private int xDestination;
    private int yDestination;
    
    //Getters
    public long getLastUpdate(){return this.lastUpdate;}
    public PeerData getPeerData(){return this.player;}
    public PeerGID getOwnerGID(){return this.player.getPeerGID();}
    public String getOwnerGIDAsString(){return this.player.getPeerGIDAsString();}
    public PlayerName getPlayerName(){return this.player.getPlayerName();}
    public String getPlayerNameAsString(){return this.player.getPlayerNameAsString();}
    public int getXDestination(){return this.xDestination;}
    public int getYDestination(){return this.yDestination;}
    
    //Constructors
    public ShipData(){}
    
    public ShipData(Ship s){
    	this.lastUpdate = s.getLastUpdate();
    	this.player = s.getPeer();
    	if(s.getDest()==null){
    		this.xDestination = (int) (s.getCenter().getX());
    		this.yDestination = (int) (s.getCenter().getY());
    	}
    	else {
	    	this.xDestination = (int) (s.getCenter().getX()+s.getDest().getX());
	    	this.yDestination = (int) (s.getCenter().getY()+s.getDest().getY());
    	}
    }
    
    public ShipData(long lastUpdate, PeerData player, int xDestination, int yDestination){
    	this.lastUpdate = lastUpdate;
        this.player = player;
        this.xDestination = xDestination;
        this.yDestination = yDestination;
    }
    
    //Comparison operation
    //--Unimplemented--
    public boolean equals(ShipData sd){
        return false;
    }
    
    //Get string representation of data for debugging
    public String getString(){
        String header = "ShipData: {";
        String updated = "\n\tupdated: "+this.lastUpdate;
        String owner =  "\n\towner: "+this.player.getString();
        String destination = "\n\tdestination: ("+this.xDestination+","+this.yDestination+")";
        String footer = "}\n";
        return header+owner+destination+footer;
    }
    
    @Override
    public Encoder getEncoder() {
        Encoder shipDataEncoder = new Encoder().initSequence();
        
        //Add last update time
        shipDataEncoder.addToSequence(new Encoder(lastUpdate));
        
        //Add player PeerData object to encoder
        shipDataEncoder.addToSequence(player.getEncoder());
                       
        //Add xVelocity double value to encoder
        shipDataEncoder.addToSequence(new Encoder(xDestination));
        
        //Add yVelocity double value to encoder
        shipDataEncoder.addToSequence(new Encoder(yDestination));
        
        return shipDataEncoder;
    }

    @Override
    public Object decode(Decoder dec) throws ASN1DecoderFail {
        Decoder sdDecoder = dec.getContent();
        
        this.lastUpdate = (sdDecoder.getFirstObject(true, Encoder.TAG_INTEGER).getInteger()).longValue();
        
        this.player = (PeerData) new PeerData().decode(sdDecoder.getFirstObject(true, PeerData.asnType));

        this.xDestination = (sdDecoder.getFirstObject(true, Encoder.TAG_INTEGER).getInteger()).intValue();
        this.yDestination = (sdDecoder.getFirstObject(true, Encoder.TAG_INTEGER).getInteger()).intValue();
        return this;
    }
}
