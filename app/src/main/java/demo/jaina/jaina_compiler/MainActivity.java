package demo.jaina.jaina_compiler;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import jaina.annotation.Layout;
import jaina.api.BindView;

@Layout(packageName = "demo.jaina.jaina_compiler", value = "activity_main")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BindView.bindView(this, this);
        mHelloWord.setText("hello jaina Proudmoore");
    }

}
