/* ------------------------------------------------------------------------- */
/*   Copyright (C) 2011 Marius C. Silaghi
                Author: Marius Silaghi: msilaghi@fit.edu
                Florida Tech, Human Decision Support Systems Laboratory
   
       This program is free software; you can redistribute it and/or modify
       it under the terms of the GNU Affero General Public License as published by
       the Free Software Foundation; either the current version of the License, or
       (at your option) any later version.
   
      This program is distributed in the hope that it will be useful,
      but WITHOUT ANY WARRANTY; without even the implied warranty of
      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
      GNU General Public License for more details.
  
      You should have received a copy of the GNU Affero General Public License
      along with this program; if not, write to the Free Software
      Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.              */
/* ------------------------------------------------------------------------- */
package util;
import java.util.Calendar;
import ASN1.Encoder;
public class Util {
    public static final int MAX_DUMP = 20;
    public static final int MAX_UPDATE_DUMP = 400;
    static String HEX[]={"0","1","2","3","4","5","6","7","8","9",
			 "A","B","C","D","E","F"};
    public static String byteToHex(byte[] b, int off, int len, String separator){
        if(b==null) return "NULL";
        String result="";
        for(int i=off; i<off+len; i++)
	    result = result+separator+HEX[(b[i]>>4)&0x0f]+HEX[b[i]&0x0f];
        return result;
    }
    public static String byteToHex(byte[] b, String sep){
        if(b==null) return "NULL";
        String result="";
        for(int i=0; i<b.length; i++)
	    result = result+sep+HEX[(b[i]>>4)&0x0f]+HEX[b[i]&0x0f];
        return result;
    }
    public static String byteToHex(byte[] b){
        return Util.byteToHex(b,"");
    }
    public static String getGeneralizedTime(){
        return Encoder.getGeneralizedTime(Calendar.getInstance());
    }
    public static Calendar getCalendar(String gdate) {
	Calendar date = Calendar.getInstance();
	if((gdate==null) || (gdate.length()<14)) {
	    return null;
	}
	date.set(Integer.parseInt(gdate.substring(0, 4)),
		 Integer.parseInt(gdate.substring(4, 6)),
		 Integer.parseInt(gdate.substring(6, 8)),
		 Integer.parseInt(gdate.substring(8, 10)),
		 Integer.parseInt(gdate.substring(10, 12)),
		 Integer.parseInt(gdate.substring(12, 14)));
	return date;
    }
}
