/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.data;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author rknowles
 */
public class BaseGraphicObject {
    private static final boolean DEBUG = false;
    
    private BufferedImage[] image;
    private int index = 0;
    private long startTime;
    private long playLength;
    private boolean playing;
    private boolean looping;
    private long freq = 1;

    
    public BaseGraphicObject(String pathname) {
        image = new BufferedImage[1];
        
        try {
            if(DEBUG)System.out.println("Reading file: "+pathname);
            this.image[0] = ImageIO.read(this.getClass().getResource(pathname));
        } catch (IOException ex) {
            Logger.getLogger(BaseGraphicObject.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public BaseGraphicObject(String[] pathname) {
        image = new BufferedImage[pathname.length];
        
        for(int ii=0;ii<pathname.length;ii++){
            try {
                this.image[ii] = ImageIO.read(new File(pathname[ii]));
            } catch (IOException ex) {
                Logger.getLogger(BaseGraphicObject.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    //Play animation over input milliseconds
    public void playOnce(long ms) {
        this.startTime = System.currentTimeMillis();
        this.freq = ms;
        this.playing = true;
        this.looping = false;
    }
    
    //Play animation with input millisecond repitition
    public void playContinuous(long ms) {
        this.startTime = System.currentTimeMillis();
        this.freq = ms;
        this.playing = true;
        this.looping = true;
    }
    
    public void stop() {
        this.playing = false;
        this.looping = false;
        this.index=0;
    }
       
    //Getter
    public BufferedImage getImage() {
        double timeRatio = (startTime-System.currentTimeMillis())/freq;
        BufferedImage returnImage = null;
        //Check for end of loop
        if(!looping&&playing&&timeRatio>1.0){
            returnImage = image[image.length-1];
            stop();
        }else if(playing) {
            index=(int) (timeRatio*(image.length-1)%image.length);
            returnImage = image[index];
        }
        else {
            returnImage = image[index];
        }
        
        return returnImage;
    }
    
    public int getLength() {
        return this.image.length;
    }
    
    //Setter
    public void setIndex(int ind){
        this.index = ind%image.length;
    }
}
