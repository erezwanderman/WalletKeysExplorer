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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;

import il.co.wanderman.erez.walletkeysexplorer.exceptions.PasswordException;
import il.co.wanderman.erez.walletkeysexplorer.model.WalletContents;

public class RequestPasswordDialog {
    private static ByteArrayOutputStream file;

    public static void showDialog(final Context context, String fileName, ByteArrayOutputStream baos) {
        file = baos;
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_request_password_dialog, null);
        final EditText txtPassword = view.findViewById(R.id.explore_wallet_password_password);
        CheckBox chkShowPassword = view.findViewById(R.id.explore_wallet_password_show_password);

        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                // set Dialog Title
                .setTitle(R.string.explore_wallet_password_title)
                // Set Dialog Message
                .setMessage(String.format(view.getResources().getString(R.string.explore_wallet_password_message), fileName))
                .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        checkPassword(context, dialog, txtPassword);
                    }
                });
            }
        });
        txtPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    checkPassword(context, dialog, txtPassword);
                    return true;
                }
                return false;
            }
        });
        chkShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                txtPassword.setTransformationMethod(isChecked ? null : PasswordTransformationMethod.getInstance());
            }
        });

        dialog.show();
        doKeepDialog(dialog);
    }

    // Prevent dialog dismiss when orientation changes
    private static void doKeepDialog(Dialog dialog){
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);
    }

    private static void checkPassword(Context context, final DialogInterface dialog, EditText txtPassword) {
        String password = txtPassword.getText().toString();
        WalletContents walletContents = null;
        try {
            walletContents = ReadProtobuf.read(file, password);
        } catch (PasswordException e) {
            AlertDialog alertDialog = new AlertDialog.Builder(((AlertDialog) dialog).getContext()).create();
            alertDialog.setTitle("Error");
            alertDialog.setMessage("Cannot decrypt the wallet file, try a different password (" + e.getCause().toString() + ")");
            alertDialog.show();
            txtPassword.selectAll();
        } catch (Exception e) {
            AlertDialog alertDialog = new AlertDialog.Builder(((AlertDialog) dialog).getContext()).create();
            alertDialog.setTitle("Error");
            alertDialog.setMessage("Error while attempting to parse wallet file: " + e.toString());
            alertDialog.show();
            txtPassword.selectAll();
        }

        if (walletContents != null) {
            MainActivity.WalletContents = walletContents;
            Intent intent = new Intent(context, DisplayWalletActivity.class);
            context.startActivity(intent);
            dialog.dismiss();
        }
    }
}
