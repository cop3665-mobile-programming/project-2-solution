package com.example.petheart;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.petheart.models.Memory;
import com.example.petheart.models.MemoryCollection;

import java.util.List;
import java.util.UUID;

public class MemoryPagerActivity extends AppCompatActivity {

    private static final String EXTRA_MEMORY_ID = "memory_id";

    private ViewPager mViewPager;
    private List<Memory> mMemories;

    public static Intent newIntent(Context packageContext, UUID memoryId){
        Intent intent = new Intent(packageContext, MemoryPagerActivity.class);
        intent.putExtra(EXTRA_MEMORY_ID, memoryId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_pager);

        UUID memoryId = (UUID) getIntent().getSerializableExtra(EXTRA_MEMORY_ID);

        mViewPager = (ViewPager) findViewById(R.id.memory_view_pager);
        mMemories = MemoryCollection.get(this).getMemories();

        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                Memory memory = mMemories.get(position);
                return MemoryFragment.newInstance(memory.getId());
            }

            @Override
            public int getCount() {
                return mMemories.size();
            }
        });

        for(int i = 0; i < mMemories.size(); ++i)
        {
            if(mMemories.get(i).getId().equals(memoryId))
            {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }
}
