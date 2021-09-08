package com.summerdewyes.mvvm_walks_with_my_dog.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.summerdewyes.mvvm_walks_with_my_dog.R

class CancelTrackingDialog : DialogFragment() {


    private var yesListener: (() -> Unit)? = null
    fun setYesListener(listener: () -> Unit) {
        yesListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("산책을 그만할까요?")
            .setMessage("산책을 그만두고 모든 데이터를 삭제하시겠습니까?")
            .setPositiveButton("네") { _, _ ->
                yesListener?.let { yes ->
                    yes()
                }
            }
            .setNegativeButton("아니요") { dialogInterface, _ ->
                dialogInterface.cancel()
            }.create()

    }
}