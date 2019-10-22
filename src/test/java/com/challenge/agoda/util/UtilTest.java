package com.challenge.agoda.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.AfterClass;
import org.junit.Test;

import com.challenge.agoda.constants.Constants;

public class UtilTest {

	private static final String HOME_DIRECTORY = System.getProperty("user.home") + File.separator;

	private static final String TEST_DIRECTORY = HOME_DIRECTORY + "TestDirectory" + File.separator;

	private static final String TEST_SUBDIRECTORY = TEST_DIRECTORY + "subdir1";

	private static final String TEST_DIRECTORY1 = HOME_DIRECTORY + "TestDirectory1" + File.separator;
	
	private static ExecutorService testExecutorService = Executors.newFixedThreadPool(1);

	@AfterClass
	public static void teardown() throws IOException {
		Util.cleanUp(TEST_DIRECTORY);
		Util.cleanUp(TEST_DIRECTORY1);
		testExecutorService.shutdown();
	}

	@Test
	public void testCreateDirectory() throws IOException {
		Util.createDirectory(TEST_DIRECTORY);
		File directory = new File(TEST_DIRECTORY);
		assertTrue(directory.exists());
	}

	@Test
	public void testCleanUp() throws IOException {
		Util.createDirectory(TEST_DIRECTORY);
		Util.cleanUp(TEST_DIRECTORY);
		File directory = new File(TEST_DIRECTORY);
		assertTrue(!directory.exists());
	}

	@Test
	public void testDeleteDirectory_KeepLevel2Zip() throws IOException {
		Util.createDirectory(TEST_DIRECTORY);
		File level2Zip = new File(TEST_DIRECTORY + "test1.level2.zip");
		level2Zip.createNewFile();
		Util.deleteDirectory(level2Zip, true);
		assertTrue(level2Zip.exists());
	}

	@Test
	public void testDeleteDirectory_RemoveLevel2Zip() throws IOException {
		Util.createDirectory(TEST_DIRECTORY);
		File level2Zip = new File(TEST_DIRECTORY + "test1.level2.zip");
		level2Zip.createNewFile();
		Util.deleteDirectory(level2Zip, false);
		assertTrue(!level2Zip.exists());
	}

	@Test
	public void testEmptyDirectory() throws IOException {
		Util.createDirectory(TEST_DIRECTORY);
		File subdir = new File(TEST_SUBDIRECTORY);
		subdir.mkdir();
		Util.deleteEmptyDirectory(subdir);
		assertTrue(!subdir.exists());
	}

	@Test
	public void testGetAllAbsolutePaths() throws IOException {
		Util.createDirectory(TEST_DIRECTORY);
		Util.createDirectory(TEST_SUBDIRECTORY);
		File file1 = new File(TEST_DIRECTORY + "file1");
		file1.createNewFile();
		File innerfile = new File(TEST_SUBDIRECTORY + File.separator + "innerfile1");
		innerfile.createNewFile();
		List<String> absPaths = Util.getAllAbsolutePaths(TEST_DIRECTORY);
		assertEquals(2, absPaths.size());
		assertTrue(absPaths.contains(file1.getAbsolutePath()));
		assertTrue(absPaths.contains(innerfile.getAbsolutePath()));
	}

	@Test
	public void testGetSortedFiles() throws IOException {
		Util.createDirectory(TEST_DIRECTORY);
		Util.createDirectory(TEST_SUBDIRECTORY);
		File file1 = new File(TEST_DIRECTORY + "file1");
		file1.createNewFile();
		File innerfile = new File(TEST_SUBDIRECTORY + "innerfile1");
		innerfile.createNewFile();
		List<File> absPaths = Util.getSortedFilesByFileSize(TEST_DIRECTORY);
		assertEquals(2, absPaths.size());
		assertTrue(absPaths.get(0).length() <= absPaths.get(1).length());
	}

	@Test
	public void testCreateOutputDirectories() throws IOException {
		File directory1 = new File(TEST_DIRECTORY);
		File subdir = new File(TEST_SUBDIRECTORY);
		File directory2 = new File(TEST_DIRECTORY1);
		directory1.mkdir();
		subdir.mkdir();
		directory2.mkdir();
		Util.createOutputDirectories(TEST_DIRECTORY, TEST_DIRECTORY1);
		File resultSubDirectory = new File(TEST_DIRECTORY1 + "subdir1");
		assertTrue(resultSubDirectory.exists());
	}

	@Test
	public void testCopyFolder() throws IOException {
		File directory1 = new File(TEST_DIRECTORY);
		File subdir = new File(TEST_SUBDIRECTORY);
		File directory2 = new File(TEST_DIRECTORY1);
		directory1.mkdir();
		subdir.mkdir();
		directory2.mkdir();
		File file1 = new File(TEST_DIRECTORY + "newFile");
		file1.createNewFile();
		Util.copyFolder(directory1, directory2);
		File resultSubDirectory = new File(TEST_DIRECTORY1 + "newFile");
		assertTrue(resultSubDirectory.exists());
	}

	@Test
	public void testIsLevel2Zip_True() throws IOException {
		assertTrue(Util.isLevel2Zip("sample.level2.zip"));
	}

	@Test
	public void testIsLevel2Zip_False() throws IOException {
		assertFalse(Util.isLevel2Zip("sample.level1.zip"));
	}

	@Test
	public void testGetFileNameFromPath() throws IOException {
		Util.createDirectory(TEST_DIRECTORY);
		File file1 = new File(TEST_DIRECTORY + "newFile.level1.zip");
		file1.createNewFile();
		String fileName = Util.getFileNameFromPath(file1.getAbsolutePath(), Constants.ZIP_LEVEL_1_EXTENSION);
		assertEquals("newFile", fileName);
	}

	@Test
	public void testGetFileNameFromPart() throws IOException {
		Util.createDirectory(TEST_DIRECTORY);
		File file1 = new File(TEST_DIRECTORY + "newFile.part_0.zip");
		file1.createNewFile();
		String fileName = Util.getFileNameFromPart(file1.getAbsolutePath());
		assertEquals(TEST_DIRECTORY + "newFile", fileName);
	}

	@Test
	public void testGetOutputDirectory() throws IOException {
		Util.createDirectory(TEST_DIRECTORY);
		File file1 = new File(TEST_DIRECTORY + "newFile.part_0.zip");
		file1.createNewFile();
		String fileName = Util.getFileNameFromPart(file1.getAbsolutePath());
		assertEquals(TEST_DIRECTORY + "newFile", fileName);
	}

	@Test
	public void testGetParentDirPath() throws IOException {
		String parent = Util.getParentDirPath(TEST_SUBDIRECTORY);
		assertEquals(TEST_DIRECTORY, parent + File.separator);
	}

	@Test
	public void testRemoveExtension() throws IOException {
		String fileName = Util.removeExtension(TEST_DIRECTORY + "newFile.level1.zip", Constants.ZIP_LEVEL_1_EXTENSION);
		assertEquals(TEST_DIRECTORY + "newFile", fileName);
	}
	
	@Test
	public void testWaitForTasksCompletion() throws IOException, InterruptedException, ExecutionException {
		List<Future> futures = new ArrayList<>();		
		Runnable task = () -> {	  
			try {
				Util.createDirectory(TEST_DIRECTORY);
			} catch (IOException e) {}
        };			
        futures.add(testExecutorService.submit(task));		
        Util.waitForTasksCompletion(futures);
        assertTrue(futures.get(0).isDone());
	}
}
