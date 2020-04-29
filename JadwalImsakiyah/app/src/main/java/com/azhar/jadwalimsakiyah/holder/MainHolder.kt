package com.azhar.jadwalimsakiyah.holder

import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.azhar.jadwalimsakiyah.R

/**
 * Created by Azhar Rivaldi on 22-12-2019.
 */

class MainHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    @JvmField
    var rlPrayList: RelativeLayout

    @JvmField
    var tvDateMasehi: TextView

    @JvmField
    var tvDateHijri: TextView

    init {
        rlPrayList = itemView.findViewById(R.id.rlPrayList)
        tvDateMasehi = itemView.findViewById(R.id.tvDateMasehi)
        tvDateHijri = itemView.findViewById(R.id.tvDateHijri)
    }
}