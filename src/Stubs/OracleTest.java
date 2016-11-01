package Stubs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.knoll.utils.Utils;

public class OracleTest {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, IOException, SQLException {
		// TODO Auto-generated method stub
		
		String sql = "Select * From Apps.Qp_Price_List_Items_V where Concatenated_Segments = ?";
		String sql1 = "SELECT * FROM BOM.CST_ITEM_COST_DETAILS WHERE ORGANIZATION_ID = ? and COST_TYPE_ID = ? AND BASIS_TYPE = ? AND INVENTORY_ITEM_ID = ?";
						
		Utils kpr = new Utils();
		Connection conn = null;
		PreparedStatement stmt1 = null;
		ResultSet rset = null;
		
		conn = kpr.getOracleConnection();
		stmt1 = conn.prepareStatement(sql);
		stmt1.setString(1, "K11046");
		rset = stmt1.executeQuery();
		
		System.out.println("Reading table Apps.Qp_Price_List_Items_V");
		while(rset.next()){
			System.out.println(rset.getString("CONCATENATED_SEGMENTS"));
			System.out.println(rset.getString("LIST_ITEM"));
		}
		
		stmt1 = conn.prepareStatement(sql1);
		stmt1.setInt(1, 89);
		stmt1.setInt(2, 1);
		stmt1.setInt(3, 5);
		stmt1.setInt(4, 1319);
		rset = stmt1.executeQuery();
		
		System.out.println("Reading BOM.CST_ITEM_COST_DETAILS ");
		while(rset.next()){
			System.out.println(rset.getInt("INVENTORY_ITEM_ID"));
			System.out.println(rset.getString("LAST_UPDATE_DATE"));
		}
		
		//clean up
				if ( rset != null ) try { rset.close(); } catch( SQLException e ) {}
				if ( stmt1 != null ) try { stmt1.close(); } catch( SQLException e ) {}
				if ( conn != null ) try { conn.close(); } catch( SQLException e ) {}
				System.out.println("That's all folks...");

	}

}
