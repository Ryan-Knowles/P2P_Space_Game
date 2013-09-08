package ddgame.network;

import java.util.ArrayList;

import ddgame.network.protocol.datatypes.PeerGID;

public class Connection {
	private static final long TIMEOUT = 90000;	//90 second timeout
	private static ArrayList<Connection> conns = new ArrayList<>();
	
	public static void addConnection(String gid){
		synchronized(conns) {
			if(getConnection(gid)==null){
				conns.add(new Connection(gid));
			}
		}
	}
	
	public static void validate(){
		ArrayList<Connection> oldConns = new ArrayList<>();
		synchronized (conns) {
			long curTime = System.currentTimeMillis();
			
			for(Connection c: conns){
				double delay = curTime - c.lastUpdate; 
				if(delay>TIMEOUT){
					oldConns.add(c);
				}
			}
			//Remove old connections
			for(Connection c: oldConns){
				conns.remove(c);
			}
		}
	}
	
	public static Connection getConnection(String gid){
		synchronized(conns){
			for(Connection c: conns){
				if(c.pGID.equals(gid)){
					return c;
				}
			}
		}
		
		return null;
	}
	
	public static void clear(){
		conns.clear();
	}
	
	public static ArrayList<String> getGIDs(){
		ArrayList<String> gids = new ArrayList<>();
		
		synchronized(conns){
			for(Connection c: conns){
				gids.add(c.pGID);
			}
		}
		
		return gids;
	}
	
	private long syncNum;
	private long lastUpdate;
	private String pGID;
	private static final long MAX_DIFF = 100000;	
	
	public Connection(String gid)
	{
		this.pGID = gid;
		this.syncNum = 0;
		this.lastUpdate = System.currentTimeMillis();
	}
	
	public void update(long num){
		this.syncNum = num;
		this.lastUpdate = System.currentTimeMillis();
	}
	
	public boolean isValid(long num){
		double syncDiff = Math.abs(syncNum-num);
		if(num>syncNum){
			return true;
		}
		//Check for number wrap
		else if(syncDiff>MAX_DIFF){
			return true;
		}
		else {
			return false;
		}
	}
}
