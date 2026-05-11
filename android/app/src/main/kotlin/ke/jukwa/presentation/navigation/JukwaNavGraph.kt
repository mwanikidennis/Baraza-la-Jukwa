package ke.jukwa.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ke.jukwa.presentation.baraza.BarazaScreen
import ke.jukwa.presentation.myreports.MyReportsScreen
import ke.jukwa.presentation.report.ReportScreen
import ke.jukwa.presentation.settings.SettingsScreen
import ke.jukwa.ui.home.HomeScreen

object Routes {
    const val HOME = "home"
    const val REPORT = "report"
    const val MY_REPORTS = "my_reports"
    const val BARAZA = "baraza"
    const val SETTINGS = "settings"
}

@Composable
fun JukwaNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToReport = { navController.navigate(Routes.REPORT) },
                onNavigateToMyReports = { navController.navigate(Routes.MY_REPORTS) },
                onNavigateToBaraza = { navController.navigate(Routes.BARAZA) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.REPORT) {
            ReportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.MY_REPORTS) {
            MyReportsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.BARAZA) {
            BarazaScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
