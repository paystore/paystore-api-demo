package br.com.phoebus.payments.demo;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import br.com.phoebus.android.payments.api.exception.ClientException;
import br.com.phoebus.android.payments.api.provider.PaymentProviderApi;
import br.com.phoebus.android.payments.api.provider.PaymentProviderRequest;
import br.com.phoebus.payments.demo.fragments.PaymentFilterFragment;
import br.com.phoebus.payments.demo.fragments.PaymentListFragment;
import br.com.phoebus.payments.demo.utils.FragmentUtils;

public class PaymentListActivity extends AppCompatActivity implements PaymentFilterFragment.OnFilterClickedListener{

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
            FragmentUtils.showFragment(this, PaymentListFragment.newInstance(api.findAll(request)), true);
        } catch (ClientException e) {
            showAlert("Falha na Solicitação: " + e.getMessage());
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

    private void showAlert(String message) {
        Snackbar.make(this.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }
}
