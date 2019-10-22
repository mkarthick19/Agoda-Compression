package com.challenge.agoda.model;

public class UnzipInputRequest {
	
	private String inputDirectoryPath;
	
	private String outputDirectoryPath;

	public String getInputDirectoryPath() {
		return inputDirectoryPath;
	}

	public String getOutputDirectoryPath() {
		return outputDirectoryPath;
	}

	public UnzipInputRequest(String inputDirectoryPath, String outputDirectoryPath) {
		super();
		this.inputDirectoryPath = inputDirectoryPath;
		this.outputDirectoryPath = outputDirectoryPath;
	}	
}
