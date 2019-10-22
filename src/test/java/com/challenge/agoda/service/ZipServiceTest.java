package com.challenge.agoda.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.challenge.agoda.constants.Constants;
import com.challenge.agoda.model.UnzipInputRequest;
import com.challenge.agoda.model.ZipInputRequest;
import com.challenge.agoda.util.Util;

public class ZipServiceTest {

	private static final String HOME_DIRECTORY = System.getProperty("user.home") + File.separator;

	private static final String TEST_INPUT_DIRECTORY = HOME_DIRECTORY + "TestInputDirectory" + File.separator;

	private static final String TEST_INPUT_DIRECTORY1 = HOME_DIRECTORY + "TestInputDirectory1" + File.separator;

	private static final String FILE1 = TEST_INPUT_DIRECTORY + "file1";

	private static final String FILE2 = TEST_INPUT_DIRECTORY + "file2";

	private static final String FILE3 = TEST_INPUT_DIRECTORY1 + "file3";

	private static final String TEST_INPUT_SUBDIRECTORY_1 = TEST_INPUT_DIRECTORY + "subdir1" + File.separator;

	private static final String TEST_EMPTY_FILE = HOME_DIRECTORY + "TestEmptyDirectory" + File.separator;

	private static final String EMPTY_FILE = TEST_EMPTY_FILE + "emptyFile";

	private static final String INNER_LEVEL1_FILE = TEST_INPUT_SUBDIRECTORY_1 + "innerlevel1file";

	private static final String TEST_INPUT_SUBDIRECTORY_2 = TEST_INPUT_SUBDIRECTORY_1 + "subdir2" + File.separator;

	private static final String INNER_LEVEL2_FILE = TEST_INPUT_SUBDIRECTORY_2 + "innerlevel2file";

	private static final String TEST_COMPRESSED_DIRECTORY = HOME_DIRECTORY + "TestCompressedDirectory" + File.separator;

	private static final String TEST_DECOMPRESSED_DIRECTORY = HOME_DIRECTORY + "TestDecompressedDirectory"
			+ File.separator;

	private static final String TEST_INPUT_DIRECTORY_MEDIA_TYPES = HOME_DIRECTORY + "TestDirectoryMediaTypes"
			+ File.separator;

	private static final String FILE_MP4 = TEST_INPUT_DIRECTORY_MEDIA_TYPES + "mp4File.mp4";

	private static final String FILE_JPG = TEST_INPUT_DIRECTORY_MEDIA_TYPES + "jpgFile.jpg";

	private static final String FILE_TXT = TEST_INPUT_DIRECTORY_MEDIA_TYPES + "txtFile.txt";

	private static final String ZIP_LEVEL_1 = TEST_COMPRESSED_DIRECTORY + "compressed.level1.zip";

	private static final String ZIP_LEVEL_2 = TEST_COMPRESSED_DIRECTORY + "compressed.level2.zip";

	private Compression compression = new Compression();

	private Decompression decompression = new Decompression();

	private static long maxCompressedSizeInMB = 1;

	@BeforeClass
	public static void setup() throws IOException {
		Util.createDirectory(TEST_INPUT_DIRECTORY);
		Util.createDirectory(TEST_INPUT_DIRECTORY1);
		Util.createDirectory(TEST_EMPTY_FILE);
		Util.createDirectory(TEST_INPUT_DIRECTORY_MEDIA_TYPES);
		Util.createDirectory(TEST_COMPRESSED_DIRECTORY);
		File newFile1 = createFile(FILE1);
		File newFile2 = createFile(FILE2);
		File newFile3 = new File(FILE_MP4);
		File newFile4 = new File(FILE_JPG);
		File newFile5 = new File(FILE_TXT);
		File newFile6 = createFile(FILE3);
		File emptyFile = new File(EMPTY_FILE);
		File zipLevel1 = new File(ZIP_LEVEL_1);
		File zipLevel2 = new File(ZIP_LEVEL_2);
		emptyFile.createNewFile();
		newFile3.createNewFile();
		newFile4.createNewFile();
		newFile5.createNewFile();
		zipLevel1 = createFile(ZIP_LEVEL_1);
		zipLevel2 = createFile(ZIP_LEVEL_2);
	}

    @AfterClass
	public static void teardown() throws IOException {
		Util.cleanUp(TEST_INPUT_DIRECTORY);
		Util.cleanUp(TEST_INPUT_DIRECTORY1);
		Util.cleanUp(TEST_EMPTY_FILE);
		Util.cleanUp(TEST_INPUT_DIRECTORY_MEDIA_TYPES);
		Util.cleanUp(TEST_COMPRESSED_DIRECTORY);
		Util.cleanUp(TEST_DECOMPRESSED_DIRECTORY);
	}

	public static File createFile(String file) throws IOException {
		File newFile1 = new File(file);
		newFile1.createNewFile();
		OutputStream os = new FileOutputStream(newFile1);
		os.write("testFile".getBytes());
		os.close();
		return newFile1;
	}

	@Test
	public void testSuccessfulCompression() throws IOException {
		ZipInputRequest zipInputRequest = new ZipInputRequest(TEST_INPUT_DIRECTORY, TEST_COMPRESSED_DIRECTORY,
				maxCompressedSizeInMB);
		compression.compress(zipInputRequest);
		List<String> paths = Util.getAllAbsolutePaths(TEST_COMPRESSED_DIRECTORY);
		assertTrue(paths.get(0).endsWith(Constants.ZIP_LEVEL_2_EXTENSION));
	}

	@Test
	public void testSuccessfulCompressionWithEmptyFile() throws IOException {
		ZipInputRequest zipInputRequest = new ZipInputRequest(TEST_EMPTY_FILE, TEST_COMPRESSED_DIRECTORY,
				maxCompressedSizeInMB);
		compression.compress(zipInputRequest);
		List<String> paths = Util.getAllAbsolutePaths(TEST_COMPRESSED_DIRECTORY);
		assertTrue(paths.get(0).endsWith(Constants.ZIP_LEVEL_1_EXTENSION));
	}

	@Test
	public void testSuccessfulCompressionWithSubDirectories() throws IOException {
		Util.createDirectory(TEST_INPUT_SUBDIRECTORY_1);
		Util.createDirectory(TEST_INPUT_SUBDIRECTORY_2);
		File innerlevel1file = createFile(INNER_LEVEL1_FILE);
		File innerlevel2file = createFile(INNER_LEVEL2_FILE);
		ZipInputRequest zipInputRequest = new ZipInputRequest(TEST_INPUT_DIRECTORY, TEST_COMPRESSED_DIRECTORY,
				maxCompressedSizeInMB);
		compression.compress(zipInputRequest);
		List<String> paths = Util.getAllAbsolutePaths(TEST_COMPRESSED_DIRECTORY);
		assertTrue(paths.get(0).endsWith(Constants.ZIP_LEVEL_2_EXTENSION));
	}

	@Test
	public void testSuccessfulCompressionWithDifferentMediaTypes() throws IOException {
		ZipInputRequest zipInputRequest = new ZipInputRequest(TEST_INPUT_DIRECTORY_MEDIA_TYPES,
				TEST_COMPRESSED_DIRECTORY, maxCompressedSizeInMB);
		compression.compress(zipInputRequest);
		List<String> paths = Util.getAllAbsolutePaths(TEST_COMPRESSED_DIRECTORY);
		assertTrue(paths.get(0).endsWith(Constants.ZIP_LEVEL_2_EXTENSION));
	}

	@Test
	public void testUnsuccessfulCompression() throws IOException {
		ZipInputRequest zipInputRequest = new ZipInputRequest(TEST_INPUT_DIRECTORY, TEST_COMPRESSED_DIRECTORY, -1);
		compression.compress(zipInputRequest);
		File compressedDir = new File(TEST_COMPRESSED_DIRECTORY);
		assertFalse(compressedDir.exists());
	}

	@Test
	public void testSuccessfulDecompression() throws IOException {
		setupForDecompression(TEST_INPUT_DIRECTORY1);
		UnzipInputRequest unzipInputRequest = new UnzipInputRequest(TEST_COMPRESSED_DIRECTORY,
				TEST_DECOMPRESSED_DIRECTORY);
		decompression.decompress(unzipInputRequest);
		assertDecompression();
	}

	@Test
	public void testSuccessfulDecompressionWithSubDirectories() throws IOException {
		setupForDecompression(TEST_INPUT_DIRECTORY);
		UnzipInputRequest unzipInputRequest = new UnzipInputRequest(TEST_COMPRESSED_DIRECTORY,
				TEST_DECOMPRESSED_DIRECTORY);
		decompression.decompress(unzipInputRequest);
		assertDecompression();
	}
	
	@Test
	public void testSuccessfulDecompressionWithDifferentMediaTypes() throws IOException {
		setupForDecompression(TEST_INPUT_DIRECTORY_MEDIA_TYPES);
		UnzipInputRequest unzipInputRequest = new UnzipInputRequest(TEST_COMPRESSED_DIRECTORY,
				TEST_DECOMPRESSED_DIRECTORY);
		decompression.decompress(unzipInputRequest);
		assertDecompression();
	}

	@Test
	public void testUnSuccessfulDecompression() throws IOException {
		setupForDecompression(TEST_INPUT_DIRECTORY_MEDIA_TYPES);
		UnzipInputRequest unzipInputRequest = new UnzipInputRequest(TEST_COMPRESSED_DIRECTORY,
				TEST_DECOMPRESSED_DIRECTORY);
		decompression.decompress(unzipInputRequest);
		assertDecompression();
	}
	
	private void setupForDecompression(String inputDir) throws IOException {
		Util.cleanUp(TEST_COMPRESSED_DIRECTORY);
		Util.cleanUp(TEST_DECOMPRESSED_DIRECTORY);
		ZipInputRequest zipInputRequest = new ZipInputRequest(inputDir, TEST_COMPRESSED_DIRECTORY,
				maxCompressedSizeInMB);
		compression.compress(zipInputRequest);
	}

	private void assertDecompression() throws IOException {
		List<String> paths = Util.getAllAbsolutePaths(TEST_DECOMPRESSED_DIRECTORY);
		for (String path : paths) {
			assertTrue(new File(path).exists());
		}
	}	
}
