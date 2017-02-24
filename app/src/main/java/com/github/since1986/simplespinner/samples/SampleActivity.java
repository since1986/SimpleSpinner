package com.github.since1986.simplespinner.samples;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.github.since1986.simplespinner.BaseSpinner;
import com.github.since1986.simplespinner.OnDropdownDismissListener;
import com.github.since1986.simplespinner.OnDropdownShowListener;
import com.github.since1986.simplespinner.OnValueChangeListener;
import com.github.since1986.simplespinner.SimpleSpinner;

public class SampleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        //spinner1
        SimpleSpinner spinner1 = (SimpleSpinner) findViewById(R.id.spinner1);
        spinner1.bindData("测试选项1", "测试选项2", "加长的测试选项", "超长的测试选项1234567890");
        spinner1.setOnDropdownShowListener(new OnDropdownShowListener() {
            @Override
            public void onDropdownShow() {
                Toast.makeText(SampleActivity.this, "已打开下拉窗", Toast.LENGTH_SHORT).show();
            }
        });
        spinner1.setOnDropdownDismissListener(new OnDropdownDismissListener() {
            @Override
            public void onDropdownDismiss() {
                Toast.makeText(SampleActivity.this, "已关闭下拉窗", Toast.LENGTH_SHORT).show();
            }
        });

        //spinner2
        SimpleSpinner spinner2 = (SimpleSpinner) findViewById(R.id.spinner2);
        spinner2.bindData("测试选项1", "测试选项2", "测试选项3", "测试选项4", "测试选项5", "测试选项6", "测试选项7", "测试选项8", "测试选项9");
        spinner2.select(2);
        spinner2.setOnValueChangeListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(View view, CharSequence originalValue, CharSequence newValue) {
                Toast.makeText(SampleActivity.this, "originalValue: " + originalValue + " newValue: " + newValue, Toast.LENGTH_LONG).show();
            }
        });

        //spinner3
        final BaseSpinner spinner3 = (BaseSpinner) findViewById(R.id.spinner3);
        spinner3.setHint("Hint3");
        final View spinner3DropdownContentView = LayoutInflater.from(this).inflate(R.layout.popup_window_spinner3, null);

        final GridView gridView = (GridView) spinner3DropdownContentView.findViewById(R.id.grid_view_popup_window_spinner3);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item);
        adapter.addAll("筛选项目1", "筛选项目2");
        gridView.setAdapter(adapter);

        Button buttonReset = (Button) spinner3DropdownContentView.findViewById(R.id.button_reset_popup_window_spinner3);
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SampleActivity.this, "reset", Toast.LENGTH_SHORT).show();
            }
        });

        Button buttonConfirm = (Button) spinner3DropdownContentView.findViewById(R.id.button_confirm_popup_window_spinner3);
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //你的业务逻辑处理
                Toast.makeText(SampleActivity.this, "confirm", Toast.LENGTH_SHORT).show();
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        spinner3.dismissDropdownPopupWindow();
                    }
                });
            }
        });
        spinner3.setDropdownPopupWindowContentView(spinner3DropdownContentView);
    }
}
