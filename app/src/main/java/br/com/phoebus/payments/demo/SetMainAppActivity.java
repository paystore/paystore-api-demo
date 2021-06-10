package br.com.phoebus.payments.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import br.com.phoebus.android.payments.api.ErrorData;
import br.com.phoebus.android.payments.api.PaymentClient;
import br.com.phoebus.android.payments.api.exception.ClientException;

public class SetMainAppActivity extends AppCompatActivity {

    private PaymentClient paymentClient;

    private EditText packageNameEdt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(R.string.setMainApp);
        setContentView(R.layout.activity_set_main_app);

        this.packageNameEdt = ((EditText) this.findViewById(R.id.packageNameEdt));

        this.packageNameEdt.setText("br.com.phoebus.payments.demo");

        this.paymentClient = new PaymentClient();
        this.paymentClient.bind(this.getApplicationContext());
    }

    public void doSetMainApp(View view) {

        if (!isDataValid()) return;

        try {
            this.paymentClient.setMainApp(packageNameEdt.getText().toString(), new PaymentClient.PaymentCallback() {
                @Override
                public void onSuccess(Object data) {

                    Toast.makeText(SetMainAppActivity.this, "App principal definido!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(ErrorData errorData) {
                    Toast.makeText(SetMainAppActivity.this, "Erro ao definir app principal: " + errorData.getPaymentsResponseCode() +
                            " = " + errorData.getResponseMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (ClientException e) {
            e.printStackTrace();
            Toast.makeText(SetMainAppActivity.this, "Falha na chamada do serviço: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }


    }

    private boolean isDataValid() {

        boolean ret = true;

        if ("".equals(this.packageNameEdt.getText().toString())) {
            this.packageNameEdt.setError(getString(R.string.requieredFieldError));
            ret = false;
        }

        return ret;
    }

}
