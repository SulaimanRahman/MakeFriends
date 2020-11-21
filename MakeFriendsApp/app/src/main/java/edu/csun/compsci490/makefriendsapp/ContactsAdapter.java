package edu.csun.compsci490.makefriendsapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.MyViewHolder> {

    private Context mContext;
    private List<UserSingleton> mData;

    public ContactsAdapter(Context mContext, List<UserSingleton> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_user_layout,parent,false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder userViewHolder, int position) {

        UserSingleton user = mData.get(position);

        userViewHolder.lastName.setText(user.getLastName());
        userViewHolder.firstName.setText(user.getFirstName());
        userViewHolder.userMajor.setText(user.getMajor());
        Glide.with(mContext).load(user.getUserProfileImg()).into(userViewHolder.userImg);
        //userViewHolder.userImg.setImageURI(user.getUserProfileImg());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView userImg;
        TextView firstName;
        TextView lastName;
        TextView userMajor;
        //CircleImageView userImg;
        //MovieSingleton movieSingleton = MovieSingleton.getInstance();

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            firstName = itemView.findViewById(R.id.firstName);
            lastName = itemView.findViewById(R.id.lastName);
            userImg = itemView.findViewById(R.id.profile_image);
            userMajor = itemView.findViewById(R.id.userMajor);
        }

    }
}
