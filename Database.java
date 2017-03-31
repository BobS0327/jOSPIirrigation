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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.Date;

public class Database {

	public static void createDatabase(String dir) {

		// SQLite connection string
		String url = "jdbc:sqlite:" + dir;

		String sql1 = "CREATE TABLE IF NOT EXISTS zones (\n" + "	idnum integer PRIMARY KEY,\n"
				+ "	lastwatered String\n" + ");";

		String sql2 = "CREATE TABLE IF NOT EXISTS programs (\n" + " name text,\n" + "	zoneseq text PRIMARY KEY,\n"
				+ " lastexecuted text\n" + ");";

		String sql3 = "CREATE TABLE IF NOT EXISTS log (\n" + "	timestamp text ,\n" + "	info String \n" + ");";

		String sql4 = "CREATE TABLE IF NOT EXISTS info (\n" + "	password text PRIMARY KEY,\n" + "	md5hash integer,\n"
				+ " url text,\n" + " port text\n" + ");";

		try (Connection conn = DriverManager.getConnection(url); java.sql.Statement stmt = conn.createStatement()) {
			// create a new table
			stmt.execute(sql1);
			stmt.execute(sql2);
			stmt.execute(sql3);
			stmt.execute(sql4);
			String sql5 = "INSERT INTO zones(idnum,lastwatered) VALUES(?,?)";
			PreparedStatement pstmt = conn.prepareStatement(sql5);

			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			String formatDateTime = now.format(formatter);

			for (int x = 0; x < 32; x++) {
				pstmt.setInt(1, x + 1);
				Date date = java.sql.Date.valueOf(java.time.LocalDate.now());
				pstmt.setString(2, date.toString());
				pstmt.executeUpdate();
			}
			String sql6 = "INSERT INTO programs(name,zoneseq, lastexecuted) VALUES(?,?,?)";
			pstmt = conn.prepareStatement(sql6);
			pstmt.setString(1, "Program1");
			// Set up default values, can be changed using a Sqlite GUI
			// interface
			// such as DB browser for Sqlite
			pstmt.setString(2,
					"1:180,2:180,3:60,4:60,5:60,6:60,7:60,8:60,9:60,10:60,11:90,12:120,13:60,14:60,15:60,16:60,17:90,18:360,19:360,20:45,21:60,22:60,23:60,24:180,25:360,26:45,27:60,28:90,29:120,30:60,31:60,32:60");

			pstmt.setString(3, formatDateTime);
			pstmt.executeUpdate();

			String sql7 = "INSERT INTO log(timestamp,info) VALUES(?,?)";
			pstmt = conn.prepareStatement(sql7);

			pstmt.setString(1, formatDateTime);
			pstmt.setString(2, "Written by R.W. Sutnavage");
			pstmt.executeUpdate();

			String sql8 = "INSERT INTO info(password,md5hash,url,port) VALUES(?,?,?,?)";
			pstmt = conn.prepareStatement(sql8);
			pstmt.setString(1, "opendoor");
			pstmt.setInt(2, 0);
			pstmt.setString(3, "http://192.168.1.248");
			pstmt.setString(4, "8080");
			pstmt.executeUpdate();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void insertLogRecord(String dbName, String timeStamp, String inData) {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String conn = "jdbc:sqlite:" + dbName;
			c = DriverManager.getConnection(conn);
			c.setAutoCommit(false);
			stmt = c.createStatement();
			String values = "VALUES (" + "'" + timeStamp + "'" + "," + "'" + inData + "'" + ");";
			// String values = "VALUES (" + timeStamp + "," + information +
			// ");";
			String sql = "INSERT INTO loginfo (timestamp,info) " + values;
			stmt.executeUpdate(sql);
			stmt.close();
			c.commit();
			c.close();
		} catch (Exception ex) {
			System.err.println(ex.getClass().getName() + ": " + ex.getMessage());
		}
	}
}
