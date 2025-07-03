package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Scanner;

public class SettingsIO {
	String filename;
	public Dictionary<String, String> stringsDict;
	public Dictionary<String, String> defaultsDict;
	FileReader fileReader;
	public final char SEPERATOR = '=';
	public final String NEWLINE = "\r\n";
	File file;

	public SettingsIO(String filename) throws FileNotFoundException {
		// TODO Auto-generated constructor stub
		this.filename = filename;
		stringsDict = new Hashtable<>();
		
		defaultsDict = new Hashtable<>();
		defaultsDict.put("godMode","false");
		defaultsDict.put("noClip","false");
		defaultsDict.put("drawShadows","false");

		defaultsDict.put("title","DEMISE 2000");
		
	}
	/**
	 * Try reading the settings file, and create it if it does not exist.
	 * @throws IOException
	 */
	public void readFile() throws IOException {
		
		file = new File(this.filename);
		System.out.println(file.getPath());
		Scanner myReader;
		//myReader = new Scanner(file);
		try {
			if(!file.exists()) throw new IOException();
			
			 myReader = new Scanner(file);
			 System.out.println("readFile file ok ");
		}catch(Exception e) {
			//if reading fails, create the file with defaults dict
			System.out.println("readFile making default file");
			//file.createNewFile();
			
			createDefaultFile();
			 myReader = new Scanner(file);
		}
		String data, d1, d2;
		int indexSep;
		//stringsDict = new Hashtable<>();
		while(myReader.hasNextLine()) {
			data = myReader.nextLine();
			indexSep = data.indexOf(SEPERATOR);
			if (indexSep==-1)continue;
			d1 = data.substring(0,indexSep);
			//remove non alphanumeric characters
			d1 = d1.replaceAll(
			          "[^a-zA-Z0-9]", "");
			d2 = data.substring(indexSep+1);
			d2 = d2.replaceAll(
			          "[^a-zA-Z0-9]", "");
			//System.out.println(d1);
			//System.out.println(d2);
			stringsDict.put(d1,d2);
			 
			
		} myReader.close();
		 ;
		
	}
	
	public boolean createDefaultFile() {
		try {
			file = new File(this.filename);
			file.createNewFile();
			writeFile(defaultsDict);
			System.out.println("createDefaultFile OK");
			return true;
			
		}catch(Exception e) {
			
			System.out.println("createDefaultFile failed");
			return false;
		}
		
	}
	
	public void deleteFile( ) { 
		boolean result=false;
	    File myObj = new File(this.filename); 
	    try {
	    	Files.deleteIfExists(Paths.get(this.filename))  ;
	    }catch(Exception e) {
	    	e.printStackTrace();
	    }
	    if (result) { 
	      System.out.println("Deleted the file: " + myObj.getName());
	    } else {
	      System.out.println("Failed to delete the file.");
	    } 
	  } 
	
	public void writeFile() throws IOException {
		file = new File(filename);
		
		if(file.createNewFile()) {
			System.out.println("file created: "+file.getName());
			
		} else {
			System.out.println("file exists: "+file.getName());
			if(file.delete()) {
				System.out.println("deleting...");
			}else {
				System.out.println("delete failed");
			}
			file.createNewFile();
		}
		String key, value;
		FileWriter fwriter = new FileWriter(this.filename);
		Enumeration keys= stringsDict.keys();
		while(keys.hasMoreElements()) {
			key = (String) keys.nextElement();
			value = stringsDict.get(key);
			fwriter.write(key+SEPERATOR+value+"\n");
		}
		fwriter.close();
		
	}
	
	public void writeFile(Dictionary inpDict) throws IOException {
		file = new File(this.filename);
		
		if(file.createNewFile()) {
			System.out.println("file created: "+file.getName());
			
		} else {
			System.out.println("file exists: "+file.getName());
			if(file.delete()) {
				System.out.println("deleting...");
			}else {
				System.out.println("delete failed");
			}
			file.createNewFile();
		}
		String key, value;
		FileWriter fwriter = new FileWriter(this.filename);
		Enumeration keys= inpDict.keys();
		while(keys.hasMoreElements()) {
			key = (String) keys.nextElement();
			value = (String) inpDict.get(key);
			fwriter.write(key+SEPERATOR+value+NEWLINE);
		}
		fwriter.close();
		
	}

}
