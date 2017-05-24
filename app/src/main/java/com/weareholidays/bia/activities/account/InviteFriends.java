package com.weareholidays.bia.activities.account;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.WebViewActivity;
import com.weareholidays.bia.activities.login.TermsActivity;
import com.weareholidays.bia.asyncTasks.ShortenURLTask;
import com.weareholidays.bia.parse.utils.ShareUtils;

public class InviteFriends extends AppCompatActivity implements View.OnClickListener {

    Button mInviteButton;
    TextView mDetailText;
    private long sLastClickTime = 0;
    private ShortenURLTask mShortenURLTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);
        inflateToolbar();
        initViews();
    }

    private void initViews() {
        mDetailText = (TextView) findViewById(R.id.invite_friend_detail_text);
        mInviteButton = (Button) findViewById(R.id.invite_friends_btn);

        mDetailText.setOnClickListener(this);
        mInviteButton.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_invite_friends, menu);
        return true;
    }

    void inflateToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.invite_friends_btn:
                inviteFriends();
                break;
            case R.id.invite_friend_detail_text:
                showDetailDialog();
                break;
            default:
                break;

        }
    }

    private void inviteFriends() {
        // Preventing multiple clicks, using threshold of 1 second
        if (SystemClock.elapsedRealtime() - sLastClickTime < 1000) {
            return;
        }
        sLastClickTime = SystemClock.elapsedRealtime();

        String sendUrl = ShareUtils.getPlayStoreUrl(InviteFriends.this);
        mShortenURLTask = new ShortenURLTask(InviteFriends.this, null, null, false, true);
        mShortenURLTask.execute(sendUrl);
    }

    private void showDetailDialog() {

        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .content("")
                .positiveText("CLOSE")
                .positiveColor(getResources().getColor(R.color.orange_primary))
                .cancelable(true)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                    }
                })
                .show();

        dialog.getContentView().setMovementMethod(LinkMovementMethod.getInstance());
        dialog.getContentView().setText(Html.fromHtml(getResources().getString(R.string.dialog_invite_friends_desc)));

        SpannableString ss = new SpannableString(getResources().getString(R.string.dialog_invite_friends_desc));

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Intent intent = new Intent(InviteFriends.this,WebViewActivity.class);
                intent.putExtra(WebViewActivity.WEB_VIEW_TITLE,"Referral Terms");
                intent.putExtra(WebViewActivity.WEB_VIEW_URL,"http://bia-app.com/referral-terms.html");
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };

        ss.setSpan(clickableSpan, ss.length()-20, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ForegroundColorSpan fcs = new ForegroundColorSpan(getResources().getColor(R.color.app_sky_blue));
        ss.setSpan(fcs, ss.length()-20, ss.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        dialog.getContentView().setText(ss);
        dialog.getContentView().setMovementMethod(LinkMovementMethod.getInstance());
        dialog.getContentView().setHighlightColor(Color.TRANSPARENT);
    }
}
