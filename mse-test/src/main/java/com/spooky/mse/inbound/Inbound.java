package com.spooky.mse.inbound;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import com.spooky.mse.BaseReceivePublicKey;
import com.spooky.mse.PublicKeyBase;
import com.spooky.mse.Tuple;
import com.spooky.mse.io.Reader;
import com.spooky.mse.o.RemotePublicKey;
import com.spooky.mse.o.SKey;
import com.spooky.mse.o.SecretKey;

//ProtocolDecoderPHE
public class Inbound extends PublicKeyBase {

	private final SKey skey;

	public Inbound(SKey skey) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
		super();
		this.skey = skey;
	}

	public SendPublicKey publicKey(Reader reader) throws Exception {
		System.out.println("correct");
		Tuple<RemotePublicKey, SecretKey> result = new BaseReceivePublicKey(keyAgreement).parse(reader.requireAtleast(publicKey.raw.length));

		RemotePublicKey remotePublicKey = result._1;
		SecretKey secret = result._2;

		System.out.println("remotePublicKey: " + Hex.encodeHexString(remotePublicKey.raw));
		System.out.println("secretKey: " + Hex.encodeHexString(secret.raw));
		System.out.println("publicKey: " + Hex.encodeHexString(publicKey.raw));

		return new SendPublicKey(skey, publicKey, remotePublicKey, secret);
	}

	public static void main(String[] args) {
		// in:
		// remotePublicKey: 3081db307306092a864886f70d0103013066026100ffffffffffffffffc90fdaa22168c234c4c6628b80dc1cd129024e088a67cc74020bbea63b139b22514a08798e3404ddef9519b3cd3a431b302b0a6df25f14374fe1356d6d51c245e485b576625e7ec6f44c42e9a63a36210000000000090563020102036400026100ee742163bac339e5945637792619b99b6497c67a953cadc547ae626ebf9fe3b9c021bf6c27103ee02e1386968d190343e5a62f32d6d1fb541254ab03dc2e3b1a1cec63043a1342620b4147cbe68c141f2110dcdc860f9eaff6e5410e21b35596
		// secretKey: 1b5a6d01fba5b289f55a71b23a408c374c979a5beb26d5863b01ffb64bc0d986badf1d2844a681999ce05a201c85086e8950ce60a650ad4f3108a2bbb0686e7d1737da603ac5f387dab31020cbd7e08b69b86ae0ec2cf364a272fcc9ab34c8f5
		// publicKey: 97366b9aae9666fe7b1f492dcc8f58aa3aa9f80203c7f64e01aa28753a4354500b18bc3ebde4cab27a4e6a60864b4e577eaa070eccb6cb4dfdc419264a07bdc7d707562bacd17add0e90928fd5818e458fdd23dc3bacba433cbe71514e395a75
		try {
			Inbound inbound = new Inbound(SKey.fromHex("597f6a218a58b0fe7880ba12466ccd89ca6c778f"));
			inbound.publicKey(new Reader() {

				@Override
				public ByteBuffer requireExact(int amount) throws Exception {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public ByteBuffer requireAtleast(int amount) throws Exception {
					return ByteBuffer.wrap(Hex.decodeHex("fd53022df6789bebe9469ce622ad236571854f3aafaec00ffd9151cbae31c71e4ce3325d66bdeab0362805c012ad473ba45f2deffc2add385aa6416606a0bfe41328a741818a00e09b6b4a735c41992a06735bb0233f41cf9740dcc0dce38843464885d65d89f4a71ee4ed5de8685fe4db7e1a552639a21940fcd24ee807e0888cfd1589939b89071118e1a9cecd09eba56870f24e2866634b4076a8f3138460b9ee25466b3c126ce2ee2be5acf37b31c4477165dc8ef86998fa5cf1cb446bbecfc84d855e981455f1be2279e4e58f7946217596c23241f0e7725ac288e6c0530ca5e4ce1474ea3baec0bf2d0c299ad8445c559c0e9a59efc9602b0a423eb8fa49e7c268ad9a498ef96d59487ec7e6aad796e87c7a8e9617bbcde4".toCharArray()));
				}

				@Override
				public void require(ByteBuffer initialPayload) throws Exception {
					// TODO Auto-generated method stub

				}

				@Override
				public void require(byte[] bs) throws Exception {
					// TODO Auto-generated method stub

				}

				@Override
				public ByteBuffer read() throws Exception {
					// TODO Auto-generated method stub
					return null;
				}
			});
		} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | DecoderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
