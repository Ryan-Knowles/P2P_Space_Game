/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.data;

import ddgame.network.protocol.datatypes.ShipData;
import java.awt.Point;
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
    
    //Constructor
    private DataController() {}
    
    public void updateDataState() {
        //call update() method on all PhysicalObjects
        playerShip.Update(bgTileset.getWidth(), bgTileset.getHeight()); //Make this a rectangle with bounds
        
        for(Ship s: otherShips.values()){
            s.Update(bgTileset.getWidth(), bgTileset.getHeight());
        }
        //checkCollisions();
    
    }
    
    public void mouseClickedAtPoint(Point p) {
        //playerShip.fireMissleAtPoint(p);
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
    
    public void makePlayerShip(String playerName){
        this.playerShip = new Ship(imagePath, playerName);
        initNewShip(this.playerShip);
    }
    
    //Returns true if ship successfully created and peerGID doesn't 
    //already exist, false if peerGID already exists or ship isn't
    //successfully created.
    public boolean addShip(String peerGID, String playerName){
        //Check if peerGID already exists
        if(otherShips.containsKey(peerGID)){
            return false;
        }
        //Create and initialize new ship then add to HashMap
        Ship ship = new Ship(imagePath, playerName);
        initNewShip(ship);
        otherShips.put(peerGID, ship);
        
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
    
    //Returns true if peerGID exists and associated ship is 
    //successfully updated, false if peerGID doesn't exist
    public boolean updateShipFromPeer(String peerGID, ShipData sd){
        //Check if peerGID already exists
        if(!otherShips.containsKey(peerGID)){
            return false;
        }
        
        //Update ship from ShipData
        Ship ship = otherShips.get(peerGID);
        if(!ship.getName().equals(sd.getPlayerNameAsString())) {
            ship.setName(sd.getPlayerNameAsString());
        }
        ship.setFaceAngle(sd.getShipFacing());
        ship.setXCenter(sd.getXCenter());
        ship.setYCenter(sd.getYCenter());
        ship.setMoveAngle(sd.getMoveFacing());
        ship.setXVelocity(sd.getXVelocity());
        ship.setYVelocity(sd.getYVelocity());
        
        return true;
    }
          
    //Initializes ship to a random position
    private void initNewShip(Ship s){
        rand.setSeed(System.currentTimeMillis());
        double xCenter = rand.nextInt(bgTileset.getWidth()-1);
        double yCenter = rand.nextInt(bgTileset.getHeight()-1);
        double facing = rand.nextDouble()*360;
        //s.init(850, 0.5, xCenter, yCenter, 60, 30, 10, facing, 256, 128);
        s.init(850, 0.5, 2000, 2000, 60, 30, 10, facing, 256, 128);
    }
    
    //Used to remove ships
    public void cleanup(){
        this.otherShips.clear();
        this.effects.clear();
        this.projectiles.clear();
        this.playerShip = null;
    }
    
    
}
