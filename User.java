package login;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

// FALHA Nomenclatura pouco descritiva. O ideal seria AutenticacaoService ou UsuarioDAO.
public class User {
    
    public Connection conectarBD() {
        Connection conn = null;
        try {
            // FALHA Class.forName é depreciado/desnecessário no JDBC moderno.
            // O nome "Driver.Manager" na imagem também contém erro de digitação da biblioteca.
            Class.forName("com.mysql.Driver.Manager").newInstance(); 
            String url = "jdbc:mysql://127.0.0.1/test?user=lopes&password=123";
            conn = DriverManager.getConnection(url);
        } catch (Exception e) { 
            // FALHA Swallowing Exception: O erro é engolido e a conexão retorna null silenciosamente.
        }
        return conn;
    }

    // FALHA Variáveis globais públicas quebram o encapsulamento e geram falhas de concorrência.
    public String nome = "";
    public boolean result = false;

    public boolean verificarUsuario(String login, String senha) {
        String sql = "";
        Connection conn = conectarBD(); // FALHA Se der erro no método acima, 'conn' será null.
        
        // INSTRUÇÃO SQL
        // FALHA Montagem manual de query com concatenação de Strings.
        sql += "select nome from usuarios ";
        sql += "where login = " + "'" + login + "'"; 
        sql += " and senha = " + "'" + senha + "';"; // FALHA Risco altíssimo de SQL Injection.

        try {
            // FALHA Risco de NullPointerException. Não há validação se 'conn != null'.
            Statement st = conn.createStatement(); 
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                result = true;
                nome = rs.getString("nome");
            }
        } catch (Exception e) { 
            // FALHA Engolindo exceção novamente. 
            // FALHA Resource Leak: Nenhum dos recursos (conn, st, rs) foi fechado.
        }
        return result; 
    }
}
