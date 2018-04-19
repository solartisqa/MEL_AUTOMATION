package com.solartis.test.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

import com.solartis.test.exception.DatabaseException;
import com.solartis.test.exception.PropertiesHandleException;
import com.solartis.test.util.common.DatabaseOperation;

public class PropertiesHandle extends Properties
{
	private static final long serialVersionUID = 1L;
	protected String path = null;
	protected String UserName;
	protected String JDBC_DRIVER;
	protected String DB_URL;
	protected String USER;
	protected String password;
	protected String queryresult;

	static DatabaseOperation ConfigQuery = new DatabaseOperation();
			
	    public PropertiesHandle( String JDBC_DRIVER, String DB_URL, String USER, String password) throws DatabaseException, PropertiesHandleException
		{			
			this.JDBC_DRIVER=JDBC_DRIVER;
			this.DB_URL=DB_URL;
			this.USER=USER;
			this.password=password;			
			WriteProperty(UserName);			
		}
		
		protected void WriteProperty(String UserName) throws DatabaseException, PropertiesHandleException
		{
			DatabaseOperation.ConnectionSetup(JDBC_DRIVER, DB_URL, USER, password);			
			this.put("melconfig", "Select * from MelConfig");
			this.put("melExpected", "Select * from MelActual");
			this.put("jdbc_driver",this.JDBC_DRIVER);
			this.put("db_url", this.DB_URL);
			this.put("db_username", this.USER);
			this.put("db_password", this.password);
		    DatabaseOperation.CloseConn();		 
		}
	
		protected void ActualAndStatus(String Actual, String Status)// FUNCTION FOR ACTUAL AND STATUS OCCURANCE
		{
			this.put("actual", Actual);
			this.put("status", Status);
		}		
		
		public PropertiesHandle(String path) throws PropertiesHandleException
		{
			this.path = path;
			
			FileInputStream configuration = null;
			
			try 
			{
				configuration = new FileInputStream(path);
			} 
			catch (FileNotFoundException e) 
			{
				throw new PropertiesHandleException("CONFIGURATION FILE PATH DOES NOT CONTAINS CONFIG FILE", e);
			}
			try 
			{
				this.load(configuration);
			} 
			catch (IOException e) 
			{
				throw new PropertiesHandleException("ERROR IN LOADING A CONFIG FILE", e);
			}
		}
		
		public void store(String newpath) throws PropertiesHandleException
		{
			Writer writer = null;
			try 
			{
				 writer = new FileWriter(newpath);
			} 
			catch (IOException e) 
			{
				throw new PropertiesHandleException("ERROR IN WRITING A CONFIG FILE", e);
			}
			try 
			{
				this.store(writer, "File saved");
			} 
			catch (IOException e) 
			{
				throw new PropertiesHandleException("ERROR IN STORING A CONFIG FILE", e);
			};
		}
		
		public void store()
		{
			Writer writer = null;
			try 
			{
				 writer = new FileWriter(this.path);
			} catch (IOException e) 
			{
				e.printStackTrace();
			}
			try 
			{
				this.store(writer, "File saved");
			} catch (IOException e) 
			{
				e.printStackTrace();
			};
		}
}