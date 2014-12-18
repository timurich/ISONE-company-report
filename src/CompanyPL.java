import java.util.*;
public class CompanyPL 
{
	private String Company;
	private ArrayList<ftrPath> paths;
	private double MWs;
	//private double price;
	private double moneySpent;
	private double dailyPL[];
	private int daysCalculated;
	
	public CompanyPL(String company) 
	{	
		Company = company;
		paths = new ArrayList<ftrPath>();
		MWs = 0;
		//this.price = price;
		moneySpent = 0;
		dailyPL = new double [31];
		daysCalculated = 0;
	}

	public void addDailyPL(double newPL) {
		dailyPL[daysCalculated] = newPL;
		daysCalculated++;
	}
	
	public void setMoneySpent(){
		int sum = 0;
		for(int i=0; i<dailyPL.length; i++) {
			sum+=dailyPL[i];
		}
		moneySpent = sum;
	}	
	public String getCompany() {
		return Company;
	}
	public void setCompany(String company) {
		Company = company;
	}
	public ArrayList<ftrPath> getPaths() {
		return paths;
	}
	public void addPath(ftrPath path) {
		paths.add(path);
	}
	public double getMWs() {
		return MWs;
	}
	public void addMWs(double mWs) {
		MWs+=mWs;
	}
//	public double getPrice() {
//		return price;
//	}
//	public void setPrice(double price) {
//		this.price = price;
//	}		
	public double getMoneySpent() {
		return moneySpent;
	}
}
