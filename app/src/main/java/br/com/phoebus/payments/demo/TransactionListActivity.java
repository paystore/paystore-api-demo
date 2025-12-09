package br.com.phoebus.payments.demo;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import br.com.phoebus.android.payments.api.PaymentStatus;
import br.com.phoebus.android.payments.api.ReversePaymentV2;
import br.com.phoebus.android.payments.api.enums.ReversalStatus;
import br.com.phoebus.android.payments.api.exception.ClientException;
import br.com.phoebus.android.payments.api.provider.PaymentProviderApi;
import br.com.phoebus.android.payments.api.provider.PaymentProviderRequest;
import br.com.phoebus.android.payments.api.provider.request.RefundProviderRequest;
import br.com.phoebus.android.payments.api.provider.request.ReversalProviderRequest;
import br.com.phoebus.android.payments.api.provider.response.ProviderResponse;
import br.com.phoebus.android.payments.api.provider.response.RefundProviderResponse;
import br.com.phoebus.android.payments.api.provider.response.TransactionProviderResponse;
import br.com.phoebus.payments.demo.fragments.OnTransactionSelectedClickListener;
import br.com.phoebus.payments.demo.fragments.TransactionListFragment;
import br.com.phoebus.payments.demo.utils.CredentialsUtils;
import br.com.phoebus.payments.demo.utils.MoneyWatcher;

public class TransactionListActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener, OnTransactionSelectedClickListener {
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS", new Locale("pt", "BR"));
    private PaymentProviderApi api;
    private RadioGroup radioGroupFilter;
    private Button doCleaning, doSearchBtn;
    private Spinner productShortName, operationMethodSpinner;
    private String selectedProductShortName = "";
    private int selectedOperationMethod = 99;
    private Integer typeStatus = R.id.type_payment;
    private CheckBox checkPending, checkProcessing, checkConfirm, checkRefunded, checkCancelled, checkReversed;
    private TextInputLayout textInputPaymentId;

    //campos em comuns entre pagamento, estorno e devolução não referenciada
    private EditText edStartDate, edFinishDate, mintValue, maxValue, paymentId, lastDigits, ticketNumber, batchNumber, acquirerResponseCode, appTransactionIdEdt;
    private CheckBox batchPend, lastBatch, lastUpdatedQuery;
    //campos do pagamento
    private LinearLayout containerPayment;
    private EditText qrId, dni, notes;

    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.search_transaction);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        api = PaymentProviderApi.create(this);

        bindViews();
        actionClicks();
        setupProductShortNameSpinner();
        setupOperationMethodSpinner();
        clearFilters();
    }

    private void bindViews() {
        radioGroupFilter = findViewById(R.id.typeFilter);
        doSearchBtn = findViewById(R.id.doSearchBtn);
        doCleaning = findViewById(R.id.doCleaning);

        containerPayment = findViewById(R.id.container_payment);

        operationMethodSpinner = findViewById(R.id.operationMethod);
        productShortName = findViewById(R.id.productShortName);

        textInputPaymentId = findViewById(R.id.textInputPaymentId);
        paymentId = textInputPaymentId.getEditText();
        edStartDate = findViewById(R.id.start_date);
        edFinishDate = findViewById(R.id.finish_date);
        mintValue = findViewById(R.id.min_value);
        maxValue = findViewById(R.id.max_value);
        lastDigits = findViewById(R.id.last_digits);
        ticketNumber = findViewById(R.id.ticket_number);
        batchNumber = findViewById(R.id.batch_number);
        acquirerResponseCode = findViewById(R.id.acquirer_response_code);
        appTransactionIdEdt = findViewById(R.id.appTransactionIdEdt);

        batchPend = findViewById(R.id.batch_pend);
        lastBatch = findViewById(R.id.last_batch);
        lastUpdatedQuery = findViewById(R.id.last_updated_query);

        checkPending = findViewById(R.id.pay_status_PENDING);
        checkProcessing = findViewById(R.id.pay_status_PROCESSING);
        checkConfirm = findViewById(R.id.pay_status_CONFIRMED);
        checkRefunded = findViewById(R.id.pay_status_REFUNDED);
        checkCancelled = findViewById(R.id.pay_status_CANCELLED);
        checkReversed = findViewById(R.id.pay_status_REVERSED);

        qrId = findViewById(R.id.qrId);
        dni = findViewById(R.id.dni);
        notes = findViewById(R.id.notes);
    }

    private void actionClicks() {
        radioGroupFilter.setOnCheckedChangeListener(this);

        doSearchBtn.setOnClickListener(this);
        doCleaning.setOnClickListener(this);

        edStartDate.setOnClickListener(this);
        edFinishDate.setOnClickListener(this);
    }

    public void clearFilters() {
        radioGroupFilter.check(R.id.type_payment);
        typeStatus = R.id.type_payment;

        paymentId.setText("");
        edStartDate.setText("");
        edFinishDate.setText("");
        lastDigits.setText("");
        ticketNumber.setText("");
        batchNumber.setText("");
        acquirerResponseCode.setText("");
        appTransactionIdEdt.setText("");
        batchPend.setChecked(false);
        lastBatch.setChecked(false);
        lastUpdatedQuery.setChecked(false);
        selectedProductShortName = "";

        checkPending.setChecked(false);
        checkProcessing.setChecked(false);
        checkConfirm.setChecked(false);
        checkRefunded.setChecked(false);
        checkCancelled.setChecked(false);
        checkReversed.setChecked(false);

        qrId.setText("");
        dni.setText("");
        notes.setText("");

        mintValue.setText("0,00");
        maxValue.setText("0,00");
        mintValue.addTextChangedListener(new MoneyWatcher(mintValue, "%,.2f"));
        maxValue.addTextChangedListener(new MoneyWatcher(maxValue, "%,.2f"));
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {

        typeStatus = i;

        switch (i) {
            case R.id.type_payment:
                textInputPaymentId.setHint("paymentId");
                visibilityStatusPayment();
                break;
            case R.id.type_reversal:
            case R.id.type_refund:
                if (i == R.id.type_refund) {
                    textInputPaymentId.setHint("refundId");
                }
                visibilityStatusReversal();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void visibilityStatusPayment() {
        containerPayment.setVisibility(View.VISIBLE);
        checkPending.setVisibility(View.VISIBLE);
        checkProcessing.setVisibility(View.GONE);
        checkProcessing.setChecked(false);
        checkConfirm.setVisibility(View.VISIBLE);
        checkRefunded.setVisibility(View.VISIBLE);
        checkCancelled.setVisibility(View.VISIBLE);
        checkReversed.setVisibility(View.VISIBLE);
        operationMethodSpinner.setVisibility(View.VISIBLE);
    }

    private void visibilityStatusReversal() {
        clearDataStatus();
        containerPayment.setVisibility(View.GONE);
        checkPending.setVisibility(View.VISIBLE);
        checkProcessing.setVisibility(View.VISIBLE);
        checkConfirm.setVisibility(View.VISIBLE);
        checkRefunded.setVisibility(View.GONE);
        checkRefunded.setChecked(false);
        checkCancelled.setVisibility(View.VISIBLE);
        checkReversed.setVisibility(View.GONE);
        checkReversed.setChecked(false);
        operationMethodSpinner.setVisibility(View.GONE);
        selectedOperationMethod = 0;
        operationMethodSpinner.setSelection(selectedOperationMethod);
    }

    private void setupProductShortNameSpinner() {
        List<String> shortNames = new ArrayList<>();
        shortNames.add("productShortName");
        shortNames.add("VISA");
        shortNames.add("VISA DÉBITO");
        shortNames.add("MASTERCARD");
        shortNames.add("MAESTRO");
        shortNames.add("AMEX");
        shortNames.add("Cabal Débito");
        shortNames.add("");

        this.productShortName.setAdapter(setupArrayAdapter(shortNames));
        this.productShortName.setOnItemSelectedListener(new OnSelectProductShortName());
    }

    private void setupOperationMethodSpinner() {
        List<String> operationMethodArray = Arrays.asList("operationMethod", getString(R.string.physicalCard), getString(R.string.QRCode), "");
        operationMethodSpinner.setAdapter(setupArrayAdapter(operationMethodArray));
        operationMethodSpinner.setOnItemSelectedListener(new OnSelectOperationMethod());
    }

    private void clearDataStatus() {
        qrId.setText("");
        dni.setText("");
        notes.setText("");
    }

    private ArrayAdapter<String> setupArrayAdapter(List<String> array) {
        return new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, array) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                return view;
            }
        };
    }

    private class OnSelectProductShortName implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
            switch (pos) {
                case 1:
                    selectedProductShortName = "VI";
                    break;
                case 2:
                    selectedProductShortName = "VI";
                    break;
                case 3:
                    selectedProductShortName = "MC";
                    break;
                case 4:
                    selectedProductShortName = "MA";
                    break;
                case 5:
                    selectedProductShortName = "AM";
                    break;
                case 6:
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.doCleaning:
                clearFilters();
                break;
            case R.id.doSearchBtn:
                generateRequest();
                break;
            case R.id.start_date:
                getCalendarDate(edStartDate, true);
                break;
            case R.id.finish_date:
                getCalendarDate(edFinishDate, false);
                break;
        }
    }

    private void getCalendarDate(EditText editTextDate, Boolean isStartDate) {
        final Calendar c = Calendar.getInstance();
        Calendar date = Calendar.getInstance();
        Context context = this;
        new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                date.set(year, month, day);
                new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        date.set(Calendar.MINUTE, minute);
                        updateDate(editTextDate, date.get(Calendar.DAY_OF_MONTH), date.get(Calendar.MONTH), date.get(Calendar.YEAR), date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE), date.get(Calendar.SECOND), date.get(Calendar.MILLISECOND));
                    }
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void updateDate(EditText ed, int day, int month, int year, int hour, int minute, int second, int millisecond) {
        ed.setText(String.format("%02d/%02d/%d %02d:%02d:%02d.%03d", day, month + 1, year, hour, minute, second, millisecond));
    }

    private void generateRequest() {
        try {
            TransactionListFragment transactionListFragment;

            switch (typeStatus) {
                case R.id.type_payment:
                case R.id.type_reversal:

                    List<TransactionProviderResponse> listTransactions;

                    if (typeStatus == R.id.type_payment) {
                        PaymentProviderRequest paymentProviderRequest = generatedPaymentProviderRequest();
                        listTransactions = api.findAllTransactions(paymentProviderRequest);

                    } else {
                        ReversalProviderRequest reversalProviderRequest = generatedReversalProviderRequest();
                        listTransactions = api.findAllReversals(reversalProviderRequest);

                    }

                    transactionListFragment = new TransactionListFragment<TransactionProviderResponse>(listTransactions, this);

                    showFragment(this, transactionListFragment);

                    break;
                case R.id.type_refund:

                    RefundProviderRequest refundProviderRequest = generatedRefundProviderRequest();

                    List<RefundProviderResponse> refundProviderResponseList = api.findAllRefunds(refundProviderRequest);

                    transactionListFragment = new TransactionListFragment<RefundProviderResponse>(refundProviderResponseList, this);

                    showFragment(this, transactionListFragment);

                    break;
            }
        } catch (PackageManager.NameNotFoundException | ParseException | ClientException e) {
            e.printStackTrace();
        }
    }

    private void showFragment(AppCompatActivity activity, TransactionListFragment transactionListFragment) {
        FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.container_transaction, transactionListFragment);
        fragmentTransaction.addToBackStack(fragmentTransaction.getClass().getSimpleName());
        fragmentTransaction.commit();
    }

    private PaymentProviderRequest generatedPaymentProviderRequest() throws PackageManager.NameNotFoundException, ParseException {
        PaymentProviderRequest paymentProviderRequest = new PaymentProviderRequest(CredentialsUtils.getMyAppInfo(getApplicationContext().getPackageManager(), getApplicationContext().getPackageName()), defaultDateRequest());

        if (!TextUtils.isEmpty(paymentId.getText())) {
            paymentProviderRequest.setPaymentId(paymentId.getText().toString());
        }

        if (!TextUtils.isEmpty(edStartDate.getText())) {
            paymentProviderRequest.setStartDate(simpleDateFormat.parse(edStartDate.getText().toString()));
        }
        if (!TextUtils.isEmpty(edFinishDate.getText())) {
            paymentProviderRequest.setFinishDate(simpleDateFormat.parse(edFinishDate.getText().toString()));
        }

        if (!TextUtils.isEmpty(mintValue.getText())) {
            paymentProviderRequest.setStartValue(toBigDecimal(mintValue.getText().toString()));
        }

        if (!TextUtils.isEmpty(maxValue.getText())) {
            paymentProviderRequest.setFinishValue(toBigDecimal(maxValue.getText().toString()));
        }

        if (!TextUtils.isEmpty(lastDigits.getText())) {
            paymentProviderRequest.setLastDigits(lastDigits.getText().toString());
        }

        if (!TextUtils.isEmpty(ticketNumber.getText())) {
            paymentProviderRequest.setTicketNumber(ticketNumber.getText().toString());
        }

        if (!TextUtils.isEmpty(dni.getText())) {
            String strDni = dni.getText().toString();
            if (strDni.length() < 6 || strDni.length() > 10) {
                dni.setError(getString(R.string.dni_error));
                return new PaymentProviderRequest();
            }
            paymentProviderRequest.setDni(strDni);
        }

        if (!TextUtils.isEmpty(qrId.getText())) {
            paymentProviderRequest.setQrId(qrId.getText().toString());
        }

        if (!TextUtils.isEmpty(notes.getText())) {
            paymentProviderRequest.setNotes(notes.getText().toString());
        }

        if (!TextUtils.isEmpty(batchNumber.getText())) {
            paymentProviderRequest.setBatchNumber(batchNumber.getText().toString());
        }

        if (!TextUtils.isEmpty(acquirerResponseCode.getText())) {
            paymentProviderRequest.setAcquirerResponseCode(acquirerResponseCode.getText().toString());
        }

        List<PaymentStatus> status = new ArrayList<>();
        if (checkPending.isChecked()) {
            status.add(PaymentStatus.PENDING);
        }
        if (checkConfirm.isChecked()) {
            status.add(PaymentStatus.CONFIRMED);
        }
        if (checkCancelled.isChecked()) {
            status.add(PaymentStatus.CANCELLED);
        }
        if (checkReversed.isChecked()) {
            status.add(PaymentStatus.REVERSED);
        }
        if (checkRefunded.isChecked()) {
            status.add(PaymentStatus.REFUNDED);
        }

        if (batchPend.isChecked()) {
            paymentProviderRequest.setBatchPend(true);
        }

        if (lastBatch.isChecked()) {
            paymentProviderRequest.setLastBatch(true);
        }

        if (lastUpdatedQuery.isChecked()) {
            paymentProviderRequest.setLastUpdateQuery(true);
        }

        paymentProviderRequest.setAppTransactionId(appTransactionIdEdt.getText().toString());

        paymentProviderRequest.setStatus(status);

        paymentProviderRequest.setAllResults(true);

        paymentProviderRequest.setProductShortName(selectedProductShortName);


        paymentProviderRequest.setOperationMethod(selectedOperationMethod);

        return paymentProviderRequest;
    }

    private ReversalProviderRequest generatedReversalProviderRequest() throws PackageManager.NameNotFoundException, ParseException {
        ReversalProviderRequest reversalProviderRequest = new ReversalProviderRequest(CredentialsUtils.getMyAppInfo(getApplicationContext().getPackageManager(), getApplicationContext().getPackageName()), defaultDateRequest());

        if (!TextUtils.isEmpty(paymentId.getText())) {
            reversalProviderRequest.setPaymentId(paymentId.getText().toString());
        }

        if (!TextUtils.isEmpty(edStartDate.getText())) {
            reversalProviderRequest.setStartDate(simpleDateFormat.parse(edStartDate.getText().toString()));
        }
        if (!TextUtils.isEmpty(edFinishDate.getText())) {
            reversalProviderRequest.setFinishDate(simpleDateFormat.parse(edFinishDate.getText().toString()));
        }

        if (!TextUtils.isEmpty(mintValue.getText())) {
            reversalProviderRequest.setStartValue(toBigDecimal(mintValue.getText().toString()));
        }

        if (!TextUtils.isEmpty(maxValue.getText())) {
            reversalProviderRequest.setFinishValue(toBigDecimal(maxValue.getText().toString()));
        }

        if (!TextUtils.isEmpty(lastDigits.getText())) {
            reversalProviderRequest.setLastDigits(lastDigits.getText().toString());
        }

        if (!TextUtils.isEmpty(ticketNumber.getText())) {
            reversalProviderRequest.setTicketNumber(ticketNumber.getText().toString());
        }

        if (!TextUtils.isEmpty(batchNumber.getText())) {
            reversalProviderRequest.setBatchNumber(batchNumber.getText().toString());
        }

        if (!TextUtils.isEmpty(acquirerResponseCode.getText())) {
            reversalProviderRequest.setAcquirerResponseCode(acquirerResponseCode.getText().toString());
        }

        List<ReversalStatus> status = new ArrayList<>();
        if (checkPending.isChecked()) {
            status.add(ReversalStatus.PENDING);
        }
        if (checkConfirm.isChecked()) {
            status.add(ReversalStatus.CONFIRMED);
        }
        if (checkCancelled.isChecked()) {
            status.add(ReversalStatus.CANCELLED);
        }

        if (checkProcessing.isChecked()) {
            status.add(ReversalStatus.PROCESSING);
        }

        if (batchPend.isChecked()) {
            reversalProviderRequest.setBatchPend(true);
        }

        if (lastBatch.isChecked()) {
            reversalProviderRequest.setLastBatch(true);
        }

        if (lastUpdatedQuery.isChecked()) {
            reversalProviderRequest.setLastUpdateQuery(true);
        }

        reversalProviderRequest.setAppTransactionId(appTransactionIdEdt.getText().toString());

        reversalProviderRequest.setStatus(status);

        reversalProviderRequest.setProductShortName(selectedProductShortName);

        reversalProviderRequest.setAllResults(true);

        reversalProviderRequest.setOperationMethod(selectedOperationMethod);

        return reversalProviderRequest;
    }

    private RefundProviderRequest generatedRefundProviderRequest() throws PackageManager.NameNotFoundException, ParseException {
        RefundProviderRequest refundProviderRequest = new RefundProviderRequest(CredentialsUtils.getMyAppInfo(getApplicationContext().getPackageManager(), getApplicationContext().getPackageName()), defaultDateRequest());

        if (!TextUtils.isEmpty(paymentId.getText())) {
            refundProviderRequest.setRefundId(paymentId.getText().toString());
        }

        if (!TextUtils.isEmpty(edStartDate.getText())) {
            refundProviderRequest.setStartDate(simpleDateFormat.parse(edStartDate.getText().toString()));
        }
        if (!TextUtils.isEmpty(edFinishDate.getText())) {
            refundProviderRequest.setFinishDate(simpleDateFormat.parse(edFinishDate.getText().toString()));
        }

        if (!TextUtils.isEmpty(mintValue.getText())) {
            refundProviderRequest.setStartValue(toBigDecimal(mintValue.getText().toString()));
        }

        if (!TextUtils.isEmpty(maxValue.getText())) {
            refundProviderRequest.setFinishValue(toBigDecimal(maxValue.getText().toString()));
        }

        if (!TextUtils.isEmpty(lastDigits.getText())) {
            refundProviderRequest.setLastDigits(lastDigits.getText().toString());
        }

        if (!TextUtils.isEmpty(ticketNumber.getText())) {
            refundProviderRequest.setTicketNumber(ticketNumber.getText().toString());
        }

        if (!TextUtils.isEmpty(batchNumber.getText())) {
            refundProviderRequest.setBatchNumber(batchNumber.getText().toString());
        }

        if (!TextUtils.isEmpty(acquirerResponseCode.getText())) {
            refundProviderRequest.setAcquirerResponseCode(acquirerResponseCode.getText().toString());
        }

        List<ReversalStatus> status = new ArrayList<>();
        if (checkPending.isChecked()) {
            status.add(ReversalStatus.PENDING);
        }
        if (checkConfirm.isChecked()) {
            status.add(ReversalStatus.CONFIRMED);
        }
        if (checkCancelled.isChecked()) {
            status.add(ReversalStatus.CANCELLED);
        }

        if (checkProcessing.isChecked()) {
            status.add(ReversalStatus.PROCESSING);
        }

        if (batchPend.isChecked()) {
            refundProviderRequest.setBatchPend(true);
        }

        if (lastBatch.isChecked()) {
            refundProviderRequest.setLastBatch(true);
        }

        if (lastUpdatedQuery.isChecked()) {
            refundProviderRequest.setLastUpdateQuery(true);
        }

        refundProviderRequest.setAppTransactionId(appTransactionIdEdt.getText().toString());

        refundProviderRequest.setStatus(status);

        refundProviderRequest.setProductShortName(selectedProductShortName);


        refundProviderRequest.setOperationMethod(selectedOperationMethod);

        return refundProviderRequest;
    }

    public BigDecimal toBigDecimal(String v) {
        try {
            NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
            Number number = formatter.parse(v);
            assert number != null;
            return BigDecimal.valueOf(number.longValue());
        } catch (ParseException e) {
            /**/
        }

        return new BigDecimal("0");
    }

    public Date defaultDateRequest() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);

        return calendar.getTime();
    }

    @Override
    public void onTransactionSelectedClick(ProviderResponse transaction) {
        Map<String, String> options = new LinkedHashMap<>();
        options.put(ResultActivity.SHOW_BUTTON_CONFIRM, "F");
        ResultActivity.callResultIntent(transaction, this, 0, options);
    }

    @Override
    public void onTransactionReverseSelectedClick(ReversePaymentV2 reversePaymentV2) {
        Map<String, String> options = new LinkedHashMap<>();
        options.put(ResultActivity.SHOW_BUTTON_CONFIRM, "F");
        ResultActivity.callResultIntent(reversePaymentV2, this, 0, options);
    }
}
