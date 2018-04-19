package com.solartis.test.melAutomation;

import java.sql.SQLException;
import java.util.Calendar;

import com.solartis.test.configuration.PropertiesHandle;
import com.solartis.test.exception.DatabaseException;
import com.solartis.test.exception.MacroException;
import com.solartis.test.exception.PropertiesHandleException;
import com.solartis.test.product.StarrBOP.MelProcess;
import com.solartis.test.util.common.DatabaseOperation;

public class MainClass 
{
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
