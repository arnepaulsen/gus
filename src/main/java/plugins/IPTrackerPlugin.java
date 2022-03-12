package plugins;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.sql.*;

import services.ExcelWriter;
import services.ServicesException;

import org.apache.poi.hssf.util.HSSFColor;

import router.SessionMgr;
import db.DbField;
import db.DbFieldDate;
import db.DbFieldString;
import forms.*;

/**
 * 
 * 10/8/08 first plugin to support history logging 11/20/08 remove spaces from
 * file name .todo make sure excel file names are legal ..like server.encode
 * 
 * sR 003205810 in process ... still need # new FIELDS
 * 
 * 2/5/09 include blank status-cd records on list page
 * 
 * 11/3/09 revision_cd new values.
 * 
 * 6/13/10 change to standard view names
 * 
 * 8/2/10 add sr_no as hidden field "remedyno" (removing 
 * 
 */

public class IPTrackerPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public String sr_type; // either "OS" for OrderSet or "SR" for non-order
	// set

	public String detail_page_nm = "";

	private String isNull = "isnull";

	public IPTrackerPlugin() throws services.ServicesException {

		super();
		this.setTableName("tip_tracker");
		this.setKeyName("tracker_id");

		// this.remedyType = "IP";
		// this.remedyKey = "sr_no";

		this.setListOrder("sr_no");
		this.setListViewName("vip_tracker_list");
		this.setSelectViewName("vip_tracker");
		
		this.setRemedyOk(true);

		this.setExcelOk(true);

		this.setListHeaders(new String[] { "SR", "Summary", "Order Set",
				"Keywords", "Level", "Priority", "Domain", "SUIT", "Status",
				"Owner", "HCG", "Planned" });

		this.setTemplateName("IPTracker.html");

		// this.setScriptInit("location.href='#sr3440557';");

		// sr3440557
	}

	public void init(SessionMgr parmSm) {

		super.init(parmSm);

		// needs to be down here to take priority over init() function in
		// absRemedy.
		this.setUpdatesLevel("inpatient");
		this.setShowAuditSubmitApprove(false); // can turn off the
		this.setNextOk(false);

		// scroll the list page down to the last item visited
		if (sm.getParentId() != null) {
			// this.setScriptInit("location.href='#sr" +
			// sm.getParentId().toString() + "';");
		}

		if (!sm.isSQLServer()) {
			isNull = "isnull_char";

		}
	}

	/***************************************************************************
	 * 
	 * List Processing
	 * 
	 **************************************************************************/

	/*
	 * need to return dynamic title using session Parm data
	 */

	public boolean getListColumnCenterOn(int columnNumber) {
		return false;
	}

	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#2) has a selector, other fields do not

		if (columnNumber == 2 || (columnNumber > 3 && columnNumber < 10)
				|| columnNumber == 10 || columnNumber == 11)
			return true;
		else
			return false;
	}

	private Hashtable getStatHT() {

		String sql = " select order_by as odor, code_value, code_desc from tcodes "
				+ " join tcode_types on tcodes.code_type_id = tcode_types.code_type_id "
				+ " where tcodes.code_type_id  = tcode_types.code_type_id  and  tcode_types.code_type = 'REMEDY' "
				+ " UNION select 99 as odor , 'ALLCLS' as y, 'Closed+Resolved' z "
				+ " UNION select 98 as odor , 'ALLOPE' as y, 'All Open' z "
				+ " ORDER BY 1 ";

		return sm.getTable("OS_STAT_EXP", sql);

	}

	/*
	 * need to return dynamic title using session Parm data
	 */
	public String getListTitle() {

		String keyword = "";
		if (sm.Parm("FilterKeyword").length() < 1)
			keyword = "Keyword?";
		else
			keyword = sm.Parm("FilterKeyword");

		return this.getTargetTitle()
				+ "&nbsp;&nbsp;&nbsp;&nbsp;Keyword&nbsp;<input type='text' name='FilterKeyword' id='FilterKeyword' size=32 maxlength=32 value='"
				+ keyword + "'>";

	}

	public WebField getListSelector(int columnNumber) {

		int start_col = 3;

		if (columnNumber == start_col + 1) {
			WebFieldSelect levels = (WebFieldSelect) getListSelector(
					"FilterLevel", "", "Level?", sm.getCodes("HIGHMEDLOW"));
			levels.allowMultiple();
			levels.setSelectedList(sm.ParmArray("FilterLevel"));
			return levels;
		}

		if (columnNumber == start_col + 2) {
			WebFieldSelect priorities = (WebFieldSelect) getListSelector(
					"FilterPriority", "", "Priority?", sm
							.getCodes("IPPRIORITIES"));
			priorities.allowMultiple();
			priorities.setSelectedList(sm.ParmArray("FilterPriority"));
			return priorities;
		}

		/*
		 * Order Set Filter - only show order sets that are actually in use..not
		 * the whole list
		 */
		if (columnNumber == 2) {

			String qry = new String(
					"select distinct  convert(order_set_no, char) as xxint1, convert(korder_set.order_set_id,char) as obj2, convert(order_set_no, char) as obj3 from korder_set  join korder_set_link on korder_set.order_set_id = korder_set_link.order_set_id "
							+ " where order_set_no <> 0 order by order_set_no");

			Hashtable sets = new Hashtable();

			sets = sm.getTable("ORDERSETS", qry);

			return getListSelector("FilterOrderset", new Integer("0"),
					"Orderset# ? ", sets);
		}

		if (columnNumber == start_col + 3) {

			String qry = "SELECT code_value, code_value, code_desc from tcodes where code_type_id = 142 and code_desc2 like '%"
					+ sr_type + "%' ORDER BY 3 ";

			Hashtable domains = new Hashtable();

			domains = sm.getTable("IPDOMAIN" + sr_type, qry);

			WebFieldSelect selector = (WebFieldSelect) getListSelector(
					"FilterDomain", "", "Domain?", domains);
			selector.allowMultiple();
			selector.setSelectedList(sm.ParmArray("FilterDomain"));
			return selector;
		}

		if (columnNumber == start_col + 4) {
			WebFieldSelect selector = (WebFieldSelect) getListSelector(
					"FilterSUIT", "", "SUIT?", sm.getCodes("AMBSUIT"));
			selector.allowMultiple();
			selector.setSelectedList(sm.ParmArray("FilterSUIT"));
			return selector;

		}

		if (columnNumber == start_col + 5)
			return getListSelector("FilterStatus", "ALLOPE", "Status?",
					getStatHT());

		if (columnNumber == start_col + 6) {
			// TODO.. Resource HOG!!! It queries every time. Let the db manager
			// cache the data.
			String qry = new String(
					"select distinct "
							+ "concat(c.last_name, ', ', c.first_name) as a , "
							+ "c.contact_id as b, "
							+ "concat(c.last_name, ', ',	c.first_name) as c "
							+ " from tip_tracker join tcontact c on tip_tracker.owner_uid = c.contact_id ");
			// just JOIN,. then only those that exist will connnect
			// + " where isnull_char(owner_uid, 0) > 0 and " + sql_len +
			// "(c.first_name) > 1 and " + sql_len + "(c.last_name) > 1");

			if (this.sr_type.equalsIgnoreCase("OS"))
				qry = qry + " AND tip_tracker.type_cd = 'OS' ";

			Hashtable contacts = new Hashtable();

			try {
				contacts = db.getLookupTable(qry);
			} catch (ServicesException e) {
			}
			return getListSelector("FilterOwner", new Integer("0"), "Owner ? ",
					contacts);
		}

		/*
		 * get unique install date
		 */
		if (columnNumber == 10) {

			String dateCast1 = null;
			String dateCast2 = null;
			String qry = null;

			dateCast1 = " ( "
					+ dbprefix
					+ "FormatDateTime(tip_tracker.hcg_meet_date, 'yyyy/mm/dd')) ";

			dateCast2 = " ( " + dbprefix
					+ "FormatDateTime(tip_tracker.hcg_meet_date, 'mm/dd/yy')) ";

			qry = " select distinct " + dateCast1 + "," + dateCast1 + ","
					+ dateCast2
					+ " from tip_tracker where hcg_meet_date > '12/31/2008' ";

			Hashtable dates = new Hashtable();

			try {
				dates = db.getLookupTable(qry);
			} catch (ServicesException e) {

			}
			return getListSelector("FilterHCG", "O", "HCG?", dates);

		}

		if (columnNumber == 11) {

			String qry = null;

			String dateCast1 = null;
			String dateCast2 = null;

			dateCast1 = " ( "
					+ dbprefix
					+ "FormatDateTime(tip_tracker.remedy_end_dt, 'yyyy/mm/dd')) ";

			dateCast2 = " ( " + dbprefix
					+ "FormatDateTime(tip_tracker.remedy_end_dt, 'mm/dd/yy')) ";

			qry = " select distinct "
					+ dateCast1
					+ ","
					+ dateCast1
					+ ","
					+ dateCast2
					+ " from tip_tracker  where  tip_tracker.remedy_end_dt > '12/31/2008' ";

			Hashtable dates1 = new Hashtable();

			try {
				dates1 = db.getLookupTable(qry);
			} catch (ServicesException e) {

			}
			return getListSelector("FilterEndDate", "O", "End?", dates1);

		}

		// debug("filter should return before now!!!");

		// will never get here

		return getListSelector("dummy", new Integer(""), "badd..",
				new Hashtable());

	}

	public String getListAnd() {
		/*
		 * watch out for "o" open values vs. zero (0) for 'all' value
		 */

		StringBuffer sb = new StringBuffer();

		sb.append(" and tracker_set_id = 0 ");

		// From the Order Set view.. only show order Sets... else show them all

		if (sr_type.equalsIgnoreCase("OS")) {
			sb.append(" and type_cd = 'OS'");
		}

		/*
		 * wow... now filtering based on strings!!
		 */

		if (!sm.Parm("FilterKeyword").equalsIgnoreCase("Keyword?")
				&& sm.Parm("FilterKeyword").length() > 0) {
			sb.append(" AND ((keyword_tx like '%" + sm.Parm("FilterKeyword")
					+ "%') OR (title_nm LIKE '%" + sm.Parm("FilterKeyword")
					+ "%'))");
		}

		filterMultiSelect(sb, "FilterLevel", "level_cd");

		filterMultiSelect(sb, "FilterPriority", "priority_cd");

		filterMultiSelect(sb, "FilterSUIT", "suit_cd");

		String[] array = sm.ParmArray("FilterDomain");
		if (array != null) {
			if (array.length > 0) {
				String x = array[0];
				StringBuffer whereList = new StringBuffer();

				if (!x.equalsIgnoreCase("0") && !x.equalsIgnoreCase("")) {

					whereList.append(" in ('" + array[0] + "'");
					for (int i = 1; i < array.length; i++) {
						whereList.append(",'" + array[i] + "'");
					}
					whereList.append(")");

					sb.append(" AND ((domain_1_cd " + whereList + ")");
					sb.append(" OR (domain_2_cd " + whereList + (")"));
					sb.append(" OR (domain_3_cd " + whereList + (")"));
					sb.append(" OR (domain_4_cd " + whereList + (")"));
					sb.append(" OR (domain_5_cd " + whereList + (")"));
					sb.append(" OR (domain_6_cd " + whereList + (")"));
					sb.append(" OR (domain_7_cd " + whereList + (")"));
					sb.append(" OR (domain_8_cd " + whereList + ("))"));
				}
			}
		}

		// default status to open if no filter present

		if (sm.Parm("FilterStatus").length() == 0
				|| sm.Parm("FilterStatus").equalsIgnoreCase("ALLOPE")) {
			sb
					.append(" AND "
							+ isNull
							+ "(status_cd,'NEW') in ('NEW','ASS','WOR','PLA', 'SCH', 'PEN') ");
		} else {

			if (sm.Parm("FilterStatus").equalsIgnoreCase("ALLCLS")) {
				sb.append(" AND status_cd in ('CLO','RES') ");
			} else {
				if (!sm.Parm("FilterStatus").equalsIgnoreCase("0")) {
					sb.append(" AND status_cd = '" + sm.Parm("FilterStatus")
							+ "'");
				}
			}
		}

		// filter on owner
		if (sm.Parm("FilterOwner").length() == 0) {
		}

		else {
			if (!sm.Parm("FilterOwner").equalsIgnoreCase("0")) {
				sb.append(" AND owner_uid = " + sm.Parm("FilterOwner"));
			}
		}

		// filter on owner
		if (sm.Parm("FilterOrderset").length() == 0) {
		} else {
			if (!sm.Parm("FilterOrderset").equalsIgnoreCase("0")) {
				sb
						.append(" and tracker_id in (select request_id as tracker_id from korder_set_link join korder_set on korder_set_link.order_set_id = korder_set.order_set_id and korder_set.order_set_id = "
								+ sm.Parm("FilterOrderset") + ")");
			}
		}

		if (sm.Parm("FilterHCG").length() == 0) {
			// sb.append(" AND review_date >= dateadd(d, -3, getdate()) ");
		} else {

			if (!sm.Parm("FilterHCG").equalsIgnoreCase("0")) {
				sb
						.append(" AND hcg_meet_date = '" + sm.Parm("FilterHCG")
								+ "'");
			}
		}

		if (sm.Parm("FilterEndDate").length() == 0) {
			// sb.append(" AND review_date >= dateadd(d, -3, getdate()) ");
		} else {

			if (!sm.Parm("FilterEndDate").equalsIgnoreCase("0")) {
				sb.append(" AND (remedy_end_dt >= '" + sm.Parm("FilterEndDate")
						+ "' AND date_add(remedy_end_dt, INTERVAL -1 DAY) <= '"
						+ sm.Parm("FilterEndDate") + "') ");

			}
			// SQL-SERVER + "' > dateadd(day,-1,remedy_end_dt)) ");
		}

		// debug(" ip trackker filter: " + sb.toString());

		return sb.toString();

	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	// force in either SR or OS to the type code
	public boolean beforeAdd(Hashtable ht) {
		ht.put("type_cd", new DbFieldString("type_cd", sr_type));

		return true;
	}

	public Hashtable<String, WebField> getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		boolean showMode = parmMode.equalsIgnoreCase("show") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		int port = sm.getServerPort();
		String host = sm.getHost();
		String tomcat = sm.getTomcatName();

		ht.put("host", new WebFieldDisplay("host", host + ":" + port));

		ht.put("tomcat_name", new WebFieldDisplay("tomcat_name", sm
				.getTomcatName()));

		ht.put("remedyno", new WebFieldHidden("remedyno", addMode ? "0" : db.getText("sr_no")));
		
		// allow java script to bypass edits on display fields.

		ht.put("reviewType", new WebFieldHidden("reviewType", (addMode ? ""
				: db.getText("rv_grp_cd"))));

		
		if (parmMode.equalsIgnoreCase("show")) {

			sm.setServiceRequestId(db.getInteger("tracker_id"), db
					.getText("title_nm"));
		}

		// save key info
		if (parmMode.equalsIgnoreCase("show")) {
			// debug("rfc plug in ... setrfcno " + db.getText("rfc_no"));

			sm.setRfcNo(db.getText("sr_no"), db.getText("title_nm"));

			// debug("back from sm : " + sm.getRfcNo());
		}

		/*
		 * tricky one - get list of related order sets.
		 */

		if (addMode) {
			ht.put("setlist", new WebFieldDisplay("setlist", ""));
		} else {
			// get a list of relsated order sets
			String sql = "select distinct s.order_set_no, s.title_nm from korder_set_link l "
					+ " join korder_set s on l.order_set_id = s.order_set_id "
					+ " where request_id = "
					+ db.getInteger("tracker_id").toString()
					+ " order by s.title_nm";

			try {
				StringBuffer sb = new StringBuffer();
				java.sql.ResultSet rs = db.getRS(sql);

				while (rs.next()) {
					sb.append(rs.getString("order_set_no") + " - "
							+ rs.getString("title_nm") + "<br>");
					// sb.append(0x0a);
				}

				rs.close();
				ht
						.put("setlist", new WebFieldDisplay("setlist", sb
								.toString()));
			} catch (Exception e) {
				ht.put("setlist", new WebFieldDisplay("setlist", ""));
			}
		}

		/*
		 * page title
		 */

		ht
				.put("pagename", new WebFieldDisplay("pagename",
						this.detail_page_nm));

		/*
		 * id's
		 */

		Hashtable sets = sm.getTable("OrderSet",
				"select title_nm, order_set_id, title_nm from korder_set");

		ht.put("order_set_id", new WebFieldSelect("order_set_id",
				addMode ? new Integer("0") : db.getInteger("order_set_id"),
				sets, true));

		ht.put("owner_uid", new WebFieldSelect("owner_uid",
				addMode ? new Integer("0") : db.getInteger("owner_uid"), sm
						.getContactHT(), true));

		ht.put("owner2_uid", new WebFieldSelect("owner2_uid",
				addMode ? new Integer("0") : db.getInteger("owner2_uid"), sm
						.getContactHT(), true));

		/*
		 * dates
		 */

		ht.put("target_date", new WebFieldDate("target_date", (addMode ? ""
				: db.getText("target_date"))));

		ht.put("tigr_rv_date", new WebFieldDate("tigr_rv_date", (addMode ? ""
				: db.getText("tigr_rv_date"))));

		/*
		 * phase 1
		 */

		ht.put("chg_memo_date", new WebFieldDate("chg_memo_date", (addMode ? ""
				: db.getText("chg_memo_date").length() < 2 && showMode ? ""
						: db.getText("chg_memo_date"))));

		ht.put("chg_memo_done_date", new WebFieldDate("chg_memo_done_date",
				(addMode ? "" : db.getText("chg_memo_done_date").length() < 2
						&& showMode ? "" : db.getText("chg_memo_done_date"))));

		/*
		 * conditional display / edit
		 */

		String na = "";

		if (db.getText("rv_grp_cd").equalsIgnoreCase("S")) {

			ht.put("suit_apvr_tx", new WebFieldText("suit_apvr_tx",
					(addMode ? "" : db.getText("suit_apvr_tx")), 3, 40));

			ht.put("suit_apv_date", new WebFieldDate("suit_apv_date",
					(addMode ? "" : db.getText("suit_apv_date").length() < 2
							&& showMode ? "" : db.getText("suit_apv_date"))));

			ht.put("suit_apv_done_date", new WebFieldDate("suit_apv_done_date",
					(addMode ? ""
							: db.getText("suit_apv_done_date").length() < 2
									&& showMode ? "" : db
									.getText("suit_apv_done_date"))));
		} else {

			if (db.getText("rv_grp_cd").equalsIgnoreCase("D")
					|| db.getText("rv_grp_cd").equalsIgnoreCase("L"))
				na = "N/A";
			else
				na = "";

			ht.put("suit_apvr_tx", new WebFieldDisplay("suit_apvr_tx", na));

			ht.put("suit_apv_date", new WebFieldDisplay("suit_apv_date", na));

			ht.put("suit_apv_done_date", new WebFieldDisplay(
					"suit_apv_done_date", na));
		}

		if (db.getText("rv_grp_cd").equalsIgnoreCase("D")) {

			ht.put("nrse_apvr_1a_tx", new WebFieldText("nrse_apvr_1a_tx",
					(addMode ? "" : db.getText("nrse_apvr_1a_tx")), 3, 40));

			ht.put("phy_apvr_1b_tx", new WebFieldText("phy_apvr_1b_tx",
					(addMode ? "" : db.getText("phy_apvr_1b_tx")), 3, 40));

			ht.put("dom_rn_apv_content_date", new WebFieldDate(
					"dom_rn_apv_content_date", (addMode ? "" : db.getText(
							"dom_rn_apv_content_date").length() < 2
							&& showMode ? "" : db
							.getText("dom_rn_apv_content_date"))));

			ht.put("dom_rn_apv_content_done_date", new WebFieldDate(
					"dom_rn_apv_content_done_date", (addMode ? "" : db.getText(
							"dom_rn_apv_content_done_date").length() < 2
							&& showMode ? "" : db
							.getText("dom_rn_apv_content_done_date"))));

			ht.put("dom_md_apv_content_date", new WebFieldDate(
					"dom_md_apv_content_date", (addMode ? "" : db.getText(
							"dom_md_apv_content_date").length() < 2
							&& showMode ? "" : db
							.getText("dom_md_apv_content_date"))));

			ht.put("dom_md_apv_content_done_date", new WebFieldDate(
					"dom_md_apv_content_done_date", (addMode ? "" : db.getText(
							"dom_md_apv_content_done_date").length() < 2
							&& showMode ? "" : db
							.getText("dom_md_apv_content_done_date"))));

		} else {

			if (db.getText("rv_grp_cd").equalsIgnoreCase("S"))
				na = "N/A";
			else
				na = "";

			ht.put("nrse_apvr_1a_tx",
					new WebFieldDisplay("nrse_apvr_1a_tx", ""));

			ht.put("phy_apvr_1b_tx", new WebFieldDisplay("phy_apvr_1b_tx", ""));

			ht.put("dom_rn_apv_content_date", new WebFieldDisplay(
					"dom_rn_apv_content_date", na));

			ht.put("dom_rn_apv_content_done_date", new WebFieldDisplay(
					"dom_rn_apv_content_done_date", na));

			ht.put("dom_md_apv_content_date", new WebFieldDisplay(
					"dom_md_apv_content_date", na));

			ht.put("dom_md_apv_content_done_date", new WebFieldDisplay(
					"dom_md_apv_content_done_date", na));

		}

		/*
		 * 
		 * end conditional edit/display
		 * 
		 */

		ht
				.put("dom_md_apv_suit_date", new WebFieldDate(
						"dom_md_apv_suit_date", (addMode ? "" : db.getText(
								"dom_md_apv_suit_date").length() < 2
								&& showMode ? "" : db
								.getText("dom_md_apv_suit_date"))));

		ht.put("dom_md_apv_suit_done_date", new WebFieldDate(
				"dom_md_apv_suit_done_date", (addMode ? "" : db.getText(
						"dom_md_apv_suit_done_date").length() < 2
						&& showMode ? "" : db
						.getText("dom_md_apv_suit_done_date"))));

		ht
				.put("dom_rn_apv_suit_date", new WebFieldDate(
						"dom_rn_apv_suit_date", (addMode ? "" : db.getText(
								"dom_rn_apv_suit_date").length() < 2
								&& showMode ? "" : db
								.getText("dom_rn_apv_suit_date"))));

		ht.put("dom_rn_apv_suit_done_date", new WebFieldDate(
				"dom_rn_apv_suit_done_date", (addMode ? "" : db.getText(
						"dom_rn_apv_suit_done_date").length() < 2
						&& showMode ? "" : db
						.getText("dom_rn_apv_suit_done_date"))));

		ht.put("nurse_apv_date", new WebFieldDate("nurse_apv_date",
				(addMode ? "" : db.getText("nurse_apv_date").length() < 2
						&& showMode ? "" : db.getText("nurse_apv_date"))));

		ht.put("nurse_apv_done_date", new WebFieldDate("nurse_apv_done_date",
				(addMode ? "" : db.getText("nurse_apv_done_date").length() < 2
						&& showMode ? "" : db.getText("nurse_apv_done_date"))));

		ht.put("med_ctr_rv_date", new WebFieldDate("med_ctr_rv_date",
				(addMode ? "" : db.getText("med_ctr_rv_date"))));

		ht.put("med_ctr_rv_done_date", new WebFieldDate("med_ctr_rv_done_date",
				(addMode ? "" : db.getText("med_ctr_rv_done_date"))));

		ht.put("content_rv_date", new WebFieldDate("content_rv_date",
				(addMode ? "" : db.getText("content_rv_date"))));

		ht.put("lead_apv_date", new WebFieldDate("lead_apv_date", (addMode ? ""
				: db.getText("lead_apv_date"))));

		ht.put("regional_apv_date", new WebFieldDate("regional_apv_date",
				(addMode ? "" : db.getText("regional_apv_date"))));

		ht.put("med_exec_comm_date", new WebFieldDate("med_exec_comm_date",
				(addMode ? "" : db.getText("med_exec_comm_date"))));

		ht.put("med_exec_comm_done_date", new WebFieldDate(
				"med_exec_comm_done_date", (addMode ? "" : db
						.getText("med_exec_comm_done_date"))));

		ht.put("lead_mockup_apv_date", new WebFieldDate("lead_mockup_apv_date",
				(addMode ? "" : db.getText("lead_mockup_apv_date"))));

		ht.put("mc_domain_apv_date", new WebFieldDate("mc_domain_apv_date",
				(addMode ? "" : db.getText("mc_domain_apv_date"))));

		ht.put("collate_rc_date", new WebFieldDate("collate_rc_date",
				(addMode ? "" : db.getText("collate_rc_date"))));

		ht.put("collate_rc_done_date", new WebFieldDate("collate_rc_done_date",
				(addMode ? "" : db.getText("collate_rc_done_date"))));

		ht.put("sponsor_due_date", new WebFieldDate("sponsor_due_date",
				(addMode ? "" : db.getText("sponsor_due_date"))));

		ht.put("sponsor_done_date", new WebFieldDate("sponsor_done_date",
				(addMode ? "" : db.getText("sponsor_done_date"))));

		ht.put("hcg_apv_date", new WebFieldDate("hcg_apv_date", (addMode ? ""
				: db.getText("hcg_apv_date"))));

		ht.put("document_date", new WebFieldDate("document_date", (addMode ? ""
				: db.getText("document_date"))));

		ht.put("document_done_date", new WebFieldDate("document_done_date",
				(addMode ? "" : db.getText("document_done_date"))));

		ht.put("hcg_meet_date", new WebFieldDate("hcg_meet_date", (addMode ? ""
				: db.getText("hcg_meet_date"))));

		ht.put("screen_shot_deadline_date", new WebFieldDate(
				"screen_shot_deadline_date", (addMode ? "" : db
						.getText("screen_shot_deadline_date"))));

		ht.put("screen_shot_date", new WebFieldDate("screen_shot_date",
				(addMode ? "" : db.getText("screen_shot_date"))));

		ht.put("screen_shot_done_date", new WebFieldDate(
				"screen_shot_done_date", (addMode ? "" : db
						.getText("screen_shot_done_date"))));

		ht.put("dist_hccgc_date", new WebFieldDate("dist_hccgc_date",
				(addMode ? "" : db.getText("dist_hccgc_date"))));

		ht.put("adv_dist_hcg_date", new WebFieldDate("adv_dist_hcg_date",
				(addMode ? "" : db.getText("adv_dist_hcg_date"))));

		ht.put("gov_due_date", new WebFieldDate("gov_due_date", (addMode ? ""
				: db.getText("gov_due_date"))));

		ht.put("gov_done_date", new WebFieldDate("gov_done_date", (addMode ? ""
				: db.getText("gov_done_date"))));

		// Removed per sr 003205810 1/3/09
		// ht.put("adv_dist_hcg_done_date", new WebFieldDate(
		// "adv_dist_hcg_done_date", (addMode ? "" : db
		// .getText("adv_dist_hcg_done_date"))));

		ht.put("dist_hccgc_date", new WebFieldDate("dist_hccgc_date",
				(addMode ? "" : db.getText("dist_hccgc_date"))));

		ht.put("regional_hccgc_date", new WebFieldDate("regional_hccgc_date",
				(addMode ? "" : db.getText("regional_hccgc_date"))));

		ht
				.put("reg_med_ctr_apv_date", new WebFieldDate(
						"reg_med_ctr_apv_date", (addMode ? "" : db.getText(
								"reg_med_ctr_apv_date").length() < 2
								&& showMode ? "" : db
								.getText("reg_med_ctr_apv_date"))));

		ht.put("reg_med_ctr_apv_done_date", new WebFieldDate(
				"reg_med_ctr_apv_done_date", (addMode ? "" : db.getText(
						"reg_med_ctr_apv_done_date").length() < 2
						&& showMode ? "" : db
						.getText("reg_med_ctr_apv_done_date"))));

		ht.put("rlse_notify_date", new WebFieldDate("rlse_notify_date",
				(addMode ? "" : db.getText("rlse_notify_date").length() < 2
						&& showMode ? "" : db.getText("rlse_notify_date"))));

		ht.put("rlse_notify_done_date", new WebFieldDate(
				"rlse_notify_done_date", (addMode ? "" : db
						.getText("rlse_notify_done_date"))));

		ht.put("required_date", new WebFieldDate("required_date", (addMode ? ""
				: db.getText("required_date"))));

		ht.put("production_date", new WebFieldDate("production_date",
				(addMode ? "" : db.getText("production_date"))));

		/*
		 * Approvers
		 */

		ht.put("nrse_apvr_1b_tx", new WebFieldText("nrse_apvr_1b_tx",
				(addMode ? "" : db.getText("nrse_apvr_1b_tx")), 3, 40));

		ht.put("reg_med_apvr_tx", new WebFieldText("reg_med_apvr_tx",
				(addMode ? "" : db.getText("reg_med_apvr_tx")), 3, 40));

		ht.put("dom_md_apv_suit_tx", new WebFieldText("dom_md_apv_suit_tx",
				(addMode ? "" : db.getText("dom_md_apv_suit_tx")), 3, 40));

		ht.put("dom_rn_apv_suit_tx", new WebFieldText("dom_rn_apv_suit_tx",
				(addMode ? "" : db.getText("dom_rn_apv_suit_tx")), 3, 40));

		/*
		 * codes
		 * 
		 * 
		 */

		String[][] revisions = { { "N", "R", "P", "T" },
				{ "New", "Revision", "Project", "Task" } };

		String[][] review_grp = { { "S", "D", "L" },
				{ "SUIT Approval", "Domain Approval", "KPHC Leadership" } };

		String[][] phase1 = {
				{ "B", "F", "W", "C" },
				{ "Building", "Waiting Domain Feedback",
						"Waiting Domain Approval", "Build Complete" } };

		String[][] screens = {
				{ "Stage", "Prod", "Train", "WIT2", "WIT3", "REGN" },
				{ "Stage", "Prod", "Train", "WIT2", "WIT3", "Regnncm" } };

		ht.put("revision_cd", new WebFieldSelect("revision_cd", addMode ? ""
				: db.getText("revision_cd"), revisions));

		Hashtable facilities = sm
				.getTable(
						"tfacility2",
						"select facility_cd, facility_cd, "
								+ "concat(facility_cd, '-', facility_nm) facility_nm from tfacility order by facility_cd ");

		ht.put("facility_cd", new WebFieldSelect("facility_cd", addMode ? ""
				: db.getText("facility_cd"), facilities, true, true));

		ht.put("phase1_stat_cd", new WebFieldSelect("phase1_stat_cd",
				addMode ? "" : db.getText("phase1_stat_cd"), phase1));

		ht.put("rv_grp_cd", new WebFieldSelect("rv_grp_cd", addMode ? "" : db
				.getText("rv_grp_cd"), review_grp));

		ht.put("status_cd", new WebFieldDisplay("status_cd", (addMode ? "" : db
				.getText("remedy_status"))));

		ht.put("remedy_grp_tx", new WebFieldDisplay("remedy_grp_tx",
				(addMode ? "" : db.getText("remedy_grp_tx"))));

		ht.put("patient_safety_cd", new WebFieldSelect("patient_safety_cd",
				addMode ? "N" : db.getText("patient_safety_cd"), sm
						.getCodes("YESNO")));

		ht.put("priority_cd", new WebFieldSelect("priority_cd",
				addMode ? "NICE" : db.getText("priority_cd"), sm
						.getCodes("IPPRIORITIES")));

		ht.put("regulatory_cd", new WebFieldSelect("regulatory_cd",
				addMode ? "N" : db.getText("regulatory_cd"), sm
						.getCodes("YESNO")));

		ht.put("impact_cd", new WebFieldSelect("impact_cd", addMode ? "N" : db
				.getText("impact_cd"), sm.getCodes("HIGHMEDLOW")));

		ht.put("suite_cd", new WebFieldSelect("suite_cd", addMode ? "" : db
				.getText("suite_cd"), sm.getCodes("AMBSUIT"), " "));

		ht.put("level_cd", new WebFieldSelect("level_cd", addMode ? "H" : db
				.getText("level_cd"), sm.getCodes("HIGHMEDLOW")));

		/*
		 * query for domains.. the list query should already have it cached !
		 */
		String qry2 = "SELECT code_value, code_value, code_desc from tcodes where code_type_id = 142 and code_desc2 like '%"
				+ sr_type + "%' ORDER BY 3 ";

		Hashtable domains = new Hashtable();

		domains = sm.getTable("IPDOMAIN" + sr_type, qry2);

		ht.put("domain_1_cd", new WebFieldSelect("domain_1_cd", addMode ? ""
				: db.getText("domain_1_cd"), domains, true, true));

		ht.put("domain_2_cd", new WebFieldSelect("domain_2_cd", addMode ? ""
				: db.getText("domain_2_cd"), domains, ""));

		ht.put("domain_3_cd", new WebFieldSelect("domain_3_cd", addMode ? ""
				: db.getText("domain_3_cd"), domains, ""));

		ht.put("domain_4_cd", new WebFieldSelect("domain_4_cd", addMode ? ""
				: db.getText("domain_4_cd"), domains, ""));

		ht.put("domain_5_cd", new WebFieldSelect("domain_5_cd", addMode ? ""
				: db.getText("domain_5_cd"), domains, ""));

		ht.put("domain_6_cd", new WebFieldSelect("domain_6_cd", addMode ? ""
				: db.getText("domain_6_cd"), domains, ""));

		ht.put("domain_7_cd", new WebFieldSelect("domain_7_cd", addMode ? ""
				: db.getText("domain_7_cd"), domains, ""));

		ht.put("domain_8_cd", new WebFieldSelect("domain_8_cd", addMode ? ""
				: db.getText("domain_8_cd"), domains, ""));

		/*
		 * girlie codes
		 */

		ht.put("screen_shot_cd", new WebFieldSelect("screen_shot_cd",
				addMode ? "" : db.getText("screen_shot_cd"), screens));

		/*
		 * strings
		 */

		ht.put("protocols_tx", new WebFieldDisplay("protocols_tx",
				(addMode ? "" : db.getText("protocols_tx"))));

		ht.put("RequesterDept", new WebFieldDisplay("RequesterDept",
				(addMode ? "" : "Dept: " + db.getText("dept_nm"))));

		ht.put("RequesterFacl", new WebFieldDisplay("RequesterFacl",
				(addMode ? "" : "Facility: " + db.getText("facility_nm"))));

		ht.put("alt_owner_tx", new WebFieldFile("alt_owner_tx", (addMode ? ""
				: db.getText("alt_owner_tx")), 32, 32));

		ht.put("doc_link_tx", new WebFieldFile("doc_link_tx", (addMode ? ""
				: db.getText("doc_link_tx")), 64, 127));

		ht.put("build_env_tx", new WebFieldString("build_env_tx", (addMode ? ""
				: db.getText("build_env_tx")), 32, 64));

		ht.put("alt_build_env_tx", new WebFieldString("alt_build_env_tx",
				(addMode ? "" : db.getText("alt_build_env_tx")), 32, 64));

		ht.put("keyword_tx", new WebFieldString("keyword_tx", (addMode ? ""
				: db.getText("keyword_tx")), 64, 64));

		ht.put("sr_no", new WebFieldString("sr_no", (addMode ? "" : db
				.getText("sr_no")), 8, 8));

		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("prim_sponsor_nm", new WebFieldString("prim_sponsor_nm",
				(addMode ? "" : db.getText("prim_sponsor_nm")), 64, 64));

		ht.put("sec_sponsor_nm", new WebFieldString("sec_sponsor_nm",
				(addMode ? "" : db.getText("sec_sponsor_nm")), 64, 64));

		ht.put("online_vers_tx", new WebFieldString("online_vers_tx",
				(addMode ? "" : db.getText("online_vers_tx")), 8, 8));

		ht.put("paper_vers_tx", new WebFieldString("paper_vers_tx",
				(addMode ? "" : db.getText("paper_vers_tx")), 8, 8));

		/*
		 * blobs
		 */

		ht.put("notes_blob", new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 4, 80));

		ht.put("build_blob", new WebFieldText("build_blob", addMode ? "" : db
				.getText("build_blob"), 4, 80));

		ht.put("validated_blob", new WebFieldText("validated_blob",
				addMode ? "" : db.getText("validated_blob"), 4, 80));

		/*
		 * Remedy Fields
		 */

		ht.put("resolution_cd", new WebFieldDisplay("resolution_cd", addMode ? ""
				: db.getText("remedyResolve")));
		
		ht.put("closure_cd", new WebFieldDisplay("closure_cd", addMode ? ""
				: db.getText("remedyClosure")));
		
		ht.put("owner_uid", new WebFieldDisplay("owner_uid", addMode ? "" : db
				.getText("remedyOwner")));

		ht.put("title_nm", new WebFieldDisplay("title_nm", (addMode ? "" : db
				.getText("title_nm"))));

		ht.put("remedy_comment_blob", new WebFieldDisplay(
				"remedy_comment_blob", (addMode ? "" : db
						.getText("remedy_comment_blob"))));

		ht
				.put("description_blob", new WebFieldDisplay(
						"description_blob", addMode ? "" : "<br>----<br>"
								+ db.getText("description_blob")));

		ht.put("remedy_requested_completion_dt", new WebFieldDisplay(
				"remedy_requested_completion_dt", addMode ? "" : db
						.getText("fmt_remedy_requested_completion_dt")));

		ht.put("remedy_pending_cd", new WebFieldDisplay("remedy_pending_cd",
				addMode ? "" : db.getText("remedy_pending_cd")));

		ht.put("remedy_create_dt", new WebFieldDisplay("remedy_create_dt",
				addMode ? "" : db.getText("remedy_create_dt")));

		ht.put("remedy_start_dt", new WebFieldDisplay("remedy_start_dt",
				addMode ? "" : db.getText("remedy_start_dt")));

		ht.put("remedy_end_dt", new WebFieldDisplay("remedy_end_dt",
				addMode ? "" : db.getText("fmt_remedy_end_dt")));

		ht.put("remedy_act_end_dt", new WebFieldDisplay("remedy_act_end_dt",
				addMode ? "" : db.getText("remedy_act_end_dt")));

		ht.put("urgency_cd", new WebFieldDisplay("urgency_cd", (addMode ? ""
				: db.getText("remedy_urgency"))));

		ht.put("remedy_asof_date", new WebFieldDisplay("remedy_asof_date",
				(addMode ? "" : db.getText("remedy_asof_date"))));

		ht.put("remedy_effort_tx", new WebFieldDisplay("remedy_effort_tx",
				(addMode ? "" : db.getText("remedy_effort_tx"))));

		ht.put("requester_uid", new WebFieldDisplay("requester_uid",
				addMode ? "" : db.getText("remedyRequester")));

		ht.put("worklog_blob", new WebFieldDisplay("worklog_blob", addMode ? ""
				: "<br>----<br>" + db.getText("worklog_blob")));

		return ht;

	}

	public String makeExcelFile() {

		ExcelWriter excel = new ExcelWriter();

		String templateName = "iptracker.xls";
		String templatePath = sm.getWebRoot() + "excel/" + templateName;
		String filePrefix = sm.getLastName().replace(" ", "") + "_IP_"
				+ this.sr_type + "_Track";
		int columns = 54;
		short startRow = 1;

		return excel.appendWorkbook(sm.getExcelPath(), templatePath,
				filePrefix, getExcelResultSet(), startRow, columns);

	}

	public ResultSet getExcelResultSet() {

		StringBuffer sb = new StringBuffer();

		sb.append("SELECT * FROM vip_tracker_excel WHERE 1=1 ");

		if (this.sr_type.equals("OS")) {
			sb.append(" AND CHG_TYPE = 'OS' ");
		}

		// default status to open if no filter present

		if (sm.Parm("FilterStatus").length() == 0
				|| sm.Parm("FilterStatus").equalsIgnoreCase("ALLOPE")) {

			sb
					.append(" AND "
							+ isNull
							+ "(status_cd,'') in ('', 'NEW','ASS','WOR','PLA', 'SCH', 'PEN') ");

		} else {

			if (sm.Parm("FilterStatus").equalsIgnoreCase("ALLCLS")) {
				sb.append(" AND status_cd in ('CLO','RES') ");
			} else {
				if (!sm.Parm("FilterStatus").equalsIgnoreCase("0")) {
					sb.append(" AND status_cd = '" + sm.Parm("FilterStatus")
							+ "'");
				}
			}
		}

		filterMultiSelect(sb, "FilterLevel", "level_cd");

		filterMultiSelect(sb, "FilterPriority", "priority_cd");

		filterMultiSelect(sb, "FilterSUIT", "suite_cd");

		// filter on owner
		if (sm.Parm("FilterOrderset").length() == 0) {
		} else {
			if (!sm.Parm("FilterOrderset").equalsIgnoreCase("0")) {
				sb
						.append(" and tracker_id in (select request_id from korder_set_link join korder_set on korder_set_link.order_set_id = korder_set.order_set_id and korder_set.order_set_id = "
								+ sm.Parm("FilterOrderset") + ")");
			}
		}

		/*
		 * tricky one... this compares the list of domain choices agains any one
		 * of the 8 domain codes
		 */
		String[] array = sm.ParmArray("FilterDomain");
		if (array != null) {
			if (array.length > 0) {
				String x = array[0];
				StringBuffer whereList = new StringBuffer();

				if (!x.equalsIgnoreCase("0") && !x.equalsIgnoreCase("")) {

					whereList.append(" in ('" + array[0] + "'");
					for (int i = 1; i < array.length; i++) {
						whereList.append(",'" + array[i] + "'");
					}

					sb.append(" AND ((domain_1_cd " + whereList + "))");
					sb.append(" OR (domain_2_cd " + whereList + ("))"));
					sb.append(" OR (domain_3_cd " + whereList + ("))"));
					sb.append(" OR (domain_4_cd " + whereList + ("))"));
					sb.append(" OR (domain_5_cd " + whereList + ("))"));
					sb.append(" OR (domain_6_cd " + whereList + ("))"));
					sb.append(" OR (domain_7_cd " + whereList + ("))"));
					sb.append(" OR (domain_8_cd " + whereList + (")))"));
					whereList.append(")");
				}
			}
		}

		// cannot ORDER_BY because the comments column causes the recordset to
		// go over 8k
		// and that causes an SQL exception.
		// leave out until new version of
		// KEYWORD SQL SERVER 2000
		// / sb.append(" ORDER BY sr_no");

		// debug("iptracker excel sql : " + sb.toString());

		ResultSet rs = null;

		try {
			rs = db.getRS(sb.toString());
		} catch (services.ServicesException e) {
			debug("Excel RS Fetch Error : " + e.toString());
		}

		return rs;

	}

	/*
	 * to do.. move this into plugin or html helper
	 */
	private void filterMultiSelect(StringBuffer sb, String filterName,
			String columnName) {
		String[] array = sm.ParmArray(filterName);
		if (array != null) {
			if (array.length > 0) {
				String x = array[0];
				if (!x.equalsIgnoreCase("0") && !x.equalsIgnoreCase("")) {
					sb.append(" AND " + columnName + " in ('" + array[0] + "'");
					for (int i = 1; i < array.length; i++) {
						sb.append(",'" + array[i] + "'");
					}
					sb.append(")");
				}
			}
		}
	}

	public void beforeUpdate(Hashtable<String, DbField> ht) {

		// get the requested date.. it drives many of the others

		System.out.println("iptrackerPlging - before update");

		Date meeting = new Date();

		try {
			DbFieldDate epic_dt = (DbFieldDate) ht.get("hcg_meet_date");
			meeting = (Date) epic_dt.fieldValue;

		} catch (Exception e) {
			// debug("Exception getting hcg meeting date.. stopping auto
			// populate : "
			// + e.toString());
			return;
		}

		// debug (" debug meeting " + meeting.toString());
		// Medical Center Review - 3rd wed or prior month

		try {

			Date date = prior3rdWednesday(meeting);

			try {
				ht.remove("med_ctr_rv_date");
			} catch (Exception e) {
				// debug("error removing med center review date");
			}
			ht.put("med_ctr_rv_date", new DbFieldDate("med_ctr_rv_date", date));

		} catch (Exception e) {
			// debug("Exception updating med_ctr_rv_date : " + e.toString());
		}

		// Collate - 1st wed or this month 1/3/09 remove as per sr 03205810

		if (false) {

			try {

				Date date = firstWednesdayMonth(meeting);

				try {
					ht.remove("collate_rc_date");
				} catch (Exception e) {
					// debug("error removing med center review date");
				}
				ht.put("collate_rc_date", new DbFieldDate("collate_rc_date",
						date));

			} catch (Exception e) {
				// debug("Exception updating collate_rc_date: " + e.toString());
			}
		}

		// Screen Shoot - prior week plus 2

		try {

			Date date = priorWeekPlus2days(meeting);

			try {
				ht.remove("screen_shot_deadline_date");
			} catch (Exception e) {
				// debug("error removing screen_shot_date");
			}
			ht.put("screen_shot_deadline_date", new DbFieldDate(
					"screen_shot_deadline_date", date));

		} catch (Exception e) {
			// debug("Exception updating collate_rc_date: " + e.toString());
		}

		// Final Resolve - Next Tuesday

		try {

			Date date = nextFriday(meeting);

			try {
				ht.remove("document_date");
			} catch (Exception e) {
				// debug("error removing document_date");
			}
			ht.put("document_date", new DbFieldDate("document_date", date));

		} catch (Exception e) {
			// debug("Exception updating collate_rc_date: " + e.toString());
		}

		// Advance dist. Date - prior wed

		try {

			Date date = priorWednesday(meeting);

			try {
				ht.remove("adv_dist_hcg_date");
			} catch (Exception e) {
				// debug("error removing adv_dist_hcg_date");
			}
			ht.put("adv_dist_hcg_date", new DbFieldDate("adv_dist_hcg_date",
					date));

		} catch (Exception e) {
			// debug("Exception updating adv_dist_hcg_date: " + e.toString());
		}

		// Medical Center Comm - next wed

		try {
			Date date = nextWednesday(meeting);
			try {
				ht.remove("med_exec_comm_date");
			} catch (Exception e) {
				// debug("error removing med_exec_comm_date");
			}
			ht.put("med_exec_comm_date", new DbFieldDate("med_exec_comm_date",
					date));
		} catch (Exception e) {
			// debug("Exception updating med_exec_comm_date: " + e.toString());
		}

		// Release Date = meeting + 1 week

		try {
			Date date = addDays(meeting, 7);
			try {
				ht.remove("rlse_notify_date");
			} catch (Exception e) {
				// debug("error removing med_exec_comm_date");
			}
			ht.put("rlse_notify_date",
					new DbFieldDate("rlse_notify_date", date));
		} catch (Exception e) {
			// debug("Exception updating med_exec_comm_date: " + e.toString());
		}

		// Release Date = meeting + 1 week

		try {
			Date date = nextMonday(meeting);
			try {
				ht.remove("document_date");
			} catch (Exception e) {
				// debug("error removing med_exec_comm_date");
			}
			ht.put("document_date", new DbFieldDate("document_date", date));
		} catch (Exception e) {
			// debug("Exception updating med_exec_comm_date: " + e.toString());
		}

		System.out.println("iptrackerPlging - before update - done");
	}

	private Date addDays(Date date, int days) {
		Calendar c = getCalendar(date);
		c.add(Calendar.DATE, days);
		return c.getTime();
	}

	private Date prior3rdWednesday(Date today) {

		Calendar c = getCalendar(today);
		int day = c.get(Calendar.DAY_OF_MONTH);

		c.add(Calendar.MONTH, -1);

		c.add(Calendar.DATE, -(day - 1));

		int dayofweek = c.get(Calendar.DAY_OF_WEEK);

		switch (dayofweek) {
		case 1:
			c.add(Calendar.DATE, 17);
			break;
		case 2:
			c.add(Calendar.DATE, 16);
			break;
		case 3:
			c.add(Calendar.DATE, 15);
			break;
		case 4:
			c.add(Calendar.DATE, 14);
			break;
		case 5:
			c.add(Calendar.DATE, 20);
			break;
		case 6:
			c.add(Calendar.DATE, 19);
			break;
		case 7:
			c.add(Calendar.DATE, 18);
			break;
		}

		return c.getTime();

	}

	private Date firstWednesdayMonth(Date today) {

		Calendar c = getCalendar(today);
		int day = c.get(Calendar.DAY_OF_MONTH);

		// c.add(Calendar.MONTH, -1);

		c.add(Calendar.DATE, -(day - 1));

		int dayofweek = c.get(Calendar.DAY_OF_WEEK);

		switch (dayofweek) {
		case 1:
			c.add(Calendar.DATE, 3);
			break;
		case 2:
			c.add(Calendar.DATE, 2);
			break;
		case 3:
			c.add(Calendar.DATE, 1);
			break;
		case 4:
			c.add(Calendar.DATE, 0);
			break;
		case 5:
			c.add(Calendar.DATE, 6);
			break;
		case 6:
			c.add(Calendar.DATE, 5);
			break;
		case 7:
			c.add(Calendar.DATE, 4);
			break;
		}

		return c.getTime();

	}

	private Date priorWeekPlus2days(Date today) {

		Calendar c = getCalendar(today);
		int day = c.get(Calendar.DAY_OF_MONTH);

		c.add(Calendar.DATE, -7);

		int dayofweek = c.get(Calendar.DAY_OF_WEEK);

		switch (dayofweek) {

		case 2:
			c.add(Calendar.DATE, -4);
			break;
		case 3:
			c.add(Calendar.DATE, -4);
			break;
		case 4:
			c.add(Calendar.DATE, -2);
			break;
		case 5:
			c.add(Calendar.DATE, -2);
			break;
		case 6:
			c.add(Calendar.DATE, -2);
			break;
		case 7:
			c.add(Calendar.DATE, -3);
			break;
		}

		return c.getTime();

	}

	private Date nextTuesday(Date today) {

		Calendar c = getCalendar(today);
		int day = c.get(Calendar.DAY_OF_MONTH);

		int dayofweek = c.get(Calendar.DAY_OF_WEEK);

		switch (dayofweek) {
		case 1:
			c.add(Calendar.DATE, 2);
			break;
		case 2:
			c.add(Calendar.DATE, 1);
			break;
		case 3:
			c.add(Calendar.DATE, 7);
			break;
		case 4:
			c.add(Calendar.DATE, 6);
			break;
		case 5:
			c.add(Calendar.DATE, 5);
			break;
		case 6:
			c.add(Calendar.DATE, 4);
			break;
		case 7:
			c.add(Calendar.DATE, 3);
			break;
		}

		return c.getTime();

	}

	private Date nextFriday(Date today) {

		Calendar c = getCalendar(today);
		int day = c.get(Calendar.DAY_OF_MONTH);

		int dayofweek = c.get(Calendar.DAY_OF_WEEK);

		// debug("find friday...day of week : " + day + " day of week : "
		// + dayofweek);

		switch (dayofweek) {
		case 1:
			c.add(Calendar.DATE, 5);
			break;
		case 2:
			c.add(Calendar.DATE, 4);
			break;
		case 3:
			c.add(Calendar.DATE, 3);
			break;
		case 4:
			c.add(Calendar.DATE, 2);
			break;
		case 5:
			c.add(Calendar.DATE, 1);
			break;
		case 6:
			c.add(Calendar.DATE, 7);
			break;
		case 7:
			c.add(Calendar.DATE, 6);
			break;
		}

		return c.getTime();

	}

	private Date nextMonday(Date today) {

		Calendar c = getCalendar(today);
		int day = c.get(Calendar.DAY_OF_MONTH);

		int dayofweek = c.get(Calendar.DAY_OF_WEEK);

		switch (dayofweek) {
		case 1:
			c.add(Calendar.DATE, 1);
			break;
		case 2:
			c.add(Calendar.DATE, 7);
			break;
		case 3:
			c.add(Calendar.DATE, 6);
			break;
		case 4:
			c.add(Calendar.DATE, 5);
			break;
		case 5:
			c.add(Calendar.DATE, 4);
			break;
		case 6:
			c.add(Calendar.DATE, 3);
			break;
		case 7:
			c.add(Calendar.DATE, 2);
			break;
		}

		return c.getTime();

	}

	private Date nextWednesday(Date today) {

		Calendar c = getCalendar(today);
		int day = c.get(Calendar.DAY_OF_MONTH);

		int dayofweek = c.get(Calendar.DAY_OF_WEEK);

		switch (dayofweek) {
		case 1:
			c.add(Calendar.DATE, 3);
			break;
		case 2:
			c.add(Calendar.DATE, 2);
			break;
		case 3:
			c.add(Calendar.DATE, 1);
			break;
		case 4:
			c.add(Calendar.DATE, 7);
			break;
		case 5:
			c.add(Calendar.DATE, 6);
			break;
		case 6:
			c.add(Calendar.DATE, 5);
			break;
		case 7:
			c.add(Calendar.DATE, 4);
			break;
		}

		return c.getTime();

	}

	private Date priorWednesday(Date today) {

		Calendar c = getCalendar(today);

		int dayofweek = c.get(Calendar.DAY_OF_WEEK);

		switch (dayofweek) {
		case 1:
			c.add(Calendar.DATE, -4);
			break;
		case 2:
			c.add(Calendar.DATE, -5);
			break;
		case 3:
			c.add(Calendar.DATE, -6);
			break;
		case 4:
			c.add(Calendar.DATE, -7);
			break;
		case 5:
			c.add(Calendar.DATE, -1);
			break;
		case 6:
			c.add(Calendar.DATE, -2);
			break;
		case 7:
			c.add(Calendar.DATE, -3);
			break;
		}

		return c.getTime();

	}

	private Calendar getCalendar(Date d) {

		Calendar cal = Calendar.getInstance();
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		cal.setTime(d);
		return cal;
	}

}
