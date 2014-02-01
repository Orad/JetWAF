package coolman;

import static org.junit.Assert.*;

import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

public class XSSParserTest {

// @Test
	// public void testXSS1() {
	// String str1 = "<script>alert('hi')</script>";
	// assertTrue(doScan(str1));
	// }
	//
	// //@Test
	// public void testXSS2() {
	// String str1 = "<ScRIPt>alert('hi')</scRIPt>";
	// assertTrue(doScan(str1));
	// }

	
	//	<link href="http://localhost:8080/ar/home?p_p_id=83'"--></style></script><script>/*<![CDATA[*/alert(document.cookie);/*]]>*/</script>" hreflang="ar-SA" rel="alternate" /> " +
	 

	/**
	 * 
	 * 
	 * 
	 * @Test public void testXSS3() { String str1 =
	 *       "';alert(String.fromCharCode(88,83,83))//\';alert(String.fromCharCode(88,83,83))//"
	 *       ;alert(String.fromCharCode(88,83,83))//\
	 *       ";alert(String.fromCharCode(88,83,83))//--></SCRIPT>"
	 *       >'><SCRIPT>alert(String.fromCharCode(88,83,83))</SCRIPT>";
	 *       assertTrue(doScan(str1)); }
	 * @Test public void testXSS4() { String str1 = "'';!--"<XSS
	 *       src='javascript: alert('hey');'>=&{()}"; assertTrue(doScan(str1));
	 *       }
	 * @Test public void testXSS4() { String str1 =
	 *       "<IMG SRC=javascript:alert('XSS')>"; assertTrue(doScan(str1)); }
	 * @Test public void testXSS4() { String str1 = "<IMG SRC="jav
	 *       ascript:alert('XSS');">"; assertTrue(doScan(str1)); }
	 */

	/**
	 * Check false positive.. this is a normal test and it should not trigger
	 * the prarser
	 */
	// @Test
	// public void testXSS3() {
	// String str1 = "sometext";
	// assertTrue(!doScan(str1));
	// }
	//

	public boolean doScan(String str) {
		for (Pattern scriptPattern : Patterns.patterns) {

			Matcher matcher = scriptPattern.matcher(str);
			boolean matched = matcher.find();
			if (matched)
				return true;

		}
		return false;
	}
}

/*
 * first, you must NOT filter "script" as is, because the word script isnt XSS
 * 
 * FAILED TO DETECT: <IMG
 * SRC=&#106;&#97;&#118;&#97;&#115;&#99;&#114;&#105;&#112;
 * &#116;&#58;&#97;&#108;&#101;&#114;&#116;&#40;&#39;&#88;&#83;&#83;&#39;&#41;>
 * 
 * <IMG
 * SRC=&#0000106&#0000097&#0000118&#0000097&#0000115&#0000099&#0000114&#0000105
 * &#
 * 0000112&#0000116&#0000058&#0000097&#0000108&#0000101&#0000114&#0000116&#0000040
 * &#0000039&#0000088&#0000083&#0000083&#0000039&#0000041>
 * 
 * <IMG
 * SRC=&#x6A&#x61&#x76&#x61&#x73&#x63&#x72&#x69&#x70&#x74&#x3A&#x61&#x6C&#x65
 * &#x72&#x74&#x28&#x27&#x58&#x53&#x53&#x27&#x29>
 * 
 * <IMG SRC="jav	ascript:alert('XSS');">
 * 
 * <IMG SRC="jav&#x09;ascript:alert('XSS');">
 * 
 * <IMG SRC="jav&#x0A;ascript:alert('XSS');">
 */
