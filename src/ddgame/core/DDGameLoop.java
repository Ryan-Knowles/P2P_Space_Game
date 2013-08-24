/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.core;

import ddgame.data.DataController;
import ddgame.graphics.Display;
import ddgame.network.NetworkController;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rknowles
 */
public class DDGameLoop implements Runnable{
    //Singleton Pattern Implementation
    private static DDGameLoop instance = null;
    public static DDGameLoop getInstance() {
        if(instance==null) instance = new DDGameLoop();
        return instance;
    }
    
    //Instantiated Code
    private volatile GameState state = null;
    private Display display = null;
    private int ticksPerSecond = 30;
    private boolean running = false;
    private Thread thread;
    
    private DDGameLoop() {
        thread = new Thread(this);
    }
    
    public synchronized void setDisplay(Display display) {
        this.display = display;
    }
    
    public synchronized void setGameState(GameState state) {
        this.state = state;
    }

    public GameState getGameState() {
        return state;
    }
    
    public synchronized void start() {
        running = true;
        thread.start();
    }
    
    public synchronized void stop() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(DDGameLoop.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run() {
        long lastTime = System.nanoTime();
        long currTime = 0;
        double nsPerTick = 1000000000D/(double)ticksPerSecond;
        double delta = 0;
        
        while(running) {  
            currTime = System.nanoTime();
            delta += (currTime-lastTime)/nsPerTick;
            lastTime = currTime;
            
            while(delta>= 1) {
            delta--;
            //System.out.println(state.name());
                switch(state) {
                    case STANDBY:
                        loopStandBy();
                        break;
                    case PLAYING:
                        loopPlaying();
                        break;
                    case LOADING:
                        loadGame();
                        break;
                    case UNLOADING:
                        unloadGame();
                        break;
                    case EXIT:
                        break;
                    default:
                        break;
                }
            }
        }
    }
    
    
    //Functionality 
    private void loopPlaying() {
        //Process network messages and PlatformEvents
        ddgame.network.NetworkController.updatePreMainUpdate();
        //Process data updates
        DataController.getInstance().updateDataState();
        //Update display based on new data state
        display.repaint();
        //Process outgoing messages
        ddgame.network.NetworkController.updatePostMainUpdate();
    }
    
    private void loopStandBy() {
        //Get network input
        //Process commands
        ddgame.network.NetworkController.updatePreMainUpdate();
        //Process outgoing messages
        ddgame.network.NetworkController.updatePostMainUpdate();
        
        //Sleep thread for 2 seconds
        try {    
            thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(DDGameLoop.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private void loadGame() {
        //Load xml data files
        //Load graphics files
        //Create background?
        DataController.getInstance();
        DDGameLoop.getInstance().setDisplay(new Display());
        DDGameLoop.getInstance().setGameState(GameState.PLAYING);
        display.enable();
    }
    
    private void unloadGame() {
        //Clear loaded files
        //Disconnect from peers, sent leave message
        DDGameLoop.getInstance().setDisplay(null);
        DDGameLoop.getInstance().setGameState(GameState.STANDBY);
        DataController.getInstance().cleanup();
        NetworkController.cleanup();
    }
}
