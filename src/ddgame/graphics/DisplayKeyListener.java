/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.graphics;

import ddgame.data.DataController;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 *
 * @author rknowles
 */
public class DisplayKeyListener implements KeyListener{
    private Display display = null;
    private DataController dataCon = DataController.getInstance();
    
    private boolean UP_KEY, DOWN_KEY, LEFT_KEY, RIGHT_KEY,
                    W_KEY, S_KEY, A_KEY, D_KEY,
                    ACCEL, REVERSE, LEFT_TURN, RIGHT_TURN;
    
    
    public DisplayKeyListener (Display disp) {
        this.display = disp;
        UP_KEY=DOWN_KEY=LEFT_KEY=RIGHT_KEY=A_KEY=W_KEY=S_KEY=D_KEY=false;
        //this.display.getFrame().ge
    }
    
    public void keyPressed (KeyEvent ke) {
        
        switch(ke.getKeyCode()) {
            case KeyEvent.VK_W:
                W_KEY = true;
                break;
            case KeyEvent.VK_UP:
                UP_KEY = true;
                break;
            case KeyEvent.VK_A:
                A_KEY = true;
                break;
            case KeyEvent.VK_LEFT:
                LEFT_KEY = true;
                break;
            case KeyEvent.VK_S:
                S_KEY = true;
                break;
            case KeyEvent.VK_DOWN:
                DOWN_KEY = true;
                break;
            case KeyEvent.VK_D:
                D_KEY = true;
                break;
            case KeyEvent.VK_RIGHT:
                RIGHT_KEY = true;
                break;
            default:
                break;
        }
        updateStates();
        dataCon.setPhysicalStates(dataCon.getPlayerShip(), ACCEL, REVERSE, LEFT_TURN, RIGHT_TURN);
    }
    
    public void keyReleased (KeyEvent ke) {
        switch(ke.getKeyCode()) {
            case KeyEvent.VK_W:
                W_KEY = false;
                break;
            case KeyEvent.VK_UP:
                UP_KEY = false;
                break;
            case KeyEvent.VK_A:
                A_KEY = false;
                break;
            case KeyEvent.VK_LEFT:
                LEFT_KEY = false;
                break;
            case KeyEvent.VK_S:
                S_KEY = false;
                break;
            case KeyEvent.VK_DOWN:
                DOWN_KEY = false;
                break;
            case KeyEvent.VK_D:
                D_KEY = false;
                break;
            case KeyEvent.VK_RIGHT:
                RIGHT_KEY = false;
                break;
        }
        updateStates();
        dataCon.setPhysicalStates(dataCon.getPlayerShip(), ACCEL, REVERSE, LEFT_TURN, RIGHT_TURN);
    }
    
    public void keyTyped (KeyEvent ke) {
        //System.out.println("Key Typed event!"+ke.getKeyChar());
        switch(ke.getKeyChar()) {
            case 'b':           //Show bounds
                typedB();
                break;
            case 'B':           
                typedB();
                break;
            case 'n':     //Show physics
                typedN();
                break;
            case 'N':     
                typedN();
                break;
            case 'v':     //Show ship variables
                typedV();
                break;
            case 'V':     
                typedV();
                break;
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            	typedNum(ke.getKeyChar()-'0');
        		break;
            default:
                break;
        }
    }
    
    private void updateStates(){
        //Update ACCEL state
        if (UP_KEY||W_KEY||DOWN_KEY||S_KEY) ACCEL=true;
        else ACCEL=false;
        //Update REVERSE state
        if (DOWN_KEY||S_KEY) REVERSE=true;
        else REVERSE=false;
        //Update LEFT_TURN state
        if (LEFT_KEY||A_KEY) LEFT_TURN=true;
        else LEFT_TURN=false;
        //Update RIGHT_TURN state
        if (RIGHT_KEY||D_KEY) RIGHT_TURN=true;
        else RIGHT_TURN=false;
    }
    
    private void typedB() {
        display.toggleShowBounds();
    }
    
    private void typedN() {
        display.togglePhysicsVariables();
    }
    
    private void typedV() {
        display.toggleShipVariables();
    }
    
    private void typedNum(int val) {
    	display.toggleSecondShip(val);
    }
}
