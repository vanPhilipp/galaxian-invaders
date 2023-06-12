package eu.imeon.invaders.core.games

import android.content.Context
import eu.imeon.invaders.core.GameObject
import eu.imeon.invaders.core.Theme
import eu.imeon.invaders.core.collision.PerformerType
import eu.imeon.invaders.core.gameobjects.asteroids.AsteroidSwarm
import eu.imeon.invaders.core.gameobjects.asteroids.AsteroidSwarmConfig
import eu.imeon.invaders.core.gameobjects.asteroids.AsteroidsRevivalExaminer
import eu.imeon.invaders.core.gameobjects.galactoids.GalactoidSwarm
import eu.imeon.invaders.core.gameobjects.ghosts.GhostSwarm
import eu.imeon.invaders.core.gameobjects.invaders.InvaderSwarm
import eu.imeon.invaders.core.gameobjects.maze.PacManMaze
import eu.imeon.invaders.core.gameobjects.maze.PacManMazeConfig
import eu.imeon.invaders.core.gameobjects.menu.GameVariant
import eu.imeon.invaders.core.gameobjects.menu.MainMenu
import eu.imeon.invaders.core.gameobjects.mystery.MysteryUfo
import eu.imeon.invaders.core.gameobjects.playership.Pacman
import eu.imeon.invaders.core.gameobjects.playership.PlayerShip
import eu.imeon.invaders.core.gameobjects.playership.RotatingShip
import eu.imeon.invaders.core.gameobjects.shelters.ShelterManager
import eu.imeon.invaders.core.gameobjects.status.StatusPresenter
import eu.imeon.invaders.core.gamestate.GameState
import eu.imeon.invaders.core.gamestate.GlobalGameState
import eu.imeon.invaders.core.sound.SoundPlayer

class SpaceInvaders(theme: Theme, soundPlayer: SoundPlayer, context: Context) :
    BaseGame(theme, soundPlayer, context) {

    // All GameObjects to be created here, not in the BaseGame layer.
    // You may use gameEventLambda, but you don't have to.
    // E.g. you might want to create an alternative or an extended Lambda.

    private val shelterManager = ShelterManager(gameEventLambda)

    private val invaderSwarm = InvaderSwarm(
        theme,
        gameEventLambda,
        missileManager,
        soundPlayer
    )

    private val galactoidSwarm = GalactoidSwarm(
        theme,
        gameEventLambda,
        missileManager,
        particleEngine
    )

    private val asteroidSwarm = AsteroidSwarm(
        theme,
        gameEventLambda
    )

    private val playerShip = PlayerShip(theme, gameEventLambda, missileManager, soundPlayer)
    private val rotatingShip =
        RotatingShip(theme, gameEventLambda, missileManager, soundPlayer, particleEngine)

    private val mysteryUfo = MysteryUfo(theme, gameEventLambda, soundPlayer)
    private val statusPresenter = StatusPresenter(theme, gameStatePersistence)
    private val revivalExaminer = AsteroidsRevivalExaminer()

    private val pacManMaze = PacManMaze()
    private val pacman = Pacman(theme, gameEventLambda, pacManMaze)
    private val ghostSwarm = GhostSwarm(theme, gameEventLambda, pacManMaze, pacman)
    private val mainMenu = MainMenu(theme, gameStatePersistence, gameEventLambda)

    init {
        gameObjectBG = arrayListOf<GameObject>(
            blingBlingStars,
            particleEngine,
            missileManager,
            mysteryUfo,
            superCollider,
        )
        gameObjectFG = arrayListOf<GameObject>(
            statusPresenter
        )

    }


    override fun levelBuilder() {
        val gameCount = 6

        var buildId = GlobalGameState.gameVariant
        var difficultyLevel = GlobalGameState.waves

        if (GlobalGameState.gameVariant == GameVariant.RoundTrip) {
            buildId = GameVariant.values()[GlobalGameState.waves % gameCount]
            difficultyLevel = GlobalGameState.waves / gameCount
        }

        pacManMaze.config= PacManMazeConfig()
        asteroidSwarm.config= AsteroidSwarmConfig()

        var gameObjectLevel= arrayListOf<GameObject>()

        when (buildId) {
            GameVariant.SpaceInvaders -> {
                gameObjectLevel=arrayListOf<GameObject>(
                    playerShip,
                    invaderSwarm,
                    shelterManager
                )
            }

            GameVariant.Galaxians -> {
                gameObjectLevel= arrayListOf<GameObject>(playerShip, galactoidSwarm)
            }
            GameVariant.Asteroids -> {
                gameObjectLevel= arrayListOf<GameObject>(
                    rotatingShip,
                    asteroidSwarm,
                    revivalExaminer
                )
            }

            GameVariant.Pacman -> {
                gameObjectLevel= arrayListOf<GameObject>(pacManMaze, ghostSwarm, pacman)
            }

            GameVariant.PacmanEatingAsteroids -> {
                gameObjectLevel= arrayListOf<GameObject>(
                    pacManMaze,
                    ghostSwarm,
                    pacman,
                    asteroidSwarm
                )
                asteroidSwarm.config= AsteroidSwarmConfig(PerformerType.Static)
                pacManMaze.config= PacManMazeConfig(smallPills = false, powerPills = true)
            }

            GameVariant.PacmanVsGalaxians -> {
                gameObjectLevel=arrayListOf<GameObject>(
                    pacManMaze,
                    ghostSwarm,
                    pacman,
                    galactoidSwarm
                )
                pacManMaze.config=PacManMazeConfig(smallPills = false, powerPills = true)
            }

            else -> throw InternalError("Invalid game variant")
        }

        gameObject = arrayListOf<GameObject>().apply {
            addAll(gameObjectBG)
            addAll(gameObjectLevel)
            addAll(gameObjectFG)
        }

        gameObject.forEach { it.prepareLevel(difficultyLevel) }
    }

    override fun mainMenuBuilder() {
        gameObject = arrayListOf<GameObject>(
            particleEngine,
            mainMenu
        )
        GlobalGameState.setGameState(GameState.GameOver)
        currentController = mainMenu
    }
}
