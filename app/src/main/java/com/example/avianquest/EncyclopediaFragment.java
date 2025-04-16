package com.example.avianquest;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EncyclopediaFragment extends Fragment {
    private EditText etSearch;
    private ImageButton btnSearch;
    private TextView tvResult;

    private final OkHttpClient client = new OkHttpClient();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_encyclopedia, container, false);

        etSearch = view.findViewById(R.id.et_search);
        btnSearch = view.findViewById(R.id.btn_search);
        tvResult = view.findViewById(R.id.tv_result);

        btnSearch.setOnClickListener(v -> performSearch());

        return view;
    }

    private void performSearch() {
        String query = etSearch.getText().toString().trim();
        if (TextUtils.isEmpty(query)) {
            Toast.makeText(requireContext(), "请输入鸟类名称", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://cn.apihz.cn/api/zici/baikebaidu.php?id=88888888&key=88888888&words=" + query;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "搜索失败，请检查网络", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    try {
                        // 打印完整的响应数据以调试
                        System.out.println("API Response: " + responseBody);
                        JSONObject jsonObject = new JSONObject(responseBody);
                        // 获取 msg 字段的内容
                        String description = jsonObject.optString("msg", "未找到相关信息");
                        requireActivity().runOnUiThread(() -> {
                            if (!description.equals("未找到相关信息")) {
                                tvResult.setText(description);
                                tvResult.setVisibility(View.VISIBLE); // Ensure the TextView is visible
                            } else {
                                tvResult.setText(""); // Clear the TextView if no content is found
                                tvResult.setVisibility(View.GONE); // Hide the TextView if no content
                            }
                        });
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "解析数据失败", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "未找到相关信息", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
}