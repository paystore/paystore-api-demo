package br.com.phoebus.payments.demo.utils;

import android.util.Log;
import java.util.Date;

public class LogUtils {

	public static final String CATEGORY = "DEMO_LOG";

	private static String getLogMessage(Object currentClass, String methodName, String msg) {
	    String date = DataTypeUtils.getAsString(new Date());
	    String className = currentClass.getClass().getSimpleName();

        return date + " Class: " + className + " Metodo: " + methodName + " detalhe: " + msg;
    }

    public static void writeLogCat(Object classCurrent, String methodName, String msg) {
        Log.d(CATEGORY, getLogMessage(classCurrent, methodName, msg));
	}

	public static void writeLogCatE(Object classCurrent, String methodName, String msg) {
        Log.e(CATEGORY, getLogMessage(classCurrent, methodName, msg));
	}
}
