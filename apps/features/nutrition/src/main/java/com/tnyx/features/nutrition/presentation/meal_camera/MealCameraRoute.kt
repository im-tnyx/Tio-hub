package com.tnyx.features.nutrition.presentation.meal_camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.common.InputImage
import com.tnyx.features.nutrition.domain.models.NutritionMeal
import com.tnyx.features.nutrition.domain.models.MealItem
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(markerClass = [ExperimentalGetImage::class])
@Composable
fun MealCameraRoute(
    onNavigateBack: () -> Unit,
    onOpenMealEditor: (NutritionMeal, String, String) -> Unit,
    onBarcodeScanned: (String) -> Unit,
    onBarcodeResolved: (MealItem) -> Unit,
    viewModel: MealCameraViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    val previewView = remember(context) {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    val imageAnalysis = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }
    val barcodeScanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E,
                )
                .build(),
        )
    }
    val barcodeHandled = remember { AtomicBoolean(false) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }

    fun navigateBack() {
        state.capturedPhotoPath?.let(::File)?.delete()
        viewModel.handleAction(MealCameraAction.BackClicked)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.handleAction(MealCameraAction.CameraPermissionChanged(granted))
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        coroutineScope.launch {
            runCatching { copyGalleryPhotoToCache(context, uri) }
                .onSuccess { photo ->
                    viewModel.handleAction(
                        MealCameraAction.PhotoCaptured(photo.path, photo.mimeType)
                    )
                }
                .onFailure { error ->
                    viewModel.handleAction(
                        MealCameraAction.PhotoSelectionFailed(
                            error.message ?: "Meal photo could not be opened.",
                        )
                    )
                }
        }
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        viewModel.handleAction(MealCameraAction.CameraPermissionChanged(granted))
        if (!granted) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(state.isBarcodeMode) {
        barcodeHandled.set(false)
    }

    fun configureBarcodeAnalyzer() {
        imageAnalysis.setAnalyzer(mainExecutor) { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage == null) {
                imageProxy.close()
                return@setAnalyzer
            }
            barcodeScanner.process(
                InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees),
            ).addOnSuccessListener { barcodes ->
                val value = barcodes.firstNotNullOfOrNull { barcode ->
                    barcode.rawValue?.trim()?.takeIf(String::isNotEmpty)
                }
                if (value != null && barcodeHandled.compareAndSet(false, true)) {
                    viewModel.handleAction(MealCameraAction.BarcodeDetected(value))
                }
            }.addOnCompleteListener {
                imageProxy.close()
            }
        }
    }

    LaunchedEffect(
        state.hasCameraPermission,
        state.capturedPhotoPath,
        state.isBarcodeMode,
    ) {
        if (!state.hasCameraPermission || state.capturedPhotoPath != null) {
            cameraProvider?.unbindAll()
            imageAnalysis.clearAnalyzer()
            camera = null
            return@LaunchedEffect
        }
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener(
            {
                runCatching {
                    providerFuture.get().also { provider ->
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        provider.unbindAll()
                        if (state.isBarcodeMode) {
                            configureBarcodeAnalyzer()
                        } else {
                            imageAnalysis.clearAnalyzer()
                        }
                        camera = provider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture,
                            *if (state.isBarcodeMode) arrayOf(imageAnalysis) else emptyArray(),
                        )
                        cameraProvider = provider
                    }
                }.onSuccess {
                    viewModel.handleAction(
                        MealCameraAction.CameraReady(camera?.cameraInfo?.hasFlashUnit() == true)
                    )
                }.onFailure { error ->
                    viewModel.handleAction(
                        MealCameraAction.CameraFailed(
                            error.message ?: "Camera could not be started.",
                        )
                    )
                }
            },
            mainExecutor,
        )
    }

    LaunchedEffect(state.isFlashEnabled, camera) {
        camera?.cameraControl?.enableTorch(state.isFlashEnabled)
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraProvider?.unbindAll()
            imageAnalysis.clearAnalyzer()
            barcodeScanner.close()
            camera = null
        }
    }

    BackHandler(onBack = ::navigateBack)

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                MealCameraEffect.NavigateBack -> onNavigateBack()
                is MealCameraEffect.OpenBarcodeSearch -> onBarcodeScanned(effect.barcode)
                is MealCameraEffect.OpenBarcodeMealEditor -> onBarcodeResolved(effect.item)
                is MealCameraEffect.OpenMealEditor -> onOpenMealEditor(
                    effect.meal,
                    effect.photoPath,
                    effect.photoMimeType,
                )
            }
        }
    }

    MealCameraScreen(
        state = state,
        cameraPreview = {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize(),
            )
        },
        onBack = ::navigateBack,
        onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
        onFlash = { viewModel.handleAction(MealCameraAction.FlashClicked) },
        onGallery = { galleryLauncher.launch("image/*") },
        onBarcode = {
            viewModel.handleAction(MealCameraAction.BarcodeClicked)
        },
        onCapture = {
            val outputFile = runCatching { createMealCameraFile(context) }
                .getOrElse { error ->
                    viewModel.handleAction(
                        MealCameraAction.PhotoSelectionFailed(
                            error.message ?: "Meal photo could not be created.",
                        )
                    )
                    return@MealCameraScreen
                }
            val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
            imageCapture.takePicture(
                outputOptions,
                mainExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        if (outputFile.length() !in 1..MAX_CAPTURED_PHOTO_BYTES) {
                            outputFile.delete()
                            viewModel.handleAction(
                                MealCameraAction.PhotoSelectionFailed(
                                    "Meal photo is too large. Maximum size is 10 MB.",
                                )
                            )
                            return
                        }
                        viewModel.handleAction(
                            MealCameraAction.PhotoCaptured(
                                path = outputFile.absolutePath,
                                mimeType = "image/jpeg",
                            )
                        )
                    }

                    override fun onError(exception: ImageCaptureException) {
                        outputFile.delete()
                        viewModel.handleAction(
                            MealCameraAction.PhotoSelectionFailed(
                                exception.message ?: "Meal photo could not be captured.",
                            )
                        )
                    }
                },
            )
        },
        onRetry = {
            state.capturedPhotoPath?.let(::File)?.delete()
            viewModel.handleAction(MealCameraAction.RetryClicked)
        },
        onDone = {
            val photoPath = state.capturedPhotoPath ?: return@MealCameraScreen
            viewModel.handleAction(MealCameraAction.AnalysisPreparationStarted)
            coroutineScope.launch {
                runCatching { prepareRecognitionImage(photoPath) }
                    .onSuccess { bytes ->
                        viewModel.handleAction(MealCameraAction.AnalysisPrepared(bytes))
                    }
                    .onFailure { error ->
                        viewModel.handleAction(
                            MealCameraAction.AnalysisPreparationFailed(
                                error.message ?: "Meal photo could not be prepared.",
                            )
                        )
                    }
            }
        },
    )
}

private const val MAX_CAPTURED_PHOTO_BYTES = 10L * 1024L * 1024L
