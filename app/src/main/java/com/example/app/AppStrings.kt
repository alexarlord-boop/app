package com.example.app

class AppStrings {
    companion object {
        //        const val version = "1.2.1"
        const val version = "1.3.3" // улучшил обработку ведомости - нулевые значения
        const val deviceDirectory = "storage/emulated/0/download/noe/"
        const val branchesFS = "${deviceDirectory}branches.json"

        const val branchList = "https://indman.nokes.ru/engine/IndManCompanies.php"
        const val controllersByBranch =
            "https://indman.nokes.ru/engine/IndManStaffByCompany_Lnk.php?Company_Lnk="

        const val recordsByListId = "https://indman.nokes.ru/engine/IndManDataByListNumber.php"
        const val controllers = "https://indman.nokes.ru/engine/IndManListsStaffOnly.php"
        const val statementsByControllerId =
            "https://indman.nokes.ru/engine/IndManListsByStaff_Lnk.php?Staff_Lnk="


        const val updateData = "https://indman.nokes.ru/engine/IndManDataUpdate.php"
    }
}

class AppUIResponses {
    companion object {
        const val noInternetConnection: String = "No internet connection"
        const val noNetworkAvailable: String = "No network available"
    }
}