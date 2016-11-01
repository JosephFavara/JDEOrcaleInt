package Stubs;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Date date = new Date();
	    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");

	    String strDate = sdf.format(date);
	    System.out.println("formatted date in mm/dd/yy : " + strDate);

	    
	    sdf = new SimpleDateFormat("dd-MM-yy");
	    strDate = sdf.format(date).trim();
	    System.out.println("formatted date in dd-MM-yy : " + strDate);
	    
	    String workdate= "20-10-16";
	    String day = workdate.substring(0, 2);
	    String month = workdate.substring(3, 5);
	    String year = workdate.substring(6, 8);
	    
	    

	}

}
