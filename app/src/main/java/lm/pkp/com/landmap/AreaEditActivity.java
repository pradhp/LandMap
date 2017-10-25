package lm.pkp.com.landmap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import lm.pkp.com.landmap.area.AreaDBHelper;
import lm.pkp.com.landmap.area.AreaElement;

public class AreaEditActivity extends AppCompatActivity{

    private AreaDBHelper adb = null;
    private AreaElement ae = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_edit);

        Bundle bundle = getIntent().getExtras();
        final String areaName = bundle.getString("area_name");

        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.show();

        adb = new AreaDBHelper(getApplicationContext());
        final AreaElement ae = adb.getAreaByName(areaName);

        final TextView areaNameView = (TextView)findViewById(R.id.area_name_fixed);
        areaNameView.setText(ae.getName());

        final EditText nameTextView = (EditText) findViewById(R.id.area_name_edit);
        nameTextView.setText(ae.getName());

        final EditText descTextView = (EditText) findViewById(R.id.area_desc_edit);
        descTextView.setText(ae.getDescription());

        Button saveButton = (Button)findViewById(R.id.area_edit_save_btn);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);

                String nameText = nameTextView.getText().toString();
                ae.setName(nameText);

                String descText = descTextView.getText().toString();
                ae.setDescription(descText);

                adb.updateArea(ae);
                areaNameView.setText(nameText);

                findViewById(R.id.loadingPanel).setVisibility(View.INVISIBLE);

                Intent positionMarkerIntent = new Intent(AreaEditActivity.this, PositionMarkerActivity.class);
                positionMarkerIntent.putExtra("area_name", ae.getName());
                startActivity(positionMarkerIntent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent positionMarkerIntent = new Intent(AreaEditActivity.this, PositionMarkerActivity.class);
                positionMarkerIntent.putExtra("area_name", ae.getName());
                startActivity(positionMarkerIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        Intent positionMarkerIntent = new Intent(AreaEditActivity.this, PositionMarkerActivity.class);
        positionMarkerIntent.putExtra("area_name", ae.getName());
        startActivity(positionMarkerIntent);
    }
}
