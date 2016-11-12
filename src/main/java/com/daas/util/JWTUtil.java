package com.daas.util;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

/**
 * Utility class to implement JWT
 * @author Vivek
 *
 */
public class JWTUtil {

	static String encodedKey;	
	static boolean keyGenerated = false;

	/**
	 * Generate secret Signing Key
	 */
	private	static void generateKey(){

		// create new key
		SecretKey secretKey = null;
		try {
			secretKey = KeyGenerator.getInstance("AES").generateKey();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		// get base64 encoded version of the key
		encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
		keyGenerated = true;
	}

	/**
	 * Get signed key, if generated
	 * @return secret key
	 */
	public String getKey(){

		if(!keyGenerated)
			return null;

		return encodedKey;

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
	private String createJWT(String id, String issuer, String subject, long ttlMillis) {

		//The JWT signature algorithm used to sign the token
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;

		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);

		//Sign our JWT with our ApiKey secret		
		if(!keyGenerated)
			return null;
		
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
		return builder.compact();
	}


	/**
	 * Decoding and validating the token
	 * @param jwt
	 * 				JWT token to validate
	 * @return true if valid, false if not.
	 */
	private boolean parseJWT(String jwt) {

		try{
			//This will throw an exception if it is not a signed JWS (as expected)
			Claims claims = Jwts.parser()         
					.setSigningKey(DatatypeConverter.parseBase64Binary(getKey()))
					.parseClaimsJws(jwt).getBody();

			// checks for claims??
			
			System.out.println("ID: " + claims.getId());
			System.out.println("Subject: " + claims.getSubject());
			System.out.println("Issuer: " + claims.getIssuer());
			System.out.println("Expiration: " + claims.getExpiration());

		} catch (SignatureException e) {
			//don't trust the JWT!
			return false;
		}
		
		return true;
		
	}

}
