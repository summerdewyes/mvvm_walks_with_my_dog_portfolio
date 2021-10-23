package com.summerdewyes.mvvm_walks_with_my_dog.ui.fragments


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.summerdewyes.mvvm_walks_with_my_dog.R
import com.summerdewyes.mvvm_walks_with_my_dog.databinding.FragmentSaveBinding
import com.summerdewyes.mvvm_walks_with_my_dog.db.Run
import com.summerdewyes.mvvm_walks_with_my_dog.other.TrackingUtility
import com.summerdewyes.mvvm_walks_with_my_dog.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

@AndroidEntryPoint
class SaveFragment : Fragment(R.layout.fragment_save) {

    private var _binding: FragmentSaveBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels()

    val args: TrackingFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSaveBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivSuccess.setOnClickListener {
            showSaveTrackingDialog()
        }

        binding.ivDelete.setOnClickListener {
            showCancelTrackingDialog()
        }

        drawArgsRunItem()
    }

    private fun endRunAndSaveToDb() {
        val runItem = args.runItem

        val journal = binding.etJournal.text.toString()
        runItem.journal = journal

        viewModel.insertRun(runItem)

        Snackbar.make(
            requireActivity().findViewById(R.id.rootView),
            "저장했습니다 :)",
            Snackbar.LENGTH_LONG
        ).show()

        findNavController().navigate(
            R.id.action_saveFragment_to_runFragment
        )

    }

    private fun drawArgsRunItem(){
        val runItem = args.runItem

        Glide.with(binding.root).load(runItem.img).into(binding.ivRunImage)
        val calendar = Calendar.getInstance().apply {
            timeInMillis = runItem.timestamp
        }

        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        binding.tvDate.text = dateFormat.format(calendar.time)

        val avgSpeed = "${runItem.avgSpeedInKMH}km/h"
        binding.tvAvgSpeed.text = avgSpeed

        val distanceInkm = "${runItem.distanceInMeters / 1000f}km"
        binding.tvDistance.text = distanceInkm

        binding.tvTime.text = TrackingUtility.getFormattedStopWatchTime(runItem.timeInMillis)

        val caloriesBurned = "${runItem.caloriesBurned}kcal"
        binding.tvCalories.text = caloriesBurned
    }


    /**
     * 산책 저장 다이어로그
     */
    private fun showSaveTrackingDialog() {
        SaveTrackingDialog().apply {
            setYesListener {
                endRunAndSaveToDb()
            }
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG_TAG)
    }

    /**
     * 산책 중지 다이어로그
     */
    private fun showCancelTrackingDialog() {
        CancelTrackingDialog().apply {
            setYesListener {
                findNavController().navigate(
                    R.id.action_saveFragment_to_runFragment
                )
            }
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG_TAG)
    }
}
