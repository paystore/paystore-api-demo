package br.com.phoebus.payments.demo;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import androidx.appcompat.app.AppCompatActivity;
import br.com.phoebus.android.payments.api.ApplicationInfo;
import br.com.phoebus.android.payments.api.Credentials;
import br.com.phoebus.android.payments.api.ErrorData;
import br.com.phoebus.android.payments.api.PaymentClient;
import br.com.phoebus.android.payments.api.SupervisorPasswordRequest;
import br.com.phoebus.android.payments.api.exception.ClientException;

public class SupervisorPasswordValidateActivity extends AppCompatActivity {

    private PaymentClient paymentClient;

    EditText setSupervisorPasswordEdt;
    EditText setSoftwareVersionEdt;
    EditText setApplicationIdEdt;
    EditText setSecretTokenEdt;

    CheckBox MD5Check;
    CheckBox sendPasswordNullCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(R.string.setSupervisorPasswordBtn);
        setContentView(R.layout.activity_supervisor_password_validate);


        setSupervisorPasswordEdt = findViewById(R.id.setSupervisorPasswordEdt);
        setSoftwareVersionEdt = findViewById(R.id.setSoftwareVersionEdt);
        setApplicationIdEdt = findViewById(R.id.setApplicationIdEdt);
        setSecretTokenEdt = findViewById(R.id.setSecretTokenEdt);

        this.MD5Check = findViewById(R.id.MD5Check);
        this.sendPasswordNullCheck = findViewById(R.id.sendPasswordNullCheck);

        this.setDefaultValues();
        this.paymentClient = new PaymentClient();
        this.paymentClient.bind(this.getApplicationContext());

    }

    private void setDefaultValues() {

        this.setSupervisorPasswordEdt.setText(null);
        this.setSoftwareVersionEdt.setText(null);
        this.setApplicationIdEdt.setText(null);
        this.setSecretTokenEdt.setText(null);
        this.MD5Check.setChecked(true);
        this.sendPasswordNullCheck.setChecked(false);
    }

    public void validatePasswordButton(View view) {
        validatePassword();
    }

    public void validatePassword() {
        SupervisorPasswordRequest supervisorPasswordRequest = new SupervisorPasswordRequest();
        ApplicationInfo applicationInfo = new ApplicationInfo();
        Credentials credentials = new Credentials();
        applicationInfo.setCredentials(credentials);
        supervisorPasswordRequest.setApplicationInfo(applicationInfo);

        if (this.setSupervisorPasswordEdt.getText() != null && !"".equals(this.setSupervisorPasswordEdt.getText().toString()) && MD5Check.isChecked() && !sendPasswordNullCheck.isChecked()) {
            supervisorPasswordRequest.setSupervisorPasswordCheck(generateMD5Hash(setSupervisorPasswordEdt.getText().toString()));
        }else if (sendPasswordNullCheck.isChecked()){
            supervisorPasswordRequest.setSupervisorPasswordCheck(null);
        } else {
            supervisorPasswordRequest.setSupervisorPasswordCheck(setSupervisorPasswordEdt.getText().toString());
        }

        if (this.setSoftwareVersionEdt.getText() != null && !"".equals(this.setSoftwareVersionEdt.getText().toString())) {
            applicationInfo.setSoftwareVersion(setSoftwareVersionEdt.getText().toString());
        }
        if (this.setApplicationIdEdt.getText() != null && !"".equals(this.setApplicationIdEdt.getText().toString())) {
            credentials.setApplicationId(setApplicationIdEdt.getText().toString());
        }
        if (this.setSecretTokenEdt.getText().length() <= 23 ) {
            Toast.makeText(this, getString(R.string.diag_token_label), Toast.LENGTH_SHORT).show();
        }
        else if (this.setSecretTokenEdt.getText() != null && !"".equals(this.setSecretTokenEdt.getText().toString())) {
            credentials.setSecretToken(setSecretTokenEdt.getText().toString());
        }


        try {
            this.paymentClient.supervisorPasswordCheck(supervisorPasswordRequest, new PaymentClient.PaymentCallback<>() {
                @Override
                public void onSuccess(Boolean aBoolean) {
                    Toast.makeText(getApplicationContext(), getString(R.string.diag_validation_SDK) + aBoolean.toString().toUpperCase(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(ErrorData errorData) {
                    Toast.makeText(getApplicationContext(), getString(R.string.diag_error_validation_SDK) + errorData.getPaymentsResponseCode() +
                            " = " + errorData.getResponseMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (ClientException e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), getString(R.string.serviceCallFailed) +": " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static String generateMD5Hash(String input) {
        byte[] hashBytes = generateMD5HashBinary(input);

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }

        return hexString.toString();
    }

    public static byte[] generateMD5HashBinary(String input) {
        try {
            MessageDigest md5Digest = MessageDigest.getInstance("MD5");
            return md5Digest.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao gerar hash MD5", e);
        }
    }

}
