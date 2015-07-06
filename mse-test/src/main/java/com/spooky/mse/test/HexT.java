package com.spooky.mse.test;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class HexT {
	public static void main(String[] args) throws DecoderException {
		String s = "52948c72e9fb14df8c21a59293bdcac8ee4e101c8e4da9af52dcff293d393c895d44f23a8064ed6e13711be16e74acea3c71bef889aa6816f145438d60c6c2de68d625bb65030797bf9f76c1e73d3d699efce2eef13bfb9eb479b01b24edd34";
		System.out.println(s.length());
		System.out.println(s.length() / 2d);
		Hex.decodeHex(s.toCharArray());
	}
}
