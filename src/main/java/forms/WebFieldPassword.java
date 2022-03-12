package forms;

/**
 * @author Arne Paulsen
 * 
 */
public class WebFieldPassword extends WebField {

	public WebFieldPassword(String parmFieldId, String parmWebText) {

		super(parmFieldId, parmWebText);

	}

	public WebFieldPassword(String parmFieldId) {

		super(parmFieldId);

	}

	public String getHTML(String parmMode) {
		if (parmMode.equalsIgnoreCase("show")) {
			return new String("<p>********</p>");
		}

		if (parmMode.equalsIgnoreCase("edit")
				|| parmMode.equalsIgnoreCase("add")) {
			return new String("<input type=password name=" + webFieldId
					+ " id=" + webFieldId +  " value=" + (String) htmlHelper.getHTML((String) value) + ">");

		}

		return new String("invalid mode");

	}

	

}
