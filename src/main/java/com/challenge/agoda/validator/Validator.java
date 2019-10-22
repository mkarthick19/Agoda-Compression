package com.challenge.agoda.validator;

import java.io.File;

import org.springframework.stereotype.Component;

import com.challenge.agoda.exception.InvalidInputException;
import com.challenge.agoda.model.UnzipInputRequest;
import com.challenge.agoda.model.ZipInputRequest;

// Validator class for Compression Decompression Application that validates input request and throws custom Invalid Input Exception,
// if the request is invalid.
@Component
public class Validator {

	public static void validateZipInput(final ZipInputRequest inputRequest) throws InvalidInputException {
		if (inputRequest.getMaxCompressedSizeThreshold() <= 0) {
			throw new InvalidInputException(
					"Max Compressed Size Threshold entered is invalid and must be greater than 0."
							+ " Please ensure the input directory is valid and try again.");
		}
		validateDirectoryPaths(inputRequest.getInputDirectoryPath());
	}

	public static void validateUnzipInput(final UnzipInputRequest inputRequest) throws InvalidInputException {
		validateDirectoryPaths(inputRequest.getInputDirectoryPath());
	}

	private static void validateDirectoryPaths(String inputDirPath) throws InvalidInputException {
		File inputDir = new File(inputDirPath);
		if (!inputDir.exists()) {
			throw new InvalidInputException(
					"Input directory does not exist. Please ensure the input directory is valid and try again.");
		}
		if (!inputDir.isDirectory()) {
			throw new InvalidInputException(
					"Input directory is not a directory. Please ensure the input directory is valid and try again.");
		}
		if (!inputDir.canRead()) {
			throw new InvalidInputException(
					"Input directory does not have read permissions. Please ensure the input directory is valid and try again.");
		}
		if (inputDir.list().length == 0) {
			throw new InvalidInputException(
					"Input directory is empty. Please ensure the input directory is valid and try again.");
		}
	}
}
