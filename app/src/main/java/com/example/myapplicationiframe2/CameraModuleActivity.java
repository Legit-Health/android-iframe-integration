package com.example.myapplicationiframe2;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CameraModuleActivity extends AppCompatActivity {

    public static final int REQUEST_SELECT_FILE = 100;
    public ValueCallback<Uri[]> uploadMessage;

    private interface PermissionListener {
        void onPermissionSelect(Boolean isGranted);
    }

    private PermissionListener permissionListener;

    ActivityResultCallback<Map<String, Boolean>> permissionCallback = (Map<String, Boolean> isGranted) -> {
        if (permissionListener != null) {
            boolean granted = true;
            for (Map.Entry<String, Boolean> permission : isGranted.entrySet()) {
                if (!permission.getValue()) granted = false;
            }
            permissionListener.onPermissionSelect(granted);
        }
    };
    private ActivityResultLauncher permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissionCallback);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WebView webView = findViewById(R.id.webview);
        WebView.setWebContentsDebuggingEnabled(true);
        WebSettings webViewSettings = webView.getSettings();
        webViewSettings.setJavaScriptEnabled(true);
        webViewSettings.setDomStorageEnabled(true);
        webViewSettings.setUseWideViewPort(true);
        webViewSettings.setLoadWithOverviewMode(true);
        webViewSettings.setAllowContentAccess(true);
        webViewSettings.setAllowFileAccess(true);
        webView.setWebChromeClient(new WebChromeClient(){

            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                // make sure there is no existing message
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }

                uploadMessage = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
                try {
                    startActivityForResult(intent, CameraModuleActivity.REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e) {
                    uploadMessage = null;
                    return false;
                }

                return true;
            }
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                boolean isRequestPermissionRequired = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M;

                List<String> permissionList = new ArrayList<>();
                if (Arrays.asList(request.getResources()).contains("android.webkit.resource.VIDEO_CAPTURE")) {
                    permissionList.add(Manifest.permission.CAMERA);
                }
                if (Arrays.asList(request.getResources()).contains("android.webkit.resource.AUDIO_CAPTURE")) {
                    permissionList.add(Manifest.permission.MODIFY_AUDIO_SETTINGS);
                    permissionList.add(Manifest.permission.RECORD_AUDIO);
                }
                if (!permissionList.isEmpty() && isRequestPermissionRequired) {
                    String[] permissions = permissionList.toArray(new String[0]);
                    permissionListener =
                            isGranted -> {
                                if (isGranted) {
                                    request.grant(request.getResources());
                                } else {
                                    request.deny();
                                }
                            };
                    permissionLauncher.launch(permissions);
                } else {
                    request.grant(request.getResources());
                }

            }


        });
        webView.loadUrl("https://iframe-pre.legit.health/?company=DpRgQyNegxTMyuaaUdZtjLxnEPyGW4sQHQDjqvUqt14WyVF1AsW8uvQEdiCa9HTt&enableCameraForAndroid=1");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SELECT_FILE) {
            if (uploadMessage == null) return;

            uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            uploadMessage = null;
        }
    }

}
