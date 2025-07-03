package main;

public class Delay {
	private int ticksRemaining=0;
	public Delay() {
		
	}
	public void reduce() {
		if (ticksRemaining > 0){
			ticksRemaining -=1;
		}
	}
	public void reset() {
		ticksRemaining =0;;
	}
	public void setDelay(int delayTicks) {
		if(delayTicks>0) {
			ticksRemaining=delayTicks;
		}
	}
	public boolean delayExpired() {
		return(ticksRemaining<=0)? true:false; 
	}
}
