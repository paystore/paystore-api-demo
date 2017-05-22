package br.com.phoebus.payments.demo.utils;

import android.content.pm.PackageManager;

import br.com.phoebus.android.payments.api.ApplicationInfo;
import br.com.phoebus.android.payments.api.Credentials;

public class CredentialsUtils {

    public static final String TEST_APPLICATION_ID = "0";
    public static final String TEST_SECRET_TOKEN = "00000000000000000000";

    public static Credentials getMyCredentials() {
        Credentials credentials = new Credentials();
        credentials.setApplicationId(TEST_APPLICATION_ID);
        credentials.setSecretToken(TEST_SECRET_TOKEN);
        return credentials;
    }

    public static ApplicationInfo getMyAppInfo(PackageManager packageManager, String packageName) throws PackageManager.NameNotFoundException {

        ApplicationInfo applicationInfo = new ApplicationInfo();
        applicationInfo.setCredentials(getMyCredentials());
        applicationInfo.setSoftwareVersion(packageManager.getPackageInfo(packageName, 0).versionName);

        return applicationInfo;
    }
}
