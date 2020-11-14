package edu.csun.compsci490.makefriendsapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends FirestoreRecyclerAdapter<UserSingleton, FriendsAdapter.userViewHolder> {


    public FriendsAdapter(@NonNull FirestoreRecyclerOptions<UserSingleton> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull userViewHolder userViewHolder, int i, @NonNull UserSingleton userSingleton) {
        userViewHolder.lastName.setText(userSingleton.getLastName());
        userViewHolder.firstName.setText(userSingleton.getFirstName());
        userViewHolder.userMajor.setText(userSingleton.getMajor());
        userViewHolder.userImg.setImageURI(userSingleton.getUserProfileImg());
    }

    @NonNull
    @Override
    public userViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_user_layout,parent,false);
        return new userViewHolder(view);
    }

    class userViewHolder extends RecyclerView.ViewHolder{

        TextView firstName;
        TextView lastName;
        TextView userMajor;
        CircleImageView userImg;

        public userViewHolder(@NonNull View itemView) {
            super(itemView);

            firstName = itemView.findViewById(R.id.firstName);
            lastName = itemView.findViewById(R.id.lastName);
            userImg = itemView.findViewById(R.id.profile_image);
            userMajor = itemView.findViewById(R.id.userMajor);
        }
    }

}
