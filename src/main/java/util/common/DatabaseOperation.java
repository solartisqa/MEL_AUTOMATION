package util.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import com.mysql.jdbc.PreparedStatement;

import test.Configuration.PropertiesHandle;
import test.exception.DatabaseException;
import test.exception.POIException;


public class DatabaseOperation
{
	private static Connection conn = null;
	private static String JDBC_DRIVER = null;
	private static String DB_URL =null;
	private static String USER=null;
	private static String PASS =null;
	protected String query = null;
	protected Statement stmt = null;
	protected ResultSet rs = null;
	protected int rs_row = 1;
	protected LinkedHashMap<Integer, LinkedHashMap<String, String>> table = null;
	protected ResultSetMetaData meta = null;
	
	public static Connection ConnectionSetup(PropertiesHandle config) throws DatabaseException 
	{
		JDBC_DRIVER =config.getProperty("jdbc_driver");
		DB_URL = config.getProperty("db_url");
		USER=config.getProperty("db_username");
		PASS =config.getProperty("db_password");
		if(conn == null)
		{
			try 
			{
				Class.forName(JDBC_DRIVER);
			} 
			catch (ClassNotFoundException e) 
			{
				throw new DatabaseException("ERROR IN JDBC_DRIVER : " + JDBC_DRIVER, e);
			}
			try 
			{
				conn = DriverManager.getConnection(DB_URL,USER,PASS);
			} 
			catch (SQLException e) 
			{
				throw new DatabaseException("ERROR IN DB - URL / USERNAME / PASSWORD", e);	
			}	
		}	
		return conn;
	}
	
	public static Connection ConnectionSetup(String JDBC_DRIVER, String DB_URL, String USER, String password) throws DatabaseException 
	{
		if(conn == null)
		{
			
			try 
			{
				Class.forName(JDBC_DRIVER);
			} 
			catch (ClassNotFoundException e) 
			{
				throw new DatabaseException("ERROR IN JDBC_DRIVER : " + JDBC_DRIVER, e);
			}
			try 
			{
				conn = DriverManager.getConnection(DB_URL,USER,password);
			} 
			catch (SQLException e) 
			{
				throw new DatabaseException("ERROR IN DB - URL / USERNAME / PASSWORD", e);	
			}	
		}
		return conn;
	}
	
	public static void CloseConn() throws DatabaseException
	{
		try 
		{
			conn.close();
		} 
		catch (SQLException e) 
		{
			throw new DatabaseException("PROBLEM WITH CLOSING DB-CONNECTION", e);
		}
		conn = null;
	}
	
	public LinkedHashMap<Integer, LinkedHashMap<String, String>> GetDataObjects(String query) throws DatabaseException
	{
		this.query = query;
		//System.out.println(this.query);
		LinkedHashMap<String, String> row = null;
		try 
		{
			stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
		    rs =    stmt.executeQuery(this.query);
		    table = new LinkedHashMap<Integer, LinkedHashMap<String, String>>();
	        meta = rs.getMetaData();        
	        while (rs.next())
	        {
	        	row = new LinkedHashMap<String, String>();
	            for (int columnIterator = 1; columnIterator <= meta.getColumnCount(); columnIterator++) 
	            {
	                String key = meta.getColumnName(columnIterator);
	                String value = rs.getString(key);
	                row.put(key, value);
	            }
	            table.put(rs_row, row);
	            rs_row = rs_row + 1;   
	        } 
	        return table;  
		} 
		catch (SQLException e) 
		{
			System.out.println(e);
			throw new DatabaseException("PROBLEM WITH RESULT-SET OBTAINED FROM DB",e);
			
		}		
	}
	
	public void UpdateRow(Integer rowNumber, LinkedHashMap<String, String> row) throws DatabaseException
	{		
		try 
		{
			rs.first();
		    int rowIterator = 1;
			do
			{
				if(rowNumber == rowIterator)
			    {
					for (int i = 1; i <= meta.getColumnCount(); i++) 
					{  
				       rs.updateString(meta.getColumnName(i), row.get(meta.getColumnName(i)));     
				    }
					rs.updateRow();
			    } 			 
			    rowIterator++;
			 }while (rs.next());	
		}			
		catch (SQLException e) 
		{
			throw new DatabaseException("PROBLEM WITH UPDATE ROW IN DB", e);
		}
	}
	
	@SuppressWarnings("unused")
	public void insertRow(LinkedHashMap<String, String> row) throws SQLException
	{
		for (Entry<String, String> entry : row.entrySet())	
		{
			//System.out.println(entry.getValue());			
		}
		String insterQuery = "INSERT INTO MelActual(temp1) VALUES(temp2)";	
		
		StringBuffer temp1 = new StringBuffer();
		StringBuffer temp2 = new StringBuffer();
		for (Entry<String, String> entry : row.entrySet())	
		{
			temp2=temp2.append("'").append(entry.getValue()).append("'").append(",");
			temp1=temp1.append(entry.getKey()).append(",");
		}
		insterQuery=insterQuery.replace("temp1", temp1.substring(0, temp1.length() - 1)).replace("temp2", temp2.substring(0, temp2.length() - 1));
		System.out.println(insterQuery);
		java.sql.PreparedStatement ps = conn.prepareStatement(insterQuery);
		ps.executeUpdate();
	}
	
	public void UpdateTable(LinkedHashMap<Integer, LinkedHashMap<String, String>> table) throws DatabaseException
	{
		this.table = table;
		LinkedHashMap<String, String> row = null;
		try 
		{
			rs.first();
		    int rowIterator = 1;
			do
			{
				for (int columnIterator = 1; columnIterator <= meta.getColumnCount(); columnIterator++) 
				{  
			       row = table.get(rowIterator);
			       rs.updateString(meta.getColumnName(columnIterator), row.get(meta.getColumnName(columnIterator)));
			    }
			 
			    rs.updateRow();
			    rowIterator++;
			 }while (rs.next());
		}	
		
		catch (SQLException e) 
		{
			throw new DatabaseException("PROBLEM WITH UPDATE ROW IN DB", e);
		}
	}
	
	public ResultSet GetQueryResultsSet(String query) throws DatabaseException
	{
		this.query = query;
		try 
		{
			stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
		    rs =    stmt.executeQuery(this.query);
		    rs.first();
		} 
		catch (SQLException e) 
		{
			throw new DatabaseException("PROBLEM WITH RESULT-SET OBTAINED FROM DB",e);
		}
		return rs;
	}
	
	public void createTable(String query) throws SQLException
	{
		this.query=query;
		 stmt = conn.createStatement();
		 //System.out.println(this.query);
		 stmt.execute(this.query);
	}
	public int countRow(String TableName) throws DatabaseException
	{
		int rowcount =0;
		try
		{
			Statement stat = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
			ResultSet resultSet = stat.executeQuery("select count(*) from "+TableName);
			 while (resultSet.next()) 
			 {
				 rowcount= resultSet.getInt(1);
			 }
		} catch (Exception e) 
		{
			e.printStackTrace();
			//throw new DatabaseException("PROBLEM in getting number of rows in mel expected table",e);
		}
		return rowcount;
	}
	
	public String lookupValue(String TableName,String lookupKey) throws DatabaseException
	{
		String lookupValue ="";
		try
		{
			Statement stat = conn.createStatement();
			ResultSet resultSet = stat.executeQuery("select lookupValue from "+TableName+" where lookupKey="+lookupKey);
			 while (resultSet.next()) 
			 {
				 lookupValue= resultSet.getString(1);
			 }
		} catch (Exception e) 
		{
			e.printStackTrace();
			throw new DatabaseException("PROBLEM in getting number of rows in mel expected table",e);
		}
		return lookupValue;
	}
	
	
	@SuppressWarnings("resource")
	public static void ExportToExcelTable(String Query,String FileToExport,String Sheet) throws DatabaseException, SQLException, FileNotFoundException, IOException
	{
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
		      //System.out.println(columnName);
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
		try
	     {
	          FileOutputStream out = new FileOutputStream(FileToExport);
	          workBook.write(out);
	          out.close();
	          System.out.println("first_excel.xls written successfully on disk.");
	      } 
	      catch (Exception e) 
	      {
	          e.printStackTrace();
	      }
		//System.out.println();
	}
	

	
	public void ImportDatatoDB(String filepath,Connection conn,String tableName,String SheetName,String Operation) throws IOException, SQLException, ClassNotFoundException, POIException
	{
		ExcelOperationsPOI xl=new ExcelOperationsPOI(filepath);
		String sql=null;
		DatabaseOperation db=new DatabaseOperation();
		
		xl.getsheets(SheetName);
		int n=xl.getTotColumns();
		int noOfRows=xl.getTotRows();
		int s=xl.getfirstRowNo();
		
		String[] Columns=new String[n];
		String insertString="";
		String values="";
		
		for(int i=s;i<n;i++)
		{
			if(Operation.equalsIgnoreCase("CREATE"))
			{
			String str1=xl.readData(1,i).toString();
			String str2=xl.readData(0,i).toString();
			Columns[i]=str1+" "+str2;
			
			insertString=insertString+xl.read_data(1,i)+",";
			}
			else
			{
				insertString=insertString+xl.read_data(0,i)+",";
			}
			values=values+"?,";
		}
		
		String ColumnString=String.join(",", Columns);
		String insertStrings=insertString.substring(0,(insertString.length()-1));
		String ValueStrings=null;
		int dataRow;
		if(Operation.equalsIgnoreCase("CREATE"))
		{
			dataRow=2;
			sql = "CREATE TABLE "+ tableName +"("+ColumnString+")";
			db.createTable(sql);
		}
		else if(Operation.equalsIgnoreCase("ALTER"))
		{
			dataRow=2;
			sql= "ALTER TABLE "+ tableName +" ADD ("+ColumnString+")";
			db.createTable(sql);
		}
		else
		{
			dataRow=1;
		}
		ValueStrings=values.substring(0,(values.length()-1));

		for(int row=dataRow;row<=noOfRows;row++)
		{
			String sql1 = "INSERT INTO "+ tableName+"("+insertStrings+")"+" VALUES("+ValueStrings+")";
			
			PreparedStatement insertStatement =(PreparedStatement) conn.prepareStatement(sql1);
			for(int col=0;col<n;col++)
			{
				insertStatement.setString(col+1,xl.readData(row, col).toString()); 
				
			}
			insertStatement.executeUpdate();
		}
		
	}
	
	public  void truncateTable(String tablename) throws SQLException
	{
		stmt = conn.createStatement();
		String query="TRUNCATE "+tablename;
		stmt.executeUpdate(query);
	}
	
	public void insetRowWithSNO(String OutputTableName,String inputTableName) throws SQLException
	{
		stmt = conn.createStatement();
		String query1 ="INSERT INTO "+OutputTableName+" (`S_No`,`Testdata`,`Flag_for_execution`) SELECT `S_No`,`Testdata`,`Flag_for_execution` FROM "+inputTableName;
		stmt.executeUpdate(query1);
	}
	
	
	public static void main(String args[]) throws DatabaseException, SQLException, FileNotFoundException, IOException, ClassNotFoundException, POIException
	{
		//Connection conn=DatabaseOperation.ConnectionSetup("com.mysql.jdbc.Driver", "jdbc:mysql://192.168.84.225:3700/Starr_DTC_Development_ADMIN", "root", "redhat");
	    //DatabaseOperation db=new DatabaseOperation();
	   // db.truncateTable("INPUT_DTC_Rating_SinglePlan");
	    //db.insetRowWithSNO("OUTPUT_DTC_Rating_SinglePlan","INPUT_DTC_Rating_SinglePlan");
		//db.ImportDatatoDB("R:\\RestFullAPIDeliverable\\Devolpement\\admin\\STARR-DTC\\RatingServiceSinglePlan\\Testdata\\QARelease.xls",conn,"INPUT_DTC_Rating_SinglePlan","Sheet1","Import");

		
	}
	
}
