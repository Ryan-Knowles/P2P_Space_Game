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
    
    private static DDGameCommHandler handler;
    private static ConnectionManager manager;
    
    private static long lastUpdate = 0;
    
    
    //Run at start of game in loading loop
    public static void init() {
        handler = DDGameCommHandler.getInstance();
        manager = ConnectionManager.getInstance();
        
        //Clear data queue
        if(handler.getDataQueueSize()>0){
            handler.getMessages();
            handler.getData();
        }
       
    }
    

    
    //Run at game cleanup
    public static void cleanup(){
        manager.clearList();
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
            if(dec.getTypeByte()==JoinRequest.asnType){
                if(DEBUG)System.out.println("processMessage: JoinRequest(start)");
                //Do JoinRequest operations
                JoinRequest msg = (JoinRequest) new JoinRequest().decode(dec);
                if(DEBUG)System.out.println("processMessage: JoinRequest(got: "+msg.getString()+")");
                long msgDelay = Math.abs(System.currentTimeMillis()-msg.getTime());
                
                //Check for outdated message (older than 30 seconds)
                if(msgDelay>MSG_TIMEOUT){
                    //Do nothing
                    if(DEBUG)System.out.println("processMessage: JoinRequest(late message - msgDelay: "+(msgDelay/1000)+" seconds)");
                }
                //Determine state of game
                else if(state==GameState.PLAYING){
                    //Accepted RequestAck operations
                    OutputEvent ackEvent = new OutputEvent(senderGID, OutputEventType.REQUEST_ACK);
                    ackEvent.setRequestAckAnswer(true);
                    outputQueue.add(ackEvent);
                    manager.addPeer(msg.getPeerData());
                    PeerLink pl = new PeerLink(msg.getPeerData().getPeerGID(), manager.getLocalPeer().getPeerGID());
                    manager.addPeerLink(pl);
                } else {
                    //Declined RequestAck operations
                    OutputEvent ackEvent = new OutputEvent(senderGID, OutputEventType.REQUEST_ACK);
                    ackEvent.setRequestAckAnswer(false);
                    outputQueue.add(ackEvent);
                }
                if(DEBUG)System.out.println("processMessage: JoinRequest(end)");
            }
            else if(dec.getTypeByte()==RequestAck.asnType){
                if(DEBUG)System.out.println("processMessage: RequestAck(start)");
                //Do requestAck operations
                RequestAck msg = (RequestAck) new RequestAck().decode(dec);
                
                long msgDelay = Math.abs(System.currentTimeMillis()-msg.getTime());
                
                //Check for outdated message (older than 30 seconds)
                if(msgDelay>MSG_TIMEOUT){
                    //Do nothing
                    if(DEBUG)System.out.println("processMessage: RequestAck(late message - msgDelay: "+(msgDelay/1000)+" seconds)");
                }
                //Verify waiting for RequestAck from sender and have been waiting 
                //less than 30 seconds
                else if(sentRequests.containsKey(senderGID)){
                    Date time = sentRequests.remove(senderGID);
                    
                    //Determine how long system has been waiting for the
                    //acknowledgment
                    long ackDelay = Math.abs(System.currentTimeMillis()-time.getTime());
                    
                    //Check if greater than 30 seconds
                    if(ackDelay>MSG_TIMEOUT){
                        return; //Do nothing
                    }
                    
                    //Since RequestAck is expected from sender and received
                    //within the time limit, process the acknowledgment
                    PeerMap requestData = msg.getPeerMap();
                    //If peermap is not null, request was accepted, else it
                    //was rejected and should do nothing.
                    if(requestData!=null){
                        manager.mergePeerMap(requestData);
                        //Start game if state=standby
                        if(state==GameState.STANDBY){
                            DDGameMain.startGame();
                        }
                    }

                }
                if(DEBUG)System.out.println("processMessage: RequestAck(end)");
            }
            else if(dec.getTypeByte()==AddPeer.asnType && state==GameState.PLAYING){
                if(DEBUG)System.out.println("processMessage: AddPeer(start)");
                //Do addpeer operations
                AddPeer msg = (AddPeer) new AddPeer().decode(dec);
                
                long msgDelay = Math.abs(System.currentTimeMillis()-msg.getTime());
                
                //Check for outdated message (older than 30 seconds)
                if(msgDelay>MSG_TIMEOUT){
                    //Do nothing
                    if(DEBUG)System.out.println("processMessage: AddPeer(late message - msgDelay: "+(msgDelay/1000)+" seconds)");
                }
                //If destination peer isn't local peer, immediately relay message
                else if(!msg.getDestination().equals(manager.getLocalPeer().getPeerGID())){
                    manager.sendToPeer(msg.getDestination().getPeerGID(), data);
                }
                //Else process DeleteLink
                else {
                    PeerData pd = msg.getPeerData();
                    manager.addPeer(pd);
                }
                if(DEBUG)System.out.println("processMessage: AddPeer(end)");
            }
            else if(dec.getTypeByte()==AddLink.asnType && state==GameState.PLAYING){
                if(DEBUG)System.out.println("processMessage: AddLink(start)");
                //Do addlink operations
                AddLink msg = (AddLink) new AddLink().decode(dec);
                
                long msgDelay = Math.abs(System.currentTimeMillis()-msg.getTime());
                
                //Check for outdated message (older than 30 seconds)
                if(msgDelay>MSG_TIMEOUT){
                    //Do nothing
                    if(DEBUG)System.out.println("processMessage: AddLink(late message - msgDelay: "+(msgDelay/1000)+" seconds)");
                }
                //If destination peer isn't local peer, immediately relay message
                else if(!msg.getDestination().equals(manager.getLocalPeer().getPeerGID())){
                    manager.sendToPeer(msg.getDestination().getPeerGID(), data);
                }
                //Else process DeleteLink
                else {
                    PeerLink pl = msg.getPeerLink();
                    manager.addPeerLink(pl);
                }
                if(DEBUG)System.out.println("processMessage: AddLink(end)");
            }
            else if(dec.getTypeByte()==DeleteLink.asnType && state==GameState.PLAYING){
                if(DEBUG)System.out.println("processMessage: DeleteLink(start)");
                //Do deletelink operations
                DeleteLink msg = (DeleteLink) new DeleteLink().decode(dec);
                
                long msgDelay = Math.abs(System.currentTimeMillis()-msg.getTime());
                
                //Check for outdated message (older than 30 seconds)
                if(msgDelay>MSG_TIMEOUT){
                    //Do nothing
                    if(DEBUG)System.out.println("processMessage: DeleteLink(late message - msgDelay: "+(msgDelay/1000)+" seconds)");
                }
                //If destination peer isn't local peer, immediately relay message
                else if(!msg.getDestination().equals(manager.getLocalPeer().getPeerGID())){
                    manager.sendToPeer(msg.getDestination().getPeerGID(), data);
                }
                //Else process DeleteLink
                else {
                    PeerLink pl = msg.getPeerLink();
                    manager.removePeerLink(pl);
                }
                if(DEBUG)System.out.println("processMessage: DeleteLink(end)");
            }
            else if(dec.getTypeByte()==UpdateShip.asnType && state==GameState.PLAYING){
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
                else if(!msg.getDestination().equals(manager.getLocalPeer().getPeerGID())){
                    String dest = msg.getDestination().getPeerGID();
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
