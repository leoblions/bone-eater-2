package main;

public class Pacer {
	
	private int tickRate;
	private int count;
	
	public Pacer(int tickRate) {
		this.tickRate = tickRate;
		this.count = 0;
	}
	
	public boolean check() {
		if (count >= tickRate) {
			count = 0;
			return true;
		}else {
			count++;
			return false;
		}
	}
	
	public int getRate() {
		return tickRate;
	}
	
	public void setRate(int rate) {
		 if (rate>0) {
			 this.tickRate = rate;
		 }
	}

}
