package com.peer_messanger.ui.activity

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.gson.Gson
import com.peer_messanger.R
import com.peer_messanger.bluetoothchat.BluetoothChatService
import com.peer_messanger.bluetoothchat.Constants
import com.peer_messanger.data.model.BluetoothMessage
import com.peer_messanger.data.model.Device
import com.peer_messanger.data.wrap.BluetoothEventResource
import com.peer_messanger.data.wrap.ScanResource
import com.peer_messanger.databinding.ActivityMainBinding
import com.peer_messanger.ui.fragments.HomeFragmentDirections
import com.peer_messanger.ui.vm.MainViewModel
import com.peer_messanger.util.*
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val vModel: MainViewModel by viewModels()
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var bluetoothOffDialog: AlertDialog? = null
    lateinit var activityMainBinding: ActivityMainBinding

    private var mChatService: BluetoothChatService? = null
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var currentConnectedDevice: BluetoothDevice? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fcv_main) as NavHostFragment
        val navController = navHostFragment.navController

        //config action bar with navhostFragment
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        //get adapter for manage bt
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
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


    private val mHandler =
        object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    Constants.MESSAGE_STATE_CHANGE -> {

                        when (msg.arg1) {

                            BluetoothChatService.STATE_CONNECTED -> {
                                println("$TAG STATE_CONNECTED")
                                //send connected device to chat fragment
                                vModel.postBluetoothEventsResource(BluetoothEventResource.DeviceConnected)
                            }

                            BluetoothChatService.STATE_CONNECTING -> {
                                println("$TAG STATE_CONNECTING")

                                //send connected device to chat fragment
                                vModel.postBluetoothEventsResource(BluetoothEventResource.DeviceConnecting)
                            }

                            BluetoothChatService.STATE_LISTEN, BluetoothChatService.STATE_NONE -> {
                                println("$TAG STATE_LISTEN || STATE_NONE")
                                //send connected device to chat fragment
                                vModel.postBluetoothEventsResource(BluetoothEventResource.DeviceDisconnected)
                            }
                        }
                    }

                    Constants.MESSAGE_WRITE -> {
                        println("$TAG MESSAGE_WRITE")

                    }


                    Constants.MESSAGE_READ -> {
                        println("$TAG MESSAGE_READ")
                        val readBuf = msg.obj as ByteArray
                        val message = String(readBuf, 0, msg.arg1)

                        if (message.startsWith("ack=")) {
                            val messageId = message.substringAfter("=")
                            //set message is IsDelivered true
                            if (messageId.isNotBlank())
                                vModel.setBluetoothMessageIsDelivered(messageId, true)
                        } else {
                            //save received message to db
                            vModel.saveReceivedMessage(message, currentConnectedDevice?.address)
                        }
                    }


                    Constants.MESSAGE_DEVICE_NAME -> {
                        println("$TAG MESSAGE_DEVICE_NAME")
                        currentConnectedDevice = msg.data.getParcelable(Constants.DEVICE_NAME)

                        //save connected user to database
                        currentConnectedDevice?.let {
                            if (!it.address.isNullOrBlank() && !it.name.isNullOrBlank())
                                vModel.saveDevice(it.address, it.name)
                        }


                        //get undelivered connected user messages
                        vModel.getUnDeliveredMessages(currentConnectedDevice!!.address)

                        //get unacknowledgedMessages connected user messages
                        vModel.getUnacknowledgedMessages(currentConnectedDevice!!.address)

                        //navigate to chat fragment
                        findNavController(R.id.fcv_main).navigate(
                            HomeFragmentDirections.actionGlobalChatFragment(
                                Device(
                                    currentConnectedDevice!!.address!!,
                                    currentConnectedDevice!!.name
                                )
                            )
                        )
                    }

                    Constants.MESSAGE_TOAST -> {
                        println("$TAG MESSAGE_TOAST")
                        val message = msg.data.getString(Constants.TOAST)
                        message?.let {
                            activityMainBinding.root.snackBar(it)
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
        if (mChatService?.state != BluetoothChatService.STATE_CONNECTED) {
            println("$TAG sendMessage is failed because not connected ")
            return
        }
        val byteArrayMessage = message.encodeToByteArray()
        mChatService?.write(byteArrayMessage)
    }


    override fun onStart() {
        super.onStart()
        // If BT is not on, request that it be enabled.
        if (!bluetoothAdapter.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            // Initialize the BluetoothChatService to perform bluetooth connections
            mChatService = BluetoothChatService(this, mHandler)
        }
    }

    override fun onResume() {
        super.onResume()
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        mChatService?.let {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (it.state == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                it.start()

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mChatService?.stop()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            // When the request to enable Bluetooth returns
            REQUEST_ENABLE_BT ->
                if (resultCode == RESULT_OK) {
                    // Bluetooth is now enabled, so set up chat service
                    mChatService = BluetoothChatService(this, mHandler)
                } else {
                    // User did not enable Bluetooth or an error occurred
                    println("$TAG BT not enabled")
                    this.showLongToast(getString(R.string.app_cant_work_without_bt_on_lunch))
                    this.finish()
                }

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
        val navController = findNavController(R.id.fcv_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

}

