package eu.imeon.invaders

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import eu.imeon.invaders.core.*
import eu.imeon.invaders.core.games.BaseGame
import eu.imeon.invaders.core.games.SpaceInvaders
import eu.imeon.invaders.core.sound.SoundPlayer
import eu.imeon.invaders.core.tests.GalactoidSwarmTestRunner
import eu.imeon.invaders.core.tests.PacManTestRunner
import eu.imeon.invaders.core.tests.SmoothCurveGeneratorTestRunner
import eu.imeon.invaders.core.tests.SpriteUnitTestRunner
import eu.imeon.invaders.core.tests.TextRenderTestRunner
import eu.imeon.invaders.core.vscreen.VScreen


enum class GameName {
    SpaceInvaders,
    PacManTestRunner,
    TextRenderTestRunner,
    SpriteUnitTestRunner,
    GalactoidSwarmTestRunner,
    SmoothCurveGeneratorTestRunner
}

class MainView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), Runnable {


    private val soundPlayer = SoundPlayer(context)
    private val themeManager = ThemeManager(context, resources)

    // Setup the default game here
    private var currentGame: BaseGame

    private val gameThread = Thread(this)
    private var playing = false

    init {
        currentGame = gameFactory(GameName.SpaceInvaders)
    }

    private fun gameFactory(gameName: GameName): BaseGame =
        when (gameName) {
            GameName.SpaceInvaders -> SpaceInvaders(
                themeManager.getTheme(ThemeType.SpaceInvaders),
                soundPlayer,
                context
            )
            GameName.PacManTestRunner-> PacManTestRunner(
                themeManager.getTheme(ThemeType.SpaceInvaders),
                soundPlayer,
                context
            )

            GameName.TextRenderTestRunner -> TextRenderTestRunner(
                themeManager.getTheme(ThemeType.SpaceInvaders),
                soundPlayer,
                context
            )

            GameName.SpriteUnitTestRunner -> SpriteUnitTestRunner(
                themeManager.getTheme(ThemeType.SpaceInvaders),
                soundPlayer,
                context
            )

            GameName.GalactoidSwarmTestRunner -> GalactoidSwarmTestRunner(
                themeManager.getTheme(
                    ThemeType.SpaceInvaders
                ), soundPlayer, context
            )

            GameName.SmoothCurveGeneratorTestRunner -> SmoothCurveGeneratorTestRunner(
                themeManager.getTheme(
                    ThemeType.FeatureTest
                ), soundPlayer, context
            )
        }

    private fun switchGame(gameName: GameName) {
        currentGame.shutdown()
        currentGame = gameFactory(gameName)
        currentGame.mainMenuBuilder()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        playing = true
        gameThread.start()
        currentGame.mainMenuBuilder()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        playing = false
        try {
            gameThread.join()
        } catch (e: InterruptedException) {
            Log.e("Error:", "joining thread")
        }

        currentGame.shutdown()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        currentGame.onDraw(canvas)

    }


//    @SuppressLint("ClickableViewAccessibility")
//    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
//        // Transform to VScreen-Space
//        val w=width.toFloat()
//        val h=height.toFloat()
//        val x=motionEvent.x / w * VScreen.width
//        val y=motionEvent.y / h * VScreen.height
//        if (motionEvent.action!=MotionEvent.ACTION_MOVE)
//            Log.i("touchEvents",motionEvent.toString())
//        when (motionEvent.action and MotionEvent.ACTION_MASK) {
//
//            // Player has touched the screen
//            // Or moved their finger while touching screen
//            /*MotionEvent.ACTION_MOVE*/
//
//            // FIXME: Store the related pointerID, so that the UP-events can be associated correctly. Track movements too.
//
//            MotionEvent.ACTION_POINTER_DOWN,
//            MotionEvent.ACTION_DOWN -> {
//                currentGame.decodeTouchAction(x, y, true)
//            }
//
//            // Player has removed finger from screen
//            MotionEvent.ACTION_POINTER_UP,
//            MotionEvent.ACTION_UP -> {
//                currentGame.decodeTouchAction(x, y, false)
//            }
//
//        }
//        return true
//    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        Log.i("MainView", "onKeyDown: ${KeyEvent.keyCodeToString(keyCode)}")
        when (keyCode) {
            KeyEvent.KEYCODE_0 -> {
                switchGame(GameName.SpaceInvaders)
                return true
            }

            KeyEvent.KEYCODE_1 -> {
                switchGame(GameName.TextRenderTestRunner)
                return true
            }

            KeyEvent.KEYCODE_2 -> {
                switchGame(GameName.SpriteUnitTestRunner)
                return true
            }

            KeyEvent.KEYCODE_3 -> {
                switchGame(GameName.GalactoidSwarmTestRunner)
                return true
            }

            KeyEvent.KEYCODE_4 -> {
                switchGame(GameName.SmoothCurveGeneratorTestRunner)
                return true
            }

        }
        return currentGame.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        Log.i("debug", "onKeyUp  : ${KeyEvent.keyCodeToString(keyCode)}")
        return currentGame.onKeyUp(keyCode, event) || super.onKeyUp(keyCode, event)
    }


    override fun run() {
        // This variable tracks the game frame rate
        val fps = 30
        val sleepInterval = (1000 / fps).toLong()
        var t0 = System.currentTimeMillis()

        while (playing) {
            // This is the background thread.
            // Whatever you do here might collide with the foreground thread.
            // No drawing! No manipulation of data that is used by the foreground.
            // That could be solved in other ways, but the simple solution here
            // is to do merely nothing, just trigger the foreground on regular
            // interval.
            Thread.sleep(sleepInterval)

            val t1 = System.currentTimeMillis()
            // Limit deltaT to a maximum, limits unexpected behaviour on debug and other disturbances.
            val deltaT = (((t1 - t0) / 1000.0).toFloat()).coerceIn(0f, .1f)
            t0 = t1

            if (hasWindowFocus()) {
                post {
                    // This lambda will be send as message to the foreground (i.e. main thread)
                    // and then it will be executed there.
                    // Conclusion: The whole game engine runs in foreground.
                    currentGame.onTimerUpdate(deltaT)
                    invalidate()
                }
            }
        }
    }

    private fun pos2area(x: Float, y: Float): Pair<Int, Int> {
        return Pair<Int, Int>(
            (x / width.toFloat() * VScreen.TOUCH_GRID_X).toInt(),
            (y / height.toFloat() * VScreen.TOUCH_GRID_Y).toInt()
        )
    }

    private fun someFingers(motionEvent: MotionEvent, actionIndex:Int, set:Boolean) {
        val nextState = BooleanArray(VScreen.TOUCH_GRID_X * VScreen.TOUCH_GRID_Y)

        for (i in 0 until motionEvent.pointerCount) {
            val px = motionEvent.getX(i)
            val py = motionEvent.getY(i)
            val (ax, ay) = pos2area(px, py)
            val state= if (actionIndex==i) set else true
            nextState[ay * VScreen.TOUCH_GRID_X + ax] = state
        }
        currentGame.updateTouchState(nextState)

//        val info =
//            (0 until motionEvent.pointerCount).joinToString { "${motionEvent.getPointerId(it)}  " }
//
//        val text = nextState.joinToString { if (it) "1" else "-" }
//
//        Log.i("touch", "$info $text")

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        val actionIndex=motionEvent.actionIndex
        when (motionEvent.action and MotionEvent.ACTION_MASK) {

            MotionEvent.ACTION_DOWN -> {
                someFingers(motionEvent, actionIndex, true)
            }

            MotionEvent.ACTION_UP -> {
                someFingers(motionEvent, actionIndex, false)

            }

            MotionEvent.ACTION_MOVE -> {
                someFingers(motionEvent, actionIndex, true)
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                someFingers(motionEvent, actionIndex, true)

            }

            MotionEvent.ACTION_POINTER_UP -> {
                someFingers(motionEvent, actionIndex, false)
            }
        }
        return true
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
    }
}
