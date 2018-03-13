package MelAutomation.MelAutomation;

import java.sql.SQLException;
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
		PropertiesHandle DB2 = new PropertiesHandle("com.mysql.jdbc.Driver","jdbc:mysql://192.168.84.225:3700/Starr_ISO_Development_ADMIN?useSSL=false","root","redhat");
		DatabaseOperation.ConnectionSetup(DB2);
	}
	
	public void generateExpectedMel() throws DatabaseException, SQLException
	{
		LinkedHashMap<Integer, LinkedHashMap<String, String>> OutputTable=inputoutputtable.GetDataObjects("Select * from OUTPUT_Quote_ISO_V4");
		for(Entry<Integer, LinkedHashMap<String, String>> entry1 : OutputTable.entrySet())
		{
			LinkedHashMap<String, String> InputOutputRow = entry1.getValue();
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
							lineMap.put(configtablerow.get("FieldNames"), "");
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
							lineMap.put(configtablerow.get("FieldNames"), this.Lookup(LookupKey, configtablerow.get("LookupTableName")));
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
	
	private String Lookup(String LookupKey,String TableName) throws DatabaseException
	{
		String LookupValue="";
		DatabaseOperation LookupTable = new DatabaseOperation();
		LinkedHashMap<Integer, LinkedHashMap<String, String>> tablePumpinData = LookupTable.GetDataObjects("Select * from "+TableName);
		for (Entry<Integer, LinkedHashMap<String, String>> entry : tablePumpinData.entrySet())	
		{
			LinkedHashMap<String, String> LookupRow = entry.getValue();
			if(LookupRow.get("Key").equals(LookupKey))
			{
				LookupValue=LookupRow.get("Value");
			}
		}
		return LookupValue;
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void Comparison(String actualTableName, String expectedTableName)
	{
		try
		{
			StringBuffer buffer = new StringBuffer();
			
			LinkedHashMap<Integer, LinkedHashMap<String, String>> actualTable = actualMelTable.GetDataObjects("Select * from "+actualTableName);
			LinkedHashMap<Integer, LinkedHashMap<String, String>> expectedTable = expectedMelTable.GetDataObjects("Select * from "+expectedTableName);
			Iterator it1 = actualTable.entrySet().iterator();
			Iterator it2 = expectedTable.entrySet().iterator();
		    while (it1.hasNext()&&it2.hasNext()) 
		    {
		        Map.Entry pair1 = (Entry) it1.next();
		        LinkedHashMap<String, String> actualRow = (LinkedHashMap<String, String>) pair1.getValue();
		        Map.Entry pair2 = (Entry) it2.next();
		        LinkedHashMap<String, String> expectedRow = (LinkedHashMap<String, String>) pair2.getValue();
		        
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
						 buffer=buffer.append(pair4.getKey()).append("is failed");
					 }
				}
		        //it1.remove(); // avoids a ConcurrentModificationException
				System.out.println("comparison Result"+buffer);
		    }
		    
		}
		catch(Exception e)
		{
			
		}
	}
	
	public static void main(String args[]) throws DatabaseException, PropertiesHandleException, MacroException, SQLException
	{
		PropertiesHandle configFile = new PropertiesHandle("com.mysql.jdbc.Driver","jdbc:mysql://192.168.84.225:3700/Starr_ISO_Development_ADMIN?useSSL=false","root","redhat");
		DatabaseOperation.ConnectionSetup(configFile);
		MelProcess processmel = new MelProcess(configFile);
		//processmel.generateExpectedMel();
		processmel.Comparison("MelActual", "MelActual_copy");
		 Calendar calendar = Calendar.getInstance();

		    int lastDate = calendar.getActualMaximum(Calendar.DATE);

		    System.out.println("Date     : " + calendar.getTime());
		    System.out.println("Last Date: " + lastDate);
	}
}
