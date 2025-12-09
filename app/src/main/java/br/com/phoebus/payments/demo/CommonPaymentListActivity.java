package br.com.phoebus.payments.demo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import br.com.phoebus.android.payments.api.Payment;
import br.com.phoebus.android.payments.api.PaymentClient;
import br.com.phoebus.android.payments.api.PaymentStatus;
import br.com.phoebus.android.payments.api.provider.PaymentProviderApi;
import br.com.phoebus.android.payments.api.provider.PaymentProviderRequest;
import br.com.phoebus.payments.demo.domain.PaymentDomain;
import br.com.phoebus.payments.demo.fragments.OnPaymentSelectedClickListener;
import br.com.phoebus.payments.demo.fragments.PaymentListFragment;
import br.com.phoebus.payments.demo.utils.AlertUtils;
import br.com.phoebus.payments.demo.utils.CredentialsUtils;
import br.com.phoebus.payments.demo.utils.FragmentUtils;
import br.com.phoebus.payments.demo.utils.Helper;

public class CommonPaymentListActivity extends AppCompatActivity implements OnPaymentSelectedClickListener {

    private PaymentClient paymentClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.paymentClient = new PaymentClient();
        this.paymentClient.bind(getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();

        List<Payment> listPayments = null;
        if (Helper.EXTRA_OPTION_CONFIRM.equals(getIntent().getExtras().getString(Helper.EXTRA_OPTION)) ||
                Helper.EXTRA_OPTION_CANCEL.equals(getIntent().getExtras().getString(Helper.EXTRA_OPTION))) {
            listPayments = getListPaymentPend();
        } else if (Helper.EXTRA_OPTION_REVERSE.equals(getIntent().getExtras().getString(Helper.EXTRA_OPTION))) {
            listPayments = getListPaymentConfirm();
        } else if (Helper.EXTRA_OPTION_PRINT.equals(getIntent().getExtras().getString(Helper.EXTRA_OPTION))) {
            listPayments = getListPaymentPrintReceipt();
        } else if (Helper.EXTRA_OPTION_CANCEL_REVERSE.equals(getIntent().getExtras().getString(Helper.EXTRA_OPTION))) {
            listPayments = getListCancelableReverse();
        }else if (Helper.EXTRA_OPTION_REPRINT.equals(getIntent().getExtras().getString(Helper.EXTRA_OPTION))) {
            listPayments = getListPaymentPrintReceipt();
        }

        showList(listPayments);
    }

    private void showList(List<Payment> listPayments) {
        if (listPayments == null || listPayments.isEmpty()) {
            this.finish();
        } else {
            FragmentUtils.showFragment(this, PaymentListFragment.newInstance(listPayments, this));
        }
    }

    private List<Payment> getListPaymentPend() {
        List<PaymentStatus> status = Arrays.asList(new PaymentStatus[]{PaymentStatus.PENDING});
        return getListPayment(status);
    }

    private List<Payment> getListPaymentConfirm() {
        List<PaymentStatus> status = Arrays.asList(new PaymentStatus[]{PaymentStatus.CONFIRMED});
        return getListPayment(status);
    }

    private List<Payment> getListPaymentPrintReceipt() {
        List<PaymentStatus> status = Arrays.asList(new PaymentStatus[]{PaymentStatus.CONFIRMED, PaymentStatus.PENDING, PaymentStatus.REVERSED});
        return getListPayment(status);
    }

    private List<Payment> getListPaymentReverse() {
        List<PaymentStatus> status = Arrays.asList(new PaymentStatus[]{PaymentStatus.REVERSED});
        return getListPayment(status);
    }

    private List<Payment> getListCancelableReverse() {
        Context context = getApplicationContext();
        PaymentProviderApi api = PaymentProviderApi.create(this);
        PaymentProviderRequest request = null;
        List<Payment> payments = new ArrayList<>();
        String paymentId = Helper.readPrefsString(context, Helper.KEY_LAST_CANCELABLE_REVERSE_ID, Helper.PREF_CONFIG);
        if (!TextUtils.isEmpty(paymentId)) {
            try {
                request = new PaymentProviderRequest(CredentialsUtils.getMyAppInfo(this.getPackageManager(), this.getPackageName()), defaultDateRequest());
                request.setPaymentId(paymentId);
                request.setStatus(Collections.singletonList(PaymentStatus.CONFIRMED));
                payments = api.findAll(request);
            } catch (Exception e) {
                showSnackBar(getString(R.string.requestFailed) + ": " + e.getMessage());
            }
        }

        if (payments.isEmpty()) {
            AlertUtils.showToast(this, getString(R.string.PaymentsList_no_pending_reversal));
        }

        return payments;
    }

    private List<Payment> getListPayment(List<PaymentStatus> status) {
        PaymentProviderApi api = PaymentProviderApi.create(this);
        List<Payment> listPayments = new ArrayList<>();
        try {
            PaymentProviderRequest request = new PaymentProviderRequest(CredentialsUtils.getMyAppInfo(this.getPackageManager(), this.getPackageName()), defaultDateRequest());
            request.setStatus(status);
            listPayments = api.findAll(request);
            if (listPayments.isEmpty()) {
                AlertUtils.showToast(this, getString(R.string.PaymentsList_notFoundPaymentsConfirm));
            }

        } catch (Exception e) {
            showSnackBar(getString(R.string.requestFailed) + ": " + e.getMessage());
        }
        return listPayments;
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

    public Date defaultDateRequest() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);

        return calendar.getTime();
    }

    private void showSnackBar(String message) {
        AlertUtils.showSnackBar(this.findViewById(android.R.id.content), message);
    }

    @Override
    public void onPaymentSelectedClick(Payment payment) {
        if (Helper.EXTRA_OPTION_CONFIRM.equals(getIntent().getExtras().getString(Helper.EXTRA_OPTION))) {
            PaymentDomain pd = new PaymentDomain(paymentClient, this);
            pd.doConfirmPayment(payment.getPaymentId());
            this.finish();

        } else if (Helper.EXTRA_OPTION_REVERSE.equals(getIntent().getExtras().getString(Helper.EXTRA_OPTION))) {

            Intent intent = new Intent(this, ReversePaymentActivity.class);
            intent.putExtra(Helper.EXTRA_PAYMENT_ID, payment.getPaymentId());
            intent.putExtra(Helper.EXTRA_VALUE, payment.getValue());
            //intent.putExtra(Helper.EXTRA_APP_PAYMENT_ID, payment.getAppTransactionId());
            startActivityForResult(intent, Helper.RETURN_REVERSE);

        } else if (Helper.EXTRA_OPTION_CANCEL.equals(getIntent().getExtras().getString(Helper.EXTRA_OPTION))) {

            PaymentDomain pd = new PaymentDomain(paymentClient, this);
            pd.doCancelPayment(payment.getPaymentId());
            onBackPressed();
        } else if (Helper.EXTRA_OPTION_CANCEL_REVERSE.equals(getIntent().getExtras().getString(Helper.EXTRA_OPTION))) {

            PaymentDomain pd = new PaymentDomain(paymentClient, this);
            pd.doCancelReversePayment(payment.getPaymentId());
            onBackPressed();
        } else if (Helper.EXTRA_OPTION_PRINT.equals(getIntent().getExtras().getString(Helper.EXTRA_OPTION))) {
            Intent intent = new Intent(this, PrintReceiptActivity.class);
            intent.putExtra(Helper.EXTRA_PAYMENT_ID, payment.getPaymentId());
            startActivityForResult(intent, Helper.RETURN_PRINT_RECEIPT);
        } else if (Helper.EXTRA_OPTION_REPRINT.equals(getIntent().getExtras().getString(Helper.EXTRA_OPTION))) {
            Intent intent = new Intent(this, ReprintReceiptV2Activity.class);
            intent.putExtra(Helper.EXTRA_PAYMENT_ID, payment.getPaymentId());
            startActivityForResult(intent, Helper.RETURN_REPRINT_RECEIPT);
        }
    }

}
