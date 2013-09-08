/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.data;

import java.util.ArrayList;

import ddgame.network.protocol.datatypes.PeerData;

/**
 *
 * @author rknowles
 */
public class Ship extends SimplePO{
    
    private PeerData peer;
    private long lastUpdate;
    private float currentHull;
    private float maxHull;
    ArrayList<Armament> weapons = new ArrayList<Armament>();
    
    //Getters
    public String getName() {return this.peer.getPlayerNameAsString();}
    public String getGID() {return this.peer.getPeerGIDAsString();}
    public long getLastUpdate(){return this.lastUpdate;}
    public PeerData getPeer(){return this.peer;}
    
    //Setters
    public void setName(String name){this.peer = new PeerData(name, this.peer.getPeerGIDAsString());}
    public void setLastUpdate(long lastUp){this.lastUpdate = lastUp;}
    
    public Ship(String pathname, PeerData pd) {
        super(pathname);
        this.peer = pd;
    }
    
    public Ship(String[] pathname, PeerData pd) {
        super(pathname);
        this.peer = pd;
    }
    
    public void addWeapon(Armament arm) {
        weapons.add(arm);
    }
}
