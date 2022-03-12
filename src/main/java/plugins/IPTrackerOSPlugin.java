package plugins;

import java.util.Hashtable;

import db.DbFieldString;

public class IPTrackerOSPlugin  extends IPTrackerPlugin {
	
	
	public IPTrackerOSPlugin() throws services.ServicesException {
		super();

		setHasDetailForm (true);
		setTargetTitle ("Order Set Tracker");
		setDetailTarget("OrderSetLink");
		setDetailTargetLabel("Order&nbsp;Sets");


		// of abstract IPTrackerPlugin
		
		this.sr_type = "OS";
		this.detail_page_nm = "Order Set Tracker";
		
	}
			
}
