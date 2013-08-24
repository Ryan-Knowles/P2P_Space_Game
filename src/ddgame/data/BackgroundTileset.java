/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.data;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 *
 * @author rknowles
 */
public class BackgroundTileset {
    public static final int NUM_STARFIELDS = 12;
    public static final int NUM_NEBULA = 9;
    public static final int MAP_WIDTH = 20;     //Measured in 256 pixel chunks
    public static final int MAP_HEIGHT = 20;    //Measured in 256 pixel chunks
    public static final int MAP_SCALE = 256;    //Pixels per chunk
    
    private BufferedImage[] starfield = new BufferedImage[NUM_STARFIELDS];
    private BufferedImage[] nebula = new BufferedImage[NUM_STARFIELDS];
    private BufferedImage bgImage = new BufferedImage(MAP_WIDTH*MAP_SCALE, MAP_HEIGHT*MAP_SCALE, BufferedImage.TYPE_INT_ARGB);
    private int[] bgPixels = ((DataBufferInt) bgImage.getRaster().getDataBuffer()).getData();
    private Random rand;
    
    //Getter
    public BufferedImage getBgImage() {return bgImage;}
    public int getWidth() {return MAP_WIDTH*MAP_SCALE;}
    public int getHeight() {return MAP_HEIGHT*MAP_SCALE;}
    
    public BackgroundTileset() {
        rand = new Random(System.currentTimeMillis());
        loadImages();
        //filterNebula();
        addStarfields();
        //addNebula();
    }
    
    private void addStarfields() {
        int xIndex = 0;
        int yIndex = 0;
        //Set starfields
        for (yIndex=0;yIndex<MAP_HEIGHT;yIndex++)
        {
            for (xIndex=0;xIndex<MAP_WIDTH;xIndex++){
                int xStart = xIndex*MAP_SCALE;
                int yStart = yIndex*MAP_SCALE;
                int sfIndex = Math.abs(rand.nextInt())%NUM_STARFIELDS;
                bgImage.getGraphics().drawImage(starfield[sfIndex], xStart, yStart, null);
                
                //int[] sfPixels = starfield[sfIndex].getRGB(0, 0, starfield[sfIndex].getWidth(), starfield[sfIndex].getHeight(), 
                                                            //null, 0, starfield[sfIndex].getWidth());
                //bgPixels[xStart+yStart*MAP_SCALE] = starfield[sfIndex].getRaster(). 
            }
        }
    }
    
    private void filterNebula() {
        int nebIndex = 0;
        
        for(nebIndex=0;nebIndex<NUM_NEBULA;nebIndex++) {
            int xIndex = 0;
            int yIndex = 0;
            BufferedImage img = nebula[nebIndex];
            for (yIndex=0;yIndex<img.getHeight();yIndex++){
                for (xIndex=0;xIndex<img.getWidth();xIndex++){
                    //get rgb in 0x--RRGGBB format
                    int rgb = img.getRGB(xIndex, yIndex);
                    //clear alpha value
                    //rgb = rgb*0xffffff00;
                    //If color isnt black, set alpha to 50%
                    if(false){//rgb  > 0 ) {
                        rgb = rgb | 0x0000007f;
                    }
                    //Set alpha to 100%
                    else {
                        rgb = rgb | 0x00ff0000;
                    }
                    img.setRGB(xIndex, yIndex, rgb);
                }
            }
        }
    }
    
    private void addNebula() {
        Color ignore = new Color(0, 0, 0);
        
        int xIndex = 0;
        int yIndex = 0;
        //Set starfields
        for (yIndex=0;yIndex<MAP_HEIGHT/2;yIndex++)
        {
            for (xIndex=0;xIndex<MAP_WIDTH/2;xIndex++){
                int xStart = xIndex*MAP_SCALE*2;
                int yStart = yIndex*MAP_SCALE*2;
                int nebIndex = Math.abs(rand.nextInt())%NUM_NEBULA;
                Graphics2D g2d = (Graphics2D) bgImage.getGraphics();
                g2d.drawImage(nebula[nebIndex], 0, 0, null);
            }
        }
    }
    
    
    private void loadImages() {
        int index = 0;
        
        //Load starfield images
        for(index=0; index<NUM_STARFIELDS; index++) {
            String filePath = "/gfx/Starfield/"+index+".png";
            //System.out.println("INT_RGB Type: "+BufferedImage.TYPE_INT_RGB+" INT_ARGB Type: "+BufferedImage.TYPE_INT_ARGB);
            try {
                starfield[index] = ImageIO.read(this.getClass().getResource(filePath));
                //System.out.println("Starfield "+index+"type: "+starfield[index].getType());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        //Load nebulea images
        for(index=0; index<NUM_NEBULA; index++) {
            String filePath = "/gfx/Nebula_RealisticHighContrast/"+index+".png";
            try {
                BufferedImage temp = ImageIO.read(this.getClass().getResource(filePath));
                nebula[index] = new BufferedImage(temp.getWidth(), temp.getHeight(), BufferedImage.TYPE_INT_ARGB);
                nebula[index].getGraphics().drawImage(temp, 0, 0, null);
                //System.out.println("Nebula "+index+"type: "+nebula[index].getType());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    

    
}
