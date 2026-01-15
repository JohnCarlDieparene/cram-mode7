package com.labactivity.crammode

import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import uriToBitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
class EditProfileActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val cloudinary = Cloudinary(
        ObjectUtils.asMap(
            "cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME,
            "api_key", BuildConfig.CLOUDINARY_API_KEY,
            "api_secret", BuildConfig.CLOUDINARY_API_SECRET
        )
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                EditProfileScreen(
                    auth = auth,
                    cloudinary = cloudinary,
                    onBack = { finish() } // <-- THIS is required
                )
            }
        }

    }
}

@Composable
fun EditProfileScreen(auth: FirebaseAuth, cloudinary: Cloudinary, onBack: () -> Unit) {
    val context = LocalContext.current
    val user = auth.currentUser

    if (user == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No user logged in")
        }
        return
    }

    var displayName by remember { mutableStateOf(TextFieldValue(user.displayName ?: "")) }
    var photoUri by remember { mutableStateOf<Uri?>(user.photoUrl) }
    var isLoading by remember { mutableStateOf(false) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) photoUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        // --- Top Row with Back Button ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onBack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit Profile", style = MaterialTheme.typography.titleMedium)
        }

        // --- Profile Header Card ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Profile photo with floating edit icon
                Box {
                    if (photoUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(photoUri),
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.displayName?.firstOrNull()?.uppercase() ?: "?",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }

                    // Floating edit icon
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.BottomEnd)
                            .offset((-4).dp, (-4).dp)
                            .background(Color(0xAA000000), CircleShape)
                            .clickable { pickImageLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Photo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Display Name and Email
                Text(
                    text = user.displayName ?: "Unknown User",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = user.email ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        // --- Display Name Input ---
        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Display Name") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        // --- Save Changes Button ---
        Button(
            onClick = {
                if (!isLoading) {
                    isLoading = true
                    (context as? ComponentActivity)?.lifecycleScope?.launch {
                        val success = saveProfile(user, displayName.text, photoUri, cloudinary, context)
                        isLoading = false
                        if (success) {
                            displayName = TextFieldValue(user.displayName ?: "")
                            photoUri = user.photoUrl
                            Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to update profile.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (isLoading) "Saving..." else "Save Changes")
        }

        Spacer(modifier = Modifier.height(200.dp))
    }
}



suspend fun saveProfile(
    user: com.google.firebase.auth.FirebaseUser,
    displayName: String,
    photoUri: Uri?,
    cloudinary: Cloudinary,
    context: android.content.Context
): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            var photoUrl: String? = null

            if (photoUri != null && photoUri.scheme == "content") {
                val bitmap: Bitmap? = uriToBitmap(context, photoUri)
                bitmap?.let {
                    val cropped = cropToCircle(resizeBitmap(it))
                    val stream = java.io.ByteArrayOutputStream()
                    cropped.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    val bytes = stream.toByteArray()
                    val uploadResult = cloudinary.uploader().upload(bytes, ObjectUtils.emptyMap())
                    photoUrl = uploadResult["secure_url"] as? String
                }
            }

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .apply { if (photoUrl != null) setPhotoUri(Uri.parse(photoUrl)) }
                .build()

            user.updateProfile(profileUpdates).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

// --- Helpers ---
fun resizeBitmap(bitmap: Bitmap, maxSize: Int = 512): Bitmap {
    val ratio = bitmap.width.toFloat() / bitmap.height
    val width: Int
    val height: Int
    if (ratio > 1) {
        width = maxSize
        height = (maxSize / ratio).toInt()
    } else {
        height = maxSize
        width = (maxSize * ratio).toInt()
    }
    return Bitmap.createScaledBitmap(bitmap, width, height, true)
}

fun cropToCircle(bitmap: Bitmap): Bitmap {
    val size = bitmap.width.coerceAtMost(bitmap.height)
    val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint().apply { isAntiAlias = true }
    val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())
    canvas.drawARGB(0, 0, 0, 0)
    canvas.drawOval(rect, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    val left = (size - bitmap.width) / 2f
    val top = (size - bitmap.height) / 2f
    canvas.drawBitmap(bitmap, left, top, paint)
    return output
}
