package objSec;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
//import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.generators.DHKeyPairGenerator;
import org.bouncycastle.crypto.params.DHKeyGenerationParameters;
import org.bouncycastle.crypto.params.DHParameters;

public class Main {

	public static void main(String[] args) {
		/*
		DHKeyPairGenerator gen = new DHKeyPairGenerator();
		BigInteger p = BigInteger.probablePrime(4096, new Random());
		BigInteger g = BigInteger.probablePrime(4096, new Random());
		DHParameters DHparams = new DHParameters(p,g);
		DHKeyGenerationParameters params = new DHKeyGenerationParameters(new SecureRandom(), DHparams);
		gen.init(params);
		AsymmetricCipherKeyPair keyPair = gen.generateKeyPair();
		
		System.out.println(keyPair.toString());
		
		System.out.println("Hello World!");
		*/

	}

}
