/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.network.data;

import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author rknowles
 */
public class PlatformEvent {
    private HashMap<DataKey, String> data = new HashMap<>();
    private Date time_received;
    private PlatformEventType eventType;

    //Getters
    //public HashMap<DataKey, String> getData(){return this.data;}
    public String getTypeString(){return this.eventType.toString();}
    public Date getTimeReceived(){return this.time_received;}

    //Constructor
    public PlatformEvent(String type_string) {
            this.time_received = new Date(System.currentTimeMillis());
            this.eventType = PlatformEventType.getType(type_string);
    }

    /////////////////////////////
    //Data Mutators and Readers//
    /////////////////////////////
    
    //////////////////////////
    //EventType = PluginData//
    //////////////////////////
    //Returns true if addPluginData is successful, false if
    //the event type is not a PluginDataRdy type or if the data
    //has already been added.
    public boolean addPluginData(String localGID, String localName){
        //Confirm valid EventType
        if(this.eventType!=PlatformEventType.PLUGIN_DATA_RDY){
            return false;
        }
        
        //Confirm keys don't already exist
        if(this.data.containsKey(DataKey.LOCAL_PEER_GID)||
                this.data.containsKey(DataKey.LOCAL_PEER_NAME)){
            return false;
        }
        
        this.data.put(DataKey.LOCAL_PEER_GID, localGID);
        this.data.put(DataKey.LOCAL_PEER_NAME, localName);
        
        return true;
    }
    
    //Returns a String equal to localGID if EventType = PluginDataRdy
    //and the key DataKey.LOCAL_PEER_GID exists in the data HashMap,
    //else it returns null
    public String getPluginDataLocalGID(){
        //Check type
        if(this.eventType!=PlatformEventType.PLUGIN_DATA_RDY){
            return null;
        }
        //Check if key exists
        if(!this.data.containsKey(DataKey.LOCAL_PEER_GID)){
            return null;
        }
        
        return this.data.get(DataKey.LOCAL_PEER_GID);
    }
    
    //Returns a String equal to localName if EventType = PluginDataRdy
    //and the key DataKey.LOCAL_PEER_NAME exists in the data HashMap,
    //else it returns null
    public String getPluginDataLocalName(){
        //Check type
        if(this.eventType!=PlatformEventType.PLUGIN_DATA_RDY){
            return null;
        }
        //Check if key exists
        if(!this.data.containsKey(DataKey.LOCAL_PEER_NAME)){
            return null;
        }
        
        return this.data.get(DataKey.LOCAL_PEER_NAME);
    }
        
    ////////////////////////////
    //EventType = JOIN_REQUEST//
    ////////////////////////////
    
    //Returns true if addJoinRequest is successful, false if
    //the EventType is not a JOIN_REQUEST type or if the data
    //has already been added.
    public boolean addJoinRequest(String peerGID){
        //Confirm valid EventType
        if(this.eventType!=PlatformEventType.JOIN_REQUEST){
            return false;
        }
        
        //Confirm DEST_PEER_GID key doesn't already exist
        if(this.data.containsKey(DataKey.DEST_PEER_GID)){
            return false;
        }
        
        this.data.put(DataKey.DEST_PEER_GID, peerGID);
        
        return true;
    }
    
    //Returns a String equal to peerGID if EventType = JOIN_REQUEST
    //and the key DataKey.DEST_PEER_GID exists in the data HashMap,
    //else it returns null
    public String getJoinRequestPeerGID(){
        //Check type
        if(this.eventType!=PlatformEventType.JOIN_REQUEST){
            return null;
        }
        //Check if key exists
        if(!this.data.containsKey(DataKey.DEST_PEER_GID)){
            return null;
        }
        
        return this.data.get(DataKey.DEST_PEER_GID);
    }
}
