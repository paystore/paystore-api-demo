package br.com.phoebus.payments.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.phoebus.android.payments.api.PaymentType;
import br.com.phoebus.payments.demo.domain.AquirerEnum;
import br.com.phoebus.payments.demo.utils.Helper;

public class PaymentTypeListActivity extends AppCompatActivity {

    private final int[] OPTIONS_PROD_ADIQ = {0, 1, 2, 3, 4, 5};

    private final int[] OPTIONS_PROD_CIELO = {0, 1, 2, 3, 4, 9, 11, 12};

    private final int[] OPTIONS_PROD_STONE = {0, 1, 2, 3};

    private final int[] OPTIONS_PROD_PRISMA = {0, 1, 2, 3, 4, 9, 11, 12};

    private final int[] OPTIONS_PROD_AMEX = {0, 1, 2, 3, 4, 9, 11, 12};

    private final int[] OPTIONS_PROD_OTHER = {0, 1, 2, 3, 4, 9, 11, 12};

    private ListView mListMenu;
    private Button btContinuar;

    private Map<Integer, PaymentType> mapOptions;

    AquirerEnum aquirerSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        aquirerSelected = AquirerEnum.getById(Helper.readPrefsInteger(this, Helper.AQUIRER_CONFIG, Helper.PREF_CONFIG));

        this.setTitle(getString(R.string.paymentTypesAcquirer).concat(" ").concat(aquirerSelected.getName()));
        setContentView(R.layout.activity_payment_type_list);

        btContinuar = (Button) findViewById(R.id.btnContunuar);

        mListMenu = (ListView) findViewById(R.id.lvMenu);
        mListMenu.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        ArrayList<String> opcoes = getMenuOptions();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, opcoes);
        mListMenu.setAdapter(adapter);

        btContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long[] checkedItemIds = mListMenu.getCheckItemIds();

                List<PaymentType> paymentTypeListSelected = new ArrayList<>();

                for (int i = 0; i < checkedItemIds.length; i++)
                {
                    PaymentType product = mapOptions.get((int) checkedItemIds[i]);
                    paymentTypeListSelected.add(product);
                }

                Intent intent = new Intent();
                intent.putExtra(Helper.EXTRA_MAIN_MENU, getIntent().getIntExtra(Helper.EXTRA_MAIN_MENU, -1));
                intent.putExtra(Helper.EXTRA_LIST_PAYMENT_TYPE, (Serializable) paymentTypeListSelected);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private ArrayList<String> getMenuOptions() {

        //definindo opcoes e fazendo o "De Para" com PaymentTypes
        mapOptions = new HashMap<>();

        ArrayList<String> list = new ArrayList<>();
        //A ordem dos itens eh importante.
        list.add(getString(R.string.credit));
        mapOptions.put(0, PaymentType.CREDIT);

        list.add(getString(R.string.debit));
        mapOptions.put(1, PaymentType.DEBIT);

        list.add(getString(R.string.credit_store));
        mapOptions.put(2, PaymentType.CREDIT_STORE);

        list.add(getString(R.string.credit_admin));
        mapOptions.put(3, PaymentType.CREDIT_ADMIN);

        list.add(getString(R.string.pre_authorization));
        mapOptions.put(4, PaymentType.PRE_AUTHORIZATION);

        list.add(getString(R.string.pre_authorization_confirmation));
        mapOptions.put(5, PaymentType.PRE_AUTHORIZATION_CONFIRMATION);

        list.add(getString(R.string.pre_authorization_credit_admin));
        mapOptions.put(6, PaymentType.PRE_AUTHORIZATION_CREDIT_ADMIN);

        list.add(getString(R.string.pre_authorization_credit_store));
        mapOptions.put(7, PaymentType.PRE_AUTHORIZATION_CREDIT_STORE);

        list.add(getString(R.string.pre_authorization_substitutive));
        mapOptions.put(8, PaymentType.PRE_AUTHORIZATION_SUBSTITUTIVE);

        list.add(getString(R.string.debit_postdated));
        mapOptions.put(9, PaymentType.DEBIT_POSTDATED);

        list.add(getString(R.string.voucher));
        mapOptions.put(10, PaymentType.VOUCHER);

        list.add(getString(R.string.voucher_meal));
        mapOptions.put(11, PaymentType.VOUCHER_MEAL);

        list.add(getString(R.string.voucher_feed));
        mapOptions.put(12, PaymentType.VOUCHER_FEED);


        return list;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        int[] optionsProdAquirerSelected = {};

        if (AquirerEnum.ADIQ.equals(aquirerSelected))
        {
            optionsProdAquirerSelected = OPTIONS_PROD_ADIQ;
        }
        else if (AquirerEnum.CIELO.equals(aquirerSelected)) {
            optionsProdAquirerSelected = OPTIONS_PROD_CIELO;
        }
        else if (AquirerEnum.STONE.equals(aquirerSelected)) {
            optionsProdAquirerSelected = OPTIONS_PROD_STONE;
        }
        else if (AquirerEnum.PRISMA.equals(aquirerSelected)) {
            optionsProdAquirerSelected = OPTIONS_PROD_PRISMA;
        }
        else if (AquirerEnum.AMEX.equals(aquirerSelected)) {
            optionsProdAquirerSelected = OPTIONS_PROD_AMEX;
        }

        else if (AquirerEnum.OTHER.equals(aquirerSelected)) {
            optionsProdAquirerSelected = OPTIONS_PROD_OTHER;
        }

        //marcando os itens
        for (int i = 0; i < optionsProdAquirerSelected.length; i++)
        {
            mListMenu.setItemChecked(optionsProdAquirerSelected[i], true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}
