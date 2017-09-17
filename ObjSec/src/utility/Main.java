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
		
		 DHParameters                dhParams = new DHParameters(p, g);
         DHKeyGenerationParameters   params2 = new DHKeyGenerationParameters(new SecureRandom(), dhParams);
         DHKeyPairGenerator          kpGen = new DHKeyPairGenerator();

         kpGen.init(params2);
		
        //DHKeyPairGenerator kpGen = getDHKeyPairGenerator(g, p);

        //
        // generate first pair
        //
        AsymmetricCipherKeyPair     pair = kpGen.generateKeyPair();

        DHPublicKeyParameters       pu1 = (DHPublicKeyParameters)pair.getPublic();
        DHPrivateKeyParameters      pv1 = (DHPrivateKeyParameters)pair.getPrivate();
        
        BigInteger pu1Y = pu1.getY();
        pu1 = new DHPublicKeyParameters(pu1Y,dhParams);
        //
        // generate second pair
        //
        pair = gen.generateKeyPair();

        DHPublicKeyParameters       pu2 = (DHPublicKeyParameters)pair.getPublic();
        DHPrivateKeyParameters      pv2 = (DHPrivateKeyParameters)pair.getPrivate();

        //
        // two way
        //
        DHAgreement    e1 = new DHAgreement();
        DHAgreement    e2 = new DHAgreement();

        e1.init(pv1);
        e2.init(pv2);

        BigInteger  m1 = e1.calculateMessage();
        BigInteger  m2 = e2.calculateMessage();

        BigInteger   k1 = e1.calculateAgreement(pu2, m2);
        BigInteger   k2 = e2.calculateAgreement(pu1, m1);

        if (k1.equals(k2))
        {
            System.out.println("OK, keys are same");
		

	}
	}
        
	
	

}
