package com.felix.themovieshow.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.felix.themovieshow.ui.detail.MovieDetailScreen
import com.felix.themovieshow.ui.favorite.FavoriteScreen
import com.felix.themovieshow.ui.home.HomeScreen
import com.felix.themovieshow.ui.review.ReviewListScreen
import com.felix.themovieshow.ui.viewmore.ViewMoreScreen

object Routes {
    const val HOME = "home"
    const val DETAIL = "detail/{movieId}"
    const val REVIEWS = "reviews/{movieId}"
    const val VIEW_MORE = "viewMore/{category}"
    const val FAVORITES = "favorites"

    fun detail(movieId: Int) = "detail/$movieId"
    fun reviews(movieId: Int) = "reviews/$movieId"
    fun viewMore(category: String) = "viewMore/$category"
}

@Composable
fun TheMovieShowNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onMovieClick = { movie -> navController.navigate(Routes.detail(movie.id)) },
                onSeeAllClick = { category ->
                    navController.navigate(Routes.viewMore(category))
                },
                onFavoriteClick = { navController.navigate(Routes.FAVORITES) }
            )
        }

        composable(Routes.FAVORITES) {
            FavoriteScreen(
                onBackClick = { navController.popBackStack() },
                onMovieClick = { movie -> navController.navigate(Routes.detail(movie.id)) }
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("movieId") { type = NavType.IntType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getInt("movieId") ?: return@composable
            MovieDetailScreen(
                onBackClick = { navController.popBackStack() },
                onSeeAllReviewsClick = { navController.navigate(Routes.reviews(movieId)) }
            )
        }

        composable(
            route = Routes.REVIEWS,
            arguments = listOf(navArgument("movieId") { type = NavType.IntType })
        ) {
            ReviewListScreen(onBackClick = { navController.popBackStack() })
        }

        composable(
            route = Routes.VIEW_MORE,
            arguments = listOf(
                navArgument("category") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "popular"

            val title = if (category == "popular") "Popular Movies" else "Top Rated"

            ViewMoreScreen(
                title = title,
                onBackClick = { navController.popBackStack() },
                onMovieClick = { movie -> navController.navigate(Routes.detail(movie.id)) }
            )
        }
    }
}