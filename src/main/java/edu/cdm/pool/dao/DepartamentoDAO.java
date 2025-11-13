package edu.cdm.pool.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import edu.cdm.pool.model.Departamento;
import edu.cdm.pool.util.DBCPDataSourceUtil;

/**
 * DAO para operaciones CRUD sobre la tabla DEPT.
 * Los métodos devuelven los tipos "normales" (entidad, Integer para clave generada, int para conteos).
 * Si no existe un registro, read devuelve null; si no se genera clave en insert devuelve null.
 */
public class DepartamentoDAO {

  
    private final DataSource ds;

    public DepartamentoDAO() {
        this.ds = DBCPDataSourceUtil.getDataSource();
    }

    // Constructor para inyección en tests
    public DepartamentoDAO(DataSource ds) {
        this.ds = ds;
    }

    public List<Departamento> findAll() throws SQLException {
        String sql = "SELECT DEPTNO, DNAME, LOC FROM DEPT";
        List<Departamento> resultados = new ArrayList<>();

        try (Connection c = ds.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {

            while (rs.next()) {
                resultados.add(new Departamento(
                        rs.getInt("DEPTNO"),
                        rs.getString("DNAME"),
                        rs.getString("LOC")
                ));
            }
        }
        return resultados;
    }

    /**
     * Inserta un departamento. Devuelve la clave generada (Integer) o null si no se obtiene.
     */
    public Integer insert(Departamento dept) throws SQLException {
        String sql = "INSERT INTO DEPT(DNAME, LOC) VALUES(?, ?)";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, dept.getDeptName());
            ps.setString(2, dept.getLoc());

            int affected = ps.executeUpdate();

            if (affected == 0) {
                return null;
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return null;
    }

    /**
     * Lee un departamento por su id. Devuelve la entidad o null si no existe.
     */
    public Departamento read(int deptNo) throws SQLException {
        String sql = "SELECT DEPTNO, DNAME, LOC FROM DEPT WHERE DEPTNO = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, deptNo);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return new Departamento(
                            rs.getInt("DEPTNO"),
                            rs.getString("DNAME"),
                            rs.getString("LOC")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Actualiza un departamento. Devuelve el número de filas afectadas.
     */
    public int update(Departamento dept) throws SQLException {
        String sql = "UPDATE DEPT SET DNAME = ?, LOC = ? WHERE DEPTNO = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, dept.getDeptName());
            ps.setString(2, dept.getLoc());
            ps.setInt(3, dept.getDeptno());

            return ps.executeUpdate();
        }
    }

    /**
     * Elimina un departamento por id. Devuelve el número de filas afectadas.
     */
    public int delete(int deptNo) throws SQLException {
        String sql = "DELETE FROM DEPT WHERE DEPTNO = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, deptNo);
            return ps.executeUpdate();
            
        }
    }
}