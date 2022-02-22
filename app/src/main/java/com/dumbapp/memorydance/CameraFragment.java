package com.dumbapp.memorydance;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.dumbapp.memorydance.databinding.FragmentCameraBinding;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraFragment extends Fragment {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 999;
    protected ExecutorService analysisExecutor;
    private Camera camera;
    private ProcessCameraProvider cameraProvider;
    private Preview cameraPreview;
    private FragmentCameraBinding binding;
    private MainViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        ((MemoryDanceApplication) requireContext().getApplicationContext()).getMemoryDanceApplicationComponent().inject(this);
        super.onCreate(savedInstanceState);
        analysisExecutor = Executors.newSingleThreadExecutor();
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_camera, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //TODO initializeFaceDetection
    }

    @Override
    public void onStart() {
        super.onStart();

        if (requireActivity().checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            initializeCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onStop() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        analysisExecutor.shutdown();
        super.onDestroy();
    }

    private void onCameraPermissionDenied() {

    }

    private void onCameraInitialized() {
        initializeStartButton();
    }

    private void onCameraInitFailed() {

    }

    private void initializeStartButton() {
        binding.startButton.setOnClickListener(view -> {
            //TODO Start the game
        });
    }

    private void initializePoseDetection() {
        final PoseDetectorOptions options =
                new PoseDetectorOptions.Builder()
                        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                        .build();
        final PoseDetector poseDetector = PoseDetection.getClient(options);
    }

    private PreviewView getCameraPreviewLayout() {
        return binding.layoutCamera;
    }

    //TODO figure out new flow for permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeCamera();
            } else {
                //TODO need a permission message instead of back nav
                Navigation.findNavController(requireView()).popBackStack();
            }
        }
    }

    private void initializeCamera() {
        new Handler(Looper.getMainLooper()).post(() -> {
            final CameraSelector cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build();

            final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
            cameraProviderFuture.addListener(() -> {
                try {
                    cameraProvider = cameraProviderFuture.get();
                    cameraProvider.unbindAll();

                    cameraPreview = new Preview.Builder().build();
                    camera = cameraProvider.bindToLifecycle(this, cameraSelector, cameraPreview);

                    final PreviewView cameraPreviewLayout = getCameraPreviewLayout();
                    cameraPreviewLayout.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
                    cameraPreview.setSurfaceProvider(cameraPreviewLayout.getSurfaceProvider());

                    onCameraInitialized();
                } catch (final Exception exception) {
                    Log.d(getClass().getSimpleName(), "initializeCamera: FAILED");
                    onCameraInitFailed();
                }
            }, ContextCompat.getMainExecutor(requireContext()));
        });
    }

    public Bitmap getPreviewBitmap() {
        return getCameraPreviewLayout().getBitmap();
    }
}
