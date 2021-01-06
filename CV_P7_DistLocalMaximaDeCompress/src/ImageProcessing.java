import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Scanner;

public class ImageProcessing {
	static int numRows, numCols, minVal, maxVal, newMinVal, newMaxVal;
	static int [][]zeroFramedAry, skeletonAry;
	static Scanner inFile;
	static PrintWriter outFile_1;
	static PrintWriter outFile_2;
	static PrintWriter outFile_3;
	static PrintWriter outFile_4;
	

	public static void main(String[] args) {
		String skeletonFileName = null;
		String decompressedFileName = null;
		try {
			inFile = new Scanner(new FileReader(args[0]));
			outFile_1 = new PrintWriter(new FileOutputStream(args[1]));
			outFile_2 = new PrintWriter(new FileOutputStream(args[2]));
			
			//The name of the compressed file is to be created during the run time of your program, 
			//using the original file name with an extension “_skeleton.”
			//using string concatenation
			String imageFileName = args[0];
			String newName = imageFileName.replace(".txt", "");
			skeletonFileName = newName + "_ skeleton.txt";
			outFile_3 = new PrintWriter(new FileOutputStream(skeletonFileName));
			
			//using string concatenation
			decompressedFileName = newName + "_ decompressed.txt";
			outFile_4 = new PrintWriter(new FileOutputStream(decompressedFileName));
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		numRows = inFile.nextInt();
		numCols = inFile.nextInt();
		minVal = inFile.nextInt();
		maxVal = inFile.nextInt();
		
		zeroFramedAry = new int[numRows+2][numCols+2];
		skeletonAry = new int[numRows+2][numCols+2];
		
		//set 2D Ary to zero
		setZero(zeroFramedAry);
		setZero(skeletonAry);
		
		loadImage(inFile, zeroFramedAry);
		
		//Perform distance transform
		compute8Distance(zeroFramedAry, outFile_1);
		
		//perform lossless compression
		skeletonExtraction(zeroFramedAry, skeletonAry, outFile_3, outFile_1);
		
		//perform decompression
		skeletonExpansion(zeroFramedAry, skeletonFileName, outFile_2);
		
		outFile_4.println(numRows + " " + numCols + " " + minVal + " " + maxVal);
		
		ary2File(zeroFramedAry, outFile_4);
		
		inFile.close();
		outFile_1.close();
		outFile_2.close();
		outFile_4.close();
		
	}
	
	static void setZero(int [][] ary) {
		for(int i = 0; i < ary.length; i++)
			for(int j = 0; j < ary[i].length; j++)
				ary[i][j] = 0;
	}
	
	//Opens the input file and load to a 2D array with extra 2 rows and extra 2 cols
	static void loadImage(Scanner inFile, int [][] ary) {
		while(inFile.hasNextInt()) {
			for(int i = 1; i < ary.length - 1; i++)
				for(int j = 1; j < ary[i].length - 1; j++)
					ary[i][j] = inFile.nextInt();
		}
	}
	
	static void compute8Distance(int[][]ary, PrintWriter outFile) {
		firstPass_8Distance(ary);
		outFile.println("1st pass distance transform");
		prettyPrint(ary, outFile);
		secondPass_8Distance(ary);
		outFile.println("2nd pass distance transform");
		prettyPrint(ary, outFile);
		
	}
	
	//8-connected distance-> P(i,j) = min(a, b, c, d) + 1
	static void firstPass_8Distance(int [][]ary) {
		for(int i = 1; i < ary.length - 1; i++)
			for(int j = 1; j < ary[i].length - 1; j++)
				if(ary[i][j] > 0)
					ary[i][j] = findMin1(ary, i,j) + 1;
	}
	
	//8-connected distance-> P(i,j) = min(a+1, b+1, c+1, d+1, P(i,j))
	static void secondPass_8Distance(int [][]ary) {
		for(int i = ary.length - 2; i > 0; i--)
			for(int j = ary[i].length - 2; j > 0; j--)
				if(ary[i][j] > 0)
					ary[i][j] = findMin2(ary, i,j);
	}
	
	static int findMin1(int[][]ary, int i, int j) {
		int min = ary[i-1][j-1];
		if(ary[i-1][j] < min)
			min = ary[i-1][j];
		if(ary[i-1][j+1] < min)
			min = ary[i-1][j+1];
		if(ary[i][j-1] < min)
			min = ary[i][j-1];
		return min;
	}
	
	static int findMin2(int[][]ary, int i, int j) {
		int min = ary[i][j];
		if(ary[i][j+1] + 1 < min)
			min = ary[i][j+1] + 1;
		if(ary[i+1][j-1] + 1 < min)
			min = ary[i+1][j-1] + 1;
		if(ary[i+1][j] + 1 < min)
			min = ary[i+1][j] + 1;
		if(ary[i+1][j+1] + 1 < min)
			min = ary[i+1][j+1] + 1;
		return min;
	}
	
	static void skeletonExtraction(int [][]zAry, int[][]sAry, PrintWriter outFile3, PrintWriter outFile1){
		computeLocalMaxima(zAry, sAry);
		outFile1.println("Local maxima");
		prettyPrint(sAry, outFile1);
		extractLocalMaxima(sAry, outFile3);
		outFile3.close();
		
	}
	//P(i,j) is a local maxima, iff P(i,j) >= a,b,c,d,e,f,g and h (all)
	//if P(i,j) is Local Maxima, then skeletonAry(i,j) = P(i,j)
	//otherwise, skeletonAry(i,j) = 0
	static void computeLocalMaxima(int [][]zAry, int [][]sAry) {
		for(int i = 1; i < zAry.length - 1; i++) {
			for(int j = 1; j < zAry[i].length - 1; j++) {
				if(zAry[i][j] > 0 && isLocalMaxima(zAry, i, j))
					sAry[i][j] = zAry[i][j];
				else
					sAry[i][j] = 0;
			}
		}
	}
	
	static boolean isLocalMaxima(int [][]zAry, int i, int j) {
		for(int r = i-1; r < i+2; r++)
			for(int c = j-1; c < j+2; c++)
				if(zAry[i][j] < zAry[r][c])
					return false;
		return true;
	}
	//find newMin and newMax from skeletonAry
	//print out triplet i j skeleton (i,j) to *skeleton* file
	static void extractLocalMaxima(int [][]sAry, PrintWriter outFile3) {
		int min = 10000;
		int max = 0;
		for(int i = 1; i < sAry.length - 1; i++) {
			for(int j = 1; j < sAry[i].length - 1; j++) {
				if(sAry[i][j] > max)
					max = sAry[i][j];
				if(sAry[i][j] < min)
					min = sAry[i][j];
			}
		}
		newMinVal = min;
		newMaxVal = max;
		outFile3.println(numRows + " " + numCols + " " + newMinVal + " " + newMaxVal);
		for(int i = 1; i < sAry.length - 1; i++)
			for(int j = 1; j < sAry[i].length - 1; j++)
				if(sAry[i][j] > 0)
					outFile3.println(i + " " + j + " " + sAry[i][j]);
	}
	
	static void skeletonExpansion(int [][]ary, String skeletonFileName, PrintWriter outFile2) {
		Scanner skeletonFile = null;
		//reopen the skeletonFile
		try {
			skeletonFile = new Scanner(new FileReader(skeletonFileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		//need to reset the zeroFramedArray
		setZero(ary);
		load(skeletonFile, zeroFramedAry);
		
		firstPassExpansion(zeroFramedAry);
		outFile2.println("1st pass Expansion");
		prettyPrint(zeroFramedAry, outFile2);
		
		secondPassExpansion(zeroFramedAry);
		outFile2.println("2nd pass Expansion");
		prettyPrint(zeroFramedAry, outFile2);
		
		skeletonFile.close();
	}
	
	//Load triplets from compressed file to ZeroFramedAry
	static void load(Scanner skeletonFile, int[][]ary) {
		int rows = skeletonFile.nextInt();
		int cols = skeletonFile.nextInt();
		int min = skeletonFile.nextInt();
		int max = skeletonFile.nextInt();
		while(skeletonFile.hasNextInt()) {
			ary[skeletonFile.nextInt()][skeletonFile.nextInt()] = skeletonFile.nextInt();
		}
	}
	
	//8-connected distance-> max = max(a-1,b-1,c-1,d-1,e-1,f-1,g-1,h-1)
	static void firstPassExpansion(int [][]ary) {
		int max = 0;
		for(int i = 1; i < ary.length - 1; i++) {
			for(int j = 1; j < ary[i].length - 1; j++) {
				if(ary[i][j] == 0) {
					max = maximum(ary, i, j) - 1;
					if(ary[i][j] < max)
						ary[i][j] = max;
				}
			}
		}
	}
	
	//8-connected distance-> max = max(a,b,c,d,e,f,g,h)
	static void secondPassExpansion(int [][]ary) {
		int max = 0;
		for(int i = ary.length - 2; i > 0; i--) {
			for(int j = ary[i].length - 2; j > 0; j--) {
				max = maximum(ary, i, j);
				if(ary[i][j] < max)
					ary[i][j] = max - 1;
			}
		}
	}
	
	static int maximum(int [][]ary, int i, int j) {
		int max = 0;
		for(int r = i-1; r < i+2; r++)
			for(int c = j-1; c < j+2; c++)
				if(ary[r][c] > max)
					max = ary[r][c];
		return max;
	}
	
	static void prettyPrint(int[][]ary, PrintWriter outFile) {
		for(int i = 1; i < ary.length - 1; i++) {
			for(int j = 1; j < ary[i].length - 1; j++) {
				if(ary[i][j] > 0) {
					if(ary[i][j] >= 10)
						outFile.print(ary[i][j] + " ");
					else
						outFile.print(ary[i][j] + "  ");
				}
				else
					outFile.print("   ");
			}
			outFile.println();
		}
	}
	
	//Threshold ZeroFramedAry
	//Produce decompressed file
	static void ary2File(int [][]ary, PrintWriter outFile4) {
		for(int i = 1; i < ary.length - 1; i++) {
			for(int j = 1; j < ary[i].length - 1; j++) {
				if(ary[i][j] >= 1)
					outFile4.print("1 ");
				else
					outFile4.print("0 ");
			}
			outFile4.println();
		}			
	}

}
