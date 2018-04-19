package com.solartis.test.baseClasses;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.solartis.test.exception.DatabaseException;
import com.solartis.test.exception.POIException;
import com.solartis.test.util.common.DatabaseOperation;
import com.solartis.test.util.common.ExcelOperationsPOI;

public abstract class ReportOperation 
{
	public void generateReport(String Samplepath,String excelreportlocation) throws POIException
	{
		ExcelOperationsPOI sample=new ExcelOperationsPOI(Samplepath);
		sample.Copy(Samplepath, excelreportlocation);
		sample.save();
	}
	public abstract void pumpResulttoReport(String TableName) throws DatabaseException, POIException;
	public abstract void importExpectedActualMELFeeds() throws FileNotFoundException, DatabaseException, IOException;
	@SuppressWarnings("resource")
	public void ExportToExcelTable(String Query,String FileToExport,String Sheet) throws DatabaseException, FileNotFoundException, IOException
	{
		
		try
		{
			System.out.println("Exporting Report with Test cases to Excel");
			DatabaseOperation db=new DatabaseOperation();
			ResultSet rs=null;
			HSSFWorkbook workBook=null;
			HSSFSheet sheet =null;
			rs=db.GetQueryResultsSet(Query);
			File file = new File(FileToExport);
			if(!file.exists())                               //Creation of Workbook and Sheet
			{
				workBook =new HSSFWorkbook();
			}
			else
			{
				workBook = new HSSFWorkbook(new FileInputStream(FileToExport));
			}
			sheet = workBook.createSheet(Sheet);
                                                                                         //import columns to Excel
			ResultSetMetaData metaData=rs.getMetaData();
			int columnCount=metaData.getColumnCount();
			ArrayList<String> columns = new ArrayList<String>();
			for (int i = 1; i <= columnCount; i++) 
			{
				String columnName = metaData.getColumnName(i);
				columns.add(columnName);
			}
		    
			HSSFRow row = sheet.createRow(0);
			int  Fieldcol=0; 
			for (String columnName : columns) 
			{
				row.createCell(Fieldcol).setCellValue(columnName);
				Fieldcol++;
			}
                                                            //import column values to Excel	
			int ValueRow=1;
			do
			{
				int Valuecol=0;
				HSSFRow valrow = sheet.createRow(ValueRow);
				for (String columnName : columns)
				{
					String value = rs.getString(columnName);
					valrow.createCell(Valuecol).setCellValue(value);
					Valuecol++;
				}
				ValueRow++;
			} while (rs.next());
		                                                    //Save the Details and close the File
		
	          FileOutputStream out = new FileOutputStream(FileToExport);
	          workBook.write(out);
	          out.close();
	          System.out.println("REPORT GENERATED SUCCESSFULLY ON DISK");
		 }
	     catch (Exception e) 
	     {
	    	 System.out.println("Error in Exporting the Testcase with Results");	 
	       e.printStackTrace();
	     }
	}
}
