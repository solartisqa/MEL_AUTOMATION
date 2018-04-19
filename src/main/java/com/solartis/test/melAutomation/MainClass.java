package com.solartis.test.melAutomation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;

import com.solartis.test.baseClasses.MelProcessBaseClass;
import com.solartis.test.baseClasses.ReportOperation;
import com.solartis.test.configuration.PropertiesHandle;
import com.solartis.test.exception.DatabaseException;
import com.solartis.test.exception.MacroException;
import com.solartis.test.exception.POIException;
import com.solartis.test.exception.PropertiesHandleException;
import com.solartis.test.product.StarrBOP.MelProcess;
import com.solartis.test.product.StarrBOP.ReportOperations;
import com.solartis.test.util.common.DatabaseOperation;

public class MainClass 
{
	public static void main(String args[]) throws DatabaseException, PropertiesHandleException, MacroException, SQLException, FileNotFoundException, POIException, IOException
	{
		PropertiesHandle configFile = new PropertiesHandle("com.mysql.jdbc.Driver","jdbc:mysql://192.168.84.225:3700/JmeterDB-STARR_ISO?useSSL=false","root","redhat");
		DatabaseOperation.ConnectionSetup(configFile);
		MelProcessBaseClass processmel = new MelProcess(configFile);
		
		//processmel.generateExpectedMel();
		processmel.Comparison("MelActual", "MelActual_copy");
		ReportOperation report = new ReportOperations("E:\\RestFullAPIDeliverable\\Devolpement\\admin\\STARR-ISO\\Mel\\SampleReport\\MELAnalysisReport.xls", "E:\\RestFullAPIDeliverable\\Devolpement\\admin\\STARR-ISO\\Mel\\Result\\");
		 Calendar calendar = Calendar.getInstance();

		    int lastDate = calendar.getActualMaximum(Calendar.DATE);

		    System.out.println("Date     : " + calendar.getTime());
		    System.out.println("Last Date: " + lastDate);
	}
}
