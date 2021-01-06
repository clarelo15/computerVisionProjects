
public class HoughTransform {
	class XYCoord {
		int x;
		int y;
		XYCoord(int x, int y){
			this.x = x;
			this.y = y;
		}
	}
	XYCoord point;
	int angleInDegree;
	double angleInRadians;
	int houghAngle = 180;
	int houghDist = (int)(2 * (Math.sqrt(Math.pow((double)ImageProcessing.getNumRows(), 2) + Math.pow((double)ImageProcessing.getNumCols(), 2))));
	//int houghDist = (int)(2 * (Math.sqrt(Math.pow((double)ImageProcessing.getNumRows() - 1, 2) + Math.pow((double)ImageProcessing.getNumCols() - 1, 2)))) + 1;
	int houghMinVal;
	int HoughMaxVal;
	int [][]houghAry;
	
	HoughTransform() {
		houghAry = new int[houghDist][houghAngle];
	}
	public void setZero(int [][]houghAry) {
		for(int i = 0; i < houghAry.length; i++)
			for(int j = 0; j < houghAry[i].length; j++)
				houghAry[i][j] = 0;
	}
	
	public void buildHoughSpace(int [][]aryI) {
		double dist = 0;
		int distInt = 0;
		for(int r = 0; r < aryI.length; r++) {
			for(int c = 0; c < aryI[r].length; c++) {
				if(aryI[r][c] > 0) {
					point = new XYCoord(c,r);
					point.x = c;
					point.y = r;
					System.out.println(point.x + " " + point.y);
					
					angleInDegree = 0;
					while(angleInDegree <= 179) {
						angleInRadians = angleInDegree / 180.00 * Math.PI;
						dist = Math.round(computeDistance(point, angleInRadians));
						distInt = (int)dist;
						houghAry[distInt][angleInDegree]++;
						angleInDegree++;
					}
				}
			}
		}
	}
	
	public double computeDistance(XYCoord point, double angleInRadians) {
		double distance = 0;
		double direct = 0;
		double offset = houghDist / 2;
		//double offset = (houghDist - 1) / 2;
		double x = point.x;
		double y = ImageProcessing.getNumRows() - point.y - 1;
		double t = 0;
		/*if(x == 0 && y == 0)
			return 0;
		else if(y == 0)
			return (double)x * Math.sin(angleInRadians) + offset;
		else if(x == 0)
			return (double)y * Math.sin(angleInRadians - (Math.PI/2)) + offset;
		else
			t = angleInRadians - Math.atan(y/x) - Math.PI/2;
			direct = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
			distance = direct * Math.cos(t) + offset;
		*/
		if(x == 0 || y == 0)
			t = angleInRadians - Math.PI/2;
		else
			t = angleInRadians - Math.atan(y/x) - Math.PI/2;
		direct = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
		distance = direct * Math.cos(t) + offset;
		
		return distance;
	}
	
	public void determineMinMax(int [][]ary) {
		int max = 0;
		int min = 99999;
		for(int i = 0; i < ary.length; i++) {
			for(int j = 0; j < ary[i].length; j++) {
				if(ary[i][j] > max)
					max = ary[i][j];
				if(ary[i][j] < min)
					min = ary[i][j];
			}
		}
		houghMinVal = min;
		HoughMaxVal = max;
	}
}

