package plugins;

public class IPTrackerSRPlugin extends IPTrackerPlugin {

	public IPTrackerSRPlugin() throws services.ServicesException {
		super();

		this.sr_type = "SR";
		this.setTargetTitle("IP Service Request Tracker");
		this.detail_page_nm = "IP Service Request Tracker";
		
	}

}
