// Monthly DA PL of each path
public class PathPL {
	private ftrPath path;
	private double dailyPL[];
	private int daysCalculated;
	
	public PathPL(ftrPath path) {		
		this.path = path;
		dailyPL = new double [31];
		daysCalculated = 0;		
	}
	public void addDailyPL(double newPL) {
		dailyPL[daysCalculated] = newPL;
		daysCalculated++;
	}
	public double getDailyPL(int index) {
		return dailyPL[index];
	}
	public double getPathTotalPL() {
		double sum = 0;
		for(int i=0; i<daysCalculated; i++){
			sum+=dailyPL[i];
		}
		return sum;
	}
	public ftrPath getPath() {
		return path;
	}
	
	//public double calculatePLforADay()
	
}
