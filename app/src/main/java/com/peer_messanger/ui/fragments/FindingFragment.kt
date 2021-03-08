package com.peer_messanger.ui.fragments

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.peer_messanger.R
import com.peer_messanger.data.wrap.ScanResource
import com.peer_messanger.databinding.FragmentFindingBinding
import com.peer_messanger.ui.activity.MainActivity
import com.peer_messanger.ui.adapters.FindingRecyclerViewAdapter
import com.peer_messanger.ui.base.BaseFragment
import com.peer_messanger.ui.listener.BluetoothDeviceItemListener
import com.peer_messanger.ui.vm.MainViewModel
import com.peer_messanger.util.PERMISSION_REQUEST_CODE
import com.peer_messanger.util.singleButtonAlertDialog


class FindingFragment : BaseFragment<MainViewModel, FragmentFindingBinding>(),
    BluetoothDeviceItemListener {


    private lateinit var mainActivity: MainActivity
    private lateinit var availableRecyclerViewAdapter: FindingRecyclerViewAdapter
    lateinit var pairedRecyclerViewAdapter: FindingRecyclerViewAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainActivity = requireActivity() as MainActivity

        availableRecyclerViewAdapter = FindingRecyclerViewAdapter(this)
        pairedRecyclerViewAdapter = FindingRecyclerViewAdapter(this)
        vBinding.rvPairedDevice.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = pairedRecyclerViewAdapter
        }


        vModel.pairedDevices.observe(viewLifecycleOwner, {

            pairedRecyclerViewAdapter.setData(ArrayList(it))
        })

        mainActivity.getPairedDevices()

        vBinding.rvOnlineDevice.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = availableRecyclerViewAdapter
        }

        vBinding.pfabSearch.setOnClickListener {
            checkPermissions()
        }
        vBinding.pfabMakeVisible.setOnClickListener {
            makeVisible()
        }
        val foundedDevices = ArrayList<BluetoothDevice>()
        vModel.scanDeviceResource.observe(viewLifecycleOwner, {
            when (it) {
                is ScanResource.DiscoveryStarted -> {
                    vBinding.pfabSearch.showLoadingAnimation(true)
                    foundedDevices.clear()
                    availableRecyclerViewAdapter.setData(ArrayList(foundedDevices))
                }
                is ScanResource.DiscoveryFinished -> {
                    vBinding.pfabSearch.showLoadingAnimation(false)
                }
                is ScanResource.ItemFound -> {
                    if (it.value !in foundedDevices) {
                        foundedDevices.add(it.value)
                        availableRecyclerViewAdapter.setData(ArrayList(foundedDevices))
                    }
                }
            }

        })


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
                    mainActivity.startScanning()
                }
            }
            else ->
                mainActivity.startScanning()
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        vBinding =
            FragmentFindingBinding.inflate(inflater, container, false)
        return vBinding.root
    }

    override fun onClick(bluetoothDevice: BluetoothDevice) {
        when (bluetoothDevice.bluetoothClass.deviceClass) {
            BluetoothClass.Device.PHONE_SMART -> {
                mainActivity.connectToDevice(bluetoothDevice.address)
            }
            else ->
                requireContext().singleButtonAlertDialog(
                    getString(R.string.inappropriate_device_select),
                    getString(R.string.ok)
                )
        }

    }

    //make bluetooth visible for other devices
    private fun makeVisible() {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        startActivity(discoverableIntent)
    }

    override fun getViewModel() = activityViewModels<MainViewModel>()
    override fun getViewBinding(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentFindingBinding.inflate(layoutInflater, container, false)

}