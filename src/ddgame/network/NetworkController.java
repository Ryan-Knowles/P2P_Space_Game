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
import ddgame.data.DataController;
import ddgame.network.data.DataContainer;
import ddgame.network.data.OutputEvent;
import ddgame.network.data.OutputEventType;
import ddgame.network.data.PlatformEvent;
import ddgame.network.protocol.commands.UpdateShip;
import ddgame.network.protocol.datatypes.PeerData;
import ddgame.network.protocol.datatypes.ShipData;
import java.util.ArrayDeque;
import java.util.ArrayList;
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
    
    private static long lastMessageTime;
    
    private static HashMap<String, Date> sentRequests = new HashMap<>();
    private static ArrayDeque<OutputEvent> outputQueue = new ArrayDeque<>(20);
    
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
        Connection.clear();
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
        	Connection.validate();
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
            	PeerData myData = new PeerData(localName, localGID);
                DataController.getInstance().makePlayerShip(myData);
            }
            if(DEBUG)System.out.println("processEvent: PLUGIN_DATA_RDY(localpeer: "+DataController.getInstance().getPlayerShip().getPeer().getString()+")");
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
            Connection.addConnection(receiver);
            DDGameMain.startGame();
            if(DEBUG)System.out.println("processEvent: JOIN_REQUEST(end)");
        }
    }
 
       
    //Process received message
    private static void processMessage(GameState state, byte[] data, String senderGID){

        Decoder dec = new Decoder(data);
       
        try {
            if(dec.getTypeByte()==UpdateShip.asnType && state==GameState.PLAYING) {
                if(DEBUG)System.out.println("processMessage: UpdateShip(start)");
                //Do update ship operations
                UpdateShip msg = (UpdateShip) new UpdateShip().decode(dec);
                
                Connection sender = Connection.getConnection(senderGID);
                if(sender == null) {
                	Connection.addConnection(senderGID);
                	sender = Connection.getConnection(senderGID);
                }
                
                if(sender.isValid(msg.getSequenceNumber())) {
                	sender.update(msg.getSequenceNumber());
                	ArrayList<ShipData> sdlist = msg.getShipData();
                	for(ShipData sd: sdlist) {
                		DataController.getInstance().updateShipFromPeer(sd);
                	}
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
                //Do nothing
                break;
            case SHIP_UPDATE:
            	UpdateShip us = UpdateShip.buildLocal();
            	byte[] data = us.encode();
            	
                Connection.validate();
                ArrayList<String> gids = Connection.getGIDs();
                for(String str: gids){
                	handler.sendMessage(str, data);
                }
                break;
            default:
                if(DEBUG)System.out.println("Error: Can't process OutputEvent::Unknown OutputEventType");
                break;
        }
    }
    
}
