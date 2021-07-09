package br.com.phoebus.payments.demo;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;

import br.com.phoebus.android.payments.api.ApplicationInfo;
import br.com.phoebus.android.payments.api.Credentials;
import br.com.phoebus.android.payments.api.ErrorData;
import br.com.phoebus.android.payments.api.PaymentClient;
import br.com.phoebus.android.payments.api.ReprintRequest;
import br.com.phoebus.android.payments.api.client.Client;
import br.com.phoebus.android.payments.api.exception.ClientException;
import br.com.phoebus.payments.demo.utils.AlertUtils;

public class ReprintActivity extends AppCompatActivity {
    EditText ticketNumber;
    EditText day;
    EditText moth;
    EditText year;
    EditText hour;
    EditText minute;
    EditText second;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reprint);

        ticketNumber = (EditText) findViewById(R.id.ticketNumber);
        day = (EditText) findViewById(R.id.transactionDay);
        moth = (EditText) findViewById(R.id.transactionMonth);
        year = (EditText) findViewById(R.id.transactionYear);
        hour = (EditText) findViewById(R.id.transactionHour);
        minute = (EditText) findViewById(R.id.transactionMinute);
        second = (EditText) findViewById(R.id.transactionSecond);

        try {
            String packageName = getPackageName();
            PackageManager packageManager = this.getPackageManager();
            ((EditText) findViewById(R.id.softwareVersion)).setText(packageManager.getPackageInfo(packageName, 0).versionName);
        } catch (Exception e) {
            Log.e(ReprintActivity.class.toString(), e.getMessage());
        }
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

    public void reprint(View view) {
        PaymentClient mPaymentClient = new PaymentClient();
        ApplicationInfo applicationInfo = createAppInfo();

        ReprintRequest reprintRequest = new ReprintRequest();
        reprintRequest.setApplicationInfo(applicationInfo);

        if (this.ticketNumber.getText() != null && !"".equals(this.ticketNumber.getText().toString())) {
            reprintRequest.setTicketNumber(ticketNumber.getText().toString());
        }

        Date paymentDate = new Date();
        EditText[] fields = {day, moth, year, hour, minute, second};
        if (isDateFilled(fields)) {
            if (isValid(fields)) {
                paymentDate.setDate(Integer.parseInt((day).getText().toString()));
                paymentDate.setMonth(Integer.parseInt((moth).getText().toString())-1);
                paymentDate.setYear(Integer.parseInt((year).getText().toString())-1900);
                paymentDate.setHours(Integer.parseInt((hour).getText().toString()));
                paymentDate.setMinutes(Integer.parseInt((minute).getText().toString()));
                paymentDate.setSeconds(Integer.parseInt((second).getText().toString()));
                paymentDate.setTime(paymentDate.getTime() - Long.parseLong(String.valueOf(paymentDate.getTime()).substring(String.valueOf(paymentDate.getTime()).length() - 3)));
                reprintRequest.setPaymentDate(paymentDate);
            } else {
                AlertUtils.showSnackBar(this.findViewById(android.R.id.content), getString(R.string.requieredFieldDateError));
                return;
            }
        }
        mPaymentClient.bind(this, new Client.OnConnectionCallback() {
            @Override
            public void onConnected() {
                try {
                    mPaymentClient.reprint(reprintRequest, new PaymentClient.PaymentCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("PayStore App Demo", "Reprint has finished successfully!");
                        }

                        @Override
                        public void onError(ErrorData errorData) {
                            if (errorData != null && !TextUtils.isEmpty(errorData.getResponseMessage())) {
                                AlertUtils.showSnackBar(findViewById(android.R.id.content), errorData.getResponseMessage());
                            }
                            Log.e("PayStore App Demo", "Reprint has finished wrongfully!");
                        }
                    });
                } catch (ClientException e) {
                    if (!TextUtils.isEmpty(e.getMessage())) {
                        AlertUtils.showSnackBar(findViewById(android.R.id.content), e.getMessage());
                    }
                }
            }

            @Override
            public void onDisconnected(boolean b) {
                Log.d("PayStore App Demo", "Reprint disconnected!");
            }
        });
    }

    private ApplicationInfo createAppInfo() {
        ApplicationInfo applicationInfo = new ApplicationInfo();
        Credentials credentials = new Credentials();

        credentials.setApplicationId(((EditText) findViewById(R.id.applicationId)).getText().toString());
        credentials.setSecretToken(((EditText) findViewById(R.id.secretToken)).getText().toString());

        applicationInfo.setCredentials(credentials);

        EditText softwareVersion = (EditText) findViewById(R.id.softwareVersion);

        if (!TextUtils.isEmpty(softwareVersion.getText())) {
            applicationInfo.setSoftwareVersion(softwareVersion.getText().toString());
        }

        return applicationInfo;
    }
}