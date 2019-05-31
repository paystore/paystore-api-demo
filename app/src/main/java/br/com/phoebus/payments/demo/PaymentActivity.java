package br.com.phoebus.payments.demo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import br.com.phoebus.android.payments.api.ErrorData;
import br.com.phoebus.android.payments.api.Payment;
import br.com.phoebus.android.payments.api.PaymentClient;
import br.com.phoebus.android.payments.api.PaymentRequest;
import br.com.phoebus.android.payments.api.PaymentRequestV2;
import br.com.phoebus.android.payments.api.PaymentType;
import br.com.phoebus.android.payments.api.PaymentV2;
import br.com.phoebus.android.payments.api.client.Client;
import br.com.phoebus.android.payments.api.exception.ClientException;
import br.com.phoebus.payments.demo.utils.CredentialsUtils;
import br.com.phoebus.payments.demo.utils.DataTypeUtils;

public class PaymentActivity extends AppCompatActivity {

    private EditText valueEdt;
    private EditText appTransactionIdEdt;
    private EditText installmentsEdt;
    private EditText emailToken;
    private CheckBox showReceiptView;

    private List<PaymentType> paymentTypes = new LinkedList<PaymentType>();

    private PaymentClient paymentClient;

    Random r = new Random(System.currentTimeMillis());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(R.string.payment_title);
        setContentView(R.layout.activity_payment);

        this.valueEdt = (EditText) this.findViewById(R.id.valueEdt);
        this.appTransactionIdEdt = (EditText) this.findViewById(R.id.appTransactionIdEdt);
        this.installmentsEdt = (EditText) this.findViewById(R.id.installmentsEdt);
        this.showReceiptView = (CheckBox) this.findViewById(R.id.chb_showReceiptView);
        this.emailToken = (EditText) this.findViewById(R.id.email_token);

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

        final PaymentRequest pr;
        try {
            pr = new PaymentRequest()
                    .withValue(DataTypeUtils.getFromString(this.valueEdt.getText().toString()))
                    .withAppTransactionId(this.appTransactionIdEdt.getText().toString())
                    .withPaymentTypes(this.paymentTypes)
                    .withApplicationInfo(CredentialsUtils.getMyAppInfo(this.getPackageManager(), this.getPackageName()))
                    .withShowReceiptView(this.showReceiptView.isChecked());
        } catch (PackageManager.NameNotFoundException e) {
            showAlert("Falha na Solicitação: " + e.getMessage());
            return;
        }

        if (this.installmentsEdt.getText() != null && !"".equals(this.installmentsEdt.getText().toString())) {
            pr.setInstallments(Integer.parseInt(this.installmentsEdt.getText().toString()));
        }

        try {
            this.paymentClient.startPayment(pr, new PaymentClient.PaymentCallback<Payment>() {
                @Override
                public void onSuccess(Payment data) {
                    showAlert("Pagamento Realizado!");

                    configureReturnData(data, pr);
                    ResultActivity.callResultIntent(data, PaymentActivity.this, 0);
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

    public void doPaymentV2(View view) {


        if (!isDataValid()) return;


        final PaymentRequestV2 pr;
        try {
            pr = new PaymentRequestV2();
            pr.setValue(DataTypeUtils.getFromString(this.valueEdt.getText().toString()));
            pr.setAppTransactionId(this.appTransactionIdEdt.getText().toString());
            pr.setPaymentTypes(this.paymentTypes);
            pr.setAppInfo(CredentialsUtils.getMyAppInfo(this.getPackageManager(), this.getPackageName()));
            pr.setShowReceiptView(this.showReceiptView.isChecked());
            pr.setTokenizeCard(true);
            pr.setTokenizeEmail(String.valueOf(this.emailToken.getText()));

        } catch (PackageManager.NameNotFoundException e) {
            showAlert("Falha na Solicitação: " + e.getMessage());
            return;
        }

        if (this.installmentsEdt.getText() != null && !"".equals(this.installmentsEdt.getText().toString())) {
            pr.setInstallments(Integer.parseInt(this.installmentsEdt.getText().toString()));
        }

        try {
            this.paymentClient.startPaymentV2(pr, new PaymentClient.PaymentCallback<PaymentV2>() {
                @Override
                public void onSuccess(PaymentV2 data) {
                    showAlert("Pagamento Realizado!");

                    configureReturnData(data, pr);
                    ResultActivity.callResultIntent(data, PaymentActivity.this, 0);
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

    private void configureReturnData(Payment data, PaymentRequest pr) {
        Intent intent = new Intent();
        intent.putExtra(MainActivity.EXTRA_VALUE, data.getValue());
        intent.putExtra(MainActivity.EXTRA_APP_PAYMENT_ID, pr.getAppTransactionId());
        intent.putExtra(MainActivity.EXTRA_PAYMENT_ID, data.getPaymentId());
        setResult(RESULT_OK, intent);
    }

    private void configureReturnData(PaymentV2 data, PaymentRequest pr) {
        Intent intent = new Intent();
        intent.putExtra(MainActivity.EXTRA_VALUE, data.getValue());
        intent.putExtra(MainActivity.EXTRA_APP_PAYMENT_ID, pr.getAppTransactionId());
        intent.putExtra(MainActivity.EXTRA_PAYMENT_ID, data.getPaymentId());
        setResult(RESULT_OK, intent);
    }

    private void showAlert(String message) {
        Snackbar.make(this.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
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
