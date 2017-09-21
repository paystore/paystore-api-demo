package br.com.phoebus.payments.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import br.com.phoebus.android.payments.api.Payment;
import br.com.phoebus.android.payments.api.ReversePayment;
import br.com.phoebus.android.payments.api.utils.DataUtils;
import br.com.phoebus.android.payments.api.utils.Intents;

public class PaymentBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intents.action.ACTION_AFTER_PAYMENT)) {
            Payment payment = DataUtils.fromBundle(Payment.class, intent.getExtras(), Intents.extra.EXTRA_PAYMENT_RETURN);
            ResultActivity.callResultIntent(payment, context, Intent.FLAG_ACTIVITY_NEW_TASK);
        } else if (intent.getAction().equals(Intents.action.ACTION_AFTER_REVERSAL)) {
            ReversePayment reversePayment = DataUtils.fromBundle(ReversePayment.class, intent.getExtras(), Intents.extra.EXTRA_PAYMENT_RETURN);
            ResultActivity.callResultIntent(reversePayment, context, Intent.FLAG_ACTIVITY_NEW_TASK);
        }

    }
}
