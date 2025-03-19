package com.jadaptive.api.x509;
import java.io.IOException;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X962Parameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.math.ec.ECCurve;

public class ECKeyPairLoaderNoCurveName {

    public static KeyPair loadECKeyPair(PrivateKeyInfo privateKeyInfo) throws InvalidParameterSpecException, NoSuchAlgorithmException, InvalidKeySpecException, IOException {

        // Parse ECPrivateKeyParameters using BouncyCastle
        ECPrivateKeyParameters ecPrivateKeyParameters =
                (ECPrivateKeyParameters) PrivateKeyFactory.createKey(privateKeyInfo);

        BigInteger d = ecPrivateKeyParameters.getD();

        // Extract curve parameters directly from the privateKeyInfo
        X962Parameters params = X962Parameters.getInstance(privateKeyInfo.getPrivateKeyAlgorithm().getParameters());

        ECParameterSpec ecParameterSpec;
        if (params.isNamedCurve()) {
            ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) params.getParameters();
            String curveName = ECNamedCurveTable.getName(oid);
            
            if (curveName == null) {
                curveName = SECNamedCurves.getName(oid); // Try SEC Named Curves if not found
            }

            if (curveName == null) {
                throw new IllegalArgumentException("Unknown named curve OID: " + oid);
            }
            
            switch(curveName) {
            case "prime256v1":
            	curveName ="secp256r1";
            	break;
            case "prime384v1":
            	curveName = "secp384r1";
            	break;
            case "prime521v1":
            	curveName= "secp521r1";
            default:
            }
            // Now get standard JCE ECParameterSpec using curveName
            AlgorithmParameters algorithmParameters = AlgorithmParameters.getInstance("EC");
            algorithmParameters.init(new ECGenParameterSpec(curveName));
            ecParameterSpec = algorithmParameters.getParameterSpec(ECParameterSpec.class);
       
	        // Construct JCE PrivateKey
	        ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(d, ecParameterSpec);
	        KeyFactory keyFactory = KeyFactory.getInstance("EC");
	        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
	
	        // Derive Public Key using BouncyCastle EC arithmetic
	        org.bouncycastle.jce.spec.ECParameterSpec bcSpec =
	                org.bouncycastle.jce.ECNamedCurveTable.getParameterSpec(curveName);
	        org.bouncycastle.math.ec.ECPoint q = bcSpec.getG().multiply(d).normalize();
	
	        ECPoint w = new ECPoint(
	                q.getAffineXCoord().toBigInteger(),
	                q.getAffineYCoord().toBigInteger()
	        );
	
	        // Construct JCE PublicKey
	        ECPublicKeySpec pubSpec = new ECPublicKeySpec(w, ecParameterSpec);
	        PublicKey publicKey = keyFactory.generatePublic(pubSpec);
	
	        return new KeyPair(publicKey, privateKey);
        
        } else {
            throw new IllegalArgumentException("Only named curves are supported by this code");
        }

    }
}
