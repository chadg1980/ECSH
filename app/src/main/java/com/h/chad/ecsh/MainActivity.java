package com.h.chad.ecsh;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        Vision.Builder visionBuilder = new Vision.Builder(
                new NetHttpTransport(),
                new AndroidJsonFactory(),
                null);
        visionBuilder.setVisionRequestInitializer(
                new VisionRequestInitializer(""));

        final Vision vision = visionBuilder.build();

        // Create new thread
        AsyncTask.execute(new Runnable() {

            @Override
            public void run() {
                //convert photo to byte array
                byte[] photoData = new byte[0];
                final InputStream inputStream =
                        getResources().openRawResource(R.raw.qqq);
                try {
                    photoData = IOUtils.toByteArray(inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Image inputImage = new Image();
                inputImage.encodeContent(photoData);
                Feature desiredFeature = new Feature();
                desiredFeature.setType("FACE_DETECTION");

                AnnotateImageRequest request = new AnnotateImageRequest();
                request.setImage(inputImage);
                request.setFeatures(Arrays.asList(desiredFeature));

                BatchAnnotateImagesRequest batchRequest =
                        new BatchAnnotateImagesRequest();

                batchRequest.setRequests(Arrays.asList(request));
                BatchAnnotateImagesResponse batchResponse = null;
                try {
                    batchResponse =
                            vision.images().annotate(batchRequest).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                List<FaceAnnotation> faces = batchResponse.getResponses()
                        .get(0).getFaceAnnotations();

                int numberOfFaces = faces.size();

                //Get joy likelihood for each face
                String likelihoods = "";
                for(int i=0; i<numberOfFaces; i++) {
                    likelihoods += "\n It is " +
                            faces.get(i).getJoyLikelihood() +
                            " that face " + i + " is happy";
                }

                // Concatenate everything
                final String message =
                        "This photo has " + numberOfFaces + " faces" + likelihoods;

                // Display toast on UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        InputStream is =
                                getResources().openRawResource(R.raw.qqq);
                        ImageView imageView = (ImageView) findViewById(R.id.this_image);
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        imageView.setImageBitmap(bitmap);
                        TextView textView = (TextView) findViewById(R.id.text_here);
                        textView.setText(message);

                    }
                });

            }

        });



    }
}
