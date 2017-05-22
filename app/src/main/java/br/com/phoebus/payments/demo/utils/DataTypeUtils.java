package br.com.phoebus.payments.demo.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by andre.figueiredo on 24/02/2017.
 */

public class DataTypeUtils {

    public static final String DATA_TIME_SIMPLE = "dd/MM/yy HH:mm:ss";
    public static final String VALUE_FORMAT = "##.##";

    private DataTypeUtils() {
        // Utility class.
    }

    public static String getAsString(Date dataHora) {
        if (dataHora == null) return "";

        DateFormat df = new SimpleDateFormat(DATA_TIME_SIMPLE);

        return df.format(dataHora);
    }

    public static String getAsString(Integer integer) {
        if (integer == null) return "";

        return integer.toString();
    }

    public static String getMoneyAsString(BigDecimal bigDecimal) {
        if (bigDecimal == null) return "";

        return NumberFormat.getCurrencyInstance().format(bigDecimal);
    }

    public static String getAsString(Enum aEnum) {
        if (aEnum == null) return "";

        return aEnum.toString();
    }

    public static BigDecimal getFromString(String s) {
        if (s == null || "".equals(s)) return null;

        try {
            DecimalFormat nf = new DecimalFormat(VALUE_FORMAT);
            nf.setParseBigDecimal(true);
            return  (BigDecimal) nf.parse(s);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String getAsString(float num) {
        DecimalFormat df = new DecimalFormat(VALUE_FORMAT);
        df.setRoundingMode(RoundingMode.HALF_DOWN);
        return  df.format(num);
    }
}
