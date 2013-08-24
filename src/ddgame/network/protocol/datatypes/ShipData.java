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
public class ShipData extends ASNObj{
    
//ShipData :== SEQUENCE {
//    player      PeerData,
//    shipFacing  REAL,
//    xCenter     INTEGER,
//    yCenter     INTEGER,
//    moveFacing  REAL,
//    xVelocity   REAL,
//    yVelocity   REAL;
//}
    
    public static final byte asnType = Encoder.buildASN1byteType(Encoder.CLASS_UNIVERSAL, Encoder.PC_CONSTRUCTED, Encoder.TAG_SEQUENCE);
    
    private PeerData player;
    private double shipFacing;
    private int xCenter;
    private int yCenter;
    private double moveFacing;
    private double xVelocity;
    private double yVelocity;
    
    //Getters
    public PeerData getPeerData(){return this.player;}
    public PeerGID getOwnerGID(){return this.player.getPeerGID();}
    public String getOwnerGIDAsString(){return this.player.getPeerGIDAsString();}
    public PlayerName getPlayerName(){return this.player.getPlayerName();}
    public String getPlayerNameAsString(){return this.player.getPlayerNameAsString();}
    public double getShipFacing(){return this.shipFacing;}
    public int getXCenter(){return this.xCenter;}
    public int getYCenter(){return this.yCenter;}
    public double getMoveFacing(){return this.moveFacing;}
    public double getXVelocity(){return this.xVelocity;}
    public double getYVelocity(){return this.yVelocity;}
    
    //Constructors
    public ShipData(){}
    
    public ShipData(PeerData player, double shipFacing, int xCenter, int yCenter, double moveFacing, 
                    double xVelocity, double yVelocity){
        this.player = player;
        this.shipFacing = shipFacing;
        this.xCenter = xCenter;
        this.yCenter = yCenter;
        this.moveFacing = moveFacing;
        this.xVelocity = xVelocity;
        this.yVelocity = yVelocity;
    }
    
    //Comparison operation
    //--Unimplemented--
    public boolean equals(ShipData sd){
        return false;
    }
    
    //Get string representation of data for debugging
    public String getString(){
        String header = "ShipData: {";
        String owner =  "\n\towner: "+this.player.getString();
        String facing = "\n\tfacing: "+this.shipFacing;
        String center = "\n\tcenter: ("+this.xCenter+","+this.yCenter+")";
        String moving = "\n\tmoving: "+this.moveFacing;
        String velocity = "\n\tvelocity: ("+this.xVelocity+","+this.yVelocity+")";
        String footer = "}\n";
        return header+owner+facing+center+moving+velocity+footer;
    }
    
    @Override
    public Encoder getEncoder() {
        Encoder shipDataEncoder = new Encoder().initSequence();
        
        //Add player PeerData object to encoder
        shipDataEncoder.addToSequence(player.getEncoder());
        
        //Add shipFacing double value to encoder
        shipDataEncoder.addToSequence(new Encoder(shipFacing));
        
        //Add xCenter integer value to encoder
        shipDataEncoder.addToSequence(new Encoder(xCenter));
        
        //Add yCenter integer value to encoder
        shipDataEncoder.addToSequence(new Encoder(yCenter));
        
        //Add moveFacing double value to encoder
        shipDataEncoder.addToSequence(new Encoder(moveFacing));
        
        //Add xVelocity double value to encoder
        shipDataEncoder.addToSequence(new Encoder(xVelocity));
        
        //Add yVelocity double value to encoder
        shipDataEncoder.addToSequence(new Encoder(yVelocity));
        
        return shipDataEncoder;
    }

    @Override
    public Object decode(Decoder dec) throws ASN1DecoderFail {
        Decoder sdDecoder = dec.getContent();
        
        this.player = (PeerData) new PeerData().decode(sdDecoder.getFirstObject(true, PeerData.asnType));
        
        this.shipFacing = sdDecoder.getFirstObject(true, Encoder.TAG_REAL).getReal();
        this.xCenter = (sdDecoder.getFirstObject(true, Encoder.TAG_INTEGER).getInteger()).intValue();
        this.yCenter = (sdDecoder.getFirstObject(true, Encoder.TAG_INTEGER).getInteger()).intValue();
        this.moveFacing = sdDecoder.getFirstObject(true, Encoder.TAG_REAL).getReal();
        this.xVelocity = sdDecoder.getFirstObject(true, Encoder.TAG_REAL).getReal();
        this.yVelocity = sdDecoder.getFirstObject(true, Encoder.TAG_REAL).getReal();
        return this;
    }
}
