import java.io.*;
import java.util.Scanner;

public class ImageProcessing {
	static int numRows, numCols, minVal, maxVal;
	static int [][]imgAry;

	static Scanner inFile;
	static PrintWriter outFile_1;
	static PrintWriter outFile_2;
	
	public ImageProcessing() {
		
	}
	
	public static int getNumRows() {
		return numRows;
	}
	public static int getNumCols() {
		return numCols;
	}
	public static void main(String[] args) {
		try {
			inFile = new Scanner(new FileReader(args[0]));
			outFile_1 = new PrintWriter(new FileOutputStream(args[1]));
			outFile_2 = new PrintWriter(new FileOutputStream(args[2]));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		numRows = inFile.nextInt();
		numCols = inFile.nextInt();
		minVal = inFile.nextInt();
		maxVal = inFile.nextInt();
		
		imgAry = new int[numRows][numCols];
		loadImage(inFile, imgAry);
	
		HoughTransform hough = new HoughTransform();
		hough.buildHoughSpace(imgAry);
		prettyPrint(hough.houghAry, outFile_1);
		hough.determineMinMax(hough.houghAry);
		
		outFile_2.print(hough.houghDist + " " + hough.houghAngle + " " + hough.houghMinVal + " " + hough.HoughMaxVal);
		outFile_2.println();
		ary2File(hough.houghAry, outFile_2);
		
		inFile.close();
		outFile_1.close();
		outFile_2.close();
		
	}
	static void loadImage(Scanner inFile, int [][]ary ) {
		while(inFile.hasNextInt()) {
			for(int i = 0; i < ary.length; i++)
				for(int j = 0; j < ary[i].length; j++)
					ary[i][j] = inFile.nextInt();
		}
	}
	
	static void prettyPrint(int[][]ary, PrintWriter outFile) {
		for(int i = 0; i < ary.length; i++) {
			for(int j = 0; j < ary[i].length; j++) {
				if(ary[i][j] > 0) {
					//if(ary[i][j] >= 10)
						outFile.print(ary[i][j] + " ");
					//else
					//	outFile.print(ary[i][j] + "  ");
				}
				else
					outFile.print("  ");
			}
			outFile.println();
		}
	}
	static void ary2File(int[][]ary, PrintWriter outFile) {
		for(int i = 0; i < ary.length; i++) {
			for(int j = 0; j < ary[i].length; j++)
				outFile.print(ary[i][j] + " ");
			outFile.println();
		}
	}
	

}
