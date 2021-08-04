package br.com.phoebus.payments.demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import br.com.phoebus.android.payments.api.ApplicationInfo;
import br.com.phoebus.android.payments.api.ErrorData;
import br.com.phoebus.android.payments.api.PaymentClient;
import br.com.phoebus.android.payments.api.Refund;
import br.com.phoebus.android.payments.api.RefundRequest;
import br.com.phoebus.android.payments.api.client.Client;
import br.com.phoebus.payments.demo.utils.CredentialsUtils;
import br.com.phoebus.payments.demo.utils.CurrencyWatcher;
import br.com.phoebus.payments.demo.utils.DataTypeUtils;
import br.com.phoebus.payments.demo.utils.Helper;

public class RefundActivity extends AppCompatActivity {
    private Spinner productShortNameSpinner;
    private EditText refundValue;
    private CheckBox printMerchantReceipt;
    private CheckBox printCustomerReceipt;
    private String selectedProductShortName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(R.string.paymentsFilter_unreferenced_devolution);
        setContentView(R.layout.activity_refund);

        productShortNameSpinner = findViewById(R.id.productShortNameRefund);
        refundValue = findViewById(R.id.refundValue);
        printCustomerReceipt = findViewById(R.id.showCustomerReceiptView);
        printMerchantReceipt = findViewById(R.id.showMerchantReceiptView);
        setupProductShortNameSpinner();

        refundValue.addTextChangedListener(new CurrencyWatcher(refundValue, false));
    }

    private void setupProductShortNameSpinner() {
        List<String> shortNames = new ArrayList<>();
        shortNames.add("");
        shortNames.add("VISA");
        shortNames.add("MASTERCARD");
        shortNames.add("AMEX");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, shortNames);
        this.productShortNameSpinner.setAdapter(adapter);
        this.productShortNameSpinner.setOnItemSelectedListener(new OnSelectProductShortName());
    }

    public void doRefund(View view) {
        try {
            PaymentClient paymentClient = new PaymentClient();
            ApplicationInfo applicationInfo = CredentialsUtils.getMyAppInfo(this.getPackageManager(), this.getPackageName());
            RefundRequest refundRequest = new RefundRequest();

            refundRequest.setApplicationInfo(applicationInfo);
            refundRequest.setProductShortName(selectedProductShortName);
            refundRequest.setValue(DataTypeUtils.getFromString(this.refundValue.getText().toString()));
            refundRequest.setPrintClientReceipt(printCustomerReceipt.isChecked());
            refundRequest.setPrintMerchantReceipt(printMerchantReceipt.isChecked());
            paymentClient.bind(this, new Client.OnConnectionCallback() {
                @Override
                public void onConnected() {
                    try {
                        paymentClient.refundPayment(refundRequest, new PaymentClient.PaymentCallback<Refund>() {
                            @Override
                            public void onSuccess(Refund refund) {
                                configureReturnData(refund);
                                ResultActivity.callResultIntent(refund, RefundActivity.this, 0);
                                Log.i("refund- acquirerid", refund.getAcquirerId() == null ? " " : refund.getAcquirerId());
                                Log.i("refund- add message", refund.getAcquirerAdditionalMessage() == null ? " " : refund.getAcquirerAdditionalMessage());
                                Log.i("refund- auth number", refund.getAcquirerAuthorizationNumber() == null ? " " : refund.getAcquirerAuthorizationNumber());
                                Log.i("refund- acqu resp code", refund.getAcquirerResponseCode() == null ? " " : refund.getAcquirerResponseCode());
                                Log.i("refund- batch number", refund.getBatchNumber() == null ? " " : refund.getBatchNumber());
                                Log.i("refund- card bin", refund.getCardBin() == null ? " " : refund.getCardBin());
                                Log.i("refund- card hold name", refund.getCardHolderName() == null ? " " : refund.getCardHolderName());
                                Log.i("refund- client via", refund.getReceipt().getClientVia() == null ? " " : refund.getReceipt().getClientVia());
                                Log.i("refund- merchant via", refund.getReceipt().getMerchantVia() == null ? " " : refund.getReceipt().getMerchantVia());
                                Log.i("refund- nsu terminal", refund.getNsuTerminal() == null ? " " : refund.getNsuTerminal());
                                Log.i("refund- last 4 digits", refund.getPanLast4Digits() == null ? " " : refund.getPanLast4Digits());
                                Log.i("refund- prod short name", refund.getProductShortName() == null ? " " : refund.getProductShortName());
                                Log.i("refund- terminal id", refund.getTerminalId() == null ? " " : refund.getTerminalId());
                                Log.i("refund- ticket number", String.valueOf(refund.getTicketNumber()) == null ? " " : String.valueOf(refund.getTicketNumber()));
                            }

                            @Override
                            public void onError(ErrorData errorData) {
                                Toast.makeText(getApplicationContext(), errorData.getResponseMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }

                }

                @Override
                public void onDisconnected(boolean b) {

                }
            });
        } catch (Exception e) {
            Log.e("Algo aconteceu: ", e.getMessage());
        }
    }

    private void configureReturnData(Refund data) {
        Intent intent = new Intent();
        intent.putExtra(Helper.EXTRA_REFUND_PAYMENT_ID, data.getRefundId());
        setResult(RESULT_OK, intent);
    }

    private class OnSelectProductShortName implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
            switch (pos) {
                case 1:
                    selectedProductShortName = "VI";
                    break;
                case 2:
                    selectedProductShortName = "MC";
                    break;
                case 3:
                    selectedProductShortName = "AM";
                    break;
                default:
                    selectedProductShortName = "";
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    }

}
