package forms;

/**
 * @author Arne Paulsen
 * 
 * Used to populate a form 'hidden' input variable 
 * 
**/
public class WebFieldHidden extends WebField {

	public WebFieldHidden(String parmFieldId, String parmWebText) {

		super(parmFieldId, parmWebText);

	}

	public String getHTML(String parmMode) {

		return new String(
			"<INPUT TYPE=HIDDEN NAME="
				+ webFieldId
				+ "  value=\""
				+ (String) htmlHelper.getHTML((String) value)
				+ "\">");
	}
	
	public String getJS(String parmMode) {

		return new String(
			"\ndocument.all['"
				+ webFieldId
				+ "'].outerHTML = '<INPUT TYPE=HIDDEN NAME="
				+ webFieldId
				+ "  value=\""
				+ (String) htmlHelper.getHTML((String) value)
				+ "\">';");
	}
}
