package edu.ualberta.med.biobank;

import java.util.List;

import org.hibernate.cfg.Configuration;
import org.hibernate.tool.EnversSchemaGenerator;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaExport.Type;
import org.hibernate.tool.hbm2ddl.Target;

public class OutputSchema {

    public static void main(String[] args) {
        Configuration config = new Configuration();

        // config.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5InnoDBDialect");
        config.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        config.setProperty("hibernate.show_sql", "true");
        config.setProperty("hibernate.hbm2ddl.auto", "create-drop");

        config.addPackage("edu.ualberta.med.biobank.model");
        List<Class<?>> modelClasses = FindUtils.getClasses("edu.ualberta.med.biobank.model");
        for (Class<?> modelClass : modelClasses) {
            config.addAnnotatedClass(modelClass);
        }

        SchemaExport schemaExport = new EnversSchemaGenerator(config).export();
        schemaExport.setDelimiter(";");
        schemaExport.setOutputFile("src/main/resources/h2-schema.sql");
        schemaExport.execute(Target.NONE, Type.CREATE);
        // schemaExport.create(true, false);
    }
}