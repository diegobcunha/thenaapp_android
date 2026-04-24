package com.diegocunha.thenaapp.feature.baby.presentation.create.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import com.diegocunha.thenaapp.feature.baby.R
import com.diegocunha.thenaapp.feature.baby.domain.model.BabyGender
import com.diegocunha.thenaapp.feature.baby.domain.model.ResponsibleType
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun BabyInfoStep(
    babyName: String,
    @StringRes babyNameError: Int?,
    babyGender: BabyGender?,
    @StringRes babyGenderError: Int?,
    responsibleType: ResponsibleType?,
    @StringRes responsibleTypeError: Int?,
    photo: Uri?,
    onNameChange: (String) -> Unit,
    onPhotoChange: (Uri?) -> Unit,
    onGenderChange: (BabyGender) -> Unit,
    onResponsibleTypeChange: (ResponsibleType) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val spacing = ThenaTheme.spacing
    val colors = MaterialTheme.colorScheme

    var showPhotoDialog by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let {
            scope.launch {
                onPhotoChange(uri)
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            cameraUri?.let { uri ->
                scope.launch {
                    onPhotoChange(uri)
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            val uri = createCameraUri(context)
            cameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    if (showPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            title = { Text(stringResource(R.string.create_baby_photo_dialog_title)) },
            text = {
                Column {
                    TextButton(onClick = {
                        showPhotoDialog = false
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            val uri = createCameraUri(context)
                            cameraUri = uri
                            cameraLauncher.launch(uri)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }) { Text(stringResource(R.string.create_baby_photo_take)) }

                    TextButton(onClick = {
                        showPhotoDialog = false
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }) { Text(stringResource(R.string.create_baby_photo_gallery)) }

                    if (photo != null) {
                        TextButton(onClick = {
                            showPhotoDialog = false
                            onPhotoChange(null)
                        }) { Text(stringResource(R.string.create_baby_photo_remove)) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoDialog = false }) {
                    Text(stringResource(R.string.create_baby_photo_cancel))
                }
            },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.lg, vertical = spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        // Avatar picker
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(colors.primaryContainer)
                    .border(
                        width = 2.dp,
                        brush = SolidColor(colors.primary),
                        shape = CircleShape,
                    )
                    .clickable { showPhotoDialog = true },
                contentAlignment = Alignment.Center,
            ) {
                if (photo != null) {
                    AsyncImage(
                        model = photo,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Text(text = "🍼", fontSize = 48.sp)
                }
            }
            TextButton(onClick = { showPhotoDialog = true }) {
                Text(stringResource(R.string.create_baby_add_photo))
            }
        }

        OutlinedTextField(
            value = babyName,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.create_baby_name_label)) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            isError = babyNameError != null,
            supportingText = babyNameError?.let { id -> { Text(stringResource(id)) } },
            singleLine = true,
        )

        Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
            Text(
                text = stringResource(R.string.create_baby_gender_title),
                style = MaterialTheme.typography.titleSmall,
            )
            GenderSelector(
                selected = babyGender,
                onSelect = onGenderChange,
            )
            if (babyGenderError != null) {
                Text(
                    text = stringResource(babyGenderError),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.error,
                    modifier = Modifier.padding(start = spacing.md),
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
            Text(
                text = stringResource(R.string.create_baby_responsible_title),
                style = MaterialTheme.typography.titleSmall
            )

            ResponsibleSelector(
                selected = responsibleType,
                onSelect = onResponsibleTypeChange
            )

            if (responsibleTypeError != null) {
                Text(
                    text = stringResource(responsibleTypeError),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.error,
                    modifier = Modifier.padding(start = spacing.md),
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.create_baby_next))
        }
    }
}

private fun createCameraUri(context: Context): Uri {
    val file = File.createTempFile("baby_photo_", ".jpg", context.cacheDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}
