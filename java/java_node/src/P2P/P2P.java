package P2P;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.HashMap;

import com.google.gson.JsonObject;

import Main.Global;
import io.netty.channel.ChannelHandlerContext;

////////////////////////////////////////////////////////////////////////////////////////////////////////
// P2P class instance control about P2P Network info (e.g. P2PAddr, PeerPublicKey etc...)             //
// P2P class is performing about between Clusters and cluster's subNetwork setting                    //
// P2P's role is controlling My P2PNetowrk info, and PeerP2PNetwork info                              //
// P2P is operating Peer info table                                                                   //
// so, known about cluster Root's P2PAddress or who is Block Generator's P2PAddress                   //
// PeerTable is distinguish who is NN of MyCluster who is Block Generator in My Cluster               //
// between NN and CN communicate using P2P_TX, P2P_RX methods                                         //
////////////////////////////////////////////////////////////////////////////////////////////////////////

enum P2P_Node_Rule{
	NN, CN, SCN
}

enum P2P_Type{
	CMD, MGMT, DATA
}

enum P2P_Cmd{
	P2P_JOIN_REQ, P2P_JOIN_RESP, P2P_PUBKEY_NOTI
}

@SuppressWarnings("unchecked")
enum  P2P_Define{
	Succcess(0), Error(-1), CheckCRC(1), CRC32Len(4), MakePubkeyPath(0), P2PAddrLen(8), PeerMax(10);

	private int value;
	@SuppressWarnings("rawtypes")
	private static Map map = new HashMap<>();
	
	private P2P_Define(int value) {
        this.value = value;
    }

    static {
        for (P2P_Define p2pDef : P2P_Define.values()) {
            map.put(p2pDef.value, p2pDef);
        }
    }

    public static P2P_Define valueOf(int p2pDef) {
        return (P2P_Define) map.get(p2pDef);
    }

    public int getValue() {
        return value;
    }
    
    public static int getTotalValue() {
    	int tot;
    	
    	tot = P2P_Define.PeerMax.getValue();
    	
    	return tot;
    }
}

@SuppressWarnings("unchecked")
enum  P2P_Header_field{
	DstAddr(8), SrcAddr(8), TimeStamp(8), Version(1), Type(1), Cmd(1), Rsvd(1), SeqNum(2), Len(2);
	
	private int value;
	@SuppressWarnings("rawtypes")
	private static Map map = new HashMap<>();
	
	private P2P_Header_field(int value) {
        this.value = value;
    }

    static {
        for (P2P_Header_field p2pHdr : P2P_Header_field.values()) {
            map.put(p2pHdr.value, p2pHdr);
        }
    }

    public static P2P_Header_field valueOf(int p2pHdr) {
        return (P2P_Header_field) map.get(p2pHdr);
    }

    public int getValue() {
        return value;
    }
    
    public static int getTotalValue() {
    	int tot;
    	
    	tot = P2P_Header_field.DstAddr.getValue() 
    			+ P2P_Header_field.SrcAddr.getValue()
    			+ P2P_Header_field.TimeStamp.getValue()
    			+ P2P_Header_field.Version.getValue()
    			+ P2P_Header_field.Type.getValue()
    			+ P2P_Header_field.Cmd.getValue()
    			+ P2P_Header_field.Rsvd.getValue()
    			+ P2P_Header_field.SeqNum.getValue();
    	
    	return tot;
    }
}

@SuppressWarnings("unchecked")
enum  P2P_Cmd_Join_Req_field{
	JoinP2PAddr(8), NodeRule(1);
	
	private int value;
	@SuppressWarnings("rawtypes")
	private static Map map = new HashMap<>();
	
	private P2P_Cmd_Join_Req_field(int value) {
        this.value = value;
    }

    static {
        for (P2P_Cmd_Join_Req_field joinReq : P2P_Cmd_Join_Req_field.values()) {
            map.put(joinReq.value, joinReq);
        }
    }

    public static P2P_Cmd_Join_Req_field valueOf(int joinReq) {
        return (P2P_Cmd_Join_Req_field) map.get(joinReq);
    }

    public int getValue() {
        return value;
    }
    
    public static int getTotalValue() {
    	int tot;
    	
    	tot = P2P_Cmd_Join_Req_field.JoinP2PAddr.getValue() 
    			+ P2P_Cmd_Join_Req_field.NodeRule.getValue();
    	
    	return tot;
    }
}

@SuppressWarnings("unchecked")
enum  P2P_Cmd_Join_Rsp_field{
	JoinP2PAddr(8), NodeRule(1);
	
	private int value;
	@SuppressWarnings("rawtypes")
	private static Map map = new HashMap<>();
	
	private P2P_Cmd_Join_Rsp_field(int value) {
        this.value = value;
    }

    static {
        for (P2P_Cmd_Join_Rsp_field joinRsp : P2P_Cmd_Join_Rsp_field.values()) {
            map.put(joinRsp.value, joinRsp);
        }
    }

    public static P2P_Cmd_Join_Rsp_field valueOf(int joinRsp) {
        return (P2P_Cmd_Join_Rsp_field) map.get(joinRsp);
    }

    public int getValue() {
        return value;
    }
    
    public static int getTotalValue() {
    	int tot;
    	
    	tot = P2P_Cmd_Join_Rsp_field.JoinP2PAddr.getValue() 
    			+ P2P_Cmd_Join_Rsp_field.NodeRule.getValue();
    	
    	return tot;
    }
}

@SuppressWarnings("unchecked")
enum  P2P_Cmd_Pubkey_Noti_field{
	CompPubkey(33), PeerSubNetAddr(4);
	
	private int value;
	@SuppressWarnings("rawtypes")
	private static Map map = new HashMap<>();
	
	private P2P_Cmd_Pubkey_Noti_field(int value) {
        this.value = value;
    }

    static {
        for (P2P_Cmd_Pubkey_Noti_field pubkeyNoti : P2P_Cmd_Pubkey_Noti_field.values()) {
            map.put(pubkeyNoti.value, pubkeyNoti);
        }
    }

    public static P2P_Cmd_Pubkey_Noti_field valueOf(int pubkeyNoti) {
        return (P2P_Cmd_Pubkey_Noti_field) map.get(pubkeyNoti);
    }

    public int getValue() {
        return value;
    }
    
    public static int getTotalValue() {
    	int tot;
    	
    	tot = P2P_Cmd_Pubkey_Noti_field.CompPubkey.getValue();
    	
    	return tot;
    }
}

public class P2P {
	private Global global;
	
	private byte[] MyClusterRoot;
	private byte[] MyP2PAddr;
	private String MyPrivateKeyPath;
	private String MyPublicKeyPath;
	private byte[] MyNodeRule;
	private short MyP2PDataSN;
	private short MyP2PCmdSN;
	
	private Map<byte[], String> PeerPublicKeyPathMap;
	private Map<byte[], InetSocketAddress>dst_sock;	
	private Map<InetSocketAddress, ChannelHandlerContext>sock_ctxs;
	
	@SuppressWarnings("unused")
	private byte[] MyNNP2PAddr;

	// Peer Table
	private boolean[] MyPeerActive;
	private byte[][] MyPeerP2PAddr;
	private ChannelHandlerContext[] MyPeerCtx;
	private byte[] MyPeerNodeRule;
	private String[] MyPeerPublicKeyPath;
	
	//
	private static byte MyVersion;

	public P2P(Global global) {
		this.global = global;
		this.PeerPublicKeyPathMap = new HashMap<byte[], String>();
		this.dst_sock = new HashMap<byte[], InetSocketAddress>();
		this.sock_ctxs = new HashMap<InetSocketAddress, ChannelHandlerContext>();
	}
	
	// 
	
	// init과 Update로 분리 필요
	
	// Parsing node.json and Setting My P2P Network info
	// e.g) MyRule, MyP2PAddress, MyClusterRoot etc...
	public void init() {
		JsonObject cons_json = this.global.getNodeJson().getAsJsonObject("CONS");
		this.MyPrivateKeyPath = cons_json.getAsJsonObject("PATH").get("MY_PRIKEY").getAsString();
		this.MyPublicKeyPath = cons_json.getAsJsonObject("PATH").get("MY_PUBKEY").getAsString();

		this.MyNodeRule = new byte[1];
		String rule = this.global.getNodeJson().get("RULE").getAsString();
		if(rule.equals("NN")) {
			this.MyNodeRule[0] = (byte)P2P_Node_Rule.NN.ordinal();
			this.global.setNetworkNodePort();
		}
		else if(rule.equals("CN")) this.MyNodeRule[0] = (byte)P2P_Node_Rule.CN.ordinal();
		else if(rule.equals("SCN")) this.MyNodeRule[0] = (byte)P2P_Node_Rule.SCN.ordinal();
		
		JsonObject p2p_json = this.global.getNodeJson().getAsJsonObject("P2P");
		this.MyClusterRoot = this.global.getCTypeCast().HexStrToByteArray(p2p_json.getAsJsonObject("CLUSTER").get("ROOT").getAsString().substring(2, 18));
		byte[] addr = this.global.getCTypeCast().HexStrToByteArray(p2p_json.getAsJsonObject("CLUSTER").get("ADDR").getAsString().substring(2, 6));
		this.MyP2PAddr = new byte[P2P_Define.P2PAddrLen.getValue()];
		System.arraycopy(this.MyClusterRoot, 0, this.MyP2PAddr, 0, P2P_Define.P2PAddrLen.getValue());
		System.arraycopy(addr, 0, this.MyP2PAddr, 6, 2);

		this.MyP2PDataSN = 0;
		this.MyP2PCmdSN = 0;

		// Peer Table Init
		initPeerTable();

		//
		this.MyVersion = 0x00;
		
		cons_json = null;
		p2p_json = null;
		addr = null;
	}

	public void P2PJoinReqPrint(byte[] P2PAddr, byte[] NodeRule) {
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "P2PAddr : " + this.global.getCTypeCast().ByteHexToString(P2PAddr));
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "NodeRule : " + this.global.getCTypeCast().ByteHexToString(NodeRule));
	}

	public void P2PJoinRspPrint(byte[] P2PAddr, byte[] NodeRule) {
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "P2PAddr : " + this.global.getCTypeCast().ByteHexToString(P2PAddr));
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "NodeRule : " + this.global.getCTypeCast().ByteHexToString(NodeRule));
	}

	public void P2PPubkeyNotiPrint(byte[] CompPubKey) {
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "CompPubKey : " + this.global.getCTypeCast().ByteHexToString(CompPubKey));
	}

	public byte[] getMyNodeRule() {
		return this.MyNodeRule;
	}

	public void setMyNodeRule(byte[] NodeRule) {
		this.MyNodeRule = NodeRule;
	}

	public String getMyPrivateKeyPath() {
		return this.MyPrivateKeyPath;
	}

	public void setMyPrivateKeyPath(String MyPrivateKeyPath) {
		this.MyPrivateKeyPath = MyPrivateKeyPath;
	}

	public String getMyPublicKeyPath() {
		return this.MyPublicKeyPath;
	}

	public void setMyPublicKeyPath (String MyPublicKeyPath) {
		this.MyPublicKeyPath = MyPublicKeyPath;
	}

	public Map<byte[], String> getPeerPublicKeyPathMap(){
		return this.PeerPublicKeyPathMap;
	}

	public byte getENodeType(String nodeType){
		if (nodeType.equals("NN"))
		{
			return (byte) P2P_Node_Rule.NN.ordinal();
		}
		else if (nodeType.equals("CN"))
		{
			return (byte) P2P_Node_Rule.CN.ordinal();
		}
		else if (nodeType.equals("SCN"))
		{
			return (byte) P2P_Node_Rule.SCN.ordinal();
		}
		else
		{
			// Error
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Node Type is Wrong");
			
			return (byte) P2P_Node_Rule.NN.ordinal();
		}
	}

	public int getP2PDefLen(String field){
		int len = 0;
		if(field.equals("P2PAddrLen"))
		{
			len = P2P_Define.P2PAddrLen.getValue();
		}
		else
		{
			// Error
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "getP2PDefLen No Filed : " + field);
		}

		return len;
	}
	
	public byte[] getMyClusterRoot() {
		return this.MyClusterRoot;
	}
	
	public byte[] getMyP2PAddr() {
		return this.MyP2PAddr;
	}
	
	// initialize My PeerTable
	// PeerTable has Peer's ActiveInfo, Peer's P2PAddress, Peer's Socket CTX, Peer's Rule, Peer's PublicKeyPath
	private void initPeerTable(){
		this.MyPeerActive = new boolean[P2P_Define.PeerMax.getValue()];
		this.MyPeerP2PAddr = new byte[P2P_Define.PeerMax.getValue()][P2P_Define.P2PAddrLen.getValue()];
		this.MyPeerCtx = new ChannelHandlerContext[P2P_Define.PeerMax.getValue()];
		this.MyPeerNodeRule = new byte[P2P_Define.PeerMax.getValue()];
		this.MyPeerPublicKeyPath = new String[P2P_Define.PeerMax.getValue()];

		for (int idx=0; idx<P2P_Define.PeerMax.getValue(); idx++)
		{
			this.MyPeerActive[idx] = false;
			this.global.getCTypeCast().memset_ByteArray(this.MyPeerP2PAddr[idx], (byte)0x00);
		}
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "SubNet PeerTable init success");
	}

	public int chkMyPeerP2P(byte[] P2PAddr){
		// Check P2P Address related to input P2P Address
		// check that peer is active
		for (int idx=0; idx<P2P_Define.PeerMax.getValue(); idx++)
		{
			if (this.MyPeerActive[idx] == true)
			{
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "peer idx[" + idx + "] had been activated.");
				if (this.global.getCTypeCast().ByteArrayCompare(this.MyPeerP2PAddr[idx], P2PAddr))
				{
					return idx;
				}
			}
		}
		
		return P2P_Define.Error.getValue();
	}

	public ChannelHandlerContext getMyPeerP2PCtx(int idx){
		return (this.MyPeerCtx[idx]);
	}

	public byte getMyPeerP2PNodeRule(int idx){
		return (this.MyPeerNodeRule[idx]);
	}

	// Setting a new Peer info
	public boolean setMyPeerP2P(byte[] P2PAddr, byte NodeRule, ChannelHandlerContext ctx){
		int idx;
		
		// Check P2P Address related to input P2P Address
		idx = chkMyPeerP2P(P2PAddr);
		// Already have peer's info 
		if (idx >= 0)
		{
			this.MyPeerCtx[idx] = ctx;
			this.MyPeerNodeRule[idx] = NodeRule;
			this.dst_sock.put(P2PAddr, (InetSocketAddress)ctx.channel().remoteAddress());

			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "peer P2P Address is already stored. idx = " + idx);
			return true;
		}
		
		// else set peer info
		for (idx=0; idx<P2P_Define.PeerMax.getValue(); idx++)
		{
			if (this.MyPeerActive[idx] == false)
			{
				this.MyPeerActive[idx] = true;
				this.MyPeerP2PAddr[idx] = P2PAddr;
				this.MyPeerCtx[idx] = ctx;
				this.MyPeerNodeRule[idx] = NodeRule;
				this.dst_sock.put(P2PAddr, (InetSocketAddress)ctx.channel().remoteAddress());

				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "peer P2P Address is stored. idx = " + idx);
				return true;
			}
		}
		
		return false;
	}
	
	// get a Peer's PulbicKeyPath for exchange two peer's public keys
	// for Peer's Signature verify
	public boolean setMyPeerPublicKeyPath(byte[] P2PAddr, String PublicKeyPath) {
		int idx;
		
		idx = chkMyPeerP2P(P2PAddr);
		if(idx >= 0) 
		{
			this.MyPeerPublicKeyPath[idx] = PublicKeyPath;
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "peer Public Key Path is stored. idx = " + idx);
			return true;
		} 
		else 
		{
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "Peer Information is not stored");
			return false;
		}
	}
	
	public short getMyP2PDataSN() {
		return this.MyP2PDataSN;
	}

	public void incMyP2PDataSN() {
		this.MyP2PDataSN++;
	}

	public short getMyP2PCmdSN() {
		return this.MyP2PCmdSN;
	}

	public void incMyP2PCmdSN() {
		this.MyP2PCmdSN++;
	}

	public byte getMyVersion() {
		return MyVersion;
	}
	
	public Map<InetSocketAddress, ChannelHandlerContext> getSockCtxs() {
		return sock_ctxs;
	}
}
