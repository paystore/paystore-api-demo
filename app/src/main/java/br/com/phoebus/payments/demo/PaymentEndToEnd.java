package br.com.phoebus.payments.demo;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import br.com.phoebus.android.payments.api.ErrorData;
import br.com.phoebus.android.payments.api.PaymentClient;
import br.com.phoebus.android.payments.api.PaymentRequestV2;
import br.com.phoebus.android.payments.api.PaymentV2;
import br.com.phoebus.payments.demo.utils.AlertUtils;
import br.com.phoebus.payments.demo.utils.LogUtils;

public class PaymentEndToEnd {

    private Context mContext;
    private PaymentClient mPaymentClient;

    public PaymentEndToEnd(Context context, PaymentClient paymentClient) {
        this.mContext = context;
        this.mPaymentClient = paymentClient;
    }

    public void doPayment(PaymentRequestV2 paymentRequest) {

        try{
            mPaymentClient.startPaymentV2(paymentRequest, new PaymentClient.PaymentCallback<PaymentV2>() {

                @Override
                public void onSuccess(PaymentV2 paymentV2) {

                    confirmPayment(paymentV2);

                    LogUtils.writeLogCat(this, "onSuccess", "Pagamento iniciado com sucesso");

                }

                @Override
                public void onError(ErrorData errorData) {
                    AlertUtils.showToast(mContext, "ERROR: " + errorData.getPaymentsResponseCode() + " | Mensagem : " +  errorData.getResponseMessage());
                    LogUtils.writeLogCatE(mContext, "onError", "ON_ERROR " + errorData.getResponseMessage());

                }
            });
        } catch (Exception e){
            AlertUtils.showToast(mContext,"Falha ao realizar o pagamento" + e.getMessage());
            LogUtils.writeLogCatE(mContext, "startPaymentV2", e.getMessage());

        }
    }

    private void confirmPayment(PaymentV2 paymentV2)
    {
        try {

            mPaymentClient.confirmPayment(paymentV2.getPaymentId(), new PaymentClient.PaymentCallback() {
                @Override
                public void onSuccess(Object o) {
                    LogUtils.writeLogCat(this, "onSuccess", "Confirmação do pagamento realizado com sucesso");

                    Map<String, String> options = new HashMap<>();
                    options.put(ResultActivity.SHOW_BUTTON_CONFIRM, "F");

                    ResultActivity.callResultIntent(paymentV2, mContext, 0, options);
                }

                @Override
                public void onError(ErrorData errorData) {
                    AlertUtils.showToast(mContext, "ERROR: " + errorData.getPaymentsResponseCode() + " | Mensagem : "
                            +  errorData.getResponseMessage());
                    LogUtils.writeLogCatE(this, "onError", "Erro na confirmação" + errorData.getResponseMessage());

                }
            });
        }catch(Exception e){
            AlertUtils.showToast(mContext,"Falha ao realizar o pagamento" + e.getMessage());

        }
    }
}
