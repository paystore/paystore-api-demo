package br.com.phoebus.payments.demo;

import android.content.Context;
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

                    confirmPayment(paymentV2.getPaymentId());

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

    private void confirmPayment(String paymentId)
    {
        try {

            mPaymentClient.confirmPayment(paymentId, new PaymentClient.PaymentCallback() {
                @Override
                public void onSuccess(Object o) {
                    LogUtils.writeLogCat(this, "onSuccess", "Confirmação do pagamento realizado com sucesso");

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
