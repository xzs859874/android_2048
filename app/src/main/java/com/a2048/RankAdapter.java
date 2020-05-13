package com.a2048;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class RankAdapter extends ArrayAdapter<RankBean> {
    private int resourceId;

    public RankAdapter(Context context, int textViewResourceId, List<RankBean> objects){
        super(context,textViewResourceId,objects);
        resourceId  = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        RankBean rankBean = getItem(position);
        View view;
        if (convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        }else {
            view = convertView;
        }
        TextView id_rank = (TextView) view.findViewById(R.id.id_t);
        TextView name_rank = (TextView) view.findViewById(R.id.name_t);
        TextView score_rank = (TextView) view.findViewById(R.id.score_t);
        TextView num_rank = (TextView) view.findViewById(R.id.num_t);
        name_rank.setText(rankBean.getName());
        score_rank.setText(rankBean.getScore().toString());
        id_rank .setText(rankBean.getId().toString());
        num_rank.setText(rankBean.getNum().toString());

//        ImageView fruitImage = (ImageView) view.findViewById(R.id.fruit_imgs);
//        TextView fruitName = (TextView) view.findViewById(R.id.fruit_name);
//        fruitImage.setImageResource(rankBean.getimgId());
//        fruitName.setText(rankBean.getName());
        return view;
    }
}
