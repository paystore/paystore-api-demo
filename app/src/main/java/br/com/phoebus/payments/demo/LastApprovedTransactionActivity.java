package br.com.phoebus.payments.demo;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import br.com.phoebus.android.payments.api.ReversePaymentV2;
import br.com.phoebus.android.payments.api.provider.PaymentProviderApi;
import br.com.phoebus.android.payments.api.provider.response.ProviderResponse;
import br.com.phoebus.payments.demo.fragments.OnTransactionSelectedClickListener;
import br.com.phoebus.payments.demo.fragments.TransactionListFragment;
import br.com.phoebus.payments.demo.utils.CredentialsUtils;
import br.com.phoebus.payments.demo.utils.FragmentUtils;

public class LastApprovedTransactionActivity extends AppCompatActivity implements OnTransactionSelectedClickListener {

    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.title_last_approved_trx);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        generatedLastTrx();
    }

    private void generatedLastTrx() {

        ProviderResponse providerResponse;
        try {
            PaymentProviderApi api = PaymentProviderApi.create(this);
            providerResponse = api.findLastTransaction(CredentialsUtils.getMyAppInfo(getApplicationContext().getPackageManager(), getApplicationContext().getPackageName()));

            Fragment transactionListFragment = new TransactionListFragment
                    (providerResponse != null ? Collections.singletonList(providerResponse) : Collections.emptyList(), this);
            FragmentUtils.showFragment(this, transactionListFragment);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
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
