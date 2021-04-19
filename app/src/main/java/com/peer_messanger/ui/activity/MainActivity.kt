package com.peer_messanger.ui.activity

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.gson.Gson
import com.peer_messanger.R
import com.peer_messanger.bluetoothchat.BTChatEvents
import com.peer_messanger.bluetoothchat.BluetoothChatService
import com.peer_messanger.data.model.BluetoothMessage
import com.peer_messanger.data.model.Device
import com.peer_messanger.data.wrap.ScanResource
import com.peer_messanger.databinding.ActivityMainBinding
import com.peer_messanger.ui.fragments.HomeFragmentDirections
import com.peer_messanger.ui.vm.MainViewModel
import com.peer_messanger.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val vModel: MainViewModel by viewModels()
    private lateinit var navController: NavController
    private var bluetoothOffDialog: AlertDialog? = null
    lateinit var activityMainBinding: ActivityMainBinding

    private var mChatService: BluetoothChatService? = null
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var currentBT: BluetoothDevice? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        navController = findNavController(R.id.fcv_main)

        //config action bar with navController
        setupActionBarWithNavController(navController)

        //get adapter for manage bt
        try {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        } catch (e: Exception) {
            showLongToast(getString(R.string.device_not_support_bt))
            finish()
        }
        registerReceiver(bluetoothReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))

        //when bluetooth turned off, show the alert dialog to user
        vModel.bluetoothLiveStatus.observe(this@MainActivity, { isOn ->
            isOn.getContentIfNotHandled {
                if (!it) {
                    bluetoothOffDialog = doubleButtonAlertDialog(
                        message = getString(R.string.turn_on_the_bluetooth),
                        positiveButtonText = getString(R.string.turn_on),
                        negativeButtonText = getString(R.string.offline_work),
                        positiveButtonAction = { enableBluetooth() })
                } else{
                    mChatService?.start()
                    bluetoothOffDialog?.dismiss()}
            }

        })

        //send ack messages to connected device
        vModel.readyToAcknowledgmentMessages.observe(this, {
            sendAckMessage(it.id)
        })

        //send to connected device undelivered messages
        vModel.unDeliveredMessages.observe(this, {
            it.forEach { message ->
                sendUnDeliverMessage(message)
            }
        })
        //send to connected device unacknowledged-messages
        vModel.unacknowledgedMessages.observe(this, {
            it.forEach { message ->
                vModel.setBluetoothMessageIsDelivered(message.id, true)
                //set unacknowledged-messages status to acknowledged
                sendAckMessage(message.id)
            }
        })
        //save self user to make relationShip
        vModel.saveDevice(selfUserDatabaseId, "self")

    }


    //Enable bluetooth without asking the user.
    private fun enableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            bluetoothAdapter.enable()
        }
    }

    //send paired devices to view models
    fun getPairedDevices() {
        vModel.postPairedDevices(bluetoothAdapter.bondedDevices.toList())
    }

    //receive bt (on/off) status
    private val bluetoothReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action != null && action == BluetoothAdapter.ACTION_STATE_CHANGED) {

                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {

                    BluetoothAdapter.STATE_DISCONNECTING -> {
                        mChatService?.stop()
                    }
                    BluetoothAdapter.STATE_OFF -> {
                        vModel.postBluetoothStatus(false)
                    }
                    BluetoothAdapter.STATE_ON -> {
                        vModel.postBluetoothStatus(true)
                    }
                }
            }
        }
    }

    //get scanning devices with broadcast receiver
    private val scanReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action != null) {
                when (action) {
                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                        vModel.postScanDeviceResource(ScanResource.DiscoveryStarted)
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        context.unregisterReceiver(this)
                        vModel.postScanDeviceResource(ScanResource.DiscoveryFinished)
                    }
                    BluetoothDevice.ACTION_FOUND -> {
                        val device =
                            intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        device?.let {
                            vModel.postScanDeviceResource(ScanResource.ItemFound(device))
                        }

                    }
                }
            }
        }
    }

    //send request scan bluetooth devices to os
    fun startScanning() {
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        registerReceiver(scanReceiver, filter)
        bluetoothAdapter.startDiscovery()
    }


    fun connectToDevice(macAddress: String) {
        //before connect make sure bluetooth is on
        if (!bluetoothAdapter.isEnabled) {
            activityMainBinding.root.snackBar(getString(R.string.turn_on_the_bluetooth))
            return
        }
        // Cancel discovery because it's costly and we're about to connect
        bluetoothAdapter.cancelDiscovery()
        // Get the BluetoothDevice object
        val bluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress)
        // Attempt to connect to the device
        mChatService?.connect(bluetoothDevice, true)

    }


    private fun setObservers() {
        lifecycleScope.launchWhenStarted {
            mChatService?.getEventFlow()?.collect {
                when (it) {

                    is BTChatEvents.SendingMessage -> {
                    }

                    is BTChatEvents.ReceivedMessage -> {


                        if (it.message.startsWith("ack=")) {
                            val messageId = it.message.substringAfter("=")
                            //set message is IsDelivered true
                            if (messageId.isNotBlank())
                                vModel.setBluetoothMessageIsDelivered(messageId, true)
                        } else {
                            //save received message to db
                            vModel.saveReceivedMessage(it.message, currentBT?.address)
                        }
                    }


                    is BTChatEvents.Connected -> {
                        currentBT = it.device ?: return@collect

                        //save connected user to database
                        currentBT?.apply {
                            if (!address.isNullOrBlank() && !name.isNullOrBlank())
                                vModel.saveDevice(address, name)
                        }


                        //get undelivered connected user messages
                        vModel.getUnDeliveredMessages(currentBT!!.address)

                        //get unacknowledgedMessages connected user messages
                        vModel.getUnacknowledgedMessages(currentBT!!.address)

                        //navigate to chat fragment
                        findNavController(R.id.fcv_main).navigate(
                            HomeFragmentDirections.actionGlobalChatFragment(
                                Device(
                                    currentBT!!.address!!,
                                    currentBT!!.name
                                )
                            )
                        )
                    }

                    is BTChatEvents.Connecting -> {
                        activityMainBinding.root.snackBar(it.device?.name + " Connecting")
                    }

                    is BTChatEvents.ConnectionFailed -> {
                        activityMainBinding.root.snackBar(it.deviceName + " Unable to connect device")

                    }

                    is BTChatEvents.Disconnect -> {
                        activityMainBinding.root.snackBar(it.deviceName + " connection was lost")
                    }
                }
            }

        }
    }

    private fun sendAckMessage(messageId: String) {
        if (messageId.isBlank()) {
            println("$TAG messageId is empty")
            return
        }
        //create ack message
        val ackMessage = "ack=$messageId"

        sendMessageWithBluetooth(ackMessage)
    }

    fun sendMessage(messageBody: String, macAddress: String) {

        if (messageBody.isBlank()) {
            println("$TAG messageBody is empty")
            return
        }
        if (macAddress.isBlank()) {
            println("$TAG macAddress is empty")
            return
        }
        //create a BluetoothMessage
        val message = BluetoothMessage(
            getCurrentUTCDateTime(), messageBody, selfUserDatabaseId, macAddress,
            getCurrentUTCDateTime(), false
        )
        //save message to db
        vModel.saveSendMessage(message)
        //convert to gson for make serializable easier
        val gsonStringMessage = Gson().toJson(message)
        sendMessageWithBluetooth(gsonStringMessage)

    }

    private fun sendUnDeliverMessage(bluetoothMessage: BluetoothMessage) {
        //convert to gson for make serializable easier
        val gsonStringMessage = Gson().toJson(bluetoothMessage)

        sendMessageWithBluetooth(gsonStringMessage)
    }

    private  fun sendMessageWithBluetooth(message: String) {
        // Check that we're actually connected before trying anything
        if (mChatService?.getState() != BluetoothChatService.STATE_CONNECTED) {
            println("$TAG sendMessage is failed because not connected ")
            return
        }

        mChatService?.sendMessage(message)
    }


    override fun onStart() {
        super.onStart()
        // If BT is not on, request that it be enabled.
        if (!bluetoothAdapter.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            lunchActivityForEnableBT.launch(enableIntent)
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            // Initialize the BluetoothChatService to perform bluetooth connections
            mChatService = BluetoothChatService(Dispatchers.IO)
            setObservers()
        }
    }

    override fun onResume() {
        super.onResume()
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        mChatService?.let {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (it.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                it.start()

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mChatService?.stop()
    }

    private val lunchActivityForEnableBT =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Bluetooth is now enabled, so set up chat service
                mChatService = BluetoothChatService(Dispatchers.IO)
            } else {
                // User did not enable Bluetooth or an error occurred
                println("$TAG BT not enabled")
                this.showLongToast(getString(R.string.app_cant_work_without_bt_on_lunch))
                this.finish()
            }
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
                    startScanning()
                } else {
                    showLongToast(getString(R.string.location_permission_is_necessary_to_search))
                    vModel.postScanDeviceResource(ScanResource.DiscoveryFinished)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}

