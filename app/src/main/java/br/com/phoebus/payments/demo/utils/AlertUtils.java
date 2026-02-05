package br.com.phoebus.payments.demo.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import br.com.phoebus.payments.demo.PrintStringBase64ReceiptActivity;

import com.google.android.material.snackbar.Snackbar;

public class AlertUtils {
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showSnackBar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

    public static void showDialog(Context context, String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title) // Set dialog title
                .setMessage(message) // Set dialog message
                .setCancelable(false) // Prevent dialog from being dismissed by back button
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void showInputDialog(Context context, PrintStringBase64ReceiptActivity.OnPositiveClickListener listener) {
        // Criar o campo EditText
        final EditText inputField = new EditText(context);
        inputField.setInputType(InputType.TYPE_CLASS_NUMBER);
        InputFilter[] filters = new InputFilter[]{
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        // Permite apenas números inteiros (0-9)
                        if (source != null && !source.toString().matches("[0-9]*")) {
                            return "";
                        }
                        return null; // Retorna null para permitir a entrada válida
                    }
                }
        };
        inputField.setFilters(filters);

        // Criar o diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Tamanho da String");
        builder.setMessage("Quantos caracteres deseja incluir:");

        // Adicionar o campo EditText ao diálogo
        builder.setView(inputField);

        // Configurar os botões
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Recuperar o texto inserido
                String userInput = inputField.getText().toString();
                // Exibir o texto em um Toast
                listener.onPositiveClick(userInput);
                Toast.makeText(context, "Você digitou: " + userInput, Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Fechar o diálogo
                dialog.cancel();
            }
        });

        // Mostrar o diálogo
        builder.create().show();
    }

}
