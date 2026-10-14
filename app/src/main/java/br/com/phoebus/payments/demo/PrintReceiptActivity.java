package br.com.phoebus.payments.demo;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import br.com.phoebus.android.payments.api.ErrorData;
import br.com.phoebus.android.payments.api.PaymentClient;
import br.com.phoebus.android.payments.api.PrintReceiptRequest;
import br.com.phoebus.android.payments.api.client.Client;
import br.com.phoebus.android.payments.api.exception.ClientException;
import br.com.phoebus.payments.demo.utils.AlertUtils;
import br.com.phoebus.payments.demo.utils.CredentialsUtils;
import br.com.phoebus.payments.demo.utils.Helper;

public class PrintReceiptActivity extends AppCompatActivity {

    private EditText paymentTransactionIdEdt;
    private CheckBox chbReceiptMerchant, chbReceiptCustomer;
    private CheckBox previewReceiptMerchant, previewReceiptCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_receipt);

        this.setTitle(getString(R.string.print_api));

        this.paymentTransactionIdEdt = findViewById(R.id.paymentTransactionIdEdt);
        this.chbReceiptMerchant = findViewById(R.id.chbReceiptMerchant);
        this.chbReceiptCustomer = findViewById(R.id.chbReceiptCustomer);
        this.previewReceiptCustomer = findViewById(R.id.previewReceiptCustomer);
        this.previewReceiptMerchant = findViewById(R.id.previewReceiptMerchant);

        try {
            if (getIntent().hasExtra(Helper.EXTRA_PAYMENT_ID)) {
                paymentTransactionIdEdt.setText(getIntent().getStringExtra(Helper.EXTRA_PAYMENT_ID));
            }
        } catch (Exception e) {
            Log.e(ReprintActivity.class.toString(), e.getMessage());
        }
    }

    private boolean isDataValid() {

        boolean ret = true;

        if ("".equals(this.paymentTransactionIdEdt.getText().toString())) {
            this.paymentTransactionIdEdt.setError(getString(R.string.requieredFieldError));
            ret = false;
        }

        return ret;
    }

    public void printReceipt(View view) {
        if (!isDataValid()) return;

        PaymentClient mPaymentClient = new PaymentClient();

        PrintReceiptRequest printReceiptRequest = new PrintReceiptRequest();

        try {
            printReceiptRequest.setApplicationInfo(CredentialsUtils.getMyAppInfo(this.getPackageManager(), this.getPackageName()));
            printReceiptRequest.setPaymentId(paymentTransactionIdEdt.getText().toString());
            printReceiptRequest.setPrintCustomerReceipt(chbReceiptCustomer.isChecked());
            printReceiptRequest.setPrintMerchantReceipt(chbReceiptMerchant.isChecked());
            printReceiptRequest.setPreviewCustomerReceipt(previewReceiptCustomer.isChecked());
            printReceiptRequest.setPreviewMerchantReceipt(previewReceiptMerchant.isChecked());

        } catch (PackageManager.NameNotFoundException e) {
            showSnackBar(getString(R.string.requestFailed) +": " + e.getMessage());
            return;
        }

        mPaymentClient.bind(this, new Client.OnConnectionCallback() {
            @Override
            public void onConnected() {
                try {
                    mPaymentClient.printReceipt(printReceiptRequest, new PaymentClient.PaymentCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("PayStore App Demo", "Print has finished successfully!");
                        }

                        @Override
                        public void onError(ErrorData errorData) {
                            if (errorData != null && !TextUtils.isEmpty(errorData.getResponseMessage())) {
                                AlertUtils.showSnackBar(findViewById(android.R.id.content), errorData.getResponseMessage());
                            }
                            Log.e("PayStore App Demo", "Print has finished wrongfully!");
                        }
                    });
                } catch (ClientException e) {
                    if (!TextUtils.isEmpty(e.getMessage())) {
                        AlertUtils.showSnackBar(findViewById(android.R.id.content), e.getMessage());
                    }
                }
            }

            @Override
            public void onDisconnected(boolean b) {
                Log.d("PayStore App Demo", "Print disconnected!");
            }
        });
    }

    private void showSnackBar(String message) {
        AlertUtils.showSnackBar(this.findViewById(android.R.id.content), message);
    }
}