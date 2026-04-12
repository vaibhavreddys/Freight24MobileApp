package com.freight24.connect.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.freight24.connect.BuildConfig
import com.freight24.connect.enum.ConnectTab
import com.freight24.connect.feature.home.HomeScreen
import com.freight24.connect.feature.login_new.view.NewLoginScreen
import com.freight24.connect.feature.login_new.view.NewOtpScreen
import com.freight24.connect.feature.indents.views.CloneIndentScreen
import com.freight24.connect.feature.indents.views.CreateIndentScreen
import com.freight24.connect.feature.indents.views.EditIndentScreen
import com.freight24.connect.feature.indents.views.IndentDetailsScreen
import com.freight24.connect.feature.indents.views.IndentSuccessScreen
import com.freight24.connect.feature.indents.views.ReviewCloneIndentScreen
import com.freight24.connect.feature.indents.views.ReviewIndentScreen
import com.freight24.connect.feature.inbox.views.InboxScreen
import com.freight24.connect.feature.trucks.views.AssignTruckScreen
import com.freight24.connect.feature.trips.views.CreateTripScreen
import com.freight24.connect.feature.trips.views.TripDetailsScreen
import com.freight24.connect.feature.trips.views.TripSuccessScreen
import com.freight24.connect.feature.verificationrequests.views.VerificationRequestsScreen
import com.freight24.connect.feature.indents.views.SearchIndentsScreen
import com.freight24.connect.feature.indents.views.SearchSelectionScreen
import com.freight24.connect.feature.indents.views.SearchType
import com.freight24.connect.feature.login.view.signin.LoginScreen
import com.freight24.connect.feature.login.view.verify.VerifyOtpScreen
import com.freight24.connect.feature.trips.views.CommonPdfViewerScreen
import com.freight24.connect.feature.trips.views.PdfViewerScreen
import com.freight24.connect.feature.trips.views.HaltingChargesScreen
import com.freight24.connect.feature.trips.views.LuChargesScreen
import com.freight24.connect.feature.trips.views.OtherDeductionsScreen
import com.freight24.connect.feature.trucks.views.ManageTrucksScreen
import com.freight24.connect.feature.trucks.views.OnboardTruckScreen

import com.freight24.connect.feature.trucks.views.details.TruckDetailsScreen
import com.freight24.connect.feature.trucks.views.EditTruckScreen
import com.freight24.connect.feature.clients.views.ClientProfileScreen
import com.freight24.connect.feature.clients.views.ManageClientsScreen
import com.freight24.connect.feature.clients.views.OnboardClientScreen
import com.freight24.connect.feature.truckowners.views.ManageTruckOwnersScreen
import com.freight24.connect.feature.truckowners.views.OnboardTruckOwnerScreen
import com.freight24.connect.feature.truckowners.profile.view.ProfileScreen

@Composable
fun ConnectNavGraph(
    navHostController: NavHostController,
    startDestination: String =
        if (BuildConfig.FEATURE_NEW_LOGIN) {
            ConnectDestination.NewLogin.route
        } else {
            ConnectDestination.Login.route
        },
) {
    val authStartDestination =
        if (BuildConfig.FEATURE_NEW_LOGIN) {
            ConnectDestination.NewLogin.route
        } else {
            ConnectDestination.Login.route
        }

    NavHost(
        navController = navHostController,
        startDestination = startDestination,
    ) {
        composable(ConnectDestination.ManageTrucks.route) {
            ManageTrucksScreen(
                navController = navHostController,
                onAddTruckClick = {
                    navHostController.navigate(ConnectDestination.OnboardTruck.route)
                },
                onTruckClick = { truckId ->
                    navHostController.navigate(ConnectDestination.TruckDetails.createRoute(truckId))
                },
                onBackClick = {
                    navHostController.popBackStack()
                }
            )
        }

        composable(ConnectDestination.OnboardTruck.route) {
            OnboardTruckScreen(
                onBackClick = {
                    navHostController.popBackStack()
                },
                onSuccessAction = {
                    navHostController.popBackStack()
                },
                navController = navHostController
            )
        }

        composable(ConnectDestination.ManageClients.route) {
            ManageClientsScreen(
                navController = navHostController,
                onAddClientClick = {
                    navHostController.navigate(ConnectDestination.OnboardClient.route)
                },
                onClientClick = { userId ->
                    navHostController.navigate(ConnectDestination.ClientProfile.createRoute(userId))
                },
                onBackClick = {
                    navHostController.popBackStack()
                }
            )
        }

        composable(ConnectDestination.OnboardClient.route) {
            OnboardClientScreen(
                onBackClick = { navHostController.popBackStack() },
                onCloseSuccess = { navHostController.popBackStack() },
                onViewClientProfile = { userId ->
                    navHostController.navigate(ConnectDestination.ClientProfile.createRoute(userId)) {
                        popUpTo(ConnectDestination.OnboardClient.route) {
                            inclusive = true
                        }
                    }
                },
            )
        }

        composable(ConnectDestination.ManageTruckOwners.route) {
            ManageTruckOwnersScreen(
                onAddTruckOwnerClick = {
                    navHostController.navigate(ConnectDestination.OnboardTruckOwner.route)
                },
                onTruckOwnerClick = { userId ->
                    navHostController.navigate(
                        ConnectDestination.TruckOwnerProfile.createRoute(
                            userId
                        )
                    )
                },
                onBackClick = {
                    navHostController.popBackStack()
                }
            )
        }

        composable(ConnectDestination.OnboardTruckOwner.route) {
            OnboardTruckOwnerScreen(
                onBackClick = { navHostController.popBackStack() },
                onCloseSuccess = { navHostController.popBackStack() },
                onViewTruckOwnerProfile = { userId ->
                    navHostController.navigate(
                        ConnectDestination.TruckOwnerProfile.createRoute(
                            userId
                        )
                    ) {
                        popUpTo(ConnectDestination.OnboardTruckOwner.route) {
                            inclusive = true
                        }
                    }
                },
            )
        }

        composable(
            route = ConnectDestination.TruckOwnerProfile.route,
            arguments = ConnectDestination.TruckOwnerProfile.arguments
        ) {
            ProfileScreen(
                onBackClick = { navHostController.popBackStack() }
            )
        }

        composable(
            route = ConnectDestination.ClientProfile.route,
            arguments = ConnectDestination.ClientProfile.arguments,
        ) {
            ClientProfileScreen(
                onBackClick = { navHostController.popBackStack() },
            )
        }

        composable(
            route = ConnectDestination.TruckDetails.route,
            arguments = ConnectDestination.TruckDetails.arguments
        ) { backStackEntry ->
            val truckId =
                backStackEntry.arguments?.getString(ConnectDestination.TruckDetails.TRUCK_ID_ARG)
                    ?: ""
            TruckDetailsScreen(
                onBackClick = { navHostController.popBackStack() },
                onEditClick = { id ->
                    navHostController.navigate(ConnectDestination.EditTruck.createRoute(id))
                }
            )
        }

        composable(
            route = ConnectDestination.EditTruck.route,
            arguments = ConnectDestination.EditTruck.arguments
        ) { backStackEntry ->
            EditTruckScreen(
                onBackClick = { navHostController.popBackStack() },
                onSuccessAction = {
                    navHostController.popBackStack()
                },
                navController = navHostController
            )
        }

        composable(ConnectDestination.NewLogin.route) {
            NewLoginScreen(
                onOtpRequested = { phoneNumber ->
                    navHostController.navigate(ConnectDestination.NewOtp.createRoute(phoneNumber))
                },
            )
        }

        composable(
            route = ConnectDestination.NewOtp.route,
            arguments = ConnectDestination.NewOtp.arguments,
        ) { backStackEntry ->
            val phoneNumber =
                backStackEntry
                    .arguments
                    ?.getString(ConnectDestination.NewOtp.PHONE_ARG)
                    .orEmpty()

            NewOtpScreen(
                phoneNumber = phoneNumber,
                onBackClick = {
                    navHostController.popBackStack()
                },
                onLoginSuccess = {
                    navHostController.navigate(ConnectDestination.Home.route) {
                        popUpTo(ConnectDestination.NewLogin.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onWrongNumberClick = {
                    navHostController.popBackStack()
                },
            )
        }

        composable(ConnectDestination.Login.route) {
            LoginScreen(onVerify = { phoneNumber ->
                navHostController.navigate(
                    ConnectDestination.VerifyOtp.createRoute(phoneNumber),
                )
            })
        }

        composable(
            route = ConnectDestination.VerifyOtp.route,
            arguments = ConnectDestination.VerifyOtp.arguments,
        ) { backStackEntry ->

            val phoneNumber =
                backStackEntry
                    .arguments
                    ?.getString(ConnectDestination.VerifyOtp.PHONE_ARG)

            VerifyOtpScreen(
                navController = navHostController,
                phoneNumber = phoneNumber ?: "",
                onLoginSuccess = {
                    navHostController.navigate(ConnectDestination.Home.route) {
                        // Clear the back stack to prevent going back to the auth flow
                        popUpTo(ConnectDestination.Login.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(
            route = ConnectDestination.Home.route,
            arguments = ConnectDestination.Home.arguments
        ) { backStackEntry ->
            val tabString = backStackEntry.arguments?.getString(ConnectDestination.Home.TAB_ARG)
            val filterString = backStackEntry.arguments?.getString(ConnectDestination.Home.FILTER_ARG)
            val startTab = tabString?.let { tabId ->
                ConnectTab.entries.find { it.id == tabId }
            }
            val startFilter = filterString?.let { filterId ->
                com.freight24.connect.feature.trips.domain.usecase.TripTypes.entries.find { it.name == filterId }
            }

            HomeScreen(
                onCreateIndentClick = {
                    navHostController.navigate(ConnectDestination.CreateIndent.route)
                },
                onIndentClick = { indentId ->
                    navHostController.navigate(ConnectDestination.IndentDetails.createRoute(indentId))
                },
                onCloneClick = { indentId, indentGroupId ->
                    navHostController.navigate(
                        ConnectDestination.CloneIndent.createRoute(
                            indentId,
                            indentGroupId
                        )
                    )
                },
                onSearchClick = {
                    navHostController.navigate(ConnectDestination.SearchIndents.route)
                },
                onTripClick = { tripId ->
                    navHostController.navigate(ConnectDestination.TripDetails.createRoute(tripId))
                },
                onManageTrucks = {
                    navHostController.navigate(ConnectDestination.ManageTrucks.route)
                },
                onManageClients = {
                    navHostController.navigate(ConnectDestination.ManageClients.route)
                },
                onManageTruckOwners = {
                    navHostController.navigate(ConnectDestination.ManageTruckOwners.route)
                },
                startTab = startTab,
                startFilter = startFilter,
                onLogout = {
                    navHostController.navigate(authStartDestination) {
                        popUpTo(ConnectDestination.Home.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                navController = navHostController
            )
        }

        composable(
            route = ConnectDestination.TripDetails.route,
            arguments = ConnectDestination.TripDetails.arguments
        ) { backStackEntry ->
            val tripId =
                backStackEntry.arguments?.getString(ConnectDestination.TripDetails.TRIP_ID_ARG)
                    ?: ""
            TripDetailsScreen(
                navController = navHostController,
                tripId = tripId,
                onBackClick = { navHostController.popBackStack() },
                onEditClick = { id ->
                    navHostController.navigate(ConnectDestination.EditTrip.createRoute(id))
                },
                onDocumentView = { tripId, docId, status ->
                    navHostController.navigate(
                        ConnectDestination.PdfViewer.createRoute(tripId, docId, status)
                    )
                },
                onHaltingClick = { tripId, truckTypeId, loadingDays, unloadingDays, notes ->
                    navHostController.navigate(
                        ConnectDestination.HaltingCharges.createRoute(tripId, truckTypeId) +
                                "?loadingDays=$loadingDays&unloadingDays=$unloadingDays&notes=$notes"
                    )
                },
                onLuClick = { loadingAmount, unloadingAmount, loadingSlipUrl, unloadingSlipUrl, loadingId, unloadingId, notes ->
                    navHostController.navigate(
                        ConnectDestination.LuCharges.createRoute(
                            tripId = tripId,
                            loadingAmount = loadingAmount,
                            unloadingAmount = unloadingAmount,
                            loadingSlipUrl = loadingSlipUrl,
                            unloadingSlipUrl = unloadingSlipUrl,
                            loadingId = loadingId,
                            unloadingId = unloadingId,
                            notes = notes
                        )
                    )
                },
                onOtherDeductionsClick = { tripId, amount, notes ->
                    navHostController.navigate(
                        ConnectDestination.OtherDeductions.createRoute(tripId, amount, notes)
                    )
                }
            )
        }

        composable(ConnectDestination.VerificationRequests.route) {
            VerificationRequestsScreen(
                onBackClick = { navHostController.popBackStack() }
            )
        }

        composable(ConnectDestination.InterestInbox.route) {
            InboxScreen(
                onBackClick = { navHostController.popBackStack() }
            )
        }

        composable(
            route = ConnectDestination.FilteredTrips.route,
            arguments = ConnectDestination.FilteredTrips.arguments
        ) {
            com.freight24.connect.feature.trips.views.FilteredTripsScreen(
                onBackClick = { navHostController.popBackStack() },
                onTripClick = { tripId ->
                    navHostController.navigate(ConnectDestination.TripDetails.createRoute(tripId))
                }
            )
        }

        composable(
            route = ConnectDestination.CommonPdfViewer.route,
            arguments = ConnectDestination.CommonPdfViewer.arguments
        ) {
            CommonPdfViewerScreen(
                onBackClick = { navHostController.popBackStack() }
            )
        }

        composable(
            route = ConnectDestination.LuCharges.route,
            arguments = ConnectDestination.LuCharges.arguments
        ) { backStackEntry ->
            val tripId =
                backStackEntry.arguments?.getString(ConnectDestination.LuCharges.TRIP_ID_ARG)
                    ?: ""
            LuChargesScreen(
                tripId = tripId,
                onBackClick = { navHostController.popBackStack() },
                onSuccess = {
                    navHostController.previousBackStackEntry?.savedStateHandle?.set(
                        "refresh",
                        true
                    )
                    navHostController.popBackStack()
                },
                onPdfPreview = { title, url, localPath ->
                    val encodedUri = url?.let { android.net.Uri.encode(it) }
                    navHostController.navigate(
                        ConnectDestination.CommonPdfViewer.createRoute(
                            title = title,
                            url = encodedUri,
                            localPath = localPath
                        )
                    )
                }
            )
        }

        composable(
            route = ConnectDestination.HaltingCharges.route + "?loadingDays={loadingDays}&unloadingDays={unloadingDays}&notes={notes}",
            arguments = ConnectDestination.HaltingCharges.arguments + listOf(
                androidx.navigation.navArgument("loadingDays") {
                    type = androidx.navigation.NavType.IntType; defaultValue = 0
                },
                androidx.navigation.navArgument("unloadingDays") {
                    type = androidx.navigation.NavType.IntType; defaultValue = 0
                },
                androidx.navigation.navArgument("notes") {
                    type = androidx.navigation.NavType.StringType; nullable =
                    true; defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val tripId =
                backStackEntry.arguments?.getString(ConnectDestination.HaltingCharges.TRIP_ID_ARG)
                    ?: ""
            val truckTypeId =
                backStackEntry.arguments?.getString(ConnectDestination.HaltingCharges.TRUCK_TYPE_ID_ARG)
                    ?: ""
            val loadingDays = backStackEntry.arguments?.getInt("loadingDays") ?: 0
            val unloadingDays = backStackEntry.arguments?.getInt("unloadingDays") ?: 0
            val notes = backStackEntry.arguments?.getString("notes") ?: ""

            HaltingChargesScreen(
                tripId = tripId,
                truckTypeId = truckTypeId,
                initialLoadingDays = loadingDays,
                initialUnloadingDays = unloadingDays,
                notes = notes,
                onBackClick = { navHostController.popBackStack() },
                onSuccess = {
                    navHostController.previousBackStackEntry?.savedStateHandle?.set(
                        "refresh",
                        true
                    )
                    navHostController.popBackStack()
                }
            )
        }

        composable(
            route = ConnectDestination.OtherDeductions.route,
            arguments = ConnectDestination.OtherDeductions.arguments
        ) { backStackEntry ->
            val tripId =
                backStackEntry.arguments?.getString(ConnectDestination.OtherDeductions.TRIP_ID_ARG)
                    ?: ""
            val amount =
                backStackEntry.arguments?.getString(ConnectDestination.OtherDeductions.AMOUNT_ARG)
                    ?: "0.0"
            val notes =
                backStackEntry.arguments?.getString(ConnectDestination.OtherDeductions.NOTES_ARG)
                    ?: ""

            OtherDeductionsScreen(
                tripId = tripId,
                onBackClick = { navHostController.popBackStack() },
                onSuccess = {
                    navHostController.previousBackStackEntry?.savedStateHandle?.set(
                        "refresh",
                        true
                    )
                    navHostController.popBackStack()
                }
            )
        }

        composable(ConnectDestination.SearchIndents.route) {
            SearchIndentsScreen(
                onBackClick = {
                    navHostController.popBackStack()
                },
                onIndentClick = { indentId ->
                    navHostController.navigate(
                        ConnectDestination.IndentDetails.createRoute(
                            indentId
                        )
                    )
                },
                onCloneClick = { indentId, indentGroupId ->
                    navHostController.navigate(
                        ConnectDestination.CloneIndent.createRoute(
                            indentId,
                            indentGroupId
                        )
                    )
                }
            )
        }

        composable(ConnectDestination.CreateIndent.route) { backStackEntry ->
            CreateIndentScreen(
                onBackClick = {
                    navHostController.popBackStack()
                },
                onNavigateToSearch = { searchType ->
                    navHostController.navigate(
                        ConnectDestination.SearchSelection.createRoute(
                            searchType
                        )
                    )
                },
                onNavigateToReview = {
                    navHostController.navigate(ConnectDestination.ReviewIndent.route)
                },
                navController = navHostController,
            )
        }

        composable(
            route = ConnectDestination.SearchSelection.route,
            arguments = ConnectDestination.SearchSelection.arguments,
        ) { backStackEntry ->
            val searchTypeString =
                backStackEntry
                    .arguments
                    ?.getString(ConnectDestination.SearchSelection.SEARCH_TYPE_ARG) ?: "CLIENT"

            val searchType = SearchType.valueOf(searchTypeString)

            SearchSelectionScreen(
                searchType = searchType,
                onBackClick = {
                    navHostController.popBackStack()
                },
                navController = navHostController,
            )
        }

        composable(ConnectDestination.ReviewIndent.route) {
            ReviewIndentScreen(
                onBackClick = {
                    navHostController.popBackStack()
                },
                onEditClick = {
                    navHostController.popBackStack()
                },
                navController = navHostController,
            )
        }

        composable(
            route = ConnectDestination.IndentSuccess.route,
            arguments = ConnectDestination.IndentSuccess.arguments,
        ) { backStackEntry ->
            val indentId =
                backStackEntry
                    .arguments
                    ?.getString(ConnectDestination.IndentSuccess.INDENT_ID_ARG) ?: ""

            IndentSuccessScreen(
                navController = navHostController,
                indentId = indentId,
                onViewIndentClick = {
                    navHostController.navigate(ConnectDestination.Home.route) {
                        launchSingleTop = true
                    }
                },
                onBackToHomeClick = {
                    navHostController.navigate(ConnectDestination.Home.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = ConnectDestination.IndentDetails.route,
            arguments = ConnectDestination.IndentDetails.arguments,
        ) { backStackEntry ->
            val indentId =
                backStackEntry.arguments?.getString(ConnectDestination.IndentDetails.INDENT_ID_ARG)
                    ?: ""
            IndentDetailsScreen(
                navController = navHostController,
                indentId = indentId,
                onBackClick = {
                    navHostController.popBackStack()
                },
                onEditClick = { id ->
                    navHostController.navigate(ConnectDestination.EditIndent.createRoute(id))
                },
                onCreateTripClick = { id ->
                    navHostController.navigate(ConnectDestination.AssignTruck.createRoute(id))
                }
            )
        }

        composable(
            route = ConnectDestination.EditIndent.route,
            arguments = ConnectDestination.EditIndent.arguments,
        ) { backStackEntry ->
            val indentId =
                backStackEntry.arguments?.getString(ConnectDestination.EditIndent.INDENT_ID_ARG)
                    ?: ""
            EditIndentScreen(
                indentId = indentId,
                onBackClick = { navHostController.popBackStack() },
                onSuccess = {
                    navHostController.previousBackStackEntry?.savedStateHandle?.set(
                        "refresh",
                        true
                    )
                    navHostController.popBackStack()
                }
            )
        }

        composable(
            route = ConnectDestination.CloneIndent.route,
            arguments = ConnectDestination.CloneIndent.arguments,
        ) { backStackEntry ->
            val indentId =
                backStackEntry.arguments?.getString(ConnectDestination.CloneIndent.INDENT_ID_ARG)
                    ?: ""
            val indentGroupId =
                backStackEntry.arguments?.getString(ConnectDestination.CloneIndent.INDENT_GROUP_ID_ARG)
                    ?: ""
            CloneIndentScreen(
                indentId = indentId,
                indentGroupId = indentGroupId,
                onBackClick = { navHostController.popBackStack() },
                onReviewClick = {
                    navHostController.navigate(ConnectDestination.ReviewCloneIndent.route)
                }
            )
        }

        composable(ConnectDestination.ReviewCloneIndent.route) {
            ReviewCloneIndentScreen(
                onBackClick = {
                    navHostController.popBackStack()
                },
                onEditClick = {
                    navHostController.popBackStack()
                },
                navController = navHostController,
            )
        }

        composable(
            route = ConnectDestination.AssignTruck.route,
            arguments = ConnectDestination.AssignTruck.arguments,
        ) { backStackEntry ->
            val indentId =
                backStackEntry.arguments?.getString(ConnectDestination.AssignTruck.INDENT_ID_ARG)
                    ?: ""
            AssignTruckScreen(
                onBackClick = { navHostController.popBackStack() },
                onTruckSelected = { truckId, truckNumber ->
                    val fromEditTrip =
                        navHostController.previousBackStackEntry?.destination?.route == ConnectDestination.EditTrip.route
                    if (fromEditTrip) {
                        navHostController.previousBackStackEntry?.savedStateHandle?.set(
                            "selected_truck_id",
                            truckId
                        )
                        navHostController.previousBackStackEntry?.savedStateHandle?.set(
                            "selected_truck_number",
                            truckNumber
                        )
                        navHostController.popBackStack()
                    } else {
                        navHostController.navigate(
                            ConnectDestination.CreateTrip.createRoute(
                                indentId,
                                truckId
                            )
                        )
                    }
                }
            )
        }

        composable(
            route = ConnectDestination.CreateTrip.route,
            arguments = ConnectDestination.CreateTrip.arguments,
        ) { backStackEntry ->
            val indentId =
                backStackEntry.arguments?.getString(ConnectDestination.CreateTrip.INDENT_ID_ARG)
                    ?: ""
            val truckId =
                backStackEntry.arguments?.getString(ConnectDestination.CreateTrip.TRUCK_ID_ARG)
                    ?: ""
            CreateTripScreen(
                indentId = indentId,
                truckId = truckId,
                onBackClick = { navHostController.popBackStack() },
                onTripCreated = { tripId, truckNumber, origin, destination, driverName, driverPhone ->
                    val route = ConnectDestination.TripSuccess.createRoute(
                        tripId,
                        truckNumber,
                        origin,
                        destination,
                        driverName,
                        driverPhone
                    )
                    navHostController.navigate(route) {
                        popUpTo(ConnectDestination.CreateTrip.route) {
                            inclusive = true
                        } // Remove create trip from stack
                    }
                }
            )
        }

        composable(
            route = ConnectDestination.TripSuccess.route,
            arguments = ConnectDestination.TripSuccess.arguments,
        ) { backStackEntry ->

            val tripId =
                backStackEntry.arguments?.getString(ConnectDestination.TripSuccess.TRIP_ID_ARG)
                    ?: ""
            val truckNumber =
                backStackEntry.arguments?.getString(ConnectDestination.TripSuccess.TRUCK_NUMBER_ARG)
                    ?: ""
            val origin =
                backStackEntry.arguments?.getString(ConnectDestination.TripSuccess.ORIGIN_ARG)
                    ?: ""
            val destination =
                backStackEntry.arguments?.getString(ConnectDestination.TripSuccess.DESTINATION_ARG)
                    ?: ""
            val driverName =
                backStackEntry.arguments?.getString(ConnectDestination.TripSuccess.DRIVER_NAME_ARG)
                    ?: ""
            val driverPhone =
                backStackEntry.arguments?.getString(ConnectDestination.TripSuccess.DRIVER_PHONE_ARG)
                    ?: ""

            TripSuccessScreen(
                tripId = tripId,
                truckNumber = truckNumber,
                origin = origin,
                destination = destination,
                driverName = driverName,
                driverPhone = driverPhone,
                onGoToTripsClick = {
                    navHostController.navigate(ConnectDestination.Home.createRoute("trips")) {
                        popUpTo(ConnectDestination.Home.route) { inclusive = true }
                    }
                },
                onBackToDashboardClick = {
                    navHostController.navigate(ConnectDestination.Home.createRoute("home")) {
                        popUpTo(ConnectDestination.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = ConnectDestination.PdfViewer.route,
            arguments = ConnectDestination.PdfViewer.arguments
        ) {
            PdfViewerScreen(
                onBackClick = { navHostController.popBackStack() },
                onSuccess = {
                    navHostController.previousBackStackEntry?.savedStateHandle?.set(
                        "refresh",
                        true
                    )
                    navHostController.popBackStack()
                }
            )
        }

        composable(
            route = ConnectDestination.EditTrip.route,
            arguments = ConnectDestination.EditTrip.arguments
        ) { backStackEntry ->
            val tripId =
                backStackEntry.arguments?.getString(ConnectDestination.EditTrip.TRIP_ID_ARG)
                    ?: ""
            com.freight24.connect.feature.trips.views.EditTripScreen(
                navController = navHostController,
                backStackEntry = backStackEntry,
                tripId = tripId,
                onBackClick = { navHostController.popBackStack() },
                onNavigateToTruckSearch = { id ->
                    navHostController.navigate(ConnectDestination.AssignTruck.createRoute(id))
                },
                onSuccess = {
                    navHostController.previousBackStackEntry?.savedStateHandle?.set(
                        "refresh",
                        true
                    )
                    navHostController.popBackStack()
                }
            )
        }
    }
}
