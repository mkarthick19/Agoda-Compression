package com.challenge.agoda.strategy;

import java.io.IOException;

import com.challenge.agoda.model.UnzipInputRequest;
import com.challenge.agoda.model.ZipInputRequest;
import com.challenge.agoda.service.Compression;
import com.challenge.agoda.service.Decompression;

// Java Util zip strategy for compression and decompression. 
public class JavaZipStrategy implements ZipStrategy {

	private Compression compression = new Compression();

	private Decompression decompression = new Decompression();

	@Override
	public void compress(ZipInputRequest zipInputRequest) throws IOException {
		compression.compress(zipInputRequest);
	}

	@Override
	public void decompress(UnzipInputRequest UnzipInputRequest) throws IOException {
		decompression.decompress(UnzipInputRequest);
	}
}
