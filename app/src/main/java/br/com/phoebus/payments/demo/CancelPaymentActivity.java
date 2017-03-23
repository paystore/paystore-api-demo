package br.com.phoebus.payments.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import br.com.phoebus.android.payments.api.ErrorData;
import br.com.phoebus.android.payments.api.PaymentClient;
import br.com.phoebus.android.payments.api.exception.ClientException;

public class CancelPaymentActivity extends AppCompatActivity {

    private PaymentClient paymentClient;

    private EditText appTransactionIdEdt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(R.string.cancel_payment_title);
        setContentView(R.layout.activity_cancel_payment);

        this.appTransactionIdEdt = ((EditText) this.findViewById(R.id.appTransactionIdEdt));

        if (getIntent() != null) {
            this.appTransactionIdEdt.setText(getIntent().getStringExtra(MainActivity.EXTRA_PAYMENT_ID));
        }

        this.paymentClient = new PaymentClient();
        this.paymentClient.bind(this.getApplicationContext());
    }

    public void doCancelPayment(View view) {

        if (!isDataValid()) return;

        try {
            this.paymentClient.cancelPayment(this.appTransactionIdEdt.getText().toString(), new PaymentClient.PaymentCallback() {
                @Override
                public void onSuccess(Object data) {

                    Toast.makeText(CancelPaymentActivity.this, "Desfazimento Realizado!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(ErrorData errorData) {

                    Toast.makeText(CancelPaymentActivity.this, "Desfazimento Não Realizado: " + errorData.getPaymentsResponseCode() + " / "
                            + errorData.getAcquirerResponseCode() + " = " + errorData.getResponseMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (ClientException e) {
            e.printStackTrace();
            Toast.makeText(CancelPaymentActivity.this, "Falha na chamada do serviço: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    private boolean isDataValid() {

        boolean ret = true;

        if ("".equals(this.appTransactionIdEdt.getText().toString())) {
            this.appTransactionIdEdt.setError(getString(R.string.requieredFieldError));
            ret = false;
        }

        return ret;
    }

}
