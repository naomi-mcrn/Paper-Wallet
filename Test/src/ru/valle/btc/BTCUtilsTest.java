/**
 The MIT License (MIT)

 Copyright (c) 2013 Valentin Konovalov

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.*/
package ru.valle.btc;

import junit.framework.TestCase;

import java.math.BigInteger;
import java.util.Arrays;

public class BTCUtilsTest extends TestCase {
    private BigInteger privateKey;
    private byte[] privateKeyBytes;


    @Override
    public void setUp() throws Exception {
        super.setUp();
        privateKeyBytes = BTCUtils.fromHex("1111111111111111111111111111111111111111111111111111111111111111");
        privateKey = new BigInteger(1, privateKeyBytes);
    }

    public void testGenPublicKey() throws Exception {
        byte[] publicKeyUncompressed = BTCUtils.generatePublicKey(privateKey, false);
        assertTrue(Arrays.equals(BTCUtils.fromHex("044f355bdcb7cc0af728ef3cceb9615d90684bb5b2ca5f859ab0f0b704075871aa385b6b1b8ead809ca67454d9683fcf2ba03456d6fe2c4abe2b07f0fbdbb2f1c1"), publicKeyUncompressed));
        byte[] publicKeyCompressed = BTCUtils.generatePublicKey(privateKey, true);
        assertTrue(Arrays.equals(BTCUtils.fromHex("034f355bdcb7cc0af728ef3cceb9615d90684bb5b2ca5f859ab0f0b704075871aa"), publicKeyCompressed));

        BigInteger privateKey2 = new BigInteger(1, BTCUtils.fromHex("dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"));
        publicKeyUncompressed = BTCUtils.generatePublicKey(privateKey2, false);
        assertTrue(Arrays.equals(BTCUtils.fromHex("04ed83704c95d829046f1ac27806211132102c34e9ac7ffa1b71110658e5b9d1bdedc416f5cefc1db0625cd0c75de8192d2b592d7e3b00bcfb4a0e860d880fd1fc"), publicKeyUncompressed));
        publicKeyCompressed = BTCUtils.generatePublicKey(privateKey2, true);
        assertTrue(Arrays.equals(BTCUtils.fromHex("02ed83704c95d829046f1ac27806211132102c34e9ac7ffa1b71110658e5b9d1bd"), publicKeyCompressed));

        BigInteger privateKey3 = new BigInteger(1, BTCUtils.fromHex("47f7616ea6f9b923076625b4488115de1ef1187f760e65f89eb6f4f7ff04b012"));
        publicKeyUncompressed = BTCUtils.generatePublicKey(privateKey3, false);
        assertTrue(Arrays.equals(BTCUtils.fromHex("042596957532fc37e40486b910802ff45eeaa924548c0e1c080ef804e523ec3ed3ed0a9004acf927666eee18b7f5e8ad72ff100a3bb710a577256fd7ec81eb1cb3"), publicKeyUncompressed));
        publicKeyCompressed = BTCUtils.generatePublicKey(privateKey3, true);
        assertTrue(Arrays.equals(BTCUtils.fromHex("032596957532fc37e40486b910802ff45eeaa924548c0e1c080ef804e523ec3ed3"), publicKeyCompressed));
    }

    public void testDoubleSha256() throws Exception {
        byte[] helloBytes = "hello".getBytes("UTF-8");
        byte[] hashed = BTCUtils.doubleSha256(helloBytes);
        assertTrue("result " + BTCUtils.toHex(hashed), Arrays.equals(BTCUtils.fromHex("9595c9df90075148eb06860365df33584b75bff782a510c6cd4883a419833d50"), hashed));
        assertTrue(Arrays.equals("hello".getBytes("UTF-8"), helloBytes));
    }

    public void testPublicKeyToAddress() throws Exception {
        assertEquals("16UwLL9Risc3QfPqBUvKofHmBQ7wMtjvM", BTCUtils.publicKeyToAddress(BTCUtils.fromHex("0450863AD64A87AE8A2FE83C1AF1A8403CB53F53E486D8511DAD8A04887E5B23522CD470243453A299FA9E77237716103ABC11A1DF38855ED6F2EE187E9C582BA6")));
        assertEquals("1MsHWS1BnwMc3tLE8G35UXsS58fKipzB7a", BTCUtils.publicKeyToAddress(BTCUtils.fromHex("044f355bdcb7cc0af728ef3cceb9615d90684bb5b2ca5f859ab0f0b704075871aa385b6b1b8ead809ca67454d9683fcf2ba03456d6fe2c4abe2b07f0fbdbb2f1c1")));
        assertEquals("1Q1pE5vPGEEMqRcVRMbtBK842Y6Pzo6nK9", BTCUtils.publicKeyToAddress(BTCUtils.fromHex("034f355bdcb7cc0af728ef3cceb9615d90684bb5b2ca5f859ab0f0b704075871aa")));
    }

    public void testDecodePrivateKeys() throws Exception {
        BTCUtils.PrivateKeyInfo pk;
        pk = BTCUtils.decodePrivateKey("KwntMbt59tTsj8xqpqYqRRWufyjGunvhSyeMo3NTYpFYzZbXJ5Hp");
        assertNotNull(pk);
        assertEquals(BTCUtils.PrivateKeyInfo.TYPE_WIF, pk.type);
        assertEquals(true, pk.isPublicKeyCompressed);
        assertEquals("KwntMbt59tTsj8xqpqYqRRWufyjGunvhSyeMo3NTYpFYzZbXJ5Hp", pk.privateKeyEncoded);
        assertTrue(Arrays.equals(privateKeyBytes, pk.privateKeyDecoded.toByteArray()));
        assertEquals(privateKey, pk.privateKeyDecoded);

        pk = BTCUtils.decodePrivateKey("L3p8oAcQTtuokSCRHQ7i4MhjWc9zornvpJLfmg62sYpLRJF9woSu");
        assertNotNull(pk);
        assertEquals(BTCUtils.PrivateKeyInfo.TYPE_WIF, pk.type);
        assertEquals(true, pk.isPublicKeyCompressed);
        assertEquals("L3p8oAcQTtuokSCRHQ7i4MhjWc9zornvpJLfmg62sYpLRJF9woSu", pk.privateKeyEncoded);
        assertTrue(Arrays.equals(BTCUtils.fromHex("00c4bbcb1fbec99d65bf59d85c8cb62ee2db963f0fe106f483d9afa73bd4e39a8a"), pk.privateKeyDecoded.toByteArray()));

        pk = BTCUtils.decodePrivateKey("5KJvsngHeMpm884wtkJNzQGaCErckhHJBGFsvd3VyK5qMZXj3hS");
        assertNotNull(pk);
        assertEquals(BTCUtils.PrivateKeyInfo.TYPE_WIF, pk.type);
        assertEquals(false, pk.isPublicKeyCompressed);
        assertEquals("5KJvsngHeMpm884wtkJNzQGaCErckhHJBGFsvd3VyK5qMZXj3hS", pk.privateKeyEncoded);
        assertTrue(Arrays.equals(BTCUtils.fromHex("00c4bbcb1fbec99d65bf59d85c8cb62ee2db963f0fe106f483d9afa73bd4e39a8a"), pk.privateKeyDecoded.toByteArray()));

        pk = BTCUtils.decodePrivateKeyAsSHA256("KwntMbt59tTsj8xqpqYqRRWufyjGunvhSyeMo3NTYpFYzZbXJ5Hp");
        assertNotNull(pk);
        assertEquals(BTCUtils.PrivateKeyInfo.TYPE_BRAIN_WALLET, pk.type);

        pk = BTCUtils.decodePrivateKey("correct horse battery staple");
        assertEquals(BTCUtils.PrivateKeyInfo.TYPE_BRAIN_WALLET, pk.type);
        //assertEquals(false, pk.isPublicKeyCompressed);
    }

    public void testVerifySignedTransaction() throws Exception {
        Transaction txWithUnspentOutput = new Transaction(BTCUtils.fromHex("01000000014a2aaf9741aee2646513dce5edf58d65acf5afd090341e4f166706a9c82876d5490000006c493046022100a82977e23d5ece7737968eb81d4a63b65ec1bdacc605d25d8869e" +
                "0fb0d94a040022100c8eefcf6c4b1cc3f050583e9dcf7734d3890ec1300fc1020d5b9407c88840bae0121023c0da0c64e2e5bb7cb0e2b4c10b425854e9b39f2bf8efa5253c0ae77b6948474ffffffff" +
                "0280f0fa02000000001976a914a3ad778585da555d5beff2f133d8e2bca06b8cbc88acca580200000000001976a914e9e64aae2d1e066db6c5ecb1a2781f418b18eef488ac00000000"));

        int indexOfOutputToSpend = 1;
        try {
            indexOfOutputToSpend = BTCUtils.findSpendableOutput(txWithUnspentOutput, "1NKkKeTDWWi5LQQdrSS7hghnbhfYtWiWHs", 0);
            assertEquals(1, indexOfOutputToSpend);
        } catch (RuntimeException e) {
            assertFalse(e.getMessage(), true);
        }


        //this spending transaction is already in blockchain and spent successfully, so it is 100% valid.
        Transaction spendTx = new Transaction(BTCUtils.fromHex("01000000017e67310ea917d364bb490ba319a9b6523e73e7f4b86cebac55fe17d0051bb2f701000000" +
                "6c493046022100b4efc48e568c586aed05ca84fad42bbc9670963bf412ef5b203ac8f7526043aa022100cb28ea336d1ad603446fffa3f67aa0a07c3e210fa4d95e15a23217e302eb7575012103e35c82156982e11c26d0670a67ad96dbba0714cf389fc099f14fa7c3c4b0a4eaffffffff" +
                "02a0860100000000001976a914f05163c32b88ff3208466f57de11734b69768bff88acda0e0000000000001976a9140f109043279b5237576312cc05c87475d063140188ac00000000"));

        try {
            BTCUtils.verify(txWithUnspentOutput.outputs[indexOfOutputToSpend].script, spendTx);
        } catch (Transaction.Script.ScriptInvalidException e) {
            assertFalse(e.getMessage(), true);
        }
        Transaction brokenSpendTx = new Transaction(BTCUtils.fromHex("01000000017e67310ea917d364bb490ba319a9b6523e73e7f4b86cebac55fe17d0051bb2f701000000" +
                "6c493046022100b4efc48e568c586aed05ca84fad42bbc9670963bf412ef5b203ac8f7526043aa022100cb28ea336d1ad603446fffa3f67aa0a07c3e210fa4d95e15a23217e302eb7575012103e35c82156982e11c26d0670a67ad96dbba0714cf389fc099f14fa7c3c4b0a4eaffffffff" +
                "02a0860100000000001976a914f05163c32b88ff3208466f57de11734b69768bff88acda0d0000000000001976a9140f109043279b5237576312cc05c87475d063140188ac00000000"));
        try {
            BTCUtils.verify(txWithUnspentOutput.outputs[indexOfOutputToSpend].script, brokenSpendTx);
            assertFalse("incorrectly signed transactions must not pass this check", true);
        } catch (Transaction.Script.ScriptInvalidException okay) {
        }
    }

    public void testCreateTransaction() throws Exception {
        try {
            final byte[] rawInputTx = BTCUtils.fromHex("0100000001ef9ea3e6b7a664ff910ed1177bfa81efa018df417fb1ee964b8165a05dc7ef5a000000008b4830450220385373efe509719e38cb63b86ca5d764be0f2bd2ffcfa03194978ca68488f57b0221009686e0b54d7831f9f06d36bfb81c5d2931a8ada079a3ff58c6109030ed0c4cd601410424161de67ec43e5bfd55f52d98d2a99a2131904b25aa08e70924d32ed44bfb4a71c94a7c4fdac886ca5bec7b7fac4209ab1443bc48ab6dec31656cd3e55b5dfcffffffff02707f0088000000001976a9143412c159747b9149e8f0726123e2939b68edb49e88ace0a6e001000000001976a914e9e64aae2d1e066db6c5ecb1a2781f418b18eef488ac00000000");
            final long fee = (long) (0.0001 * 1e8);
            final String outputAddress = "1AyyaMAyo5sbC73kdUjgBK9h3jDMoXzkcP";
            final BTCUtils.PrivateKeyInfo privateKeyInfo = BTCUtils.decodePrivateKey("L49guLBaJw8VSLnKGnMKVH5GjxTrkK4PBGc425yYwLqnU5cGpyxJ");

            final Transaction baseTx = new Transaction(rawInputTx);
            byte[] rawTxReconstructed = baseTx.getBytes();
            if (!Arrays.equals(rawTxReconstructed, rawInputTx)) {
                throw new IllegalArgumentException("Unable to decode given transaction");
            }
            KeyPair keyPair = new KeyPair(privateKeyInfo);
            final int indexOfOutputToSpend = BTCUtils.findSpendableOutput(baseTx, keyPair.address, fee);
            final Transaction spendTx;

            spendTx = BTCUtils.createTransaction(baseTx, indexOfOutputToSpend, outputAddress, fee, keyPair.publicKey, keyPair.privateKey);
            BTCUtils.verify(baseTx.outputs[indexOfOutputToSpend].script, spendTx);

        } catch (Exception e) {
            assertFalse("We have built invalid transaction", true);
        }
    }

    public void testChecksummVerification() throws Exception {
        assertTrue(BTCUtils.verifyChecksum(BTCUtils.fromHex("00010966776006953D5567439E5E39F86A0D273BEED61967F6")));
        assertFalse(BTCUtils.verifyChecksum(BTCUtils.fromHex("00010966776006953D5567439E5E39F86A0D273BEED61967F5")));
        assertFalse(BTCUtils.verifyChecksum(BTCUtils.fromHex("10010966776006953D5567439E5E39F86A0D273BEED61967F6")));
    }

    public void testAddressVerification() throws Exception {
        assertTrue(BTCUtils.verifyBitcoinAddress("16UwLL9Risc3QfPqBUvKofHmBQ7wMtjvM"));
        assertTrue(BTCUtils.verifyBitcoinAddress("1BitcoinEaterAddressDontSendf59kuE"));
        assertTrue(BTCUtils.verifyBitcoinAddress("1111111111111111111114oLvT2"));
        assertTrue(BTCUtils.verifyBitcoinAddress(" 1111111111111111111114oLvT2 "));
        assertFalse(BTCUtils.verifyBitcoinAddress(null));
        assertFalse(BTCUtils.verifyBitcoinAddress(""));
        assertFalse(BTCUtils.verifyBitcoinAddress("ваполршг"));
        assertFalse(BTCUtils.verifyBitcoinAddress("LdDNZngmxopohCUoxxvXLUJTMVvCYfXLwr"));
        assertFalse(BTCUtils.verifyBitcoinAddress("NKen68obTRcU6UG7BSDvMPWYnWe2asqFBo"));
        assertFalse(BTCUtils.verifyBitcoinAddress("1111111111111111111214oLvT2"));
    }

    public void testBase58EncodeDecode() throws Exception {
        base58EncodeDecode(new byte[0], "");
        base58EncodeDecode(BTCUtils.fromHex("61"), "2g");
        base58EncodeDecode(BTCUtils.fromHex("626262"), "a3gV");
        base58EncodeDecode(BTCUtils.fromHex("636363"), "aPEr");
        base58EncodeDecode(BTCUtils.fromHex("73696d706c792061206c6f6e6720737472696e67"), "2cFupjhnEsSn59qHXstmK2ffpLv2");
        base58EncodeDecode(BTCUtils.fromHex("00eb15231dfceb60925886b67d065299925915aeb172c06647"), "1NS17iag9jJgTHD1VXjvLCEnZuQ3rJDE9L");
        base58EncodeDecode(BTCUtils.fromHex("516b6fcd0f"), "ABnLTmg");
        base58EncodeDecode(BTCUtils.fromHex("bf4f89001e670274dd"), "3SEo3LWLoPntC");
        base58EncodeDecode(BTCUtils.fromHex("572e4794"), "3EFU7m");
        base58EncodeDecode(BTCUtils.fromHex("ecac89cad93923c02321"), "EJDM8drfXA6uyA");
        base58EncodeDecode(BTCUtils.fromHex("10c8511e"), "Rt5zm");
        base58EncodeDecode(BTCUtils.fromHex("00000000000000000000"), "1111111111");
    }

    private void base58EncodeDecode(byte[] base256, String base58){
        assertEquals(base58, BTCUtils.encodeBase58(base256));
        assertTrue(Arrays.equals(base256, BTCUtils.decodeBase58(base58)));
    }

}
