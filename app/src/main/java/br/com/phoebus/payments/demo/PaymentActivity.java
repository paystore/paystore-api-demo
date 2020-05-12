package br.com.phoebus.payments.demo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.List;
import java.util.Random;

import br.com.phoebus.android.payments.api.ErrorData;
import br.com.phoebus.android.payments.api.PaymentClient;
import br.com.phoebus.android.payments.api.PaymentRequestV2;
import br.com.phoebus.android.payments.api.PaymentType;
import br.com.phoebus.android.payments.api.PaymentV2;
import br.com.phoebus.android.payments.api.client.Client;
import br.com.phoebus.payments.demo.utils.AlertUtils;
import br.com.phoebus.payments.demo.utils.CredentialsUtils;
import br.com.phoebus.payments.demo.utils.DataTypeUtils;
import br.com.phoebus.payments.demo.utils.Helper;

public class PaymentActivity extends AppCompatActivity {

    private EditText valueEdt;
    private EditText appTransactionIdEdt;
    private EditText installmentsEdt;
    private EditText emailToken;
    private CheckBox showReceiptView;

    private List<PaymentType> paymentTypes;

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
        //recebendo a lista com os tipos de pagamento previamente selecionados na tela PaymentTypeListActivity
        if (getIntent() != null && getIntent().getExtras() != null)
            paymentTypes = (List<PaymentType>) getIntent().getExtras().getSerializable(Helper.EXTRA_LIST_PAYMENT_TYPE);

        doBind();
    }

    private void setDefaultValues() {

        this.valueEdt.setText(DataTypeUtils.getAsString(r.nextFloat() * 100F));
        this.appTransactionIdEdt.setText(Helper.APP_TRANSACTION_ID);
        showReceiptView.setChecked(true);

    }

    private void doBind() {
        this.paymentClient.bind(this, new Client.OnConnectionCallback() {
            @Override
            public void onConnected() {
                showSnackBar("Conectado!");
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

    public void doPaymentV2(View view) {

        if (!isDataValid()) return;

        final PaymentRequestV2 paymentRequestV2;
        try {
            paymentRequestV2 = new PaymentRequestV2();
            paymentRequestV2.setValue(DataTypeUtils.getFromString(this.valueEdt.getText().toString()));
            paymentRequestV2.setAppTransactionId(this.appTransactionIdEdt.getText().toString());
            paymentRequestV2.setPaymentTypes(this.paymentTypes);
            paymentRequestV2.setAppInfo(CredentialsUtils.getMyAppInfo(this.getPackageManager(), this.getPackageName()));
            paymentRequestV2.setShowReceiptView(this.showReceiptView.isChecked());
            paymentRequestV2.setTokenizeCard(false);
            paymentRequestV2.setTokenizeEmail(String.valueOf(this.emailToken.getText()));

        } catch (PackageManager.NameNotFoundException e) {
            showSnackBar("Falha na Solicitação: " + e.getMessage());
            return;
        }

        if (this.installmentsEdt.getText() != null && !"".equals(this.installmentsEdt.getText().toString())) {
            paymentRequestV2.setInstallments(Integer.parseInt(this.installmentsEdt.getText().toString()));
        }

        boolean isPaymentEndToEnd = getIntent().getExtras() != null && getIntent().getExtras().getBoolean(MainActivity.EXTRA_IS_PAYMENT_END_TO_PAYMENT);
        if (!isPaymentEndToEnd)
        {
            try {
                this.paymentClient.startPaymentV2(paymentRequestV2, new PaymentClient.PaymentCallback<PaymentV2>() {
                    @Override
                    public void onSuccess(PaymentV2 data) {
                        showSnackBar("Pagamento Realizado!");

                        configureReturnData(data, paymentRequestV2);
                        ResultActivity.callResultIntent(data, PaymentActivity.this, 0);
                    }

                    @Override
                    public void onError(ErrorData errorData) {
                        showSnackBar("Pagamento Não Realizado: " + errorData.getPaymentsResponseCode() + " / "
                                + errorData.getAcquirerResponseCode() + " = " + errorData.getResponseMessage());
                    }
                });
            } catch (Exception e) {
                showSnackBar("Falha na Solicitação: " + e.getMessage());
            }
        }
        else {
            PaymentEndToEnd paymentEndToEnd = new PaymentEndToEnd(this, paymentClient);
            paymentEndToEnd.doPayment(paymentRequestV2);
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

    private void configureReturnData(PaymentV2 data, PaymentRequestV2 paymentRequestV2) {
        Intent intent = new Intent();
        intent.putExtra(MainActivity.EXTRA_VALUE, data.getValue());
        intent.putExtra(MainActivity.EXTRA_APP_PAYMENT_ID, paymentRequestV2.getAppTransactionId());
        intent.putExtra(MainActivity.EXTRA_PAYMENT_ID, data.getPaymentId());
        setResult(RESULT_OK, intent);
    }

    private void showSnackBar(String message) {
        AlertUtils.showSnackBar(this.findViewById(android.R.id.content), message);
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
