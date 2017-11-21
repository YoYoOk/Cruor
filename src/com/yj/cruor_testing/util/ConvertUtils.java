package com.yj.cruor_testing.util;

/*
 * �ֽ�������ַ����໥ת��
 */
public class ConvertUtils {
	// ���ֽ�����ת�����ַ�������
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
			//���ַ�ʽЧ�ʺܵͣ����ַ�ʽ�����һ�����Ҫ���յ��м����  byte���鳤���ж��٣��ͻ������Ӧ��string�����
			//����String.format()�ķ�ʽ
//			StringBuilder result = new StringBuilder();
//			for(byte b : bytes){
//				result.append(String.format("%02X", b));//��һ���ֽ�ת����16����  %02X��ʾ16���ƴ�д ռ2���ַ�  ������Ϊ1  ǰ�油0
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
}
