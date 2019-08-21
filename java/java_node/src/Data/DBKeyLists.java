package Data;

import Main.Global;

////////////////////////////////////////////////////////////////////////
// DBKeyLists class is Entity Class
// this class has DatabaseKeys(like transaction id in RDB Database) info
// FirstTxDBKey is first transaction's DBkey the block has
// LastTxDBKey is last transcation's DBKey the block has
// DBKeyCnt is Number of DBKeys
// DBKeys all data of DBKeys the block has
////////////////////////////////////////////////////////////////////////

public class DBKeyLists {
	private Global global;
	
	public byte[] FirstTxDBKey;
	public byte[] LastTxDBKey;
	public byte[] DBKeyCnt;
	public byte[] DBKeys;

	public DBKeyLists(Global global) 
	{
		this.global = global;

		this.FirstTxDBKey = new byte[this.global.getCConsensus().getDBKeyListFieldLen("FirstTxDBKey")];
		this.LastTxDBKey = new byte[this.global.getCConsensus().getDBKeyListFieldLen("LastTxDBKey")];
		this.DBKeyCnt = new byte[this.global.getCConsensus().getDBKeyListFieldLen("DBKeyCnt")];
		this.DBKeys = null;
	}
	
	public DBKeyLists(Global global, byte[] FirstTxDBKey, byte[] LastTxDBKey, byte[] DBKeyCnt, byte[] DBKeys) 
	{
		this.global = global;

		this.FirstTxDBKey = new byte[this.global.getCConsensus().getDBKeyListFieldLen("FirstTxDBKey")];
		this.LastTxDBKey = new byte[this.global.getCConsensus().getDBKeyListFieldLen("LastTxDBKey")];
		
		this.FirstTxDBKey = FirstTxDBKey;
		this.LastTxDBKey = LastTxDBKey;
		this.DBKeyCnt = DBKeyCnt;

		int DBKeyCountI = this.global.getCTypeCast().ByteArrayToInt(DBKeyCnt);
		if (DBKeyCountI > 0)
		{
			this.DBKeys = new byte[DBKeys.length];
			this.DBKeys = DBKeys;
		}
		else
		{
			this.DBKeys = null;
		}
	}
	
	@Override
	public String toString() {
		String DBKeyListsStr = "My DBKeyLists\n";
		DBKeyListsStr += "FirstTxDBKey : " + this.global.getCTypeCast().ByteHexToString(this.FirstTxDBKey);
		DBKeyListsStr += "\nLastTxDBKey : " + this.global.getCTypeCast().ByteHexToString(this.LastTxDBKey);
		DBKeyListsStr += "\nDBKeyCnt : " + this.global.getCTypeCast().ByteHexToString(this.DBKeyCnt);
		
		int DBKeyCountI = this.global.getCTypeCast().ByteArrayToInt(DBKeyCnt);
		if (DBKeyCountI > 0)
		{
			DBKeyListsStr += "\nDBKeys : " + this.global.getCTypeCast().ByteHexToString(this.DBKeys);
		}
		
		return DBKeyListsStr;
	}
	
}
