package br.com.phoebus.payments.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

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
        this.paymentClient = new PaymentClient();
        this.paymentClient.bind(this.getApplicationContext());

        ListView lvTemas = (ListView) findViewById(R.id.lvTemas);

        final ArrayList<String> opcoes = loadTemas();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, opcoes);
        lvTemas.setAdapter(adapter);

        lvTemas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                    doSetTheme(opcoes.get(position));
            }
        });


    }

    private ArrayList<String> loadTemas() {

        ArrayList<String> list = new ArrayList<>();

        list.add("RedTheme");
        list.add("PinkTheme");
        list.add("PurpleTheme");
        list.add("DeepPurpleTheme");
        list.add("IndigoTheme");
        list.add("BlueTheme");
        list.add("LightBlueTheme");
        list.add("CyanTheme");
        list.add("TealTheme");
        list.add("GreenTheme");
        list.add("LightGreenTheme");
        list.add("LimeTheme");
        list.add("YellowTheme");
        list.add("AmberTheme");
        list.add("OrangeTheme");
        list.add("DeepOrangeTheme");
        list.add("BrownTheme");
        list.add("GreyTheme");
        list.add("BlueGreyTheme");

        return  list;

    }

    public void doSetTheme(String selectedTheme) {

        try {
            this.paymentClient.setTheme(selectedTheme, new PaymentClient.PaymentCallback() {
                @Override
                public void onSuccess(Object data) {

                    Toast.makeText(SetThemeActivity.this, getString(R.string.set_theme_definedTheme), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(ErrorData errorData) {
                    Toast.makeText(SetThemeActivity.this, getString(R.string.set_theme_definedThemeError) + ": " + errorData.getPaymentsResponseCode() +
                            " = " + errorData.getResponseMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (ClientException e) {
            e.printStackTrace();
            Toast.makeText(SetThemeActivity.this, getString(R.string.serviceCallFailed) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
        }


    }


}
