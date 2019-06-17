package com.papbl.runningapps.adapter;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.papbl.runningapps.R;
import com.papbl.runningapps.model.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.ViewHolder> {

    private ArrayList<Track> trackArrayList;
    private Context context;

    public TrackAdapter(ArrayList<Track> trackArrayList, Context context) {
        this.trackArrayList = trackArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.view_list_track,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Track track = trackArrayList.get(i);
        viewHolder.tangal.setText(track.getTanggal());
        viewHolder.jarak.setText(track.getJarak());
        viewHolder.durasi.setText(track.getDurasi());
        viewHolder.kalori.setText(track.getKalori());
        viewHolder.fromLat.setText("Lat : "+track.getLatFrom());
        viewHolder.fromLong.setText("Long : "+track.getLngFrom());
        viewHolder.toLat.setText("Lat : "+track.getLatDest());
        viewHolder.toLong.setText("Long : "+track.getLngDest());

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> from = geocoder.getFromLocation(track.getLatFrom(),track.getLngFrom(),1),
                            to = geocoder.getFromLocation(track.getLatDest(),track.getLngDest(),1);
            viewHolder.tvFromAddress.setText(from.get(0).getAddressLine(0));
            viewHolder.tvToAddress.setText(to.get(0).getAddressLine(0));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return trackArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tangal, jarak, durasi, kalori, fromLat, fromLong, toLat, toLong, tvFromAddress, tvToAddress;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tangal = itemView.findViewById(R.id.tvTanggalList);
            jarak = itemView.findViewById(R.id.tvJarakList);
            durasi = itemView.findViewById(R.id.tvDurasiList);
            kalori = itemView.findViewById(R.id.tvKaloriList);
            fromLat = itemView.findViewById(R.id.tvFromLatList);
            fromLong = itemView.findViewById(R.id.tvFromLongList);
            toLat = itemView.findViewById(R.id.tvToLatList);
            toLong = itemView.findViewById(R.id.tvToLongList);
            tvFromAddress = itemView.findViewById(R.id.tvFromAddressList);
            tvToAddress = itemView.findViewById(R.id.tvToAddressList);
        }
    }
}
