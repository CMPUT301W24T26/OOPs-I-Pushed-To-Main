package com.example.oopsipushedtomain;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.oopsipushedtomain.Announcements.AnnouncementListActivity;
import com.example.oopsipushedtomain.Announcements.SendAnnouncementActivity;
import com.example.oopsipushedtomain.databinding.FragmentFirstBinding;
import com.google.firebase.messaging.FirebaseMessaging;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                NavHostFragment.findNavController(FirstFragment.this)
//                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
                Intent i = new Intent(getContext(), SendAnnouncementActivity.class);
                i.putExtra("event", "AnnouncementTest");
                startActivity(i);
            }
        });

        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                NavHostFragment.findNavController(FirstFragment.this)
//                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
                Intent i = new Intent(getContext(), AnnouncementListActivity.class);
                i.putExtra("user", "UserID");
                startActivity(i);
            }
        });

        binding.buttonSub.setOnClickListener(v -> FirebaseMessaging.getInstance().subscribeToTopic("test-notifications")
                .addOnCompleteListener(task -> {
                    String msg = "Subscribed";
                    if (!task.isSuccessful()) {
                        msg = "Subscribe failed";
                    }
                    Log.d("Announcement", msg);
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                }));

        binding.buttonUnSub.setOnClickListener(v -> FirebaseMessaging.getInstance().unsubscribeFromTopic("test-notifications")
                .addOnCompleteListener(task -> {
                    String msg = "Unsubscribed";
                    if (!task.isSuccessful()) {
                        msg = "Unsubscribe failed";
                    }
                    Log.d("Announcement", msg);
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                }));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}