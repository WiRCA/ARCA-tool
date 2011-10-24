/*
 * Copyright (C) 2011 by Eero Laukkanen, Risto Virtanen, Jussi Patana, Juha Viljanen, Joona Koistinen, Pekka Rihtniemi, Mika Kekäle, Roope Hovi, Mikko Valjus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import org.junit.Test;
import play.test.UnitTest;
import utils.EncodingUtils;

import java.security.NoSuchAlgorithmException;

/**
 * @author Risto Virtanen
 */
public class EncodingUtilsTest extends UnitTest {

	@Test
    public void testSHA1Encoding() throws NoSuchAlgorithmException {
        String signature = "aaf069eee997d46c07d6570b010a9e5b5eb3f5f6";
        String encoded = EncodingUtils.encodeSHA1("a-root-cause-analysis");
        assertEquals(signature, encoded);
    }

	@Test
    public void testSHA1EncodingBase64() throws NoSuchAlgorithmException {
        String signature = "YWFmMDY5ZWVlOTk3ZDQ2YzA3ZDY1NzBiMDEwYTllNWI1ZWIzZjVmNg==";
        String encoded = EncodingUtils.encodeSHA1Base64("a-root-cause-analysis");
        assertEquals(signature, encoded);
    }

	@Test
    public void testSHA256Encoding() throws NoSuchAlgorithmException {
        String signature = "20344e63bb2ad8d2e53e6b95d5997b226dc58b1d68c0556edc53c27aee784490";
        String encoded = EncodingUtils.encodeSHA("a-root-cause-analysis", EncodingUtils.SHA256, false);
        assertEquals(signature, encoded);
    }

	@Test
    public void testSHA384Encoding() throws NoSuchAlgorithmException {
        String signature = "8fb414d98f267789b7ccf8e3faf092c7893aac82c2e841c8dd53d7f5f88ab6ffa36b47adbb2566f48172a6f800617ec3";
        String encoded = EncodingUtils.encodeSHA("a-root-cause-analysis", EncodingUtils.SHA384, false);
        assertEquals(signature, encoded);
    }

	@Test
    public void testSHA512Encoding() throws NoSuchAlgorithmException {
        String signature = "16485bb8bd497f010f6bc9c6aaa19bfa8894a00bb0c15919cf9a2ca5fbe6f5032b2b621e0a609b34b087733e8e195d59a1c04da53a0b9ad06016b0d63bac498a";
        String encoded = EncodingUtils.encodeSHA("a-root-cause-analysis", EncodingUtils.SHA512, false);
        assertEquals(signature, encoded);
    }

	@Test
    public void testNullEncoding() throws NoSuchAlgorithmException {
        String encoded = EncodingUtils.encodeSHA(null, EncodingUtils.SHA512, false);
        assertNull(encoded);
    }

}