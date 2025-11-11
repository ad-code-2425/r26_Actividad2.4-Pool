package edu.cdm.pool.dao;



import edu.cdm.pool.model.Departamento;
import edu.cdm.pool.util.DBCPDataSourceUtil;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO específico para la entidad Departamento para operaciones CRUD sobre la tabla DEPT usando el DataSource de DBCPDataSourceUtil.
 */
public class DepartamentoDAO {

    private final DataSource ds;

    public DepartamentoDAO() {
        this.ds = DBCPDataSourceUtil.getDataSource();
    }

    // constructor para inyección en tests si se necesita
    public DepartamentoDAO(DataSource ds) {
        this.ds = ds;
    }

    public List<Departamento> findAll() throws SQLException {
        List<Departamento> list = new ArrayList<>();
        String sql = "SELECT DEPTNO, DNAME, LOC FROM DEPT";
        try (Connection c = ds.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Departamento(rs.getInt("DEPTNO"), rs.getString("DNAME"), rs.getString("LOC")));
            }
        }
        return list;
    }

    public Optional<Integer> insert(Departamento dept) throws SQLException {
        String sql = "INSERT INTO DEPT(DNAME, LOC) VALUES(?, ?)";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dept.getDeptName());
            ps.setString(2, dept.getLoc());
            int affected = ps.executeUpdate();
            if (affected == 0) return Optional.empty();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return Optional.of(keys.getInt(1));
            }
        }
        return Optional.empty();
    }

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

    public int delete(int deptNo) throws SQLException {
        String sql = "DELETE FROM DEPT WHERE DEPTNO = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, deptNo);
            return ps.executeUpdate();
        }
    }
}