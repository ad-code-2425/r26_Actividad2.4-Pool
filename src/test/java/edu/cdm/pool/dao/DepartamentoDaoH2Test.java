package edu.cdm.pool.dao;

import edu.cdm.pool.model.Departamento;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests rápidos de integración usando H2 en memoria para no tocar la BD real.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DepartamentoDAOH2Test {

    private DataSource ds;
    private DepartamentoDAO dao;

    @BeforeAll
    void initDataSource() {
        JdbcDataSource h2 = new JdbcDataSource();
        h2.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        h2.setUser("sa");
        h2.setPassword("");
        this.ds = h2;
    }

    @BeforeAll
    void setupSchema() throws Exception {
        dao = new DepartamentoDAO(ds);
        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
            s.execute("DROP TABLE IF EXISTS EMP");
            s.execute("DROP TABLE IF EXISTS DEPT");

            s.execute(
                    "CREATE TABLE DEPT (" +
                            "DEPTNO INT AUTO_INCREMENT PRIMARY KEY, " +
                            "DNAME VARCHAR(100), " +
                            "LOC VARCHAR(100)" +
                            ")");

            s.execute(
                    "CREATE TABLE EMP (" +
                            "EMPNO INT AUTO_INCREMENT PRIMARY KEY, " +
                            "ENAME VARCHAR(100), " +
                            "JOB VARCHAR(100), " +
                            "MGR INT, " +
                            "HIREDATE DATE, " +
                            "SAL DECIMAL(15,2), " +
                            "COMM DECIMAL(15,2), " +
                            "DEPTNO INT, " +
                            "FOREIGN KEY (DEPTNO) REFERENCES DEPT(DEPTNO)" +
                            ")");

            s.execute("INSERT INTO DEPT(DNAME, LOC) VALUES('SALES','NEW YORK')");
            s.execute("INSERT INTO DEPT(DNAME, LOC) VALUES('RESEARCH','DALLAS')");
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
        Integer key = dao.insert(d);
        assertNotNull(key, "Debe devolverse la clave generada");
        List<Departamento> all = dao.findAll();
        assertEquals(3, all.size());

        // comprobación de que existe un departamento con el mismo nombre con el que ha
        // sido actualizado y mismo id que antes de ser actualizado
        boolean encontrado = false;
        for (Departamento dep : all) {
            if ("OPERATIONS".equals(dep.getDeptName()) && "BOSTON".equals(dep.getLoc())) {
                encontrado = true;
                break;
            }
        }
        assertTrue(encontrado);
    }

    @Test
    void testReadExisting() throws Exception {
        // insertar y leer
        Departamento d = new Departamento(null, "QUALITY", "MADRID");
        Integer key = dao.insert(d);
        assertNotNull(key, "Clave generada no debe ser null");

        Departamento leido = dao.read(key);
        assertNotNull(leido, "El departamento leído no debe ser null");
        assertEquals(key.intValue(), leido.getDeptno().intValue());
        assertEquals("QUALITY", leido.getDeptName());
        assertEquals("MADRID", leido.getLoc());
    }

    @Test
    void testUpdate() throws Exception {
        List<Departamento> all = dao.findAll();
        Departamento primero = all.get(0);
        primero.setDeptName("SALES_UPDATED");
        Integer primeroId = primero.getDeptno();
        int updated = dao.update(primero);
        assertEquals(1, updated);
        List<Departamento> again = dao.findAll();

        // comprobación de que existe un departamento con el mismo nombre con el que ha
        // sido actualizado y mismo id que antes de ser actualizado
        boolean encontrado = false;
        for (Departamento dep : again) {
            if ("SALES_UPDATED".equals(dep.getDeptName()) && primeroId.equals(dep.getDeptno())) {
                encontrado = true;
                break;
            }
        }
        assertTrue(encontrado);
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

    @AfterAll
    void shutdownH2() throws Exception {
        try (Connection conn = this.ds.getConnection();
                Statement stmt = conn.createStatement()) {

            stmt.execute("SHUTDOWN");
        }
    }
}