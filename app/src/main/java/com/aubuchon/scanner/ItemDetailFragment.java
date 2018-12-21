package com.aubuchon.scanner;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ViewFlipper;

import com.aubuchon.R;
import com.aubuchon.apis.PostWithRequestParam;
import com.aubuchon.model.KeyValueModel;
import com.aubuchon.model.ProductDetailModel;
import com.aubuchon.model.ProductDetailModel.Product;
import com.aubuchon.utility.Globals;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ItemDetailFragment extends Fragment implements OnCheckedChangeListener, OnItemClickListener {

    @BindView(R.id.radio_group_1)
    RadioGroup rg1;
    @BindView(R.id.radio_group_2)
    RadioGroup rg2;
    @BindView(R.id.view_flipper)
    ViewFlipper view_flipper;
    @BindView(R.id.rv_local_inventory)
    RecyclerView rv_local_inventory;
    @BindView(R.id.rv_inquiry)
    RecyclerView rv_inquiry;
    @BindView(R.id.rv_sales_history_store)
    RecyclerView rv_sales_history_store;
    @BindView(R.id.rv_sales_history_company)
    RecyclerView rv_sales_history_company;
    @BindView(R.id.rv_order_info)
    RecyclerView rv_order_info;
    @BindView(R.id.tv_no_data)
    AppCompatTextView tvNoData;
    @BindView(R.id.iv_photo)
    AppCompatImageView iv_photo;

    Globals globals;
    ArrayList<ProductDetailModel.Product> productArrayList;
    ProductDetailModel productDetailModel;
    private LocalInvListAdapter localInvListAdapter;
    String data;

    public static ItemDetailFragment newInstance() {
        return new ItemDetailFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        globals = (Globals) getActivity().getApplicationContext();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_item_detail, container, false);
        ButterKnife.bind(this, view);
        rg1.clearCheck(); // this is so we can start fresh, with no selection on both RadioGroups
        rg2.clearCheck();
        rg1.setOnCheckedChangeListener(this);
        rg2.setOnCheckedChangeListener(this);
        rg1.check(R.id.btn_inquiry);
        productArrayList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            productArrayList.add(new Product());
        }
        data = globals.getCurrentProductCode();

        doRequestForGetProductDetail();
        setAdapter();

        return view;
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        switch (radioGroup.getId()) {
            case R.id.radio_group_1:
                if (checkedId != -1) {
                    rg2.setOnCheckedChangeListener(null); // remove the listener before clearing so we don't throw that stackoverflow exception(like Vladimir Volodin pointed out)
                    rg2.clearCheck(); // clear the second RadioGroup!
                    rg2.setOnCheckedChangeListener(this); //reset the listener

                    switch (checkedId) {
                        case R.id.btn_inquiry:
                            view_flipper.setDisplayedChild(0);
                            //setInquiryData();
                            break;


                        case R.id.btn_sales_history:
                            view_flipper.setDisplayedChild(4);
                            setSalesHistoryData();
                            break;
                        case R.id.btn_order_info:
                            view_flipper.setDisplayedChild(3);
                            setOrderInfoData();
                            break;
                        case R.id.btn_related_items:
                            view_flipper.setDisplayedChild(5);
                            break;
                    }
                }
                break;
            case R.id.radio_group_2:
                if (checkedId != -1) {
                    rg1.setOnCheckedChangeListener(null);
                    rg1.clearCheck();
                    rg1.setOnCheckedChangeListener(this);

                    switch (checkedId) {

                        case R.id.btn_local:
                            view_flipper.setDisplayedChild(2);
                            break;
                        case R.id.btn_photo:
                            view_flipper.setDisplayedChild(1);
                            break;


                    }
                }
                break;
        }
    }

    public void doRequestForGetProductDetail() {
        String url = "http://50.206.125.135/webservice/GetProductStore.aspx?branchcode=1&data=" + data;
        // RequestParams param = HttpRequestHandler.getInstance().getProductDetailParams("001", "129991");
        new PostWithRequestParam(getActivity(), url, new RequestParams(), true, new PostWithRequestParam.OnPostWithReqParamServiceCallListener() {
            @Override
            public void onSucceedToPostCall(JSONObject response) {
                productDetailModel = new Gson().fromJson(response.toString(), new TypeToken<ProductDetailModel>() {
                }.getType());
                if (productDetailModel.getProduct().size() > 0)
                    setInquiryData();
                else {
                    Globals.showToast(getActivity(), "No data available.");
                    //finish();
                }

            }

            @Override
            public void onFailedToPostCall(int statusCode, String msg) {
                Globals.showToast(getActivity(), msg);
                //  finish();
            }

        }).execute();
    }

    private void setAdapter() {

        if (productArrayList != null && productArrayList.size() > 0) {
            if (localInvListAdapter == null) {
                localInvListAdapter = new LocalInvListAdapter(getActivity());
                localInvListAdapter.setOnItemClickListener(this);
            }

            localInvListAdapter.doRefresh(productArrayList);

            if (rv_local_inventory.getAdapter() == null) {
                rv_local_inventory.setHasFixedSize(false);
                rv_local_inventory.setLayoutManager(new LinearLayoutManager(getActivity()));
                rv_local_inventory.setAdapter(localInvListAdapter);
            }
        }
        handleEmptyList();
    }

    public void handleEmptyList() {

        if (productArrayList == null || productArrayList.isEmpty()) {
            rv_local_inventory.setVisibility(View.GONE);
            tvNoData.setVisibility(View.VISIBLE);

        } else {
            rv_local_inventory.setVisibility(View.VISIBLE);
            tvNoData.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    public void setInquiryData() {
        ArrayList<KeyValueModel> inquiryData = new ArrayList<>();
        for (Field item : productDetailModel.getProduct().get(0).getClass().getDeclaredFields()) {
            try {
                item.setAccessible(true);
                if (!item.getName().equalsIgnoreCase("imageURL"))
                    if (item.get(productDetailModel.getProduct().get(0)) != null)
                        inquiryData.add(new KeyValueModel(item.getName(), item.get(productDetailModel.getProduct().get(0)).toString()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

       /* inquiryData.add(new KeyValueModel("Item Number", "129991"));
        inquiryData.add(new KeyValueModel("Desc", "Pellet Green Supreme"));
        inquiryData.add(new KeyValueModel("Price", "6.99"));
        inquiryData.add(new KeyValueModel("Promo", "50/259.99"));
        inquiryData.add(new KeyValueModel("OH", "250.00"));
        inquiryData.add(new KeyValueModel("Available", "250.00"));
        inquiryData.add(new KeyValueModel("Section", "F030-002F"));
        inquiryData.add(new KeyValueModel("Speed#", "10"));*/

        InquiryListAdapter inquiryListAdapter = new InquiryListAdapter(getActivity());
        inquiryListAdapter.doRefresh(inquiryData);
        if (rv_inquiry.getAdapter() == null) {
            rv_inquiry.setHasFixedSize(false);
            rv_inquiry.setLayoutManager(new LinearLayoutManager(getActivity()));
            rv_inquiry.setAdapter(inquiryListAdapter);
        }

      /*  GlideApp.with(getActivity())
                .load("https://awakenedmind.com/AwakenedmindAPI/Content/CategoryHeaderImage/metting4.png"*//*productDetailModel.getProduct().get(0).getImageURL()*//*)
                .into(iv_photo);*/
    }

    public void setOrderInfoData() {
        ArrayList<KeyValueModel> inquiryData = new ArrayList<>();
        inquiryData.add(new KeyValueModel("Last Sold", "09/26/18"));
        inquiryData.add(new KeyValueModel("QTY on Order", "1500.00"));
        inquiryData.add(new KeyValueModel("PO Number", "123456789"));
        inquiryData.add(new KeyValueModel("Primary Vendor", "Lignetics of NE"));
        inquiryData.add(new KeyValueModel("Vendor#", "9961"));
        inquiryData.add(new KeyValueModel("Delivery Date", "11/30/18"));


        InquiryListAdapter inquiryListAdapter = new InquiryListAdapter(getActivity());
        inquiryListAdapter.doRefresh(inquiryData);
        if (rv_order_info.getAdapter() == null) {
            rv_order_info.setHasFixedSize(false);
            rv_order_info.setLayoutManager(new LinearLayoutManager(getActivity()));
            rv_order_info.setAdapter(inquiryListAdapter);

        }
    }

    public void setSalesHistoryData() {
        ArrayList<KeyValueModel> storeData = new ArrayList<>();
        storeData.add(new KeyValueModel("Nov", "250"));
        storeData.add(new KeyValueModel("Oct", "122"));
        storeData.add(new KeyValueModel("Sep", "036"));
        storeData.add(new KeyValueModel("Aug", "002"));
        storeData.add(new KeyValueModel("Jul", "000"));
        storeData.add(new KeyValueModel("Dec 2017", "800"));
        storeData.add(new KeyValueModel("Jan 2017", "878"));

        SalesHistoryListAdapter storeListAdapter = new SalesHistoryListAdapter(getActivity());
        storeListAdapter.doRefresh(storeData);
        if (rv_sales_history_store.getAdapter() == null) {
            rv_sales_history_store.setHasFixedSize(false);
            rv_sales_history_store.setLayoutManager(new LinearLayoutManager(getActivity()));
            rv_sales_history_store.setAdapter(storeListAdapter);

        }

        ArrayList<KeyValueModel> companyData = new ArrayList<>();
        companyData.add(new KeyValueModel("Nov", "5000"));
        companyData.add(new KeyValueModel("Oct", "1220"));
        companyData.add(new KeyValueModel("Sep", "0840"));
        companyData.add(new KeyValueModel("Aug", "0098"));
        companyData.add(new KeyValueModel("Jul", "0006"));
        companyData.add(new KeyValueModel("Dec 2017", "8999"));
        companyData.add(new KeyValueModel("Jan 2017", "9789"));
        SalesHistoryListAdapter companyListAdapter = new SalesHistoryListAdapter(getActivity());
        companyListAdapter.doRefresh(companyData);
        if (rv_sales_history_company.getAdapter() == null) {
            rv_sales_history_company.setHasFixedSize(false);
            rv_sales_history_company.setLayoutManager(new LinearLayoutManager(getActivity()));
            rv_sales_history_company.setAdapter(companyListAdapter);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                //finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}