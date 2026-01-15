package com.labactivity.crammode

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import androidx.compose.ui.focus.onFocusChanged


class AuthComposeActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        // Track if user has explicitly logged out
        var userJustLoggedOut = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Auto-login if user is already signed in and didn't log out
        val currentUser = auth.currentUser
        if (currentUser != null && !userJustLoggedOut) {
            startActivity(Intent(this, HomeComposeActivity::class.java))
            finish()
            return
        }

        setContent {
            MaterialTheme {
                AuthScreen(
                    onLoginSuccess = { email ->
                        userJustLoggedOut = false
                        val intent = Intent(this, HomeComposeActivity::class.java)
                        intent.putExtra("userEmail", email) // optional, send to Home screen
                        startActivity(intent)
                        finish()
                    },
                    googleSignInClient = googleSignInClient
                )
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoginSuccess: (String) -> Unit, // <-- now it takes the user email
    googleSignInClient: GoogleSignInClient
) {
    var isLogin by remember { mutableStateOf(true) }
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account, onLoginSuccess, context)
        } catch (e: ApiException) {
            Toast.makeText(context, "Google sign-in failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            Log.e("GoogleSignIn", "Error code: ${e.statusCode}")
        }
    }

    val scale by animateFloatAsState(targetValue = if (isLogin) 1f else 1.05f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Gradient Top Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF6C63FF), Color(0xFF9E92F9))
                    ),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
        )

        // Logo
        Card(
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.TopCenter)
                .offset(y = 100.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.lojo),
                contentDescription = "Logo",
                contentScale = ContentScale.Crop
            )
        }

        // Form
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 220.dp, start = 20.dp, end = 20.dp)
        ) {
            Text(
                text = if (isLogin) "Login" else "Create Account",
                fontSize = 36.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale)
                    .animateContentSize()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Email
                    var emailFocused by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(if (emailFocused) 8.dp else 0.dp, RoundedCornerShape(16.dp))
                            .animateContentSize()
                            .onFocusChanged { emailFocused = it.isFocused },
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF6C63FF),
                            unfocusedBorderColor = Color.Gray,
                            containerColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password
                    var passwordFocused by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(if (passwordFocused) 8.dp else 0.dp, RoundedCornerShape(16.dp))
                            .animateContentSize()
                            .onFocusChanged { passwordFocused = it.isFocused },
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF6C63FF),
                            unfocusedBorderColor = Color.Gray,
                            containerColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Login/Register Button
                    var buttonPressed by remember { mutableStateOf(false) }
                    val buttonScale by animateFloatAsState(if (buttonPressed) 0.95f else 1f)
                    Button(
                        onClick = {
                            buttonPressed = true
                            if (isLogin) {
                                loginUser(email, password, onLoginSuccess, context)
                            } else {
                                registerUser(email, password, onLoginSuccess, context) // now matches (String) -> Unit
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp)
                            .scale(buttonScale),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(if (isLogin) "Login" else "Register")
                    }


                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Divider(modifier = Modifier.weight(1f), color = Color.Gray)
                        Text("  Or sign in with  ", color = Color.Gray)
                        Divider(modifier = Modifier.weight(1f), color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Google Sign-in Button (✅ Handles account chooser)
                    var googlePressed by remember { mutableStateOf(false) }
                    val googleScale by animateFloatAsState(if (googlePressed) 0.95f else 1f)

                    Button(
                        onClick = {
                            googlePressed = true
                            googleSignInClient.signOut().addOnCompleteListener {
                                val signInIntent = googleSignInClient.signInIntent
                                googleSignInLauncher.launch(signInIntent)
                                googlePressed = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp)
                            .scale(googleScale) // animated scaling
                            .shadow(4.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp // extra pop when pressed
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google_logo), // your Google logo
                                contentDescription = "Google Logo",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sign in with Google",
                                fontSize = 16.sp
                            )
                        }
                    }



                    Spacer(modifier = Modifier.height(16.dp))

                    // Toggle Login/Register
                    Text(
                        text = if (isLogin) "Don't have an account? Register" else "Already have an account? Login",
                        color = Color.Gray,
                        modifier = Modifier.clickable {
                            isLogin = !isLogin
                            email = ""
                            password = ""
                        }
                    )
                }
            }
        }
    }
}

// ---------------- Firebase Functions ----------------

private fun loginUser(
    email: String,
    password: String,
    onLoginSuccess: (String) -> Unit, // pass email to Home screen
    context: android.content.Context
) {
    if (!email.matches(Regex("^[A-Za-z0-9._%+-]+@email\\.com$"))) {
        Toast.makeText(context, "Email must end with @email.com", Toast.LENGTH_SHORT).show()
        return
    }
    if (password.isEmpty()) {
        Toast.makeText(context, "Password is required", Toast.LENGTH_SHORT).show()
        return
    }

    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: email
                onLoginSuccess(userEmail) // pass email to Home
            } else {
                Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
}

private fun registerUser(
    email: String,
    password: String,
    onLoginSuccess: (String) -> Unit, // now accepts String
    context: android.content.Context
) {
    if (!email.matches(Regex("^[A-Za-z0-9._%+-]+@email\\.com$"))) {
        Toast.makeText(context, "Email must end with @email.com", Toast.LENGTH_SHORT).show()
        return
    }
    if (password.length < 6) {
        Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
        return
    }

    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: email
                onLoginSuccess(userEmail) // pass email
            } else {
                Toast.makeText(context, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
}


private fun firebaseAuthWithGoogle(
    account: GoogleSignInAccount,
    onLoginSuccess: (String) -> Unit,
    context: android.content.Context
) {
    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
    FirebaseAuth.getInstance().signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: account.email.orEmpty()
                onLoginSuccess(userEmail)
            } else {
                Toast.makeText(context, "Google Authentication failed.", Toast.LENGTH_SHORT).show()
            }
        }
}
