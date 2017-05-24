package com.weareholidays.bia.activities.search;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.weareholidays.bia.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArticleFragment extends Fragment {

    WebView mWebView;
    ProgressBar mProgressBar;
    OnFragmentInteractionListener mListener;
    public static final String ARTICLE_URL = "ARTICLE_URL";
    private String articleUrl;
    private final String defaultUrl = "http://www.weareholidays.com/articles/?dummy=dummy&mode=app";

    public ArticleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            articleUrl = getArguments().getString(ARTICLE_URL, defaultUrl);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_article, container, false);
        setupViews(rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void setupViews(View view) {
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
        mProgressBar.setVisibility(View.VISIBLE);

        mWebView = (WebView) view.findViewById(R.id.webview);

        if (articleUrl == null) {
            articleUrl = defaultUrl;
        }

//        String url = "http://www.google.co.in";
        if (articleUrl != null)
            mWebView.loadUrl(articleUrl);

        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                // do your stuff here
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }

    public void goBack() {
        if (mWebView != null)
            mWebView.goBack();
    }

    public interface OnFragmentInteractionListener {

    }

    public boolean canGoBack() {
        return mWebView != null && mWebView.canGoBack();
    }

    public void setArticle(String url) {
        if (url != null) {
            mWebView.loadUrl(url);
            articleUrl = url;
        }
        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                // do your stuff here
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }

}
