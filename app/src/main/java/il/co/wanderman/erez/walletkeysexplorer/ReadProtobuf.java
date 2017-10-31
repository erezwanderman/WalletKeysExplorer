/*
MIT License

Copyright (c) 2017 Erez Wanderman

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package il.co.wanderman.erez.walletkeysexplorer;

import org.bitcoinj.core.Context;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletProtobufSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.schildbach.wallet.util.Crypto;
import il.co.wanderman.erez.walletkeysexplorer.exceptions.PasswordException;
import il.co.wanderman.erez.walletkeysexplorer.model.KeyPair;
import il.co.wanderman.erez.walletkeysexplorer.model.WalletContents;

public class ReadProtobuf {
    public static WalletContents read(ByteArrayOutputStream baos, String password) throws Exception {
        resetStaticStuff();

        String walletString = new String(baos.toByteArray());
        final byte[] plainText;
        try {
            plainText = Crypto.decryptBytes(walletString, password.toCharArray());
        } catch (Exception e) {
            throw new PasswordException(e);
        }
        final InputStream is = new ByteArrayInputStream(plainText);


        Wallet wallet = null;

        wallet = new WalletProtobufSerializer().readWallet(is, true, null);

        String title;
        NetworkParameters params = wallet.getNetworkParameters();
        switch (params.getId()) {
            case NetworkParameters.ID_MAINNET:
                title = "Wallet for Bitcoin\n";
                break;
            case NetworkParameters.ID_TESTNET:
                title = "Wallet for Bitcoin Testnet\n";
                break;
            default:
                title = "Wallet for an unknown network (" + params.getId() + ")\n";
                break;
        }

        ArrayList<KeyPair> lstKeyPairs = new ArrayList<>();
        DeterministicKeyChain keyChain = wallet.getActiveKeyChain();
        Method meth = DeterministicKeyChain.class.getDeclaredMethod("getKeys", boolean.class);
        meth.setAccessible(true);
        List<ECKey> keys = (List<ECKey>) meth.invoke(keyChain, false);

        Collections.sort(keys, ECKey.AGE_COMPARATOR);
        for (ECKey key : keys) {
            KeyPair kp = new KeyPair();
            kp.setPrivateKey(key.getPrivateKeyAsWiF(params));
            kp.setAddress(key.toAddress(params).toString());
            lstKeyPairs.add(kp);
        }
        WalletContents walletContents = new WalletContents();
        walletContents.setTitle(title);
        walletContents.setKeys(lstKeyPairs.toArray(new KeyPair[0]));
        return walletContents;
    }

    private static void resetStaticStuff() {
        // bitcoinj stores NetworkParams in static variables, and if I try to load a testnet wallet
        // backup and then a mainnet wallet backup it fails.
        // As a workaround, I clear the static vars using reflection.
        Field field;
        try {
            field = Context.class.getDeclaredField("lastConstructed");
            field.setAccessible(true);
            field.set(null, null);
            field = Context.class.getDeclaredField("slot");
            field.setAccessible(true);
            field.set(null, new ThreadLocal<Context>());
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
