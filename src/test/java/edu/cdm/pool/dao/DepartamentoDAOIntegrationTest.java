package edu.cdm.pool.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import edu.cdm.pool.model.Departamento;
import edu.cdm.pool.util.DBCPDataSourceUtil;

/**
 * Tests de integración que usan el DataSource proporcionado por
 * DBCPDataSourceUtil.
 * PRECAUCIÓN: estos tests ejecutan DDL (DROP/CREATE TABLE DEPT) en la BD a la
 * que apunta el DataSource.
 * 
 * JUnit crea una única instancia de la clase de prueba para todos los métodos
 * de prueba.
 * 
 * Es decir, todos los métodos @Test comparten el mismo objeto.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DepartamentoDAOIntegrationTest {

    private DataSource ds;
    private DepartamentoDAO dao;

    @BeforeAll
    void init() {
        ds = DBCPDataSourceUtil.getDataSource();
        dao = new DepartamentoDAO(ds);
    }

    /**
     * Crea las tablas y datos de prueba en MySQL para los tests.
     *
     * @param s Statement ya abierto sobre la conexión de prueba
     * @throws Exception si ocurre un error en la ejecución de los scripts
     */
    private void setupSchemaForMySql() throws Exception {

        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
            s.execute("DROP TABLE IF EXISTS EMP");
            s.execute("DROP TABLE IF EXISTS DEPT");

            s.execute("CREATE TABLE IF NOT EXISTS DEPT (\n" +
                    "    DEPTNO INT AUTO_INCREMENT PRIMARY KEY,\n" +
                    "    DNAME VARCHAR(20),\n" +
                    "    LOC VARCHAR(20)\n" +
                    ");");

            s.execute("CREATE TABLE IF NOT EXISTS EMP (\n" +
                    "    EMPNO INT AUTO_INCREMENT PRIMARY KEY,\n" +
                    "    ENAME VARCHAR(20),\n" +
                    "    JOB VARCHAR(20),\n" +
                    "    MGR INT,\n" +
                    "    HIREDATE DATE,\n" +
                    "    SAL DECIMAL(15,2),\n" +
                    "    COMM DECIMAL(15,2),\n" +
                    "    DEPTNO INT,\n" +
                    "    FOREIGN KEY (MGR) REFERENCES EMP(EMPNO),\n" +
                    "    FOREIGN KEY (DEPTNO) REFERENCES DEPT(DEPTNO)\n" +
                    ");");

            s.execute("INSERT INTO DEPT(DNAME, LOC) VALUES('SALES','NEW YORK')");
            s.execute("INSERT INTO DEPT(DNAME, LOC) VALUES('RESEARCH','DALLAS')");
        }
    }

    /**
     * Crea las tablas y datos de prueba en SQLSERVER para los tests.
     *
     * @param s Statement ya abierto sobre la conexión de prueba
     * @throws Exception si ocurre un error en la ejecución de los scripts
     */
    private void setupSchemaForSQLServer() throws Exception {

        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {

            // Eliminar tablas si existen (orden seguro: primero tablas que dependen de
            // otras)
            s.executeUpdate("IF OBJECT_ID('dbo.EMP','U') IS NOT NULL DROP TABLE dbo.EMP");
            s.executeUpdate("IF OBJECT_ID('dbo.DEPT','U') IS NOT NULL DROP TABLE dbo.DEPT");

            // Crear DEPT
            s.executeUpdate(
                    "IF OBJECT_ID('dbo.DEPT','U') IS NULL " +
                            "BEGIN " +
                            "CREATE TABLE dbo.DEPT (" +
                            "  DEPTNO INT IDENTITY(1,1) PRIMARY KEY," +
                            "  DNAME VARCHAR(20)," +
                            "  LOC VARCHAR(20)" +
                            "); " +
                            "END");

            // Crear EMP (self-referencial y referencia a DEPT)
            s.executeUpdate(
                    "IF OBJECT_ID('dbo.EMP','U') IS NULL " +
                            "BEGIN " +
                            "CREATE TABLE dbo.EMP (" +
                            "  EMPNO INT IDENTITY(1,1) PRIMARY KEY," +
                            "  ENAME VARCHAR(20)," +
                            "  JOB VARCHAR(20)," +
                            "  MGR INT NULL," +
                            "  HIREDATE DATE NULL," +
                            "  SAL DECIMAL(15,2) NULL," +
                            "  COMM DECIMAL(15,2) NULL," +
                            "  DEPTNO INT NULL," +
                            "  CONSTRAINT FK_EMP_MGR FOREIGN KEY (MGR) REFERENCES dbo.EMP(EMPNO)," +
                            "  CONSTRAINT FK_EMP_DEPT FOREIGN KEY (DEPTNO) REFERENCES dbo.DEPT(DEPTNO)" +
                            "); " +
                            "END");

            s.execute("INSERT INTO DEPT(DNAME, LOC) VALUES('SALES','NEW YORK')");
            s.execute("INSERT INTO DEPT(DNAME, LOC) VALUES('RESEARCH','DALLAS')");
        }
    }

    @BeforeEach
    void setupSchema() throws Exception {
        String dbtype = DBCPDataSourceUtil.getDbType();
        switch (dbtype) {
            case "MYSQL":
                setupSchemaForMySql();
                break;
            case "SQLSERVER":
                setupSchemaForSQLServer();
                break;
            default:
                throw new UnsupportedOperationException("El tipo " + dbtype + " no se reconoce como tipo soportado");

        }

    }

    @Test
    void testFindAll() throws Exception {
        List<Departamento> all = dao.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testInsertAndGeneratedKey() throws Exception {
        Departamento d = new Departamento(null, "OPERATIONS", "BOSTON");
        Optional<Integer> key = dao.insert(d);
        assertTrue(key.isPresent(), "Debe devolverse la clave generada");
        List<Departamento> all = dao.findAll();
        assertEquals(3, all.size());
    }

    @Test
    void testUpdate() throws Exception {
        List<Departamento> all = dao.findAll();
        Departamento primero = all.get(0);
        primero.setDeptName("SALES_UPDATED");
        int updated = dao.update(primero);
        assertEquals(1, updated);
        List<Departamento> again = dao.findAll();
        assertTrue(again.stream().anyMatch(dep -> "SALES_UPDATED".equals(dep.getDeptName())));
    }

    @Test
    void testDelete() throws Exception {
        List<Departamento> all = dao.findAll();
        int id = all.get(0).getDeptno();
        int deleted = dao.delete(id);
        assertEquals(1, deleted);
        assertEquals(1, dao.findAll().size());
    }

    @Test
    void testDeleteNonExisting() throws Exception {
        int deleted = dao.delete(999999);
        assertEquals(0, deleted);
    }
}
