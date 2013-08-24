/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ddgame.network.data;

/**
 *
 * @author rknowles
 */
public enum PlatformEventType { 
    START_GAME,
    JOIN_REQUEST, 
    PLUGIN_DATA_RDY;

    PlatformEventType () {
    }

    public static PlatformEventType getType(String type) {
            for ( PlatformEventType eventType : PlatformEventType.values() ) {
                    if ( type.toLowerCase().equals(eventType.toString().toLowerCase()) ) { return eventType; }
            }
            return null;
    }
}	//End of enum EventType
