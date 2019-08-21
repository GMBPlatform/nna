package Main;

public class Main {
	private static Global C_Global;
	
	public static void main(String[] agrs) {
		C_Global = new Global();
		
		C_Global.getCLogger().OutConsole(C_Global.getCLogger().LOG_INFO, "GMB Node Program Start");
		// Check Database can connect
		// if database can't connect then exit program
		if(C_Global.getCDBHelper().DBConnectionCreate()) {
			// also check create database table schema
			// if table schema can't create then exit program
			if(C_Global.getCDBHelper().DBinit()) {
				//C_Global.getCDBHelper().TruncateTables();
				C_Global.ParseNodeJson();
				C_Global.getCP2P().init();
				C_Global.getCConsensus().init();
				C_Global.getCKeyUtil().MyKeyInit();
				
				C_Global.getCRunnableThread().StartNode();
				
				C_Global.getCDBHelper().DBConnectionClose();
			} else {
				C_Global.getCLogger().OutConsole(C_Global.getCLogger().LOG_ERROR, "Can't Initialize Database And Program Exit");
			}
		} else {
			C_Global.getCLogger().OutConsole(C_Global.getCLogger().LOG_ERROR, "Can't Connection MySQL Database And Program Exit");
			C_Global.getCLogger().OutConsole(C_Global.getCLogger().LOG_ERROR, "Please Check dbConf.json or MySQL is Installed");
		}
	}
}
