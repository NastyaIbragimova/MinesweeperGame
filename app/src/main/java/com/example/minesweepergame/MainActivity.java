package com.example.minesweepergame;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private GridLayout gridLayout;
    private final int SIDE = 8;
    private Cell[][] gameField = new Cell[SIDE][SIDE];
    private ImageView[][] imageArray = new ImageView[SIDE][SIDE];
    private int countClosedTiles = SIDE * SIDE;
    private int countMinesOnField;
    private int countFlags;
    private boolean isGameStopped;
    private int score;
    private TextView tvScore, tvFinish, tvMine;
    private int[] fonsArray = new int[]{R.drawable.fon2, R.drawable.fon_number1, R.drawable.fon_number2, R.drawable.fon_number3, R.drawable.fon_number4, R.drawable.fon_number5, R.drawable.fon_number6};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
    }

    private void initialize() {
        gridLayout = findViewById(R.id.grid);
        tvScore = findViewById(R.id.tv_score);
        tvFinish = findViewById(R.id.tv_final);
        tvMine = findViewById(R.id.tv_mine);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayWidth = size.x;
        for (int y = 0; y < SIDE; y++) {
            for (int x = 0; x < SIDE; x++) {
                ImageView imageView = new ImageView(this);
                imageView.setImageResource(R.drawable.fon1);
                int cellSize = (displayWidth / SIDE);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(cellSize, cellSize);
                imageView.setLayoutParams(params);
                int finalX = x;
                int finalY = y;
                imageArray[y][x] = imageView;

                Random r = new Random();
                int ran = r.nextInt(10);
                boolean isMine = ran < 1;
                if (isMine) {
                    countMinesOnField++;
                }
                gameField[y][x] = new Cell(x, y, isMine);

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isGameStopped) {
                            restart();
                        }
                        if (!isGameStopped) {
                            openTile(finalX, finalY, imageView);
                        }
                    }
                });

                imageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        markTile(finalX, finalY, imageView);
                        return true;
                    }
                });
                gridLayout.addView(imageView);
            }
        }
        countMineNeighbors();
        countFlags = countMinesOnField;
        tvMine.setText(getString(R.string.mine) + " " + countMinesOnField);
    }

    private void countMineNeighbors() {
        List<Cell> list;
        for (int x = 0; x < SIDE; x++) {
            for (int y = 0; y < SIDE; y++) {
                if (!gameField[y][x].isMine) {
                    list = getNeighbors(gameField[y][x]);
                    for (Cell object : list) {
                        if (object.isMine) {
                            gameField[y][x].countMineNeighbors++;
                        }
                    }
                }
            }
        }
    }

    private void openTile(int x, int y, ImageView imageView) {
        if (!isGameStopped && !gameField[y][x].isOpen && !gameField[y][x].isFlag) {
            if (gameField[y][x].isMine) {
                gameField[y][x].isOpen = true;
                countClosedTiles--;
                imageView.setImageResource(R.drawable.mine);
                imageView.setEnabled(true);
                gameOver();
            } else if (!gameField[y][x].isMine && gameField[y][x].countMineNeighbors == 0) {
                gameField[y][x].isOpen = true;
                countClosedTiles--;
                imageView.setImageResource(fonsArray[gameField[y][x].countMineNeighbors]);
                imageView.setEnabled(true);
                List<Cell> list = getNeighbors(gameField[y][x]);
                for (Cell o : list) {
                    if (!o.isOpen) {
                        openTile(o.x, o.y, imageArray[o.y][o.x]);
                    }
                }
                score += 5;
                setScore(score);
                if (countClosedTiles == countMinesOnField) {
                    win();
                }
            } else {
                gameField[y][x].isOpen = true;
                countClosedTiles--;
                imageView.setImageResource(fonsArray[gameField[y][x].countMineNeighbors]);
                imageView.setEnabled(true);
                score += 5;
                setScore(score);
                if (countClosedTiles == countMinesOnField) {
                    win();
                }
            }
        }
    }

    private void setScore(int score) {
        tvScore.setText(getString(R.string.score) + " " + score);
    }

    private void markTile(int x, int y, ImageView imageView) {
        if (!isGameStopped) {
            if (!gameField[y][x].isOpen && countFlags != 0) {
                if (!gameField[y][x].isFlag) {
                    gameField[y][x].isFlag = true;
                    countFlags--;
                    imageView.setImageResource(R.drawable.flag);
                    imageView.setOnClickListener(null);
                } else if (gameField[y][x].isFlag) {
                    gameField[y][x].isFlag = false;
                    countFlags++;
                    imageView.setImageResource(R.drawable.fon1);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isGameStopped) {
                                restart();
                            }
                            if (!isGameStopped) {
                                openTile(x, y, imageView);
                            }
                        }
                    });
                }
            }
        }
    }

    private void gameOver() {
        isGameStopped = true;
        setImageEnable();
        tvFinish.setText(R.string.lose);
        tvFinish.setVisibility(View.VISIBLE);
    }

    private void win() {
        isGameStopped = true;
        setImageEnable();
        tvFinish.setText(R.string.win);
        tvFinish.setVisibility(View.VISIBLE);
    }

    private void restart() {
        tvFinish.setVisibility(View.INVISIBLE);
        gridLayout.removeAllViews();
        isGameStopped = false;
        countClosedTiles = SIDE * SIDE;
        score = 0;
        countMinesOnField = 0;
        setScore(score);
        initialize();
    }

    private List<Cell> getNeighbors(Cell gameObject) {
        List<Cell> result = new ArrayList<>();
        for (int y = gameObject.y - 1; y <= gameObject.y + 1; y++) {
            for (int x = gameObject.x - 1; x <= gameObject.x + 1; x++) {
                if (y < 0 || y >= SIDE) {
                    continue;
                }
                if (x < 0 || x >= SIDE) {
                    continue;
                }
                if (gameField[y][x] == gameObject) {
                    continue;
                }
                result.add(gameField[y][x]);
            }
        }
        return result;
    }

    private void setImageEnable() {
        for (int i = 0; i < SIDE; i++) {
            for (int j = 0; j < SIDE; j++) {
                imageArray[j][i].setEnabled(true);
                imageArray[j][i].setOnClickListener(null);
            }
        }
    }

    public void onClickRestart(View view) {
        restart();
    }
}