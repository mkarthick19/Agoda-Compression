package com.challenge.agoda.model;

public class FileMetadata {
	
	private String absolutePath;
	
	private String fileName;
	
	public String getAbsolutePath() {
		return absolutePath;
	}

	public String getFileName() {
		return fileName;
	}

	public FileMetadata(String absolutePath, String fileName) {
		super();
		this.absolutePath = absolutePath;
		this.fileName = fileName;
	}	
}
