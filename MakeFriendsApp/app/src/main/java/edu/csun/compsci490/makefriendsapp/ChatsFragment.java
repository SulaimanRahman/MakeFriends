package edu.csun.compsci490.makefriendsapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChatsFragment extends Fragment{
    private ArrayList<ChatItem> chatItems;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private ImageView btnFindFriends;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        createChatList();

        // build recycler view
        final View rootView = inflater.inflate(R.layout.fragment_chats, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.chatRecyclerView);
        recyclerView.setHasFixedSize(true);
        chatAdapter = new ChatAdapter(chatItems);
        recyclerView.setAdapter(chatAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // initialise button
        btnFindFriends = rootView.findViewById(R.id.btn_findFriends);

        btnFindFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_layout, new SearchUsersFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        chatAdapter.setOnChatClickListener(new ChatAdapter.OnChatClickListener() {
            @Override
            public void onChatClick(int position) {
                enterMessagingActivity();
            }

            @Override
            public void onDeleteClick(int position) {
                deleteChat(position);
            }
        });

        return rootView;
    }

    public void insertChat(int position){
        if(chatItems.size()==1 && chatItems.get(0).getName() == ""){
            chatItems.remove(0);
            chatAdapter.notifyItemRemoved(0);
        }
        // insert new chat with new person at position 0 (top)
        chatItems.add(position, new ChatItem(R.drawable.ic_launcher_foreground, "Inserted chat", "home preview"));
        chatAdapter.notifyItemInserted(position);
    }

    public void deleteChat(int position){
        chatItems.remove(position);
        chatAdapter.notifyItemRemoved(position);

        if(chatItems.isEmpty()){
            chatItems.add(new ChatItem(R.drawable.ic_launcher_foreground, "", "NO CONVERSATIONS YET"));
            chatAdapter.notifyItemInserted(0);
        }
    }

    public void enterMessagingActivity(){
        // enter chat method must be implemented
        Intent intent = new Intent();
        intent.setClass(getActivity(), MessagingActivity.class);
        getActivity().startActivity(intent);
    }

    private void createChatList() {
        chatItems = new ArrayList<>();
        chatItems.add(new ChatItem(R.drawable.ic_launcher_foreground, "Home name", "home preview"));
        chatItems.add(new ChatItem(R.drawable.ic_launcher_foreground, "Chat name", "chat preview"));
        chatItems.add(new ChatItem(R.drawable.ic_launcher_foreground, "Folder name", "folder preview"));
    }
}