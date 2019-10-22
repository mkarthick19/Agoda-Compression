package com.challenge.agoda.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.challenge.agoda.constants.Constants;
import com.challenge.agoda.model.FileMetadata;
import com.challenge.agoda.model.ZipInputRequest;
import com.challenge.agoda.util.Util;

// This class contains first level and second level compression methods. Executor services are used 
// to make parallel calls for compression.
public class Compression {

	private Split split = new Split();

	private ExecutorService executorService = Executors.newFixedThreadPool(Constants.THREAD_POOL_SIZE);

	public void compress(ZipInputRequest zipInputRequest) throws IOException {
		String inputDir = zipInputRequest.getInputDirectoryPath();
		String outputDir = zipInputRequest.getOutputDirectoryPath();
		long maxCompressedSize = Math.min(zipInputRequest.getMaxCompressedSizeThreshold(),
				Util.getMaxAllowedJVMMemoryInMB() / Constants.THREAD_POOL_SIZE);
		Util.createOutputDirectories(inputDir, outputDir);
		List<String> inputFilesList = Util.getAllAbsolutePaths(inputDir);
		List<Future> futures = new ArrayList<>();
		for (String inputFile : inputFilesList) {
			Runnable task = () -> {
				try {
					List<FileMetadata> sourceFileParts = split.splitFile(inputFile, maxCompressedSize, outputDir);
					String resultDir = Util.getOutputDirectory(inputFile, inputDir, outputDir);
					compressFiles(sourceFileParts, resultDir);
				} catch (IOException e) {
				}
			};
			futures.add(executorService.submit(task));
		}
		Util.waitForTasksCompletion(futures);
		long maxCompressedSizeInBytes = maxCompressedSize * 1024L * 1024L;
		compressTheZips(outputDir, maxCompressedSizeInBytes);
		executorService.shutdown();
	}

	private void compressFiles(List<FileMetadata> files, String outputDirPath) throws IOException {
		for (FileMetadata file : files) {
			String compressedFileLoc = outputDirPath + file.getFileName() + Constants.ZIP_LEVEL_1_EXTENSION;
			compressFile(file.getAbsolutePath(), compressedFileLoc);
		}
	}

	private void compressFile(String filePath, String outputDirPath) throws IOException {
		FileOutputStream fos = new FileOutputStream(outputDirPath);
		ZipOutputStream zipOut = new ZipOutputStream(fos);
		File fileToZip = new File(filePath);
		FileInputStream fis = new FileInputStream(fileToZip);
		ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
		zipOut.putNextEntry(zipEntry);
		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zipOut.write(bytes, 0, length);
		}
		zipOut.close();
		fis.close();
		fos.close();
		fileToZip.delete();
	}

	private void compressTheZips(String outputDirPath, long maxCompressedSizeInBytes) throws IOException {
		List<File> zipFilesListSorted = Util.getSortedFilesByFileSize(outputDirPath);
		int index = 0, part = 0;
		File fileToZip = new File(outputDirPath);
		List<Future> futures = new ArrayList<>();
		while (index < zipFilesListSorted.size()) {
			long fileSizeSumInBytes = 0L;
			List<File> tempZipFileList = new ArrayList<>();
			while (index < zipFilesListSorted.size()) {
				fileSizeSumInBytes += zipFilesListSorted.get(index).length();
				if (fileSizeSumInBytes <= maxCompressedSizeInBytes) {
					tempZipFileList.add(zipFilesListSorted.get(index));
					index++;
				} else {
					break;
				}
			}
			if (tempZipFileList.size() <= 1) {
				break;
			}
			final Integer innerPart = new Integer(part);
			Runnable task = () -> {
				try {
					List<String> level2CompressList = new ArrayList<>();
					for (File file : tempZipFileList) {
						level2CompressList.add(fileToZip.getName() + File.separator
								+ file.getAbsolutePath().substring(outputDirPath.length()));
					}
					FileOutputStream fos = new FileOutputStream(
							outputDirPath + Constants.LEVEL2_PART + innerPart + Constants.ZIP_LEVEL_2_EXTENSION);
					ZipOutputStream zipOut = new ZipOutputStream(fos);
					zipFile(fileToZip, fileToZip.getName(), zipOut, level2CompressList);
					zipOut.close();
					fos.close();
					for (File fileToDelete : tempZipFileList) {
						fileToDelete.delete();
					}
				} catch (IOException e) {
				}
			};
			part++;
			futures.add(executorService.submit(task));
		}
		Util.waitForTasksCompletion(futures);
		Util.deleteEmptyDirectory(fileToZip);
	}

	private void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut, List<String> level2CompressList)
			throws IOException {
		if (fileToZip.isHidden() || Util.isLevel2Zip(fileName)) {
			return;
		}
		if (!fileToZip.isDirectory() && !level2CompressList.contains(fileName)) {
			return;
		}
		if (fileToZip.isDirectory()) {
			if (fileName.endsWith(File.separator)) {
				zipOut.putNextEntry(new ZipEntry(fileName));
				zipOut.closeEntry();
			} else {
				zipOut.putNextEntry(new ZipEntry(fileName + File.separator));
				zipOut.closeEntry();
			}
			File[] children = fileToZip.listFiles();
			for (File childFile : children) {
				zipFile(childFile, fileName + File.separator + childFile.getName(), zipOut, level2CompressList);
			}
			return;
		}
		FileInputStream fis = new FileInputStream(fileToZip);
		ZipEntry zipEntry = new ZipEntry(fileName);
		zipOut.putNextEntry(zipEntry);
		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zipOut.write(bytes, 0, length);
		}
		fis.close();
	}
}