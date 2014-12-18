
public class ftrAuctionResFile {
	private String name;
	private String year;
	private String month;
	
	public ftrAuctionResFile(String name, String year, String month) {	
		this.name = name;
		this.year = year;
		this.month = month;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}	
}
