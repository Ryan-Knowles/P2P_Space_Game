/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.graphics;

import ddgame.core.DDGameLoop;
import ddgame.core.GameState;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 *
 * @author rknowles
 */
public class DisplayWindowListener implements WindowListener{
    
    Display disp = null;

    public DisplayWindowListener(Display d) {
        this.disp = d;
    }
      
    @Override
    public void windowClosed(WindowEvent we) {

        System.out.println("windowClosed event");
    }
    
    @Override
    public void windowActivated(WindowEvent we) {
        //Do Nothing
    }

    @Override
    public void windowOpened(WindowEvent we) {
        //Do Nothing
    }

    @Override
    public void windowClosing(WindowEvent we) {
        disp.disable();
        DDGameLoop.getInstance().setGameState(GameState.UNLOADING);
        System.out.println("windowClosing event");
    }

    @Override
    public void windowIconified(WindowEvent we) {
        //Do Nothing
    }

    @Override
    public void windowDeiconified(WindowEvent we) {
        //Do Nothing
    }

    @Override
    public void windowDeactivated(WindowEvent we) {
        //Do Nothing
    }
}
