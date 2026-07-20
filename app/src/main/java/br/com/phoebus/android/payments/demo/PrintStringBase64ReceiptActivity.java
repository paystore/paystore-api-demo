package br.com.phoebus.android.payments.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.Executors;

import br.com.phoebus.android.payments.api.ErrorData;
import br.com.phoebus.android.payments.api.PaymentClient;
import br.com.phoebus.android.payments.api.client.Client;
import br.com.phoebus.android.payments.api.exception.ClientException;
import br.com.phoebus.android.payments.demo.utils.AlertUtils;
import br.com.phoebus.android.payments.demo.utils.ConvertBitmapToBase64;

public class PrintStringBase64ReceiptActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private boolean isTextualReceipt = true;
    private boolean printFeed = true;
    private PaymentClient mPaymentClient;

    private EditText receiptEdit;
    private TextView textToReceiptShape;
    private Switch switchView;
    private LinearLayout layout;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_string_base64_receipt);

        textToReceiptShape = (TextView) findViewById(R.id.text_to_receipt_shape);
        receiptEdit = (EditText) findViewById(R.id.receipt_content);
        layout = (LinearLayout) findViewById(R.id.image_options);
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);

        switchView = (Switch) findViewById(R.id.type);
        switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    setToImagePrinter();
                    receiptEdit.setText("");
                }else{
                    setToTextualPrinter();
                    receiptEdit.setText("");
                }
            }
        });

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch switchViewFeed = findViewById(R.id.feed);
        switchViewFeed.setChecked(true);
        switchViewFeed.setOnCheckedChangeListener((compoundButton, b) -> {
            printFeed = b;
            switchViewFeed.setChecked(b);
        });

        doBind();
    }

    public void print(View view) {

        Log.d("UI_THREAD", "is UI thread? " + (Looper.myLooper() == Looper.getMainLooper()));

        String content = receiptEdit.getText().toString();

        if(content.isEmpty()) {
            startTest();
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {

            try {
                if (isTextualReceipt) {
                    getPaymentProviderApi().printFromString(
                            getApplicationContext(),
                            content,
                            printFeed,
                            new PaymentClient.PaymentCallback() {
                                @Override
                                public void onSuccess(Object o) {
                                    runOnUiThread(() -> showToast("Impressão Realizada com sucesso."));
                                }

                                @Override
                                public void onError(ErrorData errorData) {
                                    runOnUiThread(() -> {
                                        if (errorData != null && errorData.getResponseMessage() != null) {
                                            Log.e("ErrorReceiver", errorData.getResponseMessage());
                                            showToast(errorData.getResponseMessage());
                                        }
                                    });
                                }
                            }
                    );

                } else {
                    getPaymentProviderApi().printFromBase64(
                            getApplicationContext(),
                            content,
                            printFeed,
                            new PaymentClient.PaymentCallback() {
                                @Override
                                public void onSuccess(Object o) {
                                    runOnUiThread(() -> showToast("Impressão Realizada com sucesso."));
                                }

                                @Override
                                public void onError(ErrorData errorData) {
                                    runOnUiThread(() -> {
                                        if (errorData != null && errorData.getResponseMessage() != null) {
                                            Log.e("ErrorReceiver", errorData.getResponseMessage());
                                            showToast(errorData.getResponseMessage());
                                        }
                                    });
                                }
                            }
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> showToast(e.getMessage()));
            }
        });
    }


    private void startTest(){
        AlertUtils.showInputDialog(this, new OnPositiveClickListener() {
            @Override
            public void onPositiveClick(String input) {
                try{
                    if(input != null && !input.isEmpty()){
                        StringBuilder result = new StringBuilder();
                        for (int i = 0; i < Integer.parseInt(input); i++) {
                            result.append(".");
                        }
                        String test = result.toString();
                        getPaymentProviderApi().printFromString(getApplicationContext(), test, printFeed, new PaymentClient.PaymentCallback() {
                            @Override
                            public void onSuccess(Object o) {
                                showToast("Impressão Realizada com sucesso.");
                            }

                            @Override
                            public void onError(ErrorData errorData) {
                                showToast("Impressão contém o erro: "+errorData.getResponseMessage());
                            }
                        });
                    }
                }catch (OutOfMemoryError e){
                    e.printStackTrace();
                    showToast("Ocorreu o erro: "+e.getMessage());
                }catch (NumberFormatException e){
                    e.printStackTrace();
                    showToast("Digite um valor válido");
                }catch (ClientException e){
                    e.printStackTrace();
                    showToast(e.getMessage());
                }catch (Exception e){
                    e.printStackTrace();
                    showToast(e.getMessage());
                }

            }
        });
    }

    private void showToast(String message) {
        AlertUtils.showToast(getApplicationContext(), message);
    }

    private void doBind() {
        getPaymentProviderApi().bind(this, new Client.OnConnectionCallback() {
            @Override
            public void onConnected() {
                showToast(getString(R.string.connected));
            }

            @Override
            public void onDisconnected(boolean forced) {
                showToast(getString(R.string.disconnected) + forced);
            }
        });
    }

    public String generateStringReceipt(){
        String[] brands = {"Visa Electron", "Maestro", "Mastercard", "AMEX", "Hipercard", "Elo"};
        String[] products = {"Crédito", "Débito"};
        Random random = new Random();

        // Gerar valores aleatórios
        String brand = brands[random.nextInt(brands.length)];
        String product = products[random.nextInt(products.length)];
        String value = "R$ " + new DecimalFormat("0.00").format(random.nextDouble() * 1000); // Até 1000 reais
        String ec = String.valueOf(randomLong(100000000000000L, 999999999999999L)); // 15 dígitos
        String cnpj = String.format("%02d.%03d.%03d/%04d-%02d",
                random.nextInt(100), random.nextInt(1000), random.nextInt(1000),
                random.nextInt(10000), random.nextInt(100));
        String auto = String.format("%06d", random.nextInt(1000000)); // 6 dígitos
        String cv = String.format("%012d", randomLong(0L, 999999999999L)); // 12 dígitos
        String ac = String.format("%016d", randomLong(0L, 9999999999999999L)); // 16 dígitos
        String aid = String.format("%014d", randomLong(0L, 99999999999999L)); // 14 dígitos
        String term = String.format("%08d", random.nextInt(100000000)); // 8 dígitos

        // Obter data e hora atual
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String formattedDate = dateFormat.format(calendar.getTime());
        String formattedTime = timeFormat.format(calendar.getTime());

        // Montar o comprovante
        String receipt = "Comprovante Ilustrativo        \n" +
                "Banco De Simulacao A.C.\n" +
                brand + " - Via Cliente        \n" +
                "\n" +
                "Simulador App Demo\n" +
                "Rua da Verdade - Confia\n" +
                "CNPJ: " + cnpj + "     0 0000 0000\n" +
                "EC: " + ec + " \n" +
                "\n" +
                brand + "\n" +
                "VENDA " + product.toUpperCase() + "    \n" +
                "************1234   \n" +
                formattedDate + "                       " + formattedTime + "\n" +
                "VALOR APROVADO:        " + value + "\n" +
                "\n" +
                "CV:" + cv + "            AUTO:" + auto + "\n" +
                "AC:" + ac + "\n" +
                "AID:" + aid + "\n" +
                "TERM:" + term;

        return receipt;
    }

    public void openImagePicker(View view) {
        receiptEdit.setText("");
        if(isTextualReceipt){
            setToImagePrinter();
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*"); // Only images
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void setToImagePrinter(){
        switchView.setChecked(true);
        textToReceiptShape.setText("Imprimir Recibo Gráfico (Base64)");
        isTextualReceipt = false;
        layout.setVisibility(View.VISIBLE);
        radioGroup.check(R.id.three_channel);
    }

    private void setToTextualPrinter(){
        switchView.setChecked(false);
        textToReceiptShape.setText("Imprimir Recibo Textual (String)");
        isTextualReceipt = true;
        layout.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                int channels = 4;

                int itemId = radioGroup.getCheckedRadioButtonId();

                if (itemId == R.id.three_channel) {
                    channels = 3;
                } else if (itemId == R.id.four_channel) {
                    channels = 4;
                } else {
                    channels = 0;
                }

                String imageBase64 = ConvertBitmapToBase64.toCustomChannelImageBase64(bitmap, channels);
                receiptEdit.setText(imageBase64);
                showImageDialog(this, imageBase64);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void clear(View view){
        receiptEdit.setText("");
    }

    public String generateBase64(){
        return "UklGRnJfAABXRUJQVlA4IGZfAADw/gCdASqAAYcBPlEkj0UjoiEUGfWoOAUEtLd+M/yb9ahm/oT/\n" +
                "KP4n+qHvn+Jfk381/uv6t/2j/qf4D/+fD/4Z8V/Of7F+sH93/7X+S//fy7fwHfv8+/bf8v+bHuT/\n" +
                "Dfpr9F/s/+Q/wH9g/bz///KP9P/HD8jfZX4gfr/5Wf279yPsC/Ff4h/R/65+wf9f/Zz3Ff3HtTNI\n" +
                "/1H+Q/vn7VfAF6j/J/7//dv8x/o/7n+4vsLfsX5J/3L/wfJv5h/Nf7n+T39a///4Afw/+P/2P+tf\n" +
                "s1/Yv/99Ff13/H+LD84/vH+p/zH44/YF/Hf59/mv7z/lP+v/gv/////xE/Zf9N/fP9H/0/8P///+\n" +
                "98O/yj+x/67/Df53/tf5T///9T9BP5D/PP8r/c/8r/0/8H////f90H/99t37Xf/L3P/2P/9v+6/4\n" +
                "f/1OR4rUpmSTVqT5RdsAUiCvPXPVXyErnqosYDUOx/dfb7bjuPpe7121qQQRBbKvTZ838dwuF8/K\n" +
                "W2UHPwETMlzsq43yCBIz8E1xRWpcA4Lf5Cyy3vs7lbVZx5PkJXPVXipTVEMEmANqRXz0a7pRx9fu\n" +
                "dyRkHpgEL/fP7R/RkmLoIh+38pbTUahnJ8hK56q+Qlc9VfISueqvkJXPVO+bYharH/5jnNjBbaTA\n" +
                "OpSmSOh72FhTUUIRx2P+vSFjNPrt3aISLWMtNhrd1mULx7kFre1gCYMbwUV9LTRzQmFfRVysuptL\n" +
                "p6Qqo52qWc8gYlZiJM95GireAK1NdZ/fQMiG2uxyZRTxOv2n6NMlTKaBlTl/IOT1SS6hcaLc3gus\n" +
                "obG68JEletWxnPjC53Fi9dj0Izh3aw2/7LsSRcUYKzPnwKmdYhs1VGRY7cUn+2hn0o5XplUIs7h/\n" +
                "A8jJ7bR5MSzm5GawGt8InXgap16Ltod539UThhukRrqMrybZRh9ZhE/dqbXJJ0pn1n99HFEWPwCB\n" +
                "PPGqRDPAG5Rajj6FW8kVfq/9MfQ0X04FkpP4kfaK9NMyNHL72SKiwo31Od8X1/zmIALFqjGYl2BT\n" +
                "q1yu2gNEKZpgM2e5QHJ2GpUAL/ilDp9rlwbb/GJtJTLRQK/Lhhz9+pWl+VifB+t9UcWkmBgXDXd6\n" +
                "OwgEj9QQjS0RywNl9NUv2cRbcDc/t7Hf5g9r+gIvT9aZ4/kR0OjUKGOMWEd5wktQbzi93ZFzAIAu\n" +
                "m7/PkSbWQPA/9j5gijydb2i5V8JN7q1bImGZ4WvaApNZfnyQTHEiSjxOne5LpPKBFndASxC6yGvF\n" +
                "xS+YgYkvsb2WHbR8wak7FMfgwDDBI2Z/qr4ufu4VgviRG9ZajA/LY1d2gzO/4D2vuKHWIVT4dKce\n" +
                "hpIUM7e+vflnrVOZuymKpgb/as/TKeIl4b5ND4O9H2hCGEvzav62nuGWksVx/nhumjj0tNoAd7yf\n" +
                "xCpyLoJW0whZnoMHpQ5aMjYMOGdEe1DbJbLwErnqr5CVzlPS0+uYxRyVah7X2FZfBQ7Dx15sadej\n" +
                "wpvoBHMiCqgWEKeK6kbuuF2XV7bU/Xg7OP6v9KPF/fqr5BhMHAIFH+erZGyxl8XghmWq1DRoAQAH\n" +
                "QvQrk4y72ln8M9cKGkiyydN2MM48dV9jRAKb2LUxbUnqOu3+Wm22jHlJdf4reiK7gt1NKXpP1lca\n" +
                "ui2zVfk0ZJjAlriDG4PGoiNJIdo+2ABX1JIhYzjote3cBtJV1nEsWESIS74D2uDRtWTWbRN24SSZ\n" +
                "5bLa8S2UF0qFQ2eSzlec/mpd+vAE+Qlc9VfISueqvkJK2wvbfZnSRRFo9jVEfK7hnHk+Qlc9VfFJ\n" +
                "2gSxksltTzilMmuBQ4nHaJ5Zs/xOnoZTqVLOCBuwd2gBhEx+51VNBjfJymjNUkagrXHXYWLD2mf7\n" +
                "nL/4JoSrd5FjpiFu/9BAHIABTjR4szjyfIMY+lRAnfDnhn9KQV1wF/iZXKCgYzCPUA1xEg3aljgH\n" +
                "Wp53zcPXpl6iBVOwMnVhD2ekAFTe1OZ42X0oz0o8XzW1+KMtTUjKJzwylIbwmvfEx0Gn4qKHQ9fh\n" +
                "xFwLqZ+kyCHKlnlC5DYGxxFyGWGMiEz8VjGNopECUqvYIqiEOmGQOVGBnooBLWGSZhD8R3+BAiI9\n" +
                "XZALmgcsPgAHHIXtzOWzVogieiFO/fKBel+RnZLBxJOABseawW2BHQUmo62nZ0bBh/WBIL4zHwgc\n" +
                "nZoEF1gEB5VNb4OT5uMk7tKY62vmICSEN+p+G10PAol00SatpQXMeO7DM8z+LNd45/21+Zy06/3B\n" +
                "hfiSqGL9hm1lZnLPMAXUEZ2N2HnDzAY6oO99R3hAVqUzJJq1KZgfjBxMyRdPbgFnWpSZyjWSKcPX\n" +
                "3NooSuAslQgdCG1C09dXLh6If+a1v9XMjNIR0Z4sSfd9VgRMUdUs4VqUzJJq1Jzc16TWE8vUwnl4\n" +
                "N5QQofNgvboZ7sj8DPYvkJ4VnZ8D5GbXRkwIiu7Kdia4ceT5CVz1TSrDryT78ooMjyJxPXCmT7Qf\n" +
                "d2U39hrWui8PLdAtlRFwuCzt+NQ745SJlsrodQq2f65vwetWuvBF8wsfX2vDmotk5PkJXPVXxgIH\n" +
                "hf64JrOP47LqBKFaLbYBeZu0PjiUdBxkcEQvhhzskA7U9X96GVdP6WFDKBO/6fhRJ1q7SGtEUyqn\n" +
                "3NwxTh7HH+yFdEaqaG4k+XC58to6656q8H+sRzgSRCgiB30yulcpaBp4C+hpwfE0pLbDeZF9uOBf\n" +
                "1BggkYvZi8GA79VfIStQAAD+/7gQiAz/CLreCE2Ajgenn/ydkJpkT9s1TZO0jue1C2Mp26/wX/Q6\n" +
                "7gcPThCuOAtMw1n3Tg2yyeqUd5L21wZryC2qcpWlUTF+RIRE6YTwe2Tqh6FEeTEF5evGf1Gf58PY\n" +
                "TWXhhBebLWDSuIOHo1sGRuoaGIuAjy43R65npFz/iu/bfNshpWWR0Z1GdfZmTAH7Y58mzkcoYnSm\n" +
                "7qZp2BtelN0Aqcd9iVxq2rh575uxIDGRp5rlk1x7c05dWaC3j97N8qhbC9sKMAG3Ahn75AUW51ye\n" +
                "pF1koePpNLGYgQXu5Z1vX5uCGOJRqe8hvqAQ7qh6K6xpZKqETZVeCzOUImMgtV4K5iShiMb3Vb4I\n" +
                "5UOEhRQ4gsjWmhhvkdSo0cNglr7B+CDpETAw/vV+awc2nVROMEVhpso+N+20Pi/hTUm/WAJGMEbv\n" +
                "xHsNCSOmXZ8wsHMU/OZBqQJo8smZ7cB0lrglpDDvaB9jOz4t/lUEykU+xWhsjqDl7x/FD1hdlyVT\n" +
                "CtTWuIA3+5bG+HsbqoXy+TOcbmKT5VqH38Cu1VeD6Cg4VHuri15d7h0RqdQ5K8Vg1tLyKpIR8pns\n" +
                "DpF+d1lXVwjtiHgLFD0oDWG3ai5hmLvmrXwpkf4MqWYCFjVUF7biCRvM07NvoXk8lCd/LgjfQNII\n" +
                "dNSI6YPiKvTFAywn0d7Y/4Ht9cGY3hb1J8agjVWPfkDXz7m71zodxqLJO1LK0lO4I9tKbwjN/wV9\n" +
                "Udc1rGxj+Dj3Cvy3oEK1gOp8q5HkbIovE4noEIYbuZOAmz9UxrUqb7qJXEuLtPdT4PM5TBPFOMCl\n" +
                "AHdQtdspFkU513jyyRWVZMPJczOd/vtV9kOKCZ/bdihvWotfJXt3tdd91E3Or9Yi5yjkKHrAJIX5\n" +
                "iKiw7ThNTQnhCptgJ0DXlG1pYv9WexCYzamcDGSDJCxOaRiO+TBPDw+jFPpBTJ43XaiNaxlD4mFe\n" +
                "sMVe4L1SSFX6SsFAZPOl763AlBmdynAqy3fOwK6PtBSOWO9B3ALhDVWI26yuvZ/F4lUb7Ed+ZO01\n" +
                "j9fI3lo6EZ/Rf1IcbGwD5rrrBIAq35jvWWOoYqEJcoifKW5AGVkdX3Ya1I9dbH9ypfX+FX78dupO\n" +
                "KvFj38XEGooYOLyBJN6sP4OLsR9NPGnz/rMWaP/PNbsAw4zAJUxEqexYmYrp8jAv21SlkidQZcd1\n" +
                "ruqC6uUzXyilNWWQcjJqoILSvQw9cjq8nSLNGEmtNTN6YMk4ccOCtL41BBi4XJR4zTjzcdz888/3\n" +
                "YrM65o1Nc0HMiTrQMy/umVJ+5EmV1fiStHjJlkgaK4huJ/g9hWzcoAVgPQYo5dGYBsLSKSiUZZZg\n" +
                "0Bc4W0VxelAh+3gLwWjm+fx6Jm+isSdvJtN2ycmh5c8kuQes2EiQiWwBuuuocbRC5pBvfqD8poEg\n" +
                "vZPjOl4IhoXnfqS8R9KvoHudJx0cTqdqHYOM+T1upzT8z2iUvEIZo4Kul3gSDa4/Xw+2YnraLx48\n" +
                "uH7cRG0UZRHS+8XaL+6o3+Fl4IkH+zMCJErar8iOlTjKUeAY5Lq/gRcc61PAYEXw0gDEtw71h5dG\n" +
                "bVyn7PU2zuhrn+CNAInBN3dcpE8qMA2h5InfpHgTJAD76/8kaHCNfWWlds0xwjq9a57oMm1rs6N2\n" +
                "I313oqyM9Yoki6PgPPnDs2IBkjZ40bv1Gq4LoNTpdatWPig8WQrD4kMn+M7LMl7IWFbnHvM0jJln\n" +
                "Wfx6uuzmuX5rJq/H8MGsPGg04lFquz2OrGHDihMN5QfusMHWEH7mKE4bmXLgVQDiNyf2RB1UKmpe\n" +
                "dXorTZsHJFFE3MCgL3ENiu9k44MbOYEIlTT0kH5sTHwVhs0Db0f5xDZrYpGqWNYnQBSYfynOM03x\n" +
                "FvLp9GP5esnzUYWm4sNZuQ0fRQGiIux8m1FDeN2BUqSnkP9Z7+POGhDuX7AbZmrEli2IaPOz1r61\n" +
                "zAWt2qfe44Dd/7VDmzIxFOQzQ/QpM62dTx43PL+veyviFRFSWuTjhvGuMME4e/gPQEYjT0RMOEXB\n" +
                "vZzM4IqRDJG92cyCeOb1XAL0lC1VXSzIkjNzMA8pDH2+UVzBL7Tmj+QtGVMIcC0QbsUqjJvEPbrj\n" +
                "GSk/RgAY/NJjIZe8nVKz1brCT5t2yWKqVFnbdQRBZqS4JEqP849KuSjijC2ubJg6Z2O1P4GI5I7V\n" +
                "p6xQlg/Qxkz7CxahmoN1nkrzlT2Me4wR0FotMsQLIiB+6g0k97inYDf4YkCBQPwTgxGyXIvoII4C\n" +
                "US7vSDWToC5+n4XuQlXIw63Ys68ZqNdDKOseemrxs7Jydk/6Tmy5ttr1UHA6EBgY6UkRMYe0J/fE\n" +
                "AZtEGEnjnpX1S198A6i9JZ/6U4yRXWnOd3Gd6V5Eqc9wYBZ1h4YDcq0wLQ/XprwrdmWLowK/DuKd\n" +
                "hFLzGKgHgB+UHD+c60bSFpxQ72evpw2+nlFW+OiUc1tKGhrgNqIWc9LkxjW8BFgetvyeOSkB8KLU\n" +
                "IEzPQ7tgFiFc1lkNziLWogaYVhpWzO5PSmy2EtRvA3jdcHj/9VIGFrS80iFcaXxJSCWzHo5AaUdh\n" +
                "5Vp/6Wue9D9Pyqz4HXeOsLeReg1u0GE/4GTWfkBax/OLUXQrsHoEK+To/QS++S8l4H1KYBzUKFyQ\n" +
                "JvFNZutTzBN7o8OU/s3CKfWGzixJltz/NVBzwdPuS9HxsYLjmedkuQJ5X+ftIIVMol9qJbMEOY5L\n" +
                "OIaxXOOHXwvlgWL+Cny7JxwiQ3iYOyMsKRGzEthZYccd/JPAelBEZbppY81kbXpZU7YM8rXhxdpM\n" +
                "r48t5KrIiCJhQ78YREFMmSZO0FAA8qq1GGRCDFqHaCLFmDdJmfHmB1bFEpgmviXoZa+wfMkrEbMG\n" +
                "FcYoZMiRRFgAbizXLpof772y4EK7QGD8jmzgmDK1zJDBDdQKGVguExxXq1J7m3QfnjvxjyVG6h6C\n" +
                "7KcmSqnL14RI8KCgTmQUf/HUrUCbQlSCe7P/BhVtWpHl+IeKQS0/w7wXhRdYdu3OpizaO50ohoqi\n" +
                "8qaYhKGY+cyT57L60EsP7Io+YXNmNQ3aJ4Jw4xGokCUhquLsCOvjlLKnYFuSt8UaNRpDII0yLens\n" +
                "lOE2YbRljOdLqeqGpNCXbYE5HPeej/0jTmDnI8qnLLLVPTt7BWcj2KHuQMPkJY4WoLYLCHHmbVet\n" +
                "oDKk8o/KLduemci9tPGapCdocyHhgWZEwtv6WYqK6FpSCI8YW1IrsnxT3P1Hup5uvJioTsrEAOLZ\n" +
                "U8acULUFYOn21c/OnB6kq/Lr2NLX9R0cMtYf01efAGRspcx5P7Bo4++SZk/IM9V519k9MY6HgTFh\n" +
                "s5quzhgR9V04zzTOxDzs0lsiPzpP34pKvTfnape6YxxEERXVy2MMe5NbP3Ny62CvCIBVphJIrI2O\n" +
                "ZvWJno1lyQlsSbzyX1he4bPPeMMuaVGNUCK1iZXUgOA/Ye3Bf149ECAS0cmH6DPKTc/Y2ndsDYF1\n" +
                "ZZZYYTBR2xiMxn0DMJGhjx2GiJyWCj0aeFkksRJMq3y+y6gTZT6JL5BovSm1gthyH9eYD0DDzG8V\n" +
                "vvk7esx+jzkxxgqPzw8vnxin0vPmLP3iWqpZ/bRed4G+sHrbX1dMwpR57uS2e+nv01xiraWEl+99\n" +
                "S1U5b09ApjchesedTmmQl+TOcz61meUvPQjq1iVj727aZ9SrV+yhc+x58ZUVAQbcCSecS0+0eylH\n" +
                "pa9uQ5Xac6LKXcLFqbCJi0xzuf6WOQBD70fdgei0k6JPFDUV2HWCs1+oxfHPRFfE9qsFVukAv+8A\n" +
                "E2C7S3Izathqx4CqVzmUm7NjqbN4UQeZ0CBtFuWPBPMu8447OEH+PU4y6yPZ+CsxUpBbs2dbQyop\n" +
                "NRwEdLDpU5gdKs+KTQZaPRz3oJHWAkFOhuvaWodg6G+fyG8iQif7i0r1ypMe6eEEFYG3Nod1BSYr\n" +
                "uf8dNQkRLCvY+aet6j08UjmClCR/XroJbItcLDMpMDWG71ZGr3gDFJoi13kNV9Ed4BLDiMM6PsAX\n" +
                "nr4fI11uMZwTB001S2ysgzhUjgYJSCxJh73ZKOJbXe+ZY3qRwojiTdkiu0e51vEpaEu7MWw1ZeHT\n" +
                "IXDlAYjoWiB9kj8gce9giAWDytggGYFlB9RsoIAcaJSbfKQMz66+sqHDfM4bFzD+QOn+JYEtuK6T\n" +
                "vhb+Pyeq7HXpEosdM1WYs6M9AN3vtPU8ZRHEZzsa0MkolxxWp0DFXAyRrGciG+3HpgZCg0+qWFrX\n" +
                "XYu0DauzisAEAxezyX5OYO+qxnIibvZPDvR+l8nneHrrYSWvojhelCcJcKPNeV3Ei4K1UqXoPtik\n" +
                "RwAwk1R5SRytsn1vu8WjkDlh2DjWKFPJ0YnRN6SehGwBavz9RqFDeMCZIb3ZmCPaXxDqNrVDQFrf\n" +
                "lTAisYIOJ9o6nfEXmHkHGoyfICImDqStfI6Rc5IoBGfBN7UXwaNOSpCYDTKZxvVv5T4tE8P7RrC4\n" +
                "Wl2bKV5P4FHILjGHKB4xnYW7gli8R9hQLnDreuaOc4yF0o8FP9QcZGi11e4h61ofgQ08nI0D0t0y\n" +
                "BOLONSuFQXtzF7p+rUS7N6UOv3oq1ZI1KaJ70ECd4VjXvvmWZK+kLOPEvWS7N6UOv3oqvAYDZov6\n" +
                "jBmtfgM5aQ5vBn27WMhNCzsHRb20FLO2hnXhj2vflMgspk2zfsazLab2ZU5vNEHJ3W3MP+wIz7nf\n" +
                "RX0VImB6Lk1iJ1Xx1WSG1YfGFgQ9H2ynfmuJPm7W4re1pPKg7zVrjoXyBgcsfIyUxBJCosXQ9yKG\n" +
                "BrGvsJP2HV7Oreyc3nZZfded8bA6Ak8TBIUVFCqF8fb7UskAghe48loS99CVDouHfwaavTXxBwS+\n" +
                "FcIJeKlrp1dk5sfWsLGVNXdjWD8XwKr07ipj1PmQrybCXhHvlMyVM65RHO4oacMjq4V/vofaobWn\n" +
                "6lFuT14Xu8OsxvCF/36er7x+dImHKUN+Nw2rfXrKH9eWeOUcru/BCnsXBOuDSWSFIdr1lejck3E1\n" +
                "lP4ORga4cVVmVaGgO5dHpW3gh7AlRnCfPtY/1tQ/+YnU5TGOAi599a7w0aBunac8gZRfy1uUng/G\n" +
                "o4lL4qAe4hM5y/cFCodxQD8nBj5iNy/8uk+ybxTrrthTEA17zGDUKVckKEdU03A8IzI7bKyhnCCO\n" +
                "qwvfvAa/QG1BPmljdx/eeD7KrD75F4emleUlPuZRBC1kdsg4PfpKhp272mTCIkP9NTdSXzBimZK8\n" +
                "zg6+6sWwDVc4UGRJsZqT3dUiWBbMCFtugefgjyKk7TCPOTUGfeWeZq29B84cOG4Y9WJqOsj/FUsr\n" +
                "E5Kw1zRnQLXCrS+V5Nt2gNVgTlweZyl3fjnyge0/CL3TqgS0wmVS/Ig5CnaZfPX++ZZkr6Qtq/Dw\n" +
                "n0xka6iRwWESyLPC/fKT28MKb9KE+3zcWAH5eQY01Szssmm4q476kan1uF7VLB9cLr1u1jgdXAav\n" +
                "VJ4IBKjumHPJ3xTWs5UawKq307ZEWgUHVBrzs5D/DcA5b1th6F35TDY6tWHNuCjuUVLmN7pafRrZ\n" +
                "c0DolIG6kiGIAAA8Dy/Wbf2g7WSc8FtUEDcow8w1EDeflZNtZMrAebhuH8CEwcoVSQgGdFzaOYd4\n" +
                "Hbfw7i5aa1JekL94hU48aa738W8mabdVv5iITcxo1LEXTYA8aNmqL5pK4QLur05LaxzwKQE8XM4l\n" +
                "2TXDHQSuDNVfYZiD3D/skVwHPaD3qWRSoGTeeqK3g0dx3kamowP/lWLM6XN+H7Jc+75rRrvaCtFP\n" +
                "8qxuzl1fvG+Iz3fAAmI8Xf5LS8FIc8Yzg2Ab8UQH+QQid6HA60pymQZG8ePmalHcaOlM8xb50QXR\n" +
                "RnSL12HF4kJjs8P1qBNgrQeF7xLkoe7ee1+eDCQagJoZY4PkD2Syej15HxyNoZVOYWnFRVaev5At\n" +
                "Inmy3LvfJMlpZtA3GsgwxQlWpxcQneGZq8jYvRvNpgQGlRw3TPCZ0BlJv5mNWsHl/te+aRREES7f\n" +
                "ltM4m+gtgfZ6m1aqqUystiL9KbxtMF/kGLAZSsVi/fWi1gUPhYT5MaxPt7mldBvReJzx16vRPv3m\n" +
                "yfk+CAjz26HhL41wcmNSxjK2kc2C88/GNUFPwjClY/rW/bmhJNAYmENfEbDbuacvQ3ulO8rqnRCm\n" +
                "UOCQU9drFEwb92F9QLpQw/WlqTKPV1/REKcyB1RcVH5ws4K+GMa+RYZYJTB4VWN4PunZdGdVL5Gx\n" +
                "XqWIlYoahI3PIaaMpyfPzcCnvu3ATJXtVhgfg2JWlhpha6iCEOnE/x3umCpd3JGukLtCi+jNI2fp\n" +
                "M/Ho2cSh2/ya53LMETg97aS25f/EXFNYMjqhH9FLcQ0wpV9GIyJMSjSEjbyr1yqA7eZ8ft+Np1lo\n" +
                "/ePDQZI0RN3LfdwGnIOZL+NlwBOdw1NTBkgzyKUS5vBes5CU1qU9W17IPW/ltZXXBMbi/VCRFnG2\n" +
                "buLatgVpB+KsySDBjpq/+Jh3mfPQ2rDtAgJNJUZ6pbQJOTqq2rF1++E3Bv1BUasZvcYys8OwQv+C\n" +
                "wr9lya+hD65PlX5bKj3gU4Knjs46yHY7UmyNYoAee00InfuV2e4IawP/mQVuTh07gieSgmA/rAxR\n" +
                "lM/BFTjrdzYqwD+g126IFDNI93XHjI/QFxFjhOuRSzVzp+Oc71uSX242WyJQcTRfKuRKrg+NYVzL\n" +
                "OSj/ueLrwCAy1MF8Uz5paJ14xn8BxqQ2dsTQpD5A7aZx/mQVUIuT+Kielpr4b5LOO8mjE+1NCVMk\n" +
                "L5kTNigsVApqJiKBVkOK8KJzFy0r8khkEMCgdjt6PeZmfsgmoKanJ1enLdoFdW/aYKHnjwkmrEI8\n" +
                "lGJwmD9/cTHUkBdWjx/AjHBPKQ5uP/AsLOYQH140NVZdqEBkx731ME1WDklIOPxYWHrIVQOgKbqi\n" +
                "xb75miui64ihegniYXDuVWLStCOKwxa+Mi+mGRlwL6aUiGm8LEa5JMXk+01K+qGH5A7MoaBetVSQ\n" +
                "r5/WRpWHKn+G4a9lvsyS82vwgsf9wq0A83V0tn9YMNWa3O3eusqj7bhUuxauAe0KwtiC/pE0tP+I\n" +
                "6jN7T9WFg2Z2UZTH5QGEUUvPaftNy03eYbPppvxgszxNeRjuedS8/Jg9rLkBYW5IOJ4cd4Tq/kOi\n" +
                "VjqaGNtEdswqcfh+wNjWCPGjZzhWYe/9NLXguvEUsXBEMAA/W1FW+8UFHxCMH5QQnaqQsb39EEeZ\n" +
                "2BaIdC0v8vLmylv0KqzP7q/ccukff3NGpAFD4jIWWlnVYmzTfH71Q26X/+QDxDVqIq2OlonMnxA9\n" +
                "zwAqqHcJ6TwCezzpBfWcquuwXtf3qxZ7yan77tkR4+3+yuiSLt0SupTFyymTUr5SFvLTfKfOKuoN\n" +
                "WLJyDAdj8cjiatGyVs0g17YM+k1X9pQL0xB2hYZTy6NkHw6JtEp3mXySRtw/z6IKjEEQ54+xkrYL\n" +
                "3EHp9DptbLD3rA4gFSGNUFPwmTatZH38bKQmtbCYaRf5uKKhOE7h9UCZZLG8aJM/K3fvoNTWH1MB\n" +
                "IX7b+U+Z8PXLtVDOvTR979KVYge75UPZUKxXQJWTWHZeTvK1BPhI1ukNgPfpDLoiKqsjd9arRUEG\n" +
                "3+xLfwOBrJ81sAARW8CskryEEIZuYFcdctb9ADC5oGpDl24tYTHoPPUt8pFomrVoKnhvBntWJV+4\n" +
                "t1DFZk05f3q/j2kdvCtlEXFgYX8Abfzwcb5bijVZ3WghpweQZtUAc93S67Bzb2VrDF70AodBpPYu\n" +
                "XqQcJAzBoRNXSCAe9QBYiPthQ94coXfGhsOeM4s53cmHihoMbJrED7RmU6QIUUHsgjz6NKnUYmjA\n" +
                "effTA7+n6JoOOGbGdo96PkVynWLhjcL4P9Z15IT9NjDNhjrhcYbOioLwR2v/0lC+lBQ8KKq4kwWN\n" +
                "A7mND/Imh7Z24eXbq7WwFLY0sps4Svd4VOFCrtp5X/vHf6UANXkeFhJ5bi6/amtf0GcsML1reeHw\n" +
                "pheOwCrKfqppWNJI/XqusZMjBknMUOKJD2I2275fKYSLLH946+QDSuWvMLJJ13I08KDiPH8r9iWo\n" +
                "dJZY4UtM9v2mP/BcyN9afH8LXzxoFBMGaD0IyBtiKBt6Muc1ZIqhJ7dc7Z7fQkd5WY99C7f4nG5b\n" +
                "kUerZFoR1UJYsVXMKpEsJ8fg1udySP8F/0jB5Cz8W4Ti7PS/7l9mxAM76Ow0NSfJ6c1S3V2LGACX\n" +
                "wWF6syclYyWTyNrV35KQESuZ4j+NqU0B0/zjUW37EaRb1l4ufMEtUBfT++0ZStCSrmAt2E3cBUk4\n" +
                "N1qm+eIyUjcMO0x4BQofpUOc3g9YKPFyyk9JPIwMASa4OtpEFmuICaQaB1Q/lNPvUBxxEWGnrDQU\n" +
                "fg3F/tD3sDogT5TB/S6FK70oZfOKZ1+rP3YCvUL9zd2dxl8nu5m4Nboskc7mwOZG7IVVCVefnst5\n" +
                "N8ZUyMZMwiLSwDQmdXZ1qRSR9nMvmtCt91TtpfuZNERyFJ1q5G7nt5/nskyBkTGAOW74so9VY/gr\n" +
                "DNmyGW6C+RLmkJo9C0UfkwMkSBPqkTYiRyl+tgZ8IGze1XtkPCbvz6pLYKgMwf4VP6Pwla0T3bYo\n" +
                "W/UYCClsV/T2IoFi320jqahpOuJQdGikKsWqdSRbHsltg6cX9TkYq5KcN6zVVJvhvPwUDHsSUAX3\n" +
                "gUBmIQVze1zwV211EeHdPbAhIkQOnnQffuLpcMqkY3VR9LPuz6v2Af6CmdEW75k3Go9PL+5+6Zy6\n" +
                "dCsU+GXu5MgAktpUZWVQSt5JnMc8ug74R+U7/KxqmqbYEL1pYx2kgOmI4SIQfp/8TjAcoFEeSBts\n" +
                "GBxxAmxIOoqNHpY4oNEde8snhlMHfvKgC8o5YExHZdzlnarSFmd11Kybs6KyonNIczGyPcMP9IQd\n" +
                "yWX7NIbjwNbkkC4pI3e6A3A40ZFoykCat+O8dJU7octEhnW24zRySQCyYnf/xEKmcQLKyeYYsUSE\n" +
                "EBPh2gx5mWbX/e51xEMoDrdHAVNb1co+NgM7dH6QQgHS6GCh0Ydf83UQVoXoqQFIcaZqVICL4qg0\n" +
                "htR/erQVwA3JWp6+WTAYiTEIe/eCS2bLYcqdUMM3S27ciX4NaAH8W6C4kIbeJgQo4iQwLXbU+lqf\n" +
                "/fecuUB8hgZdIYIk8h7qpR+6vrph6ppps97AOSL1PMnhQZ8zBUz4DO5BAtECBQMQyCLbpXEAKTT5\n" +
                "HSQLLFv9pAuD+H6Br07jimZoBNpoGRhDNdc4qdpf2p5ZmFhpeMKv4CuttWZLlfZiKLwF8W7dPb7d\n" +
                "thFvctRCaoPCzdLLEsMSQzWjufzq9unxHy691X7YPf1WGmgxGF+4fF2T9u0/HalawRkxjPV6BHUx\n" +
                "ekVq7ChfV8Xtky5096M9JP5xBfq/myUR+om0jPyiaVqcwxkoLxktebp4XXEMzKB78eT9zzBTPeiF\n" +
                "zh3C7LFpWeyOu3U2SsKWwpigD64j3dLZnbmaY/N3CbrMVev6dzA4uqKqc3bPz3Zbt/ehpLNxK9tm\n" +
                "qvWoLiBgmbwDMH9YV4fJuGbR6+fCCmtmZN9c+feE6La1wrmoJqS9w/SsbPcSpbnvgKaY41BruPhF\n" +
                "l9N0lvg5xPf1L2RVZ3/5VUS0WzVDc1RZMYHgzBPDRWyx/L83kRTz+CV/8CT0pOTVtABrmjV9CqS+\n" +
                "SB5+MoP6R+ZaGol1jcrfu+ADM0jvB4rJjfLqB/GtCc4FEW+Md6JV3oWUi/KsBjNqP87dssrAIffL\n" +
                "obAY4wLhr4JXnP6i899uXTXSRzu1ba8PUffAkwS9pFAZ1Oxc99r7Fj3EjD/9fhPVKz3mnCGtwMAE\n" +
                "ElpaigFtnx2//sQnUMEKPvMjPvDdHrdWUJsw4YRuqCJslQhZ1ObQdeh7UoDKP2hAiopozXo+hGVK\n" +
                "fWDQ/54JtRBGpkYZ3BKiBBsyhNkkwTAQIPzP1RNgfPQ3kdxySRwIhucrmvk6uSXE82CwtVvoJyab\n" +
                "oNhYJ9nwxk+8xa87ksb2MdTnL4iE2QoUfEltDj5UKgtU+7TFvhZsFJ0LOz9I4bDuak91WDImvQh8\n" +
                "cXtnsy8yT3nvvoyXXOdrMfPz8+gEYG1VPemFWyY8qVsrukUfIdL0sLwzc5uYW/dwaRq+92DQJ975\n" +
                "eHQVG7j2MIi4yeA7XZTZ8d6/Sqnila2JDV7arcJxB4A9q8oDENUQboZakf7C5jKMDYzF+bmkdbGV\n" +
                "3KGBPNnLKs0CnkrMPoNhCp5qsOOve0vxO+dqzl1xn00nHNS6nPK1GGMeNYkWoTOKEG4+YzOgKZ/u\n" +
                "SNscV+XQ6+WVbrr/fEwNnjUVibhRo/luRmncCn55e0Z/IxM5jFMyo6/86FWU37cat39gqr2AUD4C\n" +
                "4RlHvC0L8RLdhheaGjgfI8WE3xBDSA9MERdjKLOkcClMP8rFH0SqKhG+AZJh+USfq57ZA4H/aZli\n" +
                "HakskrUOXb8cNNYp6VqPzIn2D2szxhMmrTSC9iVqGeIlNVY3ynZ++u4/nS7b/3tal9RAyDPzrRz7\n" +
                "61k/1p+sbCe4qxCKNTB2tscWtv+QvN8vWzmajf5/hXpAopX88Z8Bk9V6nrEILwye8+fx0MiKhyL6\n" +
                "uDC66sMpVmSBD7OhrE2We4JvTm2c508SFXAt1USwYP7QAD0utqnrJI1itLe6tuvwFmocQnTwBnZP\n" +
                "c3ApiXutVRJwob1RYQ15G2XlNedb7g/xHps/l7b4qxOQtpkdSIIXp7czVKFQEzafqIMKrhUfkHOt\n" +
                "dn4UEUF8IDmPSh8oN8FEA5NF6v9b+Oxz43ztcp72G1ocqKaHuJlt9qBfPwjoNDQCMCN9fc3QM/xm\n" +
                "d3L7NoV97QesIBNM31+cxRUN3d3kRRhj1Mw6IcdCh1GlIhdeyE9WGxwswr3NGpbbELZR2ihcQmUP\n" +
                "1b6/8TDvM+FOEKALujh6HgJppvp1E0We/CGIkbXHzqT5XcIREAXU+mye58Saq8gex2kxgBrMexWV\n" +
                "vaOqwHbHL9cUNCz5K/kt+5x7UJSVDHRd+vAPXAz2LGr+YQNqZUMwA2hgEJwd3RLOKu4Eq+Yxl0YM\n" +
                "Y1DBWfN1fqBCzcck38XVfyQDAps8CjYPdL7GQZyMno9sCgBnvIztYyrf4dTIv4RynVHr6N7cLTYy\n" +
                "VuwleOkapRicwstRv4IdAKG0wQd7rGfMvJJ5RZ4YM3q+BlIyWuFzyI28+Ef1VUd67BnNlt2cBglV\n" +
                "Iqwb+q5tNLdBFh+1p2KSe8FB1AbM7dxFzHndpz/1e6Qm7U82OSX5e2376sVUmLXahmlBGDMPePUw\n" +
                "8FkDCS8uQb5livVsoPlORdKr1MznC038G2K+wDnCXo/nogqa9TDGcc5GSoE+nmh5MBv/Gn8J4ry7\n" +
                "WA1aE9XdLkMKbHnTv3gFp402C2+wJ/8TDvM+PiemWgKmVme4dpf6jTLedYuzkBu6eOmLbe3NaGIb\n" +
                "Bu50R8cXD3HVogBloR3S0m5cGjvrbgxP+w5tRH3lt0JrMHVLbjcA2FKuTM9fTbWg2WhJ4P5EoDJy\n" +
                "LMJ3Kjd1zBUEsVtB8Dy2eLJXrDUitv5KvN49MKvPCLB7wU5ZI7imZMLOzYPuPeKOxv7PBl54WKSh\n" +
                "yhF7H/32O5HqontECcAVJZuluIfbohAskJZvOyRow5AVmRzI+cpyB7bY1tDDBqBoqbmztt/7IHYl\n" +
                "AIv+AmVyVij0TAsTOg23YExMvnQmSJPNEpW3swoOj0wNzxGYxSir/GxDaIbjVOiJBPX2mbI7o8tb\n" +
                "YAxPSpHHMX9UNAO0HpmcWa9XkHwRYCO0wXWwxrPBkJ6PWp85f6EOVms548NEaBMdmo8JgTGR46Dk\n" +
                "Zg782o8ZjkPvC6neK0IerQ+mfrvVv202FbEjEpnXBZT8ef92DqqZRCbP1L2bF2C6KNdAHJZ/wNtS\n" +
                "HfEUlTfapXwyccTMC5K+QNZP5vfrPsIKYN5HFpiwF9hqScL5eoRjL2cDpn2vE+m+Xs/kjSzutAz2\n" +
                "qd0ULL03bLT6DDcxbyvp92oYK0c4tE/YuR8OHmSYz/TMUJuhVYeFuduc972ni6UKSWCI5JuT9vID\n" +
                "magmqtNM+qg8QSPgGMWwF8Mb6B9qD/4QUiOsVKBwG+sIR0ghz75tiuLvC1SXJDp5XxO6iBZ3rlX/\n" +
                "8JpIPwgDrF0OF+TvcXCIbWfCOxhsiso4AARFsE61hXlCectPvLmrjaNYcDyo/p2XzFGDhpBb+Diz\n" +
                "iumkoDM7IzmXjpOybZEBywzWmZRJepIOC+P63SpPmUd1H6y5ZNuW/XBvFZHG5ZaWg/TVx4JDZCRe\n" +
                "Lwsdr+SSBAbo7fiqmO80e50f/nNHxzqSIwhhd5Asup6gLnz9Dctib+VjM5LY81zPK+4bGb6zQio7\n" +
                "pEGVYAzGJKkxWBo85jfIQNfAA75vMsHZqeL/UJsiQDzoZ9kwXVEQB5/eEy0M5u7cWST2QXQzLGh6\n" +
                "GGd10d5QPGy05aGgEtwPIQaV526tD1QPejSXgDVx7OHLadNU4ld5vcF4wuOX2aSBZiITdlBKjygz\n" +
                "p9vzTECc9zy9reIvf1K6Rplv74cEB9Q0JzbuUvuiHVArhGjm8BWfqhWVf3c0+2F6FdIc9GOmEqzi\n" +
                "cVPICH4RW4yGAxAxfPgciKutprEWkWMxQufDGP7e67o9wB6VDl3PWCsiU68KWdiER3mlaZ3AZyGv\n" +
                "1CqXAW5ujPirKZjkM6vVdQVJJ7KFMUDnfN7PGzcs7vnZ3hzcxQcVK5Ilo3gq0TP1tR1Z1Uk1eMbV\n" +
                "j9gWl7F2h90C9sElnF6qhScGW98Pgc2MXs4UQdanCaIRrcacYONhB/keXrLry6qHvbN64XfWPiUi\n" +
                "OE4kk1yicaFTD0W8CdJwiuUnAseQiYMQThyCn8ExCCuqEbRIkuhGLT8Qef4dteWGItOagZ0tbvIx\n" +
                "l3tDNsnBvk+DGq7itXXGw0v2Pw5VStQMWej9rzRMuXIpQ4k52tJGPEjhz7oxIAzzAX8sZeDP6pdX\n" +
                "9drf++YE+mwA5ntGsh9sdca29eXFTfE/MzD/mu/WSsPHp7BQCdpnLiqJO6vSQQAOTj0UXU0oJgUC\n" +
                "MgSotsxRLfN9262zOE846ZRICwbowfaXNzSY/pDpcXfdHUND2c9aj4d9ZU73cUl5VVKaFqbiBDH4\n" +
                "SPO9VSgJ/Q+LJ6QJUeIn8m1gFG4STHhEZ6hAqrRUCIVtf+9BmHsZQiCWkGYBxgr6cV8tUpdHBaYE\n" +
                "GEpmqxGRawmrhHC9NmgxtYelvH9kjzvVUoCf0Piyepx9Ltg6B+acbMtiPd0KO7S5kKpCvnNFRFJs\n" +
                "RNEJTuvL+A/6xUpZnt81lCQJZQioaLvAxfo1w3JznD5LljzFC+LkM3PkMooySBDqlo9mFyoaG8pe\n" +
                "PQ/v9jPZXZlpZCZvlbC+rDAFCQmkT900SA2h0Ta7hicZzXvPwiT5kddpNcJjEUl5pc9aLZb/0wL3\n" +
                "JG1kq+xfM1kJSR2ehU/ULL4sp72+TRzeDjsiNPf1kPrb2pXg2DPqLGvERJm7GCG4nNIy32JLUbQj\n" +
                "RBtJ9uo9lw52T823Rl0TZEjBvWLGX+jGkPA5SgSTwCqDQIsuF9cS2spAhJC7Z8pIcAyAp7L/3b8U\n" +
                "Ng8OieKAlryThiHHbgkWhvzvYZO1hDx9Up3LtFlSwjUPhoMCpUuW8X2KIxH/dipKAQ6P8Bt8SaRm\n" +
                "GEJi7KWOKtsOBqTk4uuR40zHDJo/Gt85Op06MIPj8AtgLitFFEXE7O+GFQT0fmmWKGJUQN/CU0iS\n" +
                "efsBj1sH6XD3Eejs/gfUMznLeqiPPVOgtAsAaaBiPhlGI6Y2688tWeIdZDV6VyDhqBHx8L2l1fPJ\n" +
                "lwvpgKasUYLHcNVaRnEBUkrxYAOfETPHN5V1YuAvsep/l7isiqj+QwDKL+3YK9MmUKDUt0Gh3i3n\n" +
                "FQZu1Ackj97HA/nGQnDWNc0sSj0G1FP9jQWEri6TKxjxtCc/bsSiv6+wULan1RcE625Hdbyww7fJ\n" +
                "Mhyb12EKxKV1+jp4mbq1a1YT1fHV9wQUThb6TgO5d8o6Frq2Ksr7ygMxotQ/zWzQjkal/ocxTNxi\n" +
                "UnR2Yz+5dQVqKMRUVCfLxXo0U7dHq2KPmAuu82lL/GrsRbjn6LnRd/eN3kqjcQJNmp2X9ba3RvpV\n" +
                "hFWqruCsfQB491GpQuz2GXdOPlmEloDi9J7mSiFjMrHI7n4fXDior4ZALc/CcieZnAA24Jd6jtEP\n" +
                "PwWbPqinRG1G521mrrTgndBFFaQd/8HJn+aIucfaWFsSbhALOdSjvQh6svrb5uJlxjQlNEuSkUPa\n" +
                "UjTfH94ZbQCvCDduk/z2FE8drXc5owxaITxjRavs/FLnKpyoRWqmXU2WcuvS2gtL4BDabS/zXZie\n" +
                "xnn1VGG9d6wp03DyjocagwiZb8yviYjHPiugxnOLZYUnXLmlFCND0xe5yammnfAcwhAB/EGbbjvF\n" +
                "u/ISflQZbb/FjhVH3NkN2noFzSShjXqNWapYFqyo3kbQxoKfR91EAT38lc3NKgDeYk2fWi9Gh8BS\n" +
                "JPxMWbL8Ygg9hSYyWcNgS6eOUA2XGY6pAR/HQ0Fe7CXWYWtXJhaJPIbyEs8ymQh1FORfe30kqpbU\n" +
                "pIATZTIE5bxJ6aTkGGjHoBa+AWHp5vaa8fVMEIu2uNz3mKFwVTtKpM/8bIwnKUlQhQlrf0um/hmo\n" +
                "y1+NbV8p+lD3keSb0DiGbaEkTq5HGzGtLA0W+5zb0Om64VVAVxQ2QSuHTWENpU8QJfxBhNDo6JQm\n" +
                "c7kjXING3xP6OMnV5jv/1Ed3uA3OMVNgQLGJ/SXiqg1oNtOgH3pA2z/0sblI5scA6UoEsAmEGBfc\n" +
                "mMXeAUWMqK5CR1yPvUHZ5St/h4O3haoiEQXDywein8QYLQ5eUehgLi+HKK3ONvMZ+to1nX7AL6vu\n" +
                "iIjikYs7M9gv5Z7/l1AeHSQ31ss5R75oDMJDarf2XOiP4WrWYWUNN6boU5n+ge/3AEfsHppkKONC\n" +
                "BYlEiyHsN8q6OyJz/82FnZDYqJR98s3B4ZF+TGEOABY0/t1A2/zIZJb7QGCCBuK0xF+eDVOmEKi4\n" +
                "7xKl2ZgdqEp73s4zpskM9DILCTYVz/69bX1Osx+/aYtBS3IphKXGN+P2DC3VhlmGfbp0H/DshkPF\n" +
                "8x+wmTtuNwj/rwVcsIrrFIgf+wzFG4kP7OYHHVRRFvlu1VM9TyThGfnuo02cMKeqO6T2taQ4hiuv\n" +
                "IOdITr5yBTXDLVzAuBqyRszVqGDn+umtC55lYq6BmdXmlYoCXFfMl7suLKg0yfFs+pKqXTMhB5g3\n" +
                "1NzPavPt/UvLZvzmR8KbGiY0vkGdrEjNYbbOkPQyw9W34nRKIjQ9AgtHPAl4XXYvhfCQyoVMa73V\n" +
                "6Zxn9mDsdOpdKhN7wSflmgW+d940facH0j8iJB81Wj9hQa84valIHLKVwAhBLQkj3osjTfeq3ky8\n" +
                "ZKlwWytwcKoXOthk6xpV+fES3/V6tqBwARLGShf9L9wUbL16k6svPLdCTIgLnH8QFd8m9r7m9/aL\n" +
                "oIajfkpf//S569SZObY4Fgf0Eps4jZ/tpxIEL7DgeiD6S7e14eTdfb0w+6BxArdq7dJgtkrcKnkE\n" +
                "sbYdwpqtt0S7QNXTxYe4ImyG2YbihsqkZL9LTWS9gbKc+Bw8LoH66xGueU8s+Kq4XONuo1ZuzN0W\n" +
                "SEVKXPyqWyxvclfcFhvwOGk60N9I2/CaB4cdjZ9H+BdNa/I5Rl31LSXE21N2zVpcUReyPtp6rZCw\n" +
                "GU141bP8JGe8St0MmaCzVP8wT5U0Qb7/aD0nQbTBPledrruyhyDgDvUQS6bCNptIlzAhlVcU7ho4\n" +
                "LH2PbXsW1f5Ex1bjPCy++NE22ES1d21TKP7V+zGljImM1PRfBXONMa6opSc3dII8fZmFNmQQsFiY\n" +
                "BQnJgMv2TudqQzbH2a50tZq5yXzNkI12dB6EwiEM0OwEsTnirD7b313bkhpHl6in952x8aRaO1L8\n" +
                "Y23v9EuhK2GMgNdiryDlffzNhxqiSGU/qCah82JuXcFFIVRUwvZS++q3yq55CiBAl13gNm2p3oei\n" +
                "6MsOWjNOiXXLY+kRnybPsotLpzQ7SqUf5V2HJx+wJL/IweP+zLwVKlIQ10GYHyyWy5QBlUUQVMtF\n" +
                "4WmPWqjqE4RXAqcw6o+wGVmyPhGg+ZTsF8eGyQfHSwbQvL3jK8fUZRPApWOzOXyZmeup/qc/XKLT\n" +
                "i5nnZ5x1Ia1gKcMpNmFkn5QD3M9lu5TZkN3MV4lesfHferV6F9tPmmUbvTdPSD226NWE7YqNSz2x\n" +
                "/7IK+eYx+lZXeU+6gy9JTWzKB8zT8D0Tvz0MJL/IweP+zLwVKlIQ10LiP83aKRhXltglnYHatdBZ\n" +
                "ZcWcWINSRHGtMXhQhNqGRH1UXc3TgSt+gV8k9TjDIe2EvWZKnXIPa6Ttp3cqtia98xe86Prk4mdH\n" +
                "Z6hQukKspyTGNFohCWGC/mA5YW4LyaYYgdWMLA3W2LF9gQhgXNquaVqW2fdE6DtzpPVaMWENYAVY\n" +
                "JPWVSxKtmHbvGZhsos/iwvG8i2CNg5IVGcH0XvvzVCf12euFRHl0RGTwxK4TEi8alTeK2rLYJA/2\n" +
                "d8vhq1NzXyCItKT4muLcLj6g38Ut2SaQWuqPpPFtSdj2utrSodYh2IWRLvBvI80AC0JuWadAAAJ7\n" +
                "SqBCMJN+h+LCRPd2YEcCHDSenJGjPBe/OHth7cl4PJ+7CoKlTZ1g8UYDl0oCZL4JFIZfSRwZMZAC\n" +
                "S0W4nK9hIzH4lTT6IE3cwxVbPwx3n0JBj2oVOtZ5IKt7njAQ1QjFuiH4wyJsUAv9631eIHYYVSQ2\n" +
                "jYDyS5Dlxtys4M8Hbji2CyYEem6A20Da/ZS/VDIt391naCkYCZNA8yfFimTqnvQ26eMIPwHkQMLA\n" +
                "RHCLzu5bZ8j5t97Cz3veyChRTeDRkJ2eG0z1/W4/1Y8ETbW990fzlbtDqx8gA65lPsJAQybHYb4P\n" +
                "MVgwu09Kp6Dft1j+qS5do8YlQejWph1Euo5xY7kiz8635NmWKCn60TVkRNK+DJ1Lh1cKA3F5ZWGU\n" +
                "G+tGXsX9YeuT17gTKEEBLCBHd5zeA6o+80fOF1jlDt7dSGPXawl35CbZzRc97mrtk5y6Ej1L1YDK\n" +
                "KzHLMMM6IN/+ES5Wjlb4KdUlriS7f+QL/8668t60L5nATlO4W29RyCmgqfASLlR78NMX6KIAIV1f\n" +
                "02CMAdU1b12UfseGgRDLmSpiltnuWxuMBpPPD2rIVm5bNhyAZUkeGyAiJmh9O99CiGiBN3x0EvQy\n" +
                "g3bsTwhvaxczbaZ47gnquXYk5PlY5jOC5KJaWDHxlLvIemZL6BdXYLoPToDoGBIquE6KLjlpXj7l\n" +
                "APPvVK+eaXGCNIuCg6E8JX3O5i2GjBrmhGFtz9Wy+VpyU8BE4zaFBHcCBaIry8RQErnlwjOxDa36\n" +
                "e29zgXK8kJNZANHA+u753vilHiijHrdV6Zlx8qjzmHSUGjbjy0idFrlKO3uDu0Arr8y33zGa8E/O\n" +
                "7mcoPxIimWI3AUw+h08vOT63wPxQaCwxcgzr9yFVcaYSYHlkxZdkI1ZQ9P6Ybx3wAqnOmPUoGz7I\n" +
                "JmgFwjPYoEdEfqoqUVGgRhG1voWCmb6sXM1mcc9yQNTldLsKG/TR8jA6IBCU4uO9doXwVNrA8qiP\n" +
                "EShsXlZcK2MqAuopf5UXyXvu5jX/idoP40ElC/X70l6RzliW3NDL7mYjdeBACEzZEVoabm6hkhG4\n" +
                "y81vlh98Kh29+3Sh3ywXs7C+NVkerelhb1RYIDiEpeG8O9Orh8LzkAOb4o2c5Uv84H6VUbS1S/UT\n" +
                "7wFaCpKaBZXFkTBGz1GORBNOkylSZ+ulCB/sKi0wa8CnhBK9CUQycQTv2iopCguGdqEn0UfII0gD\n" +
                "2oGeT/XHLP9aJCuTvzD3TaayEeiqGUxvPlnGQvlI3bIjrre/tpibaDDjAAZAlZWn+MYvu0IGImFx\n" +
                "BRwNvA04gfXBLri4wzLDJ4pGzPK8Y2h/Vo7IV378gtI33O/5YC8lIX3or8Muf8D5h55U4uSzignC\n" +
                "rF0Jr7rAVnkRZpH7vJvKllPtItaIntTdrLNTcc7q4MqpHF/6ngRUAkLzi5R/djFffgcN7s8y3HsN\n" +
                "Q9qkBkfu8aB+aEUy9IMktj1ajMiK5A1PNyzXVQQQPfb+C44ZXa5BSiInqp+hpknhv0thS7p3Go/+\n" +
                "3NsRDGiqN5cE1Lysm6wBQEJo1Dh4Gtj6+fFU4ZCbO9GHxz7geYQ7Trxo2pAAO7WT8pKthTulD+rw\n" +
                "xk1jCqdnAojGyuL2Vfdz0iCD6OL8tA9FPLlnz9/jh+dPb+MME5hXQaI+D/mf4KuYXM3aIEPsma0N\n" +
                "/9LSZv1uhtDIDILqfrhW4/0/ZJnKemqCPAcG4h06AyBLx9JPrzfyL/tmAM1+SlSNp/P2rgiubBW3\n" +
                "WzkxE3zNVMJbwe4T+ciwgAiKGsDCQWw5NXsQR4XxHtNbxcPpdlGRz0ukEgCyfY26hXCQZAg79r2y\n" +
                "Esn3hsBYQRZUB9gB+fUChQi3GBgxNm9P2i9gt7TMwZXg2t/FYc18JuVRcY/vtNPPrfpHqEO0Ye3y\n" +
                "fLXLznlan+Bvzu+zo92NtxNffdbuCScoZ2/qoE0YyLUCdXsIZwyc2w6c8XtmmyKscR7AFN/mjNcd\n" +
                "7JwQKJmC6O4W5zry9plXTSybM5mfha9kRH5wfd1lFqgCUx8NYon8zAwNf9N30/0nOfjnH2BQ3HdN\n" +
                "h3hy20S3Ua+XTEDcUhgpzzRTQswYQJTYg/YKFs4t9LKprHEycdyGXvtFPJePxx/tf0jPnkvOqHVE\n" +
                "nCGXLOBRpIQ4CxLadrScqWN1bXEW/jj2R3h1VLdYPyYuPgedje4KgDnst7r4QaHDl2S//QKgsDFa\n" +
                "KgaQUb1UbxFHAwpTXRdM39dijAecN4o+0XyO+Y1m7h7Eh+4Me77klGunueCJQO19P7+evFuEjrhl\n" +
                "ghLBPOvfS04qbatACA0nsoxVfp5dPNDbUJEMdQ7CCgVu/yrI9KDDTO0bV6uiHPtor44BUVHqZjse\n" +
                "epGN1gPWSkgeDxjXttYU545TZjtTE1RGzplws1IX5EFnh4b6j9RtL0vKnHva//7XYRrvwrKJABr6\n" +
                "pa+Owjitbt6v4jOFugklEJHgI7OGfzHc1l0A4HevXofyXIj+3XDEbmZgHo2z1G/sUS7VHVDFK9oK\n" +
                "WryYWRL1YcIgZjZtb5C7oPIrl9PQHnshtWCNMVG5G7X636+SX3TkU2GTSLe1lKYhElbMXzpmN/43\n" +
                "tXt32DkXLsiLcyR3ikMVnQhkgPVLA7mBLeYs3QTUq5fMtjHHx3XYtFrprQSGEh0KzHiautuElZrb\n" +
                "fk6eWjsKpCVkge58yhzbAZGxfqf1xV3412zm1q/TH5LuRe9L3QkEN0dpHvKnaIfE3KShyOIBacFQ\n" +
                "UwxWsKwJjA79ENsaohQgdpxlnpzhNhJiDdRQMxXA9lguIe6L12ciY9uAx42koKW+zn8hiVpV3zzB\n" +
                "gOjdIfi2ygpcU2Sa9Lwk+AZcq1vBPyijOkmT6Rutl6pDVWSR/ZsQV3DoXEgwXB/qLeFDFfzs+2WY\n" +
                "VKQ8buti35ophJg4AAZqHqvQXyVz4SWPJKwpj3pabs9oC94JrAoGF0ltTcjVJY7NJ2dJmHhl+R6g\n" +
                "JfBOBuRmP6MpRlhydGbbFl8pjkBGNFhiQ9jDRW2rsiGSW69g88GvhGnaDBSgEL5eMyWFDyYxIgqG\n" +
                "qF8wybHbcrCXs5myXX8n6CG4SwAlaNQBk853sr+i4Np9BwjV6lMBDQ2PKKslh3vlVhcCc0i/X67Q\n" +
                "JPpEwsxUn8kVWyvY4mYwzlUjaPd1fDdWwfkY0ggXArX9eBw/vbiKQ9RRSgwQkGuJuybaTwCCyFxi\n" +
                "8WMWcu+Q9OSm4KHlEQPsWgMrayhYoawkHqoJtqMqQ5Mgc2cK8Z2luqLy1yCuMCEDOGBLmGKOIdGm\n" +
                "FH5k8yhx/ZbnOgksy1UsO0TEpNoEUGaKeG9E1byTvSUTxHjrnrgBKu5v2XEVl/KB/8kyGmnY1NZL\n" +
                "WHfTY7V2RvvmNqNS6Wh7eOT6UBsoddFbItEgbWXE9kbP1okmJ46qYsKkm227fz44CvbMDswMLrcM\n" +
                "5QekNqBYQXcMtwGLmX8LnxPEtBjO/fGJKN+OIn+U02AiUUTYUlyE5MNdTocu6cLTjzX8VdxiuBTO\n" +
                "MjuUv6fU3q6MULchil0Sbg4FNYrBMxC/i1dkQ/YYdkl87LAnmpidRsSojgwGdbxI6nEoT6HSI2Ld\n" +
                "G4WSSKLg3QB0BQ8tH6qsvYHS/rv+WEtMIOfswYzCf4ISeLE3owTWvpm3ujN6jLK7yV9r6Mq6fJNS\n" +
                "K47QmbW4jaXoumnU1sudlHdXONjgs2uMXcl5CqIIRjugB4FSTHzg0oGcdygELTlB7g3lcrPW2Hgy\n" +
                "0hX7JtpWJrlQqoO4uvJzyepodmin7+ge4m0koPhD0zf2336uUN/VFsh8LCGGRMKvtb5ecqVufnl+\n" +
                "tQRts1EFI0bsQcEHIZTvJ1tPgGhB63DKy+s0OUFG1afzL/D3ABbMNEpTj7c0xZaB2NWePD5aoP9O\n" +
                "NkNM8bEUX+RZaHozvFnnEWd7FGZy8e19gjQJT2MSz6pQuZlW0sNTDuRJ/+QqREjJBMXe5KzB+WVj\n" +
                "RLI7nsG7l4DmRjcbwXg0QDDYPJqoEi0CNCrlf1VEQsp+rFa/TcCR4Gk5LTgJTzz7Bi4Ka7fC9F7k\n" +
                "iSuBm8/W9siuEyxMxltHNuuvRu7wZbZYQpgeL7mRYuT/S9IZ5bQ9wAQX9zUCmCEtFtwG+iykOiiY\n" +
                "Vs3NDb0zIEvmnCRLspRUfmXk5Dg5qQ09+zL/LbX3y74WewlOKsxXmtt8Do9s1SJg2BgDtUApA1ed\n" +
                "G8Z/1ScXidqmQSxEs3Ghc+WlRftIgVqLsmGuKKVUiUDxk3+qcQNa8l7JfVR+IFTuXFA980ep8klh\n" +
                "0808EGDdxFDXX4u8G+PxZRMvKP6u0t79yiQVGHM6MsOlCmHeRhlhL0iIFn9grBBO+bdHp0XvcxIM\n" +
                "OQOJHM/hsceXslPx9LltEjOuxNO5h4UQyjeJTXZ4wi5Wk8LK5+TMLQQuQ4ABW8B5pqod+LmJG1hO\n" +
                "ClXuYaYXuTR0fl/Td6z+ZL7QBvp0XFpV/HeMX4IXiAtwHo0vr1GuEzGmc7WNO736PjM46kYxH+Mw\n" +
                "NrGxJ/A1NZI4Vu1RiK1VpSgjMRi/vFGI4ZVma7hFzvAGNEnAzmkGeyYE0oGTjzGeMaC71oLsdgM6\n" +
                "/Zr3VhIgz28UuKfd0YT5p8nh5lvbYvjeudix06ETbTFfBK8/tP5YDz06gLhn9NXfvCuF48mAcm7G\n" +
                "NX1aolt1ZcYp3SoaBZOXUBHrKrLc0uGgnqN80IkuZMT6ALRQpWs24gMOFgW7g1/yTwLmoHBOzgkZ\n" +
                "PLaNahlycOahWv/4tRf+no8Yza9qMwLZgcFPb2zUpgJWfV0Zg1WMhO/5uEJ4TRNyzbFuVI69ean0\n" +
                "YCA9IvCOMv1qB91k/ybQquhs5cEkE2ucBviaT05HJA+/aA+3AKC7GzaoTSXfesjRfblx82Et2bBz\n" +
                "jvhl8p6fypgc3HM3ImO9ROHKzeeacqMZsdFAhZ0Jw5jZhhdilaEPzoIaYU31nQnyKj6jcuZGv1gs\n" +
                "IIWzTGOz6dhfhjd1cyJYnCgycyA633stJPxndZwKON2MIVQLJM2nYbo/0bz2BlkJMnc120qODyBu\n" +
                "SenwunEqKreM6GOwFJx1KYbRS+lPJv+DMMccsHwbd7ZMDDRGt1AgrXFXxDHIcY8fdU/JWVaAQvhP\n" +
                "ykpX21MAzfv4b3eEahJLtWpi5L2+/CdhSIIdRc6G5zWhllO8VPcgcDDMLmATFcGOEg/rGKXBwz5j\n" +
                "XZ6p17MaPsXZSM0LvCBCjjV37B8GmJqqTzKVofku/ohzW9aw55Mn+MSSZB03dDGn8ln5RxNBkJ5H\n" +
                "BrPy43kv0FY7tAusVJdMT3VZjVfH89vWKrRHtbXR6QGY1d1NavpOKbr9epW2hYumt+4cZr3a9pUi\n" +
                "/EVt+9WPXVm7UKi7dnGlXkoDdb2/pSZAmcNLuSFpwH5aXf6QFcxxF3AN1JDbp71Rrr/sMRCxgTs2\n" +
                "Cxbxj96kkNONR5Sbj4hsqHkHy1z7h/ODqZOdBZpv30dHojysA2Cc71/MjbuuUeI4AI0dIxG8APed\n" +
                "6sbD1/DnzgnMUC+Rr0FHkrUcp8ajYcOR2ZDLr+qpKnIYMfCfzbbEEQhnscMS6lHactWrh5PZFTDK\n" +
                "qjnVQkUj+TMryqdA5hpzPkuFDjStFSX8dS28B0CQ4Um8Dx3UMTgYay2jrWE5V1TXHUXtxn9uBaZQ\n" +
                "A4wAYU1pVMYJw7WSn7Zmj4xStz04qsYl79iROqKlTcwlsZgw+jyxaaTTfk0kRd35nsiC4bsJuPYZ\n" +
                "T7jddHwNXyOsXNkIXvBdVNz6K2iqoSKQTqYeihs/43fh50vcadEZ/HSOYcQ/w+USwxLoOY7ds3Bg\n" +
                "hVQhTLsc2pywEY74BapnY8DE20ovbi5iXQM4G/BY4q23Z4AHmMsiPCNmaaN5vD2LRbkWlUOEE35f\n" +
                "xXvJmrH7r6B6O09wvpG99YlpnPjWux6sJP90dWsJJiaVOarDnuKaJpcTybLYWnBkxmXin73I4Po5\n" +
                "TBOkjjyU/y8H2gGmDTXjUNxD9YPV6SP0WHt+zj9pnwxHdrk5vedh1kvc/CK6iDaahTHAOXj+rrG8\n" +
                "VrVvqcWsJTMgXJL0WI1LE/u0rtbY/AeJVRIuhEu6fZrSpv8fZuEAZOx5YCvvwi7B9rTXIuJzjL+U\n" +
                "8BoNjJNlJ+unCH4y4iGcBlhOqJFLAinI8Jpa8HOrHco9gFT214EOmTv/Ehxeg2JiRZyFkv2dpUry\n" +
                "ta85gmnkpozNAD/JGSPWwjWMshu1ucmYbIMXd6ZzPOYGA+cBHgM+O3MXUsKjt0nFmseBGYP+Yn1A\n" +
                "a/h6G7MyBkebFyJATx1xOo4z+vLpyJpGFW7nMz3YLqkevkEikpEvz68RCQFwJ/uPFMmJEoZfzmCx\n" +
                "xsOYOYNIWpssUeMOtQDQDHLkFaLZU3cuN1PGlAg+zeqAeuKhD9N9sK5/kkaxs8P+qFld2bGXwNKb\n" +
                "wJh5/8FAMbLOWCcp4HMGtjYZgTbqLVWj4h6HwusFk7nbFY0PAdHpbilrWblHLqn32Lc2ZAFBKZGj\n" +
                "/d2yU1WQMy8Ay56gqYGnIl/F5sjn4g7r6M2lMt6f071KC8qS8fz3bnCmaPFpIEBhqjCou6mqJdo+\n" +
                "JA+mKy9TDnTkaOMKV8Ti2lnVGkOBYIj4sf/F2+xIaLOM1PhLBValesl8RoM5XWmEimRWJelW/Ppo\n" +
                "6HXUkgGM/cCFqASspTSkhBkLlUlmdyzX33COa1+Pyqv3JTEdtK4qX91ZCi64vla3TMc9lATwWMRb\n" +
                "rLTIsWjfLJp1BexGaXxkxVj5+m5poDw3LZdrIca8oiV3UXgebBADHX6ZOiYaBBh/IP2uxEL2iONS\n" +
                "KGp6ZLe9gH0uaIOgiR3CDa2jIjUPzqHil2Nrey35EfEkH/4i93MsXiWc69tKhNiNeHcmErL/+F7V\n" +
                "Ir4lO3+Yf6uG4BPylSUkNmggFDHSUjV9BSdb9qIy+e9enHNnk54GIjZWZFopUUJIhKkjiFASfeQD\n" +
                "gr/V8ZPJCsRjrnuZRyT8PgCHbTeN0NXCfICJJNfwsT7cYSXssOhZpcqulx8sgkOwIqCo2ZheI51/\n" +
                "wr9K2/1MCBjIoNC9hBy04xGxab8pZ9ssNN74e7maMrSnXGIxgdnLyIkpZu2lmnyWyGymV33eyMBM\n" +
                "Yl4sOr7vMoL7Y2hmg40TXXYXP/L4WcEj1oto/jBfAoU32OAmCiAZsFJY4DFxXCz08W0V/r+fXVf6\n" +
                "c7J1x1s8suaOCWxatiibpdA/xpSi4dH0FVbqrjNETJMCe8KIyQ1BEWcUvcunCQjB7+UeRYy/QfXe\n" +
                "vn18KjJD3x674ANYmPvZ0SPsintwltf4PEtmAhilFt6zGLj/+VkkFHBCO0cfcYY0gKIbFHnofX2a\n" +
                "zeMg+/MBLpsFHo3VqslHlTVviwYuBYY4D/32fYCBVRmQSpp5+sxVf1+t82GrWEkUhtAbg/CB76ZD\n" +
                "EcOPCnUJafmdJIX7jO7zbE5J7U2kZFpvgZOZczaJ2FWVeln8a3hj/ih+x0TzokFwZ4DxeDPTR9Ft\n" +
                "ygapJczUYG6ELFD2yj3NA89ESKCKWMSjLNG7nRxeJzAPyeadm4pWKr6GtsQqCwx5YCHdew5zdavx\n" +
                "zCeiw2A2ohp9Qn4F1+gjMzbL5RbLwhlmilPOOZFHDL016K3OeYaHY3on6Be7E74VQoUrW2QHDZbV\n" +
                "5E69xW1Zy985Y/3Gs0viBe6PXj1lTdWcXyJiiudLQejjZmc/OmiRYlS7LX1RSOJDPcJGnjJe825j\n" +
                "KHuaXzqZCXhUqap4opS2kpAfT9eTnOWSk4xCo+7ZnP/c8eA9pQ7YZGi45AE9Q3k2fAKl//85jiVm\n" +
                "+7X/91uqzydYL6fbjxob/9V32LEBQ3Pd8hQVVv9kctrpS5GKhlOTgkAbP9Wkcy/GAzuSmbTBU3XS\n" +
                "6XtpEMIARWRICa834yS0b/Dv4QgAMAV4rsNRtFEvDk+OCwnF1r2MgxHXUPi1HNQdPy9ejDPkld+3\n" +
                "rBRwRMJBXScUuxs2TuhcPkDrI3u5+Moy8wChNDYDGFCNYfnXfw/tmxO4U+7P8U18FS9sP+CPNebZ\n" +
                "KAA2V/H1CiCdV5EcM5fo7RCi7sWCWZOBCM2uvU61udsc7FNk1M8pPchJ2+F9tvrTeSuvPr42P1pw\n" +
                "WDtNRc5dYi96tW2lqZ/SzH83P1zALkTwnpVG7p4Wi97mXHyDeIl1zau5e5KDLtm4FRbYTmchxqpp\n" +
                "MxKy7V0am5CB7LRunOTWNtf57KqRVZzziK+8e6c7OMBZCKMuEuEBuG13hUtKN1pM1a6d15X323Aq\n" +
                "Efn50wKbYnCFKxcQ0MIAyXmNutBw4iX6i4KsoA7Y7Gr+4z1nSu/76cyaAHgeUV109wjH7JT+PC0q\n" +
                "92B9vTbqkLr5j2krmF1fNSu+DaXKgcwpX208Ein5d2L3TaNuzkrTekV1/n3fFmt1k+JHnPpcK1vV\n" +
                "fXb5XQajuu3473NOnnVzk3+uKdpg/TnebNzvLi4n9w8Y+B50Wr+tGYVV+sTwpwCk38mlTGGXuCgj\n" +
                "cY2lxk52mGe/CRtdwKwF8FJGeOe6j4DP72NrDTAvFeXkCzTApDn3VsOstzg7U3/yb0/FiX7RQWdq\n" +
                "ZQgnjOES9UfJk7vFKXg7HqPTluADSE7tmIK+QSZoPj8f0MGgEpAThtPcrbf0u2e/MA48HRVJVPF2\n" +
                "CBjdZDh2WfRBj/N3heTkWh20cDl5dv772uLpOHxrjEvjP5NWM3lLB6lRHsSYXdRAYiZrWZT8vKy6\n" +
                "MfgidcEcl3HtsjzBHe+rMSHWflH8dZadLnaVV3/JE+E0LP5Rj+GGWLvzzUaHu51n+IoMjUxzpOB/\n" +
                "Jc5mmwmF5RVTu84xUtx+3MNxuI81WHKiHri9mcyYPdBzBiSRuY21Fc8IHhL3qz7KqGssa4WaTfrA\n" +
                "WiY8Or64MPN54xudXCENd3m18VFASgERj798E79XKWp5+dEdjFA+c7XQ6D6Egs1Wrcj126oJyOp0\n" +
                "GUxzA+gY+R7cUa2hXtOOPr8RAGy2CoIc8ugTv3ZeEOtmCLwlyMG6v0I5qZyIHuIKULrkPqDRRjko\n" +
                "zvc+ybC3MX0X8EdK7nCQ1qU+TQGyZ2tDOeNAs8oqqA3kU3wo2j1mZbY6hQEdIRHCctKxvnPtQ11i\n" +
                "ac967HavWANwR27d0uiPiWWAtrAsgxOmjj1kbjlFKoh1Zy9U6jialJmr2E1xPxCXwg6jYn1G57Zz\n" +
                "GunC1oW1mCipuY795OvRvrcQSXQYRqPwhICxyi5dM8MI1MpZqkpVdJk35P1Aii0LJIkSwFcbvs/M\n" +
                "/aHL+1bPIw5vSDgpP3f4VwlisUFE6N7fsLDHwEuamSo79hQErfKzmokC6PjMdcte6mBw5HZkMrAP\n" +
                "JUrBiLH1ScYxs8647gXGLx/U1kAT37zf1jv/lrwqhQZH5zY9WawHiMK9I/4KhkfBunDzYpFHqRTK\n" +
                "xF1sSBF8z4MU0BCOp8TdfhwSxvnjlanjiAK8J7qT86E2BIdlSMeiDmyGq/pwUhDvwCNfXYDjxWB4\n" +
                "HfPk5oaTXRLym0ntOq3RFatxHQBhnPNA4YbIblkA8T3RuyOoQhDczV7WSGyqoiT2xjNjtGCEKQ6A\n" +
                "gC6x02NL/HW/em3VoJRBUKrdCe2bbqLnBJAn7OZ4C2oxrsAKdJhib8x/CA9hVUxRaa0/SCzlwcPK\n" +
                "4QkWkLfbbOionpUHLtMaTTVao87Nwh1Xb5YbF444ezhFhIaUeq9lqFYELyVsZhHeJQmgKIXazjO0\n" +
                "E5h1hN7vBnTc0TAukxaUBpTgg3pAPk5L3W1KDWnsY2Kx1JkMt+GXueXGxeRdS8bczWlreFbnm5T9\n" +
                "nP7EYU5GHT4J59zUyBiFxicTwqOGJR2eW7yCv3xWA+3dgSVSnb6H8UXxPMm4ira1TFZ9D98FFGzf\n" +
                "Rcb3WSqUdcUu3RmjHsaCMbB06kmaYN42u47fTwYEUCnuB5Ha+6POTUBfmTGaWocfNexmejdk8oUW\n" +
                "FTTeTDT+ueYAcpFr/+hFxha/eublW9uwIZGdBj8lx8gjKwoxM5pk2VDlh6/clJwG/EhOj6rCgbni\n" +
                "m4L4U9SjvTBq+bjGUvCOgt3nErytyuHaXqoO3Iyyp6/tLM+xz2wMgtfHJIufbmB10RzzJffTFUbZ\n" +
                "1n1rUtiuWiUBd0kGhfWPHmDM3jkfrsA9wHv0ua6U0EN3uNucu9xjBFhJ8CxQFmcfZ2+pbKn8Bsew\n" +
                "KRiB9A/f0xUcCeF2cHf8T3tSVID/jg6A24gAmz1lBYjG0TA+CqL0aPi7YV3vVU6tDh8u/fhry+qQ\n" +
                "U47N12tPLruF+GY9rQ9sQUjrKH+M07GM0tQ4+a9jM9G3zM8IGPcID50sqBUph9gixKuW2Yw2Z9kj\n" +
                "Blr99bJRmxYN6OypzRirytFBD1QMclGqQgD+j+ZqXSXRCOGHZ/eMbR9BRNECUdd31DS8T1XDETDP\n" +
                "1m/QQ8EEABTpEAogJ7GQKA5py8u0o+jDi4dJYiTP71O+hvkvwQAsyAvRAMEPSi7+o5UU+j08lH3k\n" +
                "dGi3jJKMAThpCVIiQoq6+XjDy+kyBo60qfN/6sTAEspsC+6Re02kxSO37PZwf4Cg6LsdNNx133Vt\n" +
                "uKG6eGCbY17UC2jIL/PkfA+hTYS1QpMz0NnJHEs+sp4hpj0J0ZI4HV9qywSigdEu/PmxBd+C9RuE\n" +
                "XEnS7uxYOn8nyTxTFR4qUihxvuxA5JG0OsW/tdEssR52zFrajkOxZVzPAPXtCTLdEgi0K0ftxOYe\n" +
                "bafD24FoOebvRTFwA0AS02TkmXHFAPejIYoFd97f0vUoV5cuocJB5hUyKKKlU+hCwPQGFrmDll2+\n" +
                "/+NJNtTw7MrCsRUmEQHecKfbb39DvC73cZvHDmXfqh2m+8cLjbJW2TAON1miCnIqOWsgyH+/9JSI\n" +
                "QH+IrqDqLABJujzjR6Srd86BayuRODgsOBJlZaRekSGD9t5po/8ZXZcFk4X1l7pHg4BOlGh7AsKI\n" +
                "HpeWoZx4YUZlTtRLEEHff12SkWF0T/yQZk9ZtlIJx+Qu3YclUCKwmRtQKvR4gwiHbUDknoxNLDSN\n" +
                "HVw1wL0+LJ0aXANGWJl0FuslXaDlpIr9Fv9wjwrrBs+WBk+mk73uYAZMBQJYGoniJ8u9GCt0kzrY\n" +
                "RQsqjQPMuMYGHMSDOLW78zhfanYRzaQo41iViQJ3vV9lZo7JyxwjyFBpy2mLj8+phIytBViywXL9\n" +
                "iuWBD8ySdHTW3LW6wOkHJtt86Z+YoHe4/2Dp7yvzopVqX+4RZzzd/StqD2fY0sa1U0wwv2kQK1F2\n" +
                "TDM2cb219VYMrWYIqhbxYSHrPRkBQYmdzjfZboB6MHohiCu2xSjCUQJxdhWmxLkFScLd5kRnaOd2\n" +
                "S/Zc68K9KGI42J/GoVB+x08ibBSnn0YX0xwSh8zW2AopOJDFBwT3y6GO/im6fJzDD3AhVaIkUb5e\n" +
                "0jgLsUJGotwJGvqdi5RCkogZpQm+4NyxPLcJP80XNsj/i+i7AOHbgH1MI+DHmvTebEmLYhrbigun\n" +
                "e9sFKK0A2URQRJJcXjpY70DC7E5Rg9Cjda0UB1wQ2OB/NL20z11FFFzDTfrzcLivpTuJ9L54OeY/\n" +
                "IrXaDnX6y6eah4ZoEG+oAdsJg8mjNPxix4N7/DuU6hvsmfFNyglOYys7jGHcMR/fMx4noXTJ1IM/\n" +
                "zehQ7cCgI/GLtX5oosSK0lMwlHM8KfI0C4CfvgchMvMTtpV7CV9hFf+n1im3UoXIFhNu9KzKTDsr\n" +
                "Lau0gcAIHqE3T8pDhv84CmDUPkch87eCQicOCsHWKxZtVLt3gqz+bxa/sRjJAU9L59Mq0vZCvUKr\n" +
                "A+asV8mUsFMB/h76uVJS0xftW8UgL5zKnnWx71LZKrE7oFrqwwwOmsSNtzA9TUbRz3XPTFq1uwBA\n" +
                "PjhK8sotDUWPMWgIBfMUoMg6hIXHkyJRLB32StLJEIVr4aI7lkAtLyDNryR+HZfuBngbYKNsDKmH\n" +
                "HCYwQ83ayQasFtji8sF+ivlyG/De3fyu1Qf2e7Jpa5fHIBAf9DzNja1aZRMCwii7fCRRZgtmIM+M\n" +
                "jrimlPgYAuVePj/YkernoDPMDG4Ungb3Kx8d82Z9qLvl7SUeseuhguQWBFh3RwUapsggKd8Hxmdl\n" +
                "Ln/HJK9kh591QznXX7J7b6qNaurV/uyncIoee050e+BaxuPpVFTBxlANQ54ErGSDFYevRGTa6rNM\n" +
                "BJFoSReKrJ8GWj9OCHguKKUOExgh5u1kg6vLiOkC4b/XSdjEP508Oyti2orD+rT5oqni2n/yj644\n" +
                "8Nt28jvYiUixN4mLft3rhnVM4nIBoRQW6QT9keFucd4Lru2CH1hBITRiH5R1siya5jzLqOoUWf2y\n" +
                "G/TBZxqjeJfD+kaEKfP4Dt3Fax8fYoQCMoWCzFyvrTFCVZ35p8/aEdq74jO7uEp/Hoa9SejMmXEB\n" +
                "DMP6xoie2xAuXnTmlRkbSrRJwS0DDG5o4VifRqlkowgMRvlAlmuq6vo0LfOh24d8slkIfm2Ae/OJ\n" +
                "97IONAdWOu4SHnECmVc8ESn3pfMou2DgxFU8W4gxE2zHthnO4bv+sotx+FfC7iCccc1DYZ/j/O/7\n" +
                "g6IPLPdO1MsaXkHF6Ot6VN5WXSC0V6Lu4L/NJE6/Zw8TQ8u44qaDRdvkqqkI3bRQKqDY7oKWRbrT\n" +
                "37452uXDtBmyxJbOYjHP5ZZ8+o2pNSDxci1hheOsted2+XVGEhWUC9MZkUVWEpTbhUGifigkvxn+\n" +
                "WgLv0zMFP+M7am0wstporxyY6qIgc7eZ23MveMPlQgQEAYWZQkxPACocLVOz20u/xN19j4Fsi31N\n" +
                "h2PBz8CeKW+fcnfRNjCztVjyxp6KQLMd0HqyFwPpE2YnxAe3mbYowMy/ngJE5Q5KuWDTvBfKGDfH\n" +
                "E4dAGsIiNEVcXRF6gUXnbOghFjk5YmDoEkShd7CuRsJ1+nxCx27RM5EHJ8klFWsc60DJMSqBFLhs\n" +
                "Y6xSkI6iIHgRrrS75aCghwxssS2ZD6YLnUbtO2W0NyXiE2U8IE4zb1WApKsquii0qOu/B+CRCCOI\n" +
                "3Ilxg1tJncYOvANREZuSR0zoyzT1/zS2lGgemwyojpdIu8To8KQ/UDpaIlMCkf8sCqmeHOrL5QSM\n" +
                "Kvr7TKRcBEnZV7wjsk5qNykJY7nGm+B500TYQ3p9z6dJ92TM6Glz9RFXsUfXSeOA7nRLGUNLTDv4\n" +
                "KCkmIi8xunQfhcBoMwD1JNNQvhMvd6j3YgsQ42EggVtVLtSaGawyLhL7RdquTLsowDjuzkFrABPa\n" +
                "vMxvbxHv0F94Q6k5kwo3smGaLYm1VpZ/hULDtvsQeuV7LGMHd2ACQX4xDnBqtrkhZxdQQbbjDyK8\n" +
                "bUft6mDoOZ8bmlPGxty8IBD+qo9/tERd+XcaW8oMvXEfAleKAAJwJFKiAAAAAA==";
    }

    public void generate(View view) {
        if(!switchView.isChecked()){
            receiptEdit.setText( generateStringReceipt());
        }else{
            receiptEdit.setText(generateBase64());
        }
    }

    // Método auxiliar para gerar um número long dentro de um intervalo
    private long randomLong(long min, long max) {
        Random random = new Random();
        return min + (long) (random.nextDouble() * (max - min));
    }

    private PaymentClient getPaymentProviderApi(){
        if(mPaymentClient==null){
            mPaymentClient = new PaymentClient();
        }
        return mPaymentClient;
    }


    public void showImageDialog(Context context, String base64Image) {
        // Step 1: Decode Base64 string to Bitmap
        byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

        // Step 2: Create an ImageView and set the Bitmap
        ImageView imageView = new ImageView(context);
        imageView.setImageBitmap(bitmap);
        imageView.setAdjustViewBounds(true); // Allow the image to adjust its size
        imageView.setPadding(20, 20, 20, 20); // Optional padding for better appearance

        // Step 3: Create and show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Image Preview"); // Optional title
        builder.setView(imageView); // Set the ImageView as the content
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss()); // Add an OK button

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public interface OnPositiveClickListener {
        void onPositiveClick(String input);
    }
}
