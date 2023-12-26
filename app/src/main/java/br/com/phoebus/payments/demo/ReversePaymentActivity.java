package br.com.phoebus.payments.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;

import br.com.phoebus.android.payments.api.ErrorData;
import br.com.phoebus.android.payments.api.PaymentClient;
import br.com.phoebus.android.payments.api.ReversePayment;
import br.com.phoebus.android.payments.api.ReversePaymentRequestV2;
import br.com.phoebus.payments.demo.utils.CredentialsUtils;
import br.com.phoebus.payments.demo.utils.Helper;

public class ReversePaymentActivity extends AppCompatActivity {

    private EditText paymentTransactionIdEdt;
    private EditText valueEdt;
    private EditText appTransactionIdEdt;
    private CheckBox chbReceiptMerchant;
    private CheckBox chbReceiptCustomer;
    private Button doReversePayment;
    private CheckBox chbPreviewMerchant;
    private CheckBox chbPreviewCustomer;

    private PaymentClient paymentClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(R.string.reverse_payment_title);
        setContentView(R.layout.activity_reverse_payment);

        this.paymentTransactionIdEdt = ((EditText) this.findViewById(R.id.paymentTransactionIdEdt));
        this.valueEdt = ((EditText) this.findViewById(R.id.valueEdt));
        this.appTransactionIdEdt = ((EditText) this.findViewById(R.id.appTransactionIdEdt));
        this.chbReceiptMerchant = findViewById(R.id.chbReceiptMerchant);
        this.chbReceiptCustomer = findViewById(R.id.chbReceiptCustomer);
        this.doReversePayment = findViewById(R.id.doPaymentBtn);
        this.chbPreviewCustomer = findViewById(R.id.chbPreviewCustomer);
        this.chbPreviewMerchant = findViewById(R.id.chbPreviewMerchant);

        setDefaultValues();

        if (getIntent() != null) {
            this.paymentTransactionIdEdt.setText(getIntent().getStringExtra(Helper.EXTRA_PAYMENT_ID));
            if (getIntent().getSerializableExtra(Helper.EXTRA_VALUE) != null)
                this.valueEdt.setText(new BigDecimal(getIntent().getSerializableExtra(Helper.EXTRA_VALUE).toString()).setScale(2).toString());
            this.appTransactionIdEdt.setText(getIntent().getStringExtra(Helper.EXTRA_APP_PAYMENT_ID));
        }

        this.paymentClient = new PaymentClient();
        this.paymentClient.bind(this.getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        doReversePayment.setEnabled(true);
    }

    private void setDefaultValues() {
        this.chbReceiptCustomer.setChecked(true);
        this.chbReceiptMerchant.setChecked(true);
        this.chbPreviewCustomer.setChecked(true);
        this.chbPreviewMerchant.setChecked(true);
    }

    public void doReversePayment(View view) {

        if (!isDataValid()) return;

        ReversePaymentRequestV2 pr = new ReversePaymentRequestV2()
                .withValue(new  BigDecimal(this.valueEdt.getText().toString()))
                .withAppTransactionId(this.appTransactionIdEdt.getText().toString())
                .withPaymentId(this.paymentTransactionIdEdt.getText().toString())
                .withCredentials(CredentialsUtils.getMyCredentials());
        pr.setPrintCustomerReceipt(chbReceiptCustomer.isChecked());
        pr.setPrintMerchantReceipt(chbReceiptMerchant.isChecked());
        pr.setPreviewCustomerReceipt(chbPreviewCustomer.isChecked());
        pr.setPreviewMerchantReceipt(chbPreviewMerchant.isChecked());

        if(view.isPressed()) {
            view.setEnabled(false);
            try {
                this.paymentClient.reversePaymentV2(pr, new PaymentClient.PaymentCallback<ReversePayment>() {
                    @Override
                    public void onSuccess(ReversePayment data) {
                        if (data.getCancelable()) {
                            Helper.writePrefs(getApplicationContext(), Helper.KEY_LAST_CANCELABLE_REVERSE_ID, data.getPaymentId(), Helper.PREF_CONFIG);
                        }
                        configureReturnData(data);
                        ResultActivity.callResultIntent(data, ReversePaymentActivity.this, 0);
                    }

                    @Override
                    public void onError(ErrorData errorData) {
                        Toast.makeText(ReversePaymentActivity.this.getApplicationContext(), getString(R.string.reverse_filter_RefundFailed) + ": " + errorData.getPaymentsResponseCode() + " / "
                                + errorData.getAcquirerResponseCode() + " = " + errorData.getResponseMessage(), Toast.LENGTH_SHORT).show();
                        view.setEnabled(true);
                    }
                });
            } catch (Exception e) {
                Toast.makeText(ReversePaymentActivity.this.getApplicationContext(), getString(R.string.serviceCallFailed) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isDataValid() {

        boolean ret = true;

        if ("".equals(this.paymentTransactionIdEdt.getText().toString())) {
            this.paymentTransactionIdEdt.setError(getString(R.string.requieredFieldError));
            ret = false;
        }

        if ("".equals(this.valueEdt.getText().toString())) {
            this.valueEdt.setError(getString(R.string.requieredFieldError));
            ret = false;
        }

        if ("".equals(this.appTransactionIdEdt.getText().toString())) {
            this.appTransactionIdEdt.setError(getString(R.string.requieredFieldError));
            ret = false;
        }

        return ret;
    }

    private void configureReturnData(ReversePayment data) {
        Intent intent = new Intent();
        intent.putExtra(Helper.EXTRA_REVERSE_PAYMENT_ID, data.getPaymentId());
        setResult(RESULT_OK, intent);
    }
}
