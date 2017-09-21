package br.com.phoebus.payments.demo.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import br.com.phoebus.payments.demo.R;

/**
 * Created by joao.gabriel on 12/05/2017.
 */

public abstract class FragmentUtils {

    public static final void showFragment(AppCompatActivity activity, Fragment fragment) {
        showFragment(activity, fragment, false, R.id.container);
    }

    public static final void showFragment(AppCompatActivity activity, Fragment fragment, boolean addToBackStack) {
        showFragment(activity, fragment, addToBackStack, R.id.container);
    }

    public static final void showFragment(AppCompatActivity activity, Fragment fragment, boolean addToBackStack, int containerId) {
        FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(containerId, fragment);
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(fragment.getClass().getSimpleName());
        }
        fragmentTransaction.commit();
    }
}
