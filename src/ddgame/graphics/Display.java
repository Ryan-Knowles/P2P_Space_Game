/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.graphics;

import ddgame.data.DataController;
import ddgame.data.Ship;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Collection;
import javax.swing.JFrame;

/**
 *
 * @author rknowles
 */
public class Display extends Canvas {
    //DEBUG TOGGLE
    private static final boolean DEBUG = true;
    
    public static final int WIDTH = 160;
    public static final int HEIGHT = 160/12*9;
    public static final int SCALE = 7;
    public static final String NAME = "DDGame";
    public static final Dimension bounds = new Dimension(WIDTH*SCALE, HEIGHT*SCALE);
    
    private JFrame frame;
    private String title;
    
    private BufferedImage backbuffer = new BufferedImage(WIDTH*SCALE, HEIGHT*SCALE, BufferedImage.TYPE_4BYTE_ABGR);
    private Graphics2D g2d = backbuffer.createGraphics();
    private AffineTransform identity = new AffineTransform();
    
    private boolean showBounds = false;             //Show bounding shape of ship used in collisions
    private boolean showPhysicsVariables = false;   //Show physics variables of ship on screen
    private boolean showShipVariables = false;      //Show ship-specific variables on screen
    
    //Getter methods
    public JFrame getFrame() {return this.frame;}
    
    public Display() {
        this.title = NAME;
        setMinimumSize(bounds);
        setMaximumSize(bounds);
        setPreferredSize(bounds);
        
        frame = new JFrame(title);
        
        frame.addWindowListener(new DisplayWindowListener(this));
        this.addKeyListener(new DisplayKeyListener(this));
        this.addMouseListener(new DisplayMouseListener());
        
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(this, BorderLayout.CENTER);
        frame.pack();
        
        frame.setLocationRelativeTo(null);
        frame.setEnabled(false);
        frame.setResizable(false);
        frame.setVisible(false);
        

    }
    
    public void enable() {
        frame.setEnabled(true);
        frame.setVisible(true);
    }
    
    public void disable() {
        frame.setEnabled(false);
        frame.setVisible(false);
    }
    
    public void paint(Graphics g) {
        g.drawImage(backbuffer, 0, 0, null);
    }
    
    public void update(Graphics g) {
        g2d.setTransform(identity);
        
        Ship player = DataController.getInstance().getPlayerShip();
        Point shipCenter = player.getCenter();
        BufferedImage bgImage = DataController.getInstance().getBackgroundImage();
        
        drawBackground(shipCenter, bgImage);
        drawProjectiles();
        drawPlayer(player);
        drawShips(player);
        drawExplosions();
        drawGUI();
        //g.drawImage(bgTileset.getBgImage(), 0, 0, this.getWidth(), this.getHeight(), displayXCorner, displayYCorner, this.getWidth(), this.getHeight(), null);
        paint(g);
    }
    
    public void drawBackground(Point center, BufferedImage image) {
        g2d.setTransform(identity);
        //Base screen coordinates
        int bgStartX  = (int) (center.getX()-(this.getWidth()/2));
        int bgStartY  = (int) (center.getY()-(this.getHeight()/2));
        int bgFinishX = (int) (center.getX()+(this.getWidth()/2));
        int bgFinishY = (int) (center.getY()+(this.getHeight()/2));
        
        //Screen outside of bounds
        int sXRem = (bgStartX<0) ? bgStartX*-1:0;
        int sYRem = (bgStartY<0) ? bgStartY*-1:0;
        int fXRem = (bgFinishX>image.getWidth()) ? bgFinishX-image.getWidth():0;
        int fYRem = (bgFinishY>image.getHeight()) ? bgFinishY-image.getHeight():0;
        
        //drawImage rect, initial rect is rect where screen and background intersect
        int dx1 = sXRem;
        int dy1 = sYRem;
        int dx2 = this.getWidth()-fXRem;
        int dy2 = this.getHeight()-fYRem;
        int sx1 = (sXRem==0) ? bgStartX:0;
        int sy1 = (sYRem==0) ? bgStartY:0;
        int sx2 = (fXRem==0) ? bgFinishX:image.getWidth();
        int sy2 = (fYRem==0) ? bgFinishY:image.getHeight();
        
        g2d.drawImage(image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, this);

        //If top-left corner needs drawing
        if(sXRem>0&&sYRem>0){
            dx1 = dy1 = 0;
            dx2 = sXRem;
            dy2 = sYRem;
            sx1 = image.getWidth()-sXRem;
            sy1 = image.getHeight()-sYRem;
            sx2 = image.getWidth();
            sy2 = image.getHeight();
            g2d.drawImage(image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, this);
        } 
        //Bot-left corner
        else if (sXRem>0&&fYRem>0) {
            dx1 = 0;
            dy1 = this.getHeight()-fYRem;
            dx2 = this.getWidth()-sXRem;
            dy2 = this.getHeight();
            sx1 = image.getWidth()-sXRem;
            sy1 = fYRem;
            sx2 = image.getWidth();
            sy2 = 0;
            g2d.drawImage(image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, this);
        }
        //Bot-right corner
        else if (fXRem>0&&fYRem>0){
            dx1 = this.getWidth()-fXRem;
            dy1 = this.getHeight()-fYRem;
            dx2 = this.getWidth();
            dy2 = this.getHeight();
            sx1 = 0;
            sy1 = 0;
            sx2 = fXRem;
            sy2 = fYRem;
            g2d.drawImage(image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, this);
        }
        //Top-Right corner
        else if (sYRem>0&&fXRem>0){
            dx1 = this.getWidth()-fXRem;
            dy1 = 0;
            dx2 = this.getWidth();
            dy2 = sYRem;
            sx1 = 0;
            sy1 = image.getHeight()-sYRem;
            sx2 = fXRem;
            sy2 = image.getHeight();
            g2d.drawImage(image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, this);
        }
        
        //Draw Top
        if(sYRem>0) {
            dx1 = sXRem;
            dy1 = 0;
            dx2 = this.getWidth()-fXRem;
            dy2 = sYRem;
            sx1 = (sXRem==0) ? bgStartX:0;
            sy1 = image.getHeight()-sYRem;
            sx2 = (fXRem==0) ? bgFinishX:image.getWidth();
            sy2 = image.getHeight();
            g2d.drawImage(image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, this);
        }
        //Draw Left
        if(sXRem>0) {
            dx1 = 0;
            dy1 = sYRem;
            dx2 = sXRem;
            dy2 = this.getHeight()-fYRem;
            sx1 = image.getWidth()-sXRem;
            sy1 = (sYRem==0) ? bgStartY:0;
            sx2 = image.getWidth();
            sy2 = (fYRem==0) ? bgFinishY:image.getHeight();
            g2d.drawImage(image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, this);
        }
        //Draw Bottom
        if(fYRem>0) {
            dx1 = sXRem;
            dy1 = this.getHeight()-fYRem;
            dx2 = this.getWidth()-fXRem;
            dy2 = this.getHeight();
            sx1 = (sXRem==0) ? bgStartX:0;
            sy1 = 0;
            sx2 = (fXRem==0) ? bgFinishX:image.getWidth();
            sy2 = fYRem;
            g2d.drawImage(image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, this);
        }
        //Draw Right
        if(fXRem>0) {
            dx1 = this.getWidth()-fXRem;
            dy1 = sYRem;
            dx2 = this.getWidth();
            dy2 = this.getHeight()-fYRem;
            sx1 = 0;
            sy1 = (sYRem==0) ? bgStartY:0;
            sx2 = fXRem;
            sy2 = (fYRem==0) ? bgFinishY:image.getHeight();
            g2d.drawImage(image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, this);
        }

    }
    
    public void drawProjectiles() {
        //DataController.getInstance().get
    }
    
    public void drawPlayer(Ship player) {
        BufferedImage image = player.getImage();
        g2d.setTransform(identity);
        //double xCenter = player
        //AffineTransform at = AffineTransform.getRotateInstance(player.getFacing(), player, ALLBITS);
        if(showBounds) {
            g2d.transform(identity);
            g2d.setColor(Color.BLUE);
            //g2d.draw(player.getShape());
            g2d.fill(player.getShape());
        }
        
        //Calculate relative position on screen based on image width/height
        //and screen width/height
        int screenXPos = (int) (bounds.getWidth()/2)-(image.getWidth()/2);
        int screenYPos = (int) (bounds.getHeight()/2)-(image.getHeight()/2);
        
        //Rotate g2d
        g2d.rotate(Math.toRadians((player.getFacing()+90)%360), bounds.getWidth()/2, bounds.getHeight()/2);
        
        //Draw ship
        g2d.drawImage(image, screenXPos, screenYPos, null);
        
        //Print player name of ship
        g2d.setTransform(identity);
        g2d.setColor(Color.WHITE);
        
        int fontXPos = (int) (bounds.getWidth()/2)-(player.getWidth()/2);
        int fontYPos = (int) (bounds.getHeight()/2)-(player.getHeight()/2);
        
        Font f = g2d.getFont();
        g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 20f));
        g2d.drawString(player.getName(), fontXPos, fontYPos);
        g2d.setFont(f);
        
        //if(DEBUG)System.out.println("drawing "+player.getName()+" @ ("+screenXPos+","+screenYPos+")");
    }
    
    public void drawShips(Ship player) {
        g2d.setTransform(identity);
        Collection<Ship> ships = DataController.getInstance().getShips();
        Point playerPoint = player.getCenter();
        for(Ship s: ships){
            Point sPoint = s.getCenter();
            int xDist = (int)sPoint.getX()-(int)playerPoint.getX()+(this.getWidth()/2);
            int yDist = (int)sPoint.getY()-(int)playerPoint.getY()+(this.getHeight()/2);
            
            
            
            //Check if ship is on screen, if so, draw it
            if(xDist>0&&xDist<this.getWidth()&&yDist>0&&yDist<this.getHeight()){
                BufferedImage image = s.getImage();
                
                //Rotate g2d
                g2d.rotate(Math.toRadians((s.getFacing()+90)%360),xDist, yDist);

                //Draw ship
                g2d.drawImage(image, (xDist-image.getWidth()/2), (yDist-image.getHeight()/2), null);

                //if(DEBUG)System.out.println("drawing "+s.getName()+" with Center: ("+s.getCenter().x+","+s.getCenter().y+")"+"on screen @ ("+(xDist-image.getWidth()/2)+","+(yDist-image.getHeight()/2)+")");
                
                //Print player name of ship
                g2d.setTransform(identity);
                g2d.setColor(Color.WHITE);

                int fontXPos = (int) (xDist)-(s.getWidth()/2);
                int fontYPos = (int) (yDist)-(s.getHeight()/2);

                Font f = g2d.getFont();
                g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 20f));
                g2d.drawString(s.getName(), fontXPos, fontYPos);
                g2d.setFont(f);
            }
        }
    }
    
    public void drawExplosions() {
        
    }
    
    public void drawGUI() {
        if(showPhysicsVariables) drawPhysicsVariables();
        if(showShipVariables) drawShipVariables();
    }
    
    private void drawPhysicsVariables() {
        g2d.setTransform(identity);
        g2d.setColor(Color.RED);
        Ship s = DataController.getInstance().getPlayerShip();
        String temp;
        temp = String.format("Position: (%.2f,%.2f)",s.getPosition().getX(), s.getPosition().getY());
        g2d.drawChars(temp.toCharArray(), 0, temp.length(), 600, 10);
        temp = String.format("Center: (%.2f,%.2f)", s.getCenter().getX(), s.getCenter().getY());
        g2d.drawChars(temp.toCharArray(), 0, temp.length(), 600, 25);
        temp = String.format("Velocity: (%.2f,%.2f)", s.getXVelocity(), s.getYVelocity());
        g2d.drawChars(temp.toCharArray(), 0, temp.length(), 600, 40);
        temp = String.format("NetVelocity: %.2f Moving: %.2f", s.getNetVelocity(), s.getMoving());
        g2d.drawChars(temp.toCharArray(), 0, temp.length(), 600, 55);
        temp = "Accelerate: "+s.isAccel()+" Reverse: "+s.istReverse();
        g2d.drawChars(temp.toCharArray(), 0, temp.length(), 600, 70);
        temp = "TurnLeft: "+s.isTurnLeft()+" TurnRight: "+s.isTurnRight();
        g2d.drawChars(temp.toCharArray(), 0, temp.length(), 600, 85);
        temp = "Alive: "+s.isAlive()+" Initialized: "+s.isInitialized();
        g2d.drawChars(temp.toCharArray(), 0, temp.length(), 600, 100);
        temp = String.format("Facing: %.2f TurnSpeed: %.0f", s.getFacing(), s.getTurnSpeed());
        g2d.drawChars(temp.toCharArray(), 0, temp.length(), 600, 115);
        
    }
    
    private void drawShipVariables() {
        g2d.setTransform(identity);
        //g2d.drawChars(data, 0, 0, 0, 0);  //To do
    }
    
    //Toggle methods
    public void toggleShowBounds() {
        this.showBounds = !this.showBounds;
        if(DEBUG)System.out.println("Bounds Toggled to: "+showBounds);
    }
    
    public void togglePhysicsVariables() {
        this.showPhysicsVariables = !this.showPhysicsVariables;
        if(DEBUG)System.out.println("Physics Variables Toggled to: "+showPhysicsVariables);
    }
    
    public void toggleShipVariables() {
        this.showShipVariables = !this.showShipVariables;
        if(DEBUG)System.out.println("Ship Variables Toggled to: "+showShipVariables);
    }
}
