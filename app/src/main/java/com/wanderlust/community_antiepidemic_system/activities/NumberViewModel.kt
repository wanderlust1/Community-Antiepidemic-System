package com.wanderlust.community_antiepidemic_system.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class NumberViewModel: ViewModel() {

    var mNum = 0

    class NumberViewModelFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return modelClass.newInstance() //使用newInstance反射实例ViewModel，并且传出去

        }
    }

}