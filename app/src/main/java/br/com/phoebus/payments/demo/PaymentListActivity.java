package br.com.phoebus.payments.demo;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.phoebus.android.payments.api.Payment;
import br.com.phoebus.android.payments.api.provider.PaymentProviderApi;
import br.com.phoebus.android.payments.api.provider.PaymentProviderRequest;
import br.com.phoebus.payments.demo.fragments.OnPaymentSelectedClickListener;
import br.com.phoebus.payments.demo.fragments.PaymentFilterFragment;
import br.com.phoebus.payments.demo.fragments.PaymentListFragment;
import br.com.phoebus.payments.demo.utils.AlertUtils;
import br.com.phoebus.payments.demo.utils.FragmentUtils;

public class PaymentListActivity extends AppCompatActivity implements PaymentFilterFragment.OnFilterClickedListener, OnPaymentSelectedClickListener {

    PaymentProviderApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        api = PaymentProviderApi.create(this);

        FragmentUtils.showFragment(this, PaymentFilterFragment.newInstance());
    }

    @Override
    public void onFilterClickedListener(PaymentProviderRequest request) {
        try {
            List listPayments = api.findAll(request);
            FragmentUtils.showFragment(this, PaymentListFragment.newInstance(listPayments, this), true);
        } catch (Exception e) {
            showSnackBar("Falha na Solicitação: " + e.getMessage());
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

    private void showSnackBar(String message) {
        AlertUtils.showSnackBar(this.findViewById(android.R.id.content), message);
    }

    @Override
    public void onPaymentSelectedClick(Payment payment) {
        Map<String, String> options = new LinkedHashMap<>();
        options.put(ResultActivity.SHOW_BUTTON_CONFIRM, "F");
        ResultActivity.callResultIntent(payment, this, 0, options);
    }
}
