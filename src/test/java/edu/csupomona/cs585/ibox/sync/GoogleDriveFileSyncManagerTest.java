package edu.csupomona.cs585.ibox.sync;

import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.Delete;
import com.google.api.services.drive.Drive.Files.Insert;
import com.google.api.services.drive.Drive.Files.List;
import com.google.api.services.drive.Drive.Files.Update;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import org.junit.Assert;

public class GoogleDriveFileSyncManagerTest {
	private final static String filename = "Test File",
								fileId = "Test";
	
	private static java.io.File mockIOFile;
	private static File googleFile;
	private static Drive mockService;
	private static Drive.Files mockFiles;
	private static Insert mockInsert;
	private static Update mockUpdate;
	private static Delete mockDelete;
	private static Drive.Files.List mockList;
	private static FileList fileList;
	
	private static GoogleDriveFileSyncManager googleSync;
	
	@BeforeClass
	public static void setupTestClass() throws IOException {
		mockIOFile = mock(java.io.File.class, Mockito.RETURNS_SMART_NULLS);
		when(mockIOFile.getName()).thenReturn(filename);
		
		googleFile = new File();
		googleFile.setTitle(filename);
		googleFile.setId(fileId);
		
		//Service mocks
		mockInsert = mock(Insert.class);
		when(mockInsert.execute()).thenReturn(googleFile);
		mockList = mock(Drive.Files.List.class);
		mockFiles = mock(Drive.Files.class, Mockito.RETURNS_SMART_NULLS);
		when(mockFiles.insert(any(File.class), any(FileContent.class))).thenReturn(mockInsert);		
		when(mockFiles.list()).thenReturn(mockList);
		mockService = mock(Drive.class, Mockito.RETURNS_SMART_NULLS);
		when(mockService.files()).thenReturn(mockFiles);
		
		googleSync = new GoogleDriveFileSyncManager(mockService);
	}
	
	@Test
	public void testAddFile() throws IOException {
		googleSync.addFile(mockIOFile);
		verify(mockInsert).execute();
	}
	
	@Test
	public void testGetFileId_Found() throws IOException {
		goodList();
		
		Assert.assertTrue(fileId.equalsIgnoreCase(googleSync.getFileId(filename)));
	}
	
	@Test
	public void testGetFileId_NotFound() throws IOException {
		badList();
		
		Assert.assertNull(googleSync.getFileId(filename));
	}
	
	@Test
	public void testUpdateFileNoFile() throws IOException {		
		badList();
		
		googleSync.updateFile(mockIOFile);
		verify(mockInsert, times(2)).execute();
	}
	
	@Test
	public void testUpdateFile() throws IOException {
		//Mock update
		mockUpdate = mock(Update.class);
		when(mockUpdate.execute()).thenReturn(googleFile);
		when(mockFiles.update(anyString(), any(File.class), any(AbstractInputStreamContent.class))).thenReturn(mockUpdate);
		
		//Good list of files
		goodList();
		
		googleSync.updateFile(mockIOFile);
		verify(mockUpdate).execute();
	}
	
	@Test
	public void testDeleteFile() throws IOException {
		mockDelete = mock(Delete.class);
		when(mockDelete.execute()).thenReturn(null);
		when(mockFiles.delete(anyString())).thenReturn(mockDelete);
		
		//Good list of files
		goodList();
		
		googleSync.deleteFile(mockIOFile);
		verify(mockDelete).execute();
	}
	
	@Test (expected=FileNotFoundException.class)
	public void testDeleteFile_NoFile() throws IOException{
		badList();
		
		googleSync.deleteFile(mockIOFile);
	}
	
	private void goodList() throws IOException {
		fileList = new FileList();
		ArrayList<File> usefulList = new ArrayList<File>();
		usefulList.add(googleFile);
		fileList.setItems(usefulList);
		when(mockList.execute()).thenReturn(fileList);
	}
	
	private void badList() throws IOException {
		fileList = new FileList();
		ArrayList<File> emptyList = new ArrayList<File>();
		fileList.setItems(emptyList);
		when(mockList.execute()).thenReturn(fileList);
	}
}
