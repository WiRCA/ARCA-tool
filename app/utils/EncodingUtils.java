/*
 * Copyright (C) 2011 by Eero Laukkanen, Risto Virtanen, Jussi Patana, Juha Viljanen, Joona Koistinen,
 * Pekka Rihtniemi, Mika Kekäle, Roope Hovi, Mikko Valjus
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

package utils;

import org.apache.commons.codec.binary.Base64;
import play.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Risto Virtanen
 */
public final class EncodingUtils {

	public static final String SHA1 = "SHA-1";
	public static final String SHA256 = "SHA-256";
	public static final String SHA384 = "SHA-384";
	public static final String SHA512 = "SHA-512";

	private EncodingUtils() {
	}

	public static String encodeSHA(String text, String algorithm, boolean encodeBase64) {
		try {
			if (text != null) {
				MessageDigest md = null;

				md = MessageDigest.getInstance(algorithm);

				md.update(text.getBytes());

				byte byteData[] = md.digest();

				StringBuilder sb = new StringBuilder();
				for (byte aByteData : byteData) {
					sb.append(Integer.toString((aByteData & 0xff) + 0x100, 16).substring(1));
				}

				if (encodeBase64) {
					return new String(Base64.encodeBase64(sb.toString().getBytes()));
				}

				return sb.toString();
			}
		} catch (NoSuchAlgorithmException e) {
			Logger.error(e, "SHA encode failed");
		}

		return null;
	}

	public static String encodeSHA1(String text) {
		return encodeSHA(text, SHA1, false);
	}

	public static String encodeSHA1Base64(String text) {
		return encodeSHA(text, SHA1, true);
	}
}
