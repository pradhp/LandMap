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

        Button saveButton = (Button)findViewById(R.id.area_edit_save_btn);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);

                EditText nameTextView = (EditText) findViewById(R.id.area_name_edit);
                String nameText = nameTextView.getText().toString();

                EditText descTextView = (EditText) findViewById(R.id.area_desc_edit);
                String descText = descTextView.getText().toString();

                adb.updateArea(ae.getId(), nameText, descText, "0.0", "0.0");
                areaNameView.setText(nameText);
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
}
