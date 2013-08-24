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
import ddgame.network.protocol.datatypes.PeerMap;
import util.Util;

/**
 *
 * @author rknowles
 */
public class RequestAck extends ASNObj{
    
//--Returns null if request is rejected,
//--returns updated PeerMap (including requester node and link)
//--if accepted
//RequestAck :== [APPLICATION 1] SEQUENCE{
//    timeSent INTEGER,
//    CHOICE {
//        reject NULL,
//        accept PeerMap;
//    };
//}

    public static final byte asnType = Encoder.buildASN1byteType(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte) 1);
    private static final byte nullType = Encoder.buildASN1byteType(Encoder.CLASS_UNIVERSAL, Encoder.PC_PRIMITIVE, Encoder.TAG_NULL);
    private static final byte pmType = PeerMap.asnType;
    
    private static final boolean DEBUG = true;
    
    private long timeSent;
    private PeerMap peerMap = null;
    
    //Getters
    public long getTime(){return this.timeSent;}
    public PeerMap getPeerMap(){return this.peerMap;}
    
    //Constructors
    public RequestAck(){}
    
    public RequestAck(PeerMap pMap){
        this.timeSent = System.currentTimeMillis();
        this.peerMap = pMap;
    }
    
    //Encoding/Decoding
    @Override
    public Encoder getEncoder() {
        Encoder pmEncoder = new Encoder().initSequence();
        pmEncoder.setASN1Type(asnType);
        
        //Add timeSent INTEGER value to encoder
        pmEncoder.addToSequence(new Encoder(timeSent));
        
        //Add choice selection to encoder
        if(peerMap==null) {
            pmEncoder.addToSequence(Encoder.getNullEncoder());
        } else {
            pmEncoder.addToSequence(peerMap.getEncoder());
        }

        return pmEncoder;
    }

    @Override
    public Object decode(Decoder dec) throws ASN1DecoderFail {
        
        Decoder pmDecoder = dec.getContent();
        
        this.timeSent = (pmDecoder.getFirstObject(true, Encoder.TAG_INTEGER).getInteger()).longValue();
        if(DEBUG)System.out.println("got time: "+this.timeSent);
        
        //Decoder choice selection
        if(pmDecoder.getTypeByte()==nullType){
            this.peerMap = null;
        } else if (pmDecoder.getTypeByte()==pmType){
            this.peerMap = (PeerMap) new PeerMap().decode(pmDecoder.getFirstObject(true, pmType));
        } else {
            byte[] nullArray = new byte[1];
            nullArray[0] = nullType;
            byte[] pmArray = new byte[1];
            pmArray[0] = pmType;
            byte[] gotArray = new byte[1];
            gotArray[0] = pmDecoder.getTypeByte();
            if(DEBUG)System.out.println("nullType: "+Util.byteToHex(nullArray)+" pmType: "+Util.byteToHex(pmArray)+" gotType: "+Util.byteToHex(gotArray));
            if(DEBUG)System.out.println(pmDecoder.dumpHex());
            throw new ASN1DecoderFail("Decoder Failure: Invalid RequestAck data");
            
        }
        
        return this;
    }
}
