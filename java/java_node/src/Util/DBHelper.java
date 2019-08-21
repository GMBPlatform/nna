package Util;

import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import Data.LightBlkInfo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;

import Main.Global;

@SuppressWarnings("unchecked")
enum DB_Field_Length {
	TXLen(40), HashLen(32), DBKeyLen(8);
	
	private int value;
	@SuppressWarnings("rawtypes")
	private static Map map = new HashMap<>();
	
	private DB_Field_Length(int value) {
		this.value = value;
	}
	
	static {
		for(DB_Field_Length dbFLen : DB_Field_Length.values()) {
			map.put(dbFLen.value, dbFLen);
		}
	}
	
	public static DB_Field_Length valueOf(int dbFLen) {
		return (DB_Field_Length) map.get(dbFLen);
	}
	
	public int getValue() {
		return value;
	}
}

public class DBHelper {
	private Global global;
	
	// databaseUrl = "jdbc:mysql://Hostname or Ip//Database Name
	private String databaseUrl;
	// user = Database UserName
	private String user;
	// password = Database User's Password
	private String password;
	private Connection conn = null;
	
	public DBHelper(Global global) {
		this.global = global; 
		
		this.databaseUrl = System.getenv("DBURL");
		this.user = System.getenv("DBUser");
		this.password = System.getenv("DBPassword");
	}
		
	public boolean DBConnectionCreate() {
		try {
			// get Database connection
			this.conn = DriverManager.getConnection(this.databaseUrl, this.user, this.password);
			
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Database Connection Success");
			return true;
		} 
		catch (SQLException se) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Database Connection Fail cause " + se.getCause());
			return false;
		}		
	}
	
	// Checking Database And Tables
	public boolean DBinit() {
		ResultSet rs = null;
		ResultSet rsTxTable = null;
		ResultSet rsInfoTable = null;
		ResultSet rsContentsTable = null;
		ResultSet rsPrevContentsTable = null;
		
		boolean isDatabaseExist = false;
		boolean isTableExist = true;
		boolean ret = false;
		
		try {
			rs = this.conn.getMetaData().getCatalogs();
			while(rs.next()) {
				if(rs.getString(1).equals("block")) isDatabaseExist = true;		
			}

			if(isDatabaseExist) {
				// Database Already Exist -> Check Each Table Exist -> Create Table or Truncate Tables
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "Database Already Exist Do TRUNCATE Routine");
				this.conn.setCatalog("block");

				// Transactions
				rsTxTable = this.conn.getMetaData().getTables(null, null, "tx", null);
				if(rsTxTable.next()) ;//isTableExist = isTableExist && TruncateTable("tx");
				else isTableExist = isTableExist && CreateTxTable();

				// Block Information
				rsInfoTable = this.conn.getMetaData().getTables(null, null, "info", null);
				if(rsInfoTable.next());//isTableExist = isTableExist && TruncateTable("info");
				else isTableExist = isTableExist && CreateInfoTable();

				// Block Contents
				rsContentsTable = this.conn.getMetaData().getTables(null, null, "contents", null);
				if(rsContentsTable.next()) ;//isTableExist = isTableExist && TruncateTable("contents");
				else isTableExist = isTableExist && CreateContentsTable();
				
				// Previous Block Contents
				rsPrevContentsTable = this.conn.getMetaData().getTables(null, null, "prev_contents", null);
				if(rsPrevContentsTable.next()) ;//isTableExist = isTableExist && TruncateTable("prev_contents");
				else isTableExist = isTableExist && CreatePrevContentsTable();
				
				ret = isTableExist;
			} else {
				// Database Not Exist
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Create New `block` Database");
				ret = CreateDatabase();
			}
		} catch(SQLException se) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "SQLException in DBinit cause " + se.getCause());

			ret = false;
		} finally {
			try {
				// close each resultset
				if(rs != null && !rs.isClosed()) rs.close();
				if(rsTxTable != null && !rsTxTable.isClosed()) rsTxTable.close();
				if(rsInfoTable != null && !rsInfoTable.isClosed()) rsInfoTable.close();
				if(rsContentsTable != null && !rsContentsTable.isClosed()) rsContentsTable.close();
				if(rsPrevContentsTable != null && !rsPrevContentsTable.isClosed()) rsPrevContentsTable.close();
			} catch (SQLException se) {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Fail to close ResultSet in DBinit cause " + se.getCause());
			}
		}
		return ret;
	}
	
	// Truncate Table
	public boolean TruncateTable(String TableName) {
		String sql = "TRUNCATE ";
		Statement stmt = null;
		boolean ret = false;
		
		try {
			sql += TableName;
			stmt = this.conn.createStatement();
			stmt.executeUpdate(sql);
			ret = true;
		} catch (SQLException se) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "SQLException in TruncateTable cause " + se.getCause());
			ret = false;
		} finally {
			try {
				// close Statement
				if(stmt != null && stmt.isClosed()) stmt.close();
			} catch (SQLException se) {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Fail to Close Statement in TruncateTable cause " + se.getCause());
			}
		}
		return ret;
	}

	// Truncate Tables
	public boolean TruncateTables() {
		ResultSet rs = null;
		ResultSet rsTxTable = null;
		ResultSet rsInfoTable = null;
		ResultSet rsContentsTable = null;
		ResultSet rsPrevContentsTable = null;
		
		boolean isDatabaseExist = false;
		boolean isTableExist = true;
		boolean ret = false;
		
		try {
			rs = this.conn.getMetaData().getCatalogs();
			while(rs.next()) {
				if(rs.getString(1).equals("block")) isDatabaseExist = true;		
			}

			if(isDatabaseExist) {
				// Database Already Exist -> Check Each Table Exist -> Create Table or Truncate Tables
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "`block` Database Already Exist");
				this.conn.setCatalog("block");

				// Transactions
				rsTxTable = this.conn.getMetaData().getTables(null, null, "tx", null);
				if(rsTxTable.next()) isTableExist = isTableExist && TruncateTable("tx");

				// Block Information
				rsInfoTable = this.conn.getMetaData().getTables(null, null, "info", null);
				if(rsInfoTable.next()) isTableExist = isTableExist && TruncateTable("info");

				// Block Contents
				rsContentsTable = this.conn.getMetaData().getTables(null, null, "contents", null);
				if(rsContentsTable.next()) isTableExist = isTableExist && TruncateTable("contents");

				// Previous Block Contents
				rsPrevContentsTable = this.conn.getMetaData().getTables(null, null, "prev_contents", null);
				if(rsPrevContentsTable.next()) isTableExist = isTableExist && TruncateTable("prev_contents");
				
				ret = isTableExist;
			} else {
				ret = false;
			}
		} catch(SQLException se) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "SQLException in TruncateTables cause " + se.getCause());
			ret = false;
		} finally {
			try {
				// close each ResultSet
				if(rs != null && rs.isClosed()) rs.close();
				if(rsTxTable != null && rsTxTable.isClosed()) rsTxTable.close();
				if(rsInfoTable != null && rsInfoTable.isClosed()) rsInfoTable.close();
				if(rsContentsTable != null && rsContentsTable.isClosed()) rsContentsTable.close();
				if(rsPrevContentsTable != null && rsPrevContentsTable.isClosed()) rsPrevContentsTable.close();
			} catch (SQLException se) {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Fail to close ResultSet in TruncateTables cause " + se.getCause());
			}
		}
		return ret;
	}
	
	// Create Block Database
	public boolean CreateDatabase() {
		String sql = "CREATE DATABASE block";
		Statement stmt = null;
		boolean ret = false;
		
		try {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Create 'block` Database");
			stmt = this.conn.createStatement();
			stmt.executeUpdate(sql);
			
			// set Catalog Using block Database
			this.conn.setCatalog("block");
			ret = CreateTxTable() && CreateInfoTable() && CreateContentsTable() && CreatePrevContentsTable();
		} catch (SQLException se) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "SQLException in CreateDatabase cause " + se.getCause());
			ret = false;
		} finally {
			try {
				// close Statement
				if(stmt != null && !stmt.isClosed()) stmt.close();
			} catch (SQLException se) {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Fail to Statement close in CreateDatabase cause " +se.getCause());
			}
		}
		sql = null;
		return ret;
	}
	
	// Create tx Table
	public boolean CreateTxTable() {
		String sql = "CREATE TABLE `tx`(";
				sql += "`block_num` bigint(20) unsigned zerofill NOT NULL DEFAULT '00000000000000000000' COMMENT 'BLOCK Number',";
				sql += "`db_key` bigint(20) unsigned zerofill NOT NULL DEFAULT '00000000000000000000' COMMENT 'DB KEY',";
				sql += "`hash` text NOT NULL COMMENT 'Transaction Hash',";
				sql += "PRIMARY KEY (`db_key`)";
				sql += ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Transactions'";
		Statement stmt = null;
		boolean ret = false;
		
		try {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Create TX Table");
			stmt = this.conn.createStatement();
			stmt.executeUpdate(sql);
			ret = true;
		} catch (SQLException se) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "SQLException in Create 'tx' table cause " + se.getCause());
			ret = false;
		} finally {
			try {
				// close statement
				if(stmt != null && stmt.isClosed()) stmt.close();
			} catch (SQLException se) {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Fail to Close Statement in Create 'tx' table cause " + se.getCause());
			}
		}
		sql = null;
		return ret;
	}
	
	// Create block info Table
	public boolean CreateInfoTable() {
		String sql = "CREATE TABLE `info` (";
				sql += "`block_num` bigint(20) unsigned zerofill NOT NULL DEFAULT '00000000000000000000' COMMENT 'Block Number',";
				sql += "`status` tinyint(3) unsigned zerofill NOT NULL DEFAULT '000' COMMENT 'Block Status',";
				sql += "`block_cfm_time` bigint(20) unsigned zerofill DEFAULT '00000000000000000000' COMMENT 'Block Confirm Time',";
				sql += "PRIMARY KEY (`block_num`)";
				sql += ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Block status Information'";
		Statement stmt = null;
		boolean ret = false;
		
		try {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Create 'info' Table");
			stmt = this.conn.createStatement();
			stmt.executeUpdate(sql);
			ret = true;
		} catch (SQLException se) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "SQLException in Create 'info' table cause " + se.getCause());
			ret = false;
		} finally {
			try {
				// close statement
				if(stmt != null && stmt.isClosed()) stmt.close();
			} catch (SQLException se) {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Fail to Close Statement in Create 'info' table cause " + se.getCause());
			}
		}
		sql = null;
		return ret;
	}
	
	// Create block contents Table
	public boolean CreateContentsTable() {
		String sql = "CREATE TABLE `contents` (";
				sql += "`block_num` bigint(20) unsigned zerofill NOT NULL DEFAULT '00000000000000000000' COMMENT 'Block Number',";
				sql += "`tier_id` tinyint(3) unsigned zerofill NOT NULL DEFAULT '000' COMMENT 'BP TierID',";
				sql += "`p2p_addr` bigint(20) unsigned zerofill NOT NULL DEFAULT '00000000000000000000' COMMENT 'BP P2PAddress',";
				sql += "`block_gen_time` bigint(20) unsigned zerofill NOT NULL DEFAULT '00000000000000000000' COMMENT 'Block Generation Time',";
				sql += "`prev_block_hash` text NOT NULL COMMENT 'Previous Block Hash',";
				sql += "`tx_count` int(11) unsigned zerofill NOT NULL DEFAULT '00000000000' COMMENT 'Number of transaction the block has',";
				sql += "`hash` text NOT NULL COMMENT 'Block Hash',";
				sql += "`signature` text NOT NULL COMMENT 'Signature of BP',";
				sql += "PRIMARY KEY (`block_num`)";
				sql += ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Block Contents'";
			Statement stmt = null;
			boolean ret = false;
			
			try {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Create 'contents' Table");
				stmt = this.conn.createStatement();
				stmt.executeUpdate(sql);
				ret = true;
			} catch (SQLException se) {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "SQLException in Create 'contents' table cause " + se.getCause());
				ret = false;
			} finally {
				try {
					// close statement
					if(stmt != null && stmt.isClosed()) stmt.close();
				} catch (SQLException se) {
					this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Fail to Close Statement in Create 'contents' table cause " + se.getCause());
				}
			}
			sql = null;
			return ret;
	}

	// Create previous block contents Table
	public boolean CreatePrevContentsTable() {
		String sql = "CREATE TABLE `prev_contents` (";
				sql += "`block_num` bigint(20) unsigned zerofill NOT NULL DEFAULT '00000000000000000000' COMMENT 'Block Number',";
				sql += "`tier_id` tinyint(3) unsigned zerofill NOT NULL DEFAULT '000' COMMENT 'BP TierID',";
				sql += "`p2p_addr` bigint(20) unsigned zerofill NOT NULL DEFAULT '00000000000000000000' COMMENT 'BP P2PAddress',";
				sql += "`block_gen_time` bigint(20) unsigned zerofill NOT NULL DEFAULT '00000000000000000000' COMMENT 'Block Generation Time',";
				sql += "`prev_block_hash` text NOT NULL COMMENT 'Previous Block Hash',";
				sql += "`tx_count` int(11) unsigned zerofill NOT NULL DEFAULT '00000000000' COMMENT 'Number of transaction the block has',";
				sql += "`hash` text NOT NULL COMMENT 'Block Hash',";
				sql += "`signature` text NOT NULL COMMENT 'Signature of BP',";
				sql += "PRIMARY KEY (`block_num`)";
				sql += ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Block Contents'";
			Statement stmt = null;
			boolean ret = false;
			
			try {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Create 'Prev contents' Table");
				stmt = this.conn.createStatement();
				stmt.executeUpdate(sql);
				ret = true;
			} catch (SQLException se) {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "SQLException in Create 'Prev contents' table cause " + se.getCause());
				ret = false;
			} finally {
				try {
					// close statement
					if(stmt != null && stmt.isClosed()) stmt.close();
				} catch (SQLException se) {
					this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Fail to Close Statement in Create 'Prev contents' table cause " + se.getCause());
				}
			}
			sql = null;
			return ret;
	}
	
	// Transaction Data Insert Routine
	// InsertTransaction Routine occur when SCA pass to NNA Transaction db_key and hash
	// Between SCA and NNA communication format is JSON
	// So, just using String data of JSON value
	public boolean InsertTransactionToDB(String block_num, String db_key, String hash) {
		// Using PreparedStatement
		String sql = "INSERT INTO tx VALUES(?,?,?)";
		PreparedStatement pstmt = null;
		boolean ret = false;
		
		try {
			pstmt = this.conn.prepareStatement(sql);
			
			pstmt.setString(1, block_num);
			pstmt.setString(2, db_key);
			pstmt.setString(3, hash);
		
			int count = pstmt.executeUpdate();
			if(count == 0) {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Fail to Insert Transaction Data");
				ret = false;
			} else {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Success to Insert Transaction Data");
				ret = true;
			}
		} catch (SQLException se) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "SQLException in Insert Transaction cause " + se.getCause());
			ret = false;
		} finally {
			try {
				// close PreparedStatement
				if(pstmt != null && !pstmt.isClosed()) pstmt.close();
			} catch (SQLException se) {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Fail to Close PreparedStatement in InsertTx cause " + se.getCause());
			}
		}
		sql = null;
		return ret;
	}
	
	// InsertTransactionsToDB Routine occur when NNA pass to CN One or more Transaction data
	// Parsing One or more each Transaction data and Insert
	public boolean InsertTransactionsToDB(byte[] block_num, byte[] TxInfoCnt, byte[] TxInfoList) {
		// Using PreparedStatement
		// For insert One or more Transaction data 
		// Using Batch PreparedStatement method
		String sql = "INSERT INTO tx VALUES(?,?,?)";
		PreparedStatement pstmt = null;
		boolean ret = false;
		
		try {
			if(TxInfoList.length % DB_Field_Length.TXLen.getValue() != 0) {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "TxInfoList Length is Error");
				return false;
			}
			
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "InsertTransactions to DB");
			pstmt = this.conn.prepareStatement(sql);
			int txInfoCnt = this.global.getCTypeCast().ByteToUnsignedInt(TxInfoCnt[0]);
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "txInfoCnt : " + txInfoCnt);
			
			int pos = 0;
			
			for(int i = 0; i < txInfoCnt; i++) {
				byte[] db_key = new byte[DB_Field_Length.DBKeyLen.getValue()];
				byte[] hash = new byte[DB_Field_Length.HashLen.getValue()];
				
				System.arraycopy(TxInfoList, pos, db_key, 0, DB_Field_Length.DBKeyLen.getValue()); pos += DB_Field_Length.DBKeyLen.getValue();
				System.arraycopy(TxInfoList, pos, hash, 0, DB_Field_Length.HashLen.getValue()); pos += DB_Field_Length.HashLen.getValue();
				
				pstmt.setString(1, Long.toUnsignedString(this.global.getCTypeCast().ByteArrayToLong(block_num)));
				pstmt.setString(2, Long.toUnsignedString(this.global.getCTypeCast().ByteArrayToLong(db_key)));
				pstmt.setString(3, this.global.getCTypeCast().ByteHexToString(hash));
				pstmt.addBatch();
				pstmt.clearParameters();
			}
			
			pstmt.executeBatch();
			ret = true;
		} catch (SQLException se) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "SQLException in Insert Transactions cause " + se.getCause());
			ret = false;
		} finally {
			try {
				// close PreparedStatement
				if(pstmt != null && !pstmt.isClosed()) pstmt.close();
			} catch (SQLException se) {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Fail to Close PreparedStatement in InsertTxS cause " + se.getCause());
			}
		}
		sql = null;
		return ret;
	}
	
	// Block Info Data Insert Routine
	// Is currently not using this method cause using about block status not yet 
	public boolean InsertBlockInfoToDB(byte[] block_num, byte status) {
		// Using PreparedStatement
		String sql = "INSERT INTO info(block_num, status) VALUES(?,?)";
		PreparedStatement pstmt = null;
		boolean ret = false;
		
		try {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Insert Block Info Data to DB");
			pstmt = this.conn.prepareStatement(sql);
		
			pstmt.setString(1, Long.toUnsignedString(this.global.getCTypeCast().ByteArrayToLong(block_num)));
			pstmt.setInt(2, this.global.getCTypeCast().ByteToUnsignedInt(status));
			
			int count = pstmt.executeUpdate();
			if(count == 0) {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Fail to Insert Block Info Data");
				ret = false;
			} else {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Success to Insert Block Info Data");
				ret = true;
			}
		} catch (SQLException se) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "SQLException in Insert Block Info cause " + se.getCause());
			ret = false;
		} finally {
			try {
				// close PreparedStatement
				if(pstmt != null && !pstmt.isClosed()) pstmt.close();
			} catch (SQLException se) {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Fail to Close PreparedStatement in Insert Block Info cause " + se.getCause());
			}
		}
		sql = null;
		return ret;
	}
	
	// Block Contents Data Insert Routine
	// Insert LightBlkInfo
	public boolean InsertBlockContentsToDB(LightBlkInfo m_LightBlkInfo) 
	{
		// Using PreparedStatement
		String sql = "INSERT INTO contents VALUES(?,?,?,?,?,?,?,?)";
		PreparedStatement pstmt = null;
		boolean ret;
		
		try {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Insert Block Contents Data to DB");
			pstmt = this.conn.prepareStatement(sql);
			
			pstmt.setString(1, Long.toUnsignedString(this.global.getCTypeCast().ByteArrayToLong(m_LightBlkInfo.BN)));
			pstmt.setInt(2, this.global.getCTypeCast().ByteToUnsignedInt(m_LightBlkInfo.TierID[0]));
			pstmt.setString(3, Long.toUnsignedString(this.global.getCTypeCast().ByteArrayToLong(m_LightBlkInfo.P2PAddr)));
			pstmt.setString(4, Long.toUnsignedString(this.global.getCTypeCast().ByteArrayToLong(m_LightBlkInfo.BlkGenTime)));
			pstmt.setString(5, this.global.getCTypeCast().ByteHexToString(m_LightBlkInfo.PrevBlkHash));
			pstmt.setString(6, Integer.toUnsignedString(this.global.getCTypeCast().ByteArrayToInt(m_LightBlkInfo.TxCnt)));
			pstmt.setString(7, this.global.getCTypeCast().ByteHexToString(m_LightBlkInfo.CurrBlkHash));
			pstmt.setString(8, this.global.getCTypeCast().ByteHexToString(m_LightBlkInfo.Sig));
			
			int count = pstmt.executeUpdate();
			if(count == 0) {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Fail to Insert Block Contents Data");
				ret = false;
			} else {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Success to Insert Block Contents Data");
				ret = true;
			}
		} catch (SQLException se) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "SQLException in Insert Block Contents cause " + se.getCause());
			ret = false;
		} finally {
			try {
				// close PreparedStatement
				if(pstmt != null && !pstmt.isClosed()) pstmt.close();
			} catch (SQLException se) {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Fail to Close PreparedStatement in Insert Block Contents cause " + se.getCause());
			}
		}
		sql = null;
		return ret;
	}

	// Previous Block Contents Data Insert Routine
	// Insert Previous block LightBlkInfo
	public boolean InsertPrevBlockContentsToDB(LightBlkInfo m_LightBlkInfo) 
	{
		// Using PreparedStatement
		String sql = "INSERT INTO prev_contents VALUES(?,?,?,?,?,?,?,?)";
		PreparedStatement pstmt = null;
		boolean ret = false;
		
		try {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Insert Prev Block Contents Data to DB");
			pstmt = this.conn.prepareStatement(sql);
			
			pstmt.setString(1, Long.toUnsignedString(this.global.getCTypeCast().ByteArrayToLong(m_LightBlkInfo.BN)));
			pstmt.setInt(2, this.global.getCTypeCast().ByteToUnsignedInt(m_LightBlkInfo.TierID[0]));
			pstmt.setString(3, Long.toUnsignedString(this.global.getCTypeCast().ByteArrayToLong(m_LightBlkInfo.P2PAddr)));
			pstmt.setString(4, Long.toUnsignedString(this.global.getCTypeCast().ByteArrayToLong(m_LightBlkInfo.BlkGenTime)));
			pstmt.setString(5, this.global.getCTypeCast().ByteHexToString(m_LightBlkInfo.PrevBlkHash));
			pstmt.setString(6, Integer.toUnsignedString(this.global.getCTypeCast().ByteArrayToInt(m_LightBlkInfo.TxCnt)));
			pstmt.setString(7, this.global.getCTypeCast().ByteHexToString(m_LightBlkInfo.CurrBlkHash));
			pstmt.setString(8, this.global.getCTypeCast().ByteHexToString(m_LightBlkInfo.Sig));
			
			int count = pstmt.executeUpdate();
			if(count == 0) {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Fail to Insert Prev Block Contents Data");
				ret = false;
			} else {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Success to Insert Prev Block Contents Data");
				ret = true;
			}
		} catch (SQLException se) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "SQLException in Insert Prev Block Contents cause " + se.getCause());
			ret = false;
		} finally {
			try {
				// close PreparedStatement
				if(pstmt != null && !pstmt.isClosed()) pstmt.close();
			} catch (SQLException se) {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Fail to Close PreparedStatement in Insert Prev Block Contents cause " + se.getCause());
			}
		}
		sql = null;
		return ret;
	}
	
	// get prev block blockNumber and hash
	public byte[][] GetPrevBlockContentsFromDB() 
	{
		ResultSet rs = null;
		byte[][] res = new byte[2][];

		String sql = "SELECT block_num, hash FROM prev_contents ORDER BY block_num DESC LIMIT 1";

		PreparedStatement pstmt = null;
		
		try {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "GetPrevBlockContents From DB");
			res[0] = null;
			res[1] = null;
		
			pstmt = this.conn.prepareStatement(sql);

			rs = pstmt.executeQuery();

			while(rs.next()) {
				res[0] = this.global.getCTypeCast().LongToByteArray(this.global.getCTypeCast().StringToLong(rs.getString(1)));
				res[1] = this.global.getCTypeCast().HexStrToByteArray(rs.getString(2));
				
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, 
						"prvblock_num1 " + this.global.getCTypeCast().ByteHexToString(res[0]));
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, 
						"prvBlkHash1 " + this.global.getCTypeCast().ByteHexToString(res[1]));
			}

			// if have not Previous Block Info
			// that mean when genesis block create
			if (res[0] == null)
			{
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "rs.next() NONE");
				
				res[0] = new byte[this.global.getCConsensus().getLightBlkFieldLen("BN")];
				res[1] = new byte[this.global.getCConsensus().getLightBlkFieldLen("CurrBlkHash")];
			}
		}
		catch (SQLException se) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "SQLException in Select Prev Block Contents cause " + se.getCause());
		} finally {
			try {
				// close PreparedStatement
				if(pstmt != null && !pstmt.isClosed()) pstmt.close();
				if(rs != null && !rs.isClosed()) rs.close();
			} catch (SQLException se) {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Fail to Close PreparedStatement in Select Prev Block Contents cause " + se.getCause());
			}
		}
		sql = null;
		return res;
	}
	
	// SelectTxXorAndCntToDB Routine call when block generation
	public byte[][] SelectTxXorAndCntToDB(byte[] blk_num) 
	{
		ResultSet rs = null;
		byte[][] res = new byte[6][];
		List<Long> TxList = new ArrayList<Long>();
		
		// Select all transactions db_key and hash the #block_num block has
		String sql = "SELECT db_key,hash FROM tx WHERE `block_num` = (?)";

		PreparedStatement pstmt = null;
		
		try {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "SelectTxXorAndCnt To DB");
			pstmt = this.conn.prepareStatement(sql);
						
			pstmt.setBigDecimal(1,  new BigDecimal(this.global.getCTypeCast().byteArrayToBigInteger(blk_num)));

			rs = pstmt.executeQuery();
			// for counting number of Tx
			int cnt = 0;
			byte[] Txor = new byte[this.global.getCConsensus().getConsDefLen("DBKeyHash")];
			while(rs.next()) {
				
				cnt++;
				long long_tmp = this.global.getCTypeCast().StringToLong(rs.getString(1));
				TxList.add(long_tmp);
				byte[] db_key = this.global.getCTypeCast().LongToByteArray(long_tmp);
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, 
						"SelectTxXorAndCntToDB db_key.length : " + db_key.length);
				byte[] hash = rs.getString(2).getBytes();
				
				byte[] xor_data = new byte[this.global.getCConsensus().getConsDefLen("DBKeyHash")];

				System.arraycopy(db_key, 0, xor_data, 0, this.global.getCConsensus().getConsDefLen("DBKey"));
				System.arraycopy(hash, 0, xor_data, this.global.getCConsensus().getConsDefLen("DBKey"), this.global.getCConsensus().getConsDefLen("Hash"));
				
				// Calculate Txor for using generate block hash
				Txor = this.global.getCTypeCast().xor(Txor , xor_data);
				db_key = null;
				hash = null;
				xor_data = null;
			}
			Collections.sort(TxList);
			
			res[0] = new byte[4];
			res[0] = this.global.getCTypeCast().IntToByteArray(cnt);
			res[1] = new byte[this.global.getCConsensus().getConsDefLen("DBKeyHash")];
			res[1] = Txor;
			
			
			res[2] = new byte[this.global.getCConsensus().getConsDefLen("DBKey")];
			res[3] = new byte[this.global.getCConsensus().getConsDefLen("DBKey")];
			res[4] = new byte[this.global.getCConsensus().getDBKeyListFieldLen("DBKeyCnt")];
			
			if(TxList.size() > 0)
			{
				res[2] = this.global.getCTypeCast().LongToByteArray(TxList.get(0));
				res[3] = this.global.getCTypeCast().LongToByteArray(TxList.get(cnt-1));
				res[4] = this.global.getCTypeCast().IntToByteArray(cnt);//cnt
				res[5] = this.global.getCTypeCast().LongListToByteArray(TxList);
			}
			Txor = null;
		}
		catch (SQLException se) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "SQLException in Select Block Contents cause " + se.getCause());
			this.global.getCTypeCast().memset_ByteArray(res[0],(byte)0x0);
			this.global.getCTypeCast().memset_ByteArray(res[1],(byte)0x0);
		} finally {
			try {
				// close PreparedStatement
				if(pstmt != null && !pstmt.isClosed()) pstmt.close();
				// close ResultSet
				if(rs != null && !rs.isClosed()) rs.close();
			} catch (SQLException se) {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Fail to Close PreparedStatement in Select Block Contents cause " + se.getCause());
				se.printStackTrace();
			}
		}
		TxList.clear();
		TxList = null;
		sql = null;
		return res;
	}
	// DataBase Connection Close Routine
	public boolean DBConnectionClose() {
		try {
			if(this.conn != null && !conn.isClosed()) {
				conn.close();
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Database Connection Close Success");
				return true;
			} else {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, 
						"Connection Close Fail Because connection is already close or connection is not exist");
				return false;
			}
		} catch (SQLException se) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Database Connection Close Fail cause " + se.getCause());
			return false;
		}
	}
	
//	public void CloseStatement(Statement stmt) {
//		try {
//			if(stmt != null && !stmt.isClosed()) stmt.close();
//		} catch(SQLException se) {
//			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Can't close SQL Statement");
//		}
//	}
//	
//	public void ClosePreparedStatement(PreparedStatement pstmt) {
//		try {
//			if(pstmt != null && !pstmt.isClosed()) pstmt.close();
//		} catch (SQLException se) {
//			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Can't close SQL PreparedStatement");
//		}
//	}
//	
//	public void CloseResultSet(ResultSet rs) {
//		try {
//			if(rs != null && !rs.isClosed()) rs.close();
//		} catch (SQLException se) {
//			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Can't close SQL ResultSet");
//		}
//	}
	
	public Connection getDBConnection() {
		return this.conn;
	}
}
