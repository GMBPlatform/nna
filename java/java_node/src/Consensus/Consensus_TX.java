package Consensus;

import Data.DBKeyLists;
import Data.LightBlkInfo;
import Main.Global;
import P2P.P2P_TX;
import Contract.Contract_TX;
import io.netty.channel.ChannelHandlerContext;

////////////////////////////////////////////////////////////////////////
// Every Consensus Protocol TX Data Pass to P2P_TX class instance     //
// then P2P_TX class instance make P2P Protocol TX and Pass to Socket //
////////////////////////////////////////////////////////////////////////


public class Consensus_TX {
	private Global global;
	private P2P_TX writer;
	private Contract_TX contract_tx;
	
	public Consensus_TX(Global global) {
		this.global = global;
		this.writer = new P2P_TX(global);
		this.contract_tx = new Contract_TX(global);
	}
	
	// BlkGenInfo Routine Perform First one time
	// this Routine Start Block Generation and Notification to SCA  
	public boolean BlkGenInfo() {
		// Check Destination P2P Address
		byte[] dstAddr;
		ChannelHandlerContext ctx;
		int idx;

		dstAddr = this.global.getCConsensus().getCurrBlkGenCN();
		
		idx = this.global.getCP2P().chkMyPeerP2P(dstAddr);
		if (idx >= 0)
		{
			ctx = this.global.getCP2P().getMyPeerP2PCtx(idx);
		}
		else
		{
			// Error
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Destination Address is NOT founded.");
			dstAddr = null;
			ctx = null;
			return false;
		}

		// Consensus Header
		byte[] ConsensusHeader = MakeConsensusHeader((byte) Cons_Type.CMD.ordinal(), (byte)Cons_Cmd.BGI.ordinal());
		byte[] Len = new byte[Cons_Header_field.Len.getValue()];
		
		// Consensus BGI
		byte[] StartTime = new byte[Cons_Cmd_BGI_field.StartTime.getValue()];
		byte[] RoundCnt = new byte[Cons_Cmd_BGI_field.RoundCnt.getValue()]; // Don't Care.
		byte[] DelayTime = new byte[Cons_Cmd_BGI_field.DelayTime.getValue()];
		byte[] FirstCNP2PAddr = new byte[Cons_Cmd_BGI_field.FirstCNP2PAddr.getValue()];
		byte[] FirstBN = new byte[Cons_Cmd_BGI_field.FirstBN.getValue()];
		byte[] TierID = new byte[Cons_Cmd_BGI_field.TierID.getValue()];
		
		//
		int msglen = ConsensusHeader.length + Len.length + Cons_Cmd_BGI_field.getTotalValue();
		byte[] m_Msg = new byte[msglen];
		
		//
		int pos = 0;

		// just For Debug
		String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName(); 
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "Name of current method: " + nameofCurrMethod);

		//
		Len = this.global.getCTypeCast().shortToByteArray((short)Cons_Cmd_BGI_field.getTotalValue());

		//
		StartTime = this.global.getCConsensus().getStartTime();
		this.global.getCTypeCast().memset_ByteArray(RoundCnt, (byte)0x00);
		DelayTime = this.global.getCConsensus().getGenTime();
		FirstCNP2PAddr = this.global.getCConsensus().getCurrBlkGenCN();
		FirstBN = this.global.getCConsensus().getBN();
		TierID = this.global.getCConsensus().getTierID();

		System.arraycopy(ConsensusHeader, 0, m_Msg, pos, ConsensusHeader.length); pos += ConsensusHeader.length;
		System.arraycopy(Len, 0, m_Msg, pos, Len.length); pos += Len.length;
		//
		System.arraycopy(StartTime, 0, m_Msg, pos, StartTime.length); pos += StartTime.length;
		System.arraycopy(RoundCnt, 0, m_Msg, pos, RoundCnt.length); pos += RoundCnt.length;
		System.arraycopy(DelayTime, 0, m_Msg, pos, DelayTime.length); pos += DelayTime.length;
		System.arraycopy(FirstCNP2PAddr, 0, m_Msg, pos, FirstCNP2PAddr.length); pos += FirstCNP2PAddr.length;
		System.arraycopy(FirstBN, 0, m_Msg, pos, FirstBN.length); pos += FirstBN.length;
		System.arraycopy(TierID, 0, m_Msg, pos, TierID.length); pos += TierID.length;
		
		// Send Genesis block info to SCA
		contract_tx.BlkGenInfoToSCA(FirstBN);
		boolean ret = (PassToP2PTX(ctx, dstAddr, m_Msg));
		
		ctx = null;
		dstAddr = null;
		ConsensusHeader = null;
		Len = null;

		StartTime = null;
		RoundCnt = null;
		DelayTime = null;
		FirstCNP2PAddr = null;
		FirstBN = null;
		TierID = null;
		m_Msg = null;
		nameofCurrMethod = null;
		
		return ret;
	}
	
	// Tx Routine Can only occur NNA to CN
	public boolean Tx(byte[] DBKey, byte[] Hash) {
		// Check Destination P2P Address
		byte[] dstAddr;
		ChannelHandlerContext ctx;
		int idx;

		dstAddr = this.global.getCConsensus().getCurrTxCN();
		
		idx = this.global.getCP2P().chkMyPeerP2P(dstAddr);
		if (idx >= 0)
		{
			ctx = this.global.getCP2P().getMyPeerP2PCtx(idx);
		}
		else
		{
			// Error
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Destination Address is NOT founded.");
			dstAddr = null;
			ctx = null;
			return false;
		}
		
		// Consensus Header
		byte[] ConsensusHeader = MakeConsensusHeader((byte) Cons_Type.CMD.ordinal(), (byte)Cons_Cmd.TX.ordinal());
		byte[] Len = new byte[Cons_Header_field.Len.getValue()];
		
		// Consensus TX
		byte[] BN = new byte[Cons_Cmd_TX_field.BN.getValue()];
		byte[] TierID = new byte[Cons_Cmd_TX_field.TierID.getValue()];
		byte[] TxInfoCnt = new byte[Cons_Cmd_TX_field.TxInfoCnt.getValue()];
		// TODO TxInfoCnt Variable
		TxInfoCnt[0] = 1;
		int TxInfoListLen = TxInfoCnt[0] * Cons_Tx_Info_List.getTotalValue();
		byte[] TxInfoList = new byte[TxInfoListLen];
		
		//
		int msglen = ConsensusHeader.length + Len.length + Cons_Cmd_TX_field.getTotalValue() + TxInfoListLen;
		byte[] m_Msg = new byte[msglen];
		
		//
		int pos;
		//
		BN = this.global.getCConsensus().getBN();
		TierID = this.global.getCConsensus().getTierID();
		
		pos = 0;
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "DBKey.length " + DBKey.length + " Hash.length " + Hash.length);
		System.arraycopy(DBKey, 0, TxInfoList, pos, DBKey.length); pos += DBKey.length;
		System.arraycopy(Hash, 0, TxInfoList, pos, Hash.length); pos += Hash.length;

		// Just for Debug
		String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName(); 
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "Name of current method: " + nameofCurrMethod);

		//
		Len = this.global.getCTypeCast().shortToByteArray((short)(Cons_Cmd_TX_field.getTotalValue() + TxInfoListLen));

		//
		pos = 0;
		System.arraycopy(ConsensusHeader, 0, m_Msg, pos, ConsensusHeader.length); pos += ConsensusHeader.length;
		System.arraycopy(Len, 0, m_Msg, pos, Len.length); pos += Len.length;
		//
		System.arraycopy(BN, 0, m_Msg, pos, BN.length); pos += BN.length;
		System.arraycopy(TierID, 0, m_Msg, pos, TierID.length); pos += TierID.length;
		System.arraycopy(TxInfoCnt, 0, m_Msg, pos, TxInfoCnt.length); pos += TxInfoCnt.length;
		System.arraycopy(TxInfoList, 0, m_Msg, pos, TxInfoList.length); pos += TxInfoList.length;
		
		boolean ret = (PassToP2PTX(ctx, dstAddr, m_Msg));
		
		ctx = null;

		dstAddr = null;
		ConsensusHeader = null;
		Len = null;

		BN = null;
		TierID = null;
		TxInfoCnt = null;
		TxInfoList = null;
		m_Msg = null;
		nameofCurrMethod = null;
		
		DBKey = null;
		Hash = null;
		
		return ret;
	}
	
	// TxAck Routine can only occur CN to NNA
	public boolean TxAck(ChannelHandlerContext ctx, byte[] dstAddr, byte[] BN, byte[] TxInfoCnt, byte[] TxInfoList) {
		//
		byte[] ConsensusHeader = MakeConsensusHeader((byte) Cons_Type.CMD.ordinal(), (byte)Cons_Cmd.TX_ACK.ordinal());
		byte[] Len = new byte[Cons_Header_field.Len.getValue()];
		
		int datalen = ConsensusHeader.length + Len.length + BN.length + TxInfoCnt.length + TxInfoList.length;
		
		byte[] Data = new byte[datalen];
		int pos = 0;
		
		//
		String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName(); 
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Name of current method: " + nameofCurrMethod);

		//
		Len = this.global.getCTypeCast().shortToByteArray((short)datalen);

		//
		System.arraycopy(ConsensusHeader, 0, Data, pos, ConsensusHeader.length); pos += ConsensusHeader.length;
		System.arraycopy(Len, 0, Data, pos, Len.length); pos += Len.length;
		//
		System.arraycopy(BN, 0, Data, pos, BN.length); pos += BN.length;
		System.arraycopy(TxInfoCnt, 0, Data, pos, TxInfoCnt.length); pos += TxInfoCnt.length;
		System.arraycopy(TxInfoList, 0, Data, pos, TxInfoList.length); 
		
		boolean ret = (PassToP2PTX(ctx, dstAddr, Data));

		ConsensusHeader = null;
		Len = null;
		Data = null;
		nameofCurrMethod = null;
		
		return ret;
	}
	
	// TxStopReq routine can only occur CN to NNA
	public boolean TxStopReq() {
		// Check Destination P2P Address
		byte[] dstAddr;
		ChannelHandlerContext ctx;
		int idx;

		dstAddr = this.global.getCP2P().getMyClusterRoot();
		
		idx = this.global.getCP2P().chkMyPeerP2P(dstAddr);
		if (idx >= 0)
		{
			ctx = this.global.getCP2P().getMyPeerP2PCtx(idx);
		}
		else
		{
			// Error
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Destination Address is NOT founded.");
			dstAddr = null;
			ctx = null;
			return false;
		}
		
		// Consensus Header
		byte[] ConsensusHeader = MakeConsensusHeader((byte) Cons_Type.CMD.ordinal(), (byte)Cons_Cmd.TX_STOP_REQ.ordinal());
		byte[] Len = new byte[Cons_Header_field.Len.getValue()];

		// Consensus TX_STOP_REQ
		byte[] P2PAddr = new byte[Cons_Cmd_TX_STOP_field.P2PAddr.getValue()];
		byte[] BN = new byte[Cons_Cmd_TX_STOP_field.BN.getValue()];
		byte[] TierID = new byte[Cons_Cmd_TX_STOP_field.TierID.getValue()];
		
		//
		int msglen = ConsensusHeader.length + Len.length + Cons_Cmd_TX_STOP_field.getTotalValue();
		byte[] m_Msg = new byte[msglen];
		
		//
		int pos = 0;

		// Just for Debug
		String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName(); 
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "Name of current method: " + nameofCurrMethod);

		//
		Len = this.global.getCTypeCast().shortToByteArray((short)Cons_Cmd_TX_STOP_field.getTotalValue());

		//
		P2PAddr = this.global.getCP2P().getMyClusterRoot();
		BN = this.global.getCConsensus().getBN();
		TierID = this.global.getCConsensus().getTierID();

		System.arraycopy(ConsensusHeader, 0, m_Msg, pos, ConsensusHeader.length); pos += ConsensusHeader.length;
		System.arraycopy(Len, 0, m_Msg, pos, Len.length); pos += Len.length;
		//
		System.arraycopy(P2PAddr, 0, m_Msg, pos, P2PAddr.length); pos += P2PAddr.length;
		System.arraycopy(BN, 0, m_Msg, pos, BN.length); pos += BN.length;
		System.arraycopy(TierID, 0, m_Msg, pos, TierID.length); pos += TierID.length;
		
		boolean ret = (PassToP2PTX(ctx, dstAddr, m_Msg));
		
		ctx = null;
		
		dstAddr = null;
		ConsensusHeader = null;
		Len = null;
		
		P2PAddr = null;
		BN = null;
		TierID = null;
		m_Msg = null;
		
		nameofCurrMethod = null;
		
		return ret;
	}
	
	// TxStopRsp routine can only occur NNA to CN
	public boolean TxStopRsp(ChannelHandlerContext ctx, byte[] dstAddr, byte[] reqBN, byte[] reqTierID) {
		// Consensus Header
		byte[] ConsensusHeader = MakeConsensusHeader((byte) Cons_Type.CMD.ordinal(), (byte)Cons_Cmd.TX_STOP_RSP.ordinal());
		byte[] Len = new byte[Cons_Header_field.Len.getValue()];
		
		// Consensus TX_STOP_RSP
		byte[] P2PAddr = new byte[Cons_Cmd_TX_STOP_field.P2PAddr.getValue()];
		byte[] BN = new byte[Cons_Cmd_TX_STOP_field.BN.getValue()];
		byte[] TierID = new byte[Cons_Cmd_TX_STOP_field.TierID.getValue()];
		
		//
		int msglen = ConsensusHeader.length + Len.length + Cons_Cmd_TX_STOP_field.getTotalValue();
		byte[] m_Msg = new byte[msglen];
		
		//
		int pos = 0;

		// Just for debug
		String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName(); 
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "Name of current method: " + nameofCurrMethod);
		//
		Len = this.global.getCTypeCast().shortToByteArray((short)Cons_Cmd_TX_STOP_field.getTotalValue());

		//
		P2PAddr = dstAddr;
		BN = reqBN;
		TierID = reqTierID;

		System.arraycopy(ConsensusHeader, 0, m_Msg, pos, ConsensusHeader.length); pos += ConsensusHeader.length;
		System.arraycopy(Len, 0, m_Msg, pos, Len.length); pos += Len.length;
		//
		System.arraycopy(P2PAddr, 0, m_Msg, pos, P2PAddr.length); pos += P2PAddr.length;
		System.arraycopy(BN, 0, m_Msg, pos, BN.length); pos += BN.length;
		System.arraycopy(TierID, 0, m_Msg, pos, TierID.length); pos += TierID.length;
		
		boolean ret = (PassToP2PTX(ctx, dstAddr, m_Msg));
		
		ctx = null;

		ConsensusHeader = null;
		Len = null;
		P2PAddr = null;
		BN = null;
		TierID = null;
		m_Msg = null;
		
		nameofCurrMethod = null;
		
		return ret;
	}
	
	// BlkNotiFromCN routine can only occur CN to NNA
	public boolean BlkNotiFromCN(LightBlkInfo m_LightBlkInfo, DBKeyLists m_DBKeyLists) {
		// Check Destination P2P Address
		byte[] dstAddr;
		ChannelHandlerContext ctx;
		int idx;

		dstAddr = this.global.getCP2P().getMyClusterRoot();
		
		idx = this.global.getCP2P().chkMyPeerP2P(dstAddr);
		if (idx >= 0)
		{
			ctx = this.global.getCP2P().getMyPeerP2PCtx(idx);
		}
		else
		{
			// Error
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Destination Address is NOT founded.");
			dstAddr = null;
			ctx = null;
			return false;
		}
		
		// Consensus Header
		byte[] ConsensusHeader = MakeConsensusHeader((byte) Cons_Type.CMD.ordinal(), (byte)Cons_Cmd.BLK_NOTI_FROM_CN.ordinal());
		byte[] Len = new byte[Cons_Header_field.Len.getValue()];
		
		// Consensus BLK_NOTI_FROM_CN
		byte[] NextDelayTime = new byte[Cons_Cmd_BLK_NOTI_field.NextDelayTime.getValue()];
		byte[] NextSeqIdx = new byte[Cons_Cmd_BLK_NOTI_field.NextSeqIdx.getValue()];
		byte[] NextBlkGenCNP2PAddr = new byte[Cons_Cmd_BLK_NOTI_field.NextBlkGenCNP2PAddr.getValue()];
		byte[] NextBN = new byte[Cons_Cmd_BLK_NOTI_field.NextBN.getValue()];

		int DBKeysLen = this.global.getCTypeCast().ByteArrayToInt(m_DBKeyLists.DBKeyCnt) * Cons_Define.DBKey.getValue();
//		byte[] DBKeys = new byte[DBKeysLen];
		
		//
		int ConsLen = Cons_Cmd_BLK_NOTI_field.getTotalValue() + Cons_Cmd_Ligth_Blk_Info_field.getTotalValue() + Cons_DB_Key_List.getTotalValue();
		int msglen = ConsensusHeader.length + Len.length + ConsLen + DBKeysLen;
		byte[] m_Msg = new byte[msglen];
		
		//
		int pos = 0;

		// Just for Debug
		String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName(); 
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "Name of current method: " + nameofCurrMethod);
		//
		Len = this.global.getCTypeCast().shortToByteArray((short)ConsLen);

		// Consensus BLK_NOTI_FROM_CN
		this.global.getCTypeCast().memset_ByteArray(NextDelayTime, (byte)0x00);
		this.global.getCTypeCast().memset_ByteArray(NextSeqIdx, (byte)0x00);
		this.global.getCTypeCast().memset_ByteArray(NextBlkGenCNP2PAddr, (byte)0x00);
		this.global.getCTypeCast().memset_ByteArray(NextBN, (byte)0x00);

		//
		System.arraycopy(ConsensusHeader, 0, m_Msg, pos, ConsensusHeader.length); pos += ConsensusHeader.length;
		System.arraycopy(Len, 0, m_Msg, pos, Len.length); pos += Len.length;
		//
		System.arraycopy(NextDelayTime, 0, m_Msg, pos, NextDelayTime.length); pos += NextDelayTime.length;
		System.arraycopy(NextSeqIdx, 0, m_Msg, pos, NextSeqIdx.length); pos += NextSeqIdx.length;
		System.arraycopy(NextBlkGenCNP2PAddr, 0, m_Msg, pos, NextBlkGenCNP2PAddr.length); pos += NextBlkGenCNP2PAddr.length;
		System.arraycopy(NextBN, 0, m_Msg, pos, NextBN.length); pos += NextBN.length;
		//
		System.arraycopy(m_LightBlkInfo.BN, 0, m_Msg, pos, m_LightBlkInfo.BN.length); pos += m_LightBlkInfo.BN.length;
		System.arraycopy(m_LightBlkInfo.TierID, 0, m_Msg, pos, m_LightBlkInfo.TierID.length); pos += m_LightBlkInfo.TierID.length;
		System.arraycopy(m_LightBlkInfo.P2PAddr, 0, m_Msg, pos, m_LightBlkInfo.P2PAddr.length); pos += m_LightBlkInfo.P2PAddr.length;
		System.arraycopy(m_LightBlkInfo.BlkGenTime, 0, m_Msg, pos, m_LightBlkInfo.BlkGenTime.length); pos += m_LightBlkInfo.BlkGenTime.length;
		System.arraycopy(m_LightBlkInfo.PrevBlkHash, 0, m_Msg, pos, m_LightBlkInfo.PrevBlkHash.length); pos += m_LightBlkInfo.PrevBlkHash.length;
		System.arraycopy(m_LightBlkInfo.TxCnt, 0, m_Msg, pos, m_LightBlkInfo.TxCnt.length); pos += m_LightBlkInfo.TxCnt.length;
		System.arraycopy(m_LightBlkInfo.CurrBlkHash, 0, m_Msg, pos, m_LightBlkInfo.CurrBlkHash.length); pos += m_LightBlkInfo.CurrBlkHash.length;
		System.arraycopy(m_LightBlkInfo.Sig, 0, m_Msg, pos, m_LightBlkInfo.Sig.length); pos += m_LightBlkInfo.Sig.length;
		//
		System.arraycopy(m_DBKeyLists.FirstTxDBKey, 0, m_Msg, pos, m_DBKeyLists.FirstTxDBKey.length); pos += m_DBKeyLists.FirstTxDBKey.length;
		System.arraycopy(m_DBKeyLists.LastTxDBKey, 0, m_Msg, pos, m_DBKeyLists.LastTxDBKey.length); pos += m_DBKeyLists.LastTxDBKey.length;
		System.arraycopy(m_DBKeyLists.DBKeyCnt, 0, m_Msg, pos, m_DBKeyLists.DBKeyCnt.length); pos += m_DBKeyLists.DBKeyCnt.length;
		if(this.global.getCTypeCast().ByteArrayToInt(m_DBKeyLists.DBKeyCnt) > 0) {
			System.arraycopy(m_DBKeyLists.DBKeys, 0, m_Msg, pos, DBKeysLen);
		}
		
		boolean ret = (PassToP2PTX(ctx, dstAddr, m_Msg));
		
		m_LightBlkInfo = null;
		m_DBKeyLists = null;
		
		ctx = null;
		
		dstAddr = null;
		ConsensusHeader = null;
		Len = null;	
		
		NextDelayTime = null;
		NextSeqIdx = null;
		NextBlkGenCNP2PAddr = null;
		NextBN = null;
		m_Msg = null;
		
		nameofCurrMethod = null;
		
		return ret;
	}
	
	// BlkNotiFromNN routine can occur (NNA to CN) or (NNA to Next NNA)	
	@SuppressWarnings("unused")
	public boolean BlkNotiFromNN(LightBlkInfo m_LightBlkInfo, DBKeyLists m_DBKeyLists,
									byte[] NextDelayTime, byte[] NextSeqIdx, byte[] NextBlkGenCNP2PAddr, byte[] NextBN) {
		// Check Destination P2P Address
		byte[] dstAddr;
		ChannelHandlerContext ctx;
		int idx;

		boolean isMySubNetCN = this.global.getCConsensus().getIsMySubNetCN();

		if (isMySubNetCN)
		{
			dstAddr = this.global.getCConsensus().getCurrBlkGenCN();
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, 
					"Send to My SubNet. P2P Addr : " + this.global.getCTypeCast().ByteHexToString(dstAddr));
		}
		else
		{
			dstAddr = this.global.getCConsensus().getRRMyNextNNP2PAddr();
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, 
					"Send to Otehr SubNet. P2P Addr : " + this.global.getCTypeCast().ByteHexToString(dstAddr));
		}

		idx = this.global.getCP2P().chkMyPeerP2P(dstAddr);
		if (idx >= 0)
		{
			ctx = this.global.getCP2P().getMyPeerP2PCtx(idx);
		}
		else
		{
			// Error
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Destination Address is NOT founded.");
			ctx = null;
			dstAddr = null;
			
			return false;
		}
		
		// Consensus Header
		byte[] ConsensusHeader = MakeConsensusHeader((byte) Cons_Type.CMD.ordinal(), (byte)Cons_Cmd.BLK_NOTI_FROM_NN.ordinal());
		byte[] Len = new byte[Cons_Header_field.Len.getValue()];
		
		// Consensus BLK_NOTI_FROM_CN

		int DBKeysLen = this.global.getCTypeCast().ByteArrayToInt(m_DBKeyLists.DBKeyCnt) * Cons_Define.DBKey.getValue();
		byte[] DBKeys = null;
		
		//
		int ConsLen = Cons_Cmd_BLK_NOTI_field.getTotalValue() + Cons_Cmd_Ligth_Blk_Info_field.getTotalValue() + Cons_DB_Key_List.getTotalValue();
		int msglen = ConsensusHeader.length + Len.length + ConsLen + DBKeysLen;
		byte[] m_Msg = new byte[msglen];
		
		//
		int pos = 0;

		// Just for Debug
		String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName(); 
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "Name of current method: " + nameofCurrMethod);

		//
		Len = this.global.getCTypeCast().shortToByteArray((short)ConsLen);

		// Consensus BLK_NOTI_FROM_NN

		//
		System.arraycopy(ConsensusHeader, 0, m_Msg, pos, ConsensusHeader.length); pos += ConsensusHeader.length;
		System.arraycopy(Len, 0, m_Msg, pos, Len.length); pos += Len.length;
		//
		System.arraycopy(NextDelayTime, 0, m_Msg, pos, NextDelayTime.length); pos += NextDelayTime.length;
		System.arraycopy(NextSeqIdx, 0, m_Msg, pos, NextSeqIdx.length); pos += NextSeqIdx.length;
		System.arraycopy(NextBlkGenCNP2PAddr, 0, m_Msg, pos, NextBlkGenCNP2PAddr.length); pos += NextBlkGenCNP2PAddr.length;
		System.arraycopy(NextBN, 0, m_Msg, pos, NextBN.length); pos += NextBN.length;
		//
		System.arraycopy(m_LightBlkInfo.BN, 0, m_Msg, pos, m_LightBlkInfo.BN.length); pos += m_LightBlkInfo.BN.length;
		System.arraycopy(m_LightBlkInfo.TierID, 0, m_Msg, pos, m_LightBlkInfo.TierID.length); pos += m_LightBlkInfo.TierID.length;
		System.arraycopy(m_LightBlkInfo.P2PAddr, 0, m_Msg, pos, m_LightBlkInfo.P2PAddr.length); pos += m_LightBlkInfo.P2PAddr.length;
		System.arraycopy(m_LightBlkInfo.BlkGenTime, 0, m_Msg, pos, m_LightBlkInfo.BlkGenTime.length); pos += m_LightBlkInfo.BlkGenTime.length;
		System.arraycopy(m_LightBlkInfo.PrevBlkHash, 0, m_Msg, pos, m_LightBlkInfo.PrevBlkHash.length); pos += m_LightBlkInfo.PrevBlkHash.length;
		System.arraycopy(m_LightBlkInfo.TxCnt, 0, m_Msg, pos, m_LightBlkInfo.TxCnt.length); pos += m_LightBlkInfo.TxCnt.length;
		System.arraycopy(m_LightBlkInfo.CurrBlkHash, 0, m_Msg, pos, m_LightBlkInfo.CurrBlkHash.length); pos += m_LightBlkInfo.CurrBlkHash.length;
		System.arraycopy(m_LightBlkInfo.Sig, 0, m_Msg, pos, m_LightBlkInfo.Sig.length); pos += m_LightBlkInfo.Sig.length;
		//
		System.arraycopy(m_DBKeyLists.FirstTxDBKey, 0, m_Msg, pos, m_DBKeyLists.FirstTxDBKey.length); pos += m_DBKeyLists.FirstTxDBKey.length;
		System.arraycopy(m_DBKeyLists.LastTxDBKey, 0, m_Msg, pos, m_DBKeyLists.LastTxDBKey.length); pos += m_DBKeyLists.LastTxDBKey.length;
		System.arraycopy(m_DBKeyLists.DBKeyCnt, 0, m_Msg, pos, m_DBKeyLists.DBKeyCnt.length); pos += m_DBKeyLists.DBKeyCnt.length;
		if(this.global.getCTypeCast().ByteArrayToInt(m_DBKeyLists.DBKeyCnt) > 0) {
			System.arraycopy(m_DBKeyLists.DBKeys, 0, m_Msg, pos, DBKeysLen);
		}
		
		boolean ret = (PassToP2PTX(ctx, dstAddr, m_Msg));
		
		ctx = null;
		dstAddr = null;
		ConsensusHeader = null;
		Len = null;
		DBKeys = null;
		m_Msg = null;
		
		nameofCurrMethod = null;
		
		return ret;
	}
	
	// Make Consensus Protocol Header
	public byte[] MakeConsensusHeader(byte type, byte cmd) {
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Make ConsensusHeader");
		byte[] Version = new byte[Cons_Header_field.Version.getValue()];
		byte[] Type = new byte[Cons_Header_field.Type.getValue()];
		byte[] Cmd = new byte[Cons_Header_field.Cmd.getValue()];
		byte[] ConsensusHeader = new byte[Cons_Header_field.getTotalValue()];
		int pos = 0;

		Version[0] = this.global.getCConsensus().getMyVersion();
		Type[0] = type;
		Cmd[0] = cmd;
		
		System.arraycopy(Version, 0, ConsensusHeader, pos, Version.length); pos += Version.length;
		System.arraycopy(Type, 0, ConsensusHeader, pos, Type.length); pos += Type.length;
		System.arraycopy(Cmd, 0, ConsensusHeader, pos, Cmd.length); pos += Cmd.length;
		
		Version = null;
		Type = null;
		Cmd = null;
		
		return ConsensusHeader;
	}
	
	synchronized public boolean PassToP2PTX(ChannelHandlerContext ctx, byte[] dstAddr, byte[] Contents) {
		return (writer.PassFromConsensusTX(ctx, dstAddr, Contents));
	}
}
