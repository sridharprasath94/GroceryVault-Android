package com.flash.groceryVault.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.flash.groceryVault.R

@Composable
fun GroceryAsyncImage(
    model: Any?,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val isPreview = LocalInspectionMode.current
    if (isPreview) {
        Image(
            painter = painterResource(R.drawable.preview_food),
            contentDescription = "Grocery Image",
            modifier = Modifier
                .width(100.dp)
                .height(100.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentScale = contentScale,
        )
    } else {
        AsyncImage(
            model = model,
            contentDescription = "Grocery Image",
            modifier = Modifier
                .width(100.dp)
                .height(100.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentScale = contentScale,
        )
    }

}

@Composable
fun GroceryImage(
    model: Any?,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val isPreview = LocalInspectionMode.current
    if (isPreview) {
        Image(
            painter = painterResource(R.drawable.preview_food),
            contentDescription = "Grocery Image",
            modifier = Modifier
                .width(100.dp)
                .height(100.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentScale = contentScale,
        )
    } else {
        Image(
            painter = rememberAsyncImagePainter(model),
            contentScale = contentScale,
            contentDescription = "Grocery image",
            modifier = Modifier
                .height(100.dp)
                .width(100.dp)
                .clip(RoundedCornerShape(10.dp)),
        )
    }

}