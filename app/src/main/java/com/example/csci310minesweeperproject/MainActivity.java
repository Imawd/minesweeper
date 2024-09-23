package com.example.csci310minesweeperproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private int ROWS = 12;
    private int COLS = 10;
    private int MINE_COUNT = 4;
    private int[][] grid;  // 0 for no mine, 1 for mine
    private boolean[][] revealedCells;
    private boolean[][] flaggedCells;
    private boolean diggingMode = true;
    private Button[][] buttons;
    private TextView mineCounter, timer;
    private Button modeButton;
    private int remainingMines = MINE_COUNT;
    private int elapsedTime = 0;
    private Handler handler = new Handler();
    private boolean gameOver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mineCounter = findViewById(R.id.mineCounter);
        timer = findViewById(R.id.timer);
        modeButton = findViewById(R.id.modeButton);

        mineCounter.setText("Mines:" + remainingMines);
        buttons = new Button[ROWS][COLS];
        grid = new int[ROWS][COLS];
        revealedCells = new boolean[ROWS][COLS];
        flaggedCells = new boolean[ROWS][COLS];
        remainingMines = MINE_COUNT;
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        placeMines();
        // Ensuring button size is what we want
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int buttonSize = screenWidth / COLS; // Make buttons square based on screen width
        buttons = new Button[ROWS][COLS];

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                final int currentRow = row;
                final int currentCol = col;
                Button button = new Button(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = buttonSize;
                params.height = buttonSize;
                params.rowSpec = GridLayout.spec(row);
                params.columnSpec = GridLayout.spec(col);
                button.setLayoutParams(params);
                button.setOnClickListener(v -> handleCellClick(currentRow, currentCol));
                buttons[row][col] = button;
                gridLayout.addView(button);
            }
        }

        // Start the timer
        startTimer();

        // Toggle between digging and flagging modes
        modeButton.setOnClickListener(v -> toggleMode());
    }
    private void placeMines() {
        Random random = new Random();
        Set<String> minePositions = new HashSet<>();

        while (minePositions.size() < MINE_COUNT) {
            int row = random.nextInt(ROWS);
            int col = random.nextInt(COLS);
            String pos = row + "," + col;
            if (!minePositions.contains(pos)) {
                grid[row][col] = 1;
                minePositions.add(pos);
            }
        }
    }
    private void handleCellClick(int row, int col) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) {
            return;
        }
        if (revealedCells[row][col]) {
            return;
        }
        if (diggingMode) {
            if (!flaggedCells[row][col] && !revealedCells[row][col]) {
                revealCell(row, col);
            }
        } else {
            if (grid[row][col] == 1 && !flaggedCells[row][col]) {
                remainingMines -= 1;
                mineCounter.setText("Mines:" + remainingMines);
            }
            else if (grid[row][col] == 1 && flaggedCells[row][col]) {
                remainingMines += 1;
                mineCounter.setText("Mines:" + remainingMines);
            }
            flaggedCells[row][col] = !flaggedCells[row][col];
            buttons[row][col].setText(flaggedCells[row][col] ? "F" : "");
        }
    }

    private void revealCell(int row, int col) {
        if (revealedCells[row][col]) {
            return;
        }
        revealedCells[row][col] = true;
        if (grid[row][col] == 1) {

            goToResultPage(false);
            buttons[row][col].setText("M");
            return;
        } else {
            int adjacentMines = countAdjacentMines(row, col);
            buttons[row][col].setText(String.valueOf(adjacentMines));

            if (adjacentMines == 0) {
                revealAdjacentCells(row, col);
            }
        }

        checkGameWin();
    }


    private int countAdjacentMines(int row, int col) {
        int mineCount = 0;
        // Combinations of directions correspond to each adjacent direction
        int[] dRow = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dCol = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < 8; i++) {
            int newRow = row + dRow[i];
            int newCol = col + dCol[i];
            // Check if corresponding cell exists
            if (newRow >= 0 && newRow < ROWS && newCol >= 0 && newCol < COLS) {
                if (grid[newRow][newCol] == 1) {
                    mineCount++;
                }
            }
        }
        return mineCount;
    }

    private void revealAdjacentCells(int row, int col) {
        // Reference above comment on directions
        int[] dRow = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dCol = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < 8; i++) {
            int newRow = row + dRow[i];
            int newCol = col + dCol[i];
            if (newRow >= 0 && newRow < ROWS && newCol >= 0 && newCol < COLS && !revealedCells[newRow][newCol]) {
                revealCell(newRow, newCol);
            }
        }
    }

    private void checkGameWin() {
        // Count how many revealed cells there are, if there are as many revealed cells as there are cells on the board minus the mines
        int revealedCount = 0;
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (revealedCells[i][j]) {
                    revealedCount++;
                }
            }
        }

        if (revealedCount == (ROWS * COLS - MINE_COUNT)) {
            goToResultPage(true);
        }
    }

    private void toggleMode() {
        diggingMode = !diggingMode;
        modeButton.setText(diggingMode ? "Mode: Digging" : "Mode: Flagging");
    }

    private void startTimer() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                elapsedTime++;
                timer.setText("Time: " + elapsedTime + "s");
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    // Call this method when the game is over to go to the result page
    private void goToResultPage(boolean won) {
        Intent intent = new Intent(MainActivity.this, ResultActivity.class);
        intent.putExtra("secondsUsed", elapsedTime);
        intent.putExtra("result", won ? "won" : "lost");
        startActivity(intent);
    }
}
