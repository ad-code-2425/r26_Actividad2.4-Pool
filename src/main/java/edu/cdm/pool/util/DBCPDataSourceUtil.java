package edu.cdm.pool.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

public class DBCPDataSourceUtil {



    private static final String DB_CONFIG_FILE = Paths.get("db.properties")
            .toString();
    // Las claves del fichero Properties:
    private static final String DB_DRIVER_CLASS = "_DB_DRIVER_CLASS";
    private static final String DB_URL = "_DB_URL";
    private static final String DB_USERNAME = "_DB_USERNAME";
    private static final String DB_PASSWORD = "_DB_PASSWORD";

    // Los SGBD soportados
    private static final String SELECTED_SGBD = "SELECTED_SGBD";

    private static String dbType = "";

    public static DataSource getDataSource() {

        String driverClassName;
        String url;
        String username;
        String password;

        Properties props;
        try {
            props = loadDbPropertiesFromClasspath();

            dbType = props.getProperty(SELECTED_SGBD);

            driverClassName = props.getProperty(concatString(dbType, DB_DRIVER_CLASS));
            url = props.getProperty(concatString(dbType, DB_URL));
            username = props.getProperty(concatString(dbType, DB_USERNAME));
            password = props.getProperty(concatString(dbType, DB_PASSWORD));

            BasicDataSource basicDataSource = new BasicDataSource();
            basicDataSource.setDriverClassName(driverClassName);
            basicDataSource.setUrl(url);
            basicDataSource.setUsername(username);
            basicDataSource.setPassword(password);

            return basicDataSource;

        } catch (

        IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Carga las propiedades de configuración de base de datos desde el classpath.
     * Devuelve el objeto Properties si se cargan correctamente, o lanza IOException
     * si el recurso no se encuentra o hay problemas al leerlo.
     *
     * @return Properties con la configuración de la base de datos
     * @throws IOException si no se encuentra el recurso o falla la lectura
     */
    private static Properties loadDbPropertiesFromClasspath() throws IOException {
        Properties props = new Properties();

        String resource = System.getProperty(DB_CONFIG_FILE, DB_CONFIG_FILE);

        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            if (is == null) {
                throw new IOException("No se ha encontrado " + resource + " en el classpath");
            }
            props.load(is);
        }

        return props;
    }

    private static String concatString(String dbType, String propertySuffix) {
        return dbType + propertySuffix;
    }

    public static String getDbType() throws IOException {
        if (dbType == "") {
            Properties props = loadDbPropertiesFromClasspath();

            dbType = props.getProperty(SELECTED_SGBD);
            
        }
        return dbType;
    }

}
