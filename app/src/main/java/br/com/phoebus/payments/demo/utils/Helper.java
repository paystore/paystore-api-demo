package br.com.phoebus.payments.demo.utils;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;


import java.text.SimpleDateFormat;
import java.util.Locale;

public class Helper {

    public static final String CATEGORY = "DEMO_LOG";
    public static final String IS_PAYMENT_END_TO_PAYMENT = "isPaymentEndToEnd";
    public static final String EXTRA_OPTION = "extra.option";
    public static final String EXTRA_OPTION_CONFIRM = "extra.option.confirm";
    public static final String EXTRA_OPTION_REVERSE = "extra.option.reverse";
    public static final String EXTRA_OPTION_CANCEL = "extra.option.cancel";
    public static final String EXTRA_OPTION_CANCEL_REVERSE = "extra.option.cancel.reverse";

    public static final String APP_TRANSACTION_ID = "123456";

    public static final String EXTRA_VALUE = "extra.value";
    public static final String EXTRA_APP_PAYMENT_ID = "extra.appPaymentId";
    public static final String EXTRA_PAYMENT_ID = "extra.paymentId";
    public static final String EXTRA_REVERSE_PAYMENT_ID = "extra.reversePaymentId";

    public static final int RETURN_PAYMENT = 1;
    public static final int RETURN_REVERSE = 2;

    public static final String PREF_CONFIG = "PREF_CONFIG";
    public static final String AQUIRER_CONFIG = "aquirer.config";

    public static final String EXTRA_MAIN_MENU = "main.menu";
    public static final String EXTRA_LIST_PAYMENT_TYPE = "extra.list.paymenttype";


    private static String logInternal(Object classCurrent, String methodName,
                                      String msg) {

        String str =
                getDataFormatada(System.currentTimeMillis())
                        + " "
                        + "Class: " + classCurrent.getClass().getSimpleName()
                        + " "
                        + "Metodo: " + methodName + " " + "detalhe: "
                        + msg;

        return str;
    }

    public static void writeLogCat(Object classCurrent, String methodName,
                                   String msg) {
        Log.d(CATEGORY, logInternal(classCurrent, methodName, msg));
    }

    public static void writeLogCatE(Object classCurrent, String methodName,
                                    String msg) {
        Log.e(CATEGORY, logInternal(classCurrent, methodName, msg));
    }

    public static String readPrefsString(Context context, String chave,
                                         String nomePreferencia) {

        SharedPreferences pref = context.getSharedPreferences(nomePreferencia,
                0);

        // Recupera o valor do contador, salvo nas preferencias
        // O segundo argumento eh o valor default se nao encontrar
        String strRetorno = pref.getString(chave, "");
        return strRetorno;
    }

    public static Integer readPrefsInteger(Context context, String chave,
                                           String nomePreferencia) {

        SharedPreferences pref = context.getSharedPreferences(nomePreferencia,
                0);

        // Recupera o valor do contador, salvo nas preferencias
        // O segundo argumento eh o valor default se nao encontrar
        Integer intRetorno = pref.getInt(chave, 0);
        return intRetorno;
    }

    public static boolean readPrefsBoolean(Context context, String chave,
                                           String nomePreferencia) {

        SharedPreferences pref = context.getSharedPreferences(nomePreferencia,
                0);

        // Recupera o valor do contador, salvo nas preferencias
        // O segundo argumento eh o valor default se nao encontrar
        boolean result = pref.getBoolean(chave, false);
        return result;
    }

    public static Long readPrefsLong(Context context, String chave, String nomePreferencia) {

        SharedPreferences prefs = context.getSharedPreferences(nomePreferencia, 0);
        Long pref = prefs.getLong(chave, 0);
        return pref;
    }

    public static void writePrefs(Context context, String chave,
                                  String valor, String nomePreferencia) {

        SharedPreferences pref = context.getSharedPreferences(nomePreferencia,
                0);

        SharedPreferences.Editor editor = pref.edit();
        editor.putString(chave, valor);

        editor.commit();

    }

    public static void writePrefs(Context context, String chave,
                                  Integer valor, String nomePreferencia) {

        SharedPreferences pref = context.getSharedPreferences(nomePreferencia,
                0);

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(chave, valor);

        editor.commit();

    }

    public static void writePrefs(Context context, String chave,
                                  boolean valor, String nomePreferencia) {

        SharedPreferences pref = context.getSharedPreferences(nomePreferencia,
                0);

        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(chave, valor);

        editor.commit();

    }

    private static String getDataFormatada(long data) {

        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "dd/MM/yyyy kk:mm:ss", Locale.getDefault());
        String dataFormatada = dateFormat.format(data);

        return dataFormatada;

    }

    public static void showAlert(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showAlertDialog(final Context context, String title, String message, DialogInterface.OnClickListener listener) {
        AlertDialog alerta;
        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        //define o titulo
        builder.setTitle(title);
        //define a mensagem
        builder.setMessage(message);
        //define um botão como positivo
        builder.setPositiveButton("Ok", listener);
        //cria o AlertDialog
        alerta = builder.create();
        //Exibe
        alerta.show();

    }


}
