package main;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;



public class Sound {
	
	
	ArrayList<Integer> soundPlayList; //store subscript of sound file to play
	Iterator<Integer> splIterator ;
	
	public final int TOTAL_CLIPS = 30;
	Clip clip;
	Clip[] clips;
	URL soundURL[]  ; //use this aray to store path of sound files
	boolean soundEnabled = true;
	Game gp;
	boolean runThread=true;
	Thread soundThread;
	boolean blockAddQueue = false;
	public long INITIAL_DEBOUNCE_COUNTER=3000000;
	public long DEF_DEBOUNCE_COUNTER=3000000;
	private final int DELAY_TICKS = 100;
	boolean clipPlayFlags[];
	boolean inputListenerAdded = false;
	Delay soundDelay = new Delay();
	
	public Sound(Game gp) {
		this.gp=gp;
		initFiles();
		initClips();
		clipPlayFlags=new boolean[TOTAL_CLIPS];
		soundPlayList = new ArrayList<Integer>();
		splIterator = soundPlayList.iterator();
		//soundThread.start();
		
		
		
		//System.out.println("thread start 1");
	}
		
	
	
	private void initFiles() {
		this.soundURL = new URL[TOTAL_CLIPS];
		soundURL[0]=getClass().getResource("/sound/aah.wav");
		soundURL[1]=getClass().getResource("/sound/mmm.wav");
		soundURL[2]=getClass().getResource("/sound/boing.wav");
		soundURL[3]=getClass().getResource("/sound/coo.wav");
		soundURL[4]=getClass().getResource("/sound/plop.wav");
	}
	
	private void initClips() {
		this.clips = new Clip[TOTAL_CLIPS];
		int clipIndex = 0;
		for (URL url : soundURL) {
			if (url!=null) {
				try {
					AudioInputStream ais = AudioSystem.getAudioInputStream(url); //create AIS from URL
					clips[clipIndex] = AudioSystem.getClip(); //get clip object for playing AIS object
					clips[clipIndex].open(ais);
				}catch (Exception e){}
				
				
			}
			clipIndex++;
		}
		
	}
	
	public void setFile(int i) {
		//ethod selects a sound file and creates objects needed to play it
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]); //create AIS from URL
			clip = AudioSystem.getClip(); //get clip object for playing AIS object
			clip.open(ais);
		}catch (Exception e) {
			
		}
		
	}
	
	public void play() {
		if(true) {
			//System.out.println("play");
			clip.start();
		}
		
		
		
		
	}
	
	public void playSE(int i) {
		if (soundDelay.delayExpired()) {
			if(soundEnabled) {
				//System.out.println("playse");
				setFile(i);
				play();
				soundDelay.setDelay(DELAY_TICKS);
			}
		}
		
		
	}
	
	
	
	public void playClipSE(int i) {
		if(soundEnabled) {
			//System.out.println("playclipse");
			clips[i].start();
			clips[i].stop();
		}
		
	}
	
	public void update() {
		soundDelay.reduce();
		
	}
	
	
	public void enqueueSE(int i) {




			
			soundPlayList.add(i);
			


		
	}
	
	public void loop() {
		if(soundEnabled) {
			
		clip.loop(Clip.LOOP_CONTINUOUSLY);
		}
	}
	
	public void stop() {
		
		clip.stop();
		
	}

	
	
	public void playMusic(int i) {
		setFile(i);
		play();
		loop();
		
	}
	public void stopMusic() {
		stop();
	}
		



	
		
		
		
	



	
}
