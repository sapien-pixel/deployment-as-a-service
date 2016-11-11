package com.daas.kubernetes.utils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;

public class CertificateUtils {
	public static void trustEveryone() { 
		try { 	
			URL url = new URL("https://35.161.227.115");
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){ 
				public boolean verify(String hostname, SSLSession session) { 
					return true; 
				}}); 
			SSLContext context = SSLContext.getInstance("TLS"); 
			context.init(null, new X509TrustManager[]{new X509TrustManager(){ 
				public void checkClientTrusted(X509Certificate[] chain, 
						String authType) throws CertificateException {} 
				public void checkServerTrusted(X509Certificate[] chain, 
						String authType) throws CertificateException {} 
				public X509Certificate[] getAcceptedIssuers() { 
					return new X509Certificate[0]; 
				}}}, new SecureRandom()); 
			HttpsURLConnection.setDefaultSSLSocketFactory( 
					context.getSocketFactory()); 
			HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
			print_https_cert(con);
		} catch (Exception e) { 
			e.printStackTrace(); 
		} 
	}

	private static InputStream fullStream ( String fname ) throws IOException {
		FileInputStream fis = new FileInputStream(fname);
		DataInputStream dis = new DataInputStream(fis);
		byte[] bytes = new byte[dis.available()];
		dis.readFully(bytes);
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		return bais;
	}

	public static void print_https_cert(HttpsURLConnection con) throws KeyStoreException, NoSuchAlgorithmException, CertificateException{

		if(con!=null){

			try {
				System.out.println("Response Code : " + con.getResponseCode());
				System.out.println("Cipher Suite : " + con.getCipherSuite());
				System.out.println("\n");

				Certificate[] certs = con.getServerCertificates();
				for(Certificate cert : certs){

					Base64 encoder = new Base64(64);
					String cert_begin = "-----BEGIN CERTIFICATE-----\n";
					String end_cert = "-----END CERTIFICATE-----";

					byte[] derCert = cert.getEncoded();
					String pemCertPre = new String(encoder.encode(derCert));
					String pemCert = cert_begin + pemCertPre + end_cert;

					System.out.println(pemCert);
					PrintWriter writer = new PrintWriter("/tmp/cert.pem");
					writer.println(pemCert);
					writer.close();

					String certfile = "/tmp/cert.pem"; /*your cert path*/

					File dirIs = new File (System.getProperty("java.home") + File.separator + "lib" + File.separator + "security" + File.separator + "cacerts");
					FileInputStream is = new FileInputStream(dirIs);
					KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
					keystore.load(is, "changeit".toCharArray());

					String alias = "kube_alias";
					char[] password = "changeit".toCharArray();

					CertificateFactory cf = CertificateFactory.getInstance("X.509");
					InputStream certstream = fullStream (certfile);
					Certificate newCert =  cf.generateCertificate(certstream);

					///
					File dir = new File (System.getProperty("java.home") + File.separator + "lib" + File.separator + "security");
					File keystoreFile = new File(dir, "cacerts");

					// Load the keystore contents
					FileInputStream in = new FileInputStream(keystoreFile);
					keystore.load(in, password);
					in.close();

					keystore.setCertificateEntry(alias, newCert);

					// Save the new keystore contents
					FileOutputStream out = new FileOutputStream(keystoreFile);
					keystore.store(out, password);
					out.close();
				}

			} catch (SSLPeerUnverifiedException e) {
				e.printStackTrace();
			} catch (IOException e){
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		trustEveryone();
	}
}