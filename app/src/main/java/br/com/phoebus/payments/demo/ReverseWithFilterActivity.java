package br.com.phoebus.payments.demo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import br.com.phoebus.android.payments.api.ApplicationInfo;
import br.com.phoebus.android.payments.api.ErrorData;
import br.com.phoebus.android.payments.api.PaymentClient;
import br.com.phoebus.android.payments.api.ReversePayment;
import br.com.phoebus.android.payments.api.ReversePaymentFilterRequest;
import br.com.phoebus.android.payments.api.client.Client;
import br.com.phoebus.payments.demo.utils.AlertUtils;
import br.com.phoebus.payments.demo.utils.CredentialsUtils;
import br.com.phoebus.payments.demo.utils.CurrencyWatcher;
import br.com.phoebus.payments.demo.utils.DataTypeUtils;
import br.com.phoebus.payments.demo.utils.Helper;
import br.com.phoebus.payments.demo.utils.MoneyWatcher;

public class ReverseWithFilterActivity extends AppCompatActivity {

    private PaymentClient paymentClient;
    private String selectedProductShortName = "";
    private int selectedOperationMethod = 99;
    private EditText edtValue;
    private Spinner productShortNameSpinner;
    private EditText qrId;
    private EditText edtTicketNumber;
    private Spinner operationMethodSpinner;
    private CheckBox chbReceiptMerchant;
    private CheckBox chbReceiptCustomer;
    private EditText day;
    private EditText moth;
    private EditText year;
    private EditText hour;
    private EditText minute;
    private EditText second;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(R.string.reverse_without_id_title);
        setContentView(R.layout.activity_reverse_with_filter);

        this.edtValue = findViewById(R.id.edtValue);
        this.qrId = findViewById(R.id.qrId);
        this.productShortNameSpinner = (Spinner) findViewById(R.id.productShortNameReverse);
        this.edtTicketNumber = findViewById(R.id.edtTicketNumber);
        this.chbReceiptMerchant = findViewById(R.id.chbReceiptMerchant);
        this.chbReceiptCustomer = findViewById(R.id.chbReceiptCustomer);
        this.operationMethodSpinner = (Spinner) findViewById(R.id.operationMethodReverse);
        this.day = (EditText) findViewById(R.id.transactionDay);
        this.moth = (EditText) findViewById(R.id.transactionMonth);
        this.year = (EditText) findViewById(R.id.transactionYear);
        this.hour = (EditText) findViewById(R.id.transactionHour);
        this.minute = (EditText) findViewById(R.id.transactionMinute);
        this.second = (EditText) findViewById(R.id.transactionSecond);

        this.edtValue.addTextChangedListener(new CurrencyWatcher(edtValue, false));
        setupProductShortNameSpinner();
        setupOperationMethodSpinner();
        this.paymentClient = new PaymentClient();
    }

    public void doReverseWithoutIdPayment(View view) {
        try {
            ApplicationInfo applicationInfo = CredentialsUtils.getMyAppInfo(this.getPackageManager(), this.getPackageName());
            ReversePaymentFilterRequest request = new ReversePaymentFilterRequest();

            request.setApplicationInfo(applicationInfo);
            request.setSoftwareVersion(applicationInfo.getSoftwareVersion());
            request.setCredentials(applicationInfo.getCredentials());
            request.setValue(DataTypeUtils.getFromString(edtValue.getText().toString()));
            request.setPrintCustomerReceipt(chbReceiptCustomer.isChecked());
            request.setPrintMerchantReceipt(chbReceiptMerchant.isChecked());
            request.setTicketNumber(edtTicketNumber.getText().toString());
            request.setOriginalQRId(qrId.getText().toString());
            request.setProductShortName(selectedProductShortName);
            request.setOperationMethod(selectedOperationMethod);

            Date paymentDate = new Date();
            EditText[] fields = {day, moth, year, hour, minute, second};
            if (isDateFilled(fields)) {
                if (isValid(fields)) {
                    paymentDate.setDate(Integer.parseInt((day).getText().toString()));
                    paymentDate.setMonth(Integer.parseInt((moth).getText().toString()) - 1);
                    paymentDate.setYear(Integer.parseInt((year).getText().toString()) - 1900);
                    paymentDate.setHours(Integer.parseInt((hour).getText().toString()));
                    paymentDate.setMinutes(Integer.parseInt((minute).getText().toString()));
                    paymentDate.setSeconds(Integer.parseInt((second).getText().toString()));
                    paymentDate.setTime(paymentDate.getTime() - Long.parseLong(String.valueOf(paymentDate.getTime()).substring(String.valueOf(paymentDate.getTime()).length() - 3)));
                    request.setPaymentDate(paymentDate);
                } else {
                    AlertUtils.showSnackBar(this.findViewById(android.R.id.content), getString(R.string.requieredFieldDateError));
                    return;
                }
            }

            paymentClient.bind(this, new Client.OnConnectionCallback() {

                @Override
                public void onConnected() {
                    try {
                        paymentClient.reversePaymentWithFilter(request, new PaymentClient.PaymentCallback<ReversePayment>() {
                            @Override
                            public void onSuccess(ReversePayment data) {
                                configureReturnData(data);
                                ResultActivity.callResultIntent(data, ReverseWithFilterActivity.this, 0);
                                Log.i("Reverse- paymentid", data.getPaymentId() == null ? " " : data.getPaymentId());
                                Log.i("Reverse- acquirerid", data.getAcquirerId() == null ? " " : data.getAcquirerId());
                                Log.i("Reverse- cancelable", String.valueOf(data.getCancelable()));
                                Log.i("Reverse-acqResponseCode", data.getAcquirerResponseCode() == null ? " " : data.getAcquirerResponseCode());
                                Log.i("Reverse-AcResponseCode", data.getAcquirerResponseCode() == null ? " " : data.getAcquirerResponseCode());
                                Log.i("Reverse-AcResponseNum", data.getAcquirerAuthorizationNumber() == null ? " " : data.getAcquirerAuthorizationNumber());
                                Log.i("Reverse-acqAddMessage", data.getAcquirerAdditionalMessage() == null ? " " : data.getAcquirerAdditionalMessage());
                                Log.i("Reverse-receiptClient", data.getReceipt().getClientVia() == null ? " " : data.getReceipt().getClientVia());
                                Log.i("Reverse-receiptMerchant", data.getReceipt().getMerchantVia() == null ? " " : data.getReceipt().getMerchantVia());
                                Log.i("Reverse-batchNumber", data.getBatchNumber() == null ? " " : data.getBatchNumber());
                                Log.i("Reverse-NSUTerminal", data.getNsuTerminal() == null ? " " : data.getNsuTerminal());
                                Log.i("Reverse-CardHolderName", data.getCardHolderName() == null ? " " : data.getCardHolderName());
                                Log.i("Reverse-CardBin", data.getCardBin() == null ? " " : data.getCardBin());
                                Log.i("Reverse-panLast4Dig", data.getPanLast4Digits() == null ? " " : data.getPanLast4Digits());
                                Log.i("Reverse-TerminalId", data.getTerminalId() == null ? " " : data.getTerminalId());
                            }

                            @Override
                            public void onError(ErrorData errorData) {
                                Toast.makeText(ReverseWithFilterActivity.this.getApplicationContext(), "Estorno Não Realizado: " + errorData.getPaymentsResponseCode() + " / "
                                        + errorData.getAcquirerResponseCode() + " = " + errorData.getResponseMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        Toast.makeText(ReverseWithFilterActivity.this.getApplicationContext(), "Falha na chamada do serviço: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onDisconnected(boolean b) {

                }
            });
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void configureReturnData(ReversePayment data) {
        Intent intent = new Intent();
        intent.putExtra(Helper.EXTRA_REVERSE_PAYMENT_ID, data.getPaymentId());
        setResult(RESULT_OK, intent);
    }

    private boolean isValid(EditText[] fields) {
        int count = 0;
        for (EditText f : fields) {
            if (f.getText().toString().trim().length() > 0) {
                count += 1;
            }
        }
        if (count < fields.length) {
            return false;
        }
        return true;
    }

    private boolean isDateFilled(EditText[] fields) {
        for (EditText text : fields) {
            if (!TextUtils.isEmpty(text.getText())) {
                return true;
            }
        }
        return false;
    }

    private void setupProductShortNameSpinner() {
        List<String> shortNames = Arrays.asList("", "VISA", "MASTERCARD","AMEX");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, shortNames);
        this.productShortNameSpinner.setAdapter(adapter);
        this.productShortNameSpinner.setOnItemSelectedListener(new OnSelectProductShortName());
    }

    private void setupOperationMethodSpinner() {
        List<String> operationMethodArray = Arrays.asList("", "Cartão Físico", "Qr Code");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, operationMethodArray);
        operationMethodSpinner.setAdapter(adapter);
        operationMethodSpinner.setOnItemSelectedListener(new OnSelectOperationMethod());
    }

    private class OnSelectOperationMethod implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            switch (position) {
                case 1:
                    selectedOperationMethod = 0;
                    break;
                case 2:
                    selectedOperationMethod = 1;
                    break;
                default:
                    selectedOperationMethod = 99;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
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



