package Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import Main.Global;

public class FileIO {
	private Global global;
	private BufferedReader br = null;
	
	public FileIO(Global global) {
		this.global = global;
	}
	
	// File Read
	public String FileReadString(String FileName) {
		String res = "";
		
		try {
			this.br = new BufferedReader(new FileReader(FileName));
		} catch (FileNotFoundException e) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, 
					FileName + " FileNotFoundException");
			return "Error FileNotFoundException";
		}
		String line;
		try {
			while((line = br.readLine()) != null) 
				res += line + "\n";
			br.close();
		} catch (IOException e) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, 
					FileName + " FileIOException");
			return "Error IOException";
		}
		line = null;
		return res;
	}
	
	// mkdir for store peer public key directory
	public boolean mkPeerKeydir(String dirPath) {
		File Folder = new File("./key/" + dirPath);
		boolean ret = false;
		
		if(!Folder.exists()) {
			// Directory not exist
			ret = makeDir(Folder);
			Folder = null;
			return ret;
		} else {
			// Directory exist -> Delete current directory and files -> new create directory and file
			File[] deleteFolderList = Folder.listFiles();
			
			for(int i = 0; i < deleteFolderList.length; i++) deleteFolderList[i].delete();
			if(deleteFolderList.length == 0 && Folder.isDirectory()) Folder.delete();
			ret = makeDir(Folder);
			Folder = null;
			deleteFolderList = null;
			return ret;
		}
	}
	
	public boolean makeDir(File Folder) {
		try {
			Folder.mkdir();
			return true;
		} catch (Exception e) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "mkdir Exception");
			return false;
		}
	}
	
	// mkfile for store peer public key file
	public boolean mkPeerKeyfile(String dirPath, String fileName) {
		File file = new File("./key/" + dirPath + "/" + fileName);
		boolean ret = false;
		if(!file.exists()) {
			ret = makeFile(file);
			file = null;
			return ret;
		} else {
			file.delete();
			ret = makeFile(file);
			file = null;
			return ret;
		}
	}
	
	public boolean makeFile(File file) {
		try {
			file.createNewFile();
			return true;
		} catch (Exception e) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "mkfile Exception");
			return false;
		}
	}
}
