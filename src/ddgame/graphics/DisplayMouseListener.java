/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.graphics;

import ddgame.data.DataController;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;

/**
 *
 * @author rknowles
 */
public class DisplayMouseListener implements MouseListener {

	private Display parent;
	
	public DisplayMouseListener(Display parent) {
		super();
		this.parent = parent;
	}
	
    @Override
    public void mouseClicked(MouseEvent e) {
    	Point2D clicked = e.getPoint();
    	Point2D cen = new Point2D.Double(parent.getWidth()/2, parent.getHeight()/2); 
    	Point2D dest = new Point2D.Double(clicked.getX()-cen.getX(), clicked.getY()-cen.getY());
        DataController.getInstance().mouseClickedAtPoint(dest);
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
    
}
