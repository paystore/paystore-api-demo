package br.com.phoebus.payments.demo.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import org.w3c.dom.Text;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.com.phoebus.android.payments.api.PaymentStatus;
import br.com.phoebus.android.payments.api.TransactionTypeFilter;
import br.com.phoebus.android.payments.api.provider.PaymentProviderRequest;
import br.com.phoebus.payments.demo.R;
import br.com.phoebus.payments.demo.utils.CredentialsUtils;
import br.com.phoebus.payments.demo.utils.MoneyWatcher;

public class PaymentFilterFragment extends Fragment{
    private OnFilterClickedListener mListener;
    private PaymentProviderRequest mPaymentRequest;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", new Locale("pt", "BR"));

    private Button mButton;
    private Button clearButton;
    private EditText edPaymentId;
    private EditText edStartDate;
    private EditText edFinishDate;
    private EditText edMinValue;
    private EditText edMaxValue;
    private EditText edFourDigits;
    private CheckBox cbStatusPending;
    private CheckBox cbStatusConfirmed;
    private CheckBox cbStatusCancelled;
    private CheckBox cbStatusReversed;
    private CheckBox cbStatusRefunded;
    private RadioGroup rdgTrxType;
    private RadioButton rdTrxTypeAll;
    private RadioButton rdTrxTypeSale;
    private RadioButton rdTrxTypeDV;
    private EditText ticketNumber;
    private EditText batchNumber;
    private EditText acquirerResponseCode;
    private CheckBox batchPend;
    private CheckBox lastBatch;
    private CheckBox lastApprovedTrx;
    private CheckBox lastUpdatedQuery;
    private Spinner productShortName;
    private EditText qrId;
    private EditText dni;
    private EditText notes;
    private Spinner operationMethodSpinner;
    private EditText appTransactionIdEdt;

    private String selectedProductShortName = "";
    private int selectedOperationMethod = 99;

    public static Fragment newInstance() {
        return new PaymentFilterFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.paymentsFilter_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment_filter, container, false);

        this.bindViews(view);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    mPaymentRequest = new PaymentProviderRequest(CredentialsUtils.getMyAppInfo(getContext().getPackageManager(), getContext().getPackageName()), new Date());

                    if(!TextUtils.isEmpty(edPaymentId.getText())){
                        mPaymentRequest.setPaymentId(edPaymentId.getText().toString());
                    }

                    if(!TextUtils.isEmpty(edStartDate.getText())){
                        mPaymentRequest.setStartDate(simpleDateFormat.parse(edStartDate.getText().toString()));
                    }
                    if(!TextUtils.isEmpty(edFinishDate.getText())){
                        mPaymentRequest.setFinishDate(simpleDateFormat.parse(edFinishDate.getText().toString()));
                    }

                    if(!TextUtils.isEmpty(edMinValue.getText())){
                        mPaymentRequest.setStartValue(toBigDecimal(edMinValue.getText().toString()));
                    }

                    if(!TextUtils.isEmpty(edMaxValue.getText())){
                        mPaymentRequest.setFinishValue(toBigDecimal(edMaxValue.getText().toString()));
                    }

                    if(!TextUtils.isEmpty(edFourDigits.getText())){
                        mPaymentRequest.setLastDigits(edFourDigits.getText().toString());
                    }

                    if (!TextUtils.isEmpty(ticketNumber.getText())) {
                        mPaymentRequest.setTicketNumber(ticketNumber.getText().toString());
                    }

                    if(!TextUtils.isEmpty(dni.getText())){
                        String strDni = dni.getText().toString();
                        if (strDni.length() < 6 || strDni.length() > 10) {
                            dni.setError(getString(R.string.dni_error));
                            return;
                        }
                        mPaymentRequest.setDni(strDni);
                    }

                    if(!TextUtils.isEmpty(qrId.getText())){
                        mPaymentRequest.setQrId(qrId.getText().toString());
                    }

                    if(!TextUtils.isEmpty(notes.getText())){
                        mPaymentRequest.setNotes(notes.getText().toString());
                    }

                    if (!TextUtils.isEmpty(batchNumber.getText())) {
                        mPaymentRequest.setBatchNumber(batchNumber.getText().toString());
                    }

                    if (!TextUtils.isEmpty(acquirerResponseCode.getText())) {
                        mPaymentRequest.setAcquirerResponseCode(acquirerResponseCode.getText().toString());
                    }

                    if(rdTrxTypeSale.isChecked()) {
                        mPaymentRequest.setTrxType(String.valueOf(TransactionTypeFilter.SALE.getId()));
                    }
                    if(rdTrxTypeDV.isChecked()) {
                        mPaymentRequest.setTrxType(String.valueOf(TransactionTypeFilter.UNREFERENCED_DEVOLUTION.getId()));
                    }

                    List<PaymentStatus> status = new ArrayList<>();
                    if(cbStatusPending.isChecked()){
                        status.add(PaymentStatus.PENDING);
                    }
                    if(cbStatusConfirmed.isChecked()){
                        status.add(PaymentStatus.CONFIRMED);
                    }
                    if(cbStatusCancelled.isChecked()){
                        status.add(PaymentStatus.CANCELLED);
                    }
                    if(cbStatusReversed.isChecked()){
                        status.add(PaymentStatus.REVERSED);
                    }
                    if(cbStatusRefunded.isChecked()){
                        status.add(PaymentStatus.REFUNDED_DEVOLUTION);
                    }

                    if (batchPend.isChecked()) {
                        mPaymentRequest.setBatchPend(true);
                    }

                    if (lastBatch.isChecked()) {
                        mPaymentRequest.setLastBatch(true);
                    }
                    if (lastApprovedTrx.isChecked()) {
                        mPaymentRequest.setLastTrx(true);
                    }

                    if (lastUpdatedQuery.isChecked()) {
                        mPaymentRequest.setLastUpdateQuery(true);
                    }

                    mPaymentRequest.setAppTransactionId(appTransactionIdEdt.getText().toString());

                    mPaymentRequest.setStatus(status);

                    //mPaymentRequest.setAllResults(true);

                    mPaymentRequest.setProductShortName(selectedProductShortName);


                    mPaymentRequest.setOperationMethod(selectedOperationMethod);

                    if (mListener != null) {
                        mListener.onFilterClickedListener(mPaymentRequest);
                    }
                } catch (PackageManager.NameNotFoundException e) {

                } catch (ParseException e) {

                }
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearFilters();
            }
        });

        return view;
    }

    public void clearFilters() {
        edPaymentId.setText("");
        edFinishDate.setText("");
        edStartDate.setText("");
        edMinValue.setText("");
        edMaxValue.setText("");
        edFourDigits.setText("");

        cbStatusPending.setChecked(false);
        cbStatusConfirmed.setChecked(false);
        cbStatusCancelled.setChecked(false);
        cbStatusReversed.setChecked(false);
        cbStatusRefunded.setChecked(false);

        ticketNumber.setText("");
        batchNumber.setText("");
        acquirerResponseCode.setText("");

        batchPend.setChecked(false);
        lastBatch.setChecked(false);
        lastApprovedTrx.setChecked(false);
        lastUpdatedQuery.setChecked(false);

        selectedProductShortName = "";
        selectedOperationMethod = 99;

        dni.setText("");
        qrId.setText("");
        notes.setText("");
        appTransactionIdEdt.setText("");
        rdgTrxType.check(R.id.all);

    }

    private void setupProductShortNameSpinner() {
        List<String> shortNames = new ArrayList<>();
        shortNames.add(getString(R.string.productShortNameHint));
        shortNames.add("VISA");
        shortNames.add("MASTERCARD");
        shortNames.add("AMEX");
        shortNames.add("");

        this.productShortName.setAdapter(setupArrayAdapter(shortNames));
        this.productShortName.setOnItemSelectedListener(new OnSelectProductShortName());
    }

    private void setupOperationMethodSpinner() {
        List<String> operationMethodArray = Arrays.asList(getString(R.string.operationMethodHint), getString(R.string.physicalCard), getString(R.string.QRCode), "");
        operationMethodSpinner.setAdapter(setupArrayAdapter(operationMethodArray));
        operationMethodSpinner.setOnItemSelectedListener(new OnSelectOperationMethod());

    }

    private void bindViews(View view) {
        mButton = (Button) view.findViewById(R.id.doFilterBtn);

        edPaymentId = (EditText) view.findViewById(R.id.paymentId);
        edStartDate = (EditText) view.findViewById(R.id.start_date);
        edFinishDate = (EditText) view.findViewById(R.id.finish_date);
        edMinValue = (EditText) view.findViewById(R.id.min_value);
        edMaxValue = (EditText) view.findViewById(R.id.max_value);
        edFourDigits = (EditText) view.findViewById(R.id.four_digits);

        cbStatusPending = (CheckBox) view.findViewById(R.id.pay_status_PENDING);
        cbStatusConfirmed = (CheckBox) view.findViewById(R.id.pay_status_CONFIRMED);
        cbStatusCancelled = (CheckBox) view.findViewById(R.id.pay_status_CANCELLED);
        cbStatusReversed = (CheckBox) view.findViewById(R.id.pay_status_REVERSED);
        cbStatusRefunded = (CheckBox) view.findViewById(R.id.pay_status_REFUNDED);

        ticketNumber = view.findViewById(R.id.ticket_number);
        batchNumber = view.findViewById(R.id.batch_number);
        acquirerResponseCode = view.findViewById(R.id.acquirer_response_code);

        batchPend = view.findViewById(R.id.pay_batch_pend);
        lastBatch = view.findViewById(R.id.pay_last_batch);
        lastApprovedTrx = view.findViewById(R.id.pay_last_approved);
        lastUpdatedQuery = view.findViewById(R.id.pay_last_updated_query);

        //text input format
        edMinValue.setText("0,00");
        edMaxValue.setText("0,00");
        edMinValue.addTextChangedListener(new MoneyWatcher(edMinValue, "%,.2f"));
        edMaxValue.addTextChangedListener(new MoneyWatcher(edMaxValue, "%,.2f"));

        clearButton = view.findViewById(R.id.doCleaning);

        productShortName = view.findViewById(R.id.productShortName);
        setupProductShortNameSpinner();

        qrId = view.findViewById(R.id.qrId);
        dni = view.findViewById(R.id.dni);
        notes = view.findViewById(R.id.notes);
        appTransactionIdEdt = view.findViewById(R.id.appTransactionIdEdt);
        rdgTrxType = (RadioGroup) view.findViewById(R.id.trxTypeRadio);
        rdTrxTypeAll = (RadioButton) view.findViewById(R.id.trxType_all);
        rdTrxTypeSale = (RadioButton) view.findViewById(R.id.trxType_sale);
        rdTrxTypeDV = (RadioButton) view.findViewById(R.id.trxType_unreferenced_devolution);

        operationMethodSpinner = view.findViewById(R.id.operationMethod);
        setupOperationMethodSpinner();

        edStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                Calendar date = Calendar.getInstance();
                new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        date.set(year, month, day);
                        new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                                date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                date.set(Calendar.MINUTE, minute);
                                updateDate(edStartDate, date.get(Calendar.DAY_OF_MONTH), date.get(Calendar.MONTH), date.get(Calendar.YEAR), date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE), 00);

                            }
                        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
                    }
                } , c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        edFinishDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                Calendar date = Calendar.getInstance();
                new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        date.set(year, month, day);
                        new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                                date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                date.set(Calendar.MINUTE, minute);
                                updateDate(edFinishDate, date.get(Calendar.DAY_OF_MONTH), date.get(Calendar.MONTH), date.get(Calendar.YEAR), date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE), 00);
                            }
                        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
                    }
                } , c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFilterClickedListener) {
            mListener = (OnFilterClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFilterClickedListener");
        }
    }

    private ArrayAdapter<String> setupArrayAdapter(List<String> array) {
        return new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, array) {
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

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFilterClickedListener {
        void onFilterClickedListener(PaymentProviderRequest request);
    }

    public void updateDate(EditText ed, int day, int month, int year, int hour, int minute, int second) {
        ed.setText(String.format("%02d/%02d/%d %02d:%02d:%02d", day, month + 1, year, hour, minute, second));
    }

    public BigDecimal toBigDecimal(String v) {
        try{
            NumberFormat formatter = NumberFormat.getNumberInstance();
            Number number = formatter.parse(v);
            return BigDecimal.valueOf(number.doubleValue());
        } catch (ParseException e){
            /**/
        }

        return new BigDecimal("0");
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


}
