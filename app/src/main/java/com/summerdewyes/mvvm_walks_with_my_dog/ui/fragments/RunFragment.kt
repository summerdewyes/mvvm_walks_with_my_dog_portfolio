package com.summerdewyes.mvvm_walks_with_my_dog.ui.fragments

import android.Manifest
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.summerdewyes.mvvm_walks_with_my_dog.R
import com.summerdewyes.mvvm_walks_with_my_dog.adpater.RunAdapter
import com.summerdewyes.mvvm_walks_with_my_dog.db.Run
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.summerdewyes.mvvm_walks_with_my_dog.other.SortType
import com.summerdewyes.mvvm_walks_with_my_dog.other.TrackingUtility
import com.summerdewyes.mvvm_walks_with_my_dog.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_run.*
import kotlinx.android.synthetic.main.fragment_settings.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run), EasyPermissions.PermissionCallbacks {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var runAdapter: RunAdapter

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermissions()
        setupRecyclerView()
        loadNameFromSharedPref()

        /**
         * 필터
         */
        when(viewModel.sortType){
            SortType.DATE -> spFilter.setSelection(0)
            SortType.RUNNING_TIME -> spFilter.setSelection(1)
            SortType.DISTANCE -> spFilter.setSelection(2)
            SortType.AVG_SPEED -> spFilter.setSelection(3)
            SortType.CALORIES_BURNED -> spFilter.setSelection(4)
        }

        spFilter.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                when(pos){
                    0 -> viewModel.sortRuns(SortType.DATE)
                    1 -> viewModel.sortRuns(SortType.RUNNING_TIME)
                    2 -> viewModel.sortRuns(SortType.DISTANCE)
                    3 -> viewModel.sortRuns(SortType.AVG_SPEED)
                    4 -> viewModel.sortRuns(SortType.CALORIES_BURNED)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        viewModel.runs.observe(viewLifecycleOwner, {
            runAdapter.submitList(it)
        })

        fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }
    }


    /**
     * 산책 기록을 데이터베이스에 삭제합니다.
     */
    private val itemTouchHelperCallback = object  : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ){
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.layoutPosition
            val run = runAdapter.differ.currentList[position]
            viewModel.deleteRun(run)
            Snackbar.make(requireView(), "삭제했습니다 :)", Snackbar.LENGTH_LONG).apply {
                setAction("Undo"){
                    viewModel.insertRun(run)
                }
                show()
            }
        }

    }

    /**
     * 리사이클러뷰
     */
    private fun setupRecyclerView() = rvRuns.apply {
        runAdapter = RunAdapter()
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(this)
    }



    private fun loadNameFromSharedPref() {
        val name = sharedPreferences.getString(Constants.KEY_NAME, "")
        tvLetsGo.text = "안녕! ${name}!"
    }

    /**
     * 권한체크
     */
    private fun requestPermissions() {
        if (TrackingUtility.hasLocationPermissions(requireContext())) { // 권한이 있는 경우에는 권한을 요청하지 않습니다.
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "앱을 사용하기 위해서는 위치 권한 허용이 필수입니다.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "앱을 사용하기 위해서는 위치 권한 허용이 필수입니다.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        TODO("Not yet implemented")
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

}