package com.challenge.agoda.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

// Class to merge multiple files into one file.
public class Merge {

	public File mergeFiles(List<File> splitFilesList, String outputDirPath) throws IOException {
		if (splitFilesList == null || splitFilesList.size() == 0) {
			return null;
		}
		File ofile = new File(outputDirPath);
		FileOutputStream fos;
		FileInputStream fis;
		byte[] fileBytes;
		int bytesRead = 0;
		try {
			fos = new FileOutputStream(ofile, true);
			for (File file : splitFilesList) {
				fis = new FileInputStream(file);
				fileBytes = new byte[(int) file.length()];
				bytesRead = fis.read(fileBytes, 0, (int) file.length());
				fos.write(fileBytes);
				fos.flush();
				fileBytes = null;
				fis = null;
				file.delete();
			}
			fos.close();
			fos = null;
		} catch (IOException exception) {
		}
		return ofile;
	}
}