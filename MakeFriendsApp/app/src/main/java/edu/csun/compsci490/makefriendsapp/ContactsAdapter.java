package edu.csun.compsci490.makefriendsapp;

import android.content.Context;
import android.net.Uri;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.MyViewHolder> {

    private final Context mContext;
    private List<Contact> mData;
    private final RecyclerviewClickListener listener;
    StorageReference storageReference;
    public ContactsAdapter(Context mContext, List<Contact> mData, RecyclerviewClickListener listener) {
        this.mContext = mContext;
        this.mData = mData;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContactsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.single_user_layout,parent,false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder userViewHolder, int position) {

        Contact user = mData.get(position);
        String PATH = user.getUserImg();
        if(!PATH.contains("@my.csun.edu")){
            PATH = "Default/BlankProfilePic";
        }
        storageReference = FirebaseStorage.getInstance().getReference().child(PATH);
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(mContext)
                        .load(uri.toString())
                        .placeholder(R.drawable.ic_baseline_account_circle_24)
                        .into(userViewHolder.userImg);
            }
        });
        userViewHolder.fullName.setText(user.getContactName());
        userViewHolder.userMajor.setText(user.getContactMajor());
        //userViewHolder.userMajor.setText(user.getMajor());
    }
    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void filterList(ArrayList<Contact> filteredList){
        mData = filteredList;
        notifyDataSetChanged();
    }

    public interface RecyclerviewClickListener{
        void onClick(View view,int pos);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView userImg;
        private TextView fullName;
        private TextView userMajor;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            fullName = itemView.findViewById(R.id.fullName);
            userImg = itemView.findViewById(R.id.profile_image);
            userMajor = itemView.findViewById(R.id.userMajor);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onClick(view,getAdapterPosition());
        }
    }
}
