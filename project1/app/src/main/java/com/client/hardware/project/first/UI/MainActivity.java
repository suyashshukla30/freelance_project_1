package com.client.hardware.project.first.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;

import com.client.hardware.project.first.ADAPTER.ProductAdapter;
import com.client.hardware.project.first.MODEL.Product;
import com.client.hardware.project.first.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ProductAdapter productAdapter;
    SearchView data_search_bar;
    RecyclerView recyclerView;
    FirebaseDatabase data_base_ref;
    DatabaseReference databaseReference;
    List<Product> productList;
    ImageView iv_filter_icn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
        recyclerView = findViewById(R.id.rv_all_data);
        data_search_bar = findViewById(R.id.sv_item);
        iv_filter_icn = findViewById(R.id.iv_filter_list);
        data_base_ref = FirebaseDatabase.getInstance();
        databaseReference = data_base_ref.getReference().child("products");
        productList = new ArrayList<>();
        if (Prefs.getBoolean("is_admin", false)) {
            Prefs.putBoolean("is_admin", true);
            startActivity(new Intent(this, admin_add_data.class));
        }
        fetch_Data_from_firebase();
        data_search_bar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                productAdapter.getFilter().filter(newText);
                return false;
            }
        });
        iv_filter_icn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFilter();
            }
        });
    }

    private void openFilter() {
        List<String> item_quality_list = new ArrayList<>();
        List<String> item_dimension_list = new ArrayList<>();
        BottomSheetDialog item_quality_bottom_sheet_dialog = new BottomSheetDialog(this);
        item_quality_bottom_sheet_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        item_quality_bottom_sheet_dialog.setContentView(R.layout.bottom_sheet_filter);

        for (Product product : productList) {
            if (!item_quality_list.contains(product.getQuality())) {
                item_quality_list.add(product.getQuality());
            }
        }

        for (Product product : productList) {
            if (!item_dimension_list.contains(product.getDimensions())) {
                item_dimension_list.add(product.getDimensions());
            }
        }

        Spinner item_quality = item_quality_bottom_sheet_dialog.findViewById(R.id.spinnerQuality);
        Spinner item_dimension = item_quality_bottom_sheet_dialog.findViewById(R.id.spinnerDimension);

        ArrayAdapter<String> qualityAdapter = new ArrayAdapter<>(this, R.layout.bottlom_layout_filter, R.id.custom_text_view, item_quality_list);
        ArrayAdapter<String> dimensionAdapter = new ArrayAdapter<>(this, R.layout.bottlom_layout_filter, R.id.custom_text_view, item_dimension_list);

        item_quality.setAdapter(qualityAdapter);
        item_dimension.setAdapter(dimensionAdapter);

        Button apply_filter = item_quality_bottom_sheet_dialog.findViewById(R.id.btnApplyFilter);
        Button clear_filter = item_quality_bottom_sheet_dialog.findViewById(R.id.btn_clr_filter);


        apply_filter.setOnClickListener(view -> {
            String selectedQuality = item_quality.getSelectedItem().toString();
            String selectedDimension = item_dimension.getSelectedItem().toString();
            productAdapter.applyFilter(selectedQuality, selectedDimension);
            item_quality_bottom_sheet_dialog.dismiss();
        });
        clear_filter.setOnClickListener(view -> {
            productAdapter.clearFilters();
            item_quality_bottom_sheet_dialog.dismiss();

        });
        item_quality_bottom_sheet_dialog.show();
    }

    private void fetch_Data_from_firebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Product product = new Product(productSnapshot.getKey(),
                            productSnapshot.child("productName").getValue().toString(),
                            productSnapshot.child("quality").getValue().toString(),
                            productSnapshot.child("dimensions").getValue().toString(),
                            productSnapshot.child("pricePerUnit").getValue().toString(),
                            productSnapshot.child("imageUrl").getValue().toString());
//                    Product product = productSnapshot.getValue(Product.class);
                    productList.add(product);
                }
                productAdapter = new ProductAdapter(productList);
                recyclerView.setAdapter(productAdapter);
                recyclerView.setLayoutManager(
                        new LinearLayoutManager(MainActivity.this));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

    }
}