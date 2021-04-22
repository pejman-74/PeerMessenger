package com.peer_messanger.ui.activity

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.peer_messanger.R
import com.peer_messanger.data.wrapper.ConnectionEvents
import com.peer_messanger.databinding.ActivityMainBinding
import com.peer_messanger.ui.fragments.HomeFragmentDirections
import com.peer_messanger.ui.vm.MainViewModel
import com.peer_messanger.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val vModel: MainViewModel by viewModels()
    private lateinit var navController: NavController
    lateinit var activityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        navController = findNavController(R.id.fcv_main)

        //config action bar with navController
        setupActionBarWithNavController(navController)

        if (!vModel.isDeviceSupportBT()) {
            showLongToast(getString(R.string.device_not_support_bt))
            finish()
        }

        //save self user to make relationShip
        vModel.saveDevice(selfUserDatabaseId, "self")

        setObservers()
    }

    private fun setObservers() {

        lifecycleScope.launchWhenStarted {
            vModel.connectionState.collect {
                when (it) {
                    is ConnectionEvents.Connected -> {
                        //navigate to chat fragment
                        findNavController(R.id.fcv_main).navigate(
                            HomeFragmentDirections.actionGlobalChatFragment()
                        )
                    }

                    is ConnectionEvents.Disconnect -> {
                        activityMainBinding.root.snackBar(it.deviceName + " connection was lost")
                    }
                }
            }

        }

    }

    override fun onStart() {
        super.onStart()
        // If BT is not on, request that it be enabled.
        if (vModel.btIsOn())
            vModel.startChatService()
        else
            doubleButtonAlertDialog(
                message = getString(R.string.turn_on_the_bluetooth),
                positiveButtonText = getString(R.string.turn_on),
                negativeButtonText = getString(R.string.offline_work),
                positiveButtonAction = {
                    vModel.enableBT()
                    vModel.startChatService()
                })

    }


    override fun onDestroy() {
        super.onDestroy()
        vModel.stopChatService()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    vModel.startScan()
                } else {
                    showLongToast(getString(R.string.location_permission_is_necessary_to_search))
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}

