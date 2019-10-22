package com.challenge.agoda.validator;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.challenge.agoda.constants.Constants;
import com.challenge.agoda.exception.InvalidInputException;
import com.challenge.agoda.model.UnzipInputRequest;
import com.challenge.agoda.model.ZipInputRequest;
import com.challenge.agoda.util.Util;

public class ValidatorTest {

	private static final String HOME_DIRECTORY = System.getProperty("user.home") + File.separator;

	private static final String TEST_DIRECTORY = HOME_DIRECTORY + "TestDirectory" + File.separator;

	private static final String TEST_FILE = TEST_DIRECTORY + "newFile1";

	private static final String TEST_SUBDIRECTORY = TEST_DIRECTORY + "subdir1";

	private static final String TEST_DIRECTORY1 = HOME_DIRECTORY + "TestDirectory1" + File.separator;

	private static long maxCompressedSizeInMB = 4;

	@BeforeClass
	public static void setup() throws IOException {
		Util.createDirectory(TEST_DIRECTORY);
		Util.createDirectory(TEST_DIRECTORY1);
		File newFile1 = new File(TEST_FILE);
		newFile1.createNewFile();
	}

	@Test
	public void testSuccessfulValidateZipInput() throws IOException, InvalidInputException {
		ZipInputRequest zipInputRequest = new ZipInputRequest(TEST_DIRECTORY, TEST_DIRECTORY1, maxCompressedSizeInMB);
		Validator.validateZipInput(zipInputRequest);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateZipInput_InvalidThreshold() throws IOException, InvalidInputException {
		ZipInputRequest zipInputRequest = new ZipInputRequest(TEST_DIRECTORY, TEST_DIRECTORY1, -1);
		Validator.validateZipInput(zipInputRequest);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateZipInput_InvalidInputDirectory() throws IOException, InvalidInputException {
		ZipInputRequest zipInputRequest = new ZipInputRequest(TEST_DIRECTORY + "InvalidDirectory", TEST_DIRECTORY1,
				maxCompressedSizeInMB);
		Validator.validateZipInput(zipInputRequest);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateZipInput_DirectoryWithoutFile() throws IOException, InvalidInputException {
		ZipInputRequest zipInputRequest = new ZipInputRequest(TEST_DIRECTORY1, TEST_DIRECTORY1, maxCompressedSizeInMB);
		Validator.validateZipInput(zipInputRequest);
	}

	@Test
	public void testSuccessfulValidateUnZipInput() throws IOException, InvalidInputException {
		UnzipInputRequest unzipInputRequest = new UnzipInputRequest(TEST_DIRECTORY, TEST_DIRECTORY1);
		Validator.validateUnzipInput(unzipInputRequest);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateUnzipInput_InvalidInputDirectory() throws IOException, InvalidInputException {
		UnzipInputRequest unzipInputRequest = new UnzipInputRequest(TEST_DIRECTORY + "InvalidDirectory",
				TEST_DIRECTORY1);
		Validator.validateUnzipInput(unzipInputRequest);
	}
}
