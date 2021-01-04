package dz.project.common;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import dz.project.ClientActivity_CommandesFragment;
import dz.project.ClientActivity_LivreursFragment;
import dz.project.ClientActivity_ProfileFragment;

public class PagerFragmentAdapter extends FragmentPagerAdapter {
    private Context context;
    private int totalTabs;

    public PagerFragmentAdapter(Context c, FragmentManager fm, int totalTabs) {
        super(fm);
        context = c;
        this.totalTabs = totalTabs;
    }

    public ClientActivity_ProfileFragment profileFragment;
    public ClientActivity_LivreursFragment livreursFragment;
    public ClientActivity_CommandesFragment commandesFragment;

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                profileFragment = new ClientActivity_ProfileFragment();
                return profileFragment;
            case 1:
                livreursFragment = new ClientActivity_LivreursFragment();
                return livreursFragment;
            case 2:
                commandesFragment = new ClientActivity_CommandesFragment();
                return commandesFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return totalTabs;
    }
}
