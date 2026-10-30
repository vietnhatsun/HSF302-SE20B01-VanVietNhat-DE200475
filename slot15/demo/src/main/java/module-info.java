module fu.de200475.demo {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens fu.de200475.demo to javafx.fxml;
    exports fu.de200475.demo;

    exports com.fu.ch08.common;
    exports com.fu.ch08.b1;
    exports com.fu.ch08.b2;
    exports com.fu.ch08.t1;
    opens com.fu.ch08.t1 to javafx.fxml, javafx.base;
    exports com.fu.ch08.t2;
    exports com.fu.ch08.t3;
    exports com.fu.ch08.t4;
    opens com.fu.ch08.t4 to javafx.fxml, javafx.base;
}
