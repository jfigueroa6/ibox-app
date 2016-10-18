package edu.csupomona.cs585.ibox;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.csupomona.cs585.ibox.sync.GoogleDriveFileSyncManager;
import edu.csupomona.cs585.ibox.sync.GoogleDriveServiceProvider;

public class IntegrationTest {
	private final static String directory = "D:\\School\\CS 585\\iBox_test",
								filename = "Test_File.txt";
	private static GoogleDriveFileSyncManager fileSyncManager;
	private static WatchDir watchDir;
	private static Thread watchThread;
	
	//@BeforeClass
	public static void testSetup() throws IOException {
		fileSyncManager = new GoogleDriveFileSyncManager(
        		GoogleDriveServiceProvider.get().getGoogleDriveClient());

        // register directory and process its events
		watchDir = new WatchDir(Paths.get(directory), fileSyncManager);
		watchThread = new Thread(new WatchDirThread());
		watchThread.start();
	}
	
	//@AfterClass
	public static void testBreakdown() {
		//Not safe, but test is finishing anyway
		watchThread.interrupt();
	}
	
	//@Test
	public void TestIntegration(){
		try {
	        //Create file in Watch Directory and verify file is transferred to Google Drive
	        System.out.println("Add file");
			PrintStream output = new PrintStream(new File(directory + "\\" + filename));
			output.println("Test file sync to Google Drive");
			output.close();
			pauseExecution(8000);			//Wait 5 seconds
			Assert.assertNotNull(fileSyncManager.getFileId(filename));
			
			//Update the file
			System.out.println("Update file");
			output = new PrintStream(new File(directory + "\\" + filename));
			output.append("Additional Info.");
			output.close();
			pauseExecution(8000);
			
			//Delete the file
			System.out.println("Delete file");
			Files.delete(Paths.get(directory + "\\" + filename));
			pauseExecution(8000);
			Assert.assertNull(fileSyncManager.getFileId(filename));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assert.fail("IO Exception");
		}
	}
	
	private void pauseExecution(long waitTime) {
		long endPause = System.currentTimeMillis() + waitTime;
		while (System.currentTimeMillis() < endPause);
	}
	
	//Executes a watch
	private static class WatchDirThread implements Runnable {
		@Override
		public void run() {
			watchDir.processEvents();
		}
		
	}
}
