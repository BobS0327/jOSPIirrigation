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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Main {
	static String databaseFile = null;

	public static void main(String[] args) {
		String programName = null;

		if (args[0] != null) {
			programName = args[0];
		} else {
			System.out.println("No program name entered on command line, program terminating");
			System.exit(1);
		}
		if (args[1] != null) {
			databaseFile = args[1];
		}
		Initialize(databaseFile);
		operateStations(databaseFile, programName);
	}

	public static void Initialize(String dbPath) {
		Path p = Paths.get(dbPath);
		String directory = p.getParent().toString();
		File dir = new File(String.valueOf(directory));
		// mkdirs creates the entire directory path including parents
		if (!dir.exists()) {
			dir.mkdirs();
		}
		if (Misc.isWindows()) {
			System.out.println("Windows OS detected");
			if (dbPath == null) {
				databaseFile = "C:/temp/ospidata.db";
			} else {
				databaseFile = dbPath;
			}
		} else // Assume Linux
		{
			System.out.println("Linux OS detected");
			// databaseFile = "/usr/local/ospidata.db";
			if (dbPath == null) {
				databaseFile = "/usr/local/ospidata.db";
			} else {
				databaseFile = dbPath;
			}
		}
		File f = new File(databaseFile);
		if (!f.exists()) {
			System.out.println(databaseFile + " does not exist, creating database");
			Database.createDatabase(databaseFile);
		} else
			System.out.println(databaseFile + " exists");

	}

	public static void operateStations(String dFile, String programName) {
		Connection c = null;
		java.sql.Statement stmt = null;
		String zoneseq = null;
		String dbFile = "jdbc:sqlite:" + dFile;
		int md5hash = -1;
		String password = null;
		String hashedPassword = null;
		String url = null;
		String port = null;
		String urlCommand = null;
		String name = null;
		PreparedStatement preparedStatement = null;
		boolean bGotProgamName = false;
		String resultText = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection(dbFile);
			// c.setAutoCommit(false);
			c.setAutoCommit(true);
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			ResultSet rs = ((java.sql.Statement) stmt).executeQuery("SELECT * FROM INFO;");
			while (rs.next()) {
				md5hash = rs.getInt("md5hash");
				password = rs.getString("password");
				url = rs.getString("url");
				port = rs.getString("port");
			}
			rs.close();
			stmt.close();
			stmt = c.createStatement();
			rs = ((java.sql.Statement) stmt).executeQuery("SELECT * FROM PROGRAMS;");
			while (rs.next()) {

				name = rs.getString("name");
				zoneseq = rs.getString("zoneseq");

				boolean equals1 = programName.equalsIgnoreCase(name);
				if (equals1 == true) {
					bGotProgamName = true;
					break;
				}
			}
			rs.close();

			if (bGotProgamName == false) {
				System.out.println(programName + " not found in database, program aborting");
				try {
					String tempString = programName + " not found in database, program aborting";
					String insertSQL = "INSERT INTO log (timestamp, info) VALUES (?, ?)";
					preparedStatement = c.prepareStatement(insertSQL);
					preparedStatement.setString(1, Misc.getDateTime());
					preparedStatement.setString(2, tempString);
					preparedStatement.executeUpdate();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					preparedStatement.close();
				}
				System.exit(1);
			}
			String sqlDelete = "DELETE FROM log";
			// Execute deletion
			stmt.executeUpdate(sqlDelete);

			try {
				String tempString = "Lawn irrigation started using " + programName;
				String insertSQL = "INSERT INTO log (timestamp, info) VALUES (?, ?)";
				preparedStatement = c.prepareStatement(insertSQL);
				preparedStatement.setString(1, Misc.getDateTime());
				preparedStatement.setString(2, tempString);
				preparedStatement.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				preparedStatement.close();
			}
			if (md5hash == 0) {
				hashedPassword = MD5.generateMD5hash(password);
			} else
				hashedPassword = password;
			ArrayList pzS = Misc.parseZoneString(zoneseq);
			for (int i = 0; i < pzS.size(); i++) {
				String[] OSArray = pzS.get(i).toString().split("\\s*:\\s*");
				System.out.println(OSArray[0] + "    " + OSArray[1]);
				urlCommand = url + ":" + port + "/cm?pw=" + hashedPassword + "&sid=" + OSArray[0] + "&en=1&t="
						+ OSArray[1];
				System.out.println(urlCommand);
				String ret = Misc.sendCommand(urlCommand);

				Object obj = JSONValue.parse(ret);
				JSONObject jsonObject = (JSONObject) obj;
				Long res = 0L;

				try {
					res = (Long) jsonObject.get("result");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (res == null) {
					System.out.println("***Successful");
					resultText = "Successful";

				} else {
					switch (res.intValue()) {
					case 1:
						System.out.println("Successful");
						resultText = "Successful";
						break;
					case 2:
						System.out.println("Unauthorized");
						resultText = "Unauthorized";
						break;
					case 3:
						System.out.println("Mismatch");
						resultText = "Mismatch";
						break;
					case 16:
						System.out.println("Data missing");
						resultText = "Data missing";
						break;
					case 17:
						System.out.println("Out of range");
						resultText = "Out of range";
						break;
					case 18:
						System.out.println("Data format error");
						resultText = "Data format error";
						break;
					case 19:
						System.out.println("RF code error");
						resultText = "RF code error";
						break;
					case 32:
						System.out.println("Page not found");
						resultText = "Page not found";
						break;
					case 48:
						System.out.println("Not permitted");
						resultText = "Not permitted";
						break;
					default:
						// throw new IllegalArgumentException(
						System.out.println("Invalid result code: " + res.intValue());
						resultText = "Invalid result code: " + res.intValue();
					}
				}
				if (res == null || res == 1) {

					try {
						String tempString = "Station id # " + OSArray[0] + " being watered for " + OSArray[1]
								+ " seconds";
						String insertSQL = "INSERT INTO log (timestamp, info) VALUES (?, ?)";
						preparedStatement = c.prepareStatement(insertSQL);
						preparedStatement.setString(1, Misc.getDateTime());
						preparedStatement.setString(2, tempString);
						preparedStatement.executeUpdate();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						preparedStatement.close();
					}
					System.out.println(urlCommand);
					Thread.sleep(Integer.parseInt(OSArray[1]) * 1000);
					boolean bIdnumFound = false;
					String sel = "SELECT * FROM zones WHERE idnum = " + OSArray[0] + ";";
					rs = stmt.executeQuery(sel);
					while (rs.next()) {
						bIdnumFound = true;
					}

					if (bIdnumFound == true) {
						String updateSQL = "update zones set  idnum=?, lastwatered=? where idnum=?";
						preparedStatement = c.prepareStatement(updateSQL);
						preparedStatement.setString(1, OSArray[0]);
						preparedStatement.setString(2, Misc.getDateTime());
						preparedStatement.setString(3, OSArray[0]);
						preparedStatement.executeUpdate();
					} else {
						String tempString = "**** Station id # " + OSArray[0] + " *NOT* found in zones table ";
						String insertSQL = "INSERT INTO log (timestamp, info) VALUES (?, ?)";
						preparedStatement = c.prepareStatement(insertSQL);
						preparedStatement.setString(1, Misc.getDateTime());
						preparedStatement.setString(2, tempString);
						preparedStatement.executeUpdate();
					}
				} else {
					String tempString = "**** Station id # " + OSArray[0] + " " + resultText;
					String insertSQL = "INSERT INTO log (timestamp, info) VALUES (?, ?)";
					preparedStatement = c.prepareStatement(insertSQL);
					preparedStatement.setString(1, Misc.getDateTime());
					preparedStatement.setString(2, tempString);
					preparedStatement.executeUpdate();
				}
			}
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		try {
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Operation done successfully");
	}
}
