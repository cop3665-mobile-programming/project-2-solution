package com.example.petheart;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.example.petheart.models.Memory;
import com.example.petheart.models.MemoryCollection;
import com.example.petheart.utils.PictureUtils;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MemoryFragment extends Fragment {

    private static final String ARG_MEMORY_ID = "memory_id";
    private static final String DIALOG_DATE = "dialog_date";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_PHOTO = 1;

    private EditText mTitleField;
    private Button mDateButton;
    private Switch mFavoritedSwitch;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private EditText mDescriptionField;

    private Memory mMemory;
    private File mPhotoFile;

    public static MemoryFragment newInstance(UUID memoryId)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARG_MEMORY_ID, memoryId);

        MemoryFragment fragment = new MemoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        UUID memoryId = (UUID) getArguments().getSerializable(ARG_MEMORY_ID);
        mMemory = MemoryCollection.get(getActivity()).getMemory(memoryId);
        mPhotoFile = MemoryCollection.get(getActivity()).getPhotoFile(mMemory);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_memory, container, false);

        mTitleField = v.findViewById(R.id.memory_title);
        mTitleField.setText(mMemory.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //this is useless
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mMemory.setTitle(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //yeehaw!
            }
        });

        mFavoritedSwitch = v.findViewById(R.id.favorite);
        mFavoritedSwitch.setChecked(mMemory.isFavorited());
        mFavoritedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mMemory.setFavorited(b);
            }
        });

        PackageManager packageManager = getActivity().getPackageManager();
        mPhotoButton = (ImageButton) v.findViewById(R.id.memory_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoFile != null && captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = FileProvider.getUriForFile(getActivity(), "petheart.fileprovider", mPhotoFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                List<ResolveInfo> cameraActivities = getActivity().getPackageManager().queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY);

                for(ResolveInfo activity : cameraActivities)
                {
                    getActivity().grantUriPermission(activity.activityInfo.packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }

                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });

        mPhotoView = (ImageView) v.findViewById(R.id.memory_photo);
        ViewTreeObserver observer = mPhotoView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                updatePhotoView();
            }
        });

        mDateButton = v.findViewById(R.id.memory_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mMemory.getDate());
                dialog.setTargetFragment(MemoryFragment.this, REQUEST_DATE);
                dialog.show(manager,DIALOG_DATE);
            }
        });

        mDescriptionField = v.findViewById(R.id.memory_description);
        mDescriptionField.setText(mMemory.getDescription());
        mDescriptionField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //this is useless
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mMemory.setDescription(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //yeehaw!
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode != Activity.RESULT_OK)
        {
            return;
        }
        if(requestCode == REQUEST_DATE)
        {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mMemory.setDate(date);
            updateDate();
        }
        else if(requestCode == REQUEST_PHOTO)
        {
            Uri uri = FileProvider.getUriForFile(getActivity(), "petheart.fileprovider", mPhotoFile);
            getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            updatePhotoView();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();

        MemoryCollection.get(getActivity()).updateMemory(mMemory);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_memory, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.delete_memory:
                MemoryCollection.get(getActivity()).deleteMemory(mMemory);
                getActivity().finish();
                return true;
            case R.id.send_memory:
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getMemoryDetails());
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.memory_subject));
                i = Intent.createChooser(i, getString(R.string.send_memory));
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateDate() {
        mDateButton.setText(mMemory.getDate().toString());
    }

    private void updatePhotoView()
    {
        if(mPhotoView == null || !mPhotoFile.exists())
        {
            mPhotoView.setImageDrawable(null);
        }
        else
        {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), mPhotoView.getMeasuredWidth(), mPhotoView.getMeasuredHeight());
            mPhotoView.setImageBitmap(bitmap);
        }
    }

    private String getMemoryDetails(){
        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mMemory.getDate()).toString();

        String report = getString(R.string.memory_details, mMemory.getTitle(), dateString, mMemory.getDescription());

        return report;
    }

}
