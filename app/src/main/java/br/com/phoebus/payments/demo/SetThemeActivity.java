package br.com.phoebus.payments.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import br.com.phoebus.android.payments.api.ErrorData;
import br.com.phoebus.android.payments.api.PaymentClient;
import br.com.phoebus.android.payments.api.exception.ClientException;

public class SetThemeActivity extends AppCompatActivity {

    private PaymentClient paymentClient;

    private EditText themeEdt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(R.string.setThemeBtn);
        setContentView(R.layout.activity_set_theme);

        this.themeEdt = ((EditText) this.findViewById(R.id.themeEdt));

        this.themeEdt.setText("GreyTheme");

        this.paymentClient = new PaymentClient();
        this.paymentClient.bind(this.getApplicationContext());
    }

    public void doSetTheme(View view) {

        if (!isDataValid()) return;

        try {
            this.paymentClient.setTheme(themeEdt.getText().toString(), new PaymentClient.PaymentCallback() {
                @Override
                public void onSuccess(Object data) {

                    Toast.makeText(SetThemeActivity.this, "Tema definido!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(ErrorData errorData) {
                    Toast.makeText(SetThemeActivity.this, "Erro ao definir tema: " + errorData.getPaymentsResponseCode() +
                            " = " + errorData.getResponseMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (ClientException e) {
            e.printStackTrace();
            Toast.makeText(SetThemeActivity.this, "Falha na chamada do serviço: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }


    }

    private boolean isDataValid() {

        boolean ret = true;

        if ("".equals(this.themeEdt.getText().toString())) {
            this.themeEdt.setError(getString(R.string.requieredFieldError));
            ret = false;
        }

        return ret;
    }

}
