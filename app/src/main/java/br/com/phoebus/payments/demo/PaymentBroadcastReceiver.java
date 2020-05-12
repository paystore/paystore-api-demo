package br.com.phoebus.payments.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import br.com.phoebus.android.payments.api.Payment;
import br.com.phoebus.android.payments.api.ReversePayment;
import br.com.phoebus.android.payments.api.utils.DataUtils;
import br.com.phoebus.android.payments.api.utils.Intents;
import br.com.phoebus.payments.demo.utils.DataTypeUtils;
import br.com.phoebus.payments.demo.utils.Helper;

public class PaymentBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intents.action.ACTION_AFTER_PAYMENT)) {
            Payment payment = DataUtils.fromBundle(Payment.class, intent.getExtras(), Intents.extra.EXTRA_PAYMENT_RETURN);
            //PaymentV2 paymentv2 = DataUtils.fromBundle(PaymentV2.class, intent.getExtras(), Intents.extra.EXTRA_PAYMENT_RETURN_V2);

            Helper.writeLogCat(this, "onReceive","Ident.do Pagamento ".concat(payment.getPaymentId()));
            Helper.writeLogCat(this, "onReceive","Ident. para a Adquirente ".concat(payment.getAcquirerId()));
            Helper.writeLogCat(this, "onReceive","Número de Autorização ".concat(payment.getAcquirerAuthorizationNumber()));
            Helper.writeLogCat(this, "onReceive","Código de Resposta ".concat(payment.getAcquirerResponseCode()));
            Helper.writeLogCat(this, "onReceive","Data/hora Adquirente ".concat(DataTypeUtils.getAsString(payment.getAcquirerResponseDate())));

            //Eh possivel chamar uma activity aqui
//            ResultActivity.callResultIntent(payment, context, Intent.FLAG_ACTIVITY_NEW_TASK);
        } else if (intent.getAction().equals(Intents.action.ACTION_AFTER_REVERSAL)) {
            ReversePayment reversePayment = DataUtils.fromBundle(ReversePayment.class, intent.getExtras(), Intents.extra.EXTRA_PAYMENT_RETURN);

            Helper.writeLogCat(this, "onReceive","Ident.do Pagamento ".concat(reversePayment.getPaymentId()));
            Helper.writeLogCat(this, "onReceive","Ident. para a Adquirente ".concat(reversePayment.getAcquirerId()));
            Helper.writeLogCat(this, "onReceive","Número de Autorização ".concat(reversePayment.getAcquirerAuthorizationNumber()));
            Helper.writeLogCat(this, "onReceive","Código de Resposta ".concat(reversePayment.getAcquirerResponseCode()));
            Helper.writeLogCat(this, "onReceive","Data/hora Adquirente ".concat(DataTypeUtils.getAsString(reversePayment.getAcquirerResponseDate())));
            Helper.writeLogCat(this, "onReceive","Pode ser Desfeito ".concat((reversePayment.getCancelable() ? "Sim" : "Não")));

        }

    }
}
