package Util;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;

import Main.Global;

public class KeyUtil {
	private Global global;
	private Signer C_Signer;
	private MyKey C_MyKey;
	
	public KeyUtil(Global global) {
		this.global = global;
		this.C_Signer = new Signer();
		this.C_MyKey = new MyKey();
	}
	
	// Set My PrivateKey and PublicKey Instance
	public void MyKeyInit() {
		Security.addProvider(new BouncyCastleProvider());
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "MyKeyInit");
		if (ReadPrivateKeyPemFile() && ReadPublicKeyPemFile()) {
			this.C_MyKey.setPrivateKey((ECPrivateKeyParameters)C_MyKey.getAsymmetricPriKeyParameter());
			this.C_MyKey.setPublicKey((ECPublicKeyParameters)C_MyKey.getAsymmetricPubKeyParameter());
			SetMyKey();
		}
	}
	
	protected boolean ReadPrivateKeyPemFile() {
		PemReader reader = null;
		PemObject pem = null;
		
		try {
			reader = new PemReader(Files.newBufferedReader(Paths.get(this.global.getCP2P().getMyPrivateKeyPath())));
			pem = reader.readPemObject();
			reader.close();
			this.C_MyKey.setAsymmetricPriKeyParameter(PrivateKeyFactory.createKey(pem.getContent()));
			reader = null;
			pem = null;
		} catch (IOException e) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Can't read Private Key Pem File");
			try {
				reader.close();
				reader = null;
				pem = null;
			} catch (IOException ie) {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Can't close PemReader");
			}
			return false;
		}
		return true;
	}
	
	// When using read My PulbicKeyPemFile
	protected boolean ReadPublicKeyPemFile() {
		PemReader reader = null;
		PemObject pem = null;
		
		try {
			reader = new PemReader(Files.newBufferedReader(Paths.get(this.global.getCP2P().getMyPublicKeyPath())));
			pem = reader.readPemObject();
			reader.close();
			this.C_MyKey.setAsymmetricPubKeyParameter(PublicKeyFactory.createKey(pem.getContent()));
			reader = null;
			pem = null;
			return true;
		} catch (IOException e) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Can't read Private Key Pem File");
			try {
				reader.close();
				reader = null;
				pem = null;
			} catch (IOException ie) {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Can't close PemReader");
			}
			return false;
		}
	}
 
	// When using read Peer PublicKeyPemFile
	protected boolean ReadPublicKeyPemFile(PeerPublicKey PeerPubKey, String PubKeyPath) {
		PemReader reader = null;
		PemObject pem = null;
		
		try {
			reader = new PemReader(Files.newBufferedReader(Paths.get(PubKeyPath)));
			pem = reader.readPemObject();
			reader.close();
			PeerPubKey.setAsymmetricPubKeyParameter(PublicKeyFactory.createKey(pem.getContent()));
			
			reader = null;
			pem = null;
			
			return true;
		} catch (IOException e) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Can't read Private Key Pem File");
			try {
				reader.close();
				reader = null;
				pem = null;
			} catch (IOException ie) {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Can't close PemReader");
			}
			return false;
		}
	}
	
	// For store Peer Public Key Pem File method
	public boolean WritePeerPublicKeyPemFile(byte[] CompPubkey, String PubKeyPath) throws NoSuchAlgorithmException, InvalidKeySpecException {		
		
		PemWriter writer = null;
		try {
			writer = new PemWriter(Files.newBufferedWriter(Paths.get(PubKeyPath)));
			writer.writeObject(new PemObject("PUBLIC KEY", getPublicKeyFromBytes(CompPubkey).getEncoded()));
			writer.close();
			writer = null;
			return true;
		} catch (IOException e) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Can't write Peer Public Key Pem File");
			try {
				writer.close();
				writer = null;
			} catch (IOException ie) {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Can't close PemWriter");
			}
			return false;
		} 
	}
	
	// Peer send public key Compressed ByteArray Form
	// For store Peer Public Key to Pem File Convert ByteArray to PublicKey Object
	public PublicKey getPublicKeyFromBytes(byte[] CompPubkey) throws NoSuchAlgorithmException, InvalidKeySpecException {
		ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec(this.global.getKeyAlgorithm());
		KeyFactory kf = KeyFactory.getInstance("ECDSA", new BouncyCastleProvider());
		ECNamedCurveSpec params = new ECNamedCurveSpec(this.global.getKeyAlgorithm(), spec.getCurve(), spec.getG(), spec.getN());
		java.security.spec.ECPoint point = ECPointUtil.decodePoint(params.getCurve(), CompPubkey);
		ECPublicKeySpec pubkeySpec = new ECPublicKeySpec(point, params);
		ECPublicKey pk = (ECPublicKey)kf.generatePublic(pubkeySpec);
		
		spec = null;
		kf = null;
		params = null;
		point = null;
		pubkeySpec = null;
	
		return pk;
	}
	
	// Change Public and Private AsymmetricKeyParameter to ByteArray Data
	public void SetMyKey() {
		X9ECParameters ec = SECNamedCurves.getByName(this.global.getKeyAlgorithm());
		org.bouncycastle.math.ec.ECPoint q = this.C_MyKey.getPublicKey().getQ();
		
		@SuppressWarnings("deprecation")
		org.bouncycastle.math.ec.ECPoint.AbstractFp fp 
			= new org.bouncycastle.math.ec.ECPoint.Fp(ec.getCurve(), q.getAffineXCoord(), q.getAffineYCoord(), false);
		
		byte[] compressed_pub = fp.getEncoded(true);
		this.C_MyKey.setPrivateKeyStr(this.C_MyKey.getPrivateKey().getD().toByteArray());
		this.C_MyKey.setPublicKeyStr(compressed_pub);
		
		compressed_pub = null;
		ec = null;
		q = null;
		fp = null;
	}
	
	// Change Peer Public AsymmetricKeyParameter to ByteArray Data
	public void SetPeerKey(PeerPublicKey peerKey) {
		X9ECParameters ec = SECNamedCurves.getByName(this.global.getKeyAlgorithm());
		org.bouncycastle.math.ec.ECPoint q = peerKey.getPublicKey().getQ();
		
		@SuppressWarnings("deprecation")
		org.bouncycastle.math.ec.ECPoint.AbstractFp fp
			= new org.bouncycastle.math.ec.ECPoint.Fp(ec.getCurve(), q.getAffineXCoord(), q.getAffineYCoord(), false);
		byte[] compressed_peer_pub = fp.getEncoded(true);
		peerKey.setPublicKeyStr(compressed_peer_pub);
		
		ec = null;
		q = null;
		fp = null;
		compressed_peer_pub = null;
	}
	
	// Do Signature using my Private Key
	public void doSignature() {
		ECDSASigner signer = new ECDSASigner();
		signer.init(true, this.C_MyKey.getPrivateKey());
		
		BigInteger[] signData = signer.generateSignature(this.C_Signer.getSigData());
		
		int sigLen;
		int sigRIdx = 0;
		int sigSIdx = 1;
		int pos1 = 0;
		int pos2 = 0;
		
		// SignR and SignS not always 32byte sometimes 31byte or 33byte
		// so checking 31byte or 33byte

		// Signature R
		sigLen = this.global.getCConsensus().getConsDefLen("SigR");
		pos1 = 0;
		pos2 = 0;
		byte[] SigR = new byte[sigLen];
		
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, 
				"SigR " + signData[sigRIdx].toByteArray().length + " " + SigR.length);
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, 
				"signData[0].toByteArray() " + this.global.getCTypeCast().ByteHexToString(signData[sigRIdx].toByteArray()));
		
		if (signData[sigRIdx].toByteArray().length < SigR.length)
		{
			sigLen = signData[sigRIdx].toByteArray().length;
			pos2 = SigR.length - signData[sigRIdx].toByteArray().length;
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_WARNING, "SigR length SMALL");
		}
		else if (signData[sigRIdx].toByteArray().length > SigR.length)
		{
			pos1 = signData[sigRIdx].toByteArray().length - SigR.length;
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_WARNING, "SigR length BIG");
		}
		else
		{
			//
		}
		System.arraycopy(signData[sigRIdx].toByteArray(), pos1, SigR, pos2, sigLen);

		// Signature S
		sigLen = this.global.getCConsensus().getConsDefLen("SigS");
		pos1 = 0;
		pos2 = 0;
		byte[] SigS = new byte[sigLen];
		
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, 
				"SigS " + signData[sigSIdx].toByteArray().length + " " + SigS.length);
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, 
				"signData[1].toByteArray() " + this.global.getCTypeCast().ByteHexToString(signData[sigSIdx].toByteArray()));
		
		if (signData[sigSIdx].toByteArray().length < SigS.length)
		{
			sigLen = signData[sigSIdx].toByteArray().length;
			pos2 = SigS.length - signData[sigSIdx].toByteArray().length;
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_WARNING, "SigS length SMALL");
		}
		else if (signData[sigSIdx].toByteArray().length > SigS.length)
		{
			pos1 = signData[sigSIdx].toByteArray().length - SigS.length;
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_WARNING, "SigS length BIG");
		}
		else
		{
			// Error
		}
		System.arraycopy(signData[sigSIdx].toByteArray(), pos1, SigS, pos2, sigLen);

		this.C_Signer.setSignerR(SigR);
		this.C_Signer.setSignerS(SigS);
		
		byte[] tmp = new byte[this.global.getCConsensus().getConsDefLen("Sig")];
		System.arraycopy(SigR, 0, tmp, 0, this.global.getCConsensus().getConsDefLen("SigR"));
		System.arraycopy(SigS, 0, tmp, this.global.getCConsensus().getConsDefLen("SigR"), this.global.getCConsensus().getConsDefLen("SigS"));
		
		signData = null;
		signer = null;
		SigR = null;
		SigS = null;
		this.C_Signer.setSignerSerialize(tmp);
		tmp = null;
	}
	
	// Actual Signature Verify method
	public boolean signatureVerify(byte[] PubKey, byte[] Data, byte[] SignR, byte[] SignS) {
		ECDSASigner verifier = new ECDSASigner();
		
		ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec(this.global.getKeyAlgorithm());
		ECDomainParameters domain = new ECDomainParameters(spec.getCurve(), spec.getG(), spec.getN());
		ECPublicKeyParameters publicKeyParams = new ECPublicKeyParameters(spec.getCurve().decodePoint(PubKey), domain);
		
		verifier.init(false, publicKeyParams);
		boolean ret = verifier.verifySignature(Data, new BigInteger(1, SignR), new BigInteger(1, SignS));
		spec = null;
		domain = null;
		publicKeyParams = null;
		verifier = null;
		
		return ret;
	}
	
	// get PeerPublicKeyPath and change Peer PublicKey Pem file to ByteArray
	public boolean PeerSignVerify(byte[] Data, byte[] SignR, byte[] SignS, String peerPublicKeyPath) {
		PeerPublicKey peerKey = new PeerPublicKey();
		boolean ret = false;
		if (ReadPublicKeyPemFile(peerKey, peerPublicKeyPath)) {
			peerKey.setPublicKey((ECPublicKeyParameters)peerKey.getAsymmetricPubKeyParameter());
			SetPeerKey(peerKey);
			ret = signatureVerify(peerKey.getPublicKeyStr(), Data, SignR, SignS);
			peerKey = null;
		} else {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Peer Pubkey SET Fail");
			peerKey = null;
			ret = false;
		}
		return ret;
	}
	
	public Signer getSigner() {
		return this.C_Signer;
	}

	public MyKey getCMyKey(){
		return this.C_MyKey;
	}
	
	// My Private and Public Key class
	public class MyKey {
		private AsymmetricKeyParameter m_AsymmetricPriKeyParameter = null;
		private AsymmetricKeyParameter m_AsymmetricPubKeyParameter = null;
		private ECPrivateKeyParameters m_PrivateKey = null;
		private ECPublicKeyParameters m_PulbicKey = null;
		private byte[] m_PrivateKeyStr = new byte[32];
		private byte[] m_PulbicKeyStr = new byte[33];
		
		public AsymmetricKeyParameter getAsymmetricPriKeyParameter() {
			return this.m_AsymmetricPriKeyParameter;
		}
		
		public void setAsymmetricPriKeyParameter(AsymmetricKeyParameter m_AsymmetricPriKeyParameter) {
			this.m_AsymmetricPriKeyParameter = m_AsymmetricPriKeyParameter;
		}
		
		public AsymmetricKeyParameter getAsymmetricPubKeyParameter() {
			return this.m_AsymmetricPubKeyParameter;
		}
		
		public void setAsymmetricPubKeyParameter(AsymmetricKeyParameter m_AsymmetricPubKeyParameter) {
			this.m_AsymmetricPubKeyParameter = m_AsymmetricPubKeyParameter;
		}
		
		public ECPrivateKeyParameters getPrivateKey() {
			return this.m_PrivateKey;
		}
		
		public void setPrivateKey(ECPrivateKeyParameters m_PrivateKey) {
			this.m_PrivateKey = m_PrivateKey;
		}
		
		public ECPublicKeyParameters getPublicKey() {
			return this.m_PulbicKey;
		}
		
		public void setPublicKey(ECPublicKeyParameters m_PublicKey) {
			this.m_PulbicKey = m_PublicKey;
		}
		
		public byte[] getPrivateKeyStr() {
			return this.m_PrivateKeyStr;
		}
		
		public void setPrivateKeyStr(byte[] m_PrivateKeyStr) {
			this.m_PrivateKeyStr = m_PrivateKeyStr;
		}

		// Compressed Public Key
		public byte[] getPublicKeyStr() {
			return this.m_PulbicKeyStr;
		}
		
		public void setPublicKeyStr(byte[] m_PublicKeyStr) {
			this.m_PulbicKeyStr = m_PublicKeyStr;
		}
	}
	
	// Peer PublicKey Class
	public class PeerPublicKey {
		private AsymmetricKeyParameter m_AsymmetricPubKeyParameter = null;
		private ECPublicKeyParameters m_PulbicKey = null;
		private byte[] m_PulbicKeyStr = new byte[33];
		
		public void PeerPublicKeyInit() {
			
		}
		
		public AsymmetricKeyParameter getAsymmetricPubKeyParameter() {
			return this.m_AsymmetricPubKeyParameter;
		}
		
		public void setAsymmetricPubKeyParameter(AsymmetricKeyParameter m_AsymmetricPubKeyParameter) {
			this.m_AsymmetricPubKeyParameter = m_AsymmetricPubKeyParameter;
		}
		
		public ECPublicKeyParameters getPublicKey() {
			return this.m_PulbicKey;
		}
		
		public void setPublicKey(ECPublicKeyParameters m_PublicKey) {
			this.m_PulbicKey = m_PublicKey;
		}

		// Compressed Public Key
		public byte[] getPublicKeyStr() {
			return this.m_PulbicKeyStr;
		}
		
		public void setPublicKeyStr(byte[] m_PublicKeyStr) {
			this.m_PulbicKeyStr = m_PublicKeyStr;
		}
	}
	
	// Signing Class
	public class Signer {
		private byte[] m_SigData;
		private byte[] m_SignerR = new byte[32];
		private byte[] m_SignerS = new byte[32];
		private byte[] m_Signer = new byte[64];
		
		public byte[] getSigData() {
			return this.m_SigData;
		}
		
		public void setSigData(byte[] m_SigData) {
			this.m_SigData = m_SigData;
		}

		public byte[] getSignerR() {
			return this.m_SignerR;
		}
		
		public void setSignerR(byte[] m_SignerR) {
			this.m_SignerR = m_SignerR;
		}
		
		public byte[] getSignerS() {
			return this.m_SignerS;
		}
		
		public void setSignerS(byte[] m_SignerS) {
			this.m_SignerS = m_SignerS;
		}
		public byte[] getSignerSerialize() {
			return m_Signer;
		}

		public void setSignerSerialize(byte[] m_Signer) {
			this.m_Signer = m_Signer;
		}
	}
}
