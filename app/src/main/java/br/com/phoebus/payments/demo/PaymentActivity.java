package br.com.phoebus.payments.demo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import br.com.phoebus.android.payments.api.AdditionalValueType;
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
import br.com.phoebus.payments.demo.utils.MoneyWatcher;

public class PaymentActivity extends AppCompatActivity {

    private static final int QRCODE = 1;
    private EditText valueEdt;
    private EditText appTransactionIdEdt;
    private EditText installmentsEdt;
    private EditText emailToken;
    private EditText addValueEdt;
    private CheckBox chbReceiptMerchant, chbReceiptCustomer, previewReceiptMerchant, previewReceiptCustomer;
    private Spinner addValueTypeSpinner;
    private Spinner accTypeIdSpinner;
    private EditText planIdEdt;
    private Spinner productShortNameSpinner;
    private Spinner operationMethodSpinner;
    private CheckBox cbAllowBenefit;
    private Button doPaymentBtnv2;

    private List<PaymentType> paymentTypes;

    private PaymentClient paymentClient;

    Random r = new Random(System.currentTimeMillis());

    private String selectedProductShortName = "";
    private String selectedAccountType = "";
    private int selectedOperationMethod = 99;
    private EditText noteEdt;
    private EditText dniEdt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(R.string.payment_title);
        setContentView(R.layout.activity_payment);

        this.valueEdt = (EditText) this.findViewById(R.id.valueEdt);
        this.appTransactionIdEdt = (EditText) this.findViewById(R.id.appTransactionIdEdt);
        this.installmentsEdt = (EditText) this.findViewById(R.id.installmentsEdt);
        this.chbReceiptMerchant = findViewById(R.id.chbReceiptMerchant);
        this.chbReceiptCustomer = findViewById(R.id.chbReceiptCustomer);
        this.previewReceiptCustomer = findViewById(R.id.previewReceiptCustomer);
        this.previewReceiptMerchant = findViewById(R.id.previewReceiptMerchant);
        this.emailToken = (EditText) this.findViewById(R.id.email_token);
        this.addValueTypeSpinner = (Spinner) this.findViewById(R.id.addValueTypeSpinner);
        this.addValueEdt = (EditText) this.findViewById(R.id.addValueEdt);
        valueEdt.addTextChangedListener(new MoneyWatcher(valueEdt, "%,.2f"));
        addValueEdt.addTextChangedListener(new MoneyWatcher(addValueEdt, "%,.2f"));

        this.accTypeIdSpinner = (Spinner) this.findViewById(R.id.accTypeIdSpinner);
        this.planIdEdt = (EditText) this.findViewById(R.id.planIdEdt);
        this.productShortNameSpinner = (Spinner) this.findViewById(R.id.productShortNameSpinner);
        this.operationMethodSpinner = (Spinner) this.findViewById(R.id.operationMethodPayment);
        this.cbAllowBenefit = (CheckBox) this.findViewById(R.id.cbAllowBenefit);
        this.doPaymentBtnv2 = (Button) this.findViewById(R.id.doPaymentBtnv2);

        this.setDefaultValues();
        this.noteEdt = (EditText) this.findViewById(R.id.noteEdt);
        this.dniEdt = (EditText) this.findViewById(R.id.dniEdt);

        this.paymentClient = new PaymentClient();
        //recebendo a lista com os tipos de pagamento previamente selecionados na tela PaymentTypeListActivity
        if (getIntent() != null && getIntent().getExtras() != null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                paymentTypes = Collections.singletonList(getIntent().getExtras().getSerializable(Helper.EXTRA_LIST_PAYMENT_TYPE, PaymentType.class));
            } else {
                paymentTypes = bundleToPaymentType(getIntent());
            }

        setupAddValueSpinner();
        setupProductShortNameSpinner();
        setupAccTypeSpinner();
        setupOperationMethodSpinner();

        doBind();
    }

    private List<PaymentType> bundleToPaymentType(Intent intent) {
        Bundle bundle = intent.getBundleExtra(Helper.EXTRA_LIST_PAYMENT_TYPE);
        List<PaymentType> types = new ArrayList<>();
        if (bundle != null) {
            types = (List<PaymentType>) bundle.getSerializable(Helper.EXTRA_LIST_PAYMENT_TYPE);
        }
        return types;
    }

    @Override
    protected void onResume() {
        super.onResume();
        doPaymentBtnv2.setEnabled(true);
    }

    private void setDefaultValues() {

        this.valueEdt.setText(DataTypeUtils.getAsString(r.nextFloat() * 100F));
        this.addValueEdt.setText(DataTypeUtils.getAsString(0 * 100F));
        this.appTransactionIdEdt.setText(Helper.APP_TRANSACTION_ID);
        this.chbReceiptCustomer.setChecked(true);
        this.chbReceiptMerchant.setChecked(true);
        this.previewReceiptMerchant.setChecked(true);
        this.previewReceiptCustomer.setChecked(true);
    }

    private void doBind() {
        this.paymentClient.bind(this, new Client.OnConnectionCallback() {
            @Override
            public void onConnected() {
                showSnackBar(getString(R.string.connected));
            }

            @Override
            public void onDisconnected(boolean forced) {
                Snackbar.make(installmentsEdt, getString(R.string.disconnected) + forced, Snackbar.LENGTH_INDEFINITE)
                        .setAction(getString(R.string.reconnect), new View.OnClickListener() {
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
            paymentRequestV2.setPrintCustomerReceipt(this.chbReceiptCustomer.isChecked());
            paymentRequestV2.setPrintMerchantReceipt(this.chbReceiptMerchant.isChecked());
            paymentRequestV2.setPreviewCustomerReceipt(this.previewReceiptCustomer.isChecked());
            paymentRequestV2.setPreviewMerchantReceipt(this.previewReceiptMerchant.isChecked());
            paymentRequestV2.setTokenizeCard(false);
            paymentRequestV2.setTokenizeEmail(String.valueOf(this.emailToken.getText()));
            String addValueType = (String) this.addValueTypeSpinner.getSelectedItem();
            paymentRequestV2.setOperationMethodAllowed(this.selectedOperationMethod);
            String accType = selectedAccountType;
            String planId = this.planIdEdt.getText().toString();
            String productShortName = selectedProductShortName;
            int allowBenefit = this.selectedOperationMethod;

            if (addValueType != null && !addValueType.isEmpty()) {
                paymentRequestV2.setAdditionalValueType(AdditionalValueType.valueOf(addValueType.toUpperCase()));
                paymentRequestV2.setAdditionalValue(DataTypeUtils.getFromString(this.addValueEdt.getText().toString()));
            }

            if(allowBenefit == QRCODE)
                paymentRequestV2.setAllowBenefit(cbAllowBenefit.isChecked());
            if (!accType.isEmpty())
                paymentRequestV2.setAccountTypeId(accType);
            if (!planId.isEmpty())
                paymentRequestV2.setPlanId(planId);
            if (!productShortName.isEmpty())
                paymentRequestV2.setProductShortName(productShortName);

        } catch (PackageManager.NameNotFoundException e) {
            showSnackBar(getString(R.string.requestFailed) +": " + e.getMessage());
            return;
        }

        if (this.installmentsEdt.getText() != null && !"".equals(this.installmentsEdt.getText().toString())) {
            paymentRequestV2.setInstallments(Integer.parseInt(this.installmentsEdt.getText().toString()));
        }

        if (this.noteEdt.getText() != null && !"".equals(this.noteEdt.getText().toString())) {
            paymentRequestV2.setNote(this.noteEdt.getText().toString());
        }
        if (this.dniEdt.getText() != null && !"".equals(this.dniEdt.getText().toString())) {
            String dni = this.dniEdt.getText().toString();
            if (dni.length() < 6 || dni.length() > 10) {
                this.dniEdt.setError(getString(R.string.dni_error));
                return;
            }
            paymentRequestV2.setDni(dni);
        }

        boolean isPaymentEndToEnd = getIntent().getExtras() != null && getIntent().getExtras().getBoolean(Helper.IS_PAYMENT_END_TO_PAYMENT);
        if (!isPaymentEndToEnd) {
            if (view.isPressed()) {
                view.setEnabled(false);
            }
            try {
                this.paymentClient.startPaymentV2(paymentRequestV2, new PaymentClient.PaymentCallback<PaymentV2>() {
                    @Override
                    public void onSuccess(PaymentV2 data) {
                        showSnackBar(getString(R.string.paymentSuccessfull));

                        configureReturnData(data, paymentRequestV2);
                        ResultActivity.callResultIntent(data, PaymentActivity.this, 0, null);
                    }

                    @Override
                    public void onError(ErrorData errorData) {
                        showSnackBar(getString(R.string.paymentsFailed) + ": " + errorData.getPaymentsResponseCode() + " / "
                                + errorData.getAcquirerResponseCode() + " = " + errorData.getResponseMessage());
                        view.setEnabled(true);
                    }
                });
            } catch (Exception e) {
                showSnackBar(getString(R.string.requestFailed) + ": " + e.getMessage());
            }
        }
        else {
           if (view.isPressed()) {
               view.setEnabled(false);
               PaymentEndToEnd paymentEndToEnd = new PaymentEndToEnd(this, paymentClient);
               paymentEndToEnd.doPayment(paymentRequestV2, view);
            }
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

    private void setupAddValueSpinner() {
        List<String> addValueTypes = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, addValueTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        addValueTypes.add("");
        addValueTypes.add("CashBack");
        addValueTypes.add("TIP");

        this.addValueTypeSpinner.setAdapter(adapter);
        this.addValueTypeSpinner.setOnItemSelectedListener(new OnSelectAddValueType());
    }

    private void setupProductShortNameSpinner() {
        List<String> shortNames = new ArrayList<>();
        shortNames.add("");
        shortNames.add("VISA");
        shortNames.add("MASTERCARD");
        shortNames.add("AMEX");
        shortNames.add("Cabal Débito");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, shortNames);
        this.productShortNameSpinner.setAdapter(adapter);
        this.productShortNameSpinner.setOnItemSelectedListener(new OnSelectProductShortName());
    }

    private void setupAccTypeSpinner() {
        List<String> accTypes = new ArrayList<>();
        accTypes.add("");
        accTypes.add(getString(R.string.account_type_caja_ajorro));
        accTypes.add(getString(R.string.account_type_c_corriente));
        accTypes.add(getString(R.string.account_type_s_caja_ajorro));
        accTypes.add(getString(R.string.account_type_s_c_corriente));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, accTypes);
        this.accTypeIdSpinner.setAdapter(adapter);
        this.accTypeIdSpinner.setOnItemSelectedListener(new OnSelectAccountType());
    }

    private void setupOperationMethodSpinner() {
        List<String> operationMethodArray = Arrays.asList("", "Cartão Físico", "QrCode");
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
                    cbAllowBenefit.setEnabled(false);
                    break;
                case 2:
                    selectedOperationMethod = 1;
                    cbAllowBenefit.setEnabled(true);
                    break;
                default:
                    selectedOperationMethod = 99;
                    cbAllowBenefit.setEnabled(false);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private class OnSelectAddValueType implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
            addValueEdt.setEnabled(pos != 0);
        }
        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    }

    private class OnSelectAccountType implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
            String item = adapterView.getItemAtPosition(pos).toString();

            if (item.equals(getString(R.string.account_type_caja_ajorro))) {
                selectedAccountType = "1";
            } else if (item.equals(getString(R.string.account_type_c_corriente))) {
                selectedAccountType = "2";
            } else if (item.equals(getString(R.string.account_type_s_caja_ajorro))) {
                selectedAccountType = "8";
            } else if (item.equals(getString(R.string.account_type_s_c_corriente))) {
                selectedAccountType = "9";
            } else {
                selectedAccountType = "";
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
                case 4:
                    selectedProductShortName = "C2";
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
