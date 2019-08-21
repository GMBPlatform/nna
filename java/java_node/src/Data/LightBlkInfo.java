package Data;

import Main.Global;

////////////////////////////////////////////////////////////////////////
// LighBlkInfo class is Entity Class
// this class has LighBlk contents info
// BN : BlockNumber
// TierID : MN's tierID
// p2pAddr : MN's P2PAddress
// BlkGenTime : Block Generation Time
// PrevBlkHash : Previous Block Hash 
// TxCnt : Transaction count the block has
// CurrBlkHash : Hash of the block
// Sig : secp256r1 Signature of MN 
////////////////////////////////////////////////////////////////////////

public class LightBlkInfo {
	private Global global;
	
	public byte[] BN;
	public byte[] TierID;
	public byte[] P2PAddr;
	public byte[] BlkGenTime;
	public byte[] PrevBlkHash;
	public byte[] TxCnt;
	public byte[] CurrBlkHash;
	public byte[] Sig;

	public LightBlkInfo(Global global) 
	{
		this.global = global;

		this.BN = new byte[this.global.getCConsensus().getLightBlkFieldLen("BN")];
		this.TierID = new byte[this.global.getCConsensus().getLightBlkFieldLen("TierID")];
		this.P2PAddr = new byte[this.global.getCConsensus().getLightBlkFieldLen("P2PAddr")];
		this.BlkGenTime = new byte[this.global.getCConsensus().getLightBlkFieldLen("BlkGenTime")];
		this.PrevBlkHash = new byte[this.global.getCConsensus().getLightBlkFieldLen("PrevBlkHash")];
		this.TxCnt = new byte[this.global.getCConsensus().getLightBlkFieldLen("TxCnt")];
		this.CurrBlkHash = new byte[this.global.getCConsensus().getLightBlkFieldLen("CurrBlkHash")];
		this.Sig = new byte[this.global.getCConsensus().getLightBlkFieldLen("Sig")];
	}
						
	public LightBlkInfo(Global global, byte[] BN, byte[] TierID, byte[] P2PAddr, byte[] BlkGenTime,
						byte[] PrevBlkHash, byte[] TxCnt, byte[] CurrBlkHash, byte[] Sig) 
	{
		this.global = global;

		this.BN = new byte[this.global.getCConsensus().getLightBlkFieldLen("BN")];
		this.TierID = new byte[this.global.getCConsensus().getLightBlkFieldLen("TierID")];
		this.P2PAddr = new byte[this.global.getCConsensus().getLightBlkFieldLen("P2PAddr")];
		this.BlkGenTime = new byte[this.global.getCConsensus().getLightBlkFieldLen("BlkGenTime")];
		this.PrevBlkHash = new byte[this.global.getCConsensus().getLightBlkFieldLen("PrevBlkHash")];
		this.TxCnt = new byte[this.global.getCConsensus().getLightBlkFieldLen("TxCnt")];
		this.CurrBlkHash = new byte[this.global.getCConsensus().getLightBlkFieldLen("CurrBlkHash")];
		this.Sig = new byte[this.global.getCConsensus().getLightBlkFieldLen("Sig")];
		
		this.BN = BN;
		this.TierID = TierID;
		this.P2PAddr = P2PAddr;
		this.BlkGenTime = BlkGenTime;
		this.PrevBlkHash = PrevBlkHash;
		this.TxCnt = TxCnt;
		this.CurrBlkHash = CurrBlkHash;
		this.Sig = Sig;
	}
	
	@Override
	public String toString() {
		String LightBlkInfoStr = "My LightBlkInfo\n";
		LightBlkInfoStr += "BN : " + this.global.getCTypeCast().ByteHexToString(this.BN);
		LightBlkInfoStr += "\nTierID : " + this.global.getCTypeCast().ByteHexToString(this.TierID);
		LightBlkInfoStr += "\nP2PAddress : " + this.global.getCTypeCast().ByteHexToString(this.P2PAddr);
		LightBlkInfoStr += "\nBlkGenTime : " + this.global.getCTypeCast().ByteArrayToLong(this.BlkGenTime);
		LightBlkInfoStr += "\nPreviousBlkHash : " + this.global.getCTypeCast().ByteHexToString(this.PrevBlkHash);
		LightBlkInfoStr += "\nTransactionCount : " + this.global.getCTypeCast().ByteHexToString(this.TxCnt);
		LightBlkInfoStr += "\nCurrentBlckHash : " + this.global.getCTypeCast().ByteHexToString(this.CurrBlkHash);
		LightBlkInfoStr += "\nSignature : " + this.global.getCTypeCast().ByteHexToString(this.Sig) + "\n";
		
		return LightBlkInfoStr;
	}
}
