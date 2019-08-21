package Main;

import java.net.InetSocketAddress;
import java.util.Random;
import java.util.Scanner;
import java.util.Iterator;

import Consensus.Consensus_TX;
import Contract.Contract_TX;
import Data.DBKeyLists;
import Data.LightBlkInfo;
import P2P.P2P_TX;
import io.netty.channel.ChannelHandlerContext;

//////////////////////////////////////////////////////////////////////////
// Example Class is Test for Each Network Protocol CMD and Util Method  //
// This class is system console input one integer value from user       //
// then branch each cmd                                                 //
// all of Cmd Send Dummy Data for test                                  //
//////////////////////////////////////////////////////////////////////////

public class Example {
	private Global global;
	private Consensus_TX consensus_tx;
	private P2P_TX p2p_tx;
	private Contract_TX contract_tx;
	
	// Using Test SocketAddress
	// Setting Up your Test IP Address and Port
	// and Remove comment cmd number 11 and 13
	// Then you can test P2PJoinReq and P2PPubkeyNoti
	private InetSocketAddress test_sockAddress;
	
	public Example(Global global) {
		this.global = global;
		this.consensus_tx = new Consensus_TX(this.global);
		this.p2p_tx = new P2P_TX(this.global);
		this.contract_tx = new Contract_TX(this.global);
	}

	// Json
	public void testParseRRNet(){
		this.global.ParseRRNetJson();
		this.global.getCConsensus().rrNetInit();
	}

	public void testParseRRSubNet(){
		this.global.ParseRRSubNetJson();
		this.global.getCConsensus().rrSubNetInit();
	}

	// P2P
	public void testP2PJoinReq(){
		ChannelHandlerContext ctx = this.global.getCP2P().getSockCtxs().get(test_sockAddress);
		byte[] dstAddr = this.global.getCTypeCast().HexStrToByteArray("0001020304050607");
		
		this.p2p_tx.P2PJoinReq(ctx, dstAddr);
		dstAddr = null;
	}

	public void testP2PPubkeyNoti(){
		ChannelHandlerContext ctx = this.global.getCP2P().getSockCtxs().get(test_sockAddress);
		byte[] dstAddr = this.global.getCTypeCast().HexStrToByteArray("0001020304050607");
		
		this.p2p_tx.P2PPubkeyNoti(ctx, dstAddr);
		dstAddr = null;
	}

	// Consensus
	public void testConsBlkGenInfo(){
		this.consensus_tx.BlkGenInfo();
	}
	
	public void testConsTx(){
		byte[] dbKey = this.global.getCTypeCast().HexStrToByteArray("0011000011112222");
		byte[] hash = this.global.getCTypeCast().HexStrToByteArray("354D54BDC0B9D94A841CFDD0BD25B9C61B603FBE47D95ECBF8B445614AAC559D");
		
		this.consensus_tx.Tx(dbKey, hash);
		dbKey = null;
		hash = null;
	}

	public void testConsTxStopReq(){
		this.consensus_tx.TxStopReq();
	}

	public void testConsBlkNotiFromCN(){
		// Light Block Information
		LightBlkInfo m_LightBlkInfo = new LightBlkInfo(this.global);
		m_LightBlkInfo.BN = this.global.getCConsensus().getBN();
		m_LightBlkInfo.TierID = this.global.getCConsensus().getTierID();
		m_LightBlkInfo.P2PAddr = this.global.getCConsensus().getCurrBlkGenCN();
		m_LightBlkInfo.BlkGenTime = this.global.getCTimeStamp().getCurrentTimeStampBA();
		this.global.getCTypeCast().memset_ByteArray(m_LightBlkInfo.PrevBlkHash, (byte) 0x1D);
		m_LightBlkInfo.TxCnt = this.global.getCTypeCast().IntToByteArray(0);
		this.global.getCTypeCast().memset_ByteArray(m_LightBlkInfo.CurrBlkHash, (byte) 0x2E);
		this.global.getCTypeCast().memset_ByteArray(m_LightBlkInfo.Sig, (byte) 0x3F);
		
		// DB Key List
		DBKeyLists m_DBKeyLists = new DBKeyLists(this.global);
		m_DBKeyLists.FirstTxDBKey = this.global.getCTypeCast().LongToByteArray(0x0000200030004001L);
		m_DBKeyLists.LastTxDBKey = this.global.getCTypeCast().LongToByteArray(0x1000200030004004L);
		m_DBKeyLists.DBKeyCnt = this.global.getCTypeCast().IntToByteArray(0);
		
		this.consensus_tx.BlkNotiFromCN(m_LightBlkInfo, m_DBKeyLists);
		m_LightBlkInfo = null;
		m_DBKeyLists = null;
	}

	public void testConsBlkNotiFromNN(){
		// Consensus BLK_NOTI_FROM_CN
		byte[] NextDelayTime = new byte[this.global.getCConsensus().getBlkNotiFieldLen("NextDelayTime")];
		byte[] NextSeqIdx = new byte[this.global.getCConsensus().getBlkNotiFieldLen("NextSeqIdx")];
		byte[] NextBlkGenCNP2PAddr = new byte[this.global.getCConsensus().getBlkNotiFieldLen("NextBlkGenCNP2PAddr")];
		byte[] NextBN = new byte[this.global.getCConsensus().getBlkNotiFieldLen("NextBN")];

		NextDelayTime = this.global.getCConsensus().getGenTime();
		NextSeqIdx = this.global.getCTypeCast().IntToByteArray(0);
		NextBlkGenCNP2PAddr = this.global.getCConsensus().getCurrBlkGenCN();
		NextBN = this.global.getCConsensus().getBN();
		
		// Light Block Information
		LightBlkInfo m_LightBlkInfo = new LightBlkInfo(this.global);
		m_LightBlkInfo.BN = this.global.getCConsensus().getBN();
		m_LightBlkInfo.TierID = this.global.getCConsensus().getTierID();
		m_LightBlkInfo.P2PAddr = this.global.getCConsensus().getCurrBlkGenCN();
		m_LightBlkInfo.BlkGenTime = this.global.getCTimeStamp().getCurrentTimeStampBA();
		this.global.getCTypeCast().memset_ByteArray(m_LightBlkInfo.PrevBlkHash, (byte) 0x1D);
		m_LightBlkInfo.TxCnt = this.global.getCTypeCast().IntToByteArray(0);
		this.global.getCTypeCast().memset_ByteArray(m_LightBlkInfo.CurrBlkHash, (byte) 0x2E);
		this.global.getCTypeCast().memset_ByteArray(m_LightBlkInfo.Sig, (byte) 0x3F);
		
		// DB Key List
		DBKeyLists m_DBKeyLists = new DBKeyLists(this.global);
		m_DBKeyLists.FirstTxDBKey = this.global.getCTypeCast().LongToByteArray(0x1000200030004001L);
		m_DBKeyLists.LastTxDBKey = this.global.getCTypeCast().LongToByteArray(0x1000200030004004L);
		m_DBKeyLists.DBKeyCnt = this.global.getCTypeCast().IntToByteArray(0);
		
		this.consensus_tx.BlkNotiFromNN(m_LightBlkInfo, m_DBKeyLists, NextDelayTime, NextSeqIdx, NextBlkGenCNP2PAddr, NextBN);
		m_LightBlkInfo = null;
		m_DBKeyLists = null;
	}

	public void testContTxAckToSCA(){
		Random rd = new Random();

		byte[] random_BN = new byte[this.global.getCConsensus().getConsDefLen("BN")];
		byte TxInfoCnt;
		byte[] TxInfoList = new byte[this.global.getCConsensus().getConsDefLen("DBKeyHash")];
		
		rd.nextBytes(random_BN);
		TxInfoCnt = 1;
		rd.nextBytes(TxInfoList);
		
		this.contract_tx.TxAckToSCA(random_BN, TxInfoCnt, TxInfoList);
		random_BN = null;
		TxInfoList = null;
	}
	
	public void testContBlkNotiToSCA(){
		Random rd = new Random();

		byte[] random_BN = new byte[this.global.getCConsensus().getConsDefLen("BN")];
		byte[] random_blkGenTime = new byte[this.global.getCConsensus().getBlkNotiFieldLen("BlkGenTime")];
		byte[] random_txCnt = new byte[this.global.getCConsensus().getLightBlkFieldLen("TxCnt")];
		byte[] random_hash = new byte[this.global.getCConsensus().getConsDefLen("Hash")];
		
		rd.nextBytes(random_BN);
		rd.nextBytes(random_blkGenTime);
		rd.nextBytes(random_txCnt);
		rd.nextBytes(random_hash);
		
		this.contract_tx.BlkNotiToSCA(random_BN, random_blkGenTime, random_txCnt, random_hash);
		random_BN = null;
		random_blkGenTime = null;
		random_txCnt = null;
		random_hash = null;
	}

	// Timer
	public void testBlockGenTimer(){
		this.global.getCTaskTimer().BlkGenTimer(0, 1000, 0);
	}

	public void testTxTimer() {
		this.global.getCTaskTimer().TxTimer(0, 200);
	}

	// DB
	public void testDBTruncateTables(){
		this.global.getCDBHelper().TruncateTables();
	}
	
	public void testSetBlkGenTime(int sec) {
		int tmp = sec * 1000;
		this.global.getCConsensus().setGenTime(this.global.getCTypeCast().IntToByteArray(tmp));
	}
	
	public void testCloseAllSocket() {
		Iterator<InetSocketAddress> keys = this.global.getCP2P().getSockCtxs().keySet().iterator();
		int itCnt = 1;
		while( keys.hasNext() ) {
			System.out.println("Iterate Count : " + Integer.toString(itCnt));
			InetSocketAddress key = keys.next();
			this.global.getCP2P().getSockCtxs().get(key).channel().closeFuture();
			this.global.getCP2P().getSockCtxs().remove(key);
			
			System.out.println("Close : " + key +"'s Socket");
		}
	}
	
	public void testCloseTCPClient() {
		
	}

	//
	public void startExample() {
		@SuppressWarnings("resource")
		Scanner input = new Scanner(System.in);
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Test Thread Start");
		int type = 0;
		int sec = 0;
		
		while(true) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Enter Eample Request No");
			type = input.nextInt();
			switch(type) {
			// json
			case 0:
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Yout Input : " + type + " is testParseRRNet()");
				this.testParseRRNet();
				break;
			case 1:
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Yout Input : " + type + " is testParseRRSubNet()");
				this.testParseRRSubNet();
				break;
			// P2P
			case 11:
				// Not Use
				// this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Yout Input : " + type + " is testP2PJoinReq()");
				// this.testP2PJoinReq();
				break;
			case 12:
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Yout Input : " + type + " is testP2PJoinResponse()");
				//this.testP2PJoinResponse();
				break;
			case 13:
				// Not Use
				//this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Yout Input : " + type + " is testP2PPubkeyNoti()");
				//this.testP2PPubkeyNoti();
				break;
			// Consensus
			case 21:
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Yout Input : " + type + " is testConsBlkGenInfo()");
				this.testConsBlkGenInfo();
				break;
			case 22:
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Yout Input : " + type + " is testConsTx()");
				this.testConsTx();
				break;
			case 23:
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Yout Input : " + type + " is testConsTxStopReq()");
				this.testConsTxStopReq();
				break;
			case 24:
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Yout Input : " + type + " is testConsBlkNotiFromCN()");
				this.testConsBlkNotiFromCN();
				break;
			case 25:
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Yout Input : " + type + " is testConsBlkNotiFromCN()");
				this.testConsBlkNotiFromNN();
				break;
			case 31:
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Yout Input : " + type + " is testContBlkNotiToSCA()");
				this.testContBlkNotiToSCA();
				break;
			case 32:
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Yout Input : " + type + " is testContBlkNotiToSCA()");
				this.testContTxAckToSCA();
				break;
			// Timer
			case 51:
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Yout Input : " + type + " is testBlockGenTimer()");
				this.testBlockGenTimer();
				break;
			case 52:
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Yout Input : " + type + " is testTxTimer()");
				this.testTxTimer();
				break;
			// DB
			case 61:
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Yout Input : " + type + " is testDBTruncateTables()");
				this.testDBTruncateTables();
				break;
			case 71:
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Please Enter BlkGenTime (Seconds)");
				sec = input.nextInt();
				this.testSetBlkGenTime(sec);
				break;
			case 81:
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Input : " + type + " is testCloseAllSocket()");
				this.testCloseAllSocket();
				break;
			default:
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Yout Input : " + type + " is Non Type of Test Set");
			}
		}
	}
}
