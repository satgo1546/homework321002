package com.jave.homework321002;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideosAdapter extends BaseAdapter implements Filterable {
    LayoutInflater inflater;
    List<PredefinedVideo> videoList;
    public List<PredefinedVideo> currentList;  //过滤后的数据
    MyFilter mFilter ;

    public VideosAdapter(Context context, List<PredefinedVideo> list){
        videoList = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        if (currentList == null) {
            return 0;
        }
        return currentList.size();
    }

    @Override
    public Object getItem(int i) {
        if (currentList == null) {
            return null;
        }
        return currentList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if(view == null){
            view = inflater.inflate(R.layout.list_item_layout, null);
            viewHolder = new ViewHolder();
            viewHolder.videoName = view.findViewById(R.id.text1);
            viewHolder.videoDescription = view.findViewById(R.id.textView);
            viewHolder.deleteButton = view.findViewById(R.id.btn_delete);
            viewHolder.thumbnailImage = view.findViewById(R.id.imageView);
            view.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.videoName.setText(currentList.get(i).getName());
        viewHolder.videoDescription.setText(currentList.get(i).getDescription());
        viewHolder.thumbnailImage.setImageDrawable(currentList.get(i).getThumbnailDrawable(view.getContext()));
        viewHolder.deleteButton.setOnClickListener(view1 -> {
            for (String suffix : new String []  {".txt", ".xml", ".mp4"}) {
                if (!new File(view1.getContext().getFilesDir(), currentList.get(i).getName() + suffix).delete()) return;
            }
            currentList.remove(i);
            ((MyApplication) view1.getContext().getApplicationContext()).rescanVideos();
            notifyDataSetChanged();
        });


        return view;
    }

    private class ViewHolder {
        public TextView videoName;
        public TextView videoDescription;
        public ImageView deleteButton;
        public ImageView thumbnailImage;
    }

    @Override
    public Filter getFilter() {

        if (mFilter ==null){
            mFilter = new MyFilter();
        }

        return mFilter;
    }

    class MyFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {

            FilterResults result = new FilterResults();

            List<PredefinedVideo> list ;

            if (TextUtils.isEmpty(charSequence)){//当过滤的关键字为空的时候，我们则显示所有的数据
                list  = videoList;
            }else {//否则把符合条件的数据对象添加到集合中
                list = new ArrayList();

                for (PredefinedVideo recomend: videoList){
                    if (recomend.getName().contains(charSequence)||recomend.getDescription().contains(charSequence)){
                        Log.d("filter", "performFiltering:"+recomend.toString());
                        list.add(recomend);
                    }
                }
            }

            result.values = list; //将得到的集合保存到FilterResults的value变量中
            result.count = list.size();//将集合的大小保存到FilterResults的count变量中

            return result;
        }

        //在publishResults方法中告诉适配器更新界面
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

            currentList = (List<PredefinedVideo>) filterResults.values;
            Log.d("filter", "publishResults:"+filterResults.count);
            notifyDataSetChanged();//通知数据发生了改变
        }
    }

}

