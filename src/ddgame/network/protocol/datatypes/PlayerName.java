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
public class PlayerName extends ASNObj{
   
//PlayerName :== UTF8String;
    
    public static final byte asnType = Encoder.buildASN1byteType(Encoder.CLASS_UNIVERSAL, Encoder.PC_PRIMITIVE, Encoder.TAG_UTF8String);
    
    private String playerName;
    
    //Getter
    public String getPlayerName(){return this.playerName;}
    
    //Constructors
    public PlayerName(){}
    
    public PlayerName(String playerName){
        this.playerName = playerName;
    }
    
    //Get string representation of data for debugging
    public String getString(){
        return "PlayerName: "+this.playerName;
    }
    
    //Comparison operation
    public boolean equals(PlayerName pn){
        return this.playerName.equals(pn.getPlayerName());
    }
    
    //Encoder/Decoder
    @Override
    public Encoder getEncoder() {
        Encoder stringEnc = new Encoder(playerName);
        return stringEnc;
    }

    @Override
    public Object decode(Decoder dec) throws ASN1DecoderFail {
        this.playerName = dec.getString(dec.getTypeByte());
        return this;
    }
     
}
