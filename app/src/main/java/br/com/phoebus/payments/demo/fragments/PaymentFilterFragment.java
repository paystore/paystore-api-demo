package br.com.phoebus.payments.demo.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.com.phoebus.android.payments.api.PaymentStatus;
import br.com.phoebus.android.payments.api.provider.PaymentProviderRequest;
import br.com.phoebus.payments.demo.R;
import br.com.phoebus.payments.demo.utils.CredentialsUtils;
import br.com.phoebus.payments.demo.utils.MoneyWatcher;

public class PaymentFilterFragment extends Fragment{
    private OnFilterClickedListener mListener;
    private PaymentProviderRequest mPaymentRequest;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private Button mButton;
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

    public static PaymentFilterFragment newInstance() {
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

                    mPaymentRequest.setStatus(status);

                    if (mListener != null) {
                        mListener.onFilterClickedListener(mPaymentRequest);
                    }
                } catch (PackageManager.NameNotFoundException e) {

                } catch (ParseException e) {

                }
            }
        });

        return view;
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


        //text input format
        edMinValue.setText("0,00");
        edMaxValue.setText("0,00");
        edMinValue.addTextChangedListener(new MoneyWatcher(edMinValue, "%,.2f"));
        edMaxValue.addTextChangedListener(new MoneyWatcher(edMaxValue, "%,.2f"));

        edStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        updateDate(edStartDate, day, month, year);
                    }
                } , c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        edFinishDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        updateDate(edFinishDate, day, month, year);
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

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFilterClickedListener {
        void onFilterClickedListener(PaymentProviderRequest request);
    }

    public void updateDate(EditText ed, int day, int month, int year){
        ed.setText(String.format("%02d/%02d/%d", day, month + 1, year));
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

}
