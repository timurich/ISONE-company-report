import java.util.Calendar;
import java.util.GregorianCalendar;


public class Util 
{	
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
