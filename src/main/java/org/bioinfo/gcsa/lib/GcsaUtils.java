package org.bioinfo.gcsa.lib;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class GcsaUtils {

	private final static String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	public static String getTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		return sdf.format(new Date());
	}

	public static String getTimeMillis() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		return sdf.format(new Date());
	}

	public static Date add24HtoDate(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.setTimeInMillis(date.getTime());// sumamos 24h a la fecha del login
		cal.add(Calendar.DATE, 1);
		return new Date(cal.getTimeInMillis());
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

	public static String randomString() {
		return randomString(10);
	}

	public static String randomString(int length) {
		StringBuilder string = new StringBuilder();
		Random r = new Random(System.currentTimeMillis());
		for (int i = 0; i < length; i++) {
			string.append(characters.charAt(r.nextInt(characters.length())));
		}
		return string.toString();
	}

	public static String sha1(String text) throws NoSuchAlgorithmException {
		MessageDigest sha1 = MessageDigest.getInstance("SHA1");
		byte[] digest = sha1.digest((text).getBytes());
		return bytes2String(digest);
	}

	public static String bytes2String(byte[] bytes) {
		StringBuilder string = new StringBuilder();
		for (byte b : bytes) {
			String hexString = Integer.toHexString(0x00FF & b);
			string.append(hexString.length() == 1 ? "0" + hexString : hexString);
		}
		return string.toString();
	}

	// public static String getSessionId() {
	// return StringUtils.randomString(20);
	// // int longitud = 20;
	// // String cadenaAleatoria = "";
	// // long milis = new java.util.GregorianCalendar().getTimeInMillis();
	// // Random r = new Random(milis);
	// // int i = 0;
	// // while (i < longitud) {
	// // char c = (char) r.nextInt(255);
	// // if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z')
	// // || (c >= 'a' && c <= 'z')) {
	// // cadenaAleatoria += c;
	// // i++;
	// // }
	// // }
	// // return cadenaAleatoria;
	// }

	public static void sendResetPasswordMail(String to, String message) {
		sendMail("correo.cipf.es", to, "babelomics@cipf.es", "Genomic cloud storage analysis password reset",
				message.toString());
	}

	public static void sendMail(String smtpServer, String to, String from, String subject, String body) {
		try {
			Properties props = System.getProperties();
			// -- Attaching to default Session, or we could start a new one --
			props.put("mail.smtp.host", smtpServer);
			javax.mail.Session session = javax.mail.Session.getDefaultInstance(props, null);
			// -- Create a new message --
			// Message msg = new javax.mail.Message(session);
			Message msg = new MimeMessage(session);
			// -- Set the FROM and TO fields --
			msg.setFrom(new InternetAddress(from));
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
			// -- We could include CC recipients too --
			// if (cc != null)
			// msg.setRecipients(Message.RecipientType.CC
			// ,InternetAddress.parse(cc, false));
			// -- Set the subject and body text --
			msg.setSubject(subject);
			msg.setText(body);
			// -- Set some other header information --
			msg.setHeader("X-Mailer", "LOTONtechEmail");
			msg.setSentDate(new Date());
			// -- Send the message --
			Transport.send(msg);
			System.out.println("Message sent OK.");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
