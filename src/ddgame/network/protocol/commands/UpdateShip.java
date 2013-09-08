/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.network.protocol.commands;

import java.util.ArrayList;

import ASN1.ASN1DecoderFail;
import ASN1.ASNObj;
import ASN1.Decoder;
import ASN1.Encoder;
import ddgame.data.DataController;
import ddgame.network.protocol.datatypes.PeerData;
import ddgame.network.protocol.datatypes.PeerGID;
import ddgame.network.protocol.datatypes.ShipData;

/**
 *
 * @author rknowles
 */
public class UpdateShip extends ASNObj{
    
//--Update physics of ship state
//	Update :== [APPLICATION 5] SEQUENCE {
//  seqNum	    INTEGER,
//  source      PeerGID,
//	applyTime	INTEGER,
//	sendTime	INTEGER,
//  shipData    ShipData;
//}

    public static final byte asnType = Encoder.buildASN1byteType(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte) 5);
    private static long syncNum = 0;
    
    public static UpdateShip buildLocal(){
    	PeerGID src = DataController.getInstance().getPlayerShip().getPeer().getPeerGID(); 
    	ArrayList<ShipData> data = DataController.getInstance().getGetShipData();
    	
    	return new UpdateShip(++syncNum, src, data);
    }
    
    private long seqNum;
    private PeerGID source;
    private ArrayList<ShipData> shipData;
    
    //Getters
    public long getSequenceNumber(){return this.seqNum;}
    public PeerGID getSource(){return this.source;}
    public ArrayList<ShipData> getShipData(){return this.shipData;}
    
    //Constructors
    public UpdateShip(){}
    
    public UpdateShip(long seqNum, PeerGID source, ArrayList<ShipData> shipData){
        this.seqNum = seqNum;
        this.source = source;
        this.shipData = shipData;
    }
    
    public UpdateShip(long seqNum, String source,  ArrayList<ShipData> shipData){
        this.seqNum = seqNum;
        this.source = new PeerGID(source);
        this.shipData = shipData;
    }
    
    //Get string representation of data for debugging
    public String getString(){
        String header = "UpdateShip: (";
        String seq = "\n\tseq: "+this.seqNum;
        String src = "\n\tsource: "+this.source.getString();
        String shipinfo = "";
        for(ShipData sd : this.shipData){
        	shipinfo += "\n\t"+sd.getString();
        }
        String footer = "\n\t)\n";
        return header+seq+src+shipinfo+footer;
    }
    
    
    //Encoding/Decoding
    @Override
    public Encoder getEncoder() {
        Encoder usEncoder = new Encoder().initSequence();
        usEncoder.setASN1Type(asnType);
        
        //Add sequenceNumber INTEGER value to encoder
        usEncoder.addToSequence(new Encoder(seqNum));
        
        //Add source PeerGID object to encoder
        usEncoder.addToSequence(source.getEncoder());
                
        //Add shipData ShipData object to encoder
        Encoder sdEncoder = new Encoder().initSequence();
        for(ShipData sd: this.shipData)
        {
        	sdEncoder.addToSequence(sd.getEncoder());
        }
        
        usEncoder.addToSequence(sdEncoder);
        
        return usEncoder;
    }

    @Override
    public Object decode(Decoder dec) throws ASN1DecoderFail {
        
        Decoder usDecoder = dec.getContent();
        
        this.seqNum = (usDecoder.getFirstObject(true, Encoder.TAG_INTEGER).getInteger()).longValue();
        
        this.source = (PeerGID) new PeerGID().decode(usDecoder.getFirstObject(true, PeerGID.asnType));
        
        this.shipData = usDecoder.<ShipData>getSequenceOfAL(ShipData.asnType, new ShipData());
        //v (ShipData) new ShipData().decode(usDecoder.getFirstObject(true, ShipData.asnType));
        
        return this;
    }

}
