package com.peer_messanger.ui.fragments

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.peer_messanger.R
import com.peer_messanger.data.model.Device
import com.peer_messanger.data.wrapper.ScanResource
import com.peer_messanger.databinding.FragmentFindingBinding
import com.peer_messanger.ui.adapters.FindingRecyclerViewAdapter
import com.peer_messanger.ui.listener.BluetoothDeviceItemListener
import com.peer_messanger.ui.vm.MainViewModel
import com.peer_messanger.util.PERMISSION_REQUEST_CODE
import com.peer_messanger.util.singleButtonAlertDialog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class FindingFragment : Fragment(),
    BluetoothDeviceItemListener {


    private val availableRecyclerViewAdapter by lazy { FindingRecyclerViewAdapter(this) }
    private val pairedRecyclerViewAdapter by lazy { FindingRecyclerViewAdapter(this) }

    private var _vBinding: FragmentFindingBinding? = null
    private val vBinding get() = _vBinding!!

    private val vModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecyclerViews()

        setListeners()

        setObservers()

    }

    private fun setObservers() {
        val foundedDevices = ArrayList<Device>()

        lifecycleScope.launch {
            vModel.scanFlow.collect {
                when (it) {
                    is ScanResource.DiscoveryStarted -> {
                        vBinding.pfabSearch.showLoadingAnimation(true)
                        foundedDevices.clear()
                        availableRecyclerViewAdapter.submitList(ArrayList(foundedDevices))
                    }
                    is ScanResource.DiscoveryFinished -> {
                        vBinding.pfabSearch.showLoadingAnimation(false)
                    }
                    is ScanResource.DeviceFound -> {
                        if (it.device !in foundedDevices) {
                            foundedDevices.add(it.device)
                            availableRecyclerViewAdapter.submitList(ArrayList(foundedDevices))
                        }
                    }
                }

            }
        }
    }

    private fun setListeners() {
        vBinding.pfabSearch.setOnClickListener {
            checkPermissions()
        }
        vBinding.pfabMakeVisible.setOnClickListener {
            makeVisible()
        }
    }

    private fun setRecyclerViews() {
        vBinding.rvPairedDevice.apply {
            adapter = pairedRecyclerViewAdapter
        }

        vBinding.rvOnlineDevice.apply {
            adapter = availableRecyclerViewAdapter
        }

        vModel.pairedDevices().let {
            pairedRecyclerViewAdapter.submitList(ArrayList(it))
        }

    }

    private fun checkPermissions() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {

                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ),
                        PERMISSION_REQUEST_CODE
                    )
                } else {
                    vModel.startScan()
                }
            }
            else ->
                vModel.startScan()
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _vBinding =
            FragmentFindingBinding.inflate(inflater, container, false)
        return vBinding.root
    }

    override fun onClick(device: Device) {
        if (device.isSmartPhone)
            vModel.connectToDevice(device.macAddress)
        else
            requireContext().singleButtonAlertDialog(
                getString(R.string.inappropriate_device_select),
                getString(R.string.ok)
            )
    }

    //make bluetooth visible for other devices
    private fun makeVisible() {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        startActivity(discoverableIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _vBinding = null
    }

}