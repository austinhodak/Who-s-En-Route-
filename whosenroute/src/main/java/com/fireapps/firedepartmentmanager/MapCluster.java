package com.fireapps.firedepartmentmanager;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.util.LruCache;

import com.androidmapsextensions.ClusterOptions;
import com.androidmapsextensions.ClusterOptionsProvider;
import com.androidmapsextensions.Marker;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.List;

/**
 * Created by austinhodak on 7/13/16.
 */

public class MapCluster implements ClusterOptionsProvider {

    private static final int[] res = {R.drawable.marker_cluster, R.drawable.marker_cluster, R.drawable.marker_cluster, R.drawable.marker_cluster, R.drawable.marker_cluster};

    private static final int[] forCounts = {10, 100, 1000, 10000, Integer.MAX_VALUE};

    private Bitmap baseBitmaps;
    private LruCache<Integer, BitmapDescriptor> cache = new LruCache<>(128);

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Rect bounds = new Rect();

    private ClusterOptions clusterOptions = new ClusterOptions().anchor(0.5f, 0.5f);

    public MapCluster(Resources resources) {
        Bitmap bitmap = Bitmap.createBitmap(168, 168, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint2 = new Paint();
        paint2.setColor(resources.getColor(R.color.md_blue_500));
        paint2.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawCircle(84, 84, 84, paint2);

        baseBitmaps = bitmap;

        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(resources.getDimension(R.dimen.text_size2));

    }

    @Override
    public ClusterOptions getClusterOptions(List<Marker> markers) {

        int markersCount = markers.size();
        BitmapDescriptor cachedIcon = cache.get(markersCount);
        if (cachedIcon != null) {
            return clusterOptions.icon(cachedIcon);
        }

        Bitmap base = null;
        int i = 0;
        do {
            //base = baseBitmaps[i];
        } while (markersCount >= forCounts[i++]);

        Bitmap bitmap = baseBitmaps.copy(Bitmap.Config.ARGB_8888, true);

        String text = String.valueOf(markersCount);
        paint.getTextBounds(text, 0, text.length(), bounds);
        float x = bitmap.getWidth() / 2.0f;
        float y = (bitmap.getHeight() - bounds.height()) / 2.0f - bounds.top;

        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(text, x, y, paint);

        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);
        cache.put(markersCount, icon);

        return clusterOptions.icon(icon);
    }
}
