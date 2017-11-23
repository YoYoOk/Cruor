package com.yj.cruor_testing.util;

/*
 * 字节数组和字符串相互转换
 */
public class ConvertUtils {
	// 将字节数组转换成字符串发送
		public static String bytesToHexString(byte[] bytes) {
			String result = "";
			for (int i = 0; i < bytes.length; i++) {
				String hexString = Integer.toHexString(bytes[i] & 0xff);
				if (hexString.length() == 1) {
					hexString = '0' + hexString;
				}
				result += hexString.toUpperCase();
			}
			return result;
			//这种方式效率很低，此种方式会产生一大堆需要回收的中间对象  byte数组长度有多少，就会产生相应的string多余的
			//采用String.format()的方式
//			StringBuilder result = new StringBuilder();
//			for(byte b : bytes){
//				result.append(String.format("%02X", b));//把一个字节转换成16进制  %02X表示16进制大写 占2个字符  若长度为1  前面补0
//			}
//			return result.toString();
		}

		public static byte[] hexStringToBytes(String hexString) {
			if (hexString == null || hexString.equals("")) {
				return null;
			}
			hexString = hexString.toUpperCase();
			int length = hexString.length() / 2;
			char[] hexChars = hexString.toCharArray();
			byte[] d = new byte[length];
			for (int i = 0; i < length; i++) {
				int pos = i * 2;
				d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
			}
			return d;
		}

		public static byte charToByte(char c) {
			return (byte) "0123456789ABCDEF".indexOf(c);
		}
		
		
		/*
		 * 将数据转成十六进制
		 */
		public static String dataConvertHex(String data) {
			String str = Long.toHexString(Long.parseLong(data)).toUpperCase();
			str = str.length() % 2 == 0 ? str : "0" + str;
			return str;
		}

		/*
		 * 将高字节转换成低字节
		 */
		public static String HighExchangeLow(String data) {
			int size = data.length();
			String str = "";
			switch (size) {
			case 2:// 即2个字节 但是目前只有输入1个字节如2次 0200
				str = data + "00";
				break;
			case 4:// 两个字节 调换高字节和低字节
				str = data.substring(2) + data.substring(0, 2);
				break;
			default:
				break;
			}
			return str;
		}
}
