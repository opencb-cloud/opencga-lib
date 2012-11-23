package org.bioinfo.gcsa.lib;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class GcsaUtils {
	public static String getTime() {
		String timeStamp;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Calendar calendar = Calendar.getInstance();
		Date now = calendar.getTime();
		timeStamp = sdf.format(now);
		return timeStamp;
	}

	public static Date toDate(String dateStr) {
		Date now = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			now = sdf.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return now;
	}

	public static String getSessionId() {
		int longitud = 20;
		String cadenaAleatoria = "";
		long milis = new java.util.GregorianCalendar().getTimeInMillis();
		Random r = new Random(milis);
		int i = 0;
		while (i < longitud) {
			char c = (char) r.nextInt(255);
			if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z')
					|| (c >= 'a' && c <= 'z')) {
				cadenaAleatoria += c;
				i++;
			}
		}
		return cadenaAleatoria;
	}
}