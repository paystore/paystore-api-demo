package br.com.phoebus.payments.demo.domain;

import android.content.Context;
import android.widget.Toast;

import br.com.phoebus.android.payments.api.ErrorData;
import br.com.phoebus.android.payments.api.PaymentClient;
import br.com.phoebus.payments.demo.R;
import br.com.phoebus.payments.demo.utils.Helper;

public class PaymentDomain {

    private PaymentClient mPaymentClient;
    private Context mContext;


    public PaymentDomain(PaymentClient paymentClient, Context context) {
        this.mContext = context;
        this.mPaymentClient = paymentClient;
    }

    public void doConfirmPayment(String paymentId) {

        if ("".equals(paymentId)) {
            Toast.makeText(mContext, R.string.payment_domain_paymentIdNotInformed, Toast.LENGTH_LONG).show();
            return;
        }

        try {
            mPaymentClient.confirmPayment(paymentId, new PaymentClient.PaymentCallback() {
                @Override
                public void onSuccess(Object data) {
                    Helper.writeLogCat(mContext, "onSuccess", mContext.getString(R.string.payment_domain_confirmationOfSuccessfulPayment));
                    Helper.showAlert(mContext, mContext.getString(R.string.payment_domain_confirmationOfSuccessfulPayment));
                }

                @Override
                public void onError(ErrorData errorData) {
                    Toast.makeText(mContext,  R.string.payment_domain_confirmationNotDone + ": " + errorData.getPaymentsResponseCode() + " / "
                            + errorData.getAcquirerResponseCode() + " = " + errorData.getResponseMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, R.string.serviceCallFailed + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    public void doCancelPayment(String paymentId) {

        if ("".equals(paymentId)) return;

        try {
            mPaymentClient.cancelPayment(paymentId, new PaymentClient.PaymentCallback() {
                @Override
                public void onSuccess(Object data) {
                    Helper.showAlert(mContext, mContext.getString(R.string.payment_domain_undoCompleted));
                }

                @Override
                public void onError(ErrorData errorData) {

                    Toast.makeText(mContext, R.string.payment_domain_undoNotDone + ": " + errorData.getPaymentsResponseCode() + " / "
                            + errorData.getAcquirerResponseCode() + " = " + errorData.getResponseMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(mContext, R.string.serviceCallFailed + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    public void doCancelReversePayment(String paymentId) {

        if ("".equals(paymentId)) return;

        try {
            mPaymentClient.cancelReversePayment(paymentId, new PaymentClient.PaymentCallback() {
                @Override
                public void onSuccess(Object data) {
                    Helper.writePrefs(mContext, Helper.KEY_LAST_CANCELABLE_REVERSE_ID, "", Helper.PREF_CONFIG);
                    Helper.showAlert(mContext, mContext.getString(R.string.payment_domain_undoCompleted));
                }

                @Override
                public void onError(ErrorData errorData) {

                    Toast.makeText(mContext, R.string.payment_domain_undoNotDone + ": " + errorData.getPaymentsResponseCode() + " / "
                            + errorData.getAcquirerResponseCode() + " = " + errorData.getResponseMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext,  R.string.serviceCallFailed + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
