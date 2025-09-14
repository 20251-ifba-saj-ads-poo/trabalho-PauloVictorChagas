module br.edu.ifba.saj.fwads {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires jakarta.validation;

    opens br.edu.ifba.saj.fwads.controller to javafx.fxml, org.hibernate.orm.core;
    opens br.edu.ifba.saj.fwads.model to javafx.base, javafx.fxml, org.hibernate.orm.core;

    exports br.edu.ifba.saj.fwads;
    exports br.edu.ifba.saj.fwads.model;
}
