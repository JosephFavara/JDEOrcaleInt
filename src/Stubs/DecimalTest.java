package Stubs;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class DecimalTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		DecimalFormat df = new DecimalFormat("#.####");
		df.setRoundingMode(RoundingMode.CEILING);
		
		double cost = 25.486283832;
		String xcost = df.format(cost);
		System.out.println(xcost);
		//double newcost = Double.parseDouble(xcost);
		System.out.println(Double.parseDouble(df.format(cost)));

	}

}
