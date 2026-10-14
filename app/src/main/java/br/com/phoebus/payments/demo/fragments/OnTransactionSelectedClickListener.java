package br.com.phoebus.payments.demo.fragments;

import br.com.phoebus.android.payments.api.ReversePaymentV2;
import br.com.phoebus.android.payments.api.provider.response.ProviderResponse;

public interface OnTransactionSelectedClickListener {
    void onTransactionSelectedClick(ProviderResponse transaction);
    void onTransactionReverseSelectedClick(ReversePaymentV2 reversePaymentV2);
}
