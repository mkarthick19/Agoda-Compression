package com.challenge.agoda.service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.challenge.agoda.constants.Constants;
import com.challenge.agoda.model.FileMetadata;

// Split a file into multiples files, mBperSplit maximum number of MB per file.
public class Split {

	public List<FileMetadata> splitFile(final String fileName, final long mBperSplit, String outputDirPath)
			throws IOException {

		if (mBperSplit <= 0) {
			throw new IllegalArgumentException("mBperSplit must be more than zero");
		}
		List<Path> partFiles = new ArrayList<>();
		final long sourceSize = Files.size(Paths.get(fileName));
		final long bytesPerSplit = 1024L * 1024L * mBperSplit;
		final long numSplits = sourceSize / bytesPerSplit;
		final long remainingBytes = sourceSize % bytesPerSplit;
		int position = 0;
		int nChunks = 0;

		int lastSlashIndex = fileName.lastIndexOf(File.separator);
		String inputFileName = fileName;
		if (lastSlashIndex != -1) {
			inputFileName = fileName.substring(lastSlashIndex + 1);
		}

		try (RandomAccessFile sourceFile = new RandomAccessFile(fileName, "r");
				FileChannel sourceChannel = sourceFile.getChannel()) {

			for (; position < numSplits; position++) {
				// write multipart files.
				writePartToFile(bytesPerSplit, position * bytesPerSplit, sourceChannel, partFiles, inputFileName,
						nChunks++, outputDirPath);
			}

			if (remainingBytes >= 0) {
				writePartToFile(remainingBytes, position * bytesPerSplit, sourceChannel, partFiles, inputFileName,
						nChunks, outputDirPath);
			}
		}
		List<FileMetadata> splitFilesList = new ArrayList<>();
		for (Path path : partFiles) {
			String filePath = outputDirPath + path.getFileName().toString();
			FileMetadata fileMetadata = new FileMetadata(filePath, path.getFileName().toString());
			splitFilesList.add(fileMetadata);
		}
		return splitFilesList;
	}

	private void writePartToFile(long byteSize, long position, FileChannel sourceChannel, List<Path> partFiles,
			String inputFileName, int nChunks, String outputDirPath) throws IOException {
		Path fileName = Paths.get(outputDirPath + inputFileName + Constants.SUFFIX + nChunks);
		try (RandomAccessFile toFile = new RandomAccessFile(fileName.toFile(), "rw");
				FileChannel toChannel = toFile.getChannel()) {
			sourceChannel.position(position);
			toChannel.transferFrom(sourceChannel, 0, byteSize);
		}
		partFiles.add(fileName);
	}

}