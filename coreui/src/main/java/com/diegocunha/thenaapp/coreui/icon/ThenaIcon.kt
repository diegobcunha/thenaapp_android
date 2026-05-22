package com.diegocunha.thenaapp.coreui.icon

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme

sealed interface ThenaIcon {
    data class Vector(val imageVector: ImageVector) : ThenaIcon
    data class Drawable(@DrawableRes val resId: Int) : ThenaIcon
}

@Composable
fun ThenaIcon(
    icon: ThenaIcon,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = LocalContentColor.current,
) {
    when (icon) {
        is ThenaIcon.Vector -> Icon(icon.imageVector, contentDescription, modifier, tint)
        is ThenaIcon.Drawable -> Icon(painterResource(icon.resId), contentDescription, modifier, tint)
    }
}

@Preview
@Composable
private fun ThenaIconVectorPreview() {
    ThenaTheme {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            ThenaIcon(
                icon = ThenaIcons.Sleep
            )
        }
    }
}

@Preview
@Composable
private fun ThenaIconDrawablePreview() {
    ThenaTheme {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            ThenaIcon(
                icon = ThenaIcons.Nap
            )
        }
    }
}
