import java.io.*;
import java.util.Scanner;

public class ImageEdge {
	static int numRows;
	static int numCols;
	static int minVal;
	static int maxVal;
	static int [][]mirrorFramedAry;
	static int [][]maskRobertRightDiag;
	static int [][]maskRobertLeftDiag;
	static int [][]maskSobelRightDiag;
	static int [][]maskSobelLeftDiag;
	static int [][]robertRightDiag;
	static int [][]robertLeftDiag;
	static int [][]sobelRightDiag;
	static int [][]sobelLeftDiag;
	static int [][]gradientEdge;
	static int [][]edgeSum;
	static Scanner inFile;
	static PrintWriter robertEdgeOut;
	static PrintWriter sobelEdgeOut;
	static PrintWriter gradientEdgeOut;
	static PrintWriter prettyOut;
	static PrintWriter deBugOut;
	
	public ImageEdge() {
		
	}

	public static void main(String[] args) {
		try {
			inFile = new Scanner(new FileReader(args[0]));
			robertEdgeOut = new PrintWriter(new FileOutputStream(args[1]));
			sobelEdgeOut = new PrintWriter(new FileOutputStream(args[2]));
			gradientEdgeOut = new PrintWriter(new FileOutputStream(args[3]));
			prettyOut = new PrintWriter( new FileOutputStream(args[4]));
			deBugOut = new PrintWriter( new FileOutputStream(args[5]));
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		numRows = inFile.nextInt();
		numCols = inFile.nextInt();
		minVal = inFile.nextInt();
		maxVal = inFile.nextInt();
		
		mirrorFramedAry = new int[numRows+2][numCols+2];
		for(int i = 0; i < mirrorFramedAry.length; i++) {
			for(int j = 0; j < mirrorFramedAry[i].length; j++)
				mirrorFramedAry[i][j] = 0;
		}
		
		maskRobertRightDiag = new int[2][2];
		maskRobertRightDiag[0][0] = -1;
		maskRobertRightDiag[0][1] = 1;
		maskRobertRightDiag[1][0] = 1;
		maskRobertRightDiag[1][1] = -1;
			
		maskRobertLeftDiag = new int[2][2];
		maskRobertLeftDiag[0][0] = 1;
		maskRobertLeftDiag[0][1] = -1;
		maskRobertLeftDiag[1][0] = -1;
		maskRobertLeftDiag[1][1] = 1;
		
		maskSobelRightDiag = new int[3][3];
		maskSobelRightDiag[0][0] = 2;
		maskSobelRightDiag[0][1] = 1;
		maskSobelRightDiag[0][2] = 0;
		maskSobelRightDiag[1][0] = 1;
		maskSobelRightDiag[1][1] = 0;
		maskSobelRightDiag[1][2] = -1;
		maskSobelRightDiag[2][0] = 0;
		maskSobelRightDiag[2][1] = -1;
		maskSobelRightDiag[2][2] = -2;
		
		maskSobelLeftDiag = new int[3][3];
		maskSobelLeftDiag[0][0] = 0;
		maskSobelLeftDiag[0][1] = 1;
		maskSobelLeftDiag[0][2] = 2;
		maskSobelLeftDiag[1][0] = -1;
		maskSobelLeftDiag[1][1] = 0;
		maskSobelLeftDiag[1][2] = 1;
		maskSobelLeftDiag[2][0] = -2;
		maskSobelLeftDiag[2][1] = -1;
		maskSobelLeftDiag[2][2] = 0;
		
		robertRightDiag = new int[numRows+2][numCols+2];
		robertLeftDiag = new int[numRows+2][numCols+2];
		sobelRightDiag = new int[numRows+2][numCols+2];
		sobelLeftDiag = new int[numRows+2][numCols+2];
		gradientEdge = new int[numRows+2][numCols+2];
		edgeSum = new int[numRows+2][numCols+2];
		
		loadImage(mirrorFramedAry,inFile);
		mirrorFramed(mirrorFramedAry);
		
		for(int i = 1; i < mirrorFramedAry.length - 1; i++) {
			for(int j = 1; j < mirrorFramedAry[i].length - 1; j++) {
				robertRightDiag[i][j] = Math.abs(convoluteRobert(i,j,maskRobertRightDiag,mirrorFramedAry));
				robertLeftDiag[i][j] = Math.abs(convoluteRobert(i,j,maskRobertLeftDiag,mirrorFramedAry));
				sobelRightDiag[i][j] = Math.abs(convoluteSobel(i,j,maskSobelRightDiag));
				sobelLeftDiag[i][j] = Math.abs(convoluteSobel(i,j,maskSobelLeftDiag));
				gradientEdge[i][j] = computeGradient(i,j);
			}
		}
		
		addTwoArys(robertRightDiag, robertLeftDiag, edgeSum);
		deBugOut.println("RobertRightDiag");
		print(robertRightDiag, deBugOut);
		deBugOut.println("RobertLeftDiag");
		print(robertLeftDiag, deBugOut);
		
		int max1 = 0;
		int min1 = edgeSum[1][1];
		for(int i = 1; i < edgeSum.length - 1; i++) {
			for(int j = 1; j < edgeSum[i].length - 1; j++) {
				if(edgeSum[i][j] > max1)
					max1 = edgeSum[i][j];
				if(edgeSum[i][j] < min1)
					min1 = edgeSum[i][j];
			}
		}
		maxVal = max1;
		minVal = min1;
		
		robertEdgeOut.println(numRows + " " + numCols +" " + minVal + " " + maxVal);
		print(edgeSum, robertEdgeOut);
		
		addTwoArys (sobelRightDiag, sobelLeftDiag, edgeSum);
		deBugOut.println("SobelRightDiag");
		print(sobelRightDiag, deBugOut);
		deBugOut.println("SobelLeftDiag");
		print(sobelLeftDiag, deBugOut);
		
		int max2 = 0;
		int min2 = edgeSum[1][1];
		for(int i = 1; i < edgeSum.length - 1; i++) {
			for(int j = 1; j < edgeSum[i].length - 1; j++) {
				if(edgeSum[i][j] > max2)
					max2 = edgeSum[i][j];
				if(edgeSum[i][j] < min2)
					min2 = edgeSum[i][j];
			}
		}
		maxVal = max2;
		minVal = min2;
		
		sobelEdgeOut.println(numRows + " " + numCols +" " + minVal + " " + maxVal);
		print(edgeSum, sobelEdgeOut);
		
		int max3 = 0;
		int min3 = gradientEdge[1][1];
		for(int i = 1; i < gradientEdge.length - 1; i++) {
			for(int j = 1; j < gradientEdge[i].length - 1; j++) {
				if(gradientEdge[i][j] > max3)
					max3 = gradientEdge[i][j];
				if(gradientEdge[i][j] < min3)
					min3 = gradientEdge[i][j];
			}
		}
		maxVal = max3;
		minVal = min3;
		
		gradientEdgeOut.println(numRows + " " + numCols +" " + minVal + " " + maxVal);
		print(gradientEdge, gradientEdgeOut);
		
		inFile.close();
		robertEdgeOut.close();
		sobelEdgeOut.close();
		gradientEdgeOut.close();
		prettyOut.close();
		deBugOut.close();

	}
	
	public static void loadImage(int [][] mirrorFramedAry, Scanner inFile) {
		while(inFile.hasNextInt()) {
			for(int i = 1; i < mirrorFramedAry.length-1; i++) 
				for(int j = 1; j < mirrorFramedAry[i].length-1; j++)
					mirrorFramedAry[i][j] = inFile.nextInt();
		}
	}
	
	public static void mirrorFramed(int [][]mirrorFramedAry) {
		for(int i = 0; i < numRows + 2; i++) {
			mirrorFramedAry[i][0] = mirrorFramedAry[i][1];
			mirrorFramedAry[i][numCols+1] = mirrorFramedAry[i][numCols];
		}
		for(int j = 0; j < numCols + 2; j++) {
			mirrorFramedAry[0][j] = mirrorFramedAry[1][j];
			mirrorFramedAry[numRows + 1][j] = mirrorFramedAry[numRows][j];
		}
	}
	
	public static void print(int [][]ary, PrintWriter outFile) {
		for(int i = 1; i < ary.length - 1; i++) {
			for(int j = 1; j < ary[i].length - 1; j++)
				outFile.print(ary[i][j] + " ");
			outFile.println();
		}
	}
	
	public static int convoluteRobert(int i, int j, int [][]mask, int [][]mirrorFramedAry) {
		int sum = 0;
		for(int r = 0; r < mask.length;  r++)
			for(int c = 0; c < mask[r].length; c++)
				sum += mask[r][c] * mirrorFramedAry[i+r][j+c];
		return sum;
	}
	
	public static int convoluteSobel(int i, int j, int [][]mask) {
		int sum = 0;
		for(int r = 0; r < mask.length;  r++)
			for(int c = 0; c < mask[r].length; c++)
				sum += mask[r][c] * mirrorFramedAry[i-1+r][j-1+c];
		return sum;
	}
	
	public static int computeGradient(int i, int j) {
		double sum = Math.pow((mirrorFramedAry[i][j] - mirrorFramedAry[i+1][j]), 2) + Math.pow((mirrorFramedAry[i][j] - mirrorFramedAry[i][j+1]), 2);
		double graVal = Math.sqrt(sum);
		
		return (int) graVal;
	}
	
	public static void addTwoArys(int [][]ary1, int [][]ary2, int [][]ary3) {
		for(int i = 0; i < ary3.length; i++)
			for(int j = 0; j < ary3[i].length; j++)
				ary3[i][j] = ary1[i][j] + ary2[i][j];
	}

}
