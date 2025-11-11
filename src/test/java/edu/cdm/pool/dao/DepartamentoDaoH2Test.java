package edu.cdm.pool.dao;

import edu.cdm.pool.model.Departamento;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests rápidos de integración usando H2 en memoria para no tocar la BD real.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DepartamentoDaoH2Test {

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

    @BeforeEach
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
                ")"
            );

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
                ")"
            );

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
        Integer primeroId = primero.getDeptno();
        int updated = dao.update(primero);
        assertEquals(1, updated);
        List<Departamento> again = dao.findAll();
        assertTrue(again.stream().anyMatch(dep -> "SALES_UPDATED".equals(dep.getDeptName()) && primeroId.equals(dep.getDeptno())));
      
    }

    @Test
    void testDelete() throws Exception {
        List<Departamento> all = dao.findAll();
        int id = all.get(0).getDeptno();
        int deleted = dao.delete(id);
        assertEquals(1, deleted);
        assertEquals(1, dao.findAll().size());
    }
}