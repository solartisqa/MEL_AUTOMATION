package com.solartis.test.product.StarrBOP;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.solartis.test.baseClasses.ReportOperation;
import com.solartis.test.configuration.PropertiesHandle;
import com.solartis.test.exception.DatabaseException;
import com.solartis.test.exception.POIException;
import com.solartis.test.util.common.DatabaseOperation;
import com.solartis.test.util.common.ExcelOperationsPOI;

public class ReportOperations extends ReportOperation
{
	protected String excelreportlocation;
	protected String Samplepath;
	protected PropertiesHandle configFile;
	protected LinkedHashMap<Integer, LinkedHashMap<String, String>> AnalyserResult;
	protected LinkedHashMap<Integer, LinkedHashMap<String, String>> RecordCount;
	
	public ReportOperations(String Samplepath, String excelreportlocation)
	{
		this.excelreportlocation=excelreportlocation;
		this.Samplepath=Samplepath;
	}	
	
	public void pumpResulttoReport(String TableName) throws DatabaseException, POIException
	{
		DatabaseOperation db=new DatabaseOperation();
		AnalyserResult=db.GetDataObjects("SELECT Policy_Number,AnalyserResult FROM `"+TableName+"`");
		RecordCount=db.GetDataObjects("SELECT Policy_Number,Coverage,Count(*) AS NoOfCount FROM `"+TableName+"` GROUP BY Policy_Number");
		
		Iterator<Entry<Integer, LinkedHashMap<String,String>>> inputtableiterator = AnalyserResult.entrySet().iterator();
		Iterator<Entry<Integer, LinkedHashMap<String,String>>> inputtableiterator1 = RecordCount.entrySet().iterator();
		ExcelOperationsPOI ob=new ExcelOperationsPOI(excelreportlocation);
		ob.getsheets("Report");
		Date today=new Date();
		ob.write_data(9, 4,today);
		int	row=12;
		int si_no=1;
		while (inputtableiterator1.hasNext()) 
		{
			 Entry<Integer, LinkedHashMap<String, String>> inputentry = inputtableiterator.next();
			 LinkedHashMap<String, String> inputrow = inputentry.getValue();
			
			    ob.write_data(row, 8,si_no );
			    ob.write_data(row,9,inputrow.get("Policy_Number"));
			    ob.write_data(row,10,Integer.parseInt(inputrow.get("NoOfCount")));
				
			 row++;
			 si_no++;
			 
		}
		si_no=1;
		while (inputtableiterator.hasNext()) 
		{
			 Entry<Integer, LinkedHashMap<String, String>> inputentry = inputtableiterator.next();
			 LinkedHashMap<String, String> inputrow = inputentry.getValue();
			
			    ob.write_data(row, 2,si_no );
			    ob.write_data(row,3,inputrow.get("Policy_Number"));
			    ob.write_data(row,4,Integer.parseInt(inputrow.get("Coverage")));
			    ob.write_data(row,5,Integer.parseInt(inputrow.get("AnalyserResult")));
				
			 row++;
			 si_no++;
			 
		}
		ob.refresh();
		ob.saveAs(excelreportlocation);
	}
	
	public void importExpectedActualMELFeeds() throws FileNotFoundException, DatabaseException, IOException
	{
		ExportToExcelTable("Select * from", excelreportlocation, "Expected");
	    ExportToExcelTable("Select * from", excelreportlocation, "Actual");
	}
}
