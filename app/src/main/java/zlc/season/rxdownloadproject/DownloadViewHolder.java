package zlc.season.rxdownloadproject;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import zlc.season.practicalrecyclerview.AbstractViewHolder;
import zlc.season.rxdownload.DownloadStatus;
import zlc.season.rxdownload.RxDownload;

import static zlc.season.rxdownloadproject.R.id.percent;
import static zlc.season.rxdownloadproject.R.id.status;

/**
 * Author: Season(ssseasonnn@gmail.com)
 * Date: 2016/10/28
 * Time: 09:37
 * FIXME
 */
public class DownloadViewHolder extends AbstractViewHolder<DownloadBean> {

    @BindView(R.id.img)
    ImageView mImg;
    @BindView(percent)
    TextView mPercent;
    @BindView(R.id.progress)
    ProgressBar mProgress;
    @BindView(R.id.size)
    TextView mSize;
    @BindView(status)
    Button mStatus;

    private DownloadBean data;
    private Context mContext;

    public DownloadViewHolder(ViewGroup parent) {
        super(parent, R.layout.service_download_item);
        ButterKnife.bind(this, itemView);
        mContext = parent.getContext();
    }

    @Override
    public void setData(DownloadBean param) {
        this.data = param;
        Picasso.with(mContext).load(param.image).into(mImg);
        mStatus.setText("开始");
    }

    @OnClick(status)
    public void onClick1() {
        if (data.state == DownloadBean.START) {
            data.state = DownloadBean.PAUSE;
            mStatus.setText("暂停");
            data.subscription = RxDownload.getInstance().downloadWithService(mContext, data.url, data.name, null)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<DownloadStatus>() {
                        @Override
                        public void onStart() {
                            super.onStart();

                        }

                        @Override
                        public void onCompleted() {
                            data.state = DownloadBean.DONE;
                            mStatus.setText("已完成");
                        }

                        @Override
                        public void onError(Throwable e) {
                            data.state = DownloadBean.START;
                            mStatus.setText("继续");
                        }

                        @Override
                        public void onNext(DownloadStatus status) {
                            mProgress.setIndeterminate(status.isChunked);
                            mProgress.setMax((int) status.getTotalSize());
                            mProgress.setProgress((int) status.getDownloadSize());
                            mPercent.setText(status.getPercent());
                            mSize.setText(status.getFormatStatusString());
                        }
                    });
        } else if (data.state == DownloadBean.PAUSE) {
            data.unsubscrbe();
            data.state = DownloadBean.START;
            mStatus.setText("继续");
        }
    }
}
