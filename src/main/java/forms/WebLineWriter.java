package forms;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



/*
 * 
 * 
 * 	All http output is redirected through here
 * 
 * 	... so I can see every line
 * 
 * 
 * 
 */

public class WebLineWriter   {

	PrintWriter out;
	HttpServletRequest req;

	HttpServletResponse resp;
	
	private StringBuffer sb = null;
	
	
	public WebLineWriter (PrintWriter out) {
		sb = new StringBuffer();
		this.out = out;
	}
		
	public void println(String s) {
		sb.append(s + "\n");
		//out.println(s);
	}
	
	public void print(String s) {
		//out.print(s);
		sb.append(s);
	}
	
	public void unLoad () {
		//System.out.println("WeblineWriter:unloading : " + sb.toString());
		
		out.println(sb.toString());
	}
	
	
	
}
