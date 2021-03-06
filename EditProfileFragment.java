package com.example.driveusers.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.driveusers.ModelClass.RegisterPojo;
import com.example.driveusers.R;
import com.example.driveusers.Utils.App;
import com.example.driveusers.Utils.Constants;
import com.example.driveusers.Utils.MyMVVM;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.hbb20.CountryCodePicker;
import com.omninos.util_data.CommonUtils;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class EditProfileFragment extends Fragment implements View.OnClickListener {

    private ImageView back_topbar;
    private TextView heading, et_email;
    private String name, email, phone, country_code, newName, newPhone, imagePath, Id, getimage, Image_user;
    private EditText et_number, et_name;
    private View view;
    private MyMVVM myMVVM;
    private CircleImageView profileImage;
    private RelativeLayout rlAddImage;
    private Uri imageUri;
    private CountryCodePicker ccp;
    private Button save_btn;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        myMVVM = ViewModelProviders.of(EditProfileFragment.this).get(MyMVVM.class);
        Id = App.getSharedpref().getModel(Constants.User_Register, RegisterPojo.class).getDetails().getId();
        Image_user = App.getSharedpref().getModel(Constants.User_Register, RegisterPojo.class).getDetails().getImage();
        ids();
        detailApi();

        click();
        return view;
    }

    private void detailApi() {
        myMVVM.UserProfile(getActivity(), Id).observe(getViewLifecycleOwner(), new Observer<RegisterPojo>() {
            @Override
            public void onChanged(RegisterPojo userDetailPojo) {
                if (userDetailPojo.getSuccess().equalsIgnoreCase("1")) {
                    name = userDetailPojo.getDetails().getUsername();
                    email = userDetailPojo.getDetails().getEmail();
                    phone = userDetailPojo.getDetails().getPhone();
                    country_code = userDetailPojo.getDetails().getCountryCode();
                    getimage = userDetailPojo.getDetails().getImage();

                    if (Image_user.equalsIgnoreCase("")) {
                        profileImage.setImageResource(R.drawable.ic_profile);
                    } else {
                        Glide.with(getActivity()).load(getimage).into(profileImage);
                    }

                    et_name.setText(name);
                    et_email.setText(email);
                    et_number.setText(phone);
                    if (country_code != null) {
                        try {
                            int myInt = Integer.parseInt(country_code);
                            ccp.setCountryForPhoneCode(myInt);
                        } catch (NumberFormatException ex) {
                            System.out.println("not a number" + ex);
                        }

                    }

                } else {
                    Toast.makeText(getActivity(), userDetailPojo.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void click() {
        back_topbar.setOnClickListener(this);
        rlAddImage.setOnClickListener(this);
        save_btn.setOnClickListener(this);
        heading.setText("Edit Profile");
    }

    private void ids() {
        back_topbar = view.findViewById(R.id.back_topbar);
        heading = view.findViewById(R.id.heading);
        et_name = view.findViewById(R.id.et_name);
        et_email = view.findViewById(R.id.et_email);
        et_number = view.findViewById(R.id.et_number);
        ccp = view.findViewById(R.id.ccp);
        rlAddImage = view.findViewById(R.id.rlAddImage);
        profileImage = view.findViewById(R.id.profileImage);
        if (Image_user.equalsIgnoreCase("")) {
            profileImage.setImageResource(R.drawable.ic_profile);
        } else {
            Glide.with(getActivity()).load(Image_user).into(profileImage);
        }
        save_btn = view.findViewById(R.id.save_btn);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_topbar:
                getActivity().onBackPressed();
                break;
            case R.id.save_btn:
                newName = et_name.getText().toString();
                newPhone = et_number.getText().toString();
                if (name.isEmpty()) {
                    et_name.setError("please enter the valid name");
                    et_name.requestFocus();
                } else if (newPhone.isEmpty()) {
                    et_number.setError("please enter your phone number");
                    et_number.requestFocus();
                } else {
                    UpdateDetail();
                }
                break;
            case R.id.rlAddImage:
                CropImage.activity()
                        .start(getContext(), EditProfileFragment.this);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                imagePath = resultUri.getPath();
                Toast.makeText(getActivity(), imagePath, Toast.LENGTH_SHORT).show();
                profileImage.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void UpdateDetail() {
        myMVVM.UpdateUserDetailProfile(getActivity(), CommonUtils.getRequestBodyText(Id), CommonUtils.getRequestBodyText(newName), CommonUtils.getRequestBodyText(newPhone), CommonUtils.getRequestBodyText(ccp.getSelectedCountryCodeWithPlus()), CommonUtils.getImgdData(imagePath, "image")).observe(getViewLifecycleOwner(), new Observer<RegisterPojo>() {
            @Override
            public void onChanged(RegisterPojo registerPojo) {
                if (registerPojo.getSuccess().equalsIgnoreCase("1")) {
                    App.getSharedpref().saveString(Constants.USER_Login_STATUS, "1");
                    App.getSharedpref().saveModel(Constants.User_Register, registerPojo);
                    getActivity().onBackPressed();
                    Toast.makeText(getActivity(), "info updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "info updated failed..", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
