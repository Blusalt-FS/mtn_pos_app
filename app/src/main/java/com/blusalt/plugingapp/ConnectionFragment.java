package com.blusalt.plugingapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.blusalt.plugingapp.databinding.FragmentConnectionBinding;
import com.google.gson.Gson;
import com.oze.blusalthorizonpos.utils.device.PosActivity;
import com.oze.blusalthorizonpos.utils.pay.TerminalResponse;
import com.oze.blusalthorizonpos.utils.utils.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import timber.log.Timber;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConnectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class ConnectionFragment extends Fragment implements BluetoothListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    static final int REQUEST_ENABLE_BT = 1;
    static final int REQUEST_LOC = 2;

    final String TAG = ConnectionFragment.class.getSimpleName();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice[] btArray;

    SendReceive sendReceive;

    static boolean isSending = false;

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;

    int REQUEST_ENABLE_BLUETOOTH = 1;

    private static final String APP_NAME = "BTChat";
    private static final UUID MY_UUID = UUID.fromString("8ce255c0-223a-11e0-ac64-0803450c9a66");

    public ConnectionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BluetoothFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ConnectionFragment newInstance(String param1, String param2) {
        ConnectionFragment fragment = new ConnectionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    FragmentConnectionBinding binding;
    NavController navController;
    final static String DEVICE_MODEL = "QCOM-BTD";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        navController = NavHostFragment.findNavController(this);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // prevent onBackPress
        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                binding.listenText.setVisibility(View.VISIBLE);
                binding.listeningImage.setVisibility(View.VISIBLE);
                binding.connectedText.setVisibility(View.VISIBLE);
                binding.connectedText1.setVisibility(View.VISIBLE);
                binding.startListening.setVisibility(View.GONE);

                binding.validateImage.setVisibility(View.GONE);
                binding.validateText.setVisibility(View.GONE);
                binding.aboutToText.setVisibility(View.GONE);
                binding.amountText.setVisibility(View.GONE);
                binding.valitateButton.setVisibility(View.GONE);
            }
        });
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }


    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_connection, container, false);

        binding.listenText.setVisibility(View.VISIBLE);
        binding.listeningImage.setVisibility(View.VISIBLE);
        binding.connectedText.setVisibility(View.VISIBLE);
        binding.connectedText1.setVisibility(View.VISIBLE);
        binding.startListening.setVisibility(View.GONE);

        binding.validateImage.setVisibility(View.GONE);
        binding.validateText.setVisibility(View.GONE);
        binding.aboutToText.setVisibility(View.GONE);
        binding.amountText.setVisibility(View.GONE);
        binding.valitateButton.setVisibility(View.GONE);

        requireActivity().startService(new Intent(requireContext(), BluetoothService.class));

        binding.startListening.setOnClickListener(v -> {
            requireActivity().startService(new Intent(requireContext(), BluetoothService.class));
        });
//                Timber.tag(TAG).d("On DestroyView is Called");
//        Navigation.findNavController(v).navigate(ConnectionFragmentDirections.actionConnectionFragmentToValidateFragment())
//                navController.navigate(ConnectionFragmentDirections.actionConnectionFragmentToValidateFragment())
//        );

        binding.toolbar.setOnClickListener((it) -> {
            Navigation.findNavController(it).navigateUp();
        });

        return binding.getRoot();
    }


    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case STATE_LISTENING:
                    Toast.makeText(getActivity(), "Listening", Toast.LENGTH_SHORT).show();
                    break;
                case STATE_CONNECTING:
//                    Toast.makeText(getActivity(), "Connecting", Toast.LENGTH_SHORT).show();
//                    status.setText("Connecting");
                    break;
                case STATE_CONNECTED:
                    Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Connected");

                    PosActivity.init("test_8c3f614d42530912fbb5a76b72626f8861410cee382948c0e14090ea6f749798db8fd109bed75759129165a2037f6a371691150896997sk");
//                    Navigation.findNavController(getView()).navigate(ConnectionFragmentDirections.actionConnectionFragmentToValidateFragment());
//                    status.setText("Connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    Toast.makeText(getActivity(), "Connection Failed", Toast.LENGTH_SHORT).show();
//                    status.setText("Connection Failed");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    try {

                        binding.listenText.setVisibility(View.GONE);
                        binding.listeningImage.setVisibility(View.GONE);
                        binding.connectedText.setVisibility(View.GONE);
                        binding.connectedText1.setVisibility(View.GONE);
                        binding.startListening.setVisibility(View.GONE);

                        binding.validateImage.setVisibility(View.VISIBLE);
                        binding.validateText.setVisibility(View.VISIBLE);
                        binding.aboutToText.setVisibility(View.VISIBLE);
                        binding.amountText.setVisibility(View.VISIBLE);
                        binding.valitateButton.setVisibility(View.VISIBLE);

                        byte[] readBuff = (byte[]) msg.obj;
                        String tempMsg = new String(readBuff, 0, msg.arg1);

//                        Toast.makeText(getActivity(), "Amount Received: " + tempMsg, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Amount Received: " + tempMsg);

                        binding.amountText.setText("₦ " + tempMsg + ".00");
//                    msg_box.setText(tempMsg);

                        binding.valitateButton.setOnClickListener(v -> {
                            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                            if (networkInfo != null && networkInfo.isConnected()) {
                                startAccountSelectionActivity(Double.valueOf(String.valueOf(tempMsg)));
                            } else {
                                Toast.makeText(getActivity(), "Please connect to the Internet", Toast.LENGTH_SHORT).show();
                            }

                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;
            }
            return true;
        }
    });

    @Override
    public void onStart() {
        super.onStart();
        try {
            ServerClass serverClass = new ServerClass();
            serverClass.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        requireActivity().stopService(new Intent(requireContext(), BluetoothService.class));
    }

    private class ServerClass extends Thread {
        private BluetoothServerSocket serverSocket;

        @SuppressLint("MissingPermission")
        public ServerClass() {
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            BluetoothSocket socket = null;

            while (socket == null) {
                try {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);

                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }

                if (socket != null) {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);

                    sendReceive = new SendReceive(socket);
                    sendReceive.start();

                    break;
                }
            }
        }
    }

    private class ClientClass extends Thread {
        private BluetoothDevice device;
        private BluetoothSocket socket;

        @SuppressLint("MissingPermission")
        public ClientClass(BluetoothDevice device1) {
            device = device1;

            try {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @SuppressLint("MissingPermission")
        public void run() {
            try {
                socket.connect();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);

                sendReceive = new SendReceive(socket);
                sendReceive.start();

            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }

    private class SendReceive extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive(BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = tempIn;
            outputStream = tempOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {

                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.tag(TAG).d("On Pause is Called");
    }

    @Override
    public void onDestroy() {
        Timber.tag(TAG).d("On Destroy is Called");
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Timber.tag(TAG).d("On DestroyView is Called");
        binding = null;
    }

    private void startAccountSelectionActivity(Double amount) {
        Intent intent = new Intent(requireActivity(), PosActivity.class);
        intent.putExtra(Constants.INTENT_EXTRA_ACCOUNT_TYPE, "10");
        intent.putExtra(Constants.INTENT_EXTRA_AMOUNT_KEY, amount);
        intent.putExtra(Constants.TERMINAL_ID, "2076NA61");
        startActivityForResult(intent, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.hasExtra("data")) {
            String result = data.getStringExtra("data");
            TerminalResponse response = new Gson().fromJson(result, TerminalResponse.class);

            String string = String.valueOf(response.responseDescription + " " + "Response Code: " + response.responseCode);
            String resultSent = String.valueOf(result);
            sendReceive.write(resultSent.getBytes());

            Log.e("TAG", string);
            Log.e("TAG resultSent", resultSent);

            if(response.responseCode.equals("03") && response.responseDescription.equals("Card Malfunction")){
                new AlertDialog.Builder(requireActivity())
                        .setTitle(String.valueOf(response.responseCode))
                        .setMessage(response.responseDescription)
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent);
                            }
                        })
                        .show();
            }else {
                Navigation.findNavController(getView()).navigate(ConnectionFragmentDirections.actionConnectionFragmentToStatusFragment(result));
            }

        }
    }

}


