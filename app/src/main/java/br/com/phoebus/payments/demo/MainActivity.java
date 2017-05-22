package br.com.phoebus.payments.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.math.BigDecimal;

public class MainActivity extends AppCompatActivity {

    private static final int RETURN_PAYMENT = 1;
    private static final int RETURN_REVERSE = 2;

    public static final String EXTRA_VALUE = "extra.value";
    public static final String EXTRA_APP_PAYMENT_ID = "extra.appPaymentId";
    public static final String EXTRA_PAYMENT_ID = "extra.paymentId";
    public static final String EXTRA_REVERSE_PAYMENT_ID = "extra.reversePaymentId";

    private BigDecimal lastValue = null;
    private String lastAppPaymentId = null;
    private String lastPaymentId = null;

    private String lastReversePaymentId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openPaymentActivity(View view) {
        Intent intent = new Intent(this, PaymentActivity.class);
        startActivityForResult(intent, RETURN_PAYMENT);
    }

    public void confirm(View view) {
        Intent intent = new Intent(this, ConfirmPaymentActivity.class);
        intent.putExtra(EXTRA_PAYMENT_ID, lastPaymentId);
        startActivity(intent);
    }

    public void cancel(View view) {
        Intent intent = new Intent(this, CancelPaymentActivity.class);
        intent.putExtra(EXTRA_PAYMENT_ID, lastPaymentId);
        startActivity(intent);
    }

    public void reverse(View view) {
        Intent intent = new Intent(this, ReversePaymentActivity.class);
        intent.putExtra(EXTRA_PAYMENT_ID, lastPaymentId);
        intent.putExtra(EXTRA_VALUE, lastValue);
        intent.putExtra(EXTRA_APP_PAYMENT_ID, lastAppPaymentId);
        startActivityForResult(intent, RETURN_REVERSE);
    }

    public void cancelReverse(View view) {
        Intent intent = new Intent(this, CancelReversePaymentActivity.class);
        intent.putExtra(EXTRA_REVERSE_PAYMENT_ID, lastReversePaymentId);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        if (requestCode == RETURN_PAYMENT) {
            this.lastAppPaymentId = data.getStringExtra(EXTRA_APP_PAYMENT_ID);
            this.lastValue = (BigDecimal) data.getSerializableExtra(EXTRA_VALUE);
            this.lastPaymentId = data.getStringExtra(EXTRA_PAYMENT_ID);
        }

        if (requestCode == RETURN_REVERSE) {
            this.lastReversePaymentId = data.getStringExtra(EXTRA_REVERSE_PAYMENT_ID);
        }
    }

    public void listPayments(View view) {
        startActivity(new Intent(this, PaymentListActivity.class));
    }
}
