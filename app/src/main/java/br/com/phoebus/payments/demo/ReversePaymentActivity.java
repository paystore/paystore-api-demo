package br.com.phoebus.payments.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.HashMap;

import br.com.phoebus.android.payments.api.Credentials;
import br.com.phoebus.android.payments.api.ErrorData;
import br.com.phoebus.android.payments.api.Payment;
import br.com.phoebus.android.payments.api.PaymentClient;
import br.com.phoebus.android.payments.api.PaymentRequest;
import br.com.phoebus.android.payments.api.ReversePayment;
import br.com.phoebus.android.payments.api.ReversePaymentRequest;
import br.com.phoebus.android.payments.api.exception.ClientException;

public class ReversePaymentActivity extends AppCompatActivity {

    private EditText paymentTransactionIdEdt;
    private EditText valueEdt;
    private EditText appTransactionIdEdt;

    private PaymentClient paymentClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(R.string.reverse_payment_title);
        setContentView(R.layout.activity_reverse_payment);

        this.paymentTransactionIdEdt = ((EditText) this.findViewById(R.id.paymentTransactionIdEdt));
        this.valueEdt = ((EditText) this.findViewById(R.id.valueEdt));
        this.appTransactionIdEdt = ((EditText) this.findViewById(R.id.appTransactionIdEdt));

        if (getIntent() != null) {
            this.paymentTransactionIdEdt.setText(getIntent().getStringExtra(MainActivity.EXTRA_PAYMENT_ID));
            if (getIntent().getSerializableExtra(MainActivity.EXTRA_VALUE) != null)
                this.valueEdt.setText(((BigDecimal) getIntent().getSerializableExtra(MainActivity.EXTRA_VALUE)).toString());
            this.appTransactionIdEdt.setText(getIntent().getStringExtra(MainActivity.EXTRA_APP_PAYMENT_ID));
        }

        this.paymentClient = new PaymentClient();
        this.paymentClient.bind(this.getApplicationContext());
    }

    public void doReversePayment(View view) {

        if (!isDataValid()) return;

        ReversePaymentRequest pr = new ReversePaymentRequest()
                .withValue(new  BigDecimal(this.valueEdt.getText().toString()))
                .withAppTransactionId(this.appTransactionIdEdt.getText().toString())
                .withPaymentId(this.paymentTransactionIdEdt.getText().toString())
                .withCredentials(new Credentials());

        try {
            this.paymentClient.reversePayment(pr, new PaymentClient.PaymentCallback<ReversePayment>() {
                @Override
                public void onSuccess(ReversePayment data) {
                    Toast.makeText(ReversePaymentActivity.this.getApplicationContext(), "Estorno Realizado!", Toast.LENGTH_SHORT).show();

                    configureReturnData(data);
                    callResultIntent(data);
                }

                @Override
                public void onError(ErrorData errorData) {
                    Toast.makeText(ReversePaymentActivity.this.getApplicationContext(), "Estorno Não Realizado: " + errorData.getPaymentsResponseCode() + " / "
                            + errorData.getAcquirerResponseCode() + " = " + errorData.getResponseMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (ClientException e) {
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

    private void callResultIntent(ReversePayment data) {
        Intent intentResult = new Intent(ReversePaymentActivity.this, ResultActivity.class);
        intentResult.putExtra(ResultActivity.CLIENT_RECEIPT, data.getReceipt().getClientVia());
        intentResult.putExtra(ResultActivity.MERCHANT_RECEIPT, data.getReceipt().getMerchantVia());

        HashMap<String, String> dataMap = new HashMap<String, String>();
        dataMap.put("Ident.do Pagamento", data.getPaymentId());
        dataMap.put("Ident. para a Adquirente", data.getAcquirerId());
        dataMap.put("Número de Autorização", data.getAcquirerAuthorizationNumber());
        dataMap.put("Código de Resposta", data.getAcquirerResponseCode());
        dataMap.put("Data/hora Adquirente", DataTypeUtils.getAsString(data.getAcquirerResponseDate()));
        dataMap.put("Pode ser Desfeito", (data.getCancelable() ? "Sim" : "Não"));

        intentResult.putExtra(ResultActivity.RESPONSE_DATA, dataMap);

        startActivity(intentResult);
    }

    private void configureReturnData(ReversePayment data) {
        Intent intent = new Intent();
        intent.putExtra(MainActivity.EXTRA_REVERSE_PAYMENT_ID, data.getPaymentId());
        setResult(RESULT_OK, intent);
    }
}
