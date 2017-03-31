/*
Copyright (C) 2016  R.W. Sutnavage

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/.
*/
package jOSPIirrigation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Misc {

	private static String OS = null;

	public static String getOsName() {
		if (OS == null) {
			OS = System.getProperty("os.name");
		}
		return OS;
	}

	public static boolean isWindows() {
		return getOsName().startsWith("Windows");
	}

	public static String getDateTime() {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date dateobj = new Date();
		// System.out.println(df.format(dateobj));
		String dateNow = df.format(dateobj).toString();
		return dateNow;
	}

	public static ArrayList parseZoneString(String inputString) {
		String[] items = inputString.split(",");
		ArrayList<String> itemList = new ArrayList<String>();

		for (String item : items) {
			itemList.add(item);
		}
		for (int i = 0; i < itemList.size(); i++) {
			String[] OSArray = itemList.get(i).toString().split("\\s*:\\s*");
		}
		return itemList;
	}

	public static String sendCommand(String urlCommand) {
		// String url1 = "http://192.168.1.248:8080/";
		// url1 += "jc?pw=e0ff85143dfa717536cbb668cc8f8e8b";
		StringBuilder returnData = new StringBuilder(1000);
		URL url = null;
		try {
			url = new URL(urlCommand);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
		connection.setDoOutput(true);
		connection.setInstanceFollowRedirects(false);
		try {
			connection.setRequestMethod("GET");
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		connection.setRequestProperty("Content-Type", "text/plain");
		connection.setRequestProperty("charset", "utf-8");
		try {
			connection.connect();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Reader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int c = 0;
		try {
			for (c = 0; (c = in.read()) >= 0;)
				// System.out.print((char)c);
				returnData.append(String.valueOf((char) c));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnData.toString();
	}

	public static String buildCommand(String url, String keyword, String parms1, String parms2, String password,
			boolean bCreateMD5Hash) {
		String MD5password = null;
		String retCommand = null;

		if (bCreateMD5Hash == true) {
			MD5password = MD5.generateMD5hash(password);
		}
		switch (keyword) {
		case "jc":
			retCommand = url;
			retCommand += keyword;
			retCommand += parms1;
			retCommand += MD5password;
			break;
		case "ja":

			break;
		case "cm": // Manual station run
			retCommand = url;
			retCommand += keyword;
			retCommand += parms1;
			retCommand += MD5password;
			retCommand += parms2;

			break;
		default:
			throw new IllegalArgumentException("Invalid keyword: " + keyword);

		}
		return retCommand;
	}

	public static void parseJSON(String keyword, String JSONstring) {
		switch (keyword) {
		case "jc":
			Object obj = JSONValue.parse(JSONstring);
			JSONObject jsonObject = (JSONObject) obj;
			java.lang.Long devt = 0l;
			Long sunset = 0l;
			Long sunrise = 0l;
			Long eip = 0l;
			String ipAddress = null;
			Object sbits = null;

			try {
				devt = (java.lang.Long) jsonObject.get("devt");
			} catch (Exception e) {
				e.printStackTrace();
			}

			devt += 14400; // Adjust for local time zone
			Date date = new Date();
			date.setTime((long) devt * 1000);
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
			String str = df.format(date);
			System.out.println(str + "  devt = " + devt);

			try {
				sunset = (Long) jsonObject.get("sunset");
				sunrise = (Long) jsonObject.get("sunrise");
				eip = (Long) jsonObject.get("eip");
				sbits = jsonObject.get("sbits");
			} catch (Exception e) {
				e.printStackTrace();
			}

			ipAddress = fromNumerical(eip);
			System.out.printf("IP Address %s\n", ipAddress);
			System.out.printf("sbits = %s\n", sbits.toString());
			long hours = sunset / 60; // since both are ints, you get an int
			long minutes = sunset % 60;
			System.out.printf("Sunset %d:%02d\n", hours, minutes);
			hours = sunrise / 60; // since both are ints, you get an int
			minutes = sunrise % 60;
			System.out.printf("Sunrise %d:%02d\n", hours, minutes);

			break;
		case "ja":

			break;
		case "cm": // Manual station run

			break;
		default:
			throw new IllegalArgumentException("Invalid keyword: " + keyword);
		}
	}

	// Convert IP long to IP dotted decimal value
	public static String fromNumerical(long address) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0, shift = 24; i < 4; i++, shift -= 8) {
			long value = (address >> shift) & 0xff;
			sb.append(value);
			if (i != 3) {
				sb.append('.');
			}
		}
		return sb.toString();
	}
}
