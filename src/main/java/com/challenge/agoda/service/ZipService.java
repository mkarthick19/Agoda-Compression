package com.challenge.agoda.service;

import com.challenge.agoda.exception.InvalidInputException;
import com.challenge.agoda.model.UnzipInputRequest;
import com.challenge.agoda.model.ZipInputRequest;
import com.challenge.agoda.strategy.ZipStrategy;
import com.challenge.agoda.validator.Validator;

// ZipService class responsible for compression and decompression. In this application, we use zip for compression algorithm,
// but we can different strategy at runtime to support different compression algorithms.
public class ZipService {

	private ZipStrategy zipStrategy;

	// Strategy Pattern
	public void setZipStrategy(ZipStrategy zipStrategy) {
		this.zipStrategy = zipStrategy;
	}

	public void compress(ZipInputRequest zipInputRequest) {
		try {
			Validator.validateZipInput(zipInputRequest);
			zipStrategy.compress(zipInputRequest);
		} catch (InvalidInputException ex) {
			System.out.println(ex.getMessage());
		} catch (Exception ex) {
			System.out.println("Error in compressing the directory " + ex);
		}
	}

	public void decompress(UnzipInputRequest unzipInputRequest) {
		try {
			Validator.validateUnzipInput(unzipInputRequest);
			zipStrategy.decompress(unzipInputRequest);
		} catch (InvalidInputException ex) {
			System.out.println(ex.getMessage());
		} catch (Exception ex) {
			System.out.println("Error in decompressing the directory :" + ex);
		}
	}
}
