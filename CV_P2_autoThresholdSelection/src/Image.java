import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;

public class Image {
	static Scanner inFile;
	static PrintWriter outFile1;
	static PrintWriter outFile2;
	static PrintWriter outFile3;
	static int numRows;
	static int numCols;
	static int minVal;
	static int maxVal;
	static int maxHight;
	static int offset;
	static int thrVal;
	static int [] histAry;
	static int [][] histGraph;
	static int [] gaussAry;
	static int [][] gaussGraph;
	static int [][] gapGraph;

	public static void main(String[] args) {
		try {
			inFile = new Scanner(new FileReader(args[0]));
			outFile1 = new PrintWriter( new FileOutputStream(args[1]));
			outFile2 = new PrintWriter( new FileOutputStream(args[2]));
			outFile3 = new PrintWriter( new FileOutputStream(args[3]));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		numRows = inFile.nextInt();
		numCols = inFile.nextInt();
		minVal = inFile.nextInt();
		maxVal = inFile.nextInt();
		offset = (maxVal-minVal)/10;   //the first modal occupies at least one-tenth from minVal to maxVal of the histogram
		thrVal = offset;
		
		//a 1D integer array to store the histogram
		histAry = new int[maxVal+1];
		for(int i = 0; i < histAry.length; i++)
			histAry[i] = 0;
		
		//a 1D integer array to store the “modified” Gaussian function
		gaussAry = new int[maxVal+1];
		for(int i = 0; i < gaussAry.length; i++)
			gaussAry[i] = 0;
		
		loadHist(histAry,inFile);    //reads and loads the histAry from inFile
		
		//find the maxHight
		int max = 0;
		for(int i = 0; i < maxVal+1; i++) {
			if(histAry[i]>max)
				max = histAry[i];
		}
		
		maxHight = max;   //The largest histAry[i] of the given portion of the histogram
		
		//a 2-D integer array for displaying the histogram in 2D
		histGraph = new int[maxVal+1][maxHight+1];
		for(int i = 0; i < histGraph.length; i++) {
			for(int j = 0; j < histGraph[i].length; j++)
				histGraph[i][j] = 0;
		}
		
		//a 2-D integer array for displaying Gaussian curves in 2D
		gaussGraph = new int[maxVal+1][maxHight+1];
		for(int i = 0; i < gaussGraph.length; i++) {
			for(int j = 0; j < gaussGraph[i].length; j++)
				gaussGraph[i][j] = 0;
		}
		
		//a 2-D integer array for displaying the gaps between Gaussian curves and the histogram in 2D
		gapGraph = new int[maxVal+1][maxHight+1];
		for(int i = 0; i < gapGraph.length; i++) {
			for(int j = 0; j < gapGraph[i].length; j++)
				gapGraph[i][j] = 0;
		}
		
		plotHistGraph(histGraph);
		outFile1.println("A 2-D display of the histogram");
		prettyPrint(histGraph,outFile1);
		int bestThrVal = biMeanGauss(thrVal);   //determines the best threshold selection (via fitGauss method)
		System.out.println(bestThrVal);
		outFile1.println("The selected threshold value is " + bestThrVal);
		
		bestThrPlot(bestThrVal);   //Plots the 2 best fit Gaussians curves
		outFile1.println("A 2-D display of the two best-fitting Gaussians curves overlaying onto the histogram");
		prettyPrint(gaussGraph, outFile1);
		
		outFile1.println("A 2-D display of the gaps between the two best-fitting Gaussian curves and the histogram");
		prettyPrint(gapGraph,outFile1);
		
		inFile.close();
		outFile1.close();
		outFile2.close();
		outFile3.close();

	}
	
	public static void loadHist(int[] histAry, Scanner inFile) {
		int i=0;
		while (inFile.hasNextInt()) {
			i = inFile.nextInt();
			histAry[i] = inFile.nextInt();
		}
	}
	
	public static void plotHistGraph(int[][] histGraph) {
		for(int i = 0; i < histGraph.length; i++) {
			for(int j = 0; j < histGraph[i].length; j++) {
				if(j == histAry[i])
					histGraph[i][j] = 1;   //assign histGraph [i, hist[i]] to 1
			}
		}
	}
	
	public static void prettyPrint(int[][] graph, PrintWriter outFile) {
		for(int i = 0; i < graph.length; i++) {
			for(int j = 0; j < graph[i].length; j++) {
				if(graph[i][j] <= 0)
					outFile.print(" ");
				else
					outFile.print("*");
			}
			outFile.println();
		}
	}
	
	//determines the best threshold selection where the two Gaussian curves fit the histogram the best
	public static int biMeanGauss(int thrVal) {
		double sum1,sum2, total, minSumDiff;
		int bestThr = thrVal;
		minSumDiff = 999999.0;
		while(thrVal < (maxVal - offset)) {
			//reset for next computation
			set1DZero(gaussAry);
			set2DZero(gaussGraph);
			set2DZero(gapGraph);
			sum1 = fitGauss(0, thrVal, gaussAry, gaussGraph);
			sum2 = fitGauss(thrVal, maxVal, gaussAry, gaussGraph);
			total = sum1 + sum2;
			if(total < minSumDiff) {
				minSumDiff = total;
				bestThr = thrVal;
			}
			thrVal++;
			prettyPrint(gaussGraph, outFile2);
			plotGaps(histAry, gaussGraph, gapGraph);
			prettyPrint(gapGraph, outFile3);
		}
		return bestThr;
	}
	
	public static void set1DZero(int[]array) {
		for(int i = 0; i < array.length; i++)
			array[i] = 0;
	}
	
	public static void set2DZero(int[][]array) {
		for(int i = 0; i < array.length; i++) {
			for(int j = 0; j < array[i].length; j++)
				array[i][j] = 0;
		}
	}
	
	//computes the Gaussian curve fitting to the histogram
	public static double fitGauss(int leftIndex, int rightIndex, int []gaussAry, int[][]gaussGraph) {
		double mean, var, sum, gVal, maxGval;
		sum = 0.0;
		mean = computeMean(leftIndex, rightIndex);
		var = computeVar(leftIndex, rightIndex, mean);
		int index = leftIndex;
		while(index <= rightIndex) {
			gVal = modifiedGauss(index, mean, var, maxHight);
			sum += Math.abs(gVal - (double)histAry[index]);
			gaussAry[index] = (int) gVal;
			gaussGraph[index][gaussAry[index]] = 1;
			index++;
		}
		return sum;
	}
	
	//computes and returns the *weighted* average of the histogram from given leftIndex to rightIndex
	public static double computeMean(int leftIndex, int rightIndex) {
		maxHight = 0;
		double sum = 0;
		double numPixels = 0;
		int index = leftIndex;
		while(index < rightIndex) {
			sum += (histAry[index]*index);
			numPixels += histAry[index];
			if (histAry[index] > maxHight)
				maxHight = histAry[index];
			index++;
		}
		double m = sum/numPixels;
		return m;
	}
	
	//computes and returns the *weighted* variance from given leftIndex to rightIndex
	public static double computeVar(int leftIndex, int rightIndex, double mean) {
		double sum = 0.0;
		double numPixels = 0;
		int index = leftIndex;
		while(index < rightIndex) {
			sum += (double) histAry[index]*(Math.pow((double)index-mean, 2));
			numPixels += histAry[index];
			index++;
		}
		double m = sum/numPixels;
		return m;
	}
	
	//g(x) = a * exp (- ((x-b)^2)/(2*c^2))), a is the height of the Gaussian Bell curve
	//a = 1/(sqrt(var * 2 * pi), b is mean and c^2 is variance
	public static double modifiedGauss(double x, double mean, double var, int maxHeight) {
		double a = maxHight * Math.exp(-(Math.pow((x-mean), 2))/(2*var));
		return a;
	}
	
	//Plots the 2 best fit Gaussians curves 
	public static void bestThrPlot(int bestThrVal) {
		double sum1, sum2;
		set1DZero(gaussAry);
		set2DZero(gaussGraph);
		set2DZero(gapGraph);
		sum1 = fitGauss(0, bestThrVal, gaussAry, gaussGraph);
		sum2 = fitGauss(bestThrVal, maxVal, gaussAry, gaussGraph);
		plotGaps(histAry,gaussGraph, gapGraph);
	}
	
	//Plot the gaps between the Gaussian curves and the  histogram
	public static void plotGaps(int[]histAry, int[][]gaussGraph, int[][]gapGraph) {
		int index = minVal;
		int first;
		int last;
		while(index < maxVal) {
			first = min(histAry[index], gaussAry[index]);
			last = max(histAry[index], gaussAry[index]);
			while(first < last) {
				gapGraph[index][first] = 1;
				first++;
			}
			index++;
		}
	}
	
	public static int min(int a, int b) {
		int m = 0;
		if(a < b)
			m = a ;
		else 
			m = b;
		return m;
	}
	
	public static int max(int a, int b) {
		int m = 0;
		if(a > b)
			m = a ;
		else 
			m = b;
		return m;
	}
}
