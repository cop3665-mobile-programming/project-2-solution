package com.example.petheart;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.petheart.models.Memory;
import com.example.petheart.models.MemoryCollection;
import com.example.petheart.utils.PictureUtils;

import java.io.File;
import java.util.List;

public class MemoryCollectionFragment extends Fragment {

    private class MemoryHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private Memory mMemory;

        private TextView mTitleTextView;
        private TextView mDateTextView;
        private ImageView mMemoryFavorited;
        private ImageView mPhotoView;

        public MemoryHolder(LayoutInflater inflater, ViewGroup parent)
        {
            super(inflater.inflate(R.layout.list_item_memory, parent, false));
            itemView.setOnClickListener(this);

            mTitleTextView = (TextView) itemView.findViewById(R.id.memory_title);
            mDateTextView = (TextView) itemView.findViewById(R.id.memory_date);
            mMemoryFavorited = (ImageView) itemView.findViewById(R.id.memory_favorited);
            mPhotoView = (ImageView) itemView.findViewById(R.id.memory_photo);
        }

        public void bind(Memory memory)
        {
            mMemory = memory;
            mTitleTextView.setText(mMemory.getTitle());
            mDateTextView.setText(mMemory.getDate().toString());
            mMemoryFavorited.setVisibility(memory.isFavorited() ? View.VISIBLE : View.INVISIBLE);
            ViewTreeObserver observer = mPhotoView.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @Override
                public void onGlobalLayout() {
                    updatePhotoView();
                }
            });
        }

        @Override
        public void onClick(View view) {
            Intent intent = MemoryPagerActivity.newIntent(getActivity(), mMemory.getId());
            startActivity(intent);
        }

        private void updatePhotoView()
        {
            File photoFile = MemoryCollection.get(getActivity()).getPhotoFile(mMemory);
            if(mPhotoView == null || !photoFile.exists())
            {
                mPhotoView.setImageDrawable(null);
            }
            else
            {
                Bitmap bitmap = PictureUtils.getScaledBitmap(photoFile.getPath(), mPhotoView.getMeasuredWidth(), mPhotoView.getMeasuredHeight());
                mPhotoView.setImageBitmap(bitmap);
            }
        }
    }

    private class MemoryAdapter extends RecyclerView.Adapter<MemoryHolder>
    {
        private List<Memory> mMemories;

        public MemoryAdapter(List<Memory> memories)
        {
            mMemories = memories;
        }

        @NonNull
        @Override
        public MemoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new MemoryHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull MemoryHolder memoryHolder, int i) {
            Memory memory = mMemories.get(i);
            memoryHolder.bind(memory);
        }

        public void setMemories(List<Memory> memories){
            mMemories = memories;
        }

        @Override
        public int getItemCount() {
            return mMemories.size();
        }
    }

    private RecyclerView mMemoryRecyclerView;
    private MemoryAdapter mAdapter;

    public static MemoryCollectionFragment newInstance()
    {
        MemoryCollectionFragment fragment = new MemoryCollectionFragment();
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_memory_collection, container, false);
        mMemoryRecyclerView = (RecyclerView) v.findViewById(R.id.memory_recycler_view);
        mMemoryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI(false);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_memory_collection, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.new_memory:
                Memory memory = new Memory();
                MemoryCollection.get(getActivity()).addMemory(memory);
                Intent intent = MemoryPagerActivity.newIntent(getActivity(), memory.getId());
                startActivity(intent);
                return true;
            case R.id.show_favorited:
                updateUI(true);
                return true;
            case R.id.show_all:
                updateUI(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        updateUI(false);
    }

    private void updateUI(boolean showFavoritedOnly)
    {
        MemoryCollection memoryCollection = MemoryCollection.get(getActivity());
        List<Memory> memories = showFavoritedOnly ? memoryCollection.getFavorited() : memoryCollection.getMemories();

        if(mAdapter == null) {
            mAdapter = new MemoryAdapter(memories);
            mMemoryRecyclerView.setAdapter(mAdapter);
        }
        else
        {
            mAdapter.setMemories(memories);
            mAdapter.notifyDataSetChanged();
        }
    }

}
