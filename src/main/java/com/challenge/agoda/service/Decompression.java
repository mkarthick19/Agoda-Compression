package com.challenge.agoda.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import com.challenge.agoda.constants.Constants;
import com.challenge.agoda.model.FileMetadata;
import com.challenge.agoda.model.UnzipInputRequest;
import com.challenge.agoda.util.Util;

// This class contains first level and second level decompression methods. Executor services are used 
// to make parallel calls for decompression.
public class Decompression {

	private Merge merge = new Merge();

	private ExecutorService executorService = Executors.newFixedThreadPool(Constants.THREAD_POOL_SIZE);

	public void decompress(UnzipInputRequest unzipInputRequest) throws IOException {
		String inputDir = unzipInputRequest.getInputDirectoryPath();
		String outputDir = unzipInputRequest.getOutputDirectoryPath();
		Util.createOutputDirectories(inputDir, outputDir);
		List<String> compressedDirList = Util.getAllAbsolutePaths(inputDir);
		List<Future> futures = new ArrayList<>();
		boolean isLevel2ZipPresent = false;
		for (String compressedDir : compressedDirList) {
			if (compressedDir.endsWith(Constants.ZIP_LEVEL_2_EXTENSION)) {
				isLevel2ZipPresent = true;
				Runnable task = () -> {
					try {
						decompressLevel2Zips(compressedDir, outputDir);
					} catch (IOException e) {
					}
				};
				futures.add(executorService.submit(task));
			}
		}
		Util.waitForTasksCompletion(futures);
		if (isLevel2ZipPresent) {
			File compressionDir = new File(inputDir);
			File srcFile = new File(outputDir + compressionDir.getName());
			File destFile = new File(outputDir);
			Util.copyFolder(srcFile, destFile);
			Util.deleteDirectory(srcFile, false);
		}
		List<String> compressedFilePaths = Util.getAllAbsolutePaths(outputDir);
		for (String compressedFile : compressedDirList) {
			if (compressedFile.endsWith(Constants.ZIP_LEVEL_1_EXTENSION)) {
				compressedFilePaths.add(compressedFile);
			}
		}
		List<FileMetadata> compressedFilesList = new ArrayList<>();
		for (String compressedFilePath : compressedFilePaths) {
			String fileName = Util.getFileNameFromPath(compressedFilePath, Constants.ZIP_LEVEL_1_EXTENSION);
			FileMetadata fileMetadata = new FileMetadata(compressedFilePath, fileName);
			compressedFilesList.add(fileMetadata);
		}
		List<FileMetadata> decompressedFilesList = decompressFiles(compressedFilesList, inputDir, outputDir);
		Map<String, Integer> mergeFilesMap = getFilesFromParts(decompressedFilesList, outputDir);
		mergeFiles(mergeFilesMap, outputDir);
		executorService.shutdown();
	}

	private List<FileMetadata> decompressFiles(List<FileMetadata> files, String inputDir, String outputDir)
			throws IOException {
		List<FileMetadata> decompressedFiles = new ArrayList<>();
		List<Future> futures = new ArrayList<>();
		for (FileMetadata file : files) {
			Runnable task = () -> {
				try {
					String resultDir = Util.getParentDirPath(file.getAbsolutePath());
					if (!(resultDir + File.separator).contains(outputDir)) {
						String residue = (resultDir + File.separator).substring(inputDir.length());
						resultDir = outputDir + residue;
					}
					decompressedFiles.add(decompressFile(file.getAbsolutePath(), file.getFileName(), resultDir));
				} catch (IOException e) {
				}
			};
			futures.add(executorService.submit(task));
		}
		Util.waitForTasksCompletion(futures);
		return decompressedFiles;
	}

	private FileMetadata decompressFile(String compressedFilePath, String compressedFileName, String outputDirPath)
			throws IOException {
		File destDir = new File(outputDirPath);
		byte[] buffer = new byte[1024];
		ZipInputStream zis = new ZipInputStream(new FileInputStream(compressedFilePath));
		ZipEntry zipEntry = zis.getNextEntry();
		while (zipEntry != null) {
			File newFile = newFile(destDir, zipEntry);
			FileOutputStream fos = new FileOutputStream(newFile);
			int len;
			while ((len = zis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
			fos.close();
			File deleteUncompressedFile = new File(destDir,
					File.separator + zipEntry.getName() + Constants.ZIP_LEVEL_1_EXTENSION);
			deleteUncompressedFile.delete();
			zipEntry = zis.getNextEntry();
		}
		zis.closeEntry();
		zis.close();

		return new FileMetadata(outputDirPath, compressedFileName);
	}

	private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());
		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}

		return destFile;
	}

	private String decompressLevel2Zips(String inputDir, String outputDir) throws IOException {
		try (ZipFile file = new ZipFile(inputDir)) {
			FileSystem fileSystem = FileSystems.getDefault();
			Enumeration<? extends ZipEntry> entries = file.entries();
			String uncompressedDirectory = outputDir;
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.isDirectory()) {
					File fileName = new File(entry.getName());
					if (fileName.isHidden()) {
						continue;
					}
					Files.createDirectories(fileSystem.getPath(uncompressedDirectory + entry.getName()));
				} else {
					InputStream is = file.getInputStream(entry);
					BufferedInputStream bis = new BufferedInputStream(is);
					String uncompressedFileName = uncompressedDirectory + entry.getName();
					Path uncompressedFilePath = fileSystem.getPath(uncompressedFileName);
					File fileName = new File(uncompressedFileName);
					if (fileName.isHidden()) {
						continue;
					}
					Files.createFile(uncompressedFilePath);
					FileOutputStream fileOutput = new FileOutputStream(uncompressedFileName);
					while (bis.available() > 0) {
						fileOutput.write(bis.read());
					}
					fileOutput.close();
				}
			}
		}
		return outputDir;
	}

	private Map<String, Integer> getFilesFromParts(List<FileMetadata> decompressedFiles, String outputDir) {
		Map<String, Integer> mergeFilesMap = new HashMap<>();
		for (FileMetadata fileMetadata : decompressedFiles) {
			String fileName = Util.getFileNameFromPart(fileMetadata.getFileName());
			if (fileMetadata.getAbsolutePath().length() > outputDir.length()) {
				String residualPath = fileMetadata.getAbsolutePath().substring(outputDir.length());
				if (!residualPath.endsWith(File.separator)) {
					residualPath += File.separator;
				}
				fileName = residualPath + fileName;
			}
			mergeFilesMap.put(fileName, mergeFilesMap.getOrDefault(fileName, 0) + 1);
		}
		return mergeFilesMap;
	}

	private void mergeFiles(Map<String, Integer> mergeFilesMap, String outputDir) throws IOException {
		List<Future> futures = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : mergeFilesMap.entrySet()) {
			Runnable task = () -> {
				try {
					String fileName = entry.getKey();
					int noOfParts = entry.getValue();
					List<File> filesList = new ArrayList<>();
					for (int part = 0; part < noOfParts; part++) {
						String filePath = outputDir + fileName + Constants.SUFFIX + part;
						filesList.add(new File(filePath));
					}
					merge.mergeFiles(filesList, outputDir + fileName);
				} catch (IOException e) {
				}
			};
			futures.add(executorService.submit(task));
		}
		Util.waitForTasksCompletion(futures);
	}
}