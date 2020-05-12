package br.com.phoebus.payments.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

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
    private CheckBox showReceiptView;

    private PaymentClient paymentClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(R.string.reverse_payment_title);
        setContentView(R.layout.activity_reverse_payment);

        this.paymentTransactionIdEdt = ((EditText) this.findViewById(R.id.paymentTransactionIdEdt));
        this.valueEdt = ((EditText) this.findViewById(R.id.valueEdt));
        this.appTransactionIdEdt = ((EditText) this.findViewById(R.id.appTransactionIdEdt));
        this.showReceiptView = (CheckBox) this.findViewById(R.id.chb_showReceiptView);
        this.showReceiptView.setChecked(true);

        if (getIntent() != null) {
            this.paymentTransactionIdEdt.setText(getIntent().getStringExtra(Helper.EXTRA_PAYMENT_ID));
            if (getIntent().getSerializableExtra(Helper.EXTRA_VALUE) != null)
                this.valueEdt.setText(new BigDecimal(getIntent().getSerializableExtra(Helper.EXTRA_VALUE).toString()).setScale(2).toString());
            this.appTransactionIdEdt.setText(getIntent().getStringExtra(Helper.EXTRA_APP_PAYMENT_ID));
        }

        this.paymentClient = new PaymentClient();
        this.paymentClient.bind(this.getApplicationContext());
    }

    public void doReversePayment(View view) {

        if (!isDataValid()) return;

        ReversePaymentRequestV2 pr = new ReversePaymentRequestV2()
                .withValue(new  BigDecimal(this.valueEdt.getText().toString()))
                .withAppTransactionId(this.appTransactionIdEdt.getText().toString())
                .withPaymentId(this.paymentTransactionIdEdt.getText().toString())
                .withCredentials(CredentialsUtils.getMyCredentials())
                .withShowReceiptView(this.showReceiptView.isChecked());

        try {
            this.paymentClient.reversePaymentV2(pr, new PaymentClient.PaymentCallback<ReversePayment>() {
                @Override
                public void onSuccess(ReversePayment data) {
                    configureReturnData(data);
                }

                @Override
                public void onError(ErrorData errorData) {
                    Toast.makeText(ReversePaymentActivity.this.getApplicationContext(), "Estorno Não Realizado: " + errorData.getPaymentsResponseCode() + " / "
                            + errorData.getAcquirerResponseCode() + " = " + errorData.getResponseMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(ReversePaymentActivity.this.getApplicationContext(), "Falha na chamada do serviço: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
