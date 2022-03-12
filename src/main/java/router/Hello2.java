package router;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import java.io.PrintWriter;
// import db.ODBCConn;
import db.SQLServerConn;

import java.sql.*;

/**
 * Servlet implementation class for Servlet: Hello2
 * 
 * @web.servlet name="Hello2" display-name="Hello2"
 * 
 * @web.servlet-mapping url-pattern="/Hello2"
 * 
 * test connection via odbc to my sql 
 * 
 * 
 */
public class Hello2 extends javax.servlet.http.HttpServlet implements
		javax.servlet.Servlet {
	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public Hello2() {
		super();
	}

	static final long serialVersionUID = 214334;

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest arg0,
	 *      HttpServletResponse arg1)
	 */
	protected void doGet(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		AnswerIt(arg0, arg1);
	}

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest arg0,
	 *      HttpServletResponse arg1)
	 */
	protected void doPost(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		AnswerIt(arg0, arg1);
	}

	private void AnswerIt(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		PrintWriter out;
		resp.setContentType("text/html");
		out = resp.getWriter();
		out.println("<html><body><br>Here is the result<br>");
		out.println(getResult());
		out.println("<br></body></html>");

	}

	private String getResult() {

		Connection conn;
		SQLServerConn jdbc;
		Statement stmt;
		ResultSet rs;
		String query = "Select project_name from tproject";

		StringBuffer sb = new StringBuffer();
		
		sb.append("<table>");
		
		try {
			
			/*
			 * odbc to mysql
			 */
			//odbc = new ODBCConn();
			//odbc.openConnection("jdbc:odbc:CMM", "root", "mysql");
			//conn = odbc.getConnection();
			
			
			/*
			 * jdbc to SQL Server
			 */
			jdbc  = new SQLServerConn();
			jdbc.openConnection("jdbc:microsoft:sqlserver://172.21.226.44:1433", "apaulsen", "password");
			conn = jdbc.getConnection();

			
			stmt = conn.createStatement();

			rs = stmt.executeQuery(query);

			while (rs.next() == true) {
				sb.append("<tr><td>" + rs.getString("project_name")+ "</td></tr>");
			}
			
		} catch (services.ServicesException e) {
			sb.append(e.toString());
		} catch (SQLException sql) {
			sb.append(sql.toString());
		}

		sb.append("</table>");
		return sb.toString();
	}
}