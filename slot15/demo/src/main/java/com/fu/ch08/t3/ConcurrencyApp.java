package com.fu.ch08.t3;

import com.fu.ch08.common.AlertUtil;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConcurrencyApp extends Application {

    private final ExecutorService pool = Executors.newFixedThreadPool(3, r -> {
        Thread t = new Thread(r, "worker");
        t.setDaemon(true); // daemon: không giữ JVM khi thoát
        return t;
    });
    private ProcessTask currentTask;
    private final List<ProcessTask> parallelTasks = new ArrayList<>();
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public void start(Stage stage) {
        // TODO 3: Đồng hồ 1 — Timeline (chạy trên FX thread, không cần runLater)
        Label clock1 = new Label();
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1),
                e -> clock1.setText("⏰ Timeline: " + LocalTime.now().format(TIME))));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        // TODO 4: Đồng hồ 2 — Thread + Platform.runLater
        Label clock2 = new Label();
        Thread clockThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                String now = LocalTime.now().format(TIME);
                Platform.runLater(() -> clock2.setText("🧵 Thread+runLater: " + now));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }, "clock");
        clockThread.setDaemon(true);
        clockThread.start();

        // TODO 5: Ví dụ SAI — chặn FX thread
        Button btnFreeze = new Button("⚠ Chạy trên UI thread (sai)");
        btnFreeze.setOnAction(e -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) { }
            AlertUtil.info("Xong sau 5 giây — trong lúc đó cửa sổ bị đơ, đồng hồ đứng yên");
        });

        // TODO 6: Khu vực xử lý 1 file
        TextField tfCount = new TextField("1000");
        CheckBox chkFail = new CheckBox("Gây lỗi ở 50%");
        Button btnStart = new Button("Bắt đầu");
        Button btnCancel = new Button("Hủy");
        ProgressBar pb = new ProgressBar(0);
        pb.setPrefWidth(400);
        Label lblPercent = new Label();
        Label lblMessage = new Label();
        ListView<String> lvErrors = new ListView<>();
        lvErrors.setPrefHeight(140);
        lvErrors.setPlaceholder(new Label("Chưa có bản ghi lỗi"));
        btnCancel.setDisable(true);

        btnStart.setOnAction(e -> {
            int n;
            try {
                n = Integer.parseInt(tfCount.getText().trim());
            } catch (NumberFormatException ex) {
                AlertUtil.warn("Số bản ghi phải là số nguyên");
                return;
            }
            if (n < 100 || n > 10_000) {
                AlertUtil.warn("Số bản ghi từ 100 đến 10.000");
                return;
            }

            lvErrors.getItems().clear();
            currentTask = new ProcessTask(n, 2, chkFail.isSelected(), lvErrors.getItems()::add);

            // TODO 6a: Binding UI ↔ Task
            pb.progressProperty().bind(currentTask.progressProperty());
            lblPercent.textProperty().bind(currentTask.progressProperty().multiply(100).asString("%.0f%%"));
            lblMessage.textProperty().bind(currentTask.messageProperty());
            btnStart.disableProperty().bind(currentTask.runningProperty());
            btnCancel.disableProperty().bind(currentTask.runningProperty().not());
            tfCount.disableProperty().bind(currentTask.runningProperty());

            // TODO 6b: Callback — chạy trên FX thread
            currentTask.setOnSucceeded(ev -> {
                Summary s = currentTask.getValue();
                AlertUtil.info("Hoàn tất: %d/%d bản ghi, %d lỗi, %.1f giây".formatted(
                        s.processed(), s.total(), s.errors(), s.millis() / 1000.0));
            });
            currentTask.setOnFailed(ev -> AlertUtil.error("Lỗi: " + currentTask.getException().getMessage()));
            currentTask.setOnCancelled(ev -> AlertUtil.warn("Đã hủy xử lý"));

            pool.submit(currentTask);
        });
        btnCancel.setOnAction(e -> {
            if (currentTask != null) currentTask.cancel();
        });

        // TODO 7: Xử lý song song 3 file
        VBox parallelBox = new VBox(6);
        Button btnParallel = new Button("Chạy 3 file song song");
        Button btnCancelAll = new Button("Hủy tất cả");
        btnParallel.setOnAction(e -> {
            parallelBox.getChildren().clear();
            parallelTasks.clear();
            int[] speeds = {1, 3, 6};
            for (int i = 0; i < 3; i++) {
                ProcessTask t = new ProcessTask(800, speeds[i], false, msg -> { });
                ProgressBar p = new ProgressBar();
                p.setPrefWidth(300);
                p.progressProperty().bind(t.progressProperty());
                Label l = new Label();
                l.textProperty().bind(t.messageProperty());
                parallelBox.getChildren().add(new HBox(10, new Label("File " + (i + 1)), p, l));
                parallelTasks.add(t);
                pool.submit(t);
            }
        });
        btnCancelAll.setOnAction(e -> parallelTasks.forEach(Task::cancel));

        VBox root = new VBox(12,
                new HBox(30, clock1, clock2), btnFreeze, new Separator(),
                new Label("Xử lý 1 file"), new HBox(8, new Label("Số bản ghi"), tfCount, chkFail, btnStart, btnCancel),
                new HBox(10, pb, lblPercent), lblMessage, new Label("Bản ghi lỗi:"), lvErrors, new Separator(),
                new HBox(8, btnParallel, btnCancelAll), parallelBox);
        root.setPadding(new Insets(16));

        stage.setTitle("T3 - Concurrency");
        stage.setScene(new Scene(root, 760, 640));
        stage.show();
    }

    // TODO 8: Dọn dẹp thread khi thoát
    @Override
    public void stop() {
        pool.shutdownNow();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
