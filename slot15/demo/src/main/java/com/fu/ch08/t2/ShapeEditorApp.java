package com.fu.ch08.t2;

import com.fu.ch08.common.AlertUtil;
import javafx.animation.*;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ShapeEditorApp extends Application {

    enum Tool { SELECT, RECT, CIRCLE, LINE, TRIANGLE }

    private final Pane drawPane = new Pane();
    private final ObjectProperty<Tool> tool = new SimpleObjectProperty<>(Tool.SELECT);
    private final ObjectProperty<Shape> selected = new SimpleObjectProperty<>();
    private final ColorPicker cpFill = new ColorPicker(Color.web("#64b5f6"));
    private final ColorPicker cpStroke = new ColorPicker(Color.web("#0d47a1"));
    private final Slider slStroke = new Slider(0, 10, 2);
    private final Slider slRotate = new Slider(0, 360, 0);
    private final Slider slScale = new Slider(0.2, 3, 1);
    private final Slider slOpacity = new Slider(0, 1, 1);
    private final CheckBox chkBlur = new CheckBox("Blur");
    private final Label lblStatus = new Label();

    private double startX, startY, dragOffsetX, dragOffsetY;
    private Shape drawing;              // shape đang được vẽ

    @Override
    public void start(Stage stage) {
        // TODO 1: Toolbar chế độ
        ToggleGroup tg = new ToggleGroup();
        HBox modeBox = new HBox(4);
        for (Tool t : Tool.values()) {
            ToggleButton b = new ToggleButton(switch (t) {
                case SELECT -> "🖱 Chọn";
                case RECT -> "▭ Chữ nhật";
                case CIRCLE -> "◯ Tròn";
                case LINE -> "╱ Đường";
                case TRIANGLE -> "△ Tam giác";
            });
            b.setToggleGroup(tg);
            b.setUserData(t);
            if (t == Tool.SELECT) b.setSelected(true);
            modeBox.getChildren().add(b);
        }
        tg.selectedToggleProperty().addListener((o, a, n) -> {
            if (n == null) a.setSelected(true);          // không cho bỏ chọn hết
            else tool.set((Tool) n.getUserData());
        });
        Button btnClear = new Button("Xóa tất cả");
        btnClear.setOnAction(e -> {
            if (AlertUtil.confirm("Xóa toàn bộ hình?")) {
                drawPane.getChildren().clear();
                selected.set(null);
            }
        });
        ToolBar toolbar = new ToolBar(
                modeBox, new Separator(),
                new Label("Tô"), cpFill,
                new Label("Viền"), cpStroke,
                new Label("Độ dày"), slStroke,
                new Separator(), btnClear
        );

        // TODO 2: Vùng vẽ — clip theo kích thước
        drawPane.setStyle("-fx-background-color: white;");
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(drawPane.widthProperty());
        clip.heightProperty().bind(drawPane.heightProperty());
        drawPane.setClip(clip);
        drawPane.setOnMousePressed(this::onPressed);
        drawPane.setOnMouseDragged(this::onDragged);
        drawPane.setOnMouseReleased(this::onReleased);
        drawPane.setOnMouseMoved(e -> updateStatus(e.getX(), e.getY()));

        // TODO 3: Panel thuộc tính & animation
        Button btnSpin = new Button("Xoay 360°");
        Button btnBlink = new Button("Nhấp nháy");
        Button btnColor = new Button("Đổi màu liên tục");
        btnSpin.setOnAction(e -> spin());
        btnBlink.setOnAction(e -> blink());
        btnColor.setOnAction(e -> colorCycle());
        VBox right = new VBox(8,
                new Label("Xoay"), slRotate,
                new Label("Phóng to"), slScale,
                new Label("Opacity"), slOpacity,
                chkBlur, new Separator(),
                btnSpin, btnBlink, btnColor
        );
        right.setPadding(new Insets(10));
        right.setPrefWidth(200);
        right.disableProperty().bind(selected.isNull());      // chưa chọn shape → panel disable

        // TODO 4: Khi đổi shape được chọn → chuyển binding
        selected.addListener((o, oldS, newS) -> onSelectionChanged(oldS, newS));

        BorderPane root = new BorderPane(drawPane, toolbar, right, new HBox(lblStatus), null);
        Scene scene = new Scene(root, 1100, 700);

        // TODO 5: Phím tắt
        scene.setOnKeyPressed(e -> {
            Shape s = selected.get();
            if (s == null) return;
            switch (e.getCode()) {
                case DELETE, BACK_SPACE -> {
                    drawPane.getChildren().remove(s);
                    selected.set(null);
                }
                case ESCAPE -> selected.set(null);
                case LEFT -> s.setTranslateX(s.getTranslateX() - 5);
                case RIGHT -> s.setTranslateX(s.getTranslateX() + 5);
                case UP -> s.setTranslateY(s.getTranslateY() - 5);
                case DOWN -> s.setTranslateY(s.getTranslateY() + 5);
                default -> { }
            }
            updateStatus(0, 0);
        });

        stage.setTitle("T2 - Shape Editor");
        stage.setScene(scene);
        stage.show();
    }

    // TODO 6: Mouse pressed
    private void onPressed(MouseEvent e) {
        if (e.getButton() != MouseButton.PRIMARY) return;
        startX = e.getX();
        startY = e.getY();
        if (tool.get() == Tool.SELECT) {
            if (e.getTarget() instanceof Shape s && s.getParent() == drawPane) {
                selected.set(s);
                dragOffsetX = e.getX() - s.getTranslateX();
                dragOffsetY = e.getY() - s.getTranslateY();
            } else {
                selected.set(null);                             // click vùng trống → bỏ chọn
            }
            return;
        }
        drawing = switch (tool.get()) {
            case RECT -> new Rectangle(startX, startY, 0, 0);
            case CIRCLE -> new Circle(startX, startY, 0);
            case LINE -> new Line(startX, startY, startX, startY);
            case TRIANGLE -> new Polygon(startX, startY, startX, startY, startX, startY);
            default -> null;
        };
        if (drawing == null) return;
        if (!(drawing instanceof Line)) drawing.setFill(cpFill.getValue());
        drawing.setStroke(cpStroke.getValue());
        drawing.setStrokeWidth(Math.max(1, slStroke.getValue()));
        installContextMenu(drawing);
        drawPane.getChildren().add(drawing);
    }

    // TODO 7: Mouse dragged
    private void onDragged(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();
        updateStatus(x, y);
        if (tool.get() == Tool.SELECT) {
            Shape s = selected.get();
            if (s != null) {
                s.setTranslateX(x - dragOffsetX);
                s.setTranslateY(y - dragOffsetY);
            }
            return;
        }
        if (drawing == null) return;
        switch (drawing) {
            case Rectangle r -> {
                r.setX(Math.min(startX, x));
                r.setY(Math.min(startY, y));
                r.setWidth(Math.abs(x - startX));
                r.setHeight(Math.abs(y - startY));
            }
            case Circle c -> c.setRadius(Math.hypot(x - startX, y - startY));
            case Line l -> {
                l.setEndX(x);
                l.setEndY(y);
            }
            case Polygon p -> p.getPoints().setAll(startX + (x - startX) / 2, startY, startX, y, x, y);
            case null, default -> { }
        }
    }

    // TODO 8: Mouse released — bỏ shape quá nhỏ (click nhầm)
    private void onReleased(MouseEvent e) {
        if (drawing == null) return;
        Bounds b = drawing.getLayoutBounds();
        if (b.getWidth() < 5 && b.getHeight() < 5) {
            drawPane.getChildren().remove(drawing);
        }
        drawing = null;
        updateStatus(e.getX(), e.getY());
    }

    // TODO 9: Chuyển binding sang shape mới
    private final DropShadow selectionEffect = new DropShadow(12, Color.ORANGE);

    private void onSelectionChanged(Shape oldS, Shape newS) {
        if (oldS != null) {
            oldS.rotateProperty().unbind();
            oldS.scaleXProperty().unbind();
            oldS.scaleYProperty().unbind();
            oldS.opacityProperty().unbind();
            oldS.getStrokeDashArray().clear();
            oldS.setEffect(Boolean.TRUE.equals(oldS.getProperties().get("blur")) ? new GaussianBlur(6) : null);
        }
        if (newS != null) {
            // Đưa slider về giá trị hiện tại của shape TRƯỚC khi bind
            slRotate.setValue(newS.getRotate());
            slScale.setValue(newS.getScaleX());
            slOpacity.setValue(newS.getOpacity());
            chkBlur.setSelected(Boolean.TRUE.equals(newS.getProperties().get("blur")));
            newS.rotateProperty().bind(slRotate.valueProperty());
            newS.scaleXProperty().bind(slScale.valueProperty());
            newS.scaleYProperty().bind(slScale.valueProperty());
            newS.opacityProperty().bind(slOpacity.valueProperty());
            newS.getStrokeDashArray().setAll(8.0, 4.0);
            newS.setEffect(selectionEffect);
        }
        updateStatus(0, 0);
    }

    {   // instance initializer: blur lưu vào properties của node, effect hiển thị khi bỏ chọn
        chkBlur.selectedProperty().addListener((o, a, on) -> {
            if (selected.get() != null) selected.get().getProperties().put("blur", on);
        });
    }

    // TODO 10: ContextMenu cho từng shape
    private void installContextMenu(Shape s) {
        MenuItem front = new MenuItem("Đưa lên trên");
        MenuItem back = new MenuItem("Đưa xuống dưới");
        MenuItem dup = new MenuItem("Nhân bản");
        MenuItem del = new MenuItem("Xóa");
        front.setOnAction(e -> s.toFront());
        back.setOnAction(e -> s.toBack());
        del.setOnAction(e -> {
            drawPane.getChildren().remove(s);
            if (selected.get() == s) selected.set(null);
        });
        dup.setOnAction(e -> duplicate(s));
        ContextMenu cm = new ContextMenu(front, back, dup, del);
        s.setOnContextMenuRequested(e -> cm.show(s, e.getScreenX(), e.getScreenY()));
    }

    private void duplicate(Shape s) {
        Shape copy = switch (s) {
            case Rectangle r -> new Rectangle(r.getX(), r.getY(), r.getWidth(), r.getHeight());
            case Circle c -> new Circle(c.getCenterX(), c.getCenterY(), c.getRadius());
            case Line l -> new Line(l.getStartX(), l.getStartY(), l.getEndX(), l.getEndY());
            case Polygon p -> {
                Polygon q = new Polygon();
                q.getPoints().setAll(p.getPoints());
                yield q;
            }
            default -> null;
        };
        if (copy == null) return;
        copy.setFill(s.getFill());
        copy.setStroke(s.getStroke());
        copy.setStrokeWidth(s.getStrokeWidth());
        copy.setTranslateX(s.getTranslateX() + 20);
        copy.setTranslateY(s.getTranslateY() + 20);
        copy.setRotate(s.getRotate());
        copy.setScaleX(s.getScaleX());
        copy.setScaleY(s.getScaleY());
        installContextMenu(copy);
        drawPane.getChildren().add(copy);
    }

    // TODO 11: Animation — rotate đang bind với slider → animate SLIDER
    private void spin() {
        Timeline t = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(slRotate.valueProperty(), 0)),
                new KeyFrame(Duration.seconds(1.2), new KeyValue(slRotate.valueProperty(), 360, Interpolator.EASE_BOTH))
        );
        t.play();
    }

    private void blink() {
        Shape s = selected.get();
        if (s == null) return;
        s.opacityProperty().unbind();                         // FadeTransition cần set opacity trực tiếp
        FadeTransition ft = new FadeTransition(Duration.millis(250), s);
        ft.setFromValue(1);
        ft.setToValue(0.1);
        ft.setCycleCount(6);
        ft.setAutoReverse(true);
        ft.setOnFinished(e -> {
            if (selected.get() == s) s.opacityProperty().bind(slOpacity.valueProperty());
        });
        ft.play();
    }

    private void colorCycle() {
        Shape s = selected.get();
        if (s == null) return;
        Paint original = s.getFill();
        Timeline t = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(s.fillProperty(), Color.RED)),
                new KeyFrame(Duration.seconds(1), new KeyValue(s.fillProperty(), Color.YELLOW)),
                new KeyFrame(Duration.seconds(2), new KeyValue(s.fillProperty(), Color.LIMEGREEN))
        );
        t.setCycleCount(2);
        t.setAutoReverse(true);
        t.setOnFinished(e -> s.setFill(original));
        t.play();
    }

    private void updateStatus(double x, double y) {
        Shape s = selected.get();
        lblStatus.setText("  Chuột: (%.0f, %.0f) | Số hình: %d | Đang chọn: %s".formatted(
                x, y, drawPane.getChildren().size(), s == null ? "—" : s.getClass().getSimpleName()));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
