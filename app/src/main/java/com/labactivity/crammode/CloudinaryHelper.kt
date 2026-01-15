// CloudinaryHelper.kt
package com.labactivity.crammode

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils

object CloudinaryHelper {
    val cloudinary: Cloudinary by lazy {
        Cloudinary(
            ObjectUtils.asMap(
                "cloud_name", "dn5xsookt",
                "api_key", "715435882716334",
                "api_secret", "v3XZNk5CyRtR6cWfTU7GxhBJmjk"
            )
        )
    }
}
