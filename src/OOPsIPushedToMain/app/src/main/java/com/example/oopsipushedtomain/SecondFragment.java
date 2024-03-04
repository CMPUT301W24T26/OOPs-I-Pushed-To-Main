package com.example.oopsipushedtomain;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.oopsipushedtomain.databinding.FragmentSecondBinding;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getArguments() != null) {
            String firstName = getArguments().getString("firstName", "");
            String lastName = getArguments().getString("lastName", "");
            String phoneNumber = getArguments().getString("phoneNumber", "");
            String emailAddress = getArguments().getString("emailAddress", "");

            // Set the text of TextViews to display the information
            binding.firstNameTextView.setText(firstName);
            binding.lastNameTextView.setText(lastName);
            binding.phoneNumberTextView.setText(phoneNumber);
            binding.emailAddressTextView.setText(emailAddress);

            // Handle the imageUri if you are passing it
            String imageUri = getArguments().getString("imageUri");
            if (imageUri != null && !imageUri.isEmpty()) {
                Uri uri = Uri.parse(imageUri);
                binding.profileImageView.setImageURI(uri);
            } else {
                // Set default image
                binding.profileImageView.setImageResource(R.drawable.default_image);
            }
        }
        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}