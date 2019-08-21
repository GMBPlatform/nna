package Consensus;

import io.netty.channel.ChannelHandlerContext;

import Main.Global;
import Contract.Contract_TX;
import Data.DBKeyLists;
import Data.LightBlkInfo;

//////////////////////////////////////////////////////////////////////////////////////////
//Every Consensus Protocol RX Data Pass from P2P_RX class instance                      //
//then Consensus_RX class instance parsing Consensus Protocol and perform each function //
//////////////////////////////////////////////////////////////////////////////////////////

public class Consensus_RX {
	private Global global;
	
	private Consensus_TX consensus_tx;
	private Contract_TX contract_tx;

	private byte[] m_Version;
	private byte[] m_Type;
	private byte[] m_Cmd;
	private byte[] m_Len;
	private byte[] m_Msg;

	@SuppressWarnings("unused")
	private LightBlkInfo m_PrivLightBlkInfo;
	
	public Consensus_RX(Global global) {
		this.global = global;
		this.consensus_tx = new Consensus_TX(this.global);
		this.contract_tx = new Contract_TX(global);
		
		this.m_Version = new byte[Cons_Header_field.Version.getValue()];
		this.m_Type = new byte[Cons_Header_field.Type.getValue()];
		this.m_Cmd = new byte[Cons_Header_field.Cmd.getValue()];
		this.m_Len = new byte[Cons_Header_field.Len.getValue()];

		this.m_PrivLightBlkInfo = null;
	}
	
	public void PassFromP2PRX(byte[] msg, ChannelHandlerContext ctx) {
		int pos;
		
		// Receive Consensus Protocol Data from P2P Instance
		// First Parsing Consensus Header Data
		// Get Current Version, Type, Cmd, Length
		// then branching by Type
		// and then if type is "CMD" then one more branching by Cmd
		// after branching by Cmd, Performing Routine each Cmd

		pos = ParseConsHeader(msg);
		if(pos > 0) {
			if(this.m_Type[0] == Cons_Type.CMD.ordinal()) {
				// Type == Cmd
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "This is a CMD Type Message");
				if(this.m_Cmd[0] == Cons_Cmd.BGI.ordinal()) {
					// Block Generation Information
					if(!ParseBlkGenInfo(ctx)) 
						this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "BlockGeneration Parsing error");
				}
				else if(this.m_Cmd[0] == Cons_Cmd.TX.ordinal()) {
					// Tx
					if(!ParseTx(ctx)) 
						this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Tx Parsing error");
				}
				else if(this.m_Cmd[0] == Cons_Cmd.TX_ACK.ordinal()) {
					// Tx Ack
					if(!ParseTxAck(ctx)) 
						this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Tx Ack Parsing error");
				}
				else if(this.m_Cmd[0] == Cons_Cmd.TX_STOP_REQ.ordinal()) {
					// Tx Stop Request
					if(!ParseTxStopReq(ctx)) 
						this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Tx Stop Req Parsing error");
				}
				else if(this.m_Cmd[0] == Cons_Cmd.TX_STOP_RSP.ordinal()) {
					// Tx Stop Response
					if(!ParseTxStopRsp(ctx)) 
						this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Tx Stop Rep Parsing error");
				}
				else if(this.m_Cmd[0] == Cons_Cmd.BLK_NOTI_FROM_CN.ordinal()) {
					// Block Notification from CN
					if(!ParseBlkNotiFromCN(ctx)) 
						this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "BlkNotiFromCN Parsing error");
				}
				else if(this.m_Cmd[0] == Cons_Cmd.BLK_NOTI_FROM_NN.ordinal()) {
					// Block Notification from NN
					if(!ParseBlkNotiFromNN(ctx)) 
						this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "BlkNotiFromNN Parsing error");
				}
				else {
					this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_WARNING, "This cmd is not a member of Consensus cmd");
				}
			}
			else if(this.m_Type[0] == Cons_Type.MGMT.ordinal()) {
				// Type == Mgmt
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "This Type is Consensus MGMT Type");
			}
			else if(this.m_Type[0] == Cons_Type.DATA.ordinal()) {
				// Type == Data
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "This Type is Consensus Data Type");
			}
			else {
				// Error
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_WARNING, "This type is not a member of Consensus Type");
			}
		}
	}
	
	synchronized public int ParseConsHeader(byte[] msg) {
		try {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Parsing Consensus Header");
			// Consensus Header = Version + Type + Cmd + Length = 5 Bytes
			int msgLen = msg.length-Cons_Header_field.getTotalValue() - Cons_Header_field.Len.getValue();
			this.m_Msg = new byte[msgLen];
			int pos = 0;
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "ParseConsHeader msgLen " + msgLen);
			
			System.arraycopy(msg, pos, this.m_Version, 0, this.m_Version.length); pos += this.m_Version.length;
			System.arraycopy(msg, pos, this.m_Type, 0, this.m_Type.length); pos += this.m_Type.length;
			System.arraycopy(msg, pos, this.m_Cmd, 0, this.m_Cmd.length); pos += this.m_Cmd.length;
			System.arraycopy(msg, pos, this.m_Len, 0, this.m_Len.length); pos += this.m_Len.length;
			System.arraycopy(msg, pos, this.m_Msg, 0, this.m_Msg.length);

			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, 
					this.m_Version[0] + " " + this.m_Type[0] + " " + this.m_Cmd[0] + " " + this.m_Len[0] + this.m_Len[1]);
			
			return pos; // Only included Header Size
		} catch (Exception e) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Error in Parse Consensus Header cause " + e.getCause());
			return 0;
		}
	}
	
	// ParseBlkGenInfo Routine Can only occur from NNA to CN
	synchronized public boolean ParseBlkGenInfo(ChannelHandlerContext ctx) {
		if(this.global.getCTypeCast().ByteHexToDecimalInt(this.m_Len) != Cons_Cmd_BGI_field.getTotalValue()) return false;
		try {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Parse BlkGenInfo");
			byte[] StartTime = new byte[Cons_Cmd_BGI_field.StartTime.getValue()]; // milliseconds unit
			byte[] RoundCnt = new byte[Cons_Cmd_BGI_field.RoundCnt.getValue()];
			byte[] DelayTime = new byte[Cons_Cmd_BGI_field.DelayTime.getValue()]; // milliseconds unit
			byte[] FirstCNP2PAddr = new byte[Cons_Cmd_BGI_field.FirstCNP2PAddr.getValue()];
			byte[] FirstBN = new byte[Cons_Cmd_BGI_field.FirstBN.getValue()];
			byte[] TierID = new byte[Cons_Cmd_BGI_field.TierID.getValue()];
			int pos = 0;
			
			System.arraycopy(this.m_Msg, pos, StartTime, 0, StartTime.length); pos += StartTime.length;
			System.arraycopy(this.m_Msg, pos, RoundCnt, 0, RoundCnt.length); pos += RoundCnt.length;
			System.arraycopy(this.m_Msg, pos, DelayTime, 0, DelayTime.length); pos += DelayTime.length;
			System.arraycopy(this.m_Msg, pos, FirstCNP2PAddr, 0, FirstCNP2PAddr.length); pos += FirstCNP2PAddr.length;
			System.arraycopy(this.m_Msg, pos, FirstBN, 0, FirstBN.length); pos += FirstBN.length;
			System.arraycopy(this.m_Msg, pos, TierID, 0, TierID.length); pos += TierID.length;
			
			// Print function just for debug
			this.global.getCConsensus().BlkGenInfoPrint(StartTime, RoundCnt, DelayTime, FirstCNP2PAddr, FirstBN, TierID);
			
			//
			byte nodeRule[] = this.global.getCP2P().getMyNodeRule();
			// if MyNodeRule == "CN"
			// then Set Block Gen timer Using Start Time
			
			if (nodeRule[0] == this.global.getCP2P().getENodeType("CN"))
			{
				if (this.global.getCTypeCast().ByteArrayCompare(this.global.getCP2P().getMyP2PAddr(), FirstCNP2PAddr))
				{
					int delayTime = this.global.getCTypeCast().ByteArrayToInt(DelayTime);
					long startTime = this.global.getCTypeCast().ByteArrayToLong(StartTime);

					this.global.getCConsensus().setBN(FirstBN);

					// Block Generation Timer (timeout = delayTime [as milliseconds unit])
					this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Block Generation Timer Start");
					this.global.getCTaskTimer().BlkGenTimer(delayTime, delayTime, startTime);
				}
			}

			StartTime = null;
			RoundCnt = null;
			DelayTime = null;
			FirstCNP2PAddr = null;
			FirstBN = null;
			TierID = null;
			nodeRule = null;
			
			return true;
		} catch (Exception e) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Error in Parse BlockGenInfo cause " + e.getCause());
			return false;
		}
		
	}
	
	// ParseTx Routine Can only occur from NNA to CN
	synchronized public boolean ParseTx(ChannelHandlerContext ctx) {
		try {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Parse Tx");
			byte[] BN = new byte[Cons_Cmd_TX_field.BN.getValue()];
			byte[] TierID = new byte[Cons_Cmd_TX_field.TierID.getValue()];
			byte[] TxInfoCnt = new byte[Cons_Cmd_TX_field.TxInfoCnt.getValue()];
			int pos = 0;
					
			System.arraycopy(this.m_Msg, pos, BN, 0, BN.length); pos += BN.length;
			System.arraycopy(this.m_Msg, pos, TierID, 0, TierID.length); pos += TierID.length;
			System.arraycopy(this.m_Msg, pos, TxInfoCnt, 0, TxInfoCnt.length); pos += TxInfoCnt.length;
			
			int TxInfoListLen = TxInfoCnt[0] * Cons_Tx_Info_List.getTotalValue();
			byte[] TxInfoList = new byte[TxInfoListLen];
			System.arraycopy(this.m_Msg, pos, TxInfoList, 0, TxInfoListLen); pos += TxInfoList.length;
			
			// Print function just for debug
			this.global.getCConsensus().TxPrint(BN, TxInfoCnt, TxInfoList);

			// Receive Transactions 
			// Store Transactions into DB
			boolean ret = true;
			ret = this.global.getCDBHelper().InsertTransactionsToDB(BN, TxInfoCnt, TxInfoList);
			
			if(ret == true) {
				//
			} else {
				// TODO if fail to insert transactions data into DB 
				//error handling
			}

			byte[] dstAddr = this.global.getCP2P().getMyClusterRoot();
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Send TxAck to " + this.global.getCTypeCast().ByteHexToString(dstAddr));
			// Send TxAck to NNA
			this.consensus_tx.TxAck(ctx, dstAddr, BN, TxInfoCnt, TxInfoList);
			
			BN = null;
			TierID = null;
			TxInfoCnt = null;
			TxInfoList = null;
			dstAddr = null;
			
			return true;
		} catch (Exception e) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Error in Parse Tx cause " + e.getCause());
			return false;
		}
		
	}
	
	// ParseTxAck Routine Can only occur from CN to NNA
	synchronized public boolean ParseTxAck(ChannelHandlerContext ctx) {
		try {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Parse TxAck");
			byte[] BN = new byte[Cons_Cmd_TX_field.BN.getValue()];
			byte[] TxInfoCnt = new byte[Cons_Cmd_TX_field.TxInfoCnt.getValue()];
			// TODO TxInfoCnt variable
			TxInfoCnt[0] = 1;
			byte[] TxInfoList = new byte[Cons_Tx_Info_List.getTotalValue() * TxInfoCnt[0]];
			int pos = 0;
			
			System.arraycopy(this.m_Msg, pos, BN, 0, BN.length); pos += BN.length;
			System.arraycopy(this.m_Msg, pos, TxInfoCnt, 0, TxInfoCnt.length); pos += TxInfoCnt.length;
			System.arraycopy(this.m_Msg, pos, TxInfoList, 0, TxInfoList.length); pos += TxInfoList.length;
			
			this.global.getCConsensus().TxAckPrint(BN, TxInfoCnt, TxInfoList);

			// Receive Processing
			// Send TxAck to SCA
			this.contract_tx.TxAckToSCA(BN, TxInfoCnt[0], TxInfoList);

			BN = null;
			TxInfoCnt = null;
			TxInfoList = null;
			
			return true;
		} catch (Exception e) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Error in Parse TxAck cause " + e.getCause());
			return false;
		}
	}
	
	// ParseTxStopReq Routine Can only occur from CN to NNA
	// this Routine also can only occur after Block Gen Timer Stop
	synchronized public boolean ParseTxStopReq(ChannelHandlerContext ctx) {
		if(this.global.getCTypeCast().ByteHexToDecimalInt(this.m_Len) != Cons_Cmd_TX_STOP_field.getTotalValue()) return false;
		try {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Parse Tx StopReq");
			byte[] P2PAddr = new byte[Cons_Cmd_TX_STOP_field.P2PAddr.getValue()];
			byte[] BN = new byte[Cons_Cmd_TX_STOP_field.BN.getValue()];
			byte[] TierID = new byte[Cons_Cmd_TX_STOP_field.TierID.getValue()];
			int pos = 0;
			
			System.arraycopy(this.m_Msg, pos, P2PAddr, 0, P2PAddr.length); pos += P2PAddr.length;
			System.arraycopy(this.m_Msg, pos, BN, 0, BN.length); pos += BN.length;
			System.arraycopy(this.m_Msg, pos, TierID, 0, TierID.length); pos += BN.length;
			
			// Print function just for debug
			this.global.getCConsensus().TxStopReqPrint(P2PAddr, BN, TierID);

			// increase current Block Number
			// and Set Current Tx Receive CN is Next BlkGen CN
			this.global.getCConsensus().incBN();
			this.global.getCConsensus().setCurrTxCN(this.global.getCConsensus().getNextBlkGenCN());

			//
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Send TxStopRsp to " + this.global.getCTypeCast().ByteHexToString(P2PAddr));
			// Send TxStopRsp to CN
			this.consensus_tx.TxStopRsp(ctx, P2PAddr, BN, TierID);
			
			P2PAddr = null;
			BN = null;
			TierID = null;
			
			return true;
		} catch (Exception e) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Error in Parse Tx cause " + e.getCause());
			return false;
		}
	}
	
	// ParseTxStopRsp Routine Can only occur from NNA to CN
	synchronized public boolean ParseTxStopRsp(ChannelHandlerContext ctx) {
		if(this.global.getCTypeCast().ByteHexToDecimalInt(this.m_Len) != Cons_Cmd_TX_STOP_field.getTotalValue()) return false;
		try {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Parse TxStopRsp");
			byte[] P2PAddr = new byte[Cons_Cmd_TX_STOP_field.P2PAddr.getValue()];
			byte[] BN = new byte[Cons_Cmd_TX_STOP_field.BN.getValue()];
			byte[] TierID = new byte[Cons_Cmd_TX_STOP_field.TierID.getValue()];
			int pos = 0;
			
			System.arraycopy(this.m_Msg, pos, P2PAddr, 0, P2PAddr.length); pos += P2PAddr.length;
			System.arraycopy(this.m_Msg, pos, BN, 0, BN.length); pos += BN.length;
			System.arraycopy(this.m_Msg, pos, TierID, 0, TierID.length); pos += BN.length;
			
			// Print function just for debug
			this.global.getCConsensus().TxStopRspPrint(P2PAddr, BN, TierID);

			// Make Light Block Information (Block Generation)
			LightBlkInfo m_LightBlkInfo = new LightBlkInfo(this.global);
			m_LightBlkInfo.BN = this.global.getCConsensus().getBN();
			m_LightBlkInfo.TierID = this.global.getCConsensus().getTierID();
			m_LightBlkInfo.P2PAddr = this.global.getCP2P().getMyP2PAddr();
			m_LightBlkInfo.BlkGenTime = this.global.getCTimeStamp().getCurrentTimeStampBA();
			
			// check if this block is genesis block or not
			if (this.global.getCTypeCast().ByteArrayToLong(m_LightBlkInfo.BN) == 1L)
			{
				// if this block is a genesis block then set PrevBlkHash fill 0x00
				this.global.getCTypeCast().memset_ByteArray(m_LightBlkInfo.PrevBlkHash, (byte) 0x00);
			}
			else
			{
				// else this block is not a gensis block then get PrevBlkHash from Database
				byte[][] Res_prvBlk = this.global.getCDBHelper().GetPrevBlockContentsFromDB();
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Res_prvBlk : " + Res_prvBlk[0].length + " " + Res_prvBlk[1].length);
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "prvBlkHash " + this.global.getCTypeCast().ByteHexToString(Res_prvBlk[1]));
				
				System.arraycopy(Res_prvBlk[1], 0, m_LightBlkInfo.PrevBlkHash, 0, m_LightBlkInfo.PrevBlkHash.length);
				Res_prvBlk = null;
			}
			
			// Get Transaction DBKeys from DB for Enter the block and Current Block Data
			byte[][] Res_SelectTxXorAndCntToDB = this.global.getCDBHelper().SelectTxXorAndCntToDB(m_LightBlkInfo.BN);

			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, 
					"0 " + Res_SelectTxXorAndCntToDB[0].length + " 1 " + Res_SelectTxXorAndCntToDB[1].length 
					+ " 2 " + Res_SelectTxXorAndCntToDB[2].length + " 3 " + Res_SelectTxXorAndCntToDB[3].length + " 4 " + Res_SelectTxXorAndCntToDB[4].length);

			System.arraycopy(Res_SelectTxXorAndCntToDB[0], 0, m_LightBlkInfo.TxCnt, 0, m_LightBlkInfo.TxCnt.length);
			
			int DataSize = 0;

			DataSize = m_LightBlkInfo.BN.length;
			DataSize += m_LightBlkInfo.TierID.length;
			DataSize += m_LightBlkInfo.P2PAddr.length;
			DataSize += m_LightBlkInfo.PrevBlkHash.length;
			DataSize += m_LightBlkInfo.TxCnt.length;

			byte[] Data = new byte[DataSize+(Cons_Define.DBKey.getValue()+Cons_Define.Hash.getValue())];
			
			DataSize = 0;

			System.arraycopy(m_LightBlkInfo.BN, DataSize, Data, DataSize, m_LightBlkInfo.BN.length);
			DataSize += m_LightBlkInfo.BN.length;
			
			System.arraycopy(m_LightBlkInfo.TierID, 0, Data, DataSize, m_LightBlkInfo.TierID.length);
			DataSize += m_LightBlkInfo.TierID.length;
			
			System.arraycopy(m_LightBlkInfo.P2PAddr, 0, Data, DataSize, m_LightBlkInfo.P2PAddr.length);
			DataSize += m_LightBlkInfo.P2PAddr.length;
			
			System.arraycopy(m_LightBlkInfo.PrevBlkHash, 0, Data, DataSize, m_LightBlkInfo.PrevBlkHash.length);
			DataSize += m_LightBlkInfo.PrevBlkHash.length;
			
			System.arraycopy(m_LightBlkInfo.TxCnt, 0, Data, DataSize, m_LightBlkInfo.TxCnt.length);
			DataSize += m_LightBlkInfo.TxCnt.length;
			
			System.arraycopy(Res_SelectTxXorAndCntToDB[1], 0, Data, DataSize, Res_SelectTxXorAndCntToDB[1].length);

			System.arraycopy(this.global.getCTypeCast().HexStrToByteArray(this.global.getCCryptos().Sha256ByteToString(Data)), 0, 
								m_LightBlkInfo.CurrBlkHash, 0, m_LightBlkInfo.CurrBlkHash.length);
			
            this.global.getCKeyUtil().getSigner().setSigData(m_LightBlkInfo.CurrBlkHash);
			this.global.getCKeyUtil().doSignature();

			System.arraycopy(this.global.getCKeyUtil().getSigner().getSignerSerialize(), 0, m_LightBlkInfo.Sig, 0, m_LightBlkInfo.Sig.length);

			// DB Key List
			DBKeyLists m_DBKeyLists = new DBKeyLists(this.global);
			System.arraycopy(Res_SelectTxXorAndCntToDB[2], 0, m_DBKeyLists.FirstTxDBKey, 0, m_DBKeyLists.FirstTxDBKey.length);
			System.arraycopy(Res_SelectTxXorAndCntToDB[3], 0, m_DBKeyLists.LastTxDBKey, 0, m_DBKeyLists.LastTxDBKey.length);
			System.arraycopy(Res_SelectTxXorAndCntToDB[4], 0, m_DBKeyLists.DBKeyCnt, 0, m_DBKeyLists.DBKeyCnt.length);
			
			// 
			int DBKeyCountI = this.global.getCTypeCast().ByteArrayToInt(m_DBKeyLists.DBKeyCnt);
			if (DBKeyCountI > 0 )
			{
				int DBKeysLen = DBKeyCountI * Cons_Define.DBKey.getValue(); // 8 = DBKey Size
				m_DBKeyLists.DBKeys = new byte[DBKeysLen];
				System.arraycopy(Res_SelectTxXorAndCntToDB[5], 0, m_DBKeyLists.DBKeys, 0, DBKeysLen);
			}

			// Insert Generated Block into DB
			this.global.getCDBHelper().InsertBlockContentsToDB(m_LightBlkInfo);
			
			// Send Block Notification from CN
			this.consensus_tx.BlkNotiFromCN(m_LightBlkInfo, m_DBKeyLists);
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Send Block Notification from CN");
			
			m_LightBlkInfo = null;
			m_DBKeyLists = null;
			P2PAddr = null;
			BN = null;
			TierID = null;
			
			Res_SelectTxXorAndCntToDB = null;
			Data = null;
			
			return true;
		} catch (Exception e) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Error in Parse TxStopReq cause " + e.getCause());
			return false;
		}
	}
	
	// ParseBlkNotiFromCN Routine Can only occur from CN to NNA
	synchronized public boolean ParseBlkNotiFromCN(ChannelHandlerContext ctx) {
		try {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Parse BlkNotiFromCN");
			// General
			byte[] NextDelayTime = new byte[Cons_Cmd_BLK_NOTI_field.NextDelayTime.getValue()];
			byte[] NextSeqIdx = new byte[Cons_Cmd_BLK_NOTI_field.NextSeqIdx.getValue()];
			byte[] NextBlkGenCNP2PAddr = new byte[Cons_Cmd_BLK_NOTI_field.NextBlkGenCNP2PAddr.getValue()];
			byte[] NextBN = new byte[Cons_Cmd_BLK_NOTI_field.NextBN.getValue()];

			// Light Block Information
			LightBlkInfo m_LightBlkInfo = new LightBlkInfo(this.global);

			// DB Key List
			DBKeyLists m_DBKeyLists = new DBKeyLists(this.global);

			//
			int pos = 0;

			// Parsing Each Field
			System.arraycopy(this.m_Msg, pos, NextDelayTime, 0, NextDelayTime.length); pos += NextDelayTime.length;
			System.arraycopy(this.m_Msg, pos, NextSeqIdx, 0, NextSeqIdx.length); pos += NextSeqIdx.length;
			System.arraycopy(this.m_Msg, pos, NextBlkGenCNP2PAddr, 0, NextBlkGenCNP2PAddr.length); pos += NextBlkGenCNP2PAddr.length;
			System.arraycopy(this.m_Msg, pos, NextBN, 0, NextBN.length); pos += NextBN.length;

			//
			System.arraycopy(this.m_Msg, pos, m_LightBlkInfo.BN, 0, m_LightBlkInfo.BN.length); pos +=  m_LightBlkInfo.BN.length;
			System.arraycopy(this.m_Msg, pos, m_LightBlkInfo.TierID, 0, m_LightBlkInfo.TierID.length); pos +=  m_LightBlkInfo.TierID.length;
			System.arraycopy(this.m_Msg, pos, m_LightBlkInfo.P2PAddr, 0, m_LightBlkInfo.P2PAddr.length); pos += m_LightBlkInfo.P2PAddr.length;
			System.arraycopy(this.m_Msg, pos, m_LightBlkInfo.BlkGenTime, 0, m_LightBlkInfo.BlkGenTime.length); pos += m_LightBlkInfo.BlkGenTime.length;
			System.arraycopy(this.m_Msg, pos, m_LightBlkInfo.PrevBlkHash, 0, m_LightBlkInfo.PrevBlkHash.length); pos += m_LightBlkInfo.PrevBlkHash.length;
			System.arraycopy(this.m_Msg, pos, m_LightBlkInfo.TxCnt, 0, m_LightBlkInfo.TxCnt.length); pos += m_LightBlkInfo.TxCnt.length;
			System.arraycopy(this.m_Msg, pos, m_LightBlkInfo.CurrBlkHash, 0, m_LightBlkInfo.CurrBlkHash.length); pos += m_LightBlkInfo.CurrBlkHash.length;
			System.arraycopy(this.m_Msg, pos, m_LightBlkInfo.Sig, 0, m_LightBlkInfo.Sig.length); pos += m_LightBlkInfo.Sig.length;

			//
			System.arraycopy(this.m_Msg, pos, m_DBKeyLists.FirstTxDBKey, 0, m_DBKeyLists.FirstTxDBKey.length); pos += m_DBKeyLists.FirstTxDBKey.length;
			System.arraycopy(this.m_Msg, pos, m_DBKeyLists.LastTxDBKey, 0, m_DBKeyLists.LastTxDBKey.length); pos += m_DBKeyLists.LastTxDBKey.length;
			System.arraycopy(this.m_Msg, pos, m_DBKeyLists.DBKeyCnt, 0, m_DBKeyLists.DBKeyCnt.length); pos += m_DBKeyLists.DBKeyCnt.length;
			
			int DBKeyCountI = this.global.getCTypeCast().ByteArrayToInt(m_DBKeyLists.DBKeyCnt);
			if (DBKeyCountI > 0 )
			{
				int DBKeysLen = DBKeyCountI * Cons_Define.DBKey.getValue(); // 8 = DBKey Size
				m_DBKeyLists.DBKeys = new byte[DBKeysLen];
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, 
						"DBKeyCount " + this.global.getCTypeCast().ByteHexToString(m_DBKeyLists.DBKeyCnt));
				System.arraycopy(this.m_Msg, pos, m_DBKeyLists.DBKeys, 0, DBKeysLen);
			}

			// Print function just for debug
			this.global.getCConsensus().BlkNotiFromCNPrint(NextDelayTime, NextSeqIdx, NextBlkGenCNP2PAddr, NextBN, m_LightBlkInfo, m_DBKeyLists);

			// Insert Generated Block into DB
			this.global.getCDBHelper().InsertBlockContentsToDB(m_LightBlkInfo);
			
			// Set Block Generation Next CN in P2P Sub Network
			this.global.getCConsensus().setBlkGenCN();
			
			// Notification My nearest other NN
			byte[] nnP2PAddr = this.global.getCConsensus().getRRMyNextNNP2PAddr();
			// if nearest other NN is me
			if (this.global.getCTypeCast().ByteArrayCompare(this.global.getCP2P().getMyP2PAddr(), nnP2PAddr))
			{
				//
				this.global.getCDBHelper().InsertPrevBlockContentsToDB(m_LightBlkInfo);
				
				this.global.getCConsensus().setIsMySubNetCN(true);
				// Set General to send Block Notification from NN
				NextDelayTime = this.global.getCConsensus().getGenTime();
				NextSeqIdx = this.global.getCTypeCast().IntToByteArray(0);
				NextBlkGenCNP2PAddr = this.global.getCConsensus().getCurrBlkGenCN();
				NextBN = this.global.getCConsensus().getBN();
			}
			else
			{
				// more than 1 consensus group case TODO
				// not my cluster Block Generation turn
				this.global.getCConsensus().setIsMySubNetCN(false);
				// Set General to send Block Notification from NN
				NextDelayTime = this.global.getCConsensus().getGenTime();
				NextSeqIdx = this.global.getCTypeCast().IntToByteArray(0);
				this.global.getCTypeCast().memset_ByteArray(NextBlkGenCNP2PAddr, (byte)0x00);
				this.global.getCTypeCast().memset_ByteArray(NextBN, (byte)0x00);
			}
			
			// Send Block Notification from NN
			this.consensus_tx.BlkNotiFromNN(m_LightBlkInfo, m_DBKeyLists, NextDelayTime, NextSeqIdx, NextBlkGenCNP2PAddr, NextBN);
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Send Block Notification From NN");
			
			// Send Block Notification to SCA
			this.contract_tx.BlkNotiToSCA(m_LightBlkInfo.BN, m_LightBlkInfo.BlkGenTime, m_LightBlkInfo.TxCnt, m_LightBlkInfo.CurrBlkHash);
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Send Block Notification to SCA");
			
			m_LightBlkInfo = null;
			m_DBKeyLists = null;
			
			NextDelayTime = null;
			NextSeqIdx = null;
			NextBlkGenCNP2PAddr = null;
			NextBN = null;
			nnP2PAddr = null;
			
			return true;
		} catch (Exception e) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Error in ParseBlkNotiFromCN cause " + e.getCause());
			return false;
		}

	}
	
	// ParseBlkNotiFromCN Routine Can occur from NNA to other NNA or NNA to CN (just 1 consensus group case)
	synchronized public boolean ParseBlkNotiFromNN(ChannelHandlerContext ctx) {
		try {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Parse BlkNotiFromNN");
			// General
			byte[] NextDelayTime = new byte[Cons_Cmd_BLK_NOTI_field.NextDelayTime.getValue()];
			byte[] NextSeqIdx = new byte[Cons_Cmd_BLK_NOTI_field.NextSeqIdx.getValue()];
			byte[] NextBlkGenCNP2PAddr = new byte[Cons_Cmd_BLK_NOTI_field.NextBlkGenCNP2PAddr.getValue()];
			byte[] NextBN = new byte[Cons_Cmd_BLK_NOTI_field.NextBN.getValue()];

			// Light Block Information
			LightBlkInfo m_LightBlkInfo = new LightBlkInfo(this.global);
			
			// DB Key List
			DBKeyLists m_DBKeyLists = new DBKeyLists(this.global);

			//
			int pos = 0;

			// Parsing each Field
			System.arraycopy(this.m_Msg, pos, NextDelayTime, 0, NextDelayTime.length); pos += NextDelayTime.length;
			System.arraycopy(this.m_Msg, pos, NextSeqIdx, 0, NextSeqIdx.length); pos += NextSeqIdx.length;
			System.arraycopy(this.m_Msg, pos, NextBlkGenCNP2PAddr, 0, NextBlkGenCNP2PAddr.length); pos += NextBlkGenCNP2PAddr.length;
			System.arraycopy(this.m_Msg, pos, NextBN, 0, NextBN.length); pos += NextBN.length;

			//
			System.arraycopy(this.m_Msg, pos, m_LightBlkInfo.BN, 0, m_LightBlkInfo.BN.length); pos +=  m_LightBlkInfo.BN.length;
			System.arraycopy(this.m_Msg, pos, m_LightBlkInfo.TierID, 0, m_LightBlkInfo.TierID.length); pos +=  m_LightBlkInfo.TierID.length;
			System.arraycopy(this.m_Msg, pos, m_LightBlkInfo.P2PAddr, 0, m_LightBlkInfo.P2PAddr.length); pos += m_LightBlkInfo.P2PAddr.length;
			System.arraycopy(this.m_Msg, pos, m_LightBlkInfo.BlkGenTime, 0, m_LightBlkInfo.BlkGenTime.length); pos += m_LightBlkInfo.BlkGenTime.length;
			System.arraycopy(this.m_Msg, pos, m_LightBlkInfo.PrevBlkHash, 0, m_LightBlkInfo.PrevBlkHash.length); pos += m_LightBlkInfo.PrevBlkHash.length;
			System.arraycopy(this.m_Msg, pos, m_LightBlkInfo.TxCnt, 0, m_LightBlkInfo.TxCnt.length); pos += m_LightBlkInfo.TxCnt.length;
			System.arraycopy(this.m_Msg, pos, m_LightBlkInfo.CurrBlkHash, 0, m_LightBlkInfo.CurrBlkHash.length); pos += m_LightBlkInfo.CurrBlkHash.length;
			System.arraycopy(this.m_Msg, pos, m_LightBlkInfo.Sig, 0, m_LightBlkInfo.Sig.length); pos += m_LightBlkInfo.Sig.length;

			//
			System.arraycopy(this.m_Msg, pos, m_DBKeyLists.FirstTxDBKey, 0, m_DBKeyLists.FirstTxDBKey.length); pos += m_DBKeyLists.FirstTxDBKey.length;
			System.arraycopy(this.m_Msg, pos, m_DBKeyLists.LastTxDBKey, 0, m_DBKeyLists.LastTxDBKey.length); pos += m_DBKeyLists.LastTxDBKey.length;
			System.arraycopy(this.m_Msg, pos, m_DBKeyLists.DBKeyCnt, 0, m_DBKeyLists.DBKeyCnt.length); pos += m_DBKeyLists.DBKeyCnt.length;

			int DBKeyCountI = this.global.getCTypeCast().ByteArrayToInt(m_DBKeyLists.DBKeyCnt);
			if (DBKeyCountI > 0 )
			{
				int DBKeysLen = DBKeyCountI * Cons_Define.DBKey.getValue();
				m_DBKeyLists.DBKeys = new byte[DBKeysLen];
				System.arraycopy(this.m_Msg, pos, m_DBKeyLists.DBKeys, 0, DBKeysLen);
			}

			// Print function just for debug
			this.global.getCConsensus().BlkNotiFromNNPrint(NextDelayTime, NextSeqIdx, NextBlkGenCNP2PAddr, NextBN, m_LightBlkInfo, m_DBKeyLists);

			byte nodeRule[] = this.global.getCP2P().getMyNodeRule();
			// if 1 consensus group case CN Set Block Gen Timer
			if (nodeRule[0] == this.global.getCP2P().getENodeType("CN"))
			{
				if (this.global.getCTypeCast().ByteArrayCompare(this.global.getCP2P().getMyP2PAddr(), NextBlkGenCNP2PAddr))
				{
					int delayTime = this.global.getCTypeCast().ByteArrayToInt(NextDelayTime);

					// Insert Previous Block Contents
					this.m_PrivLightBlkInfo = m_LightBlkInfo;
					
					this.global.getCConsensus().setPrvBN(m_LightBlkInfo.BN);
					
					this.global.getCDBHelper().InsertPrevBlockContentsToDB(m_LightBlkInfo);

					//
					this.global.getCConsensus().setBN(NextBN);

					// Block Generation Timer (timeout = delayTime [as milliseconds unit])
					this.global.getCTaskTimer().BlkGenTimer(delayTime, delayTime, 0);
				}
			}
			// else more than 1 consensus group case TODO
			else if (nodeRule[0] == this.global.getCP2P().getENodeType("NN"))
			{
				// Insert Previous Block Contents
				this.global.getCDBHelper().InsertPrevBlockContentsToDB(m_LightBlkInfo);
				// MySubNet Group Generation block turn
//				this.global.getCConsensus().setIsMySubNetCN(true);
//				// Set PrevBN and CurrentBN 
//				this.global.getCConsensus().setPrvBN(m_LightBlkInfo.BN);
//				this.global.getCConsensus().setBN(NextBN);
//				
//				// Pass to MySubNet CurrentBlkGenCN
//				this.consensus_tx.BlkNotiFromNN(m_LightBlkInfo, m_DBKeyLists, NextDelayTime, NextSeqIdx, this.global.getCConsensus().getCurrBlkGenCN(), NextBN);
			}
			
			m_LightBlkInfo = null;
			m_DBKeyLists = null;
			
			NextDelayTime = null;
			NextSeqIdx = null;
			NextBlkGenCNP2PAddr = null;
			NextBN = null;
			nodeRule = null;
			
			return true;
		} catch (Exception e) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Error in BlkNotiFromNN casue " + e.getCause());
			return false;
		}
	}
}
