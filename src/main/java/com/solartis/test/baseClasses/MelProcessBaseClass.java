package com.solartis.test.baseClasses;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.solartis.test.exception.DatabaseException;
import com.solartis.test.exception.PropertiesHandleException;
import com.solartis.test.util.common.DatabaseOperation;

public abstract class MelProcessBaseClass 
{
	protected DatabaseOperation expectedMelTable;
	protected DatabaseOperation actualMelTable;
	public abstract void importActual() throws DatabaseException, PropertiesHandleException;
	public abstract void generateExpectedMel() throws DatabaseException, SQLException;
	public abstract LinkedHashMap<String, String> GeneratLine(LinkedHashMap<String, String> ExtendedLoopConfig, LinkedHashMap<String, String>InputOutputRow);
	public String Lookup(String LookupKey,String LookupColumn,String TableName) throws DatabaseException
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
	
	public String TwoLevelLookup(String Key1,String Key2,String TableName) throws DatabaseException
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
	
	public String DynamicLookup(String LookupKey,LinkedHashMap<String, String>InputOutputRow,String TableName) throws DatabaseException
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
			String actualQuery ="Select * from "+actualTableName;
			String expectedQuery = "Select * from "+expectedTableName;
			LinkedHashMap<Integer, LinkedHashMap<String, String>> actualTable = actualMelTable.GetDataObjects(actualQuery);
			LinkedHashMap<Integer, LinkedHashMap<String, String>> expectedTable = expectedMelTable.GetDataObjects(expectedQuery);
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
			e.printStackTrace();
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
}
