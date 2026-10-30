package com.fu.ch08.t3;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.Random;
import java.util.function.Consumer;

public class ProcessTask extends Task<Summary> {
    private final int total;
    private final int delayMs;
    private final boolean failHalfway;
    private final Consumer<String> onErrorRecord;    // callback sẽ được gọi trên FX thread

    public ProcessTask(int total, int delayMs, boolean failHalfway, Consumer<String> onErrorRecord) {
        this.total = total;
        this.delayMs = delayMs;
        this.failHalfway = failHalfway;
        this.onErrorRecord = onErrorRecord;
    }

    @Override
    protected Summary call() throws Exception {
        long start = System.currentTimeMillis();
        int errors = 0, processed = 0;
        Random rnd = new Random();
        for (int i = 1; i <= total; i++) {
            if (isCancelled()) {                       // TODO 2a: kiểm tra hủy mỗi vòng
                updateMessage("Đã hủy tại bản ghi " + i);
                break;
            }
            if (failHalfway && i == total / 2) {
                throw new IllegalStateException("Dữ liệu hỏng tại bản ghi " + i);
            }
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {          // cancel() ngắt thread đang sleep
                if (isCancelled()) {
                    updateMessage("Đã hủy tại bản ghi " + i);
                    break;
                }
                throw e;
            }
            if (rnd.nextInt(100) < 3) {                // ~3% bản ghi lỗi
                errors++;
                String msg = "Bản ghi #" + i + ": email không hợp lệ";
                Platform.runLater(() -> onErrorRecord.accept(msg));   // đẩy cập nhật UI về FX thread
            }
            processed = i;
            updateProgress(i, total);
            updateMessage("Đang xử lý %d/%d".formatted(i, total));
        }
        return new Summary(total, processed, errors, System.currentTimeMillis() - start);
    }
}
