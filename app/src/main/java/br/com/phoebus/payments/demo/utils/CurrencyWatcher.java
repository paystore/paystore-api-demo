package br.com.phoebus.payments.demo.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by thalles.rafael on 16/07/2021.
 */

public class CurrencyWatcher implements TextWatcher {
    private boolean mWasEdited = false;
    private NumberFormat sCurrencyFormatter;
    private EditText editText;

    public CurrencyWatcher(EditText editText, boolean showCurrencySymbol) {
        this.editText = editText;
        this.sCurrencyFormatter = getFormatter(showCurrencySymbol);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Nothing happens
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Nothing happens
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mWasEdited) {
            mWasEdited = false;
            return;
        }

        mWasEdited = true;
        String value = editText.getText().toString().replaceAll("[^0-9]", "");
        String formattedValue = sCurrencyFormatter.format(new BigDecimal(value).divide(new BigDecimal("100")));
        editText.setText(formattedValue);
        editText.setSelection(formattedValue.length());
    }

    public DecimalFormat getFormatter(boolean showCurrencySymbol) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getCurrencyInstance();

        if (showCurrencySymbol) {
            DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
            /*
             * Checks if currency symbol is prefix or suffix
             * I'm checking if symbol '¤' (164) starts the pattern, if true set new prefix
             */
            int currencySymbolPosition = formatter.toPattern().indexOf(164);
            if (currencySymbolPosition <= 0) {
                formatter.setPositivePrefix(symbols.getCurrencySymbol() + " ");
                formatter.setNegativePrefix(symbols.getCurrencySymbol() + " -");
            }
        } else {
            formatter.setNegativePrefix("");
            formatter.setPositivePrefix("");
        }
        return formatter;
    }
}
