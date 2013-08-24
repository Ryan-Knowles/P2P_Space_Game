/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.network.data;

import java.util.HashMap;

/**
 *
 * @author rknowles
 */

public class OutputEvent {
    
    private HashMap<DataKey, Object> data = new HashMap<>();
    private String destPeer = null; //Null destination means send to all peers
    private OutputEventType eventType;
    
    //Getters
    public String getDestGID(){return this.destPeer;}
    public OutputEventType getEventType(){return this.eventType;}
    
    //Constructor
    public OutputEvent(String destination, OutputEventType eventType){
        this.destPeer = destination;
        this.eventType = eventType;
    }
    
    //Return true if REQUEST_ACK_ANSWER successfully added, false otherwise
    public boolean setRequestAckAnswer(boolean answer){
        //Check type for REQUEST_ACK
        if(this.eventType!=OutputEventType.REQUEST_ACK){
            return false;
        }
        
        //Overwrite key with new answer if it exists
        if(this.data.containsKey(DataKey.REQUEST_ACK_ANSWER)){
            this.data.remove(DataKey.REQUEST_ACK_ANSWER);
        }
        this.data.put(DataKey.REQUEST_ACK_ANSWER, answer);
        
        return true;
    }
    
    //Returns value stored in REQUEST_ACK_ANSWER if available, null otherwise
    public Object getRequestAckAnswer(){
        //Check type is REQUEST_ACK
        if(this.eventType!=OutputEventType.REQUEST_ACK){
            return null;
        }
        //Check key exists
        if(!this.data.containsKey(DataKey.REQUEST_ACK_ANSWER)){
            return null;
        }
        
        return this.data.get(DataKey.REQUEST_ACK_ANSWER);
    }
   
}
