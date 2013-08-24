/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.core;

import ddgame.network.DDGameCommHandler;
import ddgame.network.NetworkController;

/**
 *
 * @author rknowles
 */
public class DDGameMain {
    public static void init() {
        NetworkController.init();
        DDGameLoop.getInstance().setGameState(GameState.STANDBY);
        DDGameLoop.getInstance().start();
    }
    
    public static void startGame() {
        DDGameLoop loop = DDGameLoop.getInstance();
        if(loop.getGameState()==GameState.STANDBY){
            loop.setGameState(GameState.LOADING);
        }
    }
    
    public static void main(String[] args) {
        DDGameCommHandler.getInstance().newPluginDataReadyEvent("testGID", "testName");
        DDGameMain.init();
        DDGameMain.startGame();
    }
}
