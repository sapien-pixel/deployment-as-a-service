package com.daas.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daas.aws.common.AmazonEC2Common;
import com.daas.common.ConfFactory;

/**
 * Utility class to implement JWT
 * @author Vivek
 *
 */
public class JWTUtil {

	private static Logger log = LoggerFactory.getLogger(JWTUtil.class.getName());

	/**
	 * Generate secret Signing Key
	 * Write this to jwt.secret.key file
	 */
	public static void generateKey(){

		// create new key
		SecretKey secretKey = null;
		try {
			secretKey = KeyGenerator.getInstance("AES").generateKey();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		// get base64 encoded version of the key
		String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());		
		System.out.println(encodedKey);
	}

	/**
	 * Get signed key, if generated
	 * @return secret key
	 */
	public static String getKey(){

		return ConfFactory.getPrivateConf().getString("jwt.secret.key");
	}


	/**
	 * Creates a JWT with registered {@link Claim}
	 * The token is signed using the HMAC using SHA-512 algorithm
	 * and finally compacted into its String form.
	 * @param id
	 * 						ID
	 * @param issuer
	 * 						Issuer of token
	 * @param subject
	 * 						Subject of token. Usually a user identifier
	 * @param ttlMillis
	 * 						Token expiry		
	 * @return JWT in compacted form
	 */
	public static String createJWT(String id, String issuer, String subject, long ttlMillis) {

		log.info("Creating JWT for subject - "+ subject+" with id - "+ id);
		
		//The JWT signature algorithm used to sign the token
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;

		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);

		//Sign our JWT with our ApiKey secret

		byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(getKey());
		Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

		//Set the JWT Claims
		JwtBuilder builder = Jwts.builder().setId(id)
				.setIssuedAt(now)
				.setSubject(subject)
				.setIssuer(issuer)
				.signWith(signatureAlgorithm, signingKey);

		//if specified, add the expiration
		if (ttlMillis >= 0) {
			long expMillis = nowMillis + ttlMillis;
			Date exp = new Date(expMillis);
			builder.setExpiration(exp);
		}

		//Builds the JWT and serializes it to a compact, URL-safe string
		log.info("Created JWT for subject - "+ subject+" with id - "+ id);
		return builder.compact();
	}


	/**
	 *	Decoding and validating the token
	 * @param jwt
	 * 				JWT token to validate
	 * @return true if valid, false if not.
	 */
	public static boolean parseJWT(String jwt) {

		log.info("Validating JWT - "+ jwt);

		try{
			//This will throw an exception if it is not a signed JWS (as expected)
			Claims claims = Jwts.parser()
					.setSigningKey(DatatypeConverter.parseBase64Binary(getKey()))
					.parseClaimsJws(jwt).getBody();

		} catch (SignatureException e) {
			//don't trust the JWT!
			log.warn("Not a valid JWT..");
			return false;
		}
		log.info("Successfully validated the JWT - "+ jwt);
		return true;
	}

}
