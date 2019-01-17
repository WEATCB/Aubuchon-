package com.aubuchon;

import android.Manifest;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.aubuchon.scanner.ItemDetailFragment;
import com.aubuchon.scanner.ScannerActivity;
import com.aubuchon.utility.Constant;
import com.aubuchon.utility.Globals;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class NavigationActivity extends AppCompatActivity {

    private static final String BACK_STACK_ROOT_TAG = "root_fragment";

    public static final int SCAN_BARCODE_REQUEST = 1002;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.toolbar_title)
    public TextView toolbar_title;
    @BindView(R.id.iv_home)
    AppCompatImageView iv_home;
    @BindView(R.id.tv_desc)
    public TextView tv_desc;
    @BindView(R.id.tv_more)
    public TextView tv_more;
    @BindView(R.id.ll_desc)
    public LinearLayout ll_desc;

    String scannedCode = "";
    Globals globals;

    public NavigationActivity navigationActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        ButterKnife.bind(this);
        navigationActivity = NavigationActivity.this;
        globals = (Globals) getApplicationContext();
        addFragmentOnTop(HomeFragment.newInstance());
    }

    public void setToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            // Handle Navigation Option Click
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showFilterPopup();
                }
            });

            // Handle Barcode Image (Top Right) Click
            iv_home.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    PermissionListener permissionlistener = new PermissionListener() {
                        @Override
                        public void onPermissionGranted() {
                            Intent intent = new Intent(NavigationActivity.this, ScannerActivity.class);
                            startActivityForResult(intent, SCAN_BARCODE_REQUEST);
                        }

                        @Override
                        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                            Toast.makeText(NavigationActivity.this, getString(R.string.permission_denied) + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                        }
                    };

                    TedPermission.with(NavigationActivity.this)
                            .setPermissionListener(permissionlistener)
                            //.setRationaleMessage(getString(R.string.request_camera_permission))
                            .setDeniedMessage(getString(R.string.on_denied_permission))
                            .setGotoSettingButtonText(getString(R.string.setting))
                            .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .check();

                }
            });
        }
    }

    private boolean isHomeFragment() {
        List<Fragment> frags = getSupportFragmentManager().getFragments();
        for (Fragment f : frags) {
            return f instanceof HomeFragment;
        }
        return false;
    }

    /**
     * Add a fragment on top of the current tab
     */
    public void addFragmentOnTop(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    private void showFilterPopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.layout_popup_window, null);
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAtLocation(toolbar, Gravity.TOP | Gravity.START, 10, Globals.getStatusBarHeight());

        TextView tv_popup_home = popupView.findViewById(R.id.tv_popup_home);
        TextView tv_popup_product_info = popupView.findViewById(R.id.tv_popup_product_info);

        tv_popup_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isHomeFragment()) {
                    getSupportFragmentManager().popBackStack(BACK_STACK_ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    addFragmentOnTop(HomeFragment.newInstance());

                    toolbar_title.setText("");
                    ll_desc.setVisibility(View.GONE);
                }

                popupWindow.dismiss();
            }
        });

        tv_popup_product_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (globals.getPreviousProductCode() != null && !globals.getPreviousProductCode().isEmpty()) {
                    List<Fragment> frags = getSupportFragmentManager().getFragments();
                    for (Fragment f : frags) {
                        if (f instanceof HomeFragment) {
                            globals.isFromMenu = true;
                            addFragmentOnTop(ItemDetailFragment.newInstance(globals.getCurrentProductCode()));
                        } else if (f instanceof ItemDetailFragment) {
                            globals.isFromMenu = true;
                            addFragmentOnTop(ItemDetailFragment.newInstance(globals.getPreviousProductCode()));
                        }
                    }

                }

                popupWindow.dismiss();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Pop off everything up to and including the current tab
        //clear toolbar

        List<Fragment> frags = getSupportFragmentManager().getFragments();
        for (Fragment f : frags) {
            if (f instanceof HomeFragment) {
                toolbar_title.setText("");
                ll_desc.setVisibility(View.GONE);
                //finish();
                super.onBackPressed();
            } else {
                addFragmentOnTop(HomeFragment.newInstance());
                toolbar_title.setText("");
                ll_desc.setVisibility(View.GONE);
            }
        }
    }


    // Handle Result come from Bar-code Image at Top-Right Corner
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SCAN_BARCODE_REQUEST && data != null) {
            scannedCode = data.getExtras().getString(Constant.AU_Data);
            toolbar_title.setText(String.format(getString(R.string.text_sku), scannedCode));
            globals.isFromMenu = false;
            addFragmentOnTop(ItemDetailFragment.newInstance(scannedCode));

        }
    }

}