package com.challenge.agoda.strategy;

import java.io.IOException;

import com.challenge.agoda.model.UnzipInputRequest;
import com.challenge.agoda.model.ZipInputRequest;

public interface ZipStrategy {

	public void compress(ZipInputRequest zipInputRequest) throws IOException;

	public void decompress(UnzipInputRequest UnzipInputRequest) throws IOException;
}
