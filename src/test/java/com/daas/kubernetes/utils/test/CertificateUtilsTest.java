package com.daas.kubernetes.utils.test;

import org.junit.Test;
import com.daas.kubernetes.utils.*;



public class CertificateUtilsTest {
	
	@Test
	public void trustCertificate() {
		CertificateUtils.trustEveryone("https://","");
	}
}