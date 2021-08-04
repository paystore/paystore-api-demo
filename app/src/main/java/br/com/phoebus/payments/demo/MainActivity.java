package br.com.phoebus.payments.demo;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jakewharton.threetenabp.AndroidThreeTen;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.phoebus.android.payments.api.ApplicationInfo;
import br.com.phoebus.android.payments.api.ErrorData;
import br.com.phoebus.android.payments.api.Payment;
import br.com.phoebus.android.payments.api.PaymentClient;
import br.com.phoebus.android.payments.api.PaymentStatus;
import br.com.phoebus.android.payments.api.QRCodePendenciesRequest;
import br.com.phoebus.android.payments.api.QRCodePendenciesResponse;
import br.com.phoebus.android.payments.api.QRCodePendencyRequest;
import br.com.phoebus.android.payments.api.client.Client;
import br.com.phoebus.android.payments.api.exception.ClientException;
import br.com.phoebus.android.payments.api.provider.PaymentContract;
import br.com.phoebus.android.payments.api.provider.PaymentProviderApi;
import br.com.phoebus.android.payments.api.provider.PaymentProviderRequest;
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
    private final int MENU_FECHAR_LOTE = 10;
    private final int MENU_REIMPRIMIR = 11;
    private final int MENU_DEVOLUCAO_NAO_REFERENCIADA = 12;
    private final int MENU_DEVOLUCAO_REFERENCIADA = 13;
    private final int MENU_RESOLVER_PEND_QRCODE_LISTA = 14;
    private final int MENU_RESOLVER_PEND_QRCODE = 15;

    private PaymentClient mPaymentClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //adquirente default.
        this.setTitle(getString(R.string.result_activity_acquirer) + ": ".concat(AquirerEnum.OTHER.getName().toUpperCase()));
        Helper.writePrefs(this, Helper.AQUIRER_CONFIG, AquirerEnum.OTHER.getId(), Helper.PREF_CONFIG);

        AndroidThreeTen.init(getApplication());

        this.mPaymentClient = new PaymentClient();
        doBind();
        ListView listaMenu = (ListView) findViewById(R.id.lvMenu);

        ArrayList<String> opcoes = getMenuOptions();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, opcoes);
        listaMenu.setAdapter(adapter);

        listaMenu.setOnItemClickListener((adapterView, view, position, id) -> {

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
                case MENU_FECHAR_LOTE:
                    closeBatch();
                    break;
                case MENU_REIMPRIMIR:
                    reprint();
                    break;
                case MENU_DEVOLUCAO_NAO_REFERENCIADA:
                    startActivity(new Intent(this, RefundActivity.class));
                    break;
                case MENU_DEVOLUCAO_REFERENCIADA:
                    startActivity(new Intent(this, ReverseWithFilterActivity.class));
                    break;
                case MENU_RESOLVER_PEND_QRCODE_LISTA:
                    solvePendenciesQRCode();
                    break;
                case MENU_RESOLVER_PEND_QRCODE:
                    startActivity(new Intent(this, SolvePendQRCodeActivity.class));
                    break;
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
        list.add(getString(R.string.closeBatch));
        list.add(getString(R.string.reprint_api));
        list.add(getString(R.string.doRefund));
        list.add(getString(R.string.doReversePayment));
        list.add(getString(R.string.solvePendenciesQRCodeList));
        list.add(getString(R.string.solvePendenciesQRCode));

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
        this.setTitle( getString(R.string.result_activity_acquirer) + ": ".concat(item.getTitle().toString().toUpperCase()));
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
            case R.id.prisma:
                Helper.writePrefs(this, Helper.AQUIRER_CONFIG, AquirerEnum.PRISMA.getId(), Helper.PREF_CONFIG);
                return true;
            case R.id.amex:
                Helper.writePrefs(this, Helper.AQUIRER_CONFIG, AquirerEnum.AMEX.getId(), Helper.PREF_CONFIG);
                return true;
            case R.id.other:
                Helper.writePrefs(this, Helper.AQUIRER_CONFIG, AquirerEnum.OTHER.getId(), Helper.PREF_CONFIG);
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
        Helper.showAlertDialog(this, getString(R.string.attention),
                getString(R.string.main_activity_alert_MessageAcquirerCancelReverse),
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
        doSolvePend(mPaymentClient);
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
                Toast.makeText(getApplicationContext(), getString(R.string.main_activity_AllPeddingsresolved), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.main_activity_PeddingsNotFound), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void solvePendenciesQRCode() {
        doSolvePendQRCode(mPaymentClient);
    }

    private void doSolvePendQRCode(PaymentClient paymentClient) {

        try {
            ApplicationInfo applicationInfo = CredentialsUtils.getMyAppInfo(this.getPackageManager(), this.getPackageName());
            QRCodePendenciesRequest request = new QRCodePendenciesRequest();
            request.setApplicationInfo(applicationInfo);
            paymentClient.bind(this, new Client.OnConnectionCallback() {
                @Override
                public void onConnected() {
                    try {
                        paymentClient.resolveQRCodePendencies(request, new PaymentClient.PaymentCallback<List<QRCodePendenciesResponse>>() {
                            @Override
                            public void onSuccess(List<QRCodePendenciesResponse> payment) {
                                if (payment.size() > 0) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.main_activity_AllPeddingsresolved), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), getString(R.string.main_activity_PeddingsNotFound), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onError(ErrorData errorData) {
                                Toast.makeText(getApplicationContext(), getString(R.string.main_activity_Pendings_fail), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (ClientException e) {
                        Toast.makeText(getApplicationContext(), getString(R.string.main_activity_Pendings_fail), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onDisconnected(boolean b) {
                    Helper.showAlert(MainActivity.this, "Desconectado");
                }
            });
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void configureReturnData(Payment data) {
        Intent intent = new Intent();
        intent.putExtra(Helper.EXTRA_QRCODE_PENDENCIES, data.getPaymentId());
        setResult(RESULT_OK, intent);
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
                Helper.showAlert(MainActivity.this, getString(R.string.disconnected));
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

    private void closeBatch() {
        startActivity(new Intent(this, CloseBatchActivity.class));
    }

    private void reprint() {
        startActivity(new Intent(this, ReprintActivity.class));
    }

    private void doBind() {
        this.mPaymentClient.bind(this, new Client.OnConnectionCallback() {
            @Override
            public void onConnected() {
                Helper.showSnackBar(MainActivity.this, getString(R.string.connected));
            }

            @Override
            public void onDisconnected(boolean forced) {
                Toast.makeText(MainActivity.this, getString(R.string.disconnected) + forced, Toast.LENGTH_LONG).show();
            }
        });
    }

}
