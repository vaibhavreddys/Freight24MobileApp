package com.freight24.connect.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class ConnectDestination(val route: String) {
    open val arguments = emptyList<NamedNavArgument>()

    data object Login : ConnectDestination("login")

    data object Home : ConnectDestination("home?tab={tab}") {
        const val TAB_ARG = "tab"
        override val arguments = listOf(
            navArgument(TAB_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
        
        fun createRoute(tab: String? = null): String {
            return if (tab != null) "home?tab=$tab" else "home"
        }
    }

    data object CreateIndent : ConnectDestination("create_indent")

    data object VerifyOtp : ConnectDestination("verifyOtp/{phone}") {
        const val PHONE_ARG = "phone"
        override val arguments =
            listOf(
                navArgument(PHONE_ARG) {
                    type = NavType.StringType
                },
            )

        fun createRoute(phone: String): String {
            return "verifyOtp/$phone"
        }
    }

    data object SearchSelection : ConnectDestination("search/{searchType}") {
        const val SEARCH_TYPE_ARG = "searchType"
        override val arguments =
            listOf(
                navArgument(SEARCH_TYPE_ARG) {
                    type = NavType.StringType
                },
            )

        fun createRoute(searchType: String): String {
            return "search/$searchType"
        }
    }

    data object ReviewIndent : ConnectDestination("reviewIndent")

    data object IndentSuccess : ConnectDestination("indentSuccess/{indentId}") {
        const val INDENT_ID_ARG = "indentId"
        override val arguments =
            listOf(
                navArgument(INDENT_ID_ARG) {
                    type = NavType.StringType
                },
            )

        fun createRoute(indentId: String): String {
            return "indentSuccess/$indentId"
        }
    }

    data object Indents : ConnectDestination("indents")
    data object IndentDetails : ConnectDestination("indent_details/{indentId}") {
        const val INDENT_ID_ARG = "indentId"
        override val arguments = listOf(
            navArgument(INDENT_ID_ARG) { type = NavType.StringType }
        )
        fun createRoute(indentId: String) = "indent_details/$indentId"
    }

    data object EditIndent : ConnectDestination("edit_indent/{indentId}") {
        const val INDENT_ID_ARG = "indentId"
        override val arguments = listOf(
            navArgument(INDENT_ID_ARG) { type = NavType.StringType }
        )
        fun createRoute(indentId: String) = "edit_indent/$indentId"
    }

    data object CloneIndent : ConnectDestination("clone_indent/{indentId}/{indentGroupId}") {
        const val INDENT_ID_ARG = "indentId"
        const val INDENT_GROUP_ID_ARG = "indentGroupId"
        override val arguments = listOf(
            navArgument(INDENT_ID_ARG) { type = NavType.StringType },
            navArgument(INDENT_GROUP_ID_ARG) { type = NavType.StringType }
        )
        fun createRoute(indentId: String, indentGroupId: String) = "clone_indent/$indentId/$indentGroupId"
    }

    data object ReviewCloneIndent : ConnectDestination("review_clone_indent")

    data object SearchIndents : ConnectDestination("search_indents") {
        const val INDENT_ID_ARG = "indentId"
        const val INDENT_GROUP_ID_ARG = "indentGroupId"
        override val arguments = listOf(
            navArgument(INDENT_ID_ARG) { type = NavType.StringType },
            navArgument(INDENT_GROUP_ID_ARG) { type = NavType.StringType }
        )
        fun createRoute(indentId: String, indentGroupId: String) = "clone_indent/$indentId/$indentGroupId"
    }

    data object AssignTruck : ConnectDestination("assign_truck/{indentId}") {
        const val INDENT_ID_ARG = "indentId"
        override val arguments = listOf(
            navArgument(INDENT_ID_ARG) { type = NavType.StringType }
        )
        fun createRoute(indentId: String) = "assign_truck/$indentId"
    }

    data object CreateTrip : ConnectDestination("create_trip/{indentId}/{truckId}") {
        const val INDENT_ID_ARG = "indentId"
        const val TRUCK_ID_ARG = "truckId"
        override val arguments = listOf(
            navArgument(INDENT_ID_ARG) { type = NavType.StringType },
            navArgument(TRUCK_ID_ARG) { type = NavType.StringType }
        )
        fun createRoute(indentId: String, truckId: String) = "create_trip/$indentId/$truckId"
    }

    data object TripSuccess : ConnectDestination(
        "trip_success/{tripId}?truckNumber={truckNumber}&origin={origin}&destination={destination}&driverName={driverName}&driverPhone={driverPhone}"
    ) {
        const val TRIP_ID_ARG = "tripId"
        const val TRUCK_NUMBER_ARG = "truckNumber"
        const val ORIGIN_ARG = "origin"
        const val DESTINATION_ARG = "destination"
        const val DRIVER_NAME_ARG = "driverName"
        const val DRIVER_PHONE_ARG = "driverPhone"

        override val arguments = listOf(
            navArgument(TRIP_ID_ARG) { type = NavType.StringType },
            navArgument(TRUCK_NUMBER_ARG) { type = NavType.StringType },
            navArgument(ORIGIN_ARG) { type = NavType.StringType },
            navArgument(DESTINATION_ARG) { type = NavType.StringType },
            navArgument(DRIVER_NAME_ARG) { type = NavType.StringType },
            navArgument(DRIVER_PHONE_ARG) { type = NavType.StringType }
        )

        fun createRoute(
            tripId: String,
            truckNumber: String,
            origin: String,
            destination: String,
            driverName: String,
            driverPhone: String
        ): String {
            return "trip_success/$tripId?truckNumber=$truckNumber&origin=$origin&destination=$destination&driverName=$driverName&driverPhone=$driverPhone"
        }
    }

    data object TripDetails : ConnectDestination("trip_details/{tripId}") {
        const val TRIP_ID_ARG = "tripId"
        override val arguments = listOf(
            navArgument(TRIP_ID_ARG) { type = NavType.StringType }
        )
        fun createRoute(tripId: String) = "trip_details/$tripId"
    }

    data object EditTrip : ConnectDestination("edit_trip/{tripId}") {
        const val TRIP_ID_ARG = "tripId"
        override val arguments = listOf(
            navArgument(TRIP_ID_ARG) { type = NavType.StringType }
        )
        fun createRoute(tripId: String) = "edit_trip/$tripId"
    }

    data object PdfViewer : ConnectDestination("pdf_viewer/{tripId}/{documentId}?status={status}&localPath={localPath}") {
        const val TRIP_ID_ARG = "tripId"
        const val DOCUMENT_ID_ARG = "documentId"
        const val STATUS_ARG = "status"
        const val LOCAL_PATH_ARG = "localPath"
        override val arguments = listOf(
            navArgument(TRIP_ID_ARG) { type = NavType.StringType },
            navArgument(DOCUMENT_ID_ARG) { type = NavType.StringType },
            navArgument(STATUS_ARG) { type = NavType.StringType; nullable = true; defaultValue = null },
            navArgument(LOCAL_PATH_ARG) { type = NavType.StringType; nullable = true; defaultValue = null }
        )
        fun createRoute(tripId: String, documentId: String, status: String? = null, localPath: String? = null) = 
            "pdf_viewer/$tripId/$documentId" + 
            "?status=${status ?: ""}" + 
            "&localPath=${localPath ?: ""}"
    }

    data object CommonPdfViewer : ConnectDestination("common_pdf_viewer?title={title}&url={url}&localPath={localPath}") {
        const val TITLE_ARG = "title"
        const val URL_ARG = "url"
        const val LOCAL_PATH_ARG = "localPath"
        override val arguments = listOf(
            navArgument(TITLE_ARG) { type = NavType.StringType; nullable = true; defaultValue = null },
            navArgument(URL_ARG) { type = NavType.StringType; nullable = true; defaultValue = null },
            navArgument(LOCAL_PATH_ARG) { type = NavType.StringType; nullable = true; defaultValue = null }
        )
        fun createRoute(title: String, url: String? = null, localPath: String? = null) = 
            "common_pdf_viewer" + 
            "?title=${title}" + 
            "&url=${url ?: ""}" + 
            "&localPath=${localPath ?: ""}"
    }

    data object HaltingCharges : ConnectDestination("halting_charges/{tripId}/{truckTypeId}") {
        const val TRIP_ID_ARG = "tripId"
        const val TRUCK_TYPE_ID_ARG = "truckTypeId"
        override val arguments = listOf(
            navArgument(TRIP_ID_ARG) { type = NavType.StringType },
            navArgument(TRUCK_TYPE_ID_ARG) { type = NavType.StringType }
        )
        fun createRoute(tripId: String, truckTypeId: String) = "halting_charges/$tripId/$truckTypeId"
    }

    data object LuCharges : ConnectDestination("" +
            "lu_charges/{tripId}?loadingAmount={loadingAmount}&unloadingAmount={unloadingAmount}" +
            "&loadingSlipUrl={loadingSlipUrl}&unloadingSlipUrl={unloadingSlipUrl}" +
            "&loadingId={loadingId}&unloadingId={unloadingId}&notes={notes}"
    ) {
        const val TRIP_ID_ARG = "tripId"
        const val LOADING_AMOUNT_ARG = "loadingAmount"
        const val UNLOADING_AMOUNT_ARG = "unloadingAmount"
        const val LOADING_SLIP_URL_ARG = "loadingSlipUrl"
        const val UNLOADING_SLIP_URL_ARG = "unloadingSlipUrl"
        const val LOADING_ID = "loadingId"
        const val UNLOADING_ID = "unloadingId"
        const val NOTES = "notes"
        override val arguments = listOf(
            navArgument(TRIP_ID_ARG) { type = NavType.StringType },
            navArgument(LOADING_AMOUNT_ARG) { type = NavType.StringType; nullable = true; defaultValue = null },
            navArgument(UNLOADING_AMOUNT_ARG) { type = NavType.StringType; nullable = true; defaultValue = null },
            navArgument(LOADING_SLIP_URL_ARG) { type = NavType.StringType; nullable = true; defaultValue = null },
            navArgument(UNLOADING_SLIP_URL_ARG) { type = NavType.StringType; nullable = true; defaultValue = null },
            navArgument(LOADING_ID) { type = NavType.StringType; nullable = true; defaultValue = null },
            navArgument(UNLOADING_ID) { type = NavType.StringType; nullable = true; defaultValue = null },
            navArgument(NOTES) { type = NavType.StringType; nullable = true; defaultValue = "" }
        )
        fun createRoute(
            tripId: String, 
            loadingAmount: String? = null, 
            unloadingAmount: String? = null,
            loadingSlipUrl: String? = null,
            unloadingSlipUrl: String? = null,
            loadingId: String? = null,
            unloadingId: String? = null,
            notes: String? = null
        ) = "lu_charges/$tripId" + 
            "?loadingAmount=${loadingAmount ?: ""}" + 
            "&unloadingAmount=${unloadingAmount ?: ""}" +
            "&loadingSlipUrl=${loadingSlipUrl ?: ""}" +
            "&unloadingSlipUrl=${unloadingSlipUrl ?: ""}" +
            "&loadingId=${loadingId ?: ""}" +
            "&unloadingId=${unloadingId ?: ""}" +
            "&notes=${notes ?: ""}"
    }

    data object OtherDeductions : ConnectDestination("other_deductions/{tripId}?amount={amount}&notes={notes}") {
        const val TRIP_ID_ARG = "tripId"
        const val AMOUNT_ARG = "amount"
        const val NOTES_ARG = "notes"
        override val arguments = listOf(
            navArgument(TRIP_ID_ARG) { type = NavType.StringType },
            navArgument(AMOUNT_ARG) { type = NavType.StringType; nullable = true; defaultValue = "0" },
            navArgument(NOTES_ARG) { type = NavType.StringType; nullable = true; defaultValue = "" }
        )
        fun createRoute(tripId: String, amount: String? = null, notes: String? = null) = 
            "other_deductions/$tripId?amount=${amount ?: "0"}&notes=${notes ?: ""}"
    }

    data object InterestInbox : ConnectDestination("interest_inbox")

    data object FindTrucks : ConnectDestination("find_trucks")
    data object ManageTrucks : ConnectDestination("manage_trucks")
    data object OnboardTruck : ConnectDestination("onboard_truck")

    data object TruckDetails : ConnectDestination("truck_details/{truckId}") {
        const val TRUCK_ID_ARG = "truckId"
        override val arguments = listOf(
            navArgument(TRUCK_ID_ARG) { type = NavType.StringType }
        )
        fun createRoute(truckId: String) = "truck_details/$truckId"
    }

    data object EditTruck : ConnectDestination("edit_truck/{truckId}") {
        const val TRUCK_ID_ARG = "truckId"
        override val arguments = listOf(
            navArgument(TRUCK_ID_ARG) { type = NavType.StringType }
        )
        fun createRoute(truckId: String) = "edit_truck/$truckId"
    }

    data object ClientProfile : ConnectDestination("client_profile/{userId}") {
        const val USER_ID_ARG = "userId"
        override val arguments = listOf(
            navArgument(USER_ID_ARG) { type = NavType.StringType }
        )

        fun createRoute(userId: String) = "client_profile/$userId"
    }

    data object ManageClients : ConnectDestination("manage_clients")
    data object OnboardClient : ConnectDestination("onboard_client")
    data object ManageTruckOwners : ConnectDestination("manage_truck_owners")
    data object OnboardTruckOwner : ConnectDestination("onboard_truck_owner")

    data object TruckOwnerProfile : ConnectDestination("truck_owner_profile/{userId}") {
        const val USER_ID_ARG = "userId"
        override val arguments = listOf(
            navArgument(USER_ID_ARG) { type = NavType.StringType }
        )

        fun createRoute(userId: String) = "truck_owner_profile/$userId"
    }
}
