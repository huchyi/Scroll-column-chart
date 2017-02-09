package com.scrollcolumnchart;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ColumnCharView columnCharView = (ColumnCharView) findViewById(R.id.view);
    columnCharView.setValue(2017,1);
  }
}
