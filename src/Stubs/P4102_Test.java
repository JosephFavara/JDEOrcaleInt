package Stubs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.knoll.utils.Utils;

public class P4102_Test {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, IOException, SQLException {
		// TODO Auto-generated method stub
		
		final long startTime = System.currentTimeMillis();
		String sql = "Select * from F4102 where IBMCU = ? and IBPRGR <> ? or IBMCU = ? and IBSTKT <> ? and IBLNTY <> ? order by IBLITM, IBMCU";
		int i = 0;
		PreparedStatement stmt1 = null;
		ResultSet rset = null;
		Connection AS400Con = null;
		Utils kpru = new Utils();
		
		AS400Con = kpru.getIseriesConnection();
		stmt1 = AS400Con.prepareStatement(sql);
		stmt1.setString(1, "       91140");
		stmt1.setString(2, " ");
		stmt1.setString(3, "       91140");
		stmt1.setString(4, "0");
		stmt1.setString(5, "N");
		rset = stmt1.executeQuery();
		
		while (rset.next()){
			System.out.println(rset.getString("IBLITM"));
			i = i + 1;
			
		}
		
		//clean up
		if ( rset != null ) try { rset.close(); } catch( SQLException e ) {}
		if ( stmt1 != null ) try { stmt1.close(); } catch( SQLException e ) {}
		if ( AS400Con != null ) try { AS400Con.close(); } catch( SQLException e ) {}
		
		final long endTime = System.currentTimeMillis();
		System.out.println("Total execution time: " + (endTime - startTime)/1000 );

		System.out.println("total number of items " + i);;

	}

}
