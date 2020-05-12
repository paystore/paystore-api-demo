package br.com.phoebus.payments.demo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.jakewharton.threetenabp.AndroidThreeTen;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.phoebus.android.payments.api.Payment;
import br.com.phoebus.android.payments.api.PaymentClient;
import br.com.phoebus.android.payments.api.PaymentStatus;
import br.com.phoebus.android.payments.api.provider.PaymentContract;
import br.com.phoebus.android.payments.api.provider.PaymentProviderApi;
import br.com.phoebus.android.payments.api.provider.PaymentProviderRequest;
import br.com.phoebus.android.payments.api.client.Client;
import br.com.phoebus.payments.demo.domain.AquirerEnum;
import br.com.phoebus.payments.demo.domain.PaymentDomain;

import br.com.phoebus.payments.demo.utils.CredentialsUtils;
import br.com.phoebus.payments.demo.utils.Helper;

public class MainActivity extends AppCompatActivity {


    public static final String EXTRA_VALUE = "extra.value";
    public static final String EXTRA_APP_PAYMENT_ID = "extra.appPaymentId";
    public static final String EXTRA_PAYMENT_ID = "extra.paymentId";

    public static final String EXTRA_IS_PAYMENT_END_TO_PAYMENT = "extra.isPaymentEndToEnd";
    public static final String EXTRA_OPTION = "extra.option";
    public static final String EXTRA_OPTION_CONFIRM = "extra.option.confirm";

    public final int MENU_PGTO_END_TO_END = 0;
    public final int MENU_PAGAMENTO = 1;
    private final int MENU_CONFIRMAR = 2;
    private final int MENU_CANCELAR_PGTO = 3;
    private final int MENU_ESTORNAR = 4;
    private final int MENU_CANCELAR_ESTORNO = 5;
    private final int MENU_CONSULTAR = 6;
    private final int MENU_RESOLVER_PEND = 7;
    private final int MENU_DEFINIR_TEMA = 8;
    private final int MENU_DEFINIR_APP = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //adquirente default.
        this.setTitle("Adiquirente: ".concat(AquirerEnum.CIELO.getName().toUpperCase()));
        Helper.writePrefs(this, Helper.AQUIRER_CONFIG, AquirerEnum.CIELO.getId(), Helper.PREF_CONFIG);

        AndroidThreeTen.init(getApplication());

        ListView listaMenu = (ListView) findViewById(R.id.lvMenu);

        ArrayList<String> opcoes = getMenuOptions();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, opcoes);
        listaMenu.setAdapter(adapter);

        listaMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intentPaymentTypeList = new Intent(MainActivity.this, PaymentTypeListActivity.class);

                switch (position) {
                    case MENU_PGTO_END_TO_END:
                        // a activity de pagamento será chamada no método onActivityResult, apos selecão dos tipos de pgto.
                        intentPaymentTypeList.putExtra(Helper.EXTRA_MAIN_MENU, MENU_PGTO_END_TO_END);
                        startActivityForResult(intentPaymentTypeList, MENU_PGTO_END_TO_END);
                        break;
                    case MENU_PAGAMENTO:
                        intentPaymentTypeList.putExtra(Helper.EXTRA_MAIN_MENU, MENU_PAGAMENTO);
                        startActivityForResult(intentPaymentTypeList, MENU_PAGAMENTO);
                        break;
                    case MENU_CONFIRMAR:
                        confirm();
                        break;
                    case MENU_CANCELAR_PGTO:
                        cancel();
                        break;
                    case MENU_ESTORNAR:
                        reverse();
                        break;
                    case MENU_CANCELAR_ESTORNO:
                        cancelReverse();
                        break;
                    case MENU_CONSULTAR:
                        listPayments();
                        break;
                    case MENU_RESOLVER_PEND:
                        solvePendencies();
                        break;
                    case MENU_DEFINIR_TEMA:
                        setTheme();
                        break;
                    case MENU_DEFINIR_APP:
                        setMainApp();
                        break;
                }
            }
        });
    }

    private ArrayList<String> getMenuOptions() {

        ArrayList<String> list = new ArrayList<>();
        list.add(getString(R.string.paymentEndToEnd));
        list.add(getString(R.string.openPaymentBtn));
        list.add(getString(R.string.confirmBtn));
        list.add(getString(R.string.cancelBtn));
        list.add(getString(R.string.reverseBtn));
        list.add(getString(R.string.cancelReverseBtn));
        list.add(getString(R.string.paymentsFilter_mainBtn));
        list.add(getString(R.string.solvePendenciesBtn));
        list.add(getString(R.string.setThemeBtn));
        list.add(getString(R.string.setMainApp));

        return list;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_options_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        this.setTitle("Adiquirente: ".concat(item.getTitle().toString().toUpperCase()));
        checkOrUncheck(item);

        switch (item.getItemId()) {
            case R.id.adiq:
                Helper.writePrefs(this, Helper.AQUIRER_CONFIG, AquirerEnum.ADIQ.getId(), Helper.PREF_CONFIG);
                return true;
            case R.id.cielo:
                Helper.writePrefs(this, Helper.AQUIRER_CONFIG, AquirerEnum.CIELO.getId(), Helper.PREF_CONFIG);
                return true;
            case R.id.stone:
                Helper.writePrefs(this, Helper.AQUIRER_CONFIG, AquirerEnum.STONE.getId(), Helper.PREF_CONFIG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkOrUncheck(MenuItem item) {
        if (item.isChecked()) {
            // If item already checked then unchecked it
            item.setChecked(false);

        } else {
            // If item is unchecked then checked it
            item.setChecked(true);

        }

    }

    public void openPaymentActivity(Boolean isPaymentEndToEnd, Intent data) {
        Intent intent = new Intent(this, PaymentActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(Helper.IS_PAYMENT_END_TO_PAYMENT, isPaymentEndToEnd);
        bundle.putSerializable(Helper.EXTRA_LIST_PAYMENT_TYPE, data.getSerializableExtra(Helper.EXTRA_LIST_PAYMENT_TYPE));
        intent.putExtras(bundle);

        startActivity(intent);
    }

    public void confirm() {
        Intent intent = new Intent(this, CommonPaymentListActivity.class);
        intent.putExtra(EXTRA_OPTION, EXTRA_OPTION_CONFIRM);
        startActivity(intent);
    }

    public void cancel() {
        Intent intent = new Intent(this, CommonPaymentListActivity.class);
        intent.putExtra(Helper.EXTRA_OPTION, Helper.EXTRA_OPTION_CANCEL);
        startActivity(intent);
    }

    public void reverse() {
        Intent intent = new Intent(this, CommonPaymentListActivity.class);
        intent.putExtra(Helper.EXTRA_OPTION, Helper.EXTRA_OPTION_REVERSE);
        startActivity(intent);

    }

    public void cancelReverse() {
        Helper.showAlertDialog(this, "Atenção",
                "A depender do comportamento de cada adquirente, é possível que não haja desfazimento para a transação de estorno para uma determinada adquirente",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(MainActivity.this, CommonPaymentListActivity.class);
                        intent.putExtra(Helper.EXTRA_OPTION, Helper.EXTRA_OPTION_CANCEL_REVERSE);
                        startActivity(intent);
                    }
                });

    }

    public void listPayments() {
        startActivity(new Intent(this, PaymentListActivity.class));
    }

    public void setTheme() {
        startActivity(new Intent(this, SetThemeActivity.class));
    }

    public void solvePendencies() {
        PaymentClient paymentClient = new PaymentClient();
        doBind(paymentClient);

    }

    private void doSolvePend(PaymentClient paymentClient) {
        try {

            List<Payment> paymentList;
            Boolean pendingsFound = false;

            PaymentProviderRequest request = new PaymentProviderRequest(CredentialsUtils.getMyAppInfo(this.getPackageManager(), this.getPackageName()), new Date());

            request.setColumns(new String[]{
                    PaymentContract.column.ID,
                    PaymentContract.column.PAYMENT_STATUS
            });

            paymentList = PaymentProviderApi.create(this).findAll(request);
            for (final Payment payment : paymentList) {
                if (payment.getPaymentStatus() == PaymentStatus.PENDING) {
                    pendingsFound = true;
                    PaymentDomain pd = new PaymentDomain(paymentClient, this);
                    pd.doConfirmPayment(payment.getPaymentId());
                }
            }
            if (pendingsFound) {
                Toast.makeText(getApplicationContext(), "Todas as pendêcias foram resolvidas!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Nenhuma pendência encontrada!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setMainApp() {
        startActivity(new Intent(this, SetMainAppActivity.class));
    }

    private void doBind(final PaymentClient paymentClient) {
        paymentClient.bind(this, new Client.OnConnectionCallback() {
            @Override
            public void onConnected() {

                doSolvePend(paymentClient);
            }

            @Override
            public void onDisconnected(boolean forced) {
                Helper.showAlert(MainActivity.this, "Desconectado");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            //chamando as opcoes de pagamento disparada no menu principal
            int optionMenu = data.getIntExtra(Helper.EXTRA_MAIN_MENU, -1);
            switch (optionMenu) {
                case MENU_PGTO_END_TO_END:
                    openPaymentActivity(true, data);
                    break;
                case MENU_PAGAMENTO:
                    openPaymentActivity(false, data);
                    break;
            }

        }


        super.onActivityResult(requestCode, resultCode, data);
    }
}
