package utility;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.agreement.DHAgreement;
//import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.generators.DHKeyPairGenerator;
//import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.DHKeyGenerationParameters;
import org.bouncycastle.crypto.params.DHParameters;
import org.bouncycastle.crypto.params.DHPrivateKeyParameters;
import org.bouncycastle.crypto.params.DHPublicKeyParameters;

public class Main {

	public static void main(String[] args) {
		
		DHKeyPairGenerator gen = new DHKeyPairGenerator();
		BigInteger p = BigInteger.probablePrime(2048, new Random());
		BigInteger g = BigInteger.probablePrime(2048, new Random());
		System.out.println(p);
		System.out.println(g);
		DHParameters DHparams = new DHParameters(p,g);
		DHKeyGenerationParameters params = new DHKeyGenerationParameters(new SecureRandom(), DHparams);
		gen.init(params);
		AsymmetricCipherKeyPair keyPair = gen.generateKeyPair();
		/*
		DHAgreement dha = new DHAgreement();
		dha.init(keyPair.getPrivate());
		BigInteger msg = dha.calculateMessage();
		*/
		
		DHPublicKeyParameters publParams = (DHPublicKeyParameters)keyPair.getPublic();
		DHPrivateKeyParameters privParams = (DHPrivateKeyParameters)keyPair.getPrivate();
		DHAgreement dha = new DHAgreement();
		dha.init(privParams);
		BigInteger msg = dha.calculateMessage();
		
		
		BigInteger y = publParams.getY();
		DHPublicKeyParameters gtr = new DHPublicKeyParameters(y,DHparams);
		
		
		//System.out.println("key: " + keyPair.getPublic());
		
		System.out.println("Hello World!");
		

	}

}
