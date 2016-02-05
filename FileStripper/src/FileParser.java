import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Scanner;

public class FileParser {

	// Manage block comments.
	private static Boolean isInBlockComment = false;

	public static void main(String[] args) throws IOException {

		// Current working directory.
		File f = new File("."); // current directory

		System.out.println("Processing test files in " + f.getAbsolutePath());

		// Delete any "striptest-*-out.txt" files from current working
		// directory.
		deleteOutFiles();

		// Get list of test files to process.
		File[] files = getTestFiles();

		if (files == null || files.length == 0) {
			System.out.println("No tes files found!");
			return;
		}

		// Iterate test files for processing.
		for (int i = 0; i < files.length; i++) {

			System.out.println("Processing file: " + files[i].getCanonicalPath());

			processFile(files[i]);
		}
	}

	private static void deleteOutFiles() {

		File f = new File("."); // current directory

		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				String lowercaseName = name.toLowerCase();
				if (lowercaseName.startsWith("striptest-") && lowercaseName.endsWith(".txt")
						&& lowercaseName.contains("-out")) {
					return true;
				} else {
					return false;
				}
			}
		};

		File[] files = f.listFiles(filter);

		if (files == null || files.length == 0)
			return; // no out files found to delete

		for (int i = 0; i < files.length; i++) {
			files[i].delete();
		}
	}

	private static void processFile(File inFile) throws IOException {

		// Output file writer.
		BufferedWriter out = null;

		String outFilename = getOutputFileName(inFile);

		try {

			FileWriter fstream = new FileWriter(outFilename, true);

			// Output file
			out = new BufferedWriter(fstream);

			Scanner input = new Scanner(inFile);

			while (input.hasNextLine()) {

				// Get processed line for output.
				String newLine = processLine(input.nextLine());

				if (newLine != null) {
					out.write(newLine);
				}

				// Handle dangling CRLF.
				if (input.hasNextLine() && newLine != null)
					out.write("\n");
			}

			input.close();

			System.out.println("Writing output file " + outFilename);

		} catch (IOException ioe) {

		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	// A method to determine what text to include in the current line.
	private static String processLine(String line) {

		// Flag indicating that current line contains text.
		// Boolean hasText = true;
		// Boolean foundSlash = false;

		// Handle blank line case
		if (line == null || line.length() == 0)
			return null;

		// Convert line to char. array.
		char[] array = line.toCharArray();

		// Handle case when we are in a black comment.
		// Case 1 - No Stop Block Comment found, remove entire line
		// Case 2 - Stop Block Comment found. Remove beginning of line through
		// Stop Block Comment.
		// Can we merge these cases?

		// Get position of 1st stop block comment, "*/".
		// -1 indicates no start block comment found.
		int bcStop = getBlockCommentStopPosition(array);
		int bcStart = getBlockCommentStopPosition(array);

		if (isInBlockComment && bcStop < 0)
			return null;

		if (isInBlockComment && bcStop >= 0) {

			// trim beginning of array up to bcStart.
			char[] newArray = new char[array.length - bcStop];
			for (int i = bcStop; i < array.length; i++) {
				newArray[i - bcStop] = array[i];
			}
			array = newArray;
			isInBlockComment = false;
		}

		do {
			bcStart = getBlockCommentStartPosition(array);
			bcStop = getBlockCommentStopPosition(array);

		} while (bcStart == -1 || bcStop == -1);

		// HANDLE LINES WITH MULTIPLE COMMENTS
		// good text // comment
		// good text /* comment */ good text /* comment */ good text // line
		// comment.

		// Lets process any line comment first and then process any block
		// comments.

		// Get position of 1st start block comment, "/*".
		// -1 indicates no start block comment found.
		// bcStart = getBlockCommentStartPosition(array);

		// Handle whitespace
		if (isLineWhitespace(array))
			return null;

		// Handle block comment.
		if (hasBlockCommentStop(array)) {
			int posBlockCommentStop = getBlockCommentStopPosition(array);

			if (isInBlockComment && posBlockCommentStop > 0) {
				isInBlockComment = false;
				return array.toString().substring(0, posBlockCommentStop);
			}
		}

		if (isInBlockComment)
			return array.toString();

		int posBlockCommentStart = getBlockCommentStartPosition(array);

		// if(posBlockCommentStart>=0)

		// int posLineComment = getLineCommentPosition(array);

		// if(isInBlockComment && posBlockCommentStop>0)
		// return array.toString().substring(0, posBlockCommentStop);

		// Handle full line comment
		// if(array.length > 1 && array[0]=='/' && array[1]=='/' ){
		// return null;
		// }

		// This is the line to return after processing
		String newLine = "";

		// A variable used to determine if a line comment is found.
		// int slash1 = false;

		// Handle whitespace only case.
		for (int i = 0; i < array.length; i++) {

			newLine = newLine + array[i] + "";

			// Special case: line may contain a single slash char.
			// if(isInBlockComment && line.length()==1)
			// break;

			// String curr = line.substring(i, i+1);

			// if(curr != " " && curr != "\t")
			// hasText = true;

			// if(line.substring(i, i+))

		}

		return newLine;
	}

	private static boolean hasBlockCommentStop(char[] array) {
		for (int i = 1; i < array.length - 1; i++) {

			if (array[i - 1] == '*' && array[i] == '/') {
				return true;
			}
		}

		return false;
	}

	private static int getBlockCommentStopPosition(char[] array) {

		for (int i = 1; i < array.length - 1; i++) {

			if (array[i - 1] == '*' && array[i] == '/') {
				return i;
			}
		}

		// -1 indicates no stop block comment found.
		return -1;

	}

	private static int getLineCommentPosition(char[] array) {

		for (int i = 1; i < array.length - 1; i++) {

			if (array[i - 1] == '/' && array[i] == '/') {
				return i - 1;
			}
		}

		return 0;
	}

	private static int getBlockCommentStartPosition(char[] array) {

		for (int i = 1; i < array.length - 1; i++) {

			if (array[i - 1] == '/' && array[i] == '*') {
				return i - 1;
			}
		}

		return 0;
	}

	private static boolean isLineWhitespace(char[] array) {

		for (int i = 0; i < array.length; i++) {

			// Check for character other than tab or space.
			if (array[i] != '\t' && array[0] != ' ') {
				return false;
			}
		}

		return true;

	}

	// A method to create the output file name.
	private static String getOutputFileName(File inFile) {
		final String fileNameSuffix = "-out";
		String fileName = inFile.getName();
		int separator = fileName.indexOf('.');
		return fileName.substring(0, separator) + fileNameSuffix + ".txt";
	}

	private static File[] getTestFiles() {

		File f = new File("."); // current directory

		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				String lowercaseName = name.toLowerCase();
				if (lowercaseName.startsWith("striptest-") && lowercaseName.endsWith(".txt")) {
					return true;
				} else {
					return false;
				}
			}
		};

		return f.listFiles(filter);
	}
}
