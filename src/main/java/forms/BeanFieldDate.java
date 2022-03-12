package forms;

import db.DbInterface;
import router.SessionMgr;

public class BeanFieldDate extends BeanWebField {

	public BeanFieldDate(String fieldName) {

		super(fieldName);

	}

	public WebField getWebField(SessionMgr sm, DbInterface db, String mode) {

		boolean addMode = mode.equalsIgnoreCase("add") ? true : false;

		return new WebFieldDate(this.webFieldName,
				(addMode ? defaultStringValue : db.getText(databaseFieldName)));

	}
}
