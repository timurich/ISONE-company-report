/**
 * Calculate monthly PL for each market participant in ISO-NE
 * Running for a long period over multiple years and producing CSV file reports (by company and by path)
 * @author Timur
 * Version 0.2
 * Good working version. It does not use node names, only node numbers (PNodeIDs).
 */

//import java.io.FileWriter;
import java.io.*;
import java.util.*;
import java.nio.file.*;

public class MonthlyPLbyCompany 
{
	public static void main(String[] args) throws IOException 
	{
		System.out.println("Report: Market Participants and Paths PL For a given period of time.");
		System.out.println();
		
		createDirectoriesForReports();/////////////////////////////////////////
		Map<String, String> latestNodeNames = getLatestNodeNames(); //Map of <PNodeID, latestNodeName>
		
		@SuppressWarnings("resource")
		Scanner in = new Scanner(System.in);
		String year_start;
		String year_end;
		String month_start;
		String month_end;
		boolean right;
		do {
			System.out.print("Input starting year [2011 - 2015]: ");
			year_start = in.nextLine();
			int y = Integer.parseInt(year_start);			
			if(y < 2011 || y > 2015) {
				System.out.println("Wrong year! Please re-input.\n");
				right = false;
			} else right = true;
		} while(!right);
		System.out.println();
		
		do {
			System.out.print("Input ending year [2011 - 2015]: ");
			year_end = in.nextLine();
			int y = Integer.parseInt(year_end);			
			if(y < 2011 || y > 2015) {
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
		
		String requestedFtrFile = "";		
		ftrAuctionResFile [] files = getAllFTRAuctionFiles();		
		System.out.println("FTR Auction Result Files:");
		int zzz = 0;
		for(int intYear = Integer.parseInt(year_start); intYear <= Integer.parseInt(year_end); intYear++)
		{	
			for(int intMonth = Integer.parseInt(month_start); intMonth <= Integer.parseInt(month_end); intMonth++)
			{
				String year = String.valueOf(intYear);
				String month = String.valueOf(intMonth);
				if(month.length() < 2) month = "0"+month;
								
				for(int i = 0; i < files.length; i++) {
					if(files[i].getYear().equals(year) && files[i].getMonth().equals(month))						
						requestedFtrFile = files[i].getName();					
				}
				
				System.out.print(requestedFtrFile+", "); zzz++;				
				if(zzz == 5) {System.out.println(); zzz=0; }
				///////////////////Reading all paths for given month		
				Map <ftrPath, ArrayList<Double>> paths = CreateListOfAllPathsFor1month(requestedFtrFile); // set of All unique paths for the month (FTR)		
				//System.out.println("\nNumber of unique paths for the month = "+paths.size());
				
				//System.out.println("=========================================================");
				CalculatePL_of_AllPathsForGivenMonth(year, month, paths);
				//System.out.println("=========================================================");
				@SuppressWarnings("unused")
				int temp = 0;
				String fNamePaths = "Monthly_Paths_PL_"+year+month+".csv";
				
				BufferedWriter bw = new BufferedWriter(new FileWriter("C:/ISONE/Bids/Reports/Monthly_Paths_PL/"+fNamePaths));  
				bw.write("Source,sourceID,Sink,sinkID,Class,Price,Total P/L for month,");
				int h = 0;
				for(h = 1; h <= Util.getNumberOfDaysInMonth(year,month)-1; h++) bw.write("Day"+h+","); bw.write("Day"+h); //to avoid last comma)		
				bw.newLine();
				for(ftrPath key: paths.keySet()) {
					if(key.getPathTotalPL()>50 && key.getPathTotalPL() / key.getFtrPrice() > 1.5) { // FILTER 
						//System.out.println(key.toString()+", "+ key.getAllDaysPLstring()+"\t = "+ key.getPathTotalPL());
					
						ArrayList<Double> dailyPLs = key.getAllDaysPL();			
						String writeString = "";
						writeString += latestNodeNames.get(key.getSource())+","+key.getSource()+","+latestNodeNames.get(key.getSink())+","+key.getSink()+","+key.getType()+","+key.getFtrPrice()+","+key.getPathTotalPL()+",";
						for(int i = 0; i<dailyPLs.size(); i++)
							writeString += dailyPLs.get(i)+",";				
						bw.write(writeString.substring(0,writeString.length()-1));
						bw.newLine();
						temp++;
					}
								
				}
				bw.close();
				
				Map<String, Company> companies = MonthlyReport_CompaniesPL(requestedFtrFile, paths);
				
				String fNameCompanies = "Monthly_Companies_PL_"+year+month+".csv";
				BufferedWriter bw2 = new BufferedWriter(new FileWriter("C:/ISONE/Bids/Reports/Monthly_Companies_PL/"+fNameCompanies));
				
				bw2.write("Company,# of paths,total MWs,Capital Spent,Total P/L");
				bw2.newLine();
				String writeString = "";
				for(String key: companies.keySet()) 
				{	
					writeString = "";					
					writeString += key+",";
					ArrayList<ftrPath> companyPaths = companies.get(key).getPaths();					
					writeString += companyPaths.size()+",";					
					writeString += companies.get(key).getMWs()+",";					
					writeString += companies.get(key).getMoneySpent()+",";					
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
		}	
		System.out.println("DONE!!!");
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

	public static ftrAuctionResFile[] getAllFTRAuctionFiles() {
		File dir = null;		
		String fileName = "";
		String year;
		String month;
		String [] files_ = null;
		ftrAuctionResFile [] files = null;
		
		try{
			dir = new File("C:/ISONE/FTR_Positions/FTR_Bid_Results");
			
			FilenameFilter fileNameFilster = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					if(name.substring(0,8).equals("Monthly_"))
						return true;
					
					return false;
				}
			};
			
			files_ = dir.list(fileNameFilster);			
			files = new ftrAuctionResFile[files_.length];		    
			for (int i = 0; i < files_.length; i++) {				
				fileName =files_[i];
			    year = fileName.substring(8,12);
			    month = fileName.substring(13,15);
			    files[i]= new ftrAuctionResFile(fileName, year, month);		        
			}			
		} catch (Exception x) {
			    x.printStackTrace();
		}
		
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
			//int zzz = 0;
			for (Path entry: stream) {				
		        fileName = entry.getFileName().toString();
		       // zzz++;
		       // System.out.print(fileName+"  ");
		       // if(zzz == 5) {System.out.println(); zzz=0;}
		        getAllPathPricesForDay(fileName, paths);		        
		    }
		} catch (IOException x) {
		    System.err.println(x);
		}		
		//System.out.println("\n");
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
	
	public static void createDirectoriesForReports() 
	{
		File fileCompaniesReport = new File("C:/ISONE/Bids/Reports/Monthly_Companies_PL");
		File filePathsReport = new File("C:/ISONE/Bids/Reports/Monthly_Paths_PL");
		boolean success = false;

		if(fileCompaniesReport.exists()==false){
			success = fileCompaniesReport.mkdir();
			if(success == false)
				success = fileCompaniesReport.mkdirs();
		}

		if(filePathsReport.exists()==false){
			filePathsReport.mkdir();
			if(success == false)
				success = filePathsReport.mkdir();
		}
	}
	
	public static Map<String, String> getLatestNodeNames()
	{
		Map<String, String> nodeNames = new HashMap<String, String>();
		
		File daCleanFolder = new File("C:/ISONE/ISONE_DA_RT_FTR_Prices/Cong_Component_LMP_DA_By_PNodeID_Clean");
		File[] allFiles = daCleanFolder.listFiles();
		String [] str;
		for(int i = 0; i < allFiles.length; i++)
		{
			str = allFiles[i].getName().split("___");
			nodeNames.put(str[0],str[str.length-2]);			
		}
		
		return nodeNames;
	}	
}
