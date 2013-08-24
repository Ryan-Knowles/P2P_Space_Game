/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.graphics;

import ddgame.data.DataController;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 *
 * @author rknowles
 */
public class DisplayMouseListener implements MouseListener {

    @Override
    public void mouseClicked(MouseEvent e) {
        DataController.getInstance().mouseClickedAtPoint(e.getPoint());
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
