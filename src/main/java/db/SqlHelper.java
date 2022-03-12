package db;

/**
 * just used by the DbFieldString class for update/add queries
 * 
 * 
 * Change log:
 * 11/13/06 - no change, reverse concat function
 * 
 * 11/30/08 = replace html right-single-quote wtih ''
 * 
 * 01/19/09 - just replace ' with '', too easy.
 * 
 */
public class SqlHelper {


	// *************************
	// PUBLIC METHODS
	// *************************


	public String doubleQ(String s) {
		
		return s.replaceAll("'", "''");
			
		
	}
		
}
