package gov.unsc.routemapper;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Objects;

public class AchievementActivity extends AppCompatActivity {

    private HashMap<String, Achievement> achievements;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);

        LinearLayout nLayout = findViewById(R.id.nameList);
        LinearLayout pLayout = findViewById(R.id.pictureList);
        LinearLayout vLayout = findViewById(R.id.valueList);

        achievements = (HashMap<String, Achievement>) getIntent().getSerializableExtra("achieves");

        System.out.println(getIntent().getSerializableExtra("achieves"));
        for (Achievement achievement : achievements.values()) {
            TextView tv = new TextView(this);
            tv.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            tv.setTextSize(14);
            tv.setText("\n" + achievement.getName());
            nLayout.addView(tv);

            ImageView iv = new ImageView(this);
            iv.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            iv.setImageResource(achievement.getImage());
            iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            iv.setAdjustViewBounds(true);
            iv.setMaxHeight(150);
            pLayout.addView(iv);

            RadioButton rb = new RadioButton(this);
            rb.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            rb.setClickable(false);
            rb.setChecked(achievement.isAchieved());
            rb.setPadding(0, 0, 0, 100);
            vLayout.addView(rb);
        }
    }

    public void emailClick(View v) {
        email(achievements);
    }

    private String format(HashMap<String, Achievement> achievements) {
        StringBuilder out = new StringBuilder();
        out.append("Achievements: \n");
        for (Achievement a :
                achievements.values()) {
            if (a.isAchieved())
                out.append(a.getName()).append("\n");
        }
        return out.toString();
    }

    private void email(HashMap<String, Achievement> achievements) {
        Intent emailIntent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.parse("mailto:?subject=" + "Achievements"+ "&body=" + format(achievements) + "&to=");
        emailIntent.setData(data);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(AchievementActivity.this,
                    "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
