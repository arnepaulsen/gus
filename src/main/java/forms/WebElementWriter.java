package forms;

/*
 * 
 * 
 * 	wraps the html element in java script
 * 
 *   .. this is temporary until we get rid of outerHTML
 * 
 * 
 * 
 */

public class WebElementWriter {

	public WebElementWriter() {
		super();
	}

	public String write(String element, String html) {
		return new String("\ndocument.all['" + element + "'].outerHTML = '"
				+ html + "';");

	}
}
