package Consensus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import Main.Global;

import Socket.SockInfo;
import Socket.SocketThread;
import Data.DBKeyLists;
import Data.LightBlkInfo;

///////////////////////////////////////////////////////////////////////////////////////////////////
// Consensus class instance control all about consensus data (e.g. Tx, Block, Node etc..)        //
// consensus class has 2 role of node (NN or CN)                                                 //
// if role == "NN" then this consensus known other NN and own cluster subNet CNs                 //
// so, who is current Block Generation Node and next Block Generation Node                       //
// also, control about current Block Number, BlockGenTime etc....                                //
// else if role == "CN" then this consensus only known about TX, Block                           // 
// the CN node receive from NNA Through CMD what to do.                                          //
// CN's most important role is Block Generator (we called Master Node)                           //
// between NN and CN communicate using Cosnensus_TX, Consensus_RX methods                        //
///////////////////////////////////////////////////////////////////////////////////////////////////


enum Cons_Type{
	CMD, MGMT, DATA
}

enum Cons_Cmd{
	BGI, TX, TX_ACK, TX_STOP_REQ, TX_STOP_RSP, BLK_NOTI_FROM_CN, BLK_NOTI_FROM_NN
}

@SuppressWarnings("unchecked")
enum  Cons_Define{
	DBKey(8), Hash(32), PriKeyLen(32), PubKeyLen(64), CompPubKeyLen(33), Sig(64), SigR(32), SigS(32), StartTime(8), DelayTime(4), BN(8), P2PAddr(8), TierID(1);

	private int value;
	@SuppressWarnings("rawtypes")
	private static Map map = new HashMap<>();
	
	private Cons_Define(int value) {
        this.value = value;
    }

    static {
        for (Cons_Define consDef : Cons_Define.values()) {
            map.put(consDef.value, consDef);
        }
    }

    public static Cons_Define valueOf(int consDef) {
        return (Cons_Define) map.get(consDef);
    }

    public int getValue() {
        return value;
    }
    
    public static int getTotalValue() {
    	int tot;
    	
    	tot = Cons_Define.DBKey.getValue();
    	
    	return tot;
    }
}


@SuppressWarnings("unchecked")
enum  Cons_Header_field{
	Version(1), Type(1), Cmd(1), Len(2);
	
	private int value;
	@SuppressWarnings("rawtypes")
	private static Map map = new HashMap<>();
	
	private Cons_Header_field(int value) {
        this.value = value;
    }

    static {
        for (Cons_Header_field consHdr : Cons_Header_field.values()) {
            map.put(consHdr.value, consHdr);
        }
    }

    public static Cons_Header_field valueOf(int consHdr) {
        return (Cons_Header_field) map.get(consHdr);
    }

    public int getValue() {
        return value;
    }
    
    public static int getTotalValue() {
    	int tot;
    	
    	tot = Cons_Header_field.Version.getValue()
    			+ Cons_Header_field.Type.getValue()
    			+ Cons_Header_field.Cmd.getValue();
    	
    	return tot;
    }
}


@SuppressWarnings("unchecked")
enum  Cons_Cmd_Ligth_Blk_Info_field{
	BN(8), TierID(1), P2PAddr(8), BlkGenTime(8), PrevBlkHash(32), TxCnt(4), CurrBlkHash(32), Sig(64);
	
	private int value;
	@SuppressWarnings("rawtypes")
	private static Map map = new HashMap<>();
	
	private Cons_Cmd_Ligth_Blk_Info_field(int value) {
        this.value = value;
    }

    static {
        for (Cons_Cmd_Ligth_Blk_Info_field blkInfo : Cons_Cmd_Ligth_Blk_Info_field.values()) {
            map.put(blkInfo.value, blkInfo);
        }
    }

    public static Cons_Cmd_Ligth_Blk_Info_field valueOf(int blkInfo) {
        return (Cons_Cmd_Ligth_Blk_Info_field) map.get(blkInfo);
    }

    public int getValue() {
        return value;
    }
    
    public static int getTotalValue() {
    	int tot;
    	
    	tot = Cons_Cmd_Ligth_Blk_Info_field.BN.getValue() 
    			+ Cons_Cmd_Ligth_Blk_Info_field.TierID.getValue()
    			+ Cons_Cmd_Ligth_Blk_Info_field.P2PAddr.getValue()
    			+ Cons_Cmd_Ligth_Blk_Info_field.BlkGenTime.getValue()
    			+ Cons_Cmd_Ligth_Blk_Info_field.PrevBlkHash.getValue()
    			+ Cons_Cmd_Ligth_Blk_Info_field.TxCnt.getValue()
    			+ Cons_Cmd_Ligth_Blk_Info_field.CurrBlkHash.getValue()
    			+ Cons_Cmd_Ligth_Blk_Info_field.Sig.getValue();
    	
    	return tot;
    }
}

@SuppressWarnings("unchecked")
enum Cons_Tx_Info_List{
	DBKey(8), Hash(32);
	
	private int value;
	@SuppressWarnings("rawtypes")
	private static Map map = new HashMap<>();
	
	private Cons_Tx_Info_List(int value) {
        this.value = value;
    }

    static {
        for (Cons_Tx_Info_List txInfoList : Cons_Tx_Info_List.values()) {
            map.put(txInfoList.value, txInfoList);
        }
    }

    public static Cons_Tx_Info_List valueOf(int txInfoList) {
        return (Cons_Tx_Info_List) map.get(txInfoList);
    }

    public int getValue() {
        return value;
    }
    
    public static int getTotalValue() {
    	int tot;
    	
    	tot = Cons_Tx_Info_List.DBKey.getValue() 
    			+ Cons_Tx_Info_List.Hash.getValue();
    	
    	return tot;
    }
}

@SuppressWarnings("unchecked")
enum Cons_DB_Key_List{
	FirstTxDBKey(8), LastTxDBKey(8), DBKeyCnt(4), DBKeys(0);
	
	private int value;
	@SuppressWarnings("rawtypes")
	private static Map map = new HashMap<>();
	
	private Cons_DB_Key_List(int value) {
        this.value = value;
    }

    static {
        for (Cons_DB_Key_List dbKeyList : Cons_DB_Key_List.values()) {
            map.put(dbKeyList.value, dbKeyList);
        }
    }

    public static Cons_DB_Key_List valueOf(int dbKeyList) {
        return (Cons_DB_Key_List) map.get(dbKeyList);
    }

    public int getValue() {
        return value;
    }
    
    public static int getTotalValue() {
    	int tot;
    	
    	tot = Cons_DB_Key_List.FirstTxDBKey.getValue() 
    			+ Cons_DB_Key_List.LastTxDBKey.getValue()
    			+ Cons_DB_Key_List.DBKeyCnt.getValue() 
    			+ Cons_DB_Key_List.DBKeys.getValue();
    	
    	return tot;
    }
}

@SuppressWarnings("unchecked")
enum  Cons_Cmd_BGI_field{
	StartTime(8), RoundCnt(4), DelayTime(4), FirstCNP2PAddr(8), FirstBN(8), TierID(1);
	
	private int value;
	@SuppressWarnings("rawtypes")
	private static Map map = new HashMap<>();
	
	private Cons_Cmd_BGI_field(int value) {
        this.value = value;
    }

    static {
        for (Cons_Cmd_BGI_field bgiLen : Cons_Cmd_BGI_field.values()) {
            map.put(bgiLen.value, bgiLen);
        }
    }

    public static Cons_Cmd_BGI_field valueOf(int bgiLen) {
        return (Cons_Cmd_BGI_field) map.get(bgiLen);
    }

    public int getValue() {
        return value;
    }
    
    public static int getTotalValue() {
    	int tot;
    	
    	tot = Cons_Cmd_BGI_field.StartTime.getValue() 
    			+ Cons_Cmd_BGI_field.RoundCnt.getValue()
    			+ Cons_Cmd_BGI_field.DelayTime.getValue()
    			+ Cons_Cmd_BGI_field.FirstCNP2PAddr.getValue()
    			+ Cons_Cmd_BGI_field.FirstBN.getValue()
    			+ Cons_Cmd_BGI_field.TierID.getValue();
    	
    	return tot;
    }
}

@SuppressWarnings("unchecked")
enum  Cons_Cmd_TX_field{
	BN(8), TierID(1), TxInfoCnt(1), TxInfoList(0);
	
	private int value;
	@SuppressWarnings("rawtypes")
	private static Map map = new HashMap<>();
	
	private Cons_Cmd_TX_field(int value) {
        this.value = value;
    }

    static {
        for (Cons_Cmd_TX_field tx : Cons_Cmd_TX_field.values()) {
            map.put(tx.value, tx);
        }
    }

    public static Cons_Cmd_TX_field valueOf(int tx) {
        return (Cons_Cmd_TX_field) map.get(tx);
    }

    public int getValue() {
        return value;
    }
    
    public static int getTotalValue() {
    	int tot;
    	
    	tot = Cons_Cmd_TX_field.BN.getValue() 
    			+ Cons_Cmd_TX_field.TierID.getValue()
    			+ Cons_Cmd_TX_field.TxInfoCnt.getValue()
    			+ Cons_Cmd_TX_field.TxInfoList.getValue();
    	
    	return tot;
    }
}

@SuppressWarnings("unchecked")
enum  Cons_Cmd_TX_STOP_field{
	P2PAddr(8), BN(8), TierID(1);
	
	private int value;
	@SuppressWarnings("rawtypes")
	private static Map map = new HashMap<>();
	
	private Cons_Cmd_TX_STOP_field(int value) {
        this.value = value;
    }

    static {
        for (Cons_Cmd_TX_STOP_field txStop : Cons_Cmd_TX_STOP_field.values()) {
            map.put(txStop.value, txStop);
        }
    }

    public static Cons_Cmd_TX_STOP_field valueOf(int txStop) {
        return (Cons_Cmd_TX_STOP_field) map.get(txStop);
    }

    public int getValue() {
        return value;
    }
    
    public static int getTotalValue() {
    	int tot;
    	
    	tot = Cons_Cmd_TX_STOP_field.P2PAddr.getValue() 
    			+ Cons_Cmd_TX_STOP_field.BN.getValue()
    			+ Cons_Cmd_TX_STOP_field.TierID.getValue();
    	
    	return tot;
    }
}

@SuppressWarnings("unchecked")
enum  Cons_Cmd_BLK_NOTI_field{
	NextDelayTime(4), NextSeqIdx(4), NextBlkGenCNP2PAddr(8), NextBN(8), LightBlkInfo(0), DBKeyList(0);
	
	private int value;
	@SuppressWarnings("rawtypes")
	private static Map map = new HashMap<>();
	
	private Cons_Cmd_BLK_NOTI_field(int value) {
        this.value = value;
    }

    static {
        for (Cons_Cmd_BLK_NOTI_field blkNoti : Cons_Cmd_BLK_NOTI_field.values()) {
            map.put(blkNoti.value, blkNoti);
        }
    }

    public static Cons_Cmd_BLK_NOTI_field valueOf(int blkNoti) {
        return (Cons_Cmd_BLK_NOTI_field) map.get(blkNoti);
    }

    public int getValue() {
        return value;
    }
    
    public static int getTotalValue() {
    	int tot;
    	
    	tot = Cons_Cmd_BLK_NOTI_field.NextDelayTime.getValue() 
    			+ Cons_Cmd_BLK_NOTI_field.NextSeqIdx.getValue()
    			+ Cons_Cmd_BLK_NOTI_field.NextBlkGenCNP2PAddr.getValue()
    			+ Cons_Cmd_BLK_NOTI_field.NextBN.getValue() 
    			+ Cons_Cmd_BLK_NOTI_field.LightBlkInfo.getValue()
    			+ Cons_Cmd_BLK_NOTI_field.DBKeyList.getValue();
    	
    	return tot;
    }
}

public class Consensus {
	private ArrayList<SockInfo> sock_param_infos;
	private SocketThread sock_threads;
	private Global global;

	private byte[] prvBN;
	private byte[] BN;

	private byte[] tierID;

	// rr_net
	private boolean isMySubNetCN;
	private byte[] startTime;
	private byte[] genTime;
	private byte[] startBN;
	
	private int totNN;
	private int myRootIdx;
	private int myNextIdx;
	private byte[][] nnP2PAddr;
	private byte[] nnProto;
	private String[] nnIP;
	private int[] nnPort;
	
	// rr_subnet
	int cnNum;
	int currIdx;
	int nextIdx;
	private byte[][] cnP2PAddr;

	// from rr_subnet
	private byte[] currBlkGenCN;
	private byte[] nextBlkGenCN;
	private byte[] currTxCN;
	
	private static byte MyVersion;
	
	public Consensus(Global global) {
		this.global = global;
		this.sock_param_infos = new ArrayList<SockInfo>();
		this.sock_threads = new SocketThread();
	}

	public void init() {
		this.prvBN = new byte[Cons_Define.BN.getValue()];
		this.BN = new byte[Cons_Define.BN.getValue()];
		this.currBlkGenCN = new byte[Cons_Define.P2PAddr.getValue()];
		this.nextBlkGenCN = new byte[Cons_Define.P2PAddr.getValue()];
		this.currTxCN = new byte[Cons_Define.P2PAddr.getValue()];
		this.tierID = new byte[Cons_Define.TierID.getValue()];
		this.tierID[0] = 1;

		this.isMySubNetCN = false;
		this.startTime = new byte[Cons_Define.StartTime.getValue()];
		this.genTime = new byte[Cons_Define.DelayTime.getValue()]; // = block delay time (milliseconds unit)
		this.startBN = new byte[Cons_Define.BN.getValue()];
		
		this.totNN = 0;
		this.myRootIdx = 0;
		this.myNextIdx = 0;

		this.cnNum = 0;
		this.currIdx = 0;
		this.nextIdx = 0;
		
		this.MyVersion = 0x00;
	}

	// Parsing RR_Net Info 
	// and Setting each value e.g) BlockNumber, BlockGenTime etc...
	public void rrNetInit() {
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Do rrNetInit");
		byte nodeRule[] = this.global.getCP2P().getMyNodeRule();
		
		// if myNodeRule is not NN (=myNodeRule is CN) then do not setting RR_Net
		if (nodeRule[0] != this.global.getCP2P().getENodeType("NN"))
		{
			return;
		}
		
		JsonArray rrNet_json_array = this.global.getRRNetJson().getAsJsonArray("TIER");
		JsonObject rrNet_json = rrNet_json_array.get(0).getAsJsonObject();

		// Get Start Time
		byte[] startTimeTmp = this.global.getCTypeCast().StringToByteArray(rrNet_json.get("START_TIME").getAsString(), 10);
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "startTimeTmp Len : " + startTimeTmp.length);
		
		System.arraycopy(startTimeTmp, 0, this.startTime, this.startTime.length-startTimeTmp.length, startTimeTmp.length);
		long startTimeL = this.global.getCTypeCast().ByteArrayToLong(this.startTime);
		
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "START_TIME : " + startTimeL);
		
		// Get Generation Time
		byte[] genTimeTmp =  this.global.getCTypeCast().IntToByteArray(rrNet_json.get("GEN_TIME").getAsInt());
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "genTimeTmp Len : " + genTimeTmp.length);
		
		System.arraycopy(genTimeTmp, 0, this.genTime, this.genTime.length-genTimeTmp.length, genTimeTmp.length);
		int genTimeI = this.global.getCTypeCast().ByteArrayToInt(this.genTime);

		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "GEN_TIME : " + genTimeI);

		// Get Start BN
		byte[] startBNTmp = this.global.getCTypeCast().StringToByteArray(rrNet_json.get("START_BN").getAsString(), 10);
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "startBN Len : " + startBNTmp.length);

		System.arraycopy(startBNTmp, 0, this.startBN, this.startBN.length-startBNTmp.length, startBNTmp.length);
		long startBNL = this.global.getCTypeCast().ByteArrayToLong(this.startBN);

		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "START_TIME : " + startBNL);

		// totNN == Number of Total Consensus group
		this.totNN = rrNet_json.get("TOTAL_NN").getAsInt();
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "TOTAL_NN : " + this.totNN);
		
		this.nnP2PAddr = new byte[this.totNN][this.global.getCP2P().getP2PDefLen("P2PAddrLen")];
		this.nnProto = new byte[this.totNN];
		this.nnIP = new String[this.totNN];
		this.nnPort = new int[this.totNN];
		
		JsonArray array = rrNet_json.getAsJsonArray("NN_LIST");
		
		for(int idx=0; idx<this.totNN; idx++)
		{
			JsonObject  jobject = array.get(idx).getAsJsonObject();
			this.nnP2PAddr[idx] = this.global.getCTypeCast().HexStrToByteArray(jobject.get("P2P").getAsString().substring(2, 18));

			this.global.getCTypeCast().ByteArrayToPrint(this.nnP2PAddr[idx], 16);
			
			this.nnProto[idx] = jobject.getAsJsonObject("SOCK").get("PROTO").getAsByte();
			this.nnIP[idx] = jobject.getAsJsonObject("SOCK").get("IP").getAsString();
			this.nnPort[idx] = jobject.getAsJsonObject("SOCK").get("PORT").getAsInt();
			
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, 
					"Proto : " + this.nnProto[idx] + " IP : " + this.nnIP[idx] + " Port : " + this.nnPort[idx]);
			
			this.global.getCTypeCast().ByteArrayToPrint(this.global.getCP2P().getMyClusterRoot(), 16);
			
			if (this.global.getCTypeCast().ByteArrayCompare(this.global.getCP2P().getMyClusterRoot(), this.nnP2PAddr[idx]))
			{
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "this NN is ME. Index = " + idx);
				
				if (idx == 0)
				{
					this.isMySubNetCN = true;
					
				}

				long prvBNL = 0;
				// When only nn = 1, for test
				if (this.totNN == 1)
				{
					byte[][] Res_prvBlk = this.global.getCDBHelper().GetPrevBlockContentsFromDB();
					prvBNL = this.global.getCTypeCast().ByteArrayToLong(Res_prvBlk[0]);
					Res_prvBlk = null;
				}

				long BNL = prvBNL + idx + startBNL;
				this.BN = this.global.getCTypeCast().LongToByteArray(BNL);
				
				this.myRootIdx = idx;
				this.myNextIdx = this.global.getCTypeCast().next_idx(idx, this.totNN);
			}
			else
			{
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "this NN is NOT ME. Index = " + idx);
			}
			jobject = null;
		}
		rrNet_json_array = null;
		rrNet_json = null;
		array = null;
		
		nodeRule = null;
		startTimeTmp = null;
		genTimeTmp = null;
		startBNTmp = null;
		
	}

	// Parsing RR_SubNet Info 
	// and Setting each value e.g) CN1's P2PAddress, CN2's P2PAddress ...
	public void rrSubNetInit() {
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Do rrSubNetInit");
		byte nodeRule[] = this.global.getCP2P().getMyNodeRule() ;
		
		if (nodeRule[0] != this.global.getCP2P().getENodeType("NN"))
		{
			return;
		}
		
		JsonArray tier_json_array = this.global.getRRSubNetJson().getAsJsonArray("TIER");
		JsonObject tier_json = tier_json_array.get(0).getAsJsonObject();
		
		this.cnNum = tier_json.get("CN_NUM").getAsInt();
		
		this.cnP2PAddr = new byte[this.cnNum][this.global.getCP2P().getP2PDefLen("P2PAddrLen")];
		
		JsonArray array = tier_json.getAsJsonArray("CN_ADDR");
		for (int idx=0; idx<this.cnNum; idx++)
		{
			this.cnP2PAddr[idx] = this.global.getCTypeCast().HexStrToByteArray(array.get(idx).getAsString().substring(2, 18));
			this.global.getCTypeCast().ByteArrayToPrint(this.cnP2PAddr[idx], 16);
		}

		if(this.cnNum > 0)
		{
			this.currIdx = 0;
			this.nextIdx = this.global.getCTypeCast().next_idx(this.currIdx, this.cnNum);
			
			this.currBlkGenCN = this.cnP2PAddr[this.currIdx];
			this.currTxCN = this.cnP2PAddr[this.currIdx];
			this.nextBlkGenCN = this.cnP2PAddr[this.nextIdx];
		}
		
		// Just for Debug
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "currIdx " + this.currIdx + "nextIdx " + this.currIdx);
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "currIdx " + "currBlkGenCN " + this.global.getCTypeCast().ByteHexToString(this.currBlkGenCN));
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "currIdx " + "nextBlkGenCN " + this.global.getCTypeCast().ByteHexToString(this.nextBlkGenCN));
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "currIdx " + "currTxCN " + this.global.getCTypeCast().ByteHexToString(this.currTxCN));
		
		tier_json_array = null;
		tier_json = null;
		array = null;
		
		nodeRule = null;
	}

	// Sock setting using parsed node.json
	public void sock_init(JsonObject node_json) {
		JsonObject sock_json = node_json.getAsJsonObject("SOCK");
		JsonObject sock_num = sock_json.getAsJsonObject("NUM");

		int udp_svr_num = sock_num.get("UDP_SVR").getAsInt();
		int udp_cli_num = sock_num.get("UDP_CLI").getAsInt();
		int tcp_svr_num = sock_num.get("TCP_SVR").getAsInt();
		int tcp_cli_num = sock_num.get("TCP_CLI").getAsInt();

		SockInfo si;

		for (int i = 1; i <= udp_svr_num; i++) {
			String udp_svr_str = "UDP_SVR_" + Integer.toString(i);
			String mreq_ip = sock_json.getAsJsonObject(udp_svr_str).get("MREQ_IP").getAsString();
			String ip = sock_json.getAsJsonObject(udp_svr_str).get("IP").getAsString();
			int port = sock_json.getAsJsonObject(udp_svr_str).get("PORT").getAsInt();

			si = new SockInfo(mreq_ip, ip, port, udp_svr_str);
			this.sock_param_infos.add(si);
			
			udp_svr_str = null;
			mreq_ip = null;
			ip = null;

		}

		for (int i = 1; i <= udp_cli_num; i++) {
			String udp_cli_str = "UDP_CLI_" + Integer.toString(i);
			String peer_ip = sock_json.getAsJsonObject(udp_cli_str).getAsJsonObject("PEER").get("IP").getAsString();
			int peer_port = sock_json.getAsJsonObject(udp_cli_str).getAsJsonObject("PEER").get("PORT").getAsInt();
			String local_ip = sock_json.getAsJsonObject(udp_cli_str).getAsJsonObject("LOCAL").get("IP").getAsString();
			int local_port = sock_json.getAsJsonObject(udp_cli_str).getAsJsonObject("LOCAL").get("PORT").getAsInt();

			si = new SockInfo(peer_ip, peer_port, local_ip, local_port, udp_cli_str);
			this.sock_param_infos.add(si);
			
			udp_cli_str = null;
			peer_ip = null;
			local_ip = null;
		}

		for (int i = 1; i <= tcp_svr_num; i++) {
			String tcp_svr_str = "TCP_SVR_" + Integer.toString(i);
			String ip = sock_json.getAsJsonObject(tcp_svr_str).get("IP").getAsString();
			int port = sock_json.getAsJsonObject(tcp_svr_str).get("PORT").getAsInt();
			
			si = new SockInfo(ip, port, tcp_svr_str);
			this.sock_param_infos.add(si);
			
			tcp_svr_str = null;
			ip = null;
			
		}

		for (int i = 1; i <= tcp_cli_num; i++) {
			String tcp_cli_str = "TCP_CLI_" + Integer.toString(i);
			int auto_join = sock_json.getAsJsonObject(tcp_cli_str).get("AUTO_JOIN").getAsInt();
			int p2p_join = sock_json.getAsJsonObject(tcp_cli_str).get("P2P_JOIN").getAsInt();
			String peer_ip = sock_json.getAsJsonObject(tcp_cli_str).getAsJsonObject("PEER").get("IP").getAsString();
			int peer_port = sock_json.getAsJsonObject(tcp_cli_str).getAsJsonObject("PEER").get("PORT").getAsInt();
			String local_ip = sock_json.getAsJsonObject(tcp_cli_str).getAsJsonObject("LOCAL").get("IP").getAsString();
			int local_port = sock_json.getAsJsonObject(tcp_cli_str).getAsJsonObject("LOCAL").get("PORT").getAsInt();

			si = new SockInfo(auto_join, p2p_join, peer_ip, peer_port, local_ip, local_port, tcp_cli_str);
			this.sock_param_infos.add(si);
			
			tcp_cli_str = null;
			peer_ip = null;
			local_ip = null;
		}
		si = null;
		sock_json = null;
		sock_num = null;
		this.sock_threads.Sock_Thread(this.global, this.sock_param_infos);
	}

	public SocketThread getSockThread() {
		return this.sock_threads;
	}

	public int getConsDefLen(String field){
		int len = 0;
		if(field.equals("DBKey"))
		{
			len = Cons_Define.DBKey.getValue();
		}
		else if(field.equals("Hash"))
		{
			len = Cons_Define.Hash.getValue();
		}
		else if(field.equals("DBKeyHash"))
		{
			len = (Cons_Define.DBKey.getValue() + Cons_Define.Hash.getValue());
		}
		else if(field.equals("Sig"))
		{
			len = Cons_Define.Sig.getValue();
		}
		else if(field.equals("SigR"))
		{
			len = Cons_Define.SigR.getValue();
		}
		else if(field.equals("SigS"))
		{
			len = Cons_Define.SigS.getValue();
		}
		else if(field.equals("BN"))
		{
			len = Cons_Define.BN.getValue();
		}
		else
		{
			// Error
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "getConsDefLen No Field : " + field);
		}

		return len;
	}

	public int getLightBlkFieldLen(String field){
		int len = 0;
		if(field.equals("BN"))
		{
			len = Cons_Cmd_Ligth_Blk_Info_field.BN.getValue();
		}
		else if(field.equals("TierID"))
		{
			len = Cons_Cmd_Ligth_Blk_Info_field.TierID.getValue();
		}
		else if(field.equals("P2PAddr"))
		{
			len = Cons_Cmd_Ligth_Blk_Info_field.P2PAddr.getValue();
		}
		else if(field.equals("BlkGenTime"))
		{
			len = Cons_Cmd_Ligth_Blk_Info_field.BlkGenTime.getValue();
		}
		else if(field.equals("PrevBlkHash"))
		{
			len = Cons_Cmd_Ligth_Blk_Info_field.PrevBlkHash.getValue();
		}
		else if(field.equals("TxCnt"))
		{
			len = Cons_Cmd_Ligth_Blk_Info_field.TxCnt.getValue();
		}
		else if(field.equals("CurrBlkHash"))
		{
			len = Cons_Cmd_Ligth_Blk_Info_field.CurrBlkHash.getValue();
		}
		else if(field.equals("Sig"))
		{
			len = Cons_Cmd_Ligth_Blk_Info_field.Sig.getValue();
		}
		else if(field.equals("ToT"))
		{
			len = Cons_Cmd_Ligth_Blk_Info_field.getTotalValue();
		}
		else
		{
			// Error
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "getLightBlkFieldLen No Field : " + field);
		}

		return len;
	}

	public int getDBKeyListFieldLen(String field){
		int len = 0;
		if(field.equals("FirstTxDBKey"))
		{
			len = Cons_DB_Key_List.FirstTxDBKey.getValue();
		}
		else if(field.equals("LastTxDBKey"))
		{
			len = Cons_DB_Key_List.LastTxDBKey.getValue();
		}
		else if(field.equals("DBKeyCnt"))
		{
			len = Cons_DB_Key_List.DBKeyCnt.getValue();
		}
		else if(field.equals("ToT"))
		{
			len = Cons_DB_Key_List.getTotalValue();
		}
		else
		{
			// Error
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "getDBKeyListFieldLen No Field : " + field);
		}

		return len;
	}

	public int getBlkNotiFieldLen(String field){
		int len = 0;
		if(field.equals("NextDelayTime"))
		{
			len = Cons_Cmd_BLK_NOTI_field.NextDelayTime.getValue();
		}
		else if(field.equals("NextSeqIdx"))
		{
			len = Cons_Cmd_BLK_NOTI_field.NextSeqIdx.getValue();
		}
		else if(field.equals("NextBlkGenCNP2PAddr"))
		{
			len = Cons_Cmd_BLK_NOTI_field.NextBlkGenCNP2PAddr.getValue();
		}
		else if(field.equals("NextBN"))
		{
			len = Cons_Cmd_BLK_NOTI_field.NextBN.getValue();
		}
		else if(field.equals("ToT"))
		{
			len = Cons_Cmd_BLK_NOTI_field.getTotalValue();
		}
		else
		{
			// Error
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "getBlkNotiFieldLen No Field : " + field);
		}

		return len;
	}

	// BlkGenInfo Print for Debug Routine
	public void BlkGenInfoPrint(byte[] StartTime, byte[] RoundCnt, byte[] DelayTime,
									byte[] FirstCNP2PAddr, byte[] FirstBN, byte[] TierID)
	{
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "StartTime : " + this.global.getCTypeCast().ByteHexToString(StartTime));
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "RoundCnt : " + this.global.getCTypeCast().ByteHexToString(RoundCnt));
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "DelayTime : " + this.global.getCTypeCast().ByteHexToString(DelayTime));
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "FirstCNP2PAddr : " + this.global.getCTypeCast().ByteHexToString(FirstCNP2PAddr));
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "FirstBN : " + this.global.getCTypeCast().ByteHexToString(FirstBN));
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "TierID : " + this.global.getCTypeCast().ByteHexToString(TierID));
	}

	// Tx Print for Debug Routine
	public void TxPrint(byte[] BN, byte[] TxInfoCnt, byte[] TxInfoList) {
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG,
				"TxInfoList : " + this.global.getCTypeCast().ByteHexToString(TxInfoList));
	}
	
	// TxAck Print for Debug Routine
	public void TxAckPrint(byte[] BN, byte[] TxInfoCnt, byte[] TxInfoList) {
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG,
				"TxInfoList : " + this.global.getCTypeCast().ByteHexToString(TxInfoList));
	}

	// Tx Stop Request Print for Debug Routine
	public void TxStopReqPrint(byte[] P2PAddr, byte[] BN, byte[] TierID) {
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "P2PAddr : " + this.global.getCTypeCast().ByteHexToString(P2PAddr));
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "BN : " + this.global.getCTypeCast().ByteHexToString(BN));
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "TierID : " + this.global.getCTypeCast().ByteHexToString(TierID));
	}

	// Tx Stop Response Print for Debug Routine
	public void TxStopRspPrint(byte[] P2PAddr, byte[] BN, byte[] TierID) {
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "P2PAddr : " + this.global.getCTypeCast().ByteHexToString(P2PAddr));
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "BN : " + this.global.getCTypeCast().ByteHexToString(BN));
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "TierID : " + this.global.getCTypeCast().ByteHexToString(TierID));
	}

	// BlkNotiFromCN Print for Debug Routine
	public void BlkNotiFromCNPrint(byte[] NextDelayTime, byte[] NextSeqIdx, byte[] NextBlkGenCNP2PAddr,
										byte[] NextBN, LightBlkInfo m_LightBlkInfo, DBKeyLists m_DBKeyLists)
	{
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "NextDelayTime : " + this.global.getCTypeCast().ByteHexToString(NextDelayTime));
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "NextSeqIdx : " + this.global.getCTypeCast().ByteHexToString(NextSeqIdx));
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "NextBlkGenCNP2PAddr : " + this.global.getCTypeCast().ByteHexToString(NextBlkGenCNP2PAddr));
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "NextBN : " + this.global.getCTypeCast().ByteHexToString(NextBN));
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "LightBlkInfo : \n" + m_LightBlkInfo.toString());
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "DBKeyLists : \n" + m_DBKeyLists.toString());
	}

	// BlkNotiFromNN Print for Debug Routine
	public void BlkNotiFromNNPrint(byte[] NextDelayTime, byte[] NextSeqIdx, byte[] NextBlkGenCNP2PAddr,
										byte[] NextBN, LightBlkInfo m_LightBlkInfo, DBKeyLists m_DBKeyLists)
	{
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "NextDelayTime : " + this.global.getCTypeCast().ByteHexToString(NextDelayTime));
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "NextSeqIdx : " + this.global.getCTypeCast().ByteHexToString(NextSeqIdx));
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "NextBlkGenCNP2PAddr : " + this.global.getCTypeCast().ByteHexToString(NextBlkGenCNP2PAddr));
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "NextBN : " + this.global.getCTypeCast().ByteHexToString(NextBN));
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "LightBlkInfo : \n" + m_LightBlkInfo.toString());
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "DBKeyLists : \n" + m_DBKeyLists.toString());
		
	}

	public byte getMyVersion() {
		return this.MyVersion;
	}

	public byte[] getTierID() {
		return this.tierID;
	}

	public byte[] getStartTime() {
		return this.startTime;
	}

	public byte[] getGenTime() {
		return this.genTime;
	}

	public void setGenTime(byte[] GenTime) {
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, 
				"Blk GenTime Setting : " + this.global.getCTypeCast().ByteArrayToInt(GenTime) + "MILLISECONDS");
		this.genTime = GenTime;
	}

	public byte[] getBN() {
		return this.BN;
	}

	public void setBN(byte[] bn) {
		this.BN = bn;
	}

	public byte[] getPrvBN() {
		return this.prvBN;
	}
	
	public void setPrvBN(byte[] bn) {
		this.prvBN = bn;
	}

	public byte[] incBN() {
		long BNL = this.global.getCTypeCast().ByteArrayToLong(this.BN);
		BNL += this.totNN;
		this.BN = this.global.getCTypeCast().LongToByteArray(BNL);
		
		return this.BN;
	}

	public int getRRMyRootIdx(){
		return this.myRootIdx;
	}

	public int getRRMyNextIdx(){
		return this.myNextIdx;
	}

	public byte[] getRRMyNextNNP2PAddr(){
		return this.nnP2PAddr[this.myNextIdx];
	}

	public byte getRRMyNextNNProto(){
		return this.nnProto[this.myNextIdx];
	}

	public String getRRMyNextNNIP(){
		return this.nnIP[this.myNextIdx];
	}

	public int getRRMyNextNNPort(){
		return this.nnPort[this.myNextIdx];
	}

	public byte[] getCurrBlkGenCN(){
		return (this.currBlkGenCN);
	}

	public byte[] getNextBlkGenCN(){
		return (this.nextBlkGenCN);
	}

	public void setBlkGenCN(){
		if(this.cnNum > 0)
		{
			this.currIdx = this.nextIdx;
			this.nextIdx = this.global.getCTypeCast().next_idx(this.currIdx, this.cnNum);
			
			this.currBlkGenCN = this.cnP2PAddr[this.currIdx];
			this.nextBlkGenCN = this.cnP2PAddr[this.nextIdx];
		}
	}

	public boolean getIsMySubNetCN(){
		return (this.isMySubNetCN);
	}
	
	public void setIsMySubNetCN(boolean IsMySubNetCN){
		this.isMySubNetCN = IsMySubNetCN;
	}

	public byte[] getCurrTxCN(){
		return (this.currTxCN);
	}

	public void setCurrTxCN(byte[] CurrTxCN){
		this.currTxCN = CurrTxCN;
	}
}
