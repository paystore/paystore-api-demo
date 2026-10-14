package br.com.phoebus.payments.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import br.com.phoebus.android.payments.api.ErrorData;
import br.com.phoebus.android.payments.api.InitializationRequest;
import br.com.phoebus.android.payments.api.PaymentClient;
import br.com.phoebus.android.payments.api.exception.ClientException;

public class StartInitializationActivity extends AppCompatActivity {

    Button startInitializationButton;
    EditText initializationToken;

    EditText initializationAction;

    private PaymentClient mPaymentClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_initialization);

        mPaymentClient = new PaymentClient();
        mPaymentClient.bind(this);

        startInitializationButton = findViewById(R.id.start_initialization);
        initializationToken = findViewById(R.id.token);
        initializationAction = findViewById(R.id.activityAction);

    }

    public void startInitialization(){
        InitializationRequest request = new InitializationRequest();

        if (this.initializationToken.getText() != null && !"".equals(this.initializationToken.getText().toString())) {
            request.setInstallToken(initializationToken.getText().toString());
        }

        if (this.initializationAction.getText() != null && !"".equals(this.initializationAction.getText().toString())) {
            request.setActivityAction(initializationAction.getText().toString());
        }

        try {
            this.mPaymentClient.startInitialization(request, new PaymentClient.PaymentCallback() {
                @Override
                public void onSuccess(Object data) {
                    Toast.makeText(StartInitializationActivity.this, "Inicialização concluida com sucesso", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(ErrorData errorData) {
                    Toast.makeText(StartInitializationActivity.this, "Falha na inicialização" + ": " + errorData.getPaymentsResponseCode() +
                            " = " + errorData.getResponseMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

    public void startInitializationButton(View view) {
        startInitialization();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPaymentClient.unbind(this);
    }
}