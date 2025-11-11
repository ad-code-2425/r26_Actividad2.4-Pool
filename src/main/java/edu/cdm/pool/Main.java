package edu.cdm.pool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import edu.cdm.pool.model.Departamento;
import edu.cdm.pool.util.DBCPDataSourceUtil;

/**
 *
 * @author maria
 */
public class Main {

    final static String SEPARATOR = "\t\t\t\t";

    public static void main(String[] args) {
        consultarDepts();
        consultarEmpleadosRangoSalarial(1000.50f, 2000.50f);

        //Departamento operacionesDept = new Departamento(null, "OPERATIONS", "BOSTON");
        // insertarDepartamento(operacionesDept);
        // consultarDepts();

        // Cambiar el deptno en función del que se quiera eliminar
        // borrarDept(45);
        // consultarDepts();

        // operacionesDept.setDeptName("OPERACIONES 2");
        // operacionesDept.setDeptno(44);
        // actualizarDept(operacionesDept);
        // consultarDepts();
    }

    private static void consultarDepts() {
        DataSource ds = DBCPDataSourceUtil.getDataSource();

        try (
                Connection conexion = ds.getConnection();
                Statement sentencia = conexion.createStatement();
                ResultSet result = sentencia.executeQuery("SELECT * FROM DEPT");) {

            int columnas = result.getMetaData().getColumnCount();
            for (int i = 1; i <= columnas; i++) {
                System.out.print(result.getMetaData().getColumnName(i) + SEPARATOR);
            }

            System.out.println("");
            System.out.println(
                    "--------------------------------------------------------------------------------------------------------------------");
            while (result.next()) {
                System.out.println(result.getInt(1) + SEPARATOR + result.getString(2) + SEPARATOR + result.getString(3)
                        + SEPARATOR);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            System.err.println("Ha ocurrido una excepción: " + ex.getMessage());

        }
    }

    private static void consultarEmpleadosRangoSalarial(float minSalario, float maxSalario) {

        DataSource ds = DBCPDataSourceUtil.getDataSource();

        try (
                Connection conexion = ds.getConnection();
                PreparedStatement pstmt = conexion.prepareStatement(
                        "SELECT ENAME, SAL FROM EMP WHERE SAL >= ? AND SAL <=? ORDER BY SAL DESC");) {

            pstmt.setFloat(1, minSalario);
            pstmt.setFloat(2, maxSalario);

            ResultSet result = pstmt.executeQuery();

            int columnas = result.getMetaData().getColumnCount();
            for (int i = 1; i <= columnas; i++) {
                System.out.print(result.getMetaData().getColumnName(i) + SEPARATOR);
            }

            System.out.println("");
            System.out.println(
                    "--------------------------------------------------------------------------------------------------------------------");
            while (result.next()) {
                System.out.println(result.getString(1) + SEPARATOR + result.getFloat(2));
            }
            System.out.println(
                    "--------------------------------------------------------------------------------------------------------------------");
            System.out.println(
                    "--------------------------------------------------------------------------------------------------------------------");

        } catch (SQLException ex) {
            ex.printStackTrace();
            System.err.println("Ha ocurrido una excepción: " + ex.getMessage());

        }
    }

    private static void borrarDept(int deptNo) {
        DataSource ds = DBCPDataSourceUtil.getDataSource();

        try (
                Connection conexion = ds.getConnection();
                PreparedStatement pstmt = conexion.prepareStatement("DELETE FROM dept WHERE DEPTNO=?");) {

            pstmt.setInt(1, deptNo);

            int result = pstmt.executeUpdate();

            // Devolverá 0 para las sentencias SQL que no devuelven nada o el número de
            // filas afectadas
            System.out.println("");
            System.out.println(
                    "--------------------------------------------------------------------------------------------------------------------");

            System.out.println("El número de filas afectadas  es: " + result);
            System.out.println(
                    "--------------------------------------------------------------------------------------------------------------------");

            System.out.println(
                    "--------------------------------------------------------------------------------------------------------------------");

        } catch (SQLException ex) {
            ex.printStackTrace();
            System.err.println("Ha ocurrido una excepción: " + ex.getMessage());

        }
    }



    private static void insertarDepartamento(Departamento departamento) {
        DataSource ds = DBCPDataSourceUtil.getDataSource();

        try (
                Connection conexion = ds.getConnection();
                PreparedStatement pstmt = conexion.prepareStatement(
                        "INSERT INTO DEPT( DNAME,  LOC) VALUES( ?, ?);", Statement.RETURN_GENERATED_KEYS);) {

            pstmt.setString(1, departamento.getDeptName());
            pstmt.setString(2, departamento.getLoc());

            // Devolverá 0 para las sentencias SQL que no devuelven nada o el número de
            // filas afectadas
            int result = pstmt.executeUpdate();
            System.out.println("");
            System.out.println(
                    "--------------------------------------------------------------------------------------------------------------------");

            System.out.println("El número de filas afectadas es: " + result);

            ResultSet clavesResultado = pstmt.getGeneratedKeys();

            while (clavesResultado.next()) {
                System.out.println("La clave asignada al nuevo registro es: " + clavesResultado.getInt(1));
            }
            System.out.println(
                    "--------------------------------------------------------------------------------------------------------------------");

            System.out.println(
                    "--------------------------------------------------------------------------------------------------------------------");

        } catch (SQLException ex) {
            ex.printStackTrace();
            System.err.println("Ha ocurrido una excepción: " + ex.getMessage());

        }
    }

    private static void actualizarDept(Departamento departamento) {
        DataSource ds = DBCPDataSourceUtil.getDataSource();

        try (
                Connection conexion = ds.getConnection();
                PreparedStatement pstmt = conexion.prepareStatement(
                        "UPDATE DEPT  SET DNAME=?,  LOC=? WHERE DEPTNO = ?")) {

            pstmt.setString(1, departamento.getDeptName());
            pstmt.setString(2, departamento.getLoc());
            pstmt.setInt(3, departamento.getDeptno());

            int result = pstmt.executeUpdate();

            // Devolverá 0 para las sentencias SQL que no devuelven nada o el número de
            // filas afectadas
            System.out.println("");
            System.out.println(
                    "--------------------------------------------------------------------------------------------------------------------");

            System.out.println("El número de filas afectadas es: " + result);
            System.out.println(
                    "--------------------------------------------------------------------------------------------------------------------");
            System.out.println(
                    "--------------------------------------------------------------------------------------------------------------------");

        } catch (SQLException ex) {
            ex.printStackTrace();
            System.err.println("Ha ocurrido una excepción: " + ex.getMessage());

        }
    }
}
