package dd_p2p.plugin;


import ddgame.network.DDGameCommHandler;
import ddgame.core.DDGameMain;
import java.util.*;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;

class DDGamePluginRenderer implements plugin_data.PeerPluginRenderer 
{
    public java.awt.Component getTableCellRendererComponent(javax.swing.JTable a,java.lang.Object b,boolean c,boolean d,int e,int f) 
	{
		return new javax.swing.JLabel("DD Game");
    }
};

class DDGamePluginEditor implements plugin_data.PeerPluginEditor {
    public java.awt.Component getTableCellEditorComponent(javax.swing.JTable a,java.lang.Object b,boolean c,int d,int e){
	return null;
    }
    public void removeCellEditorListener(javax.swing.event.CellEditorListener a){}
    public void addCellEditorListener(javax.swing.event.CellEditorListener a){}
    public void cancelCellEditing(){}
    public boolean stopCellEditing(){return false;}
    public boolean shouldSelectCell(java.util.EventObject a){return false;}
    public boolean isCellEditable(java.util.EventObject a){return false;}
    public Object  getCellEditorValue(){return null;}
};


class StartAction extends AbstractAction {
    /**
     * Added serialVersionUID because eclipse told me to
     */
    private static final long serialVersionUID = 12345;

    public StartAction(String name, String desc) {
        super(name);
        putValue(SHORT_DESCRIPTION, desc);
    }
	
    //Start DDGame
    @Override
    public void actionPerformed(ActionEvent e) {
        //System.out.println("Start button pressed.");
        DDGameCommHandler.getInstance().newStartGameEvent();
    }
};




class JoinAction extends AbstractAction {
    /**
     * Added serialVersionUID because eclipse told me to
     */
    private static final long serialVersionUID = 12344512;

    public JoinAction(String name, String desc){
        super(name);
        putValue(SHORT_DESCRIPTION, desc);
    }
    
    //Send JoinRequest
    @Override
    public void actionPerformed(ActionEvent e) {
        //e.getSource() is a JMenuItem
    	//this.getValue() to get PluginMenus values
        String peerGID = (String) this.getValue(plugin_data.PluginMenus.GID);
        //String peerName = (String) this.getValue(plugin_data.PluginMenus.NAME);
        DDGameCommHandler.getInstance().newJoinRequestEvent(peerGID);
        
        //DEBUG info
        System.out.println("GID property: "+((String) (this.getValue(plugin_data.PluginMenus.GID))).substring(0, 20));
        System.out.println("NAME property: "+this.getValue(plugin_data.PluginMenus.NAME));
        System.out.println("MAIN GID var: "+Main.peer_GID.substring(0, 20));
        System.out.println("MAIN NAME var: "+Main.name);
    }
    
}



public class Main {
    private static final boolean DEBUG = false;
    static java.util.ArrayList<plugin_data.PluginRequest> queue = new java.util.ArrayList<plugin_data.PluginRequest>();
    public static String plugin_GID = "322145623_DD_Game";
    static String peer_GID = "default GID";
    static String plugin_name = "DD Game";
    static String name = "default name";
    static DDGamePluginRenderer renderer = new DDGamePluginRenderer();
    static DDGamePluginEditor editor = new DDGamePluginEditor();

    //Getters
    public static String getPeerGID(){return peer_GID;}
    public static String getPeerName(){return name;}
    
    //For testing purposes
    public static void main(String[] args){
        Main.init();
    }
	
	//Run main plugin functionality here
    public static void init() {
        if(DEBUG)System.out.println(plugin_name+":init() function has been run.");

        //Create StartAction
        StartAction start = new StartAction("Start", "Press to launch game");
        JoinAction join = new JoinAction("Join Game", "Click to join game");
        //Create Menu
        JMenuItem startJMI = new JMenuItem(start);
        plugin_data.PluginRequest startMenuItem = new plugin_data.PluginRequest();
        startMenuItem.type = plugin_data.PluginRequest.REGISTER_MENU;
        startMenuItem.plugin_GID = plugin_GID;
        startMenuItem.column = plugin_data.PluginMenus.COLUMN_MYSELF;
        startMenuItem.plugin_menuItem = startJMI;
        //startJMI.putClientProperty(plugin_data.PluginMenus.ROW_MYPEER, null);
        enqueue(startMenuItem);
        
        JMenuItem joinJMI = new JMenuItem(join);
        plugin_data.PluginRequest joinMenuItem = new plugin_data.PluginRequest();
        joinMenuItem.type = plugin_data.PluginRequest.REGISTER_MENU;
        joinMenuItem.plugin_GID = plugin_GID;
        joinMenuItem.column = plugin_data.PluginMenus.COLUMN_MYSELF;
        joinMenuItem.plugin_menuItem = joinJMI;
        enqueue(joinMenuItem);
        
        //Init DDGameMain
        DDGameMain.init();
    }
    
    //Used to transfer plugin data to DD.jar
    public static Hashtable<String,Object> getPluginDataHashtable() {
        Hashtable<String,Object> pd = new Hashtable<String,Object>();
        pd.put("plugin_GID", plugin_GID);
        pd.put("plugin_name", plugin_name);
        pd.put("plugin_info", "info");
        pd.put("plugin_url", "url");
        pd.put("editor",editor);
        pd.put("renderer",renderer);
        return pd;
    }

    //Needed - Peer sends message, running same plugin
    public static void handleReceivedMessage(byte[] msg, String peer_GID) {
        if(DEBUG)System.out.println("From: "+peer_GID+" got="+msg);
    	DDGameCommHandler.getInstance().addMessage(peer_GID, msg);
    }
	
    /**
     * Message with answer from local database
    */
    //Needed - Load data from database
    public static void answerMessage(String key, byte[] msg) {
    	if(DEBUG)System.out.println("From: db "+key+" got="+msg);
    	DDGameCommHandler.getInstance().receiveData(key, msg);
    }

    /** 
    tells you name and GID
    */
    //Needed - Called from DD.jar to set name and peer GID - dont need to change
    public static void setPluginData(String peer_GID, String name){
        if(DEBUG)System.out.println("Plugin:"+plugin_name+":setPluginData: "+peer_GID+" name="+name);
        Main.peer_GID = peer_GID;
        Main.name = name;
        DDGameCommHandler.getInstance().newPluginDataReadyEvent(new String(peer_GID), new String(name));
    }
	
    /**
    Called from DD to retrieve messages
    DO NOT CHANGE THIS!
    */
    public static Hashtable<String,Object> checkOutgoingMessageHashtable() 
    {
        if(DEBUG)System.out.println("Plugin:"+plugin_name+":checkOutgoingMessageHashtable: start");
        Hashtable<String,Object> pd;
        synchronized(queue)
        {
            while(queue.size()==0) {
            try {
                    queue.wait();
            } catch(Exception e){}
            }
            pd = queue.remove(0).getHashtable();
        }
	if(DEBUG)System.out.println("Plugin:"+plugin_name+":checkOutgoingMessageHashtable: got="+pd);
	return pd;
    }
    
    
    public static void enqueue(plugin_data.PluginRequest r) {
        synchronized(queue) {
            queue.add(r);
            queue.notify();
        }
    }
};

