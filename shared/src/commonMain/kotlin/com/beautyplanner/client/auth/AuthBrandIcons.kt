package com.beautyplanner.client.auth

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val GoogleVector: ImageVector
    get() = ImageVector.Builder(
        name = "GoogleIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 262f,
        viewportHeight = 262f
    ).apply {
        path(
            fill = SolidColor(Color(0xFF4285F4)),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(255.878f, 133.451f)
            curveTo(255.878f, 124.164f, 255.113f, 117.387f, 253.455f, 110.356f)
            lineTo(130.55f, 110.356f)
            lineTo(130.55f, 159.011f)
            lineTo(202.688f, 159.011f)
            curveTo(201.234f, 171.103f, 193.38f, 189.315f, 175.929f, 201.553f)
            lineTo(175.684f, 203.182f)
            lineTo(214.596f, 233.331f)
            lineTo(217.292f, 233.6f)
            curveTo(242.065f, 210.765f, 255.878f, 177.145f, 255.878f, 133.451f)
            close()
        }
        path(
            fill = SolidColor(Color(0xFF34A853)),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(130.55f, 261.1f)
            curveTo(165.798f, 261.1f, 195.389f, 249.495f, 217.003f, 229.478f)
            lineTo(175.807f, 197.565f)
            curveTo(164.783f, 205.253f, 149.987f, 210.62f, 130.55f, 210.62f)
            curveTo(96.027f, 210.62f, 66.726f, 187.847f, 56.281f, 156.37f)
            lineTo(54.75f, 156.5f)
            lineTo(14.452f, 187.687f)
            lineTo(13.925f, 189.152f)
            curveTo(35.393f, 231.798f, 79.49f, 261.1f, 130.55f, 261.1f)
            close()
        }
        path(
            fill = SolidColor(Color(0xFFFBBC05)),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(56.281f, 156.37f)
            curveTo(53.525f, 148.247f, 51.93f, 139.543f, 51.93f, 130.55f)
            curveTo(51.93f, 121.556f, 53.525f, 112.853f, 56.136f, 104.73f)
            lineTo(56.063f, 103.0f)
            lineTo(15.26f, 71.312f)
            lineTo(13.925f, 71.947f)
            curveTo(5.077f, 89.644f, 0f, 109.517f, 0f, 130.55f)
            curveTo(0f, 151.583f, 5.077f, 171.455f, 13.925f, 189.152f)
            lineTo(56.281f, 156.37f)
            close()
        }
        path(
            fill = SolidColor(Color(0xFFEB4335)),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(130.55f, 50.479f)
            curveTo(155.064f, 50.479f, 171.6f, 61.068f, 181.029f, 69.917f)
            lineTo(217.873f, 33.943f)
            curveTo(195.331f, 12.91f, 165.798f, 0f, 130.55f, 0f)
            curveTo(79.49f, 0f, 35.393f, 29.302f, 13.925f, 71.947f)
            lineTo(56.136f, 104.73f)
            curveTo(66.726f, 73.253f, 96.027f, 50.479f, 130.55f, 50.479f)
            close()
        }
    }.build()

private val MailVector: ImageVector
    get() = ImageVector.Builder(
        name = "MailIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color(0xFF1F1F1F)),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(22f, 5f)
            lineTo(22f, 9f)
            lineTo(12f, 13f)
            lineTo(2f, 9f)
            lineTo(2f, 5f)
            curveTo(2f, 4.4477f, 2.4477f, 4f, 3f, 4f)
            lineTo(21f, 4f)
            curveTo(21.5523f, 4f, 22f, 4.4477f, 22f, 5f)
            close()

            moveTo(2f, 11.154f)
            lineTo(2f, 19f)
            curveTo(2f, 19.5523f, 2.4477f, 20f, 3f, 20f)
            lineTo(21f, 20f)
            curveTo(21.5523f, 20f, 22f, 19.5523f, 22f, 19f)
            lineTo(22f, 11.154f)
            lineTo(12.3714f, 15.0056f)
            curveTo(12.1346f, 15.1003f, 11.8654f, 15.1003f, 11.6286f, 15.0056f)
            lineTo(2f, 11.154f)
            close()
        }
    }.build()

@Composable
fun GoogleIcon() {
    Icon(
        imageVector = GoogleVector,
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = Modifier.size(20.dp)
    )
}

@Composable
fun AppleGlyphIcon(
    color: Color = Color(0xFF1F1F1F)
) {
    Text(
        text = "",
        color = color,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    )
}

@Composable
fun MailIcon() {
    Icon(
        imageVector = MailVector,
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = Modifier.size(20.dp)
    )
}