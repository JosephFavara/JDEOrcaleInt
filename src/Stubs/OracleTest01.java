package Stubs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.knoll.utils.Utils;


public class OracleTest01 {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, IOException, SQLException {
		// TODO Auto-generated method stub
		//test this
		
		String sql1 = "Select INVENTORY_ITEM_ID, SEGMENT1 FROM INV.MTL_SYSTEM_ITEMS_B WHERE ORGANIZATION_ID = ? and SEGMENT1 = ?";
		
		Utils kpr = new Utils();
		Connection conn = null;
		PreparedStatement stmt1 = null;
		ResultSet rset = null;
		
		conn = kpr.getOracleConnection();
		stmt1 = conn.prepareStatement(sql1);
		stmt1.setInt(1, 89);
		stmt1.setString(2, "K11046");
		rset = stmt1.executeQuery();
		rset.next();
			int inv_ID = rset.getInt("INVENTORY_ITEM_ID");
			String segment1 = rset.getString("SEGMENT1");	
		
		
		if ( rset != null ) try { rset.close(); } catch( SQLException e ) {}
		if ( stmt1 != null ) try { stmt1.close(); } catch( SQLException e ) {}
		if ( conn != null ) try { conn.close(); } catch( SQLException e ) {}
		System.out.println("That's all folks...");

	}

}
