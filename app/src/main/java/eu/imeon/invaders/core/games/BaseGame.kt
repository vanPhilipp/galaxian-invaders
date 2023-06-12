package eu.imeon.invaders.core.games

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.Log
import android.view.KeyEvent
import eu.imeon.invaders.core.GameObject
import eu.imeon.invaders.core.Theme
import eu.imeon.invaders.core.collision.SuperCollider
import eu.imeon.invaders.core.effects.FxEngine
import eu.imeon.invaders.core.effects.GameEvent
import eu.imeon.invaders.core.effects.GameEventLambda
import eu.imeon.invaders.core.gameobjects.blingbling.BlingBlingStars
import eu.imeon.invaders.core.gameobjects.missiles.MissileManager
import eu.imeon.invaders.core.gameobjects.particles.ParticleEngine
import eu.imeon.invaders.core.gameobjects.playership.CheatCode
import eu.imeon.invaders.core.gameobjects.playership.Controllable
import eu.imeon.invaders.core.gameobjects.playership.DummyControllable
import eu.imeon.invaders.core.gamestate.GameState
import eu.imeon.invaders.core.gamestate.GameStatePersistence
import eu.imeon.invaders.core.gamestate.GlobalGameState
import eu.imeon.invaders.core.sound.SoundPlayer
import eu.imeon.invaders.core.util.BackdropScaler
import eu.imeon.invaders.core.util.Engine
import eu.imeon.invaders.core.util.Sprite
import eu.imeon.invaders.core.vscreen.VScreen

abstract class BaseGame(
    protected val theme: Theme,
    protected val soundPlayer: SoundPlayer,
    context: Context
) {

    protected val gameEventLambda: GameEventLambda =
        { gameEvent: GameEvent, pos: PointF, severity: Float, sprite: Sprite?, textureId: Int ->

            if (gameEvent.gameState != GameState.None)
                nextGameState = gameEvent.gameState

            if (gameEvent.score > 0) {
                val score = (gameEvent.score * severity).toInt()
                GlobalGameState.score += score

                if (gameEvent.renderScore) {
                    val text = "$score"
                    val paint = Paint().apply {
                        color = theme.textColor
                        typeface = mainFont
                        textSize = 24f
                    }
                    fxEngine.renderTinyScoreText(text, paint, pos)
                }

            }

            fxEngine.createFx(gameEvent, pos, severity, sprite, textureId)

        }


    protected var touchState = BooleanArray(VScreen.TOUCH_GRID_X * VScreen.TOUCH_GRID_Y)

    var paint = Paint().apply { color = Color.WHITE }
    val gameStatePersistence = GameStatePersistence(
        context.getSharedPreferences(
            "Space Invaders",
            Context.MODE_PRIVATE
        )
    )

    var gameObjectBG = arrayListOf<GameObject>()
    var gameObjectFG = arrayListOf<GameObject>()
    var gameObject = arrayListOf<GameObject>()

    var nextGameState = GameState.None

    val mainFont = theme.mainFont
    protected val backdrop = BackdropScaler(theme.backdrop)


    val particleEngine = ParticleEngine()
    val fxEngine = FxEngine(particleEngine, soundPlayer)
    val superCollider = SuperCollider()
    val blingBlingStars = BlingBlingStars(100)
    val missileManager = MissileManager(theme)
    protected var currentController: Controllable = DummyControllable()

    protected var cheatCounter = 0
    protected var isCheatingAllowed = false

    init {
        VScreen.orientation = theme.orientation
    }


    fun onTimerUpdate(deltaT0: Float) {
        // FIXME: (or don't fix me) If deltaT is too large - cause system is overloaded, missile might miss their target.
        val deltaT = if (Engine.freeze) 0f else deltaT0

        if (GlobalGameState.updateGameClocks(deltaT))
            onGameStateChange(GlobalGameState.currentState)

        if (nextGameState != GameState.None) {
            GlobalGameState.setGameState(nextGameState)
            nextGameState = GameState.None
            onGameStateChange(GlobalGameState.currentState)
        }

        gameObject.forEach { it.update(deltaT) }
        gameObject.forEach { it.registerHitables(superCollider) }

        when (GlobalGameState.currentState) {
            GameState.WaveInProgress -> {
                gameObject.forEach { it.checkCollisions(superCollider) }
            }

            GameState.RespawnPhase -> {
                val clearance = gameObject.fold(true) { sum: Boolean, go: GameObject ->
                    sum && go.readyForRespawn(superCollider)
                }
                if (clearance && GlobalGameState.runtime > 1f) {
                    GlobalGameState.setGameState(GameState.WaveInProgress)
                    onGameStateChange(GlobalGameState.currentState)
                }
            }

            else -> {}
        }
    }


    fun shutdown() {
        soundPlayer.shutdown()

        gameStatePersistence.save()
    }

    fun prepareGame() {
        GlobalGameState.waves = 0
        levelBuilder()
        gameObject.forEach { it.prepareGame() }
        gameObject.forEach { it.prepareLevel(GlobalGameState.waves,) }
        GlobalGameState.setGameState(GameState.None)
        nextGameState = GameState.WaveStarting
    }

    fun prepareLevel() {
        levelBuilder()
        currentController = gameObject.find { it is Controllable } as Controllable
        gameObject.forEach { it.prepareLevel(GlobalGameState.waves,) }
    }

    abstract fun levelBuilder()

    open fun mainMenuBuilder(){
        TODO("Implement mainMenuBuilder")

    }

    open fun onGameStateChange(gameState: GameState) {
        val paint = Paint().apply {
            color = theme.textColor
            typeface = mainFont
            textSize = 48f
        }
        // .1f, 1f
        fxEngine.renderBigCenterText(GlobalGameState.message(theme.languageId), paint)
        when (gameState) {
            GameState.PlayerShipExplodes -> {
                missileManager.flush()
            }

            GameState.PlayerDefeated -> {
                missileManager.flush()
            }

            GameState.WaveCompleted -> {
                missileManager.flush()
                GlobalGameState.waves++
            }

            GameState.GameOver -> {
                fxEngine.createFx(GameEvent.GameOver, PointF(0f, 0f), 1f, null, 0)
                val score = GlobalGameState.score
                if (score > gameStatePersistence.highScore.value) {
                    gameStatePersistence.highScore.value = score
                }
            }

            GameState.RespawnPhase -> {}
            GameState.GameStarting -> prepareGame()
            GameState.WaveStarting -> prepareLevel()
            GameState.WaveInProgress -> {
                currentController.onControllerRestart()
            }

            else -> {}
        }
    }

    open fun onDraw(canvas: Canvas) {
        paint.color = Color.argb(255, 255, 255, 255)
        //canvas.drawColor(Color.argb(255, 0, 0, 0))
        backdrop.draw(canvas, paint)

        paint.typeface = mainFont

        gameObject.forEach { it.draw(canvas) }

        if (Engine.drawDebug)
            gameObject.forEach { it.drawDebug(canvas) }

        drawTouchState(canvas)
    }


    private fun onTouchAction(areaId: Int, isPush: Boolean) {
        when (areaId) {
            0 -> mainMenuBuilder()

            3 -> {
                if (isPush) {
                    cheatCounter++
                    if (cheatCounter == 5) {
                        cheatCounter = 0
                        isCheatingAllowed = !isCheatingAllowed
                        val paint = Paint().apply {
                            color = theme.textColor
                            typeface = mainFont
                            textSize = 48f
                        }
                        fxEngine.renderBigCenterText("Cheat Mode: $isCheatingAllowed", paint)
                    }
                }
            }

            2 -> {
                if (isPush && isCheatingAllowed)
                    GlobalGameState.godMode = !GlobalGameState.godMode
            }

            6 -> {
                if (isPush && isCheatingAllowed)
                    nextGameState = GameState.WaveCompleted
            }

            10 -> currentController.onControllerTilt(if (isPush) -1 else 0)

            11 -> currentController.onControllerThrust(isPush)

            12 -> currentController.onControllerPan(if (isPush) -1 else 0)

            13 -> currentController.onControllerPan(if (isPush) 1 else 0)

            14 -> currentController.onControllerTilt(if (isPush) 1 else 0)

            15 -> if (isPush) currentController.onControllerFire()
        }
    }

    fun updateTouchState(nextState: BooleanArray) {

        for (i in nextState.indices) {
            if (touchState[i] && !nextState[i]) {
                onTouchAction(i, false)
            }
        }
        for (i in nextState.indices) {
            if (!touchState[i] && nextState[i]) {
                onTouchAction(i, true)
            }
        }

        touchState = nextState
    }

    private fun drawTouchState(canvas: Canvas) {
        paint.apply {
            color = Color.argb(32, 255, 255, 255)
            paint.style = Paint.Style.FILL
        }

        for (y in 0 until VScreen.TOUCH_GRID_Y) {
            for (x in 0 until VScreen.TOUCH_GRID_X) {
                val sx = canvas.width.toFloat() / VScreen.TOUCH_GRID_X
                val sy = canvas.height.toFloat() / VScreen.TOUCH_GRID_Y
                val deltaX = canvas.width * .01f
                val deltaY = canvas.height * .01f
                val rect = RectF(sx * x, sy * y, sx * (x + 1), sy * (y + 1))
                rect.inset(deltaX, deltaY)
                val n = y * VScreen.TOUCH_GRID_X + x
                if (touchState[n])
                    canvas.drawRect(rect, paint)
            }
        }
    }

    fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        Log.i("SpaceInvaders", "SI-onKeyDown: ${KeyEvent.keyCodeToString(keyCode)}")
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                currentController.onControllerPan(-1)
                true
            }

            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                currentController.onControllerPan(1)
                true
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                currentController.onControllerTilt(-1)
                true
            }

            KeyEvent.KEYCODE_DPAD_DOWN -> {
                currentController.onControllerTilt(1)
                true
            }

            KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_Y, KeyEvent.KEYCODE_X -> {
                currentController.onControllerFire()
                true
            }

            KeyEvent.KEYCODE_A -> {
                gameObject.onEach { it.onCheatCode(CheatCode.KillAlien) }
                true
            }

            KeyEvent.KEYCODE_C -> {
                currentController.onControllerThrust(true)
                true
            }

            KeyEvent.KEYCODE_D -> {
                Engine.drawDebug = !Engine.drawDebug
                true
            }

            KeyEvent.KEYCODE_G -> {
                GlobalGameState.godMode = !GlobalGameState.godMode
                true
            }

            KeyEvent.KEYCODE_L -> {
                gameObject.onEach { it.onCheatCode(CheatCode.LazyAliens) }
                true
            }

            KeyEvent.KEYCODE_P -> {
                GlobalGameState.peaceOnEarth = !GlobalGameState.peaceOnEarth
                true
            }

            KeyEvent.KEYCODE_R,
            KeyEvent.KEYCODE_BUTTON_START -> {
                prepareGame()
                true
            }

            KeyEvent.KEYCODE_W -> {
                nextGameState = GameState.WaveCompleted
                true
            }

            KeyEvent.KEYCODE_Z -> {
                // Shoot yourself. Hope this does not create recursion issues :-)
                currentController.onControllerKill()
                true
            }

            else -> {
                false
            }
        }
    }

    fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        Log.i("debug", "onKeyUp  : ${KeyEvent.keyCodeToString(keyCode)}")

        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT -> {
                currentController.onControllerPan(0)
                true
            }

            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN -> {
                currentController.onControllerTilt(0)
                true
            }

            KeyEvent.KEYCODE_C -> {
                currentController.onControllerThrust(false)
                true
            }

            KeyEvent.KEYCODE_X, KeyEvent.KEYCODE_SPACE -> {
                currentController.onControllerFire()
                true
            }

            else -> false
        }
    }

}