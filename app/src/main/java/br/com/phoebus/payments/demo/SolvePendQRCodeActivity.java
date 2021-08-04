package br.com.phoebus.payments.demo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;
import br.com.phoebus.android.payments.api.ApplicationInfo;
import br.com.phoebus.android.payments.api.ErrorData;
import br.com.phoebus.android.payments.api.Payment;
import br.com.phoebus.android.payments.api.PaymentClient;
import br.com.phoebus.android.payments.api.QRCodeIntentType;
import br.com.phoebus.android.payments.api.QRCodePendencyRequest;
import br.com.phoebus.android.payments.api.QRCodePendencyResponse;
import br.com.phoebus.android.payments.api.ReversePayment;
import br.com.phoebus.android.payments.api.client.Client;
import br.com.phoebus.android.payments.api.exception.ClientException;
import br.com.phoebus.payments.demo.utils.AlertUtils;
import br.com.phoebus.payments.demo.utils.CredentialsUtils;
import br.com.phoebus.payments.demo.utils.Helper;

public class SolvePendQRCodeActivity extends AppCompatActivity {

    private EditText day;
    private EditText moth;
    private EditText year;
    private EditText hour;
    private EditText minute;
    private EditText second;
    private EditText qrId;
    private PaymentClient paymentClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(R.string.solvePendQRCodetitle);
        setContentView(R.layout.activity_solve_pend_qrcode);

        this.day = (EditText) findViewById(R.id.dayDate);
        this.moth = (EditText) findViewById(R.id.monthDate);
        this.year = (EditText) findViewById(R.id.yearDate);
        this.hour = (EditText) findViewById(R.id.hourDate);
        this.minute = (EditText) findViewById(R.id.minuteDate);
        this.second = (EditText) findViewById(R.id.secondDate);
        this.qrId = (EditText) findViewById(R.id.qrId);
        this.paymentClient = new PaymentClient();
    }

    public void doSolvePendQRCode(View view) {
        try {
            ApplicationInfo applicationInfo = CredentialsUtils.getMyAppInfo(this.getPackageManager(), this.getPackageName());
            QRCodePendencyRequest request = new QRCodePendencyRequest();

            if (this.qrId.getText() != null && !"".equals(this.qrId.getText().toString())) {
                request.setQrId(qrId.getText().toString());
            }
            request.setApplicationInfo(applicationInfo);
            Date date = new Date();
            EditText[] fields = {day, moth, year, hour, minute, second};
            if (isDateFilled(fields)) {
                if (isValid(fields)) {
                    date.setDate(Integer.parseInt((day).getText().toString()));
                    date.setMonth(Integer.parseInt((moth).getText().toString()) - 1);
                    date.setYear(Integer.parseInt((year).getText().toString()) - 1900);
                    date.setHours(Integer.parseInt((hour).getText().toString()));
                    date.setMinutes(Integer.parseInt((minute).getText().toString()));
                    date.setSeconds(Integer.parseInt((second).getText().toString()));
                    date.setTime(date.getTime() - Long.parseLong(String.valueOf(date.getTime()).substring(String.valueOf(date.getTime()).length() - 3)));
                    request.setDate(date);
                } else {
                    AlertUtils.showSnackBar(this.findViewById(android.R.id.content), getString(R.string.requieredFieldDateError));
                    return;
                }
            }

            paymentClient.bind(this, new Client.OnConnectionCallback() {

                @Override
                public void onConnected() {
                    try {
                        paymentClient.resolveQRCodePendency(request, new PaymentClient.PaymentCallback<QRCodePendencyResponse>() {
                            @Override
                            public void onSuccess(QRCodePendencyResponse response) {
                                if (response.getType().equals(QRCodeIntentType.PAYMENT) && response.getPayment() != null) {
                                    configureReturnData(response.getPayment());
                                    ResultActivity.callResultIntent(response.getPayment(), SolvePendQRCodeActivity.this, 0, null);
                                } else if (response.getType().equals(QRCodeIntentType.REVERSAL) && response.getReversePayment() != null) {
                                    configureReturnData(response.getReversePayment());
                                    ResultActivity.callResultIntent(response.getReversePayment(), SolvePendQRCodeActivity.this, 0);
                                }
                            }

                            @Override
                            public void onError(ErrorData errorData) {
                                Toast.makeText(SolvePendQRCodeActivity.this.getApplicationContext(), getString(R.string.main_activity_Pendings_fail) + ": " + errorData.getPaymentsResponseCode() + " / "
                                        + errorData.getAcquirerResponseCode() + " = " + errorData.getResponseMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (ClientException e) {
                        Toast.makeText(SolvePendQRCodeActivity.this.getApplicationContext(), "Falha na chamada do serviço: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onDisconnected(boolean b) {

                }
            });
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void configureReturnData(Payment data) {
        Intent intent = new Intent();
        intent.putExtra(Helper.EXTRA_QRCODE_PENDENCY, data.getPaymentId());
        setResult(RESULT_OK, intent);
    }

    private void configureReturnData(ReversePayment data) {
        Intent intent = new Intent();
        intent.putExtra(Helper.EXTRA_QRCODE_PENDENCY, data.getPaymentId());
        setResult(RESULT_OK, intent);
    }

    private boolean isValid(EditText[] fields) {
        int count = 0;
        for (EditText f : fields) {
            if (f.getText().toString().trim().length() > 0) {
                count += 1;
            }
        }
        if (count < fields.length) {
            return false;
        }
        return true;
    }

    private boolean isDateFilled(EditText[] fields) {
        for (EditText text : fields) {
            if (!TextUtils.isEmpty(text.getText())) {
                return true;
            }
        }
        return false;
    }
}
