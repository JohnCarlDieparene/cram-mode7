package com.labactivity.crammode

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

class PrivacyPolicyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PrivacyPolicyScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Privacy Policy") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Effective Date: January 2026",
                style = MaterialTheme.typography.labelMedium
            )

            SectionTitle("1. Introduction")
            SectionText(
                "Cram Mode respects your privacy. This Privacy Policy explains how information is collected, used, and protected when using the Cram Mode mobile application."
            )

            SectionTitle("2. Information We Collect")
            SectionText(
                """
We may collect the following information:
• Account information (name, email address, profile photo) through Google Sign-In
• Study-related data such as summaries, flashcards, quizzes, and study history
• Usage data for improving app performance and features
                """.trimIndent()
            )

            SectionTitle("3. How We Use Your Information")
            SectionText(
                """
Your information is used to:
• Provide and personalize study features
• Save your study progress and history
• Improve application functionality and user experience
• Ensure secure authentication and account access
                """.trimIndent()
            )

            SectionTitle("4. Data Storage and Security")
            SectionText(
                "Cram Mode uses Firebase services to securely store user data. Reasonable security measures are implemented to protect your information from unauthorized access."
            )

            SectionTitle("5. Third-Party Services")
            SectionText(
                """
Cram Mode uses third-party services such as:
• Firebase Authentication and Firestore
• AI services for generating summaries, flashcards, and quizzes

These services may process data in accordance with their own privacy policies.
                """.trimIndent()
            )

            SectionTitle("6. Data Retention")
            SectionText(
                "Your study data is retained only as long as your account is active. You may delete your data by clearing your study history or logging out of the application."
            )

            SectionTitle("7. Changes to This Policy")
            SectionText(
                "This Privacy Policy may be updated from time to time. Any changes will be reflected within the application."
            )

            SectionTitle("8. Contact")
            SectionText(
                "If you have questions regarding this Privacy Policy, please contact the developer through the Cram Mode project repository or academic institution."
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "© 2026 Cram Mode",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun SectionText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        lineHeight = 20.sp
    )
}
