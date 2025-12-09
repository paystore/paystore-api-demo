package br.com.phoebus.payments.demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import br.com.phoebus.android.payments.api.ErrorData;
import br.com.phoebus.android.payments.api.PaymentClient;
import br.com.phoebus.android.payments.api.exception.ClientException;

public class LogoActivity extends AppCompatActivity {

    private PaymentClient paymentClient;

    private ImageView logo;

    private ImageView receiptLogo;

    private TextView textLogo;

    private TextView textReceiptLogo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(R.string.set_logo_title);
        setContentView(R.layout.activity_logo);

        this.paymentClient = new PaymentClient();
        this.paymentClient.bind(this.getApplicationContext());

        logo = findViewById(R.id.image_Logo);
        receiptLogo = findViewById(R.id.image_receipt_Logo);

        textLogo = findViewById(R.id.logo_value);
        textReceiptLogo = findViewById(R.id.logo_receipt_value);

        Button logoBtn = (Button) findViewById(R.id.get_logo_btn);
        logoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLogo();
            }
        });

        Button logoReceiptBtn = (Button) findViewById(R.id.receipt_logo_btn);
        logoReceiptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getReceiptLogo();
            }
        });
    }

    public void getLogo() {
        try {

            this.paymentClient.getLogo(new PaymentClient.PaymentCallback() {
                @Override
                public void onSuccess(Object object) {

                    String selectedLogoData = object.toString();
                    String logoValue = PaymentClient.BUNDLE_LOGO;

                    byte[] decodedString = Base64.decode(selectedLogoData, Base64.DEFAULT);
                    Bitmap decodedByteLogo = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    logo.setImageBitmap(decodedByteLogo);

                    Toast.makeText(LogoActivity.this, getString(R.string.set_logo_definedLogo) + ": " + selectedLogoData, Toast.LENGTH_SHORT).show();
                    textLogo.setText("Logo value: " + selectedLogoData);

                }

                @Override
                public void onError(ErrorData errorData) {

                    Toast.makeText(LogoActivity.this, getString(R.string.set_logo_definedLogoError) + ": " + errorData.getPaymentsResponseCode() +
                            " = " + errorData.getResponseMessage(), Toast.LENGTH_LONG).show();

                    logo.setImageResource(R.mipmap.ic_launcher);
                    String value = PaymentClient.BUNDLE_LOGO;

                    System.out.println("Logo received: " + value);

                }
            });

        } catch (ClientException e) {
            e.printStackTrace();
            Toast.makeText(LogoActivity.this, getString(R.string.serviceCallFailed) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void getReceiptLogo(){

        try {

            this.paymentClient.getReceiptLogo(new PaymentClient.PaymentCallback() {
                @Override
                public void onSuccess(Object object) {

                    String selectedReceiptLogo = object.toString();
                    String receiptLogoValue = PaymentClient.BUNDLE_LOGO;

                    System.out.println("onSuccess receipt logo: " + selectedReceiptLogo);
                    System.out.println("onSuccess receipt logo: " + receiptLogoValue);

                    byte[] decodedString = Base64.decode(selectedReceiptLogo, Base64.DEFAULT);
                    Bitmap decodedByteReceiptLogo = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    receiptLogo.setImageBitmap(decodedByteReceiptLogo);

                    Toast.makeText(LogoActivity.this, getString(R.string.set_logo_definedLogo) + ": " + receiptLogoValue, Toast.LENGTH_SHORT).show();
                    textReceiptLogo.setText("Receipt logo value: " + selectedReceiptLogo);

                }

                @Override
                public void onError(ErrorData errorData) {
                    Toast.makeText(LogoActivity.this, getString(R.string.set_receipt_logo_definedReceiptLogoError) + ": " + errorData.getPaymentsResponseCode() +
                            " = " + errorData.getResponseMessage(), Toast.LENGTH_LONG).show();

                    receiptLogo.setImageResource(R.mipmap.ic_launcher);
                    String selectedReceiptLogo = PaymentClient.BUNDLE_RECEIPT_LOGO;

                    System.out.println("Receipt logo recebida: " + selectedReceiptLogo);

                }
            });

        } catch (ClientException e) {
            e.printStackTrace();
            Toast.makeText(LogoActivity.this, getString(R.string.serviceCallFailed) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }
}
