package com.sudokuapp.presentation.navigation


import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sudokuapp.domain.model.SudokuDifficulty
import com.sudokuapp.domain.model.SudokuSize
import com.sudokuapp.presentation.screens.game.GameScreen
import com.sudokuapp.presentation.screens.game.GameViewModel
import com.sudokuapp.presentation.screens.home.HomeScreen
import com.sudokuapp.presentation.screens.home.HomeViewModel

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Game : Screen("game/{size}/{difficulty}") {
        fun createRoute(size: SudokuSize, difficulty: SudokuDifficulty): String {
            return "game/${size.name}/${difficulty.name}"
        }
    }
    data object ContinueGame : Screen("game/continue/{gameId}") {
        fun createRoute(gameId: String): String {
            return "game/continue/$gameId"
        }
    }
}

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onStartNewGame = { size, difficulty ->
                    navController.navigate(Screen.Game.createRoute(size, difficulty))
                },
                onContinueGame = { gameId ->
                    navController.navigate(Screen.ContinueGame.createRoute(gameId))
                }
            )
        }

        composable(
            route = Screen.Game.route,
            arguments = listOf(
                navArgument("size") { type = NavType.StringType },
                navArgument("difficulty") { type = NavType.StringType }
            )
        ) {
            val viewModel: GameViewModel = hiltViewModel()
            GameScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable(
            route = Screen.ContinueGame.route,
            arguments = listOf(
                navArgument("gameId") { type = NavType.StringType }
            )
        ) {
            val viewModel: GameViewModel = hiltViewModel()
            GameScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
    }
}