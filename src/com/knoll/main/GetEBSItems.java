/**
 * 
 */
package com.knoll.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.knoll.tos.F68EBS;
import com.knoll.utils.Utils;

/**
 * @author jfavara
 *
 */
public class GetEBSItems {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Utils kpru = new Utils();

		//Connectivity goodies
		PreparedStatement stmt1 = null;
		PreparedStatement addItems = null;
		ResultSet rset = null;
		Connection AS400Con = null;
		Connection OracleCon = null;
		PreparedStatement ostmt = null;
		ResultSet orset = null;
		PreparedStatement invstmt = null;
		ResultSet invset = null;


		//define SQL statements

		String orgId = "Select ORGANIZATION_ID FROM INV.MTL_PARAMETERS where ORGANIZATION_CODE = ?";

		String inventoryId = "Select INVENTORY_ITEM_ID, SEGMENT1 FROM INV.MTL_SYSTEM_ITEMS_B WHERE ORGANIZATION_ID = ?";

		String itemCost = "SELECT ITEM_COST FROM BOM.CST_ITEM_COSTS WHERE ORGANIZATION_ID = ? AND COST_TYPE_ID = ? and INVENTORY_ITEM_ID = ?";

		String itemCost1 = "SELECT USAGE_RATE_OR_AMOUNT,RESOURCE_ID FROM BOM.CST_ITEM_COST_DETAILS WHERE ORGANIZATION_ID = ?"
				+ "AND COST_TYPE_ID = ? AND BASIS_TYPE = ? AND INVENTORY_ITEM_ID = ? "; 

		String itemPrice1 = "SELECT OPERAND FROM QP.QP_LIST_LINES WHERE INVENTORY_ITEM_ID = ? AND ORGANIZATION_ID = ?";

		String itemPrice2 = "SELECT OPERAND FROM APPS.QP_PRICE_LIST_ITEMS_V WHERE CONCATENATED_SEGMENTS = ? AND NAME NOT LIKE ? AND START_DATE_ACTIVE <= ? "
				+ "AND END_DATE_ACTIVE >= ?";
		
		String F68EBS_I = "INSERT INTO F68EBS ("
				+ " QXLITM,"
				+ " QXUNCS,"
				+ " QXUPRC,"
				+ " QXSRT1,"
				+ " QXUSER,"
				+ " QXPID,"
				+ " QXJOBN,"
				+ " QXUPMJ,"
				+ " QXTDAY,"
				+ " QXQ1,"
				+ " QXAA01,"
				+ " QXWORK,"
				+ " QXRATS) VALUES ("
				+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		//work fields
		DecimalFormat df = new DecimalFormat("#.####");
		df.setRoundingMode(RoundingMode.CEILING);
		int org_ID = 0; // organization ID
		int inv_ID = 0; // inventory item id
		int ebsCost = 0; //ebs cost 
		int ebsPrice = 0; //ebs price
		int ebsRate = 0; //ebs rates
		String ebsRats = null;
		String litm = null; //2nd item number in jde.
		String ebsCostMissing = null;
		String ebsPriceMissing = null;
		String ebsRateMissing = null;
		
		final long startTime = System.currentTimeMillis(); // get start time. 

		//Start processing 
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy");
		String strDate = sdf.format(date).trim();
		String jobDate = strDate.substring(3, 5) + strDate.substring(0, 2) + strDate.substring(6, 8);

		// build EBS date 
		String ebsDate = strDate.substring(0, 2) + "-" + kpru.getAlphaMonth(strDate.substring(3, 5)) + "-" + strDate.substring(6, 8);

		// start processing
		//OK lets connect to Oracle and get organization ID
		try {

			AS400Con = kpru.getIseriesConnection(); //Get I-Series connection
			System.out.println("I-Series conneciton established....");
			addItems = AS400Con.prepareStatement(F68EBS_I);		
			
			OracleCon = kpru.getOracleConnection(); //Get Oracle connection 
			System.out.println("Oracle connection established...");
			ostmt = OracleCon.prepareStatement(orgId);
			ostmt.setString(1, "EGT");
			orset = ostmt.executeQuery();
			orset.next();
			org_ID = orset.getInt("ORGANIZATION_ID"); 
			if ( orset != null ) try { orset.close(); } catch( SQLException e ) {}
			if ( ostmt != null ) try { ostmt.close(); } catch( SQLException e ) {}

			//set up loop to start reading EBS items
			invstmt = OracleCon.prepareStatement(inventoryId); 
			invstmt.setInt(1, org_ID); //organization ID
			invset = invstmt.executeQuery();
			while(invset.next()){

				inv_ID = invset.getInt("INVENTORY_ITEM_ID");
				litm = invset.getString("SEGMENT1");

				//get item cost
				ostmt = OracleCon.prepareStatement(itemCost); 
				ostmt.setInt(1, org_ID);
				ostmt.setInt(2,1); // cost type = 1
				ostmt.setInt(3, inv_ID); //inventory item ID
				orset = ostmt.executeQuery();
				if (orset.isBeforeFirst()){

					orset.next();
					ebsCost = (int)(orset.getDouble("ITEM_COST") *10000); // item cost convert to jde 
					ebsCostMissing = "N";

				} else {

					ebsCost = 0; // no cost record set to 0
					ebsCostMissing = "Y";
				}
				if ( orset != null ) try { orset.close(); } catch( SQLException e ) {}
				if ( ostmt != null ) try { ostmt.close(); } catch( SQLException e ) {}

				//get item price
				//Check QP.QP_LIST_LINES first
				ostmt = OracleCon.prepareStatement(itemPrice1);
				ostmt.setInt(1, inv_ID);
				ostmt.setInt(2, org_ID);
				orset = ostmt.executeQuery();
				if (orset.isBeforeFirst()) {

					orset.next();
					ebsPrice =  (int)(orset.getDouble("OPERAND") *10000); // convert ebs price to JDE
					ebsPriceMissing = "N";
					if ( orset != null ) try { orset.close(); } catch( SQLException e ) {} //get what we need then close it
					if ( ostmt != null ) try { ostmt.close(); } catch( SQLException e ) {}

				} else {

					//not found in QP_LIST_LINES
					//check APPS.QP_PRICE_LIST_ITEMS_V
					if ( orset != null ) try { orset.close(); } catch( SQLException e ) {} 
					if ( ostmt != null ) try { ostmt.close(); } catch( SQLException e ) {}
					ostmt = OracleCon.prepareStatement(itemPrice2);
					ostmt.setString(1, litm);
					ostmt.setString(2,"%TRANSFER%");
					ostmt.setString(3, ebsDate);
					ostmt.setString(4, ebsDate);
					orset = ostmt.executeQuery();
					if (orset.isBeforeFirst()) {

						orset.next();
						ebsPrice =  (int)(orset.getDouble("OPERAND") *10000);
						ebsPriceMissing = "N";
						


					} else {

						ebsPrice = 0;
						ebsPriceMissing = "Y";
					}

					if ( orset != null ) try { orset.close(); } catch( SQLException e ) {} 
					if ( ostmt != null ) try { ostmt.close(); } catch( SQLException e ) {}

				} // end pricing

				// get rate information
				ostmt = OracleCon.prepareStatement(itemCost1); 
				ostmt.setInt(1, org_ID); //organization id
				ostmt.setInt(2, 1); // cost type = 1
				ostmt.setInt(3, 5); //Basis type = 5
				ostmt.setInt(4, inv_ID); //item id
				orset = ostmt.executeQuery();
				if (orset.isBeforeFirst()) {
					
					orset.next();
					ebsRate =  (int)(orset.getDouble("USAGE_RATE_OR_AMOUNT") *10000);
					Integer x = orset.getInt("RESOURCE_ID");
					ebsRats = x.toString().trim();
					ebsRateMissing = "N";

				} else {

					ebsRate = 0;
					ebsRats = " ";
					ebsRateMissing = "Y";
				}

				if ( orset != null ) try { orset.close(); } catch( SQLException e ) {} 
				if ( ostmt != null ) try { ostmt.close(); } catch( SQLException e ) {}
				
				
				//batch the records and write them when program ends
				addItems.setString(1, litm); //2nd item number
				addItems.setInt(2, ebsCost); //EBS cost
				addItems.setInt(3, ebsPrice); //EBS price
				addItems.setInt(4, ebsRate); //EBS rate
				addItems.setString(5, "ROBOT"); //user
				addItems.setString(6,"GetItem"); //program name
				addItems.setString(7, "EBSItems"); //job name
				addItems.setInt(8, Integer.parseInt(jobDate)); // job date
				addItems.setInt(9, 0); //job time 
				addItems.setString(10, ebsCostMissing); //EBS cost missing
				addItems.setString(11, ebsPriceMissing); //EBS price missing
				addItems.setString(12, ebsRateMissing); //EBS rate missing
				addItems.setString(13, ebsRats);
				addItems.addBatch(); //batch the

			} //end for reading item master
			

			int[] records = addItems.executeBatch(); // send to database

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("File not found Error conneceting to Oracle");
			e.printStackTrace();

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Class not found Exception Error conneceting to Oracle");
			e.printStackTrace();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("IO Exception Error conneceting to Oracle");
			e.printStackTrace();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("SQL exception Error conneceting to Oracle");
			e.printStackTrace();

		} finally {

			if ( orset != null ) try { orset.close(); } catch( SQLException e ) {}
			if ( ostmt != null ) try { ostmt.close(); } catch( SQLException e ) {}
			if ( invset != null ) try { invset.close(); } catch( SQLException e ) {}
			if ( invstmt != null ) try { invstmt.close(); } catch( SQLException e ) {}
			if ( AS400Con != null ) try { AS400Con.close(); } catch( SQLException e ) {}
			if ( OracleCon != null ) try { OracleCon.close(); } catch( SQLException e ) {}
		}
		
		final long endTime = System.currentTimeMillis();
		System.out.println("Total execution time in minutes: " + (endTime - startTime)/ (60 * 1000) % 60 );

	}

}
