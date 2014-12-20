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
		
		String requestedFtrFile = "";
		ArrayList <ftrAuctionResFile> files = getAllFTRAuctionFiles();
		for(int i = 0; i < files.size(); i++) {
			if(files.get(i).getYear().equals(year) && files.get(i).getMonth().equals(month))
				requestedFtrFile = files.get(i).getName();
		}
		System.out.println();
		System.out.println("FTR Auction Result File : "+requestedFtrFile);
		///////////////////Reading all paths for given month		
		Map <ftrPath, ArrayList<Double>> paths = CreateListOfAllPathsFor1month(requestedFtrFile); // set of All unique paths for the month (FTR)		
		System.out.println("\nNumber of unique paths for the month = "+paths.size());
		
		System.out.println("=========================================================");
		CalculatePL_of_AllPathsForGivenMonth(year, month, paths);
		System.out.println("=========================================================");		
		int temp = 1;		
		for(ftrPath key: paths.keySet()) {
			if(key.getPathTotalPL()>50 && key.getPathTotalPL() / key.getFtrPrice() > 1.5){
				System.out.println(key.toString()+", "+ key.getAllDaysPL()+"\t = "+ key.getPathTotalPL());
				temp++;
			}
						
		}
		System.out.println("Paths found: "+ temp);
		System.out.print("Report of all companies' P/L for this month? [y]es/[n]o");
		char ans = in.nextLine().charAt(0);
		if(ans == 'Y' || ans == 'y')
			MonthlyReport_CompaniesPL(requestedFtrFile, paths);
		else
			System.out.println("End of program.");
		
		in.close();//System.in
	}
	
	public static void MonthlyReport_CompaniesPL(String file, Map <ftrPath, ArrayList<Double>> paths){
		String ftrFile = "C:/ISONE/FTR_Positions/FTR_Bid_Results/"+file;		
		BufferedReader br = null;
		String line = "";		
		try {			 
			br = new BufferedReader(new FileReader(ftrFile));			
			while ((line = br.readLine()) != null) {	 
			        // use comma as separator
				String[] transaction  = line.split(",");	 
				if(transaction[8].equals("BUY")) {             // Map keys can only be unique, so the paths will be unique
					paths.put(new ftrPath(transaction[3],transaction[5],transaction[7],Double.parseDouble(transaction[10])),null);
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
	
	public static Map <ftrPath, ArrayList<Double>> CreateListOfAllPathsFor1month(String file) {	
		Map <ftrPath,ArrayList<Double>> paths = new HashMap<ftrPath, ArrayList<Double>>();
		String csvFile = "C:/ISONE/FTR_Positions/FTR_Bid_Results/"+file;		
		BufferedReader br = null;
		String line = "";		
		try {
			 
			br = new BufferedReader(new FileReader(csvFile));			
			while ((line = br.readLine()) != null) {	 
			        // use comma as separator
				String[] transaction  = line.split(",");	 
				if(transaction[8].equals("BUY")) {             // Map keys can only be unique, so the paths will be unique
					paths.put(new ftrPath(transaction[3],transaction[5],transaction[7],Double.parseDouble(transaction[10])),null);
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

	public static void CalculatePL_of_AllPathsForGivenMonth(String year, String month, Map<ftrPath, ArrayList<Double>> paths) {			
			
		Path dir = Paths.get("/ISONE/ISONE_DA_RT_FTR_Prices/Daily_DA_Prices");
		String fileName = "";		
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "WW_DALMP_ISO_"+year+month+"*")) {
			int zzz = 0;
			for (Path entry: stream) {				
		        fileName = entry.getFileName().toString();
		        zzz++;
		        System.out.print(fileName+"  ");
		        if(zzz == 5) {System.out.println(); zzz=0;}
		        getAllPathPricesForDay(fileName, paths);
		    }
		} catch (IOException x) {
		    System.err.println(x);
		}		
		System.out.println("\n");
	}
	
	public static void getAllPathPricesForDay(String fileName, Map<ftrPath, ArrayList<Double>> paths) throws IOException{		
		String fName = "C:/ISONE/ISONE_DA_RT_FTR_Prices/Daily_DA_Prices/"+fileName;
        int year = Integer.parseInt(fileName.substring(13,17));
        int month = Integer.parseInt(fileName.substring(17,19));
        int day = Integer.parseInt(fileName.substring(19,21));        
		   ///// getting all paths prices for the day
		Map<String, double[]> nodes_prices = new HashMap<String, double[]>();  // Map of all NODES with their prices for a day
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
				nodes_prices.put(splittedLine[27],prices);			
			}
		}
		inStream.close();	

		in.close();
				
		int xxx = 0;		
		
		for(ftrPath key: paths.keySet()) 
		{
			String source = key.getSource();
			String sink = key.getSink();
			String type = key.getType();
			
			if(nodes_prices.get(source)==null) {
				//System.out.println(pathsPL[p].getPath().getSource()+" not found !!!!!!!!!!!!!!!!!!");
				key.addDailyPL(0.0);
				xxx = xxx + 1;
				continue;
			}
			if(nodes_prices.get(sink)==null) {
				//System.out.println(pathsPL[p].getPath().getSink()+" not found !!!!!!!!!!!!!!!!!!");
				key.addDailyPL(0.0);
				xxx = xxx + 1;
				continue;
			}
					
			boolean NERCHoliday = Util.NERCHolidaysQ(year,month,day);
			boolean weekend = Util.WeekendQ(year,month,day);
			
			double sum = 0;
			if(type.equals("ONPEAK") && NERCHoliday == false && weekend == false) {
				//System.out.println("                        Match 1");
				for(int i=7; i<=22; i++) {    // OnPeak // no holiday (hours: 8 - 23) 
					sum += nodes_prices.get(sink)[i] - nodes_prices.get(source)[i];
				}
			}
			else if(type.equals("ONPEAK") && (NERCHoliday == true || weekend == true)) {
				//System.out.println("                        Match 2");
				sum = 0;  // no PL for OnPeak paths during weekends and NERD holidays
			}
			else if(type.equals("OFFPEAK") && NERCHoliday == false && weekend == false) {
				//System.out.println("                        Match 3");
				for(int i=0; i<=6; i++) { // OffPeak during week days and no holidays
					sum += nodes_prices.get(sink)[i] - nodes_prices.get(source)[i];					
				}								
				sum += nodes_prices.get(sink)[23] - nodes_prices.get(source)[23]; // 24th hour
			}
			else if(type.equals("OFFPEAK") && (NERCHoliday == true || weekend == true)) {
				//System.out.println("                        Match 4");
				for(int i=0; i<=23; i++) { // OffPeak during holidays and weekends (hours: 1 - 24) 
					sum += nodes_prices.get(sink)[i] - nodes_prices.get(source)[i];
				}
			}
			sum = Math.round(sum*100)/100.0;		
			//System.out.println(sum);
			key.addDailyPL(sum); 
		}
		//System.out.println("name mismatches: "+xxx);
		//System.out.println("============================================");
	}
}
