package eu.imeon.invaders.core

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.util.Size
import android.util.SizeF
import eu.imeon.invaders.R
import eu.imeon.invaders.core.util.Sprite
import eu.imeon.invaders.core.vscreen.VScreen
import eu.imeon.invaders.core.vscreen.VScreenOrientation

enum class ThemeType() {
    SpaceInvaders,
    FeatureTest
}

data class Theme(
    val orientation: VScreenOrientation,
    val invaders: List<Sprite>,
    val invaderTypes: List<Int>,
    val invaderGrid: SizeF,
    val missiles: List<Sprite>,
    val mysteryUfo: Sprite,
    val player: Sprite,
    val mainFont: Typeface,
    val textColor: Int,
    val backdrop: Bitmap,
    val languageId: Int
) {
}

class ThemeManager(context: Context, resources: Resources) {

    private val themes = mutableMapOf<ThemeType, Theme>()

    init {
        
        val base0=VScreen.width*1.7f
        val base1=VScreen.width*1.0f
        
        val invaderWidth = base0 * .03f
        val invaderGrid=SizeF(invaderWidth*1.6f, base0 * .04f)
        val mysteryUfoWidth = base0 * .05f
        val playerWidth = base0 * .04f

        val missiles = arrayOf(
            R.drawable.shoot_player,
            R.drawable.shoot_invader,
            R.drawable.particle_green,
            R.drawable.particle_red
        )
            .map { Sprite(context.resources, it, base1 * .02f, Size(1, 1)) }

        val f0=16f/24f
        val f1=22f/24f
        val f2=24f/24f
        val classicInvaders = listOf(
            Sprite(context.resources, R.drawable.invader_green_1, invaderWidth*f0, Size(4, 1)),
            Sprite(context.resources, R.drawable.invader_cyan_1, invaderWidth*f1, Size(4, 1)),
            Sprite(context.resources, R.drawable.invader_magenta_1, invaderWidth*f2, Size(4, 1))
        )


        themes[ThemeType.SpaceInvaders] = Theme(
            VScreenOrientation.VERTICAL,
            classicInvaders,
            listOf<Int>(0, 1, 1, 2, 2),
            invaderGrid,
            missiles,
            Sprite(context.resources, R.drawable.mystery_ufo, mysteryUfoWidth, Size(1, 1)),
            Sprite(context.resources, R.drawable.playership, playerWidth, Size(1, 1)),
            resources.getFont(R.font.press_start_2p),
            Color.WHITE,
            BitmapFactory.decodeResource(resources, R.drawable.planet_gf19536b93_1920),
            0
        )

        themes[ThemeType.FeatureTest] = Theme(
            VScreenOrientation.HORIZONTAL,
            classicInvaders,
            listOf<Int>(0, 1, 1, 2, 2),
            invaderGrid,
            missiles,
            Sprite(context.resources, R.drawable.mystery_ufo, mysteryUfoWidth, Size(1, 1)),
            Sprite(context.resources, R.drawable.playership, playerWidth, Size(1, 1)),
            resources.getFont(R.font.press_start_2p),
            Color.WHITE,
            BitmapFactory.decodeResource(resources, R.drawable.planet_gf19536b93_1920),
            0
        )
    }

    fun getTheme(type: ThemeType): Theme {
        return themes[type] ?: themes[ThemeType.SpaceInvaders]!!
    }
}
