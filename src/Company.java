import java.util.*;
public class Company 
{
	private String name;
	private ArrayList<ftrPath> paths;
	private double totalMWs;	
	private double moneySpent;	
	private double totalPL;	
	private ArrayList<Double> dailyPLs;
	
	public Company(String company) 
	{	
		name = company;
		paths = new ArrayList<ftrPath>();
		totalMWs = 0;
		totalPL = 0;		
		moneySpent = 0;	
		dailyPLs = new ArrayList<Double>();
	}
		
	public String getName() {
		return name;
	}	
	public ArrayList<ftrPath> getPaths() {
		return paths;
	}
	public void addPath(ftrPath path) {
		paths.add(path);
		dailyPLs.add(path.getPathTotalPL());
	}
	public double getMWs() {
		return Math.round(totalMWs*100)/100.0;
	}
	public void addMWs(double mWs) {
		totalMWs += mWs;
	}
	public void addMoneySpent(double money) {
		moneySpent += money;
	}	
	public double getMoneySpent() {
		return Math.round(moneySpent*100)/100.0;
	}
	public void addTotalPL(ArrayList <Double> dailyPathPLs, double MWs) {
		double sum = 0;
		for(int i = 0; i < dailyPathPLs.size(); i++)
			sum += (double)dailyPathPLs.get(i);		
		totalPL += sum * MWs;		
	}
	public double getTotalPL() {
		return Math.round(totalPL*100)/100.0;
	}
	public ArrayList<Double> getDailyPLs() {
		return dailyPLs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Company other = (Company) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
