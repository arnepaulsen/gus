package plugins;

import java.util.Hashtable;
import forms.*;
import db.*;

import java.util.*;

/**
 * Action Plugin
 * 
 * Change log:
 * 
 * 2/4/08 Copied from 'Issue'
 * 
 * 
 */

public class ActionPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * Constructors
	 * 
	 * @throws services.ServicesException
	 * 
	 * 
	 **************************************************************************/

	public ActionPlugin() throws services.ServicesException {
		super();

		this.setTableName("taction");
		this.setKeyName("action_id");
		this.setTargetTitle("Action Item");
		
		


		this.setListHeaders(new String[] { "Reference", "Title", "Status",
				"Priority", "Owner" });

		// columns after last header are not shown! used to match list filters
		// only

		this.setMoreListColumns(new String[] { "reference_nm", "title_nm",
				"s.code_desc as status_desc", "p.code_desc as priority_desc",
				"concat(u.last_name, ',', u.first_name)",
				"s.code_value", "p.code_value", "taction.assigned_uid" });

		this
				.setMoreListJoins(new String[] {
						" left join tcodes s on taction.status_cd = s.code_value and s.code_type_id  = 45 ",
						" left join tcodes p on taction.priority_cd = p.code_value and p.code_type_id  = 7 ",
						" left join tuser u on taction.assigned_uid = u.user_id " });

		this.setMoreSelectJoins (new String[] { "left join tuser as c on taction.closed_by_uid = c.user_id " });
		this.setMoreSelectColumns (new String[] { "c.last_name" });

	}

	/***************************************************************************
	 * 
	 * 8/22 : List Overrides
	 * 
	 **************************************************************************/

	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#2) has a selector, other fields do not
		if (columnNumber > 1)
			// true causes getListSelector to be called for this column.
			return true;
		else
			return false;
	}

	/*
	 * 8/22 : only the status column will be called, so no need to check the
	 * column #
	 */
	public WebField getListSelector(int columnNumber) {

		if (columnNumber == 2) {
			// default the status to open when starting new list
			WebFieldSelect wf = new WebFieldSelect("FilterStatus", (sm.Parm(
					"FilterStatus").length() == 0 ? "O" : sm
					.Parm("FilterStatus")), sm.getCodes("ISSUESTAT"),
					"All Status");
			wf.setDisplayClass("listform");
			return wf;
		} else {
			if (columnNumber == 3) {

				WebFieldSelect wf = new WebFieldSelect("FilterPriority", sm
						.Parm("FilterPriority"), sm.getCodes("PRIORITY"),
						"All Priorities");
				wf.setDisplayClass("listform");
				return wf;
			} else {

				WebFieldSelect wf = new WebFieldSelect("FilterUser", (sm.Parm(
						"FilterUser").length() == 0 ? Integer.valueOf(0)
						: Integer.parseInt(sm.Parm("FilterUser"))), sm.getUserHT(),
						"All Users");
				wf.setDisplayClass("listform");
				return wf;

			}
		}

	}

	public String getListAnd() {
		/*
		 * todo: cheating... need to map the code value to the description
		 */

		StringBuffer sb = new StringBuffer();

		// default status to open if no filter present
		if (sm.Parm("FilterStatus").length() == 0) {
			sb.append(" AND s.code_value = 'O'");
		}

		else {
			if (!sm.Parm("FilterStatus").equalsIgnoreCase("0")) {
				sb.append(" AND s.code_value = '" + sm.Parm("FilterStatus")
						+ "'");
			}
		}

		if ((!sm.Parm("FilterPriority").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterPriority").length() > 0)) {
			sb
					.append(" AND p.code_value = '" + sm.Parm("FilterPriority")
							+ "'");
		}

		if ((!sm.Parm("FilterUser").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterUser").length() > 0)) {
			sb.append(" AND taction.assigned_uid = " + sm.Parm("FilterUser"));
		}

		return sb.toString();

	}

	/***************************************************************************
	 * 
	 * update closed-by date and close-by-uid
	 * 
	 **************************************************************************/
	public void beforeUpdate(Hashtable<String, DbField> ht) {
		// * set the closed fields if new status is closed
		if (sm.Parm("status_cd").equalsIgnoreCase("c")
				&& !sm.Parm("oldstatuscode").equalsIgnoreCase("c")) {
			ht.put("closed_date", new DbFieldDate("closed_date", new Date()));
			ht.put("closed_by", new DbFieldInteger("closed_by_uid", sm
					.getUserId()));
		}
		// clear the closed fields if the issue is re-opened
		if (!sm.Parm("status_cd").equalsIgnoreCase("c")
				&& sm.Parm("oldstatuscode").equalsIgnoreCase("c")) {
			ht.put("closed_date", new DbFieldDate("closed_date", ""));
			ht.put("closed_by", new DbFieldInteger("closed_by_uid",
					Integer.valueOf(0)));
		}
	}

	/***************************************************************************
	 * 
	 * Web Page Mapping
	 * 
	 **************************************************************************/

	public Hashtable<String, WebField> getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		/*
		 * id's
		 */

		ht.put("assigned_uid", new WebFieldSelect("assigned_uid",
				addMode ? Integer.valueOf(0) : (Integer) db
						.getObject("assigned_uid"), sm.getUserHT()));

		/*
		 * codes
		 */

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "New"
				: db.getText("status_cd"), sm.getCodes("ISSUESTAT")));

		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("ISSUTYPE")));

		ht.put("priority_cd", new WebFieldSelect("priority_cd", addMode ? ""
				: db.getText("priority_cd"), sm.getCodes("PRIORITY")));

		/*
		 * text
		 */

		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		/*
		 * dates
		 */

		ht.put("closed_date", new WebFieldDisplay("closed_date", (addMode ? ""
				: db.getText("closed_date"))));

		/*
		 * Blobs
		 */

		ht.put("issue_blob", new WebFieldText("issue_blob", addMode ? "" : db
				.getText("issue_blob"), 5, 80));

		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 5, 80));

		ht.put("notes_blob", new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 5, 80));

		ht.put("resolution_blob", new WebFieldText("resolution_blob",
				addMode ? "" : db.getText("resolution_blob"), 5, 80));

		ht.put("closed_by", new WebFieldDisplay("closed_by", (addMode ? "" : db
				.getText("last_name"))));

		/*
		 * save the old status code... do not put any under-scores in the web
		 * name so it won't get treated as a db update column
		 */
		ht.put("oldstatuscode", new WebFieldHidden("oldstatuscode",
				(addMode ? "" : db.getText("status_cd"))));

		return ht;

	}

}
