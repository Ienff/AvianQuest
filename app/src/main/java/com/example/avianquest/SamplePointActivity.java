package com.example.avianquest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SamplePointActivity extends AppCompatActivity {

    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    public static final String EXTRA_SAMPLE_POINT_ID = "extra_sample_point_id";

    private EditText timeEditText;
    private EditText coordinatesEditText;
    private EditText birdSpeciesEditText;
    private RadioGroup genderRadioGroup;
    private EditText quantityEditText;
    private EditText habitatTypeEditText;
    private EditText distanceToLineEditText;
    private EditText statusEditText;
    private EditText remarksEditText;
    private Button saveButton;
    private ImageView imageView;
    private ActivityResultLauncher<Intent> takePhotoLauncher;

    private String samplePointId;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_point);

        // Initialize UI components
        timeEditText = findViewById(R.id.edit_time);
        coordinatesEditText = findViewById(R.id.edit_coordinates);
        birdSpeciesEditText = findViewById(R.id.edit_bird_species);
        genderRadioGroup = findViewById(R.id.radio_group_gender);
        quantityEditText = findViewById(R.id.edit_quantity);
        habitatTypeEditText = findViewById(R.id.edit_habitat_type);
        distanceToLineEditText = findViewById(R.id.edit_distance);
        statusEditText = findViewById(R.id.edit_status);
        remarksEditText = findViewById(R.id.edit_remarks);
        saveButton = findViewById(R.id.btn_save);
        imageView = findViewById(R.id.imageView);
        Button btnTakePhoto = findViewById(R.id.btn_take_photo);

        // Get data from intent
        Intent intent = getIntent();
        if (intent != null) {
            latitude = intent.getDoubleExtra(EXTRA_LATITUDE, 0);
            longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, 0);
            samplePointId = intent.getStringExtra(EXTRA_SAMPLE_POINT_ID);
        }

        // Set default values
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        timeEditText.setText(dateFormat.format(new Date()));
        coordinatesEditText.setText(String.format(Locale.getDefault(), "%.6f, %.6f", latitude, longitude));

        // Set save button listener
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSamplePoint();
            }
        });

        // Initialize photo taking functionality
        takePhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        imageView.setImageBitmap(imageBitmap);
                    }
                }
        );

        btnTakePhoto.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                takePhotoLauncher.launch(takePictureIntent);
            }
        });
    }

    private void saveSamplePoint() {
        // Get values from UI
        String time = timeEditText.getText().toString();
        String birdSpecies = birdSpeciesEditText.getText().toString();
        String gender = getSelectedGender();
        int quantity = 0;
        try {
            quantity = Integer.parseInt(quantityEditText.getText().toString());
        } catch (NumberFormatException ignored) {}

        String habitatType = habitatTypeEditText.getText().toString();
        int distanceToLine = 0;
        try {
            distanceToLine = Integer.parseInt(distanceToLineEditText.getText().toString());
        } catch (NumberFormatException ignored) {}

        String status = statusEditText.getText().toString();
        String remarks = remarksEditText.getText().toString();

        // TODO: Save the updated sample point to your storage mechanism
        Toast.makeText(this, "Sample point saved successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    private String getSelectedGender() {
        int selectedId = genderRadioGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            return "unknown";
        }
        RadioButton radioButton = findViewById(selectedId);
        return radioButton.getTag().toString();
    }
}