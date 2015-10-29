package com.speedata.nrfUARTv2;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.speedata.nrfUARTv2.R;
import com.speedata.nrfUARTv2.fragment.BLETestFragment;
import com.speedata.nrfUARTv2.fragment.My433TestFragment;
import com.speedata.nrfUARTv2.myinterface.OnFragmentInteractionListener;
import com.speedata.nrfUARTv2.utils.DataConversionUtils;

import java.util.zip.Inflater;

import static com.speedata.nrfUARTv2.R.id.container;

public class MenuHomeActivity extends BaseActivity implements View.OnClickListener,
        OnFragmentInteractionListener {

    private TextView actionBle, action433;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fm = getSupportFragmentManager();
        initUI();
    }

    private void initUI() {
        setContentView(R.layout.activity_menu_home);
        action433 = (TextView) findViewById(R.id.action_433);
        actionBle = (TextView) findViewById(R.id.action_ble);
        action433.setOnClickListener(this);
        actionBle.setOnClickListener(this);
        showFragment(1);
        actionBle.setBackgroundColor(Color.GRAY);
        action433.setBackgroundColor(Color.BLACK);
    }

    @Override
    public void onClick(View v) {
        if (v == actionBle) {
            showFragment(1);
            actionBle.setBackgroundColor(Color.GRAY);
            action433.setBackgroundColor(Color.BLACK);
        } else if (v == action433) {
            showFragment(2);
            action433.setBackgroundColor(Color.GRAY);
            actionBle.setBackgroundColor(Color.BLACK);
        }

    }

    private BLETestFragment bleTestFragment;// = new BLETestFragment();
    private My433TestFragment my433TestFragment;// = new My433TestFragment();

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private FragmentManager fm;

    public void showFragment(int index) {
        FragmentTransaction ft = fm.beginTransaction();
        // 想要显示一个fragment,先隐藏所有fragment，防止重叠
        hideFragments(ft);
        switch (index) {
            case 1:
                // 如果fragment1已经存在则将其显示出来
                if (bleTestFragment != null)
                    ft.show(bleTestFragment);
                    // 否则是第一次切换则添加fragment1，注意添加后是会显示出来的，replace方法也是先remove后add
                else {
                    bleTestFragment = new BLETestFragment();
                    ft.add(R.id.container, bleTestFragment);
                }
                break;
            case 2:
                if (my433TestFragment != null)
                    ft.show(my433TestFragment);
                else {
                    my433TestFragment = new My433TestFragment();
                    ft.add(R.id.container, my433TestFragment);
                }
                break;
        }
        ft.commit();
    }

    // 当fragment已被实例化，就隐藏起来
    public void hideFragments(FragmentTransaction ft) {
        if (bleTestFragment != null)
            ft.hide(bleTestFragment);
        if (my433TestFragment != null)
            ft.hide(my433TestFragment);
    }
}
