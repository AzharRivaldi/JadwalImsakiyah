package com.azhar.jadwalimsakiyah.model

import java.io.Serializable

/**
 * Created by Azhar Rivaldi on 22-12-2019.
 */

class ModelMain : Serializable {

    var txtFajr: String? = null
    var txtDhuhr: String? = null
    var txtAsr: String? = null
    var txtMaghrib: String? = null
    var txtIsha: String? = null
    var txtImsak: String? = null

    @JvmField
    var txtDate: String? = null

    @JvmField
    var txtDay: String? = null
    var txtYear: String? = null
    var txtWeekDay: String? = null

}