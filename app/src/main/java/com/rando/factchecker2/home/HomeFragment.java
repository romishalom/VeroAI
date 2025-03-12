package com.rando.factchecker2.home;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.rando.factchecker2.floatingService.FloatingService;
import com.rando.factchecker2.R;
import com.rando.factchecker2.style.Typewriter;
import com.rando.factchecker2.databinding.FragmentFirstBinding;

public class HomeFragment extends Fragment {

    private FragmentFirstBinding binding;
    private ImageButton nextBtn;
    private AlertDialog dialog;
    private Intent activityIntent;

    private HomeViewModel homeViewModel;
    private ActionsViewModel actionsViewModel;
    private boolean fistOpening = true;

    private PromptAction promptAction = PromptAction.NOTHING;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activityIntent = requireActivity().getIntent();
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        actionsViewModel = new ViewModelProvider(requireActivity()).get(ActionsViewModel.class);
        Typewriter typewriter = new Typewriter(binding.greeting);
        typewriter.setDelay(50);
        typewriter.animateText("Hello there, how can I help you today?");
        binding.promptBox.setText(activityIntent.getStringExtra("etText"));
        binding.promptBox.requestFocus();
        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!binding.promptBox.getText().toString().isEmpty()) {
                    String prompt = binding.promptBox.getText().toString();
                    if (prompt.startsWith("@Summarize ")) {
                        promptAction = PromptAction.SUMMARIZE;
                    } else if (prompt.startsWith("@TrueFalse ")) {
                        promptAction = PromptAction.TRUEFALSE;
                    }
                    homeViewModel.setPrompt(prompt);
                    actionsViewModel.setAction(promptAction);
                    binding.promptBox.setText("");
                    NavHostFragment.findNavController(HomeFragment.this)
                            .navigate(R.id.action_FirstFragment_to_SecondFragment);
                    promptAction = PromptAction.NOTHING;
                }
            }
        });

        binding.overlayActionBtn.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isMyServiceRunning()) {
                    getActivity().stopService(new Intent(getActivity(), FloatingService.class));
                }

                if (checkOverlayDisplayPermission()) {
                    // just Run service
                    getActivity().startService(new Intent(getActivity(), FloatingService.class));
                    Toast.makeText(getActivity(), "Starting Overlay", Toast.LENGTH_LONG).show();
                    getActivity().finish();

                } else {
                    //if no permission show dialog.
                    requestOverlayDisplayPermission();
                }
            }
        });

        binding.summarizeActionBtn.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (promptAction == PromptAction.SUMMARIZE) {
                    binding.summarizeActionBtn.getRoot().setBackground(getResources().getDrawable(R.drawable.back));
                    promptAction = PromptAction.NOTHING;
                    Log.d("test", promptAction.toString());
                } else {
                    if (promptAction == PromptAction.TRUEFALSE) {
                        binding.truefalseActionBtn.getRoot().setBackground(getResources().getDrawable(R.drawable.back));
                    }
                    binding.summarizeActionBtn.getRoot().setBackground(getResources().getDrawable(R.drawable.selected_action));
                    promptAction = PromptAction.SUMMARIZE;
                    Log.d("test", promptAction.toString());
                }
            }
        });

        binding.truefalseActionBtn.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (promptAction == PromptAction.TRUEFALSE) {
                    binding.truefalseActionBtn.getRoot().setBackground(getResources().getDrawable(R.drawable.back));
                    promptAction = PromptAction.NOTHING;
                    Log.d("test", promptAction.toString());
                } else {
                    if (promptAction == PromptAction.SUMMARIZE) {
                        binding.summarizeActionBtn.getRoot().setBackground(getResources().getDrawable(R.drawable.back));
                    }
                    binding.truefalseActionBtn.getRoot().setBackground(getResources().getDrawable(R.drawable.selected_action));
                    promptAction = PromptAction.TRUEFALSE;
                    Log.d("test", promptAction.toString());
                }
            }
        });

        String responseExtra = activityIntent.getStringExtra("responseExtra");
        if (responseExtra != null && fistOpening) {
            NavHostFragment.findNavController(HomeFragment.this)
                    .navigate(R.id.action_FirstFragment_to_SecondFragment);
            homeViewModel.setResponse(responseExtra);
            actionsViewModel.setAction(PromptAction.NOTHING);
            fistOpening = false;
        }

    }




    private boolean isMyServiceRunning() {
        // The ACTIVITY_SERVICE is needed to retrieve a
        // ActivityManager for interacting with the global system
        // It has a constant String value "activity".
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);

        // A loop is needed to get Service information that
        // are currently running in the System.
        // So ActivityManager.RunningServiceInfo is used.
        // It helps to retrieve a
        // particular service information, here its this service.
        // getRunningServices() method returns a list of the
        // services that are currently running
        // and MAX_VALUE is 2147483647. So at most this many services
        // can be returned by this method.
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            // If this service is found as a running,
            // it will return true or else false.
            if (FloatingService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void requestOverlayDisplayPermission() {
        // An AlertDialog is created
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // This dialog can be closed, just by taping
        // anywhere outside the dialog-box
        builder.setCancelable(true);

        // The title of the Dialog-box is set
        builder.setTitle("Screen Overlay Permission Needed");

        // The message of the Dialog-box is set
        builder.setMessage("Enable 'Display over other apps' from System Settings.");

        // The event of the Positive-Button is set
        builder.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // The app will redirect to the 'Display over other apps' in Settings.
                // This is an Implicit Intent. This is needed when any Action is needed
                // to perform, here it is
                // redirecting to an other app(Settings).
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getActivity().getPackageName()));

                // This method will start the intent. It takes two parameter, one is the Intent and the other is
                // an requestCode Integer. Here it is -1.
                startActivityForResult(intent, getActivity().RESULT_OK);
            }
        });
        dialog = builder.create();
        // The Dialog will
        // show in the screen
        dialog.show();
    }

    private boolean checkOverlayDisplayPermission() {
        // Android Version is lesser than Marshmallow or
        // the API is lesser than 23
        // doesn't need 'Display over other apps' permission enabling.
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            // If 'Display over other apps' is not enabled
            // it will return false or else true
            if (!Settings.canDrawOverlays(getActivity())) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

}