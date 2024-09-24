package com.pabloboo.runtracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.pabloboo.runtracker.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val mainRepository: MainRepository
): ViewModel() {

}