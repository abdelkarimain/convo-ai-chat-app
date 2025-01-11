package io.github.abdelkarimain.auth;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class ImageChatActivity extends AppCompatActivity {

    private LottieAnimationView lottieLoader;
    private static final String TAG = "ImageChatActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String BASE_URL = "https://pollinations.ai/p/";

    private EditText promptEditText;
    private ImageView generatedImageView;
    private MaterialButton downloadButton;
    private ProgressBar progressBar;
    private Bitmap currentImageBitmap;
    private Bitmap originalImageBitmap;
    private Bitmap displayImageBitmap;
    private String savedFilePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_chat);
        initViews();
        setupListeners();
    }

    private void initViews() {
        promptEditText = findViewById(R.id.promptEditText);
        generatedImageView = findViewById(R.id.generatedImageView);
        downloadButton = findViewById(R.id.downloadButton);
        progressBar = findViewById(R.id.progressBar); // Ensure this matches the ID in the layout
        lottieLoader = findViewById(R.id.lottieLoader);

        progressBar.setVisibility(View.GONE);
        downloadButton.setVisibility(View.GONE);
    }

    private void setLoadingState(boolean isLoading) {
        runOnUiThread(() -> {
            if (isLoading) {
                lottieLoader.setVisibility(View.VISIBLE);
                lottieLoader.playAnimation();
                downloadButton.setVisibility(View.GONE);
                generatedImageView.setImageDrawable(null);
                currentImageBitmap = null;
            } else {
                lottieLoader.setVisibility(View.GONE);
                lottieLoader.cancelAnimation();
                progressBar.setVisibility(View.GONE);
                downloadButton.setVisibility(currentImageBitmap != null ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void setupListeners() {
        findViewById(R.id.generateButton).setOnClickListener(v -> generateImage());
        findViewById(R.id.btnGoBack).setOnClickListener(v -> finish());
        downloadButton.setOnClickListener(v -> handleDownloadClick());
    }

    private void generateImage() {
        String prompt = promptEditText.getText().toString().trim();
        if (prompt.isEmpty()) {
            promptEditText.setError("Please enter a prompt");
            return;
        }

        setLoadingState(true);
        String imageUrl = buildImageUrl(prompt);
        Log.d(TAG, "Generated URL: " + imageUrl);

        loadImageWithGlide(imageUrl);
    }

    private String buildImageUrl(String prompt) {
        try {
            String encodedPrompt = URLEncoder.encode(prompt, StandardCharsets.UTF_8.toString());
            int width = 500;
            int height = 500;
            int seed = new Random().nextInt(10000);
            String model = "flux";
            return BASE_URL + encodedPrompt + "?width=" + width + "&height=" + height + "&seed=" + seed + "&model=" + model + "&nologo=true";
        } catch (IOException e) {
            Log.e(TAG, "Error encoding prompt", e);
            return BASE_URL + prompt.replace(" ", "+") + "?width=500&height=500&seed=" + new Random().nextInt(10000) + "&model=flux";
        }
    }

    private void loadImageWithGlide(String imageUrl) {
        int radius = 20;

        // First, load the original image
        Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .timeout(60000)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Bitmap> target, boolean isFirstResource) {
                        Log.e(TAG, "Glide load failed for URL: " + imageUrl, e);
                        runOnUiThread(() -> {
                            setLoadingState(false);
                            showToast("Failed to load image. Please try again.");
                        });
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model,
                                                   Target<Bitmap> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        Log.d(TAG, "Original image loaded successfully");
                        return false;
                    }
                })
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap,
                                                @Nullable Transition<? super Bitmap> transition) {
                        originalImageBitmap = bitmap;

                        // Now load the display version with rounded corners
                        Glide.with(ImageChatActivity.this)
                                .asBitmap()
                                .load(imageUrl)
                                .apply(RequestOptions.bitmapTransform(new RoundedCorners(radius)))
                                .into(new CustomTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap roundedBitmap,
                                                                @Nullable Transition<? super Bitmap> transition) {
                                        displayImageBitmap = roundedBitmap;
                                        generatedImageView.setImageBitmap(roundedBitmap);
                                        setLoadingState(false);
                                        downloadButton.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {
                                        displayImageBitmap = null;
                                    }
                                });
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        originalImageBitmap = null;
                    }
                });
    }

    private void downloadImage() {
        if (originalImageBitmap == null) {
            showToast("No image to download");
            return;
        }

        new Thread(() -> {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            String fileName = "ConvoAI_" + System.currentTimeMillis() + ".jpg";
            File file = new File(path, fileName);

            try {
                path.mkdirs();
                FileOutputStream out = new FileOutputStream(file);
                originalImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();

                runOnUiThread(() -> {
                    showToast("Image saved to Pictures");
                    savedFilePath = file.getAbsolutePath();
                    showNotification(savedFilePath);
                });
            } catch (IOException e) {
                Log.e(TAG, "Error saving image", e);
                runOnUiThread(() -> showToast("Error saving image: " + e.getMessage()));
            }
        }).start();
    }

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;

    private void showNotification(String filePath) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            return;
        }

        String channelId = "image_download_channel";
        String channelName = "Image Download Notifications";
        int notificationId = 1;

        // Create the notification channel (required for Android O and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Create an intent to open the image file
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(filePath), "image/*");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_image) // Replace with your app's icon
                .setContentTitle("Image Saved")
                .setContentText("Your image has been saved to Pictures.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, builder.build());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can now show the notification
                showNotification(savedFilePath);
            } else {
                showToast("Permission denied. Cannot show notification.");
            }
        }
    }

    private void handleDownloadClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            downloadImage();
        } else {
            checkPermissionAndDownload();
        }
    }

    private void checkPermissionAndDownload() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            downloadImage();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
}