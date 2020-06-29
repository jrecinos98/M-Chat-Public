package com.mchat.recinos.Adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.mchat.recinos.Adapters.ListAdapters.CallListAdapter;
import com.mchat.recinos.Adapters.ListAdapters.ChatListAdapter;
import com.mchat.recinos.Fragments.CallsFragment;
import com.mchat.recinos.Fragments.ChatsFragment;
import com.mchat.recinos.Fragments.EmptyListFragment;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    //public static String NO_CHAT_FRAGMENT = "no_chat_fragment";
    public static String CHAT_FRAGMENT = "chat_list_fragment";
    public static String CALLS_FRAGMENT = "call_fragment";
    private ChatsFragment chatsFragment;
    private CallsFragment callsFragment;
    private EmptyListFragment noChatFragment;
    private EmptyListFragment noCallFragment;
    private Object modified = null;
    private boolean chatsEmpty;
    private boolean callsEmpty;
    private static class TABS{
        private static String[] TITLES = {"Chats", "Calls"};
    }
    public ViewPagerAdapter(FragmentManager fm, boolean chatsEmpty, boolean callsEmpty, ChatListAdapter chatAdapter, CallListAdapter callAdapter) {
        super(fm, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.callsEmpty = callsEmpty;
        this.chatsEmpty = chatsEmpty;
        this.callsFragment = new CallsFragment(callAdapter);
        this.chatsFragment = new ChatsFragment(chatAdapter);
        this.noCallFragment = new EmptyListFragment(EmptyListFragment.CALL);
        this.noChatFragment = new EmptyListFragment(EmptyListFragment.CHAT);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0:
                if(!chatsEmpty)
                   return chatsFragment; //ChildFragment2 at position 0
                return noChatFragment;
            case 1:
                if(!callsEmpty)
                    return callsFragment;
                return noCallFragment;
            default:
                return new EmptyListFragment();
        }
    }

    @Override
    public int getCount() {
        return 2; //three fragments
    }
    @Override
    public CharSequence getPageTitle(int position) {
        return TABS.TITLES[position];
    }

    @Override
    public int getItemPosition(Object object) {
        //Compare the object references. This way only the modified item gets swapped.
        if(object == modified)
            return POSITION_NONE;
        return POSITION_UNCHANGED;
    }

    public void setListStatus(String id, boolean status){
        if(id.equals(CHAT_FRAGMENT)){
            if(chatsEmpty)
                modified = noChatFragment;
            else
                modified = chatsFragment;
            this.chatsEmpty = status;

        }else if(id.equals(CALLS_FRAGMENT)){
            if(callsEmpty)
                modified= noCallFragment;
            else
                modified = callsFragment;
            this.callsEmpty =status;
        }
        notifyDataSetChanged();
    }
}