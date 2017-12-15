package za.healthtracking.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vn.zing.pedometer.R;
import za.healthtracking.app.Settings;


public class UserProfileActivity extends AppCompatActivity {

    @BindView(R.id.radioMale)
    RadioButton radioMale;
    @BindView(R.id.radioFemale)
    RadioButton radioFemale;
    @BindView(R.id.txtWeight)
    EditText txtWeight;
    @BindView(R.id.txtStatement)
    TextView txtStatement;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        ButterKnife.bind(this);
        init();
    }

    private void init() {
        setSupportActionBar(toolbar);

        radioMale.setChecked(Settings.getUserProfileIsMale());
        radioFemale.setChecked(!Settings.getUserProfileIsMale());

        if (Settings.getUserProfileWeight() >= 10) {
            txtWeight.setText(Settings.getUserProfileWeight() + "");
            txtStatement.setText("Được sử dụng để tính lượng Calo tiêu thụ");
        } else {
            txtWeight.setText("");
            txtStatement.setText("Đây là lần đầu tiên bạn sử dụng ứng dụng.\nChúng tôi cần những thông tin này để tính chính xác lượng Calo tiêu thụ của bạn");
        }
    }

    @OnClick(R.id.btnDone)
    void onDone() {
        String strWeight = txtWeight.getText().toString();
        if (TextUtils.isEmpty(strWeight)) {
            txtWeight.setError("Cân nặng không thể bỏ trống");
            return;
        }

        if (Float.parseFloat(strWeight) < 10) {
            txtWeight.setError("Cân nặng không thể nhỏ hơn 10");
            return;
        }

        Settings.setUserProfileIsMale(radioMale.isChecked());
        Settings.setUserProfileWeight(Float.parseFloat(strWeight));
        finish();
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Hãy nhấn hoàn thành nếu đã nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
    }
}
