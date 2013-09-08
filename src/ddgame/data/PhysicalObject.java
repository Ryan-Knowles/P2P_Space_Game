/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.data;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import ddgame.physics.PolarVector;

/**
 *
 * @author rknowles
 */
public class PhysicalObject extends BaseGraphicObject{
    protected Shape shape;        //Collision detection object
    protected double xPosition, yPosition;    //Objects top-left corner at 90 degree facing
    protected double xCenter, yCenter;        //Center point of object
    private double xVelocity, yVelocity, netVelocity;
    protected double moveAngle, faceAngle;
    private double forwardAccel, reverseAccel;
    protected double width, height;           //Physical Width and Height
    private double mass, frictionConstant;
    private double turnSpeed;
    
    //State Variables
    private boolean accelerating;
    private boolean reverse;
    private boolean turningLeft, turningRight;
    private boolean initialized;                //Is object initialized?
    private boolean alive;
    
    //Time Variables
    protected long lastTick;                  	//Last time the move() function was called.
    protected final double TICK_RATE = 1000.0;	//1000 Millisecond tick rate
    
    //Getters
    public Shape getShape() {return this.shape;}
    public Point getPosition() {return new Point((int)xPosition,(int)yPosition);}
    public Point getCenter() {return new Point((int)xCenter,(int)yCenter);}
    public double getXVelocity() {return xVelocity;}
    public double getYVelocity() {return yVelocity;}
    public double getNetVelocity() {return netVelocity;}
    public double getFacing() {return faceAngle;}
    public double getMoving() {return moveAngle;}
    public int getWidth() {return (int) width;}
    public int getHeight() {return (int) height;}
    public double getTurnSpeed() {return turnSpeed;}
    public boolean isAccel() {return accelerating;}
    public boolean istReverse() {return reverse;}
    public boolean isTurnLeft() {return turningLeft;}
    public boolean isTurnRight() {return turningRight;}
    public boolean isAlive() {return alive;}
    public boolean isInitialized() {return initialized;}
    
    //Setters
    public void setAcceleration(boolean val) {this.accelerating = val;}
    public void setReverse(boolean val) {this.reverse = val;}
    public void setTurningLeft(boolean val) {this.turningLeft = val;}
    public void setTurningRight(boolean val) {this.turningRight = val;}
    public void setAlive(boolean val) {this.alive = val;}
    
    //Setters used to update position of other player's 
    //ships from network data
    public void setFaceAngle(double val){this.faceAngle = val;}
    public void setXCenter(int val){this.xCenter = (double) val;}
    public void setYCenter(int val){this.yCenter = (double) val;}
    public void setMoveAngle(double val){this.moveAngle = val;}
    public void setXVelocity(double val){this.xVelocity = val;}
    public void setYVelocity(double val){this.yVelocity = val;}
    
    //Constructors
    public PhysicalObject(String pathname) {
        super(pathname);
        alive = true;
        accelerating = false;
        reverse = false;
        turningLeft = false;
        turningRight = false;
        initialized = false;
    }
    
    public PhysicalObject(String[] pathname) {
        super(pathname);
        alive = true;
        accelerating = false;
        reverse = false;
        turningLeft = false;
        turningRight = false;
        initialized = false;
    }
    
    //Initializer
    public void init(double mass, double friction, double xCenter, double yCenter,
                     double turnSpeed, double fa, double ra, double facing, 
                     double height, double width) {
    	
        this.mass = mass;
        this.frictionConstant = friction;
        this.xCenter = xCenter;
        this.yCenter = yCenter;
        this.turnSpeed = turnSpeed;
        this.forwardAccel = fa;
        this.reverseAccel = ra;
        this.faceAngle = facing;
        this.moveAngle = facing;
        this.height = height;
        this.width = width;
        this.xPosition = this.xCenter+(this.width/2);
        this.yPosition = this.yCenter+(this.height/2);
        this.initialized = true;
        
        this.lastTick = System.currentTimeMillis();
        this.Update((int)this.xCenter+100, (int)this.yCenter+100);
    } 
    
    //Object collision
    public boolean isHitBy(Shape s) {
        Area myShape = new Area(this.shape);
        Area otherShape = new Area(s);
        Area intersect = (Area) myShape.clone();
        intersect.intersect(otherShape);    //Area is now the intersection of
                                            //myShape and otherShape (if any).
        
        return !intersect.isEmpty();
    }
    
    //Update based on time elapsed and acceleration
    public void Update(int xBound, int yBound) {
        //Make sure model is fully initialized before running
        if (!initialized) return;
        
        //Find difference in time
        long currentTime = System.currentTimeMillis();
        double timeModifier = (currentTime-lastTick)/TICK_RATE;
        //System.out.println("Current: "+currentTime+" Last: "+lastTick+"Diff: "+(currentTime-lastTick));
        //System.out.println("timeModifier: "+timeModifier+" turnSpeed*timeMod: "+(turnSpeed*timeModifier));
        calcRotation(timeModifier);
        calcVelocity(timeModifier);
        
        //Update center
        xCenter += xVelocity;
        yCenter += yVelocity;

        //Apply bounds
        if(xCenter<0)xCenter+=xBound;
        if(xCenter>xBound)xCenter-=xBound;
        if(yCenter<0)yCenter+=yBound;
        if(yCenter>yBound)yCenter-=yBound;

        //Calculate position of top-left edge (at 0 degrees)
        xPosition = xCenter-(width/2.0);
        yPosition = yCenter-(height/2.0);

        Rectangle2D.Double rect = new Rectangle2D.Double(xPosition+(width/2), yPosition+(height/2), width, width);
        shape = AffineTransform.getRotateInstance(faceAngle, xPosition, yPosition).createTransformedShape(rect);
        
        lastTick = currentTime;
    }
    
    public void updateFromCollision(PhysicalObject po) {
        
    }
    
    private void calcRotation(double timeMod) {
        if (turningRight && turningLeft) {
            //Do nothing
        } else if (turningRight) {
            faceAngle += turnSpeed*timeMod;
        } else if (turningLeft) {
            faceAngle -= turnSpeed*timeMod;
        }
        
        //Make sure faceAngle stays between 0 and 360 degrees
        if(faceAngle>360) faceAngle -= 360;
        if(faceAngle<0) faceAngle += 360;
    }
    
    private void calcVelocity(double timeMod) {
        final double DOWNWARD_ACCEL = 5.0;
        double acceleration = (!reverse) ? forwardAccel : reverseAccel;
        double relativeFacing = (!reverse) ? faceAngle : faceAngle-180;
           
        //Calculate net Acceleration/Deceleration
        double netDeceleration = (DOWNWARD_ACCEL * frictionConstant+(Math.pow(netVelocity, 2)*0.1))*timeMod;
        double relativeAcceleration = acceleration*timeMod;
        
        //System.out.println("Accel: "+acceleration+" netDec: "+netDeceleration);
        //System.out.println("netAccel: "+netAccleration);
        
        double newVelocity = ((netVelocity-netDeceleration)>0) ? (netVelocity-netDeceleration) : 0;
        PolarVector currentVector = new PolarVector(moveAngle, newVelocity);
        
        //If accelerating, add vector of direction of acceleration to
        //current vector of movement
        if (accelerating) {
            //Create PolarVectors and combine
            PolarVector newVector = new PolarVector(relativeFacing, relativeAcceleration);
            
            currentVector.add(newVector);
            

        } 
        
        //Update angles and velocities
        netVelocity = currentVector.getMagnitude();
        moveAngle = currentVector.getAngle();
        xVelocity = currentVector.getXVal();
        yVelocity = currentVector.getYVal();
        
    }
    
    
}
