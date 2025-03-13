package com.example.avianquest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
    private Bitmap capturedImage;
    private OkHttpClient client;
    private final Gson gson = new Gson();

    // Baidu API credentials
    private static final String API_KEY = "ZD3RgiDY7RncuChAJljJRyfE";
    private static final String SECRET_KEY = "b79d61b4TEFUZ9mQlHj95tiRGs99TgYK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_point);

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize HTTP client
        client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();

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
                        capturedImage = (Bitmap) extras.get("data");
                        imageView.setImageBitmap(capturedImage);

                        // Recognize animal in the image
                        recognizeAnimal(capturedImage);
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

    private void recognizeAnimal(Bitmap bitmap) {
        Toast.makeText(this, "正在识别动物...", Toast.LENGTH_SHORT).show();

        // Get access token first
        getAccessToken(new TokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                if (token != null) {
                    // Convert bitmap to base64
                    String base64Image = bitmapToBase64(bitmap);
                    if (base64Image != null) {
                        sendRecognitionRequest(token, base64Image);
                    } else {
                        Toast.makeText(SamplePointActivity.this,
                                "图像处理失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SamplePointActivity.this,
                            "无法获取API访问令牌", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendRecognitionRequest(String accessToken, String base64Image) {
        RequestBody formBody = new FormBody.Builder()
                .add("image", base64Image)
                .add("baike_num", "1")
                .build();

        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rest/2.0/image-classify/v1/animal?access_token=" + accessToken)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(SamplePointActivity.this,
                        "识别请求失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

                    if (jsonObject.has("result") && jsonObject.get("result").isJsonArray()) {
                        JsonArray results = jsonObject.getAsJsonArray("result");
                        if (results.size() > 0) {
                            JsonObject firstResult = results.get(0).getAsJsonObject();
                            String animalName = firstResult.get("name").getAsString();

                            runOnUiThread(() -> {
                                birdSpeciesEditText.setText(animalName);
                                Toast.makeText(SamplePointActivity.this,
                                        "识别结果: " + animalName, Toast.LENGTH_SHORT).show();
                            });
                            return;
                        }
                    }

                    runOnUiThread(() -> Toast.makeText(SamplePointActivity.this,
                            "无法识别动物", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(SamplePointActivity.this,
                            "API响应错误: " + response.code(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void getAccessToken(TokenCallback callback) {
        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .add("client_id", API_KEY)
                .add("client_secret", SECRET_KEY)
                .build();

        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onTokenReceived(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
                    String token = jsonObject.get("access_token").getAsString();
                    callback.onTokenReceived(token);
                } else {
                    callback.onTokenReceived(null);
                }
            }
        });
    }

    private String bitmapToBase64(Bitmap bitmap) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

    interface TokenCallback {
        void onTokenReceived(String token);
    }
}