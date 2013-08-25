package ddgame.network;

import java.util.ArrayList;

import ddgame.network.protocol.datatypes.PeerGID;

public class Connection {
	
	private static ArrayList<Connection> conns = new ArrayList<>();
	
	public static void addConnection(Connection c){
		synchronized(conns)
		{
			conns.add(c);
		}
	}
	
	
	public Connection(PeerGID gid)
	{
		
	}
}
