package forms;

import db.DbInterface;
import router.SessionMgr;

/*
 * 11/8/09 Fix - constructor using single parameter, so webfield and db_field are the same
 *  
 *     error was passing 2, then web and db names were different, really messed up
 *     
 *     TAG: GUS2.0 displayLiteral -  NOT USED
 */
public class BeanFieldCheckbox extends BeanWebField {

	//private String displayLiteral;

	public BeanFieldCheckbox(String fieldName, String defaultValue,
			String displayLiteral) {

		super(fieldName);
		this.defaultStringValue = defaultValue;
		//this.displayLiteral = displayLiteral;
	}

	public WebField getWebField(SessionMgr sm, DbInterface db, String mode) {

		//System.out.println("getWebString " + this.getWebFieldName());

		//System.out.println("checkbox : default value : " + this.defaultStringValue);
		
		//System.out.println(" web field: " + this.webFieldName + "  database field : " + databaseFieldName + " value : " + db.getText(databaseFieldName));
		
		boolean addMode = mode.equalsIgnoreCase("add") ? true : false;

		return new WebFieldCheckbox(this.webFieldName,
				(addMode ? "N" : db.getText(databaseFieldName)),
				"");

	}
}
