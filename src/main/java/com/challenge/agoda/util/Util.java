package com.challenge.agoda.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.challenge.agoda.constants.Constants;

// Util class that contains helper functions.
public final class Util {
	public static void createDirectory(String directoryName) throws IOException {
		cleanUp(directoryName);
		File directory = new File(directoryName);
		if (!directory.exists()) {
			directory.mkdir();
		}
	}

	public static void cleanUp(String directoryName) throws IOException {
		File directory = new File(directoryName);
		if (directory.exists()) {
			deleteDirectory(directory, false);
		}
	}

	public static void deleteDirectory(File file, boolean keepLevel2Zip) throws IOException {
		if (file.isDirectory()) {
			if (file.list().length == 0) {
				file.delete();
			} else {
				String files[] = file.list();
				for (String temp : files) {
					File fileDelete = new File(file, temp);
					deleteDirectory(fileDelete, keepLevel2Zip);
				}
				if (file.list().length == 0) {
					file.delete();
				}
			}
		} else {
			if (keepLevel2Zip && file.getAbsolutePath().endsWith(Constants.ZIP_LEVEL_2_EXTENSION)) {
				return;
			}
			file.delete();
		}
	}

	public static void deleteEmptyDirectory(File file) throws IOException {
		if (file.isDirectory()) {
			if (file.list().length == 0) {
				file.delete();
			} else {
				String files[] = file.list();
				for (String temp : files) {
					File fileDelete = new File(file, temp);
					deleteEmptyDirectory(fileDelete);
				}
				if (file.list().length == 0) {
					file.delete();
				}
			}
		}
	}

	public static List<String> getAllAbsolutePaths(String rootDir) throws IOException {
		List<String> absolutePathsList = new ArrayList<>();
		Files.walk(Paths.get(rootDir)).forEach(f -> {
			try {
				if (Files.isRegularFile(f) && !Files.isHidden(f.toAbsolutePath())) {
					absolutePathsList.add(f.toAbsolutePath().toString());
				}
			} catch (IOException e) {
			}
		});
		return absolutePathsList;
	}

	public static List<File> getSortedFilesByFileSize(String rootDir) throws IOException {
		List<String> zipFiles = Util.getAllAbsolutePaths(rootDir);
		List<File> zipFilesList = new ArrayList<>();
		for (String zipFile : zipFiles) {
			File zipDir = new File(zipFile);
			zipFilesList.add(zipDir);
		}
		Collections.sort(zipFilesList, (file1, file2) -> {
			return (int) (file1.length() - file2.length());
		});
		return zipFilesList;
	}

	public static void createSubDirectories(String rootDir, String outputDir) throws IOException {
		int rootDirLength = rootDir.length();
		Files.walk(Paths.get(rootDir)).forEach(f -> {
			if (Files.isDirectory(f)) {
				String path = f.toAbsolutePath().toString();
				if (path.length() > rootDirLength) {
					File dir = new File(outputDir + path.substring(rootDirLength));
					dir.mkdir();
				}
			}
		});
	}

	public static void createOutputDirectories(String inputDir, String outputDir) throws IOException {
		createDirectory(outputDir);
		createSubDirectories(inputDir, outputDir);
	}

	public static void copyFolder(File src, File dest) throws IOException {

		if (src.isDirectory()) {
			if (!dest.exists()) {
				dest.mkdir();
			}
			String files[] = src.list();
			for (String file : files) {
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				copyFolder(srcFile, destFile);
			}

		} else {
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);

			byte[] buffer = new byte[1024];

			int length;
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}

			in.close();
			out.close();
		}
	}

	public static boolean isLevel2Zip(String fileName) {
		return fileName.endsWith(Constants.ZIP_LEVEL_2_EXTENSION);
	}

	public static String getFileNameFromPath(String compressedFilePath, String extension) {
		int lastIndex = compressedFilePath.lastIndexOf(File.separator);
		if (lastIndex == -1) {
			return removeExtension(compressedFilePath, extension);
		}
		return removeExtension(compressedFilePath.substring(lastIndex + 1), extension);
	}

	public static String getFileNameFromPart(String fileName) {
		int lastIndex = fileName.lastIndexOf("part");
		if (lastIndex == -1) {
			return fileName;
		}
		return fileName.substring(0, lastIndex - 1);
	}

	public static String getOutputDirectory(String currentDir, String inputDir, String outputDir) {
		String parentPath = getParentDirPath(currentDir);
		String resultDir = outputDir;
		if (parentPath.length() > inputDir.length()) {
			resultDir += parentPath.substring(inputDir.length()) + File.separator;
		}
		return resultDir;
	}

	public static String getParentDirPath(String fileName) {
		File file = new File(fileName);
		return file.getParent();
	}

	public static long getMaxAllowedJVMMemoryInMB() {
		return (long) (Runtime.getRuntime().maxMemory()) / (1024L * 1024L);
	}

	public static String removeExtension(String fileName, String extension) {
		int lastIndex = fileName.lastIndexOf(extension);
		if (lastIndex == -1) {
			return fileName;
		}
		return fileName.substring(0, lastIndex);
	}

	public static void waitForTasksCompletion(List<Future> futures) {
		try {
			for (Future future : futures) {
				future.get();
			}
		} catch (InterruptedException | ExecutionException e) {
		}
	}
}
