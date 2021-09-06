package com.summerdewyes.mvvm_walks_with_my_dog.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.summerdewyes.mvvm_walks_with_my_dog.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepository: MainRepository
) : ViewModel() {

}