package ddgame.network.data;

import java.util.Date;

enum dataType { 
	DATA, 
	MESSAGE;
	
	dataType () {
	}
	
	public static dataType getType(String type) {
		for ( dataType dt : dataType.values() ) {
			if ( type.toLowerCase().equals( dt.toString().toLowerCase() ) ) { return dt; }
		}
		return null;
	}
}	//End of enum dataType

public class DataContainer {
	private String peerGID;
	private byte[] data;
	private Date time_received;
	private dataType type;
	
	public DataContainer(String peerGID, byte[] data, String type_string) {
		this.time_received = new Date(System.currentTimeMillis());
		this.peerGID = peerGID;
		this.data = data;
		this.type = dataType.getType(type_string);
	}
	
	public String 	getPeerGID()		{ return peerGID; }
	public byte[] 	getData()		{ return data; }
	public String 	getTypeString()		{ return type.toString(); }
	public Date 	getTimeReceived()	{ return time_received; }
}
