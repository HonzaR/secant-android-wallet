package co.electriccoin.zcash.ui

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.electriccoin.zcash.ui.NavigationTargets.ABOUT
import co.electriccoin.zcash.ui.NavigationTargets.HOME
import co.electriccoin.zcash.ui.NavigationTargets.RECEIVE
import co.electriccoin.zcash.ui.NavigationTargets.REQUEST
import co.electriccoin.zcash.ui.NavigationTargets.SCAN
import co.electriccoin.zcash.ui.NavigationTargets.SEED
import co.electriccoin.zcash.ui.NavigationTargets.SEND
import co.electriccoin.zcash.ui.NavigationTargets.SETTINGS
import co.electriccoin.zcash.ui.NavigationTargets.SUPPORT
import co.electriccoin.zcash.ui.NavigationTargets.WALLET_ADDRESS_DETAILS
import co.electriccoin.zcash.ui.configuration.ConfigurationEntries
import co.electriccoin.zcash.ui.configuration.RemoteConfig
import co.electriccoin.zcash.ui.screen.about.WrapAbout
import co.electriccoin.zcash.ui.screen.address.WrapWalletAddresses
import co.electriccoin.zcash.ui.screen.home.WrapHome
import co.electriccoin.zcash.ui.screen.receive.WrapReceive
import co.electriccoin.zcash.ui.screen.request.WrapRequest
import co.electriccoin.zcash.ui.screen.scan.WrapScanValidator
import co.electriccoin.zcash.ui.screen.seed.WrapSeed
import co.electriccoin.zcash.ui.screen.send.WrapSend
import co.electriccoin.zcash.ui.screen.settings.WrapSettings
import co.electriccoin.zcash.ui.screen.support.WrapSupport
import co.electriccoin.zcash.ui.screen.update.WrapCheckForUpdate

@Composable
@Suppress("LongMethod")
internal fun MainActivity.Navigation() {
    val navController = rememberNavController().also {
        // This suppress is necessary, as this is how we set up the nav controller for tests.
        @SuppressLint("RestrictedApi")
        navControllerForTesting = it
    }

    NavHost(navController = navController, startDestination = HOME) {
        composable(HOME) {
            WrapHome(
                goSeedPhrase = { navController.navigateJustOnce(SEED) },
                goSettings = { navController.navigateJustOnce(SETTINGS) },
                goSupport = { navController.navigateJustOnce(SUPPORT) },
                goAbout = { navController.navigateJustOnce(ABOUT) },
                goReceive = { navController.navigateJustOnce(RECEIVE) },
                goSend = { navController.navigateJustOnce(SEND) },
            )

            if (ConfigurationEntries.IS_APP_UPDATE_CHECK_ENABLED.getValue(RemoteConfig.current)) {
                WrapCheckForUpdate()
            }
        }
        composable(WALLET_ADDRESS_DETAILS) {
            WrapWalletAddresses(
                goBack = {
                    navController.popBackStackJustOnce(WALLET_ADDRESS_DETAILS)
                }
            )
        }
        composable(SETTINGS) {
            WrapSettings(
                goBack = {
                    navController.popBackStackJustOnce(SETTINGS)
                }
            )
        }
        composable(SEED) {
            WrapSeed(
                goBack = {
                    navController.popBackStackJustOnce(SEED)
                }
            )
        }
        composable(RECEIVE) {
            WrapReceive(
                onBack = { navController.popBackStackJustOnce(RECEIVE) },
                onAddressDetails = { navController.navigateJustOnce(WALLET_ADDRESS_DETAILS) }
            )
        }
        composable(REQUEST) {
            WrapRequest(goBack = { navController.popBackStackJustOnce(REQUEST) })
        }
        composable(SEND) {
            WrapSend(goBack = { navController.popBackStackJustOnce(SEND) })
        }
        composable(SUPPORT) {
            // Pop back stack won't be right if we deep link into support
            WrapSupport(goBack = { navController.popBackStackJustOnce(SUPPORT) })
        }
        composable(ABOUT) {
            WrapAbout(goBack = { navController.popBackStackJustOnce(ABOUT) })
        }
        composable(SCAN) {
            WrapScanValidator(
                onScanValid = {
                    // TODO [#449] https://github.com/zcash/secant-android-wallet/issues/449
                    navController.navigateJustOnce(SEND) {
                        popUpTo(HOME) { inclusive = false }
                    }
                },
                goBack = { navController.popBackStackJustOnce(SCAN) }
            )
        }
    }
}

private fun NavHostController.navigateJustOnce(
    route: String,
    navOptionsBuilder: (NavOptionsBuilder.() -> Unit)? = null
) {
    if (currentDestination?.route == route) {
        return
    }

    if (navOptionsBuilder != null) {
        navigate(route, navOptionsBuilder)
    } else {
        navigate(route)
    }
}

/**
 * Pops up the current screen from the back stack. Parameter currentRouteToBePopped is meant to be
 * set only to the current screen so we can easily debounce multiple screen popping from the back stack.
 *
 * @param currentRouteToBePopped current screen which should be popped up.
 */
private fun NavHostController.popBackStackJustOnce(currentRouteToBePopped: String) {
    if (currentDestination?.route != currentRouteToBePopped) {
        return
    }
    popBackStack()
}

object NavigationTargets {
    const val HOME = "home"

    const val WALLET_ADDRESS_DETAILS = "wallet_address_details"

    const val SETTINGS = "settings"

    const val SEED = "seed"

    const val RECEIVE = "receive"

    const val REQUEST = "request"

    const val SEND = "send"

    const val SUPPORT = "support"

    const val ABOUT = "about"

    const val SCAN = "scan"
}
