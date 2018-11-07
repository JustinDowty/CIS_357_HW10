package com.example.justindowty.convcalc;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

import com.example.justindowty.convcalc.dummy.HistoryContent;

public class HistoryActivity extends Activity
        implements HistoryFragment.OnListFragmentInteractionListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
    }

    public void onListFragmentInteraction(HistoryContent.HistoryItem item) {
        System.out.println("Interact!");
        Intent intent = new Intent();
        String[] vals = {item.fromVal.toString(), item.toVal.toString(), item.mode, item.fromUnits, item.toUnits};
        intent.putExtra("item", vals);
        setResult(MainActivity.HISTORY_RESULT,intent);
        finish();
    }
}
