package edu.csun.compsci490.makefriendsapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

public class contactsAdapter extends FirestoreRecyclerAdapter<UserSingleton, contactsAdapter.userViewHolder> {


    public contactsAdapter(@NonNull FirestoreRecyclerOptions<UserSingleton> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull userViewHolder userViewHolder, int i, @NonNull UserSingleton userSingleton) {
        userViewHolder.Email.setText(userSingleton.getFullName());
        userViewHolder.password.setText(userSingleton.getMajor());
    }

    @NonNull
    @Override
    public userViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_user_layout,parent,false);
        return new userViewHolder(view);
    }

    class userViewHolder extends RecyclerView.ViewHolder{

        TextView Email;
        TextView password;

        public userViewHolder(@NonNull View itemView) {
            super(itemView);

            Email = itemView.findViewById(R.id.fullNameDisplay);
            password = itemView.findViewById(R.id.userMajor);
        }
    }

}
