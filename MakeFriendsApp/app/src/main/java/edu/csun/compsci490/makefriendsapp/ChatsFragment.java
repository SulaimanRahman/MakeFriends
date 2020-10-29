package edu.csun.compsci490.makefriendsapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatsFragment extends Fragment {
    private ArrayList<ChatItem> chatItems;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private Button btnAdd, btnDelete;
    private EditText etAdd, etDelete;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatsFragment newInstance(String param1, String param2) {
        ChatsFragment fragment = new ChatsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        ArrayList<ChatItem> items = new ArrayList<>();
//        items.add(new ChatItem(R.drawable.ic_baseline_home_24, "Home name", "home preview"));
//        items.add(new ChatItem(R.drawable.ic_baseline_chat_24, "Chat name", "chat preview"));
//        items.add(new ChatItem(R.drawable.ic_baseline_folder_shared_24, "Folder name", "folder preview"));

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_chats, container, false);

        createChatList();

        View rootView = inflater.inflate(R.layout.fragment_chats, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.chatRecyclerView);
        recyclerView.setHasFixedSize(true);
        chatAdapter = new ChatAdapter(chatItems);
        recyclerView.setAdapter(chatAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // build recycler view
        btnAdd = rootView.findViewById(R.id.btn_addChat);
        btnDelete = rootView.findViewById(R.id.btn_deleteChat);
        etAdd = rootView.findViewById(R.id.et_addChat);
        etDelete = rootView.findViewById(R.id.et_deleteChat);

        chatAdapter.setOnChatClickListener(new ChatAdapter.OnChatClickListener() {
            @Override
            public void onChatClick(int position) {
                changeChat(position);
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = Integer.parseInt(etAdd.getText().toString());
                insertChat(position);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = Integer.parseInt(etDelete.getText().toString());
                deleteChat(position);
            }
        });

        return rootView;
    }

    public void insertChat(int position){
        chatItems.add(position, new ChatItem(R.drawable.ic_baseline_home_24, "Position: " + position, "home preview"));
        chatAdapter.notifyItemInserted(position);
    }

    public void deleteChat(int position){
        chatItems.remove(position);
        chatAdapter.notifyItemRemoved(position);
    }

    public void changeChat(int position){
        chatItems.get(position).clickAction();
        chatAdapter.notifyItemChanged(position);
    }

    private void createChatList() {
        chatItems = new ArrayList<>();
        chatItems.add(new ChatItem(R.drawable.ic_baseline_home_24, "Home name", "home preview"));
        chatItems.add(new ChatItem(R.drawable.ic_baseline_chat_24, "Chat name", "chat preview"));
        chatItems.add(new ChatItem(R.drawable.ic_baseline_folder_shared_24, "Folder name", "folder preview"));
    }
}