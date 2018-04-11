package MelAutomation.MelAutomation;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import test.Configuration.PropertiesHandle;
import test.exception.DatabaseException;
import test.exception.MacroException;
import test.exception.PropertiesHandleException;
import util.common.DatabaseOperation;

public class MelProcess 
{
	protected DatabaseOperation configTable = null;
	protected PropertiesHandle configFile;
	protected DatabaseOperation inputoutputtable;
	protected DatabaseOperation expectedMelTable;
	protected DatabaseOperation Outputtable;
	protected DatabaseOperation actualMelTable;
	
	public MelProcess(PropertiesHandle configFile) throws MacroException
	{
		this.configFile = configFile;
		configTable = new DatabaseOperation();
		inputoutputtable = new DatabaseOperation();
		expectedMelTable = new DatabaseOperation();
		Outputtable = new DatabaseOperation();
		actualMelTable = new DatabaseOperation();
	}
	
	public void importActual() throws DatabaseException, PropertiesHandleException
	{
		PropertiesHandle DB1 = new PropertiesHandle("com.mysql.jdbc.Driver","jdbc:mysql://192.168.84.254:3113/starrbopdb?useSSL=false","root","redhat");
		DatabaseOperation.ConnectionSetup(DB1);
		PropertiesHandle DB2 = new PropertiesHandle("com.mysql.jdbc.Driver","jdbc:mysql://192.168.84.225:3700/JmeterDB-STARR_ISO?useSSL=false","root","redhat");
		DatabaseOperation.ConnectionSetup(DB2);
	}
	
	public void generateExpectedMel() throws DatabaseException, SQLException
	{
		LinkedHashMap<Integer, LinkedHashMap<String, String>> OutputTable=inputoutputtable.GetDataObjects("SELECT * FROM STARR_BOP_Quote_Policy_Endrosement_Cancel_INPUT a INNER JOIN OUTPUT_ISO_Quote b on a.`S.No` = b.`S.No` INNER JOIN OUTPUT_ISO_PolicyIssuance c on b.`S.No` = c.`S.No`");
		for(Entry<Integer, LinkedHashMap<String, String>> entry1 : OutputTable.entrySet())
		{
			LinkedHashMap<String, String> InputOutputRow = entry1.getValue();
			if(InputOutputRow.get("Flag_for_execution").equals("Y"))
			{
				LinkedHashMap<Integer, LinkedHashMap<String, String>> coverageData = configTable.GetDataObjects("Select * from MEL_CoverageOrder");
				for (Entry<Integer, LinkedHashMap<String, String>> entry : coverageData.entrySet())	
				{
					LinkedHashMap<String, String> configtablerow = entry.getValue();
					
					LinkedHashMap<String, String> SingleLine =new LinkedHashMap<String, String>();
					SingleLine=this.GeneratLine(configtablerow,InputOutputRow);
					expectedMelTable.insertRow(SingleLine);
				}
			}
		}
	}
	
	private LinkedHashMap<String, String> GeneratLine(LinkedHashMap<String, String> ExtendedLoopConfig, LinkedHashMap<String, String>InputOutputRow)
	{
		LinkedHashMap<String, String> lineMap = new LinkedHashMap<String, String>();
		try
		{
			LinkedHashMap<Integer, LinkedHashMap<String, String>> tablePumpinData = configTable.GetDataObjects(configFile.getProperty("melconfig"));
			for (Entry<Integer, LinkedHashMap<String, String>> entry : tablePumpinData.entrySet())	
			{
				
				LinkedHashMap<String, String> configtablerow = entry.getValue();
				if (configtablerow.get("Flag").equals("Y"))
				{
					switch(configtablerow.get("FieldNature"))
					{
						case "default":
						{
							lineMap.put(configtablerow.get("FieldNames"), configtablerow.get("StaticValues"));
							break;
						}
						case "CoverageWiseLookup":
						{
							lineMap.put(configtablerow.get("FieldNames"),ExtendedLoopConfig.get(configtablerow.get("DBColumnNames")));
							break;
						}
						
						case "DefaultPolicyDetail":
						{
							lineMap.put(configtablerow.get("FieldNames"), InputOutputRow.get(configtablerow.get("DBColumnNames")));
							break;
						}
						case "Bordereau_Date":
						{
							 Calendar cal = Calendar.getInstance();
						     cal.setTime(new Date());
						     cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
						     cal.getTime();
						     SimpleDateFormat sdfmt1 = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
						     SimpleDateFormat sdfmt2= new SimpleDateFormat("yyyy-MM-dd");
						     Date dDate = sdfmt1.parse( cal.getTime().toString() );
						     String strOutput = sdfmt2.format( dDate );
						     lineMap.put(configtablerow.get("FieldNames"), strOutput);
						     break;
						}
						case "CoverageWiseExtendedLookup":
						{
							lineMap.put(configtablerow.get("FieldNames"),InputOutputRow.get(ExtendedLoopConfig.get(configtablerow.get("DBColumnNames"))));
							break;
						}
						case "PolicyWiseLookup":
						{
							String LookupKey=InputOutputRow.get(configtablerow.get("DBColumnNames"));
							lineMap.put(configtablerow.get("FieldNames"), this.Lookup(LookupKey,"Value", configtablerow.get("LookupTableName")));
							break;
						}
						case "PolicyWiseTwoLevelLookup":
						{
							//String LookupKey=InputOutputRow.get(configtablerow.get("DBColumnNames"));
							//lineMap.put(configtablerow.get("FieldNames"), this.TwoLevelLookup(LookupKey,"Value", configtablerow.get("LookupTableName")));
							//break;
						}
						case "BCEG_Code":
						{
							String Key1=InputOutputRow.get("BldgEff_class");
							String Key2=InputOutputRow.get("BldgEff_grade");
							lineMap.put(configtablerow.get("FieldNames"), this.TwoLevelLookup(Key1,Key2, configtablerow.get("LookupTableName")));
							break;
						}
						case "CommissionCalculation":
						{
							if(InputOutputRow.get("ProductionChannel").equals("BOP DTC")&&configtablerow.get("FieldNames").equals("Agency_Commission_Amt"))
							{
								if(InputOutputRow.get(ExtendedLoopConfig.get(configtablerow.get("DBColumnNames"))).equals(""))
								{
									lineMap.put(configtablerow.get("FieldNames"),"0");
								}else {
									Double floatvalue = Double.parseDouble(InputOutputRow.get(ExtendedLoopConfig.get(configtablerow.get("DBColumnNames"))))*0.05;
									DecimalFormat df = new DecimalFormat("#.##");
									String value = df.format(floatvalue);
									lineMap.put(configtablerow.get("FieldNames"),value);
								}
							}
							else if(InputOutputRow.get("ProductionChannel").equals("BOP CW")&&(configtablerow.get("FieldNames").equals("Billing_Broker_Commission_Amt")||configtablerow.get("FieldNames").equals("Commission_Amt")))
							{
								if(InputOutputRow.get(ExtendedLoopConfig.get(configtablerow.get("DBColumnNames"))).equals(""))
								{
									lineMap.put(configtablerow.get("FieldNames"),"0");
								}else {
									Double floatvalue = Double.parseDouble(InputOutputRow.get(ExtendedLoopConfig.get(configtablerow.get("DBColumnNames"))))*0.1;
									DecimalFormat df = new DecimalFormat("#.##");
									String value = df.format(floatvalue);
									lineMap.put(configtablerow.get("FieldNames"),value);
								}
							}
							else
							{
								lineMap.put(configtablerow.get("FieldNames"),"0");
							}
							break;
						}
						case "ProductionChannelBasedLookup":
						{
							String LookupKey = InputOutputRow.get("ProductionChannel");
							lineMap.put(configtablerow.get("FieldNames"), this.Lookup(LookupKey,configtablerow.get("DBColumnNames"), configtablerow.get("LookupTableName")));
							break;
						}
						case "DynamicCoverageWiseLookup":
						{
							lineMap.put(configtablerow.get("FieldNames"),this.DynamicLookup(ExtendedLoopConfig.get("CoverageOrder"), InputOutputRow, configtablerow.get("LookupTableName")));
							break;
						}
						case "Concat":
						{
							lineMap.put(configtablerow.get("FieldNames"), configtablerow.get("StaticValues")+InputOutputRow.get(configtablerow.get("DBColumnNames")));
							break;
						}
						case "ISO_State_Exception_Ind_Code":
						{
							if(InputOutputRow.get("Loc_State").equals("MA"))
							{
								lineMap.put(configtablerow.get("FieldNames"), "9");
							}
							else if(InputOutputRow.get("Loc_State").equals("MD"))
							{
								lineMap.put(configtablerow.get("FieldNames"), "8");
							}
							else if(InputOutputRow.get("Loc_State").equals("NJ") && (InputOutputRow.get("YearBuilt").equals("1978")||InputOutputRow.get("YearBuilt").equals("1979")))
							{
								lineMap.put(configtablerow.get("FieldNames"), "9");
							}
							else if(InputOutputRow.get("Loc_State").equals("NJ") && (InputOutputRow.get("YearBuilt").equals("1977")&&InputOutputRow.get("LeadAbatement").equals("No")))
							{
								lineMap.put(configtablerow.get("FieldNames"), "1");
							}
							else if(InputOutputRow.get("Loc_State").equals("NJ") && (InputOutputRow.get("YearBuilt").equals("1977")&&InputOutputRow.get("LeadAbatement").equals("Yes")))
							{
								lineMap.put(configtablerow.get("FieldNames"), "2");
							}
							else
							{
								lineMap.put(configtablerow.get("FieldNames"), "N/A");
							}
							break;
						}
						case "insuredFirstName":
						{
							if(InputOutputRow.get("Entity_Type").equals("Sole Proprietor"))
							{
								lineMap.put(configtablerow.get("FieldNames"), configtablerow.get("StaticValues"));
							}
							break;
						}
						case "chaseReferenceNumber":
						{
							if(InputOutputRow.get("ProductionChannel").equals("BOP DTC"))
							{
								lineMap.put(configtablerow.get("FieldNames"), InputOutputRow.get(configtablerow.get("DBColumnNames")).replace("QOT-", "BOP"));
							}
							break;
						}
						case "CardType":
						{
							if(InputOutputRow.get("ProductionChannel").equals("BOP DTC"))
							{
								lineMap.put(configtablerow.get("FieldNames"), InputOutputRow.get(configtablerow.get("DBColumnNames")));
							}
							break;
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return lineMap;
	}
	
	private String Lookup(String LookupKey,String LookupColumn,String TableName) throws DatabaseException
	{
		String LookupValue="";
		DatabaseOperation LookupTable = new DatabaseOperation();
		String Query="Select * from "+TableName;
		LinkedHashMap<Integer, LinkedHashMap<String, String>> tablePumpinData = LookupTable.GetDataObjects(Query);
		for (Entry<Integer, LinkedHashMap<String, String>> entry : tablePumpinData.entrySet())	
		{
			LinkedHashMap<String, String> LookupRow = entry.getValue();
			if(LookupRow.get("Key").equals(LookupKey))
			{
				LookupValue=LookupRow.get(LookupColumn);
			}
		}
		return LookupValue;
	}
	
	private String TwoLevelLookup(String Key1,String Key2,String TableName) throws DatabaseException
	{
		String LookupValue="";
		DatabaseOperation LookupTable = new DatabaseOperation();
		String Query="Select * from "+TableName;
		LinkedHashMap<Integer, LinkedHashMap<String, String>> tablePumpinData = LookupTable.GetDataObjects(Query);
		for (Entry<Integer, LinkedHashMap<String, String>> entry : tablePumpinData.entrySet())	
		{
			LinkedHashMap<String, String> LookupRow = entry.getValue();
			if(LookupRow.get("Key1").equals(Key1))
			{
				if(LookupRow.get("Key2").equals(Key2))
				{
					LookupValue=LookupRow.get("Value");
				}
			}
		}
		return LookupValue;
	}
	
	private String DynamicLookup(String LookupKey,LinkedHashMap<String, String>InputOutputRow,String TableName) throws DatabaseException
	{
		String LookupValue="";
		DatabaseOperation LookupTable = new DatabaseOperation();
		String Query="Select * from "+TableName;
		LinkedHashMap<Integer, LinkedHashMap<String, String>> tablePumpinData = LookupTable.GetDataObjects(Query);
		for (Entry<Integer, LinkedHashMap<String, String>> entry : tablePumpinData.entrySet())	
		{
			LinkedHashMap<String, String> LookupRow = entry.getValue();
			if(LookupRow.get("Key").equals(LookupKey))
			{
				if(LookupRow.get("Nature").equals("default"))
				{					
					LookupValue=LookupRow.get("Value");					
				}
				else
				{
					LookupValue=InputOutputRow.get(LookupRow.get("Value"));	
				}
			}
		}
		return LookupValue;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void Comparison(String actualTableName, String expectedTableName)
	{
		try
		{
			LinkedHashMap<Integer, LinkedHashMap<String, String>> actualTable = actualMelTable.GetDataObjects("Select * from "+actualTableName);
			LinkedHashMap<Integer, LinkedHashMap<String, String>> expectedTable = expectedMelTable.GetDataObjects("Select * from "+expectedTableName);
			Iterator it1 = actualTable.entrySet().iterator();
			Iterator it2 = expectedTable.entrySet().iterator();
			int i=1;
		    while (it1.hasNext()&&it2.hasNext()) 
		    {		    	
		        Map.Entry pair1 = (Entry) it1.next();
		        LinkedHashMap<String, String> actualRow = (LinkedHashMap<String, String>) pair1.getValue();
		        Map.Entry pair2 = (Entry) it2.next();
		        LinkedHashMap<String, String> expectedRow = (LinkedHashMap<String, String>) pair2.getValue();
		        
		        expectedMelTable.UpdateRow(i,lineToLineComparion(actualRow,expectedRow));
		        i=i+1;
		    }
		    
		}
		catch(Exception e)
		{
			
		}
	}
	
	@SuppressWarnings("rawtypes")
	public LinkedHashMap<String, String> lineToLineComparion (LinkedHashMap<String, String> actualRow,LinkedHashMap<String, String> expectedRow){
		StringBuffer buffer = new StringBuffer();
		Iterator it3 = actualRow.entrySet().iterator();
		Iterator it4 = expectedRow.entrySet().iterator();
		
		while (it3.hasNext()&&it4.hasNext()) 
		{
			 Map.Entry pair3 = (Entry) it3.next();
			 Map.Entry pair4 = (Entry) it4.next();
			 
			 if(pair3.getValue().equals(pair4.getValue()))
			 {
				 
			 }
			 else
			 {
				System.out.println(pair4.getValue()+"=============================="+pair3.getValue());
				buffer=buffer.append(pair4.getKey()).append(" is failed; ");
			 }
		}
        //it1.remove(); // avoids a ConcurrentModificationException
		expectedRow.put("AnalyserResult", buffer.toString());
		System.out.println("comparison Result"+buffer);
		return expectedRow;
	}
	
	public static void main(String args[]) throws DatabaseException, PropertiesHandleException, MacroException, SQLException
	{
		PropertiesHandle configFile = new PropertiesHandle("com.mysql.jdbc.Driver","jdbc:mysql://192.168.84.225:3700/JmeterDB-STARR_ISO?useSSL=false","root","redhat");
		DatabaseOperation.ConnectionSetup(configFile);
		MelProcess processmel = new MelProcess(configFile);
		processmel.generateExpectedMel();
		//processmel.Comparison("MelActual", "MelActual_copy");
		 Calendar calendar = Calendar.getInstance();

		    int lastDate = calendar.getActualMaximum(Calendar.DATE);

		    System.out.println("Date     : " + calendar.getTime());
		    System.out.println("Last Date: " + lastDate);
	}
}
