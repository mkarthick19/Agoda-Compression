package com.challenge.agoda.model;

public class ZipInputRequest {
	
	private String inputDirectoryPath;
	
	private String outputDirectoryPath;
	
	private long maxCompressedSizeThresholdInMB;

	public String getInputDirectoryPath() {
		return inputDirectoryPath;
	}

	public String getOutputDirectoryPath() {
		return outputDirectoryPath;
	}

	public long getMaxCompressedSizeThreshold() {
		return maxCompressedSizeThresholdInMB;
	}

	public ZipInputRequest(String inputDirectoryPath, String outputDirectoryPath, long maxCompressedSizeThresholdInMB) {
		super();
		this.inputDirectoryPath = inputDirectoryPath;
		this.outputDirectoryPath = outputDirectoryPath;
		this.maxCompressedSizeThresholdInMB = maxCompressedSizeThresholdInMB;
	}	
}
