package com.blusalt.plugingapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.blusalt.plugingapp.databinding.FragmentTransStatusBinding;
import com.google.gson.Gson;
import com.oze.blusalthorizonpos.utils.device.PosActivity;
import com.oze.blusalthorizonpos.utils.pay.TerminalResponse;

import timber.log.Timber;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TransactionStatusFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class TransactionStatusFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    static final int REQUEST_ENABLE_BT = 1;
    static final int REQUEST_LOC = 2;

    final String TAG = TransactionStatusFragment.class.getSimpleName();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TransactionStatusFragment() {
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
    public static TransactionStatusFragment newInstance(String param1, String param2) {
        TransactionStatusFragment fragment = new TransactionStatusFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    FragmentTransStatusBinding binding;
    BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice mDevice;
    View progressBar;
    Button rescanBtn;
    //    Button cancelBtn;
    NavController navController;
    //    AppPreferenceHelper appPreferenceHelper;
    final static String DEVICE_MODEL = "QCOM-BTD";

    @RequiresApi(api = Build.VERSION_CODES.S)
    private static final String[] ANDROID_12_BLE_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_FINE_LOCATION
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        appPreferenceHelper = new AppPreferenceHelper(requireContext());
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_trans_status, container, false);

        try {
            if (getArguments() != null) {
                String result = TransactionStatusFragmentArgs.fromBundle(getArguments()).getResult();
                TerminalResponse response = new Gson().fromJson(result, TerminalResponse.class);

                if (result != null) {
                    Log.e("TAG response", new Gson().toJson(response));
                    Log.e("TAG amount",  response.data.receiptInfo.transactionAmount);
                    binding.amountText.setText("₦ " + response.data.receiptInfo.transactionAmount + "00");

                    if (response.data.posResponseCode.equals("00")) {
                        binding.statusText.setText("Transaction Approved");
                    }else {
                        binding.statusText.setText("Transaction Declined");
                    }

                    binding.terminalIdTxtValue.setText(response.data.receiptInfo.merchantTID);
                    binding.cardholderNameTxtValue.setText(response.data.receiptInfo.customerCardName);
                    binding.rrnTxtValue.setText(response.data.receiptInfo.rrn);
                    binding.cardNumberTxtValue.setText(response.data.receiptInfo.customerCardPan);
                }

                binding.printReceiptButton.setOnClickListener(v -> {
                    PosActivity.prepareForPrinter(requireContext(), response);
                });

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        binding.backButton.setOnClickListener(v ->
           navController.navigate(TransactionStatusFragmentDirections.actionStatusFragmentToConnectionFragment())
        );

        binding.toolbar.setOnClickListener((it) -> {
            if (!navController.navigateUp()) requireActivity().finishAfterTransition();
        });

        return binding.getRoot();
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
}
