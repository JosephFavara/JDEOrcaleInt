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

import com.knoll.tos.F684102JE;
import com.knoll.utils.Utils;

/**
 * @author jfavara
 * This program will report item variances between JDE and EBS
 */
public class ItemVariance {

	/**
	 * @param args
	 */
	//@SuppressWarnings("resource")
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		ItemVariance iv = new ItemVariance();
		Utils kpru = new Utils();

		//Connectivity goodies
		PreparedStatement stmt1 = null;
		PreparedStatement errorstmt = null;
		ResultSet rset = null;
		Connection AS400Con = null;
		Connection OracleCon = null;
		PreparedStatement ostmt = null;
		ResultSet orset = null;
		

		//define SQL statements
		String sql = "Select * from F684102D";

		String orgId = "Select ORGANIZATION_ID FROM INV.MTL_PARAMETERS where ORGANIZATION_CODE = ?";

		String inventoryId = "Select INVENTORY_ITEM_ID, SEGMENT1 FROM INV.MTL_SYSTEM_ITEMS_B WHERE ORGANIZATION_ID = ? and SEGMENT1 = ?";

		String itemCost = "SELECT ITEM_COST FROM BOM.CST_ITEM_COSTS WHERE ORGANIZATION_ID = ? AND COST_TYPE_ID = ? and INVENTORY_ITEM_ID = ?";

		String itemCost1 = "SELECT USAGE_RATE_OR_AMOUNT,RESOURCE_ID FROM BOM.CST_ITEM_COST_DETAILS WHERE ORGANIZATION_ID = ?"
				+ "AND COST_TYPE_ID = ? AND BASIS_TYPE = ? AND INVENTORY_ITEM_ID = ? "; 

		String itemPrice1 = "SELECT OPERAND FROM QP.QP_LIST_LINES WHERE INVENTORY_ITEM_ID = ? AND ORGANIZATION_ID = ?";

		String itemPrice2 = "SELECT OPERAND FROM APPS.QP_PRICE_LIST_ITEMS_V WHERE CONCATENATED_SEGMENTS = ? AND NAME NOT LIKE ? AND START_DATE_ACTIVE <= ? "
				+ "AND END_DATE_ACTIVE >= ?";

		String F684102JE_I = "INSERT INTO F684102JE ("
				+ " QXITM,"
				+ " QXLITM,"
				+ " QXMCU,"
				+ " QXDSC1,"
				+ " QXTXLN,"
				+ " QXUNCS,"
				+ " QXFUC,"
				+ " QXUPRC,"
				+ " QXFUP,"
				+ " QXVAL,"
				+ " QXVALU,"
				+ " QXUSER,"
				+ " QXPID,"
				+ " QXJOBN,"
				+ " QXUPMJ,"
				+ " QXTDAY ) VALUES ("
				+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


		//work fields
		DecimalFormat df = new DecimalFormat("#.####");
		df.setRoundingMode(RoundingMode.CEILING);
		int org_ID = 0; // organization ID
		int inv_ID = 0; // inventory item ID
		int jdeValue = 0; //jde value for cost or price
		int ebsValue = 0; //ebs value for cost or price
		final long startTime = System.currentTimeMillis(); // get start time. 

		//Array lists
		ArrayList<F684102JE> errors = new ArrayList<F684102JE>();

		//Start processing 
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy");
		String strDate = sdf.format(date).trim();

		// build EBS date 
		String ebsDate = strDate.substring(0, 2) + "-" + kpru.getAlphaMonth(strDate.substring(3, 5)) + "-" + strDate.substring(6, 8);

		try {

			//Get I-Series connection and start reading F684102D
			AS400Con = kpru.getIseriesConnection();
			stmt1 = AS400Con.prepareStatement(sql);
			System.out.println("I-Series connection is established");
			rset = stmt1.executeQuery();
			while(rset.next()) {

				//OK lets connect to Oracle and get organization ID
				if (OracleCon == null) {

					try {

						OracleCon = kpru.getOracleConnection();
						System.out.println("Oracle connection established...");
						ostmt = OracleCon.prepareStatement(orgId);
						ostmt.setString(1, "EGT");
						orset = ostmt.executeQuery();
						if (!orset.isBeforeFirst()) {
							System.out.println("Error in orgrantiazation ID");
							return;						
						} 							
						orset.next();
						org_ID = orset.getInt("ORGANIZATION_ID"); 	

					} catch (Exception e) {
						// TODO Auto-generated catch block
						System.out.println("Error conneceting to Oracle");
						e.printStackTrace();
						return;
					}

				} 


				//get the inventory ID
				if ( orset != null ) try { orset.close(); } catch( SQLException e ) {}
				if ( ostmt != null ) try { ostmt.close(); } catch( SQLException e ) {}
				ostmt = OracleCon.prepareStatement(inventoryId); 
				String item = rset.getString("MXLITM").trim();
				ostmt.setInt(1, org_ID); //organization ID
				ostmt.setString(2,item);  //JDE 2nd item#
				orset = ostmt.executeQuery();
				if (!orset.isBeforeFirst()) {

					//we do not have this item prepare error message.
					errors.add(iv.loadError(rset, "Item not in INV.MTL_SYSTEM_ITEMS_B", "I")); // save to error for batching later.

				} else {

					orset.next();
					inv_ID = orset.getInt("INVENTORY_ITEM_ID");
					if ( orset != null ) try { orset.close(); } catch( SQLException e ) {}
					if ( ostmt != null ) try { ostmt.close(); } catch( SQLException e ) {}
					//get item cost
					ostmt = OracleCon.prepareStatement(itemCost); 
					ostmt.setInt(1, org_ID);
					ostmt.setInt(2,1); // cost type = 1
					ostmt.setInt(3, inv_ID); //inventory item ID
					orset = ostmt.executeQuery();
					if (!orset.isBeforeFirst()){

						//missing cost record
						errors.add(iv.loadError(rset, "Item not in BOM.CST_ITEM_COSTS", "I")); // save to error for batching later.

					} else {

						orset.next();
						Integer x = rset.getInt("MXUNCS");
						if (x.doubleValue()/10000 !=  Double.parseDouble(df.format(orset.getDouble("ITEM_COST")))) {

							jdeValue = rset.getInt("MXUNCS");
							ebsValue =  (int)(orset.getDouble("ITEM_COST") *10000);
							errors.add(iv.loadError(rset, "JDE and EBS cost do not match", "C", jdeValue, ebsValue));

						}
						

					}
					
					if ( orset != null ) try { orset.close(); } catch( SQLException e ) {}
					if ( ostmt != null ) try { ostmt.close(); } catch( SQLException e ) {}
					//Start working on the pricing
					//Check QP.QP_LIST_LINES first
					ostmt = OracleCon.prepareStatement(itemPrice1);
					ostmt.setInt(1, inv_ID);
					ostmt.setInt(2, org_ID);
					orset = ostmt.executeQuery();
					if (!orset.isBeforeFirst()) {

						if ( orset != null ) try { orset.close(); } catch( SQLException e ) {}
						if ( ostmt != null ) try { ostmt.close(); } catch( SQLException e ) {}
						//not in QP.QP_LIST_LINES check APPS.QP_PRICE_LIST_ITEMS_V
						ostmt = OracleCon.prepareStatement(itemPrice2);
						ostmt.setString(1, item);
						ostmt.setString(2,"%TRANSFER%");
						ostmt.setString(3, ebsDate);
						ostmt.setString(4, ebsDate);
						orset = ostmt.executeQuery();

						if (!orset.isBeforeFirst()) {

							//missing cost record
							errors.add(iv.loadError(rset, "Item not in APPS.QP_PRICE_LIST_ITEMS_V,QP.QP_LIST_LINES", "I")); // save to error for batching later.

						} else {

							// found in APPS.QP_PRICE_LIST_ITEMS_V

							orset.next();
							Integer x = rset.getInt("MXUPRC");
							if (x.doubleValue()/10000 !=  Double.parseDouble(df.format(orset.getDouble("OPERAND")))) {

								jdeValue = rset.getInt("MXUPRC");
								ebsValue =  (int)(orset.getDouble("OPERAND") *10000);
								errors.add(iv.loadError(rset, "JDE and EBS price not equal APPS.QP_PRICE_LIST_ITEMS_V", "P", jdeValue, ebsValue));

							}

						}

						if ( orset != null ) try { orset.close(); } catch( SQLException e ) {}
						if ( ostmt != null ) try { ostmt.close(); } catch( SQLException e ) {}

					} else {

						// found in QP.QP_LIST_LINES 

						orset.next();
						Integer x = rset.getInt("MXUPRC");
						if (x.doubleValue()/10000 !=  Double.parseDouble(df.format(orset.getDouble("OPERAND")))) {

							jdeValue = rset.getInt("MXUPRC");
							ebsValue =  (int)(orset.getDouble("OPERAND") *10000);
							errors.add(iv.loadError(rset, "JDE and EBS price not equal QP.QP_LIST_LINES", "P", jdeValue, ebsValue));

						}


					} 


					// get rate information
					if ( orset != null ) try { orset.close(); } catch( SQLException e ) {}
					if ( ostmt != null ) try { ostmt.close(); } catch( SQLException e ) {}
					ostmt = OracleCon.prepareStatement(itemCost1); 
					ostmt.setInt(1, org_ID); //organization id
					ostmt.setInt(2, 1); // cost type = 1
					ostmt.setInt(3, 5); //Basis type = 5
					ostmt.setInt(4, inv_ID); //item id
					orset = ostmt.executeQuery();
					if (orset.isBeforeFirst()) {
						
						//lets check the rate costs first
						orset.next();
						Integer x = rset.getInt("MXRTSD"); //frozen freight rates
						if (x.doubleValue()/10000 !=  Double.parseDouble(df.format(orset.getDouble("USAGE_RATE_OR_AMOUNT")))) {

							jdeValue = rset.getInt("MXRTSD");
							ebsValue =  (int)(orset.getDouble("USAGE_RATE_OR_AMOUNT") *10000);
							errors.add(iv.loadError(rset, "JDE and EBS freight costs not equal BOM.CST_ITEM_COST_DETAILS", "C", jdeValue, ebsValue));

						}
						
						//now lets check for frozen rates.
						String jdeRats = rset.getString("MXRATS").trim();
						Integer ebsRats = orset.getInt("RESOURCE_ID");
						if (!jdeRats.equals(ebsRats.toString().trim())){
							
							errors.add(iv.loadError(rset, "JDE and EBS rates not equal BOM.CST_ITEM_COST_DETAILS", "R", jdeRats, ebsRats.toString().trim()));
							
						}
						
						
					}
					
					if ( orset != null ) try { orset.close(); } catch( SQLException e ) {}
					if ( ostmt != null ) try { ostmt.close(); } catch( SQLException e ) {}

				} // end for item check


			} //end rset read

			//If errors exist, batch them and send them to database
			if (!errors.isEmpty()){
				
				System.out.println("Preparing to write errors");

				errorstmt = AS400Con.prepareStatement(F684102JE_I);

				//Oh my.. we have some errors
				for (F684102JE to: errors){

					errorstmt.setInt(1, to.getQxitm());
					errorstmt.setString(2, to.getQxlitm());
					errorstmt.setString(3, to.getQxmcu());
					errorstmt.setString(4, to.getQxdsc1());
					errorstmt.setString(5, to.getQxtxln());
					errorstmt.setInt(6, to.getQxuncs());
					errorstmt.setInt(7, to.getQxfuc());
					errorstmt.setInt(8, to.getQxuprc());
					errorstmt.setInt(9, to.getQxfup());
					errorstmt.setString(10, to.getQxval());
					errorstmt.setString(11, to.getQxvalu());
					errorstmt.setString(12, to.getQxuser());
					errorstmt.setString(13, to.getQxpid());
					errorstmt.setString(14, to.getQxjobn());
					errorstmt.setInt(15, to.getQxupmj());
					errorstmt.setInt(16, to.getQxtday());

					errorstmt.addBatch(); //add them to the batch

				}

				int[] records = errorstmt.executeBatch(); // send to database

			} 

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("file not found connect I-Series");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Class not found connect I-Series");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("IO exception connect I-Series");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("SQL Exception connect I-Series");

		} finally {

			if ( rset != null ) try { rset.close(); } catch( SQLException e ) {}
			if ( stmt1 != null ) try { stmt1.close(); } catch( SQLException e ) {}
			if ( orset != null ) try { orset.close(); } catch( SQLException e ) {}
			if ( ostmt != null ) try { ostmt.close(); } catch( SQLException e ) {}
			if ( AS400Con != null ) try { AS400Con.close(); } catch( SQLException e ) {}
			if ( OracleCon != null ) try { OracleCon.close(); } catch( SQLException e ) {}
			if ( errorstmt != null ) try { errorstmt.close(); } catch( SQLException e ) {}

		}


		final long endTime = System.currentTimeMillis();
		System.out.println("Total execution time in minutes: " + (endTime - startTime)/ (60 * 1000) % 60 );

	}

	/**
	 * Parses result set from F614102D and passes back F614102JE
	 * 
	 * @param result set from F684102d
	 * @param error message
	 * @param error type I = item missing, C = Cost, P = Price
	 * @return F684102JE 
	 * @throws SQLException
	 */
	private F684102JE loadError(ResultSet eset, String error, String errorType) throws SQLException{

		F684102JE info = new F684102JE();


		if (errorType.equals("I")){
			info.setQxfuc(0);
			info.setQxfup(0);
			info.setQxuncs(0);
			info.setQxuprc(0);
		}

		info.setQxitm(eset.getInt("MXITM"));
		info.setQxlitm(eset.getString("MXLITM"));
		info.setQxtxln(error);
		info.setQxmcu(eset.getString("MXMCU"));
		info.setQxdsc1(eset.getString("MXDSC1"));
		info.setQxval("     ");
		info.setQxvalu("    ");
		info.setQxuser(eset.getString("MXUSER"));
		info.setQxjobn(eset.getString("MXJOBN"));
		info.setQxpid(eset.getString("MXPID"));
		info.setQxupmj(eset.getInt("MXUPMJ"));
		info.setQxtday(eset.getInt("MXTDAY"));
		return info;

	}

	/**
	 * Parses result set from F614102D and passes back F614102JE
	 * 
	 * @param result set from F684102d
	 * @param error message
	 * @param error type I = item missing, C = Cost, P = Price
	 * @param  jde value
	 * @param  ebs value
	 * @return F684102JE 
	 * @throws SQLException
	 */
	private F684102JE loadError(ResultSet eset, String error, String errorType, int jdeValue, int ebsValue) throws SQLException{

		F684102JE info = new F684102JE();

		//cost difference
		if (errorType.equals("C")){
			info.setQxuncs(jdeValue);	
			info.setQxfuc(ebsValue);			
		}

		// price difference
		if (errorType.equals("P")){
			info.setQxuprc(jdeValue);
			info.setQxfup(ebsValue);
		}
		
		info.setQxitm(eset.getInt("MXITM"));
		info.setQxlitm(eset.getString("MXLITM"));
		info.setQxtxln(error);
		info.setQxmcu(eset.getString("MXMCU"));
		info.setQxdsc1(eset.getString("MXDSC1"));
		info.setQxval("     ");
		info.setQxvalu("    ");
		info.setQxuser(eset.getString("MXUSER"));
		info.setQxjobn(eset.getString("MXJOBN"));
		info.setQxpid(eset.getString("MXPID"));
		info.setQxupmj(eset.getInt("MXUPMJ"));
		info.setQxtday(eset.getInt("MXTDAY"));
		return info;

	}
	
	/**
	 * Parses result set from F614102D and passes back F614102JE.
	 * Use for processing frozen rates 
	 * 
	 * @param result set from F684102d
	 * @param error message
	 * @param error type I = item missing, R =  Rates
	 * @param  jde value 
	 * @param  ebs value
	 * @return F684102JE 
	 * @throws SQLException
	 */
	private F684102JE loadError(ResultSet eset, String error, String errorType, String jdeValue, String ebsValue) throws SQLException{

		F684102JE info = new F684102JE();

		//cost differ
		if (errorType.equals("R")){
			info.setQxval(jdeValue);	
			info.setQxvalu(ebsValue);			
		}
		
		info.setQxfuc(0);
		info.setQxfup(0);
		info.setQxuncs(0);
		info.setQxuprc(0);
		info.setQxitm(eset.getInt("MXITM"));
		info.setQxlitm(eset.getString("MXLITM"));
		info.setQxtxln(error);
		info.setQxmcu(eset.getString("MXMCU"));
		info.setQxdsc1(eset.getString("MXDSC1"));
		info.setQxuser(eset.getString("MXUSER"));
		info.setQxjobn(eset.getString("MXJOBN"));
		info.setQxpid(eset.getString("MXPID"));
		info.setQxupmj(eset.getInt("MXUPMJ"));
		info.setQxtday(eset.getInt("MXTDAY"));
		return info;

	}


}
