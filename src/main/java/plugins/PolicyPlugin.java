package plugins;

import java.util.Hashtable;

import router.SessionMgr;
import forms.*;

/**
 * Policy Plugin
 * 
 * Change log:
 * 
 * 9/14/2005 - Exact close of Issues... possible to make a higher class? .. but
 * unlike issues which default the list to open, policys don't have a default
 * status unlike issues, the status phase which resolved is d-delivered, not
 * c-closed as for issues.
 * 
 */

public class PolicyPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * Constructors
	 * 
	 * @throws services.ServicesException
	 * 
	 * 
	 **************************************************************************/

	public PolicyPlugin() throws services.ServicesException {
		super();

		this.setContextSwitchOk (false);

		this.setTableName("tpolicy");
		this.setKeyName("policy_id");
		this.setTargetTitle("Policy");
		this.setListHeaders( new String[] { "Reference", "Title", "Status",
				"KPA", "Owner" });

		// columns after last header are not shown! used to match list filters
		// only
		this.setMoreListColumns(new  String[] { "tpolicy.reference_nm",
				"tpolicy.title_nm", "s.code_desc as status_desc",
				"kpa.code_desc as kpa_desc",
				"concat(u.last_name, ',', u.first_name)", "s.code_value",
				"kpa.code_value", "tpolicy.owner_uid" });

		this.setMoreListJoins(new  String[] {
				" left join tcodes s on tpolicy.status_cd = s.code_value and s.code_type_id  = 72 ",
				" left join tcodes kpa on tpolicy.type_cd = kpa.code_value and kpa.code_type_id  = 69 ",
				" left join tuser u on tpolicy.owner_uid = u.user_id " });

		// this.setMoreSelectJoins (new String[] { "left join tuser as c on
		// tpolicy.closed_by_uid = c.user_id " };
		// this.setMoreSelectColumns (new String[] { "c.last_name" };

	}

	public void init(SessionMgr parmSm) {
		this.sm = parmSm;
		this.db = this.sm.getDbInterface(); // has an open connection
		this.setUpdatesOk(sm.userIsExecutive());
		
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
			WebFieldSelect wf = new WebFieldSelect("FilterStatus", sm
					.Parm("FilterStatus"), sm.getCodes("POLICYSTATUS"),
					"All Status");
			wf.setDisplayClass("listform");
			return wf;
		} else {
			if (columnNumber == 3) {

				WebFieldSelect wf = new WebFieldSelect("FilterKPA", sm
						.Parm("FilterKPA"), sm.getCodes("KPA"), "All KPA");
				wf.setDisplayClass("listform");
				return wf;
			} else {

				WebFieldSelect wf = new WebFieldSelect("FilterUser", (sm.Parm(
						"FilterUser").length() == 0 ? new Integer("0")
						: new Integer(sm.Parm("FilterUser"))), sm.getUserHT(),
						"All Owners");
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

		if ((!sm.Parm("FilterStatus").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterStatus").length() > 0)) {
			sb.append(" AND s.code_value = '" + sm.Parm("FilterStatus") + "'");
		}

		if ((!sm.Parm("FilterKPA").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterKPA").length() > 0)) {
			sb.append(" AND kpa.code_value = '" + sm.Parm("FilterKPA") + "'");
		}

		if ((!sm.Parm("FilterUser").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterUser").length() > 0)) {
			sb.append(" AND tpolicy.owner_uid = " + sm.Parm("FilterUser"));
		}

		return sb.toString();

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
		 * codes
		 * 
		 */

		ht.put("status_cd",  new WebFieldSelect("status_cd",
				addMode ? "New" : db.getText("status_cd"), sm
						.getCodes("POLICYSTATUS")));

		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("KPA")));

		ht.put("priority_cd", new WebFieldSelect("priority_cd",
				addMode ? "" : db.getText("priority_cd"), sm
						.getCodes("PRIORITY")));

		/*
		 * Text
		 */
		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("target_tx", new WebFieldString("target_tx", (addMode ? ""
				: db.getText("target_tx")), 64, 64));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 64, 64));

		/*
		 * dates
		 */

		ht.put("effective_date", new WebFieldDate("effective_date",
				(addMode ? "" : db.getText("effective_date"))));

		/*
		 * blobs
		 */
		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80));
		
		ht.put("notes_blob", new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 3, 80));

		ht.put("reason_blob", new WebFieldText("reason_blob", addMode ? ""
				: db.getText("reason_blob"), 3, 80));

		ht.put("inclusions_blob", new WebFieldText("inclusions_blob",
				addMode ? "" : db.getText("inclusions_blob"), 3, 80));

		ht.put("exclusions_blob", new WebFieldText("exclusions_blob",
				addMode ? "" : db.getText("exclusions_blob"), 3, 80));

		/*
		 * id's
		 */
		ht.put("owner_uid",  new WebFieldSelect("owner_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("owner_uid"), sm.getUserHT()));

		return ht;

	}

}
