package com.example.notemessenger;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class SectionPagerAdapter extends FragmentPagerAdapter {

    public SectionPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            // Three Tabs
            case 0 :
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;

            case 1 :
                ChatFragment chatsFragment = new ChatFragment();
                return chatsFragment;

            case 2 :
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;

            default:
                return null;
        }
    }

    // Fixer le nombre de fenêtre dans l'application
    @Override
    public int getCount() {
        return 3;
    }

    // Ajouter les titres des fenêtres
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0 :
                return "REQUESTS";
            case 1 :
                return "CHATS";
            case 2 :
                return "FRIENDS";
            default :
                return null;
        }
    }
}
