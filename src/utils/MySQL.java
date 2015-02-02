package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.jdbc.PreparedStatement;

public class MySQL {
	String url = "jdbc:mysql://localhost:3306/" + Config.db_name + "?user="
			+ Config.dbuser + "&password=" + Config.dbpass
			+ "&characterEncoding=utf-8&defaultFetchSize=100";

	public Connection conn = null;

	public MySQL() {
		if (conn == null)
			this.connection();
	}
	


	/**
	 * 连接数据库
	 *
	 * @return connection实例
	 */
	public Connection connection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url);
			if (conn.isClosed()) {
				System.out.println("连接失败");
			} else {
				System.out.println("连接成功");
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}

	public static void main(String[] arg) throws SQLException {
		new MySQL();
	}

	public ResultSet selectAll() {
		// int id = 15350;

		PreparedStatement state = null;
		ResultSet rs = null;
		String sql = "SELECT url,html_text , parse_time FROM html"; // where id < "
																	// + id;
		try {
			state = (PreparedStatement) conn.prepareStatement(sql , ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			state.enableStreamingResults();
			rs = state.executeQuery();
			// while (rs.next()) {
			// System.out.println(rs.getInt("id"));
			// System.out.println(rs.getString("title"));
			// }
		} catch (SQLException e) {
			System.out.println(e.toString());
		}

		return rs;
	}

	/*
	 * INSERT INTO api (api_name,Param,React_time,Date,Record) VALUES
	 * ('libsearchhandler','q','100',NULL,'GLE');
	 */
	/**
	 * 插入操作
	 *
	 * @param table
	 * @param fields
	 * @param values
	 * @return 0 if failed to insert
	 */
	public int insert(String table, String[] fields, String[] values,
			long reacttime) {
		String sql = "INSERT INTO " + table + "(";
		int i = 0;
		for (i = 0; i < fields.length; i++) {
			sql += fields[i] + ",";
		}
		sql = sql.substring(0, sql.length() - 1);
		sql += ") VALUES (";
		for (i = 0; i < values.length; i++) {
			sql += "'" + values[i] + "',";
		}
		sql += reacttime + ");";
		java.sql.PreparedStatement ps;
		try {
			ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			// conn.pre
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			rs.next();
			return rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
}