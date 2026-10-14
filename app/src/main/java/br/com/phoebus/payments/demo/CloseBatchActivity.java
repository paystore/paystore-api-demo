package br.com.phoebus.payments.demo;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import br.com.phoebus.android.payments.api.ApplicationInfo;
import br.com.phoebus.android.payments.api.Credentials;
import br.com.phoebus.android.payments.api.ErrorData;
import br.com.phoebus.android.payments.api.PaymentClient;
import br.com.phoebus.android.payments.api.SettleRequestResponse;
import br.com.phoebus.android.payments.api.SettleRequestResponseV2;
import br.com.phoebus.android.payments.api.SettlementRequest;
import br.com.phoebus.android.payments.api.client.Client;
import br.com.phoebus.payments.demo.utils.AlertUtils;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class CloseBatchActivity extends AppCompatActivity {
    private EditText versaoDoSoftware;
    private EditText idApp;
    private EditText token;
    private CheckBox telaComprovante;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(getString(R.string.closeBatch));
        setContentView(R.layout.activity_close_batch);

        versaoDoSoftware = findViewById(R.id.softwareVersion);
        idApp = findViewById(R.id.applicationId);
        token = findViewById(R.id.secretToken);
        telaComprovante = findViewById(R.id.showBatchReceiptView);

        try {
            String packageName = getPackageName();
            PackageManager packageManager = this.getPackageManager();
            versaoDoSoftware.setText(packageManager.getPackageInfo(packageName, 0).versionName);
        } catch (Exception e) {
            Log.e(CloseBatchActivity.class.toString(), e.getMessage());
        }
    }

    public void closeBatch(View view) {
        try {
            PaymentClient mPaymentClient = new PaymentClient();
            ApplicationInfo applicationInfo = createAppInfo();
            SettlementRequest settlementRequest = new SettlementRequest();
            settlementRequest.setApplicationInfo(applicationInfo);
            settlementRequest.setPrintMerchantReceipt(telaComprovante.isChecked());
            mPaymentClient.bind(this, new Client.OnConnectionCallback() {
                @Override
                public void onConnected() {
                    try {
                        mPaymentClient.closeBatch(settlementRequest, new PaymentClient.PaymentCallback<SettleRequestResponse>() {
                            @Override
                            public void onSuccess(SettleRequestResponse settleRequestResponseV2) {
                                ResultActivity.callResultIntent(settleRequestResponseV2, CloseBatchActivity.this, FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP);

                            }

                            @Override
                            public void onError(ErrorData errorData) {
                                Toast.makeText(CloseBatchActivity.this, errorData.getResponseMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        Toast.makeText(CloseBatchActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onDisconnected(boolean b) {
                    Log.e("MainActivity lote", getString(R.string.disconnected));
                }
            });

        } catch (Exception e) {
            Toast.makeText(CloseBatchActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showSnackBar(String message) {
        AlertUtils.showSnackBar(this.findViewById(android.R.id.content), message);
    }

    private ApplicationInfo createAppInfo() {
        ApplicationInfo applicationInfo = new ApplicationInfo();
        Credentials credentials = new Credentials();

        credentials.setApplicationId(idApp.getText().toString());
        credentials.setSecretToken(token.getText().toString());

        applicationInfo.setCredentials(credentials);
        if (!TextUtils.isEmpty(versaoDoSoftware.getText().toString())) {
            applicationInfo.setSoftwareVersion(versaoDoSoftware.getText().toString());
        }

        return applicationInfo;
    }
}
