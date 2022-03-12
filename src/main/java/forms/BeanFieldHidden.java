package forms;

/*
 * Change log:
 * 
 * 8/2/10 - Add constructor for two strings: webField, databaseField
 * 
 */

import db.DbInterface;
import router.SessionMgr;

public class BeanFieldHidden extends BeanWebField {

	public BeanFieldHidden(String fieldName) {

		super(fieldName);

	}
	
	public BeanFieldHidden (String fieldName, String databaseName) {
		super(fieldName, databaseName);
	}
	

	public WebField getWebField(SessionMgr sm, DbInterface db, String mode) {

		boolean addMode = mode.equalsIgnoreCase("add") ? true : false;

		return new WebFieldHidden(this.webFieldName, (addMode ? "" : db
				.getText(databaseFieldName)));

	}
}
