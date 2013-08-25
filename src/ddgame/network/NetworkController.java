/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.network;

import ASN1.ASN1DecoderFail;
import ASN1.Decoder;
import ddgame.core.DDGameLoop;
import ddgame.core.DDGameMain;
import ddgame.core.GameState;
import ddgame.network.data.Peer;
import ddgame.network.data.DataContainer;
import ddgame.network.data.OutputEvent;
import ddgame.network.data.OutputEventType;
import ddgame.network.data.PlatformEvent;
import ddgame.network.protocol.commands.AddLink;
import ddgame.network.protocol.commands.AddPeer;
import ddgame.network.protocol.commands.DeleteLink;
import ddgame.network.protocol.commands.JoinRequest;
import ddgame.network.protocol.commands.RequestAck;
import ddgame.network.protocol.commands.UpdateShip;
import ddgame.network.protocol.datatypes.PeerData;
import ddgame.network.protocol.datatypes.PeerLink;
import ddgame.network.protocol.datatypes.PeerMap;
import ddgame.network.protocol.datatypes.ShipData;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rknowles
 */


public class NetworkController {
    private static final boolean DEBUG = false;
    private static final int MSG_TIMEOUT = 30000;   //throw away messages older than 30 seconds
    
    private static long lastMessageTime;
    
    private static HashMap<String, Date> sentRequests = new HashMap<>();
    private static ArrayDeque<OutputEvent> outputQueue = new ArrayDeque<>(20);
    private static ArrayList<Peer> peers = new ArrayList<>();
    
    private static DDGameCommHandler handler;
    
    private static long lastUpdate = 0;
    
    
    //Run at start of game in loading loop
    public static void init() {
        handler = DDGameCommHandler.getInstance();
        
        //Clear data queue
        if(handler.getDataQueueSize()>0){
            handler.getMessages();
            handler.getData();
        }
       
    }
    

    
    //Run at game cleanup
    public static void cleanup(){
        peers.clear();
    }
    
    public static void updatePreMainUpdate(){
        GameState state = DDGameLoop.getInstance().getGameState();
        ArrayList<DataContainer> dataCon;
        ArrayList<PlatformEvent> eventQueue;
        
        //Process PlatformEvents
        if(handler.getEventQueueSize()>0){
            eventQueue = handler.getPlatformEvents();
            
            for(PlatformEvent pe: eventQueue){
                processEvent(pe);
            }
            
            eventQueue = null;
        }
        
        //Process messages
        if(handler.getMsgCount()>0){
        	long now = System.currentTimeMillis();
        	if(true){
        		double diff = (now-lastMessageTime)/1000;
        		System.out.println("seconds since last processing: "+diff);
        		System.out.println("processing "+handler.getMsgCount()+" messages");
    		}
        	
        	lastMessageTime = now;
            dataCon = handler.getMessages();
            
            for(DataContainer dc: dataCon){
                processMessage(state, dc.getData(), dc.getPeerGID());
            }
            
            dataCon = null;
        }
        
        //Push update events every 250ms
        long updateDelta = System.currentTimeMillis()-lastUpdate;
        if(state==GameState.PLAYING&&updateDelta>250){
            OutputEvent shipUpdateEvent = new OutputEvent(null, OutputEventType.SHIP_UPDATE);
            outputQueue.add(shipUpdateEvent);
            lastUpdate = System.currentTimeMillis();
        }
    }
    
    public static void processEvent(PlatformEvent pe){
        //Perform PLUGIN_DATA_RDY operations
        if(pe.getTypeString().equalsIgnoreCase("PLUGIN_DATA_RDY")){
            if(DEBUG)System.out.println("processEvent: PLUGIN_DATA_RDY(start)");
            String localGID = pe.getPluginDataLocalGID();
            String localName = pe.getPluginDataLocalName();
            
            if(localGID!=null&&localName!=null){
                Peer p = new Peer(localName, localGID, true);
                manager.setLocalPeer(p);
            }
            if(DEBUG)System.out.println("processEvent: PLUGIN_DATA_RDY(localpeer: "+manager.getLocalPeer().getPeerData().getString()+")");
            if(DEBUG)System.out.println("processEvent: PLUGIN_DATA_RDY(end)");
        } 
        
        //Perform START_GAME operations
        else if(pe.getTypeString().equalsIgnoreCase("START_GAME")){
            if(DEBUG)System.out.println("processEvent: START_GAME(start)");
            DDGameMain.startGame();
            if(DEBUG)System.out.println("processEvent: START_GAME(end)");
        }
        
        //Perform JOIN_REQUEST operations
        else if(pe.getTypeString().equalsIgnoreCase("JOIN_REQUEST")){
            if(DEBUG)System.out.println("processEvent: JOIN_REQUEST(start)");
            String receiver = pe.getJoinRequestPeerGID();
            
            JoinRequest request = new JoinRequest(manager.getLocalPeer().getPeerData());
            if(DEBUG)System.out.println("processEvent: JOIN_REQUEST("+request.getString()+")");
            byte[] encodedRequest = request.getEncoder().getBytes();
            
            handler.sendMessage(receiver, encodedRequest);
            sentRequests.put(receiver, new Date());
            if(DEBUG)System.out.println("processEvent: JOIN_REQUEST(end)");
        }
    }
 
       
    //Process received message
    private static void processMessage(GameState state, byte[] data, String senderGID){

        Decoder dec = new Decoder(data);
       
        try {
            if(dec.getTypeByte()==UpdateShip.asnType && state==GameState.PLAYING){
                if(DEBUG)System.out.println("processMessage: UpdateShip(start)");
                //Do update ship operations
                UpdateShip msg = (UpdateShip) new UpdateShip().decode(dec);
                
                long msgDelay = Math.abs(System.currentTimeMillis()-msg.getTime());
                
                //Check for outdated message (older than 30 seconds)
                if(msgDelay>MSG_TIMEOUT){
                    //Do nothing
                    if(DEBUG)System.out.println("processMessage: UpdateShip(late message - msgDelay: "+(msgDelay/1000)+" seconds)");
                }
                //If destination peer isn't local peer, immediately relay message
                else if(!msg.getSource().equals(manager.getLocalPeer().getPeerGID())){
                    String dest = msg.getSource().getPeerGID();
                    manager.sendToPeer(dest, data);
                    if(DEBUG)System.out.println("processMessage: UpdateShip(relaying msg from: "+senderGID.substring(0, 20)+" to:"+dest.substring(0, 20)+")");
                }
                //Else process update
                else {
                    ShipData sd = msg.getShipData();
                    String ownerGID = sd.getOwnerGIDAsString();
                    manager.getPeerByGID(ownerGID).pushShipDataUpdate(sd);
                }
                if(DEBUG)System.out.println("processMessage: UpdateShip(end)");
            } else {
                if(DEBUG)System.out.println("processMessage: Received Unknown Message Type");
            }
        }
        catch (ASN1DecoderFail ex) {
                Logger.getLogger(NetworkController.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    //Process outputQueue
    public static void updatePostMainUpdate(){
        //Copy outputQueue and clear Queue
        ArrayDeque<OutputEvent> outQ = outputQueue;
        outputQueue = new ArrayDeque<>();
        
        for(OutputEvent outEvent: outQ){
            processOutputEvent(outEvent);
        }
        
        outQ.clear();
        outQ = null;
    }
    
    public static void processOutputEvent(OutputEvent outEvent){
        String destGID = outEvent.getDestGID();
        if(DEBUG&&destGID!=null&&outEvent.getEventType()!=OutputEventType.SHIP_UPDATE){
            String dest = destGID.substring(0, 20);
            String local = manager.getLocalPeer().getPeerGIDAsString().substring(0, 20);
            System.out.println("processOutputEvent: destGID: "+dest);
            System.out.println("processOutputEvent: localGID: "+local);
        }
        switch(outEvent.getEventType()){
            case JOIN_REQUEST:
                //Do nothing
                break;
            case ADD_LINK:
                //Do nothing
                break;
            case ADD_PEER:
                //Do nothing
                break;
            case DELETE_LINK:
                //Do nothing
                break;
            case REQUEST_ACK:
                if(DEBUG)System.out.println("processOutputEvent: REQUEST_ACK(start)");
                //Perform REQUEST_ACK operation
                boolean answer = (boolean) outEvent.getRequestAckAnswer();
                RequestAck ack;
                
                //Generate RequestAck based on answer
                if(answer){
                    PeerMap map = manager.generatePeerMap();
                    ack = new RequestAck(map);
                } else{
                    ack = new RequestAck(null);
                }
                byte[] encodedRequestAck = ack.getEncoder().getBytes();
                
                if(destGID!=null){
                    handler.sendMessage(destGID, encodedRequestAck);
                } else{
                    if(DEBUG)System.out.println("processOutputEvent: REQUEST_ACK(Error: no destGID specified)");
                }
                if(DEBUG)System.out.println("processOutputEvent: REQUEST_ACK(end)");
                break;
            case SHIP_UPDATE:
                //if(DEBUG)System.out.println("processOutputEvent: SHIP_UPDATE(start)");
                //Perform SHIP_UPDATE operation
                ShipData localSD = manager.getLocalPeer().getShipData();
                //if(DEBUG)System.out.println("processOutputEvent: SHIP_UPDATE(sending:"+localSD.getString()+"\n)");
                byte[] encodedUpdateShip;
                //Send to all peers if destGID==null
                if(destGID==null){
                    Collection<Peer> peerList = manager.getPeerList();
                    
                    for(Peer p: peerList){
                        //Verify Peer p != local peer
                        if(!p.getPeerGIDAsString().equals(manager.getLocalPeer().getPeerGIDAsString())){
                            UpdateShip tempUS = new UpdateShip(p.getPeerGID(), localSD);
                            encodedUpdateShip = tempUS.getEncoder().getBytes();
                            manager.sendToPeer(p.getPeerGIDAsString(), encodedUpdateShip);
                        }
                        
                    }
                } else {
                    UpdateShip singleUS = new UpdateShip(destGID, localSD);
                    encodedUpdateShip = singleUS.getEncoder().getBytes();
                    manager.sendToPeer(destGID, encodedUpdateShip);
                }
                //if(DEBUG)System.out.println("processOutputEvent: SHIP_UPDATE(end)");
                break;
            default:
                if(DEBUG)System.out.println("Error: Can't process OutputEvent::Unknown OutputEventType");
                break;
        }
    }
    
}
