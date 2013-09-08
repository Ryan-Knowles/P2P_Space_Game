package ddgame.data;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class SimplePO extends PhysicalObject {

	private Point2D dest = null;
	private final int moveSpeed = 100;		//10 pixels per second
	
	public Point2D getDest(){return this.dest;}
	public void setDest(Point2D p){this.dest = p;}		//Offset from current position
	
	public SimplePO(String pathname) {
		super(pathname);
	}
	
	public SimplePO(String[] pathname) {
		super(pathname);
	}
	
	@Override
    //Update based on time elapsed and acceleration
    public void Update(int xBound, int yBound) {
		//Find difference in time
        long currentTime = System.currentTimeMillis();
        double timeModifier = (currentTime-super.lastTick)/super.TICK_RATE;
        super.lastTick = currentTime;
        
		//Check for void dest
		if (dest == null){
			return;
		}
		
        
		//Aim at dest and move
		double dX, dY;
		dX = dest.getX();
		dY = dest.getY();	
		
		if(dX == 0 && dY == 0){
			dest = null;
			return;
		}
		
		double facing = (dX!=0) ?  Math.toDegrees(Math.atan2(dY, dX)) : (dY>-1) ? 90 : 270;
		
		while(facing<0)
		{
			facing += 360;
		}
		
		if (facing>360)
		{
			facing = facing % 360;
		}
	
		super.setFaceAngle(facing);
		
		double xChange = moveSpeed * Math.cos(Math.toRadians(facing)) * timeModifier;
		double yChange = moveSpeed * Math.sin(Math.toRadians(facing)) * timeModifier;
		
        //Update center
        super.xCenter += xChange;
        super.yCenter += yChange;
        
        dX -= xChange;
        dY -= yChange;
        
        //Check for within +/-10 pixel range of target
        double dist = Math.sqrt(Math.pow(dX, 2)+Math.pow(dY, 2));
        if (dist>-2 && dist<2){
        	super.xCenter += dX;
        	dX = 0;
        	super.yCenter += dY;
        	dY = 0;
        }
        
        this.dest = new Point2D.Double(dX, dY);

        //Apply bounds
        if(super.xCenter<0){ super.xCenter+=xBound; }
        if(super.xCenter>xBound){ super.xCenter-=xBound; }
        if(super.yCenter<0){ super.yCenter+=yBound; }
        if(super.yCenter>yBound){ super.yCenter-=yBound; }

        //Calculate position of top-left edge (at 0 degrees)
        super.xPosition = super.xCenter-(super.width/2.0);
        super.yPosition = super.yCenter-(super.height/2.0);

        Rectangle2D.Double rect = new Rectangle2D.Double(super.xPosition+(super.width/2), super.yPosition+(super.height/2), super.width, super.width);
        super.shape = AffineTransform.getRotateInstance(super.faceAngle, super.xPosition, super.yPosition).createTransformedShape(rect);
        		
    }

}
