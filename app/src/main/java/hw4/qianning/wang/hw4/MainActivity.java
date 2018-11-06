package hw4.qianning.wang.hw4;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    GamePanel gamePanel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gamePanel=new GamePanel(this,getResources().getConfiguration().orientation, Helper.saveThingGrid);
        setContentView(gamePanel);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gamePanel.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gamePanel.onPause();
    }
}
