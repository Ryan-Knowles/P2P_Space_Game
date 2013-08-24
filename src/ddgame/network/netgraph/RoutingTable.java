/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.network.netgraph;

import ddgame.network.ConnectionManager;
import ddgame.network.data.Peer;
import ddgame.network.protocol.datatypes.PeerLink;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author rknowles
 */
public class RoutingTable {
    //HashMap of all links between a Peer N and its neighbors
    private HashMap<String, ArrayList<String>> peerConnections = new HashMap<>();
    //Routing table, in order to get from local Peer to some Peer N,
    //the relativeRoute returns which adjacent peer to send the message
    //to for shortest path delivery. 
    //Format is: <DestinationPeer, AdjacentPeer to send to>
    private HashMap<String, String> relativeRoute = new HashMap<>();
    //GID of the local peer
    private String localGID;
    
    //Getters
    public HashMap<String, ArrayList<String>> getPeerConnections(){return this.peerConnections;}
    public boolean hasPeer(String peerGID){return this.peerConnections.containsKey(peerGID);}
    public boolean hasLink(String peerGID1, String peerGID2){
        
        //Check if peerGID1 and peerGID2 are valid keys
        if(!this.peerConnections.containsKey(peerGID1)||
                !this.peerConnections.containsKey(peerGID2)){
            return false;
        }
        
        //Check for existing links and clean up broken links
        
        //If peerGID1 has no link to peerGID2 return false
        if(!this.peerConnections.get(peerGID1).contains(peerGID2)){
            //If peerGID2 has a link to peerGID1 when peerGID1 has no link
            //to peerGID2, remove peerGID2's link to peerGID1
            if(this.peerConnections.get(peerGID2).contains(peerGID1)){
                this.peerConnections.get(peerGID2).remove(peerGID1);
            }
            
            return false;
        }
        //If peerGID2 has no link to peerGID1 return false
        if(!this.peerConnections.get(peerGID2).contains(peerGID1)){
            //If peerGID1 has a link to peerGID2 when peerGID2 has no link
            //to peerGID1, remove peerGID1's link to peerGID2
            if(this.peerConnections.get(peerGID1).contains(peerGID2)){
                this.peerConnections.get(peerGID1).remove(peerGID2);
            }
            
            return false;
        }
        
        //Return true if peerGID1 has a link to peerGID2 and peerGID2
        //has a link to peerGID1.
        return true;
    }
    
    public String getAdjPeerForDest(String destGID){
        //Check if destGID key is valid key, return null if not
        if(!this.relativeRoute.containsKey(destGID)){
            return null;
        }
        
        return this.relativeRoute.get(destGID);
    }
    
    //Constructor
    public RoutingTable(String localGID){
        this.localGID = localGID;
        this.addPeer(localGID);
    }

    //Add node, returns true if successful, false if node already exists
    public boolean addPeer(String peerGID) {
        if(!peerConnections.containsKey(peerGID)){
            peerConnections.put(peerGID, new ArrayList<String>());
            return true;
        }
        return false;
    }
    
    public boolean addPeer(Peer p){
        String peerGID = p.getPeerGIDAsString();
        return addPeer(peerGID);
    }
    
    //Remove node, return true if node exists and is removed, false if node
    //doesn't exist
    public boolean removePeer(String peerGID){
        if(peerConnections.containsKey(peerGID)){
            return false;
        }
        
        ArrayList<String> linksToRemove = peerConnections.get(peerGID);
        for(String gid: linksToRemove){
            removeLink(peerGID, gid);
        }
        peerConnections.remove(peerGID);
        
        //Update route table
        calcRoutes();
        
        return true;
    }
    
    public boolean removePeer(Peer p){
        String peerGID = p.getPeerGIDAsString();
        return removePeer(peerGID);
    }
    
    //Add link, returns true if link doesn't already exist and is successfully
    //added, false if link already exists or link wasn't added.
    public boolean addLink(String peerGID1, String peerGID2){
        return addLink(peerGID1, peerGID2, true);
    }
    
    public boolean addLink(String peerGID1, String peerGID2, boolean reCalc){
        //Check for peer existance, add if doesn't exist
        if(!this.hasPeer(peerGID1)){
            this.addPeer(peerGID1);
        }
        if(!this.hasPeer(peerGID2)){
            this.addPeer(peerGID2);
        }
        
        //Check for pre-existing link
        if(this.hasLink(peerGID1, peerGID2)){
            return false;
        }
        
        //Everything should now be good to go to add
        //the links.
        peerConnections.get(peerGID1).add(peerGID2);
        peerConnections.get(peerGID2).add(peerGID1);
            
        //Finally, update routes if reCalc is true
        if(reCalc){ calcRoutes(); }
        
        return true;
    }
    
    //Always return true, accepts multiple links to add. Suspends
    //recalculation of relativeRoute table until after all links
    //have been processed.
    public boolean addLink(ArrayList<PeerLink> peerLinks){
        for(PeerLink pl: peerLinks){
            String node1 = pl.getFirstNode().getPeerGID();
            String node2 = pl.getSecondNode().getPeerGID();
            addLink(node1, node2, false);
        }
        calcRoutes();
        return true;
    }
    //Remove link, returns true if link exists and is successfully remove,
    //false if link doesn't exist.
    public boolean removeLink(String peerGID1, String peerGID2){
        return removeLink(peerGID1, peerGID2, true);
    }
    
   
    public boolean removeLink(String peerGID1, String peerGID2, boolean reCalc){

        //Check if link exists
        if(!this.hasLink(peerGID1, peerGID2)){
            return false;
        }
        
        //Remove peers from each other's lists
        peerConnections.get(peerGID1).remove(peerGID2);
        peerConnections.get(peerGID2).remove(peerGID1);
        
        //Check for peer without connections and remove them (if peerGID is not
        //the GID of the local peer)
        if(peerConnections.get(peerGID1).isEmpty()&&!peerGID1.equals(localGID)){
            peerConnections.remove(peerGID1);
            ConnectionManager.getInstance().removePeer(peerGID1);
        }
        
        if(peerConnections.get(peerGID2).isEmpty()&&!peerGID2.equals(localGID)){
            peerConnections.remove(peerGID2);
            ConnectionManager.getInstance().removePeer(peerGID2);
        }
        
        //Finally, update routes if reCalc is true
        if(reCalc){ calcRoutes(); }
        
        return true;
    }
    
    //Always return true, accepts multiple links to remove. Suspends
    //recalculation of relativeRoute table until after all links
    //have been processed.
    public boolean removeLink(ArrayList<PeerLink> peerLinks){
        for(PeerLink pl: peerLinks){
            String node1 = pl.getFirstNode().getPeerGID();
            String node2 = pl.getSecondNode().getPeerGID();
            removeLink(node1, node2, false);
        }
        calcRoutes();
        return true;
    }
    
    //
    //Update routingtable
    //
    private void calcRoutes(){
        //Implement a bfs
        ArrayList<String> unfoundPeers = new ArrayList<>();
        unfoundPeers.addAll(peerConnections.keySet());
        
        //Clear route hashmap
        relativeRoute.clear();
        
        //Add local
        relativeRoute.put(localGID, localGID);
        unfoundPeers.remove(localGID);
        //Add relatives of local
        for(String s: peerConnections.get(localGID)){
            relativeRoute.put(s, s);
            unfoundPeers.remove(s);
        }
        findRoutes(unfoundPeers, localGID);
        condenseRoutes();
    }
    
    private void findRoutes(ArrayList<String> peers, String rootGID){
        if(!peers.isEmpty()){
            ArrayList<String> adjPeers = peerConnections.get(rootGID);
            
            for(String s: adjPeers){
                //If peer hasn't been found yet, add peer connection
                if(peers.contains(s)){
                    peers.remove(s);
                    relativeRoute.put(s, rootGID);
                    
                }
            }
            
            for(String s: adjPeers){
                findRoutes(peers, s);
            }
        }
    }
    
    //Iterates through relativeRoute hashmap and transforms any
    //value that isn't an adjacent or local peer GID into an adjacent
    //peer GID
    private void condenseRoutes(){
        boolean notCondensed = true;
        ArrayList<String> validGIDs = new ArrayList<>();
        validGIDs.addAll(peerConnections.get(localGID));
        validGIDs.add(localGID);
        
        //Removes fillerGIDs from the relativeRoute map
        while(notCondensed){
            notCondensed = false;
            
            for(String s: relativeRoute.keySet()){
                if(!validGIDs.contains(s)){
                    String fillerGID = relativeRoute.remove(s);
                    relativeRoute.put(s, relativeRoute.get(fillerGID));
                    notCondensed = true;
                }
            }
        }
    }
    
    
}
