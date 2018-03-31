package com.xxx.reader.comic;

import com.xxx.reader.Url2FileMapper;
import com.xxx.reader.core.DrawParam;
import com.xxx.reader.prepare.PageBitmapCreator;
import com.xxx.reader.prepare.PrepareLayer;
import com.xxx.reader.prepare.PrepareLayerBuilder;

/**
 * Created by HP on 2018/3/23.
 */

public class ComicPrepareLayer {

    public PrepareLayer<ComicChapter, ComicPageInfo, DrawParam, ComicPageBitmap> ComicPrepareLayer() {
        PrepareLayerBuilder<ComicChapter, ComicPageInfo, DrawParam, ComicPageBitmap> builder = new PrepareLayerBuilder();
        builder.setCreator(new PageBitmapCreator<ComicPageBitmap>() {
            @Override
            public ComicPageBitmap create() {
                return null;
            }
        });
        builder.setiDrawable(null);
        builder.setPrepareJob(new ComicPrepareJob(new Url2FileMapper<ComicChapter>() {
            @Override
            public String map(ComicChapter iChapter, String url) {
                return null;
            }

            @Override
            public String map(String chapterName) {
                return null;
            }
        }));


        return builder.createPrepareLayer();
    }


}
