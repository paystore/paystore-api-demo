package br.com.phoebus.payments.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import br.com.phoebus.android.payments.api.ApplicationInfo;
import br.com.phoebus.android.payments.api.ErrorData;
import br.com.phoebus.android.payments.api.Payment;
import br.com.phoebus.android.payments.api.PaymentClient;
import br.com.phoebus.android.payments.api.PaymentRequest;
import br.com.phoebus.android.payments.api.PaymentType;
import br.com.phoebus.android.payments.api.client.Client;
import br.com.phoebus.android.payments.api.exception.ClientException;

public class PaymentActivity extends AppCompatActivity {

    private EditText valueEdt;
    private EditText appTransactionIdEdt;
    private EditText installmentsEdt;

    private List<PaymentType> paymentTypes = new LinkedList<PaymentType>();

    private PaymentClient paymentClient;

    Random r = new Random(System.currentTimeMillis());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(R.string.payment_title);
        setContentView(R.layout.activity_payment);

        this.valueEdt = ((EditText) this.findViewById(R.id.valueEdt));
        this.appTransactionIdEdt = ((EditText) this.findViewById(R.id.appTransactionIdEdt));
        this.installmentsEdt = ((EditText) this.findViewById(R.id.installmentsEdt));

        this.setDefaultValues();

        this.paymentClient = new PaymentClient();
        doBind();
    }

    private void setDefaultValues() {
        this.valueEdt.setText(DataTypeUtils.getAsString(r.nextFloat() * 100F));
        this.appTransactionIdEdt.setText("123456");

        ((CheckBox) this.findViewById(R.id.pay_type_CREDIT)).setChecked(true);
        paymentTypes.add(getPaymentTypeFromCheckBox((CheckBox) this.findViewById(R.id.pay_type_CREDIT)));
    }

    private void doBind() {
        this.paymentClient.bind(this, new Client.OnConnectionCallback() {
            @Override
            public void onConnected() {
                showAlert("Conectado!");
            }

            @Override
            public void onDisconnected(boolean forced) {
                Snackbar.make(installmentsEdt, "Desconectado! " + forced, Snackbar.LENGTH_INDEFINITE)
                        .setAction("Reconectar", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                doBind();
                            }
                        })
                        .show();
            }
        });
    }

    public void doPayment(View view) {

        if (!isDataValid()) return;

        final PaymentRequest pr = new PaymentRequest()
                .withValue(DataTypeUtils.getFromString(this.valueEdt.getText().toString()))
                .withAppTransactionId(this.appTransactionIdEdt.getText().toString())
                .withPaymentTypes(this.paymentTypes)
                .withApplicationInfo(new ApplicationInfo());

        if (this.installmentsEdt.getText() != null && !"".equals(this.installmentsEdt.getText().toString())) {
            pr.setInstallments(Integer.parseInt(this.installmentsEdt.getText().toString()));
        }

        try {
            this.paymentClient.startPayment(pr, new PaymentClient.PaymentCallback<Payment>() {
                @Override
                public void onSuccess(Payment data) {
                    showAlert("Pagamento Realizado!");

                    configureReturnData(data, pr);
                    callResultIntent(data);
                }

                @Override
                public void onError(ErrorData errorData) {
                    showAlert("Pagamento Não Realizado: " + errorData.getPaymentsResponseCode() + " / "
                            + errorData.getAcquirerResponseCode() + " = " + errorData.getResponseMessage());
                }
            });
        } catch (ClientException e) {
            showAlert("Falha na Solicitação: " + e.getMessage());
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

    private void callResultIntent(Payment data) {
        Intent intentResult = new Intent(PaymentActivity.this, ResultActivity.class);
        intentResult.putExtra(ResultActivity.CLIENT_RECEIPT, data.getReceipt().getClientVia());
        intentResult.putExtra(ResultActivity.MERCHANT_RECEIPT, data.getReceipt().getMerchantVia());

        HashMap<String, String> dataMap = new LinkedHashMap<String, String>();
        dataMap.put("Valor", DataTypeUtils.getMoneyAsString(data.getValue()));
        dataMap.put("Tipo de Pagamento", DataTypeUtils.getAsString(data.getPaymentType()));
        dataMap.put("Ident.do Pagamento", data.getPaymentId());
        dataMap.put("Ident. Adquirente", data.getAcquirerId());
        dataMap.put("Número de Aut.", data.getAcquirerAuthorizationNumber());
        dataMap.put("Adquirente", data.getAcquirer());
        dataMap.put("Data/hora Adquirente", DataTypeUtils.getAsString(data.getAcquirerResponseDate()));
        dataMap.put("Data/hora Terminal", DataTypeUtils.getAsString(data.getPaymentDate()));
        dataMap.put("Código de Resposta", data.getAcquirerResponseCode());
        dataMap.put("Forma de Captura", DataTypeUtils.getAsString(data.getCaptureType()));

        if (data.getCard() != null)
            dataMap.put("Cartão", data.getCard().getBin() + "..." + data.getCard().getPanLast4Digits() + " (" + data.getCard().getBrand() + ")");

        dataMap.put("Parcelas", DataTypeUtils.getAsString(data.getInstallments()));

        intentResult.putExtra(ResultActivity.RESPONSE_DATA, dataMap);

        startActivity(intentResult);
    }

    private void configureReturnData(Payment data, PaymentRequest pr) {
        Intent intent = new Intent();
        intent.putExtra(MainActivity.EXTRA_VALUE, data.getValue());
        intent.putExtra(MainActivity.EXTRA_APP_PAYMENT_ID, pr.getAppTransactionId());
        intent.putExtra(MainActivity.EXTRA_PAYMENT_ID, data.getPaymentId());
        setResult(RESULT_OK, intent);
    }

    private void showAlert(String message) {
        Snackbar.make(installmentsEdt, message, Snackbar.LENGTH_LONG).show();
    }

    public void paymentTypeClick(View view) {
        CheckBox cbView = (CheckBox) view;

        if (cbView.isChecked()) {
            paymentTypes.add(getPaymentTypeFromCheckBox(cbView));
        } else {
            paymentTypes.remove(getPaymentTypeFromCheckBox(cbView));
        }
    }

    private PaymentType getPaymentTypeFromCheckBox(CheckBox cbView) {
        String enuName = (String) cbView.getTag();

        return PaymentType.valueOf(enuName);
    }
}
