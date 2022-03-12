package router;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import forms.WebFieldRadio;

/**
 * Servlet implementation class for Servlet: Hello
 * 
 * @web.servlet name="Hello" display-name="Hello" description="HelloWorldDesc"
 * 
 * @web.servlet-mapping url-pattern="/Hello"
 * 
 */
public class Hello extends javax.servlet.http.HttpServlet implements
		javax.servlet.Servlet {
	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */

	static final long serialVersionUID = 7522527652724547L;
	
	public Hello() {
		super();
	}

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
		
		
		String[][] q02 = { { "Y", "N", "M" },
				{ "Approved : Yes", "No", "Maybe" } };
		
		WebFieldRadio r = new WebFieldRadio ("approved","M", q02 );

		
		out.println("Hello Arne");
		
		out.println ("<br> Radio Value = " + r.getHTML("show"));
		
		out.println ("<br> ? " + r.getHTML("edit"));
		
		
		out.println("<br> Done");
		
	

		
		
		
		
		
	}

}