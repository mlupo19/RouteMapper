package gov.unsc.routemapper;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Objects;

public class AchievementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);

        LinearLayout nLayout = findViewById(R.id.nameList);
        LinearLayout pLayout = findViewById(R.id.pictureList);
        LinearLayout vLayout = findViewById(R.id.valueList);

        System.out.println(getIntent().getSerializableExtra("achieves"));
        for (Achievement achievement : ((HashMap<String, Achievement>) getIntent().getSerializableExtra("achieves")).values()) {
            TextView tv = new TextView(this);
            tv.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            tv.setText(achievement.getName());
            nLayout.addView(tv);

            ImageView iv = new ImageView(this);
            iv.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            iv.setImageResource(achievement.getImage());
            pLayout.addView(iv);

            RadioButton rb = new RadioButton(this);
            rb.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            rb.setClickable(false);
            rb.setChecked(achievement.isAchieved());
            vLayout.addView(rb);
        }
    }
}
