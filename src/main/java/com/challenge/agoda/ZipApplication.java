package com.challenge.agoda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import com.challenge.agoda.model.UnzipInputRequest;
import com.challenge.agoda.model.ZipInputRequest;
import com.challenge.agoda.service.ZipService;
import com.challenge.agoda.strategy.JavaZipStrategy;

// Compression Decompression Application
public class ZipApplication {

	private static ZipService zipService;

	public static void main(String[] args) throws IOException {

		zipService = new ZipService();
		zipService.setZipStrategy(new JavaZipStrategy());

		System.out.println("Please enter 1 for Compression and 2 for Decompression :");
		MyScanner myScanner = new MyScanner();
		String userInput = myScanner.nextLine();
		if (userInput.equals("1")) {
			System.out.println("\nPlease enter the following:");
			System.out.println("\nPath to input directory :");
			String inputDirectoryPath = myScanner.nextLine();
			System.out.println("\nPath to output directory :");
			String outputDirectoryPath = myScanner.nextLine();
			System.out.println("\nMaximum Compressed Size per file threshold :");
			long maxCompressedSizeThreshold = myScanner.nextLong();

			ZipInputRequest zipInputRequest = new ZipInputRequest(inputDirectoryPath, outputDirectoryPath,
					maxCompressedSizeThreshold);
			zipService.compress(zipInputRequest);

		} else if (userInput.equals("2")) {
			System.out.println("\nPlease enter the following:");
			System.out.println("\nPath to input directory :");
			String inputDirectoryPath = myScanner.nextLine();
			System.out.println("\nPath to output directory :");
			String outputDirectoryPath = myScanner.nextLine();
			UnzipInputRequest unzipInputRequest = new UnzipInputRequest(inputDirectoryPath, outputDirectoryPath);
			zipService.decompress(unzipInputRequest);

		} else {
			System.out.println("Please enter the valid input and try again.");
		}
	}

	public static PrintWriter out;

	public static class MyScanner {
		BufferedReader bufferedReader;
		StringTokenizer stringTokenizer;

		public MyScanner() {
			bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		}

		String next() {
			while (stringTokenizer == null || !stringTokenizer.hasMoreElements()) {
				try {
					stringTokenizer = new StringTokenizer(bufferedReader.readLine());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return stringTokenizer.nextToken();
		}

		int nextInt() {
			return Integer.parseInt(next());
		}

		long nextLong() {
			return Long.parseLong(next());
		}

		String nextLine() {
			String str = "";
			try {
				str = bufferedReader.readLine();
			} catch (IOException e) {
			}
			return str;
		}
	}
}
