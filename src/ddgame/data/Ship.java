/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.data;

import java.util.ArrayList;

/**
 *
 * @author rknowles
 */
public class Ship extends PhysicalObject{
    
    private String shipName;
    private float currentHull;
    private float maxHull;
    ArrayList<Armament> weapons = new ArrayList<Armament>();
    
    //Getters
    public String getName() {return this.shipName;}
    
    //Setters
    public void setName(String name){this.shipName = name;}
    
    public Ship(String pathname, String name) {
        super(pathname);
        this.shipName = name;
    }
    
    public Ship(String[] pathname, String name) {
        super(pathname);
        this.shipName = name;
    }
    
    public void addWeapon(Armament arm) {
        weapons.add(arm);
    }
}
