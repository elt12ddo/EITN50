package utility;

import java.util.Arrays;

/**
 * Concatenates two byte arrays into one byte array.
 * @param first byte array
 * @param second byte array
 * @return concatenated byte array
 */
public class Utility {
	public static byte[] concatByte(byte[] first, byte[] second) {
		byte[] out = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, out, first.length, second.length);
		return out;
	}
	public static byte[] concatByte(byte first, byte[] second) {
		byte[] out = new byte[1 + second.length];
		out[0] = first;
		System.arraycopy(second, 0, out, 1, second.length);
		return out;
	}
}
