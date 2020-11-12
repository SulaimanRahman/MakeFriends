package edu.csun.compsci490.makefriendsapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.MyViewHolder> {

    private Context mContext;
    private static List<Contact> mData;

    public ContactsAdapter(Context mContext, List<Contact> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;

        LayoutInflater inflater = LayoutInflater.from(mContext);
        v = inflater.inflate(R.layout.fragment_friends, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
//        Glide.with(mContext)
//                .load("https://image.tmdb.org/t/p/w500" + mData.get(position).getImg())
//                .into(holder.img);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView img;
        //MovieSingleton movieSingleton = MovieSingleton.getInstance();

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            //img = itemView.findViewById(R.id.movieImage);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
//                    Log.d("Adaptery", "Position: " + position);
//                    Log.d("Adaptery", "Id: " + mData.get(position).getId());
//                    String id = mData.get(position).getId();
//                    movieSingleton.setMovieId(id);
//                    movieSingleton.setMovieModelClass((MovieModelClass)mData.get(position));
//                    AppCompatActivity activity = (AppCompatActivity) view.getContext();
//                    Fragment myFragment = new MovieProfileFragment();
//                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, myFragment).commit();


                }
            });
        }

    }
}
