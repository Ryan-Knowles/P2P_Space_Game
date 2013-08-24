/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.data;

import java.awt.Point;

/**
 *
 * @author rknowles
 */
public class Effect extends BaseGraphicObject{
    private int xPosition, yPosition;
    private int width, height;
    
    public Effect(String pathname, Point p) {
        super(pathname);
        this.width = getImage().getWidth();
        this.height = getImage().getHeight();
    }
    public Effect(String[] pathname, Point p) {
        super(pathname);
        this.width = getImage().getWidth();
        this.height = getImage().getHeight();
    }
}
