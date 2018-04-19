package com.solartis.test.util.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DirectoryManipulation
{
public static String zipFiles(String zipFile, String srcDir)
{
	try
	{
		File srcFile = new File(srcDir);
		File[] files = srcFile.listFiles();
		FileOutputStream fos = new FileOutputStream(zipFile);

		ZipOutputStream zos = new ZipOutputStream(fos);

		for (int i = 0; i < files.length; i++) 
		{

			byte[] buffer = new byte[1024];

			FileInputStream fis = new FileInputStream(files[i]);

			zos.putNextEntry(new ZipEntry(files[i].getName()));

			int length;

			while ((length = fis.read(buffer)) > 0)
			{
				zos.write(buffer, 0, length);
			}

			zos.closeEntry();

			fis.close();

		}
		zos.close();
	}
	catch (Exception e)
	{
		return e.getMessage();}
    	return "Successfully created the zip file"+zipFile;
	}


	public  static void deleteFileFromDirectory(String DirName)
	{
		File directory = new File(DirName);

		File[] files = directory.listFiles();

		for (File file : files)

		{
	 
			if (!file.delete())
			{ 
				System.out.println("Failed to delete "+file);
			}
		} 
	}

//=====================================================Zipping Fiolders===================================================
	static public void zipFolder(String srcFolder, String destZipFile) throws Exception
	{
		ZipOutputStream zip = null;
		FileOutputStream fileWriter = null;

		fileWriter = new FileOutputStream(destZipFile);
		zip = new ZipOutputStream(fileWriter);

		addFolderToZip("", srcFolder, zip);
		zip.flush();
		zip.close();
	}

  @SuppressWarnings("resource")
static private void addFileToZip(String path, String srcFile, ZipOutputStream zip)
      throws Exception 
  {

    File folder = new File(srcFile);
    if (folder.isDirectory())
    {
      addFolderToZip(path, srcFile, zip);
    }
    else
    {
      byte[] buf = new byte[1024];
      int len;
      FileInputStream in = new FileInputStream(srcFile);
      zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
      while ((len = in.read(buf)) > 0)
      {
        zip.write(buf, 0, len);
      }
    }
  }

  static private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip)
      throws Exception
  {
    File folder = new File(srcFolder);

    for (String fileName : folder.list()) 
    {
      if (path.equals("")) 
      {
        addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
      } 
      else
      {
        addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip);
      }
    }
  }
//================================================================================================


}
