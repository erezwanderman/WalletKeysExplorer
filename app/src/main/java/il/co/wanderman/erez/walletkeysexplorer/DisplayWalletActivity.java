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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import il.co.wanderman.erez.walletkeysexplorer.model.KeyPair;
import il.co.wanderman.erez.walletkeysexplorer.model.WalletContents;

public class DisplayWalletActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_wallet);

        final RadioButton radCopyAddresses = findViewById(R.id.radCopyAddresses);
        final RadioButton radCopyPrivateKeys = findViewById(R.id.radCopyPrivateKeys);

        WalletContents walletContents = MainActivity.WalletContents;
        if (walletContents != null) {
            // Capture the layout's TextView and set the string as its text
            TextView textView = findViewById(R.id.wallet_top_text);
            textView.setText(walletContents.getTitle());

            final ArrayList<KeyPair> strs = new ArrayList<>();
            if (walletContents.getKeys() != null) {
                for (KeyPair kp : walletContents.getKeys()) {
                    strs.add(kp);
                }
            }

            //ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, strs);
            ArrayAdapter adapter = new ArrayAdapter<KeyPair>(this, android.R.layout.simple_list_item_2, android.R.id.text1, strs) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                    TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                    text1.setText(strs.get(position).getAddress());
                    text2.setText(strs.get(position).getPrivateKey());
                    return view;
                }
            };

            ListView listView = (ListView) findViewById(R.id.wallet_list);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (radCopyAddresses.isChecked()) {
                        String textToCopy = strs.get(position).getAddress();
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("address", textToCopy);
                        clipboard.setPrimaryClip(clip);

                        Toast toast = Toast.makeText(view.getContext(), "Address copied", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    if (radCopyPrivateKeys.isChecked()) {
                        String textToCopy = strs.get(position).getPrivateKey();
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("address", textToCopy);
                        clipboard.setPrimaryClip(clip);

                        Toast toast = Toast.makeText(view.getContext(), "Private key copied", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            });
        }
    }
}
