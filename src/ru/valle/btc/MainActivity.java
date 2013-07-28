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

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Arrays;

public final class MainActivity extends Activity {

    private EditText addressView;
    private TextView privateKeyTypeView;
    private EditText privateKeyTextEdit;
    private View sendLayout;
    private TextView rawTxDescriptionView;
    private EditText rawTxToSpendEdit;
    private TextView recipientAddressView;
    private TextView spendTxDescriptionView;
    private TextView spendTxEdit;
    private View generateButton;

    private boolean insertingPrivateKeyProgrammatically, insertingAddressProgrammatically;
    private AsyncTask<Void, Void, KeyPair> addressGenerateTask;
    private AsyncTask<Void, Void, GenerateTransactionResult> generateTransactionTask;
    private KeyPair currentKeyPair;
    private AsyncTask<Void, Void, KeyPair> switchingCompressionTypeTask;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        addressView = (EditText) findViewById(R.id.address_label);
        generateButton = findViewById(R.id.generate_button);
        privateKeyTypeView = (TextView) findViewById(R.id.private_key_type_label);
        privateKeyTypeView.setMovementMethod(LinkMovementMethod.getInstance());
        privateKeyTextEdit = (EditText) findViewById(R.id.private_key_label);

        sendLayout = findViewById(R.id.send_layout);
        rawTxToSpendEdit = (EditText) findViewById(R.id.raw_tx);
        recipientAddressView = (TextView) findViewById(R.id.recipient_address);
        rawTxDescriptionView = (TextView) findViewById(R.id.raw_tx_description);
        spendTxDescriptionView = (TextView) findViewById(R.id.spend_tx_description);
        spendTxEdit = (TextView) findViewById(R.id.spend_tx);

        wireListeners();
        generateNewAddress();
    }

    private void wireListeners() {
        addressView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!insertingAddressProgrammatically) {
                    insertingPrivateKeyProgrammatically = true;
                    privateKeyTextEdit.setText("");
                    insertingPrivateKeyProgrammatically = false;
                    cancelAllRunningTasks();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateNewAddress();
            }
        });
        privateKeyTextEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!insertingPrivateKeyProgrammatically) {
                    cancelAllRunningTasks();
                    new AsyncTask<String, Void, KeyPair>() {
                        @Override
                        protected KeyPair doInBackground(String... params) {
                            try {
                                BTCUtils.PrivateKeyInfo privateKeyInfo = BTCUtils.decodePrivateKey(params[0]);
                                if (privateKeyInfo != null) {
                                    return new KeyPair(privateKeyInfo);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(KeyPair key) {
                            super.onPostExecute(key);
                            onKeyPairModify(key);
                        }
                    }.execute(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        TextWatcher generateTransactionOnInputChangeTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                generateSpendingTransaction(rawTxToSpendEdit.getText().toString(), recipientAddressView.getText().toString(), currentKeyPair);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        rawTxToSpendEdit.addTextChangedListener(generateTransactionOnInputChangeTextWatcher);
        recipientAddressView.addTextChangedListener(generateTransactionOnInputChangeTextWatcher);
    }

    private void onNewKeyPairGenerated(KeyPair key) {
        insertingAddressProgrammatically = true;
        if (key != null) {
            addressView.setText(key.address);
            privateKeyTypeView.setVisibility(View.VISIBLE);
            privateKeyTypeView.setText(getPrivateKeyTypeLabel(key));
            insertingPrivateKeyProgrammatically = true;
            privateKeyTextEdit.setText(key.privateKey.privateKeyEncoded);
            insertingPrivateKeyProgrammatically = false;
        } else {
            privateKeyTypeView.setVisibility(View.GONE);
            addressView.setText(getString(R.string.generating_failed));
        }
        insertingAddressProgrammatically = false;
        showSpendPanelForKeyPair(null);//generated address does not have funds to spend yet
    }

    private void onKeyPairModify(KeyPair keyPair) {
        insertingAddressProgrammatically = true;
        if (keyPair != null) {
            addressView.setText(keyPair.address);
            privateKeyTypeView.setVisibility(View.VISIBLE);
            privateKeyTypeView.setText(getPrivateKeyTypeLabel(keyPair));
        } else {
            privateKeyTypeView.setVisibility(View.GONE);
            addressView.setText(getString(R.string.bad_private_key));
        }
        insertingAddressProgrammatically = false;
        showSpendPanelForKeyPair(keyPair);
    }

    private void cancelAllRunningTasks() {
        if (addressGenerateTask != null) {
            addressGenerateTask.cancel(true);
            addressGenerateTask = null;
        }
        if (generateTransactionTask != null) {
            generateTransactionTask.cancel(true);
            generateTransactionTask = null;
        }
        if (switchingCompressionTypeTask != null) {
            switchingCompressionTypeTask.cancel(false);
            switchingCompressionTypeTask = null;
        }
    }

    static class GenerateTransactionResult {
        static final int ERROR_SOURCE_UNKNOWN = 0;
        static final int ERROR_SOURCE_INPUT_TX_FIELD = 1;
        static final int ERROR_SOURCE_ADDRESS_FIELD = 2;
        static final int HINT_FOR_ADDRESS_FIELD = 3;

        final Transaction tx;
        final String errorMessage;
        final int errorSource;

        public GenerateTransactionResult(String errorMessage, int errorSource) {
            tx = null;
            this.errorMessage = errorMessage;
            this.errorSource = errorSource;
        }

        public GenerateTransactionResult(Transaction tx) {
            this.tx = tx;
            errorMessage = null;
            errorSource = ERROR_SOURCE_UNKNOWN;
        }
    }

    private void generateSpendingTransaction(final String baseTxStr, final String outputAddress, final KeyPair keyPair) {
        rawTxToSpendEdit.setError(null);
        recipientAddressView.setError(null);
        spendTxDescriptionView.setVisibility(View.GONE);
        spendTxEdit.setVisibility(View.GONE);
        cancelAllRunningTasks();
        if (!(TextUtils.isEmpty(baseTxStr) && TextUtils.isEmpty(outputAddress)) && keyPair != null && keyPair.privateKey != null) {
            final long fee = (long) (0.0001 * 1e8);

            generateTransactionTask = new AsyncTask<Void, Void, GenerateTransactionResult>() {

                @Override
                protected GenerateTransactionResult doInBackground(Void... voids) {
                    Transaction baseTx = null;
                    int indexOfOutputToSpend = -1;
                    if (!TextUtils.isEmpty(baseTxStr)) {

                        try {
                            byte[] rawTx = BTCUtils.fromHex(baseTxStr);
                            baseTx = new Transaction(rawTx);
                            byte[] rawTxReconstructed = baseTx.getBytes();
                            if (!Arrays.equals(rawTxReconstructed, rawTx)) {
                                throw new IllegalArgumentException("Unable to decode given transaction");
                            }
                        } catch (Exception e) {
                            return new GenerateTransactionResult(getString(R.string.error_unable_to_decode_transaction), GenerateTransactionResult.ERROR_SOURCE_INPUT_TX_FIELD);
                        }

                        try {
                            indexOfOutputToSpend = BTCUtils.findSpendableOutput(baseTx, keyPair.address, fee);
                        } catch (Exception e) {
                            return new GenerateTransactionResult(e.getMessage(), GenerateTransactionResult.ERROR_SOURCE_INPUT_TX_FIELD);
                        }
                    }
                    if (TextUtils.isEmpty(outputAddress)) {
                        if (indexOfOutputToSpend >= 0 && baseTx != null) {
                            String unspentAmountStr = BTCUtils.formatValue(baseTx.outputs[indexOfOutputToSpend].value);
                            return new GenerateTransactionResult(getString(R.string.enter_address_to_spend, unspentAmountStr), GenerateTransactionResult.HINT_FOR_ADDRESS_FIELD);
                        } else {
                            return null;
                        }
                    }
                    if (!BTCUtils.verifyBitcoinAddress(outputAddress)) {
                        return new GenerateTransactionResult(getString(R.string.invalid_address), GenerateTransactionResult.ERROR_SOURCE_ADDRESS_FIELD);
                    }
                    if (baseTx == null || indexOfOutputToSpend == -1) {
                        return new GenerateTransactionResult(getString(R.string.error_no_transaction), GenerateTransactionResult.ERROR_SOURCE_INPUT_TX_FIELD);
                    }
                    final Transaction spendTx;
                    try {
                        spendTx = BTCUtils.createTransaction(baseTx, indexOfOutputToSpend, outputAddress, fee, keyPair.publicKey, keyPair.privateKey);
                        BTCUtils.verify(baseTx.outputs[indexOfOutputToSpend].script, spendTx);
                    } catch (Exception e) {
                        return new GenerateTransactionResult(getString(R.string.error_failed_to_create_transaction), GenerateTransactionResult.ERROR_SOURCE_UNKNOWN);
                    }
                    return new GenerateTransactionResult(spendTx);
                }

                @Override
                protected void onPostExecute(GenerateTransactionResult result) {
                    super.onPostExecute(result);
                    generateTransactionTask = null;
                    if (result != null) {
                        if (result.tx != null) {
                            spendTxDescriptionView.setText(getString(R.string.spend_tx_description,
                                    BTCUtils.formatValue(result.tx.outputs[0].value),
                                    keyPair.address,
                                    outputAddress,
                                    BTCUtils.formatValue(fee)
                            ));
                            spendTxDescriptionView.setVisibility(View.VISIBLE);
                            spendTxEdit.setText(BTCUtils.toHex(result.tx.getBytes()));
                            spendTxEdit.setVisibility(View.VISIBLE);
                        } else if (result.errorSource == GenerateTransactionResult.ERROR_SOURCE_INPUT_TX_FIELD) {
                            rawTxToSpendEdit.setError(result.errorMessage);
                        } else if (result.errorSource == GenerateTransactionResult.ERROR_SOURCE_ADDRESS_FIELD ||
                                result.errorSource == GenerateTransactionResult.HINT_FOR_ADDRESS_FIELD) {
                            recipientAddressView.setError(result.errorMessage);
                        }
                    }
                }
            }.execute();
        }
    }

    private void generateNewAddress() {
        insertingPrivateKeyProgrammatically = true;
        privateKeyTextEdit.setText("");
        insertingPrivateKeyProgrammatically = false;
        insertingAddressProgrammatically = true;
        addressView.setText(getString(R.string.generating));
        insertingAddressProgrammatically = false;
        addressGenerateTask = new AsyncTask<Void, Void, KeyPair>() {
            @Override
            protected KeyPair doInBackground(Void... params) {
                return BTCUtils.generateMiniKey();
            }

            @Override
            protected void onPostExecute(final KeyPair key) {
                addressGenerateTask = null;
                onNewKeyPairGenerated(key);
            }
        }.execute();
    }

    private CharSequence getPrivateKeyTypeLabel(final KeyPair keyPair) {
        int typeWithCompression = keyPair.privateKey.type == BTCUtils.PrivateKeyInfo.TYPE_BRAIN_WALLET && keyPair.privateKey.isPublicKeyCompressed ? keyPair.privateKey.type + 1 : keyPair.privateKey.type;
        CharSequence keyType = getResources().getTextArray(R.array.private_keys_types)[typeWithCompression];
        SpannableString keyTypeLabel = new SpannableString(getString(R.string.private_key_type, keyType));

        if (keyPair.privateKey.type == BTCUtils.PrivateKeyInfo.TYPE_BRAIN_WALLET) {
            String compressionStrToSpan = keyType.toString().substring(keyType.toString().indexOf(',') + 2);
            int start = keyTypeLabel.toString().indexOf(compressionStrToSpan);
            if (start >= 0) {

                ClickableSpan switchPublicKeyCompressionSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        cancelAllRunningTasks();
                        switchingCompressionTypeTask = new AsyncTask<Void, Void, KeyPair>() {

                            @Override
                            protected KeyPair doInBackground(Void... params) {
                                return new KeyPair(new BTCUtils.PrivateKeyInfo(keyPair.privateKey.type, keyPair.privateKey.privateKeyEncoded, keyPair.privateKey.privateKeyDecoded, !keyPair.privateKey.isPublicKeyCompressed));
                            }

                            @Override
                            protected void onPostExecute(KeyPair keyPair) {
                                switchingCompressionTypeTask = null;
                                onKeyPairModify(keyPair);
                            }
                        };
                        switchingCompressionTypeTask.execute();
                    }
                };
                keyTypeLabel.setSpan(switchPublicKeyCompressionSpan, start, start + compressionStrToSpan.length(), SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }
        return keyTypeLabel;
    }

    private void showSpendPanelForKeyPair(KeyPair keyPair) {
        currentKeyPair = keyPair;
        if (keyPair == null) {
            rawTxToSpendEdit.setText("");
        } else {
            String descStr = getString(R.string.raw_tx_description, keyPair.address);
            final SpannableStringBuilder builder = new SpannableStringBuilder(descStr);
            int spanBegin = descStr.indexOf(keyPair.address);
            if (spanBegin >= 0) {
                ForegroundColorSpan addressColorSpan = new ForegroundColorSpan(getResources().getColor(android.R.color.white));
                builder.setSpan(addressColorSpan, spanBegin, spanBegin + keyPair.address.length(), SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE);
            }
            setUrlSpanForAddress("blockexplorer.com", keyPair.address, builder);
            setUrlSpanForAddress("blockchain.info", keyPair.address, builder);
            rawTxDescriptionView.setText(builder);
            rawTxDescriptionView.setMovementMethod(LinkMovementMethod.getInstance());
        }
        sendLayout.setVisibility(keyPair != null ? View.VISIBLE : View.GONE);
    }

    private void setUrlSpanForAddress(String domain, String address, SpannableStringBuilder builder) {
        int spanBegin = builder.toString().indexOf(domain);
        if (spanBegin >= 0) {
            URLSpan urlSpan = new URLSpan("http://" + domain + "/address/" + address);
            builder.setSpan(urlSpan, spanBegin, spanBegin + domain.length(), SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE);
        }
    }

}
