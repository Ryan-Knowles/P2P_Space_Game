package ddgame.network;

import ddgame.network.data.DataContainer;
import ddgame.network.data.PlatformEvent;
import java.util.ArrayList;
import java.util.Iterator;

public class DDGameCommHandler {
    private static final boolean DEBUG = false;

    //Implements Singleton pattern
    private static DDGameCommHandler instance = null;
    public static DDGameCommHandler getInstance() {
            if (instance==null) { instance = new DDGameCommHandler(); }
            return instance;
    }

    //private volatile 
    private volatile ArrayList<DataContainer> data_queue = new ArrayList<>();
    private volatile ArrayList<PlatformEvent> eventQueue = new ArrayList<>();
    private volatile int data_in_queue = 0;
    private volatile int msgs_in_queue = 0;
    private DDGameCommHandler () {}

    public synchronized int getDataCount() { return data_in_queue; }
    public synchronized int getMsgCount() { return msgs_in_queue; }
    public synchronized int getDataQueueSize() { return data_queue.size(); }
    public synchronized int getEventQueueSize() { return this.eventQueue.size(); }
 
    ////////////////////////////////////////////////////
    //Methods that handle receiving new PlatformEvents//
    ////////////////////////////////////////////////////

    public synchronized ArrayList<PlatformEvent> getPlatformEvents(){
        ArrayList<PlatformEvent> events = eventQueue;
        eventQueue = new ArrayList<>();
        return events;
    }

    public synchronized void newPluginDataReadyEvent(String peerGID, String peerName){
        PlatformEvent readyEvent = new PlatformEvent("PLUGIN_DATA_RDY");
        readyEvent.addPluginData(peerGID, peerName);
        eventQueue.add(readyEvent);
    }

    public synchronized void newStartGameEvent(){
        PlatformEvent startEvent = new PlatformEvent("START_GAME");
        eventQueue.add(startEvent);
    }

    public synchronized void newJoinRequestEvent(String peerGID){
        PlatformEvent joinEvent = new PlatformEvent("JOIN_REQUEST");
        joinEvent.addJoinRequest(peerGID);
        eventQueue.add(joinEvent);
    }

    /////////////////////////////////////////////////////////
    //Methods that handle sending/receiving data from peers//
    /////////////////////////////////////////////////////////

    //Adds dataContainer of type MESSAGE to the data_queue
    public synchronized void addMessage(String peer_GID, byte[] msg) {
        if(DEBUG)System.out.println("HANDLER.addMessage:( Sender: "+peer_GID.substring(0, 20)+")");
        data_queue.add(new DataContainer(peer_GID, msg, "message"));
        msgs_in_queue++;
    }

    //Removes and returns all dataContainers of type MESSAGE from the data_queue
    public synchronized ArrayList<DataContainer> getMessages() { 
        ArrayList<DataContainer> temp = new ArrayList<>();
        String s = "message";		
        Iterator<DataContainer> dqIter = data_queue.iterator();

        //Iterate through ArrayList data_queue. For each element, add any element with type MESSAGE 
        //to the ArrayList temp and remove the element from ArrayList data_queue.
        while( dqIter.hasNext() ) {
                DataContainer dc = dqIter.next();
                if(s.toLowerCase().equals(dc.getTypeString().toLowerCase()) ) { 
                        temp.add(dc);
                        dqIter.remove(); 
                }
        }
        msgs_in_queue = msgs_in_queue - temp.size();
        assert msgs_in_queue == 0 : "Error: msgs_in_queue not equal to 0. "+msgs_in_queue+" items remaining." ;
        return temp;
    }

    public void sendMessage(String peer_GID, byte[] data) {
        if(DEBUG)System.out.println("HANDLER.sendMessage:( Receiver: "+peer_GID.substring(0, 20)+")");
        plugin_data.PluginRequest temp = new plugin_data.PluginRequest();
        temp.type = plugin_data.PluginRequest.MSG;
        temp.plugin_GID = dd_p2p.plugin.Main.plugin_GID;
        temp.peer_GID = peer_GID;
        temp.msg = data;
        dd_p2p.plugin.Main.enqueue(temp);
    }

    /////////////////////////////////////////////
    //Methods that implement store/load of data//
    /////////////////////////////////////////////

    public synchronized void receiveData(String key, byte[] data) {
            data_queue.add( new DataContainer(key, data, "data") );
            data_in_queue++;
    }

    public synchronized ArrayList<DataContainer> getData() { 
            ArrayList<DataContainer> temp = new ArrayList<DataContainer>();
            String s = "data";		
            Iterator<DataContainer> dqIter = data_queue.iterator();

            //Iterate through ArrayList data_queue. For each element, add any element with type DATA 
            //to the ArrayList temp and remove the element from ArrayList data_queue.
            while( dqIter.hasNext() ) {
                    DataContainer dc = dqIter.next();
                    if(s.toLowerCase().equals(dc.getTypeString().toLowerCase()) ) { 
                            temp.add(dc);
                            dqIter.remove(); 
                    }
            }
            data_in_queue = data_in_queue - temp.size();
            assert data_in_queue == 0 : "Error: data_in_queue not equal to 0. "+data_in_queue+" items remaining." ;
            return temp;
    }

    public void storeData(String key, byte[] data) {
            plugin_data.PluginRequest temp = new plugin_data.PluginRequest();
            temp.type = plugin_data.PluginRequest.STORE;
            temp.plugin_GID = dd_p2p.plugin.Main.plugin_GID;
            temp.key = key;
            temp.msg = data;
            dd_p2p.plugin.Main.enqueue(temp);
    }

    public void requestData(String key) {
            plugin_data.PluginRequest temp = new plugin_data.PluginRequest();
            temp.type = plugin_data.PluginRequest.RETRIEVE;
            temp.plugin_GID = dd_p2p.plugin.Main.plugin_GID;
            temp.key = key;
            dd_p2p.plugin.Main.enqueue(temp);
    }
}