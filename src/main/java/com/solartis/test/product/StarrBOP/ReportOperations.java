package com.solartis.test.product.StarrBOP;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
	
	public ReportOperations(String Samplepath, String excelreportlocation) throws POIException, DatabaseException, FileNotFoundException, IOException
	{
		Date date = new Date();
		String DateandTime = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(date);
		this.excelreportlocation=excelreportlocation+"AnalysisReport "+DateandTime+".xls";;
		this.Samplepath=Samplepath;
		this.generateReport(Samplepath, this.excelreportlocation);
		this.pumpResulttoReport("MelActual");
		this.importExpectedActualMELFeeds();
	}	
	
	public void pumpResulttoReport(String TableName) throws DatabaseException, POIException
	{
		DatabaseOperation db=new DatabaseOperation();
		AnalyserResult=db.GetDataObjects("SELECT Policy_Number,Coverage,AnalyserResult FROM `"+TableName+"`");
		RecordCount=db.GetDataObjects("SELECT Policy_Number,Count(*) AS `NoOfCount` FROM `"+TableName+"` GROUP BY Policy_Number");
		
		Iterator<Entry<Integer, LinkedHashMap<String,String>>> inputtableiterator = AnalyserResult.entrySet().iterator();
		Iterator<Entry<Integer, LinkedHashMap<String,String>>> inputtableiterator1 = RecordCount.entrySet().iterator();
		ExcelOperationsPOI ob=new ExcelOperationsPOI(excelreportlocation);
		ob.getsheets("Report");
		Date today=new Date();
		System.out.println(today);
		ob.write_data(7, 4,today);
		int	row=10;
		int si_no=1;
		while (inputtableiterator1.hasNext()) 
		{
			 Entry<Integer, LinkedHashMap<String, String>> inputentry = inputtableiterator1.next();
			 LinkedHashMap<String, String> inputrow = inputentry.getValue();
			
			    ob.write_data(row, 7,si_no );
			    ob.write_data(row,8,inputrow.get("Policy_Number"));
			    ob.write_data(row,9,Integer.parseInt(inputrow.get("NoOfCount")));
				
			 row++;
			 si_no++;
			 
		}
		si_no=1;
		row=10;
		while (inputtableiterator.hasNext()) 
		{
			 Entry<Integer, LinkedHashMap<String, String>> inputentry = inputtableiterator.next();
			 LinkedHashMap<String, String> inputrow = inputentry.getValue();
			
			    ob.write_data(row, 2,si_no );
			    ob.write_data(row,3,inputrow.get("Policy_Number"));
			    ob.write_data(row,4,inputrow.get("Coverage"));
			    ob.write_data(row,5,inputrow.get("AnalyserResult"));
				
			 row++;
			 si_no++;
			 
		}
		ob.refresh();
		ob.saveAs(excelreportlocation);
	}
	
	public void importExpectedActualMELFeeds() throws FileNotFoundException, DatabaseException, IOException
	{
		ExportToExcelTable("Select * from MelActual", excelreportlocation, "Expected");
	    ExportToExcelTable("Select * from MelActual_copy", excelreportlocation, "Actual");
	}
}
