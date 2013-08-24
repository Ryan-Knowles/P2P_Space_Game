/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.network;

import ddgame.network.data.Peer;
import ddgame.network.netgraph.RoutingTable;
import ddgame.network.protocol.datatypes.PeerData;
import ddgame.network.protocol.datatypes.PeerLink;
import ddgame.network.protocol.datatypes.PeerMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author rknowles
 */
public class ConnectionManager {
    private static final boolean DEBUG = true;
    
    //Singleton Implementation
    private static ConnectionManager instance = null;
    public static ConnectionManager getInstance(){
        if(instance == null) instance = new ConnectionManager();
        return instance;
    }
        
    //Variables
    private HashMap<String, Peer> allPeers = new HashMap<>();
    private Peer local = null;
    private RoutingTable rt = null;
    private boolean initialized = false;
    
    //Getters
    public Collection<Peer> getPeerList(){return this.allPeers.values();}
    public RoutingTable getRoutingTable(){return this.rt;}
    public Peer getPeerByGID(String peerGID){return this.allPeers.get(peerGID);}
    public boolean hasPeer(String peerGID){return this.allPeers.containsKey(peerGID);}
    public boolean hasPeer(PeerData peerData){return this.allPeers.containsKey(peerData.getPeerGIDAsString());}
    public Peer getLocalPeer(){return this.local;}
    public boolean isInitialized(){return this.initialized;}
    public boolean hasLink(PeerLink pl){
        //Check if peers exist
        if(!this.hasPeer(pl.getFirstNodeAsString())||
                !this.hasPeer(pl.getSecondNodeAsString())){
            return false;
        }
        //Check if peer1 is linked to peer2
        if(!this.getPeerByGID(pl.getFirstNodeAsString()).hasLinkToPeer(pl.getSecondNodeAsString())){
            return false;
        }
        //Check if peer2 is linked to peer1
        if(!this.getPeerByGID(pl.getSecondNodeAsString()).hasLinkToPeer(pl.getFirstNodeAsString())){
            return false;
        }
        
        return true;
    }
    
    //Setters
    public void setLocalPeer(Peer p){
        p.setShip();
        this.local = p;
        this.rt = new RoutingTable(p.getPeerGIDAsString());
        this.allPeers.put(p.getPeerGIDAsString(), p);
        this.initialized = true;
    }
    
    //Constructor
    private ConnectionManager(){}
    
    /////////////////////
    //Data manipulators//
    /////////////////////
    
    //Return true if unique peer, false if already in list
    public boolean addPeer(PeerData pd){
        //Check if initialized
        if(!this.initialized){
            return false;
        }
        //Check if trying to add local peer data
        if(this.local.getPeerGIDAsString().equals(pd.getPeerGIDAsString())){
            return false;
        }
        //Check for peer key in hashmap
        if(this.allPeers.containsKey(pd.getPeerGIDAsString())){
            return false;
        }
        
        //Create new Peer and add to HashMap
        Peer newPeer = new Peer(pd, false);
        this.allPeers.put(pd.getPeerGIDAsString(), newPeer);
        this.rt.addPeer(newPeer);
        
        return true;
    }
    
    //Return true if peer removed, false if not in list 
    public boolean removePeer(String peerGID){
        //Check if initialized
        if(!this.initialized){
            return false;
        }
        //Check that peerGID isnt local peerGID
        if(peerGID.equals(this.local.getPeerGIDAsString())){
            return false;
        }
        //Check for peer key in hashmap
        if(!this.allPeers.containsKey(peerGID)){
            return false;
        }
        
        Peer p = this.allPeers.remove(peerGID);
        this.rt.removePeer(peerGID);
        
        for(PeerLink pl: p.getPeerLinks()){
            removePeerLink(pl);
        }
        
        p.cleanup();
        
        return true;
    }
    
    //Return true if link added, false if link already exists
    public boolean addPeerLink(PeerLink pl){
        String peer1 = pl.getFirstNodeAsString();
        String peer2 = pl.getSecondNodeAsString();
        
        //Check if initialized
        if(!this.initialized){
            return false;
        }
        
        //Check for pre-existing link
        if(this.allPeers.get(peer1).hasLinkToPeer(peer2)){
            //Check for broken link
            if(!this.allPeers.get(peer2).hasLinkToPeer(peer1)){
                this.allPeers.get(peer2).addPeerLink(pl);
            }
            
            return false;
        }
        if(this.allPeers.get(peer2).hasLinkToPeer(peer1)){
            //Check for broken link
            if(!this.allPeers.get(peer1).hasLinkToPeer(peer2)){
                this.allPeers.get(peer1).addPeerLink(pl);
            }
            
            return false;
        }
        
        //Add peer link to both peers
        this.allPeers.get(peer1).addPeerLink(pl);
        this.allPeers.get(peer2).addPeerLink(pl);
        this.rt.addLink(peer1, peer2);
        
        return true;
    }
    
    //True if link removed, false otherwise
    public boolean removePeerLink(PeerLink pl){
        String peer1 = pl.getFirstNodeAsString();
        String peer2 = pl.getSecondNodeAsString();
        
        //Check if initialized
        if(!this.initialized){
            return false;
        }
        
        this.allPeers.get(peer1).removePeerLink(pl);
        this.allPeers.get(peer2).removePeerLink(pl);
        this.rt.removeLink(peer1, peer2);
        
        return true;
    }
    
    //Clears connection list, called during cleanup
    public void clearList(){
        this.initialized = false;
        for(Peer p: this.allPeers.values()){
            p.cleanup();
            this.rt.removePeer(p);
        }
        this.allPeers.clear();
        this.rt = null;
        this.setLocalPeer(this.local);
    }
    
    //Returns a PeerMap representation of the current state
    //of the ConnectionManager
    public PeerMap generatePeerMap(){
        ArrayList<PeerData> peerDataList = new ArrayList<>();
        ArrayList<PeerLink> peerLinkList = new ArrayList<>();

        //Add peers
        for(Peer p: allPeers.values()){
            peerDataList.add(p.getPeerData());
            
            //Add PeerLink if unique
            for(PeerLink pl: p.getPeerLinks()){
                if(!peerLinkList.contains(pl)){
                    peerLinkList.add(pl);
                }
            }
        }
        
        if(DEBUG){
            for(PeerData pd: peerDataList){
                System.out.println("generatePeerMap: "+pd.getString());
            }
            for(PeerLink pl: peerLinkList){
                System.out.println("generatePeerMap: "+pl.getString());
            }
        }
        
        return new PeerMap(peerDataList, peerLinkList);
    }
    
    //Returns true is successful merge, false if otherwise
    public boolean mergePeerMap(PeerMap pm){
        //Check if initialized
        if(!this.initialized){
            return false;
        }
        
        ArrayList<PeerData> peerDataList = pm.getPeerDataList();
        ArrayList<PeerLink> peerLinkList = pm.getPeerLinkList();
        if(DEBUG){
            for(PeerData pd: peerDataList){
                System.out.println("mergePeerMap: "+pd.getString());
            }
            for(PeerLink pl: peerLinkList){
                System.out.println("mergePeerMap: "+pl.getString());
            }
        }
        for(PeerData pd: peerDataList){
            //Check if peer already exists, if not, add the peer
            if(!this.hasPeer(pd)){
                this.addPeer(pd);
            }
        }
        
        for(PeerLink pl: peerLinkList){
            //Check if peerlink already exists, if not, add peerlink
            if(!this.hasLink(pl)){
                this.addPeerLink(pl);
            }
        }
        
        return true;
    }
    
    ///////////////////////// 
    //Communication methods//
    /////////////////////////
    
    //Send message to all peers
    public boolean sendBroadcast(byte[] data){
        //Check if initialized
        if(!this.initialized){
            return false;
        }
        
        Set<String> peerSet = this.allPeers.keySet();
        for(String s: peerSet){
            //Exclude sending messages to local peer
            if(!s.equals(this.local.getPeerGIDAsString())){
                sendToPeer(s, data);
            }
        }
        
        return true;
    }
    
    public boolean sendToPeer(String destGID, byte[] data){
        //if(DEBUG)System.out.println("HANDLER.sendToPeer: destGID: "+destGID.substring(0, 20));
        //Check if initialized
        if(!this.initialized){
            return false;
        }
        String adjPeer = rt.getAdjPeerForDest(destGID);
        //if(DEBUG)System.out.println("HANDLER.sendToPeer: adjGID: "+adjPeer);
        DDGameCommHandler.getInstance().sendMessage(adjPeer, data);
        
        return true;
    }
    
}
