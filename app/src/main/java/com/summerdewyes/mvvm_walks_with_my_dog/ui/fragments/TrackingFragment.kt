package com.summerdewyes.mvvm_walks_with_my_dog.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.summerdewyes.mvvm_walks_with_my_dog.R
import com.summerdewyes.mvvm_walks_with_my_dog.databinding.FragmentTrackingBinding
import com.summerdewyes.mvvm_walks_with_my_dog.db.Run
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.ACTION_PAUSE_SERVICE
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.ACTION_STOP_SERVICE
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.MAP_VIEW_BUNDLE_KEY
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.MAP_ZOOM
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.POLYLINE_COLOR
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.POLYLINE_WIDTH
import com.summerdewyes.mvvm_walks_with_my_dog.other.TrackingUtility
import com.summerdewyes.mvvm_walks_with_my_dog.services.Polyline
import com.summerdewyes.mvvm_walks_with_my_dog.services.TrackingService
import com.summerdewyes.mvvm_walks_with_my_dog.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.math.round

const val CANCEL_TRACKING_DIALOG_TAG = "CancelDialog"

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels()

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()

    private var map: GoogleMap? = null

    private var curTimeInMillis = 0L

    @set:Inject
    var weight = 80f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapViewBundle = savedInstanceState?.getBundle(MAP_VIEW_BUNDLE_KEY)
        binding.mapView.onCreate(mapViewBundle)

        binding.btnFinishRun.setOnClickListener {
            val success = zoomToSeeWholeTrack()
            if (success){
                endRunAndSaveToDb()
            } else {
                Snackbar.make(requireView(), "?????? ????????? ?????? ??? ?????????????????? :)", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }

        binding.mapView.getMapAsync { googleMap ->
            map = googleMap
            // Sets the map type to be "hybrid"
            map?.let { map ->
                map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            }
            addAllPolylines()
        }

        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis, true)
            binding.tvTimer.text = formattedTime
        })
    }

    /**
     * isTracking??? ????????? ?????? TrackingService??? ?????? Intent ????????? ???????????????.
     */
    private fun toggleRun() {
        if (isTracking) {
            //binding.miCancelTracking.visibility = View.VISIBLE
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    /**
     * ?????? ????????? ?????? Intent ????????? TrackingService??? ????????????.
     */
    private fun stopRun() {
        binding.tvTimer.text = "00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
    }

    /**
     * isTracking??? ????????? ?????? UI??? ????????????.
     */
    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (isTracking) {
            binding.btnToggleRunTxt.text = "??????"
            binding.btnFinishRun.visibility = View.GONE
        } else if (!isTracking && curTimeInMillis > 0L) {
            binding.btnToggleRunTxt.text = "??????"
            //binding.miCancelTracking.visibility = View.VISIBLE
            binding.btnFinishRun.visibility = View.VISIBLE
        }
    }


    /**
     * ?????? ?????? ????????? ??????????????? ??????????????? ?????? ??????????????? pathPoints ????????? ???????????????.
     */
    private fun addAllPolylines() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }


    /**
     * ???????????? ????????? ?????? ??????????????? ????????????.
     */
    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    /**
     * ???????????? ????????? ?????? ???????????? ???????????????.
     */
    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    var seeWholeTrack = false

    /**
     * ????????? ?????? ????????? ??????????????? ?????????.
     */
    private fun zoomToSeeWholeTrack(): Boolean {
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints) {
            for (point in polyline) {
                bounds.include(point)
            }
        }
        val width = binding.mapView.width
        val height = binding.mapView.height

        return if (seeWholeTrack){
            true
        }else{
            map?.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    bounds.build(),
                    width,
                    height,
                    (height * 0.05f).toInt()
                )
            )
            seeWholeTrack = true
            false
        }

    }

    /**
     * ?????? ????????? ????????????????????? ???????????????.
     */
    private fun endRunAndSaveToDb() {
        map?.snapshot { bmp ->
            var distanceInMeters = 0
            for (polyline in pathPoints) {
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }
            val avgSpeed =
                round((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val dataTimestamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()

            val run = Run(
                bmp,
                dataTimestamp,
                avgSpeed,
                distanceInMeters,
                curTimeInMillis,
                caloriesBurned,
                null
            )

            val bundle = Bundle().apply {
                putSerializable("runItem", run)
            }

            findNavController().navigate(
                R.id.action_trackingFragment_to_saveFragment,
                bundle
            )

            stopRun()
        }
    }


    /**
     * ?????? ?????? ???????????????
     */
    private fun showCancelTrackingDialog() {
        CancelTrackingDialog().apply {
            setYesListener {
                stopRun()
            }
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG_TAG)
    }

    /**
     * TrackingService??? ?????? ??????
     */
    private fun sendCommandToService(action: String) {
        Intent(requireContext(), TrackingService::class.java).also { Intent ->
            Intent.action = action
            requireContext().startService(Intent)
        }
    }


    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        binding.mapView?.onSaveInstanceState(mapViewBundle)
    }
}