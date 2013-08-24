/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.network.data;

import ddgame.data.DataController;
import ddgame.data.Ship;
import ddgame.network.protocol.datatypes.PeerData;
import ddgame.network.protocol.datatypes.PeerGID;
import ddgame.network.protocol.datatypes.PeerLink;
import ddgame.network.protocol.datatypes.PlayerName;
import ddgame.network.protocol.datatypes.ShipData;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author rknowles
 */
public class Peer {
    private static final boolean DEBUG = false;
    
    private Calendar joinDate = Calendar.getInstance();
    private long lastMessageTime = System.currentTimeMillis();
    private PeerData peerData;
    private HashMap<String, PeerLink> adjPeers = new HashMap<>();
    private Ship myShip;
    private boolean isLocal = false;    //Tags peer as the local peer
    
    //Getters
    public PeerGID getPeerGID(){return this.peerData.getPeerGID();}
    public String getPeerGIDAsString(){return this.peerData.getPeerGIDAsString();}
    public PlayerName getPlayerName(){return this.peerData.getPlayerName();}
    public String getPlayerNameAsString(){return this.peerData.getPlayerNameAsString();}
    public PeerData getPeerData(){return this.peerData;}
    public Calendar getJoinDate(){return this.joinDate;}
    public long getLastMessageTime(){return this.lastMessageTime;}
    public boolean isLocalPeer(){return this.isLocal;}
    
    
    //adjPeers HashMap Getters
    public boolean hasLinkToPeer(String linkedGID){return this.adjPeers.containsKey(linkedGID);}
    public Collection<String> getLinkedGIDs(){return this.adjPeers.keySet();}
    public Collection<PeerLink> getPeerLinks(){return this.adjPeers.values();}
    
    //Setters
    public void setNewPeerData(PeerData pd){this.peerData = pd;}
    
    /////////////////////////////
    //adjPeers HashMap Mutators//
    /////////////////////////////
    
    //Returns true if successful, false if peerlink already exists
    //or if peerlink does not contain a node equal to this peer's
    //peerGID.
    public boolean addPeerLink(PeerLink pl){
        String otherGID = pl.getOppositePeer(this.getPeerGIDAsString());
        
        //If PeerLink doesnt contain a node with this peerGID,
        //return false
        if(otherGID==null){
            return false;
        }
        //Check if there already exists a link
        if(this.adjPeers.containsKey(otherGID)){
            return false;
        }
        
        this.adjPeers.put(otherGID, pl);
        
        return true;
    }
    //Returns true if successful, false if peerlink doesn't exist
    //or if peerlink does not contain a node equal to this peer's
    //peerGID.
    public boolean removePeerLink(PeerLink pl){
        String otherGID = pl.getOppositePeer(this.getPeerGIDAsString());
        
        //If PeerLink doesnt contain a node with this peerGID,
        //return false
        if(otherGID==null){
            return false;
        }
        
        //Check if link exists
        if(!this.adjPeers.containsKey(otherGID)){
            return false;
        }
        
        this.adjPeers.remove(otherGID);
        
        return true;
    }
    
    //Constructor
    public Peer(PeerData pd, boolean isLocal){
        this.peerData = pd;
        this.isLocal = isLocal;
        setShip();
    }
    
    public Peer(String peerName, String peerGID, boolean isLocal){
        this.peerData = new PeerData(peerName, peerGID);
        this.isLocal = isLocal;
        setShip();
    }
    
    public void setShip(){
        if(isLocal){
            DataController.getInstance().makePlayerShip(this.getPlayerNameAsString());
            this.myShip = DataController.getInstance().getPlayerShip();
        } else{
            DataController.getInstance().addShip(this.getPeerGIDAsString(), this.getPlayerNameAsString());
            this.myShip = DataController.getInstance().getShip(this.getPeerGIDAsString());
        }
    }
    
    //Update clock
    public void tick(){
        this.lastMessageTime = System.currentTimeMillis();
    }
    
    public void pushShipDataUpdate(ShipData sd){
        if(DEBUG)System.out.println("Peer: "+this.getPlayerNameAsString()+" pushShipDataUpdate: \n"+sd.getString());
        //Make sure this is Peer's ship data
        if(!sd.getOwnerGIDAsString().equals(this.getPeerGIDAsString())){
            return;
        }
        //Update playerName if sd has different
        if(!sd.getPlayerNameAsString().equals(this.getPlayerNameAsString())){
            this.setNewPeerData(sd.getPeerData());
        }
        DataController.getInstance().updateShipFromPeer(this.getPeerGIDAsString(), sd);
    }
    
    //Only called on local peer
    //Return null if not associated with a ship
    public ShipData getShipData(){
        //Check for valid ship reference
        if(this.myShip==null){
            return null;
        }
        
        //Fill out necessary data
        PeerData owner = this.getPeerData();
        double facing = this.myShip.getFacing();
        int xCen = this.myShip.getCenter().x;
        int yCen = this.myShip.getCenter().y;
        double moving = this.myShip.getMoving();
        double xVel = this.myShip.getXVelocity();
        double yVel = this.myShip.getYVelocity();
        
        return new ShipData(owner, facing, xCen, yCen, moving, xVel, yVel);
        
    }
    
    //Clean up peer data
    public void cleanup(){
        DataController.getInstance().removeShip(this.getPeerGIDAsString());
        this.myShip = null;
        this.adjPeers = null;
    }
}
