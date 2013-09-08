/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.data;

import ddgame.network.protocol.datatypes.PeerData;
import ddgame.network.protocol.datatypes.ShipData;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author rknowles
 */
public class DataController {
    //Singleton Pattern Implementation
    private static DataController instance = null;
    public static DataController getInstance() {
        if(instance==null) instance = new DataController();
        return instance;
    }
    
    //Variables used in ship creation and initialization
    private final String imagePath = "/gfx/Flagship/1.png";
    private Random rand = new Random();
    
    //Variables to keep track of ships and update them
    private HashMap<String, Ship> otherShips = new HashMap<>();
    private ArrayList<Ordinance> projectiles = new ArrayList<Ordinance>();
    private ArrayList<Effect>   effects = new ArrayList<Effect>();
    private Ship playerShip;
    private BackgroundTileset bgTileset = new BackgroundTileset();    
    
    
    //Getter
    public Ship getPlayerShip() {return this.playerShip;}
    public Ship getShip(String peerGID){return this.otherShips.get(peerGID);}
    public BufferedImage getBackgroundImage() {return this.bgTileset.getBgImage();}
    public Collection<Ship> getShips() {return this.otherShips.values();}
    public Ship getShipAt(int val){
    	ArrayList<Ship> ships = new ArrayList<Ship>();
    	ships.addAll(getShips());
    	if(ships.size()>val){
    		return ships.get(val);
    	}
    	else {
    		return null;
    	}
	}
    
    //Constructor
    private DataController() {}
    
    public void updateDataState() {
        //call update() method on all PhysicalObjects
        playerShip.Update(bgTileset.getWidth(), bgTileset.getHeight()); //Make this a rectangle with bounds
        playerShip.setLastUpdate(System.currentTimeMillis());
        
        ArrayList<String> oldShipGIDs = new ArrayList<>();
        synchronized(otherShips){
	        for(Ship s: otherShips.values()){
	        	if(s!=playerShip){
		            double dTime = System.currentTimeMillis()-s.getLastUpdate();
		            final double MAX_DELAY = 30000;	//30 Second delay
		        	if(dTime>MAX_DELAY){
		        		oldShipGIDs.add(s.getGID());
		        	}
		        	
		        	s.Update(bgTileset.getWidth(), bgTileset.getHeight());
	        	}
	        }
	        
	        for(String str: oldShipGIDs){
	        	this.removeShip(str);
	        }
        }
        
        
        
    }
    
    public void mouseClickedAtPoint(Point2D p) {
        playerShip.setDest(p);
    }
    
    public void setPhysicalStates(Ship s, boolean accel, boolean reverse, 
                                     boolean turnLeft, boolean turnRight) {
        s.setAcceleration(accel);
        s.setReverse(reverse);
        s.setTurningLeft(turnLeft);
        s.setTurningRight(turnRight);
        /*System.out.println("Updated Physical States: \nAccel: "+accel
            +" Reverse: "+reverse+" Turning Left: "+turnLeft+" Turning Right: "+turnRight);
        */
    }
    
    public void makePlayerShip(PeerData pd){
        this.playerShip = new Ship(imagePath, pd);
        initNewShip(this.playerShip);
        otherShips.put(playerShip.getGID(), playerShip);
    }
    
    //Returns true if ship successfully created and peerGID doesn't 
    //already exist, false if peerGID already exists or ship isn't
    //successfully created.
    public boolean addShip(PeerData pd){
        //Check if peerGID already exists
        if(otherShips.containsKey(pd.getPeerGIDAsString())){
            return false;
        }
        //Create and initialize new ship then add to HashMap
        Ship ship = new Ship(imagePath, pd);
        initNewShip(ship);
        otherShips.put(pd.getPeerGIDAsString(), ship);
        
        return true;
    }
    
    //Returns true if peerGID key exists and ship is successfully
    //removed, false if peerGID doesn't exist
    public boolean removeShip(String peerGID){
        //Check if peerGID key exists
        if(!otherShips.containsKey(peerGID)){
            return false;
        }
        
        //Remove key, value pair from hashmap
        otherShips.remove(peerGID);
        
        return true;
    }
    
    public Ship updateShipFromPeer(ShipData sd){
    	String peerGID = sd.getPeerData().getPeerGIDAsString();
    	
        //Check if peerGID exists
        if(!otherShips.containsKey(peerGID)){
        	this.addShip(sd.getPeerData());
        }
        
        
        //Update ship from ShipData
        Ship ship = otherShips.get(peerGID);
        
        //Ignore sd if not a newer update
    	if(sd.getLastUpdate()<=ship.getLastUpdate()){
    		return ship;
    	}
        
        if(!ship.getName().equals(sd.getPlayerNameAsString())) {
            ship.setName(sd.getPlayerNameAsString());
        }
        
        ship.setLastUpdate(sd.getLastUpdate());
        double dX = sd.getXDestination()-ship.getCenter().getX();
        double dY = sd.getYDestination()-ship.getCenter().getY();
        ship.setDest(new Point2D.Double(dX, dY));
        
        return ship;
    }
          
    
    public ArrayList<ShipData> getGetShipData() {
    	ArrayList<ShipData> shipdata = new ArrayList<>();
    	shipdata.add(new ShipData(playerShip));
    	
    	synchronized(otherShips) {
	    	for(Ship s: otherShips.values()) {
	    		shipdata.add(new ShipData(s));
	    	}
    	}
    		    
	    return shipdata;
}

    //Initializes ship to a random position
    private void initNewShip(Ship s) {
        rand.setSeed(System.currentTimeMillis());
        double xCenter = rand.nextInt(bgTileset.getWidth()-1);
        double yCenter = rand.nextInt(bgTileset.getHeight()-1);
        double facing = rand.nextDouble()*360;
        //s.init(850, 0.5, xCenter, yCenter, 60, 30, 10, facing, 256, 128);
        s.init(850, 0.5, 500, 500, 60, 30, 10, facing, 256, 128);
    }
    
    //Used to remove ships
    public void cleanup() {
        this.otherShips.clear();
        this.effects.clear();
        this.projectiles.clear();
        initNewShip(playerShip);
        otherShips.put(playerShip.getGID(), playerShip);
    }
    
}
