/**
 * Calculate monthly PL for each market participant in ISO-NE
 * @author Timur
 * Version 0.1
 */

//import java.io.FileWriter;
import java.io.*;
import java.util.*;
import java.nio.file.*;

public class MonthlyPLbyCompany3 {

	public static void main(String[] args) throws IOException 
	{
		System.out.println("Report: Market Participants PL For a Certain Month.");
		System.out.println();
		@SuppressWarnings("resource")
		Scanner in = new Scanner(System.in);
		
		String year_start;
		String year_end;
		String month_start;
		String month_end;
		
		boolean right;
		do {
			System.out.print("Input starting year [2011 - 2014]: ");
			year_start = in.nextLine();
			int y = Integer.parseInt(year_start);			
			if(y < 2011 || y > 2014) {
				System.out.println("Wrong year! Please re-input.\n");
				right = false;
			} else right = true;
		} while(!right);
		System.out.println();
		
		do {
			System.out.print("Input ending year [2011 - 2014]: ");
			year_end = in.nextLine();
			int y = Integer.parseInt(year_end);			
			if(y < 2011 || y > 2014) {
				System.out.println("Wrong year! Please re-input.\n");
				right = false;
			} else right = true;
		} while(!right);
		System.out.println();

		do {
			System.out.print("Input starting month [1 - 12]: ");
			month_start = in.nextLine();
			int m = Integer.parseInt(month_start);
			if(m < 1 || m > 12) {
				System.out.println("Wrong month! Please re-input.\n");
				right = false;				
			} else right = true;
		} while(!right);
		if(month_start.length() < 2) month_start = "0"+month_start;
		System.out.println();

		do {
			System.out.print("Input ending month [1 - 12]: ");
			month_end = in.nextLine();
			int m = Integer.parseInt(month_end);
			if(m < 1 || m > 12) {
				System.out.println("Wrong month! Please re-input.\n");
				right = false;				
			} else right = true;
		} while(!right);
		if(month_end.length() < 2) month_end = "0"+month_end;
		System.out.println();
		
		String year = "2014"; ///////////////////////////
		String month = "12";  //////////////////////////
		
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
		int temp = 0;
		BufferedWriter bw = new BufferedWriter(new FileWriter("C:/ISONE/Monthly_Paths_PL.csv"));  
		bw.write("sourceID,sinkID,Class,Price,Total P/L for month,");
		int h = 0;
		for(h = 1; h <= Util.getNumberOfDaysInMonth(year,month)-1; h++) bw.write("Day"+h+","); bw.write("Day"+h); //to avoid last comma)		
		bw.newLine();
		for(ftrPath key: paths.keySet()) {
			if(key.getPathTotalPL()>50 && key.getPathTotalPL() / key.getFtrPrice() > 1.5) { // FILTER 
				//System.out.println(key.toString()+", "+ key.getAllDaysPLstring()+"\t = "+ key.getPathTotalPL());
			
				ArrayList<Double> dailyPLs = key.getAllDaysPL();			
				String writeString = "";
				writeString += key.getSource() +","+ key.getSink()+","+key.getType()+","+key.getFtrPrice()+","+key.getPathTotalPL()+",";
				for(int i = 0; i<dailyPLs.size(); i++)
					writeString += dailyPLs.get(i)+",";				
				bw.write(writeString.substring(0,writeString.length()-1));
				bw.newLine();
				temp++;
			}
						
		}
		bw.close();
		System.out.println("Matching paths found: "+ temp);
		System.out.println();
		System.out.print("Report of all companies' P/L for this month? [y]es/[n]o ==> ");		
		char ans = in.nextLine().charAt(0);
		if(ans == 'Y' || ans == 'y') 
		{
			Map<String, Company> companies = MonthlyReport_CompaniesPL(requestedFtrFile, paths);			
			int x = 0;
			BufferedWriter bw2 = new BufferedWriter(new FileWriter("C:/ISONE/Monthly_Companies_PL_Report.csv"));
			bw2.write("Company,# of paths,total MWs,Capital Spent,Total P/L");
			bw2.newLine();
			String writeString = "";
			for(String key: companies.keySet()) 
			{	
				writeString = "";
				System.out.println(++x+". "+key);
				writeString += key+",";
				ArrayList<ftrPath> companyPaths = companies.get(key).getPaths();
				System.out.println("\t\tNumber of paths: "+companyPaths.size());
				writeString += companyPaths.size()+",";
				System.out.println("\t\tTotal MWs purchased: "+companies.get(key).getMWs());
				writeString += companies.get(key).getMWs()+",";
				System.out.println("\t\tTotal money spent: $"+companies.get(key).getMoneySpent());
				writeString += companies.get(key).getMoneySpent()+",";
				System.out.println("\t\tTotal PL for the month: $"+companies.get(key).getTotalPL());
				writeString += companies.get(key).getTotalPL();
			//	writeString += companies.get(key).getDailyPLs().toString(); //ArrayList of PLs for the month
				bw2.write(writeString);
				bw2.newLine();
//				for(int i = 0; i < companyPaths.size(); i++)
//					System.out.println("\t\t"+companyPaths.get(i).toString());
//				if(x==5) break;
			}
			bw2.close();
		}
		else
			System.out.println("End of program.");
				
	}
	
	public static Map<String, Company> MonthlyReport_CompaniesPL(String file, Map <ftrPath, ArrayList<Double>> paths)
	{			
		String ftrFile = "C:/ISONE/FTR_Positions/FTR_Bid_Results/"+file;
		Map<String, Company> companies = new HashMap<String, Company>();
		BufferedReader br = null;		
		@SuppressWarnings({ "unused", "resource" })
		Scanner in = new Scanner(System.in);		
		int sameCompany = 1;
		String line = "";		
		try {			 
			br = new BufferedReader(new FileReader(ftrFile));
			br.readLine();//skipping first line that is just a header
			@SuppressWarnings("unused")
			Company prevComp = null; // will be used to move one step behind to finalize data for previous company when all its paths finished
			while ((line = br.readLine()) != null) 
			{	 
			        // use comma as separator
				String[] transaction  = line.split(",");
				if(transaction[8].equals("BUY")) 
				{
					String companyName = transaction[1]; // second column in FTR Auction result file
					String source = transaction[3];
					String sink = transaction[5];
					String type = transaction[7];				
					double price = Double.parseDouble(transaction[10]);
					double MWs = Double.parseDouble(transaction[9]);
					double moneySpent = MWs * price;				
					ftrPath incomingPath = new ftrPath(source, sink, type, price);
					ArrayList<Double> dailyPathPLs = paths.get(incomingPath);				
					Company company = companies.get(companyName); // is company in the Map already?				
									
					if(company == null)                      // if NO
					{                     
						company = new Company(companyName);					
						companies.put(companyName, company);			// add company to map		
						sameCompany = 1;					
					}
					else  									// if YES
					{                                   
						sameCompany = sameCompany + 1;    //counting number of paths of one company					
					}	
						
					company.addPath(incomingPath);
					company.addMWs(MWs);
					company.addMoneySpent(moneySpent);
					company.addTotalPL(dailyPathPLs, MWs);
					prevComp = company;
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
		return companies;
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
					paths.put(new ftrPath(transaction[3],transaction[5],transaction[7],Double.parseDouble(transaction[10])),new ArrayList<Double>());
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
			stream.close();
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
		@SuppressWarnings({ "unused", "resource" })
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

		//in.close();
				
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
			paths.get(key).add(sum);
		}
		//System.out.println("name mismatches: "+xxx);
		//System.out.println("============================================");
	}
}
