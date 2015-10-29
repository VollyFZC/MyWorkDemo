package android.CRC;
public class CRC {
//	public static byte crc(byte[] data) {
//		byte result = 0;
//		if (data == null || data.length < 2) {
//			return 0;
//		}
//		for (int i = 1; i < data.length - 2; i++) {
//			result += data[i];
//		}
//		return result;
//	}
	public static byte crc(byte[] data) {
		byte result = 0;
		if (data == null || data.length < 2) {
			return 0;
		}
		for (int i = 1; i < data.length - 1; i++) {
			result += data[i];
		}
		return result;
	}
}
