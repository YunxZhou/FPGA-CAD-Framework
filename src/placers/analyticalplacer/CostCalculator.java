package placers.analyticalplacer;

abstract class CostCalculator {
	
	protected boolean ints;
	protected int[] intX, intY;
	protected double[] doubleX, doubleY;
	
	
	double calculate(int[] x, int[] y) {
		this.intX = x;
		this.intY = y;
		this.ints = true;
		
		return this.calculate();
	}
	double calculate(double[] x, double[] y) {
		this.doubleX = x;
		this.doubleY = y;
		this.ints = false;
		
		return this.calculate();
	}
	
	
	protected double getX(int index) {
		if(this.ints) {
			return this.intX[index];
		} else {
			return this.doubleX[index];
		}
	}
	protected double getY(int index) {
		if(this.ints) {
			return this.intY[index];
		} else {
			return this.doubleY[index];
		}
	}
	
	
	protected abstract double calculate();
	abstract boolean requiresCircuitUpdate();
}