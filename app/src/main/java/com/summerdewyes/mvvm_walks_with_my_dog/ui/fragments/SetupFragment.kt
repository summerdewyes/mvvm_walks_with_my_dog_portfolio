package com.summerdewyes.mvvm_walks_with_my_dog.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.summerdewyes.mvvm_walks_with_my_dog.R
import com.summerdewyes.mvvm_walks_with_my_dog.databinding.FragmentSetupBinding
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.KEY_NAME
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.KEY_WEIGHT
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup) {

    private var _binding: FragmentSetupBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sharedPref: SharedPreferences

    @set:Inject
    var isFirstAppOpen = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isFirstAppOpen){
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()
            findNavController().navigate(
                R.id.action_setupFragment_to_runFragment,
                savedInstanceState,
                navOptions
            )
        }

        binding.continueLayout.setOnClickListener {
            val success = writePersonalDateToSharedPref() // writePersonalDateToSharedPref()함수의 타입이 Boolean
            if(success){
                findNavController().navigate(R.id.action_setupFragment_to_runFragment) // 네비게이션 action
            } else {
                Snackbar.make(requireView(), "이름과 몸무게를 모두 입력해주세요 :)", Snackbar.LENGTH_SHORT).show()
            }
        }
    }


    private fun writePersonalDateToSharedPref() : Boolean {
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()
        if (name.isEmpty() || weight.isEmpty()){
            return false
        }
        sharedPref.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
            .apply()
        return true
    }
}