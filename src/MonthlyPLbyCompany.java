/**
 * Calculate monthly PL for each market participant in ISO-NE
 * @author Timur
 * Version 0.1
 */

//import java.io.FileWriter;
import java.io.*;
import java.util.*;
import java.nio.file.*;

public class MonthlyPLbyCompany {

	public static void main(String[] args) throws IOException 
	{
		System.out.println("Report: Market Participants PL For a Certain Month.");
		System.out.println();
		Scanner in = new Scanner(System.in);
		String year;
		String month;
		boolean right;
		do {
			System.out.print("Input year [2011 - 2014]: ");
			year = in.nextLine();
			int y = Integer.parseInt(year);			
			if(y < 2011 || y > 2014) {
				System.out.println("Wrong year! Please re-input.\n");
				right = false;
			} else right = true;
		} while(!right);
		System.out.println();
		do {
			System.out.print("Input month [1 - 12]: ");
			month = in.nextLine();
			int m = Integer.parseInt(month);
			if(m < 1 || m > 12) {
				System.out.println("Wrong month! Please re-input.\n");
				right = false;				
			} else right = true;
		} while(!right);
		if(month.length() < 2) month = "0"+month;
		in.close();
		String requestedFile = "";
		ArrayList <ftrAuctionResFile> files = getAllFTRAuctionFiles();
		for(int i = 0; i < files.size(); i++) {
			if(files.get(i).getYear().equals(year) && files.get(i).getMonth().equals(month))
				requestedFile = files.get(i).getName();
		}
		System.out.println("===> "+requestedFile);
		///////////////////Reading all paths for given month		
		Set <ftrPath> paths = new HashSet<ftrPath>();
		paths = CreateListOfAllPathsFor1month(requestedFile); // set of All unique paths for the month (FTR)		
		PathPL [] pathsPL = new PathPL[paths.size()];
		Iterator <ftrPath> setAccess = paths.iterator();
		for(int i = 0; i < pathsPL.length; i++) {
			pathsPL[i] = new PathPL(setAccess.next());       // array of All unique purchased paths for given month
		}
		System.out.println("!!!!! number of unique paths for the month = "+pathsPL.length);
		//System.out.println(pathsPL[0].getPath().getSource()+" --> "+pathsPL[0].getPath().getSink()+" | "+pathsPL[0].getPath().getType()+" | "+
		//					pathsPL[0].getDailyPL(0)+", total PL = "+pathsPL[0].getPathTotalPL());
		
		System.out.println("=========================================================");
		CalculatePL_of_AllPathsForGivenMonth(year, month, pathsPL);
		//System.out.println("=========================================================");
		//System.out.println(pathsPL[0].)
		
	}

	public static ArrayList <ftrAuctionResFile> getAllFTRAuctionFiles() {
		Path dir = Paths.get("/ISONE/FTR_Positions/FTR_Bid_Results");
		String fileName = "";
		String year;
		String month;
		ArrayList <ftrAuctionResFile> files = new ArrayList <ftrAuctionResFile>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "Monthly_*")) {		    
			for (Path entry: stream) {
				
		        fileName = entry.getFileName().toString();
		        year = fileName.substring(8,12);
		        month = fileName.substring(13,15);
		        files.add(new ftrAuctionResFile(fileName, year, month));		        
		    }
		} catch (IOException x) {
		    System.err.println(x);
		}		
		
		//for(int i = 0; i < files.size(); i++)
		//	System.out.println(files.get(i).getName()+" -- "+files.get(i).getYear()+" -- "+files.get(i).getMonth());
		return files;
	}
	
	public static Set <ftrPath> CreateListOfAllPathsFor1month(String file) {	
		Set <ftrPath> paths = new HashSet<ftrPath>();
		String csvFile = "C:/ISONE/FTR_Positions/FTR_Bid_Results/"+file;		
		BufferedReader br = null;
		String line = "";		
		try {
			 
			br = new BufferedReader(new FileReader(csvFile));			
			while ((line = br.readLine()) != null) {	 
			        // use comma as separator
				String[] transaction  = line.split(",");	 
				if(transaction[8].equals("BUY")) {
					paths.add(new ftrPath(transaction[2],transaction[4],transaction[7],Double.parseDouble(transaction[10])));
				}
			}	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}	 
		return paths;
	}

	public static void CalculatePL_of_AllPathsForGivenMonth(String year, String month, PathPL pathsPL[]) {			
			
		Path dir = Paths.get("/ISONE/ISONE_DA_RT_FTR_Prices/Daily_DA_Prices");
		String fileName = "";		
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "WW_DALMP_ISO_"+year+month+"*")) {
			//int zzz = 1;
			for (Path entry: stream) {				
		        fileName = entry.getFileName().toString();		        
		        System.out.println(fileName);
		        getAllPathPricesForDay(fileName, pathsPL);
		       //zzz++;
		       //if(zzz>=1)break;//////////////////////////////////////////////////////////
		    }
		} catch (IOException x) {
		    System.err.println(x);
		}		
	}
	
	public static void getAllPathPricesForDay(String fileName, PathPL pathsPL[]) throws IOException{		
		String fName = "C:/ISONE/ISONE_DA_RT_FTR_Prices/Daily_DA_Prices/"+fileName;
        int year = Integer.parseInt(fileName.substring(13,17));
        int month = Integer.parseInt(fileName.substring(17,19));
        int day = Integer.parseInt(fileName.substring(19,21));        
		   ///// getting all paths prices for the day
		Map<String, double[]> paths_prices = new HashMap<String, double[]>();
		Scanner in = new Scanner(System.in);
		BufferedReader inStream = new BufferedReader(new FileReader(fName));
		String line = "";		
		int skip = 1;
		while((line = inStream.readLine()) != null){
			String [] splittedLine = null;
			double prices [] = new double[24];
			if(skip <= 5) {skip++; continue;}
			splittedLine = line.split(",");			
			if(splittedLine[2].equals("MCC")){
				for(int i=3, k=0; i<=26; i++, k++)
					prices[k] = Double.parseDouble(splittedLine[i]);
				paths_prices.put(splittedLine[0],prices);
			//	System.out.println(splittedLine[0]+" : "+Arrays.toString(paths_prices.get(splittedLine[0])));
			//	in.nextLine();
			}
		}
		inStream.close();
		
//		Set<String> keys = paths_prices.keySet();		
//		double[] value = null;
//		for(Iterator<String> z = keys.iterator(); z.hasNext();) {
//			String key = (String) z.next();
//			value = (double[])paths_prices.get(key);
//			//System.out.println(key+" : "+Arrays.toString(value));
//			//in.nextLine();
//		}
		in.close();
				
		int xxx = 0;
		for(int p=0; p < pathsPL.length; p++) {
			if(paths_prices.get(pathsPL[p].getPath().getSource())==null) {
				System.out.println(pathsPL[p].getPath().getSource()+" not found !!!!!!!!!!!!!!!!!!");
				pathsPL[p].addDailyPL(0.0);
				xxx++;
				continue;
			}
			if(paths_prices.get(pathsPL[p].getPath().getSink())==null) {
				System.out.println(pathsPL[p].getPath().getSink()+" not found !!!!!!!!!!!!!!!!!!");
				pathsPL[p].addDailyPL(0.0);
				xxx++;
				continue;
			}
			/////System.out.print(pathsPL[p].getPath().getSource()+" ===> "+pathsPL[p].getPath().getSink()+" : sum = ");
			//System.out.println(pathsPL[0].getPath().getSource()+" : "+ Arrays.toString(paths_prices.get(pathsPL[0].getPath().getSource())));
			//System.out.println(pathsPL[0].getPath().getSink()+" : "+ Arrays.toString(paths_prices.get(pathsPL[0].getPath().getSink())));
			//System.out.println(pathsPL[0].getPath().getType());
			
					
			boolean NERCHoliday = NERCHolidaysQ(year,month,day);
			boolean weekend = WeekendQ(year,month,day);
			
			double sum = 0;
			if(pathsPL[p].getPath().getType().equals("ONPEAK") && NERCHoliday == false && weekend == false) {
				//System.out.println("                        Match 1");
				for(int i=7; i<=22; i++) { // OnPeak // no holiday (hours: 8 - 23) 
					sum += paths_prices.get(pathsPL[p].getPath().getSink())[i] - paths_prices.get(pathsPL[p].getPath().getSource())[i];
				}
			}
			else if(pathsPL[p].getPath().getType().equals("ONPEAK") && (NERCHoliday == true || weekend == true)){
				//System.out.println("                        Match 2");
				sum = 0;  // no PL for OnPeak paths during weekends and NERD holidays
			}
			else if(pathsPL[p].getPath().getType().equals("OFFPEAK") && NERCHoliday == false && weekend == false){
				//System.out.println("                        Match 3");
				for(int i=0; i<=6; i++) { // OffPeak during week days and no holidays
					sum += paths_prices.get(pathsPL[p].getPath().getSink())[i] - paths_prices.get(pathsPL[p].getPath().getSource())[i];
					sum += paths_prices.get(pathsPL[p].getPath().getSink())[23] - paths_prices.get(pathsPL[p].getPath().getSource())[23]; // 24th hour
				}								
			}
			else if(pathsPL[p].getPath().getType().equals("OFFPEAK") && (NERCHoliday == true || weekend == true)){
				//System.out.println("                        Match 4");
				for(int i=0; i<=23; i++) { // OffPeak during holidays and weekends (hours: 1 - 24) 
					sum += paths_prices.get(pathsPL[p].getPath().getSink())[i] - paths_prices.get(pathsPL[p].getPath().getSource())[i];
				}
			}
			sum = Math.round(sum*100)/100.0;		
			//System.out.println(sum);
			pathsPL[p].addDailyPL(sum);
			//if(p%500==0) System.out.println("                                         Paths calculated: "+p);
		}
		System.out.println("name mismatches: "+xxx);
		System.out.println("============================================");
	}
	
	public static void getAllCompaniesPL(){
		
	}
	
	public static boolean WeekendQ(int year, int month, int day) {		
		Calendar calendar = new GregorianCalendar(year, month-1, day);
		int dayOfWeek  = calendar.get(Calendar.DAY_OF_WEEK);
		if(dayOfWeek < 2 || dayOfWeek > 6)  // Saturday, Sunday
			return true;
		else
			return false;
			
	}
	
	public static boolean NERCHolidaysQ(int year, int month, int day) {					
		Calendar calendar = new GregorianCalendar(year, month-1, day);
		int dayOfWeek  = calendar.get(Calendar.DAY_OF_WEEK);
		int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);		
		if(month == 1 && day == 1)         // New Year
			return true;
		if(month == 7 && day == 4)		   // Independence Day
			return true;
		if(month == 12 && day == 25)	   // Christmas
			return true;
		if(month == 5 && dayOfMonth >= 25 && dayOfWeek == 2 ) // Memorial Day			
			return true;
		if(month == 9 && dayOfMonth <=7 && dayOfWeek == 2 )   // Labor Day			
			return true;
		if(month == 11 && dayOfMonth >=22 && dayOfWeek == 5 ) // Thanksgiving			
			return true;					
		return false;
	}
}
