/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.physics;

/**
 *
 * @author rknowles
 */
public class PolarVector {
    private double angle;   //Angle in degrees
    private double magnitude;
    private double xVal;
    private double yVal;

    public double getAngle() {return this.angle;}
    public double getMagnitude() {return this.magnitude;}
    public double getXVal() {return this.xVal;}
    public double getYVal() {return this.yVal;}

    public PolarVector(double theta, double magnitude) {
        //Set private variables from input
        this.angle = theta;
        this.magnitude = magnitude;
        
        //Calculate X and Y values
        this.xVal = this.magnitude*Math.cos(Math.toRadians(this.angle));
        this.yVal = this.magnitude*Math.sin(Math.toRadians(this.angle));
    }

    public void add(PolarVector pv2) {
        //Update X and Y values
        this.xVal = this.xVal + pv2.getXVal();
        this.yVal = this.yVal + pv2.getYVal();
        
        //Calculate magnitude
        this.magnitude = Math.hypot(this.xVal, this.yVal);
        
        //Calculate angle
        this.angle = Math.toDegrees(Math.atan2(this.yVal, this.xVal));
    }
    
}
