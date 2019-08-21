package Main;

import java.util.concurrent.Semaphore;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import Consensus.Consensus;
import P2P.P2P;
import Contract.Contract;

import Util.DBHelper;
import Util.FileIO;
import Util.KeyUtil;
import Util.Runnable_Thread;
import Util.TypeCast;
import Util.TimeStamp;
import Util.CRC32;
import Util.Cryptos;
import Util.TaskTimer;
import Util.Logger;

public class Global {
	private static Example C_Example;
	private static Runnable_Thread C_Runnable_Thread;
	private static ISA C_ISA;
	
	private static Consensus C_Consensus;
	private static P2P C_P2P;
	private static Contract C_Contract;
	
	private final static String KeyAlgorithm = "secp256r1";
	private static JsonObject node_json;
	private static JsonObject rrNet_json;
	private static JsonObject rrSubNet_json;

	private static KeyUtil C_KeyUtil;
	
	private static DBHelper C_DBHelper;
	private static FileIO C_FileIO;
	private static TypeCast C_TypeCast;
	private static TimeStamp C_TimeStamp;
	private static CRC32 C_CRC32;
	private static TaskTimer C_CTaskTimer;
	private static Logger C_Logger;
	private static Cryptos C_Cryptos;
	
	private Semaphore sock_ctx_semaphore;
	private Semaphore fileIo_semaphore;
	
	public Global() {
		// create every class instance
		// for using global reference
		C_Runnable_Thread = new Runnable_Thread(this);
		
		C_Example = new Example(this);
		C_Consensus = new Consensus(this);
		C_P2P = new P2P(this);
		C_Contract = new Contract();
		C_ISA = new ISA(this);
		
		C_FileIO = new FileIO(this);
		C_DBHelper = new DBHelper(this);
		C_KeyUtil = new KeyUtil(this);
		C_TypeCast = new TypeCast(this);
		C_TimeStamp = new TimeStamp(this);
		C_CRC32 = new CRC32(this);
		C_CTaskTimer = new TaskTimer(this);
		C_Logger = new Logger();
		C_Cryptos = new Cryptos();
		
		// Semaphore for Socket CTX Information Map Data
		this.sock_ctx_semaphore = new Semaphore(1);
		// Semaphore for File IO
		this.fileIo_semaphore = new Semaphore(1);
	}
	
	synchronized public void ParseNodeJson() {
		// parsing node.json file and store json object in global instance
		String node_json_path = "./conf/node.json";
		JsonParser parser = new JsonParser();
		
		try {
			this.fileIo_semaphore.acquire();
			Object json = parser.parse(C_FileIO.FileReadString(node_json_path));
			JsonObject jsonObject = (JsonObject) json;
			setNodeJson(jsonObject.getAsJsonObject("NODE"));
			json = null;
			jsonObject = null;
			this.fileIo_semaphore.release();
			C_Logger.OutConsole(C_Logger.LOG_DEBUG, "Successful Parsing node.json file");
		} catch (InterruptedException e) {
			C_Logger.OutConsole(C_Logger.LOG_ERROR, "Fail to Parsing node.json file cause " + e.getCause());
		}
		node_json_path = null;
		parser = null;
	}

	synchronized public void ParseRRNetJson() {
		// parsing rr_net.json file and store json object in global instance
		String rrNet_json_path = "./conf/rr_net.json";
		JsonParser parser = new JsonParser();

		try {
			this.fileIo_semaphore.acquire();
			Object json = parser.parse(C_FileIO.FileReadString(rrNet_json_path));
			JsonObject jsonObject = (JsonObject) json;
			setRRNetJson(jsonObject.getAsJsonObject("NET"));
			json = null;
			jsonObject = null;
			this.fileIo_semaphore.release();
			C_Logger.OutConsole(C_Logger.LOG_DEBUG, "Successful Parsing rr_Net.json file");
		} catch (InterruptedException e) {
			C_Logger.OutConsole(C_Logger.LOG_ERROR, "Fail to Parsing rr_Net.json file cause " + e.getCause());
		}
		rrNet_json_path = null;
		parser = null;
	}

	synchronized public void ParseRRSubNetJson() {
		// parsing rr_subnet.json file and store json object in global instance
		String rrSubNet_json_path = "./conf/rr_subnet.json";
		JsonParser parser = new JsonParser();

		try {
			this.fileIo_semaphore.acquire();
			Object json = parser.parse(C_FileIO.FileReadString(rrSubNet_json_path));
			JsonObject jsonObject = (JsonObject) json;
			setRRSubNetJson(jsonObject.getAsJsonObject("SUBNET"));
			json = null;
			jsonObject = null;
			this.fileIo_semaphore.release();
			C_Logger.OutConsole(C_Logger.LOG_DEBUG, "Successful Parsing rr_subNet.json file");
		} catch (InterruptedException e) {
			C_Logger.OutConsole(C_Logger.LOG_ERROR, "Fail to Parsing rr_subNet.json file cause " + e.getCause());
		}
		rrSubNet_json_path = null;
		parser = null;
	}
	
	synchronized public void setNetworkNodePort() {
		// set ISA and SCA port from env
		C_ISA.setISAport(Integer.parseInt(System.getenv("ISAPort")));
		C_Contract.setSCAport(Integer.parseInt(System.getenv("SCAPort")));
	}
	
	public Runnable_Thread getCRunnableThread() {
		return C_Runnable_Thread;
	}
	
	public Example getCExample() {
		return C_Example;
	}
	
	public Consensus getCConsensus() {
		return C_Consensus;
	}
	
	public P2P getCP2P() {
		return C_P2P;
	}
	
	public Contract getCContract() {
		return C_Contract;
	}
	
	public ISA getCISA() {
		return C_ISA;
	}
	
	public DBHelper getCDBHelper() {
		return C_DBHelper;
	}
	public FileIO getCFileIO() {
		return C_FileIO;
	}
	
	public KeyUtil getCKeyUtil() {
		return C_KeyUtil;
	}
	
	public TypeCast getCTypeCast() {
		return C_TypeCast;
	}

	public TimeStamp getCTimeStamp() {
		return C_TimeStamp;
	}
	
	public CRC32 getCCRC32() {
		return C_CRC32;
	}
	
	public Cryptos getCCryptos() {
		return C_Cryptos;
	}
	
	public TaskTimer getCTaskTimer() {
		return C_CTaskTimer;
	}
	
	public Logger getCLogger() {
		return C_Logger;
	}
	
	public Semaphore getSockCtxSemaphore() {
		return this.sock_ctx_semaphore;
	}
	
	public Semaphore getFileIOSemaphore() {
		return this.fileIo_semaphore;
	}
	
	public JsonObject getNodeJson() {
		return node_json;
	}
	
	public void setNodeJson(JsonObject Node_json) {
		this.node_json = Node_json;
	}

	public JsonObject getRRNetJson() {
		return rrNet_json;
	}
	
	public void setRRNetJson(JsonObject RRNet_json) {
		this.rrNet_json = RRNet_json;
	}

	public JsonObject getRRSubNetJson() {
		return rrSubNet_json;
	}
	
	public void setRRSubNetJson(JsonObject RRSubNet_json) {
		this.rrSubNet_json = RRSubNet_json;
	}
	
	public String getKeyAlgorithm() {
		return KeyAlgorithm;
	}
	
}
